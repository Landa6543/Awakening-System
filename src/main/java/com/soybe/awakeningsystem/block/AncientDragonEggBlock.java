package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.block.entity.AncientDragonEggBlockEntity;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AncientDragonEggBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<AncientDragonEggBlock> CODEC = simpleCodec(AncientDragonEggBlock::new);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 5);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 18.0D, 14.0D);

    public AncientDragonEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        AncientDragonEggBlockEntity be = new AncientDragonEggBlockEntity(pos, state);
        be.setVariant(state.getValue(VARIANT));
        return be;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ANCIENT_DRAGON_EGG.get(), AncientDragonEggBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof AncientDragonEggBlockEntity be) {
            be.setVariant(state.getValue(VARIANT));
            if (placer instanceof Player player) {
                be.setPlacer(player);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AncientDragonEggBlockEntity be) {
            int variant = be.getVariant();
            boolean isCatalyst = switch (variant) {
                case AncientDragonEntity.VARIANT_FIRE -> stack.is(Items.FIRE_CHARGE) || stack.is(Items.BLAZE_POWDER);
                case AncientDragonEntity.VARIANT_ICE -> stack.is(Items.PACKED_ICE) || stack.is(Items.BLUE_ICE);
                case AncientDragonEntity.VARIANT_FOREST -> stack.is(Items.EMERALD) || stack.is(Items.MOSS_BLOCK);
                case AncientDragonEntity.VARIANT_SHADOW -> stack.is(Items.ECHO_SHARD) || stack.is(Items.AMETHYST_SHARD);
                case AncientDragonEntity.VARIANT_LIGHTNING -> stack.is(Items.COPPER_INGOT) || stack.is(Items.LIGHTNING_ROD) || stack.is(Items.WIND_CHARGE);
                case AncientDragonEntity.VARIANT_CELESTIAL -> stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_BLOCK) || stack.is(Items.TOTEM_OF_UNDYING);
                default -> false;
            };

            if (isCatalyst) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                be.boostIncubation(300);
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.awakening_system.dragon_egg_boosted"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AncientDragonEggBlockEntity be) {
            if (!level.isClientSide) {
                int percent = (int) (((float) be.getIncubationTicks() / AncientDragonEggBlockEntity.MAX_INCUBATION_TICKS) * 100.0F);
                Component status = be.isComfortable()
                        ? Component.translatable("message.awakening_system.dragon_egg_status_healthy")
                        : Component.translatable("message.awakening_system.dragon_egg_status_cold");
                Component name = switch (be.getVariant()) {
                    case AncientDragonEntity.VARIANT_ICE -> Component.translatable("block.awakening_system.ancient_dragon_egg_ice");
                    case AncientDragonEntity.VARIANT_FOREST -> Component.translatable("block.awakening_system.ancient_dragon_egg_forest");
                    case AncientDragonEntity.VARIANT_SHADOW -> Component.translatable("block.awakening_system.ancient_dragon_egg_shadow");
                    case AncientDragonEntity.VARIANT_LIGHTNING -> Component.translatable("block.awakening_system.ancient_dragon_egg_lightning");
                    case AncientDragonEntity.VARIANT_CELESTIAL -> Component.translatable("block.awakening_system.ancient_dragon_egg_celestial");
                    default -> Component.translatable("block.awakening_system.ancient_dragon_egg_fire");
                };
                player.displayClientMessage(Component.translatable("message.awakening_system.dragon_egg_inspect", name, percent, status), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
