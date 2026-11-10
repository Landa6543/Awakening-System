package com.soybe.awakeningsystem.tower.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import com.soybe.awakeningsystem.block.PotentialCrystalBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** Client side of the Great Tower: the measuring crystal takes the colour of the rank it shows. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GreatTowerClient {
    private GreatTowerClient() {}

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> tintIndex == 0
                ? PotentialCrystalBlock.COLORS[state.getValue(PotentialCrystalBlock.RANK)] : -1,
                SystemBlocks.POTENTIAL_CRYSTAL.get());
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | PotentialCrystalBlock.COLORS[0] : -1,
                SystemBlocks.POTENTIAL_CRYSTAL_ITEM.get());
    }

    @SubscribeEvent
    public static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) event.accept(SystemBlocks.POTENTIAL_CRYSTAL_ITEM.get());
    }
}
