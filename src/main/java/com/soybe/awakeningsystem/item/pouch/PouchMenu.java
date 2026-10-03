package com.soybe.awakeningsystem.item.pouch;

import com.soybe.awakeningsystem.furnace.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PouchMenu extends AbstractContainerMenu {
    private final PouchTier tier;
    private final InteractionHand hand;
    private final ItemStack pouchStack;
    private final IItemHandler pouchHandler;

    // Client-side constructor from RegistryFriendlyByteBuf
    public PouchMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readEnum(InteractionHand.class), extraData.readEnum(PouchTier.class));
    }

    // Server-side constructor
    public PouchMenu(int containerId, Inventory playerInventory, InteractionHand hand, PouchTier tier) {
        super(ModMenus.POUCH_MENU.get(), containerId);
        this.tier = tier;
        this.hand = hand;
        this.pouchStack = playerInventory.player.getItemInHand(hand);
        this.pouchHandler = new ComponentItemHandler(this.pouchStack, DataComponents.CONTAINER, tier.getSlotCount());

        setupPouchSlots();
        setupPlayerSlots(playerInventory);
    }

    public PouchTier getTier() {
        return tier;
    }

    private void setupPouchSlots() {
        switch (tier) {
            case WANDERER -> {
                // 18 slots: 2 rows of 9 (x: 8 + col * 18, y: 20 + row * 18)
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 9; col++) {
                        addSlot(new PouchSlot(pouchHandler, col + row * 9, 8 + col * 18, 20 + row * 18));
                    }
                }
            }
            case ADVENTURER -> {
                // 27 slots: 3 rows of 9 (x: 8 + col * 18, y: 18 + row * 18)
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 9; col++) {
                        addSlot(new PouchSlot(pouchHandler, col + row * 9, 8 + col * 18, 18 + row * 18));
                    }
                }
            }
            case OUTLANDER -> {
                // 66 slots: 6 rows of 11 (x: 10 + col * 18, y: 16 + row * 18)
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 11; col++) {
                        addSlot(new PouchSlot(pouchHandler, col + row * 11, 10 + col * 18, 16 + row * 18));
                    }
                }
            }
            case EMERGENCY -> {
                // 143 slots:
                // Upper body: 17 columns x 7 rows = 119 slots (x: 9 + col * 18, y: 20 + row * 18)
                for (int row = 0; row < 7; row++) {
                    for (int col = 0; col < 17; col++) {
                        addSlot(new PouchSlot(pouchHandler, col + row * 17, 9 + col * 18, 20 + row * 18));
                    }
                }
                // Left wing: 3 cols x 4 rows (12 slots, indices 119..130)
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 3; col++) {
                        addSlot(new PouchSlot(pouchHandler, 119 + col + row * 3, 9 + col * 18, 151 + row * 18));
                    }
                }
                // Right wing: 3 cols x 4 rows (12 slots, indices 131..142)
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 3; col++) {
                        addSlot(new PouchSlot(pouchHandler, 131 + col + row * 3, 261 + col * 18, 151 + row * 18));
                    }
                }
            }
        }
    }

    private void setupPlayerSlots(Inventory playerInv) {
        int invX = 8;
        int invY = 84;
        int hotbarY = 142;

        switch (tier) {
            case WANDERER -> {
                invX = 8;
                invY = 74;
                hotbarY = 132;
            }
            case ADVENTURER -> {
                invX = 8;
                invY = 84;
                hotbarY = 142;
            }
            case OUTLANDER -> {
                invX = 28;
                invY = 135;
                hotbarY = 193;
            }
            case EMERGENCY -> {
                invX = 81;
                invY = 155;
                hotbarY = 213;
            }
        }

        // Player main inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                addSlot(new Slot(playerInv, slotIndex, invX + col * 18, invY + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return !ItemStack.matches(getItem(), pouchStack);
                    }
                });
            }
        }

        // Hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, invX + col * 18, hotbarY) {
                @Override
                public boolean mayPickup(Player player) {
                    return !ItemStack.matches(getItem(), pouchStack);
                }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack held = player.getItemInHand(hand);
        return !held.isEmpty() && held.getItem() instanceof PouchItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            int pouchSlotCount = tier.getSlotCount();
            if (index < pouchSlotCount) {
                // Moving from pouch to player inventory
                if (!this.moveItemStackTo(slotStack, pouchSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to pouch (disallow pouches)
                if (slotStack.getItem() instanceof PouchItem) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(slotStack, 0, pouchSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    private static class PouchSlot extends SlotItemHandler {
        public PouchSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.getItem() instanceof PouchItem) {
                return false;
            }
            return super.mayPlace(stack);
        }
    }
}
