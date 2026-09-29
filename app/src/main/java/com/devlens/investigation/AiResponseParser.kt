package com.devlens.investigation

import com.devlens.detection.IncidentContextBuilder.IncidentContext

/**
 * Parses the LLM's text response into a structured InvestigationResult.
 *
 * The model may not always follow the exact format, so parsing is lenient
 * with fallback values. The raw response is always preserved.
 */
class AiResponseParser {

    fun parse(rawResponse: String, evidence: List<Evidence>, context: IncidentContext): InvestigationResult {
        val lines = rawResponse.lines().map { it.trim() }

        val summary = extractSection(lines, "SUMMARY:", stopAt = listOf("HYPOTHESIS", "MISSING", "ACTION")) {
            it.removePrefix("SUMMARY:").trim()
        } ?: inferSummary(context)

        val hypotheses = parseHypotheses(lines, evidence)
        val missingEvidence = parseListSection(lines, "MISSING:")
        val action = extractFirstMatch(lines, "ACTION:") ?: inferAction(context)
        val verifyBy = extractFirstMatch(lines, "VERIFY_BY:") ?: "FPS and frame time"

        return InvestigationResult(
            summary = summary,
            hypotheses = hypotheses,
            missingEvidence = missingEvidence,
            recommendedAction = action,
            verificationMetric = verifyBy,
            rawResponse = rawResponse
        )
    }

    private fun parseHypotheses(lines: List<String>, evidence: List<Evidence>): List<Hypothesis> {
        val hypotheses = mutableListOf<Hypothesis>()

        // Find HYPOTHESIS_N sections
        for (n in 1..3) {
            val startMarker = "HYPOTHESIS_$n:"
            val startIdx = lines.indexOfFirst { it.startsWith(startMarker) }
            if (startIdx < 0) break

            val endIdx = lines.drop(startIdx + 1).indexOfFirst { element ->
                element.startsWith("HYPOTHESIS_") || element.startsWith("MISSING:") ||
                    element.startsWith("ACTION:")
            }.let { if (it < 0) lines.size else startIdx + 1 + it }

            val block = lines.subList(startIdx + 1, endIdx)

            val cause = block.firstOrNull { it.startsWith("CAUSE:") }
                ?.removePrefix("CAUSE:")?.trim() ?: "Unknown cause"
            val support = block.filter { it.startsWith("SUPPORT:") }
                .map { it.removePrefix("SUPPORT:").trim() }
            val confidence = block.firstOrNull { it.startsWith("CONFIDENCE:") }
                ?.removePrefix("CONFIDENCE:")?.trim()?.lowercase() ?: "possible"

            hypotheses += Hypothesis(
                id = "H$n",
                cause = cause,
                supportingEvidence = support,
                contradictingEvidence = emptyList(),
                missingEvidence = emptyList(),
                confidenceLabel = confidence
            )
        }

        // Fallback if parsing failed
        if (hypotheses.isEmpty()) {
            hypotheses += buildFallbackHypothesis(evidence)
        }

        return hypotheses
    }

    private fun extractSection(
        lines: List<String>,
        startMarker: String,
        stopAt: List<String>,
        transform: (String) -> String
    ): String? {
        val idx = lines.indexOfFirst { it.startsWith(startMarker) }
        if (idx < 0) return null
        val line = lines[idx]
        return transform(line).takeIf { it.isNotBlank() }
    }

    private fun extractFirstMatch(lines: List<String>, marker: String): String? {
        return lines.firstOrNull { it.startsWith(marker) }
            ?.removePrefix(marker)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun parseListSection(lines: List<String>, marker: String): List<String> {
        val idx = lines.indexOfFirst { it.startsWith(marker) }
        if (idx < 0) return emptyList()
        val line = lines[idx].removePrefix(marker).trim()
        return if (line.isNotBlank()) listOf(line) else emptyList()
    }

    private fun inferSummary(context: IncidentContext): String {
        return "Performance incident: ${context.type.name.replace('_', ' ').lowercase()} " +
            "detected. FPS dropped from ${context.baseline.fps.toInt()} to " +
            "${context.peakFps.toInt()}, CPU at ${context.peakCpu.toInt()}%."
    }

    private fun inferAction(context: IncidentContext): String {
        return when {
            context.peakCpu > 85f -> "Profile the main thread during the incident and move " +
                "expensive computation to a background thread."
            context.memoryRiseMb > 50f -> "Inspect allocation patterns and reduce object " +
                "creation during frame-critical code paths."
            context.peakFrameTimeMs > 33f -> "Inspect rendering code for expensive draw calls " +
                "or layout inflation happening on the UI thread."
            else -> "Profile the application during the scenario that triggers the incident."
        }
    }

    private fun buildFallbackHypothesis(evidence: List<Evidence>): Hypothesis {
        val hasHighCpu = evidence.any { it.statement.contains("CPU") && (it.numericValue ?: 0.0) > 80 }
        val hasFrameDrop = evidence.any { it.statement.contains("FPS") && (it.numericValue ?: 100.0) < 30 }

        return Hypothesis(
            id = "H1",
            cause = when {
                hasHighCpu -> "Main-thread CPU overload blocking frame delivery"
                hasFrameDrop -> "Rendering workload exceeding frame budget"
                else -> "Performance bottleneck under investigation"
            },
            supportingEvidence = evidence.take(3).map { it.statement },
            contradictingEvidence = emptyList(),
            missingEvidence = listOf("Thread-level CPU attribution"),
            confidenceLabel = "possible"
        )
    }
}
