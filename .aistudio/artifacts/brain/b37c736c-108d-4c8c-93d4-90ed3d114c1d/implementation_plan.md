# NexusRoxy — Pro Edition: ChatGPT AI Studio, Full 11-File `.mcaddon` Compiler & Parallel Agent Hub

This plan elevates every layer of **NexusRoxy** with production-grade enhancements, deeper Minecraft Bedrock add-on generation, richer ChatGPT-style interactions, and rigorous error protection—without simplifying or removing any existing functionality.

### User Review & Critical Decisions

> [!IMPORTANT]
> Following your request to add all our best final ideas, audit for errors, and improve every module, here are the new capabilities included in this upgrade:

- **Pro Upgrade 1 — 11-File Specification-Complete `.mcaddon` Compiler**:
  - Expands `BedrockAddonCompiler` from 8 files to **11 complete Behavior Pack (`BP`) and Resource Pack (`RP`) files** inside every `.mcaddon` archive:
    1. `behavior_pack/manifest.json` (UUID v4 + dependencies link)
    2. `behavior_pack/entities/<id>.json` (Follower AI, Tameable, Combat Defense, Door Navigation, Nameable, Interact)
    3. `behavior_pack/spawn_rules/<id>.json` (**NEW**: Natural spawning in Overworld biomes like Cherry Grove, Plains, and Forest so companions appear naturally in your worlds!)
    4. `behavior_pack/loot_tables/entities/<id>.json` (**NEW**: Custom loot & gift tables)
    5. `behavior_pack/items/<id>_token.json` (Custom summoning amulet / mythic weapon with glint & damage)
    6. `behavior_pack/recipes/<id>_recipe.json` (Shaped crafting table recipe)
    7. `behavior_pack/texts/es_ES.lang` & `en_US.lang`
    8. `resource_pack/manifest.json`
    9. `resource_pack/entity/<id>.entity.json` + `animations/<id>.animation.json` (**NEW**: Custom idle/greeting animation definitions)
    10. `resource_pack/render_controllers/<id>.render_controllers.json` (**NEW**: Explicit Bedrock render controller)
    11. `resource_pack/textures/entity/<id>.png` (**Upgraded 64x64 Skin Engine** with multi-layer shading, eyes with highlights, blush, hair locks, hat overlay layer, and outfit trim).
- **Pro Upgrade 2 — Enhanced ChatGPT-Style Studio (`Chat IA`)**:
  - **Slash-Command & Quick Template Bar (`/femboy`, `/cariñosa`, `/guardiana`, `/arma`, `/boss`)**: Instant command triggers above the ChatGPT composer.
  - **In-Chat NPC Dialogue Simulator ("Probar Charla con NPC")**: Test how your companion responds when you give them a gift or talk to them in-game, right inside the embedded chat card.
  - **Action Toolbar on Assistant Messages**: 1-tap **Copiar Respuesta**, **Copiar JSON**, and **Regenerar / Variación de Skin** buttons on every AI message.
- **Pro Upgrade 3 — Full-Body Skin Preview & `.mcaddon` Package Diagnostic Validator**:
  - Adds a **Full-Body 2D Minecraft Character Preview** (Head, Hair Overlay, Torso, Arms, Legs) and a **Diagnóstico de Paquete `.mcaddon`** modal that validates UUIDs, JSON schemas, and lists the exact byte size of every file inside the compiled ZIP archive.

---

### 1. Overview & Core Concept

- **What It Does**:
  - Provides a ChatGPT-style conversational studio (`Chat IA`) where every prompt can synthesize, validate, preview, and export a complete 11-file Minecraft Bedrock `.mcaddon` package or launch a parallel Roxy coding agent.
  - Includes a visual **Mods MC Studio** with full-body pixel-art skin rendering, behavior/dialogue editing, natural biome spawn toggles, and a real-time `.mcaddon` ZIP archive inspector.
  - Maintains directional **Skeleton Shimmer** loading animations across all 4 feeds (`ChatTurnSkeletonCard`, `ModStudioSkeletonList`, `AgentFeedSkeletonList`, `ExploreCatalogSkeletonList`).

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Slash-Command & Conversational Mod Creation**: In `Chat IA`, tap a slash chip (`/femboy`, `/cariñosa`, `/arma`, `/boss`) or write any custom request. The AI streams a `ChatTurnSkeletonCard` shimmer and delivers a rich response with the embedded `.mcaddon` artifact card.
  2. **Interactive NPC Gift & Dialogue Test**: Inside the embedded `.mcaddon` card, tap **💬 Probar Diálogo / Dar Regalo** to cycle through the NPC's affectionate/loyal lines and see their reaction to receiving flowers, diamonds, or golden apples.
  3. **Validating & Exporting the 11-File `.mcaddon`**: Tap **Verificar Pack (11 archivos)** to inspect the compiled ZIP manifest tree and byte sizes, then tap **Guardar `.mcaddon`** or **Abrir en Minecraft**.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Natural Biome Spawn Rules (`spawn_rules/`) + Spawn Egg + Summon Command**
  - *Chosen Approach*: Include a valid `spawn_rules/<id>.json` in the Behavior Pack alongside the creative inventory Spawn Egg and `/summon roxy:<id>` support.
  - *Why*: Ensures players can encounter their custom companions naturally in survival mode (Plains, Cherry Grove, Forest) OR summon them immediately in creative/survival via crafting recipe or spawn egg.

- **Decision 2: Defensive Error Handling Across All Layers**
  - *Chosen Approach*: Wrap all clipboard operations, `FileProvider` URI grants, `ContentResolver` stream writes, color hex parsers, and JSON serializers in defensive `runCatching` blocks with clear Snackbar notifications.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                   UI LAYER (ChatGPT Studio + Mod Studio)                      │
│                                                                               │
│  ┌────────────────────────────────────┐  ┌─────────────────────────────────┐  │
│  │        ChatGptStudioScreen         │  │     MinecraftModStudioScreen    │  │
│  │ - Left Drawer (Search + Threads)   │  │ - Full-Body 2D Skin Canvas      │  │
│  │ - Slash Command Bar (/femboy, etc) │  │ - .mcaddon ZIP Tree Validator   │  │
│  │ - Embedded .mcaddon Artifact Card  │  │ - Biome Spawn & Loot Editor     │  │
│  │ - In-Card NPC Dialogue Simulator   │  │ - JSON & Lang Code Inspector    │  │
│  │ - Copy / Regenerate Turn Actions   │  │ - ModStudioSkeletonList Shimmer │  │
│  └─────────────────┬──────────────────┘  └────────────────┬────────────────┘  │
│                    └──────────────────┬───────────────────┘                   │
│                                       ▼                                       │
│                       ┌───────────────────────────────┐                       │
│                       │         RoxyViewModel         │                       │
│                       │ - Multi-Thread Chat Engine    │                       │
│                       │ - Skin Variant & Regenerator  │                       │
│                       │ - Archive Diagnostic Builder  │                       │
│                       └───────────────┬───────────────┘                       │
└───────────────────────────────────────┼───────────────────────────────────────┘
                                        ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                11-FILE BEDROCK .MCADDON COMPILER & ROOM DB                    │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                         BedrockAddonCompiler                            │  │
│  │  BP: manifest.json | entities/ | spawn_rules/ | loot_tables/ | items/   │  │
│  │      recipes/ | texts/es_ES.lang & en_US.lang | pack_icon.png           │  │
│  │  RP: manifest.json | entity/ | render_controllers/ | animations/        │  │
│  │      textures/entity/<64x64_shaded_skin>.png | pack_icon.png            │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────┘
```
