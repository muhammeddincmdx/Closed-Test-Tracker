# Closed Test Tracker

Android app for tracking Google Play closed-test usage streaks, daily app usage minutes, and test progress across selected installed apps.

## Project Structure

Package: `com.mdstudio.closedtesttracker` (sources live under `app/src/main/java/com/example/testerapp/`).

| File / package | Responsibility |
| --- | --- |
| `MainActivity.kt` | Compose UI, screen state and navigation (large — see note below). |
| `AppModels.kt` | Shared public UI model types (`AppLanguage`, `AppTheme`, `AppScreen`, `HomeFilter`, `SortMode`, `InstalledApp`). |
| `SeriesCalculator.kt` | Timezone-aware day/streak math (unit-tested). |
| `UsageReader.kt` | Reads daily usage minutes via `UsageStatsManager`. |
| `ReminderScheduler.kt` / `ReminderWorker.kt` | WorkManager daily reminder scheduling and notification. |
| `ClosedTestWidgetProvider.kt` | Home-screen widget (`RemoteViews`). |
| `LiveUsageOverlayService.kt` | Floating live-usage overlay bubble. |
| `data/` | Room database, DAO and the `TrackedApp` entity. |

Tests live in `app/src/test/` and run with `./gradlew testDebugUnitTest`.

> Note: `MainActivity.kt` still holds most of the UI and screen state. A further
> split into `ui/`, `domain/` and a `ViewModel` layer is recommended but is a
> larger, higher-risk refactor (tracked separately).

## Building & Signing

Debug build:

```
./gradlew assembleDebug
```

Release builds are minified and resource-shrunk by R8 (`isMinifyEnabled = true`,
`isShrinkResources = true`; keep-rules in `app/proguard-rules.pro`).

Signing credentials are **not** stored in `build.gradle.kts`. The build reads
them from a git-ignored `keystore.properties` file (copy
`keystore.properties.example` and fill in the values) or, as a fallback, from
the `CTT_STORE_PASSWORD`, `CTT_KEY_ALIAS` and `CTT_KEY_PASSWORD` environment
variables. If no credentials are configured the release variant falls back to
debug signing so it still builds locally.

## Daily Notes

- 2026-06-21: Added the Pro testing toolkit with testing guidance, Reddit communities, reusable post templates and tester notes. Added a Pro home shortcut, Turkish app naming, four-language localization completion (German, Japanese, Portuguese and Indonesian), and automated translation catalog tests. Prepared release `0.0.21.6` (`versionCode 17`).
- 2026-05-23: Recovered the usage-time calculation for long-running streaks. App detail now reads each tracked day individually so 14+ day series no longer collapse old days to zero, list cards show total time again, and loading states avoid flashing incorrect zero-minute values.
- 2026-05-17: Updated app picker sorting so `Newest` uses install time descending and `Oldest` uses install time ascending. Also forced picker list state to rebuild correctly when switching between `A-Z`, `Newest`, and `Oldest`.
- 2026-05-17: Reduced UI freeze when returning to the app by moving heavy usage-stat calculations off the main UI flow and caching today's usage reads.
- 2026-05-17: Release version updated to `versionCode 6` and `versionName 0.0.16.5.2`. New Android App Bundle generated for Play Console upload.
- 2026-05-06: Removed the `Reset today` management action. The day-tracking flow is now one-way, with only set day, finish, archive, and delete actions remaining.
- 2026-05-06: Added the app summaries panel, detail page graph/text view toggle, and automatic usage refresh flow.
