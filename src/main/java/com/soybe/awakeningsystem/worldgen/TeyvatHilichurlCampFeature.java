package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.AwakeningSystemMod;
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
 * Generación nativa de Campamentos y Aldeas Hilichurl de Teyvat estilo Vainilla.
 * Soporta las 34 variantes canónicas de aldeas NBT (Plains, Desert, Badlands),
 * integradas al relieve con chozas, torres de vigía, hogueras, vasijas, cofres y tribus de Hilichurls/Mitachurls.
 */
public class TeyvatHilichurlCampFeature extends Feature<NoneFeatureConfiguration> {

    private static final String[] PLAINS_VILLAGES = {
            "hilichurlvillage1", "hilichurlvillage1_final", "hilichurlvillage1_final_fixed",
            "hilichurlvillage2", "hilichurlvillage2_final", "hilichurlvillage3", "hilichurlvillage3_new",
            "hilichurlvillage4", "hilichurlvillage5", "hilichurlvillage6", "hilichurlvillage7",
            "hilichurlvillage8", "hilichurlvillage8_final"
    };

    private static final String[] DESERT_VILLAGES = {
            "hilichurl_desert_1", "hilichurl_desert_2", "hilichurl_desert_2_new",
            "hilichurl_desert_3", "hilichurl_desert_3_final", "hilichurl_desert_4",
            "hilichurl_desert_5", "hilichurl_desert_6", "hilichurl_desert_6_final",
            "hilichurl_desert_7", "hilichurl_desert_8"
    };

    private static final String[] BADLANDS_VILLAGES = {
            "hilichurl_badlands_1", "hilichurl_badlands_2", "hilichurl_badlands_3", "hilichurl_badlands_3_new",
            "hilichurl_badlands_4", "hilichurl_badlands_4_final", "hilichurl_badlands_5",
            "hilichurl_badlands_6", "hilichurl_badlands_7", "hilichurl_badlands_8"
    };

    public TeyvatHilichurlCampFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        // 1. Evitar generar sobre agua, vacío o fuera de rango de superficie
        if (origin.getY() < 60 || origin.getY() > 185) return false;
        BlockState below = level.getBlockState(origin.below());
        if (!below.isSolid() || below.liquid() || below.is(BlockTags.LEAVES)) return false;

        // 2. No generar pegado al spawn (0,0) ni a las capitales de Teyvat
        if (Math.abs(origin.getX()) < 160 && Math.abs(origin.getZ()) < 160) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.MONDSTADT_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.MONDSTADT_POS.getZ()) < 180) return false;
        if (Math.abs(origin.getX() - TeyvatRegionalBuilder.LIYUE_POS.getX()) < 180 && Math.abs(origin.getZ() - TeyvatRegionalBuilder.LIYUE_POS.getZ()) < 180) return false;

        String biomeName = level.getBiome(origin).unwrapKey().map(k -> k.location().getPath()).orElse("");

        // 3. Selección y colocación de Aldeas Hilichurl NBT canónicas
        boolean villagePlaced = false;
        if (TeyvatWorldGenManager.isAreaFlat(level, origin, 8, 3)) {
            String selectedNbt;
            if (biomeName.contains("desert")) {
                selectedNbt = DESERT_VILLAGES[rand.nextInt(DESERT_VILLAGES.length)];
            } else if (biomeName.contains("badlands") || biomeName.contains("karst") || biomeName.contains("mountain")) {
                selectedNbt = BADLANDS_VILLAGES[rand.nextInt(BADLANDS_VILLAGES.length)];
            } else {
                selectedNbt = PLAINS_VILLAGES[rand.nextInt(PLAINS_VILLAGES.length)];
            }

            TeyvatWorldGenManager.buildSolidFoundation(level, origin, 10);
            villagePlaced = TeyvatStructureManager.placeNbtStructure(level, origin, selectedNbt, -8, 0, -8);
            if (villagePlaced) {
                AwakeningSystemMod.LOGGER.info("Aldea Hilichurl canónica '{}' generada nativamente en {}", selectedNbt, origin);

                // Spawneo de patrulla de la tribu Hilichurl
                int hiliCount = 3 + rand.nextInt(3);
                for (int i = 0; i < hiliCount; i++) {
                    int ox = (rand.nextInt(9) - 4);
                    int oz = (rand.nextInt(9) - 4);
                    TeyvatWorldGenManager.spawnTeyvatMonster(level, origin.offset(ox, 1, oz), ModEntities.HILICHURL.get());
                }
                // Líder Mitachurl
                TeyvatWorldGenManager.spawnTeyvatMonster(level, origin.offset(0, 1, 2), ModEntities.MITACHURL.get());
            }
        }

        // 4. Si el terreno era demasiado accidentado para una aldea grande completa, fallback al campamento procedural adaptativo
        if (!villagePlaced) {
            if (!TeyvatWorldGenManager.isAreaFlat(level, origin, 5, 2)) {
                return false;
            }
            TeyvatWorldGenManager.generateHilichurlCamp(level, origin, rand);
        }

        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getLevel().getServer());
                data.camps.add(origin);
                data.setDirty();
            }
        } catch (Exception ignored) {}

        return true;
    }
}
