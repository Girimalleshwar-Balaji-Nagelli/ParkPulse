package com.parkpulse.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the `surveys` table without touching existing data.
 * Usage: .addMigrations(SurveyMigration.create(from = <current version>, to = <current version + 1>))
 * The SQL matches SurveyEntity exactly (Room validates it when the database opens).
 */
object SurveyMigration {
    fun create(from: Int, to: Int): Migration = object : Migration(from, to) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `surveys` (" +
                    "`id` TEXT NOT NULL, " +
                    "`parkId` TEXT NOT NULL, " +
                    "`checkInTimeMillis` INTEGER NOT NULL, " +
                    "`checkInLatitude` REAL NOT NULL, " +
                    "`checkInLongitude` REAL NOT NULL, " +
                    "`checkInAccuracyM` REAL NOT NULL, " +
                    "`treeCount` INTEGER NOT NULL, " +
                    "`lightCount` INTEGER NOT NULL, " +
                    "`cleanliness` TEXT NOT NULL, " +
                    "`notes` TEXT NOT NULL, " +
                    "`createdAtMillis` INTEGER NOT NULL, " +
                    "`syncStatus` TEXT NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_surveys_parkId` ON `surveys` (`parkId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_surveys_syncStatus` ON `surveys` (`syncStatus`)")
        }
    }
}
