package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import com.soybe.awakeningsystem.block.ElementalMonumentBlock;
import com.soybe.awakeningsystem.block.LockedTreasureChestBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.entity.LockedTreasureChestBlockEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de Generación y Ambientes Canónicos de Teyvat (Genshin Impact):
 * Genera de forma procedural y adaptativa las estructuras icónicas que definen el mundo de Teyvat:
 * 1. Estatuas de los Siete (Statues of The Seven) con halo elemental y zona de sanación divina.
 * 2. Puntos de Teletransporte (Teleport Waypoints) elevados con acceso celestial.
 * 3. Campamentos de Hilichurls con cabañas, hogueras, vigías y cofres cerrados custodiados.
 * 4. Cofres del Tesoro de Teyvat con gemas, artefactos y monedas.
 * 5. Corrientes de Viento de Anemo (Updrafts) para desplegar el planeador y volar.
 * 6. Pilares Kársticos de Jueyun (Liyue Karsts) que rompen la monotonía del terreno.
 */
public final class TeyvatWorldGenManager {
    private TeyvatWorldGenManager() {}

    private static final Set<Long> EVALUATED_TEYVAT_CHUNKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<BlockPos, Long> STATUE_HEAL_COOLDOWNS = new ConcurrentHashMap<>();

    public static class TeyvatFeaturesData extends SavedData {
        public static final String DATA_NAME = "awakening_teyvat_features";
        public final List<BlockPos> statues = new ArrayList<>();
        public final List<BlockPos> waypoints = new ArrayList<>();
        public final List<BlockPos> updrafts = new ArrayList<>();
        public final List<BlockPos> regionalStructures = new ArrayList<>();
        public final List<BlockPos> camps = new ArrayList<>();
        public final List<BlockPos> chests = new ArrayList<>();
        public final List<BlockPos> bossArenas = new ArrayList<>();
        public final List<BlockPos> shrinesOfDepths = new ArrayList<>();
        public final List<BlockPos> monuments = new ArrayList<>();
        public final Map<String, BlockPos> namedStructures = new ConcurrentHashMap<>();
        public final Set<Long> evaluatedChunks = new HashSet<>();

        public void recordNamedStructure(String name, BlockPos pos) {
            namedStructures.put(name.toLowerCase(Locale.ROOT), pos);
            setDirty();
        }

        public BlockPos getNamedStructure(String name) {
            return namedStructures.get(name.toLowerCase(Locale.ROOT));
        }

