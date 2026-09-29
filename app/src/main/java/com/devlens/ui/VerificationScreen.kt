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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devlens.DevLensViewModel
import com.devlens.demo.StressScenarios
import com.devlens.ui.theme.*
import com.devlens.verification.VerificationEngine

@Composable
fun VerificationScreen(
    uiState: DevLensViewModel.UiState,
    onApplyFixAndVerify: (String) -> Unit,
    onSaveAndReset: () -> Unit,
    onTriggerScenario: (StressScenarios.Scenario) -> Unit,
    onBack: () -> Unit
) {
    val before = uiState.beforeSnapshot
    val after = uiState.afterSnapshot
    val result = uiState.verificationResult
    val appState = uiState.appState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back", color = TextMuted) }
        }

        Text("VERIFICATION", style = MaterialTheme.typography.labelSmall,
            color = Violet, letterSpacing = 1.sp)
        Text(
            when (appState) {
                DevLensViewModel.AppState.WAITING_FOR_FIX -> "Apply Fix & Re-run"
                DevLensViewModel.AppState.VERIFYING -> "Measuring after fix..."
                DevLensViewModel.AppState.OUTCOME_DETECTED,
                DevLensViewModel.AppState.EXPERIENCE_SAVED -> "Verification Complete"
                else -> "Verification"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary, fontWeight = FontWeight.Bold
        )

        ProgressStepper(currentStep = 4)
        Spacer(Modifier.height(16.dp))

        when (appState) {
            DevLensViewModel.AppState.WAITING_FOR_FIX -> WaitingForFixContent(
                uiState = uiState,
                onApplyFix = onApplyFixAndVerify,
                onTriggerScenario = onTriggerScenario
            )

            DevLensViewModel.AppState.VERIFYING -> {
                DevLensCard {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Violet, strokeWidth = 2.dp
                        )
                        Text("Measuring metrics after fix...",
                            style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Collecting for 3 seconds before comparing...",
                        style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }

            DevLensViewModel.AppState.OUTCOME_DETECTED,
            DevLensViewModel.AppState.EXPERIENCE_SAVED -> {
                if (result != null && before != null) {
                    VerificationResultContent(result, before, after)

                    if (appState == DevLensViewModel.AppState.EXPERIENCE_SAVED) {
                        Spacer(Modifier.height(12.dp))
                        DevLensCard {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🧠", fontSize = 20.sp)
                                Column {
                                    Text("Experience Saved to Memory",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VioletLight, fontWeight = FontWeight.Bold)
                                    Text("${uiState.experienceCount} total experience(s) stored",
                                        style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                                    Text("Future similar incidents will benefit from this outcome.",
                                        style = MaterialTheme.typography.bodyMedium, color = TextDim)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    PrimaryButton("Start New Investigation →", onSaveAndReset)
                }
            }

            else -> {}
        }
    }
}

@Composable
fun WaitingForFixContent(
    uiState: DevLensViewModel.UiState,
    onApplyFix: (String) -> Unit,
    onTriggerScenario: (StressScenarios.Scenario) -> Unit
) {
    val recommendation = uiState.investigation?.result?.recommendedAction ?: "Apply the recommended fix"
    val incident = uiState.incident

    SectionLabel("INCIDENT PEAK (BEFORE)")
    if (incident != null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            MetricCard("FPS (BEFORE)", "${incident.peakFps.toInt()}", Red, modifier = Modifier.weight(1f))
            MetricCard("CPU (BEFORE)", "${incident.peakCpu.toInt()}%", Red, modifier = Modifier.weight(1f))
            MetricCard("FRAME TIME", "${incident.peakFrameTimeMs.toInt()} ms", Amber, modifier = Modifier.weight(1f))
        }
    }

    SectionLabel("RECOMMENDED FIX")
    DevLensCard {
        Text(recommendation, style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary, lineHeight = 20.sp)
    }

    SectionLabel("SIMULATE FIX")
    DevLensCard {
        Text(
            "For this demo: stop the stress scenario to simulate applying the fix. " +
            "The 'Normal Mode' simulates the app running without the expensive workload.",
            style = MaterialTheme.typography.bodyMedium, color = TextMuted, lineHeight = 17.sp
        )
        Spacer(Modifier.height(12.dp))

        // "Normal mode" = run no stress scenario (FPS/CPU should recover)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onTriggerScenario(StressScenarios.Scenario.NONE)
                    onApplyFix("Stopped stress scenario — simulates removing the expensive workload")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Fix (Normal Mode)", style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun VerificationResultContent(
    result: VerificationEngine.VerificationResult,
    before: VerificationEngine.BeforeMetrics,
    after: VerificationEngine.AfterMetrics?
) {
    val outcomeColor = when (result.outcome) {
        VerificationEngine.Outcome.SUCCESS -> Green
        VerificationEngine.Outcome.PARTIAL -> Amber
        VerificationEngine.Outcome.FAILED -> Red
        VerificationEngine.Outcome.WORSE -> Red
        VerificationEngine.Outcome.INCONCLUSIVE -> TextMuted
    }
    val outcomeEmoji = when (result.outcome) {
        VerificationEngine.Outcome.SUCCESS -> "✅"
        VerificationEngine.Outcome.PARTIAL -> "⚡"
        VerificationEngine.Outcome.FAILED -> "❌"
        VerificationEngine.Outcome.WORSE -> "📉"
        VerificationEngine.Outcome.INCONCLUSIVE -> "❓"
    }

    // Outcome banner
    DevLensCard {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(outcomeEmoji, fontSize = 28.sp)
            Column {
                Text(result.outcome.name, style = MaterialTheme.typography.headlineMedium,
                    color = outcomeColor, fontWeight = FontWeight.Bold)
                Text(result.summary, style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted, lineHeight = 17.sp)
            }
        }
    }

    SectionLabel("BEFORE / AFTER COMPARISON")
    DevLensCard {
        // FPS row
        BeforeAfterRow("FPS",
            "${before.fps.toInt()}", "${after?.fps?.toInt() ?: "–"}",
            delta = if (result.fpsImprovementPercent > 0)
                "+${result.fpsImprovementPercent.toInt()}%" else "${result.fpsImprovementPercent.toInt()}%",
            deltaColor = if (result.fpsImprovementPercent > 10) Green else Red
        )
        Divider(color = DarkLine, modifier = Modifier.padding(vertical = 4.dp))
        // CPU row
        BeforeAfterRow("CPU",
            "${before.cpuPercent.toInt()}%", "${after?.cpuPercent?.toInt() ?: "–"}%",
            delta = if (result.cpuReductionPercent > 0)
                "−${result.cpuReductionPercent.toInt()}%" else "+${(-result.cpuReductionPercent).toInt()}%",
            deltaColor = if (result.cpuReductionPercent > 5) Green else Red
        )
        Divider(color = DarkLine, modifier = Modifier.padding(vertical = 4.dp))
        // Frame time row
        BeforeAfterRow("FRAME TIME",
            "${before.frameTimeMs.toInt()} ms", "${after?.frameTimeMs?.toInt() ?: "–"} ms",
            delta = if (result.frameTimeReductionPercent > 0)
                "−${result.frameTimeReductionPercent.toInt()}%" else "No change",
            deltaColor = if (result.frameTimeReductionPercent > 10) Green else Red
        )
    }
}

@Composable
fun BeforeAfterRow(
    label: String,
    before: String,
    after: String,
    delta: String,
    deltaColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = TextMuted, modifier = Modifier.width(80.dp))
        Text(before, style = MaterialTheme.typography.titleMedium,
            color = Red, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f))
        Text("→", style = MaterialTheme.typography.bodyMedium, color = TextDim)
        Text(after, style = MaterialTheme.typography.titleMedium,
            color = Green, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center)
        Text(delta, style = MaterialTheme.typography.labelSmall,
            color = deltaColor, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
    }
}
