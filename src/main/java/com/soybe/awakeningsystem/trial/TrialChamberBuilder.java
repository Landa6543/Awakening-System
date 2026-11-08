package com.soybe.awakeningsystem.trial;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Random;

/**
 * Cámaras de Desafío del Sistema, sobre la base de las Trial Chambers originales de Minecraft:
 * se genera la estructura con el propio generador del juego (pasillos, salas en varios pisos,
 * trampas de dispensadores, cobre y tuff) y luego se le da la identidad del mod:
 * <ul>
 *   <li>los generadores de prueba pasan a ser Generadores de Desafío de Sombras, con tema al azar;</li>
 *   <li>las bóvedas pasan a ser Bóvedas Rúnicas, que se abren con la Llave de Cámara del mod;</li>
 *   <li>el tuff cincelado pasa a ser Piedra Abisal Rúnica;</li>
 *   <li>una escalera de caracol baja desde un arco rúnico en la superficie hasta la primera sala.</li>
 * </ul>
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = com.soybe.awakeningsystem.AwakeningSystemMod.MOD_ID)
public final class TrialChamberBuilder {

    private final ServerLevel level;
    private final Random random;

    private TrialChamberBuilder(ServerLevel level, BlockPos origin) {
        this.level = level;
        this.random = new Random(origin.getX() * 341873128712L + origin.getZ() * 132897987541L);
    }

    /** Genera las cámaras bajo {@code surface}; devuelve la entrada en superficie, o null si no se pudo. */
    public static BlockPos build(ServerLevel level, BlockPos surface) {
        TrialChamberBuilder b = new TrialChamberBuilder(level, surface);
        StructureStart start = b.placeVanillaChambers(surface);
        if (start == null) return null;
        int[] counts = rethemeArea(level, start.getBoundingBox(), b.random);
        org.slf4j.LoggerFactory.getLogger("AwakeningSystem").info("[Cámaras de Desafío] {} generadores y {} bóvedas convertidos", counts[0], counts[1]);
        BoundingBox first = start.getPieces().get(0).getBoundingBox();
        return b.buildSpiralEntrance(first);
    }

    // ------------------------------------------------------------------ vanilla structure

    /** Lo mismo que hace el comando /place structure minecraft:trial_chambers. */
    private StructureStart placeVanillaChambers(BlockPos pos) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.TRIAL_CHAMBERS);
        if (structure == null) return null;
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        StructureStart start = structure.generate(level.registryAccess(), generator, generator.getBiomeSource(),
                level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(),
                new ChunkPos(pos), 0, level, biome -> true);
        if (!start.isValid()) return null;

        BoundingBox box = start.getBoundingBox();
        ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
        ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
        ChunkPos.rangeClosed(min, max).forEach(chunk -> {
            level.getChunk(chunk.x, chunk.z);
            start.placeInChunk(level, level.structureManager(), generator, level.getRandom(),
                    new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                            chunk.getMaxBlockX(), level.getMaxBuildHeight(), chunk.getMaxBlockZ()), chunk);
        });
        return start;
    }

    // ------------------------------------------------------------------ System identity

    private static final int[] THEMES = {TrialSpawnerBlockEntity.THEME_HALL, TrialSpawnerBlockEntity.THEME_MURIM,
            TrialSpawnerBlockEntity.THEME_SHADOW, TrialSpawnerBlockEntity.THEME_ELEMENTAL};

    /**
     * Las Trial Chambers que Minecraft genera por su cuenta también se convierten en Cámaras de
     * Desafío del Sistema: al cargarse por primera vez un chunk que forma parte de una, se cambian
     * sus generadores, bóvedas y tuff cincelado (en el siguiente tick, fuera de la carga del chunk).
     */
    @net.neoforged.bus.api.SubscribeEvent
    public static void onChunkLoad(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel sl)) return;
        if (!(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk)) return;
        Structure chambers = sl.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.TRIAL_CHAMBERS);
        if (chambers == null) return;
        if (chunk.getReferencesForStructure(chambers).isEmpty() && chunk.getStartForStructure(chambers) == null) return;
        ChunkPos cp = chunk.getPos();
        BoundingBox area = new BoundingBox(cp.getMinBlockX(), sl.getMinBuildHeight(), cp.getMinBlockZ(), cp.getMaxBlockX(), 0, cp.getMaxBlockZ());
        sl.getServer().execute(() -> rethemeArea(sl, area, new Random(cp.toLong())));
    }

    private static int[] rethemeArea(ServerLevel level, BoundingBox box, Random random) {
        int[] counts = new int[2];
        BlockState runic = TrialRegistry.RUNIC_ABYSSAL_STONE.get().defaultBlockState();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int y = box.minY(); y <= box.maxY(); y++) {
                    p.set(x, y, z);
                    BlockState state = level.getBlockState(p);
                    if (state.is(Blocks.TRIAL_SPAWNER)) {
                        level.setBlock(p, TrialRegistry.SHADOW_TRIAL_SPAWNER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                        if (level.getBlockEntity(p) instanceof TrialSpawnerBlockEntity be) {
                            be.setTheme(THEMES[random.nextInt(THEMES.length)]);
                            counts[0]++;
                        }
                    } else if (state.is(Blocks.VAULT)) {
                        Direction facing = state.getValue(VaultBlock.FACING);
                        level.setBlock(p, TrialRegistry.AWAKENING_VAULT.get().defaultBlockState().setValue(TrialVaultBlock.FACING, facing), Block.UPDATE_CLIENTS);
                        counts[1]++;
                    } else if (state.is(Blocks.CHISELED_TUFF) || state.is(Blocks.CHISELED_TUFF_BRICKS)) {
                        level.setBlock(p, runic, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        return counts;
    }

    // ------------------------------------------------------------------ spiral entrance

    /** Anillo de 8 celdas alrededor de la columna central, en sentido horario visto desde arriba. */
    private static final int[][] RING = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};

    /**
     * Escalera de caracol de piedra abisal desde la superficie hasta el techo de la primera sala,
     * con un pozo con escalera de mano para bajar dentro. Devuelve la posición del arco de entrada.
     */
    private BlockPos buildSpiralEntrance(BoundingBox room) {
        int cx = room.getCenter().getX(), cz = room.getCenter().getZ();
        BlockState abyssal = SystemBlocks.ABYSSAL_STONE.get().defaultBlockState();
        BlockState runic = TrialRegistry.RUNIC_ABYSSAL_STONE.get().defaultBlockState();
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        int bottom = room.maxY() + 1;

        for (int y = bottom; y <= surface + 3; y++) {
            int k = y - bottom;
            boolean belowGround = y < surface;
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = new BlockPos(cx + dx, y, cz + dz);
                boolean wall = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                if (wall) {
                    // las paredes solo bajo tierra: arriba el pozo queda abierto
                    if (belowGround) level.setBlock(p, k % 9 == 4 ? lit() : (k % 3 == 0 ? runic : abyssal), Block.UPDATE_CLIENTS);
                } else if (dx == 0 && dz == 0) {
                    if (belowGround) level.setBlock(p, abyssal, Block.UPDATE_CLIENTS);
                } else {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            if (!belowGround) continue;
            // el escalón de esta altura, subiendo en sentido horario
            int[] cell = RING[k % RING.length];
            int[] next = RING[(k + 1) % RING.length];
            Direction up = horizontal(next[0] - cell[0], next[1] - cell[1]);
            level.setBlock(new BlockPos(cx + cell[0], y, cz + cell[1]),
                    Blocks.TUFF_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, up), Block.UPDATE_CLIENTS);
            // cada escalón se apoya en un bloque justo debajo
            if (k > 0) level.setBlock(new BlockPos(cx + cell[0], y - 1, cz + cell[1]), Blocks.TUFF_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        // pozo con escalera de mano hasta dentro de la sala, en una celda libre al pie del caracol
        BlockPos hole = new BlockPos(cx + RING[4][0], bottom, cz + RING[4][1]);
        for (int y = bottom - 1; y >= room.minY(); y--) {
            BlockPos p = new BlockPos(hole.getX(), y, hole.getZ());
            BlockState s = level.getBlockState(p);
            if (s.isAir()) break;
            level.setBlock(p, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
            BlockPos support = p.north();
            if (!level.getBlockState(support).isSolid()) level.setBlock(support, abyssal, Block.UPDATE_CLIENTS);
        }

        // arco rúnico de entrada en la superficie
        for (int y = 0; y < 5; y++) {
            for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
                level.setBlock(new BlockPos(cx + c[0], surface + y, cz + c[1]), y == 4 ? runic : abyssal, Block.UPDATE_CLIENTS);
            }
        }
        for (int d = -3; d <= 3; d++) {
            level.setBlock(new BlockPos(cx + d, surface + 5, cz - 3), runic, Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(cx + d, surface + 5, cz + 3), runic, Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(cx - 3, surface + 5, cz + d), runic, Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(cx + 3, surface + 5, cz + d), runic, Block.UPDATE_CLIENTS);
        }
        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        for (int[] c : new int[][]{{-3, -2}, {3, -2}, {-3, 2}, {3, 2}}) {
            level.setBlock(new BlockPos(cx + c[0], surface + 4, cz + c[1]), lantern, Block.UPDATE_CLIENTS);
        }
        return new BlockPos(cx, surface, cz + 4);
    }

    private BlockState lit() {
        return Blocks.WAXED_WEATHERED_COPPER_BULB.defaultBlockState().setValue(BlockStateProperties.LIT, true);
    }

    private static Direction horizontal(int dx, int dz) {
        if (dx > 0) return Direction.EAST;
        if (dx < 0) return Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
