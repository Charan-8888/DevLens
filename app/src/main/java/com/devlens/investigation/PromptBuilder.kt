package com.devlens.investigation

import com.devlens.data.entities.ExperienceEntity
import com.devlens.detection.IncidentContextBuilder.IncidentContext
import com.devlens.detection.IncidentType

/**
 * Builds the LLM prompt from structured evidence.
 *
 * Key design principle: the LLM receives structured facts, not raw data.
 * Every claim in the prompt is traceable to an Evidence object.
 */
class PromptBuilder {

    /**
     * Builds a structured investigation prompt for the local model.
     * Includes system instructions, evidence facts, and previous experiences if available.
     */
    fun buildInvestigationPrompt(
        context: IncidentContext,
        evidence: List<Evidence>,
        previousExperiences: List<ExperienceEntity> = emptyList()
    ): String {
        val sb = StringBuilder()

        // System instruction
        sb.appendLine("You are the DevLens investigation engine for Android performance analysis.")
        sb.appendLine("You receive structured evidence only. Do not invent metrics, logs, or code.")
        sb.appendLine("Your task: identify likely causes, list missing evidence, and recommend one specific action.")
        sb.appendLine()

        // Incident header
        sb.appendLine("=== INCIDENT ===")
        sb.appendLine("Type: ${context.type.name.replace('_', ' ')}")
        sb.appendLine("Severity: ${context.severity.name}")
        sb.appendLine("Trigger: ${context.triggerScenario.replace('_', ' ')}")
        sb.appendLine()

        // Evidence section
        sb.appendLine("=== EVIDENCE (${evidence.size} items) ===")
        evidence.forEachIndexed { i, ev ->
            sb.appendLine("[${ev.id}] ${ev.type.name}: ${ev.statement}")
        }
        sb.appendLine()

        // Previous experience section (if any)
        if (previousExperiences.isNotEmpty()) {
            sb.appendLine("=== PREVIOUS SIMILAR EXPERIENCES ===")
            previousExperiences.take(2).forEachIndexed { i, exp ->
                sb.appendLine("Experience ${i + 1}:")
                sb.appendLine("  Incident type: ${exp.incidentType}")
                sb.appendLine("  Fix attempted: ${exp.recommendedFix}")
                sb.appendLine("  Developer action: ${exp.developerAction}")
                sb.appendLine("  Before FPS: ${exp.beforeFps.toInt()}, After FPS: ${exp.afterFps.toInt()}")
                sb.appendLine("  Outcome: ${exp.outcome}")
                sb.appendLine("  Lesson: ${exp.lesson}")
                sb.appendLine()
            }
        }

        // Output format instruction
        sb.appendLine("=== OUTPUT FORMAT ===")
        sb.appendLine("Respond with this exact structure (keep each section short):")
        sb.appendLine()
        sb.appendLine("SUMMARY: [One sentence describing the most likely performance problem]")
        sb.appendLine()
        sb.appendLine("HYPOTHESIS_1:")
        sb.appendLine("  CAUSE: [likely cause]")
        sb.appendLine("  SUPPORT: [evidence IDs and brief reason]")
        sb.appendLine("  CONFIDENCE: supported|possible|unlikely")
        sb.appendLine()
        sb.appendLine("HYPOTHESIS_2:")
        sb.appendLine("  CAUSE: [alternative cause]")
        sb.appendLine("  SUPPORT: [evidence IDs and brief reason]")
        sb.appendLine("  CONFIDENCE: supported|possible|unlikely")
        sb.appendLine()
        sb.appendLine("MISSING: [what additional evidence would help confirm or rule out causes]")
        sb.appendLine()
        sb.appendLine("ACTION: [one specific developer action to investigate or fix this]")
        sb.appendLine()
        sb.appendLine("VERIFY_BY: [which metric to check after applying the fix]")

        return sb.toString()
    }

    /** Shorter prompt for quick re-analysis with different emphasis */
    fun buildFollowUpPrompt(
        context: IncidentContext,
        evidence: List<Evidence>,
        failedHypothesis: String
    ): String {
        val sb = StringBuilder()
        sb.appendLine("You are the DevLens investigation engine. A previous hypothesis was incorrect.")
        sb.appendLine("Failed hypothesis: $failedHypothesis")
        sb.appendLine()
        sb.appendLine("=== INCIDENT ===")
        sb.appendLine("Type: ${context.type.name.replace('_', ' ')}, Severity: ${context.severity.name}")
        sb.appendLine()
        sb.appendLine("=== EVIDENCE ===")
        evidence.take(8).forEach { ev ->
            sb.appendLine("[${ev.id}] ${ev.statement}")
        }
        sb.appendLine()
        sb.appendLine("Given that '$failedHypothesis' did not improve performance, " +
            "identify the ALTERNATIVE most likely cause. " +
            "Format: SUMMARY / HYPOTHESIS / ACTION / VERIFY_BY")
        return sb.toString()
    }
}
