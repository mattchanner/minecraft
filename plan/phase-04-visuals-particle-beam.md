# Phase 04 — Visual effect (particle-stream beam)

**Goal:** When a client receives a `BookCastPayload`, it plays a timed visual sequence: a particle-stream beam from origin to impact, then ground crack particles at impact, then a ring of fire eruption particles. Sound plays alongside.

**Prerequisites:** Phase 03 complete.

**Approach:** This is **Approach A** from the design discussion. Approach B (real rendered beam) is Phase 07.

## Visual storyboard

The effect runs for ~30 ticks (1.5 seconds) total:

| Tick range | What happens |
|---|---|
| 0 | Play `BLAZE_SHOOT` sound at origin. Begin emitting beam particles. |
| 0–10 | Beam: continuous stream of `FLAME` + `END_ROD` particles from origin toward impact, marching forward 4 blocks/tick. |
| 8 | Play `GENERIC_EXPLODE` sound at impact (slightly muffled pitch). Emit `EXPLOSION` particle once. |
| 8–15 | Ground crack: burst of `BLOCK` particles using the impact block's blockstate, scattered in a 3-block radius. |
| 10–30 | Fire eruption: a ring of 8 vertical jets at `IMPACT_RADIUS`, each jet a vertical column of `FLAME` + `LAVA` + `LARGE_SMOKE` particles rising 1.5 blocks. Jets pulse — strongest at tick 10, fading by tick 30. |

All timings are nominal — tune in playtesting.

## Tasks

### Effect scheduler

- [x] Create `com.example.elementalia.client.effect.BookCastEffect` with fields, constructor, `tick(ClientLevel)`, and private helpers.
- [x] Create `com.example.elementalia.client.effect.EffectManager` with static `add` and `tick` methods.

### Hook the tick

- [x] Create `com.example.elementalia.client.ClientEvents` with `@EventBusSubscriber(Dist.CLIENT)`.
- [x] Subscribe to `ClientTickEvent.Post`; call `EffectManager.tick(level)` when level is non-null.

### Wire the client handler

- [x] Replace the Phase 03 log line in `ClientBookCastHandler` with:
  ```java
  ctx.enqueueWork(() -> EffectManager.add(new BookCastEffect(payload)));
  ```

### Implement the beam

- [x] Direction, frontDist, FLAME + END_ROD per step.

### Implement the ground crack

- [x] Ticks 8–14; EXPLOSION + sound on tick 8; scattered BLOCK particles.

### Implement the fire eruption

- [x] Ticks 10–29; 8-jet ring; intensity fade; FLAME + LAVA + LARGE_SMOKE; BLAZE_SHOOT on tick 10.

### Initial sound at origin

- [x] FIRECHARGE_USE at origin on tick 0, vol 1.0, pitch 0.8.

### Verify in-game

- [x] `./gradlew runClient`. Cast the book at the ground from 10 blocks away.
- [x] You should see:
  - A streak of orange particles racing from the book to the ground (~0.5s).
  - A puff of explosion + scattered stone-bits at the impact point.
  - A ring of 8 small fire jets rising for ~1s, fading out.
- [x] You should hear three layered sounds: firecharge ignition → muffled boom → blaze shoot.
- [x] Cast at the sky (no block hit). The beam should still play out to its max range but ground crack and eruption should still emit since impact is at the end-of-beam point in mid-air. **Decision needed:** if mid-air eruption looks wrong, gate ground/eruption emission on actual block hits — note the choice here:
  - [x] Decision: keep eruption at end-of-beam even when no block hit — looks fine in playtesting.
- [ ] `./gradlew runServer` + two clients. Cast on one. Confirm both clients see the effect at the same position with the same eruption pattern (the `seed` is doing its job).
- [ ] Walk 100 blocks away from where another player casts. Confirm you don't receive the packet (tracking range cuts you off — expected).

## Acceptance criteria

- The visual effect plays smoothly on right-click, identical for all viewers.
- No crashes on dedicated server (visual code is fully client-side).
- Sounds layer correctly.
- Effect cleanly disappears after ~30 ticks; no lingering particles.

## Notes / gotchas

- `level.addParticle(...)` is the client-side spawn. **Do not** call this from server code — it does nothing on a dedicated server.
- `level.playLocalSound(..., false)` (the last arg is `distanceDelay`) plays immediately at the position; `true` adds a speed-of-sound delay. For our beam, `false` is right.
- `ParticleTypes.BLOCK` requires a `BlockParticleOption` wrapper carrying the blockstate. Forgetting the wrapper crashes.
- If particle density tanks framerate, the beam loop is the suspect — reduce step granularity from 0.3 to 0.5 before reducing elsewhere.
- Use the same `random` (seeded from payload.seed) for all randomized offsets in this effect, on every client. Otherwise clients diverge visually.
- **1.21.4 note:** `SoundEvents.GENERIC_EXPLODE` is `Holder.Reference<SoundEvent>` — call `.value()` to get the `SoundEvent` for `playLocalSound`. `BLAZE_SHOOT` and `FIRECHARGE_USE` are plain `SoundEvent` fields.
