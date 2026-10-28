package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FishingTrapBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int CONTAINER_SIZE = 9;
    public static final int FISH_INTERVAL_TICKS = 100;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    private boolean active = true;
    private boolean redstonePowered = false;
    private int progress = 0;
    private int status = 0;

    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_UP = new int[]{4, 5, 6, 7, 8};
    private static final int[] SLOTS_FOR_SIDES = new int[]{4, 5, 6, 7, 8};

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> FISH_INTERVAL_TICKS;
                case 2 -> status;
                case 3 -> active ? 1 : 0;
                case 4 -> redstonePowered ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> status = value;
                case 3 -> active = (value != 0);
                case 4 -> redstonePowered = (value != 0);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public FishingTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHING_BLOCK.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.progress = tag.getInt("FishingProgress");
        if (tag.contains("TrapActive")) {
            this.active = tag.getBoolean("TrapActive");
        } else {
            this.active = true;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("FishingProgress", this.progress);
        tag.putBoolean("TrapActive", this.active);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : this.items) {
            if (!s.isEmpty()) return false;
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
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return SLOTS_FOR_DOWN;
        return SLOTS_FOR_UP;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot >= 4 && isBait(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot < 4;
    }

    public static boolean isBait(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(SystemItems.FRUIT_PASTE_BAIT.get())
                || stack.is(SystemItems.REDROT_BAIT.get())
                || stack.is(SystemItems.FALSE_WORM_BAIT.get())
                || stack.is(SystemItems.FAKE_FLY_BAIT.get())
                || stack.is(SystemItems.GLOWGRASS_BAIT.get())
                || stack.is(SystemItems.SUGARDEW_BAIT.get());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.awakening_system.fishing_trap");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FishingTrapMenu(containerId, playerInventory, this, this.dataAccess);
    }

    public boolean hasWaterDirectlyAbove() {
        if (level == null) return false;
        BlockPos above = worldPosition.above();
        return level.getFluidState(above).is(FluidTags.WATER) || level.getBlockState(above).is(Blocks.WATER);
    }

    public boolean isWaterNearby() {
        if (level == null) return false;
        if (hasWaterDirectlyAbove()) return true;
        if (level.getFluidState(worldPosition).is(FluidTags.WATER)) return true;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adj = worldPosition.relative(dir);
            if (level.getFluidState(adj).is(FluidTags.WATER) || level.getBlockState(adj).is(Blocks.WATER)) return true;
        }
        return false;
    }

    public int getFirstBaitSlot() {
        for (int i = 4; i <= 8; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && isBait(stack)) {
                return i;
            }
        }
        return -1;
    }

    public boolean triggerActivate(@Nullable Player player) {
        if (level == null || level.isClientSide()) return false;

        if (!isWaterNearby()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("block.awakening_system.fishing_trap.requires_water"), true);
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return false;
        }

        int baitSlot = getFirstBaitSlot();
        if (baitSlot == -1) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("block.awakening_system.fishing_trap.no_bait"), true);
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return false;
        }

        ItemStack baitStack = items.get(baitSlot);
        ItemStack baitType = baitStack.copy();
        baitStack.shrink(1);
        setChanged();

        ItemStack catchItem = rollFishCatch(baitType, level.random.nextFloat());

        boolean inserted = false;
        for (int i = 0; i < 4; i++) {
            ItemStack out = items.get(i);
            if (out.isEmpty()) {
                items.set(i, catchItem.copy());
                inserted = true;
                break;
            } else if (ItemStack.isSameItemSameComponents(out, catchItem) && out.getCount() < out.getMaxStackSize()) {
                out.grow(catchItem.getCount());
                inserted = true;
                break;
            }
        }

        BlockPos waterPos = worldPosition.above();
        if (!hasWaterDirectlyAbove()) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(worldPosition.relative(dir)).is(FluidTags.WATER)) {
                    waterPos = worldPosition.relative(dir);
                    break;
                }
            }
        }

        if (!inserted) {
            net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                    level, waterPos.getX() + 0.5D, waterPos.getY() + 0.2D, waterPos.getZ() + 0.5D, catchItem
            );
            level.addFreshEntity(entity);
        }

        setChanged();

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SPLASH, waterPos.getX() + 0.5D, waterPos.getY() + 0.5D, waterPos.getZ() + 0.5D, 25, 0.3D, 0.3D, 0.3D, 0.15D);
            sl.sendParticles(ParticleTypes.BUBBLE, waterPos.getX() + 0.5D, waterPos.getY() + 0.2D, waterPos.getZ() + 0.5D, 15, 0.2D, 0.2D, 0.2D, 0.1D);
            sl.playSound(null, worldPosition, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 1.0F, 1.2F);
        }

        if (player != null) {
            player.displayClientMessage(Component.translatable("block.awakening_system.fishing_trap.catch_success", catchItem.getHoverName()), true);
            int caught = player.getPersistentData().getInt("total_fish_caught");
            player.getPersistentData().putInt("total_fish_caught", caught + 1);

            if (player instanceof ServerPlayer sp) {
                var data = SystemEvents.data(sp);
                if (data != null) {
                    data.addExperience(15);
                    AwakeningSystemMod.sync(sp);
                }
            }
        }

        this.progress = 0;
        return true;
    }

    private ItemStack rollFishCatch(ItemStack bait, float roll) {
        if (bait.is(SystemItems.FRUIT_PASTE_BAIT.get())) {
            if (roll < 0.10F) return new ItemStack(SystemItems.GLAZE_MEDAKA.get());
            if (roll < 0.30F) return new ItemStack(SystemItems.SWEET_FLOWER_MEDAKA.get());
            if (roll < 0.55F) return new ItemStack(SystemItems.AIZEN_MEDAKA.get());
            return new ItemStack(SystemItems.MEDAKA.get());
        } else if (bait.is(SystemItems.REDROT_BAIT.get())) {
            if (roll < 0.25F) return new ItemStack(SystemItems.VENOMSPINE_FISH.get());
            if (roll < 0.60F) return new ItemStack(Items.SALMON);
            return new ItemStack(Items.COD);
        } else if (bait.is(SystemItems.FALSE_WORM_BAIT.get())) {
            if (roll < 0.15F) return new ItemStack(SystemItems.RAIMEI_ANGELFISH.get());
            if (roll < 0.45F) return new ItemStack(SystemItems.ABIDING_ANGELFISH.get());
            return new ItemStack(SystemItems.CRYSTALFISH.get());
        } else if (bait.is(SystemItems.FAKE_FLY_BAIT.get())) {
            if (roll < 0.35F) return new ItemStack(Items.PUFFERFISH);
            if (roll < 0.70F) return new ItemStack(Items.TROPICAL_FISH);
            return new ItemStack(SystemItems.CRYSTALFISH.get());
        } else if (bait.is(SystemItems.GLAZE_MEDAKA.get()) || bait.is(SystemItems.GLOWGRASS_BAIT.get()) || bait.is(SystemItems.SUGARDEW_BAIT.get())) {
            if (roll < 0.25F) return new ItemStack(SystemItems.RAIMEI_ANGELFISH.get());
            if (roll < 0.60F) return new ItemStack(SystemItems.ABIDING_ANGELFISH.get());
            return new ItemStack(SystemItems.GLAZE_MEDAKA.get());
        }

        return new ItemStack(Items.COD);
    }

    public void toggleActive(@Nullable Player player) {
        this.active = !this.active;
        this.progress = 0;
        setChanged();
        if (level != null && !level.isClientSide()) {
            if (this.active) {
                level.playSound(null, worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.8F, 1.2F);
                if (player != null) {
                    player.displayClientMessage(Component.translatable("block.awakening_system.fishing_trap.enabled"), true);
                }
            } else {
                level.playSound(null, worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.8F, 0.8F);
                if (player != null) {
                    player.displayClientMessage(Component.translatable("block.awakening_system.fishing_trap.disabled"), true);
                }
            }
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FishingTrapBlockEntity be) {
        boolean redstone = level.hasNeighborSignal(pos);
        be.redstonePowered = redstone;

        if (!be.active) {
            be.status = 4;
            be.progress = 0;
            return;
        }

        if (redstone) {
            be.status = 5;
            be.progress = 0;
            return;
        }

        if (!be.isWaterNearby()) {
            be.status = 0;
            be.progress = 0;
            return;
        }

        int baitSlot = be.getFirstBaitSlot();
        if (baitSlot == -1) {
            be.status = 1;
            be.progress = 0;
            return;
        }

        boolean hasRoom = false;
        for (int i = 0; i < 4; i++) {
            ItemStack out = be.items.get(i);
            if (out.isEmpty() || out.getCount() < out.getMaxStackSize()) {
                hasRoom = true;
                break;
            }
        }

        if (!hasRoom) {
            be.status = 3;
            return;
        }

        be.status = 2;
        be.progress++;

        if (be.progress % 20 == 0 && level instanceof ServerLevel sl) {
            BlockPos waterPos = pos.above();
            if (!be.hasWaterDirectlyAbove()) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (level.getFluidState(pos.relative(dir)).is(FluidTags.WATER)) {
                        waterPos = pos.relative(dir);
                        break;
                    }
                }
            }
            sl.sendParticles(ParticleTypes.BUBBLE, waterPos.getX() + 0.5D, waterPos.getY() + 0.2D, waterPos.getZ() + 0.5D, 3, 0.15D, 0.1D, 0.15D, 0.05D);
        }

        if (be.progress >= FISH_INTERVAL_TICKS) {
            be.progress = 0;
            be.triggerActivate(null);
        }
    }
}
