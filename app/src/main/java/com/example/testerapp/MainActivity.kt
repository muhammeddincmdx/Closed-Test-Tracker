package com.mdstudio.closedtesttracker

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
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
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.mdstudio.closedtesttracker.data.AppDatabase
import com.mdstudio.closedtesttracker.data.TrackedApp
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SortMode { NAME, NEWEST, OLDEST }
enum class AppLanguage { TR, EN, FR, ES, ZH, HI, RU, AR }
private enum class LanguageMode { SYSTEM, TR, EN, FR, ES, ZH, HI, RU, AR }
enum class AppTheme { FRESH, OCEAN, SUNSET }
enum class AppThemeMode { SYSTEM, LIGHT, DARK }
enum class AppScreen { HOME, SETTINGS, DETAIL }
enum class HomeFilter { ACTIVE, COMPLETED, ARCHIVED, ALL }
private enum class DateDisplayFormat { MONTH_DAY, DAY_MONTH }
private enum class OverviewFilter { ALL, ACTIVE, COMPLETED, ARCHIVED }
private enum class DetailViewMode { GRAPH, TEXT }
private enum class ConfirmAction { FINISH, REACTIVATE, ARCHIVE, RESTORE }

private const val PREFS_NAME = "tester_settings"
private const val KEY_LANGUAGE = "language"
private const val KEY_THEME = "theme"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_DATE_FORMAT = "date_format"
private const val KEY_AUTO_TOUR = "auto_tour"
private const val KEY_REMINDER_HOUR = "reminder_hour"
private const val KEY_REMINDER_MINUTE = "reminder_minute"
private const val KEY_PLAY_PUBLISHER_PREFIX = "play_publisher_"
private const val KEY_LAST_UPDATE_NOTIFICATION_CODE = "last_update_notification_code"
private const val SUPPORT_MAIL = "mdstudiohelp@gmail.com"
private const val DONATION_URL = "https://www.buymeacoffee.com/mdx0"
private const val POLICY_URL = "https://sites.google.com/view/infomdstudio/documenter-pdf-scanner-viewer?"

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
    focusedLabelColor = secondaryTextColor(),
    unfocusedLabelColor = secondaryTextColor(),
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLeadingIconColor = secondaryTextColor(),
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTrailingIconColor = secondaryTextColor(),
    cursorColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.28f else 0.20f),
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.26f else 0.18f),
    focusedContainerColor = rowSurfaceColor(),
    unfocusedContainerColor = rowSurfaceColor()
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
    val firstInstallTime: Long,
    val lastUpdateTime: Long
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
    val initialDay: Int,
    val isNew: Boolean
)

private data class UsageSummary(
    val days: List<UsageDay>,
    val totalMinutes: Long,
    val hasPartialDailyHistory: Boolean,
    val isLoading: Boolean = false
)

private data class TodayUsageState(
    val minutesByPackage: Map<String, Long> = emptyMap(),
    val isLoading: Boolean = true
)

private enum class UsageRangeMode {
    WINDOWED,
    FULL
}

private data class PlayUpdateState(
    val isAvailable: Boolean = false,
    val availableVersionCode: Int? = null,
    val stalenessDays: Int? = null,
    val promptShown: Boolean = false
)

private fun text(
    language: AppLanguage,
    tr: String,
    en: String,
    fr: String = en,
    es: String = en,
    zh: String = en,
    hi: String = en,
    ru: String = en,
    ar: String = en
): String {
    val selected = when (language) {
        AppLanguage.TR -> tr
        AppLanguage.EN -> en
        AppLanguage.FR -> fr
        AppLanguage.ES -> es
        AppLanguage.ZH -> zh
        AppLanguage.HI -> hi
        AppLanguage.RU -> ru
        AppLanguage.AR -> ar
    }
    return if (language != AppLanguage.EN && selected == en) {
        fallbackText(language, en)
    } else {
        selected
    }
}

