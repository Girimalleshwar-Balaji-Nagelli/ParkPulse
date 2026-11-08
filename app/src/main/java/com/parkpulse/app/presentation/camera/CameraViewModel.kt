package com.parkpulse.app.presentation.camera

import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.data.photo.PhotoStorage
import com.parkpulse.app.domain.repository.ParkRepository
import com.parkpulse.app.gis.PhotoGateResult
import com.parkpulse.app.gis.PhotoLocationGate
import com.parkpulse.app.location.LocationClient
import com.parkpulse.app.location.ParkLocationManager
import com.parkpulse.app.location.toFix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

sealed interface CameraPhase {
    object Live : CameraPhase
    object Processing : CameraPhase
    class Review(val bitmap: Bitmap, val stamp: StampData) : CameraPhase
    object Saving : CameraPhase
}

data class CameraUiState(
    val parkName: String = "",
    val phase: CameraPhase = CameraPhase.Live,
    /** Live stamp for the preview overlay; null until a trustworthy GPS fix exists. */
    val stampPreview: StampData? = null,
    /** Why capture is blocked (null when ready). */
    val gpsMessage: String? = null,
    val canCapture: Boolean = false,
    val message: String? = null,
)

sealed interface CameraEvent {
    data class PhotoSaved(val draft: PhotoDraft) : CameraEvent
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    parkRepository: ParkRepository,
    private val locationClient: LocationClient,
    private val locationManager: ParkLocationManager,
    private val photoStorage: PhotoStorage,
) : ViewModel() {

    private val parkId: String = checkNotNull(savedStateHandle.get<String>("parkId"))

    private val parkName: StateFlow<String> = parkRepository.observeParks()
        .map { parks -> parks.firstOrNull { it.id == parkId }?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val hasLocationPermission = MutableStateFlow(locationManager.hasLocationPermission())
    private val lastLocation = MutableStateFlow<Location?>(null)
    private val phase = MutableStateFlow<CameraPhase>(CameraPhase.Live)
    private val message = MutableStateFlow<String?>(null)
    private val ticker: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(1_000) // keeps the overlay clock and the "stale GPS" rule live
        }
    }
    private var updatesJob: Job? = null

    private val _events = Channel<CameraEvent>(Channel.BUFFERED)
    val events: Flow<CameraEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<CameraUiState> = combine(
        parkName, lastLocation, phase, message, ticker,
    ) { name, location, currentPhase, msg, _ ->
        val fix = location?.toFix()
        val gate = PhotoLocationGate.check(fix)
        val ready = gate == PhotoGateResult.Ready && fix != null
        CameraUiState(
            parkName = name,
            phase = currentPhase,
            stampPreview = if (ready && fix != null) {
                StampData(name, fix.point.latitude, fix.point.longitude, fix.accuracyM ?: 0f, System.currentTimeMillis())
            } else null,
            gpsMessage = when {
                !hasLocationPermission.value -> "Allow location access to stamp photos"
                gate == PhotoGateResult.MockLocation -> "Mock locations are not allowed"
                gate == PhotoGateResult.WaitingForGps -> "Waiting for GPS…"
                else -> null
            },
            canCapture = ready && name.isNotEmpty() && currentPhase is CameraPhase.Live,
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CameraUiState())

    fun onScreenStart() {
        hasLocationPermission.value = locationManager.hasLocationPermission()
        startUpdates()
    }

    fun onScreenStop() {
        updatesJob?.cancel()
        updatesJob = null
    }

    fun onPermissionResult(locationGranted: Boolean) {
        hasLocationPermission.value = locationGranted
        if (locationGranted) startUpdates()
    }

    private fun startUpdates() {
        if (!hasLocationPermission.value || updatesJob?.isActive == true) return
        updatesJob = viewModelScope.launch {
            locationClient.locationUpdates(1_000L)
                .catch { hasLocationPermission.value = false }
                .collect { lastLocation.value = it }
        }
    }

    fun dismissMessage() {
        message.value = null
    }

    /** Freezes the stamp data at the moment of the shutter press. Null = capture not allowed right now. */
    fun beginCapture(): StampData? {
        val state = uiState.value
        val preview = state.stampPreview
        if (!state.canCapture || preview == null) return null
        phase.value = CameraPhase.Processing
        return preview.copy(takenAtMillis = System.currentTimeMillis())
    }

    fun onImageCaptured(source: Bitmap, rotationDegrees: Int, stamp: StampData) {
        viewModelScope.launch {
            try {
                val stamped = withContext(Dispatchers.Default) {
                    try {
                        PhotoStamper.stamp(source, rotationDegrees, stamp)
                    } finally {
                        source.recycle()
                    }
                }
                phase.value = CameraPhase.Review(stamped, stamp)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onCaptureFailed()
            }
        }
    }

    fun onCaptureFailed() {
        phase.value = CameraPhase.Live
        message.value = "Could not take the photo. Please try again."
    }

    fun retake() {
        (phase.value as? CameraPhase.Review)?.bitmap?.recycle()
        phase.value = CameraPhase.Live
    }

    /** Writes the stamped JPEG to permanent app storage and reports it. Nothing is attached to a survey yet. */
    fun confirm() {
        val review = phase.value as? CameraPhase.Review ?: return
        phase.value = CameraPhase.Saving
        viewModelScope.launch {
            try {
                val fileName = withContext(Dispatchers.IO) { photoStorage.saveJpeg(review.bitmap) }
                review.bitmap.recycle()
                val s = review.stamp
                _events.send(
                    CameraEvent.PhotoSaved(
                        PhotoDraft(
                            id = "photo-${UUID.randomUUID()}",
                            fileName = fileName,
                            takenAtMillis = s.takenAtMillis,
                            latitude = s.latitude,
                            longitude = s.longitude,
                            accuracyM = s.accuracyM,
                        )
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                phase.value = review // bitmap still valid: let the user retry or retake
                message.value = "Could not save the photo. Please try again."
            }
        }
    }

    override fun onCleared() {
        (phase.value as? CameraPhase.Review)?.bitmap?.recycle()
    }
}
