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

import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
        // "First Burn" — awarded when the player first acquires a Fire Book.
        Advancement.Builder.advancement()
                .display(
                        ModItems.FIRE_BOOK.get(),
                        Component.translatable("advancements.elementalia.first_burn.title"),
                        Component.translatable("advancements.elementalia.first_burn.description"),
                        ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementType.TASK,
                        /* showToast= */ true,
                        /* announceChat= */ true,
                        /* hidden= */ false)
                .addCriterion("has_fire_book",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FIRE_BOOK.get()))
                .save(writer, ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "first_burn"));
    }
}
