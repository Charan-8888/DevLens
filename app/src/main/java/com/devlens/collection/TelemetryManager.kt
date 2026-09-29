package com.devlens.collection

import android.content.Context
import com.devlens.data.AppDatabase
import com.devlens.data.entities.TelemetrySample
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Coordinates all collectors and produces a unified time-series of TelemetrySamples.
 *
 * Sampling interval: 500ms for CPU/memory (balanced between accuracy and battery).
 * FPS is measured continuously via Choreographer (every frame).
 */
class TelemetryManager(context: Context) {

    private val appContext = context.applicationContext
    private val frameCollector = FrameCollector()
    private val cpuCollector = CpuCollector()
    private val memoryCollector = MemoryCollector(appContext)
    private val db = AppDatabase.getInstance(appContext)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var samplingJob: Job? = null

    var sessionId: String = UUID.randomUUID().toString()
        private set

    private val _samples = MutableStateFlow<List<TelemetrySample>>(emptyList())
    val samples: StateFlow<List<TelemetrySample>> = _samples

    private val _latestSample = MutableStateFlow<TelemetrySample?>(null)
    val latestSample: StateFlow<TelemetrySample?> = _latestSample

    private val localSamples = mutableListOf<TelemetrySample>()

    val isRunning: Boolean get() = samplingJob?.isActive == true

    /**
     * Starts a new monitoring session.
     * Resets session ID, clears local samples, and begins polling.
     */
    fun startSession() {
        if (isRunning) stopSession()
        sessionId = UUID.randomUUID().toString()
        localSamples.clear()
        _samples.value = emptyList()

        // Must call from main thread because Choreographer requires it
        cpuCollector.reset()
        frameCollector.start()

        samplingJob = scope.launch {
            while (isActive) {
                delay(500)
                takeSample()
            }
        }
    }

    fun stopSession() {
        samplingJob?.cancel()
        samplingJob = null
        frameCollector.stop()
    }

    /** Captures an immediate snapshot to use as "before" baseline for verification */
    suspend fun captureSnapshot(): TelemetrySample? {
        return _latestSample.value
    }

    /** Returns the last N samples from the current session */
    fun recentSamples(n: Int = 20): List<TelemetrySample> {
        return localSamples.takeLast(n)
    }

    /** Returns all samples in a time window (relative ms from now) */
    fun samplesInWindow(windowMs: Long = 10_000): List<TelemetrySample> {
        val cutoff = System.currentTimeMillis() - windowMs
        return localSamples.filter { it.timestamp >= cutoff }
    }

    private suspend fun takeSample() {
        val ts = System.currentTimeMillis()
        val frameMetrics = frameCollector.metrics.value
        val cpuPercent = cpuCollector.sampleCpuPercent()
        val memSample = withContext(Dispatchers.IO) { memoryCollector.sample() }

        val sample = TelemetrySample(
            sessionId = sessionId,
            timestamp = ts,
            fps = frameMetrics.fps,
            frameTimeMs = frameMetrics.avgFrameTimeMs,
            cpuPercent = cpuPercent,
            memoryMb = memSample.pssMb,
            jankDetected = frameMetrics.jankDetected
        )

        localSamples.add(sample)
        _latestSample.value = sample
        _samples.value = localSamples.toList()

        // Persist to DB (fire and forget - non-blocking)
        withContext(Dispatchers.IO) {
            db.telemetryDao().insert(sample)
        }
    }
}
