package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.world.TeyvatRegionalBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generación nativa del Árbol de Cuihua de Teyvat (Mondstadt / Liyue / Bishui).
 * Árbol frutal canónico con hojas de Fruta del Ocaso (Sunsettias cosechables).
 */
public class TeyvatCuihuaTreeFeature extends Feature<NoneFeatureConfiguration> {

    public TeyvatCuihuaTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        if (Math.abs(origin.getX() - 90) < 110 && Math.abs(origin.getZ() - (-140)) < 110) {
            return false;
        }

        BlockState ground = level.getBlockState(origin.below());
        if (!ground.is(BlockTags.DIRT) && !ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.MOSS_BLOCK)
                && !ground.is(Blocks.PODZOL) && !ground.is(Blocks.COARSE_DIRT)) {
            return false;
        }

        if (!level.isEmptyBlock(origin) && !level.getBlockState(origin).canBeReplaced()) {
            return false;
        }

        return TeyvatRegionalBuilder.generateCuihuaTree(level, origin, rand, false);
    }
}
