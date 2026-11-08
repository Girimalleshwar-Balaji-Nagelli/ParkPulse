package com.parkpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.parkpulse.app.data.local.entity.SurveyEntity
import com.parkpulse.app.data.local.entity.SurveyPhotoEntity
import kotlinx.coroutines.flow.Flow

/** Abstract class (not interface) so the @Transaction method can have a body. Hilt provision is unchanged. */
@Dao
abstract class SurveyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(survey: SurveyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPhotos(photos: List<SurveyPhotoEntity>)

    /** Survey and photos land together or not at all. */
    @Transaction
    open suspend fun insertSurveyWithPhotos(survey: SurveyEntity, photos: List<SurveyPhotoEntity>) {
        insert(survey)
        if (photos.isNotEmpty()) insertPhotos(photos)
    }

    // ---- survey sync (status is SyncStatus.name) ----
    @Query("SELECT * FROM surveys WHERE syncStatus = :status ORDER BY createdAtMillis ASC")
    abstract suspend fun getBySyncStatus(status: String): List<SurveyEntity>

    @Query("SELECT COUNT(*) FROM surveys WHERE syncStatus = :status")
    abstract fun observeCountBySyncStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM surveys WHERE syncStatus = :status")
    abstract suspend fun countBySyncStatus(status: String): Int

    @Query("UPDATE surveys SET syncStatus = :status WHERE id = :id")
    abstract suspend fun updateSyncStatus(id: String, status: String)

    // ---- photo sync ----
    @Query("SELECT * FROM survey_photos WHERE syncStatus = :status ORDER BY createdAtMillis ASC")
    abstract suspend fun getPhotosBySyncStatus(status: String): List<SurveyPhotoEntity>

    @Query("SELECT COUNT(*) FROM survey_photos WHERE syncStatus = :status")
    abstract fun observePhotoCountBySyncStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM survey_photos WHERE syncStatus = :status")
    abstract suspend fun countPhotosBySyncStatus(status: String): Int

    @Query("UPDATE survey_photos SET syncStatus = :status WHERE id = :id")
    abstract suspend fun updatePhotoSyncStatus(id: String, status: String)
}
