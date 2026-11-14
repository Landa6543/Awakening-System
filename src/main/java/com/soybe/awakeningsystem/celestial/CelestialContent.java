package com.soybe.awakeningsystem.celestial;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.EnumMap;
import java.util.Map;

/** Registers the celestial armory: its weapons and the two things they leave in the world (bolts and clouds). */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CelestialContent {
    private CelestialContent() {}

    public static final Map<CelestialWeapon, CelestialWeaponItem> ITEMS = new EnumMap<>(CelestialWeapon.class);
    public static EntityType<CelestialBoltEntity> BOLT;
    public static EntityType<CelestialCloudEntity> CLOUD;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, path);
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            BOLT = EntityType.Builder.<CelestialBoltEntity>of(CelestialBoltEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1).build("celestial_bolt");
            CLOUD = EntityType.Builder.<CelestialCloudEntity>of(CelestialCloudEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F).clientTrackingRange(8).updateInterval(5).build("celestial_cloud");
            helper.register(id("celestial_bolt"), BOLT);
            helper.register(id("celestial_cloud"), CLOUD);
        });
        event.register(Registries.ITEM, helper -> {
            for (CelestialWeapon w : CelestialWeapon.values()) {
                CelestialWeaponItem item = new CelestialWeaponItem(w);
                ITEMS.put(w, item);
                helper.register(id(w.id), item);
            }
        });
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != ModCreativeTabs.COMBAT_TAB.getKey()) return;
        // right after the other unique weapons, not at the very end of the tab
        net.minecraft.world.item.ItemStack prev = new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.PHANTOMBLADE.get());
        for (Item item : ITEMS.values()) {
            net.minecraft.world.item.ItemStack next = new net.minecraft.world.item.ItemStack(item);
            try {
                event.insertAfter(prev, next, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } catch (RuntimeException e) {
                event.accept(item);                    // the anchor is missing: fall back to the end
            }
            prev = next;
        }
    }
}
