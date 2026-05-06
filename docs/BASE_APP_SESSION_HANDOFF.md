# Gemma Voice RAG Android: Session Handoff

This file is the working handoff for future coding sessions.

Use this project as a base/reference for follow-up Android AI chat apps with:
- on-device Gemma via LiteRT-LM
- voice input/output
- local document Q&A
- grounded responses with source evidence
- rich chat-style rendering in Compose

## Current Project State

Status:
- app builds successfully
- `:app:compileDebugKotlin` passes
- `assembleDebug` passes
- latest debug APK path:
  - `app/build/outputs/apk/debug/app-debug.apk`

Tested direction:
- real device target used during iteration: `Samsung S23`
- user confirmed:
  - app runs
  - follow-up turns are fast enough after engine reuse
  - TTS is good enough
  - retrieval is good enough for current scope
- still expected to need iteration:
  - malformed model markdown / layout edge cases
  - visual polish from real device screenshots

## What Was Built

Core app:
- single-module Android app
- Kotlin + Jetpack Compose
- Android min SDK `29`
- Compose Material 3 UI

Inference:
- Gemma `.litertlm` import flow
- on-device inference through LiteRT-LM
- engine reuse so the model is not reinitialized per message
- warm-up after model import to reduce first-turn latency

Voice:
- push-to-talk speech input via `SpeechRecognizer`
- spoken assistant replies via `TextToSpeech`
- TTS cleanup path strips markdown/source artifacts before speaking
- voice state coordination so speaking/listening do not overlap badly

Documents / RAG:
- import `PDF`, `TXT`, `MD`
- PDF text extraction via `pdfbox-android`
- chunking and local persisted index
- local retrieval with weighted ranking
- retrieval metadata preserved per message
- evidence/citation UI in the chat

Chat / UX:
- chat-first layout
- bottom composer
- library/settings bottom sheet
- grouped source chips opening evidence sheet
- rich block-style renderer for model answers

## Important Architecture

Main composition root:
- [AppContainer.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/AppContainer.kt)

Key services:
- [GemmaChatService.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/services/GemmaChatService.kt)
- [ModelManager.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/services/ModelManager.kt)
- [VoiceController.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/services/VoiceController.kt)
- [Chunker.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/services/Chunker.kt)

Data / retrieval:
- [DocumentRepository.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/data/repo/DocumentRepository.kt)
- [Models.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/data/model/Models.kt)

UI:
- [MainViewModel.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/MainViewModel.kt)
- [AppScreen.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/AppScreen.kt)
- [MarkdownRenderer.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/MarkdownRenderer.kt)

Theme:
- [Type.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/theme/Type.kt)
- [Theme.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/theme/Theme.kt)
- [Color.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/theme/Color.kt)

## Important Decisions

1. No backend in v1
- app is fully local for current scope
- no MCP/backend integration shipped

2. Retrieval is local and lightweight
- not embedding-based
- current retrieval is improved keyword/hybrid ranking
- chosen to keep phone complexity and latency manageable

3. TTS text is separate from display text
- display preserves formatting
- TTS sanitizes markdown/sources first

4. Citations are not only inline text
- answer body stays separate from source evidence UI
- evidence sheet uses retrieval metadata and snippets

5. Rendering moved away from plain text hacks
- earlier passes used formatter cleanup only
- current path uses block-based rendering from markdown parse

## Rendering Pipeline

Current answer rendering path:
1. model produces text
2. `MarkdownRenderer.normalizeMarkdownInput()` cleans obvious malformed output
3. CommonMark parser parses the normalized text
4. parsed nodes are converted into a local `RichBlock` model
5. Compose renders blocks with dedicated UI

Currently supported blocks:
- headings
- paragraphs
- bullet lists
- ordered lists
- block quotes
- fenced/indented code blocks
- horizontal rules
- inline emphasis / strong emphasis / inline code

Reason this matters:
- future sessions should improve this block pipeline
- do not fall back to regex-only rendering unless for narrow normalization

## Retrieval / Evidence Notes

