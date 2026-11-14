package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalNecromancerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbyssalNecromancerRenderer extends MobRenderer<AbyssalNecromancerEntity, AbyssalNecromancerModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_necromancer.png");
    private static final net.minecraft.client.renderer.RenderType GLOW = net.minecraft.client.renderer.RenderType.eyes(ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_necromancer_glow.png"));

    public AbyssalNecromancerRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssalNecromancerModel(context.bakeLayer(AbyssalNecromancerModel.LAYER_LOCATION)), 0.6F);
        // the soul-lit eyes, staff flame and souls glow in the dark
        this.addLayer(new net.minecraft.client.renderer.entity.layers.EyesLayer<>(this) {
            @Override
            public net.minecraft.client.renderer.RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalNecromancerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AbyssalNecromancerEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
    }
}
