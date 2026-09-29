package com.devlens.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devlens.DevLensViewModel
import com.devlens.ui.theme.*

@Composable
fun InvestigationScreen(
    uiState: DevLensViewModel.UiState,
    onStartVerification: () -> Unit,
    onBack: () -> Unit
) {
    val investigation = uiState.investigation
    val incident = uiState.incident
    val isInvestigating = uiState.appState == DevLensViewModel.AppState.INVESTIGATING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ─── Header ───────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) {
                Text("‹ Back", color = TextMuted)
            }
        }
        Text("AI INVESTIGATION", style = MaterialTheme.typography.labelSmall,
            color = Violet, letterSpacing = 1.sp)
        Text(
            if (isInvestigating) "Investigating..." else "Investigation Complete",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary, fontWeight = FontWeight.Bold
        )

        ProgressStepper(currentStep = 2)
        Spacer(Modifier.height(12.dp))

        // ─── LLM status ───────────────────────────────────────────────────────
        if (isInvestigating) {
            DevLensCard {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Violet, strokeWidth = 2.dp
                    )
                    Column {
                        Text("Local AI Investigation",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(uiState.llmState.ifEmpty { "Preparing model..." },
                            style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("No cloud · No API key · SmolLM2 Q4 running on-device",
                    style = MaterialTheme.typography.bodyMedium, color = TextDim)
            }
            return
        }

        // ─── Incident summary ─────────────────────────────────────────────────
        if (incident != null) {
            SectionLabel("INCIDENT SUMMARY")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MetricCard("FPS",
                    "${incident.baseline.fps.toInt()}→${incident.peakFps.toInt()}",
                    Red, "−${incident.fpsFallPercent.toInt()}%",
                    modifier = Modifier.weight(1f))
                MetricCard("CPU",
                    "${incident.baseline.cpuPercent.toInt()}→${incident.peakCpu.toInt()}%",
                    Red, "+${incident.cpuRisePercent.toInt()}pp",
                    modifier = Modifier.weight(1f))
                MetricCard("MEM",
                    "+${incident.memoryRiseMb.toInt()} MB",
                    Amber, modifier = Modifier.weight(1f))
            }
        }

        // ─── AI investigation result ───────────────────────────────────────────
        if (investigation != null) {
            val result = investigation.result

            // Previous experience indicator
            if (investigation.hadPreviousExperiences) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Violet.copy(alpha = 0.1f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🧠", fontSize = 16.sp)
                    Text(
                        "${investigation.previousExperienceCount} similar experience(s) from hindsight memory incorporated",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VioletLight
                    )
                }
            }

            SectionLabel("SUMMARY")
            DevLensCard {
                Text(result.summary, style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary, lineHeight = 20.sp)
            }

            SectionLabel("HYPOTHESES")
            result.hypotheses.forEachIndexed { i, hyp ->
                DevLensCard(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("H${i + 1}: ${hyp.cause}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        val (chipColor, chipText) = when (hyp.confidenceLabel.lowercase()) {
                            "supported" -> Green to "SUPPORTED"
                            "possible" -> Amber to "POSSIBLE"
                            "unlikely" -> Red to "UNLIKELY"
                            else -> TextMuted to hyp.confidenceLabel.uppercase()
                        }
                        StatusBadge(chipText, chipColor)
                    }
                    if (hyp.supportingEvidence.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Supporting evidence:", style = MaterialTheme.typography.labelSmall,
                            color = TextMuted, fontWeight = FontWeight.Bold)
                        hyp.supportingEvidence.forEach { ev ->
                            Text("• $ev", style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }

            if (result.missingEvidence.isNotEmpty()) {
                SectionLabel("MISSING EVIDENCE")
                DevLensCard {
                    result.missingEvidence.forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodyMedium,
                            color = Amber, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            SectionLabel("RECOMMENDED ACTION")
            DevLensCard {
                Text(result.recommendedAction, style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary, lineHeight = 20.sp)
                Spacer(Modifier.height(8.dp))
                Text("Verify by monitoring: ${result.verificationMetric}",
                    style = MaterialTheme.typography.bodyMedium, color = Violet)
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton("Verify Fix →", onStartVerification, color = Green)

            Spacer(Modifier.height(8.dp))
            Text(
                "Apply the recommended fix to the running scenario, then tap Verify Fix",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDim,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        uiState.errorMessage?.let { error ->
            Spacer(Modifier.height(16.dp))
            DevLensCard {
                Text("Error: $error", style = MaterialTheme.typography.bodyMedium, color = Red)
            }
        }
    }
}
