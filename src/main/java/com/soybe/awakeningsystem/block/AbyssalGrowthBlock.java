package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Living abyssal growth that covers the cavern floor and walls like Sculk.
 * Emits subtle abyssal void particles to create an eerie, animated cavern atmosphere.
 */
public class AbyssalGrowthBlock extends Block {

    public AbyssalGrowthBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.02D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.PORTAL, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.2D,
                    random.nextDouble() * 0.1D,
                    (random.nextDouble() - 0.5D) * 0.2D);
        }
    }
}
