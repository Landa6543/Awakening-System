package com.soybe.awakeningsystem.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TavernSeatEntity extends Entity {

    public TavernSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.getPassengers().isEmpty() || this.tickCount > 72000) {
                this.discard();
            }
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, net.minecraft.world.entity.EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, 0.05D, 0.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}