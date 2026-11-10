package com.soybe.awakeningsystem.tower.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

/**
 * The Great Cavern of Mana Crystals (floor 5): on top of the carved cave, giant crystal spires rising from the
 * floor on calcite feet, crystals hanging from the ceiling and patches of moss. A marker block at the chunk's
 * corner keeps each chunk from being dressed twice.
 */
public final class TowerCaveCrystals {
    private TowerCaveCrystals() {}

    public static void decorate(ServerLevel level, ChunkAccess chunk) {
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos marker = new BlockPos(x0, 3, z0);
        if (level.getBlockState(marker).is(Blocks.TUFF)) return;
        level.setBlock(marker, Blocks.TUFF.defaultBlockState(), 2 | 16);
        Random r = new Random(chunk.getPos().toLong() * 6364136223846793005L + 1442695040888963407L);

        if (r.nextFloat() < 0.38F) {
            BlockPos base = floorAt(level, x0 + 3 + r.nextInt(10), z0 + 3 + r.nextInt(10));
            if (base != null) spire(level, base, 6 + r.nextInt(10), r);
        }
        for (int i = 0; i < 2; i++) {
            if (r.nextFloat() < 0.45F) {
                BlockPos top = ceilingAt(level, x0 + 2 + r.nextInt(12), z0 + 2 + r.nextInt(12));
                if (top != null) hanger(level, top, 2 + r.nextInt(5));
            }
        }
        for (int i = 0; i < 3; i++) {
            BlockPos f = floorAt(level, x0 + 2 + r.nextInt(12), z0 + 2 + r.nextInt(12));   // stays inside this chunk
            if (f == null) continue;
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + dz * dz > 5 || r.nextFloat() < 0.25F) continue;
                    BlockPos g = f.offset(dx, -1, dz);
                    if (level.getBlockState(g).isSolid() && level.getBlockState(g.above()).isAir()) {
                        level.setBlock(g, Blocks.MOSS_BLOCK.defaultBlockState(), 2 | 16);
                        if (r.nextFloat() < 0.4F) level.setBlock(g.above(), Blocks.MOSS_CARPET.defaultBlockState(), 2 | 16);
                    }
                }
        }
    }

    /** The first open spot above the cave floor in a column. */
    private static BlockPos floorAt(ServerLevel level, int x, int z) {
        for (int y = 4; y < 100; y++) {
            BlockPos p = new BlockPos(x, y, z);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolid()) return p;
        }
        return null;
    }

    /** The first open spot under the cave ceiling in a column. */
    private static BlockPos ceilingAt(ServerLevel level, int x, int z) {
        for (int y = 124; y > 30; y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isSolid()) return p;
        }
        return null;
    }

    private static void spire(ServerLevel level, BlockPos base, int h, Random r) {
        for (int y = 0; y < h; y++) {
            double rad = 2.2 * (1.0 - (double) y / h) + 0.4;
            int ri = (int) Math.ceil(rad);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++) {
                    if (dx * dx + dz * dz > rad * rad) continue;
                    BlockPos p = base.offset(dx, y, dz);
                    if (!level.getBlockState(p).isAir()) continue;
                    BlockState s = y < 2 && dx * dx + dz * dz > 1 ? Blocks.CALCITE.defaultBlockState()
                            : (r.nextFloat() < 0.12F ? Blocks.BUDDING_AMETHYST.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState());
                    level.setBlock(p, s, 2 | 16);
                }
        }
        BlockPos tip = base.above(h);
        if (level.getBlockState(tip).isAir())
            level.setBlock(tip, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP), 2 | 16);
        // a few clusters growing from its sides
        for (int i = 0; i < 4; i++) {
            Direction d = Direction.from2DDataValue(i);
            BlockPos p = base.offset(d.getStepX() * 2, 2 + r.nextInt(Math.max(1, h / 2)), d.getStepZ() * 2);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.relative(d.getOpposite())).is(Blocks.AMETHYST_BLOCK))
                level.setBlock(p, Blocks.LARGE_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, d), 2 | 16);
        }
    }

    private static void hanger(ServerLevel level, BlockPos top, int len) {
        for (int k = 0; k < len; k++) {
            BlockPos p = top.below(k);
            if (!level.getBlockState(p).isAir()) return;
            level.setBlock(p, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2 | 16);
        }
        BlockPos tip = top.below(len);
        if (level.getBlockState(tip).isAir())
            level.setBlock(tip, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN), 2 | 16);
    }
}
