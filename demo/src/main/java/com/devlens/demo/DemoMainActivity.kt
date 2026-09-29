package com.devlens.demo

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

/**
 * DevLens Demo Target Application
 *
 * A separate Android app that deliberately contains workloads capable
 * of producing measurable performance incidents.
 *
 * Scenarios:
 * - Normal Mode: idle / no stress
 * - CPU Stress: heavy computation on a background thread (simulating main-thread block)
 * - Memory Stress: allocation storm with GC pressure
 * - Rendering Stress: expensive bitmap/canvas operations
 * - Combined Incident: all at once
 * - Recovery Mode: stops all stress (simulates fix)
 *
 * DevLens monitors this app's process metrics to detect incidents.
 */
class DemoMainActivity : Activity() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var activeJobs = mutableListOf<Job>()

    private val BG = Color.rgb(13, 12, 18)
    private val PANEL = Color.rgb(27, 25, 34)
    private val LINE = Color.rgb(61, 55, 71)
    private val WHITE = Color.rgb(245, 242, 249)
    private val MUTED = Color.rgb(173, 163, 183)
    private val PURPLE = Color.rgb(139, 92, 246)
    private val GREEN = Color.rgb(88, 215, 156)
    private val RED = Color.rgb(244, 105, 112)
    private val AMBER = Color.rgb(241, 183, 77)
    private val CORAL = Color.rgb(235, 115, 97)

    private fun dp(n: Float) = (n * resources.displayMetrics.density + 0.5f).toInt()
    private fun tv(s: String, size: Float, color: Int) = TextView(this).apply {
        text = s; textSize = size; setTextColor(color)
    }

    private lateinit var statusText: TextView
    private lateinit var infoText: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val shell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20f), dp(28f), dp(20f), dp(24f))
        }

        // Header
        val title = tv("DevLens", 26f, WHITE).apply { typeface = Typeface.DEFAULT_BOLD }
        root.addView(title)
        val sub = tv("Demo Target Application", 13f, MUTED)
        root.addView(sub)

        // Status card
        statusText = tv("● Normal Mode — No stress active", 14f, GREEN).apply {
            typeface = Typeface.DEFAULT_BOLD
        }
        infoText = tv("Select a scenario to trigger a performance incident", 12f, MUTED).apply {
            setPadding(0, dp(4f), 0, 0)
        }
        val statusCard = card().apply {
            addView(statusText)
            addView(infoText)
        }
        val statusLp = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(20f) }
        root.addView(statusCard, statusLp)

        // Scenario buttons
        sectionLabel(root, "STRESS SCENARIOS")
        scenarioBtn(root, "Normal Mode", "Idle · no workload · baseline state", GREEN) {
            stopAll()
            updateStatus("● Normal Mode — No stress", GREEN, "App is idle. Performance should be normal.")
        }
        scenarioBtn(root, "CPU Stress", "Heavy computation · prime numbers · matrix ops", CORAL) {
            stopAll()
            startCpuStress()
            updateStatus("● CPU Stress — ACTIVE", CORAL, "Heavy CPU workload running. FPS may drop.")
        }
        scenarioBtn(root, "Memory Stress", "Rapid allocation · GC pressure · jank", AMBER) {
            stopAll()
            startMemoryStress()
            updateStatus("● Memory Stress — ACTIVE", AMBER, "Allocation storm active. Watch for GC jank.")
        }
        scenarioBtn(root, "Rendering Stress", "Large bitmaps · canvas operations · overdraw", PURPLE) {
            stopAll()
            startRenderingStress()
            updateStatus("● Rendering Stress — ACTIVE", Color.rgb(167, 139, 250),
                "Expensive rendering active. Frame times increasing.")
        }
        scenarioBtn(root, "Combined Incident", "CPU + memory + rendering simultaneously", RED) {
            stopAll()
            startCpuStress()
            startMemoryStress()
            startRenderingStress()
            updateStatus("● COMBINED INCIDENT — CRITICAL", RED, "All stress sources active simultaneously.")
        }

        sectionLabel(root, "RECOVERY")
        scenarioBtn(root, "Recovery / Fixed Version", "Stop all stress · simulate fix applied", GREEN) {
            stopAll()
            updateStatus("✓ Recovery Mode — All stress stopped", GREEN,
                "Performance should recover. Verify with DevLens.")
        }

        // Footer
        val footer = tv(
            "This app is designed as a controlled demo target for the DevLens investigation pipeline. " +
            "Start DevLens and select a scenario here to trigger real performance incidents.",
            11f, MUTED
        ).apply {
            setPadding(0, dp(16f), 0, 0)
            setLineSpacing(dp(2f).toFloat(), 1f)
        }
        root.addView(footer)

        scroll.addView(root)
        shell.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(shell)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(PANEL)
        setPadding(dp(14f), dp(13f), dp(14f), dp(13f))
    }

    private fun sectionLabel(root: LinearLayout, text: String) {
        val label = tv(text, 10f, MUTED).apply {
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.1f
            setPadding(0, dp(20f), 0, dp(8f))
        }
        root.addView(label)
    }

    private fun scenarioBtn(root: LinearLayout, title: String, desc: String, color: Int, onClick: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(PANEL)
            setPadding(dp(14f), dp(12f), dp(14f), dp(12f))
        }
        val lp = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8f) }

        val t = tv(title, 16f, WHITE).apply { typeface = Typeface.DEFAULT_BOLD }
        val d = tv(desc, 11f, MUTED).apply { setPadding(0, dp(3f), 0, 0) }
        card.addView(t)
        card.addView(d)
        card.setOnClickListener { onClick() }

        // Color strip on left
        val strip = View(this).apply {
            setBackgroundColor(color)
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        row.addView(strip, LinearLayout.LayoutParams(dp(3f), -1))
        row.addView(card, LinearLayout.LayoutParams(0, -2, 1f))

        root.addView(row, lp)
    }

    private fun updateStatus(status: String, color: Int, info: String) {
        runOnUiThread {
            statusText.text = status
            statusText.setTextColor(color)
            infoText.text = info
        }
    }

    private fun stopAll() {
        activeJobs.forEach { it.cancel() }
        activeJobs.clear()
    }

    private fun startCpuStress() {
        val job = scope.launch {
            while (isActive) {
                // Expensive: compute primes
                var count = 0; var n = 2
                while (count < 2000) { if (isPrime(n)) count++; n++ }
                // Matrix multiply
                val size = 80
                val a = Array(size) { DoubleArray(size) { Random.nextDouble() } }
                val b = Array(size) { DoubleArray(size) { Random.nextDouble() } }
                val c = Array(size) { DoubleArray(size) }
                for (i in 0 until size) for (j in 0 until size) for (k in 0 until size)
                    c[i][j] += a[i][k] * b[k][j]
            }
        }
        activeJobs.add(job)
    }

    private fun startMemoryStress() {
        val job = scope.launch {
            while (isActive) {
                val arrays = mutableListOf<ByteArray>()
                repeat(40) { arrays.add(ByteArray(1024 * 1024) { it.toByte() }) }
                delay(60)
                arrays.clear()
                System.gc()
                delay(100)
            }
        }
        activeJobs.add(job)
    }

    private fun startRenderingStress() {
        val job = scope.launch {
            while (isActive) {
                val bmp = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                for (r in 10..400 step 4) {
                    paint.color = Color.argb(150, Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
                    paint.style = Paint.Style.STROKE
                    canvas.drawCircle(400f, 400f, r.toFloat(), paint)
                }
                bmp.recycle()
                delay(16)
            }
        }
        activeJobs.add(job)
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        val limit = sqrt(n.toDouble()).toInt()
        for (i in 2..limit) if (n % i == 0) return false
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
