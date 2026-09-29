package com.devlens.detection

import com.devlens.data.entities.TelemetrySample

/**
 * Deterministic incident detector.
 *
 * IMPORTANT: This class uses only arithmetic thresholds.
 * The LLM is never called here. It only receives structured output from this class.
 */
class IncidentDetector(private val config: ThresholdConfig = ThresholdConfig()) {

    data class DetectedIncident(
        val type: IncidentType,
        val severity: IncidentSeverity,
        val startIndex: Int,       // index into samples list
        val triggeringSamples: List<TelemetrySample>,
        val description: String
    )

    /**
     * Evaluates the sliding window of samples and returns an incident if detected.
     * Returns null if no incident is present.
     *
     * Must be called with at least [config.baselineWindowSamples + config.fpsDropConsecutiveSamples] samples.
     */
    fun evaluate(
        samples: List<TelemetrySample>,
        baseline: BaselineMetrics
    ): DetectedIncident? {
        if (samples.size < config.baselineWindowSamples + 2) return null

        // Check in priority order: combined > frame drop > CPU > memory
        return detectCombined(samples, baseline)
            ?: detectFrameDrop(samples, baseline)
            ?: detectHighCpu(samples, baseline)
            ?: detectMemoryGrowth(samples, baseline)
    }

    private fun detectFrameDrop(
        samples: List<TelemetrySample>,
        baseline: BaselineMetrics
    ): DetectedIncident? {
        val recent = samples.takeLast(config.fpsDropConsecutiveSamples + 2)
        val lowFpsSamples = recent.filter { it.fps < config.fpsDropThreshold }

        if (lowFpsSamples.size < config.fpsDropConsecutiveSamples) return null

        val worstFps = lowFpsSamples.minOf { it.fps }
        val severity = when {
            worstFps < config.fpsSevereDropThreshold -> IncidentSeverity.CRITICAL
            worstFps < config.fpsDropThreshold -> IncidentSeverity.HIGH
            else -> IncidentSeverity.MEDIUM
        }

        return DetectedIncident(
            type = IncidentType.FRAME_DROP,
            severity = severity,
            startIndex = samples.size - recent.size,
            triggeringSamples = lowFpsSamples,
            description = "FPS dropped from ${baseline.fps.toInt()} to ${worstFps.toInt()} " +
                "(${((baseline.fps - worstFps) / baseline.fps * 100).toInt()}% drop)"
        )
    }

    private fun detectHighCpu(
        samples: List<TelemetrySample>,
        baseline: BaselineMetrics
    ): DetectedIncident? {
        val recent = samples.takeLast(config.cpuConsecutiveSamples + 2)
        val highCpuSamples = recent.filter { it.cpuPercent > config.cpuHighPercent }

        if (highCpuSamples.size < config.cpuConsecutiveSamples) return null

        val peakCpu = highCpuSamples.maxOf { it.cpuPercent }
        val severity = when {
            peakCpu > config.cpuCriticalPercent -> IncidentSeverity.CRITICAL
            peakCpu > config.cpuHighPercent -> IncidentSeverity.HIGH
            else -> IncidentSeverity.MEDIUM
        }

        return DetectedIncident(
            type = IncidentType.HIGH_CPU,
            severity = severity,
            startIndex = samples.size - recent.size,
            triggeringSamples = highCpuSamples,
            description = "CPU spiked from ${baseline.cpuPercent.toInt()}% to ${peakCpu.toInt()}%"
        )
    }

    private fun detectMemoryGrowth(
        samples: List<TelemetrySample>,
        baseline: BaselineMetrics
    ): DetectedIncident? {
        if (samples.size < config.memoryGrowthWindowSamples) return null

        val window = samples.takeLast(config.memoryGrowthWindowSamples)
        val currentMem = window.last().memoryMb
        val growth = currentMem - baseline.memoryMb

        if (growth < config.memoryGrowthMb) return null

        val severity = when {
            growth > config.memoryGrowthMb * 3 -> IncidentSeverity.HIGH
            growth > config.memoryGrowthMb * 1.5f -> IncidentSeverity.MEDIUM
            else -> IncidentSeverity.LOW
        }

        return DetectedIncident(
            type = IncidentType.MEMORY_GROWTH,
            severity = severity,
            startIndex = samples.size - window.size,
            triggeringSamples = window,
            description = "Memory grew by ${growth.toInt()}MB " +
                "(${baseline.memoryMb.toInt()}MB → ${currentMem.toInt()}MB)"
        )
    }

    private fun detectCombined(
        samples: List<TelemetrySample>,
        baseline: BaselineMetrics
    ): DetectedIncident? {
        val recent = samples.takeLast(config.fpsDropConsecutiveSamples + 2)

        val hasFrameDrop = recent.count { it.fps < config.correlationFpsThreshold } >= 2
        val hasHighCpu = recent.count { it.cpuPercent > config.correlationCpuThreshold } >= 2

        if (!hasFrameDrop || !hasHighCpu) return null

        val worstFps = recent.minOf { it.fps }
        val peakCpu = recent.maxOf { it.cpuPercent }

        return DetectedIncident(
            type = IncidentType.COMBINED_DEGRADATION,
            severity = IncidentSeverity.CRITICAL,
            startIndex = samples.size - recent.size,
            triggeringSamples = recent,
            description = "Combined degradation: FPS ${baseline.fps.toInt()}→${worstFps.toInt()}, " +
                "CPU ${baseline.cpuPercent.toInt()}%→${peakCpu.toInt()}%"
        )
    }
}

data class BaselineMetrics(
    val fps: Float,
    val frameTimeMs: Float,
    val cpuPercent: Float,
    val memoryMb: Float
) {
    companion object {
        /** Calculate baseline from the first N samples of a session */
        fun fromSamples(samples: List<TelemetrySample>): BaselineMetrics {
            if (samples.isEmpty()) return BaselineMetrics(60f, 16.67f, 0f, 0f)
            return BaselineMetrics(
                fps = samples.map { it.fps }.average().toFloat(),
                frameTimeMs = samples.map { it.frameTimeMs }.average().toFloat(),
                cpuPercent = samples.map { it.cpuPercent }.average().toFloat(),
                memoryMb = samples.map { it.memoryMb }.average().toFloat()
            )
        }
    }
}
