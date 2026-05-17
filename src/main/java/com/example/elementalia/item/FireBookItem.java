package com.example.elementalia.item;

import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.network.BookCastPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;

/**
 * The Fire Book — an elemental tome that fires a beam on right-click.
 * Phase 02: right-click raytraces to the impact point and starts a cooldown.
 * Phase 03: sends a packet so clients can render the effect (Phase 04).
 * Phase 05: gameplay effects (fire, damage, knockback) applied at the impact point.
 */
public class FireBookItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final double MAX_BEAM_DISTANCE = 24.0;
    private static final int    COOLDOWN_TICKS    = 60;     // 3 seconds — tune later
    private static final float  FIRE_DAMAGE       = 6.0f;
    private static final float  KNOCKBACK_STRENGTH = 0.6f;
    public  static final double IMPACT_RADIUS     = 3.0;

    public FireBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Cooldown check on both sides to prevent the arm-swing animation
        // firing on the client while the server refuses the cast.
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        // Charge check — fizzle if empty.
        int charges = stack.getOrDefault(ModDataComponents.CHARGES.get(), 0);
        if (charges <= 0) {
            if (level.isClientSide) {
                // Play fizzle sound locally so the feedback is immediate.
                level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.5f, false);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            // Acknowledge on the client so the arm-swing animation plays.
            // All state mutations run on the server branch below.
            return InteractionResult.SUCCESS;
        }

        // Server only from here.
        // Decrement charges before casting — so a crash mid-cast still costs a charge.
        stack.set(ModDataComponents.CHARGES.get(), charges - 1);
        cast((ServerLevel) level, (ServerPlayer) player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.elementalia.fire_book.tooltip"));
        int charges = stack.getOrDefault(ModDataComponents.CHARGES.get(), 0);
        tooltip.add(Component.translatable("item.elementalia.fire_book.charges", charges)
                .withStyle(ChatFormatting.GRAY));
    }

    private void cast(ServerLevel level, ServerPlayer player, ItemStack stack) {
        Vec3 origin = player.getEyePosition();
        Vec3 end    = origin.add(player.getLookAngle().scale(MAX_BEAM_DISTANCE));

        ClipContext clipContext = new ClipContext(
                origin, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player);

        BlockHitResult hitResult = level.clip(clipContext);
        boolean hitBlock = hitResult.getType() != HitResult.Type.MISS;

        Vec3 impact = hitBlock ? hitResult.getLocation() : end;

        LOGGER.info("Fire Book cast: origin={} target={} distance={}",
                origin, impact, origin.distanceTo(impact));

        // Notify all watching clients so they can render the effect.
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new BookCastPayload(origin, impact, level.random.nextInt()));

        // --- gameplay effects (server-only) ---
        applyEntityEffects(level, player, impact);
        if (hitBlock) {
            igniteBlocks(level, impact);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
    }

    /** Damages and knocks back all living entities near the impact point. */
    private void applyEntityEffects(ServerLevel level, ServerPlayer player, Vec3 impact) {
        AABB aabb = new AABB(
                impact.x - IMPACT_RADIUS, impact.y - 2.0, impact.z - IMPACT_RADIUS,
                impact.x + IMPACT_RADIUS, impact.y + 2.0, impact.z + IMPACT_RADIUS);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class, aabb, e -> e != player && e.isAlive());

        for (LivingEntity entity : targets) {
            entity.hurt(level.damageSources().onFire(), FIRE_DAMAGE);
            entity.igniteForSeconds(5.0f);

            // Knock the entity away from the impact point.
            Vec3 dir = entity.position().subtract(impact).normalize();
            entity.push(dir.x * KNOCKBACK_STRENGTH, 0.2, dir.z * KNOCKBACK_STRENGTH);
        }
    }

    /**
     * Ignites air blocks that sit above a flammable surface within the impact ring.
     * Only called when the raytrace hit a block (not a mid-air miss).
     */
    private void igniteBlocks(ServerLevel level, Vec3 impact) {
        double r2 = IMPACT_RADIUS * IMPACT_RADIUS;
        BlockPos center = BlockPos.containing(impact);

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                // Circular boundary check.
                if (dx * dx + dz * dz > r2) continue;

                // Find the topmost motion-blocking position in this column.
                BlockPos col     = center.offset(dx, 0, dz);
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, col);
                BlockPos below   = surface.below();

                // surface is the first air block above the solid surface.
                if (level.getBlockState(surface).isAir()
                        && level.getBlockState(below).isFlammable(level, below, Direction.UP)) {
                    level.setBlockAndUpdate(surface, Blocks.FIRE.defaultBlockState());
                }
            }
        }
    }
}
