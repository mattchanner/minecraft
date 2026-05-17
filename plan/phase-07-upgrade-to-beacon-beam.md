# Phase 07 — Upgrade to beacon-style beam (Approach B)

**Goal:** Replace the particle-stream beam from Phase 04 with a real rendered beam: a textured quad that extends from the book to the impact point, glowing and pulsing like a beacon beam. Ground crack and fire eruption keep their particle-based implementation.

**Prerequisites:** Phase 06 complete. (Don't start this phase until v1 ships and works.)

## Background reading

Before writing code, read these vanilla classes:

- [ ] `net.minecraft.client.renderer.blockentity.BeaconRenderer` — reference implementation for beam rendering. Note especially `renderBeaconBeam(...)`.
- [ ] `net.minecraft.world.entity.AreaEffectCloud` and its renderer — a good example of a short-lived "effect entity" pattern.
- [ ] NeoForge's docs on `EntityType` registration and `EntityRendererProvider`.

## Approach

The beam isn't really an entity in the gameplay sense, but using an entity is the cleanest way to get a custom renderer per-instance with positions the engine already culls and tracks. We'll spawn a server-side, no-collision, short-lived `BookBeamEntity` and give it a `BookBeamRenderer` that draws the beam.

Alternative considered: a `BlockEntity`-less render hook via `RenderLevelStageEvent`. Works, but the entity approach gives free culling, frustum testing, and existence-tracking. Chosen for the upgrade.

## Tasks

### Define the beam entity

Create `com.example.elementalia.entity.BookBeamEntity`:

- [ ] Extends `Entity` (not `LivingEntity`, not `Projectile`).
- [ ] Two `EntityDataAccessor` synced data values: `Vec3` end point (use three floats or a `BlockPos` for simplicity — pick floats since the impact is sub-block precise), and `int` total lifetime ticks.
- [ ] Override `tick()`:
  - Increment age.
  - When age >= total lifetime, call `discard()`.
- [ ] `getBoundingBox()` should be a thin box from origin to end so rendering isn't aggressively culled.
- [ ] Mark `isPushedByFluid() = false`, `canBeCollidedWith() = false`, no gravity.

### Register the entity

In `com.example.elementalia.registry.ModEntities`:

- [ ] `DeferredRegister<EntityType<?>>` on `Registries.ENTITY_TYPE`.
- [ ] Register `BOOK_BEAM` with `EntityType.Builder.<BookBeamEntity>of(BookBeamEntity::new, MobCategory.MISC).sized(0.1f, 0.1f).fireImmune().build("book_beam")`.
- [ ] Call `register(modBus)` from `Elementalia`.

### Spawn the beam server-side

In `FireBookItem.cast(...)`, **in addition to** the existing `BookCastPayload` send (which still drives ground crack and eruption):

- [ ] Construct a `BookBeamEntity` at the origin position.
- [ ] Set its end point to the impact position and lifetime to 12 ticks (slightly longer than the visible beam in Phase 04 — tune in playtest).
- [ ] Call `level.addFreshEntity(beam)`.

Note: this means **the beam visual is no longer driven by the payload**; it's driven by the entity's natural client-side replication. The payload's role shrinks to "trigger the ground crack and eruption particles."

- [ ] Decide whether to remove the beam segment from `BookCastEffect` or keep it as a fallback. Recommendation: remove. The two systems shouldn't double-render.

### Custom renderer

Create `com.example.elementalia.client.render.BookBeamRenderer`:

- [ ] Extends `EntityRenderer<BookBeamEntity>`.
- [ ] In `render(...)`, port the math from `BeaconRenderer.renderBeaconBeam` but with:
  - Origin = entity position.
  - End = entity's synced end point.
  - Length = distance between them.
  - Orientation = look-at matrix oriented along the origin→end vector. Use `Quaternionf` from JOML.
  - Color tint: red→yellow gradient or solid orange. Start solid orange `(1.0, 0.5, 0.1)`.
  - Pulse intensity = `1.0 - age / lifetime` so the beam fades.
- [ ] Reuse the beacon's beam texture (`minecraft:textures/entity/beacon_beam.png`) for v1 — replace with a custom red/orange texture in a later iteration.
- [ ] Use `BeaconRenderer.BEAM_RENDER_DISTANCE` as a reasonable max draw distance.

### Register the renderer (client-only)

- [ ] In `com.example.elementalia.client.ClientSetup` (annotated `@EventBusSubscriber(modid = Elementalia.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)`):
  - Subscribe to `EntityRenderersEvent.RegisterRenderers`.
  - `event.registerEntityRenderer(ModEntities.BOOK_BEAM.get(), BookBeamRenderer::new);`

### Synced data registration

- [ ] Make sure `BookBeamEntity` defines its `EntityDataAccessor` fields with `SynchedEntityData.defineId(...)` and overrides `defineSynchedData(SynchedEntityData.Builder builder)`.
- [ ] Set the end-point values **before** `addFreshEntity` so they're included in the initial spawn packet.

### Custom beam texture (optional v2.5)

- [ ] Create `src/main/resources/assets/elementalia/textures/entity/fire_beam.png` — a 16-wide vertical strip of orange/red flame banding that tiles vertically.
- [ ] Update `BookBeamRenderer` to bind this texture instead of the vanilla beacon beam.

### Verify in-game

- [ ] Cast the book. The beam should now look like a translucent orange shaft (think End Crystal beam or beacon beam) extending from the player to the impact point, rather than a stream of discrete particles.
- [ ] The beam should fade out by tick ~12 and be cleaned up.
- [ ] Confirm the entity is properly discarded — `/data get entity @e[type=elementalia:book_beam,limit=1]` should fail with "no entities found" outside cast moments.
- [ ] Test from many angles: from above, below, looking down the beam axis (this is the trickiest case — confirm the beam doesn't disappear when viewed end-on).
- [ ] Dedicated server: cast on client A, confirm client B sees the beam at the same place and time.
- [ ] Confirm framerate impact is minor compared to the particle approach.

### Cleanup

- [ ] Once the rendered beam is working, remove the beam-stream code from `BookCastEffect` (`tickBeam`). Keep ground crack and eruption — they still work and look good.

## Acceptance criteria

- A glowing orange beam visibly extends from the book to the impact point.
- The beam fades smoothly.
- Beam, ground crack, and eruption are all visible simultaneously and look like one effect.
- No `Entity` leaks — beam entities are discarded after their lifetime.
- Behavior matches across integrated and dedicated server.

## Notes / gotchas

- `BeaconRenderer` uses `RenderType.beaconBeam(...)` — that's the render type you want, not a generic translucent quad.
- Custom entity rendering requires the entity to be spawned via the normal `addFreshEntity` path so the spawn packet replicates synced data. Don't try to spawn it via a custom packet — let vanilla's entity tracking handle it.
- If the beam appears off-position on clients, your synced data isn't set before spawn. Set it on the new entity, then add it.
- The beacon beam math assumes a vertical orientation. The hardest part of this phase is generalizing it to arbitrary orientations. Use a quaternion to rotate the rendering frame from `(0, 1, 0)` to the normalized origin→end vector, then call the original draw math.
- If you need a worked example with arbitrary orientation, look at the Create mod's beam-rendering code (BSD-licensed, open on GitHub) — they solved this exact problem.
