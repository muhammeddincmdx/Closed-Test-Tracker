package com.mdstudio.closedtesttracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mdstudio.closedtesttracker.data.AppDatabase
import com.mdstudio.closedtesttracker.data.TrackedApp
import kotlinx.coroutines.runBlocking

class WidgetAppGridService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = WidgetAppFactory(applicationContext)
}

private class WidgetAppFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var items: List<TrackedApp> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        items = runBlocking {
            AppDatabase.get(context).appDao().getAll().filterNot { it.isArchived }
        }
    }

    override fun onDestroy() = Unit
    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        val item = items.getOrNull(position) ?: return null
        val icon = runCatching { drawableBitmap(context.packageManager.getApplicationIcon(item.packageName)) }.getOrNull()
        return RemoteViews(context.packageName, R.layout.widget_app_item).apply {
            if (icon != null) setImageViewBitmap(R.id.widgetAppIcon, icon)
            setTextViewText(R.id.widgetAppDay, "${SeriesCalculator.currentDay(item)}/14")
            setOnClickFillInIntent(
                R.id.widgetAppIcon,
                Intent().putExtra(ClosedTestWidgetProvider.EXTRA_PACKAGE_NAME, item.packageName)
            )
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items.getOrNull(position)?.packageName?.hashCode()?.toLong() ?: position.toLong()
    override fun hasStableIds(): Boolean = true

    private fun drawableBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
        if (drawable is android.graphics.drawable.BitmapDrawable) return drawable.bitmap
        val size = 96
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
            drawable.setBounds(0, 0, size, size)
            drawable.draw(Canvas(bitmap))
        }
    }
}
