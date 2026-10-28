package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CasinoVipBarrierBlock extends FenceBlock {
    public static final MapCodec<CasinoVipBarrierBlock> CODEC = simpleCodec(CasinoVipBarrierBlock::new);

    public CasinoVipBarrierBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<FenceBlock> codec() {
        return (MapCodec<FenceBlock>) (MapCodec<?>) CODEC;
    }

    private static final net.minecraft.world.phys.shapes.VoxelShape INVISIBLE_WALL = net.minecraft.world.level.block.Block.box(0.0D, 0.0D, 0.0D, 16.0D, 64.0D, 16.0D);

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        if (context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc && ecc.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            if (!CasinoVipGateBlock.hasPlayerVipAccess(player)) {
                return INVISIBLE_WALL;
            }
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) {
        return state.getBlock() instanceof CasinoVipBarrierBlock
                || state.getBlock() instanceof FenceGateBlock
                || (!isExceptionForConnection(state) && isSideSolid);
    }
}
