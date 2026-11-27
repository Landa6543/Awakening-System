package com.soybe.awakeningsystem.entity.mutant;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The Mutant Warden, registered on its own, and where it turns up in the Great Tower. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MutantWardenContent {
    private MutantWardenContent() {}

    public static EntityType<MutantWardenEntity> MUTANT_WARDEN;
    public static Item MUTANT_WARDEN_EGG;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutant_warden");
        event.register(Registries.ENTITY_TYPE, helper -> {
            MUTANT_WARDEN = EntityType.Builder.of(MutantWardenEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 3.4F).eyeHeight(3.0F).fireImmune().clientTrackingRange(16).build("mutant_warden");
            helper.register(id, MUTANT_WARDEN);
        });
        event.register(Registries.ITEM, helper -> {
            MUTANT_WARDEN_EGG = new DeferredSpawnEggItem(() -> MUTANT_WARDEN, 0x0E1A20, 0x2FD9E0, new Item.Properties());
            helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutant_warden_spawn_egg"), MUTANT_WARDEN_EGG);
        });
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(MUTANT_WARDEN, MutantWardenEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) event.accept(MUTANT_WARDEN_EGG);
    }

    /**
     * On floors 16 to 19 of the Great Tower, now and then one of the floor's spawns is a Mutant Warden
     * instead, never more than one near the same place.
     */
    public static LivingEntity maybeTowerWarden(ServerLevel level, int floor, RandomSource rand, BlockPos around) {
        if (floor != 10 || rand.nextFloat() > 0.07F) return null;
        if (!level.getEntitiesOfClass(MutantWardenEntity.class, new AABB(around).inflate(96.0D)).isEmpty()) return null;
        return MUTANT_WARDEN.create(level);
    }
}
