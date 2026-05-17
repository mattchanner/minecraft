# Phase 08 — Stretch: elemental family

**Goal:** Generalize the Fire Book into an `ElementalTome` abstraction, then ship three more books (Ice, Earth, Wind), each with its own thematically-distinct cast effect. Add a progression path to discover them.

**Prerequisites:** Phase 07 complete (or Phase 06 if you've decided to stay on particle beams).

This phase is **optional and aspirational** — pick the pieces you actually want.

## Refactor: extract `ElementalTomeItem`

- [ ] Create `com.example.elementalia.item.ElementalTomeItem extends Item` (abstract).
- [ ] Move the raytrace, cooldown, charges, and payload-send code from `FireBookItem` into `ElementalTomeItem`.
- [ ] Abstract method: `protected abstract Element element();`
- [ ] Abstract method: `protected abstract void applyImpact(ServerLevel level, ServerPlayer caster, Vec3 impact);`
- [ ] `FireBookItem` becomes a thin subclass returning `Element.FIRE` and implementing fire-specific impact.

## Define `Element` enum

- [ ] Create `com.example.elementalia.element.Element` enum with values `FIRE, ICE, EARTH, WIND`.
- [ ] Each value carries: beam color, primary particle, secondary particle, sound event, status effect applied to victims.
- [ ] The `BookCastPayload` gains an `Element` field. The `BookCastEffect` reads it and dispatches to per-element visual handlers.
- [ ] The `BookBeamEntity` gains a synced `Element` field; the renderer picks tint and texture per element.

## Ice Book

Theme: cold beam, ground frosts over, victims become frozen statues briefly.

- [ ] Beam color: pale blue/white.
- [ ] Particles: `SNOWFLAKE`, `END_ROD`.
- [ ] Ground effect: replace water with ice, grass with snow layer (1 block deep) within radius.
- [ ] Entity effect: damage with `freeze` damage source, apply `MobEffects.MOVEMENT_SLOWDOWN` amplifier 4 for 5 seconds, set `entity.setTicksFrozen(140)` for the vanilla frost shader.
- [ ] Recipe: powdered snow bucket + book + 2 packed ice. Or blue ice + lapis.

## Earth Book

Theme: ground splits and a small stone pillar rises at impact, knocking entities upward.

- [ ] Beam color: deep brown/amber.
- [ ] Particles: `BLOCK` (using dirt/stone), `LARGE_SMOKE`.
- [ ] Ground effect: replace topmost block in radius with a stone-textured "raised" column 2 blocks tall (purely cosmetic on tick 0, decays back over 60 ticks — or persists if you prefer).
- [ ] Entity effect: damage with `falling_block` source, large upward knockback (`0.2, 1.4, 0.2` style), brief `MobEffects.LEVITATION` amplifier 0 for 1 second.
- [ ] Recipe: deepslate + book + 2 amethyst clusters.

## Wind Book

Theme: invisible shockwave with leaf/cloud particles, knocks entities hard outward, no ground change.

- [ ] Beam color: pale gray-blue, low opacity.
- [ ] Particles: `CLOUD`, `SWEEP_ATTACK`, falling oak leaves (use `BLOCK` with `Blocks.OAK_LEAVES.defaultBlockState()`).
- [ ] Ground effect: none (intentional).
- [ ] Entity effect: low damage (2.0), high horizontal knockback (radial direction × 2.5), brief `MobEffects.SLOW_FALLING` for the caster only.
- [ ] Recipe: feather + book + 2 breeze rods (1.21+ adds breeze rods).

## Progression path

Make the player **earn** each book rather than dumping them in the recipe book at world start.

- [ ] Build an advancement tree with root **First Burn** (Phase 06).
- [ ] Each tome's recipe is locked behind unlocking an advancement that triggers when the player completes a small ritual:
  - Fire: already unlocked (default).
  - Ice: unlock by being frozen by powder snow while holding the Fire Book.
  - Earth: unlock by mining 64 deepslate while holding the Fire Book.
  - Wind: unlock by being knocked back by a Breeze while holding an unlocked Ice or Earth book.
- [ ] Implement each unlock condition with a custom advancement trigger or by repurposing vanilla triggers (`InventoryChangeTrigger`, `EnterBlockTrigger`, `PlayerHurtEntityTrigger`).

## Refill ritual (replaces Phase 06's "books don't refill" decision)

- [ ] Define a `TomeRitualBlock` — a custom altar block (simple 1×1 block; full multiblock is a v3 thought).
- [ ] Right-click the altar with a depleted tome to start a ritual: requires the matching reagent in a specific spot (e.g., 4 blaze rods around a fire book on the altar).
- [ ] On ritual completion, restore the tome's charges and consume the reagents.
- [ ] Add an advancement: "Eternal Flame" — perform the fire ritual.

## Config

- [ ] Introduce `ElementaliaConfig` using NeoForge's `ModConfigSpec`. Expose:
  - Per-tome damage value
  - Cooldown ticks
  - Beam max distance
  - Whether tomes ignite blocks
- [ ] Reload-friendly: read at cast time, not at mod init.

## Polish

- [ ] Custom sounds — record/source one per element instead of reusing vanilla.
- [ ] Custom beam textures — one tiling texture per element.
- [ ] Tooltip flavor per element.
- [ ] A library `Element` page in the in-game guidebook (use a JEI/EMI integration or Patchouli — out of scope for "vanilla-only" but worth flagging as a Phase 09 idea).

## Acceptance criteria

- Four tomes coexist, share registration plumbing, but feel mechanically and visually distinct.
- Progression is gated — players can't craft an Ice Book on day one.
- Each tome can be refilled via its ritual.
- All effects remain stable on dedicated server.

## Notes / gotchas

- This phase is where complexity compounds. Resist the urge to add more elements until the four are polished — every new element is multiplicative work across recipes, advancements, effects, and balancing.
- Earth's "raised pillar" effect is the most likely to cause unintended consequences (replacing player builds). Either make it purely visual (a `FallingBlockEntity` with `noPhysics`), or restrict it to a configurable block list.
- Wind's no-ground-change is intentional. Resist the temptation to add ground effects — the *absence* is what makes it feel like wind.
- Once you have four elements working, the natural next idea is "combine two books for a hybrid spell." That's a Phase 10+ idea — fun to design but a big jump in scope.
