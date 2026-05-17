package com.example.elementalia.data;

import com.example.elementalia.Elementalia;
import com.example.elementalia.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Generates {@code assets/elementalia/items/*.json} entries (1.21.4+ requirement)
 * and any {@code models/item/} JSON that datagen should own.
 *
 * {@link ItemModelGenerators#declareCustomModelItem} emits only the
 * {@code items/<id>.json} pointer that says "use models/item/<id>.json";
 * the actual geometry JSON is our hand-authored
 * {@code src/main/resources/assets/elementalia/models/item/fire_book.json}.
 */
@OnlyIn(Dist.CLIENT)
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, Elementalia.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Registers assets/elementalia/items/fire_book.json pointing at
        // the hand-authored models/item/fire_book.json geometry.
        itemModels.declareCustomModelItem(ModItems.FIRE_BOOK.get());
    }
}
