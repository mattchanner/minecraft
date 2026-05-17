# Phase 03 — Networking payload

**Goal:** When the server resolves a Fire Book cast, it sends a custom packet to nearby clients carrying the origin and impact positions. Clients receive it and log. No rendering yet.

**Prerequisites:** Phase 02 complete.

## Tasks

### Define the payload

NeoForge 1.21.4 uses the `CustomPacketPayload` interface. Create `com.example.elementalia.network.BookCastPayload`:

- [x] Define as a Java `record`:
  ```java
  public record BookCastPayload(Vec3 origin, Vec3 impact, int seed) implements CustomPacketPayload { ... }
  ```
  - `origin` — where the beam starts (player's eye position at cast time)
  - `impact` — where the beam ends
  - `seed` — `int`, used by clients to make particle scatter deterministic across viewers (so everyone sees the same eruption pattern). Server passes `level.random.nextInt()`.
- [x] Define the `Type<BookCastPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "book_cast"));`.
- [x] Define the `StreamCodec<FriendlyByteBuf, BookCastPayload>` using `StreamCodec.composite(...)` over the three fields. Use a helper `VEC3_STREAM_CODEC` defined in the same class for `Vec3`.
- [x] Implement `type()` returning `TYPE`.

### Register the payload

Create `com.example.elementalia.network.ModNetwork`:

- [x] Subscribe to `RegisterPayloadHandlersEvent` on the mod event bus.
- [x] In the handler, get a `PayloadRegistrar` with `event.registrar("1")` (version string — bump if the format ever changes).
- [x] Call `registrar.playToClient(BookCastPayload.TYPE, BookCastPayload.STREAM_CODEC, ClientBookCastHandler::handle)`.
- [x] `ClientBookCastHandler` lives under `com.example.elementalia.client.network` — see below.

### Client handler (client-only code)

Create `com.example.elementalia.client.network.ClientBookCastHandler`:

- [x] Annotate the class or methods with the appropriate `Dist.CLIENT` boundary so the server JAR never tries to load it. The cleanest way: only reference this class from inside `ModNetwork`'s registration call, and ensure `ModNetwork` itself does not import any client classes — the lambda reference is resolved lazily.
- [x] `public static void handle(BookCastPayload payload, IPayloadContext ctx)`.
- [x] For Phase 03, just log: `LOGGER.info("Client received BookCast: origin={} impact={}", payload.origin(), payload.impact());`.
- [x] **Do not** spawn particles or anything else yet. That's Phase 04.

### Wire the server send

Back in `FireBookItem.cast(...)`:

- [x] After computing the impact point but before the cooldown call, send the payload to all players tracking the caster:
  ```java
  PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
      new BookCastPayload(origin, impact, level.random.nextInt()));
  ```
- [x] This automatically excludes players outside view range and includes the caster themselves.

### Verify in-game

- [x] Run `./gradlew runClient` (singleplayer).
- [x] Right-click the Fire Book. In the same log output you should see:
  - `Fire Book cast: origin=... target=...` (server log line from Phase 02)
  - `Client received BookCast: origin=... impact=...` (client log line, new this phase)
- [ ] Run `./gradlew runServer` and join with a separate client.
  - Server log gets the cast line.
  - Client log gets the receive line.
- [ ] Have a second client join. Right-click on player A's side. Confirm player B's log also receives the payload (since B is tracking A within view range).

## Acceptance criteria

- A `BookCastPayload` packet is defined, registered, and sent from the server on each cast.
- All clients tracking the caster receive and log the payload.
- The dedicated server does **not** import or load `ClientBookCastHandler` at startup. Verify by checking the server log for class loader errors — there should be none.

## Notes / gotchas

- The single most common bug here: importing a client class into common code by accident. If the server crashes on startup with `NoClassDefFoundError: net/minecraft/client/...`, that's the cause. The fix is to access the client handler only via a method reference inside a registrar call, or to use `DistExecutor.unsafeRunWhenOn` for indirect dispatch.
- `Vec3` streaming: there is no built-in `Vec3` stream codec. Either compose one from three `DOUBLE` codecs, or use the byte buffer's `writeDouble` / `readDouble` directly inside a custom codec. Compositing is cleaner.
- The `seed` field exists so that when Phase 04 generates randomized particle positions, every observing client uses the same RNG — meaning everyone sees the same eruption pattern at the same place. Without this, each client picks its own random and they'd disagree.
- `PacketDistributor.sendToPlayersTrackingEntityAndSelf` is the right channel for "effects originating from a player." For block-origin effects (Phase 08), use `sendToPlayersTrackingChunk` instead.
- **1.21.4 note:** The `StreamCodec` buffer type is `ByteBuf` (not `FriendlyByteBuf`) when composing from `ByteBufCodecs.DOUBLE`/`INT`. This is compatible with `playToClient`'s `? super RegistryFriendlyByteBuf` bound. `Function3` factory comes from `com.mojang.datafixers.util`.
