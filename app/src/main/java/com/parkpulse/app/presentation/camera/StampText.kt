package com.parkpulse.app.presentation.camera

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

data class StampData(
    val parkName: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    val takenAtMillis: Long,
)

/** Text shown both on the live preview overlay and burned into the saved JPEG, so they always match. */
object StampText {
    fun coordinates(lat: Double, lng: Double, accuracyM: Float): String =
        String.format(
            Locale.US, "%.4f %s, %.4f %s ±%d m",
            abs(lat), if (lat >= 0) "N" else "S",
            abs(lng), if (lng >= 0) "E" else "W",
            accuracyM.roundToInt(),
        )

    fun dateTime(millis: Long, zone: TimeZone = TimeZone.getDefault()): String =
        SimpleDateFormat("d MMM yyyy, HH:mm z", Locale.ENGLISH).apply { timeZone = zone }.format(Date(millis))

    fun lines(data: StampData, zone: TimeZone = TimeZone.getDefault()): List<String> = listOf(
        data.parkName,
        coordinates(data.latitude, data.longitude, data.accuracyM),
        dateTime(data.takenAtMillis, zone),
    )
}
