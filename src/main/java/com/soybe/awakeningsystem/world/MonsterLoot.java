package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Every hostile creature a player kills now leaves something: Awakening coins and, now and then, a Monster Core of
 * the rank its level belongs to. Most of the mod's creatures had no loot table at all, and the cores never dropped
 * anywhere, so the Hunter Forge had nothing to work with.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class MonsterLoot {
    private MonsterLoot() {}

    @SubscribeEvent
    public static void drops(LivingDropsEvent event) {
        LivingEntity mob = event.getEntity();
        if (mob.level().isClientSide() || mob instanceof Player) return;
        if (!(mob instanceof Enemy) && !mob.getTags().contains("dungeon_mob") && !mob.getTags().contains("tower_mob")) return;
        if (mob instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature) return;
        if (mob.getTags().contains("shadow_soldier") || mob.getTags().contains("no_loot")) return;
        if (!killedByPlayer(event)) return;

        RandomSource r = mob.getRandom();
        int level = level(mob);
        boolean boss = isBoss(mob);
        boolean elite = !boss && isElite(mob);

        // coins: most kills give a few, more the stronger the creature (creatures with a purse of their own keep theirs)
        boolean ownCoins = bound(SystemItems.AWAKENING_COIN)
                && event.getDrops().stream().anyMatch(i -> i.getItem().is(SystemItems.AWAKENING_COIN.get()));
        int coins = 0;
        if (ownCoins) coins = 0;
        else if (boss) coins = 16 + level / 2 + r.nextInt(8 + level / 4);
        else if (elite) coins = 6 + level / 5 + r.nextInt(4 + level / 8);
        else if (r.nextFloat() < 0.7f) coins = 2 + r.nextInt(2 + level / 5);
        if (coins > 0 && bound(SystemItems.AWAKENING_COIN)) add(event, new ItemStack(SystemItems.AWAKENING_COIN.get(), Math.min(64, coins)));

        // a Monster Core of the creature's rank: rare from common monsters, likely from elites, sure from bosses
        float coreChance = boss ? 1.0f : elite ? 0.4f : 0.06f;
        if (r.nextFloat() < coreChance) {
            var core = coreFor(level);
            if (bound(core)) add(event, new ItemStack(core.get(), boss ? 2 + r.nextInt(2) : 1));
        }
    }

    private static boolean killedByPlayer(LivingDropsEvent event) {
        var src = event.getSource().getEntity();
        if (src instanceof Player) return true;
        if (src instanceof TamableAnimal t && t.getOwner() instanceof Player) return true;
        if (src != null) for (String tag : src.getTags()) if (tag.startsWith("shadow_master_")) return true;
        return event.getEntity().getLastHurtByMob() instanceof Player;
    }

    /** The RPG level the mod gave the creature; a dungeon minion without one is judged by its health. */
    private static int level(LivingEntity mob) {
        int lvl = mob.getPersistentData().getInt("rpg_mob_level");
        if (lvl > 0) return lvl;
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        return Math.max(1, Math.min(300, (int) ((hp != null ? hp.getValue() : mob.getMaxHealth()) / 5.0)));
    }

    private static boolean isBoss(LivingEntity mob) {
        var t = mob.getTags();
        return t.contains("dungeon_boss") || t.contains("tower_boss") || t.contains("world_boss") || t.contains("dimensional_boss")
                || com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(mob);
    }

    private static boolean isElite(LivingEntity mob) {
        var t = mob.getTags();
        return t.contains("dungeon_miniboss") || t.contains("dungeon_chamber_boss") || t.contains("dungeon_guardian")
                || t.contains("tower_elite") || mob.getPersistentData().getBoolean("is_overworld_miniboss");
    }

    private static net.neoforged.neoforge.registries.DeferredHolder<Item, Item> coreFor(int level) {
        if (level < 20) return SystemItems.MONSTER_CORE_E;
        if (level < 45) return SystemItems.MONSTER_CORE_D;
        if (level < 75) return SystemItems.MONSTER_CORE_C;
        if (level < 110) return SystemItems.MONSTER_CORE_B;
        if (level < 160) return SystemItems.MONSTER_CORE_A;
        return SystemItems.MONSTER_CORE_S;
    }

    private static boolean bound(Holder<Item> h) {
        return h != null && h.isBound();
    }

    private static void add(LivingDropsEvent event, ItemStack stack) {
        LivingEntity e = event.getEntity();
        ItemEntity item = new ItemEntity(e.level(), e.getX(), e.getY() + 0.3, e.getZ(), stack);
        item.setDefaultPickUpDelay();
        event.getDrops().add(item);
    }
}
