package com.example.elementalia.block;

import com.example.elementalia.block.entity.TomeRitualBlockEntity;
import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.element.Element;
import com.example.elementalia.item.ElementalTomeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A 1×1 altar that refills a depleted {@link ElementalTomeItem}.
 *
 * Usage:
 *   1. Place 4 reagent blocks cardinally adjacent to the altar (north/south/east/west).
 *      Reagent depends on the tome's element ({@link #reagentFor}).
 *   2. Right-click the altar with a tome to deposit it.
 *   3. Right-click again with an empty hand to start the ritual.
 *      • If the 4 cardinal blocks match the reagent: the tome is refilled,
 *        the reagents are consumed (set to air), and per-element particles + sound
 *        play. The refilled tome is returned to the player.
 *      • Otherwise the tome is ejected without consuming reagents.
 *   4. Sneak right-click empty-handed to retrieve the tome at any time.
 */
public class TomeRitualBlock extends BaseEntityBlock {

    public static final MapCodec<TomeRitualBlock> CODEC = simpleCodec(TomeRitualBlock::new);

    private static final int RESTORED_CHARGES = 3;

    public TomeRitualBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TomeRitualBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TomeRitualBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Regular block model (full cube) — no special renderer.
        return RenderShape.MODEL;
    }

    /** Reagent block required by each element. */
    public static Block reagentFor(Element element) {
        return switch (element) {
            case FIRE  -> Blocks.MAGMA_BLOCK;
            case ICE   -> Blocks.ICE;
            case EARTH -> Blocks.DEEPSLATE;
            case WIND  -> Blocks.HAY_BLOCK;
        };
    }

    // ---- Interaction ----

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                          BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!(stack.getItem() instanceof ElementalTomeItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!(level.getBlockEntity(pos) instanceof TomeRitualBlockEntity altar)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!altar.isEmpty()) {
            // Altar already holds a tome — refuse the new one to avoid losing the held stack.
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Move one tome from the player's hand onto the altar.
        ItemStack deposited = stack.copyWithCount(1);
        altar.setHeldTome(deposited);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 0.8f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TomeRitualBlockEntity altar)) {
            return InteractionResult.PASS;
        }
        if (altar.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Sneak: just retrieve the tome unchanged.
        if (player.isShiftKeyDown()) {
            ejectTome(level, pos, altar, player);
            return InteractionResult.SUCCESS;
        }

        // Otherwise: attempt the ritual.
        ItemStack tome = altar.getHeldTome();
        if (!(tome.getItem() instanceof ElementalTomeItem tomeItem)) {
            ejectTome(level, pos, altar, player);
            return InteractionResult.SUCCESS;
        }

        Element element = tomeItem.elementForRitual();
        Block reagent = reagentFor(element);

        if (cardinalsMatch((ServerLevel) level, pos, reagent)) {
            // Consume reagents
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                level.setBlockAndUpdate(pos.relative(dir), Blocks.AIR.defaultBlockState());
            }
            // Restore charges
            ItemStack refilled = tome.copy();
            refilled.set(ModDataComponents.CHARGES.get(), RESTORED_CHARGES);

            // Element-specific FX
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 center = Vec3.atCenterOf(pos).add(0, 1.0, 0);
            serverLevel.sendParticles(element.primaryParticle(),
                    center.x, center.y, center.z, 30, 0.4, 0.4, 0.4, 0.05);
            level.playSound(null, pos, element.accentSound(), SoundSource.BLOCKS, 1.0f, 1.0f);

            altar.setHeldTome(ItemStack.EMPTY);
            givePlayerOrDrop(level, pos, player, refilled);
        } else {
            // Wrong reagents — eject the tome unchanged so the player can try again.
            ejectTome(level, pos, altar, player);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean cardinalsMatch(ServerLevel level, BlockPos pos, Block expected) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(pos.relative(dir)).is(expected)) {
                return false;
            }
        }
        return true;
    }

    private static void ejectTome(Level level, BlockPos pos, TomeRitualBlockEntity altar, Player player) {
        ItemStack tome = altar.getHeldTome();
        altar.setHeldTome(ItemStack.EMPTY);
        givePlayerOrDrop(level, pos, player, tome);
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.8f, 0.9f);
    }

    private static void givePlayerOrDrop(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            Block.popResource(level, pos.above(), stack);
        }
    }

    // ---- Drop the held tome when the block is broken ----

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof TomeRitualBlockEntity altar
                && !altar.isEmpty()) {
            Block.popResource(level, pos, altar.getHeldTome());
            altar.setHeldTome(ItemStack.EMPTY);
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
