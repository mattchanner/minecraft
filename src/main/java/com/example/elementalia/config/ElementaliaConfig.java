package com.example.elementalia.config;

import com.example.elementalia.element.Element;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.Map;

/**
 * Reload-friendly configuration for tunable gameplay values.
 *
 * Values are read at cast time, not init time — change a value in
 * {@code config/elementalia-common.toml} and the next cast picks it up.
 *
 * Per-element overrides default to the values baked into {@link Element};
 * the enum stays the source of truth for shape (which particles, sounds, etc.),
 * while damage / cooldown / range / fire-ignites become live-tunable.
 */
public final class ElementaliaConfig {

    public static final ModConfigSpec SPEC;

    private static final Map<Element, ModConfigSpec.DoubleValue> DAMAGE   = new EnumMap<>(Element.class);
    private static final Map<Element, ModConfigSpec.IntValue>    COOLDOWN = new EnumMap<>(Element.class);

    private static final ModConfigSpec.DoubleValue  BEAM_MAX_DISTANCE;
    private static final ModConfigSpec.BooleanValue FIRE_IGNITES_BLOCKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Tunable gameplay values for the Elementalia tomes.").push("gameplay");

        BEAM_MAX_DISTANCE = builder
                .comment("Maximum raytrace distance (blocks) before the beam fizzles short of any block.")
                .defineInRange("beamMaxDistance", 24.0, 4.0, 128.0);

        FIRE_IGNITES_BLOCKS = builder
                .comment("If false, Fire Book casts do not place lava or fire blocks at the impact.",
                         "Entity damage and ignition still apply — only the world-block changes are suppressed.")
                .define("fireIgnitesBlocks", true);

        builder.pop();

        builder.comment("Per-element overrides for damage and cooldown.").push("perElement");
        for (Element element : Element.values()) {
            String key = element.name().toLowerCase();
            builder.push(key);
            DAMAGE.put(element, builder
                    .comment("Damage dealt to entities in the impact radius.")
                    .defineInRange("damage", element.defaultDamage(), 0.0, 100.0));
            COOLDOWN.put(element, builder
                    .comment("Cooldown ticks before another cast is permitted.")
                    .defineInRange("cooldownTicks", 60, 1, 6000));
            builder.pop();
        }
        builder.pop();

        SPEC = builder.build();
    }

    private ElementaliaConfig() {}

    public static double damageFor(Element element) {
        return DAMAGE.get(element).get();
    }

    public static int cooldownTicksFor(Element element) {
        return COOLDOWN.get(element).get();
    }

    public static double beamMaxDistance() {
        return BEAM_MAX_DISTANCE.get();
    }

    public static boolean fireIgnitesBlocks() {
        return FIRE_IGNITES_BLOCKS.get();
    }
}
