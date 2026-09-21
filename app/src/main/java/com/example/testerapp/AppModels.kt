package com.mdstudio.closedtesttracker

import android.graphics.Bitmap

/**
 * Shared UI model types for the app.
 *
 * These public enums and data classes were extracted from MainActivity.kt to
 * keep the model layer separate from the (large) UI/Activity code. Behaviour is
 * unchanged: they live in the same package they did before.
 */

/** Ordering options for the installed-app picker. */
enum class SortMode { NAME, NEWEST, OLDEST }

/** Supported in-app UI languages (see [text] for the string lookup). */
enum class AppLanguage { TR, EN, FR, ES, ZH, HI, RU, AR, DE, JA, PT, ID }

/** Visual colour themes. */
enum class AppTheme { FRESH, OCEAN, SUNSET }

/** Light/dark mode preference. */
enum class AppThemeMode { SYSTEM, LIGHT, DARK }

/** Top-level navigation destinations. */
enum class AppScreen { HOME, SETTINGS, DETAIL, HOWTO, GALLERY, DEBUG }

/** Filters applied to the tracked-app list on the home screen. */
enum class HomeFilter { ACTIVE, COMPLETED, ARCHIVED, ALL }

/**
 * Background appearance style.
 * - [SIMPLE]  : the default fluid backdrop (free).
 * - [RICH]    : a bold purple→gold gradient (Pro).
 * - [CUSTOM]  : a user-defined gradient (Pro) — see [CustomGradient].
 */
enum class AppBackground { SIMPLE, RICH, CUSTOM }

/** Gradient direction for the RICH / CUSTOM backgrounds. */
enum class GradientType { VERTICAL, DIAGONAL, HORIZONTAL, RADIAL }

/**
 * User-defined gradient for [AppBackground.CUSTOM]. Colors are stored as ARGB
 * longs (0xAARRGGBB) so they can be persisted directly in SharedPreferences and
 * turned into a Compose Color via `Color(startColor)`.
 */
data class CustomGradient(
    val startColor: Long = 0xFF6A11CB, // purple
    val endColor: Long = 0xFFF7B733,   // gold
    val type: GradientType = GradientType.DIAGONAL
)

/** An app installed on the device, shown in the "add app" picker. */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)
