package com.example.elementalia.client.render;

import com.example.elementalia.entity.BookBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Renders a {@link BookBeamEntity} as a glowing orange beacon-style shaft.
 *
 * Coordinate math:
 *   • The PoseStack is pre-translated to the entity's world position when
 *     {@link #render} is called.
 *   • We compute a quaternion that rotates the model's Y-axis onto the beam
 *     direction vector, then call {@link BeaconRenderer#renderBeaconBeam} with
 *     a pre-counteracted translate so the shaft starts exactly at the entity.
 *
 * The beam fades (dims) over its lifetime by scaling the RGB components toward
 * black — a "dying ember" effect that suits a fire book.
 */
public class BookBeamRenderer extends EntityRenderer<BookBeamEntity, BookBeamRenderState> {

    /** Reuse the vanilla beacon texture for v1 — replace with a custom fire strip later. */
    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    /** Base orange/fire RGB components (used to build the ARGB int each frame). */
    private static final float BASE_R = 1.0f;
    private static final float BASE_G = 0.5f;
    private static final float BASE_B = 0.1f;

    public BookBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f; // beams don't cast a ground shadow
    }

    // --- render-state API ---

    @Override
    public BookBeamRenderState createRenderState() {
        return new BookBeamRenderState();
    }

    @Override
    public void extractRenderState(BookBeamEntity entity, BookBeamRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Store offset (end − start, world-space) so the renderer doesn't need abs coords.
        state.beamOffset  = entity.getEndPoint().subtract(entity.position());
        state.lifetime    = entity.getLifetime();
        state.gameTime    = entity.level().getGameTime();
        state.partialTick = partialTick;
    }

    // --- rendering ---

    @Override
    public void render(BookBeamRenderState state, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Vec3 diff = state.beamOffset;
        float length = (float) diff.length();
        if (length < 0.01f) return;

        // Fade from full brightness (orange) to black over the beam's lifetime.
        float fade = state.lifetime > 0
                ? Math.max(0f, 1.0f - (state.ageInTicks / (float) state.lifetime))
                : 1.0f;
        if (fade < 0.01f) return;

        // Pack as ARGB int; scale RGB by fade to darken as the beam dies.
        int color = ARGB.colorFromFloat(1.0f, BASE_R * fade, BASE_G * fade, BASE_B * fade);

        // Build a rotation quaternion: model-Y (0,1,0) → beam direction.
        Vector3f dir = new Vector3f((float) diff.x, (float) diff.y, (float) diff.z).normalize();
        Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0f, 1f, 0f), dir);

        poseStack.pushPose();

        // 1. Orient model-Y along the beam direction.
        poseStack.mulPose(rotation);

        // 2. Pre-counteract BeaconRenderer's internal translate(0.5, 0, 0.5) so
        //    the shaft starts exactly at the entity origin rather than half-block off.
        poseStack.translate(-0.5, 0.0, -0.5);

        // 3. Draw the beam "upward" (rotated model-Y = beam direction) for `height` blocks.
        int height = Math.max(1, Math.round(length));
        BeaconRenderer.renderBeaconBeam(
                poseStack, bufferSource, BEAM_TEXTURE,
                state.partialTick,   // sub-tick smooth animation
                1.0f,                // textureScale — 1 tile per block
                state.gameTime,
                0,                   // yOffset
                height,
                color,
                0.2f,                // inner beam radius
                0.25f                // outer glow radius
        );

        poseStack.popPose();
    }
}
