package com.example.elementalia;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class is only loaded on the physical client — safe to access client-side APIs here.
@Mod(value = Elementalia.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Elementalia.MODID, value = Dist.CLIENT)
public class ElementaliaClient {

    public ElementaliaClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Elementalia.LOGGER.info("Elementalia client setup complete.");
    }
}
