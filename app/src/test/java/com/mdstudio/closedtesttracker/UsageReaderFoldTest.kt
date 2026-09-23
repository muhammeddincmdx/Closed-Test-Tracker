package com.mdstudio.closedtesttracker

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [UsageReader.foldForegroundMillisByDay], the pure foreground
 * session reducer behind the event-based usage readers. These lock in the
 * over-counting fix: screen-off events close open sessions and dangling sessions
 * are capped, so a "6 minutes real" usage can no longer read as hundreds of
 * minutes.
 */
class UsageReaderFoldTest {

    // Android UsageEvents.Event type ints (kept literal to mirror the platform).
    private val RESUMED = 1
    private val PAUSED = 2
    private val SCREEN_NON_INTERACTIVE = 16
    private val STOPPED = 23

    private val minute = 60_000L
    private val hour = 60L * minute
    private val day = 24L * hour

    // Deterministic UTC-midnight day bucketing for tests.
    private val dayStartOf: (Long) -> Long = { t -> (t / day) * day }

    private fun ev(type: Int, atMinutes: Long, belongs: Boolean = true) =
        UsageReader.ForegroundSessionEvent(type, atMinutes * minute, belongs)

    private fun totalMinutes(map: Map<Long, Long>): Long = map.values.sum() / minute

    @Test
    fun resumedThenPaused_countsExactForegroundSpan() {
        val base = 10L * day // a clean midnight
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, base + 2 * minute, true),
            UsageReader.ForegroundSessionEvent(PAUSED, base + 8 * minute, true)
        )
        val result = UsageReader.foldForegroundMillisByDay(events, base + day, dayStartOf)
        assertEquals(6L, totalMinutes(result)) // exactly 6 minutes, the real case
    }

    @Test
    fun screenOff_closesDanglingSession_insteadOfCountingToWindowEnd() {
        // App resumed, used 6 min, then the SCREEN turned off with no app PAUSED.
        // Window end is 6 hours later. Old logic counted resume->windowEnd (~366
        // min); the fix must count only up to screen-off (6 min).
        val base = 10L * day
        val windowEnd = base + 6 * hour
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, base + 2 * minute, true),
            // global screen-off event belongs to a different package (belongs=false)
            UsageReader.ForegroundSessionEvent(SCREEN_NON_INTERACTIVE, base + 8 * minute, false)
        )
        val result = UsageReader.foldForegroundMillisByDay(events, windowEnd, dayStartOf)
        assertEquals(6L, totalMinutes(result))
    }

    @Test
    fun danglingSessionWithNoCloser_isCappedToThirtyMinutes() {
        // Resume with no PAUSED/STOPPED/screen event at all; window end is 10 days
        // out. Must cap at 30 min rather than counting 10 days of phantom foreground.
        val base = 10L * day
        val windowEnd = base + 10 * day
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, base, true)
        )
        val result = UsageReader.foldForegroundMillisByDay(events, windowEnd, dayStartOf)
        assertEquals(30L, totalMinutes(result))
    }

    @Test
    fun splashToMain_lateSplashStop_doesNotCloseMainSession() {
        // Android logs a screen change as: old PAUSED, new RESUMED, old STOPPED.
        // The late Splash STOPPED must not end the Main screen's session.
        val base = 10L * day
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, base, true, "Splash"),
            UsageReader.ForegroundSessionEvent(PAUSED, base + 1 * minute, true, "Splash"),
            UsageReader.ForegroundSessionEvent(RESUMED, base + 1 * minute, true, "Main"),
            UsageReader.ForegroundSessionEvent(STOPPED, base + 2 * minute, true, "Splash"),
            UsageReader.ForegroundSessionEvent(PAUSED, base + 21 * minute, true, "Main")
        )
        val result = UsageReader.foldForegroundMillisByDay(events, base + day, dayStartOf)
        assertEquals(21L, totalMinutes(result))
    }

    @Test
    fun screenOff_closesSession_evenWithSeveralActivitiesResumed() {
        // Multi-window / split screen: two activities resumed at once. Screen-off
        // still ends the session.
        val base = 10L * day
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, base, true, "A"),
            UsageReader.ForegroundSessionEvent(RESUMED, base + 1 * minute, true, "B"),
            UsageReader.ForegroundSessionEvent(PAUSED, base + 3 * minute, true, "A"),
            UsageReader.ForegroundSessionEvent(SCREEN_NON_INTERACTIVE, base + 5 * minute, false)
        )
        val result = UsageReader.foldForegroundMillisByDay(events, base + day, dayStartOf)
        assertEquals(5L, totalMinutes(result))
    }

    @Test
    fun multipleSessions_sumPerDay() {
        val base = 10L * day
        val events = listOf(
            ev(RESUMED, base / minute + 1),
            ev(PAUSED, base / minute + 4),   // 3 min
            ev(RESUMED, base / minute + 10),
            ev(STOPPED, base / minute + 12)  // 2 min
        )
        val result = UsageReader.foldForegroundMillisByDay(events, base + day, dayStartOf)
        assertEquals(5L, totalMinutes(result))
    }

    @Test
    fun sessionCrossingMidnight_splitsAcrossTwoDays() {
        // Start 20 minutes before midnight, end 10 minutes after -> 20 + 10.
        val midnight = 11L * day
        val start = midnight - 20 * minute
        val end = midnight + 10 * minute
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, start, true),
            UsageReader.ForegroundSessionEvent(PAUSED, end, true)
        )
        val result = UsageReader.foldForegroundMillisByDay(events, midnight + day, dayStartOf)
        assertEquals(2, result.size)
        assertEquals(20L, result[dayStartOf(start)]!! / minute)
        assertEquals(10L, result[dayStartOf(end)]!! / minute)
        assertEquals(30L, totalMinutes(result))
    }

    @Test
    fun eventsForOtherPackages_areIgnored() {
        val base = 10L * day
        val events = listOf(
            UsageReader.ForegroundSessionEvent(RESUMED, base + 1 * minute, false),
            UsageReader.ForegroundSessionEvent(PAUSED, base + 30 * minute, false)
        )
        val result = UsageReader.foldForegroundMillisByDay(events, base + day, dayStartOf)
        assertEquals(0L, totalMinutes(result))
    }
}
