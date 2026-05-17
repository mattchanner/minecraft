package com.example.elementalia.block.entity;

import com.example.elementalia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the tome placed on a {@link com.example.elementalia.block.TomeRitualBlock}.
 *
 * The stack is synced to clients so renderers (optional) can show the tome
 * floating above the altar — v1 doesn't draw it but the sync infra is in
 * place for v2 polish.
 */
public class TomeRitualBlockEntity extends BlockEntity {

    private ItemStack heldTome = ItemStack.EMPTY;

    public TomeRitualBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOME_RITUAL.get(), pos, state);
    }

    public ItemStack getHeldTome() {
        return heldTome;
    }

    public boolean isEmpty() {
        return heldTome.isEmpty();
    }

    /** Replaces the held stack. Pass {@link ItemStack#EMPTY} to clear. */
    public void setHeldTome(ItemStack stack) {
        this.heldTome = stack;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // ---- NBT persistence ----

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("HeldTome")) {
            this.heldTome = ItemStack.parseOptional(registries, tag.getCompound("HeldTome"));
        } else {
            this.heldTome = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!heldTome.isEmpty()) {
            tag.put("HeldTome", heldTome.save(registries));
        }
    }

    // ---- Network sync (client receives updates whenever the held tome changes) ----

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!heldTome.isEmpty()) {
            tag.put("HeldTome", heldTome.save(registries));
        }
        return tag;
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        CompoundTag tag = packet.getTag();
        this.heldTome = tag.contains("HeldTome")
                ? ItemStack.parseOptional(registries, tag.getCompound("HeldTome"))
                : ItemStack.EMPTY;
    }
}
