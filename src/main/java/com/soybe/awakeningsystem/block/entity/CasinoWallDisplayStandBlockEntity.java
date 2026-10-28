package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CasinoWallDisplayStandBlockEntity extends BlockEntity {

    private ItemStack displayedItem = ItemStack.EMPTY;

    public CasinoWallDisplayStandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CASINO_WALL_DISPLAY_STAND.get(), pos, blockState);
    }

    public ItemStack getDisplayedItem() {
        return this.displayedItem;
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedItem = stack != null ? stack.copy() : ItemStack.EMPTY;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ItemStack removeDisplayedItem() {
        ItemStack ret = this.displayedItem.copy();
        this.displayedItem = ItemStack.EMPTY;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return ret;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.displayedItem.isEmpty()) {
            tag.put("DisplayedItem", this.displayedItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("DisplayedItem", Tag.TAG_COMPOUND)) {
            this.displayedItem = ItemStack.parse(registries, tag.getCompound("DisplayedItem")).orElse(ItemStack.EMPTY);
        } else {
            this.displayedItem = ItemStack.EMPTY;
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
