package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public final class SeriesStructureWorldGen {
    private SeriesStructureWorldGen() {}

    public static class SeriesStructureData extends SavedData {
        public static final String DATA_NAME = "awakening_series_structures";
        private final Map<String, BlockPos> structures = new HashMap<>();

        public SeriesStructureData() {}

        public static SeriesStructureData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(SeriesStructureData::new, SeriesStructureData::load, null),
                    DATA_NAME
            );
        }

        public static SeriesStructureData load(CompoundTag tag, HolderLookup.Provider registries) {
            SeriesStructureData data = new SeriesStructureData();
            if (tag.contains("Structures", Tag.TAG_LIST)) {
                ListTag list = tag.getList("Structures", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entry = list.getCompound(i);
                    String key = entry.getString("Key");
                    BlockPos pos = new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
                    data.structures.put(key, pos);
                }
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (Map.Entry<String, BlockPos> entry : structures.entrySet()) {
                CompoundTag ct = new CompoundTag();
                ct.putString("Key", entry.getKey());
                ct.putInt("X", entry.getValue().getX());
                ct.putInt("Y", entry.getValue().getY());
                ct.putInt("Z", entry.getValue().getZ());
                list.add(ct);
            }
            tag.put("Structures", list);
            return tag;
        }

        public boolean hasStructureNearby(BlockPos pos, double minDist) {
            double sq = minDist * minDist;
            for (BlockPos p : structures.values()) {
                if (p.distSqr(pos) < sq) return true;
            }
            return false;
        }

        public void recordStructure(String type, BlockPos pos) {
            structures.put(type + "_" + pos.getX() + "_" + pos.getZ(), pos);
            setDirty();
        }

        public Map<String, BlockPos> getAll() {
            return structures;
        }
    }

    public static int findSolidSurfaceGround(ServerLevel level, int blockX, int blockZ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
        int startY = Math.min(topY + 6, level.getMaxBuildHeight() - 5);
        int minY = Math.max(level.getMinBuildHeight() + 10, 45);

        // Descartar de inmediato si la superficie es agua o fluido (océanos, ríos, lagos)
        if (level.getFluidState(new BlockPos(blockX, topY, blockZ)).is(net.minecraft.world.level.material.Fluids.WATER)
                || level.getFluidState(new BlockPos(blockX, topY - 1, blockZ)).is(net.minecraft.world.level.material.Fluids.WATER)) {
            return -1;
        }

        for (int y = startY; y >= minY; y--) {
            BlockPos pos = new BlockPos(blockX, y, blockZ);
            BlockState state = level.getBlockState(pos);

            // Si hay agua o lava en la columna hacia abajo, no es tierra seca válida
            if (!state.getFluidState().isEmpty()) return -1;

            // Ignorar aire, hojas de árboles, troncos flotantes de copas, flores y reemplazables
            if (state.isAir()) continue;
            if (state.is(BlockTags.LEAVES)) continue;
            if (state.is(BlockTags.LOGS)) continue;
            if (state.is(BlockTags.FLOWERS)) continue;
            if (state.is(BlockTags.REPLACEABLE)) continue;

            // Terreno sólido natural verdadero
            if (state.isSolid() || state.is(BlockTags.DIRT) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                    || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.TERRACOTTA)
                    || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.RED_SAND) || state.is(Blocks.SANDSTONE)
                    || state.is(BlockTags.BASE_STONE_NETHER) || state.is(Blocks.SNOW_BLOCK)) {
                return y;
            }
        }
        return -1;
    }

    public static int findStableSolidGround(ServerLevel level, int blockX, int blockZ, int sampleRadius) {
        int centerGround = findSolidSurfaceGround(level, blockX, blockZ);
        if (centerGround < 50 || centerGround > 220) return -1;

        int r = Math.max(4, sampleRadius / 2);
        int g1 = findSolidSurfaceGround(level, blockX - r, blockZ);
        int g2 = findSolidSurfaceGround(level, blockX + r, blockZ);
        int g3 = findSolidSurfaceGround(level, blockX, blockZ - r);
        int g4 = findSolidSurfaceGround(level, blockX, blockZ + r);

        // Descartar si algún punto cae en agua profunda, océano o abismo
        if (g1 < 0 || g2 < 0 || g3 < 0 || g4 < 0) return -1;

        // Si la variación de altura es mayor a 14 bloques, es un precipicio demasiado abrupto
        int max = Math.max(centerGround, Math.max(Math.max(g1, g2), Math.max(g3, g4)));
        int min = Math.min(centerGround, Math.min(Math.min(g1, g2), Math.min(g3, g4)));
        if (max - min > 14) return -1;

        return centerGround;
    }

    public static void clearStructureVegetation(ServerLevel level, BlockPos center, int rx, int ry, int rz) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int x = cx - rx; x <= cx + rx; x++) {
            for (int z = cz - rz; z <= cz + rz; z++) {
                for (int y = cy; y <= cy + ry; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(BlockTags.FLOWERS)
                            || s.is(BlockTags.REPLACEABLE) || TeyvatStructureManager.isVegetationOrTreeBlock(s)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public static void generateWineryFoundation(ServerLevel level, BlockPos origin, int offsetX, int offsetY, int offsetZ, int sizeX, int sizeZ) {
        BlockPos corner = origin.offset(offsetX, offsetY, offsetZ);
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                BlockPos floorPos = corner.offset(x, 0, z);
                BlockState floorState = level.getBlockState(floorPos);
                if (!floorState.isAir() && floorState.isSolid()) {
                    for (int dy = 1; dy <= 6; dy++) {
                        BlockPos below = floorPos.below(dy);
                        BlockState belowState = level.getBlockState(below);
                        if (belowState.isAir() || belowState.is(BlockTags.REPLACEABLE) || belowState.is(BlockTags.LEAVES) || belowState.liquid()) {
                            BlockState foundationBlock = ((x + z + dy) % 3 == 0) ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                            level.setBlock(below, foundationBlock, 2);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public static boolean generateStructure(ServerLevel level, BlockPos pos, String structureType) {
        String type = structureType.toLowerCase(java.util.Locale.ROOT);
        switch (type) {
            case "solo_leveling", "solo", "jinwoo", "hunter" -> {
                clearStructureVegetation(level, pos, 9, 12, 9);
                SeriesStructureBuilder.buildHunterOffice(level, pos);
                recordPlaced(level, "solo_leveling", pos);
                return true;
            }
            case "orv", "dokja", "subway", "metro" -> {
                // Estructura SUBTERRÁNEA: si se invoca con altura de superficie (>45), reubicar bajo tierra
                int actualY = pos.getY() > 45 ? Math.min(25, pos.getY() - 35) : pos.getY();
                if (actualY < 8) actualY = 15;
                BlockPos subwayPos = new BlockPos(pos.getX(), actualY, pos.getZ());
                SeriesStructureBuilder.buildSubwayCarriage(level, subwayPos);
                recordPlaced(level, "orv", subwayPos);
                return true;
            }
            case "estate", "lloyd", "frontera", "engineering" -> {
                clearStructureVegetation(level, pos, 14, 20, 14);
                SeriesStructureBuilder.buildFronteraEstate(level, pos);
                recordPlaced(level, "estate", pos);
                return true;
            }
            case "nano", "nano_machine", "cult", "demon" -> {
                clearStructureVegetation(level, pos, 11, 30, 11);
                SeriesStructureBuilder.buildDemonPagoda(level, pos);
                recordPlaced(level, "nano", pos);
                return true;
            }
            case "tbate", "arthur", "aether" -> {
                clearStructureVegetation(level, pos, 11, 16, 11);
                SeriesStructureBuilder.buildAetherSanctuary(level, pos);
                recordPlaced(level, "tbate", pos);
                return true;
            }
            case "ranker", "second_life", "slr", "cain", "yeonwoo", "tower", "obelisk" -> {
                clearStructureVegetation(level, pos, 12, 40, 12);
                SeriesStructureBuilder.buildRankerTower(level, pos);
                recordPlaced(level, "ranker", pos);
                return true;
            }
            case "mount_hua", "chung_myung", "plum", "hua", "mount_hua_pavilion" -> {
                clearStructureVegetation(level, pos, 11, 18, 11);
                SeriesStructureBuilder.buildMountHuaPavilion(level, pos);
                recordPlaced(level, "mount_hua", pos);
                return true;
            }
            case "valter", "hanbin", "barbarian", "rock_dimension", "valter_camp", "sword_king" -> {
                clearStructureVegetation(level, pos, 12, 14, 12);
                SeriesStructureBuilder.buildValterCamp(level, pos);
                recordPlaced(level, "valter", pos);
                return true;
            }
            case "tower_of_god", "baam", "shinsoo", "floor_of_tests", "tog", "shinsoo_shrine" -> {
                clearStructureVegetation(level, pos, 12, 18, 12);
                SeriesStructureBuilder.buildShinsooShrine(level, pos);
                recordPlaced(level, "tower_of_god", pos);
                return true;
            }
            case "ascension_pavilion", "murim_gate", "murim_pavilion", "ascension", "celestial_pavilion" -> {
                clearStructureVegetation(level, pos, 10, 16, 10);
                SeriesStructureBuilder.buildAscensionPavilion(level, pos);
                recordPlaced(level, "ascension_pavilion", pos);
                return true;
            }
            case "dragon_sanctuary", "dragon_altar", "altar_dragon", "santuario_dragon", "dragon_boss_sanctuary" -> {
                clearStructureVegetation(level, pos, 13, 20, 13);
                SeriesStructureBuilder.buildDragonSanctuary(level, pos);
                recordPlaced(level, "dragon_sanctuary", pos);
                return true;
            }
            case "roulette_pavilion", "roulette", "fortune", "ruleta", "santuario_ruleta" -> {
                clearStructureVegetation(level, pos, 10, 16, 10);
                SeriesStructureBuilder.buildRoulettePavilion(level, pos);
                recordPlaced(level, "roulette_pavilion", pos);
                return true;
            }
            case "goddess_shrine", "santuario_diosa", "mana_spring", "goddess", "shrine_goddess" -> {
                clearStructureVegetation(level, pos, 11, 18, 11);
                SeriesStructureBuilder.buildGoddessShrine(level, pos);
                recordPlaced(level, "goddess_shrine", pos);
                return true;
            }
            case "spirit_valley", "valle_bestias", "jade_spring", "valle_jade", "spirit_beast_valley" -> {
                clearStructureVegetation(level, pos, 12, 16, 12);
                SeriesStructureBuilder.buildSpiritBeastValley(level, pos);
                recordPlaced(level, "spirit_valley", pos);
                return true;
            }
            case "nine_tribulations_pagoda", "tribulation_pagoda", "pagoda_tribulaciones", "pagoda_9", "pagoda_nueve_tribulaciones" -> {
                clearStructureVegetation(level, pos, 14, 45, 14);
                SeriesStructureBuilder.buildNineTribulationsPagoda(level, pos);
                recordPlaced(level, "nine_tribulations_pagoda", pos);
                return true;
            }
            case "adventurers_guild", "guild", "gremio", "bounty_board", "isekai_guild", "tablon" -> {
                clearStructureVegetation(level, pos, 12, 18, 12);
                SeriesStructureBuilder.buildAdventurersGuild(level, pos);
                recordPlaced(level, "adventurers_guild", pos);
                return true;
            }
            case "blood_sect_fortress", "blood_sect", "culto_demoniaco", "secta_sangre", "blood_fortress" -> {
                clearStructureVegetation(level, pos, 18, 30, 18);
                SeriesStructureBuilder.buildBloodSectFortress(level, pos);
                recordPlaced(level, "blood_sect_fortress", pos);
                return true;
            }
            case "yggdrasil_root_labyrinth", "root_labyrinth", "laberinto_raices", "yggdrasil_roots", "mazmorra_raices" -> {
                clearStructureVegetation(level, pos, 18, 14, 18);
                SeriesStructureBuilder.buildYggdrasilRootLabyrinth(level, pos);
                recordPlaced(level, "yggdrasil_root_labyrinth", pos);
                return true;
            }
            case "ancestral_immortal_grotto", "immortal_grotto", "gruta_inmortal", "gruta_ancestral", "cueva_inmortal", "wu_xing_grotto" -> {
                // Estructura SUBTERRÁNEA: si se invoca con altura de superficie (>45), sumergir en profundidad
                int actualY = pos.getY() > 45 ? Math.min(32, pos.getY() - 30) : pos.getY();
                if (actualY < 15) actualY = 22;
                BlockPos grottoPos = new BlockPos(pos.getX(), actualY, pos.getZ());
                SeriesStructureBuilder.buildAncestralImmortalGrotto(level, grottoPos);
                recordPlaced(level, "ancestral_immortal_grotto", grottoPos);
                return true;
            }
            case "elemental_floating_castles", "elemental_castles", "castillos_elementales", "elemental_castle", "castillo_elemental", "reyes_elementales" -> {
                SeriesStructureBuilder.buildElementalFloatingCastles(level, pos);
                recordPlaced(level, "elemental_floating_castles", pos);
                return true;
            }
            case "hunter_expedition_camp", "hunter_camp", "campamento_cazadores", "campamento_expedicion" -> {
                clearStructureVegetation(level, pos, 9, 8, 9);
                SeriesStructureBuilder.buildHunterExpeditionCamp(level, pos);
                recordPlaced(level, "hunter_expedition_camp", pos);
                return true;
            }
            case "ruined_stargazer_outpost", "stargazer_outpost", "torre_astral", "torre_observacion", "stargazer_tower" -> {
                clearStructureVegetation(level, pos, 7, 18, 7);
                SeriesStructureBuilder.buildRuinedStargazerOutpost(level, pos);
                recordPlaced(level, "ruined_stargazer_outpost", pos);
                return true;
            }
            case "ancient_lore_stele", "lore_stele", "estela_ancestral", "estela_runica", "stele_lore" -> {
                clearStructureVegetation(level, pos, 9, 10, 9);
                SeriesStructureBuilder.buildAncientLoreStele(level, pos);
                recordPlaced(level, "ancient_lore_stele", pos);
                return true;
            }
            case "ancient_alchemy_pavilion", "alchemy_pavilion", "alchemy", "alquimia", "pabellon_alquimia" -> {
                clearStructureVegetation(level, pos, 9, 14, 9);
                SeriesStructureBuilder.buildAncientAlchemyPavilion(level, pos);
                recordPlaced(level, "ancient_alchemy_pavilion", pos);
                return true;
            }
            case "taoist_bamboo_shrine", "bamboo_shrine", "santuario_bambu", "taoist_shrine", "santuario_taoista" -> {
                clearStructureVegetation(level, pos, 8, 12, 8);
                SeriesStructureBuilder.buildTaoistBambooShrine(level, pos);
                recordPlaced(level, "taoist_bamboo_shrine", pos);
                return true;
            }

            case "dawn_winery", "teyvat_winery", "winery" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "dawn_winery", -24, 0, -24);
                if (ok) {
                    SeriesStructureBuilder.spawnNpcIfAbsent(level, pos.offset(0, 1, 4), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.DILUC);
                    recordPlaced(level, "teyvat_dawn_winery", pos);
                }
                return ok;
            }
            case "jade_chamber", "teyvat_jade_chamber" -> {
                int airY = Math.max(180, pos.getY());
                BlockPos airPos = new BlockPos(pos.getX(), airY, pos.getZ());
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, airPos, "jade_chamber", -32, 0, -32);
                if (ok) {
                    recordPlaced(level, "teyvat_jade_chamber", airPos);
                    SeriesStructureBuilder.spawnNpcIfAbsent(level, airPos.offset(0, 1, 2), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.RAIDEN_SHOGUN);
                    SeriesStructureBuilder.spawnNpcIfAbsent(level, airPos.offset(3, 1, -2), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.ACHERON);
                }
                return ok;
            }
            case "teyvat_treehouses", "treehouses" -> {
                TeyvatRegionalBuilder.buildAranaraTreehouse(level, pos);
                recordPlaced(level, "teyvat_treehouses", pos);
                return true;
            }
            case "windrise_giant_tree", "windrise", "teyvat_tree" -> {
                clearStructureVegetation(level, pos, 60, 30, 60);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "windrise_giant_tree", -71, -2, -69);
                if (ok) {
                    TeyvatStructureManager.purgeParasiteTreesInsideWindrise(level, pos);
                    recordPlaced(level, "teyvat_windrise_tree", pos);
                    SeriesStructureBuilder.spawnNpcIfAbsent(level, pos.offset(0, 1, 6), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.GANYU);
                    SeriesStructureBuilder.spawnNpcIfAbsent(level, pos.offset(4, 1, 4), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.MARCH_7TH);
                }
                return ok;
            }
            case "beidou_ship", "alcor" -> {
                BlockPos waterPos = new BlockPos(pos.getX(), 63, pos.getZ());
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, waterPos, "beidou_ship", -18, -10, -62);
                if (ok) {
                    recordPlaced(level, "teyvat_beidou_ship", waterPos);
                }
                return ok;
            }
            case "witch_island" -> {
                TeyvatRegionalBuilder.buildFontaineGazebo(level, pos);
                recordPlaced(level, "teyvat_witch_island", pos);
                return true;
            }
            case "hilichurl_camp", "campamento_hilichurl", "hilichurl", "campamento" -> {
                clearStructureVegetation(level, pos, 12, 12, 12);
                TeyvatRegionalBuilder.buildHilichurlCamp(level, pos);
                recordPlaced(level, "hilichurl_camp", pos);
                return true;
            }
            case "chasm_mine", "teyvat_chasm_mine", "mina_chasm", "mina_teyvat" -> {
                // Estructura SUBTERRÁNEA profunda: Si se invoca con cota de superficie, anclarla en cavernas profundas (Y <= 15)
                int deepY = pos.getY() > 25 ? Math.min(10, pos.getY() - 55) : pos.getY();
                if (deepY < -30) deepY = -15;
                BlockPos minePos = new BlockPos(pos.getX(), deepY, pos.getZ());
                clearStructureVegetation(level, minePos, 25, 25, 25);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, minePos, "teyvat_chasm_mine", -24, 0, -24);
                if (ok) {
                    recordPlaced(level, "teyvat_chasm_mine", minePos);
                }
                return ok;
            }
            case "mora_temple", "teyvat_mora_temple", "templo_mora", "templo_ancestral" -> {
                clearStructureVegetation(level, pos, 15, 20, 15);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_mora_temple", -13, 0, -12);
                if (ok) {
                    recordPlaced(level, "teyvat_mora_temple", pos);
                }
                return ok;
            }
            case "interastral_bank", "teyvat_interastral_bank", "banco_interastral", "banco" -> {
                clearStructureVegetation(level, pos, 8, 10, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_interastral_bank", -5, 0, -5);
                if (ok) {
                    recordPlaced(level, "teyvat_interastral_bank", pos);
                }
                return ok;
            }
            case "plains_shrine", "teyvat_plains_shrine", "santuario_llanura" -> {
                clearStructureVegetation(level, pos, 8, 8, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_plains_shrine", -6, 0, -6);
                if (ok) {
                    recordPlaced(level, "teyvat_plains_shrine", pos);
                }
                return ok;
            }
            case "desert_shrine", "teyvat_desert_shrine", "santuario_desierto" -> {
                clearStructureVegetation(level, pos, 8, 8, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_desert_shrine", -6, 0, -6);
                if (ok) {
                    recordPlaced(level, "teyvat_desert_shrine", pos);
                }
                return ok;
            }
            case "mountain_shrine", "teyvat_mountain_shrine", "santuario_montana" -> {
                clearStructureVegetation(level, pos, 8, 8, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_mountain_shrine", -6, 0, -7);
                if (ok) {
                    recordPlaced(level, "teyvat_mountain_shrine", pos);
                }
                return ok;
            }
            case "overworld_miniboss_arena", "miniboss_arena", "arena_miniboss", "arena_overworld" -> {
                TeyvatStructureManager.clearArenaCylinder(level, pos, 20, 35);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_miniboss_overworld", -15, 0, -15);
                if (!ok) {
                    ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_electro_hypostasis", -15, 0, -15);
                }
                if (ok) {
                    spawnOverworldMiniboss(level, pos);
                    BlockPos chestPos = pos.offset(0, 1, 13);
                    level.setBlock(chestPos, com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.LockedTreasureChestBlock.LOCKED, true), 3);
                    recordPlaced(level, "overworld_miniboss_arena", pos);
                }
                return ok;
            }

            default -> {
                return false;
            }
        }
    }

    private static void recordPlaced(ServerLevel level, String type, BlockPos pos) {
        if (level.getServer() != null) {
            SeriesStructureData data = SeriesStructureData.get(level.getServer());
            data.recordStructure(type, pos);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, net.minecraft.sounds.SoundSource.AMBIENT, 1.2F, 1.0F);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D, 70, 2.0D, 2.0D, 2.0D, 0.2D);
        }
    }

    public static void spawnOverworldMiniboss(ServerLevel level, BlockPos pos) {
        var rand = level.getRandom();
        net.minecraft.world.entity.EntityType<?>[] types = new net.minecraft.world.entity.EntityType<?>[]{
                com.soybe.awakeningsystem.entity.ModEntities.WILDFIRE.get(),
                com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get(),
                com.soybe.awakeningsystem.entity.ModEntities.JADE_GUARDIAN_BOSS.get(),
                com.soybe.awakeningsystem.entity.ModEntities.GREAT_SNOWBOAR_KING.get(),
                com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_LORD.get(),
                com.soybe.awakeningsystem.entity.ModEntities.ANEMO_HYPOSTASIS.get(),
                com.soybe.awakeningsystem.entity.ModEntities.GEO_HYPOSTASIS.get(),
                com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_HYPOSTASIS.get(),
                com.soybe.awakeningsystem.entity.ModEntities.PYRO_HYPOSTASIS.get(),
                com.soybe.awakeningsystem.entity.ModEntities.RUIN_GUARD.get(),
                com.soybe.awakeningsystem.entity.ModEntities.RUIN_HUNTER.get(),
                com.soybe.awakeningsystem.entity.ModEntities.RUIN_DRAKE.get(),
                com.soybe.awakeningsystem.entity.ModEntities.STONEHIDE_LAWACHURL.get(),
                com.soybe.awakeningsystem.entity.ModEntities.FROSTARM_LAWACHURL.get(),
                com.soybe.awakeningsystem.entity.ModEntities.THUNDERHELM_LAWACHURL.get(),
                com.soybe.awakeningsystem.entity.ModEntities.GEOVISHAP.get(),
                com.soybe.awakeningsystem.entity.ModEntities.RUIN_GRADER.get()
        };
        var chosenType = types[rand.nextInt(types.length)];
        var entity = chosenType.create(level);
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();

            CompoundTag tag = mob.getPersistentData();
            tag.putDouble("arena_center_x", pos.getX() + 0.5D);
            tag.putDouble("arena_center_y", (double) pos.getY() + 1.0D);
            tag.putDouble("arena_center_z", pos.getZ() + 0.5D);
            tag.putDouble("arena_max_radius", 35.0D);
            tag.putBoolean("is_overworld_miniboss", true);

            level.addFreshEntity(mob);
        }
    }

    private static final java.util.Set<Long> EVALUATED_CHUNKS = new java.util.HashSet<>();

    public static void tickNaturalGeneration(MinecraftServer server) {
        if (server == null) return;
        checkAndEnsureNpcsPresent(server);

        ServerLevel level = server.overworld();
        if (level == null) return;

        java.util.List<net.minecraft.server.level.ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        SeriesStructureData data = SeriesStructureData.get(server);

        for (net.minecraft.server.level.ServerPlayer player : players) {
            if (player.level() != level) continue;

            int pcx = player.chunkPosition().x;
            int pcz = player.chunkPosition().z;

            // Revisar trozos a distancia moderada del jugador (entre 3 y 8 trozos)
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    int distSq = dx * dx + dz * dz;
                    if (distSq < 9 || distSq > 64) continue;

                    int chunkX = pcx + dx;
                    int chunkZ = pcz + dz;
                    long chunkKey = net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ);
                    if (EVALUATED_CHUNKS.contains(chunkKey)) continue;

                    if (isRegionTargetChunk(level, chunkX, chunkZ)) {
                        if (areAllChunksLoaded(level, chunkX, chunkZ)) {
                            EVALUATED_CHUNKS.add(chunkKey);
                            attemptNaturalGeneration(level, chunkX, chunkZ, data);
                            return; // Generar como máximo 1 por ciclo para prevenir caídas de tick
                        }
                    } else {
                        EVALUATED_CHUNKS.add(chunkKey);
                    }
                }
            }
        }

        // Generación natural del Pabellón de Ascensión en la Dimensión Isekai (Lejos de Yggdrasil)
        ServerLevel isekaiLevel = server.getLevel(com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD);
        if (isekaiLevel != null) {
            for (net.minecraft.server.level.ServerPlayer player : players) {
                if (player.level() != isekaiLevel) continue;
                int pcx = player.chunkPosition().x;
                int pcz = player.chunkPosition().z;
                for (int dx = -6; dx <= 6; dx++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        int chunkX = pcx + dx;
                        int chunkZ = pcz + dz;
                        long chunkKey = (((long) chunkX) << 32) ^ ((long) chunkZ & 0xFFFFFFFFL) ^ 0x4953454BL;
                        if (EVALUATED_CHUNKS.contains(chunkKey)) continue;

                        int blockX = chunkX * 16 + 8;
                        int blockZ = chunkZ * 16 + 8;
                        // No cerca del árbol (radio mínimo de 350 bloques)
                        if (blockX * blockX + blockZ * blockZ < 350 * 350) {
                            EVALUATED_CHUNKS.add(chunkKey);
                            continue;
                        }

                        if (isIsekaiPavilionTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiPavilionGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiGoddessShrineTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiGoddessShrineGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiGuildTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiGuildGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiRootLabyrinthTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiRootLabyrinthGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiFloatingCastlesTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiFloatingCastlesGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiHunterCampTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiHunterCampGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiStargazerTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiStargazerGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isIsekaiLoreSteleTargetChunk(isekaiLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(isekaiLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptIsekaiLoreSteleGeneration(isekaiLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else {
                            EVALUATED_CHUNKS.add(chunkKey);
                        }
                    }
                }
            }
        }

        // Generación natural del Valle de las Bestias Espirituales en la Dimensión Murim (Lejos de la Secta)
        ServerLevel murimLevel = server.getLevel(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD);
        if (murimLevel != null) {
            for (net.minecraft.server.level.ServerPlayer player : players) {
                if (player.level() != murimLevel) continue;
                int pcx = player.chunkPosition().x;
                int pcz = player.chunkPosition().z;
                for (int dx = -6; dx <= 6; dx++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        int chunkX = pcx + dx;
                        int chunkZ = pcz + dz;
                        long chunkKey = (((long) chunkX) << 32) ^ ((long) chunkZ & 0xFFFFFFFFL) ^ 0x4D5552494DL;
                        if (EVALUATED_CHUNKS.contains(chunkKey)) continue;

                        int blockX = chunkX * 16 + 8;
                        int blockZ = chunkZ * 16 + 8;
                        // No cerca de la isla central de la Secta (radio mínimo de 250 bloques)
                        if (blockX * blockX + blockZ * blockZ < 250 * 250) {
                            EVALUATED_CHUNKS.add(chunkKey);
                            continue;
                        }

                        if (isMurimSpiritValleyTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimSpiritValleyGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimPagodaTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimPagodaGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimBloodSectTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimBloodSectGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimImmortalGrottoTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimImmortalGrottoGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimMountHuaTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimMountHuaGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimNanoTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimNanoGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimDragonSanctuaryTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimDragonSanctuaryGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimAlchemyTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimAlchemyGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isMurimBambooShrineTargetChunk(murimLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(murimLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptMurimBambooShrineGeneration(murimLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else {
                            EVALUATED_CHUNKS.add(chunkKey);
                        }
                    }
                }
            }
        }

        // Generación natural de estructuras en la Dimensión Teyvat
        ServerLevel teyvatLevel = server.getLevel(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvatLevel != null) {
            for (net.minecraft.server.level.ServerPlayer player : players) {
                if (player.level() != teyvatLevel) continue;
                int pcx = player.chunkPosition().x;
                int pcz = player.chunkPosition().z;
                for (int dx = -6; dx <= 6; dx++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        int chunkX = pcx + dx;
                        int chunkZ = pcz + dz;
                        long chunkKey = (((long) chunkX) << 32) ^ ((long) chunkZ & 0xFFFFFFFFL) ^ 0x544559564154L;
                        if (EVALUATED_CHUNKS.contains(chunkKey)) continue;

                        int blockX = chunkX * 16 + 8;
                        int blockZ = chunkZ * 16 + 8;
                        // No cerca del Arrecife de Musk (radio mínimo de 250 bloques)
                        if (Math.abs(blockX - com.soybe.awakeningsystem.TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getX()) < 250 && Math.abs(blockZ - com.soybe.awakeningsystem.TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getZ()) < 250) {
                            EVALUATED_CHUNKS.add(chunkKey);
                            continue;
                        }

                        if (isTeyvatTreehousesTargetChunk(teyvatLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(teyvatLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptTeyvatTreehousesGeneration(teyvatLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isTeyvatJadeChamberTargetChunk(teyvatLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(teyvatLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptTeyvatJadeChamberGeneration(teyvatLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isTeyvatWindriseTreeTargetChunk(teyvatLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(teyvatLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptTeyvatWindriseTreeGeneration(teyvatLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isTeyvatBeidouShipTargetChunk(teyvatLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(teyvatLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptTeyvatBeidouShipGeneration(teyvatLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else {
                            EVALUATED_CHUNKS.add(chunkKey);
                        }
                    }
                }
            }
        }
    }

    private static boolean isTeyvatTreehousesTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 28);
        int regionZ = Math.floorDiv(chunkZ, 28);
        long seed = ((long) regionX * 491827461937L + (long) regionZ * 184719274911L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 28 + rand.nextInt(20) + 4;
        int targetChunkZ = regionZ * 28 + rand.nextInt(20) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptTeyvatTreehousesGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 12);
        if (surfaceY < 55 || surfaceY > 160) return;
        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 350.0D)) return;
        generateStructure(level, center, "teyvat_treehouses");
    }

    private static boolean isTeyvatJadeChamberTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 749281729401L + (long) regionZ * 639182740192L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptTeyvatJadeChamberGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int targetY = 185;
        BlockPos center = new BlockPos(blockX, targetY, blockZ);
        if (data.hasStructureNearby(center, 450.0D)) return;
        generateStructure(level, center, "jade_chamber");
    }

    private static boolean isTeyvatWindriseTreeTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 30);
        int regionZ = Math.floorDiv(chunkZ, 30);
        long seed = ((long) regionX * 172940184719L + (long) regionZ * 629183749102L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 30 + rand.nextInt(22) + 4;
        int targetChunkZ = regionZ * 30 + rand.nextInt(22) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptTeyvatWindriseTreeGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 16);
        if (surfaceY < 55 || surfaceY > 160) return;
        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 500.0D)) return;
        generateStructure(level, center, "windrise_giant_tree");
    }

    private static boolean isTeyvatBeidouShipTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 291837461947L + (long) regionZ * 592837194819L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptTeyvatBeidouShipGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        BlockPos center = new BlockPos(blockX, 63, blockZ);
        // Validar que realmente sea agua profunda (océano / lago) y no tierra sólida
        if (!level.getFluidState(center).is(net.minecraft.world.level.material.Fluids.WATER)
                || !level.getFluidState(center.below(3)).is(net.minecraft.world.level.material.Fluids.WATER)) {
            return;
        }
        if (data.hasStructureNearby(center, 400.0D)) return;
        generateStructure(level, center, "beidou_ship");
    }

    private static boolean isIsekaiPavilionTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 36);
        int regionZ = Math.floorDiv(chunkZ, 36);
        long seed = ((long) regionX * 592837194819L + (long) regionZ * 291837461947L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 36 + rand.nextInt(28) + 4;
        int targetChunkZ = regionZ * 36 + rand.nextInt(28) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiPavilionGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 10);
        if (surfaceY < 55 || surfaceY > 175) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 500.0D)) return;

        generateStructure(level, center, "ascension_pavilion");
    }

    private static boolean isIsekaiGoddessShrineTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 28);
        int regionZ = Math.floorDiv(chunkZ, 28);
        long seed = ((long) regionX * 749281729401L + (long) regionZ * 382910481729L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 28 + rand.nextInt(20) + 4;
        int targetChunkZ = regionZ * 28 + rand.nextInt(20) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiGoddessShrineGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 11);
        if (surfaceY < 55 || surfaceY > 180) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 350.0D)) return;

        generateStructure(level, center, "goddess_shrine");
    }

    private static boolean isIsekaiGuildTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 14);
        int regionZ = Math.floorDiv(chunkZ, 14);
        long seed = ((long) regionX * 629183749102L + (long) regionZ * 172940184719L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 14 + rand.nextInt(10) + 2;
        int targetChunkZ = regionZ * 14 + rand.nextInt(10) + 2;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiGuildGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 10);
        if (surfaceY < 55 || surfaceY > 195) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 180.0D)) return;

        generateStructure(level, center, "adventurers_guild");
    }

    private static boolean isIsekaiRootLabyrinthTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 482910481729L + (long) regionZ * 639182740192L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiRootLabyrinthGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 14);
        if (surfaceY < 55 || surfaceY > 180) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        generateStructure(level, center, "yggdrasil_root_labyrinth");
    }

    private static boolean isIsekaiFloatingCastlesTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 36);
        int regionZ = Math.floorDiv(chunkZ, 36);
        long seed = ((long) regionX * 592817491023L + (long) regionZ * 192837482910L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 36 + rand.nextInt(26) + 5;
        int targetChunkZ = regionZ * 36 + rand.nextInt(26) + 5;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiFloatingCastlesGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int targetY = 200; // Elevación aérea en el firmamento de Isekai

        BlockPos center = new BlockPos(blockX, targetY, blockZ);
        if (data.hasStructureNearby(center, 500.0D)) return;

        generateStructure(level, center, "elemental_floating_castles");
    }

    private static boolean isIsekaiHunterCampTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 26);
        int regionZ = Math.floorDiv(chunkZ, 26);
        long seed = ((long) regionX * 184719274911L + (long) regionZ * 491827461937L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 26 + rand.nextInt(20) + 3;
        int targetChunkZ = regionZ * 26 + rand.nextInt(20) + 3;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiHunterCampGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 8);
        if (surfaceY < 55 || surfaceY > 180) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 300.0D)) return;

        generateStructure(level, center, "hunter_expedition_camp");
    }

    private static boolean isIsekaiStargazerTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 28);
        int regionZ = Math.floorDiv(chunkZ, 28);
        long seed = ((long) regionX * 381927401923L + (long) regionZ * 829104817291L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 28 + rand.nextInt(22) + 3;
        int targetChunkZ = regionZ * 28 + rand.nextInt(22) + 3;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiStargazerGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 7);
        if (surfaceY < 55 || surfaceY > 190) return;


        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 320.0D)) return;

        generateStructure(level, center, "ruined_stargazer_outpost");
    }

    private static boolean isIsekaiLoreSteleTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 30);
        int regionZ = Math.floorDiv(chunkZ, 30);
        long seed = ((long) regionX * 619284719203L + (long) regionZ * 749102847193L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 30 + rand.nextInt(24) + 3;
        int targetChunkZ = regionZ * 30 + rand.nextInt(24) + 3;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptIsekaiLoreSteleGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 9);
        if (surfaceY < 55 || surfaceY > 185) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 350.0D)) return;

        generateStructure(level, center, "ancient_lore_stele");
    }

    private static boolean isMurimSpiritValleyTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 26);
        int regionZ = Math.floorDiv(chunkZ, 26);
        long seed = ((long) regionX * 819283749102L + (long) regionZ * 471928374829L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 26 + rand.nextInt(18) + 4;
        int targetChunkZ = regionZ * 26 + rand.nextInt(18) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimSpiritValleyGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 12);
        if (surfaceY < 55 || surfaceY > 185) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 350.0D)) return;

        generateStructure(level, center, "spirit_valley");
    }

    private static boolean isMurimPagodaTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 30);
        int regionZ = Math.floorDiv(chunkZ, 30);
        long seed = ((long) regionX * 918273645123L + (long) regionZ * 635241829304L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 30 + rand.nextInt(22) + 4;
        int targetChunkZ = regionZ * 30 + rand.nextInt(22) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimPagodaGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 14);
        if (surfaceY < 55 || surfaceY > 185) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        generateStructure(level, center, "nine_tribulations_pagoda");
    }

    private static boolean isMurimBloodSectTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 34);
        int regionZ = Math.floorDiv(chunkZ, 34);
        long seed = ((long) regionX * 529184719203L + (long) regionZ * 837192048591L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 34 + rand.nextInt(26) + 4;
        int targetChunkZ = regionZ * 34 + rand.nextInt(26) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimBloodSectGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 16);
        if (surfaceY < 55 || surfaceY > 185) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        generateStructure(level, center, "blood_sect_fortress");
    }

    private static boolean isMurimImmortalGrottoTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 718294019283L + (long) regionZ * 492019284719L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimImmortalGrottoGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findSolidSurfaceGround(level, blockX, blockZ);
        if (surfaceY < 55 || surfaceY > 185) return;

        // Estructura SUBTERRÁNEA en Murim: asegurar un techo macizo de roca de al menos 25-35 bloques
        int grottoY = Math.min(32, surfaceY - 30);
        if (grottoY < 15) grottoY = 22;

        BlockPos center = new BlockPos(blockX, grottoY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        generateStructure(level, center, "ancestral_immortal_grotto");
    }

    private static boolean isMurimMountHuaTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 381920485918L + (long) regionZ * 192837465019L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimMountHuaGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 11);
        if (surfaceY < 55 || surfaceY > 190) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        generateStructure(level, center, "mount_hua");
    }

    private static boolean isMurimNanoTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 619283749104L + (long) regionZ * 827192048571L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimNanoGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 11);
        if (surfaceY < 55 || surfaceY > 190) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        generateStructure(level, center, "nano");
    }

    private static boolean isMurimDragonSanctuaryTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 40);
        int regionZ = Math.floorDiv(chunkZ, 40);
        long seed = ((long) regionX * 987654321019L + (long) regionZ * 123456789098L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 40 + rand.nextInt(32) + 4;
        int targetChunkZ = regionZ * 40 + rand.nextInt(32) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimDragonSanctuaryGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 13);
        if (surfaceY < 75 || surfaceY > 210) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 600.0D)) return;

        generateStructure(level, center, "dragon_sanctuary");
    }

    private static boolean isMurimAlchemyTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 28);
        int regionZ = Math.floorDiv(chunkZ, 28);
        long seed = ((long) regionX * 419283749102L + (long) regionZ * 781928374829L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 28 + rand.nextInt(20) + 4;
        int targetChunkZ = regionZ * 28 + rand.nextInt(20) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimAlchemyGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 11);
        if (surfaceY < 55 || surfaceY > 190) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 350.0D)) return;

        generateStructure(level, center, "ancient_alchemy_pavilion");
    }

    private static boolean isMurimBambooShrineTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 26);
        int regionZ = Math.floorDiv(chunkZ, 26);
        long seed = ((long) regionX * 628193740192L + (long) regionZ * 391827465910L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 26 + rand.nextInt(18) + 4;
        int targetChunkZ = regionZ * 26 + rand.nextInt(18) + 4;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimBambooShrineGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 10);
        if (surfaceY < 55 || surfaceY > 190) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 350.0D)) return;

        generateStructure(level, center, "taoist_bamboo_shrine");
    }

    private static boolean isRegionTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);

        long seed = ((long) regionX * 341873128712L + (long) regionZ * 132897987541L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);

        int targetChunkX = regionX * 32 + rand.nextInt(24) + 4;
        int targetChunkZ = regionZ * 32 + rand.nextInt(24) + 4;

        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static boolean areAllChunksLoaded(ServerLevel level, int chunkX, int chunkZ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!level.hasChunk(chunkX + dx, chunkZ + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void attemptNaturalGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 12);
        if (surfaceY < 55 || surfaceY > 180) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 400.0D)) return;

        Holder<Biome> biomeHolder = level.getBiome(center);
        String biomeKey = biomeHolder.unwrapKey().map(k -> k.location().getPath()).orElse("").toLowerCase(java.util.Locale.ROOT);

        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        long seed = ((long) regionX * 341873128712L + (long) regionZ * 132897987541L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);

        if (biomeKey.contains("badlands") || biomeKey.contains("stony") || biomeKey.contains("jagged") || biomeKey.contains("windswept") || biomeKey.contains("crag")) {
            if (rand.nextBoolean()) {
                generateStructure(level, center, "valter");
            } else {
                generateStructure(level, center, "ranker");
            }
        } else if (biomeKey.contains("desert") || biomeKey.contains("savanna")) {
            float p = rand.nextFloat();
            if (p < 0.25F && !data.hasStructureNearby(center, 300.0D)) {
                generateStructure(level, center, "desert_shrine");
            } else if (p < 0.50F && !data.hasStructureNearby(center, 400.0D)) {
                generateStructure(level, center, "mora_temple");
            } else if (p < 0.70F && !data.hasStructureNearby(center, 400.0D)) {
                generateStructure(level, center, "roulette_pavilion");
            } else {
                int subwayY = Math.min(25, surfaceY - 35);
                if (subwayY < 10) subwayY = 16;
                generateStructure(level, new BlockPos(blockX, subwayY, blockZ), "orv");
            }
        } else if (biomeKey.contains("cherry") || biomeKey.contains("flower") || biomeKey.contains("meadow") || biomeKey.contains("bamboo")) {
            float p = rand.nextFloat();
            if (p < 0.25F && !data.hasStructureNearby(center, 350.0D)) {
                generateStructure(level, center, "taoist_bamboo_shrine");
            } else if (p < 0.50F && !data.hasStructureNearby(center, 350.0D)) {
                generateStructure(level, center, "ancient_alchemy_pavilion");
            } else if (p < 0.70F && !data.hasStructureNearby(center, 400.0D)) {
                generateStructure(level, center, "roulette_pavilion");
            } else {
                generateStructure(level, center, "estate");
            }
        } else if (biomeKey.contains("dark_forest") || biomeKey.contains("jungle") || biomeKey.contains("swamp")) {
            generateStructure(level, center, "ranker");
        } else if (biomeKey.contains("mountain") || biomeKey.contains("peak") || biomeKey.contains("slopes") || biomeKey.contains("frozen") || biomeKey.contains("snowy")) {
            float p = rand.nextFloat();
            if (p < 0.25F && !data.hasStructureNearby(center, 350.0D)) {
                generateStructure(level, center, "mountain_shrine");
            } else if (p < 0.45F && !data.hasStructureNearby(center, 500.0D)) {
                generateStructure(level, center, "chasm_mine");
            } else if (p < 0.65F) {
                generateStructure(level, center, "valter");
            } else if (p < 0.85F) {
                generateStructure(level, center, "tbate");
            } else {
                generateStructure(level, center, "taoist_bamboo_shrine");
            }
        } else if (biomeKey.contains("ocean") || biomeKey.contains("river") || biomeKey.contains("beach") || biomeKey.contains("lush")) {
            generateStructure(level, center, "tower_of_god");
        } else if (biomeKey.contains("taiga") || biomeKey.contains("grove") || biomeKey.contains("forest") || biomeKey.contains("plains") || biomeKey.contains("savanna")) {
            float p = rand.nextFloat();
            if (p < 0.15F && !data.hasStructureNearby(center, 220.0D)) {
                generateStructure(level, center, "adventurers_guild");
            } else if (p < 0.30F && !data.hasStructureNearby(center, 300.0D)) {
                generateStructure(level, center, "plains_shrine");
            } else if (p < 0.45F && !data.hasStructureNearby(center, 400.0D)) {
                generateStructure(level, center, "interastral_bank");
            } else if (p < 0.60F && !data.hasStructureNearby(center, 250.0D)) {
                generateStructure(level, center, "hilichurl_camp");
            } else if (p < 0.72F && !data.hasStructureNearby(center, 380.0D)) {
                generateStructure(level, center, "roulette_pavilion");
            } else if (p < 0.85F && !data.hasStructureNearby(center, 400.0D)) {
                generateStructure(level, center, "overworld_miniboss_arena");
            } else if (p < 0.93F) {
                generateStructure(level, center, "estate");
            } else {
                generateStructure(level, center, "solo_leveling");
            }
        } else {
            int pick = rand.nextInt(13);
            switch (pick) {
                case 0 -> generateStructure(level, center, "solo_leveling");
                case 1 -> generateStructure(level, center, "estate");
                case 2 -> {
                    int subwayY = Math.min(25, surfaceY - 35);
                    if (subwayY < 10) subwayY = 16;
                    generateStructure(level, new BlockPos(blockX, subwayY, blockZ), "orv");
                }
                case 3 -> generateStructure(level, center, "tbate");
                case 4 -> generateStructure(level, center, "ranker");
                case 5 -> generateStructure(level, center, "valter");
                case 6 -> generateStructure(level, center, "roulette_pavilion");
                case 7 -> generateStructure(level, center, "hilichurl_camp");
                case 8 -> generateStructure(level, center, "chasm_mine");
                case 9 -> generateStructure(level, center, "mora_temple");
                case 10 -> generateStructure(level, center, "interastral_bank");
                case 11 -> generateStructure(level, center, "overworld_miniboss_arena");
                default -> generateStructure(level, center, "plains_shrine");
            }
        }
    }

    public static void checkAndEnsureNpcsPresent(MinecraftServer server) {
        if (server == null) return;
        SeriesStructureData data = SeriesStructureData.get(server);
        Map<String, BlockPos> all = data.getAll();
        if (all.isEmpty()) return;

        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            BlockPos playerPos = player.blockPosition();

            for (Map.Entry<String, BlockPos> entry : all.entrySet()) {
                String key = entry.getKey();
                BlockPos sPos = entry.getValue();

                if (sPos.distSqr(playerPos) <= 64.0 * 64.0) {
                    int cx = sPos.getX();
                    int cy = sPos.getY();
                    int cz = sPos.getZ();

                    if (key.startsWith("solo_leveling")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SUNG_JINWOO.get(), com.soybe.awakeningsystem.entity.NpcProfile.SUNG_JINWOO);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz - 3), com.soybe.awakeningsystem.entity.ModEntities.CHA_HAEIN.get(), com.soybe.awakeningsystem.entity.NpcProfile.CHA_HAEIN);
                    } else if (key.startsWith("orv")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz - 2), com.soybe.awakeningsystem.entity.ModEntities.KIM_DOKJA.get(), com.soybe.awakeningsystem.entity.NpcProfile.KIM_DOKJA);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 2, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.YOO_JOONGHYUK.get(), com.soybe.awakeningsystem.entity.NpcProfile.YOO_JOONGHYUK);
                    } else if (key.startsWith("estate")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx - 3, cy + 1, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.LLOYD_FRONTERA.get(), com.soybe.awakeningsystem.entity.NpcProfile.LLOYD_FRONTERA);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 3, cy + 1, cz - 1), com.soybe.awakeningsystem.entity.ModEntities.JAVIER_ASRAHAN.get(), com.soybe.awakeningsystem.entity.NpcProfile.JAVIER_ASRAHAN);
                    } else if (key.startsWith("nano")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.CHEON_YEOWOON.get(), com.soybe.awakeningsystem.entity.NpcProfile.CHEON_YEOWOON);
                    } else if (key.startsWith("tbate")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.ARTHUR_LEYWIN.get(), com.soybe.awakeningsystem.entity.NpcProfile.ARTHUR_LEYWIN);
                    } else if (key.startsWith("ranker")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.CHA_YEONWOO.get(), com.soybe.awakeningsystem.entity.NpcProfile.CHA_YEONWOO);
                    } else if (key.startsWith("mount_hua")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 1), com.soybe.awakeningsystem.entity.ModEntities.CHUNG_MYUNG.get(), com.soybe.awakeningsystem.entity.NpcProfile.CHUNG_MYUNG);
                    } else if (key.startsWith("valter")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 2, cy + 2, cz - 1), com.soybe.awakeningsystem.entity.ModEntities.RYU_HANBIN.get(), com.soybe.awakeningsystem.entity.NpcProfile.RYU_HANBIN);
                    } else if (key.startsWith("tower_of_god")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 3, cz + 1), com.soybe.awakeningsystem.entity.ModEntities.TWENTY_FIFTH_BAAM.get(), com.soybe.awakeningsystem.entity.NpcProfile.TWENTY_FIFTH_BAAM);
                    } else if (key.startsWith("spirit_valley")) {
                        SeriesStructureBuilder.spawnSpiritBeastIfAbsent(level, new BlockPos(cx - 4, cy + 1, cz - 2), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Bestia Inmortal] Grulla Espiritual");
                        SeriesStructureBuilder.spawnSpiritBeastIfAbsent(level, new BlockPos(cx + 6, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Bestia Espiritual] Zorro de Nueve Colas");
                    } else if (key.startsWith("taoist_bamboo_shrine") || key.startsWith("ancient_alchemy_pavilion")) {
                        SeriesStructureBuilder.spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx + 1, cy + 2, cz + 1));
                    } else if (key.startsWith("teyvat_dawn_winery")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.ZHONGLI);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 2, cy + 1, cz - 2), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.HUTAO);
                    } else if (key.startsWith("teyvat_treehouses")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.HOSHIMI_MIYABI);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.ELLEN_JOE);
                    } else if (key.startsWith("teyvat_jade_chamber")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.RAIDEN_SHOGUN);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 3, cy + 1, cz - 2), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.ACHERON);
                    } else if (key.startsWith("teyvat_windrise_tree")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 6), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.GANYU);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get(), com.soybe.awakeningsystem.entity.NpcProfile.MARCH_7TH);
                    }
                }
            }
        }
    }
}
