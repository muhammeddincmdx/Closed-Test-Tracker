package com.mdstudio.closedtesttracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM TrackedApp ORDER BY createdAtMillis DESC, appLabel")
    fun observeAll(): Flow<List<TrackedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TrackedApp)

    @Query("UPDATE TrackedApp SET isArchived = :archived WHERE packageName = :packageName")
    suspend fun setArchived(packageName: String, archived: Boolean)

    @Query("UPDATE TrackedApp SET completedAtMillis = :completedAtMillis WHERE packageName = :packageName")
    suspend fun setCompleted(packageName: String, completedAtMillis: Long?)

    @Query("""
        UPDATE TrackedApp
        SET createdAtMillis = :createdAtMillis,
            startDayIndex = :startDayIndex,
            completedAtMillis = NULL
        WHERE packageName = :packageName
    """)
    suspend fun resetSeries(packageName: String, createdAtMillis: Long, startDayIndex: Int)

    @Query("DELETE FROM TrackedApp WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
