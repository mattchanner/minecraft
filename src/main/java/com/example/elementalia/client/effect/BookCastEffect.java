package com.example.elementalia.client.effect;

import com.example.elementalia.item.FireBookItem;
import com.example.elementalia.network.BookCastPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// Note: the beam shaft is now rendered by BookBeamRenderer via BookBeamEntity.
// This class handles only the ground-crack and fire-eruption particles.

/**
 * Client-side timed effect for a single Fire Book cast.
 * Created when the client receives a {@link BookCastPayload}; ticked by
 * {@link EffectManager} once per client tick until {@code done} is set.
 *
 * Visual storyboard (ticks):
 *  0     — ignition sound
 *  8–14  — ground crack particles; explosion sound on tick 8
 *  10–29 — ring of 8 fire jets; blaze sound on tick 10
 *  30    — effect ends
 *
 * The beam shaft is handled separately by {@link com.example.elementalia.client.render.BookBeamRenderer}
 * via the {@link com.example.elementalia.entity.BookBeamEntity} that the server spawns alongside
 * the payload.
 */
public class BookCastEffect {

    private final Vec3 origin;
    private final Vec3 impact;
    private final RandomSource random;
    private int age;
    boolean done;

    public BookCastEffect(BookCastPayload payload) {
        this.origin = payload.origin();
        this.impact = payload.impact();
        // Seed from the server so every watching client sees the same scatter pattern.
        this.random = RandomSource.create(payload.seed());
        this.age    = 0;
        this.done   = false;
    }

    public void tick(ClientLevel level) {
        if (age == 0) {
            level.playLocalSound(origin.x, origin.y, origin.z,
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f, false);
        }

        if (age >= 8 && age <= 14)  tickGroundCrack(level);
        if (age >= 10 && age < 30)  tickEruption(level);

        age++;
        if (age >= 30) done = true;
    }

    // --- ground crack -------------------------------------------------------

    private void tickGroundCrack(ClientLevel level) {
        if (age == 8) {
            level.addParticle(ParticleTypes.EXPLOSION, impact.x, impact.y, impact.z, 0, 0, 0);
            level.playLocalSound(impact.x, impact.y, impact.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.7f, false);
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

    // --- fire eruption -------------------------------------------------------

    private void tickEruption(ClientLevel level) {
        if (age == 10) {
            level.playLocalSound(impact.x, impact.y, impact.z,
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f, false);
        }

        double intensity = 1.0 - (age - 10) / 20.0;
        double r         = FireBookItem.IMPACT_RADIUS;

        for (int i = 0; i < 8; i++) {
            double angle = i * (2 * Math.PI / 8);
            double jx = impact.x + Math.cos(angle) * r;
            double jz = impact.z + Math.sin(angle) * r;
            double jy = impact.y;

            level.addParticle(ParticleTypes.FLAME, jx, jy,        jz, 0, 0.3 * intensity, 0);
            level.addParticle(ParticleTypes.FLAME, jx, jy + 0.5,  jz, 0, 0.2 * intensity, 0);

            if (age % 4 == 0) {
                level.addParticle(ParticleTypes.LAVA, jx, jy, jz, 0, 0, 0);
            }

            level.addParticle(ParticleTypes.LARGE_SMOKE, jx, jy + 1.5, jz, 0, 0.05 * intensity, 0);
        }
    }
}
