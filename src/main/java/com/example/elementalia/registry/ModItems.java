package com.example.elementalia.registry;

import com.example.elementalia.Elementalia;
import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.item.FireBookItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Elementalia.MODID);

    // In 1.21.4 Item.Properties requires setId() before construction.
    // Use the Function<ResourceLocation, I> overload: NeoForge passes the registry RL
    // so we can build the ResourceKey and set it on the properties ourselves.
    public static final DeferredItem<FireBookItem> FIRE_BOOK =
            ITEMS.register("fire_book", id -> new FireBookItem(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, id))
                            .stacksTo(1)
                            .component(ModDataComponents.CHARGES.get(), 3)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
