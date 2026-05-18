# Swara

Swara is an offline-first Android emergency guide and AI assistant built with Gemma 4 LiteRT-LM for situations where internet access may be unavailable.

It combines a readable survival guide, on-device AI responses, QR text sharing, and local hotspot distribution so one prepared phone can help other phones get the app, model, and guide content without relying on cloud services.

Kaggle writeup: [Swara - Gemma 4 Good Hackathon](https://www.kaggle.com/competitions/gemma-4-good-hackathon/writeups/Swara)
## Features

- **Offline survival guide**
  - Built-in emergency guide categories for medical, fire, flood, earthquake, violence or personal safety, lost, and general emergencies.
  - Guide-first UX designed for anxious, tired, or disconnected users.
  - Quick Help, Detailed Steps, and Do Not sections for readable emergency guidance.

- **Ask Swara**
  - On-device Gemma 4 LiteRT-LM chat for adapting guide steps to the user's situation.
  - Local chat sessions saved on-device.
  - Conversation memory summary injected into later prompts to reduce repeated answers.
  - Voice input and spoken replies supported where device permissions allow it.

- **Guide sharing**
  - QR sharing for guide content and Swara responses.
  - Human-readable shared text payloads designed for phone-to-phone transfer.
  - Per-guide QR sharing from guide detail cards.

- **Local distribution**
  - Local hotspot or Wi-Fi server for downloading the Swara APK.
  - Survival pack TXT/JSON download from the host phone.
  - Imported model sharing when the sender device has a model available.
  - Single APK distribution path so receiver phones can install Swara and share it again offline.

- **Web Host**
  - Local browser UI served from the host phone.
  - Web clients can read guides and ask Swara through the host phone.
  - The model file is not exposed directly to web clients; inference stays on the host device.

- **Knowledge marketplace prototype**
  - Demo Add Guides flow.
  - Two simulated downloadable guide modules installed through the same local module pipeline intended for future real marketplace downloads.
  - Local PDF, TXT, and Markdown guide import entry point.

## Download

Get the APK from [Releases](https://github.com/AndiftiUKDW/Project-Swara/releases)

For offline phone-to-phone delivery, open:

```text
Settings -> Share App & Model -> Start server
```

Then connect the receiver phone to the same hotspot or Wi-Fi network and open the shown local URL.


## Model Setup

Swara uses Gemma 4 LiteRT-LM on device.

The app supports two model setup paths:

- **Download model** from the app when internet is available.
- **Import model manually** from local storage when the model file already exists on the device.

Current default model source:

```text
https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/blob/main/gemma-4-E2B-it.litertlm
```

The app stores imported or downloaded models locally and uses LiteRT-LM for inference on the phone.

## Development

Common build commands:

```powershell
.\gradlew.bat -g ".gradle-home" :app:compileDebugKotlin
.\gradlew.bat -g ".gradle-home" assembleDebug
```

Main implementation areas:

- UI and navigation: [`AppScreen.kt`](app/src/main/java/com/swara/app/ui/AppScreen.kt)
- App orchestration: [`MainViewModel.kt`](app/src/main/java/com/swara/app/ui/MainViewModel.kt)
- Gemma LiteRT-LM usage: [`GemmaChatService.kt`](app/src/main/java/com/swara/app/services/GemmaChatService.kt)
- Model import/download: [`ModelManager.kt`](app/src/main/java/com/swara/app/services/ModelManager.kt)
- Built-in guides and marketplace modules: [`SurvivalPackRepository.kt`](app/src/main/java/com/swara/app/data/repo/SurvivalPackRepository.kt)
- Local APK/model/pack sharing: [`DistributionServer.kt`](app/src/main/java/com/swara/app/services/DistributionServer.kt)
- Local web app host: [`WebHostServer.kt`](app/src/main/java/com/swara/app/services/WebHostServer.kt)
- QR and share payload formatting: [`SharingPayloads.kt`](app/src/main/java/com/swara/app/ui/SharingPayloads.kt)


## Known MVP Limitations

- Marketplace downloads are simulated, but they install through the real local guide module pipeline.
- Web Host is intended only for trusted local networks.
- Emergency guidance is informational and does not replace local emergency services, medical professionals, or official disaster response teams.
- Model quality depends on the imported Gemma LiteRT-LM file and device runtime performance.

## License

No explicit license has been added yet. Add one before public release or external reuse.
