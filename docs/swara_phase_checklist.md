# Swara Phase Checklist

This checklist is for the current `Project Swara` repo.

Rule:
- `done` means done in this repo as Swara
- base-app capability does not count as done unless it has been adapted and validated for Swara

Current focus:
- finish `Phase 2`

---

## Status Summary

| Phase | Status | Notes |
| --- | --- | --- |
| Phase 1: Swara Foundation | done | Swara shell, emergency-first framing, category entry, and build verification are in place. |
| Phase 2: Emergency Brain | not_started | Response behavior and emergency prompt layer still need Swara-specific implementation. |
| Phase 3: Knowledge + Survival Pack | not_started | Survival content and category-grounding not implemented yet. |
| Phase 4: Panic-Friendly UX | not_started | Base UI exists, but panic-first simplification is not done. |
| Phase 5: Lightweight Sharing | not_started | Base app patterns exist, but Swara-specific share flows are not done. |
| Phase 6: Proof Package | not_started | No Swara-specific benchmark/proof package yet. |
| Phase 7: Survival Node Roadmap | not_started | Roadmap only. |

---

## Phase 1: Swara Foundation

Goal:
- convert the reused base app into a real Swara shell for emergency assistance

Status:
- `done`

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
- `not_started`

Checklist:
- [ ] Write Swara system prompt for emergency behavior
- [ ] Define Swara response contract
- [ ] Implement default structured response format:
  - [ ] `RISK`
  - [ ] `SITUATION`
  - [ ] `DO NOW`
  - [ ] `DO NOT`
  - [ ] `NEXT QUESTION`
- [ ] Implement `Quick Help` mode behavior
- [ ] Implement `Detailed Steps` mode behavior
- [ ] Ensure model asks exactly one critical next question when needed
- [ ] Add safety boundaries:
  - [ ] avoid overconfident diagnosis
  - [ ] avoid generic chatbot filler
  - [ ] stay useful when help is unreachable
  - [ ] avoid long speculative responses
- [ ] Add category-specific prompt context for:
  - [ ] Medical
  - [ ] Fire
  - [ ] Flood
  - [ ] Earthquake
  - [ ] Violence
  - [ ] Lost
  - [ ] Other
- [ ] Make response formatting render reliably in the current chat UI
- [ ] Verify TTS output is clean for the structured response format
- [ ] Test follow-up behavior after the first answer

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
- `not_started`

Checklist:
- [ ] Create small offline survival pack
- [ ] Add category-based knowledge lookup
- [ ] Insert category-relevant content into prompt context
- [ ] Add source label / metadata
- [ ] Add last-updated metadata
- [ ] Add survival book mode without inference

---

## Phase 4: Panic-Friendly UX

Goal:
- make the app usable under stress

Status:
- `not_started`

Checklist:
- [ ] Large emergency-first category buttons
- [ ] Simplify first screen
- [ ] Reduce non-essential controls
- [ ] Improve long-answer readability
- [ ] Tune spacing and hierarchy for high-stress reading
- [ ] Validate on a real phone screen

---

## Phase 5: Lightweight Sharing

Goal:
- let one device help nearby users offline

Status:
- `not_started`

Checklist:
- [ ] Share response as text
- [ ] Share response via QR
- [ ] Share survival pack
- [ ] Document app shell sharing path
- [ ] Leave full model sharing as optional experiment

---

## Phase 6: Proof Package

Goal:
- prepare Swara-specific proof for demo and judging

Status:
- `not_started`

Checklist:
- [ ] Airplane mode proof
- [ ] Device specs note
- [ ] Model/runtime note
- [ ] Latency measurement
- [ ] Emergency eval cases
- [ ] Expected output examples
- [ ] Demo script
- [ ] Technical writeup

---

## Phase 7: Survival Node Roadmap

Goal:
- keep future distribution and host ideas documented without blocking MVP

Status:
- `not_started`

Checklist:
- [ ] Hotspot/local host concept
- [ ] Linux runner concept
- [ ] Region-priority pack examples
- [ ] Compression/split package roadmap

---

## Immediate Execution Order

1. Implement `Phase 2` emergency response behavior.
2. Then move into `Phase 3` survival pack grounding.
3. Keep `Phase 4` panic-friendly polish tied to real-device screenshots.

---

## Notes

- The base app proved the technical direction.
- Phase 1 is complete at the app-shell level in this repo.
- Do not mark `Phase 2` done until the model output is visibly emergency-first in the running app.