private fun fallbackText(language: AppLanguage, en: String): String {
    if (en.startsWith("Publisher: ")) {
        val name = en.removePrefix("Publisher: ")
        return when (language) {
            AppLanguage.TR -> "Yayıncı: $name"
            AppLanguage.FR -> "Éditeur : $name"
            AppLanguage.ES -> "Editor: $name"
            AppLanguage.ZH -> "发布者：$name"
            AppLanguage.HI -> "प्रकाशक: $name"
            AppLanguage.RU -> "Издатель: $name"
            AppLanguage.AR -> "الناشر: $name"
            AppLanguage.EN -> en
        }
    }
    if (en.startsWith("Installed version: ")) {
        val value = en.removePrefix("Installed version: ")
        return when (language) {
            AppLanguage.FR -> "Version installée : $value"
            AppLanguage.ES -> "Versión instalada: $value"
            AppLanguage.ZH -> "已安装版本：$value"
            AppLanguage.HI -> "इंस्टॉल किया गया संस्करण: $value"
            AppLanguage.RU -> "Установленная версия: $value"
            AppLanguage.AR -> "الإصدار المثبت: $value"
            else -> en
        }
    }
    if (en.startsWith("Version ")) {
        val value = en.removePrefix("Version ")
        return when (language) {
            AppLanguage.FR -> "Version $value"
            AppLanguage.ES -> "Versión $value"
            AppLanguage.ZH -> "版本 $value"
            AppLanguage.HI -> "संस्करण $value"
            AppLanguage.RU -> "Версия $value"
            AppLanguage.AR -> "الإصدار $value"
            else -> en
        }
    }
    Regex("""^Day (\d+)$""").matchEntire(en)?.let { match ->
        val value = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "Jour $value"
            AppLanguage.ES -> "Día $value"
            AppLanguage.ZH -> "第 $value 天"
            AppLanguage.HI -> "दिन $value"
            AppLanguage.RU -> "День $value"
            AppLanguage.AR -> "اليوم $value"
            else -> en
        }
    }
    Regex("""^(\d+) apps available$""").matchEntire(en)?.let { match ->
        val value = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "$value apps disponibles"
            AppLanguage.ES -> "$value apps disponibles"
            AppLanguage.ZH -> "$value 个应用可添加"
            AppLanguage.HI -> "$value ऐप उपलब्ध"
            AppLanguage.RU -> "Доступно приложений: $value"
            AppLanguage.AR -> "$value تطبيق متاح"
            else -> en
        }
    }
    Regex("""^(\d+) apps$""").matchEntire(en)?.let { match ->
        val value = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "$value apps"
            AppLanguage.ES -> "$value apps"
            AppLanguage.ZH -> "$value 个应用"
            AppLanguage.HI -> "$value ऐप"
            AppLanguage.RU -> "$value приложений"
            AppLanguage.AR -> "$value تطبيق"
            else -> en
        }
    }
    if (en.startsWith("Daily series check is scheduled around ")) {
        val time = en.substringAfter("around ").removeSuffix(".")
        return when (language) {
            AppLanguage.FR -> "La vérification quotidienne est planifiée vers $time."
            AppLanguage.ES -> "La comprobación diaria está programada alrededor de las $time."
            AppLanguage.ZH -> "每日系列检查计划在约 $time 运行。"
            AppLanguage.HI -> "दैनिक सीरीज़ जाँच लगभग $time पर नियोजित है।"
            AppLanguage.RU -> "Ежедневная проверка серии запланирована примерно на $time."
            AppLanguage.AR -> "تمت جدولة فحص السلسلة اليومي حوالي $time."
            else -> en
        }
    }
    Regex("""^Day 1-(\d+) summary$""").matchEntire(en)?.let { match ->
        val value = match.groupValues[1]
        return when (language) {
            AppLanguage.TR -> "1-$value. gün özeti"
            AppLanguage.FR -> "Résumé jours 1-$value"
            AppLanguage.ES -> "Resumen días 1-$value"
            AppLanguage.ZH -> "第 1-$value 天摘要"
            AppLanguage.HI -> "दिन 1-$value सारांश"
            AppLanguage.RU -> "Сводка дней 1-$value"
            AppLanguage.AR -> "ملخص الأيام 1-$value"
            AppLanguage.EN -> en
        }
    }
    Regex("""^Day (\d+)-(\d+) summary$""").matchEntire(en)?.let { match ->
        val first = match.groupValues[1]
        val last = match.groupValues[2]
        return when (language) {
            AppLanguage.TR -> "$first-$last. gün özeti"
            AppLanguage.FR -> "Résumé jours $first-$last"
            AppLanguage.ES -> "Resumen días $first-$last"
            AppLanguage.ZH -> "第 $first-$last 天摘要"
            AppLanguage.HI -> "दिन $first-$last सारांश"
            AppLanguage.RU -> "Сводка дней $first-$last"
            AppLanguage.AR -> "ملخص الأيام $first-$last"
            AppLanguage.EN -> en
        }
    }
    Regex("""^(.+) will be marked as completed\. You can reactivate it later\.$""").matchEntire(en)?.let { match ->
        val name = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "$name sera marqué comme terminé. Vous pourrez le réactiver plus tard."
            AppLanguage.ES -> "$name se marcará como completado. Podrás reactivarlo después."
            AppLanguage.ZH -> "$name 将标记为已完成。你以后可以重新启用。"
            AppLanguage.HI -> "$name को पूरा चिह्नित किया जाएगा। आप बाद में फिर सक्रिय कर सकते हैं।"
            AppLanguage.RU -> "$name будет отмечено как завершенное. Позже можно активировать снова."
            AppLanguage.AR -> "سيتم وضع علامة مكتمل على $name. يمكنك إعادة تفعيله لاحقاً."
            else -> en
        }
    }
    Regex("""^(.+) will return to the active list and the streak counter will continue\.$""").matchEntire(en)?.let { match ->
        val name = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "$name retournera dans la liste active et le compteur continuera."
            AppLanguage.ES -> "$name volverá a la lista activa y el contador continuará."
            AppLanguage.ZH -> "$name 将回到活跃列表，连续计数会继续。"
            AppLanguage.HI -> "$name सक्रिय सूची में वापस आएगा और streak काउंटर जारी रहेगा।"
            AppLanguage.RU -> "$name вернется в активный список, счетчик серии продолжится."
            AppLanguage.AR -> "سيعود $name إلى القائمة النشطة وسيستمر عداد السلسلة."
            else -> en
        }
    }
    Regex("""^(.+) will be moved to archive\. You can restore it later\.$""").matchEntire(en)?.let { match ->
        val name = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "$name sera déplacé dans l'archive. Vous pourrez le restaurer plus tard."
            AppLanguage.ES -> "$name se moverá al archivo. Podrás restaurarlo después."
            AppLanguage.ZH -> "$name 将移至归档。你以后可以恢复。"
            AppLanguage.HI -> "$name आर्काइव में जाएगा। आप बाद में वापस ला सकते हैं।"
            AppLanguage.RU -> "$name будет перемещено в архив. Позже можно восстановить."
            AppLanguage.AR -> "سيتم نقل $name إلى الأرشيف. يمكنك استعادته لاحقاً."
            else -> en
        }
    }
    Regex("""^(.+) will be restored from archive and shown in the list again\.$""").matchEntire(en)?.let { match ->
        val name = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "$name sera restauré depuis l'archive et réapparaîtra dans la liste."
            AppLanguage.ES -> "$name se restaurará desde el archivo y volverá a mostrarse."
            AppLanguage.ZH -> "$name 将从归档恢复并重新显示在列表中。"
            AppLanguage.HI -> "$name आर्काइव से वापस आएगा और सूची में फिर दिखेगा।"
            AppLanguage.RU -> "$name будет восстановлено из архива и снова показано в списке."
            AppLanguage.AR -> "ستتم استعادة $name من الأرشيف وسيظهر في القائمة مرة أخرى."
            else -> en
        }
    }
    Regex("""^Remove (.+) from the list\? Android usage history stays, only the tracking record is deleted\.$""").matchEntire(en)?.let { match ->
        val name = match.groupValues[1]
        return when (language) {
            AppLanguage.FR -> "Supprimer $name de la liste ? L'historique Android reste, seul le suivi est supprimé."
            AppLanguage.ES -> "¿Eliminar $name de la lista? El historial de Android permanece; solo se elimina el seguimiento."
            AppLanguage.ZH -> "从列表中移除 $name？Android 使用历史会保留，只删除跟踪记录。"
            AppLanguage.HI -> "$name को सूची से हटाएं? Android usage history रहेगा, केवल tracking record हटेगा।"
            AppLanguage.RU -> "Удалить $name из списка? История Android останется, удалится только запись отслеживания."
            AppLanguage.AR -> "هل تريد إزالة $name من القائمة؟ سيبقى سجل استخدام Android، وسيتم حذف سجل المتابعة فقط."
            else -> en
        }
    }
    fun all(fr: String, es: String, zh: String, hi: String, ru: String, ar: String) = mapOf(
        AppLanguage.FR to fr,
        AppLanguage.ES to es,
        AppLanguage.ZH to zh,
        AppLanguage.HI to hi,
        AppLanguage.RU to ru,
        AppLanguage.AR to ar
    )
    val common = when (en) {
        "Loading" -> all("Chargement", "Cargando", "加载中", "लोड हो रहा है", "Загрузка", "جارٍ التحميل")
        "Language" -> all("Langue", "Idioma", "语言", "भाषा", "Язык", "اللغة")
        "System default" -> all("Langue du système", "Idioma del sistema", "系统默认", "सिस्टम डिफ़ॉल्ट", "Системный язык", "لغة النظام الافتراضية")
        "English is used when the system language is not supported." -> all("L'anglais est utilisé si la langue du système n'est pas prise en charge.", "Se usa inglés cuando el idioma del sistema no es compatible.", "系统语言不受支持时将使用英语。", "सिस्टम भाषा समर्थित नहीं होने पर English उपयोग होती है।", "Если язык системы не поддерживается, используется английский.", "تُستخدم الإنجليزية عندما لا تكون لغة النظام مدعومة.")
        "Following device theme." -> all("Suit le thème de l'appareil.", "Sigue el tema del dispositivo.", "跟随设备主题。", "डिवाइस थीम का पालन कर रहा है।", "Использует тему устройства.", "يتبع سمة الجهاز.")
        "Dark theme is active." -> all("Le thème sombre est actif.", "El tema oscuro está activo.", "深色主题已启用。", "डार्क थीम सक्रिय है।", "Тёмная тема активна.", "السمة الداكنة مفعلة.")
        "Light theme is active." -> all("Le thème clair est actif.", "El tema claro está activo.", "浅色主题已启用。", "लाइट थीम सक्रिय है।", "Светлая тема активна.", "السمة الفاتحة مفعلة.")
        "The app interface switches to Turkish." -> all("L'interface passe en turc.", "La interfaz cambia a turco.", "应用界面将切换为土耳其语。", "ऐप इंटरफ़ेस तुर्की में होगा।", "Интерфейс приложения будет на турецком.", "ستتحول واجهة التطبيق إلى التركية.")
        "The app interface switches to English." -> all("L'interface passe en anglais.", "La interfaz cambia a inglés.", "应用界面将切换为英语。", "ऐप इंटरफ़ेस अंग्रेज़ी में होगा।", "Интерфейс приложения будет на английском.", "ستتحول واجهة التطبيق إلى الإنجليزية.")
        "The app interface switches to French." -> all("L'interface passe en français.", "La interfaz cambia a francés.", "应用界面将切换为法语。", "ऐप इंटरफ़ेस फ़्रेंच में होगा।", "Интерфейс приложения будет на французском.", "ستتحول واجهة التطبيق إلى الفرنسية.")
        "The app interface switches to Spanish." -> all("L'interface passe en espagnol.", "La interfaz cambia a español.", "应用界面将切换为西班牙语。", "ऐप इंटरफ़ेस स्पेनिश में होगा।", "Интерфейс приложения будет на испанском.", "ستتحول واجهة التطبيق إلى الإسبانية.")
        "The app interface switches to Chinese." -> all("L'interface passe en chinois.", "La interfaz cambia a chino.", "应用界面将切换为中文。", "ऐप इंटरफ़ेस चीनी में होगा।", "Интерфейс приложения будет на китайском.", "ستتحول واجهة التطبيق إلى الصينية.")
        "The app interface switches to Hindi." -> all("L'interface passe en hindi.", "La interfaz cambia a hindi.", "应用界面将切换为印地语。", "ऐप इंटरफ़ेस हिंदी में होगा।", "Интерфейс приложения будет на хинди.", "ستتحول واجهة التطبيق إلى الهندية.")
        "The app interface switches to Russian." -> all("L'interface passe en russe.", "La interfaz cambia a ruso.", "应用界面将切换为俄语。", "ऐप इंटरफ़ेस रूसी में होगा।", "Интерфейс приложения будет на русском.", "ستتحول واجهة التطبيق إلى الروسية.")
        "The app interface switches to Arabic." -> all("L'interface passe en arabe.", "La interfaz cambia a árabe.", "应用界面将切换为阿拉伯语。", "ऐप इंटरफ़ेस अरबी में होगा।", "Интерфейс приложения будет на арабском.", "ستتحول واجهة التطبيق إلى العربية.")
        "1 - 20" -> all("1 - 20", "1 - 20", "1 - 20", "1 - 20", "1 - 20", "1 - 20")
        "14-day summary" -> all("Résumé 14 jours", "Resumen de 14 días", "14 天摘要", "14 दिन सारांश", "Сводка за 14 дней", "ملخص 14 يوماً")
        "Last 14 days" -> all("14 derniers jours", "Últimos 14 días", "最近 14 天", "पिछले 14 दिन", "Последние 14 дней", "آخر 14 يوماً")
        "14 days" -> all("14 jours", "14 días", "14 天", "14 दिन", "14 дней", "14 يوماً")
        "Built to keep Google Play closed tests organized and track the 14-day streak with daily usage minutes in one place." -> all("Conçue pour organiser les tests fermés Google Play et suivre la série de 14 jours avec les minutes quotidiennes au même endroit.", "Creada para organizar pruebas cerradas de Google Play y seguir la racha de 14 días con minutos diarios en un solo lugar.", "用于整理 Google Play 封闭测试，并在一个位置跟踪 14 天连续测试和每日使用分钟数。", "Google Play closed tests को व्यवस्थित रखने और 14-दिन की streak को दैनिक उपयोग मिनटों के साथ एक जगह ट्रैक करने के लिए बनाया गया।", "Создано для организации закрытых тестов Google Play и отслеживания 14-дневной серии с ежедневными минутами в одном месте.", "صُمم لتنظيم اختبارات Google Play المغلقة ومتابعة سلسلة 14 يوماً مع دقائق الاستخدام اليومية في مكان واحد.")
        "Use the + button to pick a test app. Start from day 1 or set the current test day. Then tap the app icon on the card to open it; when you return, minutes refresh automatically." -> all("Utilisez le bouton + pour choisir une application de test. Commencez au jour 1 ou définissez le jour actuel. Touchez ensuite l'icône de la carte pour ouvrir l'application ; au retour, les minutes se mettent à jour automatiquement.", "Usa el botón + para elegir una app de prueba. Empieza desde el día 1 o ajusta el día actual. Luego toca el icono de la tarjeta para abrirla; al volver, los minutos se actualizan automáticamente.", "使用顶部 + 按钮选择测试应用。可以从第 1 天开始，也可以设置当前测试日。然后点按卡片图标打开应用；返回后分钟数会自动刷新。", "+ बटन से टेस्ट ऐप चुनें। दिन 1 से शुरू करें या वर्तमान टेस्ट दिन सेट करें। फिर कार्ड के आइकन पर टैप करके ऐप खोलें; वापस आने पर मिनट अपने आप अपडेट होंगे।", "Нажмите +, чтобы выбрать тестируемое приложение. Начните с 1-го дня или задайте текущий день теста. Затем нажмите значок приложения на карточке; при возврате минуты обновятся автоматически.", "استخدم زر + لاختيار تطبيق اختبار. ابدأ من اليوم 1 أو عيّن يوم الاختبار الحالي. ثم اضغط أيقونة التطبيق في البطاقة لفتحه؛ وعند العودة تُحدّث الدقائق تلقائياً.")
        "Usage Access is used only to read minutes for apps you choose. The app list and usage data stay on your device and are not shared for ads or analytics." -> all("L'accès à l'utilisation sert uniquement à lire les minutes des applications choisies. La liste et les données restent sur votre appareil et ne sont pas partagées pour la publicité ou l'analyse.", "El Acceso de uso solo se usa para leer los minutos de las apps que eliges. La lista y los datos permanecen en tu dispositivo y no se comparten para anuncios ni análisis.", "使用情况访问仅用于读取你选择的应用分钟数。应用列表和使用数据保留在设备上，不会用于广告或分析共享。", "Usage Access केवल चुने गए ऐप्स के मिनट पढ़ने के लिए उपयोग होता है। ऐप सूची और उपयोग डेटा आपके डिवाइस पर रहता है; विज्ञापन या analytics के लिए साझा नहीं किया जाता।", "Доступ к статистике используется только для чтения минут выбранных приложений. Список приложений и данные остаются на устройстве и не передаются для рекламы или аналитики.", "يُستخدم إذن الوصول للاستخدام فقط لقراءة دقائق التطبيقات التي تختارها. تبقى قائمة التطبيقات وبيانات الاستخدام على جهازك ولا تتم مشاركتها للإعلانات أو التحليلات.")
        "For web/PWA shortcuts, time may be counted under the browser. Android does not expose per-site usage as separate apps, so some web-style apps may show 0 min." -> all("Pour les raccourcis web/PWA, le temps peut être compté sous le navigateur. Android n'expose pas l'usage par site comme une application séparée, donc certaines apps web peuvent afficher 0 min.", "En accesos web/PWA, el tiempo puede contarse en el navegador. Android no expone el uso por sitio como apps separadas, por eso algunas apps web pueden mostrar 0 min.", "对于 Web/PWA 快捷方式，时间可能会计入浏览器。Android 不会把网站使用时间作为独立应用提供，因此某些网页类应用可能显示 0 分钟。", "Web/PWA shortcuts में समय browser के अंतर्गत गिना जा सकता है। Android site-wise usage को अलग app की तरह नहीं देता, इसलिए कुछ web-style apps 0 मिनट दिखा सकते हैं।", "Для web/PWA-ярлыков время может учитываться в браузере. Android не показывает использование по сайтам как отдельные приложения, поэтому некоторые веб-приложения могут показывать 0 мин.", "بالنسبة لاختصارات الويب/PWA، قد يُحسب الوقت ضمن المتصفح. لا يعرض Android استخدام كل موقع كتطبيق منفصل، لذلك قد تظهر بعض التطبيقات الشبيهة بالويب 0 دقيقة.")
        "Change the search or check already added apps." -> all("Changez la recherche ou vérifiez les apps déjà ajoutées.", "Cambia la búsqueda o revisa las apps ya añadidas.", "更改搜索或检查已添加的应用。", "खोज बदलें या पहले से जोड़े गए ऐप देखें।", "Измените поиск или проверьте уже добавленные приложения.", "غيّر البحث أو تحقق من التطبيقات المضافة سابقاً.")
        "Tap to add" -> all("Touchez pour ajouter", "Toca para añadir", "点按添加", "जोड़ने के लिए टैप करें", "Нажмите, чтобы добавить", "اضغط للإضافة")
        "Settings" -> all("Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки", "الإعدادات")
        "Back" -> all("Retour", "Atrás", "返回", "वापस", "Назад", "رجوع")
        "Add app" -> all("Ajouter une app", "Añadir app", "添加应用", "ऐप जोड़ें", "Добавить приложение", "إضافة تطبيق")
        "Summaries" -> all("Résumés", "Resúmenes", "摘要", "सारांश", "Сводки", "الملخصات")
        "Tracking summary" -> all("Résumé du suivi", "Resumen de seguimiento", "跟踪摘要", "ट्रैकिंग सारांश", "Сводка отслеживания", "ملخص المتابعة")
        "App summaries" -> all("Résumés des apps", "Resúmenes de apps", "应用摘要", "ऐप सारांश", "Сводки приложений", "ملخصات التطبيقات")
        "Missing today" -> all("Manquantes aujourd'hui", "Faltan hoy", "今天缺少", "आज बाकी", "Не хватает сегодня", "الناقص اليوم")
        "Apps not opened today are listed below." -> all("Les apps non ouvertes aujourd'hui sont listées ci-dessous.", "Las apps no abiertas hoy aparecen abajo.", "今天未打开的应用如下。", "आज नहीं खोले गए ऐप नीचे हैं।", "Ниже приложения, не открытые сегодня.", "التطبيقات التي لم تُفتح اليوم تظهر أدناه.")
        "Used" -> all("Utilisées", "Usadas", "已使用", "उपयोग", "Использовано", "مستخدم")
        "Missing" -> all("Manquantes", "Faltantes", "缺少", "बाकी", "Не хватает", "ناقص")
        "Done" -> all("Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم")
        "Today" -> all("Aujourd'hui", "Hoy", "今天", "आज", "Сегодня", "اليوم")
        "Streak" -> all("Série", "Racha", "连续", "स्ट्रीक", "Серия", "السلسلة")
        "Active" -> all("Actif", "Activo", "活跃", "सक्रिय", "Активные", "نشط")
        "Archive" -> all("Archive", "Archivo", "归档", "आर्काइव", "Архив", "الأرشيف")
        "All" -> all("Tout", "Todo", "全部", "सभी", "Все", "الكل")
        "Usage access is off" -> all("Accès à l'utilisation désactivé", "Acceso de uso desactivado", "使用情况访问已关闭", "Usage access बंद है", "Доступ к статистике выключен", "إذن الاستخدام متوقف")
        "Usage Access is required to show minutes." -> all("L'accès à l'utilisation est requis pour afficher les minutes.", "Se requiere Acceso de uso para mostrar minutos.", "需要使用情况访问权限才能显示分钟数。", "मिनट दिखाने के लिए Usage Access आवश्यक है।", "Для показа минут нужен доступ к статистике.", "يلزم إذن الوصول للاستخدام لعرض الدقائق.")
        "Open" -> all("Ouvrir", "Abrir", "打开", "खोलें", "Открыть", "فتح")
        "No apps yet" -> all("Aucune app", "Aún no hay apps", "暂无应用", "अभी ऐप नहीं", "Приложений нет", "لا توجد تطبيقات بعد")
        "Choose the apps to track first." -> all("Choisissez d'abord les applications à suivre.", "Primero elige las apps para seguir.", "请先选择要跟踪的应用。", "पहले ट्रैक करने वाले ऐप चुनें।", "Сначала выберите приложения для отслеживания.", "اختر التطبيقات التي تريد متابعتها أولاً.")
        "Pick app" -> all("Choisir une app", "Elegir app", "选择应用", "ऐप चुनें", "Выбрать приложение", "اختيار تطبيق")
        "This filter is empty" -> all("Ce filtre est vide", "Este filtro está vacío", "此筛选为空", "यह फ़िल्टर खाली है", "Этот фильтр пуст", "هذا الفلتر فارغ")
        "Choose another filter or add a new app." -> all("Choisissez un autre filtre ou ajoutez une app.", "Elige otro filtro o añade una app.", "请选择其他筛选或添加新应用。", "दूसरा फ़िल्टर चुनें या नया ऐप जोड़ें।", "Выберите другой фильтр или добавьте приложение.", "اختر فلترًا آخر أو أضف تطبيقًا جديدًا.")
        "Apps" -> all("Apps", "Apps", "应用", "ऐप्स", "Приложения", "التطبيقات")
        "Loading usage" -> all("Chargement de l'utilisation", "Cargando uso", "正在加载使用情况", "उपयोग लोड हो रहा है", "Загрузка использования", "جارٍ تحميل الاستخدام")
        "Usage permission required" -> all("Autorisation d'utilisation requise", "Se requiere permiso de uso", "需要使用情况权限", "Usage अनुमति आवश्यक है", "Требуется доступ к статистике", "إذن الاستخدام مطلوب")
        "Publisher loading" -> all("Chargement de l'éditeur", "Cargando editor", "正在加载发布者", "प्रकाशक लोड हो रहा है", "Загрузка издателя", "جارٍ تحميل الناشر")
        "Day" -> all("Jour", "Día", "天", "दिन", "День", "اليوم")
        "Total" -> all("Total", "Total", "总计", "कुल", "Итого", "المجموع")
        "Open app" -> all("Ouvrir l'app", "Abrir app", "打开应用", "ऐप खोलें", "Открыть приложение", "فتح التطبيق")
        "Manage" -> all("Gestion", "Gestionar", "管理", "प्रबंधन", "Управление", "إدارة")
        "Archived" -> all("Archivée", "Archivada", "已归档", "आर्काइव में", "В архиве", "مؤرشف")
        "Live" -> all("Actif", "Activo", "进行中", "सक्रिय", "Активно", "نشط")
        "Set day" -> all("Définir le jour", "Definir día", "设置天数", "दिन सेट करें", "Задать день", "تعيين اليوم")
        "Finish" -> all("Terminer", "Finalizar", "结束", "समाप्त", "Завершить", "إنهاء")
        "Reactivate" -> all("Réactiver", "Reactivar", "重新启用", "फिर सक्रिय करें", "Активировать снова", "إعادة التفعيل")
        "Restore" -> all("Restaurer", "Restaurar", "恢复", "वापस लाएँ", "Восстановить", "استعادة")
        "Delete" -> all("Supprimer", "Eliminar", "删除", "हटाएं", "Удалить", "حذف")
        "Graph" -> all("Graphique", "Gráfico", "图表", "ग्राफ", "График", "الرسم")
        "Text" -> all("Texte", "Texto", "文字", "टेक्स्ट", "Текст", "النص")
        "Cancel" -> all("Annuler", "Cancelar", "取消", "रद्द करें", "Отмена", "إلغاء")
        "Save" -> all("Enregistrer", "Guardar", "保存", "सेव करें", "Сохранить", "حفظ")
        "Date format" -> all("Format de date", "Formato de fecha", "日期格式", "तारीख़ फ़ॉर्मेट", "Формат даты", "تنسيق التاريخ")
        "Reminder time" -> all("Heure du rappel", "Hora del recordatorio", "提醒时间", "रिमाइंडर समय", "Время напоминания", "وقت التذكير")
        "Close" -> all("Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть", "إغلاق")
        "Search" -> all("Rechercher", "Buscar", "搜索", "खोजें", "Поиск", "بحث")
        "Newest" -> all("Plus récent", "Más reciente", "最新", "नवीनतम", "Новые", "الأحدث")
        "Oldest" -> all("Plus ancien", "Más antiguo", "最旧", "सबसे पुराना", "Старые", "الأقدم")
        "No results" -> all("Aucun résultat", "Sin resultados", "无结果", "कोई परिणाम नहीं", "Нет результатов", "لا توجد نتائج")
        "Minutes" -> all("Minutes", "Minutos", "分钟", "मिनट", "Минуты", "الدقائق")
        "min" -> all("min", "min", "分钟", "मिनट", "мин", "دقيقة")
        "Detail" -> all("Détail", "Detalle", "详情", "विवरण", "Детали", "التفاصيل")
        "Finish test?" -> all("Terminer le test ?", "¿Finalizar prueba?", "结束测试？", "टेस्ट समाप्त करें?", "Завершить тест?", "إنهاء الاختبار؟")
        "Reactivate?" -> all("Réactiver ?", "¿Reactivar?", "重新启用？", "फिर सक्रिय करें?", "Активировать снова?", "إعادة التفعيل؟")
        "Archive?" -> all("Archiver ?", "¿Archivar?", "归档？", "आर्काइव करें?", "В архив?", "أرشفة؟")
        "Restore?" -> all("Restaurer ?", "¿Restaurar?", "恢复？", "वापस लाएँ?", "Восстановить?", "استعادة؟")
        "Delete app" -> all("Supprimer l'app", "Eliminar app", "删除应用", "ऐप हटाएं", "Удалить приложение", "حذف التطبيق")
        "New update available" -> all("Nouvelle mise à jour disponible", "Nueva actualización disponible", "有新更新可用", "नया अपडेट उपलब्ध है", "Доступно новое обновление", "يتوفر تحديث جديد")
        "App updates" -> all("Mises à jour", "Actualizaciones", "应用更新", "ऐप अपडेट", "Обновления", "تحديثات التطبيق")
        "A new Closed Test Tracker version is ready. Tap to update." -> all("Une nouvelle version de Closed Test Tracker est prête. Touchez pour mettre à jour.", "Hay una nueva versión de Closed Test Tracker lista. Toca para actualizar.", "Closed Test Tracker 新版本已准备好。点按更新。", "Closed Test Tracker का नया संस्करण तैयार है। अपडेट के लिए टैप करें।", "Доступна новая версия Closed Test Tracker. Нажмите, чтобы обновить.", "إصدار جديد من Closed Test Tracker جاهز. اضغط للتحديث.")
        "Update" -> all("Mettre à jour", "Actualizar", "更新", "अपडेट", "Обновить", "تحديث")
        "Later" -> all("Plus tard", "Más tarde", "稍后", "बाद में", "Позже", "لاحقاً")
        "Hello, I would like support about the app." -> all("Bonjour, je voudrais de l'aide au sujet de l'application.", "Hola, quiero soporte sobre la app.", "你好，我想获得有关此应用的支持。", "नमस्ते, मुझे ऐप के बारे में सहायता चाहिए।", "Здравствуйте, мне нужна помощь по приложению.", "مرحباً، أريد دعماً بخصوص التطبيق.")
        "Which test day is this app on today? Choose between 1 and 20." -> all("À quel jour de test cette app est-elle aujourd'hui ? Choisissez entre 1 et 20.", "¿En qué día de prueba está esta app hoy? Elige entre 1 y 20.", "此应用今天是测试第几天？请选择 1 到 20。", "यह ऐप आज कौन से टेस्ट दिन पर है? 1 से 20 के बीच चुनें।", "Какой сегодня день теста для этого приложения? Выберите от 1 до 20.", "في أي يوم اختبار هذا التطبيق اليوم؟ اختر بين 1 و20.")
        "Android returns incomplete daily history for this app. Total time is calculated from the broad range total; the graph and text summary show only daily records available from the device." -> all("Android renvoie un historique quotidien incomplet pour cette app. Le total vient d'une plage large ; le graphique et le résumé affichent seulement les jours disponibles.", "Android devuelve historial diario incompleto para esta app. El total se calcula con un rango amplio; el gráfico y el texto muestran solo los registros diarios disponibles.", "Android 对此应用返回的每日历史不完整。总时间使用较大范围计算；图表和文字摘要只显示设备可提供的每日记录。", "Android इस ऐप के लिए अधूरा दैनिक इतिहास देता है। कुल समय बड़े range से निकाला जाता है; graph/text केवल उपलब्ध दैनिक रिकॉर्ड दिखाते हैं।", "Android возвращает неполную дневную историю для этого приложения. Общее время считается по широкому диапазону; график и текст показывают только доступные записи.", "يعرض Android سجلاً يومياً غير مكتمل لهذا التطبيق. يتم حساب الإجمالي من نطاق واسع؛ ويعرض الرسم والملخص السجلات اليومية المتاحة فقط.")
        "If the system language is not supported, the app automatically uses English." -> all("Si la langue du système n'est pas prise en charge, l'application utilise automatiquement l'anglais.", "Si el idioma del sistema no es compatible, la app usa inglés automáticamente.", "如果系统语言不受支持，应用会自动使用英语。", "यदि सिस्टम भाषा समर्थित नहीं है, तो ऐप अपने-आप English उपयोग करता है।", "Если язык системы не поддерживается, приложение автоматически использует английский.", "إذا لم تكن لغة النظام مدعومة، يستخدم التطبيق الإنجليزية تلقائياً.")
        "Pick apps, enter the current test day, and track daily minutes with Usage Access. Tap an app icon to open that app." -> all("Choisissez des apps, saisissez le jour de test actuel et suivez les minutes quotidiennes avec l'accès à l'utilisation. Touchez une icône pour ouvrir l'app.", "Elige apps, introduce el día actual de prueba y sigue los minutos diarios con Acceso de uso. Toca el icono para abrir la app.", "选择应用，输入当前测试日，并通过使用情况访问跟踪每日分钟数。点按应用图标即可打开应用。", "ऐप चुनें, वर्तमान टेस्ट दिन दर्ज करें, और Usage Access से रोज़ाना मिनट ट्रैक करें। ऐप खोलने के लिए उसके आइकन पर टैप करें।", "Выберите приложения, укажите текущий день теста и отслеживайте минуты через доступ к статистике. Нажмите значок приложения, чтобы открыть его.", "اختر التطبيقات، أدخل يوم الاختبار الحالي، وتابع الدقائق اليومية عبر إذن الاستخدام. اضغط أيقونة التطبيق لفتحه.")
        "Play Store" -> all("Play Store", "Play Store", "Play Store", "Play Store", "Play Store", "Play Store")
        "The streak keeps running until you finish." -> all("La série continue jusqu'à ce que vous la terminiez.", "La racha sigue hasta que la finalices.", "在你结束之前，连续计数会继续。", "जब तक आप समाप्त नहीं करते, streak चलती रहती है।", "Серия продолжается, пока вы ее не завершите.", "تستمر السلسلة حتى تنهيها.")
        "You can switch between a line chart and a written summary." -> all("Vous pouvez basculer entre un graphique linéaire et un résumé écrit.", "Puedes alternar entre gráfico de líneas y resumen escrito.", "你可以在线图和文字摘要之间切换。", "आप लाइन चार्ट और लिखित सारांश के बीच बदल सकते हैं।", "Можно переключаться между линейным графиком и текстовой сводкой.", "يمكنك التبديل بين الرسم الخطي والملخص النصي.")
        "Daily and total time is being read from the device." -> all("Les durées quotidiennes et totales sont lues depuis l'appareil.", "El tiempo diario y total se está leyendo del dispositivo.", "正在从设备读取每日和总使用时间。", "दैनिक और कुल समय डिवाइस से पढ़ा जा रहा है।", "Ежедневное и общее время считывается с устройства.", "تتم قراءة الوقت اليومي والإجمالي من الجهاز.")
        "No usage data yet" -> all("Aucune donnée d'utilisation", "Aún no hay datos de uso", "暂无使用数据", "अभी उपयोग डेटा नहीं", "Данных использования пока нет", "لا توجد بيانات استخدام بعد")
        "Test day" -> all("Jour de test", "Día de prueba", "测试日", "टेस्ट दिन", "День теста", "يوم الاختبار")
        "Selected day" -> all("Jour sélectionné", "Día seleccionado", "已选天数", "चुना गया दिन", "Выбранный день", "اليوم المحدد")
        "Change the date display used in charts and lists." -> all("Modifiez l'affichage des dates dans les graphiques et les listes.", "Cambia el formato de fecha usado en gráficos y listas.", "更改图表和列表中的日期显示方式。", "चार्ट और सूचियों में तारीख़ दिखाने का तरीका बदलें।", "Измените формат дат в графиках и списках.", "غيّر طريقة عرض التاريخ في الرسوم والقوائم.")
        "Version" -> all("Version", "Versión", "版本", "संस्करण", "Версия", "الإصدار")
        "Open the Google Play page to review what's new and install the update." -> all("Ouvrez Google Play pour voir les nouveautés et installer la mise à jour.", "Abre Google Play para ver las novedades e instalar la actualización.", "打开 Google Play 查看更新内容并安装更新。", "नया क्या है देखने और अपडेट इंस्टॉल करने के लिए Google Play खोलें।", "Откройте Google Play, чтобы посмотреть изменения и установить обновление.", "افتح صفحة Google Play لمراجعة الجديد وتثبيت التحديث.")
        "Open in Google Play" -> all("Ouvrir dans Google Play", "Abrir en Google Play", "在 Google Play 中打开", "Google Play में खोलें", "Открыть в Google Play", "فتح في Google Play")
        "No new update is currently available." -> all("Aucune nouvelle mise à jour n'est disponible pour le moment.", "No hay una nueva actualización disponible en este momento.", "当前没有新的更新可用。", "इस समय कोई नया अपडेट उपलब्ध नहीं है।", "Сейчас новое обновление недоступно.", "لا يوجد تحديث جديد حالياً.")
        "Appearance" -> all("Apparence", "Apariencia", "外观", "रूप", "Оформление", "المظهر")
        "System" -> all("Système", "Sistema", "系统", "सिस्टम", "Система", "النظام")
        "Light" -> all("Clair", "Claro", "浅色", "हल्का", "Светлая", "فاتح")
        "Dark" -> all("Sombre", "Oscuro", "深色", "गहरा", "Тёмная", "داكن")
        "Notification permission" -> all("Autorisation de notification", "Permiso de notificaciones", "通知权限", "नोटिफिकेशन अनुमति", "Разрешение уведомлений", "إذن الإشعارات")
        "On. Tap to review reminder settings." -> all("Activée. Appuyez pour vérifier les réglages.", "Activado. Toca para revisar los ajustes.", "已开启。点按查看提醒设置。", "चालू। रिमाइंडर सेटिंग देखने के लिए टैप करें।", "Включено. Нажмите, чтобы проверить настройки.", "مفعّل. اضغط لمراجعة إعدادات التذكير.")
        "Off. Enable it for reminders." -> all("Désactivée. Activez-la pour les rappels.", "Desactivado. Actívalo para recordatorios.", "已关闭。开启后才能提醒。", "बंद। रिमाइंडर के लिए अनुमति दें।", "Выключено. Включите для напоминаний.", "متوقف. فعّله للتذكيرات.")
        "Help" -> all("Aide", "Ayuda", "帮助", "मदद", "Помощь", "المساعدة")
        "Quickly control your closed-test process." -> all("Contrôlez rapidement votre test fermé.", "Controla rápidamente tu prueba cerrada.", "快速管理封闭测试流程。", "अपने closed test को तेज़ी से नियंत्रित करें।", "Быстро контролируйте закрытый тест.", "تحكم بسرعة في عملية الاختبار المغلق.")
        "Add an app, set the test day, enable Usage Access, and see refreshed minutes when you return. Tap the app icon on a card to open the tested app." -> all("Ajoutez une application, définissez le jour de test, activez l'accès à l'utilisation et retrouvez les minutes mises à jour en revenant. Touchez l'icône de l'application pour l'ouvrir.", "Añade una app, define el día de prueba, activa Acceso de uso y ve los minutos actualizados al volver. Toca el icono de la app para abrirla.", "添加应用、设置测试日、开启使用情况访问，返回后即可看到更新后的分钟数。点按卡片上的应用图标可打开测试应用。", "ऐप जोड़ें, टेस्ट दिन सेट करें, Usage Access चालू करें और वापस आने पर अपडेटेड मिनट देखें। टेस्ट ऐप खोलने के लिए कार्ड के ऐप आइकन पर टैप करें।", "Добавьте приложение, задайте день теста, включите доступ к статистике и при возврате увидите обновленные минуты. Нажмите значок приложения на карточке.", "أضف تطبيقاً، عيّن يوم الاختبار، فعّل إذن الاستخدام، وسترى الدقائق محدثة عند العودة. اضغط أيقونة التطبيق في البطاقة لفتحه.")
        "Get support" -> all("Obtenir de l'aide", "Obtener ayuda", "获取支持", "सहायता लें", "Получить поддержку", "الحصول على دعم")
        "App info" -> all("Infos application", "Información de la app", "应用信息", "ऐप जानकारी", "О приложении", "معلومات التطبيق")
        "Purpose" -> all("Objectif", "Objetivo", "用途", "उद्देश्य", "Назначение", "الغرض")
        "How to use" -> all("Utilisation", "Cómo usar", "如何使用", "कैसे उपयोग करें", "Как использовать", "طريقة الاستخدام")
        "Permissions and privacy" -> all("Autorisations et confidentialité", "Permisos y privacidad", "权限与隐私", "अनुमतियां और गोपनीयता", "Разрешения и конфиденциальность", "الأذونات والخصوصية")
        "Note" -> all("Note", "Nota", "说明", "नोट", "Примечание", "ملاحظة")
        "Legal information" -> all("Informations légales", "Información legal", "法律信息", "कानूनी जानकारी", "Правовая информация", "المعلومات القانونية")
        "Open page" -> all("Ouvrir la page", "Abrir página", "打开页面", "पेज खोलें", "Открыть страницу", "فتح الصفحة")
        "Support email" -> all("E-mail de support", "Correo de soporte", "支持邮箱", "सहायता ईमेल", "Почта поддержки", "بريد الدعم")
        "Donate / Buy me a coffee" -> all("Don / Offrir un café", "Donar / Invitar un café", "捐赠 / 请我喝咖啡", "दान / कॉफी", "Пожертвовать / кофе", "تبرع / اشترِ لي قهوة")
        "Full series" -> all("Série complète", "Serie completa", "完整系列", "पूरी सीरीज़", "Вся серия", "السلسلة كاملة")
        "Pending" -> all("En attente", "Pendiente", "等待中", "बाकी", "Ожидается", "قيد الانتظار")
        "All tracked apps and streak days" -> all("Toutes les apps suivies et jours de série", "Todas las apps seguidas y días de racha", "所有跟踪应用和连续天数", "सभी ट्रैक ऐप और streak days", "Все отслеживаемые приложения и дни серии", "كل التطبيقات المتابعة وأيام السلسلة")
        "No apps added yet" -> all("Aucune app ajoutée", "Aún no hay apps", "尚未添加应用", "अभी कोई ऐप नहीं जोड़ा", "Приложения еще не добавлены", "لم تتم إضافة تطبيقات بعد")
        "The summary screen fills up after apps are tracked." -> all("L'écran de résumé se remplit après l'ajout d'applications suivies.", "La pantalla de resumen se llenará cuando añadas apps seguidas.", "添加跟踪应用后，摘要页面会显示内容。", "ट्रैक किए गए ऐप जुड़ने पर सारांश स्क्रीन भर जाएगी।", "Сводка заполнится после добавления отслеживаемых приложений.", "تمتلئ شاشة الملخص بعد إضافة التطبيقات للمتابعة.")
        "Change the search or widen the filter." -> all("Changez la recherche ou élargissez le filtre.", "Cambia la búsqueda o amplía el filtro.", "更改搜索或扩大筛选范围。", "खोज बदलें या फ़िल्टर बढ़ाएँ।", "Измените поиск или расширьте фильтр.", "غيّر البحث أو وسّع الفلتر.")
        "Choose" -> all("Choisir", "Elegir", "选择", "चुनें", "Выбрать", "اختيار")
        "Choose language" -> all("Choisir la langue", "Elegir idioma", "选择语言", "भाषा चुनें", "Выберите язык", "اختر اللغة")
        "Theme" -> all("Thème", "Tema", "主题", "थीम", "Тема", "السمة")
        "Fresh" -> all("Vif", "Vivo", "鲜明", "ताज़ा", "Яркая", "منعش")
        "Ocean" -> all("Océan", "Océano", "海洋", "महासागर", "Океан", "المحيط")
        "Sunset" -> all("Coucher de soleil", "Atardecer", "日落", "सूर्यास्त", "Закат", "الغروب")
        "A-Z" -> all("A-Z", "A-Z", "A-Z", "A-Z", "A-Z", "أ-ي")
        "Name" -> all("Nom", "Nombre", "名称", "नाम", "Имя", "الاسم")
        "New" -> all("Nouveau", "Nuevo", "新", "नया", "Новое", "جديد")
        "Old" -> all("Ancien", "Antiguo", "旧", "पुराना", "Старое", "قديم")
        "Month.Day" -> all("Mois.Jour", "Mes.Día", "月.日", "माह.दिन", "Мес.День", "شهر.يوم")
        "Day.Month" -> all("Jour.Mois", "Día.Mes", "日.月", "दिन.माह", "День.Мес", "يوم.شهر")
        else -> null
    }
    return common?.get(language) ?: en
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
        AppLanguage.AR -> "العربية"
    }
}

