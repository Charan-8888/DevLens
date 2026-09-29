package com.devlens.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import com.devlens.demo.StressScenarios
import com.devlens.ui.theme.*

@Composable
fun MonitorScreen(
    uiState: DevLensViewModel.UiState,
    onTriggerScenario: (StressScenarios.Scenario) -> Unit,
    onStopMonitoring: () -> Unit,
    onInvestigateIncident: () -> Unit
) {
    val current = uiState.currentSample
    val isIncident = uiState.appState == DevLensViewModel.AppState.INCIDENT_DETECTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ─── Page header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LIVE MONITORING", style = MaterialTheme.typography.labelSmall,
                    color = Violet, letterSpacing = 1.sp)
                Text("DevLens", style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            StatusBadge(
                text = if (isIncident) "INCIDENT" else if (uiState.isBaselineReady) "ACTIVE" else "BASELINE",
                color = if (isIncident) Red else if (uiState.isBaselineReady) Green else Amber,
                isAlert = isIncident
            )
        }

        ProgressStepper(currentStep = 0)
        Spacer(Modifier.height(12.dp))

        // ─── Incident alert ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isIncident,
            enter = fadeIn() + slideInVertically()
        ) {
            DevLensCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚠️", fontSize = 20.sp)
                    Column {
                        Text("PERFORMANCE INCIDENT DETECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Red, fontWeight = FontWeight.Bold)
                        Text(uiState.incident?.let {
                            "${it.type.name.replace('_', ' ')}: ${it.severity.name} severity"
                        } ?: "Analysing...",
                            style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                }
                Spacer(Modifier.height(10.dp))
                PrimaryButton("Investigate Incident →", onInvestigateIncident, color = Red)
            }
        }

        // ─── Live metrics ────────────────────────────────────────────────────
        SectionLabel("LIVE METRICS")
        MetricsRow(
            fps = current?.fps ?: 0f,
            cpu = current?.cpuPercent ?: 0f,
            memMb = current?.memoryMb ?: 0f,
            isIncident = isIncident
        )

        // Frame time metric
        Spacer(Modifier.height(7.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            MetricCard(
                "FRAME TIME", "${String.format("%.1f", current?.frameTimeMs ?: 0f)} ms",
                if ((current?.frameTimeMs ?: 0f) > 33f) Red else Green,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                "JANK", if (current?.jankDetected == true) "DETECTED" else "None",
                if (current?.jankDetected == true) Red else Green,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                "SAMPLES", "${uiState.recentSamples.size}",
                Blue,
                modifier = Modifier.weight(1f)
            )
        }

        // ─── Baseline info ───────────────────────────────────────────────────
        if (uiState.baseline != null && uiState.isBaselineReady) {
            SectionLabel("CAPTURED BASELINE")
            DevLensCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FPS: ${uiState.baseline.fps.toInt()}",
                        style = MaterialTheme.typography.bodyMedium, color = Green)
                    Text("CPU: ${uiState.baseline.cpuPercent.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium, color = Blue)
                    Text("MEM: ${uiState.baseline.memoryMb.toInt()} MB",
                        style = MaterialTheme.typography.bodyMedium, color = Blue)
                }
                Spacer(Modifier.height(4.dp))
                Text("Baseline captured · Incident detection active",
                    style = MaterialTheme.typography.bodyMedium, color = TextDim)
            }
        } else {
            SectionLabel("CAPTURING BASELINE")
            DevLensCard {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Violet,
                    trackColor = DarkLine
                )
                Spacer(Modifier.height(6.dp))
                Text("Collecting stable baseline (4 seconds)...",
                    style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }

        // ─── Live chart ───────────────────────────────────────────────────────
        SectionLabel("PERFORMANCE TIMELINE")
        TelemetryChart(
            samples = uiState.recentSamples,
            modifier = Modifier.height(140.dp)
        )
        Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(ChartFps))
                Text("FPS", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(ChartCpu))
                Text("CPU", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }

        // ─── Stress triggers ─────────────────────────────────────────────────
        if (uiState.isBaselineReady && !isIncident) {
            SectionLabel("DEMO STRESS SCENARIOS")
            DevLensCard {
                Text("Trigger a controlled stress scenario to demonstrate incident detection.",
                    style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Spacer(Modifier.height(12.dp))

                val scenarios = listOf(
                    StressScenarios.Scenario.CPU_STRESS to ("CPU Stress" to "Heavy computation · prime + matrix"),
                    StressScenarios.Scenario.MEMORY_STRESS to ("Memory Stress" to "Allocation storm · GC pressure"),
                    StressScenarios.Scenario.RENDERING_STRESS to ("Rendering Stress" to "Bitmap + canvas workload"),
                    StressScenarios.Scenario.COMBINED to ("Combined Incident" to "CPU + memory together")
                )

                scenarios.forEach { (scenario, labels) ->
                    val (title, desc) = labels
                    val isActive = uiState.activeScenario == scenario
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) Violet.copy(alpha = 0.15f) else DarkSurface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium,
                                color = if (isActive) VioletLight else TextPrimary,
                                fontWeight = FontWeight.Medium)
                            Text(desc, style = MaterialTheme.typography.bodyMedium, color = TextDim)
                        }
                        Button(
                            onClick = { onTriggerScenario(scenario) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) Violet else DarkLine
                            ),
                            enabled = !isActive,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isActive) "RUNNING" else "TRIGGER",
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Stop button ──────────────────────────────────────────────────────
        PrimaryButton(
            text = "Stop Monitoring",
            onClick = onStopMonitoring,
            color = DarkSurface
        )
    }
}
