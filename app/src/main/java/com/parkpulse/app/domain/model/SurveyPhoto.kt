package com.parkpulse.app.domain.model

data class SurveyPhoto(
    val id: String,
    val surveyId: String,
    /** File name inside the app's private survey_photos directory (not an absolute path). */
    val fileName: String,
    val takenAtMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    val createdAtMillis: Long,
    val syncStatus: SyncStatus,
)
