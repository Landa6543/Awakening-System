package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieBlockEntity extends BlockEntity {
    private int squashTicks = 0;
    private int prevSquashTicks = 0;

    public PlushieBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PLUSHIE.get(), pos, blockState);
    }

    public void triggerSquash() {
        this.squashTicks = 10;
        this.prevSquashTicks = 10;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PlushieBlockEntity entity) {
        entity.prevSquashTicks = entity.squashTicks;
        if (entity.squashTicks > 0) {
            entity.squashTicks--;
        }
    }

    public float getSquashTicks(float partialTick) {
        return Mth.lerp(partialTick, (float) this.prevSquashTicks, (float) this.squashTicks);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("squashTicks", this.squashTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.squashTicks = tag.getInt("squashTicks");
        this.prevSquashTicks = this.squashTicks;
    }
}
