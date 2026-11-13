package com.soybe.awakeningsystem.mortis.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.mortis.MortisContent;
import com.soybe.awakeningsystem.mortis.WalkingJurelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client side of the San Mortis content: the walking jurel's model and renderer. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MortisClient {
    private MortisClient() {}

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WalkingJurelModel.LAYER, WalkingJurelModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MortisContent.WALKING_JUREL, JurelRenderer::new);
    }

    static final class JurelRenderer extends MobRenderer<WalkingJurelEntity, WalkingJurelModel> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/walking_jurel.png");

        JurelRenderer(EntityRendererProvider.Context context) {
            super(context, new WalkingJurelModel(context.bakeLayer(WalkingJurelModel.LAYER)), 0.4F);
        }

        @Override
        public ResourceLocation getTextureLocation(WalkingJurelEntity entity) {
            return TEXTURE;
        }
    }
}
