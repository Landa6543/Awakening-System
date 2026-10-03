package com.soybe.awakeningsystem.subspace;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SubspaceContainer implements Container {
    private final NonNullList<ItemStack> items;
    private final Runnable onChanged;

    public SubspaceContainer(NonNullList<ItemStack> items, Runnable onChanged) {
        this.items = items;
        this.onChanged = onChanged;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return (slot >= 0 && slot < items.size()) ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(this.items, slot);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.size()) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        java.util.Collections.fill(this.items, ItemStack.EMPTY);
        setChanged();
    }
}
