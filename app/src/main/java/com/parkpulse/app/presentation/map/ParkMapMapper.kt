package com.parkpulse.app.presentation.map

import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.gis.PolygonUtils

data class MappedParks(
    val parks: List<MapPark>,
    val missingBoundary: Int,
    val invalidBoundary: Int,
)

/** Domain Park -> drawable MapPark. Parks without a usable boundary are counted, never invented. */
fun List<Park>.toMappedParks(): MappedParks {
    val drawable = ArrayList<MapPark>(size)
    var missing = 0
    var invalid = 0
    for (park in this) {
        val boundary = park.boundary
        if (boundary.isNullOrEmpty()) {
            missing++
            continue
        }
        val ring = PolygonUtils.normalizeRing(boundary.map { GeoPoint(it.latitude, it.longitude) })
        if (ring == null) {
            invalid++
        } else {
            drawable += MapPark(
                id = park.id,
                name = park.name,
                status = park.status,
                ring = ring,
                areaSquareMeters = park.areaSqM,
            )
        }
    }
    return MappedParks(drawable, missing, invalid)
}