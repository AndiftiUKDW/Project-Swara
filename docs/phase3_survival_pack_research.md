# Phase 3 Survival Pack Research

Purpose:
- define the source basis for Swara's built-in offline survival pack
- keep Phase 3 grounded in public, authoritative emergency guidance
- avoid inventing medical or disaster instructions without source support

Scope:
- MVP survival pack for `Medical`, `Fire`, `Flood`, `Earthquake`, `Violence`, `Lost`, and `Other`
- deterministic category lookup first
- no vector RAG in the first pass
- English-first app content, with Indonesia-relevant source material tracked for localization

---

## Source Priority

Use sources in this order:

1. Official national/local emergency agencies.
2. Recognized emergency or health organizations.
3. Medical institutions only for simple first-aid basics.
4. Avoid blogs, commercial kit sellers, SEO pages, and unsourced advice.

Do not include:
- diagnosis claims
- medication dosing
- improvised risky procedures
- instructions that require specialized training unless clearly labeled
- instructions that assume internet, phone service, or emergency responders are reachable

---

## Primary Sources

### Indonesia / Local Disaster Context

Source:
- BNPB, `Buku Saku Tanggap Tangkas Tangguh Menghadapi Bencana`
- URL: `https://bnpb.go.id/storage/app/media/Buku%20BNPB/Buku%20Saku%20Bencana%20BNPB.pdf`
- Catalog: `https://perpustakaan.bnpb.go.id/inlislite/opac/detail-opac?id=1504`

Use for:
- Indonesia-relevant disaster categories
- phase framing: before, during, after disaster
- flood, earthquake, tsunami, landslide, volcanic eruption, wildfire/local hazards
- future Indonesian localization

Notes:
- Good canonical local source for Swara's Indonesia story.
- Phase 3 MVP should cite it as a source, but content extraction should be summarized into short app-safe checklists.

### General Emergency Supplies

Source:
- Ready.gov / FEMA, `Build A Kit`
- URL: `https://www.ready.gov/kit`

Use for:
- emergency kit basics
- water, food, radio, flashlight, first aid kit, batteries, whistle, dust mask, sanitation, tools, maps, chargers
- survival book `General / Other` section

MVP extraction:
- "Have water, food, light, first aid, communication, sanitation, and documents."
- "Prepare supplies for home, work, and car."
- "Tailor supplies for children, older adults, pets, medication, and disability needs."

### Earthquake

Sources:
- Ready.gov, `Earthquakes`
- URL: `https://www.ready.gov/earthquakes`
- American Red Cross, `Earthquake Safety`
- URL: `https://www.redcross.org/get-help/how-to-prepare-for-emergencies/types-of-emergencies/earthquake.html`
- CDC, `Preparing for Earthquakes`
- URL: `https://www.cdc.gov/earthquakes/safety/index.html`

Use for:
- Drop, Cover, Hold On
- avoid running outside during shaking
- protect head and neck
- after shaking: check hazards, avoid damaged buildings, expect aftershocks
- coastal note: move inland / higher ground if tsunami risk after shaking

MVP extraction:
- During shaking: drop, cover head and neck, hold on.
- Outside: move away from buildings, trees, power lines if possible.
- Driving: pull over safely, avoid bridges/overpasses.
- After: shoes, check injuries, avoid damaged structures, expect aftershocks.

### Flood

Sources:
- American Red Cross, `Flood Safety`
- URL: `https://www.redcross.org/flood`
- CDC, `Preparing for Floods`
- URL: `https://www.cdc.gov/floods/safety/index.html`

Use for:
- move away from floodwater
- avoid fallen power lines and electrical hazards
- do not touch electrical equipment if wet or standing in water
- wait for official clearance before returning if evacuated
- cleanup hazards and protective equipment

MVP extraction:
- Move to higher ground.
- Do not walk or drive through floodwater.
- Avoid power lines and wet electrical equipment.
- Use flashlights instead of candles.
- Treat floodwater as contaminated.

### Fire

Sources:
- American Red Cross, `Home Fire Safety`
- URL: `https://www.redcross.org/fire`
- American Red Cross, `What To Do if a Fire Starts`
- URL: `https://www.redcross.org/get-help/how-to-prepare-for-emergencies/types-of-emergencies/fire/if-a-fire-starts.html`
- American Red Cross, `Home Fire Preparedness`
- URL: `https://www.redcross.org/get-help/how-to-prepare-for-emergencies/types-of-emergencies/fire/home-fire-preparedness.html`

Use for:
- get out, stay out, call for help if possible
- yell fire, use stairs, leave belongings
- never open warm doors
- stay low under smoke
- close doors behind you
- stop, drop, roll if clothing catches fire

MVP extraction:
- Leave immediately.
- Use stairs, not elevators.
- Crawl low under smoke.
- Do not go back inside.
- If trapped, close doors and signal from window if possible.

### Medical / Bleeding

Sources:
- American Red Cross, `Bleeding, Life-Threatening External`
- URL: `https://www.redcross.org/take-a-class/resources/learn-first-aid/bleeding-life-threatening-external`
- Stop the Bleed / Indiana DHS summary
- URL: `https://www.in.gov/dhs/get-prepared/emergencies/stop-the-bleed-bleeding-control-for-the-injured/`
- Mayo Clinic, `Cuts and scrapes: First aid`
- URL: `https://www.mayoclinic.org/first-aid/first-aid-cuts/basics/art-20056711`

Use for:
- direct pressure
- life-threatening bleeding signs
- wound packing/tourniquet only with trained/available caveat
- minor cuts: wash hands, pressure, rinse with water, avoid irritants

MVP extraction:
- Severe bleeding: apply firm direct pressure with clean cloth.
- Keep pressure; add cloth on top if soaked.
- If trained and available, use tourniquet for life-threatening limb bleeding.
- Minor cut: wash hands, rinse with clean water, cover.
- Avoid hydrogen peroxide/iodine inside wound.

Safety constraints:
- Do not diagnose.
- Do not give medication dosing.
- Do not recommend removing embedded objects.
- Do not recommend tourniquet unless phrased as trained/available and life-threatening bleeding.

### Violence / Public Attack

Sources:
- Ready.gov, `Attacks in Crowded and Public Spaces`
- URL: `https://www.ready.gov/public-spaces`
- FBI, `Active Shooter Event Quick Reference Guide`
- URL: `https://www.fbi.gov/how-we-can-help-you/active-shooter-safety-resources/active-shooter-event-quick-reference-guide`
- NIH ORS, `Basic Emergency Procedures: Active Shooter`
- URL: `https://ors.od.nih.gov/ser/dem/workplace-emergencies/basic-procedures/Pages/active-shooter.aspx`

Use for:
- run, hide, fight as last resort
- leave belongings
- call/text when safe
- lock/block doors, silence devices, stay out of view
- fight only as last resort

MVP extraction:
- Escape if a safe route exists.
- Leave belongings.
- Hide behind a locked/blocked door if escape is unsafe.
- Silence phone.
- Fight/disrupt only as last resort if directly threatened.

Safety constraints:
- Avoid aggressive advice as first-line.
- Do not suggest confrontation unless no safer option.
- Avoid operational details that escalate violence.

### Lost / Stranded

Sources:
- U.S. Forest Service, `If You Get Lost`
- URL: `https://www.fs.usda.gov/visit/know-before-you-go/if-you-get-lost`
- National Park Service, `Missing Persons in the National Parks`
- URL: `https://home.nps.gov/articles/missing-persons-in-the-national-parks.htm`

Use for:
- planning and essentials
- food/water, compass/map/GPS limitations, weather protection, flashlight, matches
- search-and-rescue framing

MVP extraction:
- Stop moving if you are disoriented.
- Stay visible and conserve battery.
- Mark your location.
- Signal periodically.
- Protect from weather.
- Do not keep wandering without a plan.

Safety constraints:
- Avoid universal "stay put" if there is immediate danger.
- Ask one critical question: are you injured, in immediate danger, or can you identify your last known location?

---

## Recommended Built-In Pack Shape

File target:
- `app/src/main/assets/survival_pack_v0_1.json`

Suggested schema:

```json
{
  "id": "swara_survival_pack_v0_1",
  "title": "Swara Survival Pack",
  "version": "0.1",
  "lastUpdated": "2026-05-08",
  "language": "en",
  "disclaimer": "Offline emergency guidance. Not a replacement for trained responders or medical care.",
  "categories": [
    {
      "id": "medical",
      "title": "Medical",
      "priority": "P0",
      "sourceIds": ["redcross_bleeding", "stop_the_bleed", "mayo_cuts"],
      "quick": {
        "riskSignals": [],
        "doNow": [],
        "doNot": [],
        "nextQuestion": ""
      },
      "detailed": {
        "steps": [],
        "watchFor": [],
        "whenHelpReachable": ""
      }
    }
  ],
  "sources": [
    {
      "id": "ready_kit",
      "title": "Ready.gov Build A Kit",
      "publisher": "FEMA / Ready.gov",
      "url": "https://www.ready.gov/kit",
      "accessed": "2026-05-08"
    }
  ]
}
```

---

## Phase 3 Implementation Recommendation

Build in this order:

1. Add bundled `survival_pack_v0_1.json`.
2. Add a small `SurvivalPackRepository` that loads assets JSON.
3. Add category lookup by current `EmergencyCategory`.
4. Inject the matching category's quick/detailed content into `GemmaChatService` prompt.
5. Show source label in chat state: `Based on Swara Survival Pack v0.1`.
6. Add `Survival Book` screen that renders the same JSON without model inference.

Do not build semantic search yet.

Reason:
- deterministic category lookup is safer, testable, and enough for MVP
- survival book mode can reuse the same content
- source metadata becomes easy to display

---

## MVP Content Rules

Every category should include:

- `riskSignals`: signs that raise urgency
- `doNow`: short actions
- `doNot`: unsafe actions to avoid
- `nextQuestion`: one question that changes next guidance
- `sourceIds`: source references

Keep every action:
- short
- direct
- non-diagnostic
- useful offline
- not dependent on internet or emergency lines

Use local emergency help phrasing:
- "If help is reachable, contact local emergency services or nearby trusted people."

Avoid:
- "Call 911" as the only instruction
- region-specific emergency numbers until localization is explicit
- advice requiring specialized tools unless marked optional/trained

---

## Initial Category Coverage

Medical:
- severe bleeding
- minor cuts
- breathing/consciousness prompt only, no CPR detail unless sourced and carefully scoped

Fire:
- exit
- smoke
- blocked door
- clothing on fire

Flood:
- higher ground
- moving water
- electrical hazards
- contaminated water

Earthquake:
- during shaking
- after shaking
- trapped
- coastal tsunami risk

Violence:
- escape
- hide
- silence
- last-resort defense

Lost:
- stop
- mark location
- signal
- conserve battery
- shelter/weather

Other:
- immediate danger check
- shelter
- water
- communication
- emergency kit
