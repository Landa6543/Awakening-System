package com.soybe.awakeningsystem.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generador monumental de la Torre del Ranker / Torre de la Ilusión.
 * Diseño completamente cilíndrico de grosor constante (sin adelgazar en punta),
 * muros herméticos macizos sin huecos, fachadas góticas con vitrales rúnicos resplandecientes,
 * escalinatas funcionales y soporte completo para exportar/importar personalizaciones en JSON.
 */
public final class TowerStructureGenerator {

    private TowerStructureGenerator() {}

    public static final int TOWER_RADIUS = 16;
    public static final int WALL_THICKNESS = 3;
    public static final int TOWER_HEIGHT = 168;

    public static Path getExportFilePath() {
        return FMLPaths.CONFIGDIR.get().resolve("awakening_system/custom_tower/tower_exterior.json");
    }

    public static boolean hasCustomExport() {
        if (Files.exists(getExportFilePath())) return true;
        return TowerStructureGenerator.class.getResource("/data/awakening_system/custom_tower/tower_exterior.json") != null;
    }

    public static void generate(ServerLevel level, BlockPos origin) {
        if (hasCustomExport()) {
            if (loadCustomExport(level, origin)) {
                AwakeningSystemMod.LOGGER.info("Torre construida exitosamente desde custom JSON en {}", origin);
                return;
            }
        }
        buildProceduralTower(level, origin);
    }

