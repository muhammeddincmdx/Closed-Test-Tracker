package com.mdstudio.closedtesttracker

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.mdstudio.closedtesttracker.data.AppDatabase
import com.mdstudio.closedtesttracker.data.TrackedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.max

class LiveUsageOverlayService : Service() {
    private companion object {
        private const val MAX_OVERLAY_COUNT = 2
        private const val EVENT_LOOKBACK_MILLIS = 60L * 60L * 1000L
        private const val POLL_DELAY_MILLIS = 1_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var monitorJob: Job? = null
    private var trackedApps: List<TrackedApp> = emptyList()
    private val overlayViews = mutableListOf<TextView>()
    private val layoutParams = mutableListOf<WindowManager.LayoutParams>()
    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private var lastScreenWidth = 0
    private var lastScreenHeight = 0
    private var lastOrientation = Configuration.ORIENTATION_UNDEFINED

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!canDrawOverlay()) {
            stopSelf()
            return
        }
        createOverlays()
        scope.launch {
            AppDatabase.get(this@LiveUsageOverlayService).appDao().observeAll().collectLatest { items ->
                trackedApps = items.filter { !it.isArchived && it.completedAtMillis == null }
            }
        }
        monitorJob = scope.launch {
            while (true) {
                updateOverlayForForegroundApp()
                delay(POLL_DELAY_MILLIS)
            }
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        removeOverlay()
        scope.cancel()
        super.onDestroy()
    }

    private fun canDrawOverlay(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun createOverlays() {
        if (overlayViews.isNotEmpty()) return
        repeat(MAX_OVERLAY_COUNT) { index ->
            @SuppressLint("AppCompatCustomView")
            val bubble = object : TextView(this) {
                override fun performClick(): Boolean {
                    super.performClick()
                    return true
                }
            }.apply {
                text = initialOverlayText()
                textSize = 12f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                minWidth = 180
                setPadding(22, 10, 22, 10)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 42f
                    setColor(Color.argb(224, 20, 20, 20))
                    setStroke(2, Color.argb(90, 255, 255, 255))
                }
                elevation = 14f
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                x = 0
                y = 0
            }
            var startX = 0
            var startY = 0
            var touchX = 0f
            var touchY = 0f
            bubble.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - touchX).toInt()
                        params.y = startY + (event.rawY - touchY).toInt()
                        runCatching { windowManager.updateViewLayout(bubble, params) }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        bubble.performClick()
                        true
                    }
                    else -> false
                }
            }
            overlayViews += bubble
            layoutParams += params
            runCatching { windowManager.addView(bubble, params) }
            bubble.visibility = View.GONE
        }
        positionOverlays(force = true)
    }

    private suspend fun updateOverlayForForegroundApp() {
        if (!canDrawOverlay() || !UsageReader.hasUsageAccess(this)) {
            hideOverlay()
            return
        }
        positionOverlays()
        val foregroundPackages = withContext(Dispatchers.Default) { currentForegroundPackages() }
        val activeApps = foregroundPackages
            .mapNotNull { foregroundPackage -> trackedApps.firstOrNull { it.packageName == foregroundPackage } }
            .distinctBy { it.packageName }
            .take(overlayViews.size)
        if (activeApps.isEmpty()) {
            hideOverlay()
            return
        }
        activeApps.forEachIndexed { index, app ->
            val minutes = withContext(Dispatchers.Default) {
                liveTodayMinutes(app.packageName)
            }
            overlayViews.getOrNull(index)?.apply {
                text = getString(R.string.live_usage_overlay_text, app.appLabel, minutes)
                if (visibility != View.VISIBLE) visibility = View.VISIBLE
            }
        }
        for (index in activeApps.size until overlayViews.size) {
            overlayViews[index].visibility = View.GONE
        }
    }

    private fun hideOverlay() {
        overlayViews.forEach { it.visibility = View.GONE }
    }

    private fun positionOverlays(force: Boolean = false) {
        if (overlayViews.isEmpty() || overlayViews.size != layoutParams.size) return
        val metrics = resources.displayMetrics
        val orientation = resources.configuration.orientation
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val changed = force ||
            screenWidth != lastScreenWidth ||
            screenHeight != lastScreenHeight ||
            orientation != lastOrientation
        if (!changed) return

        lastScreenWidth = screenWidth
        lastScreenHeight = screenHeight
        lastOrientation = orientation

        val topOffset = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 44 else 92
        val secondOffset = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            max(topOffset + 74, (screenHeight * 0.58f).toInt())
        } else {
            max(topOffset + 88, (screenHeight * 0.52f).toInt())
        }

        layoutParams.forEachIndexed { index, params ->
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.x = 0
            params.y = if (index == 0) topOffset else secondOffset
            overlayViews.getOrNull(index)?.let { view ->
                runCatching { windowManager.updateViewLayout(view, params) }
            }
        }
    }

    private fun removeOverlay() {
        overlayViews.forEach { view ->
            runCatching { windowManager.removeView(view) }
        }
        overlayViews.clear()
        layoutParams.clear()
    }

    private fun initialOverlayText(): String {
        return when (resources.configuration.locales[0]?.language) {
            "tr" -> "Bugün: 0 dk"
            else -> "Today: 0 min"
        }
    }

    private fun currentForegroundPackages(): List<String> {
        val manager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = manager.queryEvents(now - EVENT_LOOKBACK_MILLIS, now)
        val event = UsageEvents.Event()
        val states = linkedMapOf<String, Boolean>()
        // On API 29+ an activity that loses focus but stays visible (e.g. the
        // unfocused pane in split-screen) emits ACTIVITY_PAUSED but not
        // ACTIVITY_STOPPED. Relying on STOPPED to hide keeps both split-screen
        // panes visible; older versions only report PAUSED/MOVE_TO_BACKGROUND so
        // there we still treat PAUSED as backgrounded.
        val usesActivityStopped = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName) continue
            if (trackedApps.none { it.packageName == event.packageName }) continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> states[event.packageName] = true
                UsageEvents.Event.ACTIVITY_STOPPED -> states[event.packageName] = false
                UsageEvents.Event.ACTIVITY_PAUSED ->
                    if (!usesActivityStopped) states[event.packageName] = false
            }
        }
        return states
            .filterValues { isForeground -> isForeground }
            .keys
            .take(MAX_OVERLAY_COUNT)
    }

    private fun liveTodayMinutes(packageName: String): Long {
        val now = System.currentTimeMillis()
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val aggregate = UsageReader.todayUsageMinutes(this, packageName)
        val activeSession = UsageReader.usageMinutesByDayMapFromEventsForDisplay(this, packageName, start, now).values.sum()
        return max(aggregate, activeSession)
    }
}

