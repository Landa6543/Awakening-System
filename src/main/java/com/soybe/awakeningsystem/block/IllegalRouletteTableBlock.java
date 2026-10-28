package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class IllegalRouletteTableBlock extends RouletteTableBlock {
    public static final MapCodec<IllegalRouletteTableBlock> CODEC = simpleCodec(IllegalRouletteTableBlock::new);

    public IllegalRouletteTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
