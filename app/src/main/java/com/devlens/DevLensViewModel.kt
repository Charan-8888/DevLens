package com.devlens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devlens.collection.TelemetryManager
import com.devlens.data.entities.TelemetrySample
import com.devlens.demo.StressScenarios
import com.devlens.detection.BaselineMetrics
import com.devlens.detection.IncidentContextBuilder
import com.devlens.detection.IncidentDetector
import com.devlens.detection.IncidentSeverity
import com.devlens.detection.IncidentType
import com.devlens.detection.ThresholdConfig
import com.devlens.hindsight.HindsightEngine
import com.devlens.investigation.InvestigationEngine
import com.devlens.verification.VerificationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Main ViewModel managing the DevLens state machine.
 *
 * State machine:
 * IDLE → MONITORING → INCIDENT_DETECTED → INVESTIGATING →
 * RECOMMENDATION_READY → VERIFYING → OUTCOME_DETECTED → EXPERIENCE_SAVED
 */
class DevLensViewModel(application: Application) : AndroidViewModel(application) {

    // ─── State machine ────────────────────────────────────────────────────────
    enum class AppState {
        IDLE,
        MONITORING,
        INCIDENT_DETECTED,
        INVESTIGATING,
        RECOMMENDATION_READY,
        WAITING_FOR_FIX,
        VERIFYING,
        OUTCOME_DETECTED,
        EXPERIENCE_SAVED
    }

