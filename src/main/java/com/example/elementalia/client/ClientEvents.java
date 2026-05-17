package com.example.elementalia.client;

import com.example.elementalia.Elementalia;
import com.example.elementalia.client.effect.EffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client-only game-event subscribers.
 * Only loaded on the physical client ({@code Dist.CLIENT}).
 */
@EventBusSubscriber(modid = Elementalia.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            EffectManager.tick(level);
        }
    }
}
