package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbyssalClusterBlock extends TeyvatCrystalBlock {
    public enum Stage {
        SMALL(5.0D, 4.0D),
        MEDIUM(8.0D, 3.0D),
        LARGE(12.0D, 2.0D),
        CLUSTER(16.0D, 1.0D);

        final double height;
        final double offset;

        Stage(double height, double offset) {
            this.height = height;
            this.offset = offset;
        }
    }

    private final Stage stage;
    private final VoxelShape upShape;
    private final VoxelShape downShape;
    private final VoxelShape northShape;
    private final VoxelShape southShape;
    private final VoxelShape westShape;
    private final VoxelShape eastShape;

    public AbyssalClusterBlock(Stage stage, Properties properties) {
        super(properties);
        this.stage = stage;
        double off = stage.offset;
        double h = stage.height;
        this.upShape = Block.box(off, 0.0D, off, 16.0D - off, h, 16.0D - off);
        this.downShape = Block.box(off, 16.0D - h, off, 16.0D - off, 16.0D, 16.0D - off);
        this.northShape = Block.box(off, off, 16.0D - h, 16.0D - off, 16.0D - off, 16.0D);
        this.southShape = Block.box(off, off, 0.0D, 16.0D - off, 16.0D - off, h);
        this.westShape = Block.box(16.0D - h, off, off, 16.0D, 16.0D - off, 16.0D - off);
        this.eastShape = Block.box(0.0D, off, off, h, 16.0D - off, 16.0D - off);
    }

    public Stage getStage() {
        return this.stage;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case DOWN -> downShape;
            case NORTH -> northShape;
            case SOUTH -> southShape;
            case WEST -> westShape;
            case EAST -> eastShape;
            case UP -> upShape;
        };
    }
}
