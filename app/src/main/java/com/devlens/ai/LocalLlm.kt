package com.devlens.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import dev.ffmpegkit.llama.LlamaResult

/**
 * Local LLM bridge using the bundled GGUF model (SmolLM2 Q4).
 *
 * Design principles:
 * 1. Model receives structured prompts, not raw telemetry
 * 2. Each invocation creates a fresh session for independent outputs
 * 3. LLM is NEVER used for threshold decisions (those are deterministic)
 * 4. If the model fails, investigation still shows deterministic evidence
 */
class LocalLlm(private val context: Context) {

    sealed class LlmState {
        object Idle : LlmState()
        data class Loading(val progress: String) : LlmState()
        object Ready : LlmState()
        data class Inferring(val tokenCount: Int) : LlmState()
        data class Error(val message: String) : LlmState()
    }

    data class LlmAnswer(
        val text: String,
        val tokens: Int,
        val tokensPerSecond: Float,
        val generationMs: Long
    )

    private val _state = MutableStateFlow<LlmState>(LlmState.Idle)
    val state: StateFlow<LlmState> = _state

    private val appContext = context.applicationContext
    private val modelFileName = "devlens-smollm2-q4-standard.gguf"
    private val modelCopyName = "devlens-model-v2.gguf"

    /**
     * Runs inference with the given prompt.
     * This is a suspend function — call from a coroutine.
     */
    suspend fun investigate(prompt: String): Result<LlmAnswer> = withContext(Dispatchers.IO) {
        var model: LlamaModel? = null
        try {
            _state.value = LlmState.Loading("Preparing model...")
            val modelFile = ensureModel()

            _state.value = LlmState.Loading("Loading model weights...")
            model = loadModel(modelFile)

            _state.value = LlmState.Inferring(0)
            val result = runInference(model, prompt)

            _state.value = LlmState.Idle
            Result.success(LlmAnswer(
                text = result.text.trim(),
                tokens = result.tokensGenerated,
                tokensPerSecond = result.tokensPerSecond,
                generationMs = result.generateTimeMs
            ))
        } catch (e: Exception) {
            _state.value = LlmState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        } finally {
            model?.let {
                try { Llama.releaseModel(it) } catch (_: Exception) {}
            }
        }
    }

    private fun ensureModel(): File {
        val target = File(appContext.filesDir, modelCopyName)
        if (target.exists() && target.length() > 80_000_000L) return target

        _state.value = LlmState.Loading("Copying model to storage...")
        copyAsset(modelFileName, target)
        return target
    }

    private fun copyAsset(assetName: String, target: File) {
        val partial = File(target.parentFile, "${target.name}.partial")
        if (partial.exists()) partial.delete()

        appContext.assets.open(assetName).use { input ->
            FileOutputStream(partial).use { output ->
                val buffer = ByteArray(1024 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
            }
        }

        if (!partial.renameTo(target)) {
            throw Exception("Could not finalize model copy to ${target.name}")
        }
    }

    private suspend fun loadModel(file: File): LlamaModel {
        val config = LlamaConfig(
            /* contextSize */ 512,
            /* threads */ 4,
            /* batchSize */ 0,
            /* temperature */ 0.7f,
            /* topP */ 0.95f,
            /* repeatPenalty */ 60,
            /* seed */ (System.nanoTime() and 0x7fffffff).toInt()
        )
        return Llama.loadModel(file.absolutePath, config)
    }

    private suspend fun runInference(model: LlamaModel, prompt: String): LlamaResult {
        return Llama.complete(
            model,
            prompt,
            "Respond concisely. Only use the provided evidence. Do not invent metrics.",
            200
        )
    }
}
