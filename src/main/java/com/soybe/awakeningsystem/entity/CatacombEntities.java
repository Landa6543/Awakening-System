package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Monsters of the mod's own dungeons (the Forgotten Catacombs and what comes after), registered on their
 * own so they do not crowd {@link ModEntities}.
 */
public final class CatacombEntities {
    private CatacombEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AwakeningSystemMod.MOD_ID);

    /** The Skeleton Lord: boss of the Forgotten Catacombs, about four and a half blocks tall. */
    public static final DeferredHolder<EntityType<?>, EntityType<SkeletonLordEntity>> SKELETON_LORD = ENTITY_TYPES.register("skeleton_lord",
            () -> EntityType.Builder.of(SkeletonLordEntity::new, MobCategory.MONSTER).sized(1.6F, 4.4F).clientTrackingRange(10).fireImmune()
                    .build("skeleton_lord"));

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Attributes {
        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(SKELETON_LORD.get(), SkeletonLordEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Renderers {
        @SubscribeEvent
        public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(SKELETON_LORD.get(), com.soybe.awakeningsystem.entity.client.SkeletonLordRenderer::new);
        }
    }
}