        public static TeyvatFeaturesData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(TeyvatFeaturesData::new, TeyvatFeaturesData::load, null),
                    DATA_NAME
            );
        }

        public static TeyvatFeaturesData load(CompoundTag tag, HolderLookup.Provider registries) {
            TeyvatFeaturesData data = new TeyvatFeaturesData();
            loadPosList(tag, "Statues", data.statues);
            loadPosList(tag, "Waypoints", data.waypoints);
            loadPosList(tag, "Updrafts", data.updrafts);
            loadPosList(tag, "RegionalStructures", data.regionalStructures);
            loadPosList(tag, "Camps", data.camps);
            loadPosList(tag, "Chests", data.chests);
            loadPosList(tag, "BossArenas", data.bossArenas);
            loadPosList(tag, "ShrinesOfDepths", data.shrinesOfDepths);
            loadPosList(tag, "Monuments", data.monuments);

            if (tag.contains("NamedStructures", Tag.TAG_COMPOUND)) {
                CompoundTag ns = tag.getCompound("NamedStructures");
                for (String key : ns.getAllKeys()) {
                    CompoundTag cp = ns.getCompound(key);
                    data.namedStructures.put(key, new BlockPos(cp.getInt("X"), cp.getInt("Y"), cp.getInt("Z")));
                }
            }

            if (tag.contains("EvaluatedChunks", Tag.TAG_LONG_ARRAY)) {
                long[] arr = tag.getLongArray("EvaluatedChunks");
                for (long l : arr) {
                    data.evaluatedChunks.add(l);
                    EVALUATED_TEYVAT_CHUNKS.add(l);
                }
            }
            return data;
        }

        private static void loadPosList(CompoundTag tag, String key, List<BlockPos> list) {
            if (tag.contains(key, Tag.TAG_LIST)) {
                ListTag lt = tag.getList(key, Tag.TAG_COMPOUND);
                for (int i = 0; i < lt.size(); i++) {
                    CompoundTag ct = lt.getCompound(i);
                    list.add(new BlockPos(ct.getInt("X"), ct.getInt("Y"), ct.getInt("Z")));
                }
            }
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            savePosList(tag, "Statues", statues);
            savePosList(tag, "Waypoints", waypoints);
            savePosList(tag, "Updrafts", updrafts);
            savePosList(tag, "RegionalStructures", regionalStructures);
            savePosList(tag, "Camps", camps);
            savePosList(tag, "Chests", chests);
            savePosList(tag, "BossArenas", bossArenas);
            savePosList(tag, "ShrinesOfDepths", shrinesOfDepths);
            savePosList(tag, "Monuments", monuments);

            CompoundTag ns = new CompoundTag();
            for (Map.Entry<String, BlockPos> e : namedStructures.entrySet()) {
                CompoundTag cp = new CompoundTag();
                cp.putInt("X", e.getValue().getX());
                cp.putInt("Y", e.getValue().getY());
                cp.putInt("Z", e.getValue().getZ());
                ns.put(e.getKey(), cp);
            }
            tag.put("NamedStructures", ns);

            long[] arr = new long[evaluatedChunks.size()];
            int idx = 0;
            for (Long l : evaluatedChunks) {
                arr[idx++] = l;
            }
            tag.putLongArray("EvaluatedChunks", arr);
            return tag;
        }

        private static void savePosList(CompoundTag tag, String key, List<BlockPos> list) {
            ListTag lt = new ListTag();
            for (BlockPos p : list) {
                CompoundTag ct = new CompoundTag();
                ct.putInt("X", p.getX());
                ct.putInt("Y", p.getY());
                ct.putInt("Z", p.getZ());
                lt.add(ct);
            }
            tag.put(key, lt);
        }
    }

    /**
     * Ciclo de actualización en el servidor para interactividad en Teyvat (Sanación de Estatuas y Corrientes de Viento)
     */
    public static void tick(MinecraftServer server) {
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) return;

        List<ServerPlayer> players = teyvat.players();
        if (players.isEmpty()) return;

        TeyvatFeaturesData data = TeyvatFeaturesData.get(server);
        long gameTime = teyvat.getGameTime();

        for (ServerPlayer player : players) {
            Vec3 pos = player.position();

            // 1. Proximidad a Estatua de los Siete (Sanación divina y recarga de Maná)
            for (BlockPos statue : data.statues) {
                if (statue.closerThan(player.blockPosition(), 8.0D)) {
                    Long lastHeal = STATUE_HEAL_COOLDOWNS.get(statue);
                    if (lastHeal == null || gameTime - lastHeal > 100L) { // Cada 5 segundos
                        STATUE_HEAL_COOLDOWNS.put(statue, gameTime);

                        // Curar vida completa
                        player.heal(player.getMaxHealth());
                        // Recargar energía / Maná
                        SystemData sysData = SystemEvents.data(player);
                        sysData.setMana(sysData.maxMana());
                        AwakeningSystemMod.sync(player);

                        player.removeAllEffects();

                        teyvat.sendParticles(ParticleTypes.HAPPY_VILLAGER, statue.getX() + 0.5D, statue.getY() + 3.0D, statue.getZ() + 0.5D, 35, 1.2D, 1.5D, 1.2D, 0.05D);
                        teyvat.sendParticles(ParticleTypes.END_ROD, statue.getX() + 0.5D, statue.getY() + 4.0D, statue.getZ() + 0.5D, 25, 0.8D, 1.0D, 0.8D, 0.05D);
                        teyvat.playSound(null, statue, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.5F);
                        teyvat.playSound(null, statue, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5F, 1.8F);

                        player.displayClientMessage(Component.literal("§b§l✦ [ESTATUA DE LOS SIETE] §fHas recibido la Bendición y Sanación de los Arcontes."), true);
                    }
                    break;
                }
            }

            // 2. Corrientes de Viento de Anemo (Updrafts)
            for (BlockPos updraft : data.updrafts) {
                if (Math.abs(pos.x - (updraft.getX() + 0.5D)) < 1.8D && Math.abs(pos.z - (updraft.getZ() + 0.5D)) < 1.8D && pos.y >= updraft.getY() && pos.y <= updraft.getY() + 24.0D) {
                    player.setDeltaMovement(player.getDeltaMovement().x * 0.8D, 1.35D, player.getDeltaMovement().z * 0.8D);
                    player.hurtMarked = true;

                    teyvat.sendParticles(ParticleTypes.CLOUD, updraft.getX() + 0.5D, updraft.getY() + 1.0D, updraft.getZ() + 0.5D, 15, 0.4D, 1.0D, 0.4D, 0.2D);
                    teyvat.sendParticles(ParticleTypes.SWEEP_ATTACK, updraft.getX() + 0.5D, updraft.getY() + 2.0D, updraft.getZ() + 0.5D, 4, 0.5D, 1.0D, 0.5D, 0.1D);
                    if (gameTime % 20 == 0) {
                        teyvat.playSound(null, updraft, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.BLOCKS, 0.8F, 1.4F);
                    }
                    break;
                }
            }
        }

        // 3. La generación de estructuras y árboles de Teyvat se ejecuta ahora de forma 100% nativa
        // en el pipeline de generación de chunks (WorldGen Features), eliminando el pop-in al caminar
        // y erradicando cualquier acceso concurrente a LegacyRandomSource.
    }

    private static void tickProceduralGeneration(ServerLevel level, List<ServerPlayer> players, TeyvatFeaturesData data) {
        for (ServerPlayer player : players) {
            int pcx = player.chunkPosition().x;
            int pcz = player.chunkPosition().z;

            int evaluatedCount = 0;
            // Evaluamos en un radio razonable alrededor del jugador (dx, dz de 2 a 6)
            for (int r = 2; r <= 6; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                        int chunkX = pcx + dx;
                        int chunkZ = pcz + dz;
                        long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
                        if (data.evaluatedChunks.contains(chunkKey)) continue;

                        // Solo procesar chunks que ya estén cargados por el servidor para evitar bloqueos
                        if (!level.hasChunk(chunkX, chunkZ)) continue;

                        data.evaluatedChunks.add(chunkKey);
                        data.setDirty();
                        attemptGenerateTeyvatFeatures(level, chunkX, chunkZ, data);
                        evaluatedCount++;
                        if (evaluatedCount >= 6) {
                            return; // Presupuesto de 6 chunks por ciclo para evitar tirones
                        }
                    }
                }
            }
        }
    }

    private static void attemptGenerateTeyvatFeatures(ServerLevel level, int chunkX, int chunkZ, TeyvatFeaturesData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;

        // No generar pegado al spawn (0,0) ni al arrecife de Musk ni a las capitales
        if (Math.abs(blockX) < 160 && Math.abs(blockZ) < 160) return;
        if (Math.abs(blockX - TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getX()) < 220 && Math.abs(blockZ - TeyvatWorldManager.MUSK_REEF_ISLAND_POS.getZ()) < 220) return;
        if (Math.abs(blockX - TeyvatRegionalBuilder.MONDSTADT_POS.getX()) < 180 && Math.abs(blockZ - TeyvatRegionalBuilder.MONDSTADT_POS.getZ()) < 180) return;
        if (Math.abs(blockX - TeyvatRegionalBuilder.LIYUE_POS.getX()) < 180 && Math.abs(blockZ - TeyvatRegionalBuilder.LIYUE_POS.getZ()) < 180) return;
        if (Math.abs(blockX - TeyvatRegionalBuilder.INAZUMA_POS.getX()) < 180 && Math.abs(blockZ - TeyvatRegionalBuilder.INAZUMA_POS.getZ()) < 180) return;
        if (Math.abs(blockX - TeyvatRegionalBuilder.SUMERU_POS.getX()) < 180 && Math.abs(blockZ - TeyvatRegionalBuilder.SUMERU_POS.getZ()) < 180) return;
        if (Math.abs(blockX - TeyvatRegionalBuilder.FONTAINE_POS.getX()) < 180 && Math.abs(blockZ - TeyvatRegionalBuilder.FONTAINE_POS.getZ()) < 180) return;

        int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(blockX, 0, blockZ)).getY();
        if (surfaceY < 60 || surfaceY > 180) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);

        // Descartar si el punto es agua o fluido (océanos, lagos)
        BlockState belowState = level.getBlockState(center.below());
        if (belowState.liquid() || !belowState.isSolid()) return;
        if (level.getFluidState(center).is(net.minecraft.world.level.material.Fluids.WATER)) return;

        long seed = ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L) ^ level.getSeed();
        RandomSource rand = RandomSource.create(seed);

        int roll = rand.nextInt(1000);

        // 1. Estatua de los Siete (0.3% - mín 550 bloques de separación)
        if (roll < 3 && isFarFromList(center, data.statues, 550.0D)) {
            generateStatueOfTheSeven(level, center);
            data.statues.add(center);
            data.setDirty();
            return;
        }

        // 2. Punto de Teletransporte (0.8% - mín 350 bloques de separación)
        if (roll >= 3 && roll < 11 && isFarFromList(center, data.waypoints, 350.0D)) {
            generateTeleportWaypoint(level, center);
            data.waypoints.add(center);
            data.setDirty();
            return;
        }

        String biomeName = level.getBiome(center).unwrapKey().map(k -> k.location().getPath()).orElse("");

        // 3. Arenas de Hipóstasis Elementales (1.4% - mín 420 bloques de separación)
        if (roll >= 11 && roll < 25 && isFarFromList(center, data.bossArenas, 420.0D)) {
            if (generateHypostasisArena(level, center, biomeName, rand)) {
                data.bossArenas.add(center);
                data.setDirty();
                return;
            }
        }

        // 4. Santuarios de las Profundidades (Shrines of Depths) (1.5% - mín 320 bloques de separación)
        if (roll >= 25 && roll < 40 && isFarFromList(center, data.shrinesOfDepths, 320.0D)) {
            if (generateShrineOfDepths(level, center, biomeName, rand)) {
                data.shrinesOfDepths.add(center);
                data.setDirty();
                return;
            }
        }

        // 5. Estructuras Arquitectónicas Regionales de Teyvat (1.0% - mín 450 bloques de separación)
        if (roll >= 40 && roll < 50 && isFarFromList(center, data.regionalStructures, 450.0D)) {
            if (biomeName.contains("narukami") || biomeName.contains("watatsumi") || biomeName.contains("yashiori")) {
                TeyvatRegionalBuilder.buildNarukamiGrandShrine(level, center);
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("desert")) {
                TeyvatRegionalBuilder.buildDesertPyramidAltar(level, center);
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("dragonspine")) {
                TeyvatRegionalBuilder.buildDragonspineFrostAltar(level, center);
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("dharma")) {
                TeyvatRegionalBuilder.buildAranaraTreehouse(level, center);
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("fontaine")) {
                TeyvatRegionalBuilder.buildFontaineGazebo(level, center);
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("dawn_winery")) {
                if (isAreaFlat(level, center, 24, 3)) {
                    boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "dawn_winery", -24, 0, -24);
                    if (placed) {
                        data.regionalStructures.add(center);
                        data.setDirty();
                        return;
                    }
                }
            }
        }

        // 6. Campamento Hilichurl (1.4% - mín 220 bloques de separación, solo en terreno suficientemente plano)
        if (roll >= 50 && roll < 64 && isFarFromList(center, data.camps, 220.0D)) {
            if (isAreaFlat(level, center, 6, 2)) {
                generateHilichurlCamp(level, center, rand);
                data.camps.add(center);
                data.setDirty();
                return;
            }
        }

        // 7. Corriente de Viento de Anemo (0.8% - mín 260 bloques de separación)
        if (roll >= 64 && roll < 72 && isFarFromList(center, data.updrafts, 260.0D)) {
            generateWindUpdraft(level, center);
            data.updrafts.add(center);
            data.setDirty();
            return;
        }

        // 8. Monumento Elemental de Teyvat (1.6% - mín 140 bloques de separación)
        if (roll >= 72 && roll < 88 && isFarFromList(center, data.monuments, 140.0D)) {
            generateElementalMonument(level, center, biomeName, rand);
            data.monuments.add(center);
            data.setDirty();
            return;
        }

        // 9. Cofre del Tesoro de Teyvat (1.4% - mín 120 bloques de separación)
        if (roll >= 88 && roll < 102 && isFarFromList(center, data.chests, 120.0D)) {
            generateWildChest(level, center, rand);
            data.chests.add(center);
            data.setDirty();
            return;
        }

        // 10. Pilares Kársticos de Jueyun en biomas de montaña / karst (1.0% - mín 300 bloques)
        if (biomeName.contains("karst") && roll >= 102 && roll < 112 && isFarFromList(center, data.regionalStructures, 300.0D)) {
            generateJueyunKarstPillars(level, center, rand);
            data.regionalStructures.add(center);
            data.setDirty();
            return;
        }

        // 10. Megaestructuras Épicas, Palacios y Mazmorras Legendarias (1.8% - mín 500 bloques)
        if (roll >= 98 && roll < 116 && isFarFromList(center, data.regionalStructures, 500.0D)) {
            if (biomeName.contains("chasm") || biomeName.contains("mine")) {
                int deepMineY = Math.min(15, surfaceY - 55);
                if (deepMineY < -30) deepMineY = -15;
                BlockPos mineCenter = new BlockPos(center.getX(), deepMineY, center.getZ());
                TeyvatStructureManager.placeNbtStructure(level, mineCenter, "teyvat_chasm_mine", -24, 0, -24);
                data.regionalStructures.add(mineCenter);
                data.setDirty();
                return;
            } else if (biomeName.contains("desert")) {
                if (rand.nextInt(3) == 0 && isFarFromList(center, data.regionalStructures, 650.0D)) {
                    buildSolidFoundation(level, center, 34);
                    TeyvatStructureManager.placeNbtStructure(level, center, "primal_temple", -33, 0, -33);
                    spawnTeyvatMonster(level, center.offset(0, 2, 10), rand.nextBoolean() ? ModEntities.RUIN_HUNTER.get() : ModEntities.RUIN_GUARD.get());
                } else if (rand.nextBoolean()) {
                    TeyvatStructureManager.placeNbtStructure(level, center, "teyvat_mora_temple", -13, 0, -12);
                } else {
                    TeyvatStructureManager.placeNbtStructure(level, center, "teyvat_desert_shrine", -6, 0, -6);
                    spawnTeyvatMonster(level, center.offset(0, 2, 7), rand.nextBoolean() ? ModEntities.RUIN_HUNTER.get() : ModEntities.RUIN_GUARD.get());
                }
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("karst") || biomeName.contains("mountain") || biomeName.contains("dragonspine")) {
                if (biomeName.contains("karst") && rand.nextInt(3) == 0 && isFarFromList(center, data.regionalStructures, 650.0D)) {
                    buildSolidFoundation(level, center, 41);
                    boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "golden_house", -40, 0, -39);
                    if (placed) {
                        BlockPos bossPos = center.offset(0, 7, 4);
                        spawnBossEntity(level, bossPos, ModEntities.CHILDE_BOSS.get());
                        AwakeningSystemMod.LOGGER.info("Casa Dorada generada con Tartaglia/Childe en {}", bossPos);
                    }
                } else if ((biomeName.contains("karst") || biomeName.contains("mountain")) && rand.nextInt(2) == 0 && isFarFromList(center, data.regionalStructures, 650.0D)) {
                    // Azhdaha Subterranean Domain (Cueva Sellada de Azhdaha bajo Nantianmen)
                    int azhdahaY = 28;
                    BlockPos azhdahaCenter = new BlockPos(center.getX(), azhdahaY, center.getZ());
                    boolean placed = TeyvatStructureManager.placeNbtStructure(level, azhdahaCenter, "azhdaha_arena_higher", -37, 0, -32);
                    if (placed) {
                        spawnBossEntity(level, azhdahaCenter.above(1), ModEntities.AZHDAHA_BOSS.get());
                        AwakeningSystemMod.LOGGER.info("Dominio Subterráneo de Azhdaha generado en {}", azhdahaCenter);
                    }
                } else {
                    TeyvatStructureManager.placeNbtStructure(level, center, "teyvat_mountain_shrine", -6, 0, -7);
                    spawnTeyvatMonster(level, center.offset(0, 2, 7), ModEntities.RUIN_HUNTER.get());
                }
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("narukami") && rand.nextInt(3) == 0 && isFarFromList(center, data.regionalStructures, 650.0D)) {
                buildSolidFoundation(level, center, 22);
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "electriccastle_fixed", -21, 0, -18);
                if (placed) {
                    spawnBossEntity(level, center.above(2), ModEntities.RAIDEN_SHOGUN_BOSS.get());
                    AwakeningSystemMod.LOGGER.info("Castillo Tenshukaku generado con Raiden Shogun en {}", center);
                }
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("enkanomiya") && rand.nextInt(3) == 0 && isFarFromList(center, data.regionalStructures, 650.0D)) {
                buildSolidFoundation(level, center, 32);
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "wolflord_structure_final", -31, 0, -29);
                if (placed) {
                    spawnBossEntity(level, center.above(6), ModEntities.GOLDEN_WOLFLORD_BOSS.get());
                    AwakeningSystemMod.LOGGER.info("Arena de Enkanomiya generada con Rey Acechador Dorado en {}", center);
                }
                data.regionalStructures.add(center);
                data.setDirty();
                return;
            } else if (biomeName.contains("windrise") || biomeName.contains("marsh") || biomeName.contains("plains")) {
                if (isAreaFlat(level, center, 7, 2)) {
                    TeyvatStructureManager.placeNbtStructure(level, center, "teyvat_plains_shrine", -6, 0, -6);
                    spawnTeyvatMonster(level, center.offset(0, 2, 7), rand.nextBoolean() ? ModEntities.RUIN_HUNTER.get() : ModEntities.RUIN_GUARD.get());
                    data.regionalStructures.add(center);
                    data.setDirty();
                    return;
                }
            }
        }

        // Los árboles se generan ahora 100% de forma nativa mediante el pipeline de worldgen de biomas (minecraft:trees_*)
    }

    private static void generateTeyvatBiomeTrees(ServerLevel level, int chunkX, int chunkZ, String biomeName, RandomSource rand) {
        int chunkCenterX = chunkX * 16 + 8;
        int chunkCenterZ = chunkZ * 16 + 8;

        // Zona Sagrada de Levantaviento (Windrise): prohibido generar árboles en 120 bloques a la redonda del Gran Roble
        if (Math.abs(chunkCenterX - 90) < 120 && Math.abs(chunkCenterZ - (-140)) < 120) {
            return;
        }

        TeyvatFeaturesData data = TeyvatFeaturesData.get(level.getServer());
        int attempts = 3 + rand.nextInt(3);
        for (int i = 0; i < attempts; i++) {
            int tx = chunkX * 16 + rand.nextInt(16);
            int tz = chunkZ * 16 + rand.nextInt(16);
            int ty = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(tx, 0, tz)).getY();
            if (ty < 60 || ty > 190) continue;
            BlockPos treeBase = new BlockPos(tx, ty, tz);

            // Nunca generar árboles encima ni cerca de arenas de jefes, santuarios ni monumentos
            if (!isFarFromList(treeBase, data.bossArenas, 25.0D)
                    || !isFarFromList(treeBase, data.shrinesOfDepths, 18.0D)
                    || !isFarFromList(treeBase, data.monuments, 10.0D)) {
                continue;
            }

            BlockState soil = level.getBlockState(treeBase.below());
            if (!soil.is(BlockTags.DIRT) && !soil.is(Blocks.GRASS_BLOCK) && !soil.is(Blocks.MOSS_BLOCK) && !soil.is(Blocks.PODZOL)) {
                if (!(biomeName.contains("karst") || biomeName.contains("mountain") || biomeName.contains("cliff") || biomeName.contains("chasm") || biomeName.contains("mine")) ||
                    (!soil.is(Blocks.STONE) && !soil.is(Blocks.SANDSTONE) && !soil.is(Blocks.COARSE_DIRT))) {
                    continue;
                }
            }
            if (level.getFluidState(treeBase).is(net.minecraft.world.level.material.Fluids.WATER) || soil.liquid()) continue;

            if (biomeName.contains("dharma") || biomeName.contains("sumeru") || biomeName.contains("rainforest") || biomeName.contains("jungle")) {
                TeyvatRegionalBuilder.generateAdhigamaTree(level, treeBase, rand);
            } else if (biomeName.contains("chasm") || biomeName.contains("mine")) {
                generateChasmQuarryFeatures(level, treeBase, rand);
            } else if (biomeName.contains("karst") || biomeName.contains("mountain") || biomeName.contains("cliff") || biomeName.contains("dragonspine")) {
                TeyvatRegionalBuilder.generateSandbearerTree(level, treeBase, rand);
            } else if (biomeName.contains("whispering") || biomeName.contains("woods") || biomeName.contains("plains")
                    || biomeName.contains("meadow") || biomeName.contains("dawn_winery") || biomeName.contains("forest")
                    || biomeName.contains("narukami") || biomeName.contains("vale") || biomeName.contains("marsh")
                    || biomeName.contains("cider") || biomeName.contains("windrise")) {
                TeyvatRegionalBuilder.generateCuihuaTree(level, treeBase, rand);
            }
        }
    }

    public static boolean isFarFromList(BlockPos pos, List<BlockPos> list, double minDist) {
        double sq = minDist * minDist;
        for (BlockPos p : list) {
            if (p.distSqr(pos) < sq) return false;
        }
        return true;
    }

    /**
     * Construcción de la sagrada Estatua de los Siete (Statue of The Seven)
     */
    public static void generateStatueOfTheSeven(ServerLevelAccessor level, BlockPos center) {
        buildSolidFoundation(level, center, 4);

        // Base escalonada de cuarzo y piedra lisa pulida
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= 9) {
                    level.setBlock(center.offset(dx, 0, dz), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
                if (distSq <= 4) {
                    level.setBlock(center.offset(dx, 1, dz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        // 4 Pilares perimetrales con linternas de almas celestiales
        int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] c : corners) {
            for (int dy = 1; dy <= 3; dy++) {
                level.setBlock(center.offset(c[0], dy, c[1]), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
            }
            level.setBlock(center.offset(c[0], 4, c[1]), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        // Pedestal y efigie del Arconte
        for (int y = 2; y <= 4; y++) {
            level.setBlock(center.above(y), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
        }
        level.setBlock(center.above(5), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(center.above(6), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
        level.setBlock(center.above(7), Blocks.SEA_LANTERN.defaultBlockState(), 2);

        AwakeningSystemMod.LOGGER.info("Estatua de los Siete erigida en Teyvat en {}", center);
    }

    /**
     * Construcción de Punto de Teletransporte (Teleport Waypoint)
     */
    public static void generateTeleportWaypoint(ServerLevelAccessor level, BlockPos center) {
        buildSolidFoundation(level, center, 2);

        // Plataforma circular de adoquín y piedra cincelada
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 4) {
                    level.setBlock(center.offset(dx, 0, dz), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);

        // Bloque del Teletransporte Celestial
        level.setBlock(center.above(), SystemBlocks.CELESTIAL_WAYPOINT.get().defaultBlockState(), 3);
    }

    /**
     * Valida que un área circular de terreno sea suficientemente plana antes de generar una estructura.
     */
    public static boolean isAreaFlat(ServerLevelAccessor level, BlockPos center, int radius, int maxAllowedDelta) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                if (dx * dx + dz * dz <= radius * radius) {
                    int y = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(center.getX() + dx, 0, center.getZ() + dz)).getY();
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        return (maxY - minY) <= maxAllowedDelta;
    }

    /**
     * Construcción de Campamento Hilichurl fiel a Teyvat:
     * Asentado sobre relieve natural suave, hoguera apagada decorativa con caldero (no daña a los mobs),
     * vasijas y cofres anclados al suelo sólido real y defensores Hilichurl y Mitachurl.
     */
    public static void generateHilichurlCamp(ServerLevelAccessor level, BlockPos center, RandomSource rand) {
        // 1. Allanado suave y orgánico del terreno (sin levantar columnas artificiales de piedra en el aire)
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= 36) {
                    BlockPos surf = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(center.getX() + dx, 0, center.getZ() + dz));
                    BlockPos ground = surf.below();
                    BlockState s = level.getBlockState(ground);
                    if (s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS)) {
                        BlockState pathState = (distSq <= 9) ? Blocks.DIRT_PATH.defaultBlockState()
                                : ((dx + dz) % 2 == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                        level.setBlock(ground, pathState, 2);
                    }
                    for (int dy = 0; dy <= 4; dy++) {
                        BlockPos airP = surf.above(dy);
                        if (TeyvatStructureManager.isVegetationOrTreeBlock(level.getBlockState(airP))) {
                            level.setBlock(airP, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 2. Hoguera Central Apagada (LIT = false) con Caldero y Asientos de Troncos (Inofensiva para los mobs)
        BlockPos firePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center);
        level.setBlock(firePos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(firePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 2);
        level.setBlock(firePos.above(), Blocks.CAULDRON.defaultBlockState(), 2);

        int[][] seatOffsets = { {-2, 0}, {2, 0}, {0, -2}, {0, 2} };
        for (int[] so : seatOffsets) {
            BlockPos seatP = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center.offset(so[0], 0, so[1]));
            level.setBlock(seatP, SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        }

        // 3. Choza Tribal Hilichurl adaptada al relieve del suelo
        BlockPos hutBase = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center.offset(4, 0, 2));
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos colBase = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, hutBase.offset(x, 0, z));
                if (x == 0 || x == 3 || z == 0 || z == 3) {
                    for (int y = 0; y < 3; y++) {
                        level.setBlock(colBase.above(y), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
                    }
                }
                level.setBlock(colBase.above(3), Blocks.HAY_BLOCK.defaultBlockState(), 2);
            }
        }
        // Entrada despejada
        BlockPos doorPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, hutBase.offset(1, 0, 0));
        level.setBlock(doorPos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(doorPos.above(), Blocks.AIR.defaultBlockState(), 2);

        // Cofre protegido en el fondo de la choza
        BlockPos chestGround = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, hutBase.offset(2, 0, 2));
        level.setBlock(chestGround, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
        populateChestLoot(level, chestGround, rand);

        // Vasijas Hilichurl ancladas al suelo sólido real (sin flotar)
        BlockPos jar1 = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, hutBase.offset(1, 0, 2));
        level.setBlock(jar1, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);
        BlockPos jar2 = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, hutBase.offset(2, 0, 1));
        level.setBlock(jar2, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);
        BlockPos jar3 = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center.offset(-3, 0, 1));
        level.setBlock(jar3, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);

        if (rand.nextFloat() < 0.40F) {
            BlockPos moraPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, hutBase.offset(1, 0, 1)).below();
            level.setBlock(moraPos, SystemBlocks.MORA_BLOCK.get().defaultBlockState(), 2);
        }

        // 4. Invocación de patrulla sobre el suelo firme
        int hilichurlCount = 2 + rand.nextInt(2);
        for (int i = 0; i < hilichurlCount; i++) {
            BlockPos hPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center.offset(rand.nextInt(5) - 2, 0, rand.nextInt(5) - 2));
            spawnTeyvatMonster(level, hPos, ModEntities.HILICHURL.get());
        }
        BlockPos mPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center.offset(rand.nextInt(3) - 1, 0, rand.nextInt(3) - 1));
        spawnTeyvatMonster(level, mPos, ModEntities.MITACHURL.get());

        if (rand.nextFloat() < 0.20F) {
            BlockPos lPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, center.offset(3, 0, -3));
            spawnTeyvatMonster(level, lPos, ModEntities.STONEHIDE_LAWACHURL.get());
        }
    }

    /**
     * Decoración ambiental de La Sima (The Chasm): cantos rodados, maderos de soporte minero,
     * pilas de piedra y escombros rocosos para enriquecer el terreno.
     */
    private static void generateChasmQuarryFeatures(ServerLevel level, BlockPos base, RandomSource rand) {
        int roll = rand.nextInt(3);
        if (roll == 0) {
            // Roca o cúmulo de cantos rodados de cantera
            int rad = 1 + rand.nextInt(2);
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    if (dx * dx + dz * dz <= rad * rad) {
                        BlockPos p = base.offset(dx, 0, dz);
                        BlockState stoneType = rand.nextBoolean() ? Blocks.COBBLESTONE.defaultBlockState()
                                : (rand.nextBoolean() ? Blocks.ANDESITE.defaultBlockState() : Blocks.TUFF.defaultBlockState());
                        level.setBlock(p, stoneType, 2);
                        if (dx == 0 && dz == 0 && rand.nextBoolean()) {
                            level.setBlock(p.above(), Blocks.STONE_SLAB.defaultBlockState(), 2);
                        }
                    }
                }
            }
        } else if (roll == 1) {
            // Madero minero de soporte con linterna
            int h = 2 + rand.nextInt(2);
            for (int dy = 0; dy < h; dy++) {
                level.setBlock(base.above(dy), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            }
            level.setBlock(base.above(h), Blocks.LANTERN.defaultBlockState(), 2);
        } else {
            // Tronco caído / madero descompuesto
            BlockState logSt = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
            for (int dx = -1; dx <= 1; dx++) {
                level.setBlock(base.offset(dx, 0, 0), logSt, 2);
            }
        }
    }

    /**
     * Construcción de Corriente de Viento de Anemo (Updraft)
     */
    public static void generateWindUpdraft(ServerLevelAccessor level, BlockPos center) {
        buildSolidFoundation(level, center, 2);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(center.offset(dx, 0, dz), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
            }
        }
        level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
    }

    /**
     * Genera un Cofre Salvaje de Teyvat en ubicaciones escénicas
     */
    public static void generateWildChest(ServerLevelAccessor level, BlockPos center, RandomSource rand) {
        level.setBlock(center, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        BlockPos chestPos = center.above();
        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, false), 3);
        populateChestLoot(level, chestPos, rand);
    }

    /**
     * Genera un Monumento Elemental de Teyvat con pedestal sagrado de piedra ancestral
     */
    public static void generateElementalMonument(ServerLevelAccessor level, BlockPos center, String biomeName, RandomSource rand) {
        buildSolidFoundation(level, center, 3);

        Block monumentBlock;
        String b = biomeName.toLowerCase(java.util.Locale.ROOT);
        if (b.contains("pyro") || b.contains("desert") || b.contains("badlands") || b.contains("volcano") || b.contains("nether")) {
            monumentBlock = SystemBlocks.PYRO_MONUMENT.get();
        } else if (b.contains("cryo") || b.contains("dragonspine") || b.contains("snow") || b.contains("ice") || b.contains("frost")) {
            monumentBlock = SystemBlocks.CRYO_MONUMENT.get();
        } else if (b.contains("electro") || b.contains("narukami") || b.contains("inazuma") || b.contains("yashiori") || b.contains("thunder")) {
            monumentBlock = SystemBlocks.ELECTRO_MONUMENT.get();
        } else if (b.contains("dendro") || b.contains("dharma") || b.contains("sumeru") || b.contains("jungle") || b.contains("forest")) {
            monumentBlock = SystemBlocks.DENDRO_MONUMENT.get();
        } else if (b.contains("hydro") || b.contains("watatsumi") || b.contains("fontaine") || b.contains("river") || b.contains("ocean") || b.contains("beach")) {
            monumentBlock = SystemBlocks.HYDRO_MONUMENT.get();
        } else if (b.contains("geo") || b.contains("karst") || b.contains("mountain") || b.contains("stone") || b.contains("chasm") || b.contains("guyun")) {
            monumentBlock = SystemBlocks.GEO_MONUMENT.get();
        } else {
            monumentBlock = SystemBlocks.ANEMO_MONUMENT.get();
        }

        // Construir pedestal de 3x3 de piedra cincelada y musgosa
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                level.setBlock(p, (dx == 0 && dz == 0) ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
            }
        }

        // 4 esquinas con losas de ladrillos de piedra
        level.setBlock(center.offset(-1, 0, -1), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);
        level.setBlock(center.offset(1, 0, -1), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);
        level.setBlock(center.offset(-1, 0, 1), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);
        level.setBlock(center.offset(1, 0, 1), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);

        // Despejar el aire sobre el altar
        for (int dy = 1; dy <= 3; dy++) {
            level.setBlock(center.above(dy), Blocks.AIR.defaultBlockState(), 2);
        }

        // Colocar el monumento con orientación aleatoria
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
        level.setBlock(center.above(1), monumentBlock.defaultBlockState().setValue(ElementalMonumentBlock.FACING, facing), 3);
    }

    /**
     * Genera monumentales Pilares Kársticos de Jueyun (Liyue Karsts)
     */
    public static void generateJueyunKarstPillars(ServerLevelAccessor level, BlockPos center, RandomSource rand) {
        int pillarHeight = 35 + rand.nextInt(25);
        int radius = 3 + rand.nextInt(3);

        for (int y = 0; y <= pillarHeight; y++) {
            int currentY = center.getY() + y;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        BlockPos p = new BlockPos(center.getX() + dx, currentY, center.getZ() + dz);
                        BlockState stone = (dx * dx + dz * dz > (radius - 1) * (radius - 1)) ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                        level.setBlock(p, stone, 2);
                    }
                }
            }
        }

        // Cima del pilar con césped, árboles de cerezo o ginkgo y cofres
        int topY = center.getY() + pillarHeight + 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    BlockPos p = new BlockPos(center.getX() + dx, topY, center.getZ() + dz);
                    level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        // Árbol decorativo en la cima del pilar kárstico
        BlockPos treePos = new BlockPos(center.getX(), topY + 1, center.getZ());
        level.setBlock(treePos, Blocks.CHERRY_LOG.defaultBlockState(), 2);
        level.setBlock(treePos.above(), Blocks.CHERRY_LOG.defaultBlockState(), 2);
        level.setBlock(treePos.above(2), Blocks.CHERRY_LOG.defaultBlockState(), 2);
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                for (int oy = 2; oy <= 4; oy++) {
                    if (Math.abs(ox) + Math.abs(oz) <= 3) {
                        BlockPos lp = treePos.offset(ox, oy, oz);
                        if (level.getBlockState(lp).isAir()) {
                            level.setBlock(lp, Blocks.CHERRY_LEAVES.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Cofre valioso secreto en la cumbre
        BlockPos summitChest = treePos.offset(1, 0, 1);
        level.setBlock(summitChest, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, false), 3);
        populateChestLoot(level, summitChest, rand);
    }

    private static void populateChestLoot(ServerLevelAccessor level, BlockPos pos, RandomSource rand) {
        var be = level.getBlockEntity(pos);
        if (be instanceof LockedTreasureChestBlockEntity chest) {
            // Protogemas (Primogems)
            int primos = 15 + rand.nextInt(25);
            chest.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), primos));

            // Moras / Monedas
            int mora = 3000 + rand.nextInt(7000);
            chest.setItem(1, new ItemStack(SystemItems.MORA.get(), Math.min(64, mora / 200)));

            // Artefacto aleatorio de Genshin Impact
            ItemStack artifact = createRandomGenshinArtifact(rand);
            if (!artifact.isEmpty()) {
                chest.setItem(2, artifact);
            }

            // Materiales de ascensión o mineral de mejora
            chest.setItem(3, new ItemStack(Items.IRON_INGOT, 2 + rand.nextInt(4)));
            chest.setItem(4, new ItemStack(Items.EMERALD, 1 + rand.nextInt(3)));

            // Materiales de Teyvat (Máscaras, Condensados y Fragmentos Cósmicos)
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                chest.setItem(5, new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), 5 + rand.nextInt(10)));
            }
            if (SystemItems.STAINED_MASK.isBound() && rand.nextBoolean()) {
                chest.setItem(6, new ItemStack(SystemItems.STAINED_MASK.get(), 1 + rand.nextInt(2)));
            } else if (SystemItems.DAMAGED_MASK.isBound()) {
                chest.setItem(6, new ItemStack(SystemItems.DAMAGED_MASK.get(), 2 + rand.nextInt(3)));
            }
        }
    }

    private static ItemStack createRandomGenshinArtifact(RandomSource rand) {
        try {
            var items = net.minecraft.core.registries.BuiltInRegistries.ITEM;
            List<GenshinArtifactItem> arts = new ArrayList<>();
            for (var it : items) {
                if (it instanceof GenshinArtifactItem art) {
                    arts.add(art);
                }
            }
            if (!arts.isEmpty()) {
                GenshinArtifactItem chosen = arts.get(rand.nextInt(arts.size()));
                return new ItemStack(chosen);
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    public static void spawnTeyvatMonster(ServerLevelAccessor level, BlockPos pos, EntityType<?> type) {
        try {
            int actualY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
            if (actualY <= level.getMinBuildHeight() + 1 || actualY >= level.getMaxBuildHeight() - 5) return;
            BlockPos groundPos = new BlockPos(pos.getX(), actualY, pos.getZ());
            BlockState below = level.getBlockState(groundPos.below());
            if (!below.isSolid() || below.is(BlockTags.LEAVES) || !level.getFluidState(groundPos).isEmpty()) return;

            var ent = type.create(level.getLevel());
            if (ent instanceof Monster monster) {
                monster.moveTo(groundPos.getX() + 0.5D, groundPos.getY(), groundPos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(monster);
            }
        } catch (Exception ignored) {}
    }

    public static void buildSolidFoundation(ServerLevelAccessor level, BlockPos center, int radius) {
        // 1. Limpieza cilíndrica exhaustiva de árboles y vegetación (hasta 35 bloques de altura)
        TeyvatStructureManager.clearArenaCylinder(level, center, radius, 35);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    BlockPos p = center.offset(dx, -1, dz);
                    // Rellenar suavemente hasta el suelo natural (máximo 8 bloques para no crear columnas gigantes)
                    for (int dy = 0; dy <= 8; dy++) {
                        BlockPos down = p.below(dy);
                        BlockState s = level.getBlockState(down);
                        if (s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS) && s.getFluidState().isEmpty() && !s.is(BlockTags.REPLACEABLE)
                                && !TeyvatStructureManager.isVegetationOrTreeBlock(s)) {
                            break;
                        }
                        if (dy == 0) {
                            level.setBlock(down, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                        } else if (dy <= 2) {
                            level.setBlock(down, Blocks.DIRT.defaultBlockState(), 2);
                        } else {
                            level.setBlock(down, Blocks.STONE.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    public static boolean generateHypostasisArena(ServerLevelAccessor level, BlockPos center, String biomeName, RandomSource rand) {
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                if (!isFarFromList(center, data.bossArenas, 420.0D)) {
                    return false;
                }
            }
        } catch (Exception ignored) {}

        if (biomeName.contains("windrise") || biomeName.contains("stormterror")) {
            buildSolidFoundation(level, center, 16);
            boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "arena_anemo_hypostasis", -15, 0, -15);
            if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, center, "anemo_hypos_floor", -15, 0, -15);
            if (placed) {
                level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                spawnBossEntity(level, center.above(3), ModEntities.ANEMO_HYPOSTASIS.get());
                recordBossArena(level, center, "anemo_hypostasis");
                AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Anemo generada en {}", center);
                return true;
            }
        } else if (biomeName.contains("narukami") || biomeName.contains("yashiori")) {
            buildSolidFoundation(level, center, 16);
            boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "arena_electro_hypostasis", -15, 0, -15);
            if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, center, "electro_hypos_floor", -15, 0, -15);
            if (placed) {
                level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                spawnBossEntity(level, center.above(3), ModEntities.ELECTRO_HYPOSTASIS.get());
                recordBossArena(level, center, "electro_hypostasis");
                AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Electro generada en {}", center);
                return true;
            }
        } else if (biomeName.contains("karst") || biomeName.contains("guyun") || biomeName.contains("dihua")) {
            buildSolidFoundation(level, center, 16);
            boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "arena_geo_hypostasis", -15, 0, -15);
            if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, center, "geo_hypostasis_floor_final_fixed", -15, 0, -15);
            if (placed) {
                level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                spawnBossEntity(level, center.above(3), ModEntities.GEO_HYPOSTASIS.get());
                recordBossArena(level, center, "geo_hypostasis");
                AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Geo generada en {}", center);
                return true;
            }
        } else if (biomeName.contains("dragonspine") || biomeName.contains("snowy") || biomeName.contains("frozen") || biomeName.contains("ice")) {
            buildSolidFoundation(level, center, 14);
            if (rand.nextBoolean()) {
                // Cueva y Arena Glacial de Regisvid Criogénica (Cryo Regisvine NBT)
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "cryoregisvine_final", -11, 0, -12);
                if (placed) {
                    level.setBlock(center, Blocks.BLUE_ICE.defaultBlockState(), 2);
                    spawnBossEntity(level, center.above(1), ModEntities.CRYO_REGISVINE.get());
                    recordBossArena(level, center, "cryo_regisvine");
                    AwakeningSystemMod.LOGGER.info("Cueva y Arena de Regisvid Criogénica generada en {}", center);
                    return true;
                }
            } else {
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "arena_cryo_hypostasis", -15, 0, -15);
                if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, center, "cryo_hypos_floor", -15, 0, -15);
                if (placed) {
                    level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, center.above(3), ModEntities.CRYO_HYPOSTASIS.get());
                    recordBossArena(level, center, "cryo_hypostasis");
                    AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Cryo generada en {}", center);
                    return true;
                }
            }
        } else if (biomeName.contains("desert")) {
            buildSolidFoundation(level, center, 16);
            if (rand.nextBoolean()) {
                // Cueva y Arena Magmática de Regisvid Pirotécnica (Pyro Regisvine NBT)
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "pyroregisvine_final", -11, 0, -12);
                if (placed) {
                    level.setBlock(center, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                    spawnBossEntity(level, center.above(1), ModEntities.PYRO_REGISVINE.get());
                    recordBossArena(level, center, "pyro_regisvine");
                    AwakeningSystemMod.LOGGER.info("Cueva y Arena de Regisvid Pirotécnica generada en {}", center);
                    return true;
                }
            } else {
                boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "arena_pyro_hypostasis", -15, 0, -15);
                if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, center, "pyro_hypos_floor", -15, 0, -15);
                if (placed) {
                    level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, center.above(3), ModEntities.PYRO_HYPOSTASIS.get());
                    recordBossArena(level, center, "pyro_hypostasis");
                    AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Pyro generada en {}", center);
                    return true;
                }
            }
        } else if (biomeName.contains("watatsumi") || biomeName.contains("fontaine")) {
            int subY = Math.min(28, center.getY() - 35);
            if (subY < 12) subY = 24;
            BlockPos subCenter = new BlockPos(center.getX(), subY, center.getZ());
            TeyvatStructureManager.clearArenaCylinder(level, subCenter, 14, 16);
            boolean placed = TeyvatStructureManager.placeNbtStructure(level, subCenter, "arena_hydro_hypostasis", -9, 0, -8);
            if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, subCenter, "hydro_hypos_bunker", -9, 0, -8);
            if (placed) {
                level.setBlock(subCenter, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                spawnBossEntity(level, subCenter.above(3), ModEntities.HYDRO_HYPOSTASIS.get());
                recordBossArena(level, center, "hydro_hypostasis");
                AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Hydro subterránea generada en {}", subCenter);
                return true;
            }
        } else if (biomeName.contains("sumeru") || biomeName.contains("rainforest") || biomeName.contains("jungle") || biomeName.contains("dendro")) {
            buildSolidFoundation(level, center, 16);
            boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, "arena_dendro_hypostasis", -15, 0, -15);
            if (!placed) placed = TeyvatStructureManager.placeNbtStructure(level, center, "dendro_hypos_floor", -15, 0, -15);
            if (placed) {
                level.setBlock(center, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                spawnBossEntity(level, center.above(3), ModEntities.DENDRO_HYPOSTASIS.get());
                recordBossArena(level, center, "dendro_hypostasis");
                AwakeningSystemMod.LOGGER.info("Arena de Hipóstasis Dendro generada en {}", center);
                return true;
            }
        }
        return false;
    }

    private static void recordBossArena(ServerLevelAccessor level, BlockPos pos, String name) {
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                data.bossArenas.add(pos);
                data.recordNamedStructure(name, pos);
                data.setDirty();
            }
        } catch (Exception ignored) {}
    }

    public static boolean generateLegendaryDungeon(ServerLevelAccessor level, BlockPos center, String biomeName, RandomSource rand) {
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                if (!isFarFromList(center, data.regionalStructures, 500.0D)) {
                    return false;
                }
            }
        } catch (Exception ignored) {}

        // 1. Inazuma (Narukami, Yashiori, Watatsumi): Tenshukaku o Plano de la Eutimia de Baal
        if (biomeName.contains("narukami") || biomeName.contains("yashiori") || biomeName.contains("watatsumi")) {
            if (rand.nextBoolean()) {
                buildSolidFoundation(level, center, 22);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, center, "electriccastle_fixed", -21, 0, -18);
                if (ok) {
                    spawnBossEntity(level, center.above(2), ModEntities.RAIDEN_SHOGUN_BOSS.get());
                    recordRegional(level, center, "tenshukaku");
                    AwakeningSystemMod.LOGGER.info("Castillo Tenshukaku de Inazuma generado en {}", center);
                    return true;
                }
            } else {
                buildSolidFoundation(level, center, 20);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, center, "baal_realm", -18, 0, -18);
                if (ok) {
                    spawnBossEntity(level, center.above(2), ModEntities.RAIDEN_SHOGUN_BOSS.get());
                    recordRegional(level, center, "baal_realm");
                    AwakeningSystemMod.LOGGER.info("Plano de la Eutimia de Baal generado en {}", center);
                    return true;
                }
            }
        }
        // 2. Liyue Karst / Guyun: Casa Dorada o Caverna Subterránea de Azhdaha
        else if (biomeName.contains("karst") || biomeName.contains("guyun") || biomeName.contains("dihua")) {
            if (rand.nextBoolean()) {
                buildSolidFoundation(level, center, 41);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, center, "golden_house", -40, 0, -39);
                if (ok) {
                    spawnBossEntity(level, center.offset(0, 7, 4), ModEntities.CHILDE_BOSS.get());
                    recordRegional(level, center, "golden_house");
                    AwakeningSystemMod.LOGGER.info("Casa Dorada de Liyue generada en {}", center);
                    return true;
                }
            } else {
                int azhdahaY = 28;
                BlockPos azhdahaCenter = new BlockPos(center.getX(), azhdahaY, center.getZ());
                TeyvatStructureManager.clearArenaCylinder(level, azhdahaCenter, 36, 25);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, azhdahaCenter, "azhdaha_arena_higher", -37, 0, -32);
                if (ok) {
                    spawnBossEntity(level, azhdahaCenter.above(1), ModEntities.AZHDAHA_BOSS.get());
                    recordRegional(level, center, "azhdaha_arena");
                    AwakeningSystemMod.LOGGER.info("Caverna Subterránea de Azhdaha generada en {}", azhdahaCenter);
                    return true;
                }
            }
        }
        // 3. Desierto de Sumeru: Templo Primigenio del Desierto
        else if (biomeName.contains("desert")) {
            buildSolidFoundation(level, center, 34);
            boolean ok = TeyvatStructureManager.placeNbtStructure(level, center, "primal_temple", -33, 0, -33);
            if (ok) {
                spawnTeyvatMonster(level, center.offset(0, 2, 10), ModEntities.RUIN_GUARD.get());
                recordRegional(level, center, "primal_temple");
                AwakeningSystemMod.LOGGER.info("Templo Primigenio del Desierto generado en {}", center);
                return true;
            }
        }
        // 4. Enkanomiya / Océano: Arena Flotante del Wolflord
        else if (biomeName.contains("enkanomiya") || biomeName.contains("ocean") || biomeName.contains("beach")) {
            buildSolidFoundation(level, center, 32);
            boolean ok = TeyvatStructureManager.placeNbtStructure(level, center, "wolflord_structure_final", -31, 0, -29);
            if (ok) {
                spawnBossEntity(level, center.above(6), ModEntities.GOLDEN_WOLFLORD_BOSS.get());
                recordRegional(level, center, "wolflord");
                AwakeningSystemMod.LOGGER.info("Arena Flotante del Wolflord generada en {}", center);
                return true;
            }
        }
        // 5. Bosques oscuros / Chasm / Whispering: Árbol Marchito del Abismo
        else if (biomeName.contains("chasm") || biomeName.contains("whispering") || biomeName.contains("dark")) {
            buildSolidFoundation(level, center, 18);
            boolean ok = TeyvatStructureManager.placeNbtStructure(level, center, "tumor_tree", -17, 0, -17);
            if (ok) {
                spawnTeyvatMonster(level, center.offset(2, 1, 2), ModEntities.ABYSS_MAGE_PYRO.get());
                spawnTeyvatMonster(level, center.offset(-2, 1, -2), ModEntities.ABYSS_MAGE_CRYO.get());
                recordRegional(level, center, "tumor_tree");
                AwakeningSystemMod.LOGGER.info("Árbol Marchito del Abismo generado en {}", center);
                return true;
            }
        }
        // 6. Mondstadt / Windrise / Cider Lake: Isla Flotante Celestial de Anemo (Y 170-200)
        else if (biomeName.contains("windrise") || biomeName.contains("cider") || biomeName.contains("plains")) {
            int skyY = Math.min(205, Math.max(170, center.getY() + 75));
            BlockPos skyCenter = new BlockPos(center.getX(), skyY, center.getZ());
            boolean ok = TeyvatStructureManager.placeNbtStructure(level, skyCenter, "floating_island_anemo", -9, 0, -16);
            if (ok) {
                BlockPos chestPos = skyCenter.above(2);
                level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
                populateShrineChestLoot(level, chestPos, rand);
                recordRegional(level, center, "floating_island_anemo");
                AwakeningSystemMod.LOGGER.info("Isla Flotante Celestial de Anemo generada en {}", skyCenter);
                return true;
            }
        }

        return false;
    }

    private static void recordRegional(ServerLevelAccessor level, BlockPos pos, String name) {
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                data.regionalStructures.add(pos);
                data.recordNamedStructure(name, pos);
                data.setDirty();
            }
        } catch (Exception ignored) {}
    }

    public static boolean generateElementalMonumentPuzzle(ServerLevelAccessor level, BlockPos center, String biomeName, RandomSource rand) {
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                if (!isFarFromList(center, data.monuments, 120.0D)) {
                    return false;
                }
            }
        } catch (Exception ignored) {}

        String puzzleNbt;
        String b = biomeName.toLowerCase(java.util.Locale.ROOT);
        if (b.contains("pyro") || b.contains("desert") || b.contains("badlands") || b.contains("volcano")) {
            puzzleNbt = "pyro_monument_spawning";
        } else if (b.contains("cryo") || b.contains("dragonspine") || b.contains("snow") || b.contains("ice")) {
            puzzleNbt = "cryo_monument_spawning";
        } else if (b.contains("electro") || b.contains("narukami") || b.contains("yashiori") || b.contains("inazuma")) {
            puzzleNbt = "electro_monument_spawning";
        } else if (b.contains("dendro") || b.contains("dharma") || b.contains("sumeru") || b.contains("jungle")) {
            puzzleNbt = "dendro_monument_spawning";
        } else if (b.contains("hydro") || b.contains("watatsumi") || b.contains("fontaine") || b.contains("river") || b.contains("ocean")) {
            puzzleNbt = "hydro_monument_spawning";
        } else if (b.contains("geo") || b.contains("karst") || b.contains("mountain") || b.contains("chasm") || b.contains("guyun")) {
            puzzleNbt = "geo_monument_spawning";
        } else {
            puzzleNbt = "anemo_monument_spawning";
        }

        buildSolidFoundation(level, center, 4);
        boolean placed = TeyvatStructureManager.placeNbtStructure(level, center, puzzleNbt, -2, 0, -2);
        if (placed) {
            // Asegurar que el bloque central sea el ElementalMonumentBlock del elemento
            generateElementalMonument(level, center, biomeName, rand);

            try {
                if (level.getLevel().getServer() != null) {
                    var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                    data.monuments.add(center);
                    data.recordNamedStructure(puzzleNbt, center);
                    data.setDirty();
                }
            } catch (Exception ignored) {}
            AwakeningSystemMod.LOGGER.info("Puzzle de Monumento Elemental ({}) generado en {}", puzzleNbt, center);
            return true;
        }

        generateElementalMonument(level, center, biomeName, rand);
        try {
            if (level.getLevel().getServer() != null) {
                var data = TeyvatFeaturesData.get(level.getLevel().getServer());
                data.monuments.add(center);
                data.setDirty();
            }
        } catch (Exception ignored) {}
        return true;
    }

    public static boolean generateShrineOfDepths(ServerLevelAccessor level, BlockPos center, String biomeName, RandomSource rand) {
        String shrineNbt = null;
        if (biomeName.contains("windrise") || biomeName.contains("whispering") || biomeName.contains("cider") || biomeName.contains("dawn_winery") || biomeName.contains("stormterror")) {
            shrineNbt = "anemo_shrine_of_depths_final";
        } else if (biomeName.contains("karst") || biomeName.contains("jueyun") || biomeName.contains("dihua") || biomeName.contains("guyun") || biomeName.contains("chasm") || biomeName.contains("chenyu")) {
            shrineNbt = "geo_shrine_of_depths_final";
        } else if (biomeName.contains("narukami") || biomeName.contains("watatsumi") || biomeName.contains("yashiori")) {
            shrineNbt = "electro_shrine_of_depths_final";
        } else if (biomeName.contains("dharma") || biomeName.contains("sumeru")) {
            shrineNbt = "dendro_shrine_of_depths_final";
        }

        if (shrineNbt != null) {
            int yC = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ());
            int y1 = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() - 5, center.getZ() - 5);
            int y2 = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() + 5, center.getZ() - 5);
            int y3 = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() - 5, center.getZ() + 5);
            int y4 = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() + 5, center.getZ() + 5);

            int minY = Math.min(yC, Math.min(Math.min(y1, y2), Math.min(y3, y4)));
            int maxY = Math.max(yC, Math.max(Math.max(y1, y2), Math.max(y3, y4)));
            if (maxY - minY > 4) {
                // Terreno demasiado empinado, descartar para evitar santuario flotante
                return false;
            }

            BlockPos solidCenter = new BlockPos(center.getX(), minY, center.getZ());
            buildSolidFoundation(level, solidCenter, 6);
            boolean placed = TeyvatStructureManager.placeNbtStructure(level, solidCenter, shrineNbt, -6, 0, -6);
            if (placed) {
                // Colocar y poblar el cofre lujoso sellado en el centro del santuario
                BlockPos chestPos = solidCenter.above(1);
                level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
                populateShrineChestLoot(level, chestPos, rand);
                AwakeningSystemMod.LOGGER.info("Santuario de las Profundidades ({}) generado en {}", shrineNbt, solidCenter);
                return true;
            }
        }
        return false;
    }

    private static void populateShrineChestLoot(ServerLevelAccessor level, BlockPos pos, RandomSource rand) {
        var be = level.getBlockEntity(pos);
        if (be instanceof LockedTreasureChestBlockEntity chest) {
            // 40 Protogemas canónicas de Santuario de las Profundidades
            chest.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), 40));

            // Destinos Entrelazados / Encuentros del Destino (1 o 2 tiradas de gacha)
            if (SystemItems.INTERTWINED_FATE.isBound()) {
                chest.setItem(1, new ItemStack(SystemItems.INTERTWINED_FATE.get(), 1 + rand.nextInt(2)));
            } else if (SystemItems.ACQUAINT_FATE.isBound()) {
                chest.setItem(1, new ItemStack(SystemItems.ACQUAINT_FATE.get(), 1 + rand.nextInt(2)));
            }

            // Monedas de Mora masivas (Bolsa o Fardo de Mora)
            if (SystemItems.MORA_BUNDLE.isBound()) {
                chest.setItem(2, new ItemStack(SystemItems.MORA_BUNDLE.get(), 1));
            } else {
                chest.setItem(2, new ItemStack(SystemItems.MORA.get(), 64));
            }

            // Artefacto Dorado (5 estrellas) de Genshin Impact
            ItemStack artifact = createRandomGenshinArtifact(rand);
            if (!artifact.isEmpty()) {
                chest.setItem(3, artifact);
            }

            // Lingotes y gemas preciosas de ascensión
            chest.setItem(4, new ItemStack(Items.DIAMOND, 2 + rand.nextInt(3)));
            chest.setItem(5, new ItemStack(Items.GOLD_INGOT, 8 + rand.nextInt(9)));

            // Insignias y Fragmentos Cósmicos
            if (SystemItems.COSMIC_FRAGMENT.isBound()) {
                chest.setItem(6, new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), 15 + rand.nextInt(15)));
            }
        }
    }

    public static void spawnBossEntity(ServerLevelAccessor level, BlockPos pos, EntityType<?> type) {
        try {
            var ent = type.create(level.getLevel());
            if (ent != null) {
                ent.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
                if (ent instanceof net.minecraft.world.entity.Mob mob) {
                    mob.setPersistenceRequired();
                    CompoundTag tag = mob.getPersistentData();
                    tag.putDouble("arena_center_x", pos.getX() + 0.5D);
                    tag.putDouble("arena_center_y", (double) pos.getY());
                    tag.putDouble("arena_center_z", pos.getZ() + 0.5D);
                    tag.putDouble("arena_max_radius", 35.0D);
                }
                level.addFreshEntity(ent);
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al spawnear boss en {}: ", pos, e);
        }
    }

    /**
     * Permite invocar / generar al instante cualquier estructura emblemática de Teyvat con sus jefes y cofres.
     */
    public static boolean spawnTeyvatStructure(ServerLevel level, BlockPos pos, String name) {
        String key = name.toLowerCase(java.util.Locale.ROOT).trim();
        TeyvatFeaturesData data = TeyvatFeaturesData.get(level.getServer());

        switch (key) {
            case "golden_house", "childe", "tartaglia", "casa_dorada" -> {
                buildSolidFoundation(level, pos, 41);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "golden_house", -40, 0, -39);
                if (ok) {
                    BlockPos bossPos = pos.offset(0, 7, 4);
                    spawnBossEntity(level, bossPos, ModEntities.CHILDE_BOSS.get());
                    data.bossArenas.add(bossPos);
                    data.recordNamedStructure("golden_house", bossPos);
                    data.recordNamedStructure("childe", bossPos);
                    data.recordNamedStructure("tartaglia", bossPos);
                    return true;
                }
            }
            case "tenshukaku", "raiden", "raiden_shogun", "baal", "shogun", "electriccastle", "electric_castle" -> {
                buildSolidFoundation(level, pos, 22);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "electriccastle_fixed", -21, 0, -18);
                if (ok) {
                    spawnBossEntity(level, pos.above(2), ModEntities.RAIDEN_SHOGUN_BOSS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("tenshukaku", pos);
                    data.recordNamedStructure("raiden", pos);
                    data.recordNamedStructure("shogun", pos);
                    data.recordNamedStructure("inazuma", pos);
                    return true;
                }
            }
            case "wolflord", "golden_wolflord", "rey_acechador", "acechador", "enkanomiya" -> {
                buildSolidFoundation(level, pos, 32);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "wolflord_structure_final", -31, 0, -29);
                if (ok) {
                    spawnBossEntity(level, pos.above(6), ModEntities.GOLDEN_WOLFLORD_BOSS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("wolflord", pos);
                    data.recordNamedStructure("enkanomiya", pos);
                    return true;
                }
            }
            case "azhdaha", "azhdaha_arena", "dominio_azhdaha" -> {
                buildSolidFoundation(level, pos, 38);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "azhdaha_arena_higher", -37, 0, -32);
                if (ok) {
                    spawnBossEntity(level, pos.above(1), ModEntities.AZHDAHA_BOSS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("azhdaha", pos);
                    return true;
                }
            }
            case "anemo_hypostasis", "anemo_cube", "hipostasis_anemo" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_anemo_hypostasis", -15, 0, -15);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, pos, "anemo_hypos_floor", -15, 0, -15);
                if (ok) {
                    level.setBlock(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, pos.above(3), ModEntities.ANEMO_HYPOSTASIS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("anemo_hypostasis", pos);
                    return true;
                }
            }
            case "electro_hypostasis", "electro_cube", "hipostasis_electro" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_electro_hypostasis", -15, 0, -15);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, pos, "electro_hypos_floor", -15, 0, -15);
                if (ok) {
                    level.setBlock(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, pos.above(3), ModEntities.ELECTRO_HYPOSTASIS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("electro_hypostasis", pos);
                    return true;
                }
            }
            case "geo_hypostasis", "geo_cube", "hipostasis_geo" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_geo_hypostasis", -15, 0, -15);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, pos, "geo_hypostasis_floor_final_fixed", -15, 0, -15);
                if (ok) {
                    level.setBlock(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, pos.above(3), ModEntities.GEO_HYPOSTASIS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("geo_hypostasis", pos);
                    return true;
                }
            }
            case "cryo_hypostasis", "cryo_cube", "hipostasis_cryo" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_cryo_hypostasis", -15, 0, -15);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, pos, "cryo_hypos_floor", -15, 0, -15);
                if (ok) {
                    level.setBlock(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, pos.above(3), ModEntities.CRYO_HYPOSTASIS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("cryo_hypostasis", pos);
                    return true;
                }
            }
            case "pyro_hypostasis", "pyro_cube", "hipostasis_pyro" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_pyro_hypostasis", -15, 0, -15);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, pos, "pyro_hypos_floor", -15, 0, -15);
                if (ok) {
                    level.setBlock(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, pos.above(3), ModEntities.PYRO_HYPOSTASIS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("pyro_hypostasis", pos);
                    return true;
                }
            }
            case "hydro_hypostasis", "hydro_cube", "hipostasis_hydro" -> {
                int subY = pos.getY() > 40 ? Math.min(28, pos.getY() - 35) : pos.getY();
                if (subY < 12) subY = 24;
                BlockPos subPos = new BlockPos(pos.getX(), subY, pos.getZ());
                TeyvatStructureManager.clearArenaCylinder(level, subPos, 14, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, subPos, "arena_hydro_hypostasis", -9, 0, -8);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, subPos, "hydro_hypos_bunker", -9, 0, -8);
                if (ok) {
                    level.setBlock(subPos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                    spawnBossEntity(level, subPos.above(3), ModEntities.HYDRO_HYPOSTASIS.get());
                    data.bossArenas.add(subPos);
                    data.recordNamedStructure("hydro_hypostasis", subPos);
                    return true;
                }
            }
            case "dendro_hypostasis", "dendro_cube", "hipostasis_dendro" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "arena_dendro_hypostasis", -15, 0, -15);
                if (!ok) ok = TeyvatStructureManager.placeNbtStructure(level, pos, "dendro_hypos_floor", -15, 0, -15);
                if (ok) {
                    level.setBlock(pos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    spawnBossEntity(level, pos.above(3), ModEntities.DENDRO_HYPOSTASIS.get());
                    data.bossArenas.add(pos);
                    data.recordNamedStructure("dendro_hypostasis", pos);
                    return true;
                }
            }
            case "cryo_regisvine", "regisvine", "regisvid", "regisvid_cryo" -> {
                buildSolidFoundation(level, pos, 16);
                for (int dx = -14; dx <= 14; dx++) {
                    for (int dz = -14; dz <= 14; dz++) {
                        if (dx * dx + dz * dz <= 14 * 14) {
                            BlockState floor = (dx * dx + dz * dz <= 6 * 6) ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
                            level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                        }
                    }
                }
                level.setBlock(pos, Blocks.BLUE_ICE.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(1), ModEntities.CRYO_REGISVINE.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("cryo_regisvine", pos);
                return true;
            }
            case "pyro_regisvine", "regisvine_pyro", "regisvid_pyro" -> {
                buildSolidFoundation(level, pos, 16);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "pyroregisvine_final", -11, 0, -12);
                if (!ok) {
                    for (int dx = -14; dx <= 14; dx++) {
                        for (int dz = -14; dz <= 14; dz++) {
                            if (dx * dx + dz * dz <= 14 * 14) {
                                BlockState floor = (dx * dx + dz * dz <= 6 * 6) ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BASALT.defaultBlockState();
                                level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                            }
                        }
                    }
                }
                level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(1), ModEntities.PYRO_REGISVINE.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("pyro_regisvine", pos);
                return true;
            }
            case "electro_regisvine", "regisvine_electro", "regisvid_electro" -> {
                buildSolidFoundation(level, pos, 16);
                for (int dx = -14; dx <= 14; dx++) {
                    for (int dz = -14; dz <= 14; dz++) {
                        if (dx * dx + dz * dz <= 14 * 14) {
                            BlockState floor = (dx * dx + dz * dz <= 6 * 6) ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.PURPLE_CONCRETE.defaultBlockState();
                            level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                        }
                    }
                }
                level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(1), ModEntities.ELECTRO_REGISVINE.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("electro_regisvine", pos);
                return true;
            }
            case "snowboar_king", "gran_rey_jabali", "great_snowboar_king" -> {
                buildSolidFoundation(level, pos, 16);
                for (int dx = -14; dx <= 14; dx++) {
                    for (int dz = -14; dz <= 14; dz++) {
                        if (dx * dx + dz * dz <= 14 * 14) {
                            BlockState floor = (dx * dx + dz * dz <= 6 * 6) ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
                            level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                        }
                    }
                }
                level.setBlock(pos, Blocks.BLUE_ICE.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(1), ModEntities.GREAT_SNOWBOAR_KING.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("great_snowboar_king", pos);
                return true;
            }
            case "oceanid", "rhodeia", "oceanida" -> {
                buildSolidFoundation(level, pos, 18);
                for (int dx = -16; dx <= 16; dx++) {
                    for (int dz = -16; dz <= 16; dz++) {
                        if (dx * dx + dz * dz <= 16 * 16) {
                            BlockState floor = (dx * dx + dz * dz <= 8 * 8) ? Blocks.WATER.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState();
                            level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                        }
                    }
                }
                level.setBlock(pos, Blocks.PRISMARINE.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(2), ModEntities.RHODEIA_OCEANID.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("rhodeia_oceanid", pos);
                return true;
            }
            case "perpetual_mechanical_array", "pma", "matriz_mecanica" -> {
                buildSolidFoundation(level, pos, 16);
                for (int dx = -14; dx <= 14; dx++) {
                    for (int dz = -14; dz <= 14; dz++) {
                        if (dx * dx + dz * dz <= 14 * 14) {
                            BlockState floor = (dx * dx + dz * dz <= 6 * 6) ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
                            level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                        }
                    }
                }
                level.setBlock(pos, Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(2), ModEntities.PERPETUAL_MECHANICAL_ARRAY.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("perpetual_mechanical_array", pos);
                return true;
            }
            case "ruin_serpent", "serpiente_ruinas", "serpiente_de_las_ruinas" -> {
                buildSolidFoundation(level, pos, 18);
                for (int dx = -16; dx <= 16; dx++) {
                    for (int dz = -16; dz <= 16; dz++) {
                        if (dx * dx + dz * dz <= 16 * 16) {
                            BlockState floor = (dx * dx + dz * dz <= 7 * 7) ? Blocks.MUD.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
                            level.setBlock(pos.offset(dx, 0, dz), floor, 2);
                        }
                    }
                }
                level.setBlock(pos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                spawnBossEntity(level, pos.above(1), ModEntities.RUIN_SERPENT.get());
                data.bossArenas.add(pos);
                data.recordNamedStructure("ruin_serpent", pos);
                return true;
            }
            case "ruin_grader", "demoledor_ruinas" -> {
                buildSolidFoundation(level, pos, 12);
                spawnTeyvatMonster(level, pos.above(1), ModEntities.RUIN_GRADER.get());
                data.regionalStructures.add(pos);
                data.recordNamedStructure("ruin_grader", pos);
                return true;
            }
            case "ruin_drake", "dragon_ruinas" -> {
                buildSolidFoundation(level, pos, 12);
                spawnTeyvatMonster(level, pos.above(1), ModEntities.RUIN_DRAKE.get());
                data.regionalStructures.add(pos);
                data.recordNamedStructure("ruin_drake", pos);
                return true;
            }
            case "frostarm_lawachurl", "cacique_hielo" -> {
                buildSolidFoundation(level, pos, 10);
                spawnTeyvatMonster(level, pos.above(1), ModEntities.FROSTARM_LAWACHURL.get());
                data.regionalStructures.add(pos);
                data.recordNamedStructure("frostarm_lawachurl", pos);
                return true;
            }
            case "thunderhelm_lawachurl", "cacique_electro" -> {
                buildSolidFoundation(level, pos, 10);
                spawnTeyvatMonster(level, pos.above(1), ModEntities.THUNDERHELM_LAWACHURL.get());
                data.regionalStructures.add(pos);
                data.recordNamedStructure("thunderhelm_lawachurl", pos);
                return true;
            }
            case "anemo_shrine", "santuario_anemo", "shrine_anemo" -> {
                buildSolidFoundation(level, pos, 7);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "anemo_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
                    populateShrineChestLoot(level, chestPos, level.getRandom());
                    data.shrinesOfDepths.add(pos);
                    data.recordNamedStructure("anemo_shrine", pos);
                    return true;
                }
            }
            case "geo_shrine", "santuario_geo", "shrine_geo" -> {
                buildSolidFoundation(level, pos, 7);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "geo_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
                    populateShrineChestLoot(level, chestPos, level.getRandom());
                    data.shrinesOfDepths.add(pos);
                    data.recordNamedStructure("geo_shrine", pos);
                    return true;
                }
            }
            case "electro_shrine", "santuario_electro", "shrine_electro" -> {
                buildSolidFoundation(level, pos, 7);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "electro_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
                    populateShrineChestLoot(level, chestPos, level.getRandom());
                    data.shrinesOfDepths.add(pos);
                    data.recordNamedStructure("electro_shrine", pos);
                    return true;
                }
            }
            case "dendro_shrine", "santuario_dendro", "shrine_dendro", "shrine", "santuario" -> {
                buildSolidFoundation(level, pos, 7);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "dendro_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(LockedTreasureChestBlock.LOCKED, true), 3);
                    populateShrineChestLoot(level, chestPos, level.getRandom());
                    data.shrinesOfDepths.add(pos);
                    data.recordNamedStructure("dendro_shrine", pos);
                    return true;
                }
            }
            case "statue", "statue_of_the_seven", "estatua", "estatua_de_los_siete" -> {
                generateStatueOfTheSeven(level, pos);
                data.statues.add(pos);
                data.recordNamedStructure("statue", pos);
                return true;
            }
            case "waypoint", "teleport", "teletransporte", "punto_teletransporte" -> {
                generateTeleportWaypoint(level, pos);
                data.waypoints.add(pos);
                data.recordNamedStructure("waypoint", pos);
                return true;
            }
            case "camp", "hilichurl_camp", "campamento", "hilichurls" -> {
                generateHilichurlCamp(level, pos, level.getRandom());
                data.camps.add(pos);
                data.recordNamedStructure("camp", pos);
                return true;
            }
            case "fix_arenas", "clean_arenas", "fix_structures", "clean" -> {
                int totalPurged = 0;
                // 1. Limpiar la arena actual donde se encuentra el jugador
                totalPurged += TeyvatStructureManager.clearArenaCylinder(level, pos, 22, 35);

                // 2. Limpiar todas las arenas de jefes registradas en Teyvat
                for (BlockPos arenaPos : data.bossArenas) {
                    totalPurged += TeyvatStructureManager.clearArenaCylinder(level, arenaPos, 20, 35);
                }

                // 3. Limpiar también Windrise si existe
                BlockPos windrisePos = data.namedStructures.getOrDefault("windrise", new BlockPos(90, 68, -140));
                totalPurged += TeyvatStructureManager.purgeParasiteTreesInsideWindrise(level, windrisePos);

                AwakeningSystemMod.LOGGER.info("Purga de estructuras completada: {} bloques invasores eliminados", totalPurged);
                return true;
            }
            case "cuihua", "arbol_cuihua", "cuihua_tree", "fruta_del_ocaso", "sunsettia_tree" -> {
                return TeyvatRegionalBuilder.generateCuihuaTree(level, pos, level.getRandom());
            }
            case "adhigama", "arbol_adhigama", "adhigama_tree" -> {
                return TeyvatRegionalBuilder.generateAdhigamaTree(level, pos, level.getRandom());
            }
            case "sandbearer", "arbol_arena", "sandbearer_tree" -> {
                return TeyvatRegionalBuilder.generateSandbearerTree(level, pos, level.getRandom());
            }
            case "fix_windrise", "clean_windrise" -> {
                BlockPos targetPos = pos;
                if (data.namedStructures.containsKey("windrise")) {
                    targetPos = data.namedStructures.get("windrise");
                } else if (Math.abs(pos.getX() - 90) < 150 && Math.abs(pos.getZ() - (-140)) < 150) {
                    targetPos = new BlockPos(90, pos.getY(), -140);
                }
                int purged = TeyvatStructureManager.purgeParasiteTreesInsideWindrise(level, targetPos);
                AwakeningSystemMod.LOGGER.info("Windrise purga manual ejecutada en {}: {} bloques eliminados", targetPos, purged);
                return true;
            }
            case "windrise", "roble", "tree", "arbol_sagrado", "windrise_giant_tree" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "windrise_giant_tree", -71, -2, -69);
                if (ok) {
                    TeyvatStructureManager.purgeParasiteTreesInsideWindrise(level, pos);
                    generateStatueOfTheSeven(level, pos.offset(0, 0, 16));
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("windrise", pos);
                    return true;
                }
            }
            case "jade_chamber", "camara_jade" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "jade_chamber", -15, 0, -15);
                if (ok) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("jade_chamber", pos);
                    return true;
                }
            }
            case "beidou_ship", "alcor", "barco_beidou" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "beidou_ship", -16, 0, -16);
                if (ok) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("beidou_ship", pos);
                    return true;
                }
            }
            case "dawn_winery", "winery", "vinedo", "vinedo_amanecer" -> {
                buildSolidFoundation(level, pos, 25);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "dawn_winery", -24, 0, -24);
                if (ok) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("dawn_winery", pos);
                    return true;
                }
            }
            case "witch_island", "isla_bruja" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "witch_island", -25, 0, -25);
                if (ok) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("witch_island", pos);
                    return true;
                }
            }
            case "teyvat_treehouses", "treehouses", "aldea_arbol" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_treehouses", -20, 0, -20);
                if (ok) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("teyvat_treehouses", pos);
                    return true;
                }
            }
            case "primal_temple", "templo_primigenio", "piramide" -> {
                buildSolidFoundation(level, pos, 34);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "primal_temple", -33, 0, -33);
                if (ok) {
                    spawnTeyvatMonster(level, pos.offset(0, 1, 10), ModEntities.RUIN_GUARD.get());
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("primal_temple", pos);
                    return true;
                }
            }
            case "chasm_mine", "mina_sima", "chasm" -> {
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "teyvat_chasm_mine", -24, 0, -24);
                if (ok) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure("chasm_mine", pos);
                    return true;
                }
            }
            case "karst", "pilar_karstico", "jueyun_karst" -> {
                generateJueyunKarstPillars(level, pos, level.getRandom());
                data.regionalStructures.add(pos);
                data.recordNamedStructure("karst", pos);
                return true;
            }
            case "updraft", "viento", "corriente_viento" -> {
                generateWindUpdraft(level, pos);
                data.updrafts.add(pos);
                data.recordNamedStructure("updraft", pos);
                return true;
            }
            case "monument", "monumento", "anemo_monument", "geo_monument", "electro_monument", "dendro_monument", "hydro_monument", "pyro_monument", "cryo_monument" -> {
                generateElementalMonument(level, pos, key, level.getRandom());
                data.monuments.add(pos);
                data.recordNamedStructure("monument", pos);
                return true;
            }
            case "abyss", "spiral_abyss", "musk_reef", "espiral_abismo", "arrecife_musk" -> {
                TeyvatAbyssPortalBuilder.generateMuskReefIsland(level, pos, Direction.SOUTH, TeyvatWorldManager.isAbyssActive());
                data.regionalStructures.add(pos);
                data.recordNamedStructure("abyss", pos);
                return true;
            }
            default -> {
                if (TeyvatStructureManager.placeNbtStructure(level, pos, key, 0, 0, 0)) {
                    data.regionalStructures.add(pos);
                    data.recordNamedStructure(key, pos);
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
