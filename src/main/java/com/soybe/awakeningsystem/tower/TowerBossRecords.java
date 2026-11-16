package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.DimensionGenSavedData;
import com.soybe.awakeningsystem.entity.MotherSpiderBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Remembers when the Nest Queen of floor 20 falls, which opens the floor's way up (it was never written down, so
 * she came back and nothing knew she had been beaten), and tells the players there.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TowerBossRecords {
    private TowerBossRecords() {}

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof MotherSpiderBossEntity) || !(event.getEntity().level() instanceof ServerLevel sl)) return;
        if (!GreatTowerWorldManager.isGreatTowerDimension(sl) || GreatTowerWorldManager.getFloorForPos(sl, event.getEntity().blockPosition()) != 20) return;
        DimensionGenSavedData.get(sl.getServer()).setTowerFloor20BossDefeated(true);
        for (ServerPlayer p : sl.players()) p.displayClientMessage(Component.translatable("awakening_system.msg.tower_boss_unsealed"), false);
    }
}
