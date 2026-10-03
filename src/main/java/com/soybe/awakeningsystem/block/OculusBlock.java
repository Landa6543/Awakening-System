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

public class OculusBlock extends Block {
    public static final MapCodec<OculusBlock> CODEC = simpleCodec(OculusBlock::new);

    // Hitbox centrado y flotante para que coincida con el modelo 3D
    private static final VoxelShape SHAPE = Block.box(3.0D, 1.0D, 3.0D, 13.0D, 15.0D, 13.0D);

    public OculusBlock(BlockBehaviour.Properties properties) {
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

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // Permite atravesar el óculo etéreo como en Genshin
    }
}
