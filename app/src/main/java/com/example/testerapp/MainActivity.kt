package com.example.testerapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.testerapp.data.AppDatabase
import com.example.testerapp.data.TrackedApp
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SortMode { NAME, NEWEST, OLDEST }
enum class AppLanguage { TR, EN, FR, ES, ZH, HI, RU }
enum class AppTheme { FRESH, OCEAN, SUNSET }
enum class AppThemeMode { SYSTEM, LIGHT, DARK }
enum class AppScreen { HOME, SETTINGS }

private const val PREFS_NAME = "tester_settings"
private const val KEY_LANGUAGE = "language"
private const val KEY_THEME = "theme"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_AUTO_TOUR = "auto_tour"
private const val KEY_PLAY_PUBLISHER_PREFIX = "play_publisher_"
private const val SUPPORT_MAIL = "mdstudiohelp@gmail.com"
private const val DONATION_URL = "https://www.buymeacoffee.com/mdx0"

private fun appColors(theme: AppTheme, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = Color(0xFFFFFFFF),
        secondary = Color(0xFFB7B7B7),
        tertiary = Color(0xFFE6D1B0),
        background = Color.Black,
        surface = Color(0xFF1C1C1E),
        surfaceVariant = Color(0xFF2C2C2E),
        onPrimary = Color.Black,
        onSecondary = Color(0xFF111111),
        onTertiary = Color(0xFF24190B),
        onBackground = Color(0xFFF5F5F7),
        onSurface = Color(0xFFF5F5F7),
        onSurfaceVariant = Color(0xFFB8B8BE),
        outline = Color(0xFF3A3A3C)
    )
} else {
    lightColorScheme(
        primary = Color(0xFF111111),
        secondary = Color(0xFF696969),
        tertiary = Color(0xFF9B7B55),
        background = Color(0xFFF7F7F5),
        surface = Color.White,
        surfaceVariant = Color(0xFFE9E9E6),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF141414),
        onSurface = Color(0xFF141414),
        onSurfaceVariant = Color(0xFF606064),
        outline = Color(0xFFD5D5D2)
    )
}

private fun backgroundBrush(theme: AppTheme, dark: Boolean) = if (dark) {
    Brush.verticalGradient(
        listOf(
            Color.Black,
            Color(0xFF050505),
            Color.Black
        )
    )
} else {
    Brush.verticalGradient(
        listOf(
            Color(0xFFFCFCFA),
            Color(0xFFF6F6F4),
            Color(0xFFF0F0ED)
        )
    )
}

@Composable
private fun FluidBackdrop(
    modifier: Modifier = Modifier,
    appTheme: AppTheme,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "fluid_backdrop")
    val driftA by transition.animateFloat(
        initialValue = -34f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_a"
    )
    val driftB by transition.animateFloat(
        initialValue = 28f,
        targetValue = -28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_b"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.76f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val accents = accentGradient(appTheme)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush(appTheme, darkTheme))
    ) {
        FluidBlob(
            modifier = Modifier
                .align(Alignment.TopStart)
                .graphicsLayer {
                    translationX = driftA
                    translationY = driftB
                    scaleX = pulse
                    scaleY = pulse
                },
            color = accents[0],
            size = 330
        )
        FluidBlob(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    translationX = -driftB
                    translationY = driftA
                    scaleX = 1.14f - (pulse - 0.76f)
                    scaleY = 1.14f - (pulse - 0.76f)
                },
            color = accents[1],
            size = 380
        )
        FluidBlob(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    translationX = driftB
                    translationY = -driftA
                    scaleX = 0.92f + (pulse - 0.76f)
                    scaleY = 0.92f + (pulse - 0.76f)
                },
            color = accents[2],
            size = 340
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (darkTheme) 0.00f else 0.12f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (darkTheme) 0.30f else 0.02f)
                        )
                    )
                )
        )
        content()
    }
}

@Composable
private fun FluidBlob(modifier: Modifier, color: Color, size: Int) {
    Box(
        modifier
            .size(size.dp)
            .blur(64.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        color.copy(alpha = 0.05f),
                        color.copy(alpha = 0.02f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(size.dp)
            )
    )
}

@Composable
private fun glassColor(strong: Boolean = false): Color {
    val dark = isDarkScheme()
    val base = MaterialTheme.colorScheme.surface
    val alpha = when {
        dark && strong -> 0.92f
        dark -> 0.82f
        strong -> 0.92f
        else -> 0.86f
    }
    return base.copy(alpha = alpha)
}

@Composable
private fun glassVariantColor(): Color {
    return if (isDarkScheme()) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }
}

@Composable
private fun glassBorder(): BorderStroke {
    return BorderStroke(
        1.dp,
        if (isDarkScheme()) MaterialTheme.colorScheme.outline.copy(alpha = 0.95f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.86f)
    )
}

@Composable
private fun isDarkScheme(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}

@Composable
private fun glassSheenBrush(): Brush {
    val dark = isDarkScheme()
    return Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (dark) 0.08f else 0.38f),
            Color.White.copy(alpha = if (dark) 0.02f else 0.10f),
            Color.Transparent,
            Color.Transparent
        )
    )
}

@Composable
private fun glassPanelBrush(): Brush {
    val dark = isDarkScheme()
    return Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.92f else 0.88f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dark) 0.40f else 0.28f),
            MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.82f else 0.78f)
        )
    )
}

@Composable
private fun glassItemBrush(): Brush {
    val dark = isDarkScheme()
    return Brush.linearGradient(
        listOf(
            glassVariantColor(),
            Color.White.copy(alpha = if (dark) 0.03f else 0.28f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dark) 0.52f else 0.28f)
        )
    )
}

