package com.soybe.awakeningsystem.entity.bestiary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.bestiary.BestiaryContent;
import com.soybe.awakeningsystem.entity.bestiary.PatchworkBearEntity;
import com.soybe.awakeningsystem.entity.bestiary.SonrisaEntity;
import com.soybe.awakeningsystem.entity.client.ShadowSkins;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.function.Predicate;

/** The bestiary creatures on screen: their models, textures, and the eyes that light up when they wake. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BestiaryClient {
    private BestiaryClient() {}

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/" + path + ".png");
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PatchworkBearModel.LAYER, PatchworkBearModel::createBodyLayer);
        event.registerLayerDefinition(FeralBearModel.LAYER, FeralBearModel::createBodyLayer);
        event.registerLayerDefinition(SonrisaModel.LAYER, SonrisaModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BestiaryContent.PATCHWORK_BEAR, BearRenderer::new);
        event.registerEntityRenderer(BestiaryContent.SONRISA, SonrisaRenderer::new);
    }

    /** A glow drawn full-bright only while the creature is roused. */
    static final class WakeGlow<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        private final ResourceLocation glow;
        private final Predicate<T> awake;

        WakeGlow(RenderLayerParent<T, M> parent, ResourceLocation glow, Predicate<T> awake) {
            super(parent);
            this.glow = glow;
            this.awake = awake;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, T e, float limbSwing, float limbSwingAmount,
                           float partialTick, float age, float netHeadYaw, float headPitch) {
            if (!awake.test(e)) return;
            this.getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(glow)), 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
    }

    /** The bear: the toy model, and once it has turned, a model of its own (FeralBearModel) with its own skin. */
    static final class BearRenderer extends MobRenderer<PatchworkBearEntity, net.minecraft.client.model.EntityModel<PatchworkBearEntity>> {
        private static final ResourceLocation TEXTURE = tex("patchwork_bear/patchwork_bear");
        private static final ResourceLocation FERAL = tex("patchwork_bear/patchwork_bear_feral");
        private final PatchworkBearModel plush;
        private final FeralBearModel feral;

        BearRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PatchworkBearModel(ctx.bakeLayer(PatchworkBearModel.LAYER)), 0.7F);
            this.plush = (PatchworkBearModel) this.model;
            this.feral = new FeralBearModel(ctx.bakeLayer(FeralBearModel.LAYER));
            this.addLayer(new WakeGlow<>(this, tex("patchwork_bear/patchwork_bear_feral_glow"), PatchworkBearEntity::isHostile));
        }

        @Override
        public void render(PatchworkBearEntity e, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
            this.model = e.isHostile() ? feral : plush;
            super.render(e, yaw, partialTick, pose, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(PatchworkBearEntity e) {
            return ShadowSkins.pick(e, e.isHostile() ? FERAL : TEXTURE);
        }

        @Override
        protected void scale(PatchworkBearEntity e, PoseStack pose, float partialTick) {
            float s = e.isHostile() ? 1.12F : 1.15F;
            pose.scale(s, s, s);
        }
    }
    static final class SonrisaRenderer extends MobRenderer<SonrisaEntity, SonrisaModel> {
        private static final ResourceLocation TEXTURE = tex("sonrisa/sonrisa");
        /** Asleep it is just a rock: the same face with no mouth. */
        private static final ResourceLocation CLOSED = tex("sonrisa/sonrisa_closed");

        SonrisaRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SonrisaModel(ctx.bakeLayer(SonrisaModel.LAYER)), 1.0F);
            this.addLayer(new WakeGlow<>(this, tex("sonrisa/sonrisa_glow"), SonrisaEntity::isAwake));
        }

        @Override
        public ResourceLocation getTextureLocation(SonrisaEntity e) {
            return ShadowSkins.pick(e, e.mouth(1.0F) > 0.5F ? TEXTURE : CLOSED);
        }

        @Override
        protected void scale(SonrisaEntity e, PoseStack pose, float partialTick) {
            pose.scale(1.1F, 1.1F, 1.1F);
        }
    }
}
