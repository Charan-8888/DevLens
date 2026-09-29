package com.devlens.investigation

import com.devlens.detection.IncidentContextBuilder.IncidentContext
import com.devlens.detection.IncidentType
import java.util.UUID

/**
 * Converts an IncidentContext into a structured list of Evidence objects.
 *
 * This is the critical transformation before the LLM receives data.
 * Raw numbers become typed, labeled, human-readable evidence statements.
 *
 * The AI receives ONLY these evidence items — never raw telemetry lists.
 */
class EvidenceCorrelator {

    fun correlate(context: IncidentContext): List<Evidence> {
        val evidence = mutableListOf<Evidence>()
        var counter = 1

        fun id() = "EVD-${counter++.toString().padStart(3, '0')}"

        // === METRIC EVIDENCE ===

        // Baseline measurements
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Baseline FPS: ${context.baseline.fps.toInt()} fps (stable before incident)",
            numericValue = context.baseline.fps.toDouble()
        )
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Baseline CPU: ${context.baseline.cpuPercent.toInt()}% (normal workload)",
            numericValue = context.baseline.cpuPercent.toDouble()
        )
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Baseline memory: ${context.baseline.memoryMb.toInt()} MB",
            numericValue = context.baseline.memoryMb.toDouble()
        )

        // Peak incident measurements
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Peak FPS during incident: ${context.peakFps.toInt()} fps",
            numericValue = context.peakFps.toDouble()
        )
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Peak CPU during incident: ${context.peakCpu.toInt()}%",
            numericValue = context.peakCpu.toDouble()
        )
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Peak memory during incident: ${context.peakMemoryMb.toInt()} MB",
            numericValue = context.peakMemoryMb.toDouble()
        )
        evidence += Evidence(
            id = id(), type = EvidenceType.METRIC,
            statement = "Peak frame time: ${String.format("%.1f", context.peakFrameTimeMs)} ms " +
                "(target: 16.7 ms for 60 fps)",
            numericValue = context.peakFrameTimeMs.toDouble()
        )

        // === BASELINE COMPARISON EVIDENCE ===

        if (context.fpsFallPercent > 5f) {
            evidence += Evidence(
                id = id(), type = EvidenceType.BASELINE_COMPARISON,
                statement = "FPS fell by ${context.fpsFallPercent.toInt()}%: " +
                    "${context.baseline.fps.toInt()} → ${context.peakFps.toInt()} fps",
                numericValue = context.fpsFallPercent.toDouble()
            )
        }

        if (context.cpuRisePercent > 10f) {
            val cpuRisePercent = if (context.baseline.cpuPercent > 0)
                (context.cpuRisePercent / context.baseline.cpuPercent * 100f) else 0f
            evidence += Evidence(
                id = id(), type = EvidenceType.BASELINE_COMPARISON,
                statement = "CPU rose by ${context.cpuRisePercent.toInt()} percentage points: " +
                    "${context.baseline.cpuPercent.toInt()}% → ${context.peakCpu.toInt()}%",
                numericValue = context.cpuRisePercent.toDouble()
            )
        }

        if (context.memoryRiseMb > 10f) {
            evidence += Evidence(
                id = id(), type = EvidenceType.BASELINE_COMPARISON,
                statement = "Memory grew by ${context.memoryRiseMb.toInt()} MB: " +
                    "${context.baseline.memoryMb.toInt()} → ${context.peakMemoryMb.toInt()} MB",
                numericValue = context.memoryRiseMb.toDouble()
            )
        }

        // Frame time comparison
        val frameTimeIncrease = context.peakFrameTimeMs - context.baseline.frameTimeMs
        if (frameTimeIncrease > 5f) {
            evidence += Evidence(
                id = id(), type = EvidenceType.BASELINE_COMPARISON,
                statement = "Frame time increased by ${frameTimeIncrease.toInt()} ms: " +
                    "${context.baseline.frameTimeMs.toInt()} → ${context.peakFrameTimeMs.toInt()} ms"
            )
        }

        // === CORRELATION EVIDENCE ===

        // CPU + FPS correlation
        if (context.peakCpu > 80f && context.fpsFallPercent > 30f) {
            val timingNote = if (context.cpuRisePercent > 20f && context.fpsFallPercent > 20f)
                "CPU elevation and frame delivery failure occurred together"
            else "CPU was elevated during the frame rate degradation period"
            evidence += Evidence(
                id = id(), type = EvidenceType.CORRELATION,
                statement = "CPU elevation (${context.peakCpu.toInt()}%) is concurrent with " +
                    "frame delivery failure (${context.peakFps.toInt()} fps). $timingNote."
            )
        }

        // Memory + FPS correlation
        if (context.memoryRiseMb > 30f && context.fpsFallPercent > 20f) {
            evidence += Evidence(
                id = id(), type = EvidenceType.CORRELATION,
                statement = "Memory growth of ${context.memoryRiseMb.toInt()} MB coincided with FPS degradation. " +
                    "GC events may be contributing to frame-delivery latency."
            )
        }

        // Incident duration
        evidence += Evidence(
            id = id(), type = EvidenceType.TIMELINE,
            statement = "Incident duration: ${String.format("%.1f", context.durationSeconds)} seconds " +
                "(${context.triggerScenario.replace('_', ' ')} scenario)"
        )

        // Jank detection
        val jankSamples = context.timeline.count { it.jankDetected }
        if (jankSamples > 0) {
            evidence += Evidence(
                id = id(), type = EvidenceType.METRIC,
                statement = "Jank detected in $jankSamples of ${context.timeline.size} samples " +
                    "(frame time > 33.3 ms)"
            )
        }

        // Incident type context
        val typeNote = when (context.type) {
            IncidentType.FRAME_DROP -> "Primary signal: sustained FPS below 30. Frame delivery is failing."
            IncidentType.HIGH_CPU -> "Primary signal: process CPU sustained above 80%. Main thread may be overloaded."
            IncidentType.MEMORY_GROWTH -> "Primary signal: significant memory growth detected. GC pressure is likely."
            IncidentType.COMBINED_DEGRADATION ->
                "Multiple signals concurrent: both frame delivery and CPU utilization are abnormal simultaneously."
        }
        evidence += Evidence(
            id = id(), type = EvidenceType.TIMELINE,
            statement = typeNote
        )

        return evidence
    }
}