@Composable
private fun secondaryTextColor(): Color {
    return MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDarkScheme()) 0.88f else 0.76f)
}

@Composable
private fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = secondaryTextColor(),
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = secondaryTextColor(),
    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedTrailingIconColor = secondaryTextColor(),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.76f),
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.26f else 0.18f),
    focusedContainerColor = glassVariantColor(),
    unfocusedContainerColor = glassVariantColor()
)

@Composable
private fun glassChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.26f else 0.18f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
    containerColor = glassVariantColor(),
    labelColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = glassVariantColor(),
    disabledLabelColor = secondaryTextColor()
)

@Composable
private fun glassCardColors(strong: Boolean = false) = CardDefaults.cardColors(
    containerColor = glassColor(strong = strong),
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun panelColor(strong: Boolean = false): Color {
    val dark = isDarkScheme()
    return when {
        dark && strong -> Color(0xFF1C1C1E).copy(alpha = 0.96f)
        dark -> Color(0xFF1C1C1E).copy(alpha = 0.88f)
        strong -> Color.White.copy(alpha = 0.98f)
        else -> Color.White.copy(alpha = 0.94f)
    }
}

@Composable
private fun panelBorder(): BorderStroke {
    return BorderStroke(
        1.dp,
        if (isDarkScheme()) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    )
}

@Composable
private fun canvasColor(): Color {
    return if (isDarkScheme()) {
        Color(0xFF151517).copy(alpha = 0.94f)
    } else {
        Color(0xFFF0F0EE).copy(alpha = 0.86f)
    }
}

@Composable
private fun rowSurfaceColor(): Color {
    return if (isDarkScheme()) {
        Color.White.copy(alpha = 0.045f)
    } else {
        Color.White.copy(alpha = 0.78f)
    }
}

@Composable
private fun separatorColor(): Color {
    return if (isDarkScheme()) {
        Color.White.copy(alpha = 0.075f)
    } else {
        Color.Black.copy(alpha = 0.055f)
    }
}

private fun headerColor(theme: AppTheme, dark: Boolean) = when {
    dark -> Color(0xFF1C1C1E)
    else -> Color.White
}

private fun accentGradient(theme: AppTheme) = when (theme) {
    AppTheme.FRESH -> listOf(Color(0xFFECECEC), Color(0xFFBDBDBD), Color(0xFFF1E4D0))
    AppTheme.OCEAN -> listOf(Color(0xFFECECEC), Color(0xFFCFCFCF), Color(0xFFE8D8C4))
    AppTheme.SUNSET -> listOf(Color(0xFFF1E4D0), Color(0xFFD6D6D6), Color(0xFFB8B8B8))
}

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val firstInstallTime: Long
)

private data class UsageDay(
    val index: Int,
    val label: String,
    val minutes: Long,
    val isToday: Boolean,
    val isFuture: Boolean
)

private data class DaySetupTarget(
    val packageName: String,
    val label: String,
    val initialDay: Int
)

private fun text(
    language: AppLanguage,
    tr: String,
    en: String,
    fr: String = en,
    es: String = en,
    zh: String = en,
    hi: String = en,
    ru: String = en
): String {
    return when (language) {
        AppLanguage.TR -> tr
        AppLanguage.EN -> en
        AppLanguage.FR -> fr
        AppLanguage.ES -> es
        AppLanguage.ZH -> zh
        AppLanguage.HI -> hi
        AppLanguage.RU -> ru
    }
}

private fun languageLabel(language: AppLanguage): String {
    return when (language) {
        AppLanguage.TR -> "Türkçe"
        AppLanguage.EN -> "English"
        AppLanguage.FR -> "Français"
        AppLanguage.ES -> "Español"
        AppLanguage.ZH -> "中文"
        AppLanguage.HI -> "हिन्दी"
        AppLanguage.RU -> "Русский"
    }
}

private fun languageFlag(language: AppLanguage): String {
    return when (language) {
        AppLanguage.TR -> "🇹🇷"
        AppLanguage.EN -> "🇬🇧"
        AppLanguage.FR -> "🇫🇷"
        AppLanguage.ES -> "🇪🇸"
        AppLanguage.ZH -> "🇨🇳"
        AppLanguage.HI -> "🇮🇳"
        AppLanguage.RU -> "🇷🇺"
    }
}

private fun languageDisplay(language: AppLanguage): String {
    return "${languageFlag(language)} ${languageLabel(language)}"
}

