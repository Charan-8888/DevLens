package com.devlens.collection

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Measures real FPS using Choreographer frame callbacks.
 *
 * Choreographer delivers one callback per vsync frame. By recording
 * the vsync timestamp for each frame, we can calculate FPS and
 * individual frame durations accurately.
 */
class FrameCollector {

    data class FrameMetrics(
        val fps: Float,
        val avgFrameTimeMs: Float,
        val jankDetected: Boolean,   // any frame > 33.3ms in the window
        val frameCount: Int
    )

    private val _metrics = MutableStateFlow(FrameMetrics(60f, 16.67f, false, 0))
    val metrics: StateFlow<FrameMetrics> = _metrics

    private val frameTimestamps = ArrayDeque<Long>(64)
    private var isRunning = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var choreographer: Choreographer? = null

    private val callback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            val now = frameTimeNanos
            frameTimestamps.addLast(now)

            // Keep a 1-second sliding window (at 60fps = ~60 frames)
            val windowNanos = 1_000_000_000L
            while (frameTimestamps.size > 1 &&
                now - frameTimestamps.first() > windowNanos) {
                frameTimestamps.removeFirst()
            }

            if (frameTimestamps.size >= 2) {
                val windowDurationNs = now - frameTimestamps.first()
                val windowSec = windowDurationNs / 1_000_000_000f
                val fps = (frameTimestamps.size - 1) / windowSec

                // Calculate average frame time from consecutive frames
                var totalFrameTimeMs = 0f
                var jank = false
                for (i in 1 until frameTimestamps.size) {
                    val dtMs = (frameTimestamps[i] - frameTimestamps[i - 1]) / 1_000_000f
                    totalFrameTimeMs += dtMs
                    // Jank = frame takes >2 expected frames at 60fps (33.3ms)
                    if (dtMs > 33.3f) jank = true
                }
                val avgFrameTime = totalFrameTimeMs / (frameTimestamps.size - 1)

                _metrics.value = FrameMetrics(
                    fps = fps.coerceIn(0f, 120f),
                    avgFrameTimeMs = avgFrameTime,
                    jankDetected = jank,
                    frameCount = frameTimestamps.size
                )
            }

            choreographer?.postFrameCallback(this)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        frameTimestamps.clear()
        // Choreographer.getInstance() must be called on a Looper thread (main thread)
        mainHandler.post {
            choreographer = Choreographer.getInstance()
            choreographer?.postFrameCallback(callback)
        }
    }

    fun stop() {
        isRunning = false
        frameTimestamps.clear()
    }

    fun currentFps(): Float = _metrics.value.fps
    fun currentFrameTimeMs(): Float = _metrics.value.avgFrameTimeMs
    fun isJanking(): Boolean = _metrics.value.jankDetected
}
