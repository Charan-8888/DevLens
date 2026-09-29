package com.devlens.hindsight

import android.content.Context
import com.devlens.data.AppDatabase
import com.devlens.data.entities.ExperienceEntity
import com.devlens.detection.IncidentContextBuilder.IncidentContext
import com.devlens.investigation.InvestigationEngine.InvestigationPackage
import com.devlens.verification.VerificationEngine
import java.util.UUID

/**
 * HindsightEngine stores and retrieves investigation experiences.
 *
 * Core responsibilities:
 * 1. Save a complete experience (incident + fix + outcome + lesson)
 * 2. Find similar past experiences for new incidents
 * 3. Score similarity deterministically (no embeddings needed for MVP)
 */
class HindsightEngine(private val context: Context) {

    private val db = AppDatabase.getInstance(context)

    data class SimilarityScore(
        val experience: ExperienceEntity,
        val score: Int,        // 0-100
        val reasons: List<String>
    )

    /**
     * Saves a verified investigation experience to the database.
     * Called after verification is complete.
     */
    suspend fun saveExperience(
        investigation: InvestigationPackage,
        verification: VerificationEngine.VerificationResult,
        developerAction: String
    ) {
        val ctx = investigation.incidentContext
        val lesson = buildLesson(investigation, verification)
        val primaryHypothesis = investigation.result.hypotheses.firstOrNull()?.cause ?: "Unknown"

        val experience = ExperienceEntity(
            id = "EXP-${UUID.randomUUID().toString().take(8).uppercase()}",
            incidentId = ctx.incidentId,
            incidentType = ctx.type.name,
            symptomsJson = buildSymptomsJson(ctx),
            initialHypothesesJson = investigation.result.hypotheses.map { it.cause }.toString(),
            recommendedFix = investigation.result.recommendedAction,
            developerAction = developerAction,
            beforeFps = verification.before.fps,
            beforeCpu = verification.before.cpuPercent,
            beforeFrameTimeMs = verification.before.frameTimeMs,
            afterFps = verification.after.fps,
            afterCpu = verification.after.cpuPercent,
            afterFrameTimeMs = verification.after.frameTimeMs,
            outcome = verification.outcome.name,
            lesson = lesson,
            // Fingerprint fields
            fpsFallPercent = ctx.fpsFallPercent,
            cpuSpikePresent = ctx.peakCpu > 80f,
            memorySpikePresent = ctx.memoryRiseMb > 30f,
            jankPresent = ctx.timeline.any { it.jankDetected },
            durationSeconds = ctx.durationSeconds,
            triggerScenario = ctx.triggerScenario
        )

        db.experienceDao().insert(experience)
    }

    /**
     * Finds experiences similar to the current incident using deterministic scoring.
     * Returns up to 3 most relevant past experiences.
     */
    suspend fun findSimilar(context: IncidentContext): List<ExperienceEntity> {
        val allExperiences = db.experienceDao().getAll()
        if (allExperiences.isEmpty()) return emptyList()

        return allExperiences
            .map { scoreExperience(it, context) }
            .filter { it.score >= 30 }  // Minimum relevance threshold
            .sortedByDescending { it.score }
            .take(3)
            .map { it.experience }
    }

    /**
     * Scores similarity between a stored experience and a new incident.
     * Uses explicit field matching — no embeddings.
     *
     * Maximum score: 100
     */
    private fun scoreExperience(experience: ExperienceEntity, current: IncidentContext): SimilarityScore {
        var score = 0
        val reasons = mutableListOf<String>()

        // Same incident type: +30 points
        if (experience.incidentType == current.type.name) {
            score += 30
            reasons += "Same incident type (${current.type.name})"
        }

        // Similar FPS drop percentage: up to +20 points
        val fpsDiff = Math.abs(experience.fpsFallPercent - current.fpsFallPercent)
        val fpsScore = when {
            fpsDiff < 10f -> 20
            fpsDiff < 25f -> 12
            fpsDiff < 40f -> 6
            else -> 0
        }
        score += fpsScore
        if (fpsScore > 0) reasons += "Similar FPS degradation (${experience.fpsFallPercent.toInt()}% vs ${current.fpsFallPercent.toInt()}%)"

        // CPU spike match: +20 points
        val cpuMatch = experience.cpuSpikePresent == (current.peakCpu > 80f)
        if (cpuMatch) {
            score += 20
            reasons += "CPU spike pattern matches"
        }

        // Memory spike match: +10 points
        val memMatch = experience.memorySpikePresent == (current.memoryRiseMb > 30f)
        if (memMatch) {
            score += 10
            reasons += "Memory pattern matches"
        }

        // Same trigger scenario: +20 points
        if (experience.triggerScenario == current.triggerScenario &&
            current.triggerScenario != "UNKNOWN") {
            score += 20
            reasons += "Same trigger scenario (${current.triggerScenario})"
        }

        return SimilarityScore(experience, score.coerceAtMost(100), reasons)
    }

    private fun buildLesson(
        investigation: InvestigationPackage,
        verification: VerificationEngine.VerificationResult
    ): String {
        val ctx = investigation.incidentContext
        return when (verification.outcome) {
            VerificationEngine.Outcome.SUCCESS ->
                "The incident (${ctx.type.name.replace('_', ' ')}) was resolved by: " +
                    "${investigation.result.recommendedAction}. " +
                    "FPS recovered from ${verification.before.fps.toInt()} to ${verification.after.fps.toInt()}."
            VerificationEngine.Outcome.PARTIAL ->
                "Partial improvement achieved with: ${investigation.result.recommendedAction}. " +
                    "FPS improved but further investigation may be needed."
            VerificationEngine.Outcome.FAILED ->
                "The fix '${investigation.result.recommendedAction}' did NOT resolve the incident. " +
                    "This hypothesis (${investigation.result.hypotheses.firstOrNull()?.cause}) " +
                    "may not be the root cause."
            VerificationEngine.Outcome.WORSE ->
                "The applied change made performance worse. " +
                    "Avoid this approach for this incident pattern."
            VerificationEngine.Outcome.INCONCLUSIVE ->
                "Results were inconclusive for: ${investigation.result.recommendedAction}. " +
                    "More controlled testing is needed."
        }
    }

    private fun buildSymptomsJson(ctx: IncidentContext): String {
        val symptoms = mutableListOf<String>()
        if (ctx.fpsFallPercent > 20f) symptoms += "FPS fell ${ctx.fpsFallPercent.toInt()}%"
        if (ctx.peakCpu > 80f) symptoms += "CPU reached ${ctx.peakCpu.toInt()}%"
        if (ctx.memoryRiseMb > 30f) symptoms += "Memory grew ${ctx.memoryRiseMb.toInt()} MB"
        if (ctx.peakFrameTimeMs > 33f) symptoms += "Frame time ${ctx.peakFrameTimeMs.toInt()} ms"
        return symptoms.toString()
    }
}
