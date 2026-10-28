package com.soybe.awakeningsystem.tower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Generador procedural de árboles y estructuras con estética 100% Minecraft Vainilla:
 * - Roble y Abedul Vainilla (árboles normales y altos con copas frondosas).
 * - Roble Oscuro Vainilla (tronco 2x2 y copa ancha para bosques oscuros).
 * - Abeto Nevado Vainilla (taiga con nieve encima de las hojas).
 * - Árbol de Cerezo Vainilla (tronco curvado y hojas rosas).
 * - Acacia Vainilla (tronco diagonal y copa plana).
 * - Hongos Gigantes Vainilla del Nether.
 * - Cactus de Desierto Vainilla.
 */
public final class TowerBiomeFeatures {

    private TowerBiomeFeatures() {}

    private static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState OAK_LEAVES = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState BIRCH_LOG = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState BIRCH_LEAVES = Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState SPRUCE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState DARK_OAK_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState DARK_OAK_LEAVES = Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState CHERRY_LOG = Blocks.CHERRY_LOG.defaultBlockState();
    private static final BlockState CHERRY_LEAVES = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState ACACIA_LOG = Blocks.ACACIA_LOG.defaultBlockState();
    private static final BlockState ACACIA_LEAVES = Blocks.ACACIA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState WARPED_STEM = Blocks.WARPED_STEM.defaultBlockState();
    private static final BlockState WARPED_WART = Blocks.WARPED_WART_BLOCK.defaultBlockState();
    private static final BlockState SHROOMLIGHT = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState SNOW = Blocks.SNOW.defaultBlockState();
    private static final BlockState CACTUS = Blocks.CACTUS.defaultBlockState();

