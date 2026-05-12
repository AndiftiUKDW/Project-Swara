# Swara Phase Checklist

This checklist is for the current `Project Swara` repo.

Rule:
- `done` means done in this repo as Swara
- base-app capability does not count as done unless it has been adapted and validated for Swara

Current focus:
- validate `Phase 7` revision build on device

---

## Status Summary

| Phase | Status | Notes |
| --- | --- | --- |
| Phase 1: Swara Foundation | done | Swara shell, emergency-first framing, category entry, and build verification are in place. |
| Phase 2: Emergency Brain | implemented_pending_device_validation | Swara response contract, mode rules, category guidance, safety boundaries, rendering, and TTS cleanup are implemented. |
| Phase 3: Knowledge + Survival Pack | implemented_pending_device_validation | Bundled survival-pack grounding, metadata, and read-only Survival Book mode are implemented. |
| Phase 4: Panic-Friendly UX | revised_pending_device_validation | Bottom navigation, simplified guide home, revised Ask flow, and settings hub are implemented. |
| Phase 5: Lightweight Sharing | validated_plus_revised | QR/text sharing plus APK/model/guide local distribution are implemented and moved under Share App & Model. |
| Phase 6: Proof Package | documentation_ready | Proof package guide is written; benchmark/eval artifacts still need device evidence. |
| Phase 7: Revision + Survival Node Roadmap | implemented_pending_device_validation | UX revision, dummy guide marketplace, model setup UX, web host prototype, and local chat sessions are implemented. |

---

## Phase 1: Swara Foundation

Goal:
- convert the reused base app into a real Swara shell for emergency assistance

Status:
- `implemented_pending_device_validation`

Already done:
- [x] Base Android app copied into this repo
- [x] Android package renamed to `com.swara.app`
- [x] App branding renamed to `Swara`
- [x] Project builds with `:app:compileDebugKotlin`
- [x] Project builds with `assembleDebug`
- [x] Debug APK output confirmed
- [x] Base handoff copied into `docs/`
- [x] Swara scope extract written into `docs/`

Still required to finish Phase 1:
- [x] Replace generic doc-QA landing copy with Swara emergency-first copy
- [x] Replace generic assistant framing with Swara emergency assistant framing across UI
- [x] Add emergency category entry points:
  - [x] Medical
  - [x] Fire
  - [x] Flood
  - [x] Earthquake
  - [x] Violence
  - [x] Lost
  - [x] Other
- [x] Define primary home-screen flow for emergency use
- [x] Confirm model import flow still makes sense in Swara wording
- [x] Remove or hide base-app UI elements that are not useful for emergency-first flow
- [x] Confirm first-run experience makes sense for Swara
- [x] Validate the app can be demoed as Swara rather than as a generic RAG chat app

Definition of done:
- app opens as `Swara`
- first screen clearly communicates offline emergency purpose
- user can choose an emergency category and enter the main flow
- no major surface still reads like the old generic prototype
- build remains green

---

## Phase 2: Emergency Brain

Goal:
- make Gemma behave like a survival instruction engine, not a general chat assistant

Status:
- `done`

Checklist:
- [x] Write Swara system prompt for emergency behavior
- [x] Define Swara response contract
- [x] Implement default structured response format:
  - [x] `RISK`
  - [x] `SITUATION`
  - [x] `DO NOW`
  - [x] `DO NOT`
  - [x] `NEXT QUESTION`
- [x] Implement `Quick Help` mode behavior
- [x] Implement `Detailed Steps` mode behavior
- [x] Ensure model asks exactly one critical next question when needed
- [x] Add safety boundaries:
  - [x] avoid overconfident diagnosis
  - [x] avoid generic chatbot filler
  - [x] stay useful when help is unreachable
  - [x] avoid long speculative responses
- [x] Add category-specific prompt context for:
  - [x] Medical
  - [x] Fire
  - [x] Flood
  - [x] Earthquake
  - [x] Violence
  - [x] Lost
  - [x] Other
- [x] Make response formatting render reliably in the current chat UI
- [x] Add TTS cleanup for the structured response format
- [ ] Validate TTS output on device with real model answers
- [ ] Test follow-up behavior after the first answer on device

Definition of done:
- emergency responses are consistently structured
- output feels like survival guidance, not chat
- quick mode is shorter and more actionable than detailed mode
- category selection changes response behavior
- TTS remains usable
- formatting works in real app UI

---

## Phase 3: Knowledge + Survival Pack

Goal:
- ground responses in offline emergency content

Status:
- `implemented_pending_device_validation`

Checklist:
- [x] Create small offline survival pack
- [x] Exclude school-shooting and active-shooter content from bundled survival-pack scope
- [x] Add category-based knowledge lookup
- [x] Insert category-relevant content into prompt context
- [x] Add source label / metadata
- [x] Add last-updated metadata
- [x] Add survival book mode without inference

Phase 3A implementation:
- [x] Add `app/src/main/assets/survival_pack_v0_1.json`
- [x] Add `SurvivalPackRepository`
- [x] Wire repository through `AppContainer`
- [x] Pass selected category pack into `GemmaChatService`
- [x] Add `VIOLENCE` bundled pack for general personal safety while excluding school-shooting and active-shooter guidance
- [ ] Validate on device that Medical, Fire, Flood, Earthquake, Lost, and Other answers use the bundled pack

