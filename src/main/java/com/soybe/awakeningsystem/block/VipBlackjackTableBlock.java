package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class VipBlackjackTableBlock extends BlackjackTableBlock {
    public static final MapCodec<VipBlackjackTableBlock> CODEC = simpleCodec(VipBlackjackTableBlock::new);

    public VipBlackjackTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
