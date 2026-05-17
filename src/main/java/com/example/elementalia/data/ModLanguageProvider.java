package com.example.elementalia.data;

import com.example.elementalia.registry.ModBlocks;
import com.example.elementalia.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        // ---- Items ----

        addItem(ModItems.FIRE_BOOK,  "Fire Book");
        add("item.elementalia.fire_book.tooltip", "The pages crackle with bound flame.");
        add("item.elementalia.fire_book.charges", "%s charges remaining");

        addItem(ModItems.ICE_BOOK,   "Ice Book");
        add("item.elementalia.ice_book.tooltip",  "The pages whisper of stilled winter.");
        add("item.elementalia.ice_book.charges",  "%s charges remaining");

        addItem(ModItems.EARTH_BOOK, "Earth Book");
        add("item.elementalia.earth_book.tooltip", "The pages press heavy with sleeping stone.");
        add("item.elementalia.earth_book.charges", "%s charges remaining");

        addItem(ModItems.WIND_BOOK,  "Wind Book");
        add("item.elementalia.wind_book.tooltip",  "The pages shiver with restless air.");
        add("item.elementalia.wind_book.charges",  "%s charges remaining");

        // ---- Blocks ----

        addBlock(ModBlocks.TOME_RITUAL, "Tome Altar");

        // ---- Creative tab ----

        add("itemGroup.elementalia", "Elementalia");

        // ---- Advancements ----

        add("advancements.elementalia.root.title",        "Bound Pages");
        add("advancements.elementalia.root.description",  "A primer for forces older than fire.");

        add("advancements.elementalia.first_burn.title",       "First Burn");
        add("advancements.elementalia.first_burn.description", "Bind a tome to flame.");

        add("advancements.elementalia.first_frost.title",       "First Frost");
        add("advancements.elementalia.first_frost.description", "Carry winter in a single bucket.");

        add("advancements.elementalia.first_quake.title",       "First Quake");
        add("advancements.elementalia.first_quake.description", "Pry up the deep stone.");

        add("advancements.elementalia.first_gale.title",       "First Gale");
        add("advancements.elementalia.first_gale.description", "Catch the breath of a Breeze.");
    }
}
