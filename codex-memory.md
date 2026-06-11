# Codex Memory / AI Handoff

This file was created from the repository contents, git history, README notes, and the current Codex session context. It does not claim to contain every historical ChatGPT/Codex conversation; older decisions may be incomplete if they are not visible in the repo or current context.

## Project Overview

Closed Test Tracker is an Android app for users running Google Play closed tests. It helps testers track selected apps over a 14-day closed-test streak, see daily usage minutes, continue counting beyond day 14 until the user explicitly finishes the test, and prepare screenshot-friendly summaries for proof of testing.

Target users are Android developers/testers who need to keep multiple closed-test apps opened regularly and documented during Google Play closed testing.

## Current Status

- Project is actively developed.
- Google Play closed-test / production-preparation work has already happened.
- Current Gradle values in `app/build.gradle.kts` are `versionCode 14` and `versionName 0.0.6.6.1`.
- Package name is `com.mdstudio.closedtesttracker` for release.
- Debug builds use `applicationIdSuffix = ".debug"` and app name `Debug`.
- Working tree is currently not clean; several app files and generated/local artifacts are modified or untracked. Do not assume all current files are committed.

## Tech Stack

- Android native app.
- Kotlin.
- Jetpack Compose UI.
- Material 3 and Material icons.
- Room database with KSP.
- WorkManager for daily reminder scheduling.
- Android UsageStats APIs for app usage reading.
- AppWidgetProvider / RemoteViews for home-screen widget.
- Google Play in-app update library.
- Google Mobile Ads SDK for banner/rewarded ad support.
- FileProvider for sharing generated screenshots/images.
- Gradle Android plugin 8.5.2.
- Kotlin plugin 2.0.21.
- compileSdk / targetSdk 35.
- minSdk 26.

## App / Product Features

- Add installed launcher apps to a tracked closed-test list.
- App picker supports search and sorting by A-Z, newest installed, and oldest installed.
- Selected apps are stored locally in Room.
- User can set the starting test day when adding an app.
- Test day is calculated by local calendar date, not by elapsed install time.
- Streak can continue beyond 14 days until the user explicitly finishes it.
- Tracked apps can be active, completed, archived, restored, or deleted.
- List cards show app icon/name, current test day, progress, today usage, and total usage.
- Detail page shows per-day usage with graph/text modes.
- Usage calculations combine aggregate UsageStats and event-based reads, with extra handling for web-backed/PWA-like apps.
- Daily reminders warn about apps not opened today and apps past day 14 waiting to be finished.
- Notification time is configurable.
- Widget summarizes active/completed/missing app state.
- Screenshot-friendly gallery/grid view exists for sharing app test status.
- Screenshot detection/share flow exists in recent code context.
- Multi-language UI includes Turkish, English, French, Spanish, Chinese, Hindi, Russian, Arabic, German, Japanese, Portuguese, and Indonesian enum support in the main screen code.
- Theme controls include system/light/dark behavior and app themes such as Fresh, Ocean, and Sunset.
- How-to-use guide assets exist under drawable resources.
- Settings includes help/about/legal/product notes and support actions.
- App can open tracked apps directly from the tracker.
- AdMob banner and rewarded-ad support exist; exact ad IDs are intentionally not documented here.
- Debug-only live usage overlay service exists behind `app/src/debug` with `SYSTEM_ALERT_WINDOW` permission.

## Important Decisions Made

- Local-first storage was chosen with Room; no account system or cloud sync is present.
- Usage tracking uses Android `PACKAGE_USAGE_STATS`; this requires the user to grant usage access in system settings.
- Broad `QUERY_ALL_PACKAGES` was removed after Google Play policy rejection; package visibility is restricted through the `<queries>` launcher intent approach.
- Only launcher apps should be listed/selected to avoid showing Android system/internal packages.
- Test day should increment by calendar day in the user's local timezone.
- Day count may exceed 14; finishing is a deliberate user action.
- Destructive management actions require confirmation or should be visually clear.
- `Reset today` was removed from management because the flow should not allow arbitrary daily reset.
- Usage totals for long streaks must not be limited to the last 14 days; detail and list totals should remain consistent for 14+ day tests.
- App data must survive normal app updates. Use `install -r`; do not uninstall during testing unless a backup/restore plan is explicit.
- Debug and release can coexist because debug has a package suffix.
- AdMob is included for monetization, but Play declarations must match manifest permissions such as `AD_ID`.
- The app should avoid `QUERY_ALL_PACKAGES` because closed-test tracking was not accepted by Google as a permitted broad package visibility use case.
- Store-facing and policy text must be tightly scoped to actual shipped features.
- Sensitive values such as keystore passwords, tokens, and ad unit IDs should not be copied into docs or prompts.

