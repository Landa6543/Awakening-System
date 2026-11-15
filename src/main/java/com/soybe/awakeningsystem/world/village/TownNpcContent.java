package com.soybe.awakeningsystem.world.village;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Registers the town's people (entity, attributes, spawn egg) and, on the client, draws them in their skins. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class TownNpcContent {
    private TownNpcContent() {}

    public static EntityType<TownNpcEntity> TOWN_NPC;
    public static Item TOWN_NPC_EGG;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "town_npc");
        event.register(Registries.ENTITY_TYPE, helper -> {
            TOWN_NPC = EntityType.Builder.of(TownNpcEntity::new, MobCategory.CREATURE).sized(0.6F, 1.8F).eyeHeight(1.62F)
                    .clientTrackingRange(10).build("town_npc");
            helper.register(id, TOWN_NPC);
        });
        event.register(Registries.ITEM, helper -> {
            TOWN_NPC_EGG = new DeferredSpawnEggItem(() -> TOWN_NPC, 0x8A5A3A, 0xC84A3A, new Item.Properties());
            helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "town_npc_spawn_egg"), TOWN_NPC_EGG);
        });
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(TOWN_NPC, TownNpcEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MAGIC_ENTITIES_TAB.getKey()) event.accept(TOWN_NPC_EGG);
    }

    /** Client side: a player-shaped body (wide arms) wearing the person's skin. */
    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(TOWN_NPC, Renderer::new);
        }
    }

    public static final class Renderer extends HumanoidMobRenderer<TownNpcEntity, PlayerModel<TownNpcEntity>> {
        private static final ResourceLocation[] TEXTURES = new ResourceLocation[TownNpcEntity.SKINS.length];

        static {
            for (int i = 0; i < TEXTURES.length; i++)
                TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/town_npc/" + TownNpcEntity.SKINS[i] + ".png");
        }

        private final PlayerModel<TownNpcEntity> wide, thin;

        public Renderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
            this.wide = this.model;
            this.thin = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        }

        @Override
        public void render(TownNpcEntity e, float yaw, float partial, com.mojang.blaze3d.vertex.PoseStack pose,
                           net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
            // skins drawn for the thin arms get the thin-armed body, the rest the wide one
            this.model = TownNpcEntity.slim(e.skin()) ? thin : wide;
            super.render(e, yaw, partial, pose, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(TownNpcEntity e) {
            return TEXTURES[e.skin()];
        }
    }
}
