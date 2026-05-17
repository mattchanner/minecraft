package com.example.elementalia;

import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.config.ElementaliaConfig;
import com.example.elementalia.registry.ModBlockEntities;
import com.example.elementalia.registry.ModBlocks;
import com.example.elementalia.registry.ModCreativeTabs;
import com.example.elementalia.registry.ModEntities;
import com.example.elementalia.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Elementalia.MODID)
public class Elementalia {

    public static final String MODID = "elementalia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Elementalia(IEventBus modEventBus, ModContainer modContainer) {
        // Components must be registered before items that reference them.
        ModDataComponents.register(modEventBus);
        // Blocks must register before their item-form (BlockItem) and BlockEntityType.
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);

        // Config registration must use the ModContainer (per-mod) and not the event bus.
        modContainer.registerConfig(ModConfig.Type.COMMON, ElementaliaConfig.SPEC);

        LOGGER.info("Elementalia loading...");
    }
}
