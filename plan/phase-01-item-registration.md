# Phase 01 — Item registration

**Goal:** The Fire Book exists as a registered item with a placeholder texture, appears in a creative tab, and can be obtained in-game via `/give`.

**Prerequisites:** Phase 00 complete.

## Tasks

### Package layout

Establish the package structure we'll use for the rest of the mod. Inside `com.example.elementalia`:

- [ ] `Elementalia.java` — mod main class (already exists from Phase 00).
- [ ] `registry/ModItems.java` — `DeferredRegister<Item>` for all our items.
- [ ] `registry/ModCreativeTabs.java` — `DeferredRegister` for our creative tab.
- [ ] `item/FireBookItem.java` — the item class.
- [ ] `client/` — empty for now, will hold client-only code in Phase 04.

### ModItems

- [ ] Create `ModItems` with a `DeferredRegister.Items` instance named after `Elementalia.MODID`.
- [ ] Register a `Supplier<Item>` entry named `fire_book` returning a `new FireBookItem(new Item.Properties().stacksTo(1))`.
- [ ] Add a `public static void register(IEventBus bus)` method that calls `ITEMS.register(bus)`.

### FireBookItem

- [ ] Create `FireBookItem extends Item`.
- [ ] Single constructor accepting `Item.Properties`.
- [ ] No behavior yet — `use()` override comes in Phase 02. This phase is purely making the item exist.

### Creative tab

- [ ] Create `ModCreativeTabs` with a `DeferredRegister<CreativeModeTab>` for `Registries.CREATIVE_MODE_TAB`.
- [ ] Register one tab `elementalia_tab`:
  - Title: `Component.translatable("itemGroup.elementalia")`
  - Icon: `ModItems.FIRE_BOOK.get().getDefaultInstance()`
  - Contents: add `FIRE_BOOK` via `output.accept(...)`.
- [ ] Add a `register(IEventBus bus)` method.

### Wire registers into the mod main class

- [ ] In `Elementalia` constructor, call `ModItems.register(modBus)` and `ModCreativeTabs.register(modBus)` using the `IEventBus` passed in.

### Assets — model

- [ ] Create `src/main/resources/assets/elementalia/models/item/fire_book.json`:
  ```json
  {
    "parent": "minecraft:item/generated",
    "textures": {
      "layer0": "elementalia:item/fire_book"
    }
  }
  ```

### Assets — texture

- [ ] Create `src/main/resources/assets/elementalia/textures/item/fire_book.png` as a 16x16 placeholder. Any recognizable image is fine for now — we'll replace it later. A solid red square with a yellow border works.

### Assets — language file

- [ ] Create `src/main/resources/assets/elementalia/lang/en_us.json`:
  ```json
  {
    "item.elementalia.fire_book": "Fire Book",
    "itemGroup.elementalia": "Elementalia"
  }
  ```

### Verify in-game

- [ ] Run `./gradlew runClient`.
- [ ] In a creative world, confirm the **Elementalia** tab is in the creative inventory.
- [ ] Confirm the Fire Book appears in that tab with the placeholder texture and the name "Fire Book".
- [ ] In a survival world, run `/give @s elementalia:fire_book`. Confirm the item appears in your inventory.
- [ ] Run `./gradlew runServer`, join it from a separate client, and confirm `/give` works there too.

## Acceptance criteria

- `elementalia:fire_book` is a registered, obtainable item.
- It has a model and texture (no purple/black missing-texture checkerboard).
- It has a localized display name.
- The Elementalia creative tab exists and contains it.
- Works on both integrated and dedicated server.

## Notes / gotchas

- Item registration is the most boilerplate-heavy part of MC modding. From here on it gets more fun.
- `Item.Properties().stacksTo(1)` — a "book" item that the player will eventually have charges on should not stack, since stacking would share state.
- If the texture shows as purple/black checkerboard, the path is wrong. Path must be exactly `assets/elementalia/textures/item/fire_book.png` and the model's `layer0` must be `elementalia:item/fire_book` (note: no `textures/` prefix, no `.png` suffix in the model).
- The translation key format is `item.<modid>.<itemid>`. Mismatches show as the raw key in-game — that's your fix signal.
