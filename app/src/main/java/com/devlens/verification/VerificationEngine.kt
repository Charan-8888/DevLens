package com.devlens.verification

import android.content.Context
import com.devlens.data.AppDatabase
import com.devlens.data.entities.VerificationRunEntity
import com.devlens.detection.IncidentContextBuilder.IncidentContext
import java.util.UUID

/**
 * Verification engine: captures before/after metrics and classifies outcome.
 *
 * The outcome classification is 100% deterministic — threshold math, no LLM.
 */
class VerificationEngine(private val context: Context) {

    private val db = AppDatabase.getInstance(context)

    enum class Outcome {
        SUCCESS,     // Clear improvement across primary metrics
        PARTIAL,     // Some improvement but not full recovery
        FAILED,      // No meaningful improvement
        WORSE,       // Metrics regressed
        INCONCLUSIVE // Insufficient change to determine
    }

    data class BeforeMetrics(
        val fps: Float,
        val cpuPercent: Float,
        val memoryMb: Float,
        val frameTimeMs: Float
    )

    data class AfterMetrics(
        val fps: Float,
        val cpuPercent: Float,
        val memoryMb: Float,
        val frameTimeMs: Float
    )

    data class VerificationResult(
        val id: String,
        val before: BeforeMetrics,
        val after: AfterMetrics,
        val outcome: Outcome,
        val fpsImprovementPercent: Float,
        val cpuReductionPercent: Float,
        val frameTimeReductionPercent: Float,
        val summary: String
    )

    /**
     * Compares before and after metrics and determines outcome.
     * Does NOT use LLM for this decision.
     */
    suspend fun verify(
        incidentContext: IncidentContext,
        investigationId: String,
        before: BeforeMetrics,
        after: AfterMetrics
    ): VerificationResult {
        val fpsImprovement = if (before.fps > 0f)
            ((after.fps - before.fps) / before.fps * 100f) else 0f
        val cpuReduction = before.cpuPercent - after.cpuPercent
        val cpuReductionPct = if (before.cpuPercent > 0f)
            (cpuReduction / before.cpuPercent * 100f) else 0f
        val frameTimeReduction = if (before.frameTimeMs > 0f)
            ((before.frameTimeMs - after.frameTimeMs) / before.frameTimeMs * 100f) else 0f

        val outcome = classifyOutcome(before, after, fpsImprovement, cpuReduction)

        val summary = buildSummary(outcome, before, after, fpsImprovement, cpuReductionPct)

        val result = VerificationResult(
            id = "VER-${UUID.randomUUID().toString().take(8).uppercase()}",
            before = before,
            after = after,
            outcome = outcome,
            fpsImprovementPercent = fpsImprovement,
            cpuReductionPercent = cpuReductionPct,
            frameTimeReductionPercent = frameTimeReduction,
            summary = summary
        )

        // Persist
        db.verificationDao().insert(
            VerificationRunEntity(
                id = result.id,
                incidentId = incidentContext.incidentId,
                investigationId = investigationId,
                beforeFps = before.fps,
                beforeCpu = before.cpuPercent,
                beforeMemoryMb = before.memoryMb,
                beforeFrameTimeMs = before.frameTimeMs,
                afterFps = after.fps,
                afterCpu = after.cpuPercent,
                afterMemoryMb = after.memoryMb,
                afterFrameTimeMs = after.frameTimeMs,
                outcome = outcome.name,
                fpsImprovementPercent = fpsImprovement,
                cpuReductionPercent = cpuReductionPct
            )
        )

        return result
    }

    /**
     * Deterministic outcome classification using threshold rules.
     * NO LLM involvement.
     */
    private fun classifyOutcome(
        before: BeforeMetrics,
        after: AfterMetrics,
        fpsImprovementPercent: Float,
        cpuReductionPp: Float
    ): Outcome {
        val fpsImproved = fpsImprovementPercent > 20f   // >20% FPS improvement
        val cpuImproved = cpuReductionPp > 10f          // >10pp CPU reduction
        val fpsRecovered = after.fps > 45f              // Back to acceptable FPS
        val frameTimeImproved = after.frameTimeMs < before.frameTimeMs * 0.7f  // 30%+ improvement
        val regressed = fpsImprovementPercent < -10f    // FPS got worse

        return when {
            regressed -> Outcome.WORSE
            fpsImproved && fpsRecovered && (cpuImproved || frameTimeImproved) -> Outcome.SUCCESS
            fpsImproved && (cpuImproved || frameTimeImproved) -> Outcome.PARTIAL
            fpsImprovementPercent in -10f..10f && cpuReductionPp in -10f..10f -> Outcome.INCONCLUSIVE
            !fpsImproved && !cpuImproved -> Outcome.FAILED
            else -> Outcome.PARTIAL
        }
    }

    private fun buildSummary(
        outcome: Outcome,
        before: BeforeMetrics,
        after: AfterMetrics,
        fpsImprovement: Float,
        cpuReductionPct: Float
    ): String = when (outcome) {
        Outcome.SUCCESS ->
            "Fix succeeded. FPS improved from ${before.fps.toInt()} to ${after.fps.toInt()} " +
                "(+${fpsImprovement.toInt()}%). CPU reduced by ${cpuReductionPct.toInt()}%."
        Outcome.PARTIAL ->
            "Partial improvement. FPS: ${before.fps.toInt()}→${after.fps.toInt()}. " +
                "Further optimization may be needed."
        Outcome.FAILED ->
            "No meaningful improvement detected. FPS: ${before.fps.toInt()}→${after.fps.toInt()}. " +
                "The applied fix may not address the root cause."
        Outcome.WORSE ->
            "Performance regressed. FPS dropped from ${before.fps.toInt()} to ${after.fps.toInt()}. " +
                "Revert the change and investigate alternative hypotheses."
        Outcome.INCONCLUSIVE ->
            "Results are inconclusive. Insufficient change to determine fix effectiveness. " +
                "Re-run with a more controlled scenario."
    }
}
