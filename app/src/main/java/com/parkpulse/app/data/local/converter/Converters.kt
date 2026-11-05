package com.parkpulse.app.data.local.converter

import androidx.room.TypeConverter
import com.parkpulse.app.domain.model.LatLng
import org.json.JSONArray
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun boundaryToJson(boundary: List<LatLng>?): String? = boundary?.let { points ->
        JSONArray().also { arr ->
            points.forEach { arr.put(JSONArray().put(it.latitude).put(it.longitude)) }
        }.toString()
    }

    @TypeConverter
    fun jsonToBoundary(json: String?): List<LatLng>? = json?.let {
        val arr = JSONArray(it)
        List(arr.length()) { i ->
            val p = arr.getJSONArray(i)
            LatLng(p.getDouble(0), p.getDouble(1))
        }
    }

    @TypeConverter
    fun dateToString(date: LocalDate): String = date.toString()

    @TypeConverter
    fun stringToDate(value: String): LocalDate = LocalDate.parse(value)
}