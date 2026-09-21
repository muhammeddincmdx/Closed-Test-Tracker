package com.mdstudio.closedtesttracker

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
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
import android.util.Log
import android.view.View
import android.widget.Toast
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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mdstudio.closedtesttracker.data.AppDatabase
import com.mdstudio.closedtesttracker.data.TrackedApp
import java.io.File
import java.io.FileOutputStream
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Public UI model enums (SortMode, AppLanguage, AppTheme, AppThemeMode,
// AppScreen, HomeFilter) and the InstalledApp data class live in AppModels.kt.
private enum class LanguageMode { SYSTEM, TR, EN, FR, ES, ZH, HI, RU, AR, DE, JA, PT, ID }
private enum class DateDisplayFormat { MONTH_DAY, DAY_MONTH }
private enum class OverviewFilter { ALL, ACTIVE, COMPLETED, ARCHIVED }
private enum class OverviewLayout { LIST, GRID }
private enum class DetailViewMode { GRAPH, TEXT }
private enum class ConfirmAction { FINISH, REACTIVATE, ARCHIVE, RESTORE }

private const val PREFS_NAME = "tester_settings"
private const val KEY_LANGUAGE = "language"
private const val KEY_THEME = "theme"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_BACKGROUND_STYLE = "background_style"
private const val KEY_GRADIENT_START = "gradient_start_color"
private const val KEY_GRADIENT_END = "gradient_end_color"
private const val KEY_GRADIENT_TYPE = "gradient_type"
private const val KEY_DATE_FORMAT = "date_format"
private const val KEY_AUTO_TOUR = "auto_tour"
private const val KEY_REMINDER_HOUR = "reminder_hour"
private const val KEY_REMINDER_MINUTE = "reminder_minute"
private const val KEY_PLAY_PUBLISHER_PREFIX = "play_publisher_"
private const val KEY_LAST_UPDATE_NOTIFICATION_CODE = "last_update_notification_code"
private const val KEY_LAST_SCREEN = "last_screen"
private const val KEY_LAST_SELECTED_PACKAGE = "last_selected_package"
private const val KEY_LAST_SCREEN_SAVED_AT = "last_screen_saved_at"
private const val KEY_HOME_LIST_INDEX = "home_list_index"
private const val KEY_HOME_LIST_OFFSET = "home_list_offset"
private const val KEY_USAGE_TODAY_CACHE_PREFIX = "usage_today_cache_"
private const val KEY_USAGE_TOTAL_CACHE_PREFIX = "usage_total_cache_"
private const val KEY_LAST_SEEN_VERSION_NAME = "last_seen_version_name"
private const val KEY_DEBUG_MODE_ENABLED = "debug_mode_enabled"
private const val LAST_SCREEN_RESTORE_TIMEOUT_MILLIS = 10L * 60L * 1000L
private const val USAGE_REFRESH_MIN_INTERVAL_MILLIS = 60L * 1000L
private const val USAGE_IDLE_REFRESH_INTERVAL_MILLIS = 2L * 60L * 1000L
private const val KEY_LIVE_USAGE_OVERLAY = "live_usage_overlay"
private const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
// TEMPORARY PRO UNLOCK: unlocks all Pro/premium features so they are visible
// without a purchase. Set back to false to restore normal Pro gating.
private const val TEMP_PRO_UNLOCK = false
private const val BANNER_PROD_AD_UNIT_ID = "ca-app-pub-5011839648327891/4443133880"
private const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private const val REWARDED_PROD_AD_UNIT_ID = "ca-app-pub-5011839648327891/1816970548"
private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
private const val KEY_PRO_OFFER_SHOWN = "pro_offer_shown"
private const val SUPPORT_MAIL = "mdstudiohelp@gmail.com"
private const val DONATION_URL = "https://www.buymeacoffee.com/mdx0"
private const val POLICY_URL = "https://sites.google.com/view/infomdstudio/closed-test-tracker?authuser=0"
private const val MD_STUDIO_URL = "https://sites.google.com/view/infomdstudio"

private fun readUsageCache(
    prefs: android.content.SharedPreferences,
    packageNames: Collection<String>,
    prefix: String
): Map<String, Long> {
    return packageNames.mapNotNull { packageName ->
        val key = "$prefix$packageName"
        if (prefs.contains(key)) packageName to prefs.getLong(key, 0L) else null
    }.toMap()
}

private fun writeUsageCache(
    prefs: android.content.SharedPreferences,
    values: Map<String, Long>,
    prefix: String
) {
    val editor = prefs.edit()
    values.forEach { (packageName, minutes) ->
        editor.putLong("$prefix$packageName", minutes)
    }
    editor.apply()
}

private fun appColors(theme: AppTheme, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = Color(0xFF9EDDE7),
        secondary = Color(0xFFE2C89A),
        tertiary = Color(0xFFB5D1C5),
        background = Color(0xFF090B0D),
        surface = Color(0xFF171B1E),
        surfaceVariant = Color(0xFF263036),
        onPrimary = Color(0xFF06292F),
        onSecondary = Color(0xFF2B1B08),
        onTertiary = Color(0xFF0E2A21),
        onBackground = Color(0xFFF6F4EE),
        onSurface = Color(0xFFF6F4EE),
        onSurfaceVariant = Color(0xFFC8D0CD),
        outline = Color(0xFF425057)
    )
} else {
    lightColorScheme(
        primary = Color(0xFF0F5966),
        secondary = Color(0xFF956A24),
        tertiary = Color(0xFF3F7568),
        background = Color(0xFFF7F3EA),
        surface = Color(0xFFFFFCF5),
        surfaceVariant = Color(0xFFE8EEE9),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF182023),
        onSurface = Color(0xFF182023),
        onSurfaceVariant = Color(0xFF5D6868),
        outline = Color(0xFFD0D8D1)
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
            Color(0xFFFFFCF4),
            Color(0xFFF4F0E7),
            Color(0xFFEAF0EB)
        )
    )
}

private fun gradientBrush(colors: List<Color>, type: GradientType): Brush = when (type) {
    GradientType.VERTICAL -> Brush.verticalGradient(colors)
    GradientType.HORIZONTAL -> Brush.horizontalGradient(colors)
    GradientType.DIAGONAL -> Brush.linearGradient(colors)
    GradientType.RADIAL -> Brush.radialGradient(colors)
}

@Composable
private fun FluidBackdrop(
    modifier: Modifier = Modifier,
    appTheme: AppTheme,
    darkTheme: Boolean,
    appBackground: AppBackground = AppBackground.SIMPLE,
    customGradient: CustomGradient = CustomGradient(),
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
    val baseBrush = when (appBackground) {
        AppBackground.SIMPLE -> backgroundBrush(appTheme, darkTheme)
        AppBackground.RICH -> gradientBrush(
            if (darkTheme) {
                listOf(Color(0xFF070A0B), Color(0xFF17292E), Color(0xFF2F271B), Color(0xFF0B1113))
            } else {
                listOf(Color(0xFFFFFBF1), Color(0xFFEAF4EF), Color(0xFFF3E3BD), Color(0xFFFFFCF8))
            },
            GradientType.DIAGONAL
        )
        AppBackground.CUSTOM -> gradientBrush(
            listOf(Color(customGradient.startColor), Color(customGradient.endColor)),
            customGradient.type
        )
    }
    val showFluidBlobs = appBackground == AppBackground.SIMPLE
    val showPremiumBlobs = appBackground == AppBackground.RICH

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBrush)
    ) {
        if (showFluidBlobs) {
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
        }
        if (showPremiumBlobs) {
            FluidBlob(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationX = -driftA * 0.65f
                        translationY = driftB * 0.50f
                        scaleX = 0.92f + (pulse - 0.76f) * 0.60f
                        scaleY = 0.92f + (pulse - 0.76f) * 0.60f
                    },
                color = if (darkTheme) Color(0xFF9EDDE7) else Color(0xFF89C7B6),
                size = 360
            )
            FluidBlob(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        translationX = driftB * 0.70f
                        translationY = -driftA * 0.55f
                        scaleX = 1.02f + (pulse - 0.76f) * 0.50f
                        scaleY = 1.02f + (pulse - 0.76f) * 0.50f
                    },
                color = if (darkTheme) Color(0xFFE2C89A) else Color(0xFFE5B767),
                size = 390
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (darkTheme) 0.03f else 0.20f),
                                Color.Transparent,
                                Color.Black.copy(alpha = if (darkTheme) 0.22f else 0.00f)
                            )
                        )
                    )
            )
        }
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
        dark && strong -> 0.94f
        dark -> 0.86f
        strong -> 0.95f
        else -> 0.90f
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
            MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.96f else 0.94f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dark) 0.56f else 0.42f),
            MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.88f else 0.86f)
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
        if (isDarkScheme()) Color.White.copy(alpha = 0.14f) else Color(0xFF6E7B72).copy(alpha = 0.16f)
    )
}

@Composable
private fun canvasColor(): Color {
    return if (isDarkScheme()) {
        Color(0xFF121719).copy(alpha = 0.96f)
    } else {
        Color(0xFFF8F3EA).copy(alpha = 0.92f)
    }
}

@Composable
private fun rowSurfaceColor(): Color {
    return if (isDarkScheme()) {
        Color(0xFF223039).copy(alpha = 0.76f)
    } else {
        Color(0xFFFFFCF5).copy(alpha = 0.92f)
    }
}

