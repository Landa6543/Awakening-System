package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class VipRouletteTableBlock extends RouletteTableBlock {
    public static final MapCodec<VipRouletteTableBlock> CODEC = simpleCodec(VipRouletteTableBlock::new);

    public VipRouletteTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
