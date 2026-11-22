package com.soybe.awakeningsystem.abyss.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.abyss.SpiralAbyssManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * Inside the Spiral Abyss the Warped Forest's own sounds (its humming loop, its mood and the odd beeps of its
 * "additions") are never played: arenas built in worlds from before the Abyss had its calm biome still carry it.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class AbyssQuiet {
    private AbyssQuiet() {}

    @SubscribeEvent
    public static void onSound(PlaySoundEvent event) {
        if (event.getSound() == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(SpiralAbyssManager.SPIRAL_ABYSS_DIMENSION)) return;
        String path = event.getSound().getLocation().getPath();
        if (path.startsWith("ambient.warped_forest") || path.startsWith("ambient.crimson_forest")
                || path.startsWith("ambient.soul_sand_valley") || path.startsWith("ambient.nether_wastes")
                || path.startsWith("ambient.basalt_deltas")) {
            event.setSound(null);
        }
    }
}