    /**
     * Genera un árbol de roble o abedul con copa frondosa estilo Minecraft Vainilla.
     */
    public static void placeVanillaOakTree(ChunkAccess chunk, int worldX, int groundY, int worldZ, int height, boolean isBirch) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockState log = isBirch ? BIRCH_LOG : OAK_LOG;
        BlockState leaves = isBirch ? BIRCH_LEAVES : OAK_LEAVES;
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < height; dy++) {
            setBlockIfInChunk(chunk, worldX, groundY + dy, worldZ, log, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        }

        int leafStart = groundY + height - 3;
        int leafTop = groundY + height + 1;
        for (int y = leafStart; y <= leafTop; y++) {
            int radius = (y >= leafTop - 1) ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && (y == leafStart || y == leafTop)) {
                        continue; // Esquinas recortadas estilo vainilla
                    }
                    int lx = worldX + dx;
                    int lz = worldZ + dz;
                    mPos.set(lx, y, lz);
                    if (isInChunk(lx, lz, cMinX, cMaxX, cMinZ, cMaxZ) && chunk.getBlockState(mPos).isAir()) {
                        chunk.setBlockState(mPos, leaves, false);
                    }
                }
            }
        }
    }

    /**
     * Genera un árbol de roble oscuro vainilla (tronco 2x2 y copa ancha y densa).
     */
    public static void placeVanillaDarkOakTree(ChunkAccess chunk, int worldX, int groundY, int worldZ, int height) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        // Tronco 2x2
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    setBlockIfInChunk(chunk, worldX + dx, groundY + dy, worldZ + dz, DARK_OAK_LOG, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
                }
            }
        }

        // Copa ancha y densa
        int topY = groundY + height;
        for (int y = topY - 2; y <= topY + 1; y++) {
            int radius = (y == topY + 1) ? 2 : 3;
            for (int dx = -radius; dx <= radius + 1; dx++) {
                for (int dz = -radius; dz <= radius + 1; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;
                    int lx = worldX + dx;
                    int lz = worldZ + dz;
                    mPos.set(lx, y, lz);
                    if (isInChunk(lx, lz, cMinX, cMaxX, cMinZ, cMaxZ) && chunk.getBlockState(mPos).isAir()) {
                        chunk.setBlockState(mPos, DARK_OAK_LEAVES, false);
                    }
                }
            }
        }
    }

    /**
     * Genera un abeto vainilla (taiga) cónico con opción de nieve sobre las ramas para el Piso 8.
     */
    public static void placeVanillaSpruceTree(ChunkAccess chunk, int worldX, int groundY, int worldZ, int height, boolean withSnow) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < height; dy++) {
            setBlockIfInChunk(chunk, worldX, groundY + dy, worldZ, SPRUCE_LOG, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        }

        // Hojas en cono escalonado
        int topY = groundY + height;
        setBlockIfInChunk(chunk, worldX, topY, worldZ, SPRUCE_LEAVES, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        if (withSnow) setBlockIfInChunk(chunk, worldX, topY + 1, worldZ, SNOW, cMinX, cMaxX, cMinZ, cMaxZ, mPos);

        for (int y = topY - 1; y >= groundY + 3; y--) {
            int distFromTop = topY - y;
            int radius = (distFromTop % 2 == 1) ? (distFromTop / 2 + 1) : (distFromTop / 2);
            radius = Math.min(2, radius);

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && radius > 1) continue;
                    int lx = worldX + dx;
                    int lz = worldZ + dz;
                    mPos.set(lx, y, lz);
                    if (isInChunk(lx, lz, cMinX, cMaxX, cMinZ, cMaxZ) && chunk.getBlockState(mPos).isAir()) {
                        chunk.setBlockState(mPos, SPRUCE_LEAVES, false);
                        if (withSnow) {
                            mPos.set(lx, y + 1, lz);
                            if (chunk.getBlockState(mPos).isAir()) {
                                chunk.setBlockState(mPos, SNOW, false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Genera un árbol de cerezo vainilla para el Piso 9.
     */
    public static void placeVanillaCherryTree(ChunkAccess chunk, int worldX, int groundY, int worldZ, int height) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < height; dy++) {
            int cx = (dy > height / 2) ? worldX + 1 : worldX;
            setBlockIfInChunk(chunk, cx, groundY + dy, worldZ, CHERRY_LOG, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        }

        int topY = groundY + height;
        for (int y = topY - 2; y <= topY + 1; y++) {
            int radius = (y == topY + 1) ? 2 : 3;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;
                    int lx = worldX + 1 + dx;
                    int lz = worldZ + dz;
                    mPos.set(lx, y, lz);
                    if (isInChunk(lx, lz, cMinX, cMaxX, cMinZ, cMaxZ) && chunk.getBlockState(mPos).isAir()) {
                        chunk.setBlockState(mPos, CHERRY_LEAVES, false);
                    }
                }
            }
        }
    }

    /**
     * Genera un árbol de acacia vainilla (sabana/desierto) con copa plana.
     */
    public static void placeVanillaAcaciaTree(ChunkAccess chunk, int worldX, int groundY, int worldZ, int height) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < height; dy++) {
            int off = dy / 3;
            setBlockIfInChunk(chunk, worldX + off, groundY + dy, worldZ + off, ACACIA_LOG, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        }

        int topX = worldX + height / 3;
        int topY = groundY + height;
        int topZ = worldZ + height / 3;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                int lx = topX + dx;
                int lz = topZ + dz;
                mPos.set(lx, topY, lz);
                if (isInChunk(lx, lz, cMinX, cMaxX, cMinZ, cMaxZ) && chunk.getBlockState(mPos).isAir()) {
                    chunk.setBlockState(mPos, ACACIA_LEAVES, false);
                }
            }
        }
    }

    /**
     * Genera un hongo gigante del Nether bien poblado para el Piso 4.
     */
    public static void placeVanillaWarpedFungus(ChunkAccess chunk, int worldX, int groundY, int worldZ, int stemHeight) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < stemHeight; dy++) {
            setBlockIfInChunk(chunk, worldX, groundY + dy, worldZ, WARPED_STEM, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        }

        int capY = groundY + stemHeight;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int lx = worldX + dx;
                int lz = worldZ + dz;
                setBlockIfInChunk(chunk, lx, capY, lz, WARPED_WART, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setBlockIfInChunk(chunk, lx, capY + 1, lz, WARPED_WART, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
                    if (dx == 0 && dz == 0) {
                        setBlockIfInChunk(chunk, lx, capY - 1, lz, SHROOMLIGHT, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
                    }
                }
            }
        }
    }

    /**
     * Genera un cactus vainilla en arena para el Piso 7 (Desierto).
     */
    public static void placeVanillaCactus(ChunkAccess chunk, int worldX, int groundY, int worldZ, int height) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= height; dy++) {
            setBlockIfInChunk(chunk, worldX, groundY + dy, worldZ, CACTUS, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
        }
    }

    /**
     * Coloca estalactitas en el techo de la cueva (Piso 5).
     */
    public static void placeStalactiteCluster(ChunkAccess chunk, int worldX, int roofY, int worldZ, int length) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        setBlockIfInChunk(chunk, worldX, roofY, worldZ, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), cMinX, cMaxX, cMinZ, cMaxZ, mPos);

        for (int i = 1; i <= length; i++) {
            int y = roofY - i;
            mPos.set(worldX, y, worldZ);
            if (isInChunk(worldX, worldZ, cMinX, cMaxX, cMinZ, cMaxZ) && chunk.getBlockState(mPos).isAir()) {
                DripstoneThickness thickness = (i == 1) ? DripstoneThickness.BASE : ((i == length) ? DripstoneThickness.TIP : DripstoneThickness.MIDDLE);
                BlockState dripState = Blocks.POINTED_DRIPSTONE.defaultBlockState()
                        .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                        .setValue(PointedDripstoneBlock.THICKNESS, thickness);
                chunk.setBlockState(mPos, dripState, false);
            }
        }
    }

    /**
     * Columna masiva de piedra natural que une suelo y techo en la cueva (Piso 5).
     */
    public static void placeCavePillar(ChunkAccess chunk, int worldX, int groundY, int roofY, int worldZ, BlockState stone) {
        int cMinX = chunk.getPos().getMinBlockX();
        int cMaxX = chunk.getPos().getMaxBlockX();
        int cMinZ = chunk.getPos().getMinBlockZ();
        int cMaxZ = chunk.getPos().getMaxBlockZ();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int y = groundY; y <= roofY; y++) {
            int radius = (y < groundY + 3 || y > roofY - 3) ? 2 : 1;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && radius > 1) continue;
                    setBlockIfInChunk(chunk, worldX + dx, y, worldZ + dz, stone, cMinX, cMaxX, cMinZ, cMaxZ, mPos);
                }
            }
        }
    }

    private static boolean isInChunk(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static void setBlockIfInChunk(ChunkAccess chunk, int x, int y, int z, BlockState state, int minX, int maxX, int minZ, int maxZ, BlockPos.MutableBlockPos mPos) {
        if (isInChunk(x, z, minX, maxX, minZ, maxZ)) {
            mPos.set(x, y, z);
            chunk.setBlockState(mPos, state, false);
        }
    }
}
