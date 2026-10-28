package com.soybe.awakeningsystem.furnace;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.EventHooks;

import java.util.function.BooleanSupplier;

public class CustomTierFurnaceMenu extends AbstractContainerMenu {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int AUGMENT_RED_SLOT = 3;
    public static final int AUGMENT_GREEN_SLOT = 4;
    public static final int AUGMENT_BLUE_SLOT = 5;

    public static final int SLOT_COUNT = 6;
    public static final int DATA_COUNT = 6;
    private static final int INV_SLOT_START = 6;
    private static final int INV_SLOT_END = 33;
    private static final int USE_ROW_SLOT_START = 33;
    private static final int USE_ROW_SLOT_END = 42;

    private final Container container;
    private final ContainerData data;
    protected final Level level;
    private final FurnaceTier tier;

    public CustomTierFurnaceMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT), FurnaceTier.MITHRIL);
    }

    public CustomTierFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, FurnaceTier tier) {
        super(switch (tier) {
            case ABYSSAL -> ModMenus.ABYSSAL_FURNACE_MENU.get();
            case SOUL_ALLOY -> ModMenus.SOUL_ALLOY_FURNACE_MENU.get();
            case ASTRALITE -> ModMenus.ASTRALITE_FURNACE_MENU.get();
            case MITHRIL -> ModMenus.MITHRIL_FURNACE_MENU.get();
            case ADAMANTITE -> ModMenus.ADAMANTITE_FURNACE_MENU.get();
            case SPIRIT_JADE -> ModMenus.SPIRIT_JADE_FURNACE_MENU.get();
            case ASTRAL -> ModMenus.ASTRAL_FURNACE_MENU.get();
            case CELESTIAL -> ModMenus.CELESTIAL_FURNACE_MENU.get();
            case PRIMORDIAL -> ModMenus.PRIMORDIAL_FURNACE_MENU.get();
        }, containerId);

        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.level = playerInventory.player.level();
        this.tier = tier;

        this.addSlot(new ConditionalSlot(container, INGREDIENT_SLOT, 56, 17, () -> !isAugmentGuiOpen()));
        this.addSlot(new ConditionalFuelSlot(this, container, FUEL_SLOT, 56, 53, () -> !isAugmentGuiOpen()));
        this.addSlot(new ConditionalResultSlot(playerInventory.player, container, RESULT_SLOT, 116, 35, () -> !isAugmentGuiOpen()));

        this.addSlot(new AugmentSlot(container, AUGMENT_RED_SLOT, 26, 35, 0, this::isAugmentGuiOpen));
        this.addSlot(new AugmentSlot(container, AUGMENT_GREEN_SLOT, 80, 35, 1, this::isAugmentGuiOpen));
        this.addSlot(new AugmentSlot(container, AUGMENT_BLUE_SLOT, 134, 35, 2, this::isAugmentGuiOpen));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    public FurnaceTier getTier() {
        return tier;
    }

    public boolean isAugmentGuiOpen() {
        return this.data.get(CustomTierFurnaceBlockEntity.DATA_AUGMENT_GUI) == 1;
    }

    public void setAugmentGuiOpen(boolean open) {
        this.data.set(CustomTierFurnaceBlockEntity.DATA_AUGMENT_GUI, open ? 1 : 0);
    }

    public int getAugmentType() {
        return this.data.get(CustomTierFurnaceBlockEntity.DATA_AUGMENT_TYPE);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            boolean open = isAugmentGuiOpen();
            setAugmentGuiOpen(!open);
            return true;
        }
        return super.clickMenuButton(player, id);
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

            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(stackInSlot, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, itemstack);
            } else if (index < INV_SLOT_START) {
                if (!this.moveItemStackTo(stackInSlot, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (isAugmentGuiOpen()) {
                    if (isRedAugment(stackInSlot)) {
                        if (!this.moveItemStackTo(stackInSlot, AUGMENT_RED_SLOT, AUGMENT_RED_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (isGreenAugment(stackInSlot)) {
                        if (!this.moveItemStackTo(stackInSlot, AUGMENT_GREEN_SLOT, AUGMENT_GREEN_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (isBlueAugment(stackInSlot)) {
                        if (!this.moveItemStackTo(stackInSlot, AUGMENT_BLUE_SLOT, AUGMENT_BLUE_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                        if (!this.moveItemStackTo(stackInSlot, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END && !this.moveItemStackTo(stackInSlot, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (this.canSmelt(stackInSlot)) {
                        if (!this.moveItemStackTo(stackInSlot, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (this.isFuel(stackInSlot)) {
                        if (!this.moveItemStackTo(stackInSlot, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                        if (!this.moveItemStackTo(stackInSlot, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END && !this.moveItemStackTo(stackInSlot, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
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

    protected boolean canSmelt(ItemStack stack) {
        if (stack.isEmpty()) return false;
        net.minecraft.world.item.crafting.SingleRecipeInput input = new net.minecraft.world.item.crafting.SingleRecipeInput(stack);
        int augType = getAugmentType();
        if (augType == 2) {
            return this.level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, input, this.level).isPresent();
        } else if (augType == 1) {
            return this.level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, input, this.level).isPresent();
        }
        return this.level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, this.level).isPresent()
                || this.level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, input, this.level).isPresent()
                || this.level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, input, this.level).isPresent();
    }

    public boolean isFuel(ItemStack stack) {
        int defaultBurn = net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
        return EventHooks.getItemBurnTime(stack, defaultBurn, RecipeType.SMELTING) > 0;
    }

    public boolean isLit() {
        return this.data.get(CustomTierFurnaceBlockEntity.DATA_LIT_TIME) > 0;
    }

    public int getBurnProgress() {
        int progress = this.data.get(CustomTierFurnaceBlockEntity.DATA_COOKING_PROGRESS);
        int total = this.data.get(CustomTierFurnaceBlockEntity.DATA_COOKING_TOTAL_TIME);
        return total != 0 && progress != 0 ? progress * 24 / total : 0;
    }

    public int getLitProgress() {
        int litDuration = this.data.get(CustomTierFurnaceBlockEntity.DATA_LIT_DURATION);
        if (litDuration == 0) {
            litDuration = 200;
        }
        return this.data.get(CustomTierFurnaceBlockEntity.DATA_LIT_TIME) * 13 / litDuration;
    }

    public static boolean isRedAugment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = key.getPath();
        String ns = key.getNamespace();
        if (ns.equals("ironfurnaces") || ns.equals(AwakeningSystemMod.MOD_ID)) {
            if (path.contains("blasting") || path.contains("smoking") || path.contains("fuel") || path.contains("generator") || path.contains("factory")) {
                return true;
            }
        }
        if (ModList.get().isLoaded("ironfurnaces")) {
            try {
                Class<?> clazz = Class.forName("ironfurnaces.items.augments.ItemAugmentRed");
                if (clazz.isInstance(stack.getItem())) return true;
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Ironfurnaces red augment reflection check failed: {}", t.getMessage());
            }
        }
        return false;
    }

    public static boolean isGreenAugment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = key.getPath();
        String ns = key.getNamespace();
        if (ns.equals("ironfurnaces") || ns.equals(AwakeningSystemMod.MOD_ID)) {
            if (path.contains("speed") || path.contains("upgrade")) {
                return true;
            }
        }
        if (ModList.get().isLoaded("ironfurnaces")) {
            try {
                Class<?> clazz = Class.forName("ironfurnaces.items.augments.ItemAugmentGreen");
                if (clazz.isInstance(stack.getItem())) return true;
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Ironfurnaces green augment reflection check failed: {}", t.getMessage());
            }
        }
        return false;
    }

    public static boolean isBlueAugment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = key.getPath();
        String ns = key.getNamespace();
        if (ns.equals("ironfurnaces") || ns.equals(AwakeningSystemMod.MOD_ID)) {
            if (path.contains("wireless") || path.contains("storage") || path.contains("blue")) {
                return true;
            }
        }
        if (ModList.get().isLoaded("ironfurnaces")) {
            try {
                Class<?> clazz = Class.forName("ironfurnaces.items.augments.ItemAugmentBlue");
                if (clazz.isInstance(stack.getItem())) return true;
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Ironfurnaces blue augment reflection check failed: {}", t.getMessage());
            }
        }
        return false;
    }

    public static class ConditionalSlot extends Slot {
        private final BooleanSupplier activeSupplier;

        public ConditionalSlot(Container container, int slot, int x, int y, BooleanSupplier activeSupplier) {
            super(container, slot, x, y);
            this.activeSupplier = activeSupplier;
        }

        @Override
        public boolean isActive() {
            return this.activeSupplier.getAsBoolean();
        }
    }

    public static class ConditionalFuelSlot extends Slot {
        private final CustomTierFurnaceMenu menu;
        private final BooleanSupplier activeSupplier;

        public ConditionalFuelSlot(CustomTierFurnaceMenu menu, Container container, int slot, int x, int y, BooleanSupplier activeSupplier) {
            super(container, slot, x, y);
            this.menu = menu;
            this.activeSupplier = activeSupplier;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.menu.isFuel(stack) || stack.is(Items.BUCKET);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return stack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(stack);
        }

        @Override
        public boolean isActive() {
            return this.activeSupplier.getAsBoolean();
        }
    }

    public static class ConditionalResultSlot extends FurnaceResultSlot {
        private final BooleanSupplier activeSupplier;

        public ConditionalResultSlot(Player player, Container container, int slot, int x, int y, BooleanSupplier activeSupplier) {
            super(player, container, slot, x, y);
            this.activeSupplier = activeSupplier;
        }

        @Override
        public boolean isActive() {
            return this.activeSupplier.getAsBoolean();
        }
    }

    public static class AugmentSlot extends Slot {
        private final int augmentColor;
        private final BooleanSupplier activeSupplier;

        public AugmentSlot(Container container, int slot, int x, int y, int augmentColor, BooleanSupplier activeSupplier) {
            super(container, slot, x, y);
            this.augmentColor = augmentColor;
            this.activeSupplier = activeSupplier;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return switch (augmentColor) {
                case 0 -> isRedAugment(stack);
                case 1 -> isGreenAugment(stack);
                case 2 -> isBlueAugment(stack);
                default -> false;
            };
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean isActive() {
            return this.activeSupplier.getAsBoolean();
        }
    }
}
