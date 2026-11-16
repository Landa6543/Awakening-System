package com.soybe.awakeningsystem.entity.witherborn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.client.ShadowSkins;
import com.soybe.awakeningsystem.entity.witherborn.MutatedWitherContent;
import com.soybe.awakeningsystem.entity.witherborn.MutatedWitherSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The Mutated Wither Skeleton on screen: its model, its charcoal bones, and the core and runes glowing. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MutatedWitherClient {
    private MutatedWitherClient() {}

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mutated_wither_skeleton/mutated_wither_skeleton.png");
    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mutated_wither_skeleton/mutated_wither_skeleton_glow.png");

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MutatedWitherSkeletonModel.LAYER, MutatedWitherSkeletonModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MutatedWitherContent.MUTATED_WITHER_SKELETON, Renderer::new);
    }

    static final class Renderer extends MobRenderer<MutatedWitherSkeletonEntity, MutatedWitherSkeletonModel> {
        Renderer(EntityRendererProvider.Context context) {
            super(context, new MutatedWitherSkeletonModel(context.bakeLayer(MutatedWitherSkeletonModel.LAYER)), 0.8F);
            this.addLayer(new ShadowSkins.Eyes<>(this, GLOW));
        }

        @Override
        public ResourceLocation getTextureLocation(MutatedWitherSkeletonEntity entity) {
            return ShadowSkins.pick(entity, TEXTURE);
        }

        @Override
        protected void scale(MutatedWitherSkeletonEntity entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(0.82F, 0.82F, 0.82F);   // the model stands about 4.4 blocks; drawn at about 3.6
        }
    }
}
