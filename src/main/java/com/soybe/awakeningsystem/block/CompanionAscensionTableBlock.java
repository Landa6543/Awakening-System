package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.CompanionAscensionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CompanionAscensionTableBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CompanionAscensionTableBlock> CODEC = simpleCodec(CompanionAscensionTableBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // VoxelShape realista de mesa de trabajo 3D ornamental:
    // Tablero superior voladizo + 4 patas estilizadas + estante inferior con núcleo de maná
    protected static final VoxelShape TABLE_TOP = Block.box(0.0D, 11.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape LEG_NW = Block.box(1.0D, 0.0D, 1.0D, 4.0D, 11.0D, 4.0D);
    protected static final VoxelShape LEG_NE = Block.box(12.0D, 0.0D, 1.0D, 15.0D, 11.0D, 4.0D);
    protected static final VoxelShape LEG_SW = Block.box(1.0D, 0.0D, 12.0D, 4.0D, 11.0D, 15.0D);
    protected static final VoxelShape LEG_SE = Block.box(12.0D, 0.0D, 12.0D, 15.0D, 11.0D, 15.0D);
    protected static final VoxelShape SHELF = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 3.5D, 14.0D);
    protected static final VoxelShape SHAPE = Shapes.or(TABLE_TOP, LEG_NW, LEG_NE, LEG_SW, LEG_SE, SHELF);

    public CompanionAscensionTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            CompanionAscensionManager.openTableForPlayer(sp);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            CompanionAscensionManager.openTableForPlayer(sp);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 1.0D;
            double cz = pos.getZ() + 0.5D;

            // Partículas de runas arcanas flotando hacia el dial astrolábico
            level.addParticle(ParticleTypes.ENCHANT,
                    cx + (random.nextDouble() - 0.5D) * 0.8D,
                    cy + 0.2D + random.nextDouble() * 0.3D,
                    cz + (random.nextDouble() - 0.5D) * 0.8D,
                    (random.nextDouble() - 0.5D) * 0.2D,
                    random.nextDouble() * 0.1D + 0.05D,
                    (random.nextDouble() - 0.5D) * 0.2D
            );
        }

        if (random.nextInt(5) == 0) {
            // Chispas celestiales de End Rod sobre la estrella astrolábica
            double px = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double py = pos.getY() + 1.0D;
            double pz = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.015D, 0.0D);
        }

        if (random.nextInt(8) == 0) {
            // Brillo místico sutil en el orbe/relicario del estante inferior
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.35D;
            double cz = pos.getZ() + 0.5D;
            level.addParticle(ParticleTypes.GLOW,
                    cx + (random.nextDouble() - 0.5D) * 0.25D,
                    cy,
                    cz + (random.nextDouble() - 0.5D) * 0.25D,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
