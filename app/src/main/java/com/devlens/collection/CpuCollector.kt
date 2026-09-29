package com.devlens.collection

import java.io.BufferedReader
import java.io.FileReader

/**
 * Measures CPU usage of the current process by reading /proc/self/stat.
 *
 * /proc/self/stat is always readable without any permissions.
 * We take two readings and compute the delta to get % CPU used.
 *
 * Format (relevant fields):
 *   pid(1) comm(2) state(3) ppid(4) ... utime(14) stime(15) ...
 *   Fields 14+15 = process CPU time in clock ticks (jiffies).
 *
 * Formula:
 *   delta_process_time / delta_total_time * 100 = CPU %
 */
class CpuCollector {

    private data class CpuSnapshot(
        val processTicks: Long,   // utime + stime from /proc/self/stat
        val totalTicks: Long      // sum of all values from /proc/stat
    )

    private var lastSnapshot: CpuSnapshot? = null

    /**
     * Returns the CPU % used by this process since the last call.
     * First call always returns 0.0 (no baseline yet).
     */
    fun sampleCpuPercent(): Float {
        val current = readSnapshot() ?: return 0f
        val prev = lastSnapshot
        lastSnapshot = current

        if (prev == null) return 0f

        val totalDelta = current.totalTicks - prev.totalTicks
        val processDelta = current.processTicks - prev.processTicks

        if (totalDelta <= 0) return 0f

        return ((processDelta.toFloat() / totalDelta.toFloat()) * 100f)
            .coerceIn(0f, 100f)
    }

    fun reset() {
        lastSnapshot = null
    }

    private fun readSnapshot(): CpuSnapshot? {
        return try {
            val processTicks = readProcessTicks()
            val totalTicks = readTotalTicks()
            if (processTicks < 0 || totalTicks < 0) null
            else CpuSnapshot(processTicks, totalTicks)
        } catch (e: Exception) {
            null
        }
    }

    /** Reads utime + stime from /proc/self/stat */
    private fun readProcessTicks(): Long {
        return try {
            val stat = java.io.File("/proc/self/stat").readText()
            // Find closing paren to skip the comm field which may contain spaces
            val afterComm = stat.indexOf(')') + 2
            val fields = stat.substring(afterComm).trim().split(" ")
            // utime is field 14 (index 11 after comm), stime is 15 (index 12)
            val utime = fields[11].toLong()
            val stime = fields[12].toLong()
            utime + stime
        } catch (e: Exception) {
            -1L
        }
    }

    /** Reads total CPU ticks from /proc/stat (first line: cpu ...) */
    private fun readTotalTicks(): Long {
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine() ?: return -1L
            reader.close()
            // "cpu  user nice system idle iowait irq softirq steal guest guest_nice"
            val values = line.trim().split("\\s+".toRegex()).drop(1)
            values.sumOf { it.toLongOrNull() ?: 0L }
        } catch (e: Exception) {
            -1L
        }
    }
}
