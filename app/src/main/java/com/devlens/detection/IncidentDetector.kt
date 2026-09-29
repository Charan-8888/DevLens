package com.devlens.detection

import com.devlens.data.entities.TelemetrySample

/**
 * Deterministic incident detector.
 *
 * IMPORTANT: This class uses only arithmetic thresholds.
 * The LLM is never called here. It only receives structured output from this class.
 *
 * CPU detection uses RELATIVE thresholds (multiplier over baseline) because
 * DevLens measures its own process CPU — on a multi-core device a stressed
 * process rarely hits 80% absolute even when it is genuinely hammering the CPU.
 */
class IncidentDetector(private val config: ThresholdConfig = ThresholdConfig()) {

    data class DetectedIncident(
        val type: IncidentType,
        val severity: IncidentSeverity,
        val startIndex: Int,
        val triggeringSamples: List<TelemetrySample>,
        val description: String
    )

    /**
     * Evaluates the sliding window of samples and returns an incident if detected.
     * Returns null if no incident is present.
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

        // Also require it's a genuine drop from baseline (not just low baseline device)
        val worstFps = lowFpsSamples.minOf { it.fps }
        val fpsDrop = baseline.fps - worstFps
        if (fpsDrop < 10f) return null  // must drop at least 10fps from baseline

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

        // PRIMARY: relative threshold — CPU rose to 2.5× baseline or more
        val baselineCpu = baseline.cpuPercent.coerceAtLeast(0.5f)  // avoid div/0
        val relativeThreshold = baselineCpu * config.cpuRelativeMultiplier

        // SECONDARY: absolute floor — must be above 15% to rule out noise
        val highCpuSamples = recent.filter { sample ->
            sample.cpuPercent > config.cpuHighPercent &&           // above absolute floor
            sample.cpuPercent > relativeThreshold                   // AND above relative threshold
        }

        if (highCpuSamples.size < config.cpuConsecutiveSamples) return null

        val peakCpu = highCpuSamples.maxOf { it.cpuPercent }
        val severity = when {
            peakCpu > config.cpuCriticalPercent -> IncidentSeverity.CRITICAL
            peakCpu > relativeThreshold * 1.5f -> IncidentSeverity.HIGH
            else -> IncidentSeverity.MEDIUM
        }

        return DetectedIncident(
            type = IncidentType.HIGH_CPU,
            severity = severity,
            startIndex = samples.size - recent.size,
            triggeringSamples = highCpuSamples,
            description = "CPU spiked from ${baseline.cpuPercent.toInt()}% to ${peakCpu.toInt()}% " +
                "(${(peakCpu / baselineCpu).toInt()}× baseline)"
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
        val baselineCpu = baseline.cpuPercent.coerceAtLeast(0.5f)

        val hasFrameDrop = recent.count { it.fps < config.correlationFpsThreshold } >= 2
        val hasHighCpu = recent.count {
            it.cpuPercent > config.cpuHighPercent &&
            it.cpuPercent > baselineCpu * config.correlationCpuMultiplier
        } >= 2

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
        fun fromSamples(samples: List<TelemetrySample>): BaselineMetrics {
            if (samples.isEmpty()) return BaselineMetrics(60f, 16.67f, 1f, 0f)
            return BaselineMetrics(
                fps = samples.map { it.fps }.average().toFloat(),
                frameTimeMs = samples.map { it.frameTimeMs }.average().toFloat(),
                cpuPercent = samples.map { it.cpuPercent }.average().toFloat().coerceAtLeast(0.5f),
                memoryMb = samples.map { it.memoryMb }.average().toFloat()
            )
        }
    }
}
