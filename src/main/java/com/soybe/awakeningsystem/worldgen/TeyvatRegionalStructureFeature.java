package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.world.TeyvatRegionalBuilder;
import com.soybe.awakeningsystem.world.TeyvatStructureManager;
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
 * Generación nativa de Estructuras Regionales, Templos y Arenas de Jefes en Teyvat.
 * Generado de forma 100% nativa en el chunk pipeline de Minecraft (sin pop-in al caminar).
 */
public class TeyvatRegionalStructureFeature extends Feature<NoneFeatureConfiguration> {

    public TeyvatRegionalStructureFeature(Codec<NoneFeatureConfiguration> codec) {
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

        if (!TeyvatWorldGenManager.isAreaClearOfBossesAndStructures(level, origin, 220.0D)) {
            return false;
        }

        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                if (!TeyvatWorldGenManager.isFarFromList2D(origin, data.regionalStructures, 350.0D)) {
                    return false;
                }
            }
        } catch (Exception ignored) {}

        String biomeName = level.getBiome(origin).unwrapKey().map(k -> k.location().getPath()).orElse("");

        int roll = rand.nextInt(100);

        if (roll < 20) {
            boolean dungeonPlaced = TeyvatWorldGenManager.generateLegendaryDungeon(level, origin, biomeName, rand);
            if (dungeonPlaced) return true;
        }

        if (roll < 45) {
            boolean arenaPlaced = TeyvatWorldGenManager.generateHypostasisArena(level, origin, biomeName, rand);
            if (arenaPlaced) return true;
        }

        if (roll < 75) {
            boolean puzzlePlaced = TeyvatWorldGenManager.generateElementalMonumentPuzzle(level, origin, biomeName, rand);
            if (puzzlePlaced) return true;
        }

        if (biomeName.contains("karst") || biomeName.contains("mountain")) {
            TeyvatWorldGenManager.generateJueyunKarstPillars(level, origin, rand);
            recordRegionalStructure(level, origin);
            return true;
        }

        if (biomeName.contains("windrise") || biomeName.contains("plains") || biomeName.contains("marsh")) {
            if (TeyvatWorldGenManager.isAreaFlat(level, origin, 7, 2)) {
                TeyvatWorldGenManager.buildSolidFoundation(level, origin, 6);
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, origin, "teyvat_plains_shrine", -6, 0, -6);
                if (placed) {
                    TeyvatWorldGenManager.spawnTeyvatMonster(level, origin.offset(0, 2, 7), rand.nextBoolean() ? ModEntities.RUIN_HUNTER.get() : ModEntities.RUIN_GUARD.get());
                    recordRegionalStructure(level, origin);
                    return true;
                }
            }
        } else if (biomeName.contains("desert")) {
            if (TeyvatWorldGenManager.isAreaFlat(level, origin, 7, 2)) {
                TeyvatWorldGenManager.buildSolidFoundation(level, origin, 6);
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, origin, "teyvat_desert_shrine", -6, 0, -6);
                if (placed) {
                    TeyvatWorldGenManager.spawnTeyvatMonster(level, origin.offset(0, 2, 7), rand.nextBoolean() ? ModEntities.RUIN_HUNTER.get() : ModEntities.RUIN_GUARD.get());
                    recordRegionalStructure(level, origin);
                    return true;
                }
            }
        }

        return false;
    }

    private void recordRegionalStructure(WorldGenLevel level, BlockPos pos) {
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                data.regionalStructures.add(pos);
                data.setDirty();
            }
        } catch (Exception ignored) {}
    }
}
