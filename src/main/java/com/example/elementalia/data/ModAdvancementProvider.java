package com.example.elementalia.data;

import com.example.elementalia.Elementalia;
import com.example.elementalia.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * Builds the elemental progression tree:
 *
 *   root (Bound Pages)
 *     └── first_burn  (Fire,  trigger: own a Fire Book)
 *           ├── first_frost  (Ice,   trigger: pick up a Powder Snow Bucket)
 *           └── first_quake  (Earth, trigger: pick up Deepslate)
 *                  └── first_gale (Wind, trigger: pick up a Breeze Rod)
 *
 * Triggers are vanilla {@code InventoryChangeTrigger}s so no custom advancement
 * trigger class is required.
 */
public class ModAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
        // Root — sits in the Adventure tab. Triggers as soon as any tome is owned;
        // the children require specific tomes / reagents.
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        ModItems.FIRE_BOOK.get(),
                        Component.translatable("advancements.elementalia.root.title"),
                        Component.translatable("advancements.elementalia.root.description"),
                        ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementType.TASK,
                        /* showToast= */ false,
                        /* announceChat= */ false,
                        /* hidden= */ false)
                .addCriterion("has_any_tome",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FIRE_BOOK.get()))
                .save(writer, ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "root"));

        // Fire — "First Burn"
        AdvancementHolder firstBurn = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        ModItems.FIRE_BOOK.get(),
                        Component.translatable("advancements.elementalia.first_burn.title"),
                        Component.translatable("advancements.elementalia.first_burn.description"),
                        /* background= */ null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("has_fire_book",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FIRE_BOOK.get()))
                .save(writer, ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "first_burn"));

        // Ice — "First Frost" — pick up a powder snow bucket
        Advancement.Builder.advancement()
                .parent(firstBurn)
                .display(
                        ModItems.ICE_BOOK.get(),
                        Component.translatable("advancements.elementalia.first_frost.title"),
                        Component.translatable("advancements.elementalia.first_frost.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_powder_snow_bucket",
                        InventoryChangeTrigger.TriggerInstance.hasItems(Items.POWDER_SNOW_BUCKET))
                .save(writer, ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "first_frost"));

        // Earth — "First Quake" — pick up deepslate
        AdvancementHolder firstQuake = Advancement.Builder.advancement()
                .parent(firstBurn)
                .display(
                        ModItems.EARTH_BOOK.get(),
                        Component.translatable("advancements.elementalia.first_quake.title"),
                        Component.translatable("advancements.elementalia.first_quake.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_deepslate",
                        InventoryChangeTrigger.TriggerInstance.hasItems(Items.DEEPSLATE))
                .save(writer, ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "first_quake"));

        // Wind — "First Gale" — pick up a breeze rod
        Advancement.Builder.advancement()
                .parent(firstQuake)
                .display(
                        ModItems.WIND_BOOK.get(),
                        Component.translatable("advancements.elementalia.first_gale.title"),
                        Component.translatable("advancements.elementalia.first_gale.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_breeze_rod",
                        InventoryChangeTrigger.TriggerInstance.hasItems(Items.BREEZE_ROD))
                .save(writer, ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "first_gale"));
    }
}
