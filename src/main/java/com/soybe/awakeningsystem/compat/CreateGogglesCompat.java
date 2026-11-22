package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.TitleDefinitions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Predicate;

/**
 * The Creator and Orthax titles see Create's kinetic readings (speed, stress, capacity) as if wearing the
 * Engineer's Goggles. Done through Create's own hook, {@code GogglesItem.addIsWearingPredicate}, found by
 * reflection so nothing is needed when Create is not installed (it replaces a mixin into Create's goggles).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CreateGogglesCompat {
    private CreateGogglesCompat() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("create")) return;
        event.enqueueWork(() -> {
            try {
                Class<?> goggles = Class.forName("com.simibubi.create.content.equipment.goggles.GogglesItem");
                Predicate<Player> titled = player -> {
                    try {
                        return player != null && TitleDefinitions.hasGogglesBuff(player);
                    } catch (Throwable t) {
                        return false;   // never let a title check break Create's goggles
                    }
                };
                goggles.getMethod("addIsWearingPredicate", Predicate.class).invoke(null, titled);
                AwakeningSystemMod.LOGGER.info("Create goggles: the Creator and Orthax titles see kinetic readings");
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.warn("Create goggles hook not available ({}); titles keep no goggles view", t.toString());
            }
        });
    }
}
