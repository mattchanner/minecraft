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

Spawning all particles in one frame looks bad. We need a per-effect tick handler.

Create `com.example.elementalia.client.effect.BookCastEffect`:

- [ ] Fields: `Vec3 origin`, `Vec3 impact`, `RandomSource random` (seeded from payload's `seed`), `int age` (starts at 0), `boolean done`.
- [ ] Constructor takes the payload.
- [ ] Method `tick(ClientLevel level)` advances `age` by 1 and dispatches to private helper methods based on the storyboard above.
- [ ] When `age >= 30`, set `done = true`.

Create `com.example.elementalia.client.effect.EffectManager`:

- [ ] Static list `List<BookCastEffect> active`.
- [ ] Static `add(BookCastEffect e)`.
- [ ] Static `tick(ClientLevel level)` — iterate `active`, call `tick(level)` on each, remove those with `done == true`. Use an iterator for safe removal.

### Hook the tick

- [ ] In `com.example.elementalia.client.ClientEvents` (annotated `@EventBusSubscriber(modid = Elementalia.MODID, value = Dist.CLIENT)`), subscribe to `ClientTickEvent.Post`.
- [ ] In the handler, get `Minecraft.getInstance().level`. If non-null, call `EffectManager.tick(level)`.

### Wire the client handler

Back in `ClientBookCastHandler.handle(...)`:

- [ ] Replace the Phase 03 log line with:
  ```java
  ctx.enqueueWork(() -> EffectManager.add(new BookCastEffect(payload)));
  ```
- [ ] `enqueueWork` ensures the effect is created on the main client thread.

### Implement the beam

In `BookCastEffect.tickBeam(ClientLevel level)`:

- [ ] Compute direction = `impact.subtract(origin).normalize()`.
- [ ] Distance = `origin.distanceTo(impact)`.
- [ ] Determine current beam front position: `frontDist = min(distance, age * 4.0)`.
- [ ] Walk from 0 to `frontDist` in steps of 0.3. At each step:
  - Spawn `ParticleTypes.FLAME` with small jittered offset (`random.nextGaussian() * 0.05` on each axis) and zero velocity.
  - Every 3rd step, also spawn `ParticleTypes.END_ROD` for a bright shimmer.
- [ ] Only run while `age < 10`.

### Implement the ground crack

In `BookCastEffect.tickGroundCrack(ClientLevel level)`:

- [ ] Only run on ticks 8 through 14 inclusive.
- [ ] On tick 8 only: spawn one `ParticleTypes.EXPLOSION` at impact and play `SoundEvents.GENERIC_EXPLODE` via `level.playLocalSound(impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.7f, false)`.
- [ ] Each tick in the range, spawn ~8 `BlockParticleOption(ParticleTypes.BLOCK, impactBlockState)` particles, where `impactBlockState` is `level.getBlockState(BlockPos.containing(impact).below())`.
  - Random offsets within a 3-block radius circle, slightly above the ground.
  - Small upward velocity (`0.1` y) plus random horizontal velocity (`±0.15`).

### Implement the fire eruption

In `BookCastEffect.tickEruption(ClientLevel level)`:

- [ ] Only run on ticks 10 through 30.
- [ ] Compute 8 jet positions: angles `i * (2π/8)` for `i = 0..7`, at distance `IMPACT_RADIUS` from impact (XZ plane, Y = impact.y).
- [ ] Intensity = `1.0 - (age - 10) / 20.0` (linearly fades from 1 to 0 over the 20-tick window).
- [ ] At each jet, spawn:
  - 2 × `ParticleTypes.FLAME` rising with velocity `(0, 0.3 * intensity, 0)`.
  - 1 × `ParticleTypes.LAVA` (sparingly — it's a heavier particle) every 4 ticks.
  - 1 × `ParticleTypes.LARGE_SMOKE` at the top of the jet trail.
- [ ] On tick 10, play `SoundEvents.BLAZE_SHOOT` once at impact.

### Initial sound at origin

- [ ] In the `BookCastEffect` constructor or on the first `tick(...)` call, play `SoundEvents.FIRECHARGE_USE` (a satisfying ignition sound) at the origin position, volume 1.0, pitch 0.8.

### Verify in-game

- [ ] `./gradlew runClient`. Cast the book at the ground from 10 blocks away.
- [ ] You should see:
  - A streak of orange particles racing from the book to the ground (~0.5s).
  - A puff of explosion + scattered stone-bits at the impact point.
  - A ring of 8 small fire jets rising for ~1s, fading out.
- [ ] You should hear three layered sounds: firecharge ignition → muffled boom → blaze shoot.
- [ ] Cast at the sky (no block hit). The beam should still play out to its max range but ground crack and eruption should still emit since impact is at the end-of-beam point in mid-air. **Decision needed:** if mid-air eruption looks wrong, gate ground/eruption emission on actual block hits — note the choice here:
  - [ ] Decision: ___________ (fill in after first playtest).
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
