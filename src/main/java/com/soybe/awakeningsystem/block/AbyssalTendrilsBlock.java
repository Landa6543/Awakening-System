package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Abyssal creeping tendrils that adhere to surfaces on all 6 faces (floor, walls, ceiling)
 * just like Sculk Veins and Glow Lichen.
 */
public class AbyssalTendrilsBlock extends GlowLichenBlock {

    public AbyssalTendrilsBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.getItemInHand().is(this.asItem()) || hasVacantFace(state);
    }

    private static boolean hasVacantFace(BlockState state) {
        for (Direction dir : DIRECTIONS) {
            if (!hasFace(state, dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0D, 0.02D, 0.0D);
        }
    }
}
