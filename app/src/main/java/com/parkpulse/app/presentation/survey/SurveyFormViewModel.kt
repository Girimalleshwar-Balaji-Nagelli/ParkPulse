package com.parkpulse.app.presentation.survey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.data.photo.PhotoStorage
import com.parkpulse.app.domain.model.Cleanliness
import com.parkpulse.app.domain.model.Survey
import com.parkpulse.app.domain.model.SurveyPhoto
import com.parkpulse.app.domain.model.SyncStatus
import com.parkpulse.app.domain.repository.ParkRepository
import com.parkpulse.app.domain.repository.SurveyRepository
import com.parkpulse.app.presentation.camera.PhotoDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

const val MAX_SURVEY_PHOTOS = 5

data class SurveyFormUiState(
    /** False when the check-in data in the navigation arguments is missing or unreadable. */
    val argsValid: Boolean = true,
    val parkName: String = "",
    val checkInTimeMillis: Long = 0L,
    val checkInAccuracyM: Float = 0f,
    val trees: String = "",
    /** "Lights: Yes/No" from the design. When Yes, [lights] holds the count (>= 1). */
    val lightsOn: Boolean = false,
    val lights: String = "",
    val cleanliness: Cleanliness? = null,
    val notes: String = "",
    val photos: List<PhotoDraft> = emptyList(),
    val showErrors: Boolean = false,
    val isSaving: Boolean = false,
) {
    val treesError: String? get() = if (showErrors && trees.isBlank()) "Enter the number of trees (0 if none)" else null
    val lightsError: String?
        get() = if (showErrors && lightsOn && (lights.toIntOrNull() ?: 0) < 1) "Enter how many lights (at least 1)" else null
    val cleanlinessError: String? get() = if (showErrors && cleanliness == null) "Choose a cleanliness status" else null
    val canAddPhoto: Boolean get() = photos.size < MAX_SURVEY_PHOTOS
}

sealed interface SurveyFormEvent {
    object Saved : SurveyFormEvent
    data class Failed(val message: String) : SurveyFormEvent
}