private fun languageFlag(language: AppLanguage): String {
    return when (language) {
        AppLanguage.TR -> "\uD83C\uDDF9\uD83C\uDDF7"
        AppLanguage.EN -> "\uD83C\uDDEC\uD83C\uDDE7"
        AppLanguage.FR -> "\uD83C\uDDEB\uD83C\uDDF7"
        AppLanguage.ES -> "\uD83C\uDDEA\uD83C\uDDF8"
        AppLanguage.ZH -> "\uD83C\uDDE8\uD83C\uDDF3"
        AppLanguage.HI -> "\uD83C\uDDEE\uD83C\uDDF3"
        AppLanguage.RU -> "\uD83C\uDDF7\uD83C\uDDFA"
        AppLanguage.AR -> "\uD83C\uDDF8\uD83C\uDDE6"
    }
}

private fun languageDisplay(language: AppLanguage): String {
    return "${languageFlag(language)} ${languageLabel(language)}"
}

private fun systemLanguage(): AppLanguage {
    return when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
        "tr" -> AppLanguage.TR
        "fr" -> AppLanguage.FR
        "es" -> AppLanguage.ES
        "zh" -> AppLanguage.ZH
        "hi" -> AppLanguage.HI
        "ru" -> AppLanguage.RU
        "ar" -> AppLanguage.AR
        else -> AppLanguage.EN
    }
}

private fun languageModeLabel(mode: LanguageMode): String {
    return when (mode) {
        LanguageMode.SYSTEM -> "🌐 System (${languageLabel(systemLanguage())})"
        LanguageMode.TR -> languageDisplay(AppLanguage.TR)
        LanguageMode.EN -> languageDisplay(AppLanguage.EN)
        LanguageMode.FR -> languageDisplay(AppLanguage.FR)
        LanguageMode.ES -> languageDisplay(AppLanguage.ES)
        LanguageMode.ZH -> languageDisplay(AppLanguage.ZH)
        LanguageMode.HI -> languageDisplay(AppLanguage.HI)
        LanguageMode.RU -> languageDisplay(AppLanguage.RU)
        LanguageMode.AR -> languageDisplay(AppLanguage.AR)
    }
}

private fun languageModeCode(mode: LanguageMode): String {
    return when (mode) {
        LanguageMode.SYSTEM -> "AUTO"
        LanguageMode.TR -> "TR"
        LanguageMode.EN -> "EN"
        LanguageMode.FR -> "FR"
        LanguageMode.ES -> "ES"
        LanguageMode.ZH -> "ZH"
        LanguageMode.HI -> "HI"
        LanguageMode.RU -> "RU"
        LanguageMode.AR -> "AR"
    }
}

private fun languageModeTitle(mode: LanguageMode, uiLanguage: AppLanguage): String {
    return when (mode) {
        LanguageMode.SYSTEM -> text(
            uiLanguage,
            "Varsayılan sistem dili",
            "System default",
            "Langue du système",
            "Idioma del sistema",
            "系统默认",
            "सिस्टम डिफ़ॉल्ट",
            "Системный язык"
        )
        LanguageMode.TR -> "Türkçe"
        LanguageMode.EN -> "English"
        LanguageMode.FR -> "Français"
        LanguageMode.ES -> "Español"
        LanguageMode.ZH -> "中文"
        LanguageMode.HI -> "हिन्दी"
        LanguageMode.RU -> "Русский"
        LanguageMode.AR -> "العربية"
    }
}

private fun languageModeSubtitle(mode: LanguageMode, uiLanguage: AppLanguage): String {
    return when (mode) {
        LanguageMode.SYSTEM -> text(
            uiLanguage,
            "Desteklenmeyen sistem dillerinde English kullanılır.",
            "English is used when the system language is not supported.",
            "English est utilisé si la langue du système n'est pas prise en charge.",
            "Se usa English cuando el idioma del sistema no es compatible.",
            "系统语言不受支持时将使用 English。",
            "सिस्टम भाषा समर्थित नहीं होने पर English उपयोग होती है।",
            "Если язык системы не поддерживается, используется English."
        )
        LanguageMode.TR -> text(uiLanguage, "Uygulama arayüzü Türkçe olur.", "The app interface switches to Turkish.", "L'interface passe en turc.", "La interfaz cambia a turco.", "应用界面将切换为土耳其语。", "ऐप इंटरफ़ेस तुर्की में होगा।", "Интерфейс приложения будет на турецком.")
        LanguageMode.EN -> text(uiLanguage, "Uygulama arayüzü English olur.", "The app interface switches to English.", "L'interface passe en anglais.", "La interfaz cambia a inglés.", "应用界面将切换为英语。", "ऐप इंटरफ़ेस अंग्रेज़ी में होगा।", "Интерфейс приложения будет на английском.")
        LanguageMode.FR -> text(uiLanguage, "Uygulama arayüzü Français olur.", "The app interface switches to French.", "L'interface passe en français.", "La interfaz cambia a francés.", "应用界面将切换为法语。", "ऐप इंटरफ़ेस फ़्रेंच में होगा।", "Интерфейс приложения будет на французском.")
        LanguageMode.ES -> text(uiLanguage, "Uygulama arayüzü Español olur.", "The app interface switches to Spanish.", "L'interface passe en espagnol.", "La interfaz cambia a español.", "应用界面将切换为西班牙语。", "ऐप इंटरफ़ेस स्पेनिश में होगा।", "Интерфейс приложения будет на испанском.")
        LanguageMode.ZH -> text(uiLanguage, "Uygulama arayüzü 中文 olur.", "The app interface switches to Chinese.", "L'interface passe en chinois.", "La interfaz cambia a chino.", "应用界面将切换为中文。", "ऐप इंटरफ़ेस चीनी में होगा।", "Интерфейс приложения будет на китайском.")
        LanguageMode.HI -> text(uiLanguage, "Uygulama arayüzü हिन्दी olur.", "The app interface switches to Hindi.", "L'interface passe en hindi.", "La interfaz cambia a hindi.", "应用界面将切换为印地语。", "ऐप इंटरफ़ेस हिंदी में होगा।", "Интерфейс приложения будет на хинди.")
        LanguageMode.RU -> text(uiLanguage, "Uygulama arayüzü Русский olur.", "The app interface switches to Russian.", "L'interface passe en russe.", "La interfaz cambia a ruso.", "应用界面将切换为俄语。", "ऐप इंटरफ़ेस रूसी में होगा।", "Интерфейс приложения будет на русском.")
        LanguageMode.AR -> text(uiLanguage, "Uygulama arayüzü العربية olur.", "The app interface switches to Arabic.", "L'interface passe en arabe.", "La interfaz cambia a árabe.", "应用界面将切换为阿拉伯语。", "ऐप इंटरफ़ेस अरबी में होगा।", "Интерфейс приложения будет на арабском.", "ستتحول واجهة التطبيق إلى العربية.")
    }
}

private fun languageModeFieldLabel(mode: LanguageMode, uiLanguage: AppLanguage): String {
    return if (mode == LanguageMode.SYSTEM) {
        "${languageModeTitle(mode, uiLanguage)} (${languageLabel(systemLanguage())})"
    } else {
        languageModeTitle(mode, uiLanguage)
    }
}

private fun LanguageMode.resolvedLanguage(): AppLanguage {
    return when (this) {
        LanguageMode.SYSTEM -> systemLanguage()
        LanguageMode.TR -> AppLanguage.TR
        LanguageMode.EN -> AppLanguage.EN
        LanguageMode.FR -> AppLanguage.FR
        LanguageMode.ES -> AppLanguage.ES
        LanguageMode.ZH -> AppLanguage.ZH
        LanguageMode.HI -> AppLanguage.HI
        LanguageMode.RU -> AppLanguage.RU
        LanguageMode.AR -> AppLanguage.AR
    }
}

