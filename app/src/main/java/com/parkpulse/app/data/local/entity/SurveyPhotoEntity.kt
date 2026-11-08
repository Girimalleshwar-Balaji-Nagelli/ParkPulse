package com.parkpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkpulse.app.domain.model.SurveyPhoto

/** Enum-like columns are plain Strings so the hand-written migration SQL stays exact. */
@Entity(tableName = "survey_photos", indices = [Index("surveyId"), Index("syncStatus")])
data class SurveyPhotoEntity(
    @PrimaryKey val id: String,
    val surveyId: String,
    val fileName: String,
    val takenAtMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    val createdAtMillis: Long,
    val syncStatus: String,
)

fun SurveyPhoto.toEntity() = SurveyPhotoEntity(
    id = id,
    surveyId = surveyId,
    fileName = fileName,
    takenAtMillis = takenAtMillis,
    latitude = latitude,
    longitude = longitude,
    accuracyM = accuracyM,
    createdAtMillis = createdAtMillis,
    syncStatus = syncStatus.name,
)
