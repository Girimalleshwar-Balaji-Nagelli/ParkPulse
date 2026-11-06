package com.parkpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parkpulse.app.data.local.entity.ParkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkDao {
    @Query("SELECT * FROM parks ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ParkEntity>>

    @Query("SELECT COUNT(*) FROM parks")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(parks: List<ParkEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(park: ParkEntity)
}