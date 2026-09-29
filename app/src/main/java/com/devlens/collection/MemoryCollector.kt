package com.devlens.collection

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

/**
 * Measures memory usage of the current process.
 *
 * Uses ActivityManager.getProcessMemoryInfo() to get PSS (Proportional Set Size),
 * which is the most accurate single-number for "how much memory does this app use".
 *
 * Also reads Debug.MemoryInfo for heap breakdown.
 */
class MemoryCollector(private val context: Context) {

    data class MemorySample(
        val pssMb: Float,        // Proportional Set Size - the main number
        val heapUsedMb: Float,   // Java heap used
        val nativeHeapMb: Float  // Native heap
    )

    private val activityManager: ActivityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    fun sample(): MemorySample {
        return try {
            val pid = android.os.Process.myPid()
            val memInfoArray = activityManager.getProcessMemoryInfo(intArrayOf(pid))
            val memInfo = memInfoArray.firstOrNull()

            val pssMb = (memInfo?.totalPss ?: 0) / 1024f
            val heapMb = (memInfo?.totalPrivateDirty ?: 0) / 1024f

            // Native heap from Debug
            val nativeMb = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)

            MemorySample(
                pssMb = pssMb,
                heapUsedMb = heapMb,
                nativeHeapMb = nativeMb
            )
        } catch (e: Exception) {
            // Fallback to runtime memory stats
            val runtime = Runtime.getRuntime()
            val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
            MemorySample(pssMb = usedMb, heapUsedMb = usedMb, nativeHeapMb = 0f)
        }
    }
}
