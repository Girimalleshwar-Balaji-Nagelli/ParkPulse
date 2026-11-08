package com.parkpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkpulse.app.domain.model.Survey

/**
 * Enum-like columns are stored as plain Strings on purpose, so this table never depends on the
 * database's TypeConverters and the hand-written migration SQL stays exact.
 */
@Entity(tableName = "surveys", indices = [Index("parkId"), Index("syncStatus")])
data class SurveyEntity(
    @PrimaryKey val id: String,
    val parkId: String,
    val checkInTimeMillis: Long,
    val checkInLatitude: Double,
    val checkInLongitude: Double,
    val checkInAccuracyM: Float,
    val treeCount: Int,
    val lightCount: Int,
    val cleanliness: String,
    val notes: String,
    val createdAtMillis: Long,
    val syncStatus: String,
)

fun Survey.toEntity() = SurveyEntity(
    id = id,
    parkId = parkId,
    checkInTimeMillis = checkInTimeMillis,
    checkInLatitude = checkInLatitude,
    checkInLongitude = checkInLongitude,
    checkInAccuracyM = checkInAccuracyM,
    treeCount = treeCount,
    lightCount = lightCount,
    cleanliness = cleanliness.name,
    notes = notes,
    createdAtMillis = createdAtMillis,
    syncStatus = syncStatus.name,
)
