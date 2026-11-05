package com.parkpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkpulse.app.domain.model.LatLng
import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.domain.model.SyncStatus
import java.time.LocalDate

@Entity(tableName = "parks", indices = [Index("name")])
data class ParkEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: ParkType,
    val areaSqM: Double,
    val boundary: List<LatLng>?,
    val lastSurveyDate: LocalDate,
    val status: ParkStatus,
    val syncStatus: SyncStatus
)

fun ParkEntity.toDomain() = Park(id, name, type, areaSqM, boundary, lastSurveyDate, status, syncStatus)
fun Park.toEntity() = ParkEntity(id, name, type, areaSqM, boundary, lastSurveyDate, status, syncStatus)