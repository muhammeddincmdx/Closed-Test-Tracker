# Closed Test Tracker

Android app for tracking Google Play closed-test usage streaks, daily app usage minutes, and test progress across selected installed apps.

## Daily Notes

- 2026-05-23: Recovered the usage-time calculation for long-running streaks. App detail now reads each tracked day individually so 14+ day series no longer collapse old days to zero, list cards show total time again, and loading states avoid flashing incorrect zero-minute values.
- 2026-05-17: Updated app picker sorting so `Newest` uses install time descending and `Oldest` uses install time ascending. Also forced picker list state to rebuild correctly when switching between `A-Z`, `Newest`, and `Oldest`.
- 2026-05-17: Reduced UI freeze when returning to the app by moving heavy usage-stat calculations off the main UI flow and caching today's usage reads.
- 2026-05-17: Release version updated to `versionCode 6` and `versionName 0.0.16.5.2`. New Android App Bundle generated for Play Console upload.
- 2026-05-06: Removed the `Reset today` management action. The day-tracking flow is now one-way, with only set day, finish, archive, and delete actions remaining.
- 2026-05-06: Added the app summaries panel, detail page graph/text view toggle, and automatic usage refresh flow.
