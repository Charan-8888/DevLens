package com.devlens.detection

/**
 * Configurable thresholds for incident detection.
 * All detection is deterministic — no LLM involvement.
 */
data class ThresholdConfig(
    // FPS thresholds
    val fpsDropThreshold: Float = 30f,          // Below 30 FPS = incident
    val fpsSevereDropThreshold: Float = 20f,    // Below 20 FPS = severe
    val fpsDropConsecutiveSamples: Int = 3,     // Must sustain for 3 samples (~1.5s)

    // Frame time thresholds (ms)
    val frameTimeJankMs: Float = 33.3f,         // 1 dropped frame at 60fps
    val frameTimeSevereMs: Float = 50f,         // 3x expected

    // CPU thresholds
    val cpuHighPercent: Float = 80f,            // High CPU
    val cpuCriticalPercent: Float = 90f,        // Critical CPU
    val cpuConsecutiveSamples: Int = 3,

    // Memory thresholds
    val memoryGrowthMb: Float = 50f,            // 50MB growth = concern
    val memoryGrowthWindowSamples: Int = 10,    // Over 10 samples (~5s)

    // Correlated incident
    val correlationFpsThreshold: Float = 35f,
    val correlationCpuThreshold: Float = 75f,

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
