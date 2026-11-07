package com.soybe.awakeningsystem.workshop;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import com.soybe.awakeningsystem.furnace.ModMenus;
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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HunterForgeBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOT_BASE = 0;
    public static final int SLOT_CORE = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_GEM = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int TOTAL_SLOTS = 5;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_BASE};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_OUTPUT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_CORE, SLOT_CATALYST, SLOT_GEM};

    private NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    private int temperingPos = 50;
    private int temperingDirection = 2;
    private int forgeProgress = 0;
    private int isForging = 0;
    private int lastQualityOrdinal = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> HunterForgeBlockEntity.this.temperingPos;
                case 1 -> HunterForgeBlockEntity.this.forgeProgress;
                case 2 -> HunterForgeBlockEntity.this.isForging;
                case 3 -> HunterForgeBlockEntity.this.lastQualityOrdinal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> HunterForgeBlockEntity.this.temperingPos = value;
                case 1 -> HunterForgeBlockEntity.this.forgeProgress = value;
                case 2 -> HunterForgeBlockEntity.this.isForging = value;
                case 3 -> HunterForgeBlockEntity.this.lastQualityOrdinal = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public HunterForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HUNTER_FORGE_TABLE.get(), pos, blockState);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HunterForgeBlockEntity entity) {
        entity.updateTempering();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HunterForgeBlockEntity entity) {
        entity.updateTempering();
    }

    private void updateTempering() {
        temperingPos += temperingDirection;
        if (temperingPos >= 100) {
            temperingPos = 100;
            temperingDirection = -3;
        } else if (temperingPos <= 0) {
            temperingPos = 0;
            temperingDirection = 3;
        }
    }

    public boolean canForge() {
        ItemStack base = getItem(SLOT_BASE);
        ItemStack core = getItem(SLOT_CORE);
        ItemStack catalyst = getItem(SLOT_CATALYST);
        ItemStack gem = getItem(SLOT_GEM);
        ItemStack out = getItem(SLOT_OUTPUT);

        if (base.isEmpty() || core.isEmpty() || catalyst.isEmpty() || gem.isEmpty()) {
            return false;
        }
        return out.isEmpty();
    }

    public boolean executeForge(Player player, int timingHit) {
        if (!canForge()) {
            return false;
        }

        int dexBonus = 0;
        if (player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            if (data != null) {
                dexBonus = data.agility();
            }
        }

        HunterForgeQuality quality = HunterForgeQuality.evaluateFromTiming(timingHit, dexBonus);
        this.lastQualityOrdinal = quality.ordinal();

        ItemStack base = getItem(SLOT_BASE);
        ItemStack result = base.copy();
        result.setCount(1);

        HunterForgeQuality.applyQuality(result, quality);

        setItem(SLOT_OUTPUT, result);

        base.shrink(1);
        getItem(SLOT_CORE).shrink(1);
        getItem(SLOT_CATALYST).shrink(1);
        getItem(SLOT_GEM).shrink(1);

        setChanged();

        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, quality == HunterForgeQuality.LEGENDARY ? 1.3F : 1.0F);
            level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.7F, 1.2F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 30, 0.3D, 0.3D, 0.3D, 0.15D);
                if (quality == HunterForgeQuality.LEGENDARY) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.2D, worldPosition.getZ() + 0.5D, 40, 0.4D, 0.4D, 0.4D, 0.2D);
                }
            }
        }

        return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.awakening_system.hunter_forge");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new HunterForgeMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return SLOTS_FOR_DOWN;
        if (side == Direction.UP) return SLOTS_FOR_UP;
        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return index != SLOT_OUTPUT;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.temperingPos = tag.getInt("TemperingPos");
        this.forgeProgress = tag.getInt("ForgeProgress");
        this.isForging = tag.getInt("IsForging");
        this.lastQualityOrdinal = tag.getInt("LastQuality");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("TemperingPos", this.temperingPos);
        tag.putInt("ForgeProgress", this.forgeProgress);
        tag.putInt("IsForging", this.isForging);
        tag.putInt("LastQuality", this.lastQualityOrdinal);
    }
}
