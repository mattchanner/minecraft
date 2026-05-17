package com.example.elementalia.item;

import com.example.elementalia.component.ModDataComponents;
import com.example.elementalia.config.ElementaliaConfig;
import com.example.elementalia.element.Element;
import com.example.elementalia.entity.BookBeamEntity;
import com.example.elementalia.network.BookCastPayload;
import com.example.elementalia.registry.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;

/**
 * Base class for all elemental tomes. Owns the right-click-to-cast flow:
 * cooldown + charge gate, raytrace, payload send, beam spawn, and impact
 * delegation. Subclasses supply the {@link Element} via {@link #element()}.
 *
 * Tooltip and charges-line translation keys are derived from the item's
 * registry id (via {@link Item#getDescriptionId()}), so each subclass only
 * needs to register lang entries — no per-class string literals here.
 */
public abstract class ElementalTomeItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Default beam distance — used as fallback if the config isn't loaded yet. */
    protected static final double DEFAULT_MAX_BEAM_DISTANCE = 24.0;
    /** Default cooldown — used as fallback if the config isn't loaded yet. */
    protected static final int    DEFAULT_COOLDOWN_TICKS    = 60;     // 3 seconds

    /**
     * Re-exported for legacy callers (notably {@code BookCastEffect}) that still
     * reference the radius via a tome class. The canonical value lives on
     * {@link Element#IMPACT_RADIUS}.
     */
    public static final double IMPACT_RADIUS = Element.IMPACT_RADIUS;

    protected ElementalTomeItem(Properties properties) {
        super(properties);
    }

    /** The element this tome casts. Returned per-subclass. */
    protected abstract Element element();

    /**
     * Public accessor used by callers that need to read the element from outside
     * the item class — notably the refill-ritual altar. Mirrors {@link #element()}.
     */
    public Element elementForRitual() {
        return element();
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
                level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.5f, false);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            // Acknowledge on the client so the arm-swing animation plays.
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
        String base = stack.getItem().getDescriptionId();   // e.g. "item.elementalia.fire_book"
        tooltip.add(Component.translatable(base + ".tooltip"));
        int charges = stack.getOrDefault(ModDataComponents.CHARGES.get(), 0);
        tooltip.add(Component.translatable(base + ".charges", charges)
                .withStyle(ChatFormatting.GRAY));
    }

    private void cast(ServerLevel level, ServerPlayer player, ItemStack stack) {
        Element element = element();
        double maxDistance = configBeamMaxDistance();

        Vec3 origin = player.getEyePosition();
        Vec3 end    = origin.add(player.getLookAngle().scale(maxDistance));

        ClipContext clipContext = new ClipContext(
                origin, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player);

        BlockHitResult hitResult = level.clip(clipContext);
        boolean hitBlock = hitResult.getType() != HitResult.Type.MISS;

        Vec3 impact = hitBlock ? hitResult.getLocation() : end;

        LOGGER.info("{} cast: origin={} target={} distance={}",
                element, origin, impact, origin.distanceTo(impact));

        // Notify all watching clients so they can render the impact effect.
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new BookCastPayload(origin, impact, level.random.nextInt(), element));

        // Spawn the beam entity — vanilla entity tracking replicates it to clients
        // so BookBeamRenderer draws the shaft.  All synced fields must be set
        // before addFreshEntity so the initial spawn packet carries them.
        BookBeamEntity beam = new BookBeamEntity(ModEntities.BOOK_BEAM.get(), level);
        beam.setPos(origin);
        beam.setEndPoint(impact);
        beam.setLifetime(BookBeamEntity.DEFAULT_LIFETIME);
        beam.setElement(element);
        level.addFreshEntity(beam);

        // --- gameplay effects (server-only) — delegated to the element. ---
        element.applyImpact(level, player, impact, hitBlock);

        player.getCooldowns().addCooldown(stack, configCooldownTicks(element));
    }

    /** Beam max distance, falling back to the baked-in default if config isn't loaded. */
    private static double configBeamMaxDistance() {
        try {
            return ElementaliaConfig.beamMaxDistance();
        } catch (RuntimeException configNotReady) {
            return DEFAULT_MAX_BEAM_DISTANCE;
        }
    }

    /** Per-element cooldown ticks, with fallback to the baked-in default. */
    private static int configCooldownTicks(Element element) {
        try {
            return ElementaliaConfig.cooldownTicksFor(element);
        } catch (RuntimeException configNotReady) {
            return DEFAULT_COOLDOWN_TICKS;
        }
    }
}
