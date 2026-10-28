package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DragonNestBlockEntity extends BlockEntity {
    private int eggCount = 0;
    private int eggVariant1 = 0;
    private int eggVariant2 = 0;

    public DragonNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRAGON_NEST.get(), pos, state);
    }

    public boolean hasEgg() {
        return this.eggCount > 0;
    }

    public int getEggCount() {
        return this.eggCount;
    }

    public int getEggVariant1() {
        return this.eggVariant1;
    }

    public int getEggVariant2() {
        return this.eggVariant2;
    }

    public int getEggVariant() {
        return this.eggVariant1;
    }

    public void setEgg(boolean hasEgg, int variant) {
        if (hasEgg) {
            setEggs(1, variant, 0);
        } else {
            setEggs(0, 0, 0);
        }
    }

    public void setEggs(int count, int v1, int v2) {
        this.eggCount = Math.max(0, Math.min(2, count));
        this.eggVariant1 = v1;
        this.eggVariant2 = v2;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean addEgg(int variant) {
        if (this.eggCount >= 2) return false;
        if (this.eggCount == 0) {
            this.eggVariant1 = variant;
            this.eggCount = 1;
        } else {
            this.eggVariant2 = variant;
            this.eggCount = 2;
        }
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return true;
    }

    public int removeEgg() {
        if (this.eggCount <= 0) return -1;
        int removedVariant;
        if (this.eggCount == 2) {
            removedVariant = this.eggVariant2;
            this.eggVariant2 = 0;
            this.eggCount = 1;
        } else {
            removedVariant = this.eggVariant1;
            this.eggVariant1 = 0;
            this.eggCount = 0;
        }
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return removedVariant;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("EggCount", this.eggCount);
        tag.putInt("EggVariant1", this.eggVariant1);
        tag.putInt("EggVariant2", this.eggVariant2);
        tag.putBoolean("HasEgg", this.eggCount > 0);
        tag.putInt("EggVariant", this.eggVariant1);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("EggCount")) {
            this.eggCount = tag.getInt("EggCount");
            this.eggVariant1 = tag.getInt("EggVariant1");
            this.eggVariant2 = tag.getInt("EggVariant2");
        } else if (tag.contains("HasEgg")) {
            boolean has = tag.getBoolean("HasEgg");
            this.eggCount = has ? 1 : 0;
            this.eggVariant1 = tag.getInt("EggVariant");
            this.eggVariant2 = 0;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("EggCount", this.eggCount);
        tag.putInt("EggVariant1", this.eggVariant1);
        tag.putInt("EggVariant2", this.eggVariant2);
        tag.putBoolean("HasEgg", this.eggCount > 0);
        tag.putInt("EggVariant", this.eggVariant1);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
