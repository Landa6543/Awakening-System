package com.soybe.awakeningsystem.block.entity;

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

public class FishingTrapMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    // Constructor del lado del cliente
    public FishingTrapMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(FishingTrapBlockEntity.CONTAINER_SIZE), new SimpleContainerData(5));
    }

    // Constructor del lado del servidor
    public FishingTrapMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.FISHING_TRAP_MENU.get(), containerId);
        checkContainerSize(container, FishingTrapBlockEntity.CONTAINER_SIZE);
        checkContainerDataCount(data, 5);
        this.container = container;
        this.data = data;
        this.addDataSlots(data);
        container.startOpen(playerInventory.player);

        // --- RANURAS DE SALIDA (Peces Capturados, slots 0..3) ---
        // Coordenadas idénticas a la interfaz original de Genshin Nature
        this.addSlot(new OutputOnlySlot(container, 0, 118, 57));
        this.addSlot(new OutputOnlySlot(container, 1, 98, 57));
        this.addSlot(new OutputOnlySlot(container, 2, 58, 57));
        this.addSlot(new OutputOnlySlot(container, 3, 78, 57));

        // --- RANURAS DE ENTRADA (Cebos / Bait, slots 4..8) ---
        this.addSlot(new BaitSlot(container, 4, 53, 17));
        this.addSlot(new BaitSlot(container, 5, 73, 17));
        this.addSlot(new BaitSlot(container, 6, 93, 17));
        this.addSlot(new BaitSlot(container, 7, 113, 17));
        this.addSlot(new BaitSlot(container, 8, 133, 17));

        // --- INVENTARIO DEL JUGADOR (3 filas x 9 columnas) ---
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 20 + col * 18, 116 + row * 18));
            }
        }

        // --- BARRA DE ACCESO RÁPIDO (Hotbar, 9 columnas) ---
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 20 + col * 18, 174));
        }
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getTotalTime() {
        int t = this.data.get(1);
        return t > 0 ? t : FishingTrapBlockEntity.FISH_INTERVAL_TICKS;
    }

    public int getStatus() {
        return this.data.get(2);
    }

    public boolean isActive() {
        return this.data.get(3) != 0;
    }

    public boolean isRedstonePowered() {
        return this.data.get(4) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof FishingTrapBlockEntity trap) {
            if (id == 0) {
                trap.toggleActive(player);
                return true;
            } else if (id == 1) {
                return trap.triggerActivate(player);
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            // De la trampa de pesca al inventario del jugador
            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, itemstack);
            } else {
                // Del inventario a la trampa: si es cebo a las ranuras 4..8
                if (FishingTrapBlockEntity.isBait(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 4, 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 36) {
                    if (!this.moveItemStackTo(stackInSlot, 36, 45, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stackInSlot, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }

    public static class OutputOnlySlot extends Slot {
        public OutputOnlySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    public static class BaitSlot extends Slot {
        public BaitSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FishingTrapBlockEntity.isBait(stack);
        }
    }
}
