package com.example.elementalia.component;

import com.example.elementalia.Elementalia;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Elementalia.MODID);

    /**
     * Remaining casts on this book stack.
     * Persisted to NBT (via Codec.INT) and synced to clients (via VAR_INT).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGES =
            DATA_COMPONENTS.register("charges", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