class MainActivity : ComponentActivity() {
    private val db by lazy { AppDatabase.get(this) }
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.schedule(this)
        } else {
            openNotificationSettings(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = runCatching {
            AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        }.getOrDefault(AppThemeMode.SYSTEM)
        val systemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val darkTheme = when (themeMode) {
            AppThemeMode.SYSTEM -> systemDark
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }
        setTheme(if (darkTheme) R.style.Theme_TesterApp_Dark else R.style.Theme_TesterApp_Light)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

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
                    onRequestNotificationPermission = { requestNotificationPermissionIfNeeded(force = true) },
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
                                    startDayIndex = day.coerceIn(1, 20),
                                    createdAtMillis = SeriesCalculator.dayStartMillis(0)
                                )
                            )
                            ClosedTestWidgetProvider.updateAll(this@MainActivity)
                        }
                    },
                    onUpdateStartDay = { pkg, day ->
                        lifecycleScope.launch {
                            db.appDao().resetSeries(
                                packageName = pkg,
                                createdAtMillis = SeriesCalculator.dayStartMillis(0),
                                startDayIndex = day.coerceIn(1, 20)
                            )
                            ClosedTestWidgetProvider.updateAll(this@MainActivity)
                        }
                    },
                    onArchiveApp = { pkg, archived ->
                        lifecycleScope.launch {
                            db.appDao().setArchived(pkg, archived)
                            ClosedTestWidgetProvider.updateAll(this@MainActivity)
                        }
                    },
                    onMarkCompleted = { pkg ->
                        lifecycleScope.launch {
                            db.appDao().setCompleted(pkg, System.currentTimeMillis())
                            ClosedTestWidgetProvider.updateAll(this@MainActivity)
                        }
                    },
                    onReactivateApp = { pkg ->
                        lifecycleScope.launch {
                            db.appDao().setCompleted(pkg, null)
                            db.appDao().setArchived(pkg, false)
                            ClosedTestWidgetProvider.updateAll(this@MainActivity)
                        }
                    },
                    onDeleteApp = { pkg ->
                        lifecycleScope.launch {
                            db.appDao().delete(pkg)
                            ClosedTestWidgetProvider.updateAll(this@MainActivity)
                        }
                    }
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded(force: Boolean = false) {
        if (hasNotificationPermission(this)) {
            ReminderScheduler.schedule(this)
            if (force) openNotificationSettings(this)
            return
        }
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun openUsageSettings(activity: Activity) {
    activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
}

private fun hasNotificationPermission(context: android.content.Context): Boolean {
    return Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun openNotificationSettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= 26) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onSuccess { return }
    }
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun showUpdateAvailableNotificationIfNeeded(
    context: android.content.Context,
    language: AppLanguage,
    availableVersionCode: Int
) {
    if (!hasNotificationPermission(context)) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (prefs.getInt(KEY_LAST_UPDATE_NOTIFICATION_CODE, -1) == availableVersionCode) return
    prefs.edit().putInt(KEY_LAST_UPDATE_NOTIFICATION_CODE, availableVersionCode).apply()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "app_updates"
    if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(channelId) == null) {
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                text(language, "Uygulama güncellemeleri", "App updates", "Mises à jour", "Actualizaciones", "应用更新", "ऐप अपडेट", "Обновления", "تحديثات التطبيق"),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = android.net.Uri.parse("market://details?id=${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val fallbackIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentIntent = PendingIntent.getActivity(
        context,
        2001,
        if (intent.resolveActivity(context.packageManager) != null) intent else fallbackIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    )

    val title = text(
        language,
        "Yeni güncelleme var",
        "New update available",
        "Nouvelle mise à jour disponible",
        "Nueva actualización disponible",
        "有新更新可用",
        "नया अपडेट उपलब्ध है",
        "Доступно новое обновление",
        "يتوفر تحديث جديد"
    )
    val body = text(
        language,
        "Closed Test Tracker için yeni sürüm hazır. Güncellemek için dokun.",
        "A new Closed Test Tracker version is ready. Tap to update.",
        "Une nouvelle version de Closed Test Tracker est prête. Touchez pour mettre à jour.",
        "Hay una nueva versión de Closed Test Tracker lista. Toca para actualizar.",
        "Closed Test Tracker 新版本已准备好。点按更新。",
        "Closed Test Tracker का नया संस्करण तैयार है। अपडेट के लिए टैप करें।",
        "Доступна новая версия Closed Test Tracker. Нажмите, чтобы обновить.",
        "إصدار جديد من Closed Test Tracker جاهز. اضغط للتحديث."
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    manager.notify(2001, notification)
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
    openExternalPage(context, DONATION_URL)
}

private fun openPolicyPage(context: android.content.Context) {
    openExternalPage(context, POLICY_URL)
}

private fun openExternalPage(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = android.net.Uri.parse(url)
        addCategory(Intent.CATEGORY_BROWSABLE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .recoverCatching { context.startActivity(intent) }
}

private fun openPlayStorePage(context: android.content.Context, packageName: String) {
    val marketIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(marketIntent) }
        .recoverCatching { context.startActivity(webIntent) }
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
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime
                )
            }.getOrNull()
        }
        .distinctBy { it.packageName }
        .toList()
}

