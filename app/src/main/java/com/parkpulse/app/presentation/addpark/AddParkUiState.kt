package com.parkpulse.app.presentation.addpark

import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import java.time.LocalDate

data class AddParkUiState(
    val name: String = "",
    val type: ParkType? = null,
    val areaText: String = "",
    val status: ParkStatus? = null,
    val surveyDate: LocalDate? = null,
    val nameError: String? = null,
    val typeError: String? = null,
    val areaError: String? = null,
    val statusError: String? = null,
    val dateError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null
)