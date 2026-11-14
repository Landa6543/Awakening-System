package com.soybe.awakeningsystem.celestial.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.celestial.CelestialContent;
import com.soybe.awakeningsystem.celestial.CelestialWeapon;
import com.soybe.awakeningsystem.celestial.CelestialWeaponItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client side of the celestial armory: how bolts and clouds are drawn, and Galaxia's second look. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CelestialClient {
    private CelestialClient() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CelestialContent.BOLT, CelestialBoltRenderer::new);
        event.registerEntityRenderer(CelestialContent.CLOUD, CelestialCloudRenderer::new);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(CelestialContent.ITEMS.get(CelestialWeapon.GALAXIA),
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "capricorn"),
                (stack, level, entity, seed) -> CelestialWeaponItem.capricorn(stack) ? 1.0F : 0.0F));
    }
}
