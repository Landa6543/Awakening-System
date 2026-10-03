package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GachaMachineBlock extends Block {
    public static final MapCodec<GachaMachineBlock> CODEC = simpleCodec(GachaMachineBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape MIDDLE_PART = Block.box(4, 0, 1, 12, 16, 15);
    private static final VoxelShape LEFT_MIDDLE_PART = Block.box(12, 0, 2, 14, 16, 14);
    private static final VoxelShape LEFT_PART = Block.box(14, 0, 4, 15, 16, 12);
    private static final VoxelShape RIGHT_MIDDLE_PART = Block.box(2, 0, 2, 4, 16, 14);
    private static final VoxelShape RIGHT_PART = Block.box(1, 0, 4, 2, 16, 12);

    private static final VoxelShape UP_MIDDLE_PART = Block.box(0.5, 0, 0.5, 15.5, 1, 15.5);
    private static final VoxelShape UP_LOWER_PART = Block.box(0, 1, 0, 16, 15, 16);
    private static final VoxelShape UP_UPPER_PART = Block.box(0.5, 15, 0.5, 15.5, 16, 15.5);

    private static final VoxelShape LOWER_SHAPE = Shapes.or(MIDDLE_PART, LEFT_MIDDLE_PART, LEFT_PART, RIGHT_MIDDLE_PART, RIGHT_PART);
    private static final VoxelShape UPPER_SHAPE = Shapes.or(UP_UPPER_PART, UP_MIDDLE_PART, UP_LOWER_PART);

    public static final int SPIN_COST_MORA = 5000;

    public GachaMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return handleInteraction(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemInteractionResult result = handleInteraction(state, level, pos, player);
        return result == ItemInteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private ItemInteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this)) {
                return handleInteraction(lowerState, level, lowerPos, player);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);

            if (data.mora() >= SPIN_COST_MORA) {
                data.spendMora(SPIN_COST_MORA);
                com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);

                // Sonido de inserción de moneda
                level.playSound(null, pos, ModSounds.CASINO_INSERT_COIN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 15, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 10, 0.2D, 0.2D, 0.2D, 0.05D);
                }

                sp.sendSystemMessage(Component.literal("§e🪙 ¡5,000 Mora insertadas! Girando el mecanismo del Gachapon..."), true);

                // Programar dispensación en 20 ticks (1 segundo)
                level.scheduleTick(pos, this, 20);
                return ItemInteractionResult.SUCCESS;
            } else {
                sp.sendSystemMessage(Component.literal("§cSaldo insuficiente. Necesitas §65,000 Mora §cpara girar el Gachapon de Teyvat."), true);
                return ItemInteractionResult.CONSUME;
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return;

        Direction facing = state.getValue(FACING);
        double spawnX = pos.getX() + 0.5D + (facing.getStepX() * 0.65D);
        double spawnY = pos.getY() + 0.3D;
        double spawnZ = pos.getZ() + 0.5D + (facing.getStepZ() * 0.65D);

        ItemStack capsuleStack = new ItemStack(SystemItems.GACHA_CAPSULE.get());
        ItemEntity capsuleEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, capsuleStack);
        capsuleEntity.setDeltaMovement(new Vec3(facing.getStepX() * 0.15D, 0.1D, facing.getStepZ() * 0.15D));
        level.addFreshEntity(capsuleEntity);

        // Sonido de premio y partículas
        level.playSound(null, pos, ModSounds.GACHA_UNCOMMON_PRIZE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.FIREWORK, spawnX, spawnY + 0.2D, spawnZ, 15, 0.2D, 0.2D, 0.2D, 0.05D);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spawnX, spawnY + 0.2D, spawnZ, 10, 0.2D, 0.2D, 0.2D, 0.05D);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.is(this) && otherState.getValue(HALF) != half) {
            level.destroyBlock(otherPos, !player.isCreative());
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP && (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER)) {
                return Blocks.AIR.defaultBlockState();
            }
            if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN && (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }
}
