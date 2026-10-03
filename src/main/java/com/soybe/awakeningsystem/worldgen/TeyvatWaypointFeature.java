package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.world.TeyvatRegionalBuilder;
import com.soybe.awakeningsystem.world.TeyvatWorldGenManager;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generación nativa de Punto de Teletransporte (Teleport Waypoint) de Teyvat.
 * Se genera con pedestal circular y punto celestial directamente durante la creación del chunk.
 */
public class TeyvatWaypointFeature extends Feature<NoneFeatureConfiguration> {

    public TeyvatWaypointFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        if (origin.getY() < 60 || origin.getY() > 185) return false;
        BlockState below = level.getBlockState(origin.below());
        if (!below.isSolid() || below.liquid() || below.is(BlockTags.LEAVES)) return false;

        if (Math.abs(origin.getX()) < 160 && Math.abs(origin.getZ()) < 160) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.MONDSTADT_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.MONDSTADT_POS.getZ()) < 180) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.LIYUE_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.LIYUE_POS.getZ()) < 180) return false;

        TeyvatWorldGenManager.generateTeleportWaypoint(level, origin);

        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                data.waypoints.add(origin);
                data.setDirty();
            }
        } catch (Exception ignored) {}

        return true;
    }
}
