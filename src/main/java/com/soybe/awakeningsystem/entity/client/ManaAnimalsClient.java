package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Model layers of the mana animals (vanilla shapes with crystals) and the glow of their crystals. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ManaAnimalsClient {
    private ManaAnimalsClient() {}

    public static final ModelLayerLocation PIG_CRYSTALS = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pig_crystals"), "main");

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ManaCowModel.LAYER, ManaCowModel::createBodyLayer);
        event.registerLayerDefinition(ManaSheepModel.LAYER, ManaSheepModel::createBodyLayer);
        event.registerLayerDefinition(ManaChickenModel.LAYER, ManaChickenModel::createBodyLayer);
        event.registerLayerDefinition(PIG_CRYSTALS, ManaAnimalsClient::pigWithCrystals);
    }

    /** The mana pig's own model with a ridge of violet crystals along its back and two on its brow. */
    public static LayerDefinition pigWithCrystals() {
        return ManaCrystals.extend(ManaPigModel.createBodyLayer(), root -> {
            PartDefinition head = root.getChild("head");
            ManaCrystals.shard(head, -2.0F, -4.0F, -4.0F, 3, -0.2F, -0.35F);
            ManaCrystals.shard(head, 2.0F, -4.0F, -5.0F, 2, -0.2F, 0.35F);
            ManaCrystals.shard(root, 0.0F, 10.5F, -4.0F, 5, -0.2F, 0.1F);
            ManaCrystals.shard(root, -2.5F, 10.5F, 0.0F, 4, 0.1F, -0.4F);
            ManaCrystals.shard(root, 2.0F, 10.5F, 3.0F, 6, 0.15F, 0.25F);
            ManaCrystals.shard(root, -0.5F, 10.5F, 6.5F, 3, 0.4F, -0.1F);
        });
    }

    /** Draws the crystals (the only opaque pixels of the glow texture) at full brightness. */
    public static class Glow<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
        private final RenderType type;

        public Glow(RenderLayerParent<T, M> parent, String animal) {
            super(parent);
            this.type = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                    "textures/entity/animal/" + animal + "_glow.png"));
        }

        @Override
        public RenderType renderType() {
            return type;
        }
    }
}
