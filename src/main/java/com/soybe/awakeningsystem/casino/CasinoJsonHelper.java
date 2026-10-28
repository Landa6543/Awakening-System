package com.soybe.awakeningsystem.casino;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soybe.awakeningsystem.block.*;
import com.soybe.awakeningsystem.block.entity.CasinoTrophyBlockEntity;
import com.soybe.awakeningsystem.casino.darts.DartboardBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTablePart;
import com.soybe.awakeningsystem.world.TeyvatCasinoBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CasinoJsonHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CasinoJsonHelper() {}

    public static Path getCasinoDirectory() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("awakening_system/custom_casino");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static boolean hasCustomLayout() {
        Path defPath = getCasinoDirectory().resolve("casino_custom.json");
        if (Files.exists(defPath)) return true;
        try (java.io.InputStream in = CasinoJsonHelper.class.getResourceAsStream("/data/awakening_system/casino/casino_custom.json")) {
            return in != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static File exportFurniture(ServerLevel level, BlockPos origin, int radius, String layoutName) {
        if (layoutName == null || layoutName.isBlank()) {
            layoutName = "casino_custom";
        }
        if (!layoutName.endsWith(".json")) {
            layoutName += ".json";
        }

        JsonObject root = new JsonObject();
        root.addProperty("layout_name", layoutName);
        root.addProperty("export_date", LocalDateTime.now().format(DATE_FORMAT));
        root.addProperty("origin_x", origin.getX());
        root.addProperty("origin_y", origin.getY());
        root.addProperty("origin_z", origin.getZ());
        root.addProperty("radius", radius);

        JsonArray itemsArray = new JsonArray();
        int count = 0;

        int minX = origin.getX() - radius;
        int maxX = origin.getX() + radius;
        int minZ = origin.getZ() - radius;
        int maxZ = origin.getZ() + radius;
        int minY = origin.getY() - 4;
        int maxY = origin.getY() + 24;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (isCasinoFurniture(s.getBlock())) {
                        if (isNonMasterPart(s)) continue;

                        JsonObject obj = new JsonObject();
                        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(s.getBlock());
                        obj.addProperty("block", key.toString());
                        obj.addProperty("dx", x - origin.getX());
                        obj.addProperty("dy", y - origin.getY());
                        obj.addProperty("dz", z - origin.getZ());

                        if (s.hasProperty(HorizontalDirectionalBlock.FACING)) {
                            obj.addProperty("facing", s.getValue(HorizontalDirectionalBlock.FACING).getName());
                        }

                        if (s.getBlock() instanceof CasinoTrophyBlock && level.getBlockEntity(p) instanceof CasinoTrophyBlockEntity be) {
                            obj.addProperty("trophy_type", be.getTrophyType());
                            obj.addProperty("winner_name", be.getWinnerName());
                            obj.addProperty("achievement_desc", be.getAchievementDesc());
                            obj.addProperty("awarded_date", be.getAwardedDate());
                        }

                        itemsArray.add(obj);
                        count++;
                    }
                }
            }
        }

        root.add("furniture", itemsArray);
        root.addProperty("total_count", count);

        try {
            File outFile = getCasinoDirectory().resolve(layoutName).toFile();
            try (FileWriter writer = new FileWriter(outFile)) {
                GSON.toJson(root, writer);
            }
            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int importFurniture(ServerLevel level, BlockPos origin, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            fileName = "casino_custom.json";
        }
        if (!fileName.endsWith(".json")) {
            fileName += ".json";
        }

        java.io.Reader reader = null;
        Path path = getCasinoDirectory().resolve(fileName);
        if (Files.exists(path)) {
            try {
                reader = new java.io.FileReader(path.toFile(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            java.io.InputStream in = CasinoJsonHelper.class.getResourceAsStream("/data/awakening_system/casino/" + fileName);
            if (in != null) {
                reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        if (reader == null) return -1;

        try (java.io.Reader r = reader) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (!root.has("furniture")) return 0;

            JsonArray arr = root.getAsJsonArray("furniture");
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

                Direction facing = Direction.NORTH;
                if (obj.has("facing")) {
                    facing = Direction.byName(obj.get("facing").getAsString());
                    if (facing == null) facing = Direction.NORTH;
                }

                if (b instanceof VipBlackjackTableBlock) {
                    TeyvatCasinoBuilder.placeVipBlackjackTable(level, target, facing);
                } else if (b instanceof BlackjackTableBlock) {
                    boolean illegal = (b instanceof IllegalBlackjackTableBlock);
                    TeyvatCasinoBuilder.placeBlackjackTable(level, target, facing, illegal);
                } else if (b instanceof VipRouletteTableBlock) {
                    TeyvatCasinoBuilder.placeVipRouletteTable(level, target, facing);
                } else if (b instanceof RouletteTableBlock) {
                    boolean illegal = (b instanceof IllegalRouletteTableBlock);
                    TeyvatCasinoBuilder.placeRouletteTable(level, target, facing, illegal);
                } else if (b instanceof PoolTableBlock) {
                    boolean illegal = (b instanceof com.soybe.awakeningsystem.casino.pool.IllegalPoolTableBlock);
                    TeyvatCasinoBuilder.placePoolTable(level, target, facing, illegal);
                } else if (b instanceof SlotMachineBlock) {
                    boolean illegal = ((SlotMachineBlock) b).isIllegal();
                    TeyvatCasinoBuilder.placeSlotMachine(level, target, facing, illegal);
                } else if (b instanceof DartboardBlock) {
                    TeyvatCasinoBuilder.placeDartboard(level, target, facing);
                } else if (b instanceof CasinoTrophyBlock) {
                    int type = obj.has("trophy_type") ? obj.get("trophy_type").getAsInt() : 1;
                    String winner = obj.has("winner_name") ? obj.get("winner_name").getAsString() : "";
                    String desc = obj.has("achievement_desc") ? obj.get("achievement_desc").getAsString() : "";
                    TeyvatCasinoBuilder.placeTrophy(level, target, facing, type, winner, desc);
                } else {
                    BlockState st = b.defaultBlockState();
                    if (st.hasProperty(HorizontalDirectionalBlock.FACING)) {
                        st = st.setValue(HorizontalDirectionalBlock.FACING, facing);
                    }
                    level.setBlock(target, st, 3);
                }
                placed++;
            }
            return placed;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int clearFurniture(ServerLevel level, BlockPos origin, int radius) {
        int cleared = 0;
        int minX = origin.getX() - radius;
        int maxX = origin.getX() + radius;
        int minZ = origin.getZ() - radius;
        int maxZ = origin.getZ() + radius;
        int minY = origin.getY() - 4;
        int maxY = origin.getY() + 24;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (isCasinoFurniture(s.getBlock())) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                        cleared++;
                    }
                }
            }
        }
        return cleared;
    }

    public static boolean isCasinoFurniture(Block b) {
        return b instanceof SlotMachineBlock
                || b instanceof BlackjackTableBlock
                || b instanceof RouletteTableBlock
                || b instanceof PoolTableBlock
                || b instanceof CasinoChairBlock
                || b instanceof CasinoCashierBlock
                || b instanceof ClandestineCashierBlock
                || b instanceof CasinoTrophyBlock
                || b instanceof CasinoWallDisplayStandBlock
                || b instanceof CasinoVipBarrierBlock
                || b instanceof CasinoVipGateBlock
                || b instanceof CasinoReceptionGateBlock
                || b instanceof CasinoBarStoolBlock
                || b instanceof DartboardBlock;
    }

    private static boolean isNonMasterPart(BlockState s) {
        if (s.hasProperty(BlackjackTableBlock.PART) && s.getValue(BlackjackTableBlock.PART) != BlackjackPart.FRONT_LEFT) {
            return true;
        }
        if (s.hasProperty(RouletteTableBlock.PART) && s.getValue(RouletteTableBlock.PART) != RoulettePart.WHEEL_LEFT) {
            return true;
        }
        if (s.hasProperty(PoolTableBlock.PART) && s.getValue(PoolTableBlock.PART) != PoolTablePart.CORNER_NW) {
            return true;
        }
        return false;
    }
}
