package com.example.elementalia.registry;

import com.example.elementalia.Elementalia;
import com.example.elementalia.block.TomeRitualBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Elementalia.MODID);

    /** Item-form registrations for the blocks above; registered against the items bus. */
    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(Elementalia.MODID);

    public static final DeferredBlock<TomeRitualBlock> TOME_RITUAL = BLOCKS.register(
            "tome_ritual",
            id -> new TomeRitualBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> TOME_RITUAL_ITEM = BLOCK_ITEMS.register(
            "tome_ritual",
            id -> new BlockItem(TOME_RITUAL.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ITEMS.register(bus);
    }
}
