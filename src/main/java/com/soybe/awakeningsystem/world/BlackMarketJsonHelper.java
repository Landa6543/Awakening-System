package com.soybe.awakeningsystem.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.casino.pool.IllegalPoolTableBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTablePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class BlackMarketJsonHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private BlackMarketJsonHelper() {}

    public static Path getDirectory() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("awakening_system/custom_black_market");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception ignored) {}
        return dir;
    }

    public static boolean hasCustomStructure() {
        Path defPath = getDirectory().resolve("default.json");
        if (Files.exists(defPath)) {
            try {
                if (Files.size(defPath) > 500) return true;
            } catch (Exception ignored) {}
        }
        try (java.io.InputStream in = BlackMarketJsonHelper.class.getResourceAsStream("/data/awakening_system/structures/black_market_den.json")) {
            return in != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static File exportStructure(ServerLevel level, BlockPos origin, int radius, String layoutName) {
        if (layoutName == null || layoutName.isBlank()) {
            layoutName = "default";
        }
        if (!layoutName.endsWith(".json")) {
            layoutName += ".json";
        }

        // Auto-snap to known Black Market Den center if player is nearby or on the surface entrance
        if (level.getServer() != null) {
            var data = SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
            if (data != null) {
                for (var entry : data.getAll().entrySet()) {
                    if (entry.getKey().startsWith("black_market_den")) {
                        BlockPos center = entry.getValue();
                        if (Math.abs(origin.getX() - center.getX()) <= 32 && Math.abs(origin.getZ() - center.getZ()) <= 32) {
                            origin = center;
                            break;
                        }
                    }
                }
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("layout_name", layoutName);
        root.addProperty("export_date", LocalDateTime.now().format(DATE_FORMAT));
        root.addProperty("origin_x", origin.getX());
        root.addProperty("origin_y", origin.getY());
        root.addProperty("origin_z", origin.getZ());
        root.addProperty("radius", radius);

        JsonArray blocksArray = new JsonArray();
        int count = 0;

        int minX = origin.getX() - radius;
        int maxX = origin.getX() + radius;
        int minZ = origin.getZ() - radius;
        int maxZ = origin.getZ() + radius;
        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - 1);
        int maxY = Math.min(level.getMaxBuildHeight(), origin.getY() + 10);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.isAir()) continue;

                    if (s.getBlock() instanceof PoolTableBlock && s.hasProperty(PoolTableBlock.PART)
                            && s.getValue(PoolTableBlock.PART) != PoolTablePart.CORNER_NW) {
                        continue;
                    }
                    if (s.getBlock() instanceof SlotMachineBlock && s.hasProperty(SlotMachineBlock.PART)
                            && s.getValue(SlotMachineBlock.PART) == SlotMachineBlock.SlotMachinePart.BACK) {
                        continue;
                    }
                    if (s.getBlock() instanceof DoorBlock && s.hasProperty(DoorBlock.HALF)
                            && s.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                        continue;
                    }
                    if (s.getBlock() instanceof BedBlock && s.hasProperty(BedBlock.PART)
                            && s.getValue(BedBlock.PART) == BedPart.HEAD) {
                        continue;
                    }

                    JsonObject obj = new JsonObject();
                    ResourceLocation key = BuiltInRegistries.BLOCK.getKey(s.getBlock());
                    obj.addProperty("block", key.toString());
                    obj.addProperty("dx", x - origin.getX());
                    obj.addProperty("dy", y - origin.getY());
                    obj.addProperty("dz", z - origin.getZ());

                    JsonObject props = new JsonObject();
                    for (Property<?> prop : s.getProperties()) {
                        props.addProperty(prop.getName(), s.getValue(prop).toString());
                    }
                    obj.add("properties", props);

                    blocksArray.add(obj);
                    count++;
                }
            }
        }

        root.add("blocks", blocksArray);
        root.addProperty("total_count", count);

        try {
            File outFile = getDirectory().resolve(layoutName).toFile();
            try (FileWriter writer = new FileWriter(outFile)) {
                GSON.toJson(root, writer);
            }
            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int importStructure(ServerLevel level, BlockPos origin, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            fileName = "default.json";
        }
        if (!fileName.endsWith(".json")) {
            fileName += ".json";
        }

        java.io.Reader reader = null;
        Path path = getDirectory().resolve(fileName);
        if (Files.exists(path)) {
            try {
                reader = new java.io.FileReader(path.toFile(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
        if (reader == null) {
            String resName = (fileName.equals("default.json") || fileName.equals("default")) ? "black_market_den.json" : fileName;
            java.io.InputStream in = BlackMarketJsonHelper.class.getResourceAsStream("/data/awakening_system/structures/" + resName);
            if (in != null) {
                reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        if (reader == null) return -1;

        try (java.io.Reader r = reader) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (!root.has("blocks")) return 0;

            JsonArray arr = root.getAsJsonArray("blocks");
            if (arr.isEmpty()) {
                // Try fallback to embedded black_market_den.json
                java.io.InputStream in = BlackMarketJsonHelper.class.getResourceAsStream("/data/awakening_system/structures/black_market_den.json");
                if (in != null) {
                    try (var fallbackReader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
                        root = GSON.fromJson(fallbackReader, JsonObject.class);
                        if (root.has("blocks")) {
                            arr = root.getAsJsonArray("blocks");
                        }
                    }
                }
            }
            int radius = root.has("radius") ? root.get("radius").getAsInt() : 12;
            SeriesStructureBuilder.clearRoom(level, origin.getX() - radius, origin.getX() + radius, origin.getY() + 1, origin.getY() + 8, origin.getZ() - radius, origin.getZ() + radius);

            int placed = 0;

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();
                String blockId = obj.get("block").getAsString();
                int dx = obj.get("dx").getAsInt();
                int dy = obj.get("dy").getAsInt();
                int dz = obj.get("dz").getAsInt();
                BlockPos target = origin.offset(dx, dy, dz);

                Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
                if (b == null || b == Blocks.AIR) continue;

                JsonObject props = obj.has("properties") ? obj.getAsJsonObject("properties") : null;
                Direction facing = Direction.NORTH;
                if (props != null && props.has("facing")) {
                    Direction parsed = Direction.byName(props.get("facing").getAsString());
                    if (parsed != null) facing = parsed;
                }

                if (b instanceof PoolTableBlock) {
                    SeriesStructureBuilder.placeIllegalPoolTable(level, target, facing);
                } else if (b instanceof SlotMachineBlock sm) {
                    TeyvatCasinoBuilder.placeSlotMachine(level, target, facing, sm.isIllegal());
                } else if (b instanceof DoorBlock) {
                    BlockState lower = applyProperties(b.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), props);
                    BlockState upper = applyProperties(b.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), props);
                    level.setBlock(target, lower, 3);
                    level.setBlock(target.above(), upper, 3);
                } else {
                    BlockState state = applyProperties(b.defaultBlockState(), props);
                    level.setBlock(target, state, 3);
                }
                placed++;
            }
            return placed;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static BlockState applyProperties(BlockState state, JsonObject props) {
        if (props == null) return state;
        for (Property<?> prop : state.getProperties()) {
            if (props.has(prop.getName())) {
                String valStr = props.get(prop.getName()).getAsString();
                state = setPropHelper(state, prop, valStr);
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState setPropHelper(BlockState state, Property<T> prop, String val) {
        var opt = prop.getValue(val);
        return opt.map(t -> state.setValue(prop, t)).orElse(state);
    }
}
