package com.devlens.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devlens.data.dao.*
import com.devlens.data.entities.*

@Database(
    entities = [
        TelemetrySample::class,
        IncidentEntity::class,
        InvestigationEntity::class,
        VerificationRunEntity::class,
        ExperienceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
    abstract fun incidentDao(): IncidentDao
    abstract fun investigationDao(): InvestigationDao
    abstract fun verificationDao(): VerificationDao
    abstract fun experienceDao(): ExperienceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devlens.db"
                ).build().also { INSTANCE = it }
            }
    }
}
