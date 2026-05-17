# Phase 02 — Right-click and raytrace

**Goal:** Right-clicking the Fire Book performs a server-side raytrace to find the ground impact point, logs the result, and starts a cooldown. No visuals yet.

**Prerequisites:** Phase 01 complete.

## Tasks

### Override use behavior

In `FireBookItem`:

- [x] Override `InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)`.
- [x] On the client side (`level.isClientSide`), return `InteractionResultHolder.success(...)` so the player swings their arm. Do not run any logic — wait for the server to authorize.
- [x] On the server side, call into a `cast(...)` method (defined below) that does the work, then return `InteractionResultHolder.success(...)`.

### Casting parameters (constants in `FireBookItem`)

Define these as `private static final` constants so they're easy to tune later:

- [x] `MAX_BEAM_DISTANCE = 24.0` (blocks)
- [x] `COOLDOWN_TICKS = 60` (3 seconds at 20 tps — generous for testing, tune later)
- [x] `IMPACT_RADIUS = 3.0` (used in Phase 05, but define now)

### Raytrace

In a private method `private void cast(ServerLevel level, ServerPlayer player, ItemStack stack)`:

- [x] Build a `Vec3` start at the player's eye position: `player.getEyePosition()`.
- [x] Build a `Vec3` end at `start.add(player.getLookAngle().scale(MAX_BEAM_DISTANCE))`.
- [x] Construct a `ClipContext` with:
  - `Block = ClipContext.Block.COLLIDER`
  - `Fluid = ClipContext.Fluid.NONE`
  - The player as the entity for context.
- [x] Call `level.clip(clipContext)` to get a `BlockHitResult`.
- [x] If `hitResult.getType() == HitResult.Type.MISS`, use the end vector as the impact point (the beam dissipates in mid-air).
- [x] Otherwise use `hitResult.getLocation()` as the impact point.

### Cooldown via data component

NeoForge 1.21+ exposes `ItemCooldowns` for per-item cooldowns. We use it directly — no custom component needed yet.

- [x] After the raytrace, call `player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS)`.
- [x] In `use()`, **before** the cast, check `player.getCooldowns().isOnCooldown(this)`. If true, return `InteractionResultHolder.fail(stack)` immediately. Do this on both sides so the client doesn't desync.

### Logging

Add a `private static final Logger LOGGER = LogUtils.getLogger();` at the top of `FireBookItem`.

- [x] In `cast`, log: `LOGGER.info("Fire Book cast: origin={} target={} distance={}", origin, impact, origin.distanceTo(impact));`
- [x] This goes away in Phase 04 once visuals replace it as the feedback signal.

### Verify in-game

- [x] Run `./gradlew runClient`. Use `/give @s elementalia:fire_book`.
- [x] Right-click while looking at the ground. Confirm:
  - The player swings their arm.
  - The game log (in IntelliJ's run window or `latest.log`) contains a `Fire Book cast: origin=... target=... distance=...` line.
- [x] Right-click again immediately. Confirm no second log entry (cooldown working).
- [x] Wait 3 seconds, right-click again. Confirm a new log entry.
- [x] Right-click while looking at the sky (no block in range). Confirm a log entry where the target is roughly 24 blocks ahead and the distance is ~24.
- [ ] Repeat on dedicated server (`runServer` + a client connecting). Confirm the logs appear in the **server** console, not the client console.

## Acceptance criteria

- Right-clicking the Fire Book on the server produces a log entry with origin and target coordinates.
- The cooldown prevents spam.
- Behavior is identical on integrated server (singleplayer) and dedicated server.
- No client/server crash; no client-only classes imported into `FireBookItem`.

## Notes / gotchas

- `Item#use` runs on **both** sides. The `level.isClientSide` check is essential. Never put server-only state mutations on the client branch.
- The `ClipContext.Fluid.NONE` choice means the beam passes through water. Change to `ANY` later if you want water to stop the beam.
- Logging is fine for now but is the wrong way to confirm gameplay long-term. Phase 04's particles will give the real signal.
- `ServerPlayer` is the dedicated server's player class. `Player` is the common abstract class. Cast safely after the `isClientSide` check.
