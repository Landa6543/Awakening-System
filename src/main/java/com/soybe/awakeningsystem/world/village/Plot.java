package com.soybe.awakeningsystem.world.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * One building site. Buildings are designed facing north (front wall at local z = 0, local x across the front,
 * local z going back, local y = 0 the floor); a plot turns them to face any way and lifts them to their ground.
 */
final class Plot {
    final VillageCanvas canvas;
    final int minX, minZ, floorY, w, d;
    final Rotation rot;

    /**
     * @param minX  world x of the footprint's west edge
     * @param minZ  world z of the footprint's north edge
     * @param front the world direction the front (door) faces
     */
    Plot(VillageCanvas canvas, int minX, int minZ, int floorY, int w, int d, Direction front) {
        this.canvas = canvas;
        this.minX = minX;
        this.minZ = minZ;
        this.floorY = floorY;
        this.w = w;
        this.d = d;
        this.rot = switch (front) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /** World footprint size along x and z. */
    static int worldW(int w, int d, Direction front) { return front.getAxis() == Direction.Axis.X ? d : w; }
    static int worldD(int w, int d, Direction front) { return front.getAxis() == Direction.Axis.X ? w : d; }

    BlockPos pos(int x, int y, int z) {
        int wx, wz;
        switch (rot) {
            case CLOCKWISE_90 -> { wx = (d - 1) - z; wz = x; }
            case CLOCKWISE_180 -> { wx = (w - 1) - x; wz = (d - 1) - z; }
            case COUNTERCLOCKWISE_90 -> { wx = z; wz = (w - 1) - x; }
            default -> { wx = x; wz = z; }
        }
        return new BlockPos(minX + wx, floorY + y, minZ + wz);
    }

    /** A local direction turned into the world. */
    Direction dir(Direction local) {
        return local.getAxis().isHorizontal() ? rot.rotate(local) : local;
    }

    void set(int x, int y, int z, BlockState s) {
        canvas.set(pos(x, y, z), s.rotate(rot));
    }

    void set(int x, int y, int z, Block b) {
        set(x, y, z, b.defaultBlockState());
    }

    BlockState get(int x, int y, int z) {
        return canvas.get(pos(x, y, z));
    }

    void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(x, y, z, s);
    }

    void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block b) {
        fill(x0, y0, z0, x1, y1, z1, b.defaultBlockState());
    }

    /** Same pseudo-random answer for the same spot, so rebuilding gives the same village. */
    int noise(int x, int y, int z) {
        BlockPos p = pos(x, y, z);
        int h = p.getX() * 73856093 ^ p.getY() * 19349663 ^ p.getZ() * 83492791;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        return (h ^ (h >>> 15)) & 0x7fffffff;
    }

    static BlockState facing(Block b, Direction f) {
        BlockState s = b.defaultBlockState();
        if (s.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return s.setValue(BlockStateProperties.HORIZONTAL_FACING, f);
        if (s.hasProperty(BlockStateProperties.FACING)) return s.setValue(BlockStateProperties.FACING, f);
        return s;
    }
}
