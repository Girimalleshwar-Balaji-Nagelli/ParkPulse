package com.parkpulse.app.presentation.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.repository.ParkRepository
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.gis.PolygonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SurveyParkItem(
    val id: String,
    val name: String,
    val typeLabel: String,
    val status: ParkStatus,
    /** False when the park has no usable boundary: geofenced check-in is impossible. */
    val canSurvey: Boolean,
)

@HiltViewModel
class SurveyParkListViewModel @Inject constructor(
    parkRepository: ParkRepository,
) : ViewModel() {

    /** null = still loading. */
    val items: StateFlow<List<SurveyParkItem>?> = parkRepository.observeParks()
        .map { parks ->
            parks.map { park ->
                val ring = park.boundary
                    ?.map { GeoPoint(it.latitude, it.longitude) }
                    ?.let(PolygonUtils::normalizeRing)
                SurveyParkItem(park.id, park.name, park.type.label, park.status, canSurvey = ring != null)
            }.sortedWith(compareByDescending<SurveyParkItem> { it.canSurvey }.thenBy { it.name.lowercase() })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
