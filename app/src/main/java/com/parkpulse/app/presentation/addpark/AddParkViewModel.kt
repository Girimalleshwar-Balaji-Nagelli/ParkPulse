package com.parkpulse.app.presentation.addpark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.domain.model.SyncStatus
import com.parkpulse.app.domain.usecase.AddParkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
class AddParkViewModel @Inject constructor(
    private val addPark: AddParkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddParkUiState())
    val uiState: StateFlow<AddParkUiState> = _state.asStateFlow()

    private val _saved = Channel<Unit>(Channel.BUFFERED)
    /** Emits once after a park has been written to Room. */
    val saved: Flow<Unit> = _saved.receiveAsFlow()

    fun onNameChange(value: String) = _state.update {
        it.copy(name = value.take(MAX_NAME_LENGTH), nameError = null, saveError = null)
    }

    fun onTypeChange(value: ParkType) = _state.update {
        it.copy(type = value, typeError = null, saveError = null)
    }

    fun onAreaChange(value: String) = _state.update {
        it.copy(areaText = sanitizeArea(value), areaError = null, saveError = null)
    }

    fun onStatusChange(value: ParkStatus) = _state.update {
        it.copy(status = value, statusError = null, saveError = null)
    }

    fun onDateChange(value: LocalDate) = _state.update {
        it.copy(surveyDate = value, dateError = null, saveError = null)
    }

    fun onSave() {
        val s = _state.value
        if (s.isSaving) return

        val name = s.name.trim()
        val area = s.areaText.toDoubleOrNull()

        val nameError = when {
            name.isEmpty() -> "Enter a park name"
            name.length < 2 -> "Name is too short"
            else -> null
        }
        val typeError = if (s.type == null) "Select a park type" else null
        val areaError = when {
            s.areaText.isBlank() -> "Enter the area in square metres"
            area == null -> "Enter a valid number"
            area <= 0.0 -> "Area must be greater than 0"
            area > MAX_AREA_SQM -> "Area is too large"
            else -> null
        }
        val statusError = if (s.status == null) "Select a status" else null
        val dateError = when {
            s.surveyDate == null -> "Select the last survey date"
            s.surveyDate.isAfter(LocalDate.now()) -> "Date cannot be in the future"
            else -> null
        }

        if (listOf(nameError, typeError, areaError, statusError, dateError).any { it != null }) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    typeError = typeError,
                    areaError = areaError,
                    statusError = statusError,
                    dateError = dateError
                )
            }
            return
        }

        val park = Park(
            id = UUID.randomUUID().toString(),
            name = name,
            type = s.type!!,
            areaSqM = area!!,
            boundary = null,
            lastSurveyDate = s.surveyDate!!,
            status = s.status!!,
            syncStatus = SyncStatus.PENDING
        )

        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                addPark(park)
                _saved.send(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(isSaving = false, saveError = "Couldn't save the park. Please try again.")
                }
            }
        }
    }

    /** Keeps digits and a single decimal point only. */
    private fun sanitizeArea(input: String): String {
        var dotSeen = false
        return buildString {
            for (c in input) {
                when {
                    c.isDigit() -> append(c)
                    c == '.' && !dotSeen -> { dotSeen = true; append(c) }
                }
            }
        }.take(12)
    }

    private companion object {
        const val MAX_NAME_LENGTH = 100
        const val MAX_AREA_SQM = 100_000_000.0
    }
}