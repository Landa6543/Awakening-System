package com.soybe.awakeningsystem.subspace;

import com.soybe.awakeningsystem.furnace.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SubspaceMenu extends AbstractContainerMenu {
    public static final int CONTAINER_ROWS = 6;
    public static final int CONTAINER_COLS = 9;
    public static final int SUBSPACE_SLOTS = CONTAINER_ROWS * CONTAINER_COLS;

    private final Container container;
    private final ContainerData data;

    public SubspaceMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SUBSPACE_SLOTS), new SimpleContainerData(4));
    }

    public SubspaceMenu(int containerId, Inventory playerInventory, Container container) {
        this(containerId, playerInventory, container, new SimpleContainerData(4));
    }

    public SubspaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.SUBSPACE_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        for (int r = 0; r < CONTAINER_ROWS; ++r) {
            for (int c = 0; c < CONTAINER_COLS; ++c) {
                this.addSlot(new SubspaceSlot(this, container, c + r * CONTAINER_COLS, 8 + c * 18, 18 + r * 18));
            }
        }

        int playerInvY = 140;
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 9; ++c) {
                this.addSlot(new Slot(playerInventory, c + r * 9 + 9, 8 + c * 18, playerInvY + r * 18));
            }
        }

        for (int c = 0; c < 9; ++c) {
            this.addSlot(new Slot(playerInventory, c, 8 + c * 18, playerInvY + 58));
        }

        this.addDataSlots(data);
    }

    public int getUnlockedSlots() {
        int v = this.data.get(0);
        return v <= 0 ? 54 : v;
    }

    public int getChestCount() {
        int v = this.data.get(1);
        return v <= 0 ? 2 : v;
    }

    public int getCurrentPage() {
        return Math.max(0, this.data.get(2));
    }

    public int getTotalPages() {
        int v = this.data.get(3);
        return v <= 0 ? 1 : v;
    }

    public void setPage(int page) {
        int total = getTotalPages();
        int target = Math.max(0, Math.min(page, total - 1));
        this.data.set(2, target);
        this.sendAllDataToRemote();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < SUBSPACE_SLOTS) {
                if (!this.moveItemStackTo(itemstack1, SUBSPACE_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int maxUnlockedInPage = Math.min(SUBSPACE_SLOTS, Math.max(0, getUnlockedSlots() - getCurrentPage() * 54));
                if (maxUnlockedInPage <= 0 || !this.moveItemStackTo(itemstack1, 0, maxUnlockedInPage, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp && sp.server != null) {
            net.minecraft.server.level.ServerLevel sl = sp.server.getLevel(SubspaceManager.SUBSPACE_REALM);
            if (sl != null && sp.serverLevel().dimension().equals(SubspaceManager.SUBSPACE_REALM)) {
                SubspaceManager.syncSubspaceToWorld(sl, sp);
            }
            com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
        }
    }

    public Container getContainer() {
        return this.container;
    }

    public static class SubspaceSlot extends Slot {
        private final SubspaceMenu menu;
        private final int slotInPage;

        public SubspaceSlot(SubspaceMenu menu, Container container, int slotInPage, int x, int y) {
            super(container, slotInPage, x, y);
            this.menu = menu;
            this.slotInPage = slotInPage;
        }

        public int getGlobalIndex() {
            if (this.container instanceof SubspaceContainer) {
                return this.menu.getCurrentPage() * 54 + this.slotInPage;
            }
            return this.slotInPage;
        }

        public boolean isSlotUnlocked() {
            return (this.menu.getCurrentPage() * 54 + this.slotInPage) < this.menu.getUnlockedSlots();
        }

        @Override
        public ItemStack getItem() {
            int idx = getGlobalIndex();
            return (idx < this.container.getContainerSize()) ? this.container.getItem(idx) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            int idx = getGlobalIndex();
            if (idx < this.container.getContainerSize()) {
                this.container.setItem(idx, stack);
                this.setChanged();
            }
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            set(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            int idx = getGlobalIndex();
            return (idx < this.container.getContainerSize()) ? this.container.removeItem(idx, amount) : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isSlotUnlocked();
        }

        @Override
        public boolean mayPickup(Player player) {
            return hasItem();
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }
    }
}
