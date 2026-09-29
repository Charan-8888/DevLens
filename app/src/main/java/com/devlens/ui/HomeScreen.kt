package com.devlens.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devlens.DevLensViewModel
import com.devlens.ui.theme.*

@Composable
fun HomeScreen(
    uiState: DevLensViewModel.UiState,
    onStartMonitoring: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ─── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "DevLens",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Android Performance Investigator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            StatusBadge("ON-DEVICE AI", Violet)
        }

        Spacer(Modifier.height(28.dp))

        // ─── Hero description ─────────────────────────────────────────────────
        DevLensCard {
            Text(
                "Evidence-Driven Performance Investigation",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "DevLens monitors real runtime metrics, detects performance incidents, " +
                "correlates evidence, and uses a local AI model to generate hypotheses " +
                "and fix recommendations — completely offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }

        SectionLabel("INVESTIGATION LOOP")

        // ─── Flow steps ───────────────────────────────────────────────────────
        val steps = listOf(
            "01" to "Monitor real FPS, CPU, and memory",
            "02" to "Detect incidents deterministically",
            "03" to "Correlate evidence into structured facts",
            "04" to "AI reasons over evidence, not raw data",
            "05" to "Verify fix with real before/after metrics",
            "06" to "Store experience for future investigations"
        )
        DevLensCard {
            steps.forEach { (num, desc) ->
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(num, style = MaterialTheme.typography.labelSmall,
                        color = Violet, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp))
                    Text(desc, style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted, lineHeight = 17.sp)
                }
            }
        }

        SectionLabel("WHAT DEVLENS IS NOT")
        DevLensCard {
            Text(
                "Not a dashboard · Not a chat-bot · Not a device cleaner\n" +
                "Not a universal profiler replacement\n\n" +
                "DevLens is a working performance investigation pipeline that " +
                "turns symptom numbers into evidence-backed hypotheses.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }

        // ─── Experience memory indicator ──────────────────────────────────────
        if (uiState.experienceCount > 0) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Violet.copy(alpha = 0.1f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🧠", fontSize = 18.sp)
                Column {
                    Text("Hindsight Memory Active",
                        style = MaterialTheme.typography.titleMedium,
                        color = VioletLight, fontWeight = FontWeight.Bold)
                    Text("${uiState.experienceCount} investigation experience(s) stored",
                        style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ─── CTA ─────────────────────────────────────────────────────────────
        PrimaryButton(
            text = "Begin Performance Investigation →",
            onClick = onStartMonitoring,
            color = Violet
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Real metrics · Local AI · No cloud · No API key",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
