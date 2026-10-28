package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum RoulettePart implements StringRepresentable {
    WHEEL_LEFT("wheel_left", 0, 0, true),
    WHEEL_RIGHT("wheel_right", 1, 0, false),
    TABLE_MID_LEFT("table_mid_left", 0, 1, false),
    TABLE_MID_RIGHT("table_mid_right", 1, 1, false),
    TABLE_END_LEFT("table_end_left", 0, 2, false),
    TABLE_END_RIGHT("table_end_right", 1, 2, false);

    private final String name;
    private final int offsetX;
    private final int offsetZ;
    private final boolean isMaster;

    RoulettePart(String name, int offsetX, int offsetZ, boolean isMaster) {
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

    public static BlockPos getPartPos(BlockPos masterPos, RoulettePart part, Direction facing) {
        Direction right = facing.getClockWise();
        return masterPos.relative(right, part.offsetX).relative(facing, part.offsetZ);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
