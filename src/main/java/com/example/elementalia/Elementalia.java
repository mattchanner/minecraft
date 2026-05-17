package com.example.elementalia;

import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.registry.ModCreativeTabs;
import com.example.elementalia.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Elementalia.MODID)
public class Elementalia {

    public static final String MODID = "elementalia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Elementalia(IEventBus modEventBus, ModContainer modContainer) {
        // Components must be registered before items that reference them.
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        LOGGER.info("Elementalia loading...");
    }
}
