package com.soybe.awakeningsystem.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GiantSandwormBodyPart extends Entity {
    private static final EntityDataAccessor<Integer> RENDER_SCALE =
            SynchedEntityData.defineId(GiantSandwormBodyPart.class, EntityDataSerializers.INT);

    @Nullable
    public GiantSandwormEntity owner;

    public GiantSandwormBodyPart(EntityType<? extends GiantSandwormBodyPart> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void initOwner(GiantSandwormEntity owner) {
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RENDER_SCALE, 12);
    }

    public int getRenderScale() {
        return this.entityData.get(RENDER_SCALE);
    }

    public void setRenderScale(int scale) {
        this.entityData.set(RENDER_SCALE, Math.max(1, scale));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("render_scale")) {
            setRenderScale(tag.getInt("render_scale"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("render_scale", getRenderScale());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 65536.0D;
    }

    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (this.owner != null && this.owner.isAlive()) {
            return this.owner.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || (this.owner != null && this.owner == entity);
    }

    @Override
    public void playerTouch(Player player) {
        if (!level().isClientSide && this.owner != null && this.owner.isAlive() && !player.isInvulnerable()) {
            if (player.invulnerableTime <= 0) {
                float headDmg = (float) this.owner.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                float bodyDmg = Math.max(2.5F, headDmg * 0.4F);
                player.hurt(damageSources().mobAttack(this.owner), bodyDmg);
                player.invulnerableTime = 20; // 1 full second immunity between body segment touches
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (this.owner == null || !this.owner.isAlive() || this.owner.isRemoved()) {
                this.discard();
            }
        }
    }
}
