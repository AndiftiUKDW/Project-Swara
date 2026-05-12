# Phase 7 Revision Implementation Notes

Date: 2026-05-11

Scope: major UX revision after user feedback. Main goal was lower cognitive load during emergencies and clearer setup flows outside the emergency path.

## Implemented

- Replaced hamburger navigation with persistent bottom navigation:
  - `Guide`
  - `Ask`
  - `Settings`
- Simplified home page:
  - Removed top model/status clutter.
  - Removed large hero card.
  - Renamed visible `Pack` wording to `Guide`.
  - Home now starts with `Library of available emergency guides.`
- Revised guide detail:
  - Cleaner top bar with guide title.
  - Primary actions: `Ask Swara about this`, `Show QR`.
  - Content starts directly with `Quick Help`, `Detailed Steps`, `Do Not`.
  - Added downloaded guide modules display.
- Rebuilt Settings as a tile hub:
  - `Import Model`
  - `Add Guides`
  - `Share App & Model`
  - `Web Host`
  - `Language`
  - `Accessibility`
- Added dedicated Import Model page:
  - Persistent model status card.
  - Manual import/replace retained.
  - Download model button present with safe fallback message until source is configured.
- Added dummy Knowledge Marketplace:
  - Search UI.
  - Local guide import entry.
  - Two simulated working downloads using authoritative emergency guidance:
    - `Heat Exhaustion Guide` for Medical.
    - `Power Outage Guide` for General.
  - Simulated downloads install through the same module pipeline intended for real remote guide downloads.
- Moved existing APK/model/guide distribution to `Share App & Model`:
  - Existing server behavior preserved.
  - Added local installation walkthrough.
  - Added `How to Install` helper sheet.
- Added separate `Web Host` page:
  - Separate from APK/model distribution.
  - Starts a dedicated local server for browser guide access.
  - Serves guide list/detail web UI from the host phone.
  - Includes trusted-local-network warning.
- Reworked Ask mode:
  - Familiar chatbot layout.
  - Session list behind left menu button.
  - Response settings behind right gear.
  - Removed emergency context picker from main chat surface.
  - Missing model warning with `Import Model` action.
  - Assistant message actions: `Share Guide`, `Speak`.
- Added local chat sessions:
  - New chat.
  - Reopen previous chat.
  - Delete session.
  - Messages persisted locally.
  - Session title generated from first user message.
- Added conversation memory:
  - Recent conversation summary is injected into the next prompt.
  - Recent messages still passed to Gemma.
- Added category inference:
  - Lightweight rule classifier picks Medical, Fire, Flood, Earthquake, Violence, Lost, or General from the prompt.
  - If user starts from a guide, selected guide/category is retained.
- Reworked response prompt:
  - Shifted from rigid uppercase form to human emergency guidance.
  - Target structure: short urgency sentence, `Do this now`, `Avoid`, optional one question.

## Staged / Not Fully Implemented

- Online Gemma model download is UI-ready but source URL is not configured.
- Web Host currently serves guide UI only. AI chat over web is intentionally staged because it needs lifecycle/concurrency validation.
- Language marketplace is placeholder/demo.
- Accessibility is placeholder.
- Marketplace remote download/checksum behavior is not wired yet, but data model supports `REMOTE`, `payloadUrl`, and `checksum`.

## Validation Checklist

- [ ] Bottom nav persists across Guide, Ask, and Settings.
- [ ] Home page no longer shows model status or old hero card.
- [ ] Guide cards read as `Guide`, not `Pack`.
- [ ] Guide detail opens quickly and shows actions near top.
- [ ] `Ask Swara about this` opens Ask and sends guide-context prompt.
- [ ] Ask mode shows missing-model warning when model is absent.
- [ ] Import Model manual import still works.
- [ ] Add Guides downloads `Heat Exhaustion Guide`.
- [ ] Medical Guide shows added Heat Exhaustion module.
- [ ] Add Guides downloads `Power Outage Guide`.
- [ ] General Guide shows added Power Outage module.
- [ ] Share App & Model server still downloads APK/model/guide files.
- [ ] Web Host URL opens browser guide UI on receiver phone.
- [ ] Chat sessions persist after leaving Ask tab.
- [ ] Share Guide QR opens and Copy works.

## Build

Debug build passed:

```text
./gradlew.bat -g ".gradle-home" assembleDebug
BUILD SUCCESSFUL
```
