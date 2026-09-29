package com.devlens.detection

/**
 * Configurable thresholds for incident detection.
 * All detection is deterministic — no LLM involvement.
 *
 * NOTE: DevLens measures its OWN process CPU (via /proc/self/stat).
 * On an 8-core phone, one stressed core ≈ 12% of total.
 * We therefore use RELATIVE thresholds (multiplier over baseline)
 * as the primary signal, with low absolute floors as secondary.
 */
data class ThresholdConfig(
    // FPS thresholds
    val fpsDropThreshold: Float = 45f,          // Below 45 FPS = incident
    val fpsSevereDropThreshold: Float = 30f,    // Below 30 FPS = severe
    val fpsDropConsecutiveSamples: Int = 2,     // Must sustain 2 samples (~1s)

    // Frame time thresholds (ms)
    val frameTimeJankMs: Float = 33.3f,         // 1 dropped frame at 60fps
    val frameTimeSevereMs: Float = 50f,         // 3x expected

    // CPU thresholds — RELATIVE to baseline
    val cpuHighPercent: Float = 15f,            // Absolute floor: >15% process CPU
    val cpuCriticalPercent: Float = 40f,        // Absolute critical: >40%
    val cpuRelativeMultiplier: Float = 2.5f,    // 2.5× baseline = anomaly
    val cpuConsecutiveSamples: Int = 2,

    // Memory thresholds
    val memoryGrowthMb: Float = 30f,            // 30MB growth = concern
    val memoryGrowthWindowSamples: Int = 6,     // Over 6 samples (~3s)

    // Correlated incident
    val correlationFpsThreshold: Float = 45f,
    val correlationCpuMultiplier: Float = 2.0f,

    // Baseline calculation window
    val baselineWindowSamples: Int = 6          // First 6 samples (~3s) = baseline
)

enum class IncidentType {
    FRAME_DROP,
    HIGH_CPU,
    MEMORY_GROWTH,
    COMBINED_DEGRADATION
}

enum class IncidentSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
