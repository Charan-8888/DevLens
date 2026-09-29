package com.devlens.demo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Collection of stress scenarios that run WITHIN the DevLens process.
 * Each scenario produces measurable CPU and/or memory pressure that
 * DevLens then observes via its own telemetry collectors.
 *
 * Scenarios are designed for reliable, reproducible demo incidents.
 */
object StressScenarios {

    enum class Scenario {
        NONE,
        CPU_STRESS,
        MEMORY_STRESS,
        RENDERING_STRESS,
        COMBINED
    }

    private var activeJob: Job? = null

    /**
     * Starts the specified stress scenario in a background coroutine.
     * Automatically stops after [durationMs] milliseconds.
     */
    fun start(
        scenario: Scenario,
        scope: CoroutineScope,
        durationMs: Long = 8000L,
        onCompleted: (() -> Unit)? = null
    ) {
        stop()
        if (scenario == Scenario.NONE) return

        val numWorkers = if (scenario == Scenario.CPU_STRESS || scenario == Scenario.COMBINED) {
            Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
        } else 1

        activeJob = scope.launch(Dispatchers.Default) {
            val deadline = System.currentTimeMillis() + durationMs

            val workers = List(numWorkers) {
                launch {
                    while (isActive && System.currentTimeMillis() < deadline) {
                        when (scenario) {
                            Scenario.CPU_STRESS -> cpuStressIteration()
                            Scenario.MEMORY_STRESS -> memoryStressIteration()
                            Scenario.RENDERING_STRESS -> renderingStressIteration()
                            Scenario.COMBINED -> {
                                cpuStressIteration()
                                memoryStressIteration()
                            }
                            Scenario.NONE -> {}
                        }
                    }
                }
            }
            workers.forEach { it.join() }
            // Notify caller so UI state can be reset
            onCompleted?.invoke()
        }
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
    }

    val isRunning: Boolean get() = activeJob?.isActive == true

    /**
     * Heavy CPU scenario: expensive prime-number calculation, matrix operations.
     * Targets 80-95% CPU utilization on the main cores.
     */
    private fun cpuStressIteration() {
        // Expensive: compute primes using trial division
        var count = 0
        var n = 2
        while (count < 2000) {
            if (isPrime(n)) count++
            n++
        }

        // Matrix multiplication (100x100)
        val size = 100
        val a = Array(size) { DoubleArray(size) { Random.nextDouble() } }
        val b = Array(size) { DoubleArray(size) { Random.nextDouble() } }
        val c = Array(size) { DoubleArray(size) }
        for (i in 0 until size) {
            for (j in 0 until size) {
                for (k in 0 until size) {
                    c[i][j] += a[i][k] * b[k][j]
                }
            }
        }

        // Trigonometric series (computationally expensive)
        var sum = 0.0
        for (i in 1..5000) {
            sum += sin(i.toDouble()) * cos(i.toDouble()) * sqrt(i.toDouble())
        }
    }

    /**
     * Memory pressure scenario: rapidly allocates and abandons large byte arrays
     * to trigger frequent GC cycles, which cause jank.
     */
    private fun memoryStressIteration() {
        val arrays = mutableListOf<ByteArray>()
        // Allocate 50 chunks of 1MB each
        repeat(50) {
            arrays.add(ByteArray(1024 * 1024) { it.toByte() })
        }
        // Hold briefly then release (triggers GC)
        Thread.sleep(50)
        arrays.clear()
        System.gc()
    }

    /**
     * Rendering stress scenario: creates large bitmaps with expensive canvas operations.
     * Uses CPU-based rendering that would stall the UI thread if done there.
     */
    private fun renderingStressIteration() {
        // Create a large bitmap and draw complex patterns onto it
        val bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw many concentric circles (expensive blending)
        for (r in 10..500 step 5) {
            paint.color = Color.argb(
                Random.nextInt(100, 200),
                Random.nextInt(0, 255),
                Random.nextInt(0, 255),
                Random.nextInt(0, 255)
            )
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawCircle(512f, 512f, r.toFloat(), paint)
        }

        // Draw random lines
        repeat(200) {
            paint.color = Color.argb(150, Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
            canvas.drawLine(
                Random.nextFloat() * 1024,
                Random.nextFloat() * 1024,
                Random.nextFloat() * 1024,
                Random.nextFloat() * 1024,
                paint
            )
        }

        bitmap.recycle()
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false
        val limit = sqrt(n.toDouble()).toInt()
        for (i in 3..limit step 2) {
            if (n % i == 0) return false
        }
        return true
    }
}