    data class UiState(
        val appState: AppState = AppState.IDLE,
        val activeScenario: StressScenarios.Scenario = StressScenarios.Scenario.NONE,
        val currentSample: TelemetrySample? = null,
        val recentSamples: List<TelemetrySample> = emptyList(),
        val baseline: BaselineMetrics? = null,
        val incident: IncidentContextBuilder.IncidentContext? = null,
        val investigation: InvestigationEngine.InvestigationPackage? = null,
        val beforeSnapshot: VerificationEngine.BeforeMetrics? = null,
        val afterSnapshot: VerificationEngine.AfterMetrics? = null,
        val verificationResult: VerificationEngine.VerificationResult? = null,
        val llmState: String = "",
        val errorMessage: String? = null,
        val experienceCount: Int = 0,
        val isBaselineReady: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ─── Engines ─────────────────────────────────────────────────────────────
    private val telemetryManager = TelemetryManager(application)
    private val incidentDetector = IncidentDetector(ThresholdConfig())
    private val contextBuilder = IncidentContextBuilder()
    private val investigationEngine = InvestigationEngine(application)
    private val verificationEngine = VerificationEngine(application)
    private val hindsightEngine = HindsightEngine(application)

    private var capturedBaseline: BaselineMetrics? = null
    private var detectionEnabled = false

    init {
        // Observe LLM state
        viewModelScope.launch {
            investigationEngine.llmState.collect { state ->
                _uiState.update { it.copy(llmState = state.toString()) }
            }
        }

        // Observe telemetry samples
        viewModelScope.launch {
            telemetryManager.samples.collect { samples ->
                val latest = samples.lastOrNull()
                _uiState.update { s ->
                    s.copy(
                        currentSample = latest,
                        recentSamples = samples.takeLast(30)
                    )
                }
                // Run detection when we have enough data and are in MONITORING state
                if (detectionEnabled && samples.size >= 8 && capturedBaseline != null) {
                    detectIncident(samples)
                }
            }
        }

        // Load experience count
        viewModelScope.launch {
            val count = (getApplication<Application>() as DevLensApp)
                .database.experienceDao().count()
            _uiState.update { it.copy(experienceCount = count) }
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    fun startMonitoring() {
        detectionEnabled = false
        capturedBaseline = null
        _uiState.update { it.copy(
            appState = AppState.MONITORING,
            incident = null,
            investigation = null,
            verificationResult = null,
            isBaselineReady = false,
            activeScenario = StressScenarios.Scenario.NONE
        )}
        telemetryManager.startSession()

        // Collect baseline for 4 seconds before enabling detection
        viewModelScope.launch {
            delay(4000)
            val baselineSamples = telemetryManager.recentSamples(8)
            if (baselineSamples.isNotEmpty()) {
                capturedBaseline = BaselineMetrics.fromSamples(baselineSamples)
                _uiState.update { it.copy(baseline = capturedBaseline, isBaselineReady = true) }
                detectionEnabled = true
            }
        }
    }

    fun stopMonitoring() {
        detectionEnabled = false
        StressScenarios.stop()
        telemetryManager.stopSession()
        _uiState.update { it.copy(appState = AppState.IDLE, activeScenario = StressScenarios.Scenario.NONE) }
    }

    fun triggerScenario(scenario: StressScenarios.Scenario) {
        _uiState.update { it.copy(activeScenario = scenario) }
        if (scenario == StressScenarios.Scenario.NONE) {
            StressScenarios.stop()
        } else {
            StressScenarios.start(
                scenario = scenario,
                scope = viewModelScope,
                durationMs = 60_000L,
                onCompleted = {
                    _uiState.update { it.copy(activeScenario = StressScenarios.Scenario.NONE) }
                }
            )
            // Guaranteed fallback: force incident after 5 seconds of stress
            // This ensures the AI pipeline fires even if the threshold detector is too conservative
            viewModelScope.launch {
                delay(5_000)
                if (_uiState.value.appState == AppState.MONITORING &&
                    _uiState.value.activeScenario == scenario) {
                    forceTriggerIncident(scenario)
                }
            }
        }
    }

    private fun forceTriggerIncident(scenario: StressScenarios.Scenario) {
        val samples = telemetryManager.recentSamples(8)
        val baseline = capturedBaseline ?: BaselineMetrics(60f, 16.67f, 1f, 0f)

        val type = when (scenario) {
            StressScenarios.Scenario.CPU_STRESS -> IncidentType.HIGH_CPU
            StressScenarios.Scenario.MEMORY_STRESS -> IncidentType.MEMORY_GROWTH
            StressScenarios.Scenario.RENDERING_STRESS -> IncidentType.FRAME_DROP
            StressScenarios.Scenario.COMBINED -> IncidentType.COMBINED_DEGRADATION
            StressScenarios.Scenario.NONE -> return
        }

        val syntheticIncident = IncidentDetector.DetectedIncident(
            type = type,
            severity = IncidentSeverity.HIGH,
            startIndex = 0,
            triggeringSamples = samples,
            description = "Triggered by ${scenario.name.replace('_', ' ')} scenario"
        )

        val context = contextBuilder.build(
            incident = syntheticIncident,
            allSamples = samples,
            baseline = baseline,
            sessionId = telemetryManager.sessionId,
            triggerScenario = scenario.name
        )

        detectionEnabled = false
        _uiState.update { it.copy(appState = AppState.INCIDENT_DETECTED, incident = context) }

        viewModelScope.launch(Dispatchers.IO) {
            (getApplication<Application>() as DevLensApp)
                .database.incidentDao().insert(context.toEntity())
        }
    }

    fun stopScenario() {
        StressScenarios.stop()
        _uiState.update { it.copy(activeScenario = StressScenarios.Scenario.NONE) }
    }

    fun startInvestigation() {
        val incidentCtx = _uiState.value.incident ?: return
        _uiState.update { it.copy(appState = AppState.INVESTIGATING) }

        viewModelScope.launch {
            try {
                val pkg = investigationEngine.investigate(incidentCtx)
                _uiState.update { it.copy(
                    appState = AppState.RECOMMENDATION_READY,
                    investigation = pkg
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    appState = AppState.INCIDENT_DETECTED,
                    errorMessage = "Investigation failed: ${e.message}"
                )}
            }
        }
    }

    fun startVerification() {
        val incident = _uiState.value.incident ?: return
        val current = _uiState.value.currentSample ?: return

        // Capture before metrics from the incident peak
        val before = VerificationEngine.BeforeMetrics(
            fps = incident.peakFps,
            cpuPercent = incident.peakCpu,
            memoryMb = incident.peakMemoryMb,
            frameTimeMs = incident.peakFrameTimeMs
        )
        _uiState.update { it.copy(
            appState = AppState.WAITING_FOR_FIX,
            beforeSnapshot = before
        )}
    }

    fun applyFixAndVerify(developerAction: String = "Applied recommended fix") {
        _uiState.update { it.copy(appState = AppState.VERIFYING) }
        // Reset to normal operation (no stress)
        StressScenarios.stop()

        viewModelScope.launch {
            // Wait 3 seconds for metrics to stabilize after fix
            delay(3000)
            val afterSample = telemetryManager.latestSample.value ?: return@launch
            val before = _uiState.value.beforeSnapshot ?: return@launch
            val incident = _uiState.value.incident ?: return@launch
            val investigation = _uiState.value.investigation ?: return@launch

            val after = VerificationEngine.AfterMetrics(
                fps = afterSample.fps,
                cpuPercent = afterSample.cpuPercent,
                memoryMb = afterSample.memoryMb,
                frameTimeMs = afterSample.frameTimeMs
            )

            val result = verificationEngine.verify(incident, investigation.id, before, after)

            _uiState.update { it.copy(
                appState = AppState.OUTCOME_DETECTED,
                afterSnapshot = after,
                verificationResult = result
            )}

            // Save experience to hindsight memory
            hindsightEngine.saveExperience(investigation, result, developerAction)
            val newCount = (getApplication<Application>() as DevLensApp)
                .database.experienceDao().count()
            _uiState.update { it.copy(
                appState = AppState.EXPERIENCE_SAVED,
                experienceCount = newCount
            )}
        }
    }

    fun resetToMonitoring() {
        _uiState.update { it.copy(
            appState = AppState.MONITORING,
            incident = null,
            investigation = null,
            beforeSnapshot = null,
            afterSnapshot = null,
            verificationResult = null,
            activeScenario = StressScenarios.Scenario.NONE,
            errorMessage = null
        )}
        capturedBaseline = null
        detectionEnabled = false
        telemetryManager.startSession()

        // Recapture baseline
        viewModelScope.launch {
            delay(4000)
            val baselineSamples = telemetryManager.recentSamples(8)
            if (baselineSamples.isNotEmpty()) {
                capturedBaseline = BaselineMetrics.fromSamples(baselineSamples)
                _uiState.update { it.copy(baseline = capturedBaseline, isBaselineReady = true) }
                detectionEnabled = true
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private fun detectIncident(samples: List<TelemetrySample>) {
        val baseline = capturedBaseline ?: return
        if (_uiState.value.appState != AppState.MONITORING) return

        val incident = incidentDetector.evaluate(samples, baseline) ?: return

        // Build structured context
        val context = contextBuilder.build(
            incident = incident,
            allSamples = samples,
            baseline = baseline,
            sessionId = telemetryManager.sessionId,
            triggerScenario = _uiState.value.activeScenario.name
        )

        // Disable further detection until this incident is resolved
        detectionEnabled = false

        _uiState.update { it.copy(
            appState = AppState.INCIDENT_DETECTED,
            incident = context
        )}

        // Persist the incident
        viewModelScope.launch(Dispatchers.IO) {
            (getApplication<Application>() as DevLensApp)
                .database.incidentDao().insert(context.toEntity())
        }
    }

    override fun onCleared() {
        super.onCleared()
        StressScenarios.stop()
        telemetryManager.stopSession()
    }
}
