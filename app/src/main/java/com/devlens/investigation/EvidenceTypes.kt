package com.devlens.investigation

/**
 * Standard evidence vocabulary for the DevLens investigation engine.
 *
 * Every piece of evidence fed to the AI must be typed and traceable.
 * The AI should not receive raw telemetry — only structured evidence.
 */

enum class EvidenceType {
    METRIC,              // A measured value (fps=23, cpu=94%)
    CORRELATION,         // A relationship observed between two signals
    TIMELINE,            // A sequence of events over time
    BASELINE_COMPARISON, // Current vs. baseline delta
    PREVIOUS_EXPERIENCE  // From hindsight memory
}

data class Evidence(
    val id: String,
    val type: EvidenceType,
    val statement: String,
    val numericValue: Double? = null,
    val timestamp: Long? = null
)

data class Hypothesis(
    val id: String,
    val cause: String,
    val supportingEvidence: List<String>,
    val contradictingEvidence: List<String>,
    val missingEvidence: List<String>,
    val confidenceLabel: String  // "supported", "possible", "unlikely"
)

data class InvestigationResult(
    val summary: String,
    val hypotheses: List<Hypothesis>,
    val missingEvidence: List<String>,
    val recommendedAction: String,
    val verificationMetric: String,
    val rawResponse: String      // Full LLM output for debugging
)
