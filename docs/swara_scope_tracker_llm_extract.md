# Swara Scope Tracker Extract

Source site: `https://gemma4-scope-tracker.vercel.app/`

Extraction date: `2026-05-06`

Purpose: LLM-friendly export of:
- `Phases` tab data
- `Resource -> Brand Guidelines`
- `Resource -> PRD`

Notes:
- Phase task `status` is the tracker default status from the site bundle.
- If a task has no explicit default status in source, treat it as `not_started`.
- `brief_md` is included only when present in the source.

---

## Data Model

```text
phase:
  id: string
  title: string
  focus: string
  tasks: task[]

task:
  id: string
  priority: P0 | P1 | P2 | CUT
  title: string
  note: string
  status: done | in_progress | not_started
  brief_md?: markdown
```

---

## Phases

### Phase 1

- id: `phase-1-barebone-runtime`
- title: `Phase 1: Barebone App + Runtime`
- focus: `Lock the basic offline app shell and prove Gemma 4 runs locally.`

#### Tasks

1. `phase-1-simple-ui`
   - priority: `P0`
   - status: `done`
   - title: `Simple emergency UI`
   - note: `Basic screen, category entry point, and enough UI to test flows.`
   - brief_md:

~~~~md
## Output

Barebone app UI is already present enough to continue building product flows.

## Definition of done

- App opens locally.
- Main emergency flow exists.
- UI is simple enough for team testing.
~~~~

2. `phase-1-gemma-running`
   - priority: `P0`
   - status: `done`
   - title: `Gemma 4 running properly`
   - note: `Local inference works on target device/runtime path.`
   - brief_md:

~~~~md
## Output

Gemma 4 can produce a local answer through the current prototype/runtime path.

## Next validation

- Record exact device specs.
- Record model size and quantization.
- Capture offline/airplane-mode proof.
~~~~

3. `phase-1-airplane-proof`
   - priority: `P0`
   - status: `not_started`
   - title: `Airplane mode proof`
   - note: `Show app and model response still work with internet disabled.`
   - brief_md:

~~~~md
## Why this is next

The core thesis is 100% offline survival guidance. The demo must visibly prove that the app is not calling a cloud API.

## Deliverable

- Short video clip or screenshots.
- Device in airplane mode.
- Successful emergency response.
~~~~

4. `phase-1-runtime-notes`
   - priority: `P0`
   - status: `not_started`
   - title: `Runtime notes`
   - note: `Document model name, runtime path, device, RAM, storage, and limitations.`

### Phase 2

- id: `phase-2-emergency-brain`
- title: `Phase 2: Emergency Brain`
- focus: `Make Gemma behave like a survival instruction engine, not a normal chatbot.`

#### Tasks

1. `phase-2-system-prompt`
   - priority: `P0`
   - status: `not_started`
   - title: `System prompt`
   - note: `Emergency assistant rules, offline thesis, safety boundaries.`

2. `phase-2-response-format`
   - priority: `P0`
   - status: `not_started`
   - title: `Structured response format`
   - note: `Risk, Situation, Do Now, Do Not, Next Question.`
   - brief_md:

~~~~md
## Target format

```text
RISK
Low / Medium / High / Unknown

SITUATION
Short summary.

DO NOW
1. Immediate action.
2. Immediate action.
3. Immediate action.

DO NOT
1. Avoid unsafe action.
2. Avoid unsafe action.

NEXT QUESTION
Ask exactly one critical question.
```
~~~~

3. `phase-2-category-templates`
   - priority: `P0`
   - status: `not_started`
   - title: `Category prompt templates`
   - note: `Medical, Fire, Flood, Earthquake, Violence, Lost, Other each gets focused context.`

4. `phase-2-quick-detailed`
   - priority: `P0`
   - status: `not_started`
   - title: `Quick / Detailed behavior`
   - note: `Quick is short survival action; Detailed adds deeper steps without becoming bloated.`

5. `phase-2-safety-rules`
   - priority: `P0`
   - status: `not_started`
   - title: `Safety rules`
   - note: `No overconfident diagnosis, one critical question, useful even when help is unreachable.`

### Phase 3

- id: `phase-3-knowledge-survival`
- title: `Phase 3: Knowledge + Survival Book`
- focus: `Build a small curated survival pack before attempting full RAG.`

#### Tasks

1. `phase-3-survival-pack`
   - priority: `P0`
   - status: `not_started`
   - title: `Small survival pack`
   - note: `Curated checklist/snippets for medical, fire, flood, earthquake, violence, lost.`
   - brief_md:

~~~~md
## Scope control

Do not start with vector RAG. Start with deterministic category lookup:

