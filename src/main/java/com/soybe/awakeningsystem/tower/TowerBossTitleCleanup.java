package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.world.WorldBossScalingManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * A creature that was once made a "WORLD BOSS - Lv. N" by mistake (a tower boss before the tower was left out,
 * or a common mob whose name only held a boss word, like the hi-LICH-url) loses the title when it loads, and gets
 * back its own name, health and damage.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TowerBossTitleCleanup {
    private TowerBossTitleCleanup() {}

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) return;
        var tag = mob.getPersistentData();
        if (!tag.contains("world_boss_scaled_level")) return;
        if (!GreatTowerWorldManager.isGreatTowerDimension(event.getLevel()) && WorldBossScalingManager.isWorldBoss(mob)) return;
        tag.remove("world_boss_scaled_level");
        tag.remove("rpg_boss_original_name");
        tag.remove("rpg_base_boss_level");
        if (tag.contains("rpg_base_boss_hp")) {
            var hp = mob.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(tag.getDouble("rpg_base_boss_hp"));
                mob.setHealth(Math.min(mob.getHealth(), mob.getMaxHealth()));
            }
            var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null && tag.contains("rpg_base_boss_dmg")) dmg.setBaseValue(tag.getDouble("rpg_base_boss_dmg"));
            tag.remove("rpg_base_boss_hp");
            tag.remove("rpg_base_boss_dmg");
        }
        tag.remove("arena_center_x");
        tag.remove("arena_center_y");
        tag.remove("arena_center_z");
        if (mob.getCustomName() != null && mob.getCustomName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
                && "awakening_system.boss.world_boss_title".equals(tc.getKey())) {
            mob.setCustomName(null);
            mob.setCustomNameVisible(false);
        }
    }
}
