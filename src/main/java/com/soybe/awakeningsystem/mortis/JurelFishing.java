package com.soybe.awakeningsystem.mortis;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/**
 * Jureles live in the sea: fishing in an ocean or on a beach, a third of the fish that bite are jureles
 * instead of cod or salmon. Rivers and lakes keep their own fish.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class JurelFishing {
    private JurelFishing() {}

    @SubscribeEvent
    public static void onFished(ItemFishedEvent event) {
        if (MortisContent.JUREL == null) return;
        var hook = event.getHookEntity();
        var level = hook.level();
        var biome = level.getBiome(hook.blockPosition());
        if (!biome.is(BiomeTags.IS_OCEAN) && !biome.is(BiomeTags.IS_BEACH)) return;
        var player = event.getEntity();
        // the event holds a copy of the list but the very same stacks: the fish is emptied (so it is not thrown)
        // and a jurel flies to the player in its place, the way the game throws a catch
        for (ItemStack s : event.getDrops()) {
            if (!(s.is(Items.COD) || s.is(Items.SALMON)) || level.random.nextFloat() >= 0.33F) continue;
            int count = s.getCount();
            s.setCount(0);
            var item = new net.minecraft.world.entity.item.ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), new ItemStack(MortisContent.JUREL, count));
            double dx = player.getX() - hook.getX(), dy = player.getY() - hook.getY(), dz = player.getZ() - hook.getZ();
            item.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
            level.addFreshEntity(item);
        }
    }
}
