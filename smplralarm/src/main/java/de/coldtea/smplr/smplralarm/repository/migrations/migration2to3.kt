package de.coldtea.smplr.smplralarm.repository.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE alarm_notification_table ADD COLUMN date Long NOT NULL DEFAULT 0 ")
    }
}