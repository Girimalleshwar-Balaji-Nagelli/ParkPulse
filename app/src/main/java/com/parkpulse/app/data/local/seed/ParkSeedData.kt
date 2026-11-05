package com.parkpulse.app.data.local.seed

import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkStatus.CLOSED
import com.parkpulse.app.domain.model.ParkStatus.NEEDS_REPAIR
import com.parkpulse.app.domain.model.ParkStatus.OPEN
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.domain.model.ParkType.FOREST_PARK
import com.parkpulse.app.domain.model.ParkType.GARDEN
import com.parkpulse.app.domain.model.ParkType.LAKEFRONT
import com.parkpulse.app.domain.model.ParkType.PLAYGROUND
import com.parkpulse.app.domain.model.SyncStatus
import java.time.LocalDate

/**
 * Pune park seed data.
 *
 * DATA STATUS:
 *  - Names: real Pune parks/green spaces.
 *  - boundary: MISSING (null) for every park. Real polygons must be sourced
 *    (e.g. OpenStreetMap / PMC GIS) and filled in; they are not fabricated here.
 *  - type, areaSqM, lastSurveyDate, status, syncStatus: PLACEHOLDER values for UI development.
 */
object ParkSeedData {

    private fun park(
        id: Int, name: String, type: ParkType, area: Double,
        survey: String, status: ParkStatus, pending: Boolean = false
    ) = Park(
        id = "pune-park-%03d".format(id),
        name = name,
        type = type,
        areaSqM = area,
        boundary = null,
        lastSurveyDate = LocalDate.parse(survey),
        status = status,
        syncStatus = if (pending) SyncStatus.PENDING else SyncStatus.SYNCED
    )

    val parks: List<Park> = listOf(
        park(1, "Sambhaji Park", GARDEN, 40000.0, "2026-09-12", OPEN),
        park(2, "Saras Baug", GARDEN, 45000.0, "2026-08-30", OPEN),
        park(3, "Pu La Deshpande Udyan", GARDEN, 100000.0, "2026-09-02", OPEN),
        park(4, "Empress Garden", GARDEN, 160000.0, "2026-07-21", NEEDS_REPAIR, pending = true),
        park(5, "Bund Garden", GARDEN, 52000.0, "2026-08-15", OPEN),
        park(6, "Peshwe Park", PLAYGROUND, 24000.0, "2026-06-18", NEEDS_REPAIR),
        park(7, "Kamla Nehru Park", PLAYGROUND, 18000.0, "2026-09-25", OPEN),
        park(8, "Osho Teerth Park", GARDEN, 15000.0, "2026-05-09", CLOSED),
        park(9, "Joggers Park Kalyani Nagar", PLAYGROUND, 9000.0, "2026-10-03", OPEN, pending = true),
        park(10, "Pashan Lake", LAKEFRONT, 210000.0, "2026-08-04", OPEN),
        park(11, "Katraj Lake", LAKEFRONT, 180000.0, "2026-07-11", NEEDS_REPAIR),
        park(12, "Khadakwasla Lakefront", LAKEFRONT, 320000.0, "2026-06-27", CLOSED, pending = true),
        park(13, "Vetal Tekdi", FOREST_PARK, 1500000.0, "2026-09-19", OPEN),
        park(14, "Taljai Tekdi", FOREST_PARK, 900000.0, "2026-08-22", OPEN),
        park(15, "ARAI Hill (Hanuman Tekdi)", FOREST_PARK, 700000.0, "2026-07-05", NEEDS_REPAIR),
        park(16, "Pashan-Sus Biodiversity Park", FOREST_PARK, 400000.0, "2026-10-08", OPEN),
        park(17, "Bhamburda Van Udyan", FOREST_PARK, 600000.0, "2026-05-30", CLOSED),
        park(18, "Parvati Hill", FOREST_PARK, 250000.0, "2026-09-07", OPEN),
        park(19, "Baner Hill", FOREST_PARK, 800000.0, "2026-08-11", NEEDS_REPAIR),
        park(20, "Rajiv Gandhi Udyan (Katraj)", GARDEN, 130000.0, "2026-10-14", OPEN),
        park(21, "Shaniwar Wada Lawns", GARDEN, 11000.0, "2026-06-02", OPEN)
    )
}