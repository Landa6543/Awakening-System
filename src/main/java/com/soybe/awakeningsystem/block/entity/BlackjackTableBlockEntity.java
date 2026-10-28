package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlackjackTableBlockEntity extends BlockEntity {

    public BlackjackTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACKJACK_TABLE.get(), pos, state);
    }
}
