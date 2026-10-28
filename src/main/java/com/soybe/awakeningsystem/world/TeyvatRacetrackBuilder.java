package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.UmaBookmakerBlock;
import com.soybe.awakeningsystem.uma.TeyvatRacetrackManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Constructor de la estructura física del Gran Hipódromo de Teyvat.
 * Permite cargar estructuras exportadas por el usuario (/racetrack export) o generar la pista procedural oficial.
 */
public final class TeyvatRacetrackBuilder {

    private static volatile boolean racetrackBuilt = false;

    private TeyvatRacetrackBuilder() {}

    public static void forceRebuild(ServerLevel level) {
        TeyvatStructureManager.clearCache();
        racetrackBuilt = false;
        net.minecraft.server.MinecraftServer server = level.getServer();
        if (server != null) {
            try {
                var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                data.setRacetrackBuilt(false);
                data.setRacetrackTerrainCleaned(false);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error resetting TeyvatAbyssSavedData for racetrack: ", e);
            }
        }
        level.setBlock(new BlockPos(120, 76, 235), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(120, 75, 235), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(125, 75, 235), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(120, 76, 231), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(120, 76, 234), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(120, 76, 236), Blocks.AIR.defaultBlockState(), 2);

        for (int l = 0; l < 9; l++) {
            BlockPos p = new BlockPos(135, 68, 231 - 4 + l);
            if (level.getBlockState(p).getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            }
            BlockPos up = p.above();
            if (level.getBlockState(up).getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
                level.setBlock(up, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        cleanupRacetrackParasiticTerrain(level, TeyvatRacetrackManager.TRACK_CENTER);
        buildRacetrackAt(level, TeyvatRacetrackManager.TRACK_CENTER);

        racetrackBuilt = true;
        if (server != null) {
            try {
                var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                data.setRacetrackBuilt(true);
                data.setRacetrackTerrainCleaned(true);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for racetrack: ", e);
            }
        }
    }

    public static Set<BlockPos> getValidTrackPositions(BlockPos origin) {
        Set<BlockPos> validPositions = new HashSet<>(20000);
        try (InputStream in = TeyvatStructureManager.class.getResourceAsStream("/data/awakening_system/structures/uma_racetrack.nbt")) {
            if (in == null) {
                try (InputStream inAlt = TeyvatStructureManager.class.getResourceAsStream("/data/awakening_system/structure/uma_racetrack.nbt")) {
                    if (inAlt != null) {
                        readTrackPositionsFromStream(inAlt, origin, validPositions);
                    }
                }
            } else {
                readTrackPositionsFromStream(in, origin, validPositions);
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("[Hipódromo] Error al leer uma_racetrack.nbt para filtrado: ", e);
        }
        return validPositions;
    }

    private static void readTrackPositionsFromStream(InputStream in, BlockPos origin, Set<BlockPos> validPositions) throws Exception {
        CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        ListTag blocksList = tag.getList("blocks", Tag.TAG_COMPOUND);
        int offX = origin.getX() - 95;
        int offY = origin.getY() - 3;
        int offZ = origin.getZ() - 50;
        for (int i = 0; i < blocksList.size(); i++) {
            CompoundTag bTag = blocksList.getCompound(i);
            ListTag posList = bTag.getList("pos", Tag.TAG_INT);
            int dx = posList.getInt(0);
            int dy = posList.getInt(1);
            int dz = posList.getInt(2);
            validPositions.add(new BlockPos(offX + dx, offY + dy, offZ + dz));
        }
    }

    public static void cleanupRacetrackParasiticTerrain(ServerLevel level, BlockPos origin) {
        if (origin == null) origin = TeyvatRacetrackManager.TRACK_CENTER;
        AwakeningSystemMod.LOGGER.info("[Hipódromo] Saneando terreno parásito (caja de montaña en el mar) en ({}, {}, {})...",
                origin.getX(), origin.getY(), origin.getZ());

        int minX = origin.getX() - 95;
        int maxX = minX + 191;
        int minZ = origin.getZ() - 50;
        int maxZ = minZ + 101;
        int seaLevel = 63;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }

        Set<BlockPos> validTrackPositions = getValidTrackPositions(origin);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int oceanFloor = Math.max(level.getMinBuildHeight() + 2, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z));
                for (int y = 92; y >= 50; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (validTrackPositions.contains(p)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(p);

                    if (state.isAir()) {
                        if (x >= 115 && y <= seaLevel && y >= oceanFloor) {
                            level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                        }
                        continue;
                    }

                    if (state.is(Blocks.WATER)) {
                        continue;
                    }

                    if (state.getBlock() instanceof com.soybe.awakeningsystem.block.UmaBookmakerBlock
                            || state.getBlock() instanceof com.soybe.awakeningsystem.block.StartingGateBlock
                            || state.getBlock() instanceof com.soybe.awakeningsystem.block.RacetrackBleachersBlock) {
                        continue;
                    }

                    boolean isParasitic = isParasiticTerrainBlock(state);

                    if (isParasitic) {
                        if (y > seaLevel) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                        }
                    } else if (x >= 115) {
                        if (isResidualTerrainInSea(state)) {
                            if (y > seaLevel) {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            } else {
                                level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
        AwakeningSystemMod.LOGGER.info("[Hipódromo] ¡Terreno parásito purgado exitosamente y océano restaurado a la normalidad!");
    }

    private static boolean isParasiticTerrainBlock(BlockState s) {
        if (s == null) return false;
        return s.is(Blocks.DIORITE) || s.is(Blocks.POLISHED_DIORITE)
                || s.is(Blocks.ANDESITE) || s.is(Blocks.POLISHED_ANDESITE)
                || s.is(Blocks.GRANITE) || s.is(Blocks.POLISHED_GRANITE)
                || s.is(Blocks.CHERRY_LEAVES) || s.is(Blocks.CHERRY_LOG)
                || s.is(Blocks.STRIPPED_CHERRY_LOG) || s.is(Blocks.CHERRY_WOOD)
                || s.is(Blocks.PINK_PETALS) || s.is(Blocks.PURPLE_TERRACOTTA)
                || s.is(Blocks.PURPLE_CONCRETE) || s.is(Blocks.PURPLE_WOOL)
                || s.is(Blocks.WHITE_TERRACOTTA) || s.is(Blocks.RED_TERRACOTTA);
    }

    private static boolean isResidualTerrainInSea(BlockState s) {
        if (s == null) return false;
        return s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.STONE)
                || s.is(Blocks.COBBLESTONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.SANDSTONE)
                || s.is(Blocks.CLAY) || s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.TERRACOTTA);
    }

    public static Path getExportFilePath() {
        return FMLPaths.CONFIGDIR.get().resolve("awakening_system/racetrack_custom_export.json");
    }

    public static boolean hasExportedTrack() {
        return Files.exists(getExportFilePath());
    }

    public static boolean buildExportedTrack(ServerLevel level) {
        return buildCustomOrEmbeddedTrack(level);
    }

    public static boolean buildCustomOrEmbeddedTrack(ServerLevel level) {
        Path path = getExportFilePath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (buildTrackFromJson(json, level)) {
                    AwakeningSystemMod.LOGGER.info("[Teyvat Hipódromo] Pista personalizada cargada exitosamente desde archivo de configuración: {}", path);
                    return true;
                }
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.error("[Teyvat Hipódromo] Error al cargar desde config: ", e);
            }
        }
        return false;
    }

    public static boolean buildTrackFromJson(JsonObject json, ServerLevel level) {
        if (json == null) return false;
        int cx = json.get("centerX").getAsInt();
        int cy = json.get("centerY").getAsInt();
        int cz = json.get("centerZ").getAsInt();
        JsonArray blocks = json.getAsJsonArray("blocks");
        if (blocks == null || blocks.isEmpty()) return false;

        java.util.List<JsonObject> list = new java.util.ArrayList<>(blocks.size());
        for (JsonElement el : blocks) {
            if (el.isJsonObject()) {
                list.add(el.getAsJsonObject());
            }
        }
        list.sort(java.util.Comparator.comparingInt(b -> b.get("y").getAsInt()));

        java.util.List<BlockPos> placedBleachers = new java.util.ArrayList<>();

        for (JsonObject bObj : list) {
            int dx = bObj.get("x").getAsInt();
            int dy = bObj.get("y").getAsInt();
            int dz = bObj.get("z").getAsInt();
            String blockId = bObj.get("block").getAsString();
            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(blockId));
            if (block == null || block == Blocks.AIR) continue;

            BlockPos pos = new BlockPos(cx + dx, cy + dy, cz + dz);
            BlockState state = block.defaultBlockState();

            if (block instanceof com.soybe.awakeningsystem.block.StartingGateBlock) {
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf half =
                        dy <= 0 ? net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER
                                : net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER;
                state = state.setValue(com.soybe.awakeningsystem.block.StartingGateBlock.FACING, Direction.EAST)
                             .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.HALF, half)
                             .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.OPEN, false)
                             .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.POWERED, false);
                level.setBlock(pos, state, 3);
                continue;
            } else if (block instanceof com.soybe.awakeningsystem.block.RacetrackBleachersBlock) {
                state = state.setValue(com.soybe.awakeningsystem.block.RacetrackBleachersBlock.FACING, Direction.SOUTH);
                placedBleachers.add(pos);
            } else if (block instanceof com.soybe.awakeningsystem.block.UmaBookmakerBlock) {
                state = state.setValue(com.soybe.awakeningsystem.block.UmaBookmakerBlock.FACING, Direction.SOUTH);
            }

            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.PERSISTENT)) {
                state = state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.PERSISTENT, true);
            }

            level.setBlock(pos, state, 2);
        }

        for (BlockPos bPos : placedBleachers) {
            BlockState bState = level.getBlockState(bPos);
            if (bState.getBlock() instanceof com.soybe.awakeningsystem.block.RacetrackBleachersBlock bb) {
                BlockState connected = bb.updateBleacherShape(bState, level, bPos);
                if (connected != bState) {
                    level.setBlock(bPos, connected, 3);
                }
            }
        }

        racetrackBuilt = true;
        AwakeningSystemMod.LOGGER.info("[Teyvat Hipódromo] ¡Pista de carreras generada con {} bloques!", list.size());
        return true;
    }

    public static void buildRacetrackIfAbsent(ServerLevel level) {
        if (racetrackBuilt) return;

        net.minecraft.server.MinecraftServer server = level.getServer();
        if (server != null) {
            try {
                var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                if (!data.isRacetrackTerrainCleaned()) {
                    cleanupRacetrackParasiticTerrain(level, TeyvatRacetrackManager.TRACK_CENTER);
                    data.setRacetrackTerrainCleaned(true);
                    buildRacetrackAt(level, TeyvatRacetrackManager.TRACK_CENTER);
                    data.setRacetrackBuilt(true);
                    racetrackBuilt = true;
                    return;
                }
                if (data.isRacetrackBuilt()) {
                    racetrackBuilt = true;
                    return;
                }
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error checking TeyvatAbyssSavedData for racetrack: ", e);
            }
        }

        BlockPos versionMarker = new BlockPos(120, 76, 231);
        if (level.getBlockState(versionMarker).is(Blocks.BEACON) || level.getBlockState(new BlockPos(120, 68, 231)).is(Blocks.SMOOTH_STONE)) {
            if (server != null) {
                try {
                    var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                    if (!data.isRacetrackTerrainCleaned()) {
                        cleanupRacetrackParasiticTerrain(level, TeyvatRacetrackManager.TRACK_CENTER);
                        data.setRacetrackTerrainCleaned(true);
                        buildRacetrackAt(level, TeyvatRacetrackManager.TRACK_CENTER);
                    }
                    data.setRacetrackBuilt(true);
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for racetrack: ", e);
                }
            }
            racetrackBuilt = true;
            return;
        }

        if (buildCustomOrEmbeddedTrack(level)) {
            if (server != null) {
                try {
                    var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                    data.setRacetrackBuilt(true);
                    data.setRacetrackTerrainCleaned(true);
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for custom racetrack: ", e);
                }
            }
            return;
        }

        cleanupRacetrackParasiticTerrain(level, TeyvatRacetrackManager.TRACK_CENTER);
        buildRacetrackAt(level, TeyvatRacetrackManager.TRACK_CENTER);

        racetrackBuilt = true;
        if (server != null) {
            try {
                var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                data.setRacetrackBuilt(true);
                data.setRacetrackTerrainCleaned(true);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for racetrack: ", e);
            }
        }
    }

    public static void buildRacetrackAt(ServerLevel level, BlockPos origin) {
        if (origin == null) origin = TeyvatRacetrackManager.TRACK_CENTER;

        AwakeningSystemMod.LOGGER.info("[Hipódromo] Construyendo circuito blindado de carreras de Umas en ({}, {}, {})...",
                origin.getX(), origin.getY(), origin.getZ());

        if (TeyvatStructureManager.placeNbtStructure(level, origin, "uma_racetrack", -95, -3, -50)) {
            AwakeningSystemMod.LOGGER.info("[Hipódromo] Estructura NBT 'uma_racetrack.nbt' colocada con éxito.");

            // Cimentación limpia en agua: si la pista cruza mar, proyectar pilares de piedra limpios al lecho marino
            int minX = origin.getX() - 95;
            int maxX = minX + 191;
            int minZ = origin.getZ() - 50;
            int maxZ = minZ + 101;
            int trackFloorY = origin.getY() - 1;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos surfacePos = new BlockPos(x, trackFloorY, z);
                    BlockState sState = level.getBlockState(surfacePos);
                    if (sState.is(Blocks.STONE_BRICKS) || sState.is(Blocks.DIRT_PATH) || sState.is(Blocks.PACKED_MUD) || sState.is(Blocks.COARSE_DIRT)) {
                        int oceanFloor = Math.max(level.getMinBuildHeight() + 2, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z));
                        for (int y = trackFloorY - 1; y >= oceanFloor; y--) {
                            BlockPos fPos = new BlockPos(x, y, z);
                            BlockState cur = level.getBlockState(fPos);
                            if (cur.is(Blocks.WATER) || cur.isAir() || cur.is(Blocks.SEAGRASS) || cur.is(Blocks.TALL_SEAGRASS)) {
                                level.setBlock(fPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }

            upgradeRacetrackBlocks(level, origin);

            racetrackBuilt = true;
            return;
        }

        int offX = origin.getX() - 180;
        int offY = origin.getY() - 68;
        int offZ = origin.getZ() - 260;
        int trackY = origin.getY() - 1;

        TeyvatRacetrackManager manager = TeyvatRacetrackManager.get(level);
        if (manager == null) return;

        int totalWaypoints = manager.getTotalWaypoints();
        Map<Long, Double> columns = new HashMap<>();

        for (int w = 0; w < totalWaypoints; w++) {
            Vec3 p1Raw = manager.getWaypoint(w, 4);
            Vec3 p2Raw = manager.getWaypoint((w + 1) % totalWaypoints, 4);
            if (p1Raw == null || p2Raw == null) continue;

            Vec3 p1 = new Vec3(p1Raw.x + offX, p1Raw.y + offY, p1Raw.z + offZ);
            Vec3 p2 = new Vec3(p2Raw.x + offX, p2Raw.y + offY, p2Raw.z + offZ);

            double dx = p2.x - p1.x;
            double dz = p2.z - p1.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.001) continue;

            double nx = -dz / dist;
            double nz = dx / dist;

            int steps = (int) Math.ceil(dist * 4.0);
            for (int s = 0; s <= steps; s++) {
                double t = (double) s / steps;
                double midX = p1.x + dx * t;
                double midZ = p1.z + dz * t;

                for (double off = -9.5; off <= 9.5; off += 0.30) {
                    int bx = (int) Math.floor(midX + nx * off);
                    int bz = (int) Math.floor(midZ + nz * off);
                    long key = (((long) bx) << 32) | (bz & 0xFFFFFFFFL);

                    double absOff = Math.abs(off);
                    Double prev = columns.get(key);
                    if (prev == null || absOff < prev) {
                        columns.put(key, absOff);
                    }
                }
            }
        }

        for (Map.Entry<Long, Double> entry : columns.entrySet()) {
            long key = entry.getKey();
            int bx = (int) (key >> 32);
            int bz = (int) (key & 0xFFFFFFFFL);
            double dist = entry.getValue();

            for (int y = trackY + 1; y <= trackY + 30; y++) {
                BlockPos airPos = new BlockPos(bx, y, bz);
                if (!level.getBlockState(airPos).isAir()) {
                    level.setBlock(airPos, Blocks.AIR.defaultBlockState(), 2);
                }
            }

            BlockPos floorPos = new BlockPos(bx, trackY, bz);

            int oceanFloor = Math.max(level.getMinBuildHeight() + 2, level.getHeight(Heightmap.Types.OCEAN_FLOOR, bx, bz));
            for (int y = trackY - 1; y >= oceanFloor; y--) {
                BlockPos fPos = new BlockPos(bx, y, bz);
                BlockState current = level.getBlockState(fPos);
                if (current.isAir() || current.is(Blocks.WATER) || current.is(Blocks.SEAGRASS) || current.is(Blocks.TALL_SEAGRASS)) {
                    BlockState foundation = (dist >= 4.8) ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                    level.setBlock(fPos, foundation, 2);
                }
            }

            boolean isStartFinishCorridor = (bx >= (110 + offX) && bx <= (150 + offX) && bz >= (226 + offZ) && bz <= (246 + offZ));

            if (dist > 5.8) {
                level.setBlock(floorPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
            } else if (dist >= 4.8 && !isStartFinishCorridor) {
                level.setBlock(floorPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                level.setBlock(floorPos.above(), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3);
            } else {
                BlockState trackSurface = SystemBlocks.RACETRACK_TURF.isBound()
                        ? SystemBlocks.RACETRACK_TURF.get().defaultBlockState()
                        : Blocks.GRASS_BLOCK.defaultBlockState();
                level.setBlock(floorPos, trackSurface, 2);
                level.setBlock(floorPos.above(), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int ix = 135 + offX; ix <= 195 + offX; ix++) {
            for (int iz = 110 + offZ; iz <= 210 + offZ; iz++) {
                long ikey = (((long) ix) << 32) | (iz & 0xFFFFFFFFL);
                if (!columns.containsKey(ikey)) {
                    BlockPos inPos = new BlockPos(ix, trackY, iz);
                    BlockState inSt = level.getBlockState(inPos);
                    if (inSt.isAir() || inSt.is(Blocks.WATER) || inSt.is(Blocks.SEAGRASS) || inSt.is(Blocks.DIRT)) {
                        level.setBlock(inPos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        level.setBlock(inPos.above(), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int x = 118 + offX; x <= 140 + offX; x++) {
            for (int y = trackY + 1; y <= trackY + 11; y++) {
                for (int z = 220 + offZ; z <= 246 + offZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && (s.is(Blocks.GOLD_BLOCK) || s.is(Blocks.SEA_LANTERN) || s.is(Blocks.QUARTZ_PILLAR)
                            || s.is(Blocks.BEACON) || s.is(Blocks.IRON_BARS) || s.is(Blocks.GREEN_WOOL)
                            || s.is(Blocks.WHITE_WOOL) || s.is(Blocks.SMOOTH_STONE))) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int fx = 120 + offX, fy = trackY + 1, fz = 231 + offZ;
        for (int z = fz - 7; z <= fz + 7; z++) {
            BlockPos linePos = new BlockPos(fx, fy - 1, z);
            BlockState check = ((z % 2) == 0) ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.BLACK_CONCRETE.defaultBlockState();
            level.setBlock(linePos, check, 2);
            level.setBlock(new BlockPos(fx, fy, z), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(new BlockPos(fx, fy + 1, z), Blocks.AIR.defaultBlockState(), 2);
        }
        for (int h = 0; h < 7; h++) {
            level.setBlock(new BlockPos(fx, fy + h, fz - 8), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            level.setBlock(new BlockPos(fx, fy + h, fz + 8), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
        }
        for (int z = fz - 8; z <= fz + 8; z++) {
            level.setBlock(new BlockPos(fx, fy + 7, z), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
            if (z % 2 == 0) {
                level.setBlock(new BlockPos(fx, fy + 6, z), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(fx, fy + 8, fz), Blocks.BEACON.defaultBlockState(), 2);

        int gx = 135 + offX, gy = trackY + 1, gz = 231 + offZ;
        for (int lane = 0; lane < 9; lane++) {
            int laneZ = gz - 4 + lane;
            level.setBlock(new BlockPos(gx, gy - 1, laneZ), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(gx - 1, gy - 1, laneZ), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(gx - 2, gy - 1, laneZ), Blocks.SMOOTH_STONE.defaultBlockState(), 2);

            for (int h = 0; h < 4; h++) {
                level.setBlock(new BlockPos(gx - 3, gy + h, laneZ), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
            }

            for (int x = gx - 2; x <= gx; x++) {
                for (int h = 0; h < 4; h++) {
                    level.setBlock(new BlockPos(x, gy + h, laneZ), Blocks.AIR.defaultBlockState(), 2);
                }
            }

            BlockState roofWool = ((lane % 2) == 0) ? Blocks.GREEN_WOOL.defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState();
            level.setBlock(new BlockPos(gx - 2, gy + 4, laneZ), roofWool, 2);
            level.setBlock(new BlockPos(gx - 1, gy + 4, laneZ), roofWool, 2);
            level.setBlock(new BlockPos(gx, gy + 4, laneZ), roofWool, 2);

            if (SystemBlocks.STARTING_GATE != null) {
                level.setBlock(new BlockPos(gx, gy, laneZ),
                        SystemBlocks.STARTING_GATE.get().defaultBlockState()
                                .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.FACING, Direction.EAST)
                                .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                                .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.OPEN, false), 3);
                level.setBlock(new BlockPos(gx, gy + 1, laneZ),
                        SystemBlocks.STARTING_GATE.get().defaultBlockState()
                                .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.FACING, Direction.EAST)
                                .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)
                                .setValue(com.soybe.awakeningsystem.block.StartingGateBlock.OPEN, false), 3);
            }

            for (int x = gx + 1; x <= gx + 15; x++) {
                for (int h = 0; h < 5; h++) {
                    level.setBlock(new BlockPos(x, gy + h, laneZ), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int x = gx - 3; x <= gx - 1; x++) {
            for (int h = 0; h < 4; h++) {
                level.setBlock(new BlockPos(x, gy + h, gz - 5), Blocks.IRON_BARS.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x, gy + h, gz + 5), Blocks.IRON_BARS.defaultBlockState(), 2);
            }
        }

        for (int x = 117 + offX; x <= 127 + offX; x++) {
            for (int y = trackY + 1; y <= trackY + 8; y++) {
                for (int z = 217 + offZ; z <= 227 + offZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.is(Blocks.RED_WOOL) || s.is(Blocks.WHITE_WOOL) || s.is(Blocks.STRIPPED_DARK_OAK_LOG)
                            || s.is(Blocks.LANTERN) || s.is(Blocks.STONE_BRICK_STAIRS)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int bx = 122 + offX, by = trackY + 1, bz = 216 + offZ;
        for (int x = bx - 4; x <= bx + 4; x++) {
            for (int z = bz - 4; z <= bz + 4; z++) {
                int oceanBed = Math.max(level.getMinBuildHeight() + 2, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z));
                for (int y = by - 1; y >= oceanBed; y--) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }
                for (int y = by; y <= by + 5; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = bx - 2; x <= bx + 2; x++) {
            level.setBlock(new BlockPos(x, by, bz + 5), Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH), 2);
        }
        for (int h = 0; h < 4; h++) {
            level.setBlock(new BlockPos(bx - 3, by + h, bz - 3), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(bx + 3, by + h, bz - 3), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(bx - 3, by + h, bz + 3), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(bx + 3, by + h, bz + 3), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }
        for (int x = bx - 4; x <= bx + 4; x++) {
            for (int z = bz - 4; z <= bz + 4; z++) {
                BlockState wool = ((x + z) % 2 == 0) ? Blocks.RED_WOOL.defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState();
                level.setBlock(new BlockPos(x, by + 4, z), wool, 2);
            }
        }
        level.setBlock(new BlockPos(bx - 2, by + 3, bz - 2), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(bx + 2, by + 3, bz - 2), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(bx, by + 3, bz), Blocks.LANTERN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(bx - 1, by, bz - 1), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(bx + 1, by, bz - 1), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        if (SystemBlocks.UMA_BOOKMAKER != null) {
            level.setBlock(new BlockPos(bx, by, bz - 1),
                    SystemBlocks.UMA_BOOKMAKER.get().defaultBlockState().setValue(UmaBookmakerBlock.FACING, Direction.SOUTH),
                    3);
        }

        if (SystemBlocks.RACETRACK_BLEACHERS != null) {
            for (int x = bx - 2; x <= bx + 2; x++) {
                level.setBlock(new BlockPos(x, by, bz + 2),
                        SystemBlocks.RACETRACK_BLEACHERS.get().defaultBlockState()
                                .setValue(com.soybe.awakeningsystem.block.RacetrackBleachersBlock.FACING, Direction.NORTH), 3);
            }
        }

        AwakeningSystemMod.LOGGER.info("[Hipódromo] ¡Circuito construido con éxito en ({}, {}, {})!",
                origin.getX(), origin.getY(), origin.getZ());
    }

    /**
     * Actualiza y sustituye en vivo los bloques obsoletos del hipódromo en el mundo:
     * - Vallas de ladrillo de piedra (stone_brick_wall) -> Vallas Blancas de Paddock (racetrack_paddock_fence)
     * - Suelo de tierra, caminos y lodo (dirt_path, packed_mud, coarse_dirt, etc.) -> Césped de Competición (racetrack_turf)
     * @return arreglo con [vallasReemplazadas, céspedReemplazado]
     */
    public static int[] upgradeRacetrackBlocks(ServerLevel level, BlockPos origin) {
        if (origin == null) origin = TeyvatRacetrackManager.TRACK_CENTER;
        int minX = origin.getX() - 100;
        int maxX = minX + 205;
        int minZ = origin.getZ() - 60;
        int maxZ = minZ + 125;
        int minY = Math.max(level.getMinBuildHeight() + 2, origin.getY() - 5);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, origin.getY() + 25);

        int wallsCount = 0;
        int turfCount = 0;

        BlockState whiteFence = SystemBlocks.RACETRACK_PADDOCK_FENCE.get().defaultBlockState();
        BlockState turf = SystemBlocks.RACETRACK_TURF.get().defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState st = level.getBlockState(p);

                    // 1. Reemplazo de vallas de piedra por las vallas blancas oficiales del hipódromo
                    if (st.is(Blocks.STONE_BRICK_WALL) || st.is(Blocks.MOSSY_STONE_BRICK_WALL) || st.is(Blocks.COBBLESTONE_WALL)) {
                        level.setBlock(p, whiteFence, 3);
                        wallsCount++;
                    }
                    // 2. Reemplazo de pista de tierra / lodo / pasto sucio en la cota de la pista
                    else if (y >= origin.getY() - 3 && y <= origin.getY() + 3) {
                        if (st.is(Blocks.DIRT_PATH) || st.is(Blocks.PACKED_MUD) || st.is(Blocks.COARSE_DIRT)
                                || st.is(Blocks.ROOTED_DIRT) || st.is(Blocks.MUD) || st.is(Blocks.DIRT)) {
                            level.setBlock(p, turf, 2);
                            turfCount++;
                        }
                    }
                }
            }
        }
        AwakeningSystemMod.LOGGER.info("[Hipódromo] ¡Materiales actualizados en ({}, {}, {})! Vallas: {}, Césped: {}",
                origin.getX(), origin.getY(), origin.getZ(), wallsCount, turfCount);
        return new int[]{wallsCount, turfCount};
    }
}

