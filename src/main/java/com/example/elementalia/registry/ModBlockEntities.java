package com.example.elementalia.registry;

import com.example.elementalia.Elementalia;
import com.example.elementalia.block.entity.TomeRitualBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Elementalia.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TomeRitualBlockEntity>> TOME_RITUAL =
            BLOCK_ENTITY_TYPES.register("tome_ritual",
                    () -> new BlockEntityType<>(TomeRitualBlockEntity::new, ModBlocks.TOME_RITUAL.get()));

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
