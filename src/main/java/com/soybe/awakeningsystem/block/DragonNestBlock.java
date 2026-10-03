package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.block.entity.DragonNestBlockEntity;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonNestBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<DragonNestBlock> CODEC = simpleCodec(DragonNestBlock::new);
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 0, 2);
    protected static final VoxelShape SHAPE = Block.box(-3.0D, 0.0D, -3.0D, 19.0D, 10.0D, 19.0D);

    public DragonNestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EGGS, 0));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EGGS);
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
        return new DragonNestBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int eggVariant = getEggVariantFromItem(stack);
        if (eggVariant >= 0 && level.getBlockEntity(pos) instanceof DragonNestBlockEntity be) {
            if (be.getEggCount() < 2) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                be.addEgg(eggVariant);
                level.setBlock(pos, state.setValue(EGGS, be.getEggCount()), 3);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.2F, 0.85F);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof DragonNestBlockEntity be && be.getEggCount() > 0) {
            if (!level.isClientSide) {
                int variant = be.removeEgg();
                if (variant >= 0) {
                    ItemStack eggStack = getEggItemForVariant(variant);
                    if (!player.getInventory().add(eggStack)) {
                        player.drop(eggStack, false);
                    }
                    level.setBlock(pos, state.setValue(EGGS, be.getEggCount()), 3);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 0.9F);

                    // ACTIVACIÓN DE LA FURIA DRACÓNICA ESTILO ARK
                    triggerDragonEggTheftAlert(level, pos, player);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DragonNestBlockEntity be) {
                if (be.getEggCount() >= 1) {
                    ItemStack egg1 = getEggItemForVariant(be.getEggVariant1());
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), egg1);
                }
                if (be.getEggCount() >= 2) {
                    ItemStack egg2 = getEggItemForVariant(be.getEggVariant2());
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), egg2);
                }
                if (be.getEggCount() > 0) {
                    triggerDragonEggTheftAlert(level, pos, null);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    public static void triggerDragonEggTheftAlert(Level level, BlockPos pos, @Nullable Player thief) {
        if (!(level instanceof ServerLevel sl)) return;

        // Radio de furia dracónica: 96 bloques
        double range = 96.0D;
        AABB searchBox = new AABB(pos).inflate(range, 48.0D, range);
        List<AncientDragonEntity> dragons = level.getEntitiesOfClass(AncientDragonEntity.class, searchBox,
                dragon -> !dragon.isTame() && dragon.isAlive());

        if (!dragons.isEmpty()) {
            for (AncientDragonEntity dragon : dragons) {
                dragon.triggerRoar(40);
                dragon.setFlying(true);
                if (thief != null) {
                    dragon.setEnragedThief(thief);
                    dragon.setTarget(thief);
                }
            }

            if (thief != null) {
                thief.displayClientMessage(Component.translatable("message.awakening_system.dragon_egg_theft"), false);
                sl.playSound(null, thief.blockPosition(), ModSounds.ANCIENT_DRAGON_ROAR.get(), SoundSource.HOSTILE, 2.5F, 0.95F);
            }
        }
    }

    public static int getEggVariantFromItem(ItemStack stack) {
        if (stack.is(SystemItems.ANCIENT_DRAGON_EGG_FIRE.get())) return AncientDragonEntity.VARIANT_FIRE;
        if (stack.is(SystemItems.ANCIENT_DRAGON_EGG_ICE.get())) return AncientDragonEntity.VARIANT_ICE;
        if (stack.is(SystemItems.ANCIENT_DRAGON_EGG_FOREST.get())) return AncientDragonEntity.VARIANT_FOREST;
        if (stack.is(SystemItems.ANCIENT_DRAGON_EGG_SHADOW.get())) return AncientDragonEntity.VARIANT_SHADOW;
        if (stack.is(SystemItems.ANCIENT_DRAGON_EGG_LIGHTNING.get())) return AncientDragonEntity.VARIANT_LIGHTNING;
        if (stack.is(SystemItems.ANCIENT_DRAGON_EGG_CELESTIAL.get())) return AncientDragonEntity.VARIANT_CELESTIAL;
        return -1;
    }

    public static ItemStack getEggItemForVariant(int variant) {
        return switch (variant) {
            case AncientDragonEntity.VARIANT_ICE -> new ItemStack(SystemItems.ANCIENT_DRAGON_EGG_ICE.get());
            case AncientDragonEntity.VARIANT_FOREST -> new ItemStack(SystemItems.ANCIENT_DRAGON_EGG_FOREST.get());
            case AncientDragonEntity.VARIANT_SHADOW -> new ItemStack(SystemItems.ANCIENT_DRAGON_EGG_SHADOW.get());
            case AncientDragonEntity.VARIANT_LIGHTNING -> new ItemStack(SystemItems.ANCIENT_DRAGON_EGG_LIGHTNING.get());
            case AncientDragonEntity.VARIANT_CELESTIAL -> new ItemStack(SystemItems.ANCIENT_DRAGON_EGG_CELESTIAL.get());
            default -> new ItemStack(SystemItems.ANCIENT_DRAGON_EGG_FIRE.get());
        };
    }
}
