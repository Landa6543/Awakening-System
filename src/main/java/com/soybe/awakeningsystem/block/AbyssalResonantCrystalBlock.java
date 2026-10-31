package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AbyssalResonantCrystalBlock extends Block {

    public AbyssalResonantCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && !entity.isSteppingCarefully() && level instanceof ServerLevel sl) {
            double speed = entity.getDeltaMovement().lengthSqr();
            if (speed > 0.005D && sl.random.nextInt(3) == 0) {
                float pitch = 0.65F + sl.random.nextFloat() * 0.5F;
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, pitch);
                sl.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 3, 0.25D, 0.05D, 0.25D, 0.02D);
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
