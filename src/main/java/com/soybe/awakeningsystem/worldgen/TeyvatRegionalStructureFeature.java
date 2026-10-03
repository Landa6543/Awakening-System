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

        // No generar pegado al spawn ni a las capitales
        if (Math.abs(origin.getX()) < 160 && Math.abs(origin.getZ()) < 160) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.MONDSTADT_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.MONDSTADT_POS.getZ()) < 180) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.LIYUE_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.LIYUE_POS.getZ()) < 180) return false;

        String biomeName = level.getBiome(origin).unwrapKey().map(k -> k.location().getPath()).orElse("");

        int roll = rand.nextInt(100);

        // 1. Palacios y Mazmorras Legendarias (Tenshukaku, Baal Realm, Golden House, Azhdaha, Primal Temple, Wolflord, Tumor Tree, Floating Island - 500-650m mín)
        if (roll < 20) {
            boolean dungeonPlaced = TeyvatWorldGenManager.generateLegendaryDungeon(level, origin, biomeName, rand);
            if (dungeonPlaced) return true;
        }

        // 2. Arenas de Hipóstasis Elementales y Cuevas de Regisvines (420m mín)
        if (roll < 45) {
            boolean arenaPlaced = TeyvatWorldGenManager.generateHypostasisArena(level, origin, biomeName, rand);
            if (arenaPlaced) return true;
        }

        // 3. 7 Puzzles de Monumentos Elementales Interactivos
        if (roll < 70) {
            boolean puzzlePlaced = TeyvatWorldGenManager.generateElementalMonumentPuzzle(level, origin, biomeName, rand);
            if (puzzlePlaced) return true;
        }

        // 4. Estructuras arquitectónicas regionales de cada nación
        if (biomeName.contains("narukami") || biomeName.contains("watatsumi") || biomeName.contains("yashiori")) {
            TeyvatRegionalBuilder.buildNarukamiGrandShrine(level.getLevel(), origin);
            return true;
        } else if (biomeName.contains("desert")) {
            TeyvatRegionalBuilder.buildDesertPyramidAltar(level.getLevel(), origin);
            return true;
        } else if (biomeName.contains("dragonspine")) {
            TeyvatRegionalBuilder.buildDragonspineFrostAltar(level.getLevel(), origin);
            return true;
        } else if (biomeName.contains("dharma")) {
            TeyvatRegionalBuilder.buildAranaraTreehouse(level.getLevel(), origin);
            return true;
        } else if (biomeName.contains("fontaine")) {
            TeyvatRegionalBuilder.buildFontaineGazebo(level.getLevel(), origin);
            return true;
        } else if (biomeName.contains("karst") || biomeName.contains("mountain")) {
            TeyvatWorldGenManager.generateJueyunKarstPillars(level, origin, rand);
            return true;
        } else if (biomeName.contains("windrise") || biomeName.contains("plains") || biomeName.contains("marsh")) {
            if (TeyvatWorldGenManager.isAreaFlat(level, origin, 7, 2)) {
                TeyvatWorldGenManager.buildSolidFoundation(level, origin, 8);
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, origin, "teyvat_plains_shrine", -6, 0, -6);
                if (placed) {
                    TeyvatWorldGenManager.spawnTeyvatMonster(level, origin.offset(0, 2, 7), rand.nextBoolean() ? ModEntities.RUIN_HUNTER.get() : ModEntities.RUIN_GUARD.get());
                    return true;
                }
            }
        }

        return false;
    }
}