## Completed Actions

From README/git history/current repo:

- Initial Closed Test Tracker app created.
- Back navigation from settings fixed.
- Tracking management features added.
- Auto-increment test day by calendar date added.
- Series count allowed to continue past day 14.
- Detail screen and streak flow refined.
- `Reset today` management action removed.
- Detail page graph/text mode, app summaries, and automatic usage refresh added.
- Long-running streak usage totals recovered so old days no longer collapse to zero.
- Home/list cards show total time again after long-streak fix.
- Loading states adjusted to avoid flashing incorrect zero-minute values.
- App picker sorting fixed so newest/oldest use install time correctly and list state rebuilds when sort mode changes.
- UI was iteratively modernized toward glass/screenshot-friendly cards and grid summaries.
- App icon asset `closed_test_tracker.png` exists.
- How-to-use screenshots/assets were added.
- Widget provider exists for streak status.
- WorkManager reminder flow exists.
- Ad support was added, including rewarded support in settings/support area and banner infrastructure.
- `app-ads.txt` setup was handled externally via the developer website in prior release work.
- Release/package policy work included target SDK 35 and removal of broad package visibility.

Recent git log highlights:

- `651e019 testde 12. gün reklam yapısı getirildi`
- `5119410 Refresh usage and installed apps on resume`
- `64b4fd6 Recover long streak usage totals`
- `8554c65 Finalize Closed Test Tracker 0.0.16.5.2 updates`
- `fb483bf Remove reset action and document changes`
- `356b35e Refine detail screen and streak flow`
- `9a1b64f Let series count continue past 14`
- `956d5cb feat: auto increment test day based on calendar date`
- `4427f32 Add tracking management features`
- `9ab603f Handle back from settings`
- `70442f0 Initial Closed Test Tracker app`

- 2026-06-11: Reusable features exported to mobil-app-collected-data-kotlin-jetpack-compose as feature folders 01-08.

## Open Tasks / Next Actions

- Verify the current dirty working tree before any release work; decide which local changes should be committed and which generated artifacts should be ignored/removed.
- Confirm whether the current app should ship as `versionCode 14`, `versionName 0.0.6.6.1`, or a newer release version.
- Test screenshot-friendly gallery/grid on a real phone with many tracked apps.
- Verify screenshot share prompt behavior on physical devices and Android versions that support screen capture callbacks.
- Confirm AdMob banner/rewarded behavior on Play-installed release builds, not only local debug builds.
- Confirm Google Play Data Safety declarations match current permissions: usage access, notifications, advertising ID, installed launcher app visibility, and screen capture detection.
- Review all translations after UI changes; mixed English strings have been a repeated issue.
- Re-check app picker refresh when a newly installed app appears without restarting the app.
- Re-check usage totals for 30+ day streaks and web-backed/PWA apps on the user's real device.
- Decide whether debug-only live usage overlay should remain debug-only or become a production feature; production overlay would require extra policy/UX review.
- Clean up or ignore local screenshots, emulator artifacts, `.android-local`, `.gradle-user*`, `device_backups`, and other generated folders if they should not be committed.

## Known Issues / Risks

- The working tree is currently dirty with many modified/untracked files unrelated to this handoff document.
- `howtouse.md` text appears mojibake/encoding-corrupted in the current checkout and should be repaired before publishing documentation.
- Some notification translation strings in `ReminderWorker.kt` appear ASCII-transliterated or mojibake in the current source view.
- UsageStats can be delayed or inconsistent depending on Android version, vendor ROM, PWA/web-backed apps, and whether the app has usage access.
- Web-backed fallback currently estimates from browser foreground time; this can overcount or misattribute if multiple web apps/browsers are used.
- `PACKAGE_USAGE_STATS` is a special access permission; Play/user messaging must clearly explain why it is needed.
- `DETECT_SCREEN_CAPTURE` and screenshot-share UX may have Android version limitations and should be tested carefully.
- `AD_ID` is declared; Play Console advertising ID declaration must remain consistent.
- Google Play policy rejected earlier use of `QUERY_ALL_PACKAGES`; do not reintroduce it.
- Debug build has a separate package suffix and may not reflect Play update/ad behavior exactly.
- Keystore/signing details exist in build configuration, but sensitive values must not be copied into docs or prompts.

