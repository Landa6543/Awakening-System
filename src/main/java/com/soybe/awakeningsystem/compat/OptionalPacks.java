package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * Built-in data packs that only switch on when another mod is there, so the mod never depends on it.
 * <ul>
 *   <li>{@code compat/ascended_arts}: with Ascended Arts (an Epic Fight addon) the scythes get its real scythe
 *   moveset instead of borrowing the spear's.</li>
 * </ul>
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OptionalPacks {
    private OptionalPacks() {}

    @SubscribeEvent
    public static void addPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        if (ModList.get().isLoaded("ascended_arts") && ModList.get().isLoaded("epicfight")) {
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "compat/ascended_arts"),
                    PackType.SERVER_DATA, Component.literal("Awakening System: Ascended Arts scythes"),
                    PackSource.BUILT_IN, true, Pack.Position.TOP);
        }
    }
}