private fun sortInstalledApps(apps: List<InstalledApp>, sortMode: SortMode): List<InstalledApp> {
    return when (sortMode) {
        SortMode.NAME -> apps.sortedWith(
            compareBy<InstalledApp> { it.label.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
        SortMode.NEWEST -> apps.sortedWith(
            compareByDescending<InstalledApp> { it.firstInstallTime }
                .thenByDescending { it.lastUpdateTime }
                .thenBy { it.label.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
        SortMode.OLDEST -> apps.sortedWith(
            compareBy<InstalledApp> { it.firstInstallTime }
                .thenBy { it.lastUpdateTime }
                .thenBy { it.label.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
    }
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
    onRequestNotificationPermission: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    observeTrackedApps: ((List<TrackedApp>) -> Unit) -> Unit,
    onTrackApp: (String, String, Int) -> Unit,
    onUpdateStartDay: (String, Int) -> Unit,
    onArchiveApp: (String, Boolean) -> Unit,
    onMarkCompleted: (String) -> Unit,
    onReactivateApp: (String) -> Unit,
    onDeleteApp: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    var languageMode by remember {
        mutableStateOf(
            runCatching {
                LanguageMode.valueOf(prefs.getString(KEY_LANGUAGE, LanguageMode.SYSTEM.name) ?: LanguageMode.SYSTEM.name)
            }.recoverCatching {
                LanguageMode.valueOf(
                    AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AppLanguage.EN.name) ?: AppLanguage.EN.name).name
                )
            }.getOrDefault(LanguageMode.SYSTEM)
        )
    }
    val language = languageMode.resolvedLanguage()
    var tracked by remember { mutableStateOf(emptyList<TrackedApp>()) }
    var showPicker by remember { mutableStateOf(false) }
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var homeFilter by rememberSaveable { mutableStateOf(HomeFilter.ACTIVE) }
    var reminderHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_HOUR, 20)) }
    var dateFormat by remember {
        mutableStateOf(
            runCatching {
                DateDisplayFormat.valueOf(prefs.getString(KEY_DATE_FORMAT, DateDisplayFormat.MONTH_DAY.name) ?: DateDisplayFormat.MONTH_DAY.name)
            }.getOrDefault(DateDisplayFormat.MONTH_DAY)
        )
    }
    var daySetupTarget by remember { mutableStateOf<DaySetupTarget?>(null) }
    var deleteTarget by remember { mutableStateOf<TrackedApp?>(null) }
    var confirmTarget by remember { mutableStateOf<Pair<ConfirmAction, TrackedApp>?>(null) }
    var selectedPackageName by rememberSaveable { mutableStateOf<String?>(null) }
    var showOverviewSheet by remember { mutableStateOf(false) }
    var usageAccess by remember { mutableStateOf(UsageReader.hasUsageAccess(context)) }
    var notificationAllowed by remember { mutableStateOf(hasNotificationPermission(context)) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var playUpdateState by remember { mutableStateOf(PlayUpdateState()) }
    var updatePromptVisible by remember { mutableStateOf(false) }
    val homeListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    val playPublishers = remember { mutableStateMapOf<String, String>() }
    val playPublisherRequested = remember { mutableStateMapOf<String, Boolean>() }
    val usageSummaryCache = remember { mutableStateMapOf<String, UsageSummary>() }
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    val appVersionName = remember {
        runCatching {
            val info = if (Build.VERSION.SDK_INT >= 33) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            info.versionName ?: "-"
        }.getOrDefault("-")
    }
    val selectedItem = tracked.firstOrNull { it.packageName == selectedPackageName }
    val topTitle = when (screen) {
        AppScreen.SETTINGS -> text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки")
        AppScreen.DETAIL -> selectedItem?.appLabel ?: text(language, "Detay", "Detail", "Détail", "Detalle", "详情", "विवरण", "Детали")
        AppScreen.HOME -> "Closed Test Tracker"
    }

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

    suspend fun refreshInstalledApps() {
        apps = withContext(Dispatchers.Default) {
            installedApps(context.packageManager)
        }
    }

    fun refreshPlayUpdateState() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                val isAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val availableVersionCode = if (isAvailable) appUpdateInfo.availableVersionCode() else null
                playUpdateState = playUpdateState.copy(
                    isAvailable = isAvailable,
                    availableVersionCode = availableVersionCode,
                    stalenessDays = if (isAvailable) appUpdateInfo.clientVersionStalenessDays() else null
                )
                if (isAvailable && availableVersionCode != null) {
                    showUpdateAvailableNotificationIfNeeded(
                        context = context,
                        language = language,
                        availableVersionCode = availableVersionCode
                    )
                }
                if (isAvailable && !playUpdateState.promptShown) {
                    updatePromptVisible = true
                    playUpdateState = playUpdateState.copy(promptShown = true)
                }
            }
            .addOnFailureListener {
                playUpdateState = playUpdateState.copy(isAvailable = false, availableVersionCode = null, stalenessDays = null)
            }
    }

    LaunchedEffect(Unit) { observeTrackedApps { tracked = it } }
    LaunchedEffect(Unit) { refreshInstalledApps() }
    LaunchedEffect(Unit) { refreshPlayUpdateState() }
    LaunchedEffect(refreshTick) {
        usageAccess = UsageReader.hasUsageAccess(context)
        notificationAllowed = hasNotificationPermission(context)
        refreshPlayUpdateState()
    }
    LaunchedEffect(usageAccess) {
        while (true) {
            delay(20_000)
            refreshTick++
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTick++
                lifecycleOwner.lifecycleScope.launch {
                    refreshInstalledApps()
                    delay(700)
                    refreshTick++
                    delay(1_800)
                    refreshTick++
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    DisposableEffect(Unit) {
        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                lifecycleOwner.lifecycleScope.launch {
                    delay(250)
                    refreshInstalledApps()
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            packageFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
    BackHandler(enabled = screen != AppScreen.HOME) {
        screen = AppScreen.HOME
        selectedPackageName = null
    }
    LaunchedEffect(showPicker) {
        while (showPicker) {
            refreshInstalledApps()
            delay(1_500)
        }
    }

    val activeTracked = tracked.filter { !it.isArchived && it.completedAtMillis == null }
    val completedTracked = tracked.filter { !it.isArchived && it.completedAtMillis != null }
    val archivedTracked = tracked.filter { it.isArchived }
    val filteredTracked = when (homeFilter) {
        HomeFilter.ACTIVE -> activeTracked
        HomeFilter.COMPLETED -> completedTracked
        HomeFilter.ARCHIVED -> archivedTracked
        HomeFilter.ALL -> tracked
    }
    val visibleTrackedApps = remember(tracked) {
        tracked.filterNot { it.isArchived }
    }
    val visibleUsagePackages = remember(tracked) {
        visibleTrackedApps.map { it.packageName }
    }
    var todayUsageCache by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var totalUsageCache by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    val todayUsageState by produceState(
        initialValue = TodayUsageState(
            minutesByPackage = todayUsageCache,
            isLoading = usageAccess && visibleUsagePackages.isNotEmpty() && todayUsageCache.isEmpty()
        ),
        usageAccess,
        refreshTick,
        visibleUsagePackages
    ) {
        value = if (!usageAccess || visibleUsagePackages.isEmpty()) {
            TodayUsageState(isLoading = false)
        } else {
            if (todayUsageCache.isNotEmpty()) {
                value = TodayUsageState(minutesByPackage = todayUsageCache, isLoading = false)
            }
            val fresh = withContext(Dispatchers.Default) {
                UsageReader.todayUsageMinutesMap(context, visibleUsagePackages)
            }
            todayUsageCache = fresh
            TodayUsageState(
                minutesByPackage = fresh,
                isLoading = false
            )
        }
    }
    val todayUsageMap = todayUsageState.minutesByPackage
    val todayUsageLoading = todayUsageState.isLoading
    val totalUsageState by produceState(
        initialValue = TodayUsageState(
            minutesByPackage = totalUsageCache,
            isLoading = usageAccess && visibleUsagePackages.isNotEmpty() && totalUsageCache.isEmpty()
        ),
        usageAccess,
        refreshTick,
        visibleTrackedApps
    ) {
        value = if (!usageAccess || visibleTrackedApps.isEmpty()) {
            TodayUsageState(isLoading = false)
        } else {
            if (totalUsageCache.isNotEmpty()) {
                value = TodayUsageState(minutesByPackage = totalUsageCache, isLoading = false)
            }
            val fresh = withContext(Dispatchers.Default) {
                val now = System.currentTimeMillis()
                visibleTrackedApps.associate { item ->
                    val currentDay = SeriesCalculator.currentDay(item)
                    if (currentDay <= 0) {
                        item.packageName to 0L
                    } else {
                        val seriesStart = SeriesCalculator.dayStartMillisForTestDay(item, 1)
                        val mergedDaily = UsageReader.mergedUsageMinutesByDayMap(
                            context = context,
                            packageName = item.packageName,
                            startMillis = seriesStart,
                            endMillis = now
                        )
                        val eventDaily = UsageReader.usageMinutesByDayMapFromEvents(
                            context = context,
                            packageName = item.packageName,
                            startMillis = seriesStart,
                            endMillis = now
                        )
                        val dailyTotal = mergedDaily.values.sum()
                        val eventTotal = eventDaily.values.sum()
                        val bestTotal = UsageReader.bestEffortTotalUsageMinutes(
                            context = context,
                            packageName = item.packageName,
                            seriesStartMillis = seriesStart,
                            dailyTotalMinutes = dailyTotal,
                            eventTotalMinutes = eventTotal
                        )
                        item.packageName to bestTotal
                    }
                }
            }
            totalUsageCache = fresh
            TodayUsageState(
                minutesByPackage = fresh,
                isLoading = false
            )
        }
    }
    val totalUsageMap = totalUsageState.minutesByPackage
    val totalUsageLoading = totalUsageState.isLoading
    val missingTodayApps = if (usageAccess && !todayUsageLoading) {
        activeTracked.filter { (todayUsageMap[it.packageName] ?: 0L) == 0L }
    } else {
        emptyList()
    }
    val usedTodayCount = if (usageAccess && !todayUsageLoading) activeTracked.count { (todayUsageMap[it.packageName] ?: 0L) > 0L } else 0
    val missingTodayCount = if (usageAccess && !todayUsageLoading) {
        activeTracked.count { (todayUsageMap[it.packageName] ?: 0L) == 0L }
    } else {
        0
    }
    val todayTotalMinutes = if (usageAccess && !todayUsageLoading) {
        visibleUsagePackages.sumOf { todayUsageMap[it] ?: 0L }
    } else {
        0L
    }

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
                            topTitle,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (screen == AppScreen.DETAIL) {
                        IconButton(onClick = {
                            screen = AppScreen.HOME
                            selectedPackageName = null
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = text(language, "Geri", "Back", "Retour", "Atrás", "返回", "वापस", "Назад", "رجوع"))
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
                                    Icon(Icons.Rounded.Add, contentDescription = text(language, "Uygulama ekle", "Add app", "Ajouter une app", "Añadir app", "添加应用", "ऐप जोड़ें", "Добавить приложение", "إضافة تطبيق"))
                                }
                                IconButton(onClick = { screen = AppScreen.SETTINGS }) {
                                    Icon(Icons.Rounded.Settings, contentDescription = text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки"))
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
            when (screen) {
                AppScreen.SETTINGS -> {
                    SettingsPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(p)
                            .padding(10.dp),
                        language = language,
                        appTheme = appTheme,
                        themeMode = themeMode,
                        darkTheme = darkTheme,
                        reminderHour = reminderHour,
                        dateFormat = dateFormat,
                        notificationAllowed = notificationAllowed,
                        languageMode = languageMode,
                        onLanguageChange = {
                            languageMode = it
                            prefs.edit().putString(KEY_LANGUAGE, it.name).apply()
                        },
                        onThemeChange = onThemeChange,
                        onThemeModeChange = onThemeModeChange,
                        onReminderHourChange = { hour ->
                            reminderHour = hour
                            prefs.edit()
                                .putInt(KEY_REMINDER_HOUR, hour)
                                .putInt(KEY_REMINDER_MINUTE, 0)
                                .apply()
                            ReminderScheduler.schedule(context)
                        },
                        onDateFormatChange = { format ->
                            dateFormat = format
                            prefs.edit().putString(KEY_DATE_FORMAT, format.name).apply()
                        },
                        appVersionName = appVersionName,
                        playUpdateState = playUpdateState,
                        onRequestNotificationPermission = onRequestNotificationPermission,
                        onSendMail = { sendSupportMail(context, language) },
                        onDonate = { openDonationPage(context) },
                        onOpenPolicyPage = { openPolicyPage(context) },
                        onOpenUpdate = { openPlayStorePage(context, context.packageName) }
                    )
                }

                AppScreen.DETAIL -> {
                    val item = selectedItem
                    if (item == null) {
                        EmptyState(language, onAdd = { screen = AppScreen.HOME })
                    } else {
                        val appInfo = remember(apps, item.packageName) {
                            apps.firstOrNull { it.packageName == item.packageName }
                        }
                        val usageSummary = rememberUsageSummaryAsync(
                            context = context,
                            item = item,
                            usageAccess = usageAccess,
                            refreshTick = refreshTick,
                            dateFormat = dateFormat,
                            rangeMode = UsageRangeMode.FULL,
                            initialSummary = usageSummaryCache[usageSummaryCacheKey(item, dateFormat, UsageRangeMode.FULL)],
                            onSummaryReady = { summary ->
                                usageSummaryCache[usageSummaryCacheKey(item, dateFormat, UsageRangeMode.FULL)] = summary
                            }
                        )
                        LaunchedEffect(item.packageName) {
                            ensurePlayPublisher(item.packageName)
                        }
                        DetailPage(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(p)
                                .padding(10.dp),
                            item = item,
                            icon = appInfo?.icon,
                            playPublisherName = playPublishers[item.packageName],
                            language = language,
                            usageDays = usageSummary.days,
                            totalMinutes = usageSummary.totalMinutes,
                            hasPartialDailyHistory = usageSummary.hasPartialDailyHistory,
                            isUsageLoading = usageSummary.isLoading,
                            onOpenApp = { openTrackedApp(context, item.packageName) },
                            onOpenPlayStore = { openPlayStorePage(context, item.packageName) },
                            onEditDay = {
                                daySetupTarget = DaySetupTarget(
                                    packageName = item.packageName,
                                    label = item.appLabel,
                                    initialDay = SeriesCalculator.currentDay(item),
                                    isNew = false
                                )
                            },
                            onMarkCompleted = { confirmTarget = ConfirmAction.FINISH to item },
                            onReactivate = { confirmTarget = ConfirmAction.REACTIVATE to item },
                            onArchive = { confirmTarget = (if (item.isArchived) ConfirmAction.RESTORE else ConfirmAction.ARCHIVE) to item },
                            onDelete = { deleteTarget = item }
                        )
                    }
                }

                AppScreen.HOME -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(p)
                            .padding(10.dp)
                    ) {
                        LazyColumn(
                            state = homeListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 130.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                DashboardHeader(
                                    language = language,
                                    usedTodayCount = usedTodayCount,
                                    missingTodayCount = missingTodayCount,
                                    completedCount = completedTracked.size,
                                    todayMinutes = todayTotalMinutes,
                                    onRefresh = { refreshTick++ },
                                    onOpenOverview = { showOverviewSheet = true }
                                )
                            }
                            if (usageAccess && missingTodayApps.isNotEmpty()) {
                                item {
                                    MissingTodayCard(
                                        language = language,
                                        apps = missingTodayApps
                                    )
                                }
                            }
                            item {
                                HomeFilterBar(
                                    language = language,
                                    selected = homeFilter,
                                    activeCount = activeTracked.size,
                                    completedCount = completedTracked.size,
                                    archivedCount = archivedTracked.size,
                                    allCount = tracked.size,
                                    onSelect = { homeFilter = it }
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
                            } else if (filteredTracked.isEmpty()) {
                                item {
                                    EmptyFilterState(language)
                                }
                            } else {
                                item {
                                    AppListCanvas(language = language, count = filteredTracked.size) {
                                        filteredTracked.forEachIndexed { index, item ->
                                            val appInfo = remember(apps, item.packageName) {
                                                apps.firstOrNull { it.packageName == item.packageName }
                                            }
                                            LaunchedEffect(item.packageName) {
                                                ensurePlayPublisher(item.packageName)
                                            }
                                            AppUsageCard(
                                                item = item,
                                                icon = appInfo?.icon,
                                                playPublisherName = playPublishers[item.packageName],
                                                language = language,
                                                hasUsageAccess = usageAccess,
                                                todayMinutes = if (usageAccess) todayUsageMap[item.packageName] else null,
                                                totalMinutes = if (usageAccess) totalUsageMap[item.packageName] else null,
                                                isTodayLoading = todayUsageLoading,
                                                isTotalLoading = totalUsageLoading,
                                                onOpenApp = { openTrackedApp(context, item.packageName) },
                                                onClick = {
                                                    selectedPackageName = item.packageName
                                                    screen = AppScreen.DETAIL
                                                }
                                            )
                                            if (index != filteredTracked.lastIndex) {
                                                CanvasDivider(Modifier.padding(start = 78.dp, end = 14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .zIndex(1f)
                        ) {
                            TestAdAreaCard(language = language)
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
                daySetupTarget = DaySetupTarget(app.packageName, app.label, 1, true)
            }
        )
    }

    if (showOverviewSheet) {
        AppOverviewSheet(
            language = language,
            tracked = tracked,
            usageAccess = usageAccess,
            todayUsageMap = todayUsageMap,
            installedAppsByPackage = apps.associateBy { it.packageName },
            playPublishers = playPublishers,
            onDismiss = { showOverviewSheet = false },
            onSelect = { packageName ->
                selectedPackageName = packageName
                screen = AppScreen.DETAIL
                showOverviewSheet = false
            }
        )
    }

    daySetupTarget?.let { target ->
        DaySetupDialog(
            language = language,
            target = target,
            onDismiss = { daySetupTarget = null },
            onConfirm = { day ->
                if (target.isNew) {
                    onTrackApp(target.packageName, target.label, day)
                } else {
                    onUpdateStartDay(target.packageName, day)
                }
                daySetupTarget = null
            }
        )
    }

    confirmTarget?.let { (action, target) ->
        val title = when (action) {
            ConfirmAction.FINISH -> text(language, "Testi bitir?", "Finish test?")
            ConfirmAction.REACTIVATE -> text(language, "Tekrar aktif edilsin mi?", "Reactivate?")
            ConfirmAction.ARCHIVE -> text(language, "Arşivlensin mi?", "Archive?")
            ConfirmAction.RESTORE -> text(language, "Arşivden çıkarılsın mı?", "Restore?")
        }
        val message = when (action) {
            ConfirmAction.FINISH -> text(
                language,
                "${target.appLabel} tamamlandı olarak işaretlenecek. İstersen daha sonra tekrar aktif edebilirsin.",
                "${target.appLabel} will be marked as completed. You can reactivate it later."
            )
            ConfirmAction.REACTIVATE -> text(
                language,
                "${target.appLabel} tekrar aktif listeye alınacak ve seri sayacı devam edecek.",
                "${target.appLabel} will return to the active list and the streak counter will continue."
            )
            ConfirmAction.ARCHIVE -> text(
                language,
                "${target.appLabel} arşive taşınacak. İstersen arşivden tekrar çıkarabilirsin.",
                "${target.appLabel} will be moved to archive. You can restore it later."
            )
            ConfirmAction.RESTORE -> text(
                language,
                "${target.appLabel} arşivden çıkarılıp tekrar listede görünecek.",
                "${target.appLabel} will be restored from archive and shown in the list again."
            )
        }
        val confirmLabel = when (action) {
            ConfirmAction.FINISH -> text(language, "Bitir", "Finish")
            ConfirmAction.REACTIVATE -> text(language, "Aktif et", "Reactivate")
            ConfirmAction.ARCHIVE -> text(language, "Arşivle", "Archive")
            ConfirmAction.RESTORE -> text(language, "Arşivden çıkar", "Restore")
        }

        ConfirmationSheet(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            dismissLabel = text(language, "İptal", "Cancel"),
            onDismiss = { confirmTarget = null },
            onConfirm = {
                when (action) {
                    ConfirmAction.FINISH -> onMarkCompleted(target.packageName)
                    ConfirmAction.REACTIVATE -> onReactivateApp(target.packageName)
                    ConfirmAction.ARCHIVE -> onArchiveApp(target.packageName, true)
                    ConfirmAction.RESTORE -> onArchiveApp(target.packageName, false)
                }
                when (action) {
                    ConfirmAction.ARCHIVE -> {
                        homeFilter = HomeFilter.ARCHIVED
                        screen = AppScreen.HOME
                        selectedPackageName = null
                    }
                    ConfirmAction.RESTORE -> {
                        homeFilter = if (target.completedAtMillis == null) HomeFilter.ACTIVE else HomeFilter.COMPLETED
                        screen = AppScreen.HOME
                        selectedPackageName = null
                    }
                    ConfirmAction.REACTIVATE -> {
                        homeFilter = HomeFilter.ACTIVE
                        screen = AppScreen.HOME
                        selectedPackageName = null
                    }
                    ConfirmAction.FINISH -> homeFilter = HomeFilter.COMPLETED
                }
                confirmTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        ConfirmationSheet(
            title = text(language, "Uygulamayı sil", "Delete app"),
            message = text(
                language,
                "${target.appLabel} listeden kaldırılsın mı? Kullanım geçmişi Android tarafında kalır, sadece takip kaydı silinir.",
                "Remove ${target.appLabel} from the list? Android usage history stays, only the tracking record is deleted."
            ),
            confirmLabel = text(language, "Sil", "Delete"),
            dismissLabel = text(language, "İptal", "Cancel"),
            destructive = true,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDeleteApp(target.packageName)
                deleteTarget = null
                screen = AppScreen.HOME
                selectedPackageName = null
            }
        )
    }

    if (updatePromptVisible && playUpdateState.isAvailable) {
        ConfirmationSheet(
            title = text(
                language,
                "Yeni güncelleme var",
                "New update available",
                "Nouvelle mise à jour disponible",
                "Nueva actualización disponible",
                "有新更新可用",
                "नया अपडेट उपलब्ध है",
                "Доступно новое обновление"
            ),
            message = text(
                language,
                buildString {
                    append("Closed Test Tracker için yeni bir sürüm bulundu.")
                    append(" İstersen şimdi Google Play üzerinden güncelleyebilirsin.")
                },
                buildString {
                    append("A new version of Closed Test Tracker is available.")
                    append(" You can update now through Google Play.")
                },
                "Une nouvelle version de Closed Test Tracker est disponible. Vous pouvez mettre à jour via Google Play.",
                "Hay una nueva versión de Closed Test Tracker disponible. Puedes actualizar desde Google Play.",
                "Closed Test Tracker 有新版本可用。你现在可以通过 Google Play 更新。",
                "Closed Test Tracker का नया संस्करण उपलब्ध है। आप अभी Google Play से अपडेट कर सकते हैं।",
                "Доступна новая версия Closed Test Tracker. Вы можете обновить приложение через Google Play."
            ),
            confirmLabel = text(language, "Güncelle", "Update", "Mettre à jour", "Actualizar", "更新", "अपडेट", "Обновить"),
            dismissLabel = text(language, "Daha sonra", "Later", "Plus tard", "Más tarde", "稍后", "बाद में", "Позже"),
            onDismiss = { updatePromptVisible = false },
            onConfirm = {
                updatePromptVisible = false
                openPlayStorePage(context, context.packageName)
            }
        )
    }
}

@Composable
private fun DashboardHeader(
    language: AppLanguage,
    usedTodayCount: Int,
    missingTodayCount: Int,
    completedCount: Int,
    todayMinutes: Long,
    onRefresh: () -> Unit,
    onOpenOverview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = canvasColor(), contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(1.dp, separatorColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text(language, "Takip özeti", "Tracking summary", ar = "ملخص المتابعة"),
                        color = secondaryTextColor(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text(language, "Uygulama özetleri", "App summaries", ar = "ملخصات التطبيقات"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = rowSurfaceColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, separatorColor()),
                        modifier = Modifier.clickable(onClick = onOpenOverview)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(text(language, "Özetler", "Summaries", ar = "الملخصات"), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Surface(
                        color = rowSurfaceColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, separatorColor()),
                        modifier = Modifier.clickable(onClick = onRefresh)
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(18.dp)
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStatChip(text(language, "Kullanılan", "Used"), usedTodayCount.toString(), Modifier.weight(1f))
                SummaryStatChip(text(language, "Eksik", "Missing", "Manquantes", "Faltantes", "缺少", "बाकी", "Не хватает", "ناقص"), missingTodayCount.toString(), Modifier.weight(1f))
                SummaryStatChip(text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم"), completedCount.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStatChip(text(language, "Bugün", "Today"), "$todayMinutes ${minuteLabel(language)}", Modifier.weight(1f))
                SummaryStatChip(text(language, "Seri", "Streak"), "14+", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HomeFilterBar(
    language: AppLanguage,
    selected: HomeFilter,
    activeCount: Int,
    completedCount: Int,
    archivedCount: Int,
    allCount: Int,
    onSelect: (HomeFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SortChip("${text(language, "Aktif", "Active", "Actif", "Activo", "活跃", "सक्रिय", "Активные", "نشط")} $activeCount", selected == HomeFilter.ACTIVE) {
            onSelect(HomeFilter.ACTIVE)
        }
        SortChip("${text(language, "Tamamlandı", "Done")} $completedCount", selected == HomeFilter.COMPLETED) {
            onSelect(HomeFilter.COMPLETED)
        }
        SortChip("${text(language, "Arşiv", "Archive")} $archivedCount", selected == HomeFilter.ARCHIVED) {
            onSelect(HomeFilter.ARCHIVED)
        }
        SortChip("${text(language, "Tümü", "All")} $allCount", selected == HomeFilter.ALL) {
            onSelect(HomeFilter.ALL)
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
private fun SummaryStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = rowSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, separatorColor())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(label, color = secondaryTextColor(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
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
                Text(text(language, "Uygulama seç", "Pick app", "Choisir une app", "Elegir app", "选择应用", "ऐप चुनें", "Выбрать приложение"))
            }
        }
    }
}

@Composable
private fun EmptyFilterState(language: AppLanguage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = panelColor(strong = true), contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(26.dp),
        border = panelBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text(language, "Bu filtre boş", "This filter is empty"), fontWeight = FontWeight.Bold)
            Text(
                text(language, "Başka bir filtre seç veya yeni uygulama ekle.", "Choose another filter or add a new app."),
                color = secondaryTextColor()
            )
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
    hasUsageAccess: Boolean,
    todayMinutes: Long?,
    totalMinutes: Long?,
    isTodayLoading: Boolean,
    isTotalLoading: Boolean,
    onOpenApp: () -> Unit,
    onClick: () -> Unit
) {
    val day = SeriesCalculator.currentDay(item)
    val usageLine = when {
        !hasUsageAccess -> text(language, "Kullanım izni gerekli", "Usage permission required", "Autorisation d'utilisation requise", "Se requiere permiso de uso", "需要使用情况权限", "Usage अनुमति आवश्यक है", "Требуется доступ к статистике")
        todayMinutes == null -> text(language, "Bugün yükleniyor", "Today loading", "Aujourd'hui en chargement", "Hoy cargando", "今天加载中", "आज लोड हो रहा है", "Сегодня загружается", "اليوم قيد التحميل")
        isTotalLoading -> text(
            language,
            "Bugün $todayMinutes ${minuteLabel(language)} | Toplam yükleniyor",
            "Today $todayMinutes ${minuteLabel(language)} | Total loading",
            "Aujourd'hui $todayMinutes ${minuteLabel(language)} | Total en chargement",
            "Hoy $todayMinutes ${minuteLabel(language)} | Total cargando",
            "今天 $todayMinutes ${minuteLabel(language)} | 总计加载中",
            "आज $todayMinutes ${minuteLabel(language)} | कुल लोड हो रहा है",
            "Сегодня $todayMinutes ${minuteLabel(language)} | Итого загружается",
            "اليوم $todayMinutes ${minuteLabel(language)} | المجموع قيد التحميل"
        )
        totalMinutes == null -> text(
            language,
            "Bugün $todayMinutes ${minuteLabel(language)} | Toplam yükleniyor",
            "Today $todayMinutes ${minuteLabel(language)} | Total loading",
            "Aujourd'hui $todayMinutes ${minuteLabel(language)} | Total en chargement",
            "Hoy $todayMinutes ${minuteLabel(language)} | Total cargando",
            "今天 $todayMinutes ${minuteLabel(language)} | 总计加载中",
            "आज $todayMinutes ${minuteLabel(language)} | कुल लोड हो रहा है",
            "Сегодня $todayMinutes ${minuteLabel(language)} | Итого загружается",
            "اليوم $todayMinutes ${minuteLabel(language)} | المجموع قيد التحميل"
        )
        else -> text(
            language,
            "Bugün $todayMinutes ${minuteLabel(language)} | Toplam $totalMinutes ${minuteLabel(language)}",
            "Today $todayMinutes ${minuteLabel(language)} | Total $totalMinutes ${minuteLabel(language)}",
            "Aujourd'hui $todayMinutes ${minuteLabel(language)} | Total $totalMinutes ${minuteLabel(language)}",
            "Hoy $todayMinutes ${minuteLabel(language)} | Total $totalMinutes ${minuteLabel(language)}",
            "今天 $todayMinutes ${minuteLabel(language)} | 总计 $totalMinutes ${minuteLabel(language)}",
            "आज $todayMinutes ${minuteLabel(language)} | कुल $totalMinutes ${minuteLabel(language)}",
            "Сегодня $todayMinutes ${minuteLabel(language)} | Итого $totalMinutes ${minuteLabel(language)}",
            "اليوم $todayMinutes ${minuteLabel(language)} | المجموع $totalMinutes ${minuteLabel(language)}"
        )
    }

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
                    usageLine,
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
                color = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Text(
                    when {
                        item.isArchived -> text(language, "Arşiv", "Archive")
                        item.completedAtMillis != null -> text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم")
                        else -> "$day/14"
                    },
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
    }
}

@Composable
private fun DetailPage(
    modifier: Modifier,
    item: TrackedApp,
    icon: Bitmap?,
    playPublisherName: String?,
    language: AppLanguage,
    usageDays: List<UsageDay>,
    totalMinutes: Long,
    hasPartialDailyHistory: Boolean,
    isUsageLoading: Boolean,
    onOpenApp: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onEditDay: () -> Unit,
    onMarkCompleted: () -> Unit,
    onReactivate: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val day = SeriesCalculator.currentDay(item)
    val today = usageDays.firstOrNull { it.isToday }?.minutes ?: 0L
    val todayLabel = if (isUsageLoading) text(language, "Yükleniyor", "Loading") else "$today ${minuteLabel(language)}"
    val totalLabel = if (isUsageLoading) text(language, "Yükleniyor", "Loading") else "$totalMinutes ${minuteLabel(language)}"
    val max = usageDays.maxOfOrNull { it.minutes } ?: 0L
    val hasUsageData = !isUsageLoading && usageDays.any { !it.isFuture && it.minutes > 0L }
    var viewMode by rememberSaveable(item.packageName) { mutableStateOf(DetailViewMode.GRAPH) }
    val detailListState = rememberSaveable(item.packageName, saver = LazyListState.Saver) { LazyListState() }

    LazyColumn(
        state = detailListState,
        modifier = modifier,
        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = canvasColor(), contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, separatorColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (icon != null) {
                            Image(
                                bitmap = icon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).clickable(onClick = onOpenApp)
                            )
                        }
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.appLabel, fontWeight = FontWeight.Bold, fontSize = 24.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                if (playPublisherName != null) {
                                    text(language, "Yayıncı: $playPublisherName", "Publisher: $playPublisherName")
                                } else {
                                    text(language, "Yayıncı bilgisi yükleniyor", "Publisher loading")
                                },
                                color = secondaryTextColor(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile(text(language, "Gün", "Day"), "$day/14", Modifier.weight(1f))
                        StatTile(text(language, "Bugün", "Today"), todayLabel, Modifier.weight(1f))
                        StatTile(text(language, "Toplam", "Total", "Total", "Total", "总计", "कुल", "Итого", "المجموع"), totalLabel, Modifier.weight(1f))
                    }

                    UsageBars(usageDays, max)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailActionButton(text(language, "Uygulamayı aç", "Open app"), Icons.AutoMirrored.Rounded.OpenInNew, onOpenApp, Modifier.weight(1f))
                        DetailActionButton(text(language, "Play Store", "Play Store"), Icons.Rounded.ShoppingBag, onOpenPlayStore, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = canvasColor(), contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, separatorColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text(language, "Yönetim", "Manage"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            when {
                                item.isArchived -> text(language, "Arşivde", "Archived")
                                item.completedAtMillis == null -> text(language, "Canlı", "Live")
                                else -> text(language, "Tamamlandı", "Done")
                            },
                            color = secondaryTextColor(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text(language, "Seri bitirmeden sayaç devam eder.", "The streak keeps running until you finish.", "La série continue jusqu'à ce que vous la terminiez.", "La racha sigue hasta que la finalices.", "在你结束之前，连续计数会继续。", "जब तक आप समाप्त नहीं करते, streak चलती रहती है।", "Серия продолжается, пока вы ее не завершите.", "تستمر السلسلة حتى تنهيها."),
                        color = secondaryTextColor(),
                        fontSize = 12.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        DetailActionButton(
                            text(language, "Günü ayarla", "Set day"),
                            Icons.Rounded.Refresh,
                            onEditDay,
                            modifier = Modifier.weight(1f),
                            compact = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (item.completedAtMillis == null) {
                            DetailActionButton(
                                text(language, "Bitir", "Finish"),
                                Icons.Rounded.Done,
                                onMarkCompleted,
                                modifier = Modifier.weight(1f),
                                compact = true
                            )
                        } else {
                            DetailActionButton(
                                text(language, "Tekrar aktif", "Reactivate"),
                                Icons.Rounded.Refresh,
                                onReactivate,
                                modifier = Modifier.weight(1f),
                                compact = true
                            )
                        }
                        DetailActionButton(
                            if (item.isArchived) text(language, "Arşivden çıkar", "Restore") else text(language, "Arşiv", "Archive"),
                            Icons.Rounded.Archive,
                            onArchive,
                            modifier = Modifier.weight(1f),
                            compact = true
                        )
                    }
                    DetailActionButton(
                        text(language, "Listeden sil", "Delete"),
                        Icons.Rounded.Delete,
                        onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        compact = true
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = canvasColor(), contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.dp, separatorColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val firstVisibleDay = usageDays.firstOrNull()?.index ?: 1
                        val lastVisibleDay = usageDays.lastOrNull()?.index ?: 14
                        val summaryTitle = if (firstVisibleDay == 1 && lastVisibleDay > 14) {
                            text(language, "1-$lastVisibleDay. gün özeti", "Day 1-$lastVisibleDay summary", "Résumé jours 1-$lastVisibleDay", "Resumen días 1-$lastVisibleDay", "第 1-$lastVisibleDay 天摘要", "दिन 1-$lastVisibleDay सारांश", "Сводка дней 1-$lastVisibleDay", "ملخص الأيام 1-$lastVisibleDay")
                        } else if (lastVisibleDay > 14) {
                            text(language, "$firstVisibleDay-$lastVisibleDay. gün özeti", "Day $firstVisibleDay-$lastVisibleDay summary", "Résumé jours $firstVisibleDay-$lastVisibleDay", "Resumen días $firstVisibleDay-$lastVisibleDay", "第 $firstVisibleDay-$lastVisibleDay 天摘要", "दिन $firstVisibleDay-$lastVisibleDay सारांश", "Сводка дней $firstVisibleDay-$lastVisibleDay", "ملخص الأيام $firstVisibleDay-$lastVisibleDay")
                        } else {
                            text(language, "14 günlük özet", "14-day summary", "Résumé 14 jours", "Resumen de 14 días", "14 天摘要", "14 दिन सारांश", "Сводка за 14 дней", "ملخص 14 يوماً")
                        }
                        Text(summaryTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip(
                                text(language, "Grafik", "Graph", "Graphique", "Gráfico", "图表", "ग्राफ", "График"),
                                viewMode == DetailViewMode.GRAPH
                            ) { viewMode = DetailViewMode.GRAPH }
                            ThemeChip(
                                text(language, "Yazılı", "Text", "Texte", "Texto", "文字", "टेक्स्ट", "Текст"),
                                viewMode == DetailViewMode.TEXT
                            ) { viewMode = DetailViewMode.TEXT }
                        }
                    }
                    Text(
                        text(language, "İstersen çizgi grafik, istersen yazılı özet görürsün.", "You can switch between a line chart and a written summary.", "Vous pouvez basculer entre un graphique linéaire et un résumé écrit.", "Puedes alternar entre gráfico de líneas y resumen escrito.", "你可以在线图和文字摘要之间切换。", "आप लाइन चार्ट और लिखित सारांश के बीच बदल सकते हैं।", "Можно переключаться между линейным графиком и текстовой сводкой.", "يمكنك التبديل بين الرسم الخطي والملخص النصي."),
                        color = secondaryTextColor(),
                        fontSize = 12.sp
                    )
                    if (hasPartialDailyHistory) {
                        Text(
                            text(
                                language,
                                "Android bu uygulama için eski günlerin günlük dökümünü eksik veriyor. Toplam süre geniş aralık toplamından hesaplanır; grafik ve yazılı özet sadece cihazın gün gün verdiği kayıtları gösterir.",
                                "Android returns incomplete daily history for this app. Total time is calculated from the broad range total; the graph and text summary show only daily records available from the device."
                            ),
                            color = secondaryTextColor(),
                            fontSize = 12.sp
                        )
                    }
                    if (isUsageLoading) {
                        EmptyDetailState(
                            title = text(language, "Kullanım yükleniyor", "Loading usage", "Chargement de l'utilisation", "Cargando uso", "正在加载使用情况", "उपयोग लोड हो रहा है", "Загрузка использования"),
                            body = text(language, "Günlük ve toplam süre cihazdan okunuyor.", "Daily and total time is being read from the device.", "Les durées quotidiennes et totales sont lues depuis l'appareil.", "El tiempo diario y total se está leyendo del dispositivo.", "正在从设备读取每日和总使用时间。", "दैनिक और कुल समय डिवाइस से पढ़ा जा रहा है।", "Ежедневное и общее время считывается с устройства.")
                        )
                    } else if (!hasUsageData) {
                        EmptyDetailState(
                            title = text(language, "Henüz kullanım verisi yok", "No usage data yet"),
                            body = text(
                                language,
                                if (today == 0L) "Bu uygulama için bugün veri görünmüyor. Kullanım izni açıksa uygulamayı açıp biraz bekle." else "Bu seri için henüz anlamlı kullanım verisi oluşmadı.",
                                if (today == 0L) "There is no data for today yet. If usage access is on, open the app and wait a bit." else "No meaningful usage data has been collected for this streak yet."
                            )
                        )
                    } else if (viewMode == DetailViewMode.GRAPH) {
                        UsageTimelineGrid(language = language, days = usageDays)
                    } else {
                        UsageTextSummary(language = language, days = usageDays)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = rowSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(if (compact) 18.dp else 20.dp),
        border = BorderStroke(1.dp, separatorColor())
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 12.dp else 14.dp, vertical = if (compact) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(if (compact) 18.dp else 20.dp))
            Text(
                label,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) 13.sp else 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationSheet(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    destructive: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = canvasColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.48f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 42.dp, height = 4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(separatorColor())
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(rowSurfaceColor())
                    .border(panelBorder(), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(message, color = secondaryTextColor(), lineHeight = 20.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rowSurfaceColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = panelBorder()
                ) {
                    Text(dismissLabel, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (destructive) {
                            MaterialTheme.colorScheme.error.copy(alpha = if (isDarkScheme()) 0.28f else 0.14f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.18f else 0.10f)
                        },
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (destructive) MaterialTheme.colorScheme.error.copy(alpha = 0.36f) else separatorColor()
                    )
                ) {
                    Text(confirmLabel, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxWidth().height(86.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val barHeight = if (maxMinutes == 0L) 8 else (8 + (day.minutes * 64 / maxMinutes)).toInt()
            Box(
                modifier = Modifier.weight(1f).height(86.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(5.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaySetupDialog(
    language: AppLanguage,
    target: DaySetupTarget,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val dayRange = 1..20
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val initialDay = target.initialDay.coerceIn(dayRange.first, dayRange.last)
    val selectedDay by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centered = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - center)
            }
            (centered?.index?.plus(1) ?: initialDay).coerceIn(dayRange.first, dayRange.last)
        }
    }

    LaunchedEffect(target) {
        scope.launch {
            listState.scrollToItem((initialDay - 1).coerceIn(0, dayRange.last - 1), scrollOffset = 62)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = canvasColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.48f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 54.dp, height = 5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.22f else 0.16f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = rowSurfaceColor(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, separatorColor())
                ) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(11.dp).size(22.dp)
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text(language, "Test günü", "Test day"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(
                        target.label,
                        color = secondaryTextColor(),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть"),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text(language, "Bugün bu uygulama testinin kaçıncı günü? 1 ile 20 arasında seç.", "Which test day is this app on today? Choose between 1 and 20."),
                color = secondaryTextColor()
            )

            Surface(
                color = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Column(
                    Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text(language, "Seçili gün", "Selected day"),
                                color = secondaryTextColor(),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                selectedDay.toString(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.14f else 0.10f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.24f else 0.18f)
                            )
                        ) {
                            Text(
                                text(language, "1 - 20", "1 - 20"),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = canvasColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, separatorColor())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(196.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            if (isDarkScheme()) Color(0xFF22252C) else Color(0xFFF6F4EE),
                                            if (isDarkScheme()) Color(0xFF171A20) else Color(0xFFECE7DE),
                                            if (isDarkScheme()) Color(0xFF20242B) else Color(0xFFE2DCCF)
                                        )
                                    )
                                )
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 66.dp)
                            ) {
                                items(dayRange.toList()) { day ->
                                    val selected = day == selectedDay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clickable {
                                                scope.launch {
                                                    listState.scrollToItem((day - 1).coerceIn(0, dayRange.last - 1), scrollOffset = 62)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = if (selected) 25.sp else 18.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f)
                                            },
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = if (selected) 1.08f else 0.92f
                                                scaleY = if (selected) 1.08f else 0.92f
                                            }
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                canvasColor().copy(alpha = 0.90f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                canvasColor().copy(alpha = 0.90f)
                                            )
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .padding(horizontal = 6.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.14f else 0.10f),
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = if (isDarkScheme()) 0.11f else 0.08f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.14f else 0.10f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.24f else 0.18f),
                                        RoundedCornerShape(18.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text(language, "İptal", "Cancel"))
                }
                Button(
                    onClick = { onConfirm(selectedDay) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text(language, "Kaydet", "Save", "Enregistrer", "Guardar", "保存", "सेव करें", "Сохранить", "حفظ"))
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(
    modifier: Modifier,
    language: AppLanguage,
    appTheme: AppTheme,
    themeMode: AppThemeMode,
    darkTheme: Boolean,
    reminderHour: Int,
    dateFormat: DateDisplayFormat,
    notificationAllowed: Boolean,
    languageMode: LanguageMode,
    onLanguageChange: (LanguageMode) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onReminderHourChange: (Int) -> Unit,
    onDateFormatChange: (DateDisplayFormat) -> Unit,
    appVersionName: String,
    playUpdateState: PlayUpdateState,
    onRequestNotificationPermission: () -> Unit,
    onSendMail: () -> Unit,
    onDonate: () -> Unit,
    onOpenPolicyPage: () -> Unit,
    onOpenUpdate: () -> Unit
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
                    LanguageDropdown(
                        language = language,
                        languageMode = languageMode,
                        onLanguageChange = onLanguageChange
                    )
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                    title = text(language, "Tarih biçimi", "Date format", "Format de date", "Formato de fecha", "日期格式", "तारीख़ फ़ॉर्मेट", "Формат даты")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text(
                                language,
                                "Grafikte ve liste özetlerinde tarih gösterimi buradan değişir.",
                                "Change the date display used in charts and lists.",
                                "Modifier l'affichage des dates dans les graphiques et les listes.",
                                "Cambiar el formato de fecha usado en gráficos y listas.",
                                "在图表和列表中更改日期显示方式。",
                                "चार्ट और सूचियों में तारीख़ दिखाने का तरीका बदलें।",
                                "Измените формат отображения дат в графиках и списках."
                            ),
                            color = secondaryTextColor()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip(dateFormatChipLabel(language, DateDisplayFormat.MONTH_DAY), dateFormat == DateDisplayFormat.MONTH_DAY) {
                                onDateFormatChange(DateDisplayFormat.MONTH_DAY)
                            }
                            ThemeChip(dateFormatChipLabel(language, DateDisplayFormat.DAY_MONTH), dateFormat == DateDisplayFormat.DAY_MONTH) {
                                onDateFormatChange(DateDisplayFormat.DAY_MONTH)
                            }
                        }
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = text(language, "Sürüm", "Version", "Version", "Versión", "版本", "संस्करण", "Версия")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text(
                                language,
                                "Yüklü sürüm: $appVersionName",
                                "Installed version: $appVersionName",
                                "Version installée : $appVersionName",
                                "Versión instalada: $appVersionName",
                                "已安装版本：$appVersionName",
                                "इंस्टॉल किया गया संस्करण: $appVersionName",
                                "Установленная версия: $appVersionName"
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (playUpdateState.isAvailable) {
                            Text(
                                text(
                                    language,
                                    "Yeni güncelleme var",
                                    "New update available",
                                    "Nouvelle mise à jour disponible",
                                    "Nueva actualización disponible",
                                    "有新更新可用",
                                    "नया अपडेट उपलब्ध है",
                                    "Доступно новое обновление"
                                ),
                                color = secondaryTextColor()
                            )
                            Text(
                                text(
                                    language,
                                    "Güncelleme ile gelen değişiklikleri görmek ve yüklemek için Google Play sayfasını aç.",
                                    "Open the Google Play page to review what's new and install the update.",
                                    "Ouvrez Google Play pour voir les nouveautés et installer la mise à jour.",
                                    "Abre Google Play para ver las novedades e instalar la actualización.",
                                    "打开 Google Play 查看更新内容并安装更新。",
                                    "नया क्या है देखने और अपडेट इंस्टॉल करने के लिए Google Play खोलें।",
                                    "Откройте Google Play, чтобы посмотреть список изменений и установить обновление."
                                ),
                                color = secondaryTextColor()
                            )
                            Button(
                                onClick = onOpenUpdate,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = rowSurfaceColor(),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = panelBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(text(language, "Google Play'de aç", "Open in Google Play", "Ouvrir dans Google Play", "Abrir en Google Play", "在 Google Play 中打开", "Google Play में खोलें", "Открыть в Google Play"))
                            }
                        } else {
                            Text(
                                text(
                                    language,
                                    "Yeni güncelleme görünmüyor.",
                                    "No new update is currently available.",
                                    "Aucune nouvelle mise à jour n'est disponible pour le moment.",
                                    "No hay una nueva actualización disponible en este momento.",
                                    "当前没有新的更新可用。",
                                    "इस समय कोई नया अपडेट उपलब्ध नहीं है।",
                                    "Сейчас новое обновление недоступно."
                                ),
                                color = secondaryTextColor()
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                    title = text(language, "Görünüm", "Appearance", "Apparence", "Apariencia", "外观", "रूप", "Оформление")
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChip(text(language, "Sistem", "System", "Système", "Sistema", "系统", "सिस्टम", "Система"), themeMode == AppThemeMode.SYSTEM) {
                            onThemeModeChange(AppThemeMode.SYSTEM)
                        }
                        ThemeChip(text(language, "Açık", "Light", "Clair", "Claro", "浅色", "हल्का", "Светлая"), themeMode == AppThemeMode.LIGHT) {
                            onThemeModeChange(AppThemeMode.LIGHT)
                        }
                        ThemeChip(text(language, "Koyu", "Dark", "Sombre", "Oscuro", "深色", "गहरा", "Тёмная"), themeMode == AppThemeMode.DARK) {
                            onThemeModeChange(AppThemeMode.DARK)
                        }
                    }
                    Text(
                        text(
                            language,
                            if (themeMode == AppThemeMode.SYSTEM) "Cihaz temasını takip ediyor." else if (darkTheme) "Koyu tema aktif." else "Açık tema aktif.",
                            if (themeMode == AppThemeMode.SYSTEM) "Following device theme." else if (darkTheme) "Dark theme is active." else "Light theme is active.",
                            if (themeMode == AppThemeMode.SYSTEM) "Suit le thème de l'appareil." else if (darkTheme) "Le thème sombre est actif." else "Le thème clair est actif.",
                            if (themeMode == AppThemeMode.SYSTEM) "Sigue el tema del dispositivo." else if (darkTheme) "El tema oscuro está activo." else "El tema claro está activo.",
                            if (themeMode == AppThemeMode.SYSTEM) "跟随设备主题。" else if (darkTheme) "深色主题已启用。" else "浅色主题已启用。",
                            if (themeMode == AppThemeMode.SYSTEM) "डिवाइस थीम का पालन कर रहा है।" else if (darkTheme) "डार्क थीम सक्रिय है।" else "लाइट थीम सक्रिय है।",
                            if (themeMode == AppThemeMode.SYSTEM) "Использует тему устройства." else if (darkTheme) "Тёмная тема активна." else "Светлая тема активна."
                        )
                    )
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                    title = text(language, "Bildirim saati", "Reminder time", "Heure du rappel", "Hora del recordatorio", "提醒时间", "रिमाइंडर समय", "Время напоминания")
                ) {
                    Text(
                        text(
                            language,
                            "Günlük seri kontrolü her gün yaklaşık ${reminderHour.toString().padStart(2, '0')}:00 için planlanır.",
                            "Daily series check is scheduled around ${reminderHour.toString().padStart(2, '0')}:00.",
                            "La vérification quotidienne de la série est planifiée vers ${reminderHour.toString().padStart(2, '0')}:00.",
                            "La comprobación diaria de la serie se programa alrededor de las ${reminderHour.toString().padStart(2, '0')}:00.",
                            "每日系列检查计划在约 ${reminderHour.toString().padStart(2, '0')}:00 运行。",
                            "दैनिक सीरीज़ जाँच लगभग ${reminderHour.toString().padStart(2, '0')}:00 पर नियोजित है।",
                            "Ежедневная проверка серии запланирована примерно на ${reminderHour.toString().padStart(2, '0')}:00."
                        ),
                        color = secondaryTextColor()
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(9, 12, 18).forEach { hour ->
                                ThemeChip("${hour.toString().padStart(2, '0')}:00", reminderHour == hour) {
                                    onReminderHourChange(hour)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(20, 22).forEach { hour ->
                                ThemeChip("${hour.toString().padStart(2, '0')}:00", reminderHour == hour) {
                                    onReminderHourChange(hour)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
                    title = text(language, "Bildirim izni", "Notification permission", "Autorisation de notification", "Permiso de notificaciones", "通知权限", "नोटिफिकेशन अनुमति", "Разрешение уведомлений"),
                    subtitle = if (notificationAllowed) {
                        text(language, "Açık. Hatırlatma ayarlarını görmek için dokun.", "On. Tap to review reminder settings.", "Activée. Appuyez pour vérifier les réglages.", "Activado. Toca para revisar los ajustes.", "已开启。点按查看提醒设置。", "चालू। रिमाइंडर सेटिंग देखने के लिए टैप करें।", "Включено. Нажмите, чтобы проверить настройки.")
                    } else {
                        text(language, "Kapalı. Hatırlatmalar için izni aç.", "Off. Enable it for reminders.", "Désactivée. Activez-la pour les rappels.", "Desactivado. Actívalo para recordatorios.", "已关闭。开启后才能提醒。", "बंद। रिमाइंडर के लिए अनुमति दें।", "Выключено. Включите для напоминаний.")
                    },
                    onClick = onRequestNotificationPermission
                )
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = null) },
                    title = text(language, "Yardım", "Help", "Aide", "Ayuda", "帮助", "मदद", "Помощь")
                ) {
                    Surface(
                        color = rowSurfaceColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, separatorColor())
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text(
                                    language,
                                    "Kapalı test sürecini hızlı kontrol et.",
                                    "Quickly control your closed-test process.",
                                    "Contrôlez rapidement votre test fermé.",
                                    "Controla rápidamente tu prueba cerrada.",
                                    "快速管理封闭测试流程。",
                                    "अपने closed test को तेज़ी से नियंत्रित करें।",
                                    "Быстро контролируйте закрытый тест."
                                ),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text(
                                    language,
                                    "Uygulama ekle, test gününü ayarla, kullanım iznini aç ve geri döndüğünde süreleri otomatik güncel gör. Karttaki uygulama ikonuna dokunarak test ettiğin uygulamayı açabilirsin.",
                                    "Add an app, set the test day, enable Usage Access, and see refreshed minutes when you return. Tap the app icon on a card to open the tested app.",
                                    "Ajoutez une application, définissez le jour de test, activez l'accès à l'utilisation et retrouvez les minutes mises à jour en revenant. Touchez l'icône de l'application pour l'ouvrir.",
                                    "Añade una app, define el día de prueba, activa Acceso de uso y ve los minutos actualizados al volver. Toca el icono de la app para abrirla.",
                                    "添加应用、设置测试日、开启使用情况访问，返回后即可看到更新后的分钟数。点按卡片上的应用图标可打开测试应用。",
                                    "ऐप जोड़ें, टेस्ट दिन सेट करें, Usage Access चालू करें और वापस आने पर अपडेटेड मिनट देखें। टेस्ट ऐप खोलने के लिए कार्ड के ऐप आइकन पर टैप करें।",
                                    "Добавьте приложение, задайте день теста, включите доступ к статистике и при возврате увидите обновленные минуты. Нажмите значок приложения на карточке, чтобы открыть тестируемое приложение."
                                ),
                                color = secondaryTextColor()
                            )
                            Button(
                                onClick = onSendMail,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(text(language, "Destek al", "Get support", "Obtenir de l'aide", "Obtener ayuda", "获取支持", "सहायता लें", "Получить поддержку"))
                            }
                        }
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = text(language, "Uygulama hakkında", "App info", "Infos application", "Información de la app", "应用信息", "ऐप जानकारी", "О приложении")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Closed Test Tracker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text(
                                language,
                                "Sürüm $appVersionName",
                                "Version $appVersionName",
                                "Version $appVersionName",
                                "Versión $appVersionName",
                                "版本 $appVersionName",
                                "संस्करण $appVersionName",
                                "Версия $appVersionName"
                            ),
                            color = secondaryTextColor()
                        )
                        InfoBullet(
                            title = text(language, "Amaç", "Purpose", "Objectif", "Objetivo", "用途", "उद्देश्य", "Назначение"),
                            body = text(
                                language,
                                "Google Play kapalı testlerini düzenli sürdürmek, 14 günlük test serisini ve günlük kullanım dakikalarını tek ekranda takip etmek için geliştirildi.",
                                "Built to keep Google Play closed tests organized and track the 14-day streak with daily usage minutes in one place.",
                                "Conçue pour organiser les tests fermés Google Play et suivre la série de 14 jours avec les minutes quotidiennes au même endroit.",
                                "Creada para organizar pruebas cerradas de Google Play y seguir la racha de 14 días con minutos diarios en un solo lugar.",
                                "用于整理 Google Play 封闭测试，并在一个位置跟踪 14 天连续测试和每日使用分钟数。",
                                "Google Play closed tests को व्यवस्थित रखने और 14-दिन की streak को दैनिक उपयोग मिनटों के साथ एक जगह ट्रैक करने के लिए बनाया गया।",
                                "Создано для организации закрытых тестов Google Play и отслеживания 14-дневной серии с ежедневными минутами в одном месте."
                            )
                        )
                        InfoBullet(
                            title = text(language, "Nasıl kullanılır?", "How to use", "Utilisation", "Cómo usar", "如何使用", "कैसे उपयोग करें", "Как использовать"),
                            body = text(
                                language,
                                "Üstteki + tuşuyla test uygulamasını seç. Günü 1'den başlatabilir veya mevcut test gününü ayarlayabilirsin. Sonra karttaki ikona dokunup uygulamayı aç, geri dönünce süreler otomatik yenilenir.",
                                "Use the + button to pick a test app. Start from day 1 or set the current test day. Then tap the app icon on the card to open it; when you return, minutes refresh automatically.",
                                "Utilisez le bouton + pour choisir une application de test. Commencez au jour 1 ou définissez le jour actuel. Touchez ensuite l'icône de la carte pour ouvrir l'application ; au retour, les minutes se mettent à jour automatiquement.",
                                "Usa el botón + para elegir una app de prueba. Empieza desde el día 1 o ajusta el día actual. Luego toca el icono de la tarjeta para abrirla; al volver, los minutos se actualizan automáticamente.",
                                "使用顶部 + 按钮选择测试应用。可以从第 1 天开始，也可以设置当前测试日。然后点按卡片图标打开应用；返回后分钟数会自动刷新。",
                                "+ बटन से टेस्ट ऐप चुनें। दिन 1 से शुरू करें या वर्तमान टेस्ट दिन सेट करें। फिर कार्ड के आइकन पर टैप करके ऐप खोलें; वापस आने पर मिनट अपने आप अपडेट होंगे।",
                                "Нажмите +, чтобы выбрать тестируемое приложение. Начните с 1-го дня или задайте текущий день теста. Затем нажмите значок приложения на карточке; при возврате минуты обновятся автоматически."
                            )
                        )
                        InfoBullet(
                            title = text(language, "İzinler ve gizlilik", "Permissions and privacy", "Autorisations et confidentialité", "Permisos y privacidad", "权限与隐私", "अनुमतियां और गोपनीयता", "Разрешения и конфиденциальность"),
                            body = text(
                                language,
                                "Kullanım erişimi sadece seçtiğin uygulamaların sürelerini okumak için kullanılır. Uygulama listesi ve kullanım verileri cihazında kalır; reklam veya analiz amacıyla paylaşılmaz.",
                                "Usage Access is used only to read minutes for apps you choose. The app list and usage data stay on your device and are not shared for ads or analytics.",
                                "L'accès à l'utilisation sert uniquement à lire les minutes des applications choisies. La liste et les données restent sur votre appareil et ne sont pas partagées pour la publicité ou l'analyse.",
                                "El Acceso de uso solo se usa para leer los minutos de las apps que eliges. La lista y los datos permanecen en tu dispositivo y no se comparten para anuncios ni análisis.",
                                "使用情况访问仅用于读取你选择的应用分钟数。应用列表和使用数据保留在设备上，不会用于广告或分析共享。",
                                "Usage Access केवल चुने गए ऐप्स के मिनट पढ़ने के लिए उपयोग होता है। ऐप सूची और उपयोग डेटा आपके डिवाइस पर रहता है; विज्ञापन या analytics के लिए साझा नहीं किया जाता।",
                                "Доступ к статистике используется только для чтения минут выбранных приложений. Список приложений и данные остаются на устройстве и не передаются для рекламы или аналитики."
                            )
                        )
                        InfoBullet(
                            title = text(language, "Not", "Note", "Note", "Nota", "说明", "नोट", "Примечание"),
                            body = text(
                                language,
                                "Web/PWA kısayollarında süre bazen tarayıcıya yazılabilir. Android site bazlı kullanım süresini ayrı uygulama gibi vermediği için bazı web tarzı uygulamalar 0 dk görünebilir.",
                                "For web/PWA shortcuts, time may be counted under the browser. Android does not expose per-site usage as separate apps, so some web-style apps may show 0 min.",
                                "Pour les raccourcis web/PWA, le temps peut être compté sous le navigateur. Android n'expose pas l'usage par site comme une application séparée, donc certaines apps web peuvent afficher 0 min.",
                                "En accesos web/PWA, el tiempo puede contarse en el navegador. Android no expone el uso por sitio como apps separadas, por eso algunas apps web pueden mostrar 0 min.",
                                "对于 Web/PWA 快捷方式，时间可能会计入浏览器。Android 不会把网站使用时间作为独立应用提供，因此某些网页类应用可能显示 0 分钟。",
                                "Web/PWA shortcuts में समय browser के अंतर्गत गिना जा सकता है। Android site-wise usage को अलग app की तरह नहीं देता, इसलिए कुछ web-style apps 0 मिनट दिखा सकते हैं।",
                                "Для web/PWA-ярлыков время может учитываться в браузере. Android не показывает использование по сайтам как отдельные приложения, поэтому некоторые веб-приложения могут показывать 0 мин."
                            )
                        )
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = text(language, "Yasal bilgiler", "Legal information", "Informations légales", "Información legal", "法律信息", "कानूनी जानकारी", "Правовая информация")
                ) {
                    Button(
                        onClick = onOpenPolicyPage,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = rowSurfaceColor(),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = panelBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(text(language, "Sayfayı aç", "Open page", "Ouvrir la page", "Abrir página", "打开页面", "पेज खोलें", "Открыть страницу"))
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                    title = text(language, "Destek e-postası", "Support email", "E-mail de support", "Correo de soporte", "支持邮箱", "सहायता ईमेल", "Почта поддержки"),
                    subtitle = SUPPORT_MAIL,
                    onClick = onSendMail
                )
            }
        }
        item {
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
                    title = text(language, "Bağış / Kahve ısmarla", "Donate / Buy me a coffee", "Don / Offrir un café", "Donar / Invitar un café", "捐赠 / 请我喝咖啡", "दान / कॉफी", "Пожертвовать / кофе"),
                    subtitle = DONATION_URL,
                    onClick = onDonate
                )
            }
        }
    }
}

@Composable
private fun InfoBullet(
    title: String,
    body: String
) {
    Surface(
        color = rowSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, separatorColor())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = secondaryTextColor(), fontSize = 13.sp)
        }
    }
}

@Composable
private fun UsageTimelineGrid(language: AppLanguage, days: List<UsageDay>) {
    val visibleDays = days.filterNot { it.isFuture }
    val maxMinutes = maxOf(1L, visibleDays.maxOfOrNull { it.minutes } ?: 0L)
    val dark = isDarkScheme()
    val gridColor = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = if (dark) 0.82f else 0.72f)
    val pointColor = MaterialTheme.colorScheme.tertiary
    val minLabel = minuteLabel(language)
    val firstVisibleDay = days.firstOrNull()?.index ?: 1
    val lastVisibleDay = days.lastOrNull()?.index ?: 14
    val rangeLabel = if (firstVisibleDay == 1 && lastVisibleDay > 14) {
        text(
            language,
            "Tüm seri",
            "Full series",
            "Série complète",
            "Serie completa",
            "完整系列",
            "पूरी सीरीज़",
            "Вся серия",
            "السلسلة كاملة"
        )
    } else if (lastVisibleDay > 14) {
        text(language, "Son 14 gün", "Last 14 days", "14 derniers jours", "Últimos 14 días", "最近 14 天", "पिछले 14 दिन", "Последние 14 дней", "آخر 14 يوماً")
    } else {
        text(language, "14 gün", "14 days", "14 jours", "14 días", "14 天", "14 दिन", "14 дней", "14 يوماً")
    }
    val xLabels = visibleDays.mapIndexedNotNull { index, day ->
        val step = maxOf(1, visibleDays.size / 4)
        if (index % step == 0 || index == visibleDays.lastIndex) day.label else null
    }
    val yMarks = listOf(maxMinutes, maxMinutes / 2, 0L).distinct().sortedDescending()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                color = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Text(
                    text = text(language, "Dakika", "Minutes", "Minutes", "Minutos", "分钟", "मिनट", "Минуты", "الدقائق"),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Surface(
                color = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Text(
                    text = rangeLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.width(42.dp).height(220.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yMarks.forEach { mark ->
                    Text(
                        text = if (mark == 0L) "0 $minLabel" else "$mark $minLabel",
                        color = secondaryTextColor(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(220.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (dark) Color(0xFF1E2229) else Color(0xFFF7F5EF),
                                if (dark) Color(0xFF171A20) else Color(0xFFF0EBDD)
                            )
                        )
                    )
                    .border(1.dp, separatorColor(), RoundedCornerShape(22.dp))
            ) {
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    val leftPad = 18f
                    val topPad = 18f
                    val rightPad = 18f
                    val bottomPad = 24f
                    val chartWidth = size.width - leftPad - rightPad
                    val chartHeight = size.height - topPad - bottomPad
                    val stepX = if (visibleDays.size <= 1) 0f else chartWidth / (visibleDays.size - 1)

                    yMarks.forEach { mark ->
                            val ratio = mark.toFloat() / maxMinutes.toFloat()
                            val y = topPad + (chartHeight * (1f - ratio))
                            drawLine(
                                color = gridColor,
                            start = Offset(leftPad, y),
                            end = Offset(size.width - rightPad, y),
                            strokeWidth = 1.2f
                        )
                    }

                    val plotted = visibleDays.mapIndexed { index, day ->
                        val x = if (visibleDays.size <= 1) size.width / 2f else leftPad + (stepX * index)
                        val ratio = if (day.isFuture) 0f else day.minutes.toFloat() / maxMinutes.toFloat()
                        val y = topPad + (chartHeight * (1f - ratio.coerceIn(0f, 1f)))
                        Offset(x, y)
                    }

                    for (i in 0 until plotted.lastIndex) {
                        drawLine(
                            color = lineColor,
                            start = plotted[i],
                            end = plotted[i + 1],
                            strokeWidth = 5f
                        )
                    }

                    plotted.forEachIndexed { index, point ->
                        drawCircle(
                            color = pointColor,
                            radius = if (visibleDays[index].isToday) 7.5f else 5.5f,
                            center = point
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = if (dark) 0.22f else 0.30f),
                            radius = if (visibleDays[index].isToday) 4.2f else 3.0f,
                            center = point
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 42.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                                text = label,
                                color = secondaryTextColor(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UsageTextSummary(language: AppLanguage, days: List<UsageDay>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { day ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text(
                            text = day.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (day.isFuture) "-" else text(language, "Gün ${day.index}", "Day ${day.index}", "Jour ${day.index}", "Día ${day.index}", "第 ${day.index} 天", "दिन ${day.index}", "День ${day.index}", "اليوم ${day.index}"),
                            color = secondaryTextColor(),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = if (day.isFuture) {
                            text(language, "Bekleniyor", "Pending", "En attente", "Pendiente", "等待中", "बाकी", "Ожидается", "قيد الانتظار")
                        } else {
                            "${day.minutes} ${minuteLabel(language)}"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = secondaryTextColor(),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDetailState(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = rowSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, separatorColor())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(body, color = secondaryTextColor(), fontSize = 13.sp)
        }
    }
}

@Composable
private fun MissingTodayCard(language: AppLanguage, apps: List<TrackedApp>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = canvasColor(), contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, separatorColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text(language, "Bugün eksik olanlar", "Missing today", ar = "الناقص اليوم"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${apps.size}", color = secondaryTextColor(), fontWeight = FontWeight.Bold)
            }
            Text(
                text(language, "Bugün açılmayan uygulamalar aşağıda.", "Apps not opened today are listed below.", ar = "التطبيقات التي لم تُفتح اليوم تظهر أدناه."),
                color = secondaryTextColor(),
                fontSize = 12.sp
            )
            apps.take(3).forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appLabel,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = app.packageName,
                        color = secondaryTextColor(),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppOverviewSheet(
    language: AppLanguage,
    tracked: List<TrackedApp>,
    usageAccess: Boolean,
    todayUsageMap: Map<String, Long>,
    installedAppsByPackage: Map<String, InstalledApp>,
    playPublishers: Map<String, String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(OverviewFilter.ALL) }
    val activeCount = tracked.count { !it.isArchived && it.completedAtMillis == null }
    val completedCount = tracked.count { !it.isArchived && it.completedAtMillis != null }
    val archivedCount = tracked.count { it.isArchived }
    val filteredTracked = remember(tracked, query, filter) {
        val q = query.trim().lowercase()
        tracked.filter { item ->
            val matchesQuery = q.isBlank() || item.appLabel.lowercase().contains(q) || item.packageName.lowercase().contains(q)
            val matchesFilter = when (filter) {
                OverviewFilter.ALL -> true
                OverviewFilter.ACTIVE -> !item.isArchived && item.completedAtMillis == null
                OverviewFilter.COMPLETED -> !item.isArchived && item.completedAtMillis != null
                OverviewFilter.ARCHIVED -> item.isArchived
            }
            matchesQuery && matchesFilter
        }.sortedWith(
            compareByDescending<TrackedApp> { !it.isArchived }
                .thenByDescending { it.completedAtMillis == null }
                .thenByDescending { SeriesCalculator.currentDay(it) }
                .thenBy { it.appLabel.lowercase() }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = canvasColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.48f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 54.dp, height = 5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.22f else 0.16f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text(language, "Uygulama özetleri", "App summaries"), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text(language, "Tüm takip edilen uygulamalar ve seri günleri", "All tracked apps and streak days"),
                        color = secondaryTextColor()
                    )
                }
                Surface(
                    color = rowSurfaceColor(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, separatorColor()),
                    modifier = Modifier.clickable(onClick = onDismiss)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть"),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp).size(18.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStatChip(text(language, "Aktif", "Active", "Actif", "Activo", "活跃", "सक्रिय", "Активные", "نشط"), activeCount.toString(), Modifier.weight(1f))
                SummaryStatChip(text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم"), completedCount.toString(), Modifier.weight(1f))
                SummaryStatChip(text(language, "Arşiv", "Archive"), archivedCount.toString(), Modifier.weight(1f))
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(text(language, "Ara", "Search", "Rechercher", "Buscar", "搜索", "खोजें", "Поиск")) },
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
                SortChip(text(language, "Tümü", "All"), filter == OverviewFilter.ALL) { filter = OverviewFilter.ALL }
                SortChip(text(language, "Aktif", "Active", "Actif", "Activo", "活跃", "सक्रिय", "Активные", "نشط"), filter == OverviewFilter.ACTIVE) { filter = OverviewFilter.ACTIVE }
                SortChip(text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم"), filter == OverviewFilter.COMPLETED) { filter = OverviewFilter.COMPLETED }
                SortChip(text(language, "Arşiv", "Archive"), filter == OverviewFilter.ARCHIVED) { filter = OverviewFilter.ARCHIVED }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = rowSurfaceColor(), contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, separatorColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (tracked.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text(language, "Henüz uygulama eklenmedi", "No apps added yet"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text(language, "Özet ekranı, takip edilen uygulamalar eklenince dolacak.", "The summary screen fills up after apps are tracked."), color = secondaryTextColor())
                    }
                } else if (filteredTracked.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text(language, "Sonuç yok", "No results", "Aucun résultat", "Sin resultados", "无结果", "कोई परिणाम नहीं", "Нет результатов"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text(language, "Aramayı değiştir ya da filtreyi genişlet.", "Change the search or widen the filter."),
                            color = secondaryTextColor()
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = filteredTracked,
                            key = { it.packageName }
                        ) { item ->
                            val day = SeriesCalculator.currentDay(item)
                            val today = if (usageAccess) todayUsageMap[item.packageName] ?: 0L else 0L
                            val publisher = playPublishers[item.packageName]
                            val installedApp = installedAppsByPackage[item.packageName]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(item.packageName) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (installedApp != null) {
                                    Image(
                                        bitmap = installedApp.icon.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (item.isArchived) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.08f else 0.045f))
                                    )
                                }
                                Spacer(Modifier.size(12.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        item.appLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                    text = buildString {
                        append(
                            when {
                                item.isArchived -> text(language, "Arşiv", "Archive")
                                item.completedAtMillis != null -> text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم")
                                else -> "$day/14"
                            }
                        )
                        if (usageAccess) append("  •  ").append(today).append(" ").append(minuteLabel(language))
                        if (!publisher.isNullOrBlank()) append("  •  ").append(publisher)
                    },
                                        color = secondaryTextColor(),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
            Surface(
                color = when {
                    item.isArchived -> rowSurfaceColor()
                    item.completedAtMillis != null -> rowSurfaceColor()
                    else -> rowSurfaceColor()
                },
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, separatorColor())
                                ) {
                                    Text(
                                        text = when {
                                            item.isArchived -> text(language, "Arşiv", "Archive")
                                            item.completedAtMillis != null -> text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم")
                                            else -> "$day/14"
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            CanvasDivider(Modifier.padding(start = 66.dp, end = 12.dp))
                        }
                    }
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
private fun LanguageDropdown(
    language: AppLanguage,
    languageMode: LanguageMode,
    onLanguageChange: (LanguageMode) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showSheet = true },
        color = rowSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(22.dp),
        border = panelBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = languageModeFlag(languageMode),
                fontSize = 24.sp
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text(language, "Dil", "Language", "Langue", "Idioma", "语言", "भाषा", "Язык", "اللغة"),
                    color = secondaryTextColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    languageModeFieldLabel(languageMode, language),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text(language, "Seç", "Choose", "Choisir", "Elegir", "选择", "चुनें", "Выбрать", "اختيار"),
                color = secondaryTextColor(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }

    if (showSheet) {
        LanguagePickerSheet(
            language = language,
            languageMode = languageMode,
            onDismiss = { showSheet = false },
            onLanguageChange = {
                onLanguageChange(it)
                showSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerSheet(
    language: AppLanguage,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onLanguageChange: (LanguageMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = canvasColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.48f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.22f else 0.16f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(rowSurfaceColor())
                    .border(panelBorder(), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text(language, "Dil seç", "Choose language", "Choisir la langue", "Elegir idioma", "选择语言", "भाषा चुनें", "Выберите язык", "اختر اللغة"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text(
                        language,
                        "Sistem dili desteklenmiyorsa uygulama otomatik olarak English kullanır.",
                        "If the system language is not supported, the app automatically uses English.",
                        "Si la langue du système n'est pas prise en charge, l'application utilise automatiquement English.",
                        "Si el idioma del sistema no es compatible, la app usa English automáticamente.",
                        "如果系统语言不受支持，应用会自动使用 English。",
                        "यदि सिस्टम भाषा समर्थित नहीं है, तो ऐप अपने-आप English उपयोग करता है।",
                        "Если язык системы не поддерживается, приложение автоматически использует English.",
                        "إذا لم تكن لغة النظام مدعومة، يستخدم التطبيق English تلقائياً."
                    ),
                    color = secondaryTextColor(),
                    lineHeight = 20.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageMode.entries.forEach { item ->
                    LanguageOptionRow(
                        language = language,
                        mode = item,
                        selected = item == languageMode,
                        onClick = { onLanguageChange(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    language: AppLanguage,
    mode: LanguageMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.13f else 0.08f)
    } else {
        rowSurfaceColor()
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.18f else 0.12f) else separatorColor()
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = languageModeFlag(mode),
                fontSize = 24.sp
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    languageModeTitle(mode, language),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    languageModeSubtitle(mode, language),
                    color = secondaryTextColor(),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(
                    Icons.Rounded.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TestAdAreaCard(language: AppLanguage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = canvasColor(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, separatorColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text(language, "Test reklam alanı", "Test ad area", "Zone pub test", "Área de anuncio de prueba", "测试广告区域", "टेस्ट विज्ञापन क्षेत्र", "Тестовая рекламная зона", "منطقة إعلان تجريبية"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text(language, "Bu alan reklam testleri için ayrıldı.", "This area is reserved for ad tests.", "Cette zone est réservée aux tests publicitaires.", "Esta área está reservada para pruebas de anuncios.", "该区域用于广告测试。", "यह क्षेत्र विज्ञापन परीक्षणों के लिए आरक्षित है।", "Эта область предназначена для тестов рекламы.", "هذه المنطقة مخصصة لاختبارات الإعلانات."),
                color = secondaryTextColor(),
                fontSize = 12.sp
            )
        }
    }
}

private fun languageModeFlag(mode: LanguageMode): String {
    return when (mode) {
        LanguageMode.SYSTEM -> "🌐"
        LanguageMode.TR -> languageFlag(AppLanguage.TR)
        LanguageMode.EN -> languageFlag(AppLanguage.EN)
        LanguageMode.FR -> languageFlag(AppLanguage.FR)
        LanguageMode.ES -> languageFlag(AppLanguage.ES)
        LanguageMode.ZH -> languageFlag(AppLanguage.ZH)
        LanguageMode.HI -> languageFlag(AppLanguage.HI)
        LanguageMode.RU -> languageFlag(AppLanguage.RU)
        LanguageMode.AR -> languageFlag(AppLanguage.AR)
    }
}

@Composable
private fun LanguageCodePill(code: String, selected: Boolean) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.14f else 0.09f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkScheme()) 0.07f else 0.045f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, separatorColor())
    ) {
        Text(
            code,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
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
                Text(text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки"))
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
                        title = text(language, "Tema", "Theme", "Thème", "Tema", "主题", "थीम", "Тема", "السمة")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip(text(language, "Canlı", "Fresh", "Vif", "Vivo", "鲜明", "ताज़ा", "Яркая"), appTheme == AppTheme.FRESH) { onThemeChange(AppTheme.FRESH) }
                            ThemeChip(text(language, "Okyanus", "Ocean", "Océan", "Océano", "海洋", "महासागर", "Океан"), appTheme == AppTheme.OCEAN) { onThemeChange(AppTheme.OCEAN) }
                            ThemeChip(text(language, "Günbatımı", "Sunset", "Coucher de soleil", "Atardecer", "日落", "सूर्यास्त", "Закат"), appTheme == AppTheme.SUNSET) { onThemeChange(AppTheme.SUNSET) }
                        }
                    }
                }
                item {
                    SettingsSection(
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                        title = text(language, "Dil", "Language", "Langue", "Idioma", "语言", "भाषा", "Язык", "اللغة")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip("${languageFlag(AppLanguage.TR)} Türkçe", language == AppLanguage.TR) { onLanguageChange(AppLanguage.TR) }
                                ThemeChip("${languageFlag(AppLanguage.EN)} English", language == AppLanguage.EN) { onLanguageChange(AppLanguage.EN) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip("${languageFlag(AppLanguage.FR)} Français", language == AppLanguage.FR) { onLanguageChange(AppLanguage.FR) }
                                ThemeChip("${languageFlag(AppLanguage.ES)} Español", language == AppLanguage.ES) { onLanguageChange(AppLanguage.ES) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip("${languageFlag(AppLanguage.ZH)} 中文", language == AppLanguage.ZH) { onLanguageChange(AppLanguage.ZH) }
                                ThemeChip("${languageFlag(AppLanguage.HI)} हिन्दी", language == AppLanguage.HI) { onLanguageChange(AppLanguage.HI) }
                                ThemeChip("${languageFlag(AppLanguage.RU)} Русский", language == AppLanguage.RU) { onLanguageChange(AppLanguage.RU) }
                                ThemeChip("${languageFlag(AppLanguage.AR)} العربية", language == AppLanguage.AR) { onLanguageChange(AppLanguage.AR) }
                            }
                        }
                    }
                }
                item {
                    SettingsSection(
                    icon = { Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = null) },
                        title = text(language, "Yardım", "Help")
                    ) {
                        Text(
                            text(
                                language,
                                "Uygulama seç, test gününü gir ve kullanım erişimi izniyle gün gün süreleri takip et. Kart ikonuna dokunarak ilgili uygulamayı açabilirsin.",
                                "Pick apps, enter the current test day, and track daily minutes with Usage Access. Tap an app icon to open that app.",
                                "Choisis des applications, saisis le jour de test actuel et suis les minutes quotidiennes avec l'accès à l'utilisation. Appuie sur l'icône d'une application pour l'ouvrir.",
                                "Elige apps, introduce el día actual de prueba y sigue los minutos diarios con Acceso de uso. Toca el icono de una app para abrirla.",
                                "选择应用，输入当前测试日，并通过使用情况访问跟踪每日分钟数。点按应用图标即可打开应用。",
                                "ऐप चुनें, वर्तमान टेस्ट दिन दर्ज करें, और Usage Access से रोज़ाना मिनट ट्रैक करें। ऐप खोलने के लिए उसके आइकन पर टैप करें।",
                                "Выберите приложения, укажите текущий день теста и отслеживайте ежедневные минуты через доступ к статистике использования. Коснитесь значка приложения, чтобы открыть его."
                            )
                        )
                    }
                }
                item {
                    SettingsAction(
                        icon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        title = text(language, "Destek e-postası", "Support email", "E-mail d'assistance", "Correo de soporte", "支持邮箱", "सहायता ईमेल", "Почта поддержки"),
                        subtitle = SUPPORT_MAIL,
                        onClick = onSendMail
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть"))
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
        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = secondaryTextColor())
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
    val listState = remember(sortMode, query) { LazyListState() }
    val filtered = remember(apps, trackedPackageNames, query, sortMode) {
        val q = query.trim().lowercase()
        val base = apps
            .filterNot { it.packageName in trackedPackageNames }
            .filter {
                q.isBlank() || it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        sortInstalledApps(base, sortMode)
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
                        text(language, "Uygulama seç", "Pick app", "Choisir une app", "Elegir app", "选择应用", "ऐप चुनें", "Выбрать приложение"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text(language, "${filtered.size} uygulama eklenebilir", "${filtered.size} apps available", ar = "${filtered.size} تطبيق متاح"),
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
                        text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть"),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(text(language, "Ara", "Search", "Rechercher", "Buscar", "搜索", "खोजें", "Поиск")) },
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
                SortChip(text(language, "Yeni", "Newest", "Plus récent", "Más reciente", "最新", "नवीनतम", "Новые"), sortMode == SortMode.NEWEST) { sortMode = SortMode.NEWEST }
                SortChip(text(language, "Eski", "Oldest", "Plus ancien", "Más antiguo", "最旧", "सबसे पुराना", "Старые"), sortMode == SortMode.OLDEST) { sortMode = SortMode.OLDEST }
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
                        Text(text(language, "Sonuç yok", "No results", "Aucun résultat", "Sin resultados", "无结果", "कोई परिणाम नहीं", "Нет результатов"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text(language, "Aramayı değiştir veya eklenmiş uygulamaları kontrol et.", "Change the search or check already added apps.", ar = "غيّر البحث أو تحقق من التطبيقات المضافة سابقاً."),
                            color = secondaryTextColor()
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.heightIn(max = 560.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filtered, key = { "${sortMode.name}:${it.packageName}" }) { app ->
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
                                        publisher?.let { text(language, "Yayıncı: $it", "Publisher: $it", ar = "الناشر: $it") }
                                            ?: text(language, "Seçmek için dokun", "Tap to add", ar = "اضغط للإضافة"),
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
    val listState = remember(sortMode, query) { LazyListState() }
    val filtered = remember(apps, query, sortMode) {
        val q = query.trim().lowercase()
        val base = apps.filter {
            q.isBlank() || it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
        sortInstalledApps(base, sortMode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text(language, "Uygulama seç", "Pick app", "Choisir une app", "Elegir app", "选择应用", "ऐप चुनें", "Выбрать приложение")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(text(language, "Ara", "Search", "Rechercher", "Buscar", "搜索", "खोजें", "Поиск")) },
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
                    state = listState,
                    modifier = Modifier.heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(filtered, key = { "${sortMode.name}:${it.packageName}" }) { app ->
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
        confirmButton = { TextButton(onClick = onDismiss) { Text(text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть")) } }
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
    item: TrackedApp,
    dateFormat: DateDisplayFormat,
    rangeMode: UsageRangeMode,
    usageMinutesByStart: Map<Long, Long>
): List<UsageDay> {
    val currentDay = SeriesCalculator.currentDay(item)
    val lastVisibleDay = currentDay
    val firstVisibleDay = when (rangeMode) {
        UsageRangeMode.FULL -> 1
        UsageRangeMode.WINDOWED -> if (lastVisibleDay > 14) lastVisibleDay - 13 else 1
    }
    val todayStart = SeriesCalculator.dayStartMillis(0)
    return (firstVisibleDay..lastVisibleDay).map { testDay ->
        val dayStart = SeriesCalculator.dayStartMillisForTestDay(item, testDay)
        val isFuture = dayStart > todayStart
        UsageDay(
            index = testDay,
            label = formatUsageDate(dayStart, dateFormat),
            minutes = if (!isFuture) {
                usageMinutesByStart[dayStart] ?: 0L
            } else {
                0L
            },
            isToday = dayStart == todayStart,
            isFuture = isFuture
        )
    }
}

private fun totalUsageMinutesForSeries(
    item: TrackedApp,
    usageAccess: Boolean,
    usageMinutesByStart: Map<Long, Long>
): Long {
    if (!usageAccess) return 0L
    val currentDay = SeriesCalculator.currentDay(item)
    val todayStart = SeriesCalculator.dayStartMillis(0)
    return (1..currentDay).sumOf { testDay ->
        val dayStart = SeriesCalculator.dayStartMillisForTestDay(item, testDay)
        if (dayStart > todayStart) {
            0L
        } else {
            usageMinutesByStart[dayStart] ?: 0L
        }
    }
}

private fun usageSummaryCacheKey(
    item: TrackedApp,
    dateFormat: DateDisplayFormat,
    rangeMode: UsageRangeMode
): String {
    return listOf(
        item.packageName,
        item.createdAtMillis,
        item.startDayIndex,
        item.completedAtMillis ?: 0L,
        item.isArchived,
        dateFormat.name,
        rangeMode.name
    ).joinToString("|")
}

@Composable
private fun rememberUsageSummaryAsync(
    context: android.content.Context,
    item: TrackedApp,
    usageAccess: Boolean,
    refreshTick: Int,
    dateFormat: DateDisplayFormat,
    rangeMode: UsageRangeMode,
    initialSummary: UsageSummary?,
    onSummaryReady: (UsageSummary) -> Unit
): UsageSummary {
    val itemKey = remember(item) {
        listOf(
            item.packageName,
            item.createdAtMillis,
            item.startDayIndex,
            item.completedAtMillis,
            item.isArchived
        )
    }
    val state by produceState(
        initialValue = initialSummary ?: UsageSummary(
            days = testUsageDays(item, dateFormat, rangeMode, emptyMap()),
            totalMinutes = 0L,
            hasPartialDailyHistory = false,
            isLoading = true
        ),
        usageAccess,
        refreshTick,
        dateFormat,
        rangeMode,
        itemKey
    ) {
        val computed = withContext(Dispatchers.Default) {
            val currentDay = SeriesCalculator.currentDay(item)
            val seriesStart = SeriesCalculator.dayStartMillisForTestDay(item, 1)
            val usageMinutesByStart = if (usageAccess && currentDay > 0) {
                UsageReader.mergedUsageMinutesByDayMap(
                    context = context,
                    packageName = item.packageName,
                    startMillis = seriesStart,
                    endMillis = System.currentTimeMillis()
                )
            } else {
                emptyMap()
            }
            val eventMinutesByStart = if (usageAccess && currentDay > 0) {
                UsageReader.usageMinutesByDayMapFromEvents(
                    context = context,
                    packageName = item.packageName,
                    startMillis = seriesStart,
                    endMillis = System.currentTimeMillis()
                )
            } else {
                emptyMap()
            }
            val dailyTotal = totalUsageMinutesForSeries(item, usageAccess, usageMinutesByStart)
            val eventTotal = totalUsageMinutesForSeries(item, usageAccess, eventMinutesByStart)
            val bestTotal = if (usageAccess && currentDay > 0) {
                UsageReader.bestEffortTotalUsageMinutes(
                    context = context,
                    packageName = item.packageName,
                    seriesStartMillis = seriesStart,
                    dailyTotalMinutes = dailyTotal,
                    eventTotalMinutes = eventTotal
                )
            } else {
                0L
            }
            val days = testUsageDays(item, dateFormat, rangeMode, usageMinutesByStart)
            UsageSummary(
                days = days,
                totalMinutes = bestTotal,
                hasPartialDailyHistory = bestTotal > dailyTotal,
                isLoading = false
            )
        }
        value = computed
        onSummaryReady(computed)
    }
    return state
}

private fun formatUsageDate(millis: Long, dateFormat: DateDisplayFormat): String {
    val pattern = when (dateFormat) {
        DateDisplayFormat.MONTH_DAY -> "MM.dd"
        DateDisplayFormat.DAY_MONTH -> "dd.MM"
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}

private fun dateFormatChipLabel(language: AppLanguage, format: DateDisplayFormat): String {
    return when (format) {
        DateDisplayFormat.MONTH_DAY -> text(language, "Ay.Gün", "Month.Day", "Mois.Jour", "Mes.Día", "月.日", "माह.दिन", "Мес.День", "شهر.يوم")
        DateDisplayFormat.DAY_MONTH -> text(language, "Gün.Ay", "Day.Month", "Jour.Mois", "Día.Mes", "日.月", "दिन.माह", "День.Мес", "يوم.شهر")
    }
}

private fun minuteLabel(language: AppLanguage): String {
    return text(language, "dk", "min", "min", "min", "分钟", "मिनट", "мин", "دقيقة")
}












