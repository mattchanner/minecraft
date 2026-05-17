# Elementalia — NeoForge Minecraft Mod

A NeoForge mod for Minecraft Java Edition that adds a family of **Elemental Tomes**. Right-click a tome to fire a beam that produces an elemental impact effect at the strike point. The first tome shipped is the Fire Book; ice, earth, and wind variants come later.

## Current status

**Design phase.** No code exists yet — only the implementation plan in `plan/`. Phase 00 (project setup) has not yet started.

The first task in any new working session is to read `plan/README.md`, then resume from the lowest-numbered phase whose tasks aren't fully ticked.

## Stack and decisions

| Key | Value |
|---|---|
| Loader | NeoForge |
| Minecraft version | 1.21.4 |
| Java | JDK 21 |
| Build tool | Gradle (wrapper, no global install) |
| Target | Dedicated-server compatible (not just singleplayer) |
| Mod ID | `elementalia` |
| Mod display name | `Elementalia` |
| Root Java package | `com.example.elementalia` (change to a real domain before release) |
| License | TBD (decide before public release) |

If any of these change, update both this file and `plan/README.md`.

## The one rule that breaks everything if forgotten

**Client-only code must never be loaded on the dedicated server.** Importing `net.minecraft.client.*` from common or server-reachable code crashes server startup with `NoClassDefFoundError`.

Concretely:
- All visual code (particles, sounds, screen rendering, beam rendering) lives under `com.example.elementalia.client.*`.
- Client classes are referenced from common code only via lazy method references or `DistExecutor`.
- Never call `Minecraft.getInstance()` from common code.
- The server is authoritative for gameplay (damage, fire blocks, cooldowns). It sends a `BookCastPayload` packet so clients render the visual locally.

Every phase file in `plan/` reinforces this; treat it as a hard invariant.

## Repository layout

```
.
├── CLAUDE.md                     ← this file (project memory)
├── README.md                     ← TBD; not yet created
├── plan/                         ← implementation plan, one file per phase
│   ├── README.md                 ← index and decisions
│   └── phase-00 … phase-08.md    ← tickable task lists
├── src/                          ← TBD; created in Phase 00 (NeoForge MDK)
├── build.gradle                  ← TBD; from MDK
├── gradle.properties             ← TBD; from MDK, configured in Phase 00
└── settings.gradle               ← TBD; from MDK
```

## Workflow

1. Read `plan/README.md` for the high-level map and decisions.
2. Open the lowest-numbered phase file with unticked boxes.
3. Work the tasks in order.
4. Tick boxes (`- [ ]` → `- [x]`) as you complete them.
5. When all boxes in a phase are ticked, update the phase status table in `plan/README.md` from **Not started** → **Done**.
6. If reality diverges from the plan, **edit the plan first**, then continue.

## Useful commands (after Phase 00)

These don't work yet — they become available once Phase 00 sets up the NeoForge MDK.

```bash
./gradlew build              # compile + run unit tests + produce mod jar
./gradlew runClient          # launch a dev MC client with the mod loaded
./gradlew runServer          # launch a dev dedicated server with the mod loaded
./gradlew runData            # regenerate datapack JSON from datagen providers
./gradlew runGameTestServer  # run @GameTest cases (after Phase 06)
./gradlew clean              # wipe build outputs
```

Built mod jar lands in `build/libs/`.

## Multi-machine note

This repo is intended to be cloned on another machine that already has the dev environment set up (JDK 21, IntelliJ, Git). When working on a fresh clone:

1. Run `./gradlew build` first — it downloads and decompiles the MC source on first use; this can take several minutes.
2. Import as a Gradle project in IntelliJ; the `runClient` / `runServer` run configurations are generated automatically.
3. Do **not** commit the `run/` directory or `build/`. The `.gitignore` already excludes them.

## When in doubt

- Don't guess on Minecraft API specifics — many things changed between 1.20.x and 1.21.4. Read NeoForge's official docs for the current version, or look at vanilla source for the relevant class.
- Don't import client classes into common code. (Repeating because it's the #1 cause of dedicated-server crashes.)
- Don't hand-write JSON when datagen could produce it (Phase 06 onward).
- Don't expand scope mid-phase. Finish the phase, then plan the addition.
