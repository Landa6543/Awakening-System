package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbyssalOrcRenderer extends MobRenderer<AbyssalOrcEntity, AbyssalOrcModel> {
    private static final ResourceLocation RED =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_orc.png");
    private static final ResourceLocation GREEN =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_orc_green.png");

    public AbyssalOrcRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssalOrcModel(context.bakeLayer(AbyssalOrcModel.LAYER_LOCATION)), 0.95F);
        addLayer(new GlowingEyes(this));
    }

    /** The red eyes glow in the dark. */
    static final class GlowingEyes extends net.minecraft.client.renderer.entity.layers.EyesLayer<AbyssalOrcEntity, AbyssalOrcModel> {
        private static final net.minecraft.client.renderer.RenderType EYES = net.minecraft.client.renderer.RenderType.eyes(
                ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_orc_eyes.png"));

        GlowingEyes(net.minecraft.client.renderer.entity.RenderLayerParent<AbyssalOrcEntity, AbyssalOrcModel> parent) {
            super(parent);
        }

        @Override
        public net.minecraft.client.renderer.RenderType renderType() {
            return EYES;
        }
    }

    /** The model itself is a hulking brute; a little extra size makes it about three blocks tall. */
    @Override
    protected void scale(AbyssalOrcEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick) {
        poseStack.scale(1.08F, 1.08F, 1.08F);
    }

    @Override
    /** Each orc is red (cream stripes) or green (dark-red stripes), fixed by its id so it never changes. */
    public ResourceLocation getTextureLocation(AbyssalOrcEntity entity) {
        return entity.isGreen() ? GREEN : RED;
    }
}
