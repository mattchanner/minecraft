package com.example.elementalia.data;

import com.example.elementalia.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        // Item name
        addItem(ModItems.FIRE_BOOK, "Fire Book");

        // Tooltip keys
        add("item.elementalia.fire_book.tooltip", "The pages crackle with bound flame.");
        add("item.elementalia.fire_book.charges", "%s charges remaining");

        // Creative tab
        add("itemGroup.elementalia", "Elementalia");

        // Advancement strings
        add("advancements.elementalia.first_burn.title", "First Burn");
        add("advancements.elementalia.first_burn.description", "Bind a tome to flame.");
    }
}
