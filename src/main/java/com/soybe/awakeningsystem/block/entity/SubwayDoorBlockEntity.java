package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Entidad de bloque para las puertas corredizas del metro (Línea 3 de Seúl).
 * Controla la animación suave de deslizamiento neumático tanto para apertura
 * como para cierre en tiempo real con interpolación de partial ticks.
 */
public class SubwayDoorBlockEntity extends BlockEntity {

    private float slideProgress;
    private float prevSlideProgress;
    private boolean initialized = false;

    public SubwayDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUBWAY_DOOR.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.initialized) {
            boolean isOpen = getBlockState().hasProperty(DoorBlock.OPEN) && getBlockState().getValue(DoorBlock.OPEN);
            this.slideProgress = isOpen ? 1.0F : 0.0F;
            this.prevSlideProgress = this.slideProgress;
            this.initialized = true;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SubwayDoorBlockEntity be) {
        be.prevSlideProgress = be.slideProgress;
        boolean targetOpen = state.hasProperty(DoorBlock.OPEN) && state.getValue(DoorBlock.OPEN);

        float speed = 0.12F;
        if (targetOpen && be.slideProgress < 1.0F) {
            be.slideProgress = Math.min(1.0F, be.slideProgress + speed);
        } else if (!targetOpen && be.slideProgress > 0.0F) {
            be.slideProgress = Math.max(0.0F, be.slideProgress - speed);
        }
    }

    public float getSlideProgress(float partialTick) {
        return Mth.lerp(partialTick, this.prevSlideProgress, this.slideProgress);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("SlideProgress", this.slideProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("SlideProgress")) {
            this.slideProgress = tag.getFloat("SlideProgress");
            this.prevSlideProgress = this.slideProgress;
            this.initialized = true;
        } else {
            boolean isOpen = getBlockState().hasProperty(DoorBlock.OPEN) && getBlockState().getValue(DoorBlock.OPEN);
            this.slideProgress = isOpen ? 1.0F : 0.0F;
            this.prevSlideProgress = this.slideProgress;
            this.initialized = true;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putFloat("SlideProgress", this.slideProgress);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
