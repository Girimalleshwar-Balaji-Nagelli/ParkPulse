package com.parkpulse.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.parkpulse.app.data.local.converter.Converters
import com.parkpulse.app.data.local.dao.ParkDao
import com.parkpulse.app.data.local.entity.ParkEntity

@Database(entities = [ParkEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ParkDatabase : RoomDatabase() {
    abstract fun parkDao(): ParkDao
}