package com.example.elementalia.client.render;

import com.example.elementalia.entity.BookBeamEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Render state snapshot for {@link com.example.elementalia.entity.BookBeamEntity}.
 *
 * Extracted once per frame by {@link BookBeamRenderer#extractRenderState} so the
 * renderer never touches live entity state during the draw call.
 */
public class BookBeamRenderState extends EntityRenderState {

    /**
     * Beam end point relative to the entity spawn position (i.e. {@code endWorld - entityPos}).
     * The PoseStack in {@link BookBeamRenderer#render} is already translated to entity world
     * position, so applying this offset in model space gives us the world impact point.
     */
    public Vec3 beamOffset = Vec3.ZERO;

    /** Total lifetime in ticks — used to compute the fade alpha. */
    public int lifetime = BookBeamEntity.DEFAULT_LIFETIME;

    /** Level game time at extraction — drives the beam's rotation/scroll animation. */
    public long gameTime = 0L;

    /** Raw partial-tick value — used for sub-tick smooth animation. */
    public float partialTick = 0f;
}
