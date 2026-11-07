package com.parkpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parkpulse.app.data.local.entity.ParkEntity
import com.parkpulse.app.domain.model.SyncStatus
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

    // ---- sync ----
    @Query("SELECT * FROM parks WHERE syncStatus = :status ORDER BY name COLLATE NOCASE ASC")
    suspend fun getBySyncStatus(status: SyncStatus): List<ParkEntity>

    @Query("SELECT COUNT(*) FROM parks WHERE syncStatus = :status")
    fun observeCountBySyncStatus(status: SyncStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM parks WHERE syncStatus = :status")
    suspend fun countBySyncStatus(status: SyncStatus): Int

    @Query("UPDATE parks SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
}