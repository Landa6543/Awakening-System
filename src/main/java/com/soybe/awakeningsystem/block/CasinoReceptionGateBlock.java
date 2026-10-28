package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

public class CasinoReceptionGateBlock extends FenceGateBlock {
    public static final MapCodec<CasinoReceptionGateBlock> CODEC = simpleCodec(CasinoReceptionGateBlock::new);

    public CasinoReceptionGateBlock(BlockBehaviour.Properties properties) {
        super(WoodType.OAK, properties);
    }

    @Override
    public MapCodec<FenceGateBlock> codec() {
        return (MapCodec<FenceGateBlock>) (MapCodec<?>) CODEC;
    }
}
