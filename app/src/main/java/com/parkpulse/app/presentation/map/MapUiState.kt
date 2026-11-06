package com.parkpulse.app.presentation.map

import android.location.Location
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.gis.CompassDirection
import com.parkpulse.app.gis.GeoPoint

/** A park that has a verified, renderable boundary. [ring] is open (first point not repeated). */
data class MapPark(
    val id: String,
    val name: String,
    val status: ParkStatus,
    val ring: List<GeoPoint>,
    val areaSquareMeters: Double,
)

data class SelectedParkInfo(
    val id: String,
    val name: String,
    val status: ParkStatus,
    val areaSquareMeters: Double,
    /** Metres to the nearest point on the park boundary. Null when location is unknown. */
    val distanceMeters: Double?,
    val isUserInside: Boolean,
    /** Bearing from the user to the nearest boundary point. Null when inside or location unknown. */
    val bearingDegrees: Double?,
    val direction: CompassDirection?,
)

data class MapUiState(
    val isLoading: Boolean = true,
    val parks: List<MapPark> = emptyList(),
    /** Parks whose Park.boundary is null (no verified boundary yet). Not drawn. */
    val missingBoundaryCount: Int = 0,
    /** Parks with a boundary that is unusable (bad coordinates, <3 points, zero area). Not drawn. */
    val invalidBoundaryCount: Int = 0,
    val userLocation: Location? = null,
    val hasLocationPermission: Boolean = false,
    val selectedParkId: String? = null,
    val selected: SelectedParkInfo? = null,
)