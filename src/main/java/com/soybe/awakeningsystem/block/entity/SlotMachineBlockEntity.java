package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class SlotMachineBlockEntity extends BlockEntity {
    private UUID currentUser;
    private long lastUsedTick = 0;

    public SlotMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLOT_MACHINE.get(), pos, state);
    }

    public boolean isInUse() {
        if (currentUser == null) return false;
        if (level != null && level.getGameTime() - lastUsedTick > 1200) { // 60 segundos timeout
            currentUser = null;
            return false;
        }
        return true;
    }

    public boolean isUsedBy(Player player) {
        return currentUser != null && currentUser.equals(player.getUUID());
    }

    public boolean tryLock(Player player) {
        long now = level != null ? level.getGameTime() : 0;
        if (currentUser == null || (now - lastUsedTick > 1200)) {
            currentUser = player.getUUID();
            lastUsedTick = now;
            setChanged();
            return true;
        }
        if (currentUser.equals(player.getUUID())) {
            lastUsedTick = now;
            return true;
        }
        return false;
    }

    public void unlock(Player player) {
        if (currentUser != null && currentUser.equals(player.getUUID())) {
            currentUser = null;
            setChanged();
        }
    }

    public void forceUnlock() {
        currentUser = null;
        setChanged();
    }
}
