package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.world.TeyvatRegionalBuilder;
import com.soybe.awakeningsystem.world.TeyvatWorldGenManager;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generación nativa de Santuarios de las Profundidades (Shrines of Depths) de Teyvat.
 * Se genera con cúpula arquitectónica canónica, barrera elemental y Cofre Lujoso con 40 Protogemas y recompensas.
 */
public class TeyvatShrineOfDepthsFeature extends Feature<NoneFeatureConfiguration> {

    public TeyvatShrineOfDepthsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        if (origin.getY() < 60 || origin.getY() > 185) return false;
        BlockState below = level.getBlockState(origin.below());
        if (!below.isSolid() || below.liquid() || below.is(BlockTags.LEAVES)) return false;

        // 2. No generar pegado al spawn ni a las capitales
        if (Math.abs(origin.getX()) < 160 && Math.abs(origin.getZ()) < 160) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.MONDSTADT_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.MONDSTADT_POS.getZ()) < 180) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.LIYUE_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.LIYUE_POS.getZ()) < 180) return false;

        // 3. Control estricto de distancia canónica (320m mínimo entre santuarios)
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                if (!TeyvatWorldGenManager.isFarFromList(origin, data.shrinesOfDepths, 320.0D)) {
                    return false;
                }
            }
        } catch (Exception ignored) {}

        String biomeName = level.getBiome(origin).unwrapKey().map(k -> k.location().getPath()).orElse("");

        boolean placed = TeyvatWorldGenManager.generateShrineOfDepths(level, origin, biomeName, rand);
        if (placed) {
            try {
                if (level.getLevel().getServer() != null) {
                    var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                    data.shrinesOfDepths.add(origin);
                    data.setDirty();
                }
            } catch (Exception ignored) {}
        }
        return placed;
    }
}
