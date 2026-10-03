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
 * Features de árboles nativos de Inazuma (Aralia, Otogi, Yumemiru, Arce de Inazuma / Maple).
 * Soportan generación tanto en biomas de Teyvat (islas de Inazuma) como en el Nether.
 */
public class TeyvatInazumaTreeFeatures {

    private static boolean isValidGround(WorldGenLevel level, BlockPos pos) {
        BlockState ground = level.getBlockState(pos.below());
        return ground.is(BlockTags.DIRT) || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MOSS_BLOCK)
                || ground.is(Blocks.PODZOL) || ground.is(Blocks.COARSE_DIRT) || ground.is(Blocks.STONE)
                || ground.is(Blocks.NETHERRACK) || ground.is(Blocks.CRIMSON_NYLIUM) || ground.is(Blocks.WARPED_NYLIUM)
                || ground.is(Blocks.SOUL_SOIL) || ground.is(Blocks.SOUL_SAND) || ground.is(Blocks.BASALT) || ground.is(Blocks.BLACKSTONE);
    }

    private static boolean canReplaceOrigin(WorldGenLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos) || level.getBlockState(pos).canBeReplaced();
    }

    public static class AraliaTreeFeature extends Feature<NoneFeatureConfiguration> {
        public AraliaTreeFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            if (!isValidGround(level, origin) || !canReplaceOrigin(level, origin)) return false;
            return TeyvatRegionalBuilder.generateAraliaTree(level, origin, context.random(), false);
        }
    }

    public static class OtogiTreeFeature extends Feature<NoneFeatureConfiguration> {
        public OtogiTreeFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            if (!isValidGround(level, origin) || !canReplaceOrigin(level, origin)) return false;
            return TeyvatRegionalBuilder.generateOtogiTree(level, origin, context.random(), false);
        }
    }

    public static class YumemiruTreeFeature extends Feature<NoneFeatureConfiguration> {
        public YumemiruTreeFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            if (!isValidGround(level, origin) || !canReplaceOrigin(level, origin)) return false;
            return TeyvatRegionalBuilder.generateYumemiruTree(level, origin, context.random(), false);
        }
    }

    public static class MapleTreeFeature extends Feature<NoneFeatureConfiguration> {
        public MapleTreeFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            if (!isValidGround(level, origin) || !canReplaceOrigin(level, origin)) return false;
            return TeyvatRegionalBuilder.generateMapleTree(level, origin, context.random(), false);
        }
    }
}
