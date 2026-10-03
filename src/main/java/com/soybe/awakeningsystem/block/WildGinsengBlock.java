package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Planta mística de Qingseng (Ginseng Milenario Silvestre) que crece en las islas celestiales de Murim.
 * Al recolectarse, otorga el Ginseng Milenario consumible para cultivo de Qi.
 */
public class WildGinsengBlock extends BushBlock {
    public static final com.mojang.serialization.MapCodec<WildGinsengBlock> CODEC = simpleCodec(WildGinsengBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public WildGinsengBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double px = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double py = pos.getY() + 0.3D + random.nextDouble() * 0.6D;
            double pz = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.015D, 0.0D);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0.0D, 0.02D, 0.0D);
            }
        }
    }
}
