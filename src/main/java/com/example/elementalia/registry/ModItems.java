package com.example.elementalia.registry;

import com.example.elementalia.Elementalia;
import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.item.EarthBookItem;
import com.example.elementalia.item.FireBookItem;
import com.example.elementalia.item.IceBookItem;
import com.example.elementalia.item.WindBookItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Elementalia.MODID);

    private static final int DEFAULT_CHARGES = 3;

    // In 1.21.4 Item.Properties requires setId() before construction.
    // Use the Function<ResourceLocation, I> overload: NeoForge passes the registry RL
    // so we can build the ResourceKey and set it on the properties ourselves.
    public static final DeferredItem<FireBookItem> FIRE_BOOK =
            ITEMS.register("fire_book", id -> new FireBookItem(tomeProperties(id)));

    public static final DeferredItem<IceBookItem> ICE_BOOK =
            ITEMS.register("ice_book", id -> new IceBookItem(tomeProperties(id)));

    public static final DeferredItem<EarthBookItem> EARTH_BOOK =
            ITEMS.register("earth_book", id -> new EarthBookItem(tomeProperties(id)));

    public static final DeferredItem<WindBookItem> WIND_BOOK =
            ITEMS.register("wind_book", id -> new WindBookItem(tomeProperties(id)));

    private static Item.Properties tomeProperties(net.minecraft.resources.ResourceLocation id) {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .stacksTo(1)
                .component(ModDataComponents.CHARGES.get(), DEFAULT_CHARGES);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
