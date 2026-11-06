package com.parkpulse.app.presentation.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.repository.ParkRepository
import com.parkpulse.app.gis.CompassDirectionCalculator
import com.parkpulse.app.gis.DistanceCalculator
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.gis.PolygonUtils
import com.parkpulse.app.location.LocationClient
import com.parkpulse.app.location.ParkLocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    parkRepository: ParkRepository,
    private val locationClient: LocationClient,
    private val locationManager: ParkLocationManager,
) : ViewModel() {

    private val mappedParks = parkRepository.observeParks()
        .map { it.toMappedParks() }
        .flowOn(Dispatchers.Default)

    private val userLocation = MutableStateFlow<Location?>(null)
    private val hasPermission = MutableStateFlow(locationManager.hasLocationPermission())
    private val selectedId = MutableStateFlow<String?>(null)
    private var locationJob: Job? = null

    val uiState: StateFlow<MapUiState> = combine(
        mappedParks, userLocation, hasPermission, selectedId,
    ) { mapped, loc, permission, selId ->
        val selectedPark = mapped.parks.firstOrNull { it.id == selId }
        MapUiState(
            isLoading = false,
            parks = mapped.parks,
            missingBoundaryCount = mapped.missingBoundary,
            invalidBoundaryCount = mapped.invalidBoundary,
            userLocation = loc,
            hasLocationPermission = permission,
            selectedParkId = selectedPark?.id,
            selected = selectedPark?.let { buildInfo(it, loc) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState(hasLocationPermission = locationManager.hasLocationPermission()),
    )

    fun onParkTapped(parkId: String?) {
        selectedId.value = parkId
    }

    fun onPermissionResult(granted: Boolean) {
        hasPermission.value = granted
        if (granted) startLocationUpdates()
    }

    fun isLocationServiceEnabled(): Boolean = locationManager.isLocationServiceEnabled()

    fun startLocationUpdates() {
        if (!hasPermission.value || locationJob?.isActive == true) return
        locationJob = viewModelScope.launch {
            locationClient.locationUpdates()
                .catch { hasPermission.value = false } // SecurityException: permission revoked
                .collect { userLocation.value = it }
        }
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun buildInfo(park: MapPark, loc: Location?): SelectedParkInfo {
        val base = SelectedParkInfo(
            id = park.id, name = park.name, status = park.status,
            areaSquareMeters = park.areaSquareMeters,
            distanceMeters = null, isUserInside = false, bearingDegrees = null, direction = null,
        )
        if (loc == null) return base
        val user = GeoPoint(loc.latitude, loc.longitude)
        if (PolygonUtils.contains(park.ring, user)) {
            return base.copy(distanceMeters = 0.0, isUserInside = true)
        }
        val nearest = PolygonUtils.nearestPointOnBoundary(park.ring, user)
        val bearing = CompassDirectionCalculator.bearingDegrees(user, nearest)
        return base.copy(
            distanceMeters = DistanceCalculator.meters(user, nearest),
            bearingDegrees = bearing,
            direction = CompassDirectionCalculator.fromBearing(bearing),
        )
    }
}