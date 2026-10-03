package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/**
 * Hojas frutales interactivas (Árboles de Cuihua y Madera de Maná).
 * Permite cosechar frutas maduras (Sunsettias o Frutas de Maná) haciendo clic derecho.
 * Admite polvo de hueso para madurar la fruta al instante y maduración natural con luz solar.
 */
public class FruitLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final MapCodec<FruitLeavesBlock> CODEC = simpleCodec(props -> new FruitLeavesBlock(props, () -> Items.AIR));
    public static final BooleanProperty HAS_FRUIT = BooleanProperty.create("has_fruit");

    private final Supplier<Item> fruitSupplier;

    public FruitLeavesBlock(BlockBehaviour.Properties properties, Supplier<Item> fruitSupplier) {
        super(properties);
        this.fruitSupplier = fruitSupplier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, 7)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false)
                .setValue(HAS_FRUIT, false));
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_FRUIT);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(HAS_FRUIT);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(HAS_FRUIT, true), 3);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        // Si no tiene fruta y recibe suficiente luz, madura fruta gradualmente
        if (!state.getValue(HAS_FRUIT) && level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            if (random.nextInt(6) == 0) {
                level.setBlock(pos, state.setValue(HAS_FRUIT, true), 3);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BONE_MEAL)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (state.getValue(HAS_FRUIT)) {
            harvestFruit(level, pos, state, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(HAS_FRUIT)) {
            harvestFruit(level, pos, state, player);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (state.getValue(HAS_FRUIT)) {
            Item fruit = fruitSupplier.get();
            if (fruit != null && fruit != Items.AIR) {
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        level, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D,
                        new ItemStack(fruit, 1 + level.random.nextInt(2))
                );
                itemEntity.setDeltaMovement(0.0D, 0.12D, 0.0D);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }

    private void harvestFruit(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            Item fruit = fruitSupplier.get();
            if (fruit != null && fruit != Items.AIR) {
                int count = 1 + level.random.nextInt(2); // 1 a 2 frutas por recolección
                ItemStack fruitStack = new ItemStack(fruit, count);
                if (player == null || !player.addItem(fruitStack)) {
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            level, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, fruitStack
                    );
                    itemEntity.setDeltaMovement(0.0D, 0.15D, 0.0D);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.85F + level.random.nextFloat() * 0.3F);
            level.setBlock(pos, state.setValue(HAS_FRUIT, false), 3);
        }
    }
}
