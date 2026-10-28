package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SoulAlloyOreBlock extends DropExperienceBlock {
    public SoulAlloyOreBlock(IntProvider xpRange, Properties properties) {
        super(xpRange, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.02D, 0.0D);
        }
        if (random.nextInt(20) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
            double y = pos.getY() + 0.9D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
            level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0.0D, 0.03D, 0.0D);
        }
        if (random.nextInt(40) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 0.35F, 1.2F, false);
        }
    }
}
