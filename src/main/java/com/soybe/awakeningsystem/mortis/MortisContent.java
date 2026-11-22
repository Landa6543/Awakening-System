package com.soybe.awakeningsystem.mortis;

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

/**
 * The "San Mortis" corner of the mod, registered on its own: the link ring (a communicator worn at the neck
 * or wrist), the walking jurel and its spawn egg. The giant can itself is built by {@link SanMortisCan}.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MortisContent {
    private MortisContent() {}

    public static EntityType<WalkingJurelEntity> WALKING_JUREL;
    public static Item LINK_RING;
    public static Item WALKING_JUREL_EGG;
    /** What a walking jurel leaves behind: a jurel, to cook and eat. */
    public static Item JUREL, COOKED_JUREL;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, path);
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            WALKING_JUREL = EntityType.Builder.of(WalkingJurelEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 1.25F).eyeHeight(1.0F).clientTrackingRange(10).build("walking_jurel");
            helper.register(id("walking_jurel"), WALKING_JUREL);
        });
        event.register(Registries.ITEM, helper -> {
            LINK_RING = new LinkRingItem(new Item.Properties().stacksTo(1));
            helper.register(id("link_ring"), LINK_RING);
            WALKING_JUREL_EGG = new DeferredSpawnEggItem(() -> WALKING_JUREL, 0x5B7FA6, 0xE9EEF2, new Item.Properties());
            helper.register(id("walking_jurel_spawn_egg"), WALKING_JUREL_EGG);
            JUREL = new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build()));
            helper.register(id("jurel"), JUREL);
            COOKED_JUREL = new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build()));
            helper.register(id("cooked_jurel"), COOKED_JUREL);
        });
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(WALKING_JUREL, WalkingJurelEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) {
            event.accept(LINK_RING);
            event.accept(WALKING_JUREL_EGG);
            event.accept(JUREL);
            event.accept(COOKED_JUREL);
        }
    }
}
