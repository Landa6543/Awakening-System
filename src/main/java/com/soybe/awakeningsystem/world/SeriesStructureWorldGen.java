package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.NpcProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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

        public boolean hasStructureNearby(BlockPos pos, ResourceKey<Level> dim, double minDist) {
            double sq = minDist * minDist;
            String dimPath = (dim != null) ? dim.location().getPath() : "overworld";
            for (Map.Entry<String, BlockPos> entry : structures.entrySet()) {
                String k = entry.getKey();
                boolean matchesDim;
                if (k.contains("#")) {
                    String[] parts = k.split("#");
                    matchesDim = parts.length >= 2 && parts[1].equals(dimPath);
                } else {
                    matchesDim = "overworld".equals(dimPath);
                }
                if (matchesDim && entry.getValue().distSqr(pos) < sq) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasStructureNearby(BlockPos pos, double minDist) {
            return hasStructureNearby(pos, Level.OVERWORLD, minDist);
        }

        public boolean hasStructureNearbyOfType(String type, BlockPos pos, double minDist) {
            double sq = minDist * minDist;
            String prefix = type + "_";
            String prefixHash = type + "#";
            for (Map.Entry<String, BlockPos> entry : structures.entrySet()) {
                if (entry.getKey().startsWith(prefix) || entry.getKey().startsWith(prefixHash)) {
                    if (entry.getValue().distSqr(pos) < sq) return true;
                }
            }
            return false;
        }

        public void recordStructure(String type, BlockPos pos, ResourceKey<Level> dim) {
            String dimPath = (dim != null) ? dim.location().getPath() : "overworld";
            structures.put(type + "#" + dimPath + "#" + pos.getX() + "_" + pos.getZ(), pos);
            setDirty();
        }

        public void recordStructure(String type, BlockPos pos) {
            recordStructure(type, pos, Level.OVERWORLD);
        }

        public Map<String, BlockPos> getAll() {
            return structures;
        }
    }

    public static int findSolidSurfaceGround(ServerLevel level, int blockX, int blockZ) {
        int motionBlockingY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
        int oceanFloorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, blockX, blockZ);
        if (motionBlockingY > oceanFloorY) {
            return -1; // Water or fluid is present at the surface
        }

        int startY = Math.min(motionBlockingY + 2, level.getMaxBuildHeight() - 5);
        int minY = Math.max(level.getMinBuildHeight() + 10, 45);

        for (int y = startY; y >= minY; y--) {
            BlockPos pos = new BlockPos(blockX, y, blockZ);
            BlockState state = level.getBlockState(pos);

            if (!state.getFluidState().isEmpty()) return -1;
            if (state.isAir()) continue;
            if (state.is(BlockTags.LEAVES)) continue;
            if (state.is(BlockTags.LOGS)) continue;
            if (state.is(BlockTags.FLOWERS)) continue;
            if (state.is(BlockTags.REPLACEABLE)) continue;

            if (state.isSolid() || state.is(BlockTags.DIRT) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                    || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.TERRACOTTA)
                    || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.RED_SAND) || state.is(Blocks.SANDSTONE)
                    || state.is(BlockTags.BASE_STONE_NETHER) || state.is(Blocks.SNOW_BLOCK)) {
                return y;
            }
        }
        return -1;
    }

    public static int findSuitableFlatTerrain(ServerLevel level, int centerX, int centerZ, int radiusX, int radiusZ, int maxSlopeTolerance) {
        int centerGround = findSolidSurfaceGround(level, centerX, centerZ);
        if (centerGround < 50 || centerGround > 230) return -1;

        int rx = Math.max(2, radiusX);
        int rz = Math.max(2, radiusZ);

        boolean isMurim = level.dimension().equals(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD);

        // 1. Water buffer check: ensure no fluid or water body exists within 3 blocks of the structure footprint (Overworld/surface only; floating islands have natural void drops)
        if (!isMurim) {
            int bufX = rx + 3;
            int bufZ = rz + 3;
            int[][] waterBufferSamples = {
                {-bufX, 0}, {bufX, 0}, {0, -bufZ}, {0, bufZ},
                {-bufX, -bufZ}, {bufX, -bufZ}, {-bufX, bufZ}, {bufX, bufZ}
            };
            for (int[] offset : waterBufferSamples) {
                int sx = centerX + offset[0];
                int sz = centerZ + offset[1];
                int sy = findSolidSurfaceGround(level, sx, sz);
                if (sy < 45) {
                    return -1; // Water or void detected in buffer zone, reject site
                }
            }
        }

        // 2. Interior slope and flatness check (including half-radius samples)
        int[][] samples = {
            {0, 0},
            {-rx, -rz},
            {rx, -rz},
            {-rx, rz},
            {rx, rz},
            {0, -rz},
            {0, rz},
            {-rx, 0},
            {rx, 0},
            {-rx / 2, -rz / 2},
            {rx / 2, -rz / 2},
            {-rx / 2, rz / 2},
            {rx / 2, rz / 2}
        };

        int minY = centerGround;
        int maxY = centerGround;

        for (int[] offset : samples) {
            int sx = centerX + offset[0];
            int sz = centerZ + offset[1];
            int sy = findSolidSurfaceGround(level, sx, sz);
            if (isMurim) {
                if (sy < 50 || sy > 230) {
                    continue; // In floating islands, edge samples near cliffs are safely filled by foundation
                }
            } else {
                if (sy < 50 || sy > 230) {
                    return -1; // Outside bounds or liquid found
                }
            }
            if (sy < minY) minY = sy;
            if (sy > maxY) maxY = sy;

            int effectiveSlopeTol = isMurim ? Math.max(maxSlopeTolerance, 5) : maxSlopeTolerance;
            if (maxY - minY > effectiveSlopeTol) {
                return -1; // Exceeds slope tolerance
            }
        }

        return centerGround;
    }

    public static int findStableSolidGround(ServerLevel level, int blockX, int blockZ, int sampleRadius) {
        return findSuitableFlatTerrain(level, blockX, blockZ, sampleRadius, sampleRadius, 3);
    }

    public static void clearStructureVegetation(ServerLevel level, BlockPos center, int rx, int ry, int rz) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        // Strictly protect the perimeter: only clear interior room volume, never cut trees outside
        for (int x = cx - rx + 1; x <= cx + rx - 1; x++) {
            for (int z = cz - rz + 1; z <= cz + rz - 1; z++) {
                for (int y = cy; y <= cy + ry; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.getFluidState().isEmpty()) continue; // Never destroy water
                    if (s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(BlockTags.FLOWERS)
                            || s.is(BlockTags.REPLACEABLE) || TeyvatStructureManager.isVegetationOrTreeBlock(s)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public static void clearVegetationStrictlyInside(ServerLevel level, BlockPos origin, Vec3i size) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        int sx = size.getX();
        int sy = size.getY();
        int sz = size.getZ();

        for (int x = ox; x < ox + sx; x++) {
            for (int z = oz; z < oz + sz; z++) {
                for (int y = oy; y < oy + sy; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.getFluidState().isEmpty()) continue; // Never destroy water
                    if (s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(BlockTags.FLOWERS)
                            || s.is(BlockTags.REPLACEABLE) || TeyvatStructureManager.isVegetationOrTreeBlock(s)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public static void adaptSurroundingTerrain(ServerLevel level, BlockPos origin, int sizeX, int sizeZ, Direction entranceFacing, String canonicalName) {
        if (canonicalName != null) {
            String c = canonicalName.toLowerCase(Locale.ROOT);
            if (c.equals("orv") || c.equals("elemental_floating_castles") || c.equals("ancestral_immortal_grotto")
                    || c.equals("black_market_den") || c.equals("chasm_mine") || c.equals("beidou_ship") || c.equals("witch_island")) {
                return; // Underground, sky, or ocean structures do not adapt surface land
            }
        }

        Holder<Biome> biomeHolder = level.getBiome(origin);
        String biomeName = biomeHolder.unwrapKey().map(k -> k.location().getPath()).orElse("plains").toLowerCase(Locale.ROOT);

        BlockState topBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState subBlock = Blocks.DIRT.defaultBlockState();

        if (biomeName.contains("desert") || biomeName.contains("beach")) {
            topBlock = Blocks.SAND.defaultBlockState();
            subBlock = Blocks.SANDSTONE.defaultBlockState();
        } else if (biomeName.contains("badlands") || biomeName.contains("red_sand")) {
            topBlock = Blocks.RED_SAND.defaultBlockState();
            subBlock = Blocks.RED_SANDSTONE.defaultBlockState();
        } else if (biomeName.contains("snow") || biomeName.contains("ice") || biomeName.contains("frozen")) {
            topBlock = Blocks.SNOW_BLOCK.defaultBlockState();
            subBlock = Blocks.DIRT.defaultBlockState();
        } else if (biomeName.contains("old_growth") || biomeName.contains("mega_taiga") || biomeName.contains("podzol")) {
            topBlock = Blocks.PODZOL.defaultBlockState();
            subBlock = Blocks.DIRT.defaultBlockState();
        }

        int minX = origin.getX();
        int maxX = origin.getX() + sizeX - 1;
        int minZ = origin.getZ();
        int maxZ = origin.getZ() + sizeZ - 1;
        int floorY = origin.getY();

        // 3-block organic transition berm
        for (int dist = 1; dist <= 3; dist++) {
            for (int x = minX - dist; x <= maxX + dist; x++) {
                for (int z = minZ - dist; z <= maxZ + dist; z++) {
                    int dx = (x < minX) ? (minX - x) : (x > maxX ? (x - maxX) : 0);
                    int dz = (z < minZ) ? (minZ - z) : (z > maxZ ? (z - maxZ) : 0);
                    if (Math.max(dx, dz) != dist) continue;

                    // Skip entrance corridor (handled by entrance path, unless it's estate where we want full natural lawn)
                    if (entranceFacing != null && !"estate".equals(canonicalName)) {
                        int midX = (minX + maxX) / 2;
                        int midZ = (minZ + maxZ) / 2;
                        if (entranceFacing == Direction.NORTH && z < minZ && Math.abs(x - midX) <= 1) continue;
                        if (entranceFacing == Direction.SOUTH && z > maxZ && Math.abs(x - midX) <= 1) continue;
                        if (entranceFacing == Direction.WEST && x < minX && Math.abs(z - midZ) <= 1) continue;
                        if (entranceFacing == Direction.EAST && x > maxX && Math.abs(z - midZ) <= 1) continue;
                    }

                    int groundY = findSolidSurfaceGround(level, x, z);
                    if (groundY < 0) continue; // Never touch water or voids

                    int targetY = floorY - dist;

                    if (groundY < targetY) {
                        // Terrain is lower: build gentle natural slope upward to meet foundation
                        for (int y = groundY + 1; y < targetY; y++) {
                            BlockPos fillPos = new BlockPos(x, y, z);
                            if (level.getBlockState(fillPos).getFluidState().isEmpty()) {
                                level.setBlock(fillPos, subBlock, 2);
                            }
                        }
                        BlockPos topPos = new BlockPos(x, targetY, z);
                        if (level.getBlockState(topPos).getFluidState().isEmpty()) {
                            level.setBlock(topPos, topBlock, 2);
                        }
                        for (int y = targetY + 1; y <= targetY + 3; y++) {
                            BlockPos abovePos = new BlockPos(x, y, z);
                            BlockState as = level.getBlockState(abovePos);
                            if (as.is(BlockTags.REPLACEABLE) || as.is(BlockTags.FLOWERS)) {
                                level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    } else if (groundY > targetY && groundY <= floorY + 2) {
                        // Terrain is slightly higher: gently taper down towards the building perimeter
                        int smoothY = Math.min(groundY, floorY + (dist - 1));
                        if (smoothY < groundY) {
                            for (int y = groundY; y > smoothY; y--) {
                                BlockPos clearPos = new BlockPos(x, y, z);
                                BlockState cs = level.getBlockState(clearPos);
                                if (!cs.is(BlockTags.LOGS) && cs.getFluidState().isEmpty()) {
                                    level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 2);
                                }
                            }
                            BlockPos capPos = new BlockPos(x, smoothY, z);
                            if (level.getBlockState(capPos).getFluidState().isEmpty()) {
                                level.setBlock(capPos, topBlock, 2);
                            }
                        }
                    }

                    // Natural biomic flora softening on the berm
                    int surfaceY = Math.max(groundY, targetY);
                    BlockPos surfPos = new BlockPos(x, surfaceY, z);
                    BlockPos aboveSurf = surfPos.above();
                    if (level.getBlockState(surfPos).is(Blocks.GRASS_BLOCK) && level.getBlockState(aboveSurf).isAir()) {
                        long hash = (long) x * 3129871L ^ (long) z * 116129781L;
                        int roll = (int) Math.abs(hash % 100);
                        if (roll < 8) {
                            level.setBlock(aboveSurf, Blocks.DANDELION.defaultBlockState(), 2);
                        } else if (roll < 15) {
                            level.setBlock(aboveSurf, Blocks.POPPY.defaultBlockState(), 2);
                        } else if (roll < 22) {
                            level.setBlock(aboveSurf, Blocks.CORNFLOWER.defaultBlockState(), 2);
                        } else if (roll < 35) {
                            level.setBlock(aboveSurf, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    public static void generateEntrancePath(ServerLevel level, BlockPos doorThreshold, Direction entranceFacing, String canonicalName) {
        if ("estate".equals(canonicalName)) return; // User request: remove path for estate
        if (entranceFacing == null) entranceFacing = Direction.NORTH;
        int stepX = entranceFacing.getStepX();
        int stepZ = entranceFacing.getStepZ();

        int perpX = (stepX == 0) ? 1 : 0;
        int perpZ = (stepZ == 0) ? 1 : 0;

        int doorY = doorThreshold.getY();

        for (int step = 0; step <= 5; step++) {
            int cx = doorThreshold.getX() + stepX * step;
            int cz = doorThreshold.getZ() + stepZ * step;

            int width = (step <= 2) ? 1 : 0;

            for (int w = -width; w <= width; w++) {
                int px = cx + perpX * w;
                int pz = cz + perpZ * w;

                int groundY = findSolidSurfaceGround(level, px, pz);
                if (groundY < 0) continue; // Never touch water

                // If step == 0 and there is a drop from door, place entrance stairs
                if (step == 0 && groundY < doorY) {
                    BlockPos stairPos = new BlockPos(px, groundY + 1, pz);
                    if (level.getBlockState(stairPos).isAir() || level.getBlockState(stairPos).is(BlockTags.REPLACEABLE)) {
                        BlockState stairState = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, entranceFacing);
                        level.setBlock(stairPos, stairState, 2);
                        continue;
                    }
                }

                BlockPos pathPos = new BlockPos(px, groundY, pz);
                BlockState current = level.getBlockState(pathPos);
                if (current.getFluidState().isEmpty() && !current.isAir()) {
                    long seed = (long) px * 341873128712L + (long) pz * 132897987541L + step;
                    int r = (int) Math.abs(seed % 100);

                    BlockState pathBlock;
                    if (r < 65) {
                        pathBlock = Blocks.DIRT_PATH.defaultBlockState();
                    } else if (r < 85) {
                        pathBlock = Blocks.COARSE_DIRT.defaultBlockState();
                    } else if (r < 95) {
                        pathBlock = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                    } else {
                        pathBlock = Blocks.GRAVEL.defaultBlockState();
                    }

                    level.setBlock(pathPos, pathBlock, 2);

                    BlockPos above1 = pathPos.above();
                    BlockPos above2 = pathPos.above(2);
                    if (level.getBlockState(above1).is(BlockTags.REPLACEABLE) || level.getBlockState(above1).is(BlockTags.FLOWERS)) {
                        level.setBlock(above1, Blocks.AIR.defaultBlockState(), 2);
                    }
                    if (level.getBlockState(above2).is(BlockTags.REPLACEABLE) || level.getBlockState(above2).is(BlockTags.FLOWERS)) {
                        level.setBlock(above2, Blocks.AIR.defaultBlockState(), 2);
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

    public static boolean placeSeriesNbtStructure(ServerLevel level, BlockPos center, String templateName, int offsetY, int clearRadiusX, int clearHeightY, int clearRadiusZ, Runnable fallback) {
        String canonicalName = canonicalizeStructureName(templateName);
        try {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, templateName);
            StructureTemplateManager stm = level.getStructureManager();
            Optional<StructureTemplate> opt = stm.get(id);

            StructureTemplate template = null;
            if (opt.isPresent()) {
                template = opt.get();
            } else {
                String[] paths = {
                        "/data/awakening_system/structures/" + templateName + ".nbt",
                        "/data/awakening_system/structure/" + templateName + ".nbt"
                };
                for (String p : paths) {
                    try (InputStream in = SeriesStructureWorldGen.class.getResourceAsStream(p)) {
                        if (in != null) {
                            CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                            template = new StructureTemplate();
                            template.load(level.holderLookup(Registries.BLOCK), tag);
                            break;
                        }
                    } catch (Exception e) {
                        AwakeningSystemMod.LOGGER.debug("Could not read structure template from {}: {}", p, e.getMessage());
                    }
                }
            }

            if (template != null) {
                Vec3i size = template.getSize();
                int offX = -size.getX() / 2;
                int offZ = -size.getZ() / 2;
                BlockPos origin = center.offset(offX, offsetY, offZ);

                // Strictly clear vegetation only within the template bounding box (never outside)
                clearVegetationStrictlyInside(level, origin, size);

                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setIgnoreEntities(false)
                        .setKnownShape(true)
                        .setMirror(Mirror.NONE)
                        .setRotation(Rotation.NONE);

                template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2 | 16);

                // Gentle organic foundation under solid floor blocks only (max 3 blocks deep, never into water)
                for (int x = 0; x < size.getX(); x++) {
                    for (int z = 0; z < size.getZ(); z++) {
                        BlockPos floorPos = origin.offset(x, 0, z);
                        BlockState floorState = level.getBlockState(floorPos);
                        if (!floorState.isAir() && floorState.isSolid() && floorState.getFluidState().isEmpty()) {
                            for (int dy = 1; dy <= 3; dy++) {
                                BlockPos downPos = floorPos.below(dy);
                                BlockState bs = level.getBlockState(downPos);
                                if (!bs.getFluidState().isEmpty()) {
                                    break;
                                }
                                if (bs.isAir() || bs.is(BlockTags.REPLACEABLE) || bs.is(BlockTags.LEAVES)) {
                                    BlockState foundationBlock = (dy == 1) ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                                    level.setBlock(downPos, foundationBlock, 2);
                                } else if (bs.isSolid()) {
                                    break;
                                }
                            }
                        }
                    }
                }

                // Seamless environmental adaptation and entrance trail
                adaptSurroundingTerrain(level, origin, size.getX(), size.getZ(), Direction.NORTH, canonicalName);
                if (!"estate".equals(canonicalName)) {
                    generateEntrancePath(level, new BlockPos(center.getX(), origin.getY(), origin.getZ()), Direction.NORTH, canonicalName);
                }
                return true;
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al colocar estructura NBT {}: {}", templateName, e.getMessage());
        }

        if (fallback != null) {
            fallback.run();
            int r = getStructureRadius(canonicalName);
            BlockPos origin = center.offset(-r, 0, -r);
            int size = r * 2 + 1;
            adaptSurroundingTerrain(level, origin, size, size, Direction.NORTH, canonicalName);
            if (!"estate".equals(canonicalName)) {
                generateEntrancePath(level, new BlockPos(center.getX(), center.getY(), center.getZ() - r), Direction.NORTH, canonicalName);
            }
        }
        return true;
    }

    public static boolean placeSeriesNbtStructure(ServerLevel level, BlockPos center, String templateName, int clearRadiusX, int clearHeightY, int clearRadiusZ, Runnable fallback) {
        return placeSeriesNbtStructure(level, center, templateName, 0, clearRadiusX, clearHeightY, clearRadiusZ, fallback);
    }

    public static boolean generateStructure(ServerLevel level, BlockPos pos, String structureType) {
        String type = structureType.toLowerCase(java.util.Locale.ROOT);
        switch (type) {
            case "solo_leveling", "solo", "jinwoo", "hunter", "hunter_office" -> {
                placeSeriesNbtStructure(level, pos, "hunter_office", 8, 12, 8, () -> {
                    SeriesStructureBuilder.buildHunterOffice(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + 4), ModEntities.SUNG_JINWOO.get(), NpcProfile.SUNG_JINWOO);
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX() + 4, pos.getY() + 1, pos.getZ() - 3), ModEntities.CHA_HAEIN.get(), NpcProfile.CHA_HAEIN);
                recordPlaced(level, "solo_leveling", pos);
                return true;
            }
            case "orv", "dokja", "subway", "metro", "orv_subway_carriage" -> {
                int actualY = pos.getY() > 45 ? Math.min(25, pos.getY() - 35) : pos.getY();
                if (actualY < 8) actualY = 15;
                BlockPos subwayPos = new BlockPos(pos.getX(), actualY, pos.getZ());
                SeriesStructureBuilder.clearRoom(level, subwayPos.getX() - 8, subwayPos.getX() + 7, subwayPos.getY(), subwayPos.getY() + 9, subwayPos.getZ() - 10, subwayPos.getZ() + 10);
                placeSeriesNbtStructure(level, subwayPos, "orv_subway_carriage", 8, 10, 10, () -> SeriesStructureBuilder.buildSubwayCarriage(level, subwayPos));
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(subwayPos.getX(), subwayPos.getY() + 4, subwayPos.getZ() - 2), ModEntities.KIM_DOKJA.get(), NpcProfile.KIM_DOKJA);
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(subwayPos.getX() + 1, subwayPos.getY() + 4, subwayPos.getZ() + 2), ModEntities.YOO_JOONGHYUK.get(), NpcProfile.YOO_JOONGHYUK);
                recordPlaced(level, "orv", subwayPos);
                return true;
            }
            case "estate", "lloyd", "frontera", "engineering", "frontera_estate" -> {
                placeSeriesNbtStructure(level, pos, "frontera_estate", 8, 16, 8, () -> {
                    SeriesStructureBuilder.buildFronteraEstate(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX() - 4, pos.getY() + 1, pos.getZ() + 3), ModEntities.LLOYD_FRONTERA.get(), NpcProfile.LLOYD_FRONTERA);
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX() + 4, pos.getY() + 1, pos.getZ() - 3), ModEntities.JAVIER_ASRAHAN.get(), NpcProfile.JAVIER_ASRAHAN);
                recordPlaced(level, "estate", pos);
                return true;
            }
            case "nano", "nano_machine", "cult", "demon", "demon_pagoda" -> {
                placeSeriesNbtStructure(level, pos, "demon_pagoda", 8, 16, 8, () -> {
                    SeriesStructureBuilder.buildDemonPagoda(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ() + 2), ModEntities.CHEON_YEOWOON.get(), NpcProfile.CHEON_YEOWOON);
                recordPlaced(level, "nano", pos);
                return true;
            }
            case "tbate", "arthur", "aether", "aether_sanctuary" -> {
                placeSeriesNbtStructure(level, pos, "aether_sanctuary", 11, 16, 11, () -> {
                    clearStructureVegetation(level, pos, 11, 16, 11);
                    SeriesStructureBuilder.buildAetherSanctuary(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + 2), ModEntities.ARTHUR_LEYWIN.get(), NpcProfile.ARTHUR_LEYWIN);
                recordPlaced(level, "tbate", pos);
                return true;
            }
            case "ranker", "second_life", "slr", "cain", "yeonwoo", "tower", "obelisk", "ranker_tower" -> {
                placeSeriesNbtStructure(level, pos, "ranker_tower", 12, 40, 12, () -> {
                    clearStructureVegetation(level, pos, 12, 40, 12);
                    SeriesStructureBuilder.buildRankerTower(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ() + 2), ModEntities.CHA_YEONWOO.get(), NpcProfile.CHA_YEONWOO);
                recordPlaced(level, "ranker", pos);
                return true;
            }
            case "mount_hua", "chung_myung", "plum", "hua", "mount_hua_pavilion" -> {
                placeSeriesNbtStructure(level, pos, "mount_hua_pavilion", 11, 18, 11, () -> {
                    clearStructureVegetation(level, pos, 11, 18, 11);
                    SeriesStructureBuilder.buildMountHuaPavilion(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ() + 1), ModEntities.CHUNG_MYUNG.get(), NpcProfile.CHUNG_MYUNG);
                recordPlaced(level, "mount_hua", pos);
                return true;
            }
            case "valter", "hanbin", "barbarian", "rock_dimension", "valter_camp", "sword_king" -> {
                placeSeriesNbtStructure(level, pos, "valter_camp", 12, 14, 12, () -> {
                    clearStructureVegetation(level, pos, 12, 14, 12);
                    SeriesStructureBuilder.buildValterCamp(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX() + 2, pos.getY() + 2, pos.getZ() - 1), ModEntities.RYU_HANBIN.get(), NpcProfile.RYU_HANBIN);
                recordPlaced(level, "valter", pos);
                return true;
            }
            case "tower_of_god", "baam", "shinsoo", "floor_of_tests", "tog", "shinsoo_shrine" -> {
                placeSeriesNbtStructure(level, pos, "shinsoo_shrine", 12, 18, 12, () -> {
                    clearStructureVegetation(level, pos, 12, 18, 12);
                    SeriesStructureBuilder.buildShinsooShrine(level, pos);
                });
                SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ() + 1), ModEntities.TWENTY_FIFTH_BAAM.get(), NpcProfile.TWENTY_FIFTH_BAAM);
                recordPlaced(level, "tower_of_god", pos);
                return true;
            }
            case "ascension_pavilion", "murim_gate", "murim_pavilion", "ascension", "celestial_pavilion" -> {
                placeSeriesNbtStructure(level, pos, "ascension_pavilion", 0, 10, 16, 10, () -> {
                    clearStructureVegetation(level, pos, 10, 16, 10);
                    SeriesStructureBuilder.buildAscensionPavilion(level, pos);
                });
                recordPlaced(level, "ascension_pavilion", pos);
                return true;
            }
            case "dragon_sanctuary", "dragon_altar", "altar_dragon", "santuario_dragon", "dragon_boss_sanctuary" -> {
                placeSeriesNbtStructure(level, pos, "dragon_sanctuary", 0, 16, 20, 16, () -> {
                    clearStructureVegetation(level, pos, 13, 20, 13);
                    SeriesStructureBuilder.buildDragonSanctuary(level, pos);
                });
                recordPlaced(level, "dragon_sanctuary", pos);
                return true;
            }
            case "roulette_pavilion", "roulette", "fortune", "ruleta", "santuario_ruleta" -> {
                placeSeriesNbtStructure(level, pos, "roulette_pavilion", 0, 10, 16, 10, () -> {
                    clearStructureVegetation(level, pos, 10, 16, 10);
                    SeriesStructureBuilder.buildRoulettePavilion(level, pos);
                });
                SeriesStructureBuilder.populateRoulettePavilion(level, pos);
                recordPlaced(level, "roulette_pavilion", pos);
                return true;
            }
            case "goddess_shrine", "santuario_diosa", "mana_spring", "goddess", "shrine_goddess" -> {
                placeSeriesNbtStructure(level, pos, "goddess_shrine", 0, 11, 18, 11, () -> {
                    clearStructureVegetation(level, pos, 11, 18, 11);
                    SeriesStructureBuilder.buildGoddessShrine(level, pos);
                });
                SeriesStructureBuilder.populateGoddessShrine(level, pos);
                recordPlaced(level, "goddess_shrine", pos);
                return true;
            }
            case "spirit_valley", "valle_bestias", "jade_spring", "valle_jade", "spirit_beast_valley" -> {
                placeSeriesNbtStructure(level, pos, "spirit_valley", -1, 13, 16, 13, () -> {
                    clearStructureVegetation(level, pos, 12, 16, 12);
                    SeriesStructureBuilder.buildSpiritBeastValley(level, pos);
                });
                SeriesStructureBuilder.populateSpiritValley(level, pos);
                recordPlaced(level, "spirit_valley", pos);
                return true;
            }
            case "nine_tribulations_pagoda", "tribulation_pagoda", "pagoda_tribulaciones", "pagoda_9", "pagoda_nueve_tribulaciones" -> {
                placeSeriesNbtStructure(level, pos, "nine_tribulations_pagoda", 0, 14, 45, 14, () -> {
                    clearStructureVegetation(level, pos, 14, 45, 14);
                    SeriesStructureBuilder.buildNineTribulationsPagoda(level, pos);
                });
                SeriesStructureBuilder.populateNineTribulationsPagoda(level, pos);
                recordPlaced(level, "nine_tribulations_pagoda", pos);
                return true;
            }
            case "pagoda_arena", "arena_pagoda", "pagoda_stage", "pagoda_trial_arena" -> {
                com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 1, true);
                recordPlaced(level, "pagoda_arena", pos);
                return true;
            }
            case "pagoda_arena_stage_1" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 1, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_2" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 2, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_3" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 3, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_4" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 4, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_5" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 5, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_6" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 6, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_7" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 7, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_8" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 8, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "pagoda_arena_stage_9" -> { com.soybe.awakeningsystem.PagodaTrialManager.generateArenaInWorld(level, pos, 9, true); recordPlaced(level, "pagoda_arena", pos); return true; }
            case "adventurers_guild", "guild", "gremio", "bounty_board", "isekai_guild", "tablon" -> {
                placeSeriesNbtStructure(level, pos, "adventurers_guild", 0, 12, 18, 12, () -> {
                    clearStructureVegetation(level, pos, 12, 18, 12);
                    SeriesStructureBuilder.buildAdventurersGuild(level, pos);
                });
                SeriesStructureBuilder.populateAdventurersGuild(level, pos);
                recordPlaced(level, "adventurers_guild", pos);
                return true;
            }
            case "blood_sect_fortress", "blood_sect", "culto_demoniaco", "secta_sangre", "blood_fortress" -> {
                placeSeriesNbtStructure(level, pos, "blood_sect_fortress", 0, 18, 30, 18, () -> {
                    clearStructureVegetation(level, pos, 18, 30, 18);
                    SeriesStructureBuilder.buildBloodSectFortress(level, pos);
                });
                SeriesStructureBuilder.populateBloodSectFortress(level, pos);
                recordPlaced(level, "blood_sect_fortress", pos);
                return true;
            }
            case "yggdrasil_root_labyrinth", "root_labyrinth", "laberinto_raices", "yggdrasil_roots", "mazmorra_raices" -> {
                placeSeriesNbtStructure(level, pos, "yggdrasil_root_labyrinth", -9, 18, 16, 18, () -> {
                    clearStructureVegetation(level, pos, 18, 14, 18);
                    SeriesStructureBuilder.buildYggdrasilRootLabyrinth(level, pos);
                });
                SeriesStructureBuilder.populateYggdrasilRootLabyrinth(level, pos);
                recordPlaced(level, "yggdrasil_root_labyrinth", pos);
                return true;
            }
            case "ancestral_immortal_grotto", "immortal_grotto", "gruta_inmortal", "gruta_ancestral", "cueva_inmortal", "wu_xing_grotto" -> {
                int actualY = pos.getY() > 45 ? Math.min(32, pos.getY() - 30) : pos.getY();
                if (actualY < 15) actualY = 22;
                BlockPos grottoPos = new BlockPos(pos.getX(), actualY, pos.getZ());
                placeSeriesNbtStructure(level, grottoPos, "ancestral_immortal_grotto", -1, 14, 15, 34, () -> {
                    SeriesStructureBuilder.buildAncestralImmortalGrotto(level, grottoPos);
                });
                SeriesStructureBuilder.populateAncestralImmortalGrotto(level, grottoPos);
                recordPlaced(level, "ancestral_immortal_grotto", grottoPos);
                return true;
            }
            case "elemental_floating_castles", "elemental_castles", "castillos_elementales", "elemental_castle", "castillo_elemental", "reyes_elementales" -> {
                int airY = Math.max(180, pos.getY());
                BlockPos airPos = new BlockPos(pos.getX(), airY, pos.getZ());
                placeSeriesNbtStructure(level, airPos, "elemental_floating_castles", -12, 64, 70, 64, () -> {
                    SeriesStructureBuilder.buildElementalFloatingCastles(level, airPos);
                });
                SeriesStructureBuilder.populateElementalFloatingCastles(level, airPos);
                recordPlaced(level, "elemental_floating_castles", airPos);
                return true;
            }
            case "hunter_expedition_camp", "hunter_camp", "campamento_cazadores", "campamento_expedicion" -> {
                placeSeriesNbtStructure(level, pos, "hunter_expedition_camp", -1, 11, 8, 9, () -> {
                    clearStructureVegetation(level, pos, 9, 8, 9);
                    SeriesStructureBuilder.buildHunterExpeditionCamp(level, pos);
                });
                SeriesStructureBuilder.populateHunterExpeditionCamp(level, pos);
                recordPlaced(level, "hunter_expedition_camp", pos);
                return true;
            }
            case "ruined_stargazer_outpost", "stargazer_outpost", "torre_astral", "torre_observacion", "stargazer_tower" -> {
                placeSeriesNbtStructure(level, pos, "ruined_stargazer_outpost", 0, 7, 18, 7, () -> {
                    clearStructureVegetation(level, pos, 7, 18, 7);
                    SeriesStructureBuilder.buildRuinedStargazerOutpost(level, pos);
                });
                SeriesStructureBuilder.populateRuinedStargazerOutpost(level, pos);
                recordPlaced(level, "ruined_stargazer_outpost", pos);
                return true;
            }
            case "ancient_lore_stele", "lore_stele", "estela_ancestral", "estela_runica", "stele_lore" -> {
                placeSeriesNbtStructure(level, pos, "ancient_lore_stele", 0, 9, 12, 9, () -> {
                    clearStructureVegetation(level, pos, 9, 10, 9);
                    SeriesStructureBuilder.buildAncientLoreStele(level, pos);
                });
                SeriesStructureBuilder.populateAncientLoreStele(level, pos);
                recordPlaced(level, "ancient_lore_stele", pos);
                return true;
            }
            case "ancient_alchemy_pavilion", "alchemy_pavilion", "alchemy", "alquimia", "pabellon_alquimia" -> {
                placeSeriesNbtStructure(level, pos, "ancient_alchemy_pavilion", 1, 9, 14, 9, () -> {
                    clearStructureVegetation(level, pos, 9, 14, 9);
                    SeriesStructureBuilder.buildAncientAlchemyPavilion(level, pos);
                });
                SeriesStructureBuilder.populateAncientAlchemyPavilion(level, pos);
                recordPlaced(level, "ancient_alchemy_pavilion", pos);
                return true;
            }
            case "taoist_bamboo_shrine", "bamboo_shrine", "santuario_bambu", "taoist_shrine", "santuario_taoista" -> {
                placeSeriesNbtStructure(level, pos, "taoist_bamboo_shrine", 1, 8, 12, 8, () -> {
                    clearStructureVegetation(level, pos, 8, 12, 8);
                    SeriesStructureBuilder.buildTaoistBambooShrine(level, pos);
                });
                SeriesStructureBuilder.populateTaoistBambooShrine(level, pos);
                recordPlaced(level, "taoist_bamboo_shrine", pos);
                return true;
            }
            case "black_market_den", "black_market", "mercado_negro", "den", "clandestine_den", "guarida_mercado_negro" -> {
                int actualY = pos.getY() > 40 ? Math.min(20, pos.getY() - 40) : pos.getY();
                if (actualY < -30) actualY = -10;
                BlockPos denPos = new BlockPos(pos.getX(), actualY, pos.getZ());
                SeriesStructureBuilder.buildBlackMarketDen(level, denPos);
                recordPlaced(level, "black_market_den", denPos);
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
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "witch_island", -15, 0, -15);
                if (ok) {
                    recordPlaced(level, "teyvat_witch_island", pos);
                }
                return ok;
            }
            case "hilichurl_camp", "campamento_hilichurl", "hilichurl", "campamento" -> {
                clearStructureVegetation(level, pos, 12, 12, 12);
                TeyvatRegionalBuilder.buildHilichurlCamp(level, pos);
                recordPlaced(level, "hilichurl_camp", pos);
                return true;
            }
            case "chasm_mine", "teyvat_chasm_mine", "mina_chasm", "mina_teyvat" -> {
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
            case "grand_casino", "casino", "monte_carlo", "casino_monte_carlo" -> {
                if (!level.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD)) {
                    return false;
                }
                TeyvatCasinoBuilder.buildCasino(level, pos);
                recordPlaced(level, "grand_casino", pos);
                return true;
            }
            case "racetrack", "hipodromo", "uma_racetrack" -> {
                TeyvatRacetrackBuilder.buildRacetrackAt(level, pos);
                recordPlaced(level, "racetrack", pos);
                return true;
            }

            case "anemo_shrine", "anemo_shrine_of_depths", "anemo_shrine_of_depths_final", "santuario_anemo" -> {
                clearStructureVegetation(level, pos, 8, 12, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "anemo_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.LockedTreasureChestBlock.LOCKED, true), 3);
                    com.soybe.awakeningsystem.world.TeyvatWorldGenManager.populateShrineChestLoot(level, chestPos, level.getRandom());
                    recordPlaced(level, "anemo_shrine_of_depths", pos);
                }
                return ok;
            }
            case "geo_shrine", "geo_shrine_of_depths", "geo_shrine_of_depths_final", "santuario_geo" -> {
                clearStructureVegetation(level, pos, 8, 12, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "geo_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.LockedTreasureChestBlock.LOCKED, true), 3);
                    com.soybe.awakeningsystem.world.TeyvatWorldGenManager.populateShrineChestLoot(level, chestPos, level.getRandom());
                    recordPlaced(level, "geo_shrine_of_depths", pos);
                }
                return ok;
            }
            case "electro_shrine", "electro_shrine_of_depths", "electro_shrine_of_depths_final", "santuario_electro" -> {
                clearStructureVegetation(level, pos, 8, 12, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "electro_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.LockedTreasureChestBlock.LOCKED, true), 3);
                    com.soybe.awakeningsystem.world.TeyvatWorldGenManager.populateShrineChestLoot(level, chestPos, level.getRandom());
                    recordPlaced(level, "electro_shrine_of_depths", pos);
                }
                return ok;
            }
            case "dendro_shrine", "dendro_shrine_of_depths", "dendro_shrine_of_depths_final", "santuario_dendro" -> {
                clearStructureVegetation(level, pos, 8, 12, 8);
                boolean ok = TeyvatStructureManager.placeNbtStructure(level, pos, "dendro_shrine_of_depths_final", -6, 0, -6);
                if (ok) {
                    BlockPos chestPos = pos.above(1);
                    level.setBlock(chestPos, com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.LockedTreasureChestBlock.LOCKED, true), 3);
                    com.soybe.awakeningsystem.world.TeyvatWorldGenManager.populateShrineChestLoot(level, chestPos, level.getRandom());
                    recordPlaced(level, "dendro_shrine_of_depths", pos);
                }
                return ok;
            }

            default -> {
                return false;
            }
        }
    }

    public static synchronized void ensureInitialSpawnStructures(ServerLevel level) {
        // Disabled: structures now generate naturally via exploration and /structure tp on verified dry flat terrain.
    }

    private static void placeInitialStructureSafe(ServerLevel level, SeriesStructureData data, int targetX, int targetZ, String type) {
    }

    private static void recordPlaced(ServerLevel level, String type, BlockPos pos) {
        if (level.getServer() != null) {
            SeriesStructureData data = SeriesStructureData.get(level.getServer());
            data.recordStructure(type, pos, level.dimension());
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, net.minecraft.sounds.SoundSource.AMBIENT, 1.2F, 1.0F);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D, 70, 2.0D, 2.0D, 2.0D, 0.2D);
        }
    }

    public static void spawnOverworldMiniboss(ServerLevel level, BlockPos pos) {
        var rand = level.getRandom();
        net.minecraft.world.entity.EntityType<?>[] types = new net.minecraft.world.entity.EntityType<?>[]{
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

    public static void onChunkLoaded(ServerLevel level, net.minecraft.world.level.ChunkPos pos) {
        if (level == null || !level.dimension().equals(Level.OVERWORLD)) return;
        int cx = pos.x;
        int cz = pos.z;
        long chunkKey = net.minecraft.world.level.ChunkPos.asLong(cx, cz);
        if (EVALUATED_CHUNKS.contains(chunkKey)) return;

        if (isRegionTargetChunk(level, cx, cz)) {
            if (areAllChunksLoaded(level, cx, cz)) {
                EVALUATED_CHUNKS.add(chunkKey);
                SeriesStructureData data = SeriesStructureData.get(level.getServer());
                attemptNaturalGeneration(level, cx, cz, data);
            }
        }
    }

    public static void tickNaturalGeneration(MinecraftServer server) {
        if (server == null) return;
        checkAndEnsureNpcsPresent(server);

        ServerLevel level = server.overworld();
        if (level == null) return;

        java.util.List<net.minecraft.server.level.ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        SeriesStructureData data = SeriesStructureData.get(server);
        int generatedCount = 0;

        for (net.minecraft.server.level.ServerPlayer player : players) {
            if (player.level() != level) continue;

            int pcx = player.chunkPosition().x;
            int pcz = player.chunkPosition().z;

            for (int dx = -10; dx <= 10; dx++) {
                for (int dz = -10; dz <= 10; dz++) {
                    int chunkX = pcx + dx;
                    int chunkZ = pcz + dz;
                    long chunkKey = net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ);
                    if (EVALUATED_CHUNKS.contains(chunkKey)) continue;

                    if (isOverworldRacetrackTargetChunk(level, chunkX, chunkZ)) {
                        if (areAllChunksLoaded(level, chunkX, chunkZ)) {
                            EVALUATED_CHUNKS.add(chunkKey);
                            attemptOverworldRacetrackGeneration(level, chunkX, chunkZ, data);
                            if (++generatedCount >= 2) return;
                        }
                    } else if (isRegionTargetChunk(level, chunkX, chunkZ)) {
                        if (areAllChunksLoaded(level, chunkX, chunkZ)) {
                            EVALUATED_CHUNKS.add(chunkKey);
                            attemptNaturalGeneration(level, chunkX, chunkZ, data);
                            if (++generatedCount >= 2) return;
                        }
                    } else {
                        EVALUATED_CHUNKS.add(chunkKey);
                    }
                }
            }
        }

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
                        } else if (isTeyvatCasinoTargetChunk(teyvatLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(teyvatLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptTeyvatCasinoGeneration(teyvatLevel, chunkX, chunkZ, data);
                                return;
                            }
                        } else if (isTeyvatRacetrackTargetChunk(teyvatLevel, chunkX, chunkZ)) {
                            if (areAllChunksLoaded(teyvatLevel, chunkX, chunkZ)) {
                                EVALUATED_CHUNKS.add(chunkKey);
                                attemptTeyvatRacetrackGeneration(teyvatLevel, chunkX, chunkZ, data);
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
        if (!level.getFluidState(center).is(net.minecraft.world.level.material.Fluids.WATER)
                || !level.getFluidState(center.below(3)).is(net.minecraft.world.level.material.Fluids.WATER)) {
            return;
        }
        if (data.hasStructureNearby(center, 400.0D)) return;
        generateStructure(level, center, "beidou_ship");
    }

    private static boolean isTeyvatCasinoTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 180);
        int regionZ = Math.floorDiv(chunkZ, 180);
        long seed = ((long) regionX * 382910481729L + (long) regionZ * 749281729401L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 180 + rand.nextInt(160) + 10;
        int targetChunkZ = regionZ * 180 + rand.nextInt(160) + 10;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptTeyvatCasinoGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 16);
        if (surfaceY < 55 || surfaceY > 150) return;
        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearbyOfType("grand_casino", center, 3200.0D)) return;
        generateStructure(level, center, "grand_casino");
    }

    private static boolean isTeyvatRacetrackTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 180);
        int regionZ = Math.floorDiv(chunkZ, 180);
        long seed = ((long) regionX * 582910481729L + (long) regionZ * 249281729401L) ^ (level.getSeed() + 9991823719L);
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 180 + rand.nextInt(160) + 10;
        int targetChunkZ = regionZ * 180 + rand.nextInt(160) + 10;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptTeyvatRacetrackGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 20);
        if (surfaceY < 55 || surfaceY > 140) return;
        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearbyOfType("racetrack", center, 3200.0D)) return;
        generateStructure(level, center, "racetrack");
    }

    private static boolean isOverworldRacetrackTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 180);
        int regionZ = Math.floorDiv(chunkZ, 180);
        long seed = ((long) regionX * 381927461937L + (long) regionZ * 827461947193L) ^ (level.getSeed() + 4817294819L);
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 180 + rand.nextInt(160) + 10;
        int targetChunkZ = regionZ * 180 + rand.nextInt(160) + 10;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptOverworldRacetrackGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 20);
        if (surfaceY < 55 || surfaceY > 140) return;
        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearbyOfType("racetrack", center, 3200.0D)) return;
        Holder<Biome> biomeHolder = level.getBiome(center);
        String biomeKey = biomeHolder.unwrapKey().map(k -> k.location().getPath()).orElse("").toLowerCase(java.util.Locale.ROOT);
        if (!biomeKey.contains("plains") && !biomeKey.contains("meadow") && !biomeKey.contains("savanna")) return;
        generateStructure(level, center, "racetrack");
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
        int targetY = 200;

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
        int surfaceY = findStableSolidGround(level, blockX, blockZ, 6);
        if (surfaceY < 50 || surfaceY > 200) {
            surfaceY = findSolidSurfaceGround(level, blockX, blockZ);
        }
        if (surfaceY < 50 || surfaceY > 200) return;

        BlockPos center = new BlockPos(blockX, surfaceY, blockZ);
        if (data.hasStructureNearby(center, 220.0D)) return;

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
        int regionX = Math.floorDiv(chunkX, 22);
        int regionZ = Math.floorDiv(chunkZ, 22);
        long seed = ((long) regionX * 619283749104L + (long) regionZ * 827192048571L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        int targetChunkX = regionX * 22 + rand.nextInt(16) + 3;
        int targetChunkZ = regionZ * 22 + rand.nextInt(16) + 3;
        return (chunkX == targetChunkX && chunkZ == targetChunkZ);
    }

    private static void attemptMurimNanoGeneration(ServerLevel level, int chunkX, int chunkZ, SeriesStructureData data) {
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;

        int bestX = centerX;
        int bestZ = centerZ;
        int bestY = -1;

        // Try center and candidate offsets across the chunk to find solid floating island ground
        int[] offsets = {0, -5, 5, -9, 9};
        search:
        for (int ox : offsets) {
            for (int oz : offsets) {
                int bx = centerX + ox;
                int bz = centerZ + oz;
                int gy = findSuitableFlatTerrain(level, bx, bz, 8, 8, 6);
                if (gy >= 55 && gy <= 215) {
                    bestX = bx;
                    bestZ = bz;
                    bestY = gy;
                    break search;
                }
            }
        }

        if (bestY < 55 || bestY > 215) return;

        BlockPos center = new BlockPos(bestX, bestY, bestZ);
        if (data.hasStructureNearby(center, com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD, 280.0D)) return;

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

    public static boolean isRegionTargetChunk(ServerLevel level, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 20);
        int regionZ = Math.floorDiv(chunkZ, 20);

        long seed = ((long) regionX * 341873128712L + (long) regionZ * 132897987541L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);

        int targetChunkX = regionX * 20 + rand.nextInt(14) + 3;
        int targetChunkZ = regionZ * 20 + rand.nextInt(14) + 3;

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
        if (level == null || !level.dimension().equals(Level.OVERWORLD)) return;

        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;

        int regionX = Math.floorDiv(chunkX, 20);
        int regionZ = Math.floorDiv(chunkZ, 20);
        long seed = ((long) regionX * 341873128712L + (long) regionZ * 132897987541L) ^ level.getSeed();
        java.util.Random rand = new java.util.Random(seed);
        float p = rand.nextFloat();

        Holder<Biome> biomeHolder = level.getBiome(new BlockPos(blockX, 64, blockZ));
        String biomeKey = biomeHolder.unwrapKey().map(k -> k.location().getPath()).orElse("").toLowerCase(java.util.Locale.ROOT);

        // ONLY Overworld Manhwa structures generate in the Overworld!
        // Murim structures belong exclusively in MurimWorldManager.MURIM_WORLD
        // Isekai structures belong exclusively in IsekaiWorldManager.ISEKAI_WORLD
        // Teyvat structures belong exclusively in TeyvatWorldManager.TEYVAT_WORLD

        if (biomeKey.contains("meadow") || biomeKey.contains("plains")) {
            if (p < 0.40F) {
                tryPlaceNaturalStructure(level, blockX, blockZ, "solo_leveling", 8, 2, 280.0D, data);
            } else if (p < 0.75F) {
                tryPlaceNaturalStructure(level, blockX, blockZ, "estate", 8, 2, 320.0D, data);
            } else if (p < 0.90F) {
                tryPlaceNaturalStructure(level, blockX, blockZ, "roulette_pavilion", 10, 2, 280.0D, data);
            } else {
                tryPlaceNaturalStructure(level, blockX, blockZ, "overworld_miniboss_arena", 14, 2, 400.0D, data);
            }
        } else if (biomeKey.contains("badlands") || biomeKey.contains("savanna") || biomeKey.contains("stony") || biomeKey.contains("crag") || biomeKey.contains("windswept")) {
            if (p < 0.50F) {
                tryPlaceNaturalStructure(level, blockX, blockZ, "valter", 12, 2, 320.0D, data);
            } else {
                tryPlaceNaturalStructure(level, blockX, blockZ, "ranker", 12, 3, 350.0D, data);
            }
        } else if (biomeKey.contains("desert")) {
            if (p < 0.50F) {
                tryPlaceNaturalStructure(level, blockX, blockZ, "valter", 12, 2, 320.0D, data);
            } else {
                int groundY = findSolidSurfaceGround(level, blockX, blockZ);
                if (groundY >= 50) {
                    int subwayY = Math.min(25, groundY - 35);
                    if (subwayY < 12) subwayY = 16;
                    BlockPos subwayPos = new BlockPos(blockX, subwayY, blockZ);
                    if (!data.hasStructureNearby(subwayPos, 350.0D)) {
                        generateStructure(level, subwayPos, "orv");
                    }
                }
            }
        } else if (biomeKey.contains("dark_forest") || biomeKey.contains("swamp") || biomeKey.contains("bamboo") || biomeKey.contains("jungle")) {
            int groundY = findSolidSurfaceGround(level, blockX, blockZ);
            if (groundY >= 50) {
                int denY = Math.min(20, groundY - 40);
                if (denY < -20) denY = -5;
                BlockPos denPos = new BlockPos(blockX, denY, blockZ);
                if (!data.hasStructureNearby(denPos, 350.0D)) {
                    generateStructure(level, denPos, "black_market_den");
                }
            }
        } else if (biomeKey.contains("cherry") || biomeKey.contains("flower")) {
            tryPlaceNaturalStructure(level, blockX, blockZ, "roulette_pavilion", 10, 2, 300.0D, data);
        } else if (biomeKey.contains("mountain") || biomeKey.contains("peak") || biomeKey.contains("slopes") || biomeKey.contains("frozen") || biomeKey.contains("snowy") || biomeKey.contains("ice")) {
            tryPlaceNaturalStructure(level, blockX, blockZ, "tbate", 11, 4, 350.0D, data);
        } else if (biomeKey.contains("beach") || biomeKey.contains("shore")) {
            tryPlaceNaturalStructure(level, blockX, blockZ, "tower_of_god", 10, 2, 350.0D, data);
        } else if (biomeKey.contains("taiga") || biomeKey.contains("forest") || biomeKey.contains("grove")) {
            if (p < 0.50F) {
                tryPlaceNaturalStructure(level, blockX, blockZ, "estate", 8, 2, 320.0D, data);
            } else {
                tryPlaceNaturalStructure(level, blockX, blockZ, "solo_leveling", 8, 2, 300.0D, data);
            }
        }
    }

    private static boolean tryPlaceNaturalStructure(ServerLevel level, int blockX, int blockZ, String structureType, int radius, int maxSlopeTolerance, double minSpacing, SeriesStructureData data) {
        int groundY = findSuitableFlatTerrain(level, blockX, blockZ, radius, radius, maxSlopeTolerance);
        if (groundY < 50 || groundY > 230) return false;

        BlockPos center = new BlockPos(blockX, groundY, blockZ);
        if (data.hasStructureNearby(center, minSpacing)) return false;

        generateStructure(level, center, structureType);
        return true;
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
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 4, cz - 2), com.soybe.awakeningsystem.entity.ModEntities.KIM_DOKJA.get(), com.soybe.awakeningsystem.entity.NpcProfile.KIM_DOKJA);
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 4, cz + 2), com.soybe.awakeningsystem.entity.ModEntities.YOO_JOONGHYUK.get(), com.soybe.awakeningsystem.entity.NpcProfile.YOO_JOONGHYUK);
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
                    } else if (key.startsWith("black_market_den")) {
                        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.BLACK_MARKET_DEALER.get(), com.soybe.awakeningsystem.entity.NpcProfile.BLACK_MARKET_DEALER);
                    }
                }
            }
        }
    }

    public static String canonicalizeStructureName(String name) {
        if (name == null) return "solo_leveling";
        String s = name.toLowerCase(Locale.ROOT).trim();
        return switch (s) {
            case "solo_leveling", "solo", "jinwoo", "hunter", "hunter_office" -> "solo_leveling";
            case "orv", "dokja", "subway", "metro", "orv_subway_carriage" -> "orv";
            case "estate", "lloyd", "frontera", "engineering", "frontera_estate" -> "estate";
            case "nano", "nano_machine", "cult", "demon", "demon_pagoda" -> "nano";
            case "tbate", "arthur", "aether", "aether_sanctuary" -> "tbate";
            case "ranker", "second_life", "slr", "cain", "yeonwoo", "tower", "obelisk", "ranker_tower" -> "ranker";
            case "mount_hua", "chung_myung", "plum", "hua", "mount_hua_pavilion" -> "mount_hua";
            case "valter", "hanbin", "barbarian", "rock_dimension", "valter_camp", "sword_king" -> "valter";
            case "tower_of_god", "baam", "shinsoo", "floor_of_tests", "tog", "shinsoo_shrine" -> "tower_of_god";
            case "ascension_pavilion", "murim_gate", "murim_pavilion", "ascension", "celestial_pavilion" -> "ascension_pavilion";
            case "dragon_sanctuary", "dragon_altar", "altar_dragon", "santuario_dragon", "dragon_boss_sanctuary" -> "dragon_sanctuary";
            case "roulette_pavilion", "roulette", "fortune", "ruleta", "santuario_ruleta" -> "roulette_pavilion";
            case "adventurers_guild", "guild", "gremio", "gremio_aventureros", "adventurer_guild" -> "adventurers_guild";
            case "goddess_shrine", "goddess", "diosa", "santuario_diosa" -> "goddess_shrine";
            case "nine_tribulations_pagoda", "pagoda", "tribulations", "nueve_tribulaciones" -> "nine_tribulations_pagoda";
            case "yggdrasil_root_labyrinth", "yggdrasil", "labyrinth", "laberinto", "roots", "raices" -> "yggdrasil_root_labyrinth";
            case "blood_sect_fortress", "blood_sect", "secta_sangre", "fortaleza_sangre" -> "blood_sect_fortress";
            case "spirit_valley", "valle_espiritual", "spirit" -> "spirit_valley";
            case "ancestral_immortal_grotto", "immortal_grotto", "gruta_inmortal", "grotto", "cueva_inmortal" -> "ancestral_immortal_grotto";
            case "elemental_floating_castles", "elemental_castles", "castillos_elementales", "elemental_castle", "castillo_elemental", "reyes_elementales" -> "elemental_floating_castles";
            case "hunter_expedition_camp", "hunter_camp", "campamento_cazadores", "campamento_expedicion" -> "hunter_expedition_camp";
            case "ruined_stargazer_outpost", "stargazer_outpost", "torre_astral", "torre_observacion", "stargazer_tower" -> "ruined_stargazer_outpost";
            case "ancient_lore_stele", "lore_stele", "estela_ancestral", "estela_runica", "stele_lore" -> "ancient_lore_stele";
            case "ancient_alchemy_pavilion", "alchemy_pavilion", "alchemy", "alquimia", "pabellon_alquimia" -> "ancient_alchemy_pavilion";
            case "taoist_bamboo_shrine", "bamboo_shrine", "santuario_bambu", "taoist_shrine", "santuario_taoista" -> "taoist_bamboo_shrine";
            case "black_market_den", "black_market", "mercado_negro", "den", "clandestine_den", "guarida_mercado_negro" -> "black_market_den";
            case "dawn_winery", "teyvat_winery", "winery", "teyvat_dawn_winery", "diluc" -> "dawn_winery";
            case "jade_chamber", "teyvat_jade_chamber", "camara_jade", "ningguang" -> "jade_chamber";
            case "teyvat_treehouses", "treehouses", "aranara" -> "teyvat_treehouses";
            case "windrise_giant_tree", "windrise", "teyvat_tree", "arbol_windrise", "teyvat_windrise_tree" -> "windrise_giant_tree";
            case "beidou_ship", "alcor", "barco_beidou", "teyvat_beidou_ship" -> "beidou_ship";
            case "witch_island", "teyvat_witch_island", "isla_bruja" -> "witch_island";
            case "hilichurl_camp", "campamento_hilichurl", "hilichurl", "campamento" -> "hilichurl_camp";
            case "chasm_mine", "teyvat_chasm_mine", "mina_chasm", "mina_teyvat", "chasm" -> "chasm_mine";
            case "mora_temple", "teyvat_mora_temple", "templo_mora", "templo_ancestral" -> "mora_temple";
            case "interastral_bank", "teyvat_interastral_bank", "banco_interastral", "banco" -> "interastral_bank";
            case "plains_shrine", "teyvat_plains_shrine", "santuario_llanura" -> "plains_shrine";
            case "desert_shrine", "teyvat_desert_shrine", "santuario_desierto" -> "desert_shrine";
            case "mountain_shrine", "teyvat_mountain_shrine", "santuario_montana" -> "mountain_shrine";
            case "overworld_miniboss_arena", "miniboss_arena", "arena_miniboss", "arena_overworld", "hypostasis" -> "overworld_miniboss_arena";
            case "grand_casino", "casino", "monte_carlo", "casino_monte_carlo" -> "grand_casino";
            case "racetrack", "hipodromo", "uma_racetrack" -> "racetrack";
            case "golden_house", "casa_dorada", "childe_arena" -> "golden_house";
            case "tenshukaku", "shogun_palace", "inazuma_castle", "palacio_shogun" -> "tenshukaku";
            case "anemo_shrine", "anemo_shrine_of_depths", "anemo_shrine_of_depths_final", "santuario_anemo", "anemo" -> "anemo_shrine";
            case "geo_shrine", "geo_shrine_of_depths", "geo_shrine_of_depths_final", "santuario_geo", "geo" -> "geo_shrine";
            case "electro_shrine", "electro_shrine_of_depths", "electro_shrine_of_depths_final", "santuario_electro", "electro" -> "electro_shrine";
            case "dendro_shrine", "dendro_shrine_of_depths", "dendro_shrine_of_depths_final", "santuario_dendro", "dendro" -> "dendro_shrine";
            default -> s;
        };
    }

    public static boolean matchesCanonical(String key, String canonicalName) {
        if (key == null || canonicalName == null) return false;
        String k = key.toLowerCase(Locale.ROOT).trim();
        String c = canonicalName.toLowerCase(Locale.ROOT).trim();
        if (k.equals(c) || k.startsWith(c + "#") || k.startsWith(c + "_")) return true;

        String prefix = k;
        int sep = k.indexOf('#');
        if (sep > 0) {
            prefix = k.substring(0, sep);
        } else {
            int lastUnderscore = k.lastIndexOf('_');
            if (lastUnderscore > 0) {
                int secondLast = k.lastIndexOf('_', lastUnderscore - 1);
                if (secondLast > 0 && (Character.isDigit(k.charAt(lastUnderscore + 1)) || k.charAt(lastUnderscore + 1) == '-')) {
                    prefix = k.substring(0, secondLast);
                }
            }
        }
        if (prefix.equals(c) || canonicalizeStructureName(prefix).equals(c)) {
            return true;
        }
        return k.contains(c);
    }

    public static ResourceKey<Level> getTargetDimensionForStructure(String canonicalName) {
        return switch (canonicalName) {
            case "nano", "mount_hua", "spirit_valley", "nine_tribulations_pagoda",
                 "blood_sect_fortress", "ancestral_immortal_grotto", "dragon_sanctuary",
                 "ancient_alchemy_pavilion", "taoist_bamboo_shrine", "ascension_pavilion" ->
                    com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD;

            case "adventurers_guild", "goddess_shrine", "yggdrasil_root_labyrinth",
                 "elemental_floating_castles", "hunter_expedition_camp",
                 "ruined_stargazer_outpost", "ancient_lore_stele" ->
                    com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD;

            case "dawn_winery", "jade_chamber", "windrise_giant_tree", "beidou_ship",
                 "witch_island", "hilichurl_camp", "chasm_mine", "mora_temple",
                 "interastral_bank", "grand_casino", "racetrack", "golden_house",
                 "tenshukaku", "teyvat_treehouses", "anemo_shrine", "geo_shrine",
                 "electro_shrine", "dendro_shrine", "mountain_shrine",
                 "desert_shrine", "plains_shrine" ->
                    com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD;

            default -> Level.OVERWORLD;
        };
    }

    public static ResourceKey<Level> getDimensionOfKey(String key, String canonicalName) {
        if (key != null && key.contains("#")) {
            String[] parts = key.split("#");
            if (parts.length >= 2) {
                String dimStr = parts[1];
                if (dimStr.contains("murim")) return com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD;
                if (dimStr.contains("isekai")) return com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD;
                if (dimStr.contains("teyvat")) return com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD;
                return Level.OVERWORLD;
            }
        }
        return getTargetDimensionForStructure(canonicalName);
    }

    public static int getStructureRadius(String canonicalName) {
        return switch (canonicalName) {
            case "estate", "solo_leveling", "nano" -> 8;
            case "tbate", "mount_hua", "ancient_alchemy_pavilion" -> 11;
            case "taoist_bamboo_shrine", "tower_of_god", "ruined_stargazer_outpost", "hunter_expedition_camp", "roulette_pavilion" -> 10;
            case "ranker", "valter", "nine_tribulations_pagoda", "adventurers_guild", "goddess_shrine" -> 12;
            case "spirit_valley", "dragon_sanctuary", "overworld_miniboss_arena", "dawn_winery" -> 14;
            case "blood_sect_fortress", "yggdrasil_root_labyrinth" -> 16;
            case "racetrack" -> 20;
            default -> 10;
        };
    }

    public static int getStructureSlopeTolerance(String canonicalName) {
        return switch (canonicalName) {
            case "tbate", "nine_tribulations_pagoda", "dragon_sanctuary", "ranker", "blood_sect_fortress" -> 4;
            case "estate", "solo_leveling", "roulette_pavilion", "valter", "tower_of_god", "overworld_miniboss_arena" -> 2;
            default -> 3;
        };
    }

    public static BlockPos getSafeLandingPosition(ServerLevel level, BlockPos basePos, String canonicalName) {
        String c = canonicalizeStructureName(canonicalName);
        return switch (c) {
            case "orv" -> basePos.offset(-5, 4, 0);
            case "beidou_ship" -> basePos.offset(0, 11, 0);
            case "jade_chamber", "elemental_floating_castles" -> basePos.offset(0, 2, 0);
            case "chasm_mine", "black_market_den", "ancestral_immortal_grotto" -> basePos.offset(0, 1, 0);
            case "anemo_shrine", "geo_shrine", "electro_shrine", "dendro_shrine",
                 "plains_shrine", "desert_shrine", "mountain_shrine", "goddess_shrine" -> basePos.offset(0, 1, -5);
            case "estate" -> basePos.offset(0, 1, -10);
            case "solo_leveling" -> basePos.offset(0, 1, -9);
            case "overworld_miniboss_arena" -> basePos.offset(0, 1, -12);
            default -> {
                int groundY = findSolidSurfaceGround(level, basePos.getX(), basePos.getZ() - 6);
                if (groundY > 0) {
                    yield new BlockPos(basePos.getX(), groundY + 1, basePos.getZ() - 6);
                }
                yield basePos.offset(0, 1, -6);
            }
        };
    }

    public static List<String> getPreferredBiomes(String canonicalName) {
        return switch (canonicalName) {
            case "solo_leveling", "estate" -> List.of("plains", "meadow");
            case "orv" -> List.of("desert", "badlands", "plains", "savanna", "forest");
            case "adventurers_guild" -> List.of("plains", "forest", "meadow");
            case "nano" -> List.of("demonic", "bamboo", "peaks", "pine", "dark");
            case "tbate" -> List.of("mountain", "peak", "slopes", "stony_peaks", "jagged_peaks", "frozen");
            case "ranker", "valter" -> List.of("badlands", "savanna", "stony", "windswept");
            case "mount_hua", "ancient_alchemy_pavilion", "taoist_bamboo_shrine" -> List.of("cherry", "flower", "bamboo", "meadow", "peaks");
            case "spirit_valley" -> List.of("bamboo", "forest", "valley", "cherry", "jungle");
            case "nine_tribulations_pagoda", "dragon_sanctuary", "ascension_pavilion" -> List.of("mountain", "peak", "stony_peaks", "peaks", "cherry");
            case "blood_sect_fortress" -> List.of("crimson", "dark", "peaks", "badlands");
            case "ancestral_immortal_grotto" -> List.of("peaks", "mountain", "bamboo");
            case "tower_of_god" -> List.of("beach", "stony_shore", "plains");
            case "beidou_ship", "goddess_shrine", "witch_island" -> List.of("ocean", "deep_ocean", "beach");
            case "dendro_shrine", "teyvat_treehouses" -> List.of("jungle", "forest");
            case "geo_shrine", "desert_shrine", "mora_temple" -> List.of("desert", "badlands", "savanna");
            case "anemo_shrine", "dawn_winery", "windrise_giant_tree", "plains_shrine" -> List.of("plains", "meadow", "forest");
            case "yggdrasil_root_labyrinth" -> List.of("jungle", "dark_forest", "swamp");
            case "hunter_expedition_camp", "ancient_lore_stele", "hilichurl_camp" -> List.of("taiga", "forest", "grove");
            case "roulette_pavilion", "racetrack", "overworld_miniboss_arena", "interastral_bank", "grand_casino", "golden_house", "tenshukaku" -> List.of("plains", "meadow", "savanna");
            default -> List.of("plains", "meadow");
        };
    }

    public record NaturalSearchResult(ServerLevel level, BlockPos basePos, BlockPos landingPos, String canonicalName, String biomeName) {}

    public static NaturalSearchResult findOrCreateNaturalStructure(ServerLevel originLevel, ServerPlayer player, String rawName) {
        String canon = canonicalizeStructureName(rawName);
        MinecraftServer server = originLevel.getServer();
        if (server == null) return null;

        ResourceKey<Level> targetDim = getTargetDimensionForStructure(canon);
        ServerLevel targetLevel = server.getLevel(targetDim);
        if (targetLevel == null) targetLevel = originLevel;

        SeriesStructureData data = SeriesStructureData.get(server);
        List<String> keywords = getPreferredBiomes(canon);
        int rad = getStructureRadius(canon);
        int slopeTol = getStructureSlopeTolerance(canon);

        int originChunkX = player.blockPosition().getX() >> 4;
        int originChunkZ = player.blockPosition().getZ() >> 4;
        int pRegX = Math.floorDiv(originChunkX, 20);
        int pRegZ = Math.floorDiv(originChunkZ, 20);

        BlockPos foundPos = null;
        String foundBiome = "natural";

        searchLoop:
        for (int r = 0; r <= 40; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    int rx = pRegX + dx;
                    int rz = pRegZ + dz;

                    long seed = ((long) rx * 341873128712L + (long) rz * 132897987541L) ^ targetLevel.getSeed();
                    Random rand = new Random(seed);
                    int tcX = rx * 20 + rand.nextInt(14) + 3;
                    int tcZ = rz * 20 + rand.nextInt(14) + 3;

                    int bx = tcX * 16 + 8;
                    int bz = tcZ * 16 + 8;

                    Holder<Biome> bHolder = targetLevel.getBiome(new BlockPos(bx, 64, bz));
                    String bKey = bHolder.unwrapKey().map(k -> k.location().getPath()).orElse("").toLowerCase(Locale.ROOT);

                    boolean match = false;
                    for (String kw : keywords) {
                        if (bKey.contains(kw)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) continue;

                    BlockPos candPos = new BlockPos(bx, 64, bz);
                    if (data != null && data.hasStructureNearby(candPos, 200.0D)) continue;

                    if (canon.equals("orv")) {
                        int sy = findSolidSurfaceGround(targetLevel, bx, bz);
                        if (sy < 40) continue;
                        int subwayY = Math.min(25, sy - 35);
                        if (subwayY < 10) subwayY = 16;
                        foundPos = new BlockPos(bx, subwayY, bz);
                    } else if (canon.equals("black_market_den")) {
                        int sy = findSolidSurfaceGround(targetLevel, bx, bz);
                        if (sy < 40) continue;
                        int denY = Math.min(20, sy - 40);
                        if (denY < -30) denY = -10;
                        foundPos = new BlockPos(bx, denY, bz);
                    } else if (canon.equals("elemental_floating_castles") || canon.equals("jade_chamber")) {
                        foundPos = new BlockPos(bx, 185, bz);
                    } else if (canon.equals("beidou_ship") || canon.equals("witch_island")) {
                        foundPos = new BlockPos(bx, 63, bz);
                    } else {
                        int surfaceY = findSuitableFlatTerrain(targetLevel, bx, bz, rad, rad, slopeTol);
                        if (surfaceY < 50 || surfaceY > 230) continue;
                        foundPos = new BlockPos(bx, surfaceY, bz);
                    }

                    foundBiome = bKey;
                    break searchLoop;
                }
            }
        }

        if (foundPos == null) {
            var closest = targetLevel.findClosestBiome3d(holder -> {
                String k = holder.unwrapKey().map(key -> key.location().getPath()).orElse("").toLowerCase(Locale.ROOT);
                for (String kw : keywords) {
                    if (k.contains(kw)) return true;
                }
                return false;
            }, player.blockPosition(), 6400, 32, 64);

            if (closest != null) {
                BlockPos bPos = closest.getFirst();
                int baseBx = bPos.getX();
                int baseBz = bPos.getZ();
                Holder<Biome> bHolder = targetLevel.getBiome(bPos);
                foundBiome = bHolder.unwrapKey().map(k -> k.location().getPath()).orElse("").toLowerCase(Locale.ROOT);

                if (canon.equals("orv")) {
                    int sy = findSolidSurfaceGround(targetLevel, baseBx, baseBz);
                    if (sy < 40) sy = 64;
                    int subwayY = Math.min(25, sy - 35);
                    if (subwayY < 10) subwayY = 16;
                    foundPos = new BlockPos(baseBx, subwayY, baseBz);
                } else if (canon.equals("black_market_den")) {
                    int sy = findSolidSurfaceGround(targetLevel, baseBx, baseBz);
                    if (sy < 40) sy = 64;
                    int denY = Math.min(20, sy - 40);
                    if (denY < -30) denY = -10;
                    foundPos = new BlockPos(baseBx, denY, baseBz);
                } else if (canon.equals("elemental_floating_castles") || canon.equals("jade_chamber")) {
                    foundPos = new BlockPos(baseBx, 185, baseBz);
                } else if (canon.equals("beidou_ship") || canon.equals("witch_island")) {
                    foundPos = new BlockPos(baseBx, 63, baseBz);
                } else {
                    for (int scanR = 0; scanR <= 16; scanR += 4) {
                        for (int sx = -scanR; sx <= scanR; sx += 4) {
                            for (int sz = -scanR; sz <= scanR; sz += 4) {
                                int testX = baseBx + sx * 16;
                                int testZ = baseBz + sz * 16;
                                int sy = findSuitableFlatTerrain(targetLevel, testX, testZ, rad, rad, slopeTol);
                                if (sy >= 50 && sy <= 230) {
                                    foundPos = new BlockPos(testX, sy, testZ);
                                    break;
                                }
                            }
                            if (foundPos != null) break;
                        }
                        if (foundPos != null) break;
                    }
                }
            }
        }

        if (foundPos == null) {
            for (int r = 0; r <= 30; r++) {
                for (int dx = -r; dx <= r; dx += 2) {
                    for (int dz = -r; dz <= r; dz += 2) {
                        int bx = (pRegX + dx) * 16 + 8;
                        int bz = (pRegZ + dz) * 16 + 8;
                        int sy = findSuitableFlatTerrain(targetLevel, bx, bz, rad, rad, slopeTol);
                        if (sy >= 50 && sy <= 230) {
                            foundPos = new BlockPos(bx, sy, bz);
                            foundBiome = "flat_terrain";
                            break;
                        }
                    }
                    if (foundPos != null) break;
                }
                if (foundPos != null) break;
            }
        }

        if (foundPos == null) {
            int px = player.blockPosition().getX() + 32;
            int pz = player.blockPosition().getZ() + 32;
            int sy = findSolidSurfaceGround(targetLevel, px, pz);
            if (sy < 50) {
                sy = targetDim.equals(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD) ? 120 : 64;
            }
            foundPos = new BlockPos(px, sy, pz);
            foundBiome = "fallback";
        }

        generateStructure(targetLevel, foundPos, canon);
        checkAndEnsureNpcsPresent(server);
        BlockPos landing = getSafeLandingPosition(targetLevel, foundPos, canon);
        return new NaturalSearchResult(targetLevel, foundPos, landing, canon, foundBiome);
    }

    public static void clearCache() {
        EVALUATED_CHUNKS.clear();
    }
}
