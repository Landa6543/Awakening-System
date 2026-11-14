package com.soybe.awakeningsystem.celestial;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Where celestial weapons come from in survival: strong creatures killed by a player sometimes drop one. The
 * tougher the creature, the better the chance and the rarer the weapon it can drop.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class CelestialDrops {
    private CelestialDrops() {}

    @SubscribeEvent
    public static void drops(LivingDropsEvent event) {
        var victim = event.getEntity();
        if (victim.level().isClientSide || !(event.getSource().getEntity() instanceof Player)) return;
        float hp = victim.getMaxHealth();
        if (hp < 300) return;
        float chance = hp >= 2000 ? 0.25F : hp >= 800 ? 0.15F : 0.08F;
        var rand = victim.getRandom();
        if (rand.nextFloat() > chance) return;
        int maxTier = hp >= 2000 ? 3 : hp >= 800 ? 2 : 1;
        List<CelestialWeapon> pool = new ArrayList<>();
        for (CelestialWeapon w : CelestialWeapon.values()) if (w.tier <= maxTier) pool.add(w);
        if (pool.isEmpty()) return;
        CelestialWeapon w = pool.get(rand.nextInt(pool.size()));
        event.getDrops().add(new ItemEntity(victim.level(), victim.getX(), victim.getY() + 0.5, victim.getZ(), new ItemStack(CelestialContent.ITEMS.get(w))));
    }
}
