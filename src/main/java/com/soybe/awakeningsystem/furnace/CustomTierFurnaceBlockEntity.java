package com.soybe.awakeningsystem.furnace;

import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CustomTierFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_RESULT = 2;
    public static final int SLOT_AUGMENT_RED = 3;
    public static final int SLOT_AUGMENT_GREEN = 4;
    public static final int SLOT_AUGMENT_BLUE = 5;
    public static final int TOTAL_SLOTS = 6;

    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int DATA_AUGMENT_GUI = 4;
    public static final int DATA_AUGMENT_TYPE = 5;
    public static final int DATA_SIDE_TOP = 6;
    public static final int DATA_SIDE_BOTTOM = 7;
    public static final int DATA_SIDE_FRONT = 8;
    public static final int DATA_SIDE_BACK = 9;
    public static final int DATA_SIDE_LEFT = 10;
    public static final int DATA_SIDE_RIGHT = 11;
    public static final int DATA_AUTO_INPUT = 12;
    public static final int DATA_AUTO_OUTPUT = 13;
    public static final int DATA_REDSTONE_MODE = 14;
    public static final int DATA_COUNT = 15;

    // Side IO modes: 0=None, 1=Input, 2=Output, 3=Fuel, 4=Input/Output
    public static final int MODE_NONE = 0;
    public static final int MODE_INPUT = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_FUEL = 3;
    public static final int MODE_IO = 4;

    // Relative sides: 0=TOP, 1=BOTTOM, 2=FRONT, 3=BACK, 4=LEFT, 5=RIGHT
    private final int[] sideConfig = new int[]{MODE_INPUT, MODE_OUTPUT, MODE_NONE, MODE_FUEL, MODE_FUEL, MODE_FUEL};
    private int autoInput = 0;
    private int autoOutput = 0;
    // 0=Ignored, 1=Low (no redstone), 2=High (redstone required)
    private int redstoneMode = 0;
    private int autoIoCooldown = 0;

    private static final int[] SLOTS_NONE = new int[0];
    private static final int[] SLOTS_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_DOWN = new int[]{SLOT_RESULT, SLOT_FUEL};
    private static final int[] SLOTS_HORIZONTAL = new int[]{SLOT_FUEL};
    private static final int[] SLOTS_RESULT_ONLY = new int[]{SLOT_RESULT};
    private static final int[] SLOTS_IO = new int[]{SLOT_INPUT, SLOT_RESULT};

    private final FurnaceTier tier;
    private NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private int showAugmentGui = 0;

    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;

    public ItemStack getInstalledAugment() {
        return this.items.get(SLOT_AUGMENT_RED);
    }

    public void setInstalledAugment(ItemStack augment) {
        this.items.set(SLOT_AUGMENT_RED, augment);
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            int newType = getAugmentType();
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(CustomTierFurnaceBlock.TYPE) && state.getValue(CustomTierFurnaceBlock.TYPE) != newType) {
                this.level.setBlock(this.worldPosition, state.setValue(CustomTierFurnaceBlock.TYPE, newType), 3);
            }
        }
    }

    public ItemStack removeInstalledAugment() {
        ItemStack prev = this.items.get(SLOT_AUGMENT_RED).copy();
        this.items.set(SLOT_AUGMENT_RED, ItemStack.EMPTY);
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(CustomTierFurnaceBlock.TYPE) && state.getValue(CustomTierFurnaceBlock.TYPE) != 0) {
                this.level.setBlock(this.worldPosition, state.setValue(CustomTierFurnaceBlock.TYPE, 0), 3);
            }
        }
        return prev;
    }

    public int getAugmentType() {
        ItemStack red = this.items.get(SLOT_AUGMENT_RED);
        if (red.isEmpty()) return 0;
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(red.getItem()).getPath();
        if (path.contains("blasting")) return 2;
        if (path.contains("smoking")) return 1;
        return 0;
    }

    public int getShowAugmentGui() {
        return this.showAugmentGui;
    }

    public void setShowAugmentGui(int show) {
        this.showAugmentGui = show;
        setChanged();
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> CustomTierFurnaceBlockEntity.this.litTime;
                case DATA_LIT_DURATION -> CustomTierFurnaceBlockEntity.this.litDuration;
                case DATA_COOKING_PROGRESS -> CustomTierFurnaceBlockEntity.this.cookingProgress;
                case DATA_COOKING_TOTAL_TIME -> CustomTierFurnaceBlockEntity.this.cookingTotalTime;
                case DATA_AUGMENT_GUI -> CustomTierFurnaceBlockEntity.this.showAugmentGui;
                case DATA_AUGMENT_TYPE -> CustomTierFurnaceBlockEntity.this.getAugmentType();
                case DATA_SIDE_TOP, DATA_SIDE_BOTTOM, DATA_SIDE_FRONT, DATA_SIDE_BACK, DATA_SIDE_LEFT, DATA_SIDE_RIGHT ->
                        CustomTierFurnaceBlockEntity.this.sideConfig[index - DATA_SIDE_TOP];
                case DATA_AUTO_INPUT -> CustomTierFurnaceBlockEntity.this.autoInput;
                case DATA_AUTO_OUTPUT -> CustomTierFurnaceBlockEntity.this.autoOutput;
                case DATA_REDSTONE_MODE -> CustomTierFurnaceBlockEntity.this.redstoneMode;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_LIT_TIME -> CustomTierFurnaceBlockEntity.this.litTime = value;
                case DATA_LIT_DURATION -> CustomTierFurnaceBlockEntity.this.litDuration = value;
                case DATA_COOKING_PROGRESS -> CustomTierFurnaceBlockEntity.this.cookingProgress = value;
                case DATA_COOKING_TOTAL_TIME -> CustomTierFurnaceBlockEntity.this.cookingTotalTime = value;
                case DATA_AUGMENT_GUI -> CustomTierFurnaceBlockEntity.this.showAugmentGui = value;
                case DATA_SIDE_TOP, DATA_SIDE_BOTTOM, DATA_SIDE_FRONT, DATA_SIDE_BACK, DATA_SIDE_LEFT, DATA_SIDE_RIGHT ->
                        CustomTierFurnaceBlockEntity.this.sideConfig[index - DATA_SIDE_TOP] = value;
                case DATA_AUTO_INPUT -> CustomTierFurnaceBlockEntity.this.autoInput = value;
                case DATA_AUTO_OUTPUT -> CustomTierFurnaceBlockEntity.this.autoOutput = value;
                case DATA_REDSTONE_MODE -> CustomTierFurnaceBlockEntity.this.redstoneMode = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public CustomTierFurnaceBlockEntity(BlockPos pos, BlockState state, FurnaceTier tier) {
        super(switch (tier) {
            case ABYSSAL -> ModBlockEntities.ABYSSAL_FURNACE.get();
            case SOUL_ALLOY -> ModBlockEntities.SOUL_ALLOY_FURNACE.get();
            case ASTRALITE -> ModBlockEntities.ASTRALITE_FURNACE.get();
            case MITHRIL -> ModBlockEntities.MITHRIL_FURNACE.get();
            case ADAMANTITE -> ModBlockEntities.ADAMANTITE_FURNACE.get();
            case SPIRIT_JADE -> ModBlockEntities.SPIRIT_JADE_FURNACE.get();
            case ASTRAL -> ModBlockEntities.ASTRAL_FURNACE.get();
            case CELESTIAL -> ModBlockEntities.CELESTIAL_FURNACE.get();
            case PRIMORDIAL -> ModBlockEntities.PRIMORDIAL_FURNACE.get();
        }, pos, state);
        this.tier = tier;
        this.cookingTotalTime = tier.getCookTime();
    }

    public FurnaceTier getTier() {
        return tier;
    }

    public boolean isLit() {
        return this.litTime > 0;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.litTime = tag.getInt("BurnTime");
        this.cookingProgress = tag.getInt("CookTime");
        this.cookingTotalTime = tag.getInt("CookTimeTotal");
        if (this.cookingTotalTime <= 0) {
            this.cookingTotalTime = this.tier.getCookTime();
        }
        this.litDuration = tag.getInt("LitDuration");
        if (tag.contains("ShowAugmentGui")) {
            this.showAugmentGui = tag.getInt("ShowAugmentGui");
        }
        if (tag.contains("SideConfig", Tag.TAG_INT_ARRAY)) {
            int[] arr = tag.getIntArray("SideConfig");
            for (int i = 0; i < Math.min(arr.length, 6); i++) {
                this.sideConfig[i] = Math.max(0, Math.min(4, arr[i]));
            }
        }
        this.autoInput = tag.getInt("AutoInput");
        this.autoOutput = tag.getInt("AutoOutput");
        this.redstoneMode = tag.getInt("RedstoneMode");
        if (tag.contains("InstalledAugment", Tag.TAG_COMPOUND) && this.items.get(SLOT_AUGMENT_RED).isEmpty()) {
            ItemStack legacyAug = ItemStack.parse(registries, tag.getCompound("InstalledAugment")).orElse(ItemStack.EMPTY);
            if (!legacyAug.isEmpty()) {
                this.items.set(SLOT_AUGMENT_RED, legacyAug);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BurnTime", this.litTime);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        tag.putInt("LitDuration", this.litDuration);
        tag.putInt("ShowAugmentGui", this.showAugmentGui);
        tag.putIntArray("SideConfig", this.sideConfig);
        tag.putInt("AutoInput", this.autoInput);
        tag.putInt("AutoOutput", this.autoOutput);
        tag.putInt("RedstoneMode", this.redstoneMode);
        if (!this.items.get(SLOT_AUGMENT_RED).isEmpty()) {
            tag.put("InstalledAugment", this.items.get(SLOT_AUGMENT_RED).save(registries));
        }
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CustomTierFurnaceBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean stateChanged = false;

        int targetType = be.getAugmentType();
        if (state.hasProperty(CustomTierFurnaceBlock.TYPE) && state.getValue(CustomTierFurnaceBlock.TYPE) != targetType) {
            state = state.setValue(CustomTierFurnaceBlock.TYPE, targetType);
            level.setBlock(pos, state, 3);
            stateChanged = true;
        }

        if (be.isLit()) {
            be.litTime--;
        }

        if ((be.autoInput == 1 || be.autoOutput == 1) && ++be.autoIoCooldown >= 5) {
            be.autoIoCooldown = 0;
            if (be.processAutoIO(level, pos, state)) {
                stateChanged = true;
            }
        }

        boolean redstoneAllows = be.isRedstoneAllowed(level, pos);

        ItemStack fuelStack = be.items.get(SLOT_FUEL);
        ItemStack inputStack = be.items.get(SLOT_INPUT);
        boolean hasInput = !inputStack.isEmpty();
        boolean hasFuel = !fuelStack.isEmpty();

        int baseCookTime = be.tier.getCookTime();
        int effectiveCookTime = (targetType != 0) ? Math.max(1, baseCookTime / 2) : baseCookTime;
        be.cookingTotalTime = effectiveCookTime;

        if (redstoneAllows && (be.isLit() || (hasFuel && hasInput))) {
            RecipeHolder<?> recipe = hasInput ? be.getCurrentRecipe(level, inputStack) : null;
            int maxStackSize = be.getMaxStackSize();

            if (be.canBurn(recipe, be.items, maxStackSize)) {
                if (!be.isLit()) {
                    be.litTime = be.getBurnDuration(fuelStack);
                    be.litDuration = be.litTime;
                    if (be.isLit()) {
                        stateChanged = true;
                        if (fuelStack.hasCraftingRemainingItem()) {
                            be.items.set(SLOT_FUEL, fuelStack.getCraftingRemainingItem());
                        } else if (hasFuel) {
                            fuelStack.shrink(1);
                            if (fuelStack.isEmpty()) {
                                be.items.set(SLOT_FUEL, fuelStack.getCraftingRemainingItem());
                            }
                        }
                    }
                }

                if (be.isLit() && be.canBurn(recipe, be.items, maxStackSize)) {
                    be.cookingProgress++;
                    if (be.cookingProgress >= be.cookingTotalTime) {
                        be.cookingProgress = 0;
                        be.cookingTotalTime = effectiveCookTime;
                        if (be.burn(recipe, be.items, maxStackSize)) {
                            stateChanged = true;
                        }
                    }
                } else {
                    be.cookingProgress = 0;
                }
            } else {
                be.cookingProgress = 0;
            }
        } else if (!be.isLit() && be.cookingProgress > 0) {
            be.cookingProgress = Math.max(0, be.cookingProgress - 2);
        }

        if (wasLit != be.isLit()) {
            stateChanged = true;
            state = state.setValue(CustomTierFurnaceBlock.LIT, be.isLit());
            level.setBlock(pos, state, 3);
        }

        if (stateChanged) {
            setChanged(level, pos, state);
        }
    }

    private RecipeHolder<?> getCurrentRecipe(Level level, ItemStack input) {
        if (input.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        RecipeManager rm = serverLevel.getRecipeManager();
        int augType = getAugmentType();

        if (augType == 2) {
            Optional<RecipeHolder<BlastingRecipe>> blasting = rm.getRecipeFor(RecipeType.BLASTING, recipeInput, serverLevel);
            return blasting.orElse(null);
        } else if (augType == 1) {
            Optional<RecipeHolder<SmokingRecipe>> smoking = rm.getRecipeFor(RecipeType.SMOKING, recipeInput, serverLevel);
            return smoking.orElse(null);
        } else {
            Optional<RecipeHolder<SmeltingRecipe>> smelting = rm.getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel);
            if (smelting.isPresent()) return smelting.get();

            Optional<RecipeHolder<BlastingRecipe>> blasting = rm.getRecipeFor(RecipeType.BLASTING, recipeInput, serverLevel);
            if (blasting.isPresent()) return blasting.get();

            Optional<RecipeHolder<SmokingRecipe>> smoking = rm.getRecipeFor(RecipeType.SMOKING, recipeInput, serverLevel);
            return smoking.orElse(null);
        }
    }

    private boolean canBurn(@Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> inventory, int maxStackSize) {
        if (inventory.get(SLOT_INPUT).isEmpty() || recipeHolder == null) {
            return false;
        }

        Recipe<?> recipe = recipeHolder.value();
        if (!(this.level instanceof ServerLevel serverLevel)) return false;

        ItemStack result = recipe.getResultItem(serverLevel.registryAccess());
        if (result.isEmpty()) return false;

        ItemStack currentResult = inventory.get(SLOT_RESULT);
        if (currentResult.isEmpty()) return true;

        if (!ItemStack.isSameItemSameComponents(currentResult, result)) return false;

        int combined = currentResult.getCount() + result.getCount();
        return combined <= maxStackSize && combined <= currentResult.getMaxStackSize();
    }

    private boolean burn(@Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> inventory, int maxStackSize) {
        if (recipeHolder == null || !canBurn(recipeHolder, inventory, maxStackSize) || !(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }

        ItemStack input = inventory.get(SLOT_INPUT);
        ItemStack recipeResult = recipeHolder.value().getResultItem(serverLevel.registryAccess()).copy();
        ItemStack currentResult = inventory.get(SLOT_RESULT);

        int countToProduce = recipeResult.getCount();
        if (this.tier.getDoubleOutputChance() > 0.0F && serverLevel.getRandom().nextFloat() < this.tier.getDoubleOutputChance()) {
            countToProduce *= 2;
        }

        recipeResult.setCount(countToProduce);

        if (currentResult.isEmpty()) {
            inventory.set(SLOT_RESULT, recipeResult);
        } else if (ItemStack.isSameItemSameComponents(currentResult, recipeResult)) {
            int availableSpace = Math.min(maxStackSize, currentResult.getMaxStackSize()) - currentResult.getCount();
            int add = Math.min(availableSpace, recipeResult.getCount());
            currentResult.grow(add);
        }

        input.shrink(1);
        return true;
    }

    private int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }

        if (this.tier == FurnaceTier.ASTRAL || this.tier == FurnaceTier.CELESTIAL || this.tier == FurnaceTier.PRIMORDIAL) {
            String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(fuel.getItem()).getPath();
            if (path.contains("coin") || path.contains("star") || path.contains("soul") || path.contains("celestial") || path.contains("primordial")) {
                return this.tier == FurnaceTier.PRIMORDIAL ? 12000 : (this.tier == FurnaceTier.CELESTIAL ? 8000 : 4000);
            }
        }

        int defaultTime = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel.getItem(), 0);
        return EventHooks.getItemBurnTime(fuel, defaultTime, RecipeType.SMELTING);
    }

    private boolean isRedstoneAllowed(Level level, BlockPos pos) {
        if (this.redstoneMode == 1) {
            return !level.hasNeighborSignal(pos);
        }
        if (this.redstoneMode == 2) {
            return level.hasNeighborSignal(pos);
        }
        return true;
    }

    private int getRelativeSideIndex(Direction worldSide, BlockState state) {
        if (worldSide == Direction.UP) return 0; // TOP
        if (worldSide == Direction.DOWN) return 1; // BOTTOM
        Direction facing = state.hasProperty(CustomTierFurnaceBlock.FACING)
                ? state.getValue(CustomTierFurnaceBlock.FACING)
                : Direction.NORTH;
        if (worldSide == facing) return 2; // FRONT
        if (worldSide == facing.getOpposite()) return 3; // BACK
        if (worldSide == facing.getClockWise()) return 4; // LEFT
        return 5; // RIGHT
    }

    private boolean processAutoIO(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;
        for (Direction dir : Direction.values()) {
            int rel = getRelativeSideIndex(dir, state);
            int mode = this.sideConfig[rel];
            if (mode == MODE_NONE) continue;

            BlockPos neighborPos = pos.relative(dir);
            net.neoforged.neoforge.items.IItemHandler neighbor = level.getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                    neighborPos, dir.getOpposite());
            if (neighbor == null) continue;

            if (this.autoInput == 1 && (mode == MODE_INPUT || mode == MODE_IO || mode == MODE_FUEL)) {
                int targetSlot = (mode == MODE_FUEL) ? SLOT_FUEL : SLOT_INPUT;
                ItemStack current = this.items.get(targetSlot);
                if (current.getCount() < current.getMaxStackSize() || current.isEmpty()) {
                    for (int s = 0; s < neighbor.getSlots(); s++) {
                        ItemStack inNeighbor = neighbor.getStackInSlot(s);
                        if (inNeighbor.isEmpty()) continue;
                        if (targetSlot == SLOT_FUEL && getBurnDuration(inNeighbor) <= 0) continue;
                        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, inNeighbor)) continue;
                        int space = (current.isEmpty() ? inNeighbor.getMaxStackSize() : current.getMaxStackSize() - current.getCount());
                        if (space <= 0) break;
                        ItemStack extracted = neighbor.extractItem(s, Math.min(64, space), false);
                        if (!extracted.isEmpty()) {
                            if (current.isEmpty()) {
                                this.items.set(targetSlot, extracted);
                            } else {
                                current.grow(extracted.getCount());
                            }
                            changed = true;
                            break;
                        }
                    }
                }
            }

            if (this.autoOutput == 1 && (mode == MODE_OUTPUT || mode == MODE_IO)) {
                ItemStack outStack = this.items.get(SLOT_RESULT);
                if (!outStack.isEmpty()) {
                    ItemStack remaining = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(neighbor, outStack.copy(), false);
                    if (remaining.getCount() != outStack.getCount()) {
                        this.items.set(SLOT_RESULT, remaining);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int rel = getRelativeSideIndex(side, getBlockState());
        return switch (this.sideConfig[rel]) {
            case MODE_INPUT -> SLOTS_UP;
            case MODE_OUTPUT -> SLOTS_RESULT_ONLY;
            case MODE_FUEL -> SLOTS_HORIZONTAL;
            case MODE_IO -> SLOTS_IO;
            default -> SLOTS_NONE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        if (direction != null) {
            int rel = getRelativeSideIndex(direction, getBlockState());
            int mode = this.sideConfig[rel];
            if (index == SLOT_INPUT && mode != MODE_INPUT && mode != MODE_IO) return false;
            if (index == SLOT_FUEL && mode != MODE_FUEL) return false;
            if (index != SLOT_INPUT && index != SLOT_FUEL) return false;
        }
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        int rel = getRelativeSideIndex(direction, getBlockState());
        int mode = this.sideConfig[rel];
        if (index == SLOT_RESULT) {
            return mode == MODE_OUTPUT || mode == MODE_IO;
        }
        if (index == SLOT_FUEL && (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET))) {
            return mode == MODE_OUTPUT || mode == MODE_FUEL || mode == MODE_IO;
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
            if (slot == SLOT_AUGMENT_RED && this.level != null && !this.level.isClientSide) {
                int newType = getAugmentType();
                BlockState state = this.level.getBlockState(this.worldPosition);
                if (state.hasProperty(CustomTierFurnaceBlock.TYPE) && state.getValue(CustomTierFurnaceBlock.TYPE) != newType) {
                    this.level.setBlock(this.worldPosition, state.setValue(CustomTierFurnaceBlock.TYPE, newType), 3);
                }
            }
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(this.items, slot);
        if (!result.isEmpty() && slot == SLOT_AUGMENT_RED && this.level != null && !this.level.isClientSide) {
            int newType = getAugmentType();
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(CustomTierFurnaceBlock.TYPE) && state.getValue(CustomTierFurnaceBlock.TYPE) != newType) {
                this.level.setBlock(this.worldPosition, state.setValue(CustomTierFurnaceBlock.TYPE, newType), 3);
            }
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack current = this.items.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(current, stack);
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (slot == SLOT_INPUT && !sameItem) {
            this.cookingTotalTime = this.tier.getCookTime();
            this.cookingProgress = 0;
            this.setChanged();
        }

        if (slot == SLOT_AUGMENT_RED) {
            this.setChanged();
            if (this.level != null && !this.level.isClientSide) {
                int newType = getAugmentType();
                BlockState state = this.level.getBlockState(this.worldPosition);
                if (state.hasProperty(CustomTierFurnaceBlock.TYPE) && state.getValue(CustomTierFurnaceBlock.TYPE) != newType) {
                    this.level.setBlock(this.worldPosition, state.setValue(CustomTierFurnaceBlock.TYPE, newType), 3);
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == SLOT_RESULT) {
            return false;
        } else if (index == SLOT_FUEL) {
            ItemStack currentFuel = this.items.get(SLOT_FUEL);
            return getBurnDuration(stack) > 0 || (stack.is(Items.BUCKET) && !currentFuel.is(Items.BUCKET));
        } else if (index == SLOT_AUGMENT_RED) {
            return CustomTierFurnaceMenu.isRedAugment(stack);
        } else if (index == SLOT_AUGMENT_GREEN) {
            return CustomTierFurnaceMenu.isGreenAugment(stack);
        } else if (index == SLOT_AUGMENT_BLUE) {
            return CustomTierFurnaceMenu.isBlueAugment(stack);
        } else {
            return true;
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.awakening_system." + this.tier.getId() + "_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CustomTierFurnaceMenu(containerId, inventory, this, this.dataAccess, this.tier);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public NonNullList<ItemStack> getInventoryItems() {
        return this.items;
    }

    public void setInventoryItems(NonNullList<ItemStack> newItems) {
        this.items = newItems;
        this.setChanged();
    }
}
