package com.example.elementalia.client.effect;

import net.minecraft.client.multiplayer.ClientLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks all active {@link BookCastEffect}s and ticks them once per client tick.
 * Completed effects are removed automatically.
 */
public class EffectManager {

    private static final List<BookCastEffect> active = new ArrayList<>();

    public static void add(BookCastEffect effect) {
        active.add(effect);
    }

    public static void tick(ClientLevel level) {
        Iterator<BookCastEffect> it = active.iterator();
        while (it.hasNext()) {
            BookCastEffect effect = it.next();
            effect.tick(level);
            if (effect.done) {
                it.remove();
            }
        }
    }
}
