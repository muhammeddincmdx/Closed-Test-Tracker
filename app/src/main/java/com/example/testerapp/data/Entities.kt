package com.example.testerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackedApp(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val startDayIndex: Int = 1,
    val createdAtMillis: Long = System.currentTimeMillis()
)
