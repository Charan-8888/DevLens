package com.devlens.investigation

import android.content.Context
import com.devlens.ai.LocalLlm
import com.devlens.data.AppDatabase
import com.devlens.data.entities.InvestigationEntity
import com.devlens.detection.IncidentContextBuilder.IncidentContext
import com.devlens.hindsight.HindsightEngine
import java.util.UUID

/**
 * Orchestrates the full investigation pipeline:
 *   IncidentContext → Evidence → Hindsight → Prompt → LLM → InvestigationResult → Persist
 */
class InvestigationEngine(private val context: Context) {

    private val correlator = EvidenceCorrelator()
    private val promptBuilder = PromptBuilder()
    private val parser = AiResponseParser()
    private val llm = LocalLlm(context)
    private val hindsightEngine = HindsightEngine(context)
    private val db = AppDatabase.getInstance(context)

    // Expose LLM state for UI progress
    val llmState = llm.state

    data class InvestigationPackage(
        val id: String,
        val incidentContext: IncidentContext,
        val evidence: List<Evidence>,
        val result: InvestigationResult,
        val hadPreviousExperiences: Boolean,
        val previousExperienceCount: Int
    )

    suspend fun investigate(incidentContext: IncidentContext): InvestigationPackage {
        val investigationId = "INV-${UUID.randomUUID().toString().take(8).uppercase()}"

        // 1. Correlate evidence from the incident context
        val evidence = correlator.correlate(incidentContext)

        // 2. Retrieve similar previous experiences
        val experiences = hindsightEngine.findSimilar(incidentContext)

        // 3. Inject previous experience as PREVIOUS_EXPERIENCE evidence items
        val experienceEvidence = experiences.mapIndexed { i, exp ->
            Evidence(
                id = "EXP-${i + 1}",
                type = EvidenceType.PREVIOUS_EXPERIENCE,
                statement = "Previous similar incident (${exp.incidentType}): " +
                    "Fix '${exp.recommendedFix}' → Outcome: ${exp.outcome}. " +
                    "Lesson: ${exp.lesson}"
            )
        }
        val allEvidence = evidence + experienceEvidence

        // 4. Build the structured prompt
        val prompt = promptBuilder.buildInvestigationPrompt(incidentContext, allEvidence, experiences)

        // 5. Run local LLM inference
        val llmResult = llm.investigate(prompt)
        val rawResponse = llmResult.getOrNull()?.text ?: buildFallbackResponse(incidentContext, allEvidence)

        // 6. Parse the response into structured output
        val investigationResult = parser.parse(rawResponse, allEvidence, incidentContext)

        // 7. Persist to database
        val entity = InvestigationEntity(
            id = investigationId,
            incidentId = incidentContext.incidentId,
            summary = investigationResult.summary,
            hypothesesJson = investigationResult.hypotheses.toString(),
            missingEvidenceJson = investigationResult.missingEvidence.toString(),
            recommendedAction = investigationResult.recommendedAction,
            verificationMetric = investigationResult.verificationMetric,
            confidenceLabel = investigationResult.hypotheses.firstOrNull()?.confidenceLabel ?: "unknown",
            rawAiResponse = rawResponse
        )
        db.investigationDao().insert(entity)

        return InvestigationPackage(
            id = investigationId,
            incidentContext = incidentContext,
            evidence = allEvidence,
            result = investigationResult,
            hadPreviousExperiences = experiences.isNotEmpty(),
            previousExperienceCount = experiences.size
        )
    }

    /** Fallback when LLM fails - still shows deterministic evidence */
    private fun buildFallbackResponse(context: IncidentContext, evidence: List<Evidence>): String {
        val topEvidence = evidence.take(4).joinToString("; ") { it.statement }
        return """
SUMMARY: Performance incident detected via deterministic analysis (AI inference unavailable).

HYPOTHESIS_1:
  CAUSE: ${when {
    context.peakCpu > 85f -> "Main-thread CPU overload"
    context.peakFrameTimeMs > 33f -> "Rendering workload exceeds frame budget"
    context.memoryRiseMb > 50f -> "Memory pressure and GC events"
    else -> "Performance degradation under investigation"
  }}
  SUPPORT: $topEvidence
  CONFIDENCE: possible

MISSING: Thread-level CPU profiling data

ACTION: ${when {
    context.peakCpu > 85f -> "Move expensive computation off the main thread."
    context.peakFrameTimeMs > 33f -> "Profile the rendering pipeline for expensive draw calls."
    context.memoryRiseMb > 50f -> "Reduce object allocation rate during frame-critical code paths."
    else -> "Use Android Profiler to capture a trace during the scenario."
}}

VERIFY_BY: FPS and frame time after applying fix
        """.trimIndent()
    }
}
