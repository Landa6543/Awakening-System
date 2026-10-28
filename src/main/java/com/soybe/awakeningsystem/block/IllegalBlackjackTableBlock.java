package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class IllegalBlackjackTableBlock extends BlackjackTableBlock {
    public static final MapCodec<IllegalBlackjackTableBlock> CODEC = simpleCodec(IllegalBlackjackTableBlock::new);

    public IllegalBlackjackTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
