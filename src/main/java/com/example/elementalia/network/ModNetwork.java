package com.example.elementalia.network;

import com.example.elementalia.Elementalia;
import com.example.elementalia.client.network.ClientBookCastHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Elementalia.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // playToClient — the handler is only invoked on the physical client, so
        // ClientBookCastHandler (which may import client-only classes in later phases)
        // will never be loaded on the dedicated server.
        registrar.playToClient(
                BookCastPayload.TYPE,
                BookCastPayload.STREAM_CODEC,
                (payload, ctx) -> ClientBookCastHandler.handle(payload, ctx));
    }
}
