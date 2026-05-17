# Phase 05 — Gameplay effects

**Goal:** A Fire Book cast actually damages entities, ignites flammable blocks at the impact point, and pushes mobs away. Numerical balance is rough; tuning is a later concern.

**Prerequisites:** Phase 04 complete.

## Tasks

### Damage entities

In `FireBookItem.cast(...)`, after sending the network payload, before applying cooldown:

- [ ] Build an `AABB` centered on `impact` with radius `IMPACT_RADIUS` on X and Z and 2.0 on Y.
- [ ] Query `level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player && e.isAlive())`.
- [ ] For each entity:
  - Apply damage: `entity.hurt(level.damageSources().onFire(), 6.0f)`. (Choosing the `onFire` source means existing fire resistance behaves correctly.)
  - Set them on fire: `entity.setRemainingFireTicks(100)` (5 seconds).
  - Apply knockback: compute horizontal direction from impact to entity, normalize, scale to 0.6, and call `entity.push(dx, 0.2, dz)`.

### Damage source — decide

The `onFire` source treats the damage as fire damage, which interacts with fire resistance enchantments and the Fire Resistance potion. Good default.

- [ ] Note here whether you want a custom damage source (e.g., attributed to the caster, like a player kill): ___________ (fill in after first playtest). For v1, `onFire` is fine.

### Ignite flammable blocks

A simple but flavorful rule: every block in the impact ring that's air with a flammable block below becomes fire.

- [ ] Loop over a 7×7 column centered on impact (Y at impact level, ±3 in X and Z).
- [ ] For each `(x, z)`:
  - Find the topmost solid block at that column near `impact.y` (use `level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, ...)` for robustness).
  - If the block above is air and the top block's `BlockState.isFlammable(level, pos, Direction.UP)` returns true, call `level.setBlockAndUpdate(airPos, Blocks.FIRE.defaultBlockState())`.
- [ ] Skip positions outside `IMPACT_RADIUS` (use circular distance check, not square).

### Respect protection

- [ ] Wrap the block-modification loop in a check: if the level is a `ServerLevel`, call `EventHooks.canEntityGrief` or check the `mobGriefing` rule. (Use `level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)`.) For player-caused effects, mob griefing isn't quite the right gate — but adding a configurable toggle is overkill for v1. For now:
- [ ] **Decision for v1:** always ignite, ignore mob griefing. Note here if changed: ___________.

### Particle-only mid-air casts

If the player casts at the sky (no block hit), the impact point is mid-air. Damage and knockback still make sense; fire-ignition does not.

- [ ] Track whether the raytrace returned `Type.MISS`. If so, skip the ignition loop entirely.
- [ ] Damage and knockback still apply (a beam still hits whatever's in the air at the impact point).

### Verify in-game

- [ ] Spawn a cow and a zombie 10 blocks ahead. Cast at the ground in front of them.
  - Both should take damage and catch fire.
  - Both should be pushed away from the impact.
- [ ] Cast at a wood plank floor. Confirm fire blocks appear on the affected tiles.
- [ ] Cast at a stone floor. Confirm no fire (stone isn't flammable).
- [ ] Cast at yourself: damage should be skipped for the caster (`e != player` filter).
- [ ] Cast at peaceful difficulty: confirm hostile mobs are pushed but not damaged (vanilla behavior — peaceful zeros damage to players, not from players).
- [ ] On dedicated server, confirm fire ignition replicates to all viewers.

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
- `EXPLOSION` damage with `Level.ExplosionInteraction.NONE` is an alternative for the area effect, but doing it manually gives you finer control over knockback and fire — recommended for our case.
