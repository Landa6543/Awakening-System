package com.soybe.awakeningsystem.casino.pool;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class IllegalPoolTableBlock extends PoolTableBlock {
    public static final MapCodec<IllegalPoolTableBlock> CODEC = simpleCodec(IllegalPoolTableBlock::new);

    public IllegalPoolTableBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
