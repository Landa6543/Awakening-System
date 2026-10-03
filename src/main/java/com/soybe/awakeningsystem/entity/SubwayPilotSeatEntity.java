package com.soybe.awakeningsystem.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Entidad auxiliar invisible para sentarse en la cabina o asientos de metro.
 */
public class SubwayPilotSeatEntity extends Entity {
    private BlockPos controllerPos;

    public SubwayPilotSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public void setControllerPos(BlockPos pos) {
        this.controllerPos = pos;
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.getPassengers().isEmpty() || this.tickCount > 72000) {
                this.discard();
            }
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, net.minecraft.world.entity.EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, 0.1D, 0.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("CtrlX")) {
            this.controllerPos = new BlockPos(tag.getInt("CtrlX"), tag.getInt("CtrlY"), tag.getInt("CtrlZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.controllerPos != null) {
            tag.putInt("CtrlX", this.controllerPos.getX());
            tag.putInt("CtrlY", this.controllerPos.getY());
            tag.putInt("CtrlZ", this.controllerPos.getZ());
        }
    }
}
