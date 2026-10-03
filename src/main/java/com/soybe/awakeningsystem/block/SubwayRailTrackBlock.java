package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Vía férrea 3D realista para el metro de ORV, totalmente funcional para vagonetas (minecarts).
 * Permite conexiones rectas, pendientes y giros/curvas de 90 grados con física de rieles de Minecraft.
 */
public class SubwayRailTrackBlock extends RailBlock {
    public static final MapCodec<SubwayRailTrackBlock> CODEC = simpleCodec(SubwayRailTrackBlock::new);

    public SubwayRailTrackBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<RailBlock> codec() {
        return (MapCodec<RailBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public float getRailMaxSpeed(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.vehicle.AbstractMinecart cart) {
        return 0.5f;
    }
}
