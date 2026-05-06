# Gemma Voice RAG Android Prototype

Android app prototype for:
- on-device `Gemma 4 E2B` inference with LiteRT-LM
- push-to-talk voice input
- spoken assistant replies
- local document RAG over `PDF`, `TXT`, and `MD`

## What is implemented
- Jetpack Compose single-module Android app scaffold
- LiteRT-LM integration path for `.litertlm` model import
- local document ingestion with `pdfbox-android`
- chunking + Room/FTS retrieval
- grounded prompt assembly with citations
- speech-to-text with `SpeechRecognizer`
- text-to-speech with `TextToSpeech`

## Current environment blockers
This workspace did not have a local Android SDK configured during implementation.

Verified locally:
- Gradle wrapper boots with `Gradle 8.7`
- project configuration loads under `JDK 21`

Not verified locally:
- `assembleDebug`
- Kotlin/Compose compilation
- runtime inference on a physical device

## Local setup
1. Install Android SDK and at least one platform matching `compileSdk = 35`.
2. Create `local.properties` in the repo root:

```properties
sdk.dir=C\\:\\path\\to\\Android\\Sdk
```

3. Use `JDK 21` for Gradle.
4. Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

## Model setup
- Import a local Gemma `.litertlm` file from inside the app.
- The app currently expects a filename ending in `.litertlm`.
- The model is copied to app-managed storage at runtime, not bundled in the APK.

## Notes
- Retrieval in v1 uses local keyword search with Room FTS, not embeddings.
- Citations are attached to responses from retrieved chunks.
- MCP is not implemented in v1, but retrieval is isolated behind repository/service layers for future extension.
