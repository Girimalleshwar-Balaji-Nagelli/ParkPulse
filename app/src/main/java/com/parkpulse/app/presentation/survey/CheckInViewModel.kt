package com.parkpulse.app.presentation.survey

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.repository.ParkRepository
import com.parkpulse.app.gis.CheckInResult
import com.parkpulse.app.gis.CheckInValidator
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.gis.PolygonUtils
import com.parkpulse.app.location.LocationClient
import com.parkpulse.app.location.ParkLocationManager
import com.parkpulse.app.location.toFix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CheckInStatus {
    object Loading : CheckInStatus
    object ParkNotFound : CheckInStatus
    object NoBoundary : CheckInStatus
    object PermissionDenied : CheckInStatus
    object LocationOff : CheckInStatus
    object SearchingGps : CheckInStatus
    data class Evaluated(val result: CheckInResult) : CheckInStatus
}

data class CheckInUiState(
    val parkName: String = "",
    val status: CheckInStatus = CheckInStatus.Loading,
) {
    val canCheckIn: Boolean
        get() = (status as? CheckInStatus.Evaluated)?.result is CheckInResult.Allowed
}

sealed interface CheckInEvent {
    data class CheckedIn(
        val parkId: String,
        val timeMillis: Long,
        val latitude: Double,
        val longitude: Double,
        val accuracyM: Float,
    ) : CheckInEvent
}

@HiltViewModel
class CheckInViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    parkRepository: ParkRepository,
    private val locationClient: LocationClient,
    private val locationManager: ParkLocationManager,
) : ViewModel() {

    private val parkId: String = checkNotNull(savedStateHandle.get<String>("parkId"))

    private sealed interface Lookup {
        object Loading : Lookup
        object NotFound : Lookup
        class Found(val name: String, val ring: List<GeoPoint>?) : Lookup
    }

    private val lookup: StateFlow<Lookup> = parkRepository.observeParks()
        .map { parks ->
            val park = parks.firstOrNull { it.id == parkId }
            val result: Lookup = if (park == null) {
                Lookup.NotFound
            } else {
                // Same validation the map uses: an unusable boundary means no check-in. Never invented.
                val ring = park.boundary
                    ?.map { GeoPoint(it.latitude, it.longitude) }
                    ?.let(PolygonUtils::normalizeRing)
                Lookup.Found(park.name, ring)
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Lookup.Loading)

    private val hasPermission = MutableStateFlow(locationManager.hasLocationPermission())
    private val locationEnabled = MutableStateFlow(locationManager.isLocationServiceEnabled())
    private val lastLocation = MutableStateFlow<Location?>(null)
    private val ticker: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(1_000) // re-evaluate every second so an aging fix turns "stale" by itself
        }
    }
    private var updatesJob: Job? = null

    private val _events = Channel<CheckInEvent>(Channel.BUFFERED)
    val events: Flow<CheckInEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<CheckInUiState> = combine(
        lookup, hasPermission, locationEnabled, lastLocation, ticker,
    ) { found, permission, enabled, location, _ ->
        when (found) {
            Lookup.Loading -> CheckInUiState()
            Lookup.NotFound -> CheckInUiState(status = CheckInStatus.ParkNotFound)
            is Lookup.Found -> CheckInUiState(found.name, statusFor(found.ring, permission, enabled, location))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckInUiState())

    private fun statusFor(
        ring: List<GeoPoint>?,
        permission: Boolean,
        enabled: Boolean,
        location: Location?,
    ): CheckInStatus = when {
        ring == null -> CheckInStatus.NoBoundary
        !permission -> CheckInStatus.PermissionDenied
        !enabled -> CheckInStatus.LocationOff
        location == null -> CheckInStatus.SearchingGps
        else -> CheckInStatus.Evaluated(CheckInValidator.evaluate(ring, location.toFix()))
    }

    /** Call when the screen becomes visible (also after returning from system settings). */
    fun onScreenStart() {
        hasPermission.value = locationManager.hasLocationPermission()
        locationEnabled.value = locationManager.isLocationServiceEnabled()
        startUpdates()
    }

    fun onScreenStop() {
        updatesJob?.cancel()
        updatesJob = null
    }

    fun onPermissionResult(granted: Boolean) {
        hasPermission.value = granted
        if (granted) startUpdates()
    }

    private fun startUpdates() {
        if (!hasPermission.value || updatesJob?.isActive == true) return
        updatesJob = viewModelScope.launch {
            locationClient.locationUpdates(1_000L)
                .catch { hasPermission.value = false } // permission revoked while running
                .collect { lastLocation.value = it }
        }
    }

    /** Re-validates against the newest fix at tap time. Check-in is impossible unless Allowed. */
    fun onCheckIn() {
        val found = lookup.value as? Lookup.Found ?: return
        val location = lastLocation.value ?: return
        if (!hasPermission.value || !locationManager.isLocationServiceEnabled()) return
        val result = CheckInValidator.evaluate(found.ring, location.toFix())
        if (result !is CheckInResult.Allowed) return
        viewModelScope.launch {
            _events.send(
                CheckInEvent.CheckedIn(
                    parkId = parkId,
                    timeMillis = System.currentTimeMillis(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyM = result.accuracyM,
                )
            )
        }
    }
}
