package com.example.testerapp.data

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
}
