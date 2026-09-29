package com.devlens.collection

import android.os.SystemClock

/**
 * Measures CPU usage of the current process by reading /proc/self/stat.
 *
 * Android 8+ restricts access to /proc/stat (system-wide), so we measure
 * CPU usage as: (process_cpu_ticks_delta / wall_clock_delta_in_ticks) * 100
 *
 * Since /proc/self/stat is always readable, we express the result as
 * "percentage of a single core" and then normalise across all cores so
 * the value is comparable to a typical "CPU %" display (0–100%).
 *
 * Formula:
 *   cpu% = (process_ticks_delta / (wallMs_delta * HZ / 1000)) / numCores * 100
 *
 * HZ (clock ticks per second) is typically 100 on Android (USER_HZ).
 */
class CpuCollector {

    private data class CpuSnapshot(
        val processTicks: Long,   // utime + stime from /proc/self/stat
        val wallTimeMs: Long      // SystemClock.elapsedRealtime()
    )

    private val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
    // USER_HZ is 100 on virtually every Android device
    private val userHz = 100L

    private var lastSnapshot: CpuSnapshot? = null

    /**
     * Returns the CPU % used by this process across all cores since the last call.
     * Range: 0–100 (already normalised per-core so it reflects real load).
     * First call always returns 0.0 (no baseline yet).
     */
    fun sampleCpuPercent(): Float {
        val current = readSnapshot() ?: return 0f
        val prev = lastSnapshot
        lastSnapshot = current

        if (prev == null) return 0f

        val wallMs = current.wallTimeMs - prev.wallTimeMs
        if (wallMs <= 0) return 0f

        val processDelta = current.processTicks - prev.processTicks
        if (processDelta < 0) return 0f

        // Convert wall time to ticks at USER_HZ (100 ticks/s)
        val wallTicks = wallMs * userHz / 1000L
        if (wallTicks <= 0) return 0f

        // Raw percentage across all cores combined
        val rawPercent = (processDelta.toFloat() / (wallTicks.toFloat() * numCores)) * 100f
        return rawPercent.coerceIn(0f, 100f)
    }

    fun reset() {
        lastSnapshot = null
    }

    private fun readSnapshot(): CpuSnapshot? {
        val processTicks = readProcessTicks()
        if (processTicks < 0) return null
        return CpuSnapshot(processTicks, SystemClock.elapsedRealtime())
    }

    /** Reads utime + stime from /proc/self/stat (always accessible, no permissions needed) */
    private fun readProcessTicks(): Long {
        return try {
            val stat = java.io.File("/proc/self/stat").readText()
            // Skip comm field (may contain spaces and parens) by finding closing ')'
            val afterComm = stat.indexOf(')') + 2
            val fields = stat.substring(afterComm).trim().split(" ")
            // utime = index 11 (field 14), stime = index 12 (field 15) relative to afterComm
            val utime = fields[11].toLong()
            val stime = fields[12].toLong()
            utime + stime
        } catch (e: Exception) {
            -1L
        }
    }
}
