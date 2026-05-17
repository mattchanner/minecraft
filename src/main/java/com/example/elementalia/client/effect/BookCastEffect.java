package com.example.elementalia.client.effect;

import com.example.elementalia.element.Element;
import com.example.elementalia.network.BookCastPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side timed effect for a single tome cast. Created when the client
 * receives a {@link BookCastPayload}; ticked by {@link EffectManager} once
 * per client tick until {@link #done} is set.
 *
 * Visual storyboard (ticks) — shared across all elements:
 *  0     — cast sound (element-specific)
 *  8–14  — ground-crack debris particles
 *  10–29 — element-specific eruption shape:
 *           • FIRE — ring of 8 vertical jets
 *           • ICE  — ring of 8 vertical jets (snowflake/end-rod)
 *           • EARTH— single rising column at impact
 *           • WIND — outward radial sweep (no jets)
 *  30    — effect ends
 *
 * The beam shaft is rendered by {@link com.example.elementalia.client.render.BookBeamRenderer}
 * via {@link com.example.elementalia.entity.BookBeamEntity}; this class only
 * handles particles and sounds at the impact point.
 */
public class BookCastEffect {

    private final Vec3 origin;
    private final Vec3 impact;
    private final Element element;
    private final RandomSource random;
    private int age;
    boolean done;

    public BookCastEffect(BookCastPayload payload) {
        this.origin  = payload.origin();
        this.impact  = payload.impact();
        this.element = payload.element();
        // Seed from the server so every watching client sees the same scatter pattern.
        this.random  = RandomSource.create(payload.seed());
        this.age     = 0;
        this.done    = false;
    }

    public void tick(ClientLevel level) {
        if (age == 0) {
            level.playLocalSound(origin.x, origin.y, origin.z,
                    element.castSound(), SoundSource.PLAYERS, 1.0f, 0.8f, false);
        }

        if (age >= 8 && age <= 14)  tickGroundCrack(level);
        if (age >= 10 && age < 30)  tickEruption(level);

        age++;
        if (age >= 30) done = true;
    }

    // --- ground crack -------------------------------------------------------

    /**
     * Element-agnostic: sample the block under the impact and spew block
     * fragments outward. Replaces the explosion sound with the element's
     * impact sound so the audio bed matches the visuals.
     */
    private void tickGroundCrack(ClientLevel level) {
        if (age == 8) {
            level.addParticle(ParticleTypes.EXPLOSION, impact.x, impact.y, impact.z, 0, 0, 0);
            level.playLocalSound(impact.x, impact.y, impact.z,
                    element.impactSound(), SoundSource.PLAYERS, 1.0f, 0.7f, false);
        }

        BlockState impactBlock = level.getBlockState(BlockPos.containing(impact).below());
        for (int i = 0; i < 8; i++) {
            double angle  = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * 3.0;
            double px = impact.x + Math.cos(angle) * radius;
            double pz = impact.z + Math.sin(angle) * radius;
            double py = impact.y + 0.1;
            double vx = (random.nextDouble() - 0.5) * 0.3;
            double vy = 0.1 + random.nextDouble() * 0.1;
            double vz = (random.nextDouble() - 0.5) * 0.3;
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, impactBlock),
                    px, py, pz, vx, vy, vz);
        }
    }

    // --- eruption (per-element) ---------------------------------------------

    private void tickEruption(ClientLevel level) {
        if (age == 10) {
            level.playLocalSound(impact.x, impact.y, impact.z,
                    element.accentSound(), SoundSource.PLAYERS, 1.0f, 0.8f, false);
        }

        double intensity = 1.0 - (age - 10) / 20.0;
        double r         = Element.IMPACT_RADIUS;

        switch (element) {
            case FIRE, ICE -> emitJetRing(level, r, intensity);
            case EARTH     -> emitRisingColumn(level, intensity);
            case WIND      -> emitRadialSweep(level, r, intensity);
        }
    }

    /** FIRE / ICE: a ring of 8 vertical jets around the impact perimeter. */
    private void emitJetRing(ClientLevel level, double r, double intensity) {
        ParticleOptions primary   = element.primaryParticle();
        ParticleOptions secondary = element.secondaryParticle();
        ParticleOptions trail     = element.trailParticle();

        for (int i = 0; i < 8; i++) {
            double angle = i * (2 * Math.PI / 8);
            double jx = impact.x + Math.cos(angle) * r;
            double jz = impact.z + Math.sin(angle) * r;
            double jy = impact.y;

            level.addParticle(primary, jx, jy,       jz, 0, 0.3 * intensity, 0);
            level.addParticle(primary, jx, jy + 0.5, jz, 0, 0.2 * intensity, 0);

            if (age % 4 == 0) {
                level.addParticle(secondary, jx, jy, jz, 0, 0, 0);
            }

            if (trail != null) {
                level.addParticle(trail, jx, jy + 1.5, jz, 0, 0.05 * intensity, 0);
            }
        }
    }

    /** EARTH: a single rising column of debris at the impact point. */
    private void emitRisingColumn(ClientLevel level, double intensity) {
        ParticleOptions primary   = element.primaryParticle();
        ParticleOptions secondary = element.secondaryParticle();

        for (int i = 0; i < 6; i++) {
            double jitter = 0.4;
            double px = impact.x + (random.nextDouble() - 0.5) * jitter;
            double pz = impact.z + (random.nextDouble() - 0.5) * jitter;
            double py = impact.y + random.nextDouble() * 2.0;
            double vy = 0.15 + random.nextDouble() * 0.15;

            level.addParticle(primary, px, py, pz, 0, vy * intensity, 0);
        }

        if (age % 3 == 0) {
            level.addParticle(secondary, impact.x, impact.y + 1.0, impact.z, 0, 0.1 * intensity, 0);
        }
    }

    /** WIND: an outward radial sweep of cloud particles, low vertical velocity. */
    private void emitRadialSweep(ClientLevel level, double r, double intensity) {
        ParticleOptions primary   = element.primaryParticle();
        ParticleOptions secondary = element.secondaryParticle();

        int spokes = 16;
        double ringRadius = r * Math.min(1.0, (age - 10) / 10.0); // grows then holds
        for (int i = 0; i < spokes; i++) {
            double angle = i * (2 * Math.PI / spokes);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double px = impact.x + cos * ringRadius;
            double pz = impact.z + sin * ringRadius;
            double py = impact.y + 0.2;

            // Drift outward, slight rise — wind feel.
            double vx = cos * 0.15 * intensity;
            double vz = sin * 0.15 * intensity;

            level.addParticle(primary, px, py, pz, vx, 0.05 * intensity, vz);
        }

        if (age == 10) {
            level.addParticle(secondary, impact.x, impact.y + 0.5, impact.z, 0, 0, 0);
        }
    }
}
