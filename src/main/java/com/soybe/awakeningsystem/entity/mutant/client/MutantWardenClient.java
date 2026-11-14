package com.soybe.awakeningsystem.entity.mutant.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.mutant.MutantWardenContent;
import com.soybe.awakeningsystem.entity.mutant.MutantWardenEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * The Mutant Warden on screen: the user's model and animations, the animated souls texture, and the
 * emissive core and spines breathing in a slow glow, as in the user's own layer.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MutantWardenClient {
    private MutantWardenClient() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MutantWardenContent.MUTANT_WARDEN, Renderer::new);
    }

    /** geo/mutant_warden.geo.json, animations/mutant_warden.animation.json, textures/entity/mutant_warden.png */
    static final class Model extends DefaultedEntityGeoModel<MutantWardenEntity> {
        Model() {
            super(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutant_warden"), false);
        }
    }

    static final class Renderer extends GeoEntityRenderer<MutantWardenEntity> {
        Renderer(EntityRendererProvider.Context context) {
            super(context, new Model());
            this.shadowRadius = 1.4F;
            addRenderLayer(new Breathing(this));
        }
    }

    /** The core and spines glow full-bright, breathing: dim on the out-breath, bright on the in-breath. */
    static final class Breathing extends GeoRenderLayer<MutantWardenEntity> {
        private static final ResourceLocation EMISSIVE =
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mutant_warden_emissive.png");

        Breathing(GeoRenderer<MutantWardenEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MutantWardenEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            float t = (animatable.tickCount + partialTick) * 0.07F;
            float breath = (Mth.sin(t) + 1.0F) * 0.5F;
            float glow = 0.2F + 0.8F * (float) Math.pow(breath, 1.6);
            int a = (int) (glow * 255.0F);
            int colour = (a << 24) | (a << 16) | (a << 8) | a;
            RenderType emissive = RenderType.entityTranslucentEmissive(EMISSIVE);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissive, bufferSource.getBuffer(emissive),
                    partialTick, 15728880, packedOverlay, colour);
        }
    }
}