Phase 3B implementation:
- [x] Add catalog `version`
- [x] Add catalog `lastUpdated`
- [x] Add catalog `scope`
- [x] Add per-pack source URLs
- [x] Surface version, update date, scope, source labels, and source URLs in Gemma prompt context

Phase 3C implementation:
- [x] Expose bundled packs to UI state
- [x] Add Survival Book section to Swara Kit
- [x] Add read-only pack list
- [x] Add read-only pack detail screen
- [x] Show quick help, detailed steps, do-not list, supplies, and sources without model inference
- [ ] Validate Survival Book navigation on device

---

## Phase 4: Panic-Friendly UX

Goal:
- make the app usable under stress

Status:
- `implemented_pending_device_validation`

Checklist:
- [x] Large emergency-first category buttons
- [x] Simplify first screen
- [x] Reduce non-essential controls
- [x] Improve long-answer readability
- [x] Tune spacing and hierarchy for high-stress reading
- [ ] Validate on a real phone screen

Implementation notes:
- [x] Make Survival Book / guide the first surface after launch
- [x] Move chat into explicit `Ask Swara` mode
- [x] Add `Read guide` and `Ask Swara` actions per bundled pack
- [x] Keep Kit / Settings separate from primary emergency guide flow
- [x] Add renderer cleanup for duplicate emergency section headings
- [x] Strengthen prompt rule so each emergency heading appears once

---

## Phase 5: Lightweight Sharing

Goal:
- let one device help nearby users offline

Status:
- `phase_5a_implemented_pending_device_validation`

Checklist:
- [x] Share response as text
- [x] Share response via QR
- [x] Share survival pack
- [x] Document app shell sharing path
- [x] Leave full model sharing as optional experiment

Phase 5A implementation:
- [x] Add compact `SWARA/CHAT/1` QR payloads
- [x] Add compact `SWARA/PACK/1` QR payloads
- [x] Keep QR payloads uppercase ASCII and line-oriented
- [x] Add native Android text share for assistant answers
- [x] Add native Android text share for Survival Book packs
- [x] Add QR display sheet for assistant answers and guide packs
- [ ] Validate QR scanning on another phone

Phase 5B implementation:
- [x] Remove `A1` / `A2` labels from chat QR output to reduce payload size
- [x] Shorten Swara Kit survival-book description
- [x] Keep guide/answer text sharing available through Android share sheet

Phase 5C implementation:
- [x] Add prototype local HTTP distribution server
- [x] Add manual hotspot / same-Wi-Fi instructions
- [x] Add local URL QR
- [x] Serve installed APK at `/app.apk`
- [x] Serve survival pack TXT and JSON
- [x] Serve imported Gemma model at `/model` when present
- [x] Validate receiver phone can open the local URL over hotspot
- [x] Validate APK download/install path on receiver
- [x] Validate model download behavior and transfer time
- [x] Move Kit / Settings from bottom sheet to left navigation drawer
- [x] Remove redundant bundled survival book list from Kit / Settings drawer

---

## Phase 6: Proof Package

Goal:
- prepare Swara-specific proof for demo and judging

Status:
- `documentation_ready`

Checklist:
- [ ] Airplane mode proof
- [ ] Device specs note
- [ ] Model/runtime note
- [ ] Latency measurement
- [ ] Emergency eval cases
- [ ] Expected output examples
- [ ] Demo script
- [ ] Technical writeup
- [x] Phase 6 proof package guide written in `docs/phase6_proof_package_guide.md`

---

## Phase 7: Revision + Survival Node Roadmap

Goal:
- reduce emergency cognitive load, prepare setup flows, and separate app/model sharing from browser Web Host

Status:
- `implemented_pending_device_validation`

Checklist:
- [x] Replace hamburger navigation with persistent bottom navigation
- [x] Simplify Guide home and remove misleading header status
- [x] Rename user-facing Pack wording to Guide
- [x] Redesign guide detail around quick reading and top actions
- [x] Replace dense Swara Kit with Settings tile hub
- [x] Add dedicated Import Model page
- [x] Preserve manual model import
- [x] Add model download entry with safe unconfigured-source state
- [x] Add dummy Knowledge Marketplace
- [x] Add two simulated working guide downloads through real module install path
- [x] Show added modules inside guide categories
- [x] Move current APK/model/guide sharing into Share App & Model
- [x] Add Web Host page separate from distribution server
- [x] Add browser guide UI served from host phone
- [x] Rework Ask mode into chat-first UI
- [x] Add missing-model warning in Ask mode
- [x] Add local chat sessions and persistence
- [x] Add lightweight conversation memory injection
- [x] Add lightweight category inference
- [x] Rework prompt style toward human emergency guidance
- [ ] Validate revised navigation on device
- [ ] Validate marketplace module install on device
- [ ] Validate Web Host receiver browser on device
- [ ] Validate chat memory with multi-turn emergency prompt
- [ ] Configure real Gemma model download source
- [ ] Add AI endpoint to Web Host after native chat validation

---

## Immediate Execution Order

1. Validate Phase 7 revised UI on device.
2. Capture screenshots for proof package.
3. Use Phase 6 eval cases to test model behavior and memory on-device.
4. Configure real model hosting only after license/source path is confirmed.

---

## Notes

- The base app proved the technical direction.
- Phase 1 is complete at the app-shell level in this repo.
- Phase 2 is implemented in the app prompt path and should be validated on-device with real emergency prompts.
