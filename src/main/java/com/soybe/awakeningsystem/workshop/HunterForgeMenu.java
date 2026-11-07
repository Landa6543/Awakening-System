package com.soybe.awakeningsystem.workshop;

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

public class HunterForgeMenu extends AbstractContainerMenu {
    public static final int SLOT_BASE = 0;
    public static final int SLOT_CORE = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_GEM = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int FORGE_SLOTS_COUNT = 5;

    private final Container container;
    private final ContainerData data;

    public HunterForgeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(FORGE_SLOTS_COUNT), new SimpleContainerData(4));
    }

    public HunterForgeMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.HUNTER_FORGE_MENU.get(), containerId);
        checkContainerSize(container, FORGE_SLOTS_COUNT);
        checkContainerDataCount(data, 4);
        this.container = container;
        this.data = data;

        // Slot 0: Base Item (Weapon / Armor / Material)
        this.addSlot(new ForgeSlot(container, SLOT_BASE, 87, 27));
        // Slot 1: Monster Core
        this.addSlot(new ForgeSlot(container, SLOT_CORE, 109, 27));
        // Slot 2: Mana/Qi Catalyst
        this.addSlot(new ForgeSlot(container, SLOT_CATALYST, 87, 49));
        // Slot 3: Enchanting Gem / Rune
        this.addSlot(new ForgeSlot(container, SLOT_GEM, 109, 49));
        // Slot 4: Output
        this.addSlot(new Slot(container, SLOT_OUTPUT, 151, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player Inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 166));
        }

        this.addDataSlots(data);
    }

    /** Ranura que solo acepta lo que le corresponde (arma o armadura, núcleo, catalizador, gema). */
    private static class ForgeSlot extends Slot {
        ForgeSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return HunterForgeBlockEntity.isValidFor(this.getContainerSlot(), stack);
        }
    }

    public int getTemperingPos() {
        return this.data.get(0);
    }

    public int getForgeProgress() {
        return this.data.get(1);
    }

    /** Ticks que quedan de la pausa tras un golpe (0 = la aguja se mueve). */
    public int getStrikeTicks() {
        return this.data.get(2);
    }

    public boolean isForging() {
        return this.data.get(2) != 0;
    }

    public HunterForgeQuality getLastQuality() {
        int ord = this.data.get(3);
        HunterForgeQuality[] vals = HunterForgeQuality.values();
        if (ord >= 0 && ord < vals.length) {
            return vals[ord];
        }
        return HunterForgeQuality.NORMAL;
    }

    public boolean canForge() {
        if (container instanceof HunterForgeBlockEntity be) {
            return be.canForge();
        }
        ItemStack base = container.getItem(SLOT_BASE);
        ItemStack core = container.getItem(SLOT_CORE);
        ItemStack catalyst = container.getItem(SLOT_CATALYST);
        ItemStack gem = container.getItem(SLOT_GEM);
        ItemStack out = container.getItem(SLOT_OUTPUT);
        return !base.isEmpty() && !core.isEmpty() && !catalyst.isEmpty() && !gem.isEmpty() && out.isEmpty() && !isForging();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof HunterForgeBlockEntity be) {
            return be.executeForge(player, id);
        }
        return false;
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
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == SLOT_OUTPUT) {
                if (!this.moveItemStackTo(stackInSlot, FORGE_SLOTS_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, itemstack);
            } else if (index < FORGE_SLOTS_COUNT) {
                if (!this.moveItemStackTo(stackInSlot, FORGE_SLOTS_COUNT, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, FORGE_SLOTS_COUNT - 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
