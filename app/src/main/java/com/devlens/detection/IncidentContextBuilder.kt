package com.devlens.detection

import com.devlens.data.entities.IncidentEntity
import com.devlens.data.entities.TelemetrySample
import java.util.UUID

/**
 * Builds a structured IncidentContext from a detected incident.
 * This is the data package sent to the evidence correlator.
 */
class IncidentContextBuilder {

    data class IncidentContext(
        val incidentId: String,
        val sessionId: String,
        val type: IncidentType,
        val severity: IncidentSeverity,
        val startTime: Long,
        val endTime: Long,
        val baseline: BaselineMetrics,
        val peakFps: Float,
        val peakCpu: Float,
        val peakMemoryMb: Float,
        val peakFrameTimeMs: Float,
        val timeline: List<TelemetrySample>,
        val triggerScenario: String,
        // Derived statistics
        val fpsFallPercent: Float,
        val cpuRisePercent: Float,
        val memoryRiseMb: Float,
        val durationSeconds: Float
    ) {
        fun toEntity(): IncidentEntity = IncidentEntity(
            id = incidentId,
            sessionId = sessionId,
            type = type.name,
            severity = severity.name,
            startTime = startTime,
            endTime = endTime,
            baselineFps = baseline.fps,
            baselineCpu = baseline.cpuPercent,
            baselineMemoryMb = baseline.memoryMb,
            peakFps = peakFps,
            peakCpu = peakCpu,
            peakMemoryMb = peakMemoryMb,
            triggerScenario = triggerScenario
        )
    }

    fun build(
        incident: IncidentDetector.DetectedIncident,
        allSamples: List<TelemetrySample>,
        baseline: BaselineMetrics,
        sessionId: String,
        triggerScenario: String = "UNKNOWN"
    ): IncidentContext {
        val timeline = incident.triggeringSamples
        val startTime = timeline.firstOrNull()?.timestamp ?: System.currentTimeMillis()
        val endTime = timeline.lastOrNull()?.timestamp ?: startTime

        val peakFps = timeline.minOfOrNull { it.fps } ?: baseline.fps
        val peakCpu = timeline.maxOfOrNull { it.cpuPercent } ?: baseline.cpuPercent
        val peakMemory = timeline.maxOfOrNull { it.memoryMb } ?: baseline.memoryMb
        val peakFrameTime = timeline.maxOfOrNull { it.frameTimeMs } ?: baseline.frameTimeMs

        val fpsFallPercent = if (baseline.fps > 0)
            ((baseline.fps - peakFps) / baseline.fps * 100f).coerceAtLeast(0f)
        else 0f

        val cpuRise = (peakCpu - baseline.cpuPercent).coerceAtLeast(0f)
        val memRise = (peakMemory - baseline.memoryMb).coerceAtLeast(0f)
        val duration = (endTime - startTime) / 1000f

        return IncidentContext(
            incidentId = "INC-${UUID.randomUUID().toString().take(8).uppercase()}",
            sessionId = sessionId,
            type = incident.type,
            severity = incident.severity,
            startTime = startTime,
            endTime = endTime,
            baseline = baseline,
            peakFps = peakFps,
            peakCpu = peakCpu,
            peakMemoryMb = peakMemory,
            peakFrameTimeMs = peakFrameTime,
            timeline = timeline,
            triggerScenario = triggerScenario,
            fpsFallPercent = fpsFallPercent,
            cpuRisePercent = cpuRise,
            memoryRiseMb = memRise,
            durationSeconds = duration
        )
    }
}