@Composable
private fun separatorColor(): Color {
    return if (isDarkScheme()) {
        Color(0xFFBFE4E7).copy(alpha = 0.12f)
    } else {
        Color(0xFF0F5966).copy(alpha = 0.10f)
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

// InstalledApp data class lives in AppModels.kt

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
    ar: String = en,
    de: String = en,
    ja: String = en,
    pt: String = en,
    id: String = en
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
        AppLanguage.DE -> de
        AppLanguage.JA -> ja
        AppLanguage.PT -> pt
        AppLanguage.ID -> id
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
            AppLanguage.DE -> "Herausgeber: $name"
            AppLanguage.JA -> "公開元: $name"
            AppLanguage.PT -> "Editor: $name"
            AppLanguage.ID -> "Penerbit: $name"
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
            AppLanguage.DE -> "Installierte Version: $value"
            AppLanguage.JA -> "インストール済みバージョン: $value"
            AppLanguage.PT -> "Versão instalada: $value"
            AppLanguage.ID -> "Versi terpasang: $value"
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
            AppLanguage.DE -> "Version $value"
            AppLanguage.JA -> "バージョン $value"
            AppLanguage.PT -> "Versão $value"
            AppLanguage.ID -> "Versi $value"
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
            AppLanguage.DE -> "Tag $value"
            AppLanguage.JA -> "$value 日目"
            AppLanguage.PT -> "Dia $value"
            AppLanguage.ID -> "Hari $value"
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
            AppLanguage.DE -> "$value Apps verfügbar"
            AppLanguage.JA -> "$value 個のアプリが利用可能"
            AppLanguage.PT -> "$value apps disponíveis"
            AppLanguage.ID -> "$value aplikasi tersedia"
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
            AppLanguage.DE -> "$value Apps"
            AppLanguage.JA -> "$value 個のアプリ"
            AppLanguage.PT -> "$value apps"
            AppLanguage.ID -> "$value aplikasi"
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
            AppLanguage.DE -> "Die tägliche Serienprüfung ist gegen $time geplant."
            AppLanguage.JA -> "毎日の継続確認は $time 頃に予定されています。"
            AppLanguage.PT -> "A verificação diária está agendada para cerca de $time."
            AppLanguage.ID -> "Pemeriksaan rangkaian harian dijadwalkan sekitar $time."
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
            AppLanguage.DE -> "Zusammenfassung Tag 1-$value"
            AppLanguage.JA -> "1〜$value 日目の概要"
            AppLanguage.PT -> "Resumo dos dias 1-$value"
            AppLanguage.ID -> "Ringkasan hari 1-$value"
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
            AppLanguage.DE -> "Zusammenfassung Tag $first-$last"
            AppLanguage.JA -> "$first〜$last 日目の概要"
            AppLanguage.PT -> "Resumo dos dias $first-$last"
            AppLanguage.ID -> "Ringkasan hari $first-$last"
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
            AppLanguage.DE -> "$name wird als abgeschlossen markiert. Du kannst die App später reaktivieren."
            AppLanguage.JA -> "$name を完了として記録します。後で再開できます。"
            AppLanguage.PT -> "$name será marcado como concluído. Você poderá reativá-lo depois."
            AppLanguage.ID -> "$name akan ditandai selesai. Anda dapat mengaktifkannya lagi nanti."
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
            AppLanguage.DE -> "$name kehrt zur aktiven Liste zurück und die Serie wird fortgesetzt."
            AppLanguage.JA -> "$name は有効な一覧に戻り、継続日数の計測が続きます。"
            AppLanguage.PT -> "$name voltará à lista ativa e a sequência continuará."
            AppLanguage.ID -> "$name akan kembali ke daftar aktif dan rangkaian akan berlanjut."
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
            AppLanguage.DE -> "$name wird archiviert. Du kannst die App später wiederherstellen."
            AppLanguage.JA -> "$name をアーカイブします。後で復元できます。"
            AppLanguage.PT -> "$name será movido para o arquivo. Você poderá restaurá-lo depois."
            AppLanguage.ID -> "$name akan dipindahkan ke arsip. Anda dapat memulihkannya nanti."
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
            AppLanguage.DE -> "$name wird aus dem Archiv wiederhergestellt und erneut in der Liste angezeigt."
            AppLanguage.JA -> "$name をアーカイブから復元し、一覧に再表示します。"
            AppLanguage.PT -> "$name será restaurado do arquivo e voltará à lista."
            AppLanguage.ID -> "$name akan dipulihkan dari arsip dan tampil lagi di daftar."
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
            AppLanguage.DE -> "$name aus der Liste entfernen? Der Android-Nutzungsverlauf bleibt erhalten; nur der Tracking-Eintrag wird gelöscht."
            AppLanguage.JA -> "$name を一覧から削除しますか？Android の利用履歴は残り、追跡データのみ削除されます。"
            AppLanguage.PT -> "Remover $name da lista? O histórico de uso do Android será mantido; apenas o registro será excluído."
            AppLanguage.ID -> "Hapus $name dari daftar? Riwayat penggunaan Android tetap ada; hanya catatan pelacakan yang dihapus."
            else -> en
        }
    }
    fun auto(language: AppLanguage, value: String): String {
        val de = mapOf(
            "Loading" to "Wird geladen",
            "Language" to "Sprache",
            "System default" to "Systemstandard",
            "Settings" to "Einstellungen",
            "Back" to "Zurück",
            "Add app" to "App hinzufügen",
            "Summaries" to "Zusammenfassungen",
            "Tracking summary" to "Tracking-Übersicht",
            "App summaries" to "App-Übersichten",
            "Missing today" to "Heute fehlend",
            "Used" to "Genutzt",
            "Missing" to "Fehlend",
            "Done" to "Fertig",
            "Today" to "Heute",
            "Streak" to "Serie",
            "Active" to "Aktiv",
            "Archive" to "Archiv",
            "All" to "Alle",
            "Open" to "Öffnen",
            "No apps yet" to "Noch keine Apps",
            "Choose the apps to track first." to "Wähle zuerst die Apps aus, die du verfolgen möchtest.",
            "Pick app" to "App auswählen",
            "This filter is empty" to "Dieser Filter ist leer",
            "Apps" to "Apps",
            "Loading usage" to "Nutzung wird geladen",
            "Usage permission required" to "Nutzungszugriff erforderlich",
            "Publisher loading" to "Publisher wird geladen",
            "Day" to "Tag",
            "Total" to "Gesamt",
            "Open app" to "App öffnen",
            "Manage" to "Verwalten",
            "Archived" to "Archiviert",
            "Live" to "Aktiv",
            "Set day" to "Tag festlegen",
            "Finish" to "Beenden",
            "Reactivate" to "Reaktivieren",
            "Restore" to "Wiederherstellen",
            "Delete" to "Löschen",
            "Graph" to "Grafik",
            "Text" to "Text",
            "Cancel" to "Abbrechen",
            "Save" to "Speichern",
            "Date format" to "Datumsformat",
            "Reminder time" to "Erinnerungszeit",
            "Close" to "Schließen",
            "Search" to "Suchen",
            "Newest" to "Neueste",
            "Oldest" to "Älteste",
            "No results" to "Keine Ergebnisse",
            "Minutes" to "Minuten",
            "min" to "Min.",
            "Detail" to "Details",
            "Finish test?" to "Test beenden?",
            "Reactivate?" to "Reaktivieren?",
            "Archive?" to "Archivieren?",
            "Restore?" to "Wiederherstellen?",
            "Delete app" to "App löschen",
            "New update available" to "Neues Update verfügbar",
            "App updates" to "App-Updates",
            "Update" to "Aktualisieren",
            "Later" to "Später",
            "Test day" to "Testtag",
            "Selected day" to "Ausgewählter Tag",
            "Version" to "Version",
            "Open in Google Play" to "In Google Play öffnen",
            "Appearance" to "Darstellung",
            "System" to "System",
            "Light" to "Hell",
            "Dark" to "Dunkel",
            "Notification permission" to "Benachrichtigungsberechtigung",
            "Help" to "Hilfe",
            "App info" to "App-Info",
            "Purpose" to "Zweck",
            "How to use" to "Verwendung",
            "Note" to "Hinweis",
            "Support email" to "Support-E-Mail",
            "Full series" to "Gesamte Serie",
            "Pending" to "Ausstehend",
            "Choose" to "Auswählen",
            "Choose language" to "Sprache auswählen",
            "Theme" to "Design",
            "Fresh" to "Frisch",
            "Ocean" to "Ozean",
            "Sunset" to "Sonnenuntergang",
            "Name" to "Name",
            "New" to "Neu",
            "Old" to "Alt"
        )
        val ja = mapOf(
            "Loading" to "読み込み中",
            "Language" to "言語",
            "System default" to "システム既定",
            "Settings" to "設定",
            "Back" to "戻る",
            "Add app" to "アプリを追加",
            "Summaries" to "概要",
            "Tracking summary" to "追跡概要",
            "App summaries" to "アプリ概要",
            "Missing today" to "今日未使用",
            "Used" to "使用済み",
            "Missing" to "未使用",
            "Done" to "完了",
            "Today" to "今日",
            "Streak" to "連続",
            "Active" to "有効",
            "Archive" to "アーカイブ",
            "All" to "すべて",
            "Open" to "開く",
            "No apps yet" to "アプリはまだありません",
            "Choose the apps to track first." to "まず追跡するアプリを選択してください。",
            "Pick app" to "アプリを選択",
            "This filter is empty" to "このフィルターは空です",
            "Apps" to "アプリ",
            "Loading usage" to "使用状況を読み込み中",
            "Usage permission required" to "使用状況アクセスが必要です",
            "Publisher loading" to "公開元を読み込み中",
            "Day" to "日",
            "Total" to "合計",
            "Open app" to "アプリを開く",
            "Manage" to "管理",
            "Archived" to "アーカイブ済み",
            "Live" to "進行中",
            "Set day" to "日数を設定",
            "Finish" to "終了",
            "Reactivate" to "再開",
            "Restore" to "復元",
            "Delete" to "削除",
            "Graph" to "グラフ",
            "Text" to "テキスト",
            "Cancel" to "キャンセル",
            "Save" to "保存",
            "Date format" to "日付形式",
            "Reminder time" to "リマインダー時刻",
            "Close" to "閉じる",
            "Search" to "検索",
            "Newest" to "新しい順",
            "Oldest" to "古い順",
            "No results" to "結果なし",
            "Minutes" to "分",
            "min" to "分",
            "Detail" to "詳細",
            "Finish test?" to "テストを終了しますか？",
            "Reactivate?" to "再開しますか？",
            "Archive?" to "アーカイブしますか？",
            "Restore?" to "復元しますか？",
            "Delete app" to "アプリを削除",
            "New update available" to "新しい更新があります",
            "App updates" to "アプリ更新",
            "Update" to "更新",
            "Later" to "後で",
            "Test day" to "テスト日",
            "Selected day" to "選択した日",
            "Version" to "バージョン",
            "Open in Google Play" to "Google Playで開く",
            "Appearance" to "外観",
            "System" to "システム",
            "Light" to "ライト",
            "Dark" to "ダーク",
            "Notification permission" to "通知権限",
            "Help" to "ヘルプ",
            "App info" to "アプリ情報",
            "Purpose" to "目的",
            "How to use" to "使い方",
            "Note" to "注記",
            "Support email" to "サポートメール",
            "Full series" to "全期間",
            "Pending" to "保留中",
            "Choose" to "選択",
            "Choose language" to "言語を選択",
            "Theme" to "テーマ",
            "Fresh" to "フレッシュ",
            "Ocean" to "オーシャン",
            "Sunset" to "サンセット",
            "Name" to "名前",
            "New" to "新規",
            "Old" to "古い"
        )
        val pt = mapOf(
            "Loading" to "Carregando",
            "Language" to "Idioma",
            "System default" to "Padrão do sistema",
            "Settings" to "Configurações",
            "Back" to "Voltar",
            "Add app" to "Adicionar app",
            "Summaries" to "Resumos",
            "Tracking summary" to "Resumo de acompanhamento",
            "App summaries" to "Resumos dos apps",
            "Missing today" to "Faltando hoje",
            "Used" to "Usados",
            "Missing" to "Faltando",
            "Done" to "Concluído",
            "Today" to "Hoje",
            "Streak" to "Sequência",
            "Active" to "Ativo",
            "Archive" to "Arquivo",
            "All" to "Todos",
            "Open" to "Abrir",
            "No apps yet" to "Ainda sem apps",
            "Choose the apps to track first." to "Escolha primeiro os apps para acompanhar.",
            "Pick app" to "Escolher app",
            "This filter is empty" to "Este filtro está vazio",
            "Apps" to "Apps",
            "Loading usage" to "Carregando uso",
            "Usage permission required" to "Permissão de uso necessária",
            "Publisher loading" to "Carregando publicador",
            "Day" to "Dia",
            "Total" to "Total",
            "Open app" to "Abrir app",
            "Manage" to "Gerenciar",
            "Archived" to "Arquivado",
            "Live" to "Ativo",
            "Set day" to "Definir dia",
            "Finish" to "Finalizar",
            "Reactivate" to "Reativar",
            "Restore" to "Restaurar",
            "Delete" to "Excluir",
            "Graph" to "Gráfico",
            "Text" to "Texto",
            "Cancel" to "Cancelar",
            "Save" to "Salvar",
            "Date format" to "Formato de data",
            "Reminder time" to "Hora do lembrete",
            "Close" to "Fechar",
            "Search" to "Pesquisar",
            "Newest" to "Mais recentes",
            "Oldest" to "Mais antigos",
            "No results" to "Sem resultados",
            "Minutes" to "Minutos",
            "min" to "min",
            "Detail" to "Detalhe",
            "Finish test?" to "Finalizar teste?",
            "Reactivate?" to "Reativar?",
            "Archive?" to "Arquivar?",
            "Restore?" to "Restaurar?",
            "Delete app" to "Excluir app",
            "New update available" to "Nova atualização disponível",
            "App updates" to "Atualizações",
            "Update" to "Atualizar",
            "Later" to "Mais tarde",
            "Test day" to "Dia de teste",
            "Selected day" to "Dia selecionado",
            "Version" to "Versão",
            "Open in Google Play" to "Abrir no Google Play",
            "Appearance" to "Aparência",
            "System" to "Sistema",
            "Light" to "Claro",
            "Dark" to "Escuro",
            "Notification permission" to "Permissão de notificação",
            "Help" to "Ajuda",
            "App info" to "Informações do app",
            "Purpose" to "Objetivo",
            "How to use" to "Como usar",
            "Note" to "Nota",
            "Support email" to "E-mail de suporte",
            "Full series" to "Série completa",
            "Pending" to "Pendente",
            "Choose" to "Escolher",
            "Choose language" to "Escolher idioma",
            "Theme" to "Tema",
            "Fresh" to "Novo",
            "Ocean" to "Oceano",
            "Sunset" to "Pôr do sol",
            "Name" to "Nome",
            "New" to "Novo",
            "Old" to "Antigo"
        )
        val id = mapOf(
            "Loading" to "Memuat",
            "Language" to "Bahasa",
            "System default" to "Default sistem",
            "Settings" to "Pengaturan",
            "Back" to "Kembali",
            "Add app" to "Tambah aplikasi",
            "Summaries" to "Ringkasan",
            "Tracking summary" to "Ringkasan pelacakan",
            "App summaries" to "Ringkasan aplikasi",
            "Missing today" to "Belum dibuka hari ini",
            "Used" to "Dipakai",
            "Missing" to "Belum",
            "Done" to "Selesai",
            "Today" to "Hari ini",
            "Streak" to "Rangkaian",
            "Active" to "Aktif",
            "Archive" to "Arsip",
            "All" to "Semua",
            "Open" to "Buka",
            "No apps yet" to "Belum ada aplikasi",
            "Choose the apps to track first." to "Pilih aplikasi yang ingin dilacak terlebih dahulu.",
            "Pick app" to "Pilih aplikasi",
            "This filter is empty" to "Filter ini kosong",
            "Apps" to "Aplikasi",
            "Loading usage" to "Memuat penggunaan",
            "Usage permission required" to "Izin penggunaan diperlukan",
            "Publisher loading" to "Memuat penerbit",
            "Day" to "Hari",
            "Total" to "Total",
            "Open app" to "Buka aplikasi",
            "Manage" to "Kelola",
            "Archived" to "Diarsipkan",
            "Live" to "Aktif",
            "Set day" to "Atur hari",
            "Finish" to "Selesai",
            "Reactivate" to "Aktifkan lagi",
            "Restore" to "Pulihkan",
            "Delete" to "Hapus",
            "Graph" to "Grafik",
            "Text" to "Teks",
            "Cancel" to "Batal",
            "Save" to "Simpan",
            "Date format" to "Format tanggal",
            "Reminder time" to "Waktu pengingat",
            "Close" to "Tutup",
            "Search" to "Cari",
            "Newest" to "Terbaru",
            "Oldest" to "Terlama",
            "No results" to "Tidak ada hasil",
            "Minutes" to "Menit",
            "min" to "mnt",
            "Detail" to "Detail",
            "Finish test?" to "Selesaikan tes?",
            "Reactivate?" to "Aktifkan lagi?",
            "Archive?" to "Arsipkan?",
            "Restore?" to "Pulihkan?",
            "Delete app" to "Hapus aplikasi",
            "New update available" to "Pembaruan tersedia",
            "App updates" to "Pembaruan aplikasi",
            "Update" to "Perbarui",
            "Later" to "Nanti",
            "Test day" to "Hari tes",
            "Selected day" to "Hari dipilih",
            "Version" to "Versi",
            "Open in Google Play" to "Buka di Google Play",
            "Appearance" to "Tampilan",
            "System" to "Sistem",
            "Light" to "Terang",
            "Dark" to "Gelap",
            "Notification permission" to "Izin notifikasi",
            "Help" to "Bantuan",
            "App info" to "Info aplikasi",
            "Purpose" to "Tujuan",
            "How to use" to "Cara pakai",
            "Note" to "Catatan",
            "Support email" to "Email dukungan",
            "Full series" to "Seri lengkap",
            "Pending" to "Tertunda",
            "Choose" to "Pilih",
            "Choose language" to "Pilih bahasa",
            "Theme" to "Tema",
            "Fresh" to "Segar",
            "Ocean" to "Laut",
            "Sunset" to "Senja",
            "Name" to "Nama",
            "New" to "Baru",
            "Old" to "Lama"
        )
        return when (language) {
            AppLanguage.DE -> de[value]
            AppLanguage.JA -> ja[value]
            AppLanguage.PT -> pt[value]
            AppLanguage.ID -> id[value]
            else -> null
        } ?: value
    }

    fun all(
        fr: String,
        es: String,
        zh: String,
        hi: String,
        ru: String,
        ar: String,
        de: String = auto(AppLanguage.DE, en),
        ja: String = auto(AppLanguage.JA, en),
        pt: String = auto(AppLanguage.PT, en),
        id: String = auto(AppLanguage.ID, en)
    ) = mapOf(
        AppLanguage.FR to fr,
        AppLanguage.ES to es,
        AppLanguage.ZH to zh,
        AppLanguage.HI to hi,
        AppLanguage.RU to ru,
        AppLanguage.AR to ar,
        AppLanguage.DE to de,
        AppLanguage.JA to ja,
        AppLanguage.PT to pt,
        AppLanguage.ID to id
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
    return translatedCopy(language, en) ?: common?.get(language) ?: auto(language, en)
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
        AppLanguage.DE -> "Deutsch"
        AppLanguage.JA -> "日本語"
        AppLanguage.PT -> "Português"
        AppLanguage.ID -> "Indonesia"
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
        AppLanguage.DE -> "\uD83C\uDDE9\uD83C\uDDEA"
        AppLanguage.JA -> "\uD83C\uDDEF\uD83C\uDDF5"
        AppLanguage.PT -> "\uD83C\uDDF5\uD83C\uDDF9"
        AppLanguage.ID -> "\uD83C\uDDEE\uD83C\uDDE9"
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
        "de" -> AppLanguage.DE
        "ja" -> AppLanguage.JA
        "pt" -> AppLanguage.PT
        "in", "id" -> AppLanguage.ID
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
        LanguageMode.DE -> languageDisplay(AppLanguage.DE)
        LanguageMode.JA -> languageDisplay(AppLanguage.JA)
        LanguageMode.PT -> languageDisplay(AppLanguage.PT)
        LanguageMode.ID -> languageDisplay(AppLanguage.ID)
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
        LanguageMode.DE -> "DE"
        LanguageMode.JA -> "JA"
        LanguageMode.PT -> "PT"
        LanguageMode.ID -> "ID"
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
        LanguageMode.DE -> "Deutsch"
        LanguageMode.JA -> "日本語"
        LanguageMode.PT -> "Português"
        LanguageMode.ID -> "Indonesia"
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
        LanguageMode.DE -> text(uiLanguage, "Uygulama arayüzü Deutsch olur.", "The app interface switches to German.", de = "Die App-Oberfläche wechselt zu Deutsch.")
        LanguageMode.JA -> text(uiLanguage, "Uygulama arayüzü 日本語 olur.", "The app interface switches to Japanese.", ja = "アプリの表示言語が日本語になります。")
        LanguageMode.PT -> text(uiLanguage, "Uygulama arayüzü Português olur.", "The app interface switches to Portuguese.", pt = "A interface do app muda para português.")
        LanguageMode.ID -> text(uiLanguage, "Uygulama arayüzü Indonesia olur.", "The app interface switches to Indonesian.", id = "Antarmuka aplikasi beralih ke bahasa Indonesia.")
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
        LanguageMode.DE -> AppLanguage.DE
        LanguageMode.JA -> AppLanguage.JA
        LanguageMode.PT -> AppLanguage.PT
        LanguageMode.ID -> AppLanguage.ID
    }
}

