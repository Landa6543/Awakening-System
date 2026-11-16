package com.soybe.awakeningsystem.entity.bestiary;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Creatures of the manga bestiary (roadmap 14.10 Patchwork Bear, 14.12 Sonrisa) and where they turn up in the tower. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class BestiaryContent {
    private BestiaryContent() {}

    public static EntityType<PatchworkBearEntity> PATCHWORK_BEAR;
    public static EntityType<SonrisaEntity> SONRISA;
    public static Item BEAR_EGG, SONRISA_EGG;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            PATCHWORK_BEAR = EntityType.Builder.of(PatchworkBearEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 1.9F).eyeHeight(1.65F).clientTrackingRange(10).build("patchwork_bear");
            helper.register(id("patchwork_bear"), PATCHWORK_BEAR);
            SONRISA = EntityType.Builder.of(SonrisaEntity::new, MobCategory.MONSTER)
                    .sized(1.9F, 1.9F).eyeHeight(1.1F).clientTrackingRange(10).build("sonrisa");
            helper.register(id("sonrisa"), SONRISA);
        });
        event.register(Registries.ITEM, helper -> {
            BEAR_EGG = new DeferredSpawnEggItem(() -> PATCHWORK_BEAR, 0xA8825A, 0x6E1E1E, new Item.Properties());
            helper.register(id("patchwork_bear_spawn_egg"), BEAR_EGG);
            SONRISA_EGG = new DeferredSpawnEggItem(() -> SONRISA, 0x78746C, 0x1E2A6E, new Item.Properties());
            helper.register(id("sonrisa_spawn_egg"), SONRISA_EGG);
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, path);
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(PATCHWORK_BEAR, PatchworkBearEntity.createAttributes().build());
        event.put(SONRISA, SonrisaEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) {
            event.accept(BEAR_EGG);
            event.accept(SONRISA_EGG);
        }
    }

    /**
     * Now and then one of a floor's spawns is one of these instead: the bear on the dead forest (19), the dry
     * marshes (22) and the storm peaks (24); Sonrisa in the great cavern (5) and among the copper rocks (24).
     */
    public static LivingEntity maybeFloorMob(ServerLevel level, int floor, RandomSource rand) {
        float r = rand.nextFloat();
        if ((floor == 19 || floor == 22 || floor == 24) && r < 0.15F) return PATCHWORK_BEAR.create(level);
        if (floor == 5 && r < 0.2F) return SONRISA.create(level);
        if (floor == 24 && r >= 0.15F && r < 0.27F) return SONRISA.create(level);
        return null;
    }
}
