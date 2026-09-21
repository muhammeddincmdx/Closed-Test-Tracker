package com.mdstudio.closedtesttracker

import android.app.Activity
import android.content.Context
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Asks for a Play In-App Review rarely and only after meaningful use.
 * Google enforces its own quota, so we also throttle locally.
 */
object InAppReviewHelper {
    private const val PREFS_NAME = "tester_settings"
    private const val KEY_LAUNCH_COUNT = "review_launch_count"
    private const val KEY_FIRST_LAUNCH = "review_first_launch_millis"
    private const val KEY_LAST_REQUEST = "review_last_request_millis"

    private const val MIN_LAUNCHES = 5
    private const val MIN_DAYS_SINCE_FIRST_LAUNCH = 3L
    private const val MIN_DAYS_BETWEEN_REQUESTS = 120L
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    fun recordLaunch(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit().putInt(KEY_LAUNCH_COUNT, prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1)
        if (!prefs.contains(KEY_FIRST_LAUNCH)) {
            editor.putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis())
        }
        editor.apply()
    }

    private fun isEligible(context: Context, hasActiveTracking: Boolean): Boolean {
        if (!hasActiveTracking) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, now)
        val lastRequest = prefs.getLong(KEY_LAST_REQUEST, 0L)
        return prefs.getInt(KEY_LAUNCH_COUNT, 0) >= MIN_LAUNCHES &&
            now - firstLaunch >= MIN_DAYS_SINCE_FIRST_LAUNCH * DAY_MILLIS &&
            (lastRequest == 0L || now - lastRequest >= MIN_DAYS_BETWEEN_REQUESTS * DAY_MILLIS)
    }

    suspend fun maybeRequestReview(activity: Activity, hasActiveTracking: Boolean) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (!isEligible(activity, hasActiveTracking)) return
        // Mark before asking so a failure or quota drop never causes repeated prompts.
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LAST_REQUEST, System.currentTimeMillis()).apply()
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            val info = manager.requestReview()
            manager.launchReview(activity, info)
        }
    }
}