## Build & Run Instructions

Prerequisites:

- Windows + PowerShell environment.
- Android SDK installed.
- JDK compatible with Gradle/Android plugin.
- Use the Gradle wrapper in this repo.

Common commands:

```powershell
cd C:\md\tester-app
.\gradlew.bat --no-daemon --max-workers=2 :app:assembleDebug
```

Debug APK output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Install over Wi-Fi ADB without clearing user data:

```powershell
$adb = 'C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe'
& $adb connect <PHONE_IP:PORT>
& $adb -s <PHONE_IP:PORT> install -r app\build\outputs\apk\debug\app-debug.apk
& $adb -s <PHONE_IP:PORT> shell monkey -p com.mdstudio.closedtesttracker.debug -c android.intent.category.LAUNCHER 1
```

Release bundle command:

```powershell
.\gradlew.bat --no-daemon --max-workers=2 :app:bundleRelease
```

Expected release AAB output:

```text
app\build\outputs\bundle\release\app-release.aab
```

Important: do not run uninstall/clear-data commands unless the user explicitly approves data loss or a backup/restore path is prepared.

## Release / Google Play Notes

- Release package name: `com.mdstudio.closedtesttracker`.
- Debug package name: `com.mdstudio.closedtesttracker.debug`.
- Current checked Gradle version values: `versionCode 14`, `versionName 0.0.6.6.1`.
- App name: `Closed Test Tracker` for release; debug build app name is `Debug`.
- Target SDK is 35.
- App is intended for Google Play closed-test tracking and release-readiness workflows.
- Earlier Play policy issue: `QUERY_ALL_PACKAGES` was not accepted. Current manifest uses launcher `<queries>` instead.
- Important permissions/features in manifest:
  - `PACKAGE_USAGE_STATS` for usage tracking.
  - `POST_NOTIFICATIONS` for reminders.
  - `INTERNET` for ads/update/network-backed features.
  - `AD_ID` for ad monetization declarations.
  - `DETECT_SCREEN_CAPTURE` for screenshot/share UX.
  - Launcher `<queries>` for visible app picker.
- AdMob is integrated. Keep Play Console advertising ID declaration and Data Safety answers aligned with current SDK/manifest behavior.
- Developer website/app-ads.txt setup is external to this repo and must stay consistent in Play Console/AdMob.
- Store listing claims should only mention shipped behavior: tracking selected installed launcher apps, usage minutes, reminders, screenshot-friendly summaries, and closed-test organization.

## AI Agent Instructions

- Preserve existing architecture unless there is a clear reason to change it.
- Do not do broad refactors before understanding current behavior and user data migration impact.
- Treat user data preservation as critical; never uninstall or clear app data unless explicitly approved.
- Prefer small, safe changes and verify with Gradle build.
- Before editing, inspect relevant code paths in `MainActivity.kt`, `UsageReader.kt`, `SeriesCalculator.kt`, Room entities/DAO, and manifest.
- Do not add `QUERY_ALL_PACKAGES` back.
- Do not document or expose secrets, tokens, keystore passwords, signing credentials, or exact ad unit IDs.
- Keep Play policy/data safety implications in mind for permission, ads, screenshot, package visibility, overlay, and usage-access changes.
- When installing to a device, use `adb install -r` and relaunch with `monkey`; report the exact endpoint/package.
- If the repo is dirty, avoid staging unrelated user changes. Stage only files directly relevant to the task.
- After each task, give a short summary with build/install/push status and any known gaps.

## Context Gaps

- Exact current Google Play publishing state is not fully derivable from the repo.
- Current AdMob review/serving status is external and must be checked in AdMob/Play Console.
- Which untracked screenshots/artifacts should be kept, ignored, or deleted needs user confirmation.
- Whether the debug live overlay should become a production feature needs product and policy approval.
- Full historical ChatGPT/Codex conversations are not embedded in this repo; this handoff may miss decisions that only existed in chat.
- Final production privacy policy/terms URLs and current store listing text should be verified externally before release.

