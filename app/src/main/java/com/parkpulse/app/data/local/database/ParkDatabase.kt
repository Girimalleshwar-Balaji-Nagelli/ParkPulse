package com.parkpulse.app.data.local.database

import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.os.Build.VERSION_CODES.N
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.parkpulse.app.data.local.converter.Converters
import com.parkpulse.app.data.local.dao.ParkDao
import com.parkpulse.app.data.local.dao.SurveyDao
import com.parkpulse.app.data.local.entity.ParkEntity
import com.parkpulse.app.data.local.entity.SurveyEntity
import com.parkpulse.app.data.local.entity.SurveyPhotoEntity

//@Database(entities = [ParkEntity::class, SurveyEntity::class,], version = 1, exportSchema = false)
@Database(
    entities = [
        ParkEntity::class,
        SurveyEntity::class,
        SurveyPhotoEntity::class,   // add (keep any other entities you already list)
    ],
    version =  2,                // V = the version your phone's database currently has
    exportSchema = false// leave exportSchema as it is, and keep your existing @TypeConverters
)
@TypeConverters(Converters::class)
abstract class ParkDatabase : RoomDatabase() {
    abstract fun parkDao(): ParkDao
    abstract fun surveyDao(): SurveyDao
}