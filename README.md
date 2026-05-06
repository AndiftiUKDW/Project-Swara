# Swara Android App

Swara is an offline-first emergency guidance app built on top of the `Gemma Android Test APP` base project.

Current repo status:
- Android starter app copied into this repo
- package renamed to `com.swara.app`
- app branding renamed to `Swara`
- base handoff preserved at [docs/BASE_APP_SESSION_HANDOFF.md](docs/BASE_APP_SESSION_HANDOFF.md)
- Swara product context extracted at [docs/swara_scope_tracker_llm_extract.md](docs/swara_scope_tracker_llm_extract.md)

Base app capabilities currently reused:
- on-device Gemma via LiteRT-LM
- voice input/output
- local document ingestion and retrieval
- grounded chat answers with evidence
- Compose chat UI with markdown rendering

Immediate next product work for Swara:
- replace generic document-QA framing with emergency-first flows
- add emergency categories and panic-friendly UI
- add survival book mode
- adapt retrieval/content toward offline survival packs and SOPs
- preserve offline model import and engine reuse path

Local build notes:
- expected JDK: `21`
- expected Android SDK: `D:\\AndroidSDK`
- local SDK path is configured in `local.properties`

Compile:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat -g ".gradle-home" :app:compileDebugKotlin
```
