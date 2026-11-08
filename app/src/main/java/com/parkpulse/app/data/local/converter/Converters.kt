package com.parkpulse.app.data.local.converter

import androidx.room.TypeConverter
import com.parkpulse.app.domain.model.LatLng
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.domain.model.SyncStatus
import org.json.JSONArray
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun boundaryToJson(boundary: List<LatLng>?): String? {
        return boundary?.let { points ->
            JSONArray().apply {
                points.forEach { point ->
                    put(
                        JSONArray()
                            .put(point.latitude)
                            .put(point.longitude)
                    )
                }
            }.toString()
        }
    }

    @TypeConverter
    fun jsonToBoundary(json: String?): List<LatLng>? {
        return json?.let {
            val array = JSONArray(it)
            List(array.length()) { index ->
                val point = array.getJSONArray(index)
                LatLng(
                    point.getDouble(0),
                    point.getDouble(1)
                )
            }
        }
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun stringToDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun parkTypeToString(value: ParkType): String = value.name

    @TypeConverter
    fun stringToParkType(value: String): ParkType =
        ParkType.valueOf(value)

    @TypeConverter
    fun parkStatusToString(value: ParkStatus): String = value.name

    @TypeConverter
    fun stringToParkStatus(value: String): ParkStatus =
        ParkStatus.valueOf(value)

    @TypeConverter
    fun syncStatusToString(value: SyncStatus): String = value.name

    @TypeConverter
    fun stringToSyncStatus(value: String): SyncStatus =
        SyncStatus.valueOf(value)
}