package com.example.elementalia.element;

import com.example.elementalia.config.ElementaliaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The four elemental flavours of {@code ElementalTomeItem}.
 *
 * Each constant bundles the visual/audio/gameplay parameters for one element
 * and supplies its own {@link #applyImpact} server-side behaviour. The enum
 * lives in common code so the network payload and beam entity can refer to it
 * by ordinal — keep it free of {@code net.minecraft.client.*} imports.
 */
public enum Element {

    FIRE(0xFF8019,
            ParticleTypes.FLAME,
            ParticleTypes.LAVA,
            ParticleTypes.LARGE_SMOKE,
            SoundEvents.FIRECHARGE_USE,
            SoundEvents.GENERIC_EXPLODE.value(),
            SoundEvents.BLAZE_SHOOT,
            6.0f,
            0.6f) {
        @Override
        public void applyImpact(ServerLevel level, ServerPlayer caster, Vec3 impact, boolean hitBlock) {
            float kb = knockbackStrength();
            forEachVictim(level, caster, impact, entity -> {
                entity.hurt(level.damageSources().onFire(), damage());
                entity.igniteForSeconds(5.0f);
                Vec3 dir = entity.position().subtract(impact).normalize();
                entity.push(dir.x * kb, 0.2, dir.z * kb);
            });
            if (hitBlock && fireIgnitesBlocksConfig()) igniteBlocks(level, impact);
        }
    },

    ICE(0xC0E8FF,
            ParticleTypes.SNOWFLAKE,
            ParticleTypes.END_ROD,
            null,
            SoundEvents.GLASS_BREAK,
            SoundEvents.AMETHYST_BLOCK_BREAK,
            SoundEvents.POWDER_SNOW_HIT,
            4.0f,
            0.0f) {
        @Override
        public void applyImpact(ServerLevel level, ServerPlayer caster, Vec3 impact, boolean hitBlock) {
            forEachVictim(level, caster, impact, entity -> {
                entity.hurt(level.damageSources().freeze(), damage());
                entity.setTicksFrozen(140);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
            });
            if (hitBlock) freezeGround(level, impact);
        }
    },

    EARTH(0x8B5A2B,
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
            ParticleTypes.LARGE_SMOKE,
            null,
            SoundEvents.STONE_HIT,
            SoundEvents.ANVIL_LAND,
            SoundEvents.STONE_BREAK,
            5.0f,
            0.0f) {
        @Override
        public void applyImpact(ServerLevel level, ServerPlayer caster, Vec3 impact, boolean hitBlock) {
            forEachVictim(level, caster, impact, entity -> {
                entity.hurt(level.damageSources().magic(), damage());
                entity.push(0.0, 1.4, 0.0);                                   // pure vertical knock-up
                entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
            });
            // No world-block changes — see Phase 08 "raised pillar" gotcha.
            // The rising-column visual is purely client-side particles.
        }
    },

    WIND(0xD0E0F0,
            ParticleTypes.CLOUD,
            ParticleTypes.SWEEP_ATTACK,
            null,
            SoundEvents.ELYTRA_FLYING,
            SoundEvents.BREEZE_WHIRL,
            SoundEvents.WIND_CHARGE_THROW,
            2.0f,
            2.5f) {
        @Override
        public void applyImpact(ServerLevel level, ServerPlayer caster, Vec3 impact, boolean hitBlock) {
            float kb = knockbackStrength();
            forEachVictim(level, caster, impact, entity -> {
                entity.hurt(level.damageSources().magic(), damage());
                Vec3 dir = entity.position().subtract(impact);
                // Use horizontal-only direction so wind shoves outward rather than up/down.
                double len = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                if (len > 1e-4) {
                    entity.push(dir.x / len * kb, 0.3, dir.z / len * kb);
                }
            });
            // Caster gets slow-falling so the wind backwash doesn't hurt them.
            caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
            // No ground effect — absence is the theme.
        }
    };

    /** Radius (in blocks) of the spherical AABB scanned for victims and ground effects. */
    public static final double IMPACT_RADIUS = 3.0;

    /** Inner radius for FIRE's lava-disc effect (outside this ring, fire is placed on top instead). */
    private static final double FIRE_LAVA_RADIUS = 1.5;

    private final int beamColorArgb;
    private final ParticleOptions primaryParticle;
    private final ParticleOptions secondaryParticle;
    private final @Nullable ParticleOptions trailParticle;
    private final SoundEvent castSound;
    private final SoundEvent impactSound;
    private final SoundEvent accentSound;
    private final float damage;
    private final float knockbackStrength;

    Element(int beamColorArgb,
            ParticleOptions primaryParticle,
            ParticleOptions secondaryParticle,
            @Nullable ParticleOptions trailParticle,
            SoundEvent castSound,
            SoundEvent impactSound,
            SoundEvent accentSound,
            float damage,
            float knockbackStrength) {
        this.beamColorArgb     = beamColorArgb;
        this.primaryParticle   = primaryParticle;
        this.secondaryParticle = secondaryParticle;
        this.trailParticle     = trailParticle;
        this.castSound         = castSound;
        this.impactSound       = impactSound;
        this.accentSound       = accentSound;
        this.damage            = damage;
        this.knockbackStrength = knockbackStrength;
    }

    public int beamColorArgb()                       { return beamColorArgb; }
    public ParticleOptions primaryParticle()         { return primaryParticle; }
    public ParticleOptions secondaryParticle()       { return secondaryParticle; }
    @Nullable public ParticleOptions trailParticle() { return trailParticle; }
    public SoundEvent castSound()                    { return castSound; }
    public SoundEvent impactSound()                  { return impactSound; }
    public SoundEvent accentSound()                  { return accentSound; }
    public float knockbackStrength()                 { return knockbackStrength; }

    /**
     * Baked-in default damage. Safe to call from anywhere — does NOT consult
     * {@link ElementaliaConfig}, so it can be used during {@code ElementaliaConfig}'s
     * own static init to seed its {@code defineInRange} defaults without
     * triggering a class-init cycle.
     */
    public float defaultDamage() {
        return damage;
    }

    /**
     * Effective damage at cast time — reads {@link ElementaliaConfig} if the
     * config has been loaded, otherwise falls back to {@link #defaultDamage}.
     *
     * The catch is intentionally broad: during {@code ElementaliaConfig}'s
     * own static init the {@code DAMAGE} map is partially built, so a lookup
     * may NPE (not just IllegalStateException). We never want such a call to
     * propagate out of {@code damage()} — the baked-in default is always a
     * safe answer.
     */
    public float damage() {
        try {
            return (float) ElementaliaConfig.damageFor(this);
        } catch (RuntimeException configNotReady) {
            return damage;
        }
    }

    /**
     * Server-side gameplay impact applied at {@code impact}. The caster is excluded
     * from area-of-effect targeting. Implementations may read but must not mutate
     * the {@code Element} instance.
     *
     * @param hitBlock true when the raytrace terminated on a block — controls
     *                 whether ground effects (lava disc, ice patch, etc.) run.
     */
    public abstract void applyImpact(ServerLevel level, ServerPlayer caster, Vec3 impact, boolean hitBlock);

    /** Network-friendly byte encoding (one byte = one ordinal). */
    public byte toByte() {
        return (byte) ordinal();
    }

    /** Inverse of {@link #toByte} — returns FIRE for unknown values to stay forward-compatible. */
    public static Element fromByte(byte b) {
        Element[] values = values();
        int idx = b & 0xFF;
        return (idx >= 0 && idx < values.length) ? values[idx] : FIRE;
    }

    /**
     * Reads the {@code fireIgnitesBlocks} flag from {@link ElementaliaConfig};
     * falls back to {@code true} (the default) if the config isn't loaded yet.
     */
    private static boolean fireIgnitesBlocksConfig() {
        try {
            return ElementaliaConfig.fireIgnitesBlocks();
        } catch (RuntimeException configNotReady) {
            return true;
        }
    }

    // ---- shared impact helpers ----

    /**
     * Iterates every alive living entity inside the impact AABB (caster excluded)
     * and applies the given per-victim effect. Used by all four elements.
     */
    protected static void forEachVictim(ServerLevel level, ServerPlayer caster, Vec3 impact,
                                        java.util.function.Consumer<LivingEntity> apply) {
        AABB aabb = new AABB(
                impact.x - IMPACT_RADIUS, impact.y - 2.0, impact.z - IMPACT_RADIUS,
                impact.x + IMPACT_RADIUS, impact.y + 2.0, impact.z + IMPACT_RADIUS);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class, aabb, e -> e != caster && e.isAlive());

        for (LivingEntity entity : targets) {
            apply.accept(entity);
        }
    }

    /**
     * ICE ground effect — within {@link #IMPACT_RADIUS}: water becomes ice,
     * and any flat dirt/grass/stone gets a snow-layer placed on top.
     */
    private static void freezeGround(ServerLevel level, Vec3 impact) {
        double r2 = IMPACT_RADIUS * IMPACT_RADIUS;
        BlockPos center = BlockPos.containing(impact);

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double dist2 = dx * dx + dz * dz;
                if (dist2 > r2) continue;

                BlockPos col     = center.offset(dx, 0, dz);
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, col);
                BlockPos below   = surface.below();

                BlockState belowState = level.getBlockState(below);
                FluidState belowFluid = belowState.getFluidState();

                if (belowFluid.is(Fluids.WATER) || belowFluid.is(Fluids.FLOWING_WATER)) {
                    // Replace top water with ice.
                    level.setBlockAndUpdate(below, Blocks.ICE.defaultBlockState());
                } else {
                    BlockState snowLayer = Blocks.SNOW.defaultBlockState();
                    if (level.getBlockState(surface).isAir()
                            && snowLayer.canSurvive(level, surface)) {
                        level.setBlockAndUpdate(surface, snowLayer);
                    }
                }
            }
        }
    }

    /**
     * Replaces the topmost solid block in the inner disc with lava, and places
     * fire on top of any flammable surface in the outer ring.
     */
    private static void igniteBlocks(ServerLevel level, Vec3 impact) {
        double r2     = IMPACT_RADIUS * IMPACT_RADIUS;
        double lavaR2 = FIRE_LAVA_RADIUS * FIRE_LAVA_RADIUS;
        BlockPos center = BlockPos.containing(impact);

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double dist2 = dx * dx + dz * dz;
                if (dist2 > r2) continue;

                BlockPos col     = center.offset(dx, 0, dz);
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, col);
                BlockPos below   = surface.below();

                if (dist2 <= lavaR2) {
                    level.setBlockAndUpdate(below, Blocks.LAVA.defaultBlockState());
                } else {
                    if (level.getBlockState(surface).isAir()
                            && level.getBlockState(below).isFlammable(level, below, Direction.UP)) {
                        level.setBlockAndUpdate(surface, Blocks.FIRE.defaultBlockState());
                    }
                }
            }
        }
    }
}
