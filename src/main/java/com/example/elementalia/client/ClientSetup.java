package com.example.elementalia.client;

import com.example.elementalia.Elementalia;
import com.example.elementalia.client.render.BookBeamRenderer;
import com.example.elementalia.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only mod-bus subscribers that run after registries are frozen.
 * Kept separate from {@link ClientEvents} (game-bus) to avoid mixing bus types.
 */
@EventBusSubscriber(modid = Elementalia.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BOOK_BEAM.get(), BookBeamRenderer::new);
    }
}
