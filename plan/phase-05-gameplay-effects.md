# Phase 05 — Gameplay effects

**Goal:** A Fire Book cast actually damages entities, ignites flammable blocks at the impact point, and pushes mobs away. Numerical balance is rough; tuning is a later concern.

**Prerequisites:** Phase 04 complete.

## Tasks

### Damage entities

- [x] Build an `AABB` centered on `impact` with radius `IMPACT_RADIUS` on X and Z and 2.0 on Y.
- [x] Query `level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player && e.isAlive())`.
- [x] For each entity: apply `onFire` damage (6.0f), `igniteForSeconds(5.0f)`, push away from impact.

### Damage source — decide

- [x] v1 decision: use `onFire` source (plain, no player attribution). Note: custom attributed source deferred to later phase.

### Ignite flammable blocks

- [x] Loop over 7×7 column centered on impact (±3 X and Z).
- [x] For each (x, z): use `getHeightmapPos(MOTION_BLOCKING, col)` to find the surface air block. If the block below is flammable (`Direction.UP`), set fire.
- [x] Skip positions outside `IMPACT_RADIUS` with circular distance check.

### Respect protection

- [x] v1 decision: always ignite, no mob griefing gate (deferred to Phase 06 config).

### Particle-only mid-air casts

- [x] Track `hitBlock = hitResult.getType() != MISS`. Skip ignition loop if `!hitBlock`.
- [x] Damage and knockback still apply regardless.

### Verify in-game

- [x] Spawn a cow and a zombie 10 blocks ahead. Cast at the ground in front of them.
  - Both should take damage and catch fire.
  - Both should be pushed away from the impact.
- [x] Cast at a wood plank floor. Confirm fire blocks appear on the affected tiles.
- [x] Cast at a stone floor. Confirm no fire (stone isn't flammable).
- [x] Cast at yourself: damage should be skipped for the caster (`e != player` filter).
- [x] Cast at peaceful difficulty: confirm hostile mobs are pushed but not damaged (vanilla behavior — peaceful zeros damage to players, not from players).
- [x] On dedicated server, confirm fire ignition replicates to all viewers.

## Acceptance criteria

- Entities in the impact zone take fire damage and are knocked back.
- Flammable blocks at the impact ring catch fire.
- The caster is never damaged by their own cast.
- Stone and other non-flammable surfaces don't ignite.

## Notes / gotchas

- The `hurt` method is the canonical way to damage entities; never modify health directly — it bypasses armor, enchantments, and events.
- Don't call `level.setBlockAndUpdate` from the client side. This whole phase's logic lives in the server branch of `use()` — verify by checking that `level instanceof ServerLevel` before any block mutation.
- Fire blocks need a supporting block below them or they immediately extinguish. The "air above flammable" check handles this.
- If you find that damage feels too high or too low after playtesting, tune the `6.0f` value here rather than introducing config files. Config is a Phase 06 concern.
- **1.21.4 note:** `igniteForSeconds(float)` is preferred over `setRemainingFireTicks(int)` — it's the newer API.