- Medical basics
- Fire
- Flood
- Earthquake
- Violence / safety threat
- Lost / stranded
~~~~

2. `phase-3-survival-book-ui`
   - priority: `P0`
   - status: `not_started`
   - title: `Survival book UI`
   - note: `Static checklist mode that works without model inference.`

3. `phase-3-knowledge-selection`
   - priority: `P0`
   - status: `not_started`
   - title: `Category-based knowledge selection`
   - note: `Selected category inserts the relevant survival snippet into the prompt.`

4. `phase-3-source-metadata`
   - priority: `P0`
   - status: `not_started`
   - title: `Source label + metadata`
   - note: `Show pack label/version/last updated so guidance feels grounded.`

5. `phase-3-rag-roadmap`
   - priority: `P1`
   - status: `not_started`
   - title: `Offline RAG`
   - note: `Upgrade from simple lookup later if time allows.`

### Phase 4

- id: `phase-4-light-sharing`
- title: `Phase 4: Lightweight Sharing`
- focus: `Share useful guidance first: response and survival pack before model package.`

#### Tasks

1. `phase-4-share-response-text`
   - priority: `P0`
   - status: `not_started`
   - title: `Share response as text`
   - note: `Use native share/file/text path for the generated emergency answer.`

2. `phase-4-share-response-qr`
   - priority: `P0`
   - status: `not_started`
   - title: `Share response via QR`
   - note: `QR payload contains readable emergency instructions.`

3. `phase-4-share-survival-pack`
   - priority: `P0`
   - status: `not_started`
   - title: `Share survival checklist/pack`
   - note: `Share the lightest useful package that works without the model.`

4. `phase-4-app-shell-sharing`
   - priority: `P0`
   - status: `not_started`
   - title: `App shell sharing path`
   - note: `Document or prototype APK/package transfer where feasible.`

5. `phase-4-model-sharing-experiment`
   - priority: `P1`
   - status: `not_started`
   - title: `Model package sharing experiment`
   - note: `Heavy package; attempt only after response and survival pack sharing work.`

### Phase 5

- id: `phase-5-proof-package`
- title: `Phase 5: Proof Package`
- focus: `Create the evidence judges need: evals, benchmarks, demo script, and technical writeup.`

#### Tasks

1. `phase-5-eval-cases`
   - priority: `P0`
   - status: `not_started`
   - title: `Emergency eval cases`
   - note: `20-30 prompts across medical, fire, flood, earthquake, violence, lost.`

2. `phase-5-golden-outputs`
   - priority: `P0`
   - status: `not_started`
   - title: `Expected output examples`
   - note: `Golden examples that follow the structured emergency format.`

3. `phase-5-latency-device`
   - priority: `P0`
   - status: `not_started`
   - title: `Latency + device specs`
   - note: `Time to first token, total response time, phone, RAM, model size.`

4. `phase-5-demo-script`
   - priority: `P0`
   - status: `not_started`
   - title: `Demo script`
   - note: `Airplane mode, ask emergency, share QR, survival book.`

5. `phase-5-technical-depth`
   - priority: `P0`
   - status: `not_started`
   - title: `Technical depth writeup`
   - note: `Explain why this is an offline Gemma 4 survival system, not a chatbot.`

### Phase 6

- id: `phase-6-survival-node`
- title: `Phase 6: Survival Node Roadmap`
- focus: `Keep host mode and heavy package distribution as P1/P2 after MVP value is clear.`

#### Tasks

1. `phase-6-hotspot-web`
   - priority: `P1`
   - status: `not_started`
   - title: `Hotspot/local web host`
   - note: `One phone or Linux device hosts a page reachable through IP/QR.`

2. `phase-6-linux-runner`
   - priority: `P1`
   - status: `not_started`
   - title: `Linux barebone runner`
   - note: `CLI/webserver prototype for survival node vision.`

3. `phase-6-region-packs`
   - priority: `P1`
   - status: `not_started`
   - title: `Region-priority packs`
   - note: `Example: flood-priority Jakarta/Sumatra pack.`

4. `phase-6-compression`
   - priority: `P2`
   - status: `not_started`
   - title: `Compression/split package`
   - note: `Optimize app/model/knowledge transfer later.`

---

## Resource: Brand Guidelines

Source endpoint: `https://gemma4-scope-tracker.vercel.app/api/resource?id=brand`

# Swara Brand Guidelines

## Brand Core

- Product name: `Swara`
- Concept phrase: `Gema Swara` (echoing voice)
- Technical line: `Powered by Gemma 4`
- Product category: Offline-first emergency guidance and distribution system

