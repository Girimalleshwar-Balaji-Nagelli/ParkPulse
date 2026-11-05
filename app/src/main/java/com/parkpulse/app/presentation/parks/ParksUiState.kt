package com.parkpulse.app.presentation.parks

import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.model.ParkStatus

enum class StatusFilter(val label: String, val status: ParkStatus?) {
    ALL("All", null),
    OPEN("Open", ParkStatus.OPEN),
    NEEDS_REPAIR("Needs repair", ParkStatus.NEEDS_REPAIR),
    CLOSED("Closed", ParkStatus.CLOSED);

    fun matches(parkStatus: ParkStatus) = status == null || status == parkStatus
}

data class ParksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val parks: List<Park> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
    val filter: StatusFilter = StatusFilter.ALL
) {
    val isEmpty: Boolean get() = !isLoading && error == null && totalCount == 0
    val noResults: Boolean get() = !isLoading && error == null && totalCount > 0 && parks.isEmpty()
}