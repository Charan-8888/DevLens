package com.devlens.data.dao

import androidx.room.*
import com.devlens.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sample: TelemetrySample)

    @Query("SELECT * FROM telemetry_samples WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSamplesForSession(sessionId: String): List<TelemetrySample>

    @Query("SELECT * FROM telemetry_samples WHERE sessionId = :sessionId AND timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun getSamplesInWindow(sessionId: String, from: Long, to: Long): List<TelemetrySample>

    @Query("DELETE FROM telemetry_samples WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}

@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incident: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY startTime DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getById(id: String): IncidentEntity?

    @Query("SELECT * FROM incidents ORDER BY startTime DESC LIMIT 20")
    suspend fun getRecent(): List<IncidentEntity>
}

@Dao
interface InvestigationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investigation: InvestigationEntity)

    @Query("SELECT * FROM investigations WHERE incidentId = :incidentId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForIncident(incidentId: String): InvestigationEntity?

    @Query("SELECT * FROM investigations WHERE id = :id")
    suspend fun getById(id: String): InvestigationEntity?
}

@Dao
interface VerificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: VerificationRunEntity)

    @Query("SELECT * FROM verification_runs WHERE incidentId = :incidentId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForIncident(incidentId: String): VerificationRunEntity?
}

@Dao
interface ExperienceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(experience: ExperienceEntity)

    @Query("SELECT * FROM experiences ORDER BY createdAt DESC")
    fun getAllExperiences(): Flow<List<ExperienceEntity>>

    @Query("SELECT * FROM experiences WHERE incidentType = :type ORDER BY createdAt DESC")
    suspend fun getByIncidentType(type: String): List<ExperienceEntity>

    @Query("SELECT * FROM experiences ORDER BY createdAt DESC LIMIT 50")
    suspend fun getAll(): List<ExperienceEntity>

    @Query("SELECT COUNT(*) FROM experiences")
    suspend fun count(): Int
}
