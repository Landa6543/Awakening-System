package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TowerMissionBoardBlockEntity extends BlockEntity {
    public TowerMissionBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWER_MISSION_BOARD.get(), pos, state);
    }
}
