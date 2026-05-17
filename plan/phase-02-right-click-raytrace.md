# Phase 02 — Right-click and raytrace

**Goal:** Right-clicking the Fire Book performs a server-side raytrace to find the ground impact point, logs the result, and starts a cooldown. No visuals yet.

**Prerequisites:** Phase 01 complete.

## Tasks

### Override use behavior

In `FireBookItem`:

- [ ] Override `InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)`.
- [ ] On the client side (`level.isClientSide`), return `InteractionResultHolder.success(...)` so the player swings their arm. Do not run any logic — wait for the server to authorize.
- [ ] On the server side, call into a `cast(...)` method (defined below) that does the work, then return `InteractionResultHolder.success(...)`.

### Casting parameters (constants in `FireBookItem`)

Define these as `private static final` constants so they're easy to tune later:

- [ ] `MAX_BEAM_DISTANCE = 24.0` (blocks)
- [ ] `COOLDOWN_TICKS = 60` (3 seconds at 20 tps — generous for testing, tune later)
- [ ] `IMPACT_RADIUS = 3.0` (used in Phase 05, but define now)

### Raytrace

In a private method `private void cast(ServerLevel level, ServerPlayer player, ItemStack stack)`:

- [ ] Build a `Vec3` start at the player's eye position: `player.getEyePosition()`.
- [ ] Build a `Vec3` end at `start.add(player.getLookAngle().scale(MAX_BEAM_DISTANCE))`.
- [ ] Construct a `ClipContext` with:
  - `Block = ClipContext.Block.COLLIDER`
  - `Fluid = ClipContext.Fluid.NONE`
  - The player as the entity for context.
- [ ] Call `level.clip(clipContext)` to get a `BlockHitResult`.
- [ ] If `hitResult.getType() == HitResult.Type.MISS`, use the end vector as the impact point (the beam dissipates in mid-air).
- [ ] Otherwise use `hitResult.getLocation()` as the impact point.

### Cooldown via data component

NeoForge 1.21+ exposes `ItemCooldowns` for per-item cooldowns. We use it directly — no custom component needed yet.

- [ ] After the raytrace, call `player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS)`.
- [ ] In `use()`, **before** the cast, check `player.getCooldowns().isOnCooldown(this)`. If true, return `InteractionResultHolder.fail(stack)` immediately. Do this on both sides so the client doesn't desync.

### Logging

Add a `private static final Logger LOGGER = LogUtils.getLogger();` at the top of `FireBookItem`.

- [ ] In `cast`, log: `LOGGER.info("Fire Book cast: origin={} target={} distance={}", origin, impact, origin.distanceTo(impact));`
- [ ] This goes away in Phase 04 once visuals replace it as the feedback signal.

### Verify in-game

- [ ] Run `./gradlew runClient`. Use `/give @s elementalia:fire_book`.
- [ ] Right-click while looking at the ground. Confirm:
  - The player swings their arm.
  - The game log (in IntelliJ's run window or `latest.log`) contains a `Fire Book cast: origin=... target=... distance=...` line.
- [ ] Right-click again immediately. Confirm no second log entry (cooldown working).
- [ ] Wait 3 seconds, right-click again. Confirm a new log entry.
- [ ] Right-click while looking at the sky (no block in range). Confirm a log entry where the target is roughly 24 blocks ahead and the distance is ~24.
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
