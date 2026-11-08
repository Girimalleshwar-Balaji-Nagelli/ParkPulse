package com.parkpulse.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the `survey_photos` table. Run it AFTER SurveyMigration, e.g. if surveys were added in
 * N -> N+1, use SurveyPhotoMigration.create(from = N + 1, to = N + 2). Existing data is untouched.
 */
object SurveyPhotoMigration {
    fun create(from: Int, to: Int): Migration = object : Migration(from, to) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `survey_photos` (" +
                    "`id` TEXT NOT NULL, " +
                    "`surveyId` TEXT NOT NULL, " +
                    "`fileName` TEXT NOT NULL, " +
                    "`takenAtMillis` INTEGER NOT NULL, " +
                    "`latitude` REAL NOT NULL, " +
                    "`longitude` REAL NOT NULL, " +
                    "`accuracyM` REAL NOT NULL, " +
                    "`createdAtMillis` INTEGER NOT NULL, " +
                    "`syncStatus` TEXT NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_survey_photos_surveyId` ON `survey_photos` (`surveyId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_survey_photos_syncStatus` ON `survey_photos` (`syncStatus`)")
        }
    }
}
