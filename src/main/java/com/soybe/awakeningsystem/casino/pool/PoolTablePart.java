package com.soybe.awakeningsystem.casino.pool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum PoolTablePart implements StringRepresentable {
    CORNER_NW("corner_nw", 0, 0, true),
    EDGE_N("edge_n", 1, 0, false),
    CORNER_NE("corner_ne", 2, 0, false),

    SIDE_W1("side_w1", 0, 1, false),
    CENTER_1("center_1", 1, 1, false),
    SIDE_E1("side_e1", 2, 1, false),

    SIDE_W2("side_w2", 0, 2, false),
    CENTER_2("center_2", 1, 2, false),
    SIDE_E2("side_e2", 2, 2, false),

    CORNER_SW("corner_sw", 0, 3, false),
    EDGE_S("edge_s", 1, 3, false),
    CORNER_SE("corner_se", 2, 3, false);

    private final String name;
    private final int offsetX;
    private final int offsetZ;
    private final boolean isMaster;

    PoolTablePart(String name, int offsetX, int offsetZ, boolean isMaster) {
        this.name = name;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.isMaster = isMaster;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public BlockPos getMasterPos(BlockPos pos, Direction facing) {
        Direction right = facing.getClockWise();
        return pos.relative(right, -this.offsetX).relative(facing, -this.offsetZ);
    }

    public static BlockPos getPartPos(BlockPos masterPos, PoolTablePart part, Direction facing) {
        Direction right = facing.getClockWise();
        return masterPos.relative(right, part.offsetX).relative(facing, part.offsetZ);
    }
}
