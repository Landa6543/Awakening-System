package com.soybe.awakeningsystem.furnace;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CustomTierFurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 2);

    private final FurnaceTier tier;

    public CustomTierFurnaceBlock(BlockBehaviour.Properties properties, FurnaceTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(TYPE, 0));
    }

    public FurnaceTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof FurnaceUpgradeItem) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = itemKey.getPath();
        boolean isBlasting = path.equals("augment_blasting") || path.endsWith("_blasting");
        boolean isSmoking = path.equals("augment_smoking") || path.endsWith("_smoking");

        if (isBlasting || isSmoking) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomTierFurnaceBlockEntity furnaceBE) {
                if (!furnaceBE.getInstalledAugment().isEmpty()) {
                    ItemStack previous = furnaceBE.removeInstalledAugment();
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 1.0D, pos.getZ(), previous);
                }

                furnaceBE.setInstalledAugment(stack.copyWithCount(1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                int newType = isBlasting ? 2 : 1;
                level.setBlock(pos, state.setValue(TYPE, newType), 3);
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);

                Component msg = isBlasting
                        ? Component.translatable("awakening_system.furnace.upgraded_blasting")
                        : Component.translatable("awakening_system.furnace.upgraded_smoker");
                player.displayClientMessage(msg, true);
                return ItemInteractionResult.CONSUME;
            }
        }

        if (stack.isEmpty() && player.isCrouching()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomTierFurnaceBlockEntity furnaceBE && !furnaceBE.getInstalledAugment().isEmpty()) {
                if (!level.isClientSide()) {
                    ItemStack removed = furnaceBE.removeInstalledAugment();
                    if (!player.getInventory().add(removed)) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY() + 1.0D, pos.getZ(), removed);
                    }
                    level.setBlock(pos, state.setValue(TYPE, 0), 3);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
                    player.displayClientMessage(Component.translatable("awakening_system.furnace.upgrade_removed"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CustomTierFurnaceBlockEntity furnaceBE) {
            player.openMenu(furnaceBE);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isCrouching()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomTierFurnaceBlockEntity furnaceBE && !furnaceBE.getInstalledAugment().isEmpty()) {
                if (!level.isClientSide()) {
                    ItemStack removed = furnaceBE.removeInstalledAugment();
                    if (!player.getInventory().add(removed)) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY() + 1.0D, pos.getZ(), removed);
                    }
                    level.setBlock(pos, state.setValue(TYPE, 0), 3);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
                    player.displayClientMessage(Component.translatable("awakening_system.furnace.upgrade_removed"), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CustomTierFurnaceBlockEntity furnaceBE) {
                Containers.dropContents(level, pos, furnaceBE);
                if (!furnaceBE.getInstalledAugment().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), furnaceBE.getInstalledAugment());
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomTierFurnaceBlockEntity(pos, state, this.tier);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, getExpectedBlockEntityType(), CustomTierFurnaceBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<CustomTierFurnaceBlockEntity> getExpectedBlockEntityType() {
        return (BlockEntityType<CustomTierFurnaceBlockEntity>) (BlockEntityType<?>) switch (this.tier) {
            case ABYSSAL -> ModBlockEntities.ABYSSAL_FURNACE.get();
            case SOUL_ALLOY -> ModBlockEntities.SOUL_ALLOY_FURNACE.get();
            case ASTRALITE -> ModBlockEntities.ASTRALITE_FURNACE.get();
            case MITHRIL -> ModBlockEntities.MITHRIL_FURNACE.get();
            case ADAMANTITE -> ModBlockEntities.ADAMANTITE_FURNACE.get();
            case SPIRIT_JADE -> ModBlockEntities.SPIRIT_JADE_FURNACE.get();
            case ASTRAL -> ModBlockEntities.ASTRAL_FURNACE.get();
            case CELESTIAL -> ModBlockEntities.CELESTIAL_FURNACE.get();
            case PRIMORDIAL -> ModBlockEntities.PRIMORDIAL_FURNACE.get();
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;

        int type = state.getValue(TYPE);

        if (type == 1) {
            if (random.nextDouble() < 0.1D) {
                level.playLocalSound(x, y, z, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 1.1D, z, 0.0D, 0.07D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, x, y + 1.1D, z, 0.0D, 0.03D, 0.0D);
        } else if (type == 2) {
            if (random.nextDouble() < 0.1D) {
                level.playLocalSound(x, y, z, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        } else {
            if (random.nextDouble() < 0.1D) {
                level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }

        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double offset = random.nextDouble() * 0.6D - 0.3D;
        double dx = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52D : offset;
        double dy = random.nextDouble() * 6.0D / 16.0D;
        double dz = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52D : offset;

        level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.0D, 0.0D);

        if (type == 2) {
            level.addParticle(ParticleTypes.LAVA, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.0D, 0.0D);
        }

        switch (this.tier) {
            case SOUL_ALLOY -> {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.01D, 0.0D);
                level.addParticle(ParticleTypes.SCULK_SOUL, x + dx, y + dy + 0.1D, z + dz, 0.0D, 0.02D, 0.0D);
            }
            case ASTRALITE -> {
                level.addParticle(ParticleTypes.END_ROD, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.02D, 0.0D);
                level.addParticle(ParticleTypes.PORTAL, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.0D, 0.0D);
            }
            case MITHRIL -> level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.0D, 0.0D);
            case ADAMANTITE -> level.addParticle(ParticleTypes.FLAME, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.0D, 0.0D);
            case SPIRIT_JADE -> {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.02D, 0.0D);
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + dx, y + dy + 0.1D, z + dz, 0.0D, 0.0D, 0.0D);
            }
            case ASTRAL -> {
                level.addParticle(ParticleTypes.END_ROD, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.02D, 0.0D);
                level.addParticle(ParticleTypes.WITCH, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.01D, 0.0D);
            }
            case CELESTIAL -> {
                level.addParticle(ParticleTypes.END_ROD, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.03D, 0.0D);
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.02D, 0.0D);
            }
            case PRIMORDIAL -> {
                level.addParticle(ParticleTypes.DRAGON_BREATH, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.02D, 0.0D);
                level.addParticle(ParticleTypes.WITCH, x + dx, y + dy + 0.2D, z + dz, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
