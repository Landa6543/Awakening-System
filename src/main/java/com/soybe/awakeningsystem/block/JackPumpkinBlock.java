package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * La Calabaza de Jack (Stingy Jack's Soul Pumpkin Block):
 * Calabaza espectral petrificada que arde con fuego perpetuo de almas del Limbo.
 * Emite nivel máximo de luz (15) disipando la oscuridad del purgatorio.
 */
public class JackPumpkinBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<JackPumpkinBlock> CODEC = simpleCodec(JackPumpkinBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Voxel shape: 14x14 pumpkin base up to the forged iron ring
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 22.0D, 15.0D);

    public JackPumpkinBlock(Properties properties) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.35D;
        double z = pos.getZ() + 0.5D;

        // Partículas de fuego de alma brotando de la boca y ojos
        double offsetX = direction.getStepX() * 0.42D;
        double offsetZ = direction.getStepZ() * 0.42D;

        if (random.nextFloat() < 0.65F) {
            level.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x + offsetX + (random.nextDouble() - 0.5D) * 0.25D,
                    y + (random.nextDouble() - 0.5D) * 0.15D,
                    z + offsetZ + (random.nextDouble() - 0.5D) * 0.25D,
                    0.0D, 0.02D, 0.0D
            );
        }

        // Humo y destellos de vacío
        if (random.nextFloat() < 0.35F) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    x + (random.nextDouble() - 0.5D) * 0.2D,
                    pos.getY() + 1.15D,
                    z + (random.nextDouble() - 0.5D) * 0.2D,
                    0.0D, 0.03D, 0.0D
            );
        }
    }
}