    // =========================================================================
    // CONSTRUCCIÓN PROCEDURAL MONOLÍTICA CILÍNDRICA
    // =========================================================================
    public static void buildProceduralTower(ServerLevel level, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        int r = TOWER_RADIUS; // 16
        int rInner = r - WALL_THICKNESS; // 13

        // 1. Cimientos subterráneos masivos (Y = -8..0)
        for (int y = -8; y <= 0; y++) {
            for (int dx = -r - 2; dx <= r + 2; dx++) {
                for (int dz = -r - 2; dz <= r + 2; dz++) {
                    int dSq = dx * dx + dz * dz;
                    if (dSq <= (r + 2) * (r + 2)) {
                        BlockPos p = new BlockPos(ox + dx, oy + y, oz + dz);
                        BlockState mat = (dSq >= r * r) ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState();
                        level.setBlock(p, mat, 2);
                    }
                }
            }
        }

        // 2. Cilindro Monumental Continuo de Grosor Constante (Y = 1..168)
        // Muros macizos herméticos: dSq entre rInner*rInner y r*r
        for (int y = 1; y <= TOWER_HEIGHT; y++) {
            // Verificar si este piso es un anillo de cornisa/balcón exterior (cada 28 bloques)
            boolean isBeltFloor = (y % 28 == 0);
            boolean isBeltParapet = (y % 28 == 1);
            boolean isBeltCorbel = (y % 28 == 27);

            for (int dx = -r - 2; dx <= r + 2; dx++) {
                for (int dz = -r - 2; dz <= r + 2; dz++) {
                    int dSq = dx * dx + dz * dz;

                    // Muro exterior cilíndrico estándar (Radio 13 a 16)
                    if (dSq <= r * r && dSq >= rInner * rInner) {
                        BlockPos p = new BlockPos(ox + dx, oy + y, oz + dz);

                        // Pilastras angulares cada 45 grados (8 pilastras verticales que recorren toda la torre)
                        double angle = (Math.toDegrees(Math.atan2(dz, dx)) + 360) % 360;
                        boolean isPilaster = (Math.abs(angle % 45) < 3.5 || Math.abs(angle % 45) > 41.5);

                        // Fachada frontal Sur (+Z): Puerta de entrada en Y=1..9 y Gran Puerta Rúnica en Y=36..52
                        if (dz >= r - 3 && Math.abs(dx) <= 3) {
                            if (y >= 1 && y <= 9) {
                                // Vano de la entrada principal
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                                continue;
                            }
                            if (y >= 36 && y <= 52) {
                                // Gran Puerta Rúnica Cian
                                if (Math.abs(dx) == 3 || y == 36 || y == 52) {
                                    level.setBlock(p, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                                } else if (Math.abs(dx) == 0 && y >= 39 && y <= 49) {
                                    level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                                } else {
                                    level.setBlock(p, Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 2);
                                }
                                continue;
                            }
                        }

                        // Ventanales ojivales rúnicos en los otros paños de la pared
                        int tierY = y % 28;
                        boolean isWindowZone = (tierY >= 8 && tierY <= 20);
                        boolean isWindowColumn = (!isPilaster && (angle % 45 >= 18 && angle % 45 <= 27));

                        if (isWindowZone && isWindowColumn) {
                            if (dSq >= (r - 1) * (r - 1)) {
                                // Vitral exterior violeta/magenta
                                BlockState glass = (tierY % 2 == 0) ? Blocks.PURPLE_STAINED_GLASS.defaultBlockState() : Blocks.MAGENTA_STAINED_GLASS.defaultBlockState();
                                level.setBlock(p, glass, 2);
                            } else {
                                // Luz interior que hace brillar el vitral
                                level.setBlock(p, Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(), 2);
                            }
                        } else {
                            // Bloques de piedra gótica estructural
                            BlockState st = isPilaster ? Blocks.CHISELED_DEEPSLATE.defaultBlockState()
                                    : ((y % 4 == 0) ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                            level.setBlock(p, st, 2);
                        }
                    }

                    // Cornisas de machicolado / balcones que abrazan el cilindro sin cambiar su grosor
                    if (isBeltFloor && dSq <= (r + 2) * (r + 2) && dSq >= r * r) {
                        level.setBlock(new BlockPos(ox + dx, oy + y, oz + dz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    }
                    if (isBeltParapet && dSq <= (r + 2) * (r + 2) && dSq >= (r + 1) * (r + 1)) {
                        level.setBlock(new BlockPos(ox + dx, oy + y, oz + dz), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
                    }
                    if (isBeltCorbel && dSq <= (r + 1) * (r + 1) && dSq >= r * r) {
                        level.setBlock(new BlockPos(ox + dx, oy + y, oz + dz), Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState(), 2);
                    }

                    // Interior del cilindro (dSq < rInner * rInner)
                    if (dSq < rInner * rInner) {
                        BlockPos ip = new BlockPos(ox + dx, oy + y, oz + dz);
                        if (y == 1) {
                            level.setBlock(ip, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                        } else if (isBeltFloor && dSq >= (rInner - 3) * (rInner - 3)) {
                            // Pasarela perimetral interior para poder caminar
                            level.setBlock(ip, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                        } else {
                            level.setBlock(ip, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 3. Gran Escalinata Frontal Funcional (Hacia el Sur +Z, Y = 1..4, Z = 16..24)
        for (int step = 0; step <= 5; step++) {
            int curZ = 16 + step;
            int stepY = 4 - step;
            if (stepY < 1) stepY = 1;

            for (int dx = -4; dx <= 4; dx++) {
                BlockPos sp = new BlockPos(ox + dx, oy + stepY, oz + curZ);
                // Escalera bien orientada: mirando al SUR para subir hacia el NORTE (al portal)
                level.setBlock(sp, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);

                // Relleno sólido debajo para que nunca quede hueco
                for (int fy = stepY - 1; fy >= 1; fy--) {
                    level.setBlock(new BlockPos(ox + dx, oy + fy, oz + curZ), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }

                // Barandillas laterales
                if (dx == -4 || dx == 4) {
                    level.setBlock(sp.above(), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
                }
            }
        }

        // 3b. Tablón Físico de Misiones Diario de la Torre (Sección 14.3)
        // Integrado en la base exterior de la torre como punto de encuentro de cazadores (5 bloques de ancho por 3 de alto)
        int boardX = ox - 7;
        int boardZ = oz + 20;
        for (int bx = -2; bx <= 2; bx++) {
            level.setBlock(new BlockPos(boardX + bx, oy, boardZ), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(boardX + bx, oy + 1, boardZ), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
        }
        for (int py = 1; py <= 4; py++) {
            level.setBlock(new BlockPos(boardX - 3, oy + py, boardZ), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(boardX + 3, oy + py, boardZ), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(boardX - 3, oy + 5, boardZ), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(boardX + 3, oy + 5, boardZ), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        BlockPos boardPos = new BlockPos(boardX, oy + 2, boardZ);
        level.setBlock(boardPos, SystemBlocks.TOWER_MISSION_BOARD.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.TowerMissionBoardBlock.FACING, Direction.SOUTH), 2);

        // Plaza simétrica este para reunión de cazadores (X = +7, Z = 20)
        int plazaX = ox + 7;
        for (int bx = -1; bx <= 1; bx++) {
            level.setBlock(new BlockPos(plazaX + bx, oy, boardZ), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(plazaX + bx, oy + 1, boardZ), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }
        level.setBlock(new BlockPos(plazaX - 2, oy + 1, boardZ), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(plazaX - 2, oy + 2, boardZ), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(plazaX + 2, oy + 1, boardZ), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(plazaX + 2, oy + 2, boardZ), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // 4. Portal de Entrada Gótico Interior en Y = 2..8
        for (int dx = -2; dx <= 2; dx++) {
            for (int y = 2; y <= 7; y++) {
                BlockPos pp = new BlockPos(ox + dx, oy + y, oz + (rInner + 1));
                if (dx == -2 || dx == 2 || y == 7) {
                    level.setBlock(pp, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                } else {
                    level.setBlock(pp, SystemBlocks.ASCENSION_PORTAL.get().defaultBlockState(), 2);
                }
            }
        }

        // 5. Corona Almenada Superior en Y = 168..172 (Manteniendo el mismo diámetro de fortaleza)
        for (int dx = -r - 2; dx <= r + 2; dx++) {
            for (int dz = -r - 2; dz <= r + 2; dz++) {
                int dSq = dx * dx + dz * dz;

                // Techo plano en Y = 168
                if (dSq <= (r + 1) * (r + 1)) {
                    level.setBlock(new BlockPos(ox + dx, oy + 168, oz + dz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }

                // Almenas perimetrales en Y = 169
                if (dSq <= (r + 1) * (r + 1) && dSq >= r * r) {
                    double angle = (Math.toDegrees(Math.atan2(dz, dx)) + 360) % 360;
                    if ((int) (angle / 10) % 2 == 0) {
                        level.setBlock(new BlockPos(ox + dx, oy + 169, oz + dz), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                        level.setBlock(new BlockPos(ox + dx, oy + 170, oz + dz), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
                    } else {
                        level.setBlock(new BlockPos(ox + dx, oy + 169, oz + dz), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Faro / Baliza Celestial Central en la cima de la corona
        for (int h = 1; h <= 4; h++) {
            level.setBlock(new BlockPos(ox, oy + 168 + h, oz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(ox, oy + 173, oz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
    }

    // =========================================================================
    // EXPORTACIÓN A JSON (Idéntico al Hipódromo)
    // =========================================================================
    public static int exportTower(ServerLevel level, BlockPos center, int customRadius) {
        int r = customRadius > 0 ? customRadius : TOWER_RADIUS + 8;
        int minY = -8;
        int maxY = TOWER_HEIGHT + 10;

        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        root.addProperty("radius", r);
        root.addProperty("height", maxY);

        JsonArray blocksArray = new JsonArray();
        int count = 0;

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                for (int y = cy + minY; y <= cy + maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState st = level.getBlockState(p);
                    if (!st.isAir()) {
                        JsonObject b = new JsonObject();
                        b.addProperty("x", x - cx);
                        b.addProperty("y", y - cy);
                        b.addProperty("z", z - cz);
                        b.addProperty("b", BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString());
                        blocksArray.add(b);
                        count++;
                    }
                }
            }
        }

        root.addProperty("count", count);
        root.add("blocks", blocksArray);

        Path outPath = getExportFilePath();
        try {
            if (outPath.getParent() != null) {
                Files.createDirectories(outPath.getParent());
            }
            Files.writeString(outPath, root.toString());
            AwakeningSystemMod.LOGGER.info("Torre exportada con {} bloques a {}", count, outPath);
            return count;
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al exportar la torre: ", e);
            return -1;
        }
    }

    // =========================================================================
    // IMPORTACIÓN DESDE JSON
    // =========================================================================
    public static boolean loadCustomExport(ServerLevel level, BlockPos origin) {
        Path p = getExportFilePath();
        Reader reader = null;
        try {
            if (Files.exists(p)) {
                reader = Files.newBufferedReader(p);
            } else {
                InputStream is = TowerStructureGenerator.class.getResourceAsStream("/data/awakening_system/custom_tower/tower_exterior.json");
                if (is != null) {
                    reader = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            if (reader == null) return false;

            try (Reader r = reader) {
                JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
                JsonArray blocks = root.getAsJsonArray("blocks");
                if (blocks == null) return false;

                int ox = origin.getX();
                int oy = origin.getY();
                int oz = origin.getZ();

                for (JsonElement el : blocks) {
                    JsonObject b = el.getAsJsonObject();
                    int x = ox + b.get("x").getAsInt();
                    int y = oy + b.get("y").getAsInt();
                    int z = oz + b.get("z").getAsInt();
                    String blockId = b.get("b").getAsString();

                    Block bl = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
                    if (bl != null && bl != Blocks.AIR) {
                        level.setBlock(new BlockPos(x, y, z), bl.defaultBlockState(), 2);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al cargar la torre custom: ", e);
            return false;
        }
    }
}
