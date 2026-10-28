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

        if (!TeyvatWorldGenManager.isAreaClearOfBossesAndStructures(level, origin, 120.0D)) {
            return false;
        }

        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                if (!TeyvatWorldGenManager.isFarFromList2D(origin, data.waypoints, 240.0D)) {
                    return false;
                }
            }
        } catch (Exception ignored) {}

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
