package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TavernTableBlock extends Block {
    public static final MapCodec<TavernTableBlock> CODEC = simpleCodec(TavernTableBlock::new);

    private static final VoxelShape TOP = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape LEG_FL = Block.box(1.5D, 0.0D, 1.5D, 4.5D, 13.0D, 4.5D);
    private static final VoxelShape LEG_FR = Block.box(11.5D, 0.0D, 1.5D, 14.5D, 13.0D, 4.5D);
    private static final VoxelShape LEG_BL = Block.box(1.5D, 0.0D, 11.5D, 4.5D, 13.0D, 14.5D);
    private static final VoxelShape LEG_BR = Block.box(11.5D, 0.0D, 11.5D, 14.5D, 13.0D, 14.5D);
    private static final VoxelShape SHAPE = Shapes.or(TOP, LEG_FL, LEG_FR, LEG_BL, LEG_BR);

    public TavernTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}