package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AzhdahaBossEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class AzhdahaRenderer extends MobRenderer<AzhdahaBossEntity, AzhdahaModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/azhdaha.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/azhdaha_glow.png");

    public AzhdahaRenderer(EntityRendererProvider.Context context) {
        super(context, new AzhdahaModel(context.bakeLayer(AzhdahaModel.LAYER_LOCATION)), 2.2F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(GLOW_TEXTURE);
            }
        });
    }

    @Override
    protected void scale(AzhdahaBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.0F, 1.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(AzhdahaBossEntity entity) {
        return TEXTURE;
    }
}