## Brand Lore (v0.1)

Swara begins with a simple belief: in an emergency, people do not need more noise. They need one clear voice.

Inspired by the idea of gema and swara, Swara is an echoing voice: a single voice that does not disappear when systems fail, but carries forward. One device becomes one source of guidance. Three nearby devices become a small chorus of support. Nine become a local safety net. Hundreds become a distributed emergency voice network.

Swara is built for the first critical hours after disruption, when internet access may be unstable, responders may be delayed, and people need calm, immediate direction. It is not designed to replace emergency services. It is designed to keep guidance alive until help arrives.

Powered by Gemma 4, Swara turns ordinary offline devices into resilient points of assistance. Each device can understand, guide, repeat, and back up the others. The system is local-first by design: private when privacy matters, available when the network is gone, and concise when stress is high.

Swara's identity should feel like a beacon, not an alarm: calm but urgent, human but precise, technical but warm.

Core message:

```text
You are not alone.
Here is what to do next.
Pass it on.
```

## Core Metaphor

```text
One voice -> many voices -> a resilient network
```

## Positioning

Swara is an offline-first emergency guidance system for the first 72 hours of a disaster. It is designed for conditions where internet, cloud AI, hotlines, and emergency services may be unavailable or overloaded.

Primary message:

> Swara turns one device into a shareable emergency voice.

Secondary message:

> Guidance can spread locally even when the internet cannot.

## Messaging (Short Version)

Swara is an offline-first emergency guidance system powered by Gemma 4. Inspired by "gema swara," or an echoing voice, Swara turns one local device into a clear source of help and many devices into a distributed emergency voice network. Built for the first critical hours of crisis, Swara delivers calm, concise, safety-first guidance when connectivity, time, and attention are limited.

## Naming Rules

Use:

- `Swara`
- `Swara, powered by Gemma 4`
- `Swara is powered by Gemma 4`

Do not use as the main product name:

- `GemmaSwara`
- `GemmaSuara`
- any name that makes the product look like an official Google or Gemma product

Reason:

- the brand should stand on its own
- Gemma should be used as technology attribution, not the product brand

## Taglines

Primary:

> One voice when the network goes silent.

Alternatives:

> Guidance that echoes through the first 72 hours.

> A calm voice when systems fail.

> One voice. Many backups. Clear next steps.

## Brand Principles

- Calm over panic.
- Clarity over completeness.
- Local over dependent.
- Human over robotic.
- Networked over isolated.

## Tone of Voice

Swara speaks like a trained, steady helper beside you: direct, reassuring, action-oriented, and clear under stress.

Do:

- "Move away from the danger."
- "Do not go back inside."
- "Apply firm pressure to the bleeding."
- "Answer one question: are they breathing normally?"

Avoid:

- long, speculative analysis
- overconfident medical claims
- sounding like a generic chatbot

## Visual Direction

Core idea:

- one source -> many signals
- one voice -> distributed echoes
- signal -> node -> network

Suggested directions:

- signal waves / echo rings
- mesh nodes / redundancy
- beacon light
- shield geometry
- waveform simplified into a network
- path / direction line

## Color Direction

Primary palette direction:

- deep navy for trust and stability
- teal for signal, clarity, and calm
- safety orange for urgency and action
- light neutrals for readability

Avoid:

- overly playful palettes
- neon-heavy cyber styling
- generic purple startup gradients

## Attribution Note

Gemma should be credited as the model/runtime technology, not used as the primary product brand.

Preferred attribution:

> Powered by Gemma 4

Or:

> Built with Gemma 4 for offline emergency guidance

---

## Resource: PRD

Source endpoint: `https://gemma4-scope-tracker.vercel.app/api/resource?id=prd`

# PRD - Swara (Powered by Gemma 4)

This PRD is intentionally broad. The tracker tabs exist to keep execution focused while preserving this as canonical context.

## 0. Retrieval Audit Summary

This document is a revision after re-checking scope notes and team discussion. Key corrections:

1. Core thesis clarified: built for the first 72 hours of a disaster when internet and emergency services may be unavailable, overloaded, or unusable.
2. Call-help is not a mandatory output block: the app may mention seeking help if available, but MVP must not depend on emergency services being reachable.
3. Survival book mode is not only an automatic fallback: it is a standalone primary mode for low-storage / no-model usage.
4. Distribution is a main feature: sharing app, model package, survival pack, response, QR, hotspot/local access is a core value proposition.
5. Host mode is vision/P1, not core P0: mobile demo stays primary; Linux/webserver/Raspberry Pi-like node is strong roadmap.
6. Emergency router is not multi-model routing: MVP uses category/knowledge selection, not Cactus-style routing across multiple models.
7. SLM/fine-tuning is an experiment/story path: can be tried 3-8 hours if time allows, but prompt-only mode is MVP-safe.
8. Cactus skipped for now: we do not want multi-model routing; focus on one main model, Gemma 4 E2B-it.
9. LiteRT is primary special track: mobile/on-device/offline is core.
10. English-first demo; Indonesian/localization partial/roadmap: international hackathon, but localization remains an impact story.

