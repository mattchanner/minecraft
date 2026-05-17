# Phase 06 — Polish v1

**Goal:** Ship-quality v1: real texture, recipe to craft the book, tooltip describing it, charges instead of unlimited use, datagen pipeline, advancement for first use.

**Prerequisites:** Phase 05 complete.

## Tasks

### Real texture

- [x] Replace `src/main/resources/assets/elementalia/textures/item/fire_book.png` with a 16×16 fire-themed book texture. If you don't have art yet, search "minecraft book texture template" — the leather book texture is a fine starting base — and recolor to fire tones (deep red cover, orange runes).
- [x] Optionally add a second model for the "open" state (used during the cast animation in Phase 07). For now, a single closed-book texture is fine.

### Tooltip

In `FireBookItem`:

- [x] Override `appendHoverText(ItemStack, TooltipContext, List<Component>, TooltipFlag)`.
- [x] Add one or two flavor lines using `Component.translatable("item.elementalia.fire_book.tooltip")`.
- [x] Add a charges line: `Component.translatable("item.elementalia.fire_book.charges", remainingCharges).withStyle(ChatFormatting.GRAY)`.
- [x] Update `en_us.json` with the two new keys:
  - `item.elementalia.fire_book.tooltip` — flavor text (e.g., "The pages crackle with bound flame.")
  - `item.elementalia.fire_book.charges` — `%s charges remaining`

### Charges via data component

Replace the unlimited-use design with 3 charges per book.

- [x] Create `com.example.elementalia.component.ModDataComponents`:
  - `DeferredRegister<DataComponentType<?>>` on `Registries.DATA_COMPONENT_TYPE`.
  - Register `CHARGES` as a `DataComponentType<Integer>` with a `Codec.INT` codec and `ByteBufCodecs.VAR_INT` network codec.
- [x] In `ModItems`, when registering `fire_book`, attach an initial value via `Item.Properties().component(ModDataComponents.CHARGES.get(), 3)`.
- [x] In `FireBookItem.use(...)`:
  - Read current charges via `stack.getOrDefault(ModDataComponents.CHARGES.get(), 0)`.
  - If zero, return `InteractionResultHolder.fail(stack)` and play a quiet "fizzle" sound (`SoundEvents.FIRE_EXTINGUISH`, pitch 1.5).
  - Otherwise decrement: `stack.set(ModDataComponents.CHARGES.get(), current - 1)`.
- [x] Update the tooltip to read from this component.

### Refilling charges — decide

- [x] **Decision for v1:** charges don't refill. The book is consumable-ish. Players can craft more. Note here if changed: ___________.
- [x] (Phase 08 introduces a ritual / brewing recipe for refilling.)

### Recipe via datagen

NeoForge's datagen system generates JSON from Java at build time. Set it up properly now — every future item benefits.

- [x] Create `com.example.elementalia.data.DataGenerators` annotated `@EventBusSubscriber(modid = Elementalia.MODID, bus = EventBusSubscriber.Bus.MOD)`.
- [x] Subscribe to `GatherDataEvent`. In the handler:
  - Get the `DataGenerator`, `PackOutput`, and `CompletableFuture<HolderLookup.Provider>` from the event.
  - Add a `ModRecipeProvider` (extends `RecipeProvider`).
  - Add a `ModLanguageProvider` (extends `LanguageProvider`) and move the entries from `en_us.json` into it — JSON gets regenerated.
- [x] Implement the Fire Book recipe in `ModRecipeProvider`:
  - Shaped, 3×3:
    - Top row: blaze powder, blaze rod, blaze powder
    - Middle: leather, book, leather
    - Bottom: blaze powder, blaze rod, blaze powder
  - Output: 1 fire book.
  - Adjust if too cheap/expensive after playtesting.
- [x] Add `./gradlew runClientData` to your workflow (NeoGradle 7.1 uses `clientData` not `data`). This regenerates JSON into `src/generated/resources/`, which Gradle automatically includes in the jar.

### Advancement: "First Burn"

- [x] In a `ModAdvancementProvider` (implements `AdvancementSubProvider`), add an advancement triggered the first time the player acquires the Fire Book.
- [x] Use `InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FIRE_BOOK.get())` so the advancement fires when the player first acquires the book.
- [x] Title: "First Burn." Description: "Bind a tome to flame."

### Game test (optional but recommended)

NeoForge supports `@GameTest` for automated in-world testing.

- [ ] Add a `GameTest` that:
  - Spawns a zombie and a wool block in front of a fake player.
  - Has the fake player cast a Fire Book at the wool.
  - Asserts after 30 ticks: zombie is on fire, wool position has a fire block, charges decremented.
- [ ] Wire `runGameTestServer` into the Gradle config. Run it locally as a sanity gate.

### Final build sanity

- [x] `./gradlew runClientData && ./gradlew build` passes end-to-end.
- [x] Inspect the built jar in `build/libs/`. Confirm it contains:
  - The generated recipe JSON.
  - The lang file (now from datagen).
  - The advancement JSON.
  - Your texture.

## Acceptance criteria

- The Fire Book has 3 charges, decrements on use, fizzles at 0.
- A crafting recipe exists and works in survival.
- Tooltip shows flavor text and remaining charges.
- An advancement triggers on first acquisition.
- Datagen produces all repetitive JSON; no JSON is hand-written outside `models/` and `textures/`.
- A `./gradlew clean build` from scratch produces a usable jar.

## Notes / gotchas

- Data components replaced item NBT in 1.20.5. Don't mix them with old NBT calls in tutorials predating 1.20.5 — those are wrong for 1.21.4.
- After `runClientData` runs, the generated files are real files on disk under `src/generated/resources/`. Commit them — they're part of the source of truth for the mod's JSON content. (Some projects don't commit generated files; doing so makes review of changes much easier. Recommended for this mod.)
- NeoGradle 7.1 does not have a `data` run type — use `clientData` (task `runClientData`) for all datagen.
- If your recipe doesn't appear in the recipe book, check the `id()` you used and that the recipe provider's `pack` is registered with the data generator.
- The "First Burn" advancement is decorative for v1 but lays groundwork for the Phase 08 progression tree.
