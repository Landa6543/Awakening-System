package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum BlackjackPart implements StringRepresentable {
    FRONT_LEFT("front_left", 0, 0, true),
    FRONT_RIGHT("front_right", 1, 0, false),
    BACK_LEFT("back_left", 0, 1, false),
    BACK_RIGHT("back_right", 1, 1, false);

    private final String name;
    private final int offsetX;
    private final int offsetZ;
    private final boolean isMaster;

    BlackjackPart(String name, int offsetX, int offsetZ, boolean isMaster) {
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

    public static BlockPos getPartPos(BlockPos masterPos, BlackjackPart part, Direction facing) {
        Direction right = facing.getClockWise();
        return masterPos.relative(right, part.offsetX).relative(facing, part.offsetZ);
    }
}