class MainActivity : ComponentActivity() {
    private val db by lazy { AppDatabase.get(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= 33) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        setContent {
            val prefs = remember { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
            var appTheme by remember {
                mutableStateOf(
                    runCatching {
                        AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.FRESH.name) ?: AppTheme.FRESH.name)
                    }.getOrDefault(AppTheme.FRESH)
                )
            }
            var themeMode by remember {
                mutableStateOf(
                    runCatching {
                        AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
                    }.getOrDefault(AppThemeMode.SYSTEM)
                )
            }
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MaterialTheme(colorScheme = appColors(appTheme, darkTheme)) {
                MainScreen(
                    appTheme = appTheme,
                    themeMode = themeMode,
                    darkTheme = darkTheme,
                    onThemeChange = {
                        appTheme = it
                        prefs.edit().putString(KEY_THEME, it.name).apply()
                    },
                    onThemeModeChange = {
                        themeMode = it
                        prefs.edit().putString(KEY_THEME_MODE, it.name).apply()
                    },
                    onOpenUsageSettings = { openUsageSettings(this) },
                    observeTrackedApps = { onData ->
                        lifecycleScope.launch {
                            db.appDao().observeAll().collectLatest(onData)
                        }
                    },
                    onTrackApp = { pkg, label, day ->
                        lifecycleScope.launch {
                            db.appDao().upsert(
                                TrackedApp(
                                    packageName = pkg,
                                    appLabel = label,
                                    startDayIndex = day.coerceIn(1, 14)
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun openUsageSettings(activity: Activity) {
    activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
}

private fun openTrackedApp(context: android.content.Context, packageName: String) {
    runCatching {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

private fun sendSupportMail(context: android.content.Context, language: AppLanguage) {
    val subject = "Closed Test Tracker support"
    val body = text(
        language,
        "Merhaba, uygulama hakkında destek almak istiyorum.",
        "Hello, I would like support about the app."
    )
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = android.net.Uri.parse("mailto:$SUPPORT_MAIL")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_MAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun openDonationPage(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(DONATION_URL)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun installedApps(pm: PackageManager): List<InstalledApp> {
    val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val launchablePackages = if (Build.VERSION.SDK_INT >= 33) {
        pm.queryIntentActivities(
            launchIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
        )
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
    }.mapNotNull { it.activityInfo?.packageName }.toSet()

    val apps: List<ApplicationInfo> = if (Build.VERSION.SDK_INT >= 33) {
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.getInstalledApplications(0)
    }

    return apps.asSequence()
        .filter { it.packageName in launchablePackages }
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 }
        .mapNotNull { info ->
            runCatching {
                val pkg = info.packageName
                val label = pm.getApplicationLabel(info).toString().ifBlank { pkg }
                val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                }
                InstalledApp(
                    packageName = pkg,
                    label = label,
                    icon = pm.getApplicationIcon(info).toBitmap(),
                    firstInstallTime = packageInfo.firstInstallTime
                )
            }.getOrNull()
        }
        .distinctBy { it.packageName }
        .toList()
}

private suspend fun fetchPlayPublisherName(packageName: String): String? = withContext(Dispatchers.IO) {
    val encodedPackage = URLEncoder.encode(packageName, "UTF-8")
    val urls = listOf(
        "https://play.google.com/store/apps/details?id=$encodedPackage&hl=tr&gl=TR",
        "https://play.google.com/store/apps/details?id=$encodedPackage&hl=en&gl=US"
    )

    for (targetUrl in urls) {
        val publisher = runCatching {
            val connection = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android) ClosedTestTracker/1.0")
                setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7")
            }
            try {
                if (connection.responseCode !in 200..299) return@runCatching null
                val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                parsePlayPublisher(html)
            } finally {
                connection.disconnect()
            }
        }.getOrNull()

        if (!publisher.isNullOrBlank()) return@withContext publisher
    }
    null
}

private fun parsePlayPublisher(html: String): String? {
    val patterns = listOf(
        Regex("""<a href="/store/apps/developer[^"]*">\s*<span>(.*?)</span>\s*</a>""", RegexOption.DOT_MATCHES_ALL),
        Regex("""/store/apps/developer\?id=[^"]+">\s*<span>(.*?)</span>""", RegexOption.DOT_MATCHES_ALL)
    )

    return patterns.asSequence()
        .mapNotNull { it.find(html)?.groupValues?.getOrNull(1) }
        .map(::decodeHtmlText)
        .firstOrNull { it.isNotBlank() && !it.contains("<") }
}

private fun decodeHtmlText(value: String): String {
    val withoutTags = value.replace(Regex("<[^>]+>"), "").trim()
    return if (Build.VERSION.SDK_INT >= 24) {
        Html.fromHtml(withoutTags, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(withoutTags).toString()
    }.trim()
}

private fun Drawable.toBitmap(): Bitmap {
    val width = intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(192) ?: 96
    val height = intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(192) ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appTheme: AppTheme,
    themeMode: AppThemeMode,
    darkTheme: Boolean,
    onThemeChange: (AppTheme) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onOpenUsageSettings: () -> Unit,
    observeTrackedApps: ((List<TrackedApp>) -> Unit) -> Unit,
    onTrackApp: (String, String, Int) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    var language by remember {
        mutableStateOf(
            runCatching {
                AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AppLanguage.TR.name) ?: AppLanguage.TR.name)
            }.getOrDefault(AppLanguage.TR)
        )
    }
    var tracked by remember { mutableStateOf(emptyList<TrackedApp>()) }
    var showPicker by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var autoTourEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_AUTO_TOUR, false)) }
    var daySetupTarget by remember { mutableStateOf<DaySetupTarget?>(null) }
    var expandedPackage by remember { mutableStateOf<String?>(null) }
    var usageAccess by remember { mutableStateOf(UsageReader.hasUsageAccess(context)) }
    var refreshTick by remember { mutableIntStateOf(0) }
    val apps = remember { installedApps(context.packageManager) }
    val playPublishers = remember { mutableStateMapOf<String, String>() }
    val playPublisherRequested = remember { mutableStateMapOf<String, Boolean>() }

    suspend fun ensurePlayPublisher(packageName: String) {
        if (playPublisherRequested[packageName] == true) return
        playPublisherRequested[packageName] = true

        val cacheKey = "$KEY_PLAY_PUBLISHER_PREFIX$packageName"
        val cached = prefs.getString(cacheKey, null)
        if (!cached.isNullOrBlank()) {
            playPublishers[packageName] = cached
            return
        }

        val publisher = fetchPlayPublisherName(packageName)
        if (!publisher.isNullOrBlank()) {
            playPublishers[packageName] = publisher
            prefs.edit().putString(cacheKey, publisher).apply()
        }
    }

    LaunchedEffect(Unit) { observeTrackedApps { tracked = it } }
    LaunchedEffect(refreshTick) { usageAccess = UsageReader.hasUsageAccess(context) }

    FluidBackdrop(
        modifier = Modifier.fillMaxSize(),
        appTheme = appTheme,
        darkTheme = darkTheme
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                title = {
                    Surface(
                        color = glassColor(strong = true),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                        border = glassBorder()
                    ) {
                        Text(
                            if (screen == AppScreen.SETTINGS) {
                                text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки")
                            } else {
                                "Closed Test Tracker"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (screen == AppScreen.SETTINGS) {
                        IconButton(onClick = { screen = AppScreen.HOME }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = text(language, "Geri", "Back"))
                        }
                    }
                },
                actions = {
                    if (screen == AppScreen.HOME) {
                        Surface(
                            color = glassColor(strong = true),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(24.dp),
                            border = glassBorder(),
                            modifier = Modifier.padding(end = 8.dp),
                            shadowElevation = 6.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showPicker = true }) {
                                    Icon(Icons.Rounded.Add, contentDescription = text(language, "Uygulama ekle", "Add app"))
                                }
                                IconButton(onClick = { screen = AppScreen.SETTINGS }) {
                                    Icon(Icons.Rounded.Settings, contentDescription = text(language, "Ayarlar", "Settings"))
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
                )
            }
        ) { p ->
            if (screen == AppScreen.SETTINGS) {
                SettingsPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(p)
                        .padding(10.dp),
                    language = language,
                    appTheme = appTheme,
                    themeMode = themeMode,
                    darkTheme = darkTheme,
                    autoTourEnabled = autoTourEnabled,
                    onLanguageChange = {
                        language = it
                        prefs.edit().putString(KEY_LANGUAGE, it.name).apply()
                    },
                    onThemeChange = onThemeChange,
                    onThemeModeChange = onThemeModeChange,
                    onAutoTourChange = {
                        autoTourEnabled = it
                        prefs.edit().putBoolean(KEY_AUTO_TOUR, it).apply()
                    },
                    onSendMail = { sendSupportMail(context, language) },
                    onDonate = { openDonationPage(context) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(p)
                        .padding(10.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        DashboardHeader(
                            appTheme = appTheme,
                            darkTheme = darkTheme,
                            language = language,
                            trackedCount = tracked.size,
                            todayMinutes = tracked.sumOf {
                                if (usageAccess) UsageReader.todayUsageMinutes(context, it.packageName) else 0L
                            },
                            onRefresh = { refreshTick++ }
                        )
                    }

                    if (!usageAccess) {
                        item {
                            PermissionCard(language, onOpenUsageSettings)
                        }
                    }

                    if (tracked.isEmpty()) {
                        item {
                            EmptyState(language, onAdd = { showPicker = true })
                        }
                    } else {
                        item {
                            AppListCanvas(language = language, count = tracked.size) {
                                tracked.forEachIndexed { index, item ->
                                    val appInfo = remember(apps, item.packageName) {
                                        apps.firstOrNull { it.packageName == item.packageName }
                                    }
                                    val usageDays = remember(item, usageAccess, refreshTick) {
                                        testUsageDays(context, item, usageAccess)
                                    }
                                    LaunchedEffect(item.packageName) {
                                        ensurePlayPublisher(item.packageName)
                                    }
                                    AppUsageCard(
                                        item = item,
                                        icon = appInfo?.icon,
                                        playPublisherName = playPublishers[item.packageName],
                                        language = language,
                                        appTheme = appTheme,
                                        usageDays = usageDays,
                                        expanded = expandedPackage == item.packageName,
                                        onOpenApp = { openTrackedApp(context, item.packageName) },
                                        onClick = {
                                            expandedPackage = if (expandedPackage == item.packageName) null else item.packageName
                                        },
                                        onEditDay = {
                                            daySetupTarget = DaySetupTarget(
                                                packageName = item.packageName,
                                                label = item.appLabel,
                                                initialDay = SeriesCalculator.currentDay(item)
                                            )
                                        }
                                    )
                                    if (index != tracked.lastIndex) {
                                        CanvasDivider(Modifier.padding(start = 78.dp, end = 14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        AppPickerSheet(
            language = language,
            appTheme = appTheme,
            apps = apps,
            trackedPackageNames = tracked.map { it.packageName }.toSet(),
            playPublishers = playPublishers,
            onLoadPublisher = { ensurePlayPublisher(it) },
            onDismiss = { showPicker = false },
            onPick = { app ->
                showPicker = false
                daySetupTarget = DaySetupTarget(app.packageName, app.label, 1)
            }
        )
    }

    daySetupTarget?.let { target ->
        DaySetupDialog(
            language = language,
            target = target,
            onDismiss = { daySetupTarget = null },
            onConfirm = { day ->
                onTrackApp(target.packageName, target.label, day)
                daySetupTarget = null
            }
        )
    }
}

@Composable
private fun DashboardHeader(
    appTheme: AppTheme,
    darkTheme: Boolean,
    language: AppLanguage,
    trackedCount: Int,
    todayMinutes: Long,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text(language, "Takip özeti", "Tracking summary"),
            color = secondaryTextColor(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatTile("App", trackedCount.toString(), Modifier.weight(1f))
            StatTile(text(language, "Bugün", "Today"), "$todayMinutes dk", Modifier.weight(1f))
            Surface(
                color = glassColor(strong = true),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(22.dp),
                border = glassBorder(),
                modifier = Modifier.clickable(onClick = onRefresh)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(text(language, "Yenile", "Refresh"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.14f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(rowSurfaceColor())
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, color = secondaryTextColor())
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermissionCard(language: AppLanguage, onOpenUsageSettings: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = panelColor(strong = true), contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(26.dp),
        border = panelBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text(language, "Kullanım süresi izni kapalı", "Usage access is off"), fontWeight = FontWeight.Bold)
            Text(
                text(language, "Dakikaları göstermek için kullanım erişimi izni gerekli.", "Usage Access is required to show minutes."),
                color = secondaryTextColor()
            )
            Button(onClick = onOpenUsageSettings, shape = RoundedCornerShape(18.dp)) {
                Text(text(language, "İzni aç", "Open"))
            }
        }
    }
}

@Composable
private fun EmptyState(language: AppLanguage, onAdd: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = panelColor(strong = true), contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(26.dp),
        border = panelBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text(language, "Liste boş", "No apps yet"), fontWeight = FontWeight.Bold)
            Text(text(language, "Önce takip edilecek uygulamaları seç.", "Choose the apps to track first."), color = secondaryTextColor())
            Button(onClick = onAdd, shape = RoundedCornerShape(18.dp)) {
                Text(text(language, "Uygulama seç", "Pick app"))
            }
        }
    }
}

@Composable
private fun AppListCanvas(
    language: AppLanguage,
    count: Int,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = canvasColor(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, separatorColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text(language, "Uygulamalar", "Apps"),
                    color = secondaryTextColor(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    count.toString(),
                    color = secondaryTextColor(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            CanvasDivider(Modifier.padding(horizontal = 18.dp))
            content()
        }
    }
}

@Composable
private fun CanvasDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(separatorColor())
    )
}

@Composable
private fun AppUsageCard(
    item: TrackedApp,
    icon: Bitmap?,
    playPublisherName: String?,
    language: AppLanguage,
    appTheme: AppTheme,
    usageDays: List<UsageDay>,
    expanded: Boolean,
    onOpenApp: () -> Unit,
    onClick: () -> Unit,
    onEditDay: () -> Unit
) {
    val day = SeriesCalculator.currentDay(item)
    val today = usageDays.firstOrNull { it.isToday }?.minutes ?: 0L
    val total = usageDays.filterNot { it.isFuture }.sumOf { it.minutes }
    val max = usageDays.maxOfOrNull { it.minutes } ?: 0L
    val completed = day >= 14

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenApp)
                )
            } else {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onOpenApp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.appLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text(language, "Bugün $today dk | Toplam $total dk", "Today $today min | Total $total min"),
                    color = secondaryTextColor(),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (playPublisherName != null) {
                    Text(
                        text(language, "Yayıncı: $playPublisherName", "Publisher: $playPublisherName"),
                        color = secondaryTextColor(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                color = if (isDarkScheme()) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.045f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Text(
                    if (completed) text(language, "Tamamlandı", "Done") else "$day/14",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        LinearProgressIndicator(
            progress = { day.coerceAtMost(14) / 14f },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(99.dp)),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.74f else 0.58f),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.10f else 0.08f)
        )

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(rowSurfaceColor())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsageBars(usageDays, max)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text(language, "Test günleri", "Test days"), fontWeight = FontWeight.Bold)
                    TextButton(onClick = onEditDay) { Text(text(language, "Günü ayarla", "Set day")) }
                }
                usageDays.forEach { dayItem ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text(language, "Gün ${dayItem.index} - ${dayItem.label}", "Day ${dayItem.index} - ${dayItem.label}"))
                        Text(
                            if (dayItem.isFuture) "-" else "${dayItem.minutes} dk",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun UsageBars(days: List<UsageDay>, maxMinutes: Long) {
    val todayColor = MaterialTheme.colorScheme.tertiary
    val usedColor = MaterialTheme.colorScheme.primary
    val futureColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val barHeight = if (maxMinutes == 0L) 8 else (8 + (day.minutes * 46 / maxMinutes)).toInt()
            Box(
                modifier = Modifier.weight(1f).height(58.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                day.isToday -> todayColor
                                day.isFuture -> futureColor
                                day.minutes > 0 -> usedColor
                                else -> emptyColor
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun DaySetupDialog(
    language: AppLanguage,
    target: DaySetupTarget,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var dayText by remember(target) { mutableStateOf(target.initialDay.coerceIn(1, 14).toString()) }
    val selectedDay = dayText.toIntOrNull()?.coerceIn(1, 14) ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text(language, "Test günü", "Test day")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(target.label, fontWeight = FontWeight.Bold)
                Text(text(language, "Bugün bu uygulama testinin kaçıncı günü?", "Which test day is this app on today?"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 7, 14).forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { dayText = day.toString() },
                            label = { Text(day.toString()) },
                            shape = RoundedCornerShape(16.dp),
                            colors = glassChipColors()
                        )
                    }
                }
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { dayText = it.filter(Char::isDigit).take(2) },
                    label = { Text(text(language, "Gün (1-14)", "Day (1-14)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDay) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text(language, "Kaydet", "Save"))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text(language, "İptal", "Cancel")) } }
    )
}

@Composable
private fun SettingsPage(
    modifier: Modifier,
    language: AppLanguage,
    appTheme: AppTheme,
    themeMode: AppThemeMode,
    darkTheme: Boolean,
    autoTourEnabled: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAutoTourChange: (Boolean) -> Unit,
    onSendMail: () -> Unit,
    onDonate: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .background(canvasColor())
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                    title = text(language, "Dil", "Language", "Langue", "Idioma", "语言", "भाषा", "Язык")
                ) {
                    LanguageDropdown(language = language, onLanguageChange = onLanguageChange)
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                    title = text(language, "Görünüm", "Appearance", "Apparence", "Apariencia", "外观", "दिखावट", "Внешний вид")
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChip(text(language, "Sistem", "System", "Système", "Sistema", "系统", "सिस्टम", "Система"), themeMode == AppThemeMode.SYSTEM) {
                            onThemeModeChange(AppThemeMode.SYSTEM)
                        }
                        ThemeChip(text(language, "Açık", "Light", "Clair", "Claro", "浅色", "लाइट", "Светлая"), themeMode == AppThemeMode.LIGHT) {
                            onThemeModeChange(AppThemeMode.LIGHT)
                        }
                        ThemeChip(text(language, "Koyu", "Dark", "Sombre", "Oscuro", "深色", "डार्क", "Тёмная"), themeMode == AppThemeMode.DARK) {
                            onThemeModeChange(AppThemeMode.DARK)
                        }
                    }
                    Text(
                        text(
                            language,
                            if (themeMode == AppThemeMode.SYSTEM) "Cihaz temasını takip ediyor." else if (darkTheme) "Koyu tema aktif." else "Açık tema aktif.",
                            if (themeMode == AppThemeMode.SYSTEM) "Following device theme." else if (darkTheme) "Dark theme is active." else "Light theme is active."
                        )
                    )
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                    title = text(language, "2 dk otomatik tur", "2 min auto tour", "Tour auto 2 min", "Tour auto 2 min", "2分钟自动轮巡", "2 मिनट ऑटो टूर", "Авто-тур 2 мин")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text(
                                language,
                                "Açıkken günlük kullanılmayan uygulamalar için daha belirgin hatırlatma verir. Android güvenliği nedeniyle uygulamaları sessizce kapatamaz.",
                                "When enabled, reminders are stronger for apps not used today. Android security does not allow silently closing other apps.",
                                "Activé, les rappels sont renforcés. Android ne permet pas de fermer d'autres apps en silence.",
                                "Activado, los recordatorios son más claros. Android no permite cerrar otras apps en silencio.",
                                "开启后会加强提醒。Android 不允许静默关闭其他应用。",
                                "चालू होने पर रिमाइंडर मजबूत होंगे। Android दूसरी ऐप्स को चुपचाप बंद करने नहीं देता।",
                                "При включении напоминания заметнее. Android не разрешает тихо закрывать другие приложения."
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = autoTourEnabled, onCheckedChange = onAutoTourChange)
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Help, contentDescription = null) },
                    title = text(language, "Yardım", "Help", "Aide", "Ayuda", "帮助", "मदद", "Помощь")
                ) {
                    Text(
                        text(
                            language,
                            "Uygulama seç, test gününü gir ve kullanım erişimi izniyle gün gün süreleri takip et. Kart ikonuna dokunarak ilgili uygulamayı açabilirsin.",
                            "Pick apps, enter the current test day, and track daily minutes with Usage Access. Tap an app icon to open that app.",
                            "Choisis les apps, indique le jour de test et suis les minutes avec l'accès d'utilisation.",
                            "Elige apps, introduce el día de prueba y sigue los minutos con acceso de uso.",
                            "选择应用，输入测试天数，并通过使用情况权限跟踪每日分钟数。",
                            "ऐप चुनें, टेस्ट दिन दर्ज करें और Usage Access से दैनिक मिनट देखें।",
                            "Выберите приложения, укажите день теста и отслеживайте минуты через Usage Access."
                        )
                    )
                }
            }
        }
        item {
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                    title = text(language, "Destek e-postası", "Support email", "E-mail support", "Correo soporte", "支持邮箱", "सपोर्ट ईमेल", "Почта поддержки"),
                    subtitle = SUPPORT_MAIL,
                    onClick = onSendMail
                )
            }
        }
        item {
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
                    title = text(language, "Bağış / Kahve ısmarla", "Donate / Buy me a coffee", "Don / Offrir un café", "Donar / Invitar un café", "捐赠 / 请我喝咖啡", "दान / कॉफी", "Донат / кофе"),
                    subtitle = DONATION_URL,
                    onClick = onDonate
                )
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = text(language, "Hakkında", "About", "À propos", "Acerca de", "关于", "के बारे में", "О приложении")
                ) {
                    Text(
                        text(
                            language,
                            "Closed Test Tracker v1.0\nMD Studio tarafından Google Play kapalı testlerini daha düzenli sürdürmek, test serilerini ve uygulama kullanım sürelerini gün gün takip etmek için geliştirildi.\n\nWeb/PWA kısayollarında süre tarayıcı paketine (Chrome, Samsung Internet vb.) yazılabilir; Android site bazlı süreyi uygulamalara vermez. Bu yüzden bazı web tarzı uygulamalar 0 dk görünebilir.\n\nPlay Store yayıncı adı Android tarafından cihaz içinden verilmez. Uygulama internet varsa Play Store sayfasından yayıncı adını okumayı dener; sayfa erişilemiyorsa yayıncı boş kalabilir.",
                            "Closed Test Tracker v1.0\nDeveloped by MD Studio to keep Google Play closed tests more organized and track test streaks with daily app usage minutes.\n\nFor web/PWA shortcuts, time can be attributed to the browser package (Chrome, Samsung Internet, etc.). Android does not expose per-site usage time to apps, so some web-style apps may show 0 min.\n\nAndroid does not expose the Play Store publisher name locally. When internet is available, the app tries to read it from the Play Store page; if the page is unavailable, publisher may stay empty.",
                            "Closed Test Tracker v1.0\nDéveloppé par MD Studio pour mieux organiser les tests fermés Google Play et suivre les séries avec les minutes d'utilisation.\n\nPour les raccourcis web/PWA, le temps peut être attribué au navigateur (Chrome, Samsung Internet, etc.). Android ne fournit pas le temps par site aux apps.\n\nAndroid ne fournit pas localement le nom de l'éditeur Play Store; l'app essaie de le lire depuis la page Play Store.",
                            "Closed Test Tracker v1.0\nDesarrollado por MD Studio para organizar mejor las pruebas cerradas de Google Play y seguir rachas con minutos de uso.\n\nEn accesos web/PWA, el tiempo puede asignarse al navegador (Chrome, Samsung Internet, etc.). Android no entrega tiempo por sitio a las apps.\n\nAndroid no expone localmente el editor de Play Store; la app intenta leerlo desde la página de Play Store.",
                            "Closed Test Tracker v1.0\n由 MD Studio 开发，用于更有序地维护 Google Play 封闭测试，并跟踪测试连续天数和应用使用分钟数。\n\n对于 Web/PWA 快捷方式，使用时长可能会计入浏览器包（Chrome、Samsung Internet 等）。Android 不向应用提供按网站统计的使用时长。\n\nAndroid 不在本地提供 Play Store 发布者名称；本应用会尝试从 Play Store 页面读取。",
                            "Closed Test Tracker v1.0\nMD Studio द्वारा Google Play बंद टेस्ट को अधिक व्यवस्थित रखने और दैनिक ऐप उपयोग मिनटों के साथ टेस्ट स्ट्रीक ट्रैक करने के लिए बनाया गया।\n\nWeb/PWA शॉर्टकट में समय ब्राउज़र पैकेज (Chrome, Samsung Internet आदि) में जुड़ सकता है। Android ऐप्स को साइट-वार उपयोग समय नहीं देता।\n\nAndroid Play Store publisher नाम लोकल रूप से नहीं देता; ऐप इसे Play Store पेज से पढ़ने की कोशिश करता है।",
                            "Closed Test Tracker v1.0\nРазработано MD Studio, чтобы удобнее вести закрытые тесты Google Play и отслеживать серии с минутами использования приложений.\n\nДля web/PWA-ярлыков время может записываться на пакет браузера (Chrome, Samsung Internet и т. д.). Android не передает приложениям статистику по отдельным сайтам.\n\nAndroid не раскрывает имя издателя Play Store локально; приложение пытается прочитать его со страницы Play Store."
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = rowSurfaceColor(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, separatorColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(language: AppLanguage, onLanguageChange: (AppLanguage) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = languageDisplay(language),
            onValueChange = {},
            readOnly = true,
            label = { Text("Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            colors = glassTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppLanguage.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(languageDisplay(item), color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onLanguageChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    language: AppLanguage,
    appTheme: AppTheme,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onSendMail: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Settings, contentDescription = null)
                Text(text(language, "Ayarlar", "Settings"))
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    SettingsSection(
                        icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                        title = text(language, "Tema", "Theme")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip(text(language, "Canlı", "Fresh"), appTheme == AppTheme.FRESH) { onThemeChange(AppTheme.FRESH) }
                            ThemeChip(text(language, "Okyanus", "Ocean"), appTheme == AppTheme.OCEAN) { onThemeChange(AppTheme.OCEAN) }
                            ThemeChip(text(language, "Günbatımı", "Sunset"), appTheme == AppTheme.SUNSET) { onThemeChange(AppTheme.SUNSET) }
                        }
                    }
                }
                item {
                    SettingsSection(
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                        title = text(language, "Dil", "Language")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip("Türkçe", language == AppLanguage.TR) { onLanguageChange(AppLanguage.TR) }
                            ThemeChip("English", language == AppLanguage.EN) { onLanguageChange(AppLanguage.EN) }
                        }
                    }
                }
                item {
                    SettingsSection(
                        icon = { Icon(Icons.Rounded.Help, contentDescription = null) },
                        title = text(language, "Yardım", "Help")
                    ) {
                        Text(
                            text(
                                language,
                                "Uygulama seç, test gününü gir ve kullanım erişimi izniyle gün gün süreleri takip et. Kart ikonuna dokunarak ilgili uygulamayı açabilirsin.",
                                "Pick apps, enter the current test day, and track daily minutes with Usage Access. Tap an app icon to open that app."
                            )
                        )
                    }
                }
                item {
                    SettingsAction(
                        icon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        title = text(language, "Destek e-postası", "Support email"),
                        subtitle = SUPPORT_MAIL,
                        onClick = onSendMail
                    )
                }
                item {
                    SettingsSection(
                        icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                        title = text(language, "Hakkında", "About")
                    ) {
                        Text(
                            text(
                                language,
                                "Closed Test Tracker v1.0\nMD Studio tarafından Google Play kapalı testlerini daha düzenli sürdürmek, test serilerini ve uygulama kullanım sürelerini gün gün takip etmek için geliştirildi.",
                                "Closed Test Tracker v1.0\nDeveloped by MD Studio to keep Google Play closed tests more organized and track test streaks with daily app usage minutes."
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text(language, "Kapat", "Close"))
            }
        }
    )
}

@Composable
private fun SettingsSection(
    icon: @Composable () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            Text(title, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
private fun SettingsAction(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(if (isDarkScheme()) Color.White.copy(alpha = 0.055f) else Color.White.copy(alpha = 0.62f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = panelColor(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(16.dp),
            border = panelBorder()
        ) {
            Box(Modifier.padding(8.dp)) {
                icon()
            }
        }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = secondaryTextColor(), style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = secondaryTextColor())
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        colors = glassChipColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(
    language: AppLanguage,
    appTheme: AppTheme,
    apps: List<InstalledApp>,
    trackedPackageNames: Set<String>,
    playPublishers: Map<String, String>,
    onLoadPublisher: suspend (String) -> Unit,
    onDismiss: () -> Unit,
    onPick: (InstalledApp) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SortMode.NAME) }
    val filtered = remember(apps, query, sortMode) {
        val q = query.trim().lowercase()
        val base = apps
            .filterNot { it.packageName in trackedPackageNames }
            .filter {
                q.isBlank() || it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        when (sortMode) {
            SortMode.NAME -> base.sortedBy { it.label.lowercase() }
            SortMode.NEWEST -> base.sortedByDescending { it.firstInstallTime }
            SortMode.OLDEST -> base.sortedBy { it.firstInstallTime }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = if (isDarkScheme()) Color.Black else Color(0xFFF7F7F5),
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.46f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 54.dp, height = 5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.24f else 0.18f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = panelColor(strong = true),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(22.dp),
                    border = panelBorder()
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(11.dp).size(24.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text(language, "Uygulama seç", "Pick app"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text(language, "${filtered.size} uygulama eklenebilir", "${filtered.size} apps available"),
                        color = secondaryTextColor(),
                        fontWeight = FontWeight.Medium
                    )
                }
                Surface(
                    color = rowSurfaceColor(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, separatorColor()),
                    modifier = Modifier.clickable(onClick = onDismiss)
                ) {
                    Text(
                        text(language, "Kapat", "Close"),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(text(language, "Ara", "Search")) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = secondaryTextColor(),
                    unfocusedLabelColor = secondaryTextColor(),
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedLeadingIconColor = secondaryTextColor(),
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.26f else 0.18f),
                    unfocusedBorderColor = separatorColor(),
                    focusedContainerColor = rowSurfaceColor(),
                    unfocusedContainerColor = rowSurfaceColor()
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortChip(text(language, "A-Z", "A-Z"), sortMode == SortMode.NAME) { sortMode = SortMode.NAME }
                SortChip(text(language, "Yeni", "Newest"), sortMode == SortMode.NEWEST) { sortMode = SortMode.NEWEST }
                SortChip(text(language, "Eski", "Oldest"), sortMode == SortMode.OLDEST) { sortMode = SortMode.OLDEST }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = canvasColor(),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, separatorColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (filtered.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text(language, "Sonuç yok", "No results"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text(language, "Aramayı değiştir veya eklenmiş uygulamaları kontrol et.", "Change the search or check already added apps."),
                            color = secondaryTextColor()
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 560.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filtered, key = { it.packageName }) { app ->
                            val publisher = playPublishers[app.packageName]
                            LaunchedEffect(app.packageName) {
                                onLoadPublisher(app.packageName)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(app) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = app.icon.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(Modifier.size(12.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        app.label,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        publisher?.let { text(language, "Yayıncı: $it", "Publisher: $it") }
                                            ?: text(language, "Seçmek için dokun", "Tap to add"),
                                        color = secondaryTextColor(),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = if (isDarkScheme()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.045f),
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(17.dp),
                                    border = BorderStroke(1.dp, separatorColor())
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp).size(18.dp)
                                    )
                                }
                            }
                            CanvasDivider(Modifier.padding(start = 70.dp, end = 14.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun AppPickerDialog(
    language: AppLanguage,
    apps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onPick: (InstalledApp) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SortMode.NAME) }
    val filtered = remember(apps, query, sortMode) {
        val q = query.trim().lowercase()
        val base = apps.filter {
            q.isBlank() || it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
        when (sortMode) {
            SortMode.NAME -> base.sortedBy { it.label.lowercase() }
            SortMode.NEWEST -> base.sortedByDescending { it.firstInstallTime }
            SortMode.OLDEST -> base.sortedBy { it.firstInstallTime }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text(language, "Uygulama seç", "Pick app")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(text(language, "Ara", "Search")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortChip(text(language, "İsim", "Name"), sortMode == SortMode.NAME) { sortMode = SortMode.NAME }
                    SortChip(text(language, "Yeni", "New"), sortMode == SortMode.NEWEST) { sortMode = SortMode.NEWEST }
                    SortChip(text(language, "Eski", "Old"), sortMode == SortMode.OLDEST) { sortMode = SortMode.OLDEST }
                }
                Text(text(language, "${filtered.size} uygulama", "${filtered.size} apps"))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPick(app) }
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(7.dp))
                            )
                            Spacer(Modifier.size(8.dp))
                            Column {
                                Text(app.label, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text(language, "Kapat", "Close")) } }
    )
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if (isDarkScheme()) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.10f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            containerColor = rowSurfaceColor(),
            labelColor = secondaryTextColor(),
            disabledContainerColor = rowSurfaceColor(),
            disabledLabelColor = secondaryTextColor()
        )
    )
}

private fun testUsageDays(
    context: android.content.Context,
    item: TrackedApp,
    usageAccess: Boolean
): List<UsageDay> {
    val formatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val currentDay = SeriesCalculator.currentDay(item)
    return (1..14).map { testDay ->
        val offsetFromToday = testDay - currentDay
        val dayStart = SeriesCalculator.dayStartMillis(offsetFromToday)
        val isFuture = offsetFromToday > 0
        UsageDay(
            index = testDay,
            label = formatter.format(Date(dayStart)),
            minutes = if (usageAccess && !isFuture) {
                UsageReader.usageMinutesByDay(context, item.packageName, dayStart)
            } else {
                0L
            },
            isToday = testDay == currentDay,
            isFuture = isFuture
        )
    }
}