@HiltViewModel
class SurveyFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    parkRepository: ParkRepository,
    private val surveyRepository: SurveyRepository,
    private val photoStorage: PhotoStorage,
) : ViewModel() {

    private val parkId: String? = savedStateHandle.get<String>("parkId")
    private val checkInTime: Long? = savedStateHandle.get<Long>("checkInTime")
    private val latitude: Double? = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
    private val longitude: Double? = savedStateHandle.get<String>("lng")?.toDoubleOrNull()
    private val accuracy: Float? = savedStateHandle.get<String>("accuracy")?.toFloatOrNull()

    /** One id per form instance: saving twice (double tap) can never create two rows. */
    private val surveyId = "survey-${UUID.randomUUID()}"

    private val argsValid =
        parkId != null && checkInTime != null && latitude != null && longitude != null && accuracy != null

    /** Photos taken for this form survive process death through the handle (files are on disk). */
    private val restoredPhotos: List<PhotoDraft> =
        savedStateHandle.get<ArrayList<String>>(KEY_PHOTOS).orEmpty().mapNotNull(PhotoDraft::decode)

    private val _uiState = MutableStateFlow(
        SurveyFormUiState(
            argsValid = argsValid,
            checkInTimeMillis = checkInTime ?: 0L,
            checkInAccuracyM = accuracy ?: 0f,
            photos = restoredPhotos,
        )
    )
    val uiState: StateFlow<SurveyFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<SurveyFormEvent>(Channel.BUFFERED)
    val events: Flow<SurveyFormEvent> = _events.receiveAsFlow()

    private var saved = false

    init {
        viewModelScope.launch {
            val name = parkRepository.observeParks().first().firstOrNull { it.id == parkId }?.name
            _uiState.update { it.copy(parkName = name ?: "") }
        }
        // The camera screen hands its result back through this survey screen's saved state.
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>(KEY_NEW_PHOTO, null).filterNotNull().collect { encoded ->
                savedStateHandle[KEY_NEW_PHOTO] = null
                PhotoDraft.decode(encoded)?.let(::addPhoto)
            }
        }
    }

    fun photoFile(fileName: String): File = photoStorage.fileFor(fileName)

    private fun addPhoto(draft: PhotoDraft) {
        if (_uiState.value.photos.size >= MAX_SURVEY_PHOTOS) {
            photoStorage.delete(draft.fileName)
            return
        }
        _uiState.update { it.copy(photos = it.photos + draft) }
        persistPhotos()
    }

    fun removePhoto(photoId: String) {
        val removed = _uiState.value.photos.firstOrNull { it.id == photoId } ?: return
        _uiState.update { s -> s.copy(photos = s.photos.filterNot { it.id == photoId }) }
        persistPhotos()
        photoStorage.delete(removed.fileName)
    }

    private fun persistPhotos() {
        savedStateHandle[KEY_PHOTOS] = ArrayList(_uiState.value.photos.map { it.encode() })
    }

    fun onTreesChange(value: String) {
        if (value.length <= MAX_COUNT_DIGITS && value.all(Char::isDigit)) _uiState.update { it.copy(trees = value) }
    }

    fun onTreesStep(delta: Int) = _uiState.update { s ->
        if (s.trees.isBlank() && delta < 0) s
        else s.copy(trees = ((s.trees.toIntOrNull() ?: 0) + delta).coerceIn(0, MAX_COUNT).toString())
    }

    fun onLightsToggle() = _uiState.update { s ->
        val on = !s.lightsOn
        s.copy(lightsOn = on, lights = if (on && s.lights.isBlank()) "1" else s.lights)
    }

    fun onLightsChange(value: String) {
        if (value.length <= MAX_COUNT_DIGITS && value.all(Char::isDigit)) _uiState.update { it.copy(lights = value) }
    }

    fun onLightsStep(delta: Int) = _uiState.update { s ->
        s.copy(lights = ((s.lights.toIntOrNull() ?: 0) + delta).coerceIn(1, MAX_COUNT).toString())
    }

    fun onCleanlinessChange(value: Cleanliness) = _uiState.update { it.copy(cleanliness = value) }

    fun onNotesChange(value: String) {
        if (value.length <= MAX_NOTES) _uiState.update { it.copy(notes = value) }
    }

    fun save() {
        val s = _uiState.value
        if (!argsValid || s.isSaving) return
        val trees = s.trees.toIntOrNull()
        val lights = if (s.lightsOn) s.lights.toIntOrNull()?.takeIf { it >= 1 } else 0
        val cleanliness = s.cleanliness
        if (trees == null || lights == null || cleanliness == null) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }

        val now = System.currentTimeMillis()
        val survey = Survey(
            id = surveyId,
            parkId = checkNotNull(parkId),
            checkInTimeMillis = checkNotNull(checkInTime),
            checkInLatitude = checkNotNull(latitude),
            checkInLongitude = checkNotNull(longitude),
            checkInAccuracyM = checkNotNull(accuracy),
            treeCount = trees,
            lightCount = lights,
            cleanliness = cleanliness,
            notes = s.notes.trim(),
            createdAtMillis = now,
            syncStatus = SyncStatus.PENDING, // offline-first: the sync layer uploads it later
        )
        val photos = s.photos.map {
            SurveyPhoto(
                id = it.id,
                surveyId = surveyId,
                fileName = it.fileName,
                takenAtMillis = it.takenAtMillis,
                latitude = it.latitude,
                longitude = it.longitude,
                accuracyM = it.accuracyM,
                createdAtMillis = now,
                syncStatus = SyncStatus.PENDING,
            )
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                surveyRepository.saveSurvey(survey, photos) // one transaction: survey + photo records
                saved = true
                _events.send(SurveyFormEvent.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(SurveyFormEvent.Failed("Could not save the survey. Please try again."))
            }
        }
    }

    /** Leaving the form without saving discards the draft photo files so they do not pile up. */
    override fun onCleared() {
        if (!saved) _uiState.value.photos.forEach { photoStorage.delete(it.fileName) }
    }

    companion object {
        const val KEY_NEW_PHOTO = "new_photo"
        private const val KEY_PHOTOS = "draft_photos"
        private const val MAX_COUNT_DIGITS = 5
        private const val MAX_COUNT = 99_999
        private const val MAX_NOTES = 500
    }
}
