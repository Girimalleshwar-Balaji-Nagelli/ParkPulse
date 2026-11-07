package com.parkpulse.app.presentation.createpark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.model.LatLng
import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.domain.model.SyncStatus
import com.parkpulse.app.domain.repository.ParkRepository
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.gis.PolygonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateParkViewModel @Inject constructor(
    private val parkRepository: ParkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateParkUiState())
    val uiState: StateFlow<CreateParkUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreateParkEvent>(Channel.BUFFERED)
    val events: Flow<CreateParkEvent> = _events.receiveAsFlow()

    fun addPoint(point: GeoPoint) = updatePoints { it + point }

    fun movePoint(index: Int, point: GeoPoint) = updatePoints { pts ->
        if (index !in pts.indices) pts else pts.toMutableList().also { it[index] = point }
    }

    fun undoLastPoint() = updatePoints { it.dropLast(1) }

    fun clearPoints() = updatePoints { emptyList() }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameTouched = true) }
    fun onTypeChange(value: ParkType) = _uiState.update { it.copy(type = value) }
    fun onStatusChange(value: ParkStatus) = _uiState.update { it.copy(status = value) }

    fun save() {
        _uiState.update { it.copy(nameTouched = true) }
        val s = _uiState.value
        val stats = s.stats
        if (!s.canSave || stats == null) return

        val park = Park(
            id = "park-${UUID.randomUUID()}",
            name = s.name.trim(),
            type = s.type,
            areaSqM = stats.areaSqM,
            // Stored as a closed ring, same convention as the seeded OSM boundaries.
            boundary = PolygonUtils.closed(s.points).map { LatLng(it.latitude, it.longitude) },
            lastSurveyDate = LocalDate.now(),
            status = s.status,
            syncStatus = SyncStatus.PENDING, // created locally, not synced yet
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                parkRepository.addPark(park)
                _events.send(CreateParkEvent.Saved)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(CreateParkEvent.Failed("Could not save the park. Please try again."))
            }
        }
    }

    private fun updatePoints(transform: (List<GeoPoint>) -> List<GeoPoint>) {
        _uiState.update { state ->
            val points = transform(state.points)
            val (stats, error) = analyse(points)
            state.copy(points = points, stats = stats, boundaryError = error)
        }
    }

    private fun analyse(points: List<GeoPoint>): Pair<BoundaryStats?, String?> {
        if (points.size < MIN_POINTS) return null to null
        if (points.distinct().size != points.size) {
            return null to "Two points are at the same spot. Move one of them."
        }
        if (PolygonUtils.isSelfIntersecting(points)) {
            return null to "The boundary crosses itself. Drag points until no lines cross."
        }
        val area = PolygonUtils.areaSquareMeters(points)
        if (area < MIN_AREA_SQM) {
            return null to "Area is under ${MIN_AREA_SQM.toInt()} m². Zoom in and spread the points out."
        }
        return BoundaryStats(
            areaSqM = area,
            areaAcres = PolygonUtils.sqmToAcres(area),
            perimeterM = PolygonUtils.perimeterMeters(points),
        ) to null
    }

    private companion object {
        const val MIN_POINTS = 3
        const val MIN_AREA_SQM = 100.0
    }
}