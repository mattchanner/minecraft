package com.example.elementalia.data;

import com.example.elementalia.Elementalia;
import com.example.elementalia.registry.ModBlocks;
import com.example.elementalia.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.stream.Stream;

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
        // Registers assets/elementalia/items/<id>.json pointing at the hand-authored
        // models/item/<id>.json geometry for each tome.
        itemModels.declareCustomModelItem(ModItems.FIRE_BOOK.get());
        itemModels.declareCustomModelItem(ModItems.ICE_BOOK.get());
        itemModels.declareCustomModelItem(ModItems.EARTH_BOOK.get());
        itemModels.declareCustomModelItem(ModItems.WIND_BOOK.get());

        // Block item — emits the assets/elementalia/items/tome_ritual.json
        // pointer that references our hand-authored item model.
        itemModels.declareCustomModelItem(ModBlocks.TOME_RITUAL_ITEM.get());
    }

    /**
     * Skip block-state validation. We hand-author the blockstate and block
     * model JSON for {@code tome_ritual} under {@code src/main/resources/} —
     * returning empty here tells ModelProvider not to expect those files
     * from datagen.
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }
}