class MainActivity : ComponentActivity() {
    private val db by lazy { AppDatabase.get(this) }
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.schedule(this)
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
        if (savedInstanceState == null) {
            InAppReviewHelper.recordLaunch(this)
        }
        enableEdgeToEdge()

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
                    restoreLastScreenFromPrefs = true,
                    onThemeChange = {
                        appTheme = it
                        prefs.edit().putString(KEY_THEME, it.name).apply()
                    },
                    onThemeModeChange = {
                        themeMode = it
                        prefs.edit().putString(KEY_THEME_MODE, it.name).apply()
                    },
                    onRequestNotificationPermission = { requestNotificationPermissionIfNeeded(force = false) },
                    onOpenNotificationSettings = { requestNotificationPermissionIfNeeded(force = true) },
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

private fun hasOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        android.net.Uri.parse("package:${context.packageName}")
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private fun setLiveOverlayEnabled(context: Context, enabled: Boolean) {
    val intent = Intent(context, LiveUsageOverlayService::class.java)
    if (enabled && hasOverlayPermission(context)) {
        runCatching { context.startService(intent) }
    } else {
        runCatching { context.stopService(intent) }
    }
}

private fun hasNotificationPermission(context: android.content.Context): Boolean {
    return Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun openNotificationSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onSuccess { return }

    val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(fallbackIntent) }
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
    if (manager.getNotificationChannel(channelId) == null) {
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
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
        UsageReader.markAppLaunched(context, packageName)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun shareCurrentScreenImage(context: Context, language: AppLanguage) {
    val activity = context.findActivity() ?: return
    runCatching {
        val root = activity.findViewById<View>(android.R.id.content)
        val bitmap = root.drawToBitmap(Bitmap.Config.ARGB_8888)
        val shareDir = File(activity.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(shareDir, "closed_test_tracker_gallery.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Closed Test Tracker")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(
            Intent.createChooser(
                intent,
                text(language, "Ekran görüntüsünü paylaş", "Share screenshot", "Partager la capture", "Compartir captura", "分享截图", "स्क्रीनशॉट साझा करें", "Поделиться скриншотом", "مشاركة لقطة الشاشة")
            )
        )
    }.onFailure {
        Toast.makeText(
            context,
            text(language, "Paylaşım açılamadı", "Could not open share sheet", "Impossible d'ouvrir le partage", "No se pudo abrir compartir", "无法打开分享", "शेयर शीट नहीं खुली", "Не удалось открыть меню отправки", "تعذر فتح نافذة المشاركة"),
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun captureCurrentScreenImage(context: Context): Boolean {
    val activity = context.findActivity() ?: return false
    return runCatching {
        val root = activity.findViewById<View>(android.R.id.content)
        val bitmap = root.drawToBitmap(Bitmap.Config.ARGB_8888)
        val shareDir = File(activity.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(shareDir, "closed_test_tracker_gallery.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    }.getOrDefault(false)
}

private fun shareCachedGalleryImage(context: Context, language: AppLanguage) {
    val activity = context.findActivity() ?: return
    runCatching {
        val file = File(File(activity.cacheDir, "shared_images"), "closed_test_tracker_gallery.png")
        if (!file.exists()) {
            captureCurrentScreenImage(context)
        }
        if (!file.exists()) error("Screenshot cache was not created")
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Closed Test Tracker")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(
            Intent.createChooser(
                intent,
                text(language, "Ekran görüntüsünü paylaş", "Share screenshot", "Partager la capture", "Compartir captura", "分享截图", "स्क्रीनशॉट साझा करें", "Поделиться скриншотом", "مشاركة لقطة الشاشة")
            )
        )
    }.onFailure {
        Toast.makeText(
            context,
            text(language, "Paylaşım açılamadı", "Could not open share sheet", "Impossible d'ouvrir le partage", "No se pudo abrir compartir", "无法打开分享", "शेयर शीट नहीं खुली", "Не удалось открыть меню отправки", "تعذر فتح نافذة المشاركة"),
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun shareAppWithFriends(context: Context, language: AppLanguage) {
    val shareText = text(
        language,
        "Closed Test Tracker ile Google Play kapalı testlerini ve günlük kullanım serilerini takip edebilirsin:\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "Track Google Play closed tests and daily usage streaks with Closed Test Tracker:\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "Suivez les tests fermés Google Play et les séries d'utilisation quotidienne avec Closed Test Tracker :\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "Sigue pruebas cerradas de Google Play y rachas de uso diario con Closed Test Tracker:\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "使用 Closed Test Tracker 跟踪 Google Play 封闭测试和每日使用连续记录：\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "Closed Test Tracker से Google Play closed tests और daily usage streaks ट्रैक करें:\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "Отслеживайте закрытые тесты Google Play и ежедневные серии с Closed Test Tracker:\nhttps://play.google.com/store/apps/details?id=${context.packageName}",
        "تابع اختبارات Google Play المغلقة وسلاسل الاستخدام اليومية عبر Closed Test Tracker:\nhttps://play.google.com/store/apps/details?id=${context.packageName}"
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Closed Test Tracker")
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(
                intent,
                text(language, "Uygulamayı paylaş", "Share app", "Partager l'application", "Compartir app", "分享应用", "ऐप साझा करें", "Поделиться приложением", "مشاركة التطبيق")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
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

private fun openStudioPage(context: android.content.Context) {
    openExternalPage(context, MD_STUDIO_URL)
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

private fun isDebuggableApp(context: Context): Boolean {
    return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

private fun adLoadMessage(language: AppLanguage, error: LoadAdError): String {
    return when (error.code) {
        AdRequest.ERROR_CODE_NO_FILL -> text(
            language,
            "Şu anda uygun reklam yok. Biraz sonra tekrar dene.",
            "No suitable ad is available right now. Try again later.",
            "Aucune publicité adaptée pour le moment. Réessayez plus tard.",
            "No hay un anuncio adecuado ahora. Inténtalo más tarde.",
            "目前没有合适的广告，请稍后再试。",
            "अभी उपयुक्त विज्ञापन उपलब्ध नहीं है। बाद में फिर कोशिश करें।",
            "Сейчас нет подходящей рекламы. Попробуйте позже.",
            "لا يوجد إعلان مناسب الآن. حاول لاحقاً."
        )
        AdRequest.ERROR_CODE_NETWORK_ERROR -> text(
            language,
            "Bağlantı sorunu nedeniyle reklam alınamadı.",
            "Ad could not load because of a network issue.",
            "La publicité n'a pas pu être chargée à cause du réseau.",
            "El anuncio no pudo cargarse por un problema de red.",
            "由于网络问题，广告无法加载。",
            "नेटवर्क समस्या के कारण विज्ञापन लोड नहीं हुआ।",
            "Реклама не загрузилась из-за проблемы сети.",
            "تعذر تحميل الإعلان بسبب مشكلة في الشبكة."
        )
        AdRequest.ERROR_CODE_INVALID_REQUEST -> text(
            language,
            "Reklam isteği geçersiz görünüyor. Yayın ayarları kontrol edilmeli.",
            "The ad request looks invalid. Publishing settings should be checked.",
            "La demande de publicité semble invalide. Vérifiez les réglages.",
            "La solicitud de anuncio parece inválida. Revisa la configuración.",
            "广告请求无效，请检查发布设置。",
            "विज्ञापन अनुरोध अमान्य है। पब्लिशिंग सेटिंग जांचें।",
            "Запрос рекламы недействителен. Проверьте настройки.",
            "طلب الإعلان غير صالح. تحقق من إعدادات النشر."
        )
        else -> text(
            language,
            "Reklam yüklenemedi. Biraz sonra tekrar dene.",
            "Ad could not load. Try again later.",
            "La publicité n'a pas pu être chargée. Réessayez plus tard.",
            "El anuncio no pudo cargarse. Inténtalo más tarde.",
            "广告无法加载，请稍后再试。",
            "विज्ञापन लोड नहीं हुआ। बाद में फिर कोशिश करें।",
            "Реклама не загрузилась. Попробуйте позже.",
            "تعذر تحميل الإعلان. حاول لاحقاً."
        )
    }
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
    val launchableActivities = if (Build.VERSION.SDK_INT >= 33) {
        pm.queryIntentActivities(
            launchIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
        )
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
    }

    return launchableActivities.asSequence()
        .mapNotNull { it.activityInfo?.applicationInfo }
        .filter { info ->
            (info.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        }
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

private fun appIconBitmap(pm: PackageManager, packageName: String): Bitmap? {
    return runCatching { pm.getApplicationIcon(packageName).toBitmap() }.getOrNull()
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
    return Html.fromHtml(withoutTags, Html.FROM_HTML_MODE_LEGACY).toString().trim()
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
    restoreLastScreenFromPrefs: Boolean,
    onThemeChange: (AppTheme) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
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
    var trackedLoaded by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var showProOffer by remember { mutableStateOf(false) }
    var showProHub by remember { mutableStateOf(false) }
    var showTestShare by remember { mutableStateOf(false) }
    var showTopActions by rememberSaveable { mutableStateOf(false) }
    var addAnotherPromptLabel by remember { mutableStateOf<String?>(null) }
    var whatsNewVisible by remember { mutableStateOf(false) }
    var debugModeEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_DEBUG_MODE_ENABLED, false)) }
    var settingsIconTapCount by remember { mutableIntStateOf(0) }
    val proManager = remember { ProManager(context) }
    val proBillingState by proManager.state.collectAsState()
    val proStorage = remember { ProStorage(context) }
    // TEMP_PRO_UNLOCK forces all Pro features open; flip the flag above to revert.
    val isPro = proBillingState.isPro || BuildConfig.PRO_PREVIEW || TEMP_PRO_UNLOCK
    var appBackground by remember {
        mutableStateOf(
            runCatching {
                AppBackground.valueOf(
                    prefs.getString(KEY_BACKGROUND_STYLE, AppBackground.SIMPLE.name) ?: AppBackground.SIMPLE.name
                )
            }.getOrDefault(AppBackground.SIMPLE)
        )
    }
    var customGradient by remember {
        mutableStateOf(
            CustomGradient(
                startColor = prefs.getLong(KEY_GRADIENT_START, CustomGradient().startColor),
                endColor = prefs.getLong(KEY_GRADIENT_END, CustomGradient().endColor),
                type = runCatching {
                    GradientType.valueOf(
                        prefs.getString(KEY_GRADIENT_TYPE, GradientType.DIAGONAL.name) ?: GradientType.DIAGONAL.name
                    )
                }.getOrDefault(GradientType.DIAGONAL)
            )
        )
    }
    // Rich/custom backgrounds are Pro perks; Normal remains available even for Pro users.
    val effectiveBackground = if (isPro) {
        appBackground
    } else {
        AppBackground.SIMPLE
    }
    val restoredPackageName = remember(restoreLastScreenFromPrefs) {
        if (restoreLastScreenFromPrefs) {
            prefs.getString(KEY_LAST_SELECTED_PACKAGE, null)?.takeIf { it.isNotBlank() }
        } else {
            null
        }
    }
    val restoredScreen = remember(restoreLastScreenFromPrefs, restoredPackageName) {
        val savedAt = prefs.getLong(KEY_LAST_SCREEN_SAVED_AT, 0L)
        val canRestore = restoreLastScreenFromPrefs &&
            savedAt > 0L &&
            System.currentTimeMillis() - savedAt <= LAST_SCREEN_RESTORE_TIMEOUT_MILLIS
        val value = runCatching {
            AppScreen.valueOf(prefs.getString(KEY_LAST_SCREEN, AppScreen.HOME.name) ?: AppScreen.HOME.name)
        }.getOrDefault(AppScreen.HOME)
        if (!canRestore || value == AppScreen.HOWTO || (value == AppScreen.DETAIL && restoredPackageName.isNullOrBlank())) {
            AppScreen.HOME
        } else {
            value
        }
    }
    val canRestoreLastUiState = remember(restoreLastScreenFromPrefs) {
        val savedAt = prefs.getLong(KEY_LAST_SCREEN_SAVED_AT, 0L)
        restoreLastScreenFromPrefs &&
            savedAt > 0L &&
            System.currentTimeMillis() - savedAt <= LAST_SCREEN_RESTORE_TIMEOUT_MILLIS
    }
    var screen by rememberSaveable { mutableStateOf(restoredScreen) }
    var homeFilter by rememberSaveable { mutableStateOf(HomeFilter.ACTIVE) }
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
    var reminderHour by remember { mutableIntStateOf(prefs.getInt(KEY_REMINDER_HOUR, 20)) }
    var liveOverlayEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_LIVE_USAGE_OVERLAY, false)) }
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
    var selectedPackageName by rememberSaveable { mutableStateOf(restoredPackageName) }
    var gallerySharePromptVisible by rememberSaveable { mutableStateOf(false) }
    var usageAccess by remember { mutableStateOf(UsageReader.hasUsageAccess(context)) }
    var notificationAllowed by remember { mutableStateOf(hasNotificationPermission(context)) }
    var overlayAllowed by remember { mutableStateOf(hasOverlayPermission(context)) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var lastUsageRefreshAt by remember { mutableLongStateOf(0L) }
    var playUpdateState by remember { mutableStateOf(PlayUpdateState()) }
    var updatePromptVisible by remember { mutableStateOf(false) }
    var supportRewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var supportAdLoading by remember { mutableStateOf(false) }
    var supportAdStatus by remember { mutableStateOf<String?>(null) }
    var supportAdRetryAfterMillis by remember { mutableLongStateOf(0L) }
    val homeListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = if (canRestoreLastUiState) prefs.getInt(KEY_HOME_LIST_INDEX, 0) else 0,
            firstVisibleItemScrollOffset = if (canRestoreLastUiState) prefs.getInt(KEY_HOME_LIST_OFFSET, 0) else 0
        )
    }
    var homePullDistance by remember { mutableStateOf(0f) }
    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    val playPublishers = remember { mutableStateMapOf<String, String>() }
    val playPublisherRequested = remember { mutableStateMapOf<String, Boolean>() }
    val usageSummaryCache = remember { mutableStateMapOf<String, UsageSummary>() }
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    fun requestUsageRefresh(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (force || now - lastUsageRefreshAt >= USAGE_REFRESH_MIN_INTERVAL_MILLIS) {
            lastUsageRefreshAt = now
            refreshTick++
        }
    }
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
    val uiScope = rememberCoroutineScope()
    val appLogoBitmap = remember {
        runCatching {
            drawableToBitmap(context.packageManager.getApplicationIcon(context.packageName))
        }.getOrNull()
    }
    fun persistLastUiState() {
        if (screen == AppScreen.HOWTO) return
        prefs.edit()
            .putString(KEY_LAST_SCREEN, screen.name)
            .putString(KEY_LAST_SELECTED_PACKAGE, selectedPackageName.orEmpty())
            .putInt(KEY_HOME_LIST_INDEX, homeListState.firstVisibleItemIndex)
            .putInt(KEY_HOME_LIST_OFFSET, homeListState.firstVisibleItemScrollOffset)
            .putLong(KEY_LAST_SCREEN_SAVED_AT, System.currentTimeMillis())
            .apply()
    }
    val appDisplayName = remember { context.getString(R.string.app_name) }
    val selectedItem = tracked.firstOrNull { it.packageName == selectedPackageName }
    val topTitle = when (screen) {
        AppScreen.SETTINGS -> text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки")
        AppScreen.DETAIL -> selectedItem?.appLabel ?: text(language, "Detay", "Detail", "Détail", "Detalle", "详情", "विवरण", "Детали")
        AppScreen.HOWTO -> text(language, "Nasıl kullanılır", "How to use", "Utilisation", "Cómo usar", "如何使用", "कैसे उपयोग करें", "Как использовать", "طريقة الاستخدام")
        AppScreen.GALLERY -> text(language, "Genel özet", "General summary", "Résumé général", "Resumen general", "总览摘要", "सामान्य सारांश", "Общая сводка", "الملخص العام")
        AppScreen.DEBUG -> text(language, "Debug modu", "Debug mode", "Mode debug", "Modo debug", "调试模式", "Debug मोड", "Режим отладки", "وضع التصحيح", de = "Debug-Modus", ja = "デバッグモード", pt = "Modo debug", id = "Mode debug")
        AppScreen.HOME -> appDisplayName
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

    fun rewardedAdUnitId(): String {
        return if (isDebuggableApp(context)) REWARDED_TEST_AD_UNIT_ID else REWARDED_PROD_AD_UNIT_ID
    }

    fun loadSupportRewardedAd(force: Boolean = false) {
        if ((supportRewardedAd != null && !force) || supportAdLoading) return
        val now = System.currentTimeMillis()
        if (!force && now < supportAdRetryAfterMillis) return
        supportAdLoading = true
        supportAdStatus = text(
            language,
            "Reklam hazırlanıyor...",
            "Preparing ad...",
            "Préparation de la publicité...",
            "Preparando anuncio...",
            "正在准备广告...",
            "विज्ञापन तैयार हो रहा है...",
            "Реклама подготавливается...",
            "يتم تجهيز الإعلان..."
        )
        RewardedAd.load(
            context,
            rewardedAdUnitId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    supportAdLoading = false
                    supportRewardedAd = ad
                    supportAdRetryAfterMillis = 0L
                    supportAdStatus = text(
                        language,
                        "Reklam hazır.",
                        "Ad is ready.",
                        "La publicité est prête.",
                        "El anuncio está listo.",
                        "广告已准备好。",
                        "विज्ञापन तैयार है।",
                        "Реклама готова.",
                        "الإعلان جاهز."
                    )
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    supportAdLoading = false
                    supportRewardedAd = null
                    supportAdRetryAfterMillis = System.currentTimeMillis() + if (error.code == AdRequest.ERROR_CODE_NO_FILL) 60_000L else 20_000L
                    supportAdStatus = adLoadMessage(language, error)
                    Log.w("ClosedTestAds", "Rewarded failed: code=${error.code}, domain=${error.domain}, message=${error.message}")
                }
            }
        )
    }

    fun showSupportRewardedAd() {
        val activity = context.findActivity()
        val ad = supportRewardedAd
        if (activity == null) {
            Toast.makeText(context, text(language, "Bu işlem için ekran aktif olmalı.", "Screen must be active for this action."), Toast.LENGTH_SHORT).show()
            return
        }
        if (ad == null) {
            loadSupportRewardedAd(force = true)
            Toast.makeText(
                context,
                supportAdStatus ?: text(
                    language,
                    "Reklam hazırlanıyor, birkaç saniye sonra tekrar dene.",
                    "Ad is loading, try again in a few seconds.",
                    "La publicité se charge, réessayez dans quelques secondes.",
                    "El anuncio se está cargando, inténtalo de nuevo en unos segundos.",
                    "广告正在加载，请几秒后重试。",
                    "विज्ञापन लोड हो रहा है, कुछ सेकंड बाद फिर कोशिश करें।",
                    "Реклама загружается, попробуйте снова через несколько секунд.",
                    "يتم تحميل الإعلان، حاول مرة أخرى بعد بضع ثوانٍ."
                ),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                supportRewardedAd = null
                loadSupportRewardedAd()
            }
            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                supportRewardedAd = null
                loadSupportRewardedAd(force = true)
            }
        }
        ad.show(activity) {
            Toast.makeText(
                context,
                text(
                    language,
                    "Destek için teşekkürler!",
                    "Thanks for your support!",
                    "Merci pour votre soutien !",
                    "¡Gracias por tu apoyo!",
                    "感谢支持！",
                    "समर्थन के लिए धन्यवाद!",
                    "Спасибо за поддержку!",
                    "شكراً لدعمك!"
                ),
                Toast.LENGTH_SHORT
            ).show()
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

    LaunchedEffect(Unit) {
        observeTrackedApps {
            tracked = it
            trackedLoaded = true
        }
    }
    LaunchedEffect(trackedLoaded) {
        if (!trackedLoaded) return@LaunchedEffect
        // Let the user settle in before the (rarely shown) Play review sheet.
        delay(8_000)
        val activity = context as? android.app.Activity ?: return@LaunchedEffect
        InAppReviewHelper.maybeRequestReview(
            activity,
            hasActiveTracking = tracked.any { !it.isArchived && it.completedAtMillis == null }
        )
    }
    LaunchedEffect(isPro) {
        if (!isPro) MobileAds.initialize(context) {}
    }
    LaunchedEffect(Unit) {
        proManager.connect()
        if (!prefs.getBoolean(KEY_PRO_OFFER_SHOWN, false) && !proBillingState.isPro) {
            showProOffer = true
            prefs.edit().putBoolean(KEY_PRO_OFFER_SHOWN, true).apply()
        }
    }
    DisposableEffect(proManager) {
        onDispose { proManager.close() }
    }
    LaunchedEffect(Unit) { refreshInstalledApps() }
    LaunchedEffect(Unit) { refreshPlayUpdateState() }
    LaunchedEffect(appVersionName) {
        val lastSeen = prefs.getString(KEY_LAST_SEEN_VERSION_NAME, null)
        if (lastSeen == null) {
            prefs.edit().putString(KEY_LAST_SEEN_VERSION_NAME, appVersionName).apply()
        } else if (lastSeen != appVersionName) {
            whatsNewVisible = true
            prefs.edit().putString(KEY_LAST_SEEN_VERSION_NAME, appVersionName).apply()
        }
    }
    LaunchedEffect(isPro) {
        if (!isPro) loadSupportRewardedAd()
    }
    LaunchedEffect(Unit) {
        delay(700)
        onRequestNotificationPermission()
    }
    LaunchedEffect(Unit) {
        if (!prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)) {
            screen = AppScreen.HOWTO
            prefs.edit().putBoolean(KEY_ONBOARDING_SHOWN, true).apply()
        }
    }
    LaunchedEffect(screen, selectedPackageName) {
        persistLastUiState()
    }
    // The detail screen recomputes usage on every open, but the home list only
    // refreshed on a throttled tick, so list durations could lag behind the
    // detail view. Force a fresh usage recompute whenever the home list becomes
    // visible so both screens stay consistent.
    LaunchedEffect(screen) {
        if (screen == AppScreen.HOME) {
            requestUsageRefresh(force = true)
        }
    }
    LaunchedEffect(homeListState) {
        snapshotFlow {
            homeListState.firstVisibleItemIndex to homeListState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                prefs.edit()
                    .putInt(KEY_HOME_LIST_INDEX, index)
                    .putInt(KEY_HOME_LIST_OFFSET, offset)
                    .putLong(KEY_LAST_SCREEN_SAVED_AT, System.currentTimeMillis())
                    .apply()
            }
    }
    LaunchedEffect(screen, tracked) {
        if (screen == AppScreen.DETAIL && selectedPackageName != null && tracked.isNotEmpty() && tracked.none { it.packageName == selectedPackageName }) {
            selectedPackageName = null
            screen = AppScreen.HOME
        }
    }
    LaunchedEffect(refreshTick) {
        usageAccess = UsageReader.hasUsageAccess(context)
        notificationAllowed = hasNotificationPermission(context)
        overlayAllowed = hasOverlayPermission(context)
        setLiveOverlayEnabled(context, liveOverlayEnabled && overlayAllowed)
        refreshPlayUpdateState()
    }
    LaunchedEffect(liveOverlayEnabled, overlayAllowed) {
        setLiveOverlayEnabled(context, liveOverlayEnabled && overlayAllowed)
    }
    LaunchedEffect(usageAccess) {
        while (true) {
            delay(USAGE_IDLE_REFRESH_INTERVAL_MILLIS)
            requestUsageRefresh(force = false)
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val finishedTrackedSession = UsageReader.finishPendingLaunchedSession(context)
                    requestUsageRefresh(force = finishedTrackedSession)
                    lifecycleOwner.lifecycleScope.launch {
                        refreshInstalledApps()
                        if (finishedTrackedSession) {
                            delay(1_200)
                            requestUsageRefresh(force = true)
                        }
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    persistLastUiState()
                }
                else -> Unit
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
    DisposableEffect(screen) {
        val activity = context.findActivity()
        if (Build.VERSION.SDK_INT >= 34 && activity != null && screen == AppScreen.GALLERY) {
            val callback = object : Activity.ScreenCaptureCallback {
                override fun onScreenCaptured() {
                    gallerySharePromptVisible = true
                }
            }
            activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
            onDispose {
                runCatching { activity.unregisterScreenCaptureCallback(callback) }
            }
        } else {
            onDispose { }
        }
    }
    LaunchedEffect(screen, tracked, apps) {
        if (screen == AppScreen.GALLERY) {
            delay(350)
            captureCurrentScreenImage(context)
        }
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
    val filteredTracked = remember(tracked, homeFilter, homeSearchQuery) {
        val base = when (homeFilter) {
            HomeFilter.ACTIVE -> activeTracked
            HomeFilter.COMPLETED -> completedTracked
            HomeFilter.ARCHIVED -> archivedTracked
            HomeFilter.ALL -> tracked
        }
        val q = homeSearchQuery.trim().lowercase()
        if (q.isBlank()) {
            base
        } else {
            base.filter {
                it.appLabel.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }
    }
    val visibleTrackedApps = remember(tracked) {
        tracked.filterNot { it.isArchived }
    }
    val visibleUsagePackages = remember(tracked) {
        visibleTrackedApps.map { it.packageName }
    }
    val visibleUsagePackageKey = remember(visibleUsagePackages) { visibleUsagePackages.joinToString("|") }
    var todayUsageCache by remember(visibleUsagePackageKey) {
        mutableStateOf(readUsageCache(prefs, visibleUsagePackages, KEY_USAGE_TODAY_CACHE_PREFIX))
    }
    var totalUsageCache by remember(visibleUsagePackageKey) {
        mutableStateOf(readUsageCache(prefs, visibleUsagePackages, KEY_USAGE_TOTAL_CACHE_PREFIX))
    }
    val todayUsageState by produceState(
        initialValue = TodayUsageState(
            minutesByPackage = todayUsageCache,
            isLoading = usageAccess && visibleUsagePackages.isNotEmpty() && todayUsageCache.isEmpty()
        ),
        usageAccess,
        refreshTick,
        visibleTrackedApps
    ) {
        value = if (!usageAccess || visibleTrackedApps.isEmpty()) {
            TodayUsageState(isLoading = false)
        } else {
            if (todayUsageCache.isNotEmpty()) {
                value = TodayUsageState(minutesByPackage = todayUsageCache, isLoading = false)
            }
            val fresh = withContext(Dispatchers.Default) {
                val todayStart = SeriesCalculator.dayStartMillis(0)
                val now = System.currentTimeMillis()
                visibleTrackedApps.associate { item ->
                    val minutes = UsageReader.mergedUsageMinutesByDayMap(
                        context = context,
                        packageName = item.packageName,
                        startMillis = todayStart,
                        endMillis = now
                    )[todayStart] ?: 0L
                    item.packageName to minutes
                }
            }
            todayUsageCache = fresh
            writeUsageCache(prefs, fresh, KEY_USAGE_TODAY_CACHE_PREFIX)
            TodayUsageState(
                minutesByPackage = fresh,
                isLoading = false
            )
        }
    }
    val todayUsageMap = todayUsageState.minutesByPackage
    val todayUsageLoading = todayUsageState.isLoading
    var missingHighlightReady by remember { mutableStateOf(false) }
    LaunchedEffect(usageAccess, visibleUsagePackages, todayUsageLoading) {
        if (!usageAccess || visibleUsagePackages.isEmpty() || todayUsageLoading) {
            missingHighlightReady = false
        } else {
            delay(2_500)
            missingHighlightReady = true
        }
    }
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
                        val eventDaily = UsageReader.usageMinutesByDayMapFromEventsForDisplay(
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
            writeUsageCache(prefs, fresh, KEY_USAGE_TOTAL_CACHE_PREFIX)
            TodayUsageState(
                minutesByPackage = fresh,
                isLoading = false
            )
        }
    }
    val totalUsageMap = totalUsageState.minutesByPackage
    val totalUsageLoading = totalUsageState.isLoading
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
        darkTheme = darkTheme,
        appBackground = effectiveBackground,
        customGradient = customGradient
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (screen) {
                            AppScreen.HOME -> {
                                if (appLogoBitmap != null) {
                                    Image(
                                        bitmap = appLogoBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(29.dp)
                                    )
                                }
                            }
                            AppScreen.HOWTO -> {
                                Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = null, modifier = Modifier.size(22.dp))
                            }
                            AppScreen.SETTINGS -> {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable {
                                            settingsIconTapCount++
                                            if (settingsIconTapCount >= 4) {
                                                settingsIconTapCount = 0
                                                debugModeEnabled = true
                                                prefs.edit().putBoolean(KEY_DEBUG_MODE_ENABLED, true).apply()
                                                screen = AppScreen.DEBUG
                                                Toast.makeText(
                                                    context,
                                                    text(language, "Debug modu açıldı", "Debug mode enabled"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                )
                            }
                            AppScreen.GALLERY -> {
                                GridDotsIcon(modifier = Modifier.size(22.dp))
                            }
                            AppScreen.DETAIL -> Unit
                            AppScreen.DEBUG -> {
                                Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(22.dp))
                            }
                        }
                        if (!(screen == AppScreen.HOME && showTopActions)) {
                            Text(
                                topTitle,
                                modifier = Modifier.weight(1f, fill = true),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                },
                navigationIcon = {
                    // Intentionally empty: back navigation uses system back gesture/button.
                },
                actions = {
                    if (screen == AppScreen.HOME) {
                        Row(
                            modifier = Modifier.padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showPicker = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = text(language, "Uygulama ekle", "Add app", "Ajouter une app", "Añadir app", "添加应用", "ऐप जोड़ें", "Добавить приложение", "إضافة تطبيق"))
                            }
                            if (showTopActions) {
                                IconButton(onClick = { showTestShare = true }) {
                                    Icon(Icons.Rounded.Share, contentDescription = text(language, "Yeni test paylaş", "Share new test"))
                                }
                                IconButton(onClick = { screen = AppScreen.HOWTO }) {
                                    Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = text(language, "Nasıl kullanılır", "How to use", "Utilisation", "Cómo usar", "如何使用", "कैसे उपयोग करें", "Как использовать", "طريقة الاستخدام"))
                                }
                                if (isPro) {
                                    TopActionPill(
                                        onClick = { showProHub = true },
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = if (isDarkScheme()) 0.26f else 0.18f),
                                        contentColor = MaterialTheme.colorScheme.secondary
                                    ) {
                                        Icon(Icons.Rounded.Star, contentDescription = text(language, "Pro araçlar", "Pro tools", "Outils Pro", "Herramientas Pro", "Pro 工具", "Pro टूल", "Инструменты Pro", "أدوات Pro"), modifier = Modifier.size(20.dp))
                                    }
                                }
                                if (debugModeEnabled) {
                                    TopActionPill(
                                        onClick = { screen = AppScreen.DEBUG },
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.28f else 0.16f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Icon(Icons.Rounded.Info, contentDescription = text(language, "Debug sayfası", "Debug page", "Page debug", "Página debug", "调试页面", "Debug page", "Страница отладки", "صفحة التصحيح", de = "Debug-Seite", ja = "デバッグページ", pt = "Página debug", id = "Halaman debug"), modifier = Modifier.size(20.dp))
                                    }
                                }
                                IconButton(onClick = { screen = AppScreen.SETTINGS }) {
                                    Icon(Icons.Rounded.Settings, contentDescription = text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки"))
                                }
                            }
                            IconButton(onClick = { showTopActions = !showTopActions }) {
                                ActionRailToggleIcon(
                                    expanded = showTopActions,
                                    modifier = Modifier.size(25.dp)
                                )
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
                        overlayAllowed = overlayAllowed,
                        liveOverlayEnabled = liveOverlayEnabled,
                        showLiveOverlayOption = true,
                        isPro = isPro,
                        appBackground = appBackground,
                        customGradient = customGradient,
                        onBackgroundChange = { style ->
                            appBackground = style
                            prefs.edit().putString(KEY_BACKGROUND_STYLE, style.name).apply()
                        },
                        onCustomGradientChange = { gradient ->
                            customGradient = gradient
                            prefs.edit()
                                .putLong(KEY_GRADIENT_START, gradient.startColor)
                                .putLong(KEY_GRADIENT_END, gradient.endColor)
                                .putString(KEY_GRADIENT_TYPE, gradient.type.name)
                                .apply()
                        },
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
                        onLiveOverlayChange = { enabled ->
                            liveOverlayEnabled = enabled
                            prefs.edit().putBoolean(KEY_LIVE_USAGE_OVERLAY, enabled).apply()
                            if (enabled && !hasOverlayPermission(context)) {
                                openOverlaySettings(context)
                            } else {
                                setLiveOverlayEnabled(context, enabled)
                            }
                        },
                        appVersionName = appVersionName,
                        playUpdateState = playUpdateState,
                        usageAccess = usageAccess,
                        debugModeEnabled = debugModeEnabled,
                        onDisableDebugMode = {
                            debugModeEnabled = false
                            prefs.edit().putBoolean(KEY_DEBUG_MODE_ENABLED, false).apply()
                        },
                        onOpenDebugMode = { screen = AppScreen.DEBUG },
                        onRequestNotificationPermission = onOpenNotificationSettings,
                        onSendMail = { sendSupportMail(context, language) },
                        onDonate = { openDonationPage(context) },
                        supportAdStatus = supportAdStatus,
                        supportAdLoading = supportAdLoading,
                        supportAdReady = supportRewardedAd != null,
                        onWatchSupportAd = { showSupportRewardedAd() },
                        onShareApp = { shareAppWithFriends(context, language) },
                        onOpenPolicyPage = { openPolicyPage(context) },
                        onOpenStudioPage = { openStudioPage(context) },
                        onOpenPro = { showProHub = true },
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
                            onOpenApp = {
                                persistLastUiState()
                                openTrackedApp(context, item.packageName)
                            },
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

                AppScreen.HOWTO -> {
                    HowToScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(p)
                            .padding(10.dp),
                        language = language,
                        onContinue = { screen = AppScreen.HOME }
                    )
                }

                AppScreen.GALLERY -> {
                    Box(Modifier.fillMaxSize()) {
                        AppGalleryScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(p)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            language = language,
                            screenshotMode = false,
                            appDisplayName = appDisplayName,
                            appLogoBitmap = appLogoBitmap,
                            tracked = tracked,
                            installedAppsByPackage = apps.associateBy { it.packageName },
                            onSelect = { packageName ->
                                selectedPackageName = packageName
                                screen = AppScreen.DETAIL
                            }
                        )
                        if (gallerySharePromptVisible) {
                            ScreenshotSharePrompt(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(p)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                language = language,
                                onShare = {
                                    gallerySharePromptVisible = false
                                    shareCachedGalleryImage(context, language)
                                },
                                onDismiss = { gallerySharePromptVisible = false }
                            )
                        }
                    }
                }

                AppScreen.DEBUG -> {
                    DebugPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(p)
                            .padding(10.dp),
                        language = language,
                        appVersionName = appVersionName,
                        usageAccess = usageAccess,
                        overlayAllowed = overlayAllowed,
                        liveOverlayEnabled = liveOverlayEnabled,
                        notificationAllowed = notificationAllowed,
                        trackedApps = tracked,
                        todayUsageMap = todayUsageMap,
                        totalUsageMap = totalUsageMap,
                        todayLoading = todayUsageLoading,
                        totalLoading = totalUsageLoading,
                        onRefresh = { requestUsageRefresh(force = true) },
                        onDisable = {
                            debugModeEnabled = false
                            prefs.edit().putBoolean(KEY_DEBUG_MODE_ENABLED, false).apply()
                            screen = AppScreen.SETTINGS
                        }
                    )
                }

                AppScreen.HOME -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(p)
                            .padding(10.dp)
                            .pointerInput(homeListState) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (homePullDistance > 90f) {
                                            requestUsageRefresh(force = true)
                                        }
                                        homePullDistance = 0f
                                    },
                                    onDragCancel = { homePullDistance = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        val atTop = homeListState.firstVisibleItemIndex == 0 &&
                                            homeListState.firstVisibleItemScrollOffset == 0
                                        if (atTop && dragAmount > 0f) {
                                            homePullDistance = (homePullDistance + dragAmount).coerceAtMost(160f)
                                        } else if (dragAmount < 0f) {
                                            homePullDistance = (homePullDistance + dragAmount).coerceAtLeast(0f)
                                        }
                                    }
                                )
                            }
                    ) {
                        if (todayUsageLoading || totalUsageLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .zIndex(3f),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            )
                        }
                        if (homePullDistance > 8f) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .graphicsLayer {
                                        translationY = (homePullDistance / 4f).coerceAtMost(34f)
                                    }
                                    .zIndex(2f),
                                color = canvasColor(),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(999.dp),
                                border = panelBorder(),
                                shadowElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(17.dp)
                                                .graphicsLayer {
                                                    rotationZ = (homePullDistance / 90f).coerceIn(0f, 1f) * 180f
                                                }
                                        )
                                        Text(
                                            text = if (homePullDistance > 90f) {
                                                text(language, "Bırak yenilensin", "Release to refresh", "Relâcher pour actualiser", "Suelta para actualizar", "松开刷新", "छोड़ें और रीफ्रेश करें", "Отпустите для обновления", "اترك للتحديث", de = "Zum Aktualisieren loslassen", ja = "離して更新", pt = "Solte para atualizar", id = "Lepas untuk refresh")
                                            } else {
                                                text(language, "Yenilemek için çek", "Pull to refresh", "Tirer pour actualiser", "Tira para actualizar", "下拉刷新", "रीफ्रेश के लिए खींचें", "Потяните для обновления", "اسحب للتحديث", de = "Zum Aktualisieren ziehen", ja = "引いて更新", pt = "Puxe para atualizar", id = "Tarik untuk refresh")
                                            },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { (homePullDistance / 90f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .width(132.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(999.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    )
                                }
                            }
                        }
                        LazyColumn(
                            state = homeListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 130.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (trackedLoaded && tracked.isNotEmpty()) {
                                item {
                                    DashboardHeader(
                                    language = language,
                                    usedTodayCount = usedTodayCount,
                                    completedCount = completedTracked.size,
                                    todayMinutes = todayTotalMinutes,
                                    onRefresh = { requestUsageRefresh(force = true) },
                                    onOpenGallery = {
                                        screen = AppScreen.GALLERY
                                    }
                                    )
                                }
                                if (isPro) {
                                    item {
                                        ProHomePanel(
                                            language = language,
                                            onOpen = { showProHub = true }
                                        )
                                    }
                                }
                                item {
                                    OutlinedTextField(
                                    value = homeSearchQuery,
                                    onValueChange = { homeSearchQuery = it },
                                    label = { Text(text(language, "Ara", "Search", "Rechercher", "Buscar", "搜索", "खोजें", "Поиск", "بحث")) },
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
                            }

                            if (trackedLoaded && tracked.isNotEmpty() && !usageAccess) {
                                item {
                                    PermissionCard(language, onOpenUsageSettings)
                                }
                            }

                            if (!trackedLoaded) {
                                item {
                                    LoadingListState(language)
                                }
                            } else if (tracked.isEmpty()) {
                                item {
                                    EmptyHomeState(
                                        language = language,
                                        usageAccess = usageAccess,
                                        onAdd = { showPicker = true },
                                        onOpenUsageSettings = onOpenUsageSettings,
                                        onShareTest = { showTestShare = true }
                                    )
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
                                            SwipeableAppCard(
                                                language = language,
                                                onArchive = { onArchiveApp(item.packageName, true) },
                                                onDelete = { onDeleteApp(item.packageName) }
                                            ) {
                                            AppUsageCard(
                                                item = item,
                                                displayLabel = appInfo?.label?.takeUnless { it.isBlank() } ?: item.appLabel,
                                                icon = appInfo?.icon,
                                                playPublisherName = playPublishers[item.packageName],
                                                language = language,
                                                hasUsageAccess = usageAccess,
                                                todayMinutes = if (usageAccess) todayUsageMap[item.packageName] else null,
                                                totalMinutes = if (usageAccess) totalUsageMap[item.packageName] else null,
                                                isTodayLoading = todayUsageLoading && todayUsageMap[item.packageName] == null,
                                                isTotalLoading = totalUsageLoading && totalUsageMap[item.packageName] == null,
                                                isMissingToday = usageAccess &&
                                                    missingHighlightReady &&
                                                    !todayUsageLoading &&
                                                    !item.isArchived &&
                                                    item.completedAtMillis == null &&
                                                    (todayUsageMap[item.packageName] ?: 0L) == 0L,
                                                onOpenApp = {
                                                    persistLastUiState()
                                                    openTrackedApp(context, item.packageName)
                                                },
                                                onClick = {
                                                    selectedPackageName = item.packageName
                                                    screen = AppScreen.DETAIL
                                                }
                                            )
                                            }
                                            if (index != filteredTracked.lastIndex) {
                                                CanvasDivider(Modifier.padding(start = 78.dp, end = 14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!isPro) {
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

    if (showProOffer && !isPro) {
        ProOfferDialog(
            language = language,
            state = proBillingState,
            onBuy = { context.findActivity()?.let(proManager::launchPurchase) },
            onLater = { showProOffer = false }
        )
    }
    if (showProHub) {
        ProHubSheet(
            language = language,
            isPro = isPro,
            billingState = proBillingState,
            storage = proStorage,
            onBuy = { context.findActivity()?.let(proManager::launchPurchase) },
            onDismiss = { showProHub = false }
        )
    }
    if (showTestShare) {
        TestShareSheet(
            language = language,
            isPro = isPro,
            storage = proStorage,
            onDismiss = { showTestShare = false }
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
                    addAnotherPromptLabel = target.label
                } else {
                    onUpdateStartDay(target.packageName, day)
                }
                daySetupTarget = null
            }
        )
    }

    addAnotherPromptLabel?.let { label ->
        ConfirmationSheet(
            title = text(
                language,
                "Uygulama eklendi",
                "App added",
                "Application ajoutée",
                "App añadida",
                "应用已添加",
                "ऐप जोड़ दिया गया",
                "Приложение добавлено",
                "تمت إضافة التطبيق",
                de = "App hinzugefügt",
                ja = "アプリを追加しました",
                pt = "App adicionado",
                id = "Aplikasi ditambahkan"
            ),
            message = text(
                language,
                "$label takip listesine 1. günden eklendi. İstersen şimdi başka bir test uygulaması daha ekleyebilirsin.",
                "$label was added to tracking from day 1. You can add another test app now if you want.",
                "$label a été ajoutée au suivi depuis le jour 1. Vous pouvez ajouter une autre app de test maintenant.",
                "$label se añadió al seguimiento desde el día 1. Puedes añadir otra app de prueba ahora.",
                "$label 已从第 1 天添加到跟踪。你现在可以继续添加另一个测试应用。",
                "$label को दिन 1 से ट्रैकिंग में जोड़ा गया। चाहें तो अभी दूसरा टेस्ट ऐप जोड़ें।",
                "$label добавлено в отслеживание с 1-го дня. При желании можно добавить ещё одно тестовое приложение.",
                "تمت إضافة $label إلى المتابعة من اليوم 1. يمكنك إضافة تطبيق اختبار آخر الآن.",
                de = "$label wurde ab Tag 1 zur Verfolgung hinzugefügt. Du kannst jetzt eine weitere Test-App hinzufügen.",
                ja = "$label は1日目から追跡に追加されました。必要なら別のテストアプリも追加できます。",
                pt = "$label foi adicionado ao acompanhamento desde o dia 1. Você pode adicionar outro app de teste agora.",
                id = "$label ditambahkan ke pelacakan mulai hari 1. Anda bisa menambahkan aplikasi tes lain sekarang."
            ),
            confirmLabel = text(
                language,
                "Başka uygulama ekle",
                "Add another app",
                "Ajouter une autre app",
                "Añadir otra app",
                "再添加一个应用",
                "दूसरा ऐप जोड़ें",
                "Добавить ещё приложение",
                "إضافة تطبيق آخر",
                de = "Weitere App hinzufügen",
                ja = "別のアプリを追加",
                pt = "Adicionar outro app",
                id = "Tambah aplikasi lain"
            ),
            dismissLabel = text(
                language,
                "Kapat",
                "Close",
                "Fermer",
                "Cerrar",
                "关闭",
                "बंद करें",
                "Закрыть",
                "إغلاق",
                de = "Schließen",
                ja = "閉じる",
                pt = "Fechar",
                id = "Tutup"
            ),
            onDismiss = {
                addAnotherPromptLabel = null
                screen = AppScreen.HOME
            },
            onConfirm = {
                addAnotherPromptLabel = null
                screen = AppScreen.HOME
                showPicker = true
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
    if (whatsNewVisible) {
        ConfirmationSheet(
            title = text(language, "Yenilikler", "What's new", "Nouveautés", "Novedades", "更新内容", "नया क्या है", "Что нового", "ما الجديد", de = "Neuigkeiten", ja = "新機能", pt = "Novidades", id = "Yang baru"),
            message = whatsNewText(language),
            confirmLabel = text(language, "Tamam", "OK", "OK", "Aceptar", "确定", "ठीक है", "ОК", "حسناً", de = "OK", ja = "OK", pt = "OK", id = "OK"),
            dismissLabel = text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть", "إغلاق", de = "Schließen", ja = "閉じる", pt = "Fechar", id = "Tutup"),
            onDismiss = { whatsNewVisible = false },
            onConfirm = { whatsNewVisible = false }
        )
    }
}

@Composable
private fun DashboardHeader(
    language: AppLanguage,
    usedTodayCount: Int,
    completedCount: Int,
    todayMinutes: Long,
    onRefresh: () -> Unit,
    onOpenGallery: () -> Unit
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
                        text(language, "Takip", "Tracking", ar = "المتابعة"),
                        color = secondaryTextColor(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text(language, "Uygulamalar", "Apps", ar = "التطبيقات"),
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
                        modifier = Modifier.clickable(onClick = onOpenGallery)
                    ) {
                        GridDotsIcon(modifier = Modifier.padding(10.dp).size(18.dp))
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
private fun ProHomePanel(
    language: AppLanguage,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.30f else 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        if (isDarkScheme()) {
                            listOf(Color(0xFF203239).copy(alpha = 0.96f), Color(0xFF2B281E).copy(alpha = 0.94f), Color(0xFF172226).copy(alpha = 0.96f))
                        } else {
                            listOf(Color(0xFFFFF6DF), Color(0xFFE9F5F0), Color(0xFFFFFEFA))
                        }
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = if (isDarkScheme()) Color(0xFFE2C89A).copy(alpha = 0.18f) else Color(0xFF0F5966).copy(alpha = 0.10f),
                    contentColor = if (isDarkScheme()) Color(0xFFEFD8AA) else Color(0xFF0F5966),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, if (isDarkScheme()) Color(0xFFE2C89A).copy(alpha = 0.28f) else Color(0xFF0F5966).copy(alpha = 0.16f))
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.padding(10.dp).size(20.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = text(language, "Pro araçlar hazır", "Pro tools ready", "Outils Pro prêts", "Herramientas Pro listas", "Pro 工具已就绪", "Pro टूल तैयार", "Инструменты Pro готовы", "أدوات Pro جاهزة", de = "Pro-Werkzeuge bereit", ja = "Proツール準備完了", pt = "Ferramentas Pro prontas", id = "Alat Pro siap"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = text(language, "Reklamsız görünüm, şablonlar, topluluklar ve testçi notları.", "Ad-free view, templates, communities and tester notes.", "Vue sans publicité, modèles, communautés et notes de testeurs.", "Vista sin anuncios, plantillas, comunidades y notas de testers.", "无广告视图、模板、社区和测试者备注。", "बिना विज्ञापन, टेम्पलेट, समुदाय और tester notes.", "Без рекламы, шаблоны, сообщества и заметки тестировщиков.", "عرض بلا إعلانات وقوالب ومجتمعات وملاحظات مختبرين.", de = "Werbefreie Ansicht, Vorlagen, Communitys und Tester-Notizen.", ja = "広告なし表示、テンプレート、コミュニティ、テスター記録。", pt = "Visual sem anúncios, modelos, comunidades e notas de testadores.", id = "Tampilan bebas iklan, template, komunitas, dan catatan penguji."),
                        color = secondaryTextColor(),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp), tint = secondaryTextColor())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProHomeChip(Icons.Rounded.Info, text(language, "Bilgi", "Guide", "Guide", "Guía", "指南", "गाइड", "Гайд", "الدليل"), Modifier.weight(1f))
                ProHomeChip(Icons.Rounded.Share, text(language, "Paylaş", "Post", "Post", "Post", "帖子", "पोस्ट", "Пост", "منشور"), Modifier.weight(1f))
                ProHomeChip(Icons.Rounded.Star, text(language, "Kaynak", "Community", "Communauté", "Comunidad", "社区", "समुदाय", "Сообщество", "المجتمع"), Modifier.weight(1f))
                ProHomeChip(Icons.Rounded.Done, text(language, "Testçi", "Testers", "Testeurs", "Testers", "测试者", "टेस्टर", "Тестеры", "المختبرون"), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProHomeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = rowSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, separatorColor())
    ) {
        Column(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GridDotsIcon(modifier: Modifier = Modifier) {
    ComposeCanvas(modifier = modifier) {
        val gap = size.minDimension / 3.6f
        val radius = size.minDimension / 7.6f
        val startX = (size.width - gap * 2) / 2f
        val startY = (size.height - gap * 2) / 2f
        repeat(3) { row ->
            repeat(3) { col ->
                val cream = (row + col) % 2 == 0
                drawCircle(
                    color = if (cream) Color(0xFFE8DDC8) else Color(0xFFAFC8DD),
                    radius = radius,
                    center = Offset(startX + col * gap, startY + row * gap)
                )
            }
        }
    }
}

@Composable
private fun TopActionPill(
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .size(40.dp)
            .clickable(onClick = onClick),
        color = color,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun ActionRailToggleIcon(
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    ComposeCanvas(modifier = modifier) {
        val path = androidx.compose.ui.graphics.Path().apply {
            if (expanded) {
                moveTo(size.width * 0.30f, size.height * 0.50f)
                lineTo(size.width * 0.70f, size.height * 0.24f)
                lineTo(size.width * 0.70f, size.height * 0.76f)
            } else {
                moveTo(size.width * 0.70f, size.height * 0.50f)
                lineTo(size.width * 0.30f, size.height * 0.24f)
                lineTo(size.width * 0.30f, size.height * 0.76f)
            }
            close()
        }
        drawPath(path, accent)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeQuickMenuSheet(
    language: AppLanguage,
    isPro: Boolean,
    onDismiss: () -> Unit,
    onShareTest: () -> Unit,
    onHowTo: () -> Unit,
    onSettings: () -> Unit,
    onPro: () -> Unit
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
                    .size(width = 46.dp, height = 4.dp)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text(language, "Hızlı işlemler", "Quick actions", "Actions rapides", "Acciones rápidas", "快捷操作", "त्वरित क्रियाएँ", "Быстрые действия", "إجراءات سريعة", de = "Schnellaktionen", ja = "クイック操作", pt = "Ações rápidas", id = "Aksi cepat"),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Text(
                text(language, "Başlığı boş bırakmak için sık kullanılan araçlar burada toplandı.", "Tools are grouped here so the app title stays readable.", "Les outils sont regroupés ici pour garder le titre lisible.", "Las herramientas se agrupan aquí para que el título siga legible.", "工具集中在这里，让应用标题保持清晰。", "ऐप शीर्षक साफ़ रहे इसलिए टूल यहाँ रखे गए हैं।", "Инструменты собраны здесь, чтобы название было читаемым.", "تم جمع الأدوات هنا ليبقى اسم التطبيق واضحاً.", de = "Werkzeuge sind hier gebündelt, damit der App-Name lesbar bleibt.", ja = "アプリ名を読みやすくするため、ツールをここにまとめました。", pt = "As ferramentas ficam aqui para manter o título legível.", id = "Alat dikumpulkan di sini agar judul tetap terbaca."),
                color = secondaryTextColor(),
                lineHeight = 19.sp
            )
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                    title = text(language, "Yeni test paylaş", "Share new test", "Partager un nouveau test", "Compartir nueva prueba", "分享新测试", "नया टेस्ट साझा करें", "Поделиться новым тестом", "مشاركة اختبار جديد", de = "Neuen Test teilen", ja = "新しいテストを共有", pt = "Compartilhar novo teste", id = "Bagikan tes baru"),
                    subtitle = text(language, "Testçi arama metni ve bağlantılarını hazırla.", "Prepare tester request text and links.", "Préparez le texte et les liens pour les testeurs.", "Prepara texto y enlaces para testers.", "准备测试者招募文本和链接。", "टेस्टर अनुरोध टेक्स्ट और लिंक तैयार करें।", "Подготовьте текст и ссылки для тестировщиков.", "حضّر نص طلب المختبرين والروابط.", de = "Text und Links für Tester vorbereiten.", ja = "テスター募集文とリンクを準備します。", pt = "Prepare texto e links para testadores.", id = "Siapkan teks dan tautan permintaan penguji."),
                    onClick = onShareTest
                )
                CanvasDivider()
                SettingsAction(
                    icon = { Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = null) },
                    title = text(language, "Nasıl kullanılır", "How to use", "Utilisation", "Cómo usar", "如何使用", "कैसे उपयोग करें", "Как использовать", "طريقة الاستخدام", de = "Anleitung", ja = "使い方", pt = "Como usar", id = "Cara pakai"),
                    subtitle = text(language, "İlk kurulum ve takip akışını tekrar göster.", "Show setup and tracking guide again.", "Afficher à nouveau le guide de configuration.", "Mostrar de nuevo la guía de uso.", "再次显示设置和跟踪指南。", "सेटअप और ट्रैकिंग गाइड फिर दिखाएँ।", "Показать руководство снова.", "اعرض دليل الإعداد والمتابعة مرة أخرى.", de = "Einrichtung und Tracking erneut anzeigen.", ja = "設定と追跡ガイドを再表示します。", pt = "Mostrar o guia novamente.", id = "Tampilkan panduan lagi."),
                    onClick = onHowTo
                )
                CanvasDivider()
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                    title = text(language, "Ayarlar", "Settings", "Paramètres", "Ajustes", "设置", "सेटिंग्स", "Настройки", "الإعدادات", de = "Einstellungen", ja = "設定", pt = "Configurações", id = "Pengaturan"),
                    subtitle = text(language, "Dil, tema, bildirim ve canlı süre balonu.", "Language, theme, reminders and live usage bubble.", "Langue, thème, rappels et bulle de durée.", "Idioma, tema, recordatorios y burbuja.", "语言、主题、提醒和实时气泡。", "भाषा, थीम, रिमाइंडर और लाइव बबल।", "Язык, тема, напоминания и плавающий таймер.", "اللغة والسمة والتذكيرات وفقاعة الوقت.", de = "Sprache, Design, Erinnerungen und Zeitblase.", ja = "言語、テーマ、通知、ライブバブル。", pt = "Idioma, tema, lembretes e bolha ao vivo.", id = "Bahasa, tema, pengingat, dan gelembung durasi."),
                    onClick = onSettings
                )
                if (isPro) {
                    CanvasDivider()
                    SettingsAction(
                        icon = { Icon(Icons.Rounded.Star, contentDescription = null) },
                        title = text(language, "Pro araçlar", "Pro tools", "Outils Pro", "Herramientas Pro", "Pro 工具", "Pro टूल", "Инструменты Pro", "أدوات Pro", de = "Pro-Werkzeuge", ja = "Proツール", pt = "Ferramentas Pro", id = "Alat Pro"),
                        subtitle = text(language, "Şablonlar, topluluklar ve testçi notları.", "Templates, communities and tester notes.", "Modèles, communautés et notes.", "Plantillas, comunidades y notas.", "模板、社区和测试者备注。", "टेम्पलेट, समुदाय और नोट्स।", "Шаблоны, сообщества и заметки.", "قوالب ومجتمعات وملاحظات.", de = "Vorlagen, Communitys und Notizen.", ja = "テンプレート、コミュニティ、メモ。", pt = "Modelos, comunidades e notas.", id = "Template, komunitas, dan catatan."),
                        onClick = onPro
                    )
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
private fun LoadingListState(language: AppLanguage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = panelColor(strong = true), contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(26.dp),
        border = panelBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text(language, "Uygulamalar yükleniyor", "Loading apps"), fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = separatorColor()
            )
        }
    }
}

@Composable
private fun EmptyHomeState(
    language: AppLanguage,
    usageAccess: Boolean,
    onAdd: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onShareTest: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = canvasColor(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(32.dp),
        border = panelBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Done, contentDescription = null, modifier = Modifier.padding(10.dp).size(24.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Closed Test Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text(language, "Test çalışma alanın hazır", "Your test workspace is ready"), color = secondaryTextColor(), fontSize = 13.sp)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text(language, "İlk 14 günlük testini başlat", "Start your first 14-day test"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text(
                        language,
                        "Telefondaki test uygulamalarını seç, günlük kullanımını takip et ve hiçbir test gününü kaçırma.",
                        "Choose test apps from your phone, track daily usage, and never miss a testing day."
                    ),
                    color = secondaryTextColor(),
                    lineHeight = 20.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OnboardingStep("1", text(language, "Test uygulamalarını seç", "Choose test apps"), text(language, "Telefonundaki launcher uygulamalarından ekle.", "Add apps directly from your launcher list."))
                OnboardingStep("2", text(language, "Kullanım erişimini aç", "Enable Usage Access"), text(language, "Bugün ve toplam kullanım dakikalarını otomatik oku.", "Automatically read today and total usage minutes."))
                OnboardingStep("3", text(language, "14 günlük seriyi takip et", "Track the 14-day streak"), text(language, "Widget ve günlük bildirimlerle süreci düzenli tut.", "Stay consistent with the widget and daily reminders."))
            }

            Surface(
                color = if (usageAccess) Color(0xFF1F6B45).copy(alpha = 0.18f) else rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, if (usageAccess) Color(0xFF2F8A5D).copy(alpha = 0.45f) else separatorColor())
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(if (usageAccess) Icons.Rounded.Done else Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        if (usageAccess) text(language, "Kullanım erişimi hazır", "Usage Access is ready")
                        else text(language, "Kullanım erişimi henüz kapalı", "Usage Access is not enabled yet"),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(text(language, "Test uygulaması ekle", "Add test apps"), fontWeight = FontWeight.Bold)
            }

            if (!usageAccess) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onOpenUsageSettings),
                    color = rowSurfaceColor(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, separatorColor())
                ) {
                    Text(
                        text(language, "Kullanım erişimini ayarla", "Set up Usage Access"),
                        modifier = Modifier.padding(13.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            TextButton(onClick = onShareTest, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(7.dp))
                Text(text(language, "Yeni test uygulaması paylaş", "Share a new test app"))
            }
        }
    }
}

@Composable
private fun OnboardingStep(number: String, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = rowSurfaceColor(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, separatorColor())
        ) {
            Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Text(number, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = secondaryTextColor(), fontSize = 12.sp)
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
@OptIn(ExperimentalMaterial3Api::class)
private fun SwipeableAppCard(
    language: AppLanguage,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onArchive()
                state.reset()
            }
            SwipeToDismissBoxValue.EndToStart -> {
                onDelete()
                state.reset()
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val direction = state.dismissDirection
            val archive = direction == SwipeToDismissBoxValue.StartToEnd
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1F6B45)
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = if (archive) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (archive) Icons.Rounded.Archive else Icons.Rounded.Delete, contentDescription = null)
                        Text(if (archive) text(language, "Arşivle", "Archive") else text(language, "Sil", "Delete"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        content = { content() }
    )
}

@Composable
private fun AppUsageCard(
    item: TrackedApp,
    displayLabel: String,
    icon: Bitmap?,
    playPublisherName: String?,
    language: AppLanguage,
    hasUsageAccess: Boolean,
    todayMinutes: Long?,
    totalMinutes: Long?,
    isTodayLoading: Boolean,
    isTotalLoading: Boolean,
    isMissingToday: Boolean,
    onOpenApp: () -> Unit,
    onClick: () -> Unit
) {
    val day = SeriesCalculator.currentDay(item)
    val usageLine = when {
        !hasUsageAccess -> text(language, "Kullanım izni gerekli", "Usage permission required", "Autorisation d'utilisation requise", "Se requiere permiso de uso", "需要使用情况权限", "Usage अनुमति आवश्यक है", "Требуется доступ к статистике")
        isTodayLoading || todayMinutes == null -> text(language, "Bugün yükleniyor", "Today loading", "Aujourd'hui en chargement", "Hoy cargando", "今天加载中", "आज लोड हो रहा है", "Сегодня загружается", "اليوم قيد التحميل")
        totalMinutes != null -> text(
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
        isTotalLoading || totalMinutes == null -> text(
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
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isMissingToday) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDarkScheme()) 0.18f else 0.42f)
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = if (isMissingToday) 1.dp else 0.dp,
                color = if (isMissingToday) {
                    MaterialTheme.colorScheme.error.copy(alpha = if (isDarkScheme()) 0.28f else 0.16f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(22.dp)
            )
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
                    displayLabel,
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
            color = Color(0xFF1B5E20),
            trackColor = Color(0xFF1B5E20).copy(alpha = if (isDarkScheme()) 0.30f else 0.18f)
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
    val context = LocalContext.current
    val dayRange = 1..20
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val initialDay = (if (target.isNew) 1 else target.initialDay).coerceIn(dayRange.first, dayRange.last)
    val appIcon = remember(target.packageName) {
        runCatching {
            drawableToBitmap(context.packageManager.getApplicationIcon(target.packageName))
        }.getOrNull()
    }
    // Authoritative selection, seeded from initialDay (day 1 for a newly added
    // app). It changes only when the user actually drags the wheel or taps a day
    // — the initial programmatic scroll below must NOT shift it, which previously
    // made new apps default to day 2 instead of 1.
    var selectedDay by remember(target) { mutableIntStateOf(initialDay) }

    LaunchedEffect(target) {
        var dragging = false
        launch {
            listState.interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is DragInteraction.Start -> dragging = true
                    is DragInteraction.Stop, is DragInteraction.Cancel -> dragging = false
                }
            }
        }
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) return@snapshotFlow null
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - center)
            }?.index
        }.collect { index ->
            if (dragging && index != null) {
                selectedDay = (index + 1).coerceIn(dayRange.first, dayRange.last)
            }
        }
    }

    LaunchedEffect(target) {
        listState.scrollToItem((initialDay - 1).coerceIn(0, dayRange.last - 1), scrollOffset = 62)
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.16f else 0.10f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.30f else 0.18f))
                ) {
                    if (appIcon != null) {
                        Image(
                            bitmap = appIcon.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(7.dp)
                                .size(30.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(11.dp).size(22.dp)
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text(language, "Test günü", "Test day"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        target.label,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (target.isNew) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.18f else 0.12f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.34f else 0.22f))
                        ) {
                            Text(
                                text(language, "Yeni", "New", "Nouveau", "Nuevo", "新建", "नया", "Новый", "جديد"),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
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
            }

            Surface(
                color = rowSurfaceColor(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, separatorColor())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text(language, "Bugün bu uygulama testinin kaçıncı günü? 1 ile 20 arasında seç.", "Which test day is this app on today? Choose between 1 and 20."),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.14f else 0.10f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkScheme()) 0.28f else 0.18f)
                            )
                        ) {
                            Text(
                                text(language, "Gün", "Day"),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            "$selectedDay / 20",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

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
                                fontSize = 44.sp,
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
                                                selectedDay = day
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
    overlayAllowed: Boolean,
    liveOverlayEnabled: Boolean,
    showLiveOverlayOption: Boolean,
    isPro: Boolean,
    appBackground: AppBackground,
    customGradient: CustomGradient,
    onBackgroundChange: (AppBackground) -> Unit,
    onCustomGradientChange: (CustomGradient) -> Unit,
    languageMode: LanguageMode,
    onLanguageChange: (LanguageMode) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onReminderHourChange: (Int) -> Unit,
    onDateFormatChange: (DateDisplayFormat) -> Unit,
    onLiveOverlayChange: (Boolean) -> Unit,
    appVersionName: String,
    playUpdateState: PlayUpdateState,
    usageAccess: Boolean,
    debugModeEnabled: Boolean,
    onDisableDebugMode: () -> Unit,
    onOpenDebugMode: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onSendMail: () -> Unit,
    onDonate: () -> Unit,
    supportAdStatus: String?,
    supportAdLoading: Boolean,
    supportAdReady: Boolean,
    onWatchSupportAd: () -> Unit,
    onShareApp: () -> Unit,
    onOpenPolicyPage: () -> Unit,
    onOpenStudioPage: () -> Unit,
    onOpenPro: () -> Unit,
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
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Star, contentDescription = null) },
                    title = if (isPro) "Closed Test Tracker Pro" else text(language, "Pro'ya geç", "Get Pro"),
                    subtitle = if (isPro) {
                        text(language, "Pro aktif. Reklamlar kapalı ve tüm Pro araçları açık.", "Pro is active. Ads are removed and all Pro tools are unlocked.")
                    } else {
                        text(language, "Tek seferlik satın alma; şablonlar, testçi notları ve test kaynakları.", "One-time purchase for templates, tester notes and testing resources.")
                    },
                    onClick = onOpenPro
                )
        }
        }
        if (debugModeEnabled) {
            item {
                SettingsCard {
                    SettingsAction(
                        icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                        title = text(language, "Debug sayfası", "Debug page", "Page debug", "Página debug", "调试页面", "Debug page", "Страница отладки", "صفحة التصحيح", de = "Debug-Seite", ja = "デバッグページ", pt = "Página debug", id = "Halaman debug"),
                        subtitle = text(language, "Süre teşhisleri, izinler ve canlı balon durumunu tekrar aç.", "Reopen usage diagnostics, permissions and live bubble status.", "Rouvrez les diagnostics, autorisations et bulle.", "Reabre diagnósticos, permisos y burbuja.", "重新打开使用诊断、权限和气泡状态。", "usage diagnostics, permissions और bubble status फिर खोलें।", "Открыть диагностику, разрешения и таймер.", "أعد فتح التشخيصات والأذونات وحالة الفقاعة.", de = "Diagnose, Berechtigungen und Zeitblase öffnen.", ja = "使用診断、権限、バブル状態を再表示します。", pt = "Reabrir diagnósticos, permissões e bolha.", id = "Buka ulang diagnostik, izin, dan bubble."),
                        onClick = onOpenDebugMode
                    )
                }
            }
        }
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
                    icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                    title = text(language, "Arka plan", "Background", "Arrière-plan", "Fondo", "背景", "पृष्ठभूमि", "Фон")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip(
                                text(language, "Normal görünüm", "Normal look", "Aspect normal", "Vista normal", "普通外观", "सामान्य रूप", "Обычный вид", "المظهر العادي", de = "Normale Ansicht", ja = "通常表示", pt = "Visual normal", id = "Tampilan normal"),
                                appBackground == AppBackground.SIMPLE
                            ) { onBackgroundChange(AppBackground.SIMPLE) }
                            ThemeChip(
                                (if (!isPro) "🔒 " else "") + text(language, "Pro görünüm", "Pro look", "Aspect Pro", "Vista Pro", "Pro 外观", "Pro रूप", "Pro-вид", "مظهر Pro", de = "Pro-Ansicht", ja = "Pro表示", pt = "Visual Pro", id = "Tampilan Pro"),
                                appBackground == AppBackground.RICH
                            ) { if (isPro) onBackgroundChange(AppBackground.RICH) else onOpenPro() }
                            ThemeChip(
                                (if (!isPro) "🔒 " else "") + text(language, "Özel", "Custom", "Personnalisé", "Personalizado", "自定义", "कस्टम", "Свой"),
                                appBackground == AppBackground.CUSTOM
                            ) { if (isPro) onBackgroundChange(AppBackground.CUSTOM) else onOpenPro() }
                        }
                        Text(
                            text(
                                language,
                                "Normal görünüm herkes için açıktır. Pro görünüm ve özel arka planlar Pro üyelikle açılır.",
                                "Normal look is available to everyone. Pro look and custom backgrounds unlock with Pro.",
                                "L'aspect normal est disponible pour tous. L'aspect Pro et les fonds personnalisés sont réservés à Pro.",
                                "La vista normal está disponible para todos. La vista Pro y los fondos personalizados requieren Pro.",
                                "普通外观所有人都可使用。Pro 外观和自定义背景需 Pro 解锁。",
                                "सामान्य रूप सभी के लिए है। Pro रूप और custom background Pro से खुलते हैं।",
                                "Обычный вид доступен всем. Pro-вид и свои фоны открываются с Pro.",
                                "المظهر العادي متاح للجميع. مظهر Pro والخلفيات المخصصة تتطلب Pro.",
                                de = "Die normale Ansicht ist für alle verfügbar. Pro-Ansicht und eigene Hintergründe werden mit Pro freigeschaltet.",
                                ja = "通常表示は全員が使えます。Pro表示とカスタム背景はProで利用できます。",
                                pt = "O visual normal está disponível para todos. O visual Pro e fundos personalizados exigem Pro.",
                                id = "Tampilan normal tersedia untuk semua. Tampilan Pro dan latar khusus terbuka dengan Pro."
                            ),
                            color = secondaryTextColor()
                        )
                        if (isPro && appBackground == AppBackground.CUSTOM) {
                            val palette = listOf(
                                0xFF6A11CB, 0xFFB24592, 0xFFF7B733, 0xFF2575FC,
                                0xFF11998E, 0xFFEB3349, 0xFF141E30, 0xFFFF8008
                            )
                            Text(text(language, "Başlangıç rengi", "Start color"), color = secondaryTextColor())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                palette.forEach { argb ->
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(argb))
                                            .border(
                                                BorderStroke(
                                                    if (customGradient.startColor == argb) 3.dp else 1.dp,
                                                    if (customGradient.startColor == argb) MaterialTheme.colorScheme.onSurface else separatorColor()
                                                ),
                                                RoundedCornerShape(50)
                                            )
                                            .clickable { onCustomGradientChange(customGradient.copy(startColor = argb)) }
                                    )
                                }
                            }
                            Text(text(language, "Bitiş rengi", "End color"), color = secondaryTextColor())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                palette.forEach { argb ->
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(argb))
                                            .border(
                                                BorderStroke(
                                                    if (customGradient.endColor == argb) 3.dp else 1.dp,
                                                    if (customGradient.endColor == argb) MaterialTheme.colorScheme.onSurface else separatorColor()
                                                ),
                                                RoundedCornerShape(50)
                                            )
                                            .clickable { onCustomGradientChange(customGradient.copy(endColor = argb)) }
                                    )
                                }
                            }
                            Text(text(language, "Gradyen türü", "Gradient type"), color = secondaryTextColor())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip(text(language, "Dikey", "Vertical"), customGradient.type == GradientType.VERTICAL) {
                                    onCustomGradientChange(customGradient.copy(type = GradientType.VERTICAL))
                                }
                                ThemeChip(text(language, "Çapraz", "Diagonal"), customGradient.type == GradientType.DIAGONAL) {
                                    onCustomGradientChange(customGradient.copy(type = GradientType.DIAGONAL))
                                }
                                ThemeChip(text(language, "Yatay", "Horizontal"), customGradient.type == GradientType.HORIZONTAL) {
                                    onCustomGradientChange(customGradient.copy(type = GradientType.HORIZONTAL))
                                }
                                ThemeChip(text(language, "Radyal", "Radial"), customGradient.type == GradientType.RADIAL) {
                                    onCustomGradientChange(customGradient.copy(type = GradientType.RADIAL))
                                }
                            }
                        }
                    }
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
        if (showLiveOverlayOption) {
            item {
                SettingsCard {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onLiveOverlayChange(!liveOverlayEnabled) },
                        color = if (isDarkScheme()) Color.White.copy(alpha = 0.055f) else Color.White.copy(alpha = 0.62f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, separatorColor())
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = panelColor(),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = panelBorder()
                            ) {
                                Box(Modifier.padding(8.dp)) {
                                    Icon(Icons.Rounded.Info, contentDescription = null)
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text(language, "Canlı süre balonu", "Live usage bubble", "Bulle de durée en direct", "Burbuja de uso en vivo", "实时使用气泡", "लाइव उपयोग बबल", "Плавающее время", "فقاعة الوقت المباشر", de = "Live-Zeitblase", ja = "ライブ使用時間バブル", pt = "Bolha de uso ao vivo", id = "Gelembung durasi langsung"),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (liveOverlayEnabled && overlayAllowed) {
                                        text(language, "Açık. Test uygulamasındayken üstte bugünkü dakika görünür.", "On. Today's minutes appear above test apps.", "Activé. Les minutes du jour s'affichent au-dessus des apps de test.", "Activado. Los minutos de hoy aparecen sobre las apps de prueba.", "已开启。测试应用上方会显示今天的分钟数。", "चालू। टेस्ट ऐप्स के ऊपर आज के मिनट दिखेंगे।", "Включено. Минуты за сегодня показываются поверх тестовых приложений.", "مفعّل. تظهر دقائق اليوم فوق تطبيقات الاختبار.", de = "Aktiv. Heutige Minuten erscheinen über Test-Apps.", ja = "オン。テストアプリ上に今日の分数が表示されます。", pt = "Ativado. Os minutos de hoje aparecem sobre os apps de teste.", id = "Aktif. Menit hari ini muncul di atas aplikasi tes.")
                                    } else if (liveOverlayEnabled) {
                                        text(language, "İzin bekleniyor. Dokunup üstte gösterme iznini aç.", "Permission needed. Tap and allow display over other apps.", "Autorisation requise. Touchez pour autoriser l'affichage par-dessus les apps.", "Permiso necesario. Toca y permite mostrar sobre otras apps.", "需要权限。点按并允许在其他应用上显示。", "अनुमति चाहिए। टैप करके other apps के ऊपर दिखाने की अनुमति दें।", "Нужно разрешение. Нажмите и разрешите показ поверх других приложений.", "يلزم الإذن. اضغط واسمح بالظهور فوق التطبيقات الأخرى.", de = "Berechtigung nötig. Tippen und Anzeige über anderen Apps erlauben.", ja = "権限が必要です。他のアプリの上に表示を許可してください。", pt = "Permissão necessária. Toque e permita exibir sobre outros apps.", id = "Butuh izin. Ketuk dan izinkan tampil di atas aplikasi lain.")
                                    } else {
                                        text(language, "Kapalı. Sadece debug denemesi için.", "Off. Debug test only.", "Désactivé. Test debug uniquement.", "Desactivado. Solo prueba debug.", "已关闭。仅用于调试测试。", "बंद। केवल debug टेस्ट के लिए।", "Выключено. Только для debug-теста.", "متوقف. لاختبار debug فقط.", de = "Aus. Nur Debug-Test.", ja = "オフ。デバッグテスト専用です。", pt = "Desativado. Apenas teste debug.", id = "Mati. Hanya tes debug.")
                                    },
                                    color = secondaryTextColor(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = liveOverlayEnabled,
                                onCheckedChange = { onLiveOverlayChange(it) }
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = text(language, "Debug modu", "Debug mode", "Mode debug", "Modo debug", "调试模式", "Debug मोड", "Режим отладки", "وضع التصحيح", de = "Debug-Modus", ja = "デバッグモード", pt = "Modo debug", id = "Mode debug")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text(
                                language,
                                "Açmak için Ayarlar başlığındaki ayarlar ikonuna 4 kez dokun.",
                                "Tap the settings icon in the Settings title 4 times to enable it.",
                                "Touchez 4 fois l'icône des paramètres dans le titre.",
                                "Toca 4 veces el icono de ajustes del título.",
                                "点击设置标题中的设置图标 4 次即可开启。",
                                "Settings शीर्षक के आइकन पर 4 बार टैप करें।",
                                "Нажмите значок настроек в заголовке 4 раза.",
                                "اضغط أيقونة الإعدادات في العنوان 4 مرات.",
                                de = "Tippe 4 Mal auf das Einstellungssymbol im Titel.",
                                ja = "設定タイトルの設定アイコンを4回タップします。",
                                pt = "Toque 4 vezes no ícone de configurações do título.",
                                id = "Ketuk ikon pengaturan di judul 4 kali."
                            ),
                            color = secondaryTextColor()
                        )
                        if (debugModeEnabled) {
                            SettingsAction(
                                icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                                title = text(language, "Debug sayfasını aç", "Open debug page", "Ouvrir la page debug", "Abrir página debug", "打开调试页面", "Debug page खोलें", "Открыть страницу отладки", "فتح صفحة التصحيح", de = "Debug-Seite öffnen", ja = "デバッグページを開く", pt = "Abrir página debug", id = "Buka halaman debug"),
                                subtitle = text(language, "Test uygulamalarının bugün/toplam süreleri ve izin durumları burada görünür.", "Shows test apps, today/total minutes and permission status.", "Affiche les apps, durées et autorisations.", "Muestra apps, minutos y permisos.", "显示应用、分钟和权限状态。", "ऐप, मिनट और permissions दिखाता है।", "Показывает приложения, минуты и разрешения.", "يعرض التطبيقات والدقائق والأذونات.", de = "Zeigt Apps, Minuten und Berechtigungen.", ja = "アプリ、分数、権限を表示します。", pt = "Mostra apps, minutos e permissões.", id = "Menampilkan app, menit, dan izin."),
                                onClick = onOpenDebugMode
                            )
                            CanvasDivider()
                            SettingsAction(
                                icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                                title = text(language, "Debug modunu kapat", "Disable debug mode", "Désactiver le debug", "Desactivar debug", "关闭调试模式", "Debug mode बंद करें", "Отключить режим отладки", "إيقاف وضع التصحيح", de = "Debug-Modus ausschalten", ja = "デバッグモードを無効化", pt = "Desativar modo debug", id = "Matikan mode debug"),
                                subtitle = text(language, "Debug kısayolu ve teşhis sayfası gizlenir.", "Hides the debug shortcut and diagnostics page.", "Masque le raccourci debug.", "Oculta el acceso debug.", "隐藏调试入口。", "Debug shortcut छिपेगा।", "Скрывает отладку.", "يخفي اختصار التصحيح.", de = "Blendet Debug aus.", ja = "デバッグを非表示にします。", pt = "Oculta o debug.", id = "Menyembunyikan debug."),
                                onClick = onDisableDebugMode
                            )
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
                        Button(
                            onClick = onOpenStudioPage,
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
                            Text(
                                text(
                                    language,
                                    "MD Studio uygulamalarını gör",
                                    "See MD Studio apps",
                                    "Voir les apps MD Studio",
                                    "Ver apps de MD Studio",
                                    "查看 MD Studio 应用",
                                    "MD Studio ऐप्स देखें",
                                    "Посмотреть приложения MD Studio",
                                    "شاهد تطبيقات MD Studio",
                                    de = "MD Studio Apps ansehen",
                                    ja = "MD Studio のアプリを見る",
                                    pt = "Ver apps da MD Studio",
                                    id = "Lihat aplikasi MD Studio"
                                )
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsSection(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = text(language, "Kullanım Şartları ve Gizlilik Politikası", "Term of usage & Privacy Policy", "Conditions d'utilisation et politique de confidentialité", "Términos de uso y política de privacidad", "使用条款与隐私政策", "उपयोग की शर्तें और गोपनीयता नीति", "Условия использования и политика конфиденциальности", "شروط الاستخدام وسياسة الخصوصية")
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
                        Text(text(language, "Aç", "Open", "Ouvrir", "Abrir", "打开", "खोलें", "Открыть", "فتح"))
                    }
                }
            }
        }
        item {
            SettingsCard {
                SettingsAction(
                    icon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                    title = text(language, "Uygulamayı arkadaşlarınla paylaş", "Share app with friends", "Partager avec des amis", "Compartir con amigos", "与朋友分享应用", "दोस्तों के साथ ऐप साझा करें", "Поделиться с друзьями", "شارك التطبيق مع الأصدقاء"),
                    subtitle = text(
                        language,
                        "Closed Test Tracker bağlantısını paylaş.",
                        "Share the Closed Test Tracker link.",
                        "Partagez le lien Closed Test Tracker.",
                        "Comparte el enlace de Closed Test Tracker.",
                        "分享 Closed Test Tracker 链接。",
                        "Closed Test Tracker लिंक साझा करें।",
                        "Поделитесь ссылкой Closed Test Tracker.",
                        "شارك رابط Closed Test Tracker."
                    ),
                    onClick = onShareApp
                )
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
        if (!isPro) {
            item {
                SettingsCard {
                    SettingsAction(
                        icon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
                        title = text(language, "Reklam izleyerek destek ol", "Support by watching ads", "Soutenir en regardant des pubs", "Apoyar viendo anuncios", "通过观看广告支持", "विज्ञापन देखकर समर्थन करें", "Поддержать просмотром рекламы", "ادعم عبر مشاهدة الإعلانات"),
                        subtitle = when {
                            supportAdReady -> text(language, "Reklam hazır. İzlemek için dokun.", "Ad is ready. Tap to watch.")
                            supportAdLoading -> text(language, "Reklam hazırlanıyor...", "Preparing ad...")
                            supportAdStatus != null -> supportAdStatus
                            else -> text(language, "İstersen reklam izleyerek uygulamayı destekleyebilirsin.", "You can watch an ad to support the app.")
                        },
                        onClick = onWatchSupportAd
                    )
                }
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
private fun DebugInfoPanel(
    language: AppLanguage,
    appVersionName: String,
    usageAccess: Boolean,
    overlayAllowed: Boolean,
    liveOverlayEnabled: Boolean,
    notificationAllowed: Boolean,
    trackedApps: List<TrackedApp>,
    onDisable: () -> Unit
) {
    val active = trackedApps.count { !it.isArchived && it.completedAtMillis == null }
    val completed = trackedApps.count { it.completedAtMillis != null }
    val archived = trackedApps.count { it.isArchived }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoBullet(
            title = text(language, "Durum", "Status", "Statut", "Estado", "状态", "स्थिति", "Статус", "الحالة", de = "Status", ja = "状態", pt = "Estado", id = "Status"),
            body = buildString {
                append("Closed Test Tracker $appVersionName\n")
                append("${Build.MANUFACTURER} ${Build.MODEL} - Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
                append("Usage Access: ${if (usageAccess) "OK" else "OFF"}\n")
                append("Overlay permission: ${if (overlayAllowed) "OK" else "OFF"}\n")
                append("Live bubble preference: ${if (liveOverlayEnabled) "ON" else "OFF"}\n")
                append("Notifications: ${if (notificationAllowed) "OK" else "OFF"}")
            }
        )
        InfoBullet(
            title = text(language, "Uygulama kayıtları", "Tracked apps", "Apps suivies", "Apps seguidas", "跟踪应用", "ट्रैक ऐप्स", "Отслеживаемые приложения", "التطبيقات المتابعة", de = "Verfolgte Apps", ja = "追跡アプリ", pt = "Apps acompanhados", id = "Aplikasi dilacak"),
            body = text(
                language,
                "Toplam: ${trackedApps.size}, aktif: $active, tamamlanan: $completed, arşiv: $archived. Liste ve ayrıntı süreleri farklı görünürse bu ekranın bilgileriyle hata bildir.",
                "Total: ${trackedApps.size}, active: $active, completed: $completed, archived: $archived. If list and detail times differ, report the issue with this screen's details.",
                "Total : ${trackedApps.size}, actifs : $active, terminés : $completed, archive : $archived. Si les durées diffèrent, signalez avec ces informations.",
                "Total: ${trackedApps.size}, activas: $active, completadas: $completed, archivo: $archived. Si los tiempos difieren, informa con estos datos.",
                "总计：${trackedApps.size}，活跃：$active，完成：$completed，归档：$archived。如列表与详情时间不同，请附上此信息反馈。",
                "कुल: ${trackedApps.size}, सक्रिय: $active, पूर्ण: $completed, आर्काइव: $archived. समय अलग दिखे तो इस जानकारी के साथ रिपोर्ट करें।",
                "Всего: ${trackedApps.size}, активных: $active, завершено: $completed, архив: $archived. Если время отличается, отправьте эти данные.",
                "الإجمالي: ${trackedApps.size}، نشط: $active، مكتمل: $completed، أرشيف: $archived. إذا اختلفت المدة أرسل هذه المعلومات.",
                de = "Gesamt: ${trackedApps.size}, aktiv: $active, abgeschlossen: $completed, Archiv: $archived. Bei abweichenden Zeiten diese Daten mitsenden.",
                ja = "合計: ${trackedApps.size}、有効: $active、完了: $completed、アーカイブ: $archived。時間が違う場合はこの情報を送ってください。",
                pt = "Total: ${trackedApps.size}, ativos: $active, concluídos: $completed, arquivo: $archived. Se os tempos divergirem, envie estes dados.",
                id = "Total: ${trackedApps.size}, aktif: $active, selesai: $completed, arsip: $archived. Jika durasi berbeda, laporkan dengan data ini."
            )
        )
        InfoBullet(
            title = text(language, "Hata bildirimi", "Bug report", "Rapport de bug", "Informe de error", "错误报告", "बग रिपोर्ट", "Отчет об ошибке", "بلاغ خطأ", de = "Fehlerbericht", ja = "不具合報告", pt = "Relato de erro", id = "Laporan bug"),
            body = text(
                language,
                "Bir hata görürsen telefon modeli, Android sürümü, uygulama adı ve ekran görüntüsü ile $SUPPORT_MAIL adresine gönder. Debug modunu kapatmak için aşağıdaki düğmeyi kullan.",
                "If you see an issue, send phone model, Android version, app name and a screenshot to $SUPPORT_MAIL. Use the button below to disable debug mode.",
                "En cas de problème, envoyez modèle, version Android, nom de l'app et capture à $SUPPORT_MAIL. Utilisez le bouton ci-dessous pour désactiver.",
                "Si ves un error, envía modelo, versión Android, app y captura a $SUPPORT_MAIL. Usa el botón para desactivar el modo debug.",
                "如遇问题，请将手机型号、Android 版本、应用名和截图发送至 $SUPPORT_MAIL。可用下方按钮关闭调试模式。",
                "समस्या दिखे तो phone model, Android version, app name और screenshot $SUPPORT_MAIL पर भेजें। नीचे बटन से debug mode बंद करें।",
                "При ошибке отправьте модель, версию Android, имя приложения и скриншот на $SUPPORT_MAIL. Кнопка ниже отключает режим.",
                "إذا ظهرت مشكلة أرسل طراز الهاتف وإصدار Android واسم التطبيق ولقطة شاشة إلى $SUPPORT_MAIL. استخدم الزر أدناه لإيقاف الوضع.",
                de = "Bei Fehlern Modell, Android-Version, App-Name und Screenshot an $SUPPORT_MAIL senden. Unten kann der Debug-Modus deaktiviert werden.",
                ja = "問題があれば端末モデル、Androidバージョン、アプリ名、スクリーンショットを $SUPPORT_MAIL に送ってください。下のボタンで無効化できます。",
                pt = "Se houver erro, envie modelo, versão Android, nome do app e captura para $SUPPORT_MAIL. Use o botão abaixo para desativar.",
                id = "Jika ada masalah, kirim model, versi Android, nama app, dan screenshot ke $SUPPORT_MAIL. Gunakan tombol di bawah untuk mematikan."
            )
        )
        Button(
            onClick = onDisable,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = rowSurfaceColor(),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = panelBorder()
        ) {
            Text(text(language, "Debug modunu kapat", "Disable debug mode", "Désactiver le debug", "Desactivar debug", "关闭调试模式", "Debug mode बंद करें", "Отключить режим отладки", "إيقاف وضع التصحيح", de = "Debug-Modus ausschalten", ja = "デバッグモードを無効化", pt = "Desativar modo debug", id = "Matikan mode debug"), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DebugPage(
    modifier: Modifier,
    language: AppLanguage,
    appVersionName: String,
    usageAccess: Boolean,
    overlayAllowed: Boolean,
    liveOverlayEnabled: Boolean,
    notificationAllowed: Boolean,
    trackedApps: List<TrackedApp>,
    todayUsageMap: Map<String, Long>,
    totalUsageMap: Map<String, Long>,
    todayLoading: Boolean,
    totalLoading: Boolean,
    onRefresh: () -> Unit,
    onDisable: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .background(canvasColor())
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsCard {
                DebugInfoPanel(
                    language = language,
                    appVersionName = appVersionName,
                    usageAccess = usageAccess,
                    overlayAllowed = overlayAllowed,
                    liveOverlayEnabled = liveOverlayEnabled,
                    notificationAllowed = notificationAllowed,
                    trackedApps = trackedApps,
                    onDisable = onDisable
                )
            }
        }
        item {
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text(language, "Test uygulama süreleri", "Test app usage", "Utilisation des apps", "Uso de apps", "测试应用使用", "टेस्ट ऐप उपयोग", "Использование приложений", "استخدام تطبيقات الاختبار", de = "Test-App-Nutzung", ja = "テストアプリ使用状況", pt = "Uso dos apps", id = "Pemakaian app tes"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text(language, "Liste ve ayrıntı farklı görünürse bu satırları kontrol et.", "If list and detail differ, check these rows.", "Si la liste et le détail diffèrent, vérifiez ici.", "Si lista y detalle difieren, revisa aquí.", "如列表和详情不同，请检查这些行。", "List/detail अलग हों तो ये rows देखें।", "Если список и детали отличаются, проверьте строки.", "إذا اختلفت القائمة والتفاصيل فتحقق من هذه الصفوف."),
                            color = secondaryTextColor(),
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = rowSurfaceColor(), contentColor = MaterialTheme.colorScheme.onSurface),
                        border = panelBorder()
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        if (trackedApps.isEmpty()) {
            item {
                SettingsCard {
                    Text(text(language, "Henüz takip edilen uygulama yok.", "No tracked apps yet."), color = secondaryTextColor())
                }
            }
        } else {
            items(trackedApps, key = { it.packageName }) { app ->
                val today = todayUsageMap[app.packageName]
                val total = totalUsageMap[app.packageName]
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(app.appLabel, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(app.packageName, color = secondaryTextColor(), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryStatChip(
                                text(language, "Bugün", "Today"),
                                if (todayLoading && today == null) text(language, "Yükleniyor", "Loading") else "${today ?: 0} ${minuteLabel(language)}",
                                Modifier.weight(1f)
                            )
                            SummaryStatChip(
                                text(language, "Toplam", "Total"),
                                if (totalLoading && total == null) text(language, "Yükleniyor", "Loading") else "${total ?: 0} ${minuteLabel(language)}",
                                Modifier.weight(1f)
                            )
                        }
                        val flags = listOfNotNull(
                            if (app.isArchived) text(language, "Arşiv", "Archive") else null,
                            if (app.completedAtMillis != null) text(language, "Tamamlandı", "Done") else null
                        ).ifEmpty { listOf(text(language, "Aktif", "Active")) }
                        Text(flags.joinToString(" • "), color = secondaryTextColor(), fontSize = 12.sp)
                    }
                }
            }
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
private fun AppGalleryScreen(
    modifier: Modifier = Modifier,
    language: AppLanguage,
    screenshotMode: Boolean,
    appDisplayName: String,
    appLogoBitmap: Bitmap?,
    tracked: List<TrackedApp>,
    installedAppsByPackage: Map<String, InstalledApp>,
    onSelect: (String) -> Unit
) {
    val packageManager = LocalContext.current.packageManager
    val visibleTracked = tracked.sortedWith(
        compareByDescending<TrackedApp> { !it.isArchived }
            .thenByDescending { it.completedAtMillis == null }
            .thenByDescending { SeriesCalculator.currentDay(it) }
            .thenBy { it.appLabel.lowercase() }
    )
    val itemCount = visibleTracked.size.coerceAtLeast(1)
    val activeCount = visibleTracked.count { !it.isArchived && it.completedAtMillis == null }
    val completedCount = visibleTracked.count { it.completedAtMillis != null }
    val activeDays = visibleTracked
        .filter { !it.isArchived && it.completedAtMillis == null }
        .map { SeriesCalculator.currentDay(it) }
    val summaryText = gallerySummaryText(
        language = language,
        activeCount = activeCount,
        completedCount = completedCount,
        minDay = activeDays.minOrNull(),
        maxDay = activeDays.maxOrNull()
    )
    val columns = when {
        screenshotMode && itemCount > 42 -> 8
        screenshotMode && itemCount > 30 -> 7
        screenshotMode && itemCount > 20 -> 6
        else -> 5
    }

    if (visibleTracked.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text(language, "Henüz uygulama eklenmedi", "No apps added yet"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text(language, "Takip edilen uygulamalar burada ikon olarak görünür.", "Tracked apps appear here as icons."), color = secondaryTextColor())
        }
    } else {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            canvasColor(),
                            panelColor(),
                            canvasColor()
                        )
                    )
                )
        ) {
            val horizontalPadding = if (screenshotMode) 12.dp else 18.dp
            val verticalPadding = if (screenshotMode) 12.dp else 24.dp
            val horizontalGap = if (screenshotMode) 10.dp else 22.dp
            val verticalGap = if (screenshotMode) 8.dp else 24.dp
            val headerHeight = if (screenshotMode) 76.dp else 84.dp
            val footerHeight = if (screenshotMode) 74.dp else 92.dp
            val rows = ((visibleTracked.size + columns - 1) / columns).coerceAtLeast(1)
            val availableWidth = maxWidth - horizontalPadding * 2 - horizontalGap * (columns - 1)
            val availableHeight = maxHeight - headerHeight - footerHeight - verticalPadding * 2 - verticalGap * (rows - 1)
            val maxCellWidth = availableWidth / columns
            val maxCellHeight = availableHeight / rows
            val iconSize = if (screenshotMode) {
                minOf(maxCellWidth * 0.82f, maxCellHeight * 0.76f, 68.dp).coerceAtLeast(34.dp)
            } else {
                68.dp
            }
            val badgeFontSize = when {
                iconSize < 42.dp -> 7.sp
                iconSize < 52.dp -> 8.sp
                else -> 9.sp
            }
            val badgeHorizontalPadding = when {
                iconSize < 42.dp -> 4.dp
                iconSize < 52.dp -> 5.dp
                else -> 6.dp
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .padding(horizontal = if (screenshotMode) 18.dp else 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (appLogoBitmap != null) {
                    Image(
                        bitmap = appLogoBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(if (screenshotMode) 30.dp else 34.dp)
                    )
                } else {
                    GridDotsIcon(Modifier.size(if (screenshotMode) 28.dp else 30.dp))
                }
                Text(
                    appDisplayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = if (screenshotMode) 22.sp else 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerHeight),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = verticalPadding,
                    bottom = footerHeight + verticalPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                verticalArrangement = Arrangement.spacedBy(verticalGap),
                userScrollEnabled = !screenshotMode
            ) {
                items(
                    items = visibleTracked,
                    key = { it.packageName }
                ) { item ->
                    val day = SeriesCalculator.currentDay(item)
                    val installedApp = installedAppsByPackage[item.packageName]
                    val iconBitmap = installedApp?.icon ?: remember(item.packageName, installedApp) {
                        appIconBitmap(packageManager, item.packageName)
                    }
                    val dayLabel = when {
                        item.isArchived -> text(language, "Arşiv", "Archive")
                        item.completedAtMillis != null -> text(language, "Tamam", "Done", "Terminé", "Listo", "完成", "पूर्ण", "Готово", "تم")
                        else -> "$day/14"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.92f)
                            .clickable(enabled = !screenshotMode) { onSelect(item.packageName) }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(iconSize)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.22f),
                                    spotColor = Color.Black.copy(alpha = 0.16f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF111111)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (iconBitmap != null) {
                                Image(
                                    bitmap = iconBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                GridPlaceholderIcon(Modifier.fillMaxSize())
                            }
                        }
                        Surface(
                            color = Color(0xFFFFA726),
                            contentColor = Color.Black,
                            shape = RoundedCornerShape(999.dp),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .graphicsLayer {
                                    translationX = 8.dp.toPx()
                                    translationY = 2.dp.toPx()
                                }
                        ) {
                            Text(
                                text = dayLabel,
                                modifier = Modifier.padding(horizontal = badgeHorizontalPadding, vertical = 3.dp),
                                fontWeight = FontWeight.Black,
                                fontSize = badgeFontSize
                            )
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = if (screenshotMode) 16.dp else 18.dp, vertical = if (screenshotMode) 12.dp else 16.dp),
                color = Color(0xFF111111).copy(alpha = if (screenshotMode) 0.90f else 0.86f),
                contentColor = Color.White,
                shape = RoundedCornerShape(if (screenshotMode) 22.dp else 26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                shadowElevation = if (screenshotMode) 0.dp else 8.dp
            ) {
                Text(
                    text = summaryText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (screenshotMode) 12.sp else 13.sp,
                    lineHeight = if (screenshotMode) 15.sp else 17.sp,
                    color = Color.White.copy(alpha = 0.94f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScreenshotSharePrompt(
    modifier: Modifier = Modifier,
    language: AppLanguage,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF111111).copy(alpha = 0.96f),
        contentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        shadowElevation = 18.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text(language, "Ekran görüntüsü alındı", "Screenshot captured", "Capture d'écran prise", "Captura hecha", "已截屏", "स्क्रीनशॉट लिया गया", "Скриншот сделан", "تم التقاط لقطة الشاشة", de = "Screenshot aufgenommen", ja = "スクリーンショットを撮影しました", pt = "Captura feita", id = "Screenshot diambil"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text(language, "Bu özeti paylaşmak ister misin?", "Share this summary?", "Partager ce résumé ?", "¿Compartir este resumen?", "分享此摘要？", "यह सारांश साझा करें?", "Поделиться сводкой?", "هل تريد مشاركة هذا الملخص؟", de = "Diese Übersicht teilen?", ja = "この概要を共有しますか？", pt = "Compartilhar este resumo?", id = "Bagikan ringkasan ini?"),
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onDismiss) {
                Text(
                    text(language, "Kapat", "Close", "Fermer", "Cerrar", "关闭", "बंद करें", "Закрыть", "إغلاق", de = "Schließen", ja = "閉じる", pt = "Fechar", id = "Tutup"),
                    color = Color.White.copy(alpha = 0.76f)
                )
            }
            Button(
                onClick = onShare,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(text(language, "Paylaş", "Share", "Partager", "Compartir", "分享", "साझा करें", "Поделиться", "مشاركة", de = "Teilen", ja = "共有", pt = "Compartilhar", id = "Bagikan"))
            }
        }
    }
}

private fun gallerySummaryText(
    language: AppLanguage,
    activeCount: Int,
    completedCount: Int,
    minDay: Int?,
    maxDay: Int?
): String {
    if (activeCount == 0) {
        return text(
            language,
            "Aktif test uygulaması yok. $completedCount uygulama tamamlandı.",
            "No active test apps. $completedCount apps completed.",
            "Aucune app de test active. $completedCount apps terminées.",
            "No hay apps de prueba activas. $completedCount apps completadas.",
            "没有活跃测试应用。$completedCount 个应用已完成。",
            "कोई सक्रिय टेस्ट ऐप नहीं। $completedCount ऐप पूरे हुए।",
            "Нет активных тестовых приложений. Завершено: $completedCount.",
            "لا توجد تطبيقات اختبار نشطة. اكتمل $completedCount تطبيق.",
            de = "Keine aktiven Test-Apps. $completedCount Apps abgeschlossen.",
            ja = "アクティブなテストアプリはありません。$completedCount 個のアプリが完了しました。",
            pt = "Nenhum app de teste ativo. $completedCount apps concluídos.",
            id = "Tidak ada aplikasi tes aktif. $completedCount aplikasi selesai."
        )
    }
    val range = if (minDay != null && maxDay != null && minDay != maxDay) {
        "$minDay-$maxDay"
    } else {
        "${minDay ?: maxDay ?: 1}"
    }
    return text(
        language,
        "$activeCount uygulama test edilmekte. Bazıları $range. gün aralığında, $completedCount uygulama tamamlandı.",
        "$activeCount apps are being tested. Some are around day $range, $completedCount apps completed.",
        "$activeCount apps sont en test. Certaines sont autour des jours $range, $completedCount apps terminées.",
        "$activeCount apps están en prueba. Algunas están entre los días $range, $completedCount apps completadas.",
        "$activeCount 个应用正在测试。部分处于第 $range 天，$completedCount 个已完成。",
        "$activeCount ऐप टेस्ट में हैं। कुछ दिन $range के आसपास हैं, $completedCount ऐप पूरे हुए।",
        "Тестируется приложений: $activeCount. Некоторые на днях $range, завершено: $completedCount.",
        "يتم اختبار $activeCount تطبيق. بعضها حول اليوم $range، واكتمل $completedCount تطبيق.",
        de = "$activeCount Apps werden getestet. Einige liegen bei Tag $range, $completedCount Apps sind abgeschlossen.",
        ja = "$activeCount 個のアプリをテスト中です。一部は $range 日目付近、$completedCount 個が完了しました。",
        pt = "$activeCount apps estão em teste. Alguns estão por volta do dia $range, $completedCount apps concluídos.",
        id = "$activeCount aplikasi sedang diuji. Beberapa sekitar hari $range, $completedCount aplikasi selesai."
    )
}

@Composable
private fun GridPlaceholderIcon(modifier: Modifier = Modifier) {
    ComposeCanvas(modifier = modifier) {
        drawRoundRect(
            color = Color(0xFF111111),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx())
        )
        val road = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.12f, size.height)
            cubicTo(size.width * 0.30f, size.height * 0.72f, size.width * 0.42f, size.height * 0.58f, size.width * 0.48f, size.height * 0.36f)
            cubicTo(size.width * 0.54f, size.height * 0.18f, size.width * 0.70f, size.height * 0.12f, size.width * 0.84f, size.height * 0.08f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(road, Color.White)
        val lineColor = Color(0xFF111111)
        drawLine(lineColor, Offset(size.width * 0.48f, size.height * 0.92f), Offset(size.width * 0.52f, size.height * 0.78f), strokeWidth = 2.2.dp.toPx())
        drawLine(lineColor, Offset(size.width * 0.54f, size.height * 0.68f), Offset(size.width * 0.57f, size.height * 0.56f), strokeWidth = 2.dp.toPx())
        drawLine(lineColor, Offset(size.width * 0.60f, size.height * 0.46f), Offset(size.width * 0.64f, size.height * 0.36f), strokeWidth = 1.8.dp.toPx())
        drawCircle(Color.White, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.73f, size.height * 0.20f))
        drawCircle(Color(0xFF111111), radius = size.minDimension * 0.045f, center = Offset(size.width * 0.73f, size.height * 0.20f))
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = panelBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier
                .background(glassPanelBrush())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LanguageMode.entries.toList()) { item ->
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

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is android.graphics.drawable.BitmapDrawable) {
        drawable.bitmap?.let { return it }
    }
    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
private fun TestAdAreaCard(language: AppLanguage) {
    var bannerState by remember {
        mutableStateOf(
            text(language, "Reklam yükleniyor", "Loading ad", "Chargement de la publicité", "Cargando anuncio", "正在加载广告", "विज्ञापन लोड हो रहा है", "Загрузка рекламы", "يتم تحميل الإعلان", de = "Anzeige wird geladen", ja = "広告を読み込み中", pt = "Carregando anúncio", id = "Memuat iklan")
        )
    }
    var bannerLoaded by remember { mutableStateOf(false) }
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!bannerLoaded) {
                    Text(
                        bannerState,
                        color = secondaryTextColor(),
                        fontSize = 11.sp
                    )
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = if (isDebuggableApp(ctx)) BANNER_TEST_AD_UNIT_ID else BANNER_PROD_AD_UNIT_ID
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    bannerLoaded = true
                                    Log.d("ClosedTestAds", "Banner loaded")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    bannerLoaded = false
                                    bannerState = adLoadMessage(language, error)
                                    Log.w("ClosedTestAds", "Banner failed: code=${error.code}, domain=${error.domain}, message=${error.message}")
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HowToScreen(
    modifier: Modifier = Modifier,
    language: AppLanguage,
    onContinue: () -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text(language, "Uygulamayı ilk açtığınızda", "When you open the app for the first time", "Lorsque vous ouvrez l'application pour la première fois", "Cuando abras la app por primera vez", "首次打开应用时", "जब आप ऐप पहली बार खोलें", "При первом запуске приложения", "عند فتح التطبيق لأول مرة"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text(language, "Kullanım süresi iznini telefon ayarlarından açın. Bu izin, günlük sürelerin takip edilmesi için gereklidir.", "Enable Usage Access from phone settings. This permission is required to track daily usage times.", "Activez l'accès d'utilisation dans les paramètres du téléphone. Cette autorisation est requise pour suivre les durées quotidiennes.", "Activa el acceso de uso desde la configuración del teléfono. Este permiso es necesario para seguir los tiempos diarios.", "请在手机设置中开启使用情况访问权限。此权限用于跟踪每日使用时长。", "फोन सेटिंग्स से Usage Access अनुमति चालू करें। यह दैनिक समय ट्रैकिंग के लिए आवश्यक है।", "Включите доступ к статистике в настройках телефона. Это нужно для отслеживания ежедневного времени.", "فعّل إذن الوصول للاستخدام من إعدادات الهاتف. هذا الإذن مطلوب لتتبع الوقت اليومي."),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }
            item {
                Image(
                    painter = painterResource(id = R.drawable.guide_step_1),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
            item {
                Text(
                    text(language, "Sonrasında", "After that", "Ensuite", "Después", "然后", "इसके बाद", "Далее", "بعد ذلك"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                Text(
                    text(language, "Yukarıdaki + ikonu ile uygulama ekleyin. Teste daha önce başladıysanız gün seçerek mevcut süreci devam ettirebilirsiniz.", "Add apps using the + icon at the top. If you already started testing before, set the current day and continue.", "Ajoutez des apps avec l'icône + en haut. Si vous avez déjà commencé le test, définissez le jour actuel et continuez.", "Agrega apps con el ícono + arriba. Si ya empezaste la prueba antes, define el día actual y continúa.", "使用顶部 + 图标添加应用。如果之前已开始测试，可设置当前天数继续。", "ऊपर + आइकन से ऐप जोड़ें। यदि पहले से टेस्ट शुरू है तो वर्तमान दिन सेट करके जारी रखें।", "Добавьте приложения через значок + сверху. Если тест уже начат, задайте текущий день и продолжайте.", "أضف التطبيقات من أيقونة + بالأعلى. إذا بدأت الاختبار مسبقاً يمكنك تحديد اليوم الحالي والمتابعة."),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }
            item {
                Image(
                    painter = painterResource(id = R.drawable.guide_step_2),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
            item {
                Image(
                    painter = painterResource(id = R.drawable.guide_step_3),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
            item {
                Text(
                    text(language, "Gün sayısını seçtikten sonra", "After selecting the day", "Après avoir sélectionné le jour", "Después de seleccionar el día", "选择天数后", "दिन चुनने के बाद", "После выбора дня", "بعد تحديد اليوم"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                Text(
                    text(language, "Takip başlar. Uygulama özetinde ve listede seri gününü ve kullanım dakikalarını görebilirsiniz. İkona dokunarak uygulamayı direkt açabilirsiniz.", "Tracking starts. You can see streak day and usage minutes in both summary and list. Tap the icon to open the app directly.", "Le suivi commence. Vous voyez le jour de série et les minutes d'utilisation dans le résumé et la liste. Touchez l'icône pour ouvrir l'app.", "Empieza el seguimiento. Puedes ver el día de racha y los minutos en resumen y lista. Toca el ícono para abrir la app.", "开始跟踪。可在摘要和列表看到连续天数与使用分钟。点击图标可直接打开应用。", "ट्रैकिंग शुरू होती है। सारांश और सूची में स्ट्रीक दिन व मिनट दिखेंगे। आइकन टैप कर ऐप सीधे खोलें।", "Отслеживание начнется. День серии и минуты видны в сводке и списке. Нажмите иконку, чтобы открыть приложение.", "يبدأ التتبع. سترى يوم السلسلة ودقائق الاستخدام في الملخص والقائمة. اضغط الأيقونة لفتح التطبيق مباشرة."),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }
            item {
                Image(
                    painter = painterResource(id = R.drawable.guide_step_4),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            color = canvasColor().copy(alpha = 0.94f),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, separatorColor()),
            shadowElevation = 12.dp
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text(
                        language,
                        "Uygulamaya geç",
                        "Continue to app",
                        "Continuer vers l'app",
                        "Ir a la app",
                        "进入应用",
                        "ऐप पर जाएँ",
                        "Перейти к приложению",
                        "الانتقال إلى التطبيق",
                        de = "Zur App",
                        ja = "アプリへ進む",
                        pt = "Ir para o app",
                        id = "Lanjut ke aplikasi"
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
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
        LanguageMode.DE -> languageFlag(AppLanguage.DE)
        LanguageMode.JA -> languageFlag(AppLanguage.JA)
        LanguageMode.PT -> languageFlag(AppLanguage.PT)
        LanguageMode.ID -> languageFlag(AppLanguage.ID)
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip("${languageFlag(AppLanguage.DE)} Deutsch", language == AppLanguage.DE) { onLanguageChange(AppLanguage.DE) }
                                ThemeChip("${languageFlag(AppLanguage.JA)} 日本語", language == AppLanguage.JA) { onLanguageChange(AppLanguage.JA) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip("${languageFlag(AppLanguage.PT)} Português", language == AppLanguage.PT) { onLanguageChange(AppLanguage.PT) }
                                ThemeChip("${languageFlag(AppLanguage.ID)} Indonesia", language == AppLanguage.ID) { onLanguageChange(AppLanguage.ID) }
                            }
                        }
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
                                Text(
                                    text = text(language, "Uygulama seçmek için dokun", "Tap to choose"),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
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
                UsageReader.usageMinutesByDayMapFromEventsForDisplay(
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

private fun whatsNewText(language: AppLanguage): String {
    return text(
        language,
        "Canlı süre balonu ayarlarda kalıcı hale getirildi.\nYeni sürümde değişiklikleri ilk açılışta gösterme eklendi.\nDebug modu eklendi: Ayarlar başlığındaki ayarlar ikonuna 4 kez dokunarak açabilirsin.\nUygulama adı düzeltildi ve kullanım süresi teşhisi iyileştirildi.",
        "The live usage bubble setting is now always available in Settings.\nA first-open what's new dialog was added for new versions.\nDebug mode was added: tap the settings icon in the Settings title 4 times to enable it.\nThe app name was fixed and usage-time diagnostics were improved.",
        "Le réglage de la bulle d'utilisation en direct est maintenant toujours disponible.\nUne fenêtre Nouveautés s'affiche après une mise à jour.\nMode debug ajouté : touchez 4 fois l'icône des paramètres dans le titre.\nLe nom de l'app et le diagnostic du temps d'utilisation ont été améliorés.",
        "El ajuste de burbuja de uso en vivo ahora siempre está disponible.\nSe añadió una ventana de novedades al abrir una nueva versión.\nModo debug añadido: toca 4 veces el icono de ajustes del título.\nSe corrigió el nombre de la app y se mejoró el diagnóstico de uso.",
        "实时使用气泡设置现在始终显示在设置中。\n新增版本首次打开时的更新说明窗口。\n新增调试模式：点击设置标题中的设置图标 4 次即可开启。\n修复应用名称并改进使用时长诊断。",
        "Live usage bubble सेटिंग अब Settings में हमेशा उपलब्ध है।\nनए संस्करण पर पहली बार खुलने पर What's new दिखेगा।\nDebug mode जोड़ा गया: Settings शीर्षक के icon पर 4 बार टैप करें।\nऐप नाम और usage-time diagnostics सुधारे गए।",
        "Настройка плавающего таймера теперь всегда доступна в настройках.\nДобавлено окно изменений при первом запуске новой версии.\nДобавлен режим отладки: нажмите значок настроек в заголовке 4 раза.\nИсправлено имя приложения и улучшена диагностика времени.",
        "أصبح خيار فقاعة الوقت المباشر متاحاً دائماً في الإعدادات.\nتمت إضافة نافذة تعرض الجديد عند فتح إصدار جديد لأول مرة.\nتمت إضافة وضع التصحيح: اضغط أيقونة الإعدادات في العنوان 4 مرات.\nتم إصلاح اسم التطبيق وتحسين تشخيص مدة الاستخدام.",
        de = "Die Live-Zeitblase ist nun dauerhaft in den Einstellungen verfügbar.\nBeim ersten Start einer neuen Version werden Neuerungen angezeigt.\nDebug-Modus hinzugefügt: Tippe 4 Mal auf das Einstellungssymbol im Titel.\nApp-Name und Nutzungszeit-Diagnose wurden verbessert.",
        ja = "ライブ使用時間バブル設定が常に設定画面に表示されます。\n新バージョン初回起動時に変更内容を表示します。\nデバッグモードを追加しました: 設定タイトルのアイコンを4回タップします。\nアプリ名と使用時間診断を改善しました。",
        pt = "A bolha de uso ao vivo agora fica sempre disponível nas configurações.\nFoi adicionada uma janela de novidades ao abrir uma nova versão.\nModo debug adicionado: toque 4 vezes no ícone de configurações do título.\nNome do app e diagnóstico de uso foram melhorados.",
        id = "Pengaturan gelembung durasi langsung kini selalu tersedia.\nDialog yang baru ditambahkan untuk versi baru.\nMode debug ditambahkan: ketuk ikon pengaturan di judul 4 kali.\nNama aplikasi dan diagnostik durasi pemakaian diperbaiki."
    )
}

private fun minuteLabel(language: AppLanguage): String {
    return text(language, "dk", "min", "min", "min", "分钟", "मिनट", "мин", "دقيقة")
}












