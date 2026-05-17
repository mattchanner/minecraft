package com.example.elementalia.registry;

import com.example.elementalia.Elementalia;
import com.example.elementalia.entity.BookBeamEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Elementalia.MODID);

    // build() takes ResourceKey<EntityType<?>> (wildcard) — no cast needed.
    public static final DeferredHolder<EntityType<?>, EntityType<BookBeamEntity>> BOOK_BEAM =
            ENTITY_TYPES.register("book_beam", id ->
                    EntityType.Builder.<BookBeamEntity>of(BookBeamEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