## 1. Product Summary

- Working name: Swara (concept phrase: Gema Swara)
- Product type: Offline-first emergency survival assistant + disaster distribution system
- Core model: Gemma 4 E2B-it
- Primary runtime target: LiteRT / mobile on-device runtime
- Primary special track: LiteRT
- Secondary optional tracks: Unsloth experiment, llama.cpp/Linux roadmap
- Skipped special track: Cactus unless real multi-model routing exists

Swara is a 100% offline emergency survival assistant powered by Gemma 4. It is designed for the first 72 hours of a disaster, when internet connectivity, cell networks, electricity, and emergency services may be unavailable, overloaded, or unreachable.

The MVP is not just "Gemma 4 running on a phone." The MVP is a mobile-first survival system where one device can:

1. run Gemma 4 locally;
2. provide structured emergency/survival instructions;
3. open a static survival book without model inference;
4. share responses, app shell, model package, or survival pack through offline-friendly flows;
5. demonstrate airplane-mode functionality;
6. provide enough benchmark/eval/writeup evidence for technical judging.

### MVP Statement

> A 100% offline mobile emergency assistant running Gemma 4 locally, with structured survival responses, bundled survival knowledge, survival-book mode, and QR/local sharing of guidance and packages to nearby users.

### Vision Statement

> Turn phones and low-cost Linux devices into shareable offline survival nodes that distribute AI-guided emergency knowledge during the first 72 hours of a disaster.

### One-Sentence Pitch

> Swara turns a phone into a shareable emergency voice, running Gemma 4 fully offline to provide structured survival guidance and distribute emergency knowledge through QR, hotspot, and local package sharing when internet and emergency services are unavailable.

## 2. Background and Context

In major emergencies, normal assumptions fail:

- internet may be unavailable;
- mobile networks may be overloaded;
- electricity may be limited;
- emergency services may be unreachable;
- people may panic and need short, actionable instructions;
- cloud AI tools may not work;
- one working phone or local device may need to help multiple people nearby.

This product is built around the idea that emergency AI should not depend on the cloud. It should run locally, degrade gracefully, and be shareable.

Two layers:

1. AI guidance layer - Gemma 4 E2B-it running locally for emergency instructions.
2. Survival distribution layer - static survival book, app/model/survival-pack sharing, QR/local transfer, and future hotspot/Linux host mode.

## 3. Problem Statement

During the first 72 hours of a disaster, many people need reliable survival guidance but cannot depend on internet access, cloud AI, or emergency hotlines. Existing emergency apps often assume connectivity or reachable services. Swara solves this by making emergency guidance local, lightweight, and shareable.

The problem is not only "how do we answer emergency questions?" but also:

> How can one working device distribute emergency knowledge to nearby people when networks are down?

## 4. Product Thesis

### Core Thesis

Gemma 4 E2B-it can act as a local emergency instruction-following model on mobile/edge devices. Combined with an offline survival pack and QR/local distribution, it can become a practical survival node for disaster scenarios.

### What Makes This Different

Not a normal chatbot:

- runs offline;
- uses category-based emergency flows;
- produces structured survival outputs;
- includes survival book mode without AI;
- supports sharing app/response/model/survival pack;
- can evolve into Linux/Raspberry Pi-like host nodes.

Not a replacement for emergency services:

- designed for scenarios where services may be unavailable;
- if help is reachable, users can still seek help;
- core value is survival guidance when help cannot be reached.

## 5. Target Users

### 5.1 Primary Users

| User | Need |
| --- | --- |
| Civilian in emergency | Needs fast offline survival instructions. |
| Disaster survivor in first 72 hours | Needs guidance when internet/help is unavailable. |
| Community volunteer | Needs a tool to help others locally. |
| Person with one working phone | Needs to share instructions or app package to others. |

### 5.2 Secondary Users

| User | Need |
| --- | --- |
| Public facility operator | Wants future emergency node in mall, school, hospital, police post, office. |
| NGO/disaster response group | Wants offline survival packs for local regions. |
| Developer/maintainer | Wants to build region packs, Linux host mode, or fine-tuned model. |
| Hackathon judge | Needs proof that Gemma 4 is used meaningfully and the tech is real. |

