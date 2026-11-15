package com.soybe.awakeningsystem.kaisel;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import com.soybe.awakeningsystem.entity.client.ShadowSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Registers Kaisel (entity, attributes, spawn egg) and, on the client, draws it and reads the rider's flight keys. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class KaiselContent {
    private KaiselContent() {}

    public static EntityType<KaiselEntity> KAISEL;
    public static Item KAISEL_EGG;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            KAISEL = EntityType.Builder.of(KaiselEntity::new, MobCategory.MONSTER).sized(3.6F, 4.0F).eyeHeight(3.8F)
                    .clientTrackingRange(16).updateInterval(1).build("kaisel");
            helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "kaisel"), KAISEL);
        });
        event.register(Registries.ITEM, helper -> {
            KAISEL_EGG = new DeferredSpawnEggItem(() -> KAISEL, 0xE9ECEF, 0x3FCBFF, new Item.Properties());
            helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "kaisel_spawn_egg"), KAISEL_EGG);
        });
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(KAISEL, KaiselEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) event.accept(KAISEL_EGG);
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(KAISEL, Renderer::new);
        }
    }

    /** The rider's keys, read where the rider plays: Space climbs, Ctrl (sprint) flies faster. */
    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
    public static final class Keys {
        private Keys() {}

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            boolean riding = mc.player != null && mc.player.getVehicle() instanceof KaiselEntity && mc.screen == null;
            KaiselEntity.clientUp = riding && mc.options.keyJump.isDown();
            KaiselEntity.clientFast = riding && mc.options.keySprint.isDown();
        }
    }

    static final class Model extends GeoModel<KaiselEntity> {
        private static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/entity/kaisel.geo.json");
        private static final ResourceLocation ANIM = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/entity/kaisel.animation.json");
        private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/kaisel/kaisel.png");

        @Override
        public ResourceLocation getModelResource(KaiselEntity e) { return GEO; }

        /** White alive; black and cyan once it is one of the Monarch's shadows (kaisel_shadow.png). */
        @Override
        public ResourceLocation getTextureResource(KaiselEntity e) { return ShadowSkins.pick(e, TEX); }

        @Override
        public ResourceLocation getAnimationResource(KaiselEntity e) { return ANIM; }
    }

    /** Kaisel at its size, with the glowing parts (golden eyes alive; cyan cracks, belly, crown and teeth as a shadow) lit. */
    public static final class Renderer extends GeoEntityRenderer<KaiselEntity> {
        public Renderer(EntityRendererProvider.Context ctx) {
            super(ctx, new Model());
            this.shadowRadius = 2.5F;
            withScale(1.5F);
            addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }
    }
}
