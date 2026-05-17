package com.example.elementalia.entity;

import com.example.elementalia.element.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A short-lived, no-collision entity that carries the start and end position
 * of a Fire Book beam so the client renderer can draw it as a beacon-style shaft.
 *
 * Lifecycle: spawned server-side in {@link com.example.elementalia.item.FireBookItem},
 * replicated to clients via vanilla entity tracking, discarded after
 * {@link #DEFAULT_LIFETIME} ticks.
 */
public class BookBeamEntity extends Entity {

    private static final EntityDataAccessor<Float> END_X =
            SynchedEntityData.defineId(BookBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y =
            SynchedEntityData.defineId(BookBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z =
            SynchedEntityData.defineId(BookBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME =
            SynchedEntityData.defineId(BookBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> ELEMENT =
            SynchedEntityData.defineId(BookBeamEntity.class, EntityDataSerializers.BYTE);

    public static final int DEFAULT_LIFETIME = 12; // ticks (~0.6 s)

    public BookBeamEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(END_X, 0.0f);
        builder.define(END_Y, 0.0f);
        builder.define(END_Z, 0.0f);
        builder.define(LIFETIME, DEFAULT_LIFETIME);
        builder.define(ELEMENT, Element.FIRE.toByte());
    }

    // --- public API used by FireBookItem and BookBeamRenderer ---

    public void setEndPoint(Vec3 pos) {
        entityData.set(END_X, (float) pos.x);
        entityData.set(END_Y, (float) pos.y);
        entityData.set(END_Z, (float) pos.z);
        // Refresh the bounding box now that the end point is known.
        this.setPos(this.getX(), this.getY(), this.getZ());
    }

    public Vec3 getEndPoint() {
        return new Vec3(
                entityData.get(END_X),
                entityData.get(END_Y),
                entityData.get(END_Z));
    }

    public void setLifetime(int ticks) {
        entityData.set(LIFETIME, ticks);
    }

    public int getLifetime() {
        return entityData.get(LIFETIME);
    }

    public void setElement(Element element) {
        entityData.set(ELEMENT, element.toByte());
    }

    public Element getElement() {
        return Element.fromByte(entityData.get(ELEMENT));
    }

    // --- Entity overrides ---

    @Override
    public void tick() {
        super.tick();
        // Server drives the lifetime; clients observe the entity disappearing naturally.
        if (!level().isClientSide && tickCount >= getLifetime()) {
            discard();
        }
    }

    /**
     * Override to expand the AABB to encompass both endpoints so the beam is
     * not aggressively frustum-culled when the player is near the far end.
     * {@code getBoundingBox()} is final; this protected method is the override point.
     */
    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        Vec3 end = getEndPoint();
        // Guard against default (unset) end point — fall back to the normal tiny box.
        if (end.equals(Vec3.ZERO)) return super.makeBoundingBox(position);
        return new AABB(
                Math.min(position.x, end.x) - 1, Math.min(position.y, end.y) - 1, Math.min(position.z, end.z) - 1,
                Math.max(position.x, end.x) + 1, Math.max(position.y, end.y) + 1, Math.max(position.z, end.z) + 1);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false; // beam entities cannot be hurt
    }

    // --- NBT persistence (beam entities are transient, but vanilla requires these) ---

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(END_X, tag.getFloat("EndX"));
        entityData.set(END_Y, tag.getFloat("EndY"));
        entityData.set(END_Z, tag.getFloat("EndZ"));
        entityData.set(LIFETIME, tag.getInt("Lifetime"));
        // Older saves (Phase 06/07) lack an Element tag — default to FIRE.
        entityData.set(ELEMENT, tag.contains("Element") ? tag.getByte("Element") : Element.FIRE.toByte());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("EndX", entityData.get(END_X));
        tag.putFloat("EndY", entityData.get(END_Y));
        tag.putFloat("EndZ", entityData.get(END_Z));
        tag.putInt("Lifetime", entityData.get(LIFETIME));
        tag.putByte("Element", entityData.get(ELEMENT));
    }
}
