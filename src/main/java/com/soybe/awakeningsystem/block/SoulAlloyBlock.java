package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SoulAlloyBlock extends Block {
    public SoulAlloyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.015D, 0.0D);
        }
        if (random.nextInt(28) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
            double y = pos.getY() + 0.95D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
            level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0.0D, 0.025D, 0.0D);
        }
    }
}
