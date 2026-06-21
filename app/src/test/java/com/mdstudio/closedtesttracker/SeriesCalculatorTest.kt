package com.mdstudio.closedtesttracker

import com.mdstudio.closedtesttracker.data.TrackedApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Test

/**
 * Unit tests for [SeriesCalculator], the timezone-aware day/streak math that
 * the whole tracking feature relies on. Pure JVM logic — no Android runtime
 * needed, so these run under `testDebugUnitTest`.
 */
class SeriesCalculatorTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    /** Millis at the start of [daysFromToday] relative to today, in the system zone. */
    private fun startOfDay(daysFromToday: Long): Long =
        LocalDate.now(zone).plusDays(daysFromToday).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun trackedApp(
        createdDaysAgo: Long,
        startDayIndex: Int = 1,
        completedAtMillis: Long? = null
    ) = TrackedApp(
        packageName = "com.example.app",
        appLabel = "Example",
        startDayIndex = startDayIndex,
        // add a few hours so we are safely inside the day, not on its boundary
        createdAtMillis = startOfDay(-createdDaysAgo) + 6 * 60 * 60 * 1000,
        completedAtMillis = completedAtMillis
    )

    @Test
    fun currentDay_onCreationDay_returnsStartDayIndex() {
        val app = trackedApp(createdDaysAgo = 0, startDayIndex = 1)
        assertEquals(1, SeriesCalculator.currentDay(app, now = startOfDay(0) + 12 * 60 * 60 * 1000))
    }

    @Test
    fun currentDay_advancesOnePerDay() {
        val app = trackedApp(createdDaysAgo = 3, startDayIndex = 1)
        // created 3 days ago, started at day 1 -> today is day 4
        assertEquals(4, SeriesCalculator.currentDay(app, now = startOfDay(0) + 12 * 60 * 60 * 1000))
    }

    @Test
    fun currentDay_respectsCustomStartDayIndex() {
        val app = trackedApp(createdDaysAgo = 2, startDayIndex = 5)
        // started at day 5, 2 days passed -> day 7
        assertEquals(7, SeriesCalculator.currentDay(app, now = startOfDay(0) + 12 * 60 * 60 * 1000))
    }

    @Test
    fun currentDay_neverGoesBelowStartDayIndex_whenNowBeforeCreation() {
        val app = trackedApp(createdDaysAgo = 0, startDayIndex = 3)
        // "now" before creation should clamp passed days to 0
        val now = startOfDay(-5)
        assertEquals(3, SeriesCalculator.currentDay(app, now = now))
    }

    @Test
    fun currentDay_freezesAtCompletion() {
        val app = trackedApp(
            createdDaysAgo = 10,
            startDayIndex = 1,
            // completed 4 days after creation -> frozen at day 5
            completedAtMillis = startOfDay(-6) + 6 * 60 * 60 * 1000
        )
        val frozen = SeriesCalculator.currentDay(app, now = startOfDay(0) + 12 * 60 * 60 * 1000)
        assertEquals(5, frozen)
    }

    @Test
    fun dayStartMillisForTestDay_isStartOfDayAndOrdered() {
        val app = trackedApp(createdDaysAgo = 0, startDayIndex = 1)
        val day1 = SeriesCalculator.dayStartMillisForTestDay(app, testDay = 1)
        val day2 = SeriesCalculator.dayStartMillisForTestDay(app, testDay = 2)

        // day 1 maps to the creation day's start-of-day
        assertEquals(startOfDay(0), day1)
        // consecutive test days are exactly 24h apart (DST aside, true for test data)
        assertEquals(24 * 60 * 60 * 1000L, day2 - day1)
        assertTrue(day2 > day1)
    }

    @Test
    fun dayStartMillis_offsetZero_isTodayMidnight() {
        assertEquals(startOfDay(0), SeriesCalculator.dayStartMillis(0))
    }
}
