package com.soybe.awakeningsystem.entity.witherborn;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The Mutated Wither Skeleton (roadmap 14.9), registered on its own. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MutatedWitherContent {
    private MutatedWitherContent() {}

    public static EntityType<MutatedWitherSkeletonEntity> MUTATED_WITHER_SKELETON;
    public static Item EGG;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            MUTATED_WITHER_SKELETON = EntityType.Builder.of(MutatedWitherSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 3.6F).eyeHeight(3.25F).fireImmune().clientTrackingRange(12).build("mutated_wither_skeleton");
            helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutated_wither_skeleton"), MUTATED_WITHER_SKELETON);
        });
        event.register(Registries.ITEM, helper -> {
            EGG = new DeferredSpawnEggItem(() -> MUTATED_WITHER_SKELETON, 0x1C1B21, 0x8A3FD9, new Item.Properties());
            helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutated_wither_skeleton_spawn_egg"), EGG);
        });
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(MUTATED_WITHER_SKELETON, MutatedWitherSkeletonEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) event.accept(EGG);
    }
}
