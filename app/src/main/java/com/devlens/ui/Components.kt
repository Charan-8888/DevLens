package com.devlens.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devlens.data.entities.TelemetrySample
import com.devlens.ui.theme.*

// ─── Shared Composables ───────────────────────────────────────────────────────

@Composable
fun DevLensCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkPanel)
            .border(1.dp, DarkLine, RoundedCornerShape(14.dp))
            .padding(15.dp),
        content = content
    )
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    color: Color,
    delta: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPanel)
            .border(1.dp, DarkLine, RoundedCornerShape(12.dp))
            .padding(11.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
            color = color, fontWeight = FontWeight.Bold)
        if (delta != null) {
            Text(delta, style = MaterialTheme.typography.labelSmall, color = color,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun MetricsRow(
    fps: Float,
    cpu: Float,
    memMb: Float,
    isIncident: Boolean = false
) {
    val fpsColor = if (fps < 30f && isIncident) Red else if (fps < 45f) Amber else Green
    val cpuColor = if (cpu > 85f && isIncident) Red else if (cpu > 70f) Amber else Blue
    val memColor = if (isIncident) Amber else Blue

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        MetricCard("FPS", "${fps.toInt()}", fpsColor, modifier = Modifier.weight(1f))
        MetricCard("CPU", "${cpu.toInt()}%", cpuColor, modifier = Modifier.weight(1f))
        MetricCard("MEM", "${memMb.toInt()} MB", memColor, modifier = Modifier.weight(1f))
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Violet,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) color else DarkLine)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium,
            color = if (enabled) TextPrimary else TextMuted,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadge(text: String, color: Color, isAlert: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (isAlert) 0.5f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = if (isAlert) alpha else 1f))
        )
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/**
 * Mini telemetry sparkline chart for FPS history.
 * Draws a smooth path through the last N samples.
 */
@Composable
fun TelemetryChart(
    samples: List<TelemetrySample>,
    modifier: Modifier = Modifier
) {
    if (samples.size < 2) {
        Box(modifier = modifier.background(DarkPanel, RoundedCornerShape(14.dp)))
        return
    }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(DarkPanel)
    ) {
        val w = size.width
        val h = size.height
        val padH = 20f
        val padW = 8f

        // ─── Grid lines ────────────────────────────────────────────
        drawLine(DarkLine, Offset(padW, h * 0.25f), Offset(w - padW, h * 0.25f),
            strokeWidth = 1f)
        drawLine(DarkLine, Offset(padW, h * 0.5f), Offset(w - padW, h * 0.5f),
            strokeWidth = 1f)
        drawLine(DarkLine, Offset(padW, h * 0.75f), Offset(w - padW, h * 0.75f),
            strokeWidth = 1f)

        val maxFps = 65f
        val minFps = 0f

        fun fpsToY(fps: Float) = padH + (1f - (fps - minFps) / (maxFps - minFps)) * (h - 2 * padH)
        fun cpuToY(cpu: Float) = padH + (1f - cpu / 100f) * (h - 2 * padH)

        val step = (w - 2 * padW) / (samples.size - 1).toFloat()

        // ─── FPS line ──────────────────────────────────────────────
        val fpsPath = Path()
        samples.forEachIndexed { i, s ->
            val x = padW + i * step
            val y = fpsToY(s.fps)
            if (i == 0) fpsPath.moveTo(x, y) else fpsPath.lineTo(x, y)
        }
        drawPath(fpsPath, ChartFps, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // ─── CPU line ──────────────────────────────────────────────
        val cpuPath = Path()
        samples.forEachIndexed { i, s ->
            val x = padW + i * step
            val y = cpuToY(s.cpuPercent)
            if (i == 0) cpuPath.moveTo(x, y) else cpuPath.lineTo(x, y)
        }
        drawPath(cpuPath, ChartCpu, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
    }
}

@Composable
fun ProgressStepper(currentStep: Int) {
    val steps = listOf("SCAN", "DETECT", "EXPLAIN", "FIX", "VERIFY")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { i, step ->
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall,
                color = if (i == currentStep) Violet else if (i < currentStep) Green else TextDim,
                fontWeight = if (i == currentStep) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
