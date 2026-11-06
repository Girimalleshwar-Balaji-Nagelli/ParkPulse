
package com.parkpulse.app.data.local.seed

import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkStatus.*
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.domain.model.ParkType.*
import com.parkpulse.app.domain.model.LatLng
import com.parkpulse.app.domain.model.SyncStatus
import java.time.LocalDate

object ParkSeedData {

    private fun park(
        id: Int,
        name: String,
        type: ParkType,
        area: Double,
        survey: String,
        status: ParkStatus,
        boundary: List<LatLng>? = null,
        pending: Boolean = false
    ) = Park(
        id = "pune-park-%03d".format(id),
        name = name,
        type = type,
        areaSqM = area,
        boundary = boundary,
        lastSurveyDate = LocalDate.parse(survey),
        status = status,
        syncStatus = if (pending) SyncStatus.PENDING
        else SyncStatus.SYNCED
    )

    val parks: List<Park> = listOf(
        park(1, "Sambhaji Park", GARDEN, 40000.0, "2026-09-12", OPEN,
            boundary = ParkBoundaries.sambhajiPark),

        park(2, "Saras Baug", GARDEN, 45000.0, "2026-08-30", OPEN,
            boundary = ParkBoundaries.sarasBaug),

        park(3, "Pu La Deshpande Udyan", GARDEN, 100000.0, "2026-09-02", OPEN,
            boundary = ParkBoundaries.puLaDeshpandeUdyan),

        park(4, "Empress Garden", GARDEN, 160000.0, "2026-07-21", NEEDS_REPAIR,
            boundary = ParkBoundaries.empressGarden, pending = true),

        park(5, "Bund Garden", GARDEN, 52000.0, "2026-08-15", OPEN,
            boundary = ParkBoundaries.bundGarden),

        park(6, "Peshwe Park", PLAYGROUND, 24000.0, "2026-06-18", NEEDS_REPAIR,
            boundary = ParkBoundaries.peshwePark),

        park(7, "Kamla Nehru Park", PLAYGROUND, 18000.0, "2026-09-25", OPEN,
            boundary = ParkBoundaries.kamlaNehruPark),

        park(8, "Osho Teerth Park", GARDEN, 15000.0, "2026-05-09", CLOSED,
            boundary = ParkBoundaries.oshoTeerthPark),

        park(9, "Joggers Park Kalyani Nagar", PLAYGROUND, 9000.0, "2026-10-03", OPEN,
            boundary = ParkBoundaries.joggersPark, pending = true),

        park(10, "Pashan Lake", LAKEFRONT, 210000.0, "2026-08-04", OPEN,
            boundary = ParkBoundaries.pashanLake),

        park(11, "Katraj Lake", LAKEFRONT, 180000.0, "2026-07-11", NEEDS_REPAIR,
            boundary = ParkBoundaries.katrajLake),

        park(12, "Khadakwasla Lakefront", LAKEFRONT, 320000.0, "2026-06-27", CLOSED,
            boundary = ParkBoundaries.khadakwaslaLakefront, pending = true),

        park(13, "Vetal Tekdi", FOREST_PARK, 1500000.0, "2026-09-19", OPEN,
            boundary = ParkBoundaries.vetalTekdi),

        park(14, "Taljai Tekdi", FOREST_PARK, 900000.0, "2026-08-22", OPEN,
            boundary = ParkBoundaries.taljaiTekdi),

        park(15, "ARAI Hill (Hanuman Tekdi)", FOREST_PARK, 700000.0, "2026-07-05", NEEDS_REPAIR,
            boundary = ParkBoundaries.araiHill),

        park(16, "Pashan-Sus Biodiversity Park", FOREST_PARK, 400000.0, "2026-10-08", OPEN,
            boundary = ParkBoundaries.pashanSusPark),

        park(17, "Bhamburda Van Udyan", FOREST_PARK, 600000.0, "2026-05-30", CLOSED,
            boundary = ParkBoundaries.bhamburdaVanUdyan),

        park(18, "Parvati Hill", FOREST_PARK, 250000.0, "2026-09-07", OPEN,
            boundary = ParkBoundaries.parvatiHill),

        park(19, "Baner Hill", FOREST_PARK, 800000.0, "2026-08-11", NEEDS_REPAIR,
            boundary = ParkBoundaries.banerHill),

        park(20, "Rajiv Gandhi Udyan (Katraj)", GARDEN, 130000.0, "2026-10-14", OPEN,
            boundary = ParkBoundaries.rajivGandhiUdyan),

        park(21, "Shaniwar Wada Lawns", GARDEN, 11000.0, "2026-06-02", OPEN,
            boundary = ParkBoundaries.shaniwarWadaLawns)
    )
}