Current retrieval improvements:
- term-frequency scoring
- exact phrase boosts
- title/document-name boosts
- proximity boost
- early-page boost
- diversity selection
- neighbor chunk stitching

Evidence metadata kept in state:
- score
- snippet
- page
- chunk index
- matched terms
- related chunk ids

View-model state:
- `MainUiState.evidenceByMessage`

UI result:
- each assistant reply can open a source/evidence sheet

## Known Good Build Commands

Use `JDK 21`.

PowerShell:
```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Compile Kotlin:
```powershell
.\gradlew.bat -g ".gradle-home" :app:compileDebugKotlin
```

Build APK:
```powershell
.\gradlew.bat -g ".gradle-home" assembleDebug
```

Why `-g ".gradle-home"`:
- keeps Gradle state inside the repo
- avoids previous disk/path issues on `C:`

## Device / Local Environment Assumptions

Known environment from this project:
- Android SDK used at:
  - `D:\AndroidSDK`
- model tested path assumption:
  - import `.litertlm` through the app, not bundled in APK

If local SDK is not configured, add/update:
- [local.properties](/D:/Tugas/Gemma%20Android%20Test%20APP/local.properties)

Expected SDK line:
```properties
sdk.dir=D\:\\AndroidSDK
```

## Known Limitations

1. Image understanding in docs
- retrieval is text-based
- embedded figures/images are not actually understood

2. Model output can still be malformed
- renderer is much better now
- but Gemma can still emit broken markdown patterns
- future work should keep improving normalization, not revert to plain raw text

3. No direct source navigation yet
- evidence sheet shows excerpts/snippets
- it does not open the exact PDF page in a document viewer

4. No embeddings / semantic retrieval
- current retrieval is practical, not state-of-the-art semantic search

5. Minor warning remains
- `VoiceController.kt` has a deprecation warning around override annotation
- not a functional blocker

## What Changed Over Time

Early problems that were fixed:
- slow follow-up due to repeated engine initialization
- streaming message overwrite bug
- cramped chat layout
- raw markdown markers in chat
- raw markdown/source artifacts spoken by TTS
- weak citation presentation

Current baseline already includes those fixes.

## Recommended Next Steps For Future Sessions

Priority order:

1. Real-device render hardening
- use screenshots from the phone
- fix remaining malformed markdown patterns
- improve spacing rules for long answers

2. Better source navigation
- page-level open behavior for PDF evidence
- stronger “why this was cited” presentation

3. Retrieval quality upgrades
- hybrid search improvements
- optional semantic retrieval later

4. Visual polish
- refine long-answer density
- improve evidence sheet readability
- tune bubble widths/spacing by device size

5. Optional future productization
- structured answer schemas
- semantic search
- MCP/backend support
- image/document multimodal support

## Guidance For Future Sessions

If continuing this project:
- trust [SESSION_HANDOFF.md](/D:/Tugas/Gemma%20Android%20Test%20APP/SESSION_HANDOFF.md) over the older `README` when they conflict
- preserve the block-based renderer direction
- preserve the TTS-cleanup/display-text separation
- preserve evidence metadata in view-model state
- prefer real-device screenshot-driven polish over abstract UI changes

If using this repo as a base for another app:
- copy the architecture and service boundaries
- keep `GemmaChatService`, `DocumentRepository`, `VoiceController`, and `MarkdownRenderer` separated
- treat rendering, retrieval, and TTS as independent pipelines

## Suggested First Read For A New Session

Read in this order:
1. [SESSION_HANDOFF.md](/D:/Tugas/Gemma%20Android%20Test%20APP/SESSION_HANDOFF.md)
2. [MainViewModel.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/MainViewModel.kt)
3. [AppScreen.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/AppScreen.kt)
4. [MarkdownRenderer.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/ui/MarkdownRenderer.kt)
5. [DocumentRepository.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/data/repo/DocumentRepository.kt)
6. [GemmaChatService.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/services/GemmaChatService.kt)
7. [VoiceController.kt](/D:/Tugas/Gemma%20Android%20Test%20APP/app/src/main/java/com/example/gemmavoicerag/services/VoiceController.kt)
