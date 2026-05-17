package com.mdstudio.closedtesttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackedApp(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val startDayIndex: Int = 1,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val completedAtMillis: Long? = null
)
