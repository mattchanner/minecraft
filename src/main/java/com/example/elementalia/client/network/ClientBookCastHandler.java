package com.example.elementalia.client.network;

import com.example.elementalia.client.effect.BookCastEffect;
import com.example.elementalia.client.effect.EffectManager;
import com.example.elementalia.network.BookCastPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side handler for {@link BookCastPayload}.
 *
 * This class lives under the {@code client} package and must never be imported
 * from common or server-reachable code directly (only via a lambda in ModNetwork).
 */
public class ClientBookCastHandler {

    public static void handle(BookCastPayload payload, IPayloadContext ctx) {
        // enqueueWork ensures the effect is created on the main client thread,
        // not the network thread.
        ctx.enqueueWork(() -> EffectManager.add(new BookCastEffect(payload)));
    }
}
