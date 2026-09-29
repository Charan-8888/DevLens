package com.devlens.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One telemetry sample collected every ~500ms during monitoring.
 * All values are from real Android APIs — no simulation.
 */
@Entity(tableName = "telemetry_samples")
data class TelemetrySample(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestamp: Long,         // System.currentTimeMillis()
    val fps: Float,              // Measured from Choreographer callbacks
    val frameTimeMs: Float,      // Average frame duration in ms
    val cpuPercent: Float,       // Process CPU % from /proc/self/stat
    val memoryMb: Float,         // PSS in MB from ActivityManager
    val jankDetected: Boolean    // frameTimeMs > 33.3ms (2x 16.67ms)
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val type: String,            // FRAME_DROP, CPU_SPIKE, MEMORY_GROWTH, COMBINED
    val severity: String,        // LOW, MEDIUM, HIGH, CRITICAL
    val startTime: Long,
    val endTime: Long,
    val baselineFps: Float,
    val baselineCpu: Float,
    val baselineMemoryMb: Float,
    val peakFps: Float,
    val peakCpu: Float,
    val peakMemoryMb: Float,
    val triggerScenario: String = "UNKNOWN"
)

@Entity(tableName = "investigations")
data class InvestigationEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val summary: String,
    val hypothesesJson: String,   // JSON array of Hypothesis
    val missingEvidenceJson: String,
    val recommendedAction: String,
    val verificationMetric: String,
    val confidenceLabel: String,
    val rawAiResponse: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "verification_runs")
data class VerificationRunEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val investigationId: String,
    val beforeFps: Float,
    val beforeCpu: Float,
    val beforeMemoryMb: Float,
    val beforeFrameTimeMs: Float,
    val afterFps: Float,
    val afterCpu: Float,
    val afterMemoryMb: Float,
    val afterFrameTimeMs: Float,
    val outcome: String,          // SUCCESS, PARTIAL, FAILED, WORSE, INCONCLUSIVE
    val fpsImprovementPercent: Float,
    val cpuReductionPercent: Float,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "experiences")
data class ExperienceEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val incidentType: String,
    val symptomsJson: String,           // JSON list of symptom strings
    val initialHypothesesJson: String,  // JSON list
    val recommendedFix: String,
    val developerAction: String,        // What the dev actually did
    val beforeFps: Float,
    val beforeCpu: Float,
    val beforeFrameTimeMs: Float,
    val afterFps: Float,
    val afterCpu: Float,
    val afterFrameTimeMs: Float,
    val outcome: String,                // SUCCESS/PARTIAL/FAILED/WORSE/INCONCLUSIVE
    val lesson: String,
    // Fingerprint fields for similarity matching
    val fpsFallPercent: Float,
    val cpuSpikePresent: Boolean,
    val memorySpikePresent: Boolean,
    val jankPresent: Boolean,
    val durationSeconds: Float,
    val triggerScenario: String,
    val createdAt: Long = System.currentTimeMillis()
)
