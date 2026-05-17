# Elemental Fire Book — Implementation Plan

A NeoForge 1.21.4 mod that adds an **Elemental Fire Book** — right-click to fire a beam that cracks the ground and erupts fire at the impact point. Built to work on dedicated servers.

## Approach

- **v1 (Phases 0–6)**: Particle-stream beam (the "easy" approach A from the design discussion).
- **v2 (Phase 7)**: Upgrade to a beacon-style rendered beam (approach B).
- **v3+ (Phase 8)**: Extend into a family of elemental tomes (ice / earth / wind).

## Phases

| # | Phase | Status |
|---|---|---|
| 00 | [Project setup](phase-00-setup.md) | Done |
| 01 | [Item registration](phase-01-item-registration.md) | Done |
| 02 | [Right-click + raytrace](phase-02-right-click-raytrace.md) | Done |
| 03 | [Networking payload](phase-03-networking.md) | Done |
| 04 | [Visual effect — particle beam](phase-04-visuals-particle-beam.md) | Done |
| 05 | [Gameplay effects](phase-05-gameplay-effects.md) | Done |
| 06 | [Polish v1](phase-06-polish-v1.md) | Done |
| 07 | [Upgrade to beacon-style beam](phase-07-upgrade-to-beacon-beam.md) | Not started |
| 08 | [Stretch — elemental family](phase-08-stretch-elemental-family.md) | Not started |

Tick boxes inside each phase file as work is completed. When all boxes in a phase are ticked, change its status above to **Done**.

## Decisions and conventions

These are defaults — change them in this file before Phase 0 if you want something different. The rest of the plan references them.

| Key | Value | Notes |
|---|---|---|
| Mod ID | `elementalia` | Lowercase, no spaces. Used as namespace for all registry IDs. |
| Mod display name | `Elementalia` | Shown in the mods menu. |
| Root package | `com.example.elementalia` | Change `com.example` to your domain if you have one. |
| MC version | `1.21.4` | Pinned. |
| NeoForge version | Latest stable for 1.21.4 | Looked up during Phase 0. |
| Java | JDK 21 | Required by MC 1.21+. |
| Loader | NeoForge | Decided. |
| License | TBD | Pick before any public release. |

## Client/server split — read once, then never forget

This mod runs on a **dedicated server**, meaning:

- The server JAR has **no** client classes. Importing `net.minecraft.client.*` from server-reachable code will crash the server on load.
- All visual code (particles, sound playback, screen shake, beam rendering) lives in client-only classes guarded by `Dist.CLIENT` or registered through client-only event buses.
- The server is **authoritative** for gameplay state. The flow is always:
  1. Client tells server "I right-clicked."
  2. Server validates and applies gameplay changes (damage, fire blocks, cooldown).
  3. Server sends a custom packet to nearby clients telling them what to render.
  4. Each client plays its own local visual.
- Never call `Minecraft.getInstance()` from common or server code. Particles are spawned on the client only, after the packet arrives.

## Glossary

- **Common code**: classes loaded on both physical client and physical server. Default location for game logic.
- **Client-only code**: classes loaded only on the physical client. Lives in `client/` subpackages and is registered through `FMLClientSetupEvent` or `@EventBusSubscriber(Dist.CLIENT)`.
- **Payload**: NeoForge 1.21.4's term for a custom network packet. Replaces the old `SimpleChannel` API.
- **Data component**: 1.21+ replacement for item NBT. Used here for charges and cooldown.
- **Datagen**: build-time JSON generation for recipes / loot / lang / models. Avoids hand-writing the JSON.

## Working with this plan

- Read each phase file in order; do not skip ahead — later phases assume earlier work exists.
- Each phase ends with **Acceptance criteria**. Don't tick the last box until those pass.
- If reality diverges from the plan, **update the plan file** before continuing. The plan is the source of truth for what's been agreed.