## 6. Product Goals

### 6.1 MVP Goals

1. Run Gemma 4 E2B-it locally on a phone.
2. Work in airplane mode / no internet.
3. Provide emergency category selection.
4. Produce structured survival guidance.
5. Include survival book mode that works without AI.
6. Include local survival/SOP knowledge pack.
7. Share Gemma response via QR/text/file.
8. Share app shell and/or survival pack offline.
9. Attempt/model package sharing path if feasible.
10. Provide demo, benchmark, eval, and technical writeup.

### 6.2 Roadmap Goals

1. Linux/webserver host mode.
2. Raspberry Pi-like survival node vision.
3. Hotspot + QR access to local survival node.
4. Region-based survival packs.
5. Fine-tuned emergency SLM via LoRA/Unsloth.
6. llama.cpp/GGUF resource-constrained mode.
7. Voice input and text-to-speech.
8. Compression and split-package distribution.
9. Battery/thermal-aware mode.
10. Output validator.

## 7. Non-Goals for MVP

Not required for MVP:

1. Full cloud fallback.
2. Cactus-style multi-model routing.
3. Smaller fallback model.
4. Production-ready Raspberry Pi deployment.
5. Full automated safety validator.
6. NFC/tap production flow.
7. Printable emergency card.
8. Organization-specific SOP system.
9. Fully polished voice/TTS.
10. Ollama as the main runtime.

## 8. Product Principles

### 8.1 100% Offline First

The app must be demonstrable in airplane mode.

### 8.2 Survival Over Chat

Responses should be action-oriented, not conversational.

### 8.3 No Dependency on Emergency Services

The app can mention help if reachable, but MVP should not rely on emergency numbers or call-first workflows.

### 8.4 Shareability is Core

The product must help users spread instructions, survival packs, app shell, and eventually model packages.

### 8.5 Graceful Degradation

If the model cannot run, the app still works as a survival book. If full model sharing is too heavy, share survival pack first. If host mode is not ready, QR/text sharing still works.

### 8.6 Panic-Friendly UX

Large buttons, clear categories, short outputs, simple toggles.

### 8.7 Real Tech, Not Demo Theater

Repo/writeup must include benchmark, eval cases, device specs, prompt templates, runtime notes, and offline proof.

## 9. MVP Scope by Workstream (High Level)

This PRD's workstreams are tracked in:

- Roadmap Progress tab (feature scope)
- Development Phases tab (execution order)

Key workstreams:

- App Development
- Model Runtime Development
- SLM / Emergency Intelligence Development
- Knowledge Development
- Distribution Development
- Evaluation & Benchmark Development
- Product / Business / Writeup

## 10. Default Response Format

MVP format:

```text
RISK
Low / Medium / High / Unknown

SITUATION
Short summary of what the user is facing.

DO NOW
1. Immediate survival action.
2. Immediate survival action.
3. Immediate survival action.

DO NOT
1. Avoid this action.
2. Avoid this action.

NEXT QUESTION
Ask exactly one critical question.
~~~~

Optional note, only when appropriate:

```text
IF HELP IS REACHABLE
Try to contact local emergency help or nearby trusted people. If not reachable, continue with the steps above.
```

## 11. Repository / Submission Structure (Target)

```text
/app
  mobile app source
  emergency UI
  survival book mode
  QR/text/file sharing

/model
  Gemma 4 runtime notes
  LiteRT notes
  quantization notes
  prompt templates

/knowledge
  survival_pack/
  categories/
  metadata.json

/distribution
  package_strategy.md
  app_shell_notes.md
  survival_pack_transfer.md
  model_pack_transfer_experiment.md

/evals
  emergency_test_cases.jsonl
  expected_outputs.jsonl
  safety_fail_cases.jsonl

/benchmarks
  offline.md
  latency.md
  device_specs.md
  memory.md
  battery.md

/docs
  PRD.md
  architecture.md
  technical_depth.md
  roadmap.md
  business_plan.md
  safety.md
  model_card.md

/demo
  script.md
  screenshots/
  video_notes.md
```

## 12. MVP Lock (Six Blocks)

The MVP has six blocks only:

| Block | MVP Output |
| --- | --- |
| App | Emergency UI + category buttons + survival book. |
| Runtime | Gemma 4 E2B-it runs offline on phone. |
| SLM Behavior | Prompt template + structured emergency/survival answer. |
| Knowledge | Local survival/SOP pack bundled offline. |
| Distribution | Share response + app/survival pack; model sharing experiment if feasible. |
| Proof | Demo video + benchmarks + eval cases + writeup. |
