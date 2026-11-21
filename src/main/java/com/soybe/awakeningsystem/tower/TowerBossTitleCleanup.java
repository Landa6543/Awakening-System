package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * A tower boss that was once given a "WORLD BOSS - Lv. N" title (before the tower was left out of world bosses)
 * loses it when it loads, and goes back to its own name.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TowerBossTitleCleanup {
    private TowerBossTitleCleanup() {}

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) return;
        if (!GreatTowerWorldManager.isGreatTowerDimension(event.getLevel())) return;
        var tag = mob.getPersistentData();
        if (!tag.contains("world_boss_scaled_level")) return;
        tag.remove("world_boss_scaled_level");
        tag.remove("rpg_boss_original_name");
        if (mob.getCustomName() != null && mob.getCustomName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
                && "awakening_system.boss.world_boss_title".equals(tc.getKey())) {
            mob.setCustomName(null);
            mob.setCustomNameVisible(false);
        }
    }
}
