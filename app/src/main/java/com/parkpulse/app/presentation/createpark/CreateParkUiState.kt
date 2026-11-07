package com.parkpulse.app.presentation.createpark

import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.gis.GeoPoint

data class BoundaryStats(
    val areaSqM: Double,
    val areaAcres: Double,
    val perimeterM: Double,
)

data class CreateParkUiState(
    /** Open ring: the first point is NOT repeated at the end. */
    val points: List<GeoPoint> = emptyList(),
    val name: String = "",
    val nameTouched: Boolean = false,
    val type: ParkType = ParkType.GARDEN,
    val status: ParkStatus = ParkStatus.OPEN,
    /** Non-null only when the boundary is valid (>= 3 points, simple, big enough). */
    val stats: BoundaryStats? = null,
    /** Non-null when >= 3 points exist but the boundary is not valid. */
    val boundaryError: String? = null,
    val isSaving: Boolean = false,
) {
    val nameError: String? get() = if (nameTouched && name.isBlank()) "Enter a park name" else null
    val canSave: Boolean
        get() = points.size >= 3 && stats != null && boundaryError == null && name.isNotBlank() && !isSaving
}

sealed interface CreateParkEvent {
    object Saved : CreateParkEvent
    data class Failed(val message: String) : CreateParkEvent
}