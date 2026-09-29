# DevLens 🔍

> **AI-powered Android performance investigator** — collects runtime evidence, detects performance incidents, investigates root causes with a local LLM, verifies fixes, and learns from every experience.

---

## What is DevLens?

DevLens is **not** a dashboard that merely shows CPU, memory, and FPS numbers.

It implements a complete **investigation loop**:

```
Your Android App
      ↓
Runtime Evidence Collection   (CPU · Memory · FPS · Frame time)
      ↓
Performance Incident Detection  (deterministic thresholds, no LLM)
      ↓
Evidence Correlation
      ↓
AI Investigation  ← Local on-device LLM (SmolLM2 Q4 via llama.cpp)
      ↓
Hypotheses + Evidence
      ↓
Recommended Fix
      ↓
Developer Applies Fix
      ↓
Re-run / Verification  (Before vs After comparison)
      ↓
Did the Fix Work?
      ↓
Store Investigation Experience  (Room DB hindsight memory)
      ↓
Re-used in Future Similar Incidents
```

The LLM is **never** used for threshold decisions — all incident detection is deterministic. The LLM provides structured hypotheses and recommendations *after* the deterministic engine has already confirmed an incident.

---

## Architecture

```
DevLens_New_Version/
├── app/                            # Main DevLens app
│   └── src/main/java/com/devlens/
│       ├── collection/             # Telemetry collectors
│       │   ├── CpuCollector.kt     # /proc/self/stat → wall-clock CPU %
│       │   ├── MemoryCollector.kt  # ActivityManager PSS
│       │   ├── FrameCollector.kt   # Choreographer frame times
│       │   └── TelemetryManager.kt # Unified 500ms sampler
│       ├── detection/              # Deterministic incident detection
│       │   ├── IncidentDetector.kt
│       │   ├── ThresholdConfig.kt
│       │   ├── BaselineMetrics.kt
│       │   └── IncidentContextBuilder.kt
│       ├── investigation/          # AI investigation pipeline
│       │   ├── InvestigationEngine.kt
│       │   ├── EvidenceCorrelator.kt
│       │   ├── PromptBuilder.kt
│       │   └── AiResponseParser.kt
│       ├── ai/
│       │   └── LocalLlm.kt         # llama.cpp bridge (SmolLM2 Q4 GGUF)
│       ├── verification/
│       │   └── VerificationEngine.kt  # Before/After comparison
│       ├── hindsight/
│       │   └── HindsightEngine.kt     # Experience persistence & retrieval
│       ├── data/                   # Room database entities & DAOs
│       ├── ui/                     # Compose screens
│       │   ├── HomeScreen.kt
│       │   ├── MonitorScreen.kt
│       │   ├── InvestigationScreen.kt
│       │   └── VerificationScreen.kt
│       ├── DevLensViewModel.kt     # State machine
│       └── MainActivity.kt
│
└── demo/                           # Controlled target app for demos
    └── src/main/java/com/devlens/demo/
        ├── DemoMainActivity.kt     # Scenario selector UI
        └── StressScenarios.kt      # CPU · Memory · Rendering stress
```

---

## Key Features

| Feature | Details |
|---|---|
| **Real-time telemetry** | CPU (process), Memory (PSS), FPS, Frame time @ 500ms |
| **Deterministic detection** | Threshold-based, no LLM in the hot path |
| **Local LLM** | SmolLM2 1.7B Q4_K_M via llama.cpp — fully on-device, no internet |
| **Structured investigation** | Evidence correlation → prompt → structured JSON response → parsed hypotheses |
| **Before/After verification** | Captures metric snapshot before fix, re-measures after, computes delta |
| **Hindsight memory** | Failed/successful investigations stored in Room DB, retrieved for similar future incidents |
| **Demo target app** | Separate APK with controlled CPU · Memory · Rendering · Combined stress scenarios |

---

## State Machine

```
IDLE
  └─→ MONITORING          (baseline capture, 4s)
        └─→ INCIDENT_DETECTED
              └─→ INVESTIGATING      (local LLM running)
                    └─→ RECOMMENDATION_READY
                          └─→ WAITING_FOR_FIX
                                └─→ VERIFYING         (3s stabilisation)
                                      └─→ OUTCOME_DETECTED
                                            └─→ EXPERIENCE_SAVED
                                                  └─→ back to MONITORING
```

---

## Project Structure — Modules

### `:app` — DevLens Investigator

The investigator app. It monitors **its own process** metrics (CPU, memory, rendering) via:
- `/proc/self/stat` for CPU ticks (wall-clock delta formula, Android 8+ compatible)
- `ActivityManager.getProcessMemoryInfo()` for PSS memory
- `Choreographer.FrameCallback` for frame timing

### `:demo` — Demo Target App (`com.devlens.demo`)

A completely separate APK installed alongside DevLens. It provides:
- **Normal Mode** — idle baseline
- **CPU Stress** — prime sieve + 100×100 matrix multiply across all available cores
- **Memory Stress** — 50 × 1MB allocation cycles to trigger GC jank
- **Rendering Stress** — large off-screen bitmap with hundreds of canvas ops
- **Combined Incident** — all three simultaneously
- **Recovery** — stops all stress to demonstrate metric normalisation

---

## Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| JDK | 21 (tested with Microsoft OpenJDK 21.0.12) |
| Android SDK | API 34 (Build-Tools 34.x) |
| Gradle | 8.10.2 (wrapper included) |
| Android device | API 26+ (Android 8.0+) with USB debugging enabled |

### Build & Install

```bash
# 1. Clone the repository
git clone https://github.com/Charan-8888/DevLens.git
cd DevLens

# 2. Set environment variables (or configure local.properties)
export ANDROID_HOME=/path/to/android-sdk
export JAVA_HOME=/path/to/jdk-21

# 3. Build and install the investigator app
./gradlew :app:installDebug

# 4. Build and install the demo target app
./gradlew :demo:installDebug

# 5. (Optional) Build both in one command
./gradlew :app:installDebug :demo:installDebug
```

### Enable USB Installation on Vivo / iQOO / Xiaomi

On certain OEM devices, you must enable **"Install via USB"** (a separate toggle from USB Debugging) in **Developer Options** before `adb install` will succeed.

---

## How to Run a Demo

1. Open the **DevLens Demo** app on your phone and tap **Combined Incident** (or any scenario).
2. Switch to the **DevLens** app and tap **Start Monitoring**.
3. Wait ~4 seconds for the baseline to be captured (green "ACTIVE" badge appears).
4. Tap **Trigger** on any stress scenario — CPU % will spike and FPS will drop.
5. DevLens automatically detects the incident within ~1–2 seconds.
6. Tap **"Investigate Incident →"** to run the on-device LLM investigation.
7. Review the AI-generated hypothesis and recommended fix.
8. Tap **"Start Verification"**, apply the fix (or tap Recovery in the Demo app), then tap **"Apply Fix & Verify"**.
9. DevLens compares Before vs After and saves the experience to its memory.

---

## Local LLM

DevLens uses **SmolLM2 1.7B Instruct Q4_K_M** (GGUF format) via the `llama.cpp` Android library.

- **Model size**: ~87 MB
- **Quantization**: Q4_K_M (4-bit, good quality/speed tradeoff)
- **Context window**: 512 tokens (investigation prompts are structured to fit)
- **Threads**: 4 (configurable in `LocalLlm.kt`)
- **No internet required**: completely on-device inference

The model is bundled as an asset and copied to internal storage on first launch. The prompt is fully structured (no free-text hallucination path) and the response is parsed as JSON for reliable hypothesis extraction.

---

## Detection Thresholds

All thresholds are configurable via `ThresholdConfig.kt`:

| Metric | Warning | Critical |
|---|---|---|
| FPS | < 30 fps | < 20 fps |
| Frame time | > 33 ms | > 50 ms |
| CPU | > 80% | > 90% |
| Memory growth | > 50 MB over 10 samples | — |

An incident must sustain for **3 consecutive samples (~1.5 seconds)** before being declared, preventing false positives from momentary spikes.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + StateFlow |
| Database | Room (SQLite) |
| Coroutines | Kotlin Coroutines + Dispatchers.Default |
| Local AI | SmolLM2 Q4 via llama.cpp (custom AAR) |
| Telemetry | /proc/self/stat · ActivityManager · Choreographer |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

---

## Hackathon Context

DevLens was built for the **iQOO Hackathon** as a proof-of-concept for a fully local, AI-powered Android performance investigation tool. The entire pipeline — from raw metric collection through LLM investigation to experience-based learning — runs on the device with no cloud dependencies.

---

## License

This project is a hackathon prototype. All rights reserved.
