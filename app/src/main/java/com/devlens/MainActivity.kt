package com.devlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devlens.demo.StressScenarios
import com.devlens.ui.*
import com.devlens.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: DevLensViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DevLensTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBg)
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    DevLensNavigation(
                        uiState = uiState,
                        onStartMonitoring = { viewModel.startMonitoring() },
                        onStopMonitoring = { viewModel.stopMonitoring() },
                        onTriggerScenario = { viewModel.triggerScenario(it) },
                        onInvestigateIncident = { viewModel.startInvestigation() },
                        onStartVerification = { viewModel.startVerification() },
                        onApplyFixAndVerify = { viewModel.applyFixAndVerify(it) },
                        onSaveAndReset = { viewModel.resetToMonitoring() },
                        onDismissError = { viewModel.dismissError() }
                    )
                }
            }
        }
    }
}

@Composable
fun DevLensNavigation(
    uiState: DevLensViewModel.UiState,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onTriggerScenario: (StressScenarios.Scenario) -> Unit,
    onInvestigateIncident: () -> Unit,
    onStartVerification: () -> Unit,
    onApplyFixAndVerify: (String) -> Unit,
    onSaveAndReset: () -> Unit,
    onDismissError: () -> Unit
) {
    // Route based on app state
    when (uiState.appState) {
        DevLensViewModel.AppState.IDLE -> {
            HomeScreen(
                uiState = uiState,
                onStartMonitoring = onStartMonitoring
            )
        }

        DevLensViewModel.AppState.MONITORING,
        DevLensViewModel.AppState.INCIDENT_DETECTED -> {
            MonitorScreen(
                uiState = uiState,
                onTriggerScenario = onTriggerScenario,
                onStopMonitoring = onStopMonitoring,
                onInvestigateIncident = onInvestigateIncident
            )
        }

        DevLensViewModel.AppState.INVESTIGATING,
        DevLensViewModel.AppState.RECOMMENDATION_READY -> {
            InvestigationScreen(
                uiState = uiState,
                onStartVerification = onStartVerification,
                onBack = { onStopMonitoring() }
            )
        }

        DevLensViewModel.AppState.WAITING_FOR_FIX,
        DevLensViewModel.AppState.VERIFYING,
        DevLensViewModel.AppState.OUTCOME_DETECTED,
        DevLensViewModel.AppState.EXPERIENCE_SAVED -> {
            VerificationScreen(
                uiState = uiState,
                onApplyFixAndVerify = onApplyFixAndVerify,
                onSaveAndReset = onSaveAndReset,
                onTriggerScenario = onTriggerScenario,
                onBack = {
                    // Go back to investigation
                }
            )
        }
    }
}
