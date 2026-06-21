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
enum class AppScreen { HOME, SETTINGS, DETAIL, HOWTO, GALLERY }

/** Filters applied to the tracked-app list on the home screen. */
enum class HomeFilter { ACTIVE, COMPLETED, ARCHIVED, ALL }

/** An app installed on the device, shown in the "add app" picker. */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)
