package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoldenWolflordBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoldenWolflordRenderer extends MobRenderer<GoldenWolflordBossEntity, GoldenWolflordModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/golden_wolflord.png");

    public GoldenWolflordRenderer(EntityRendererProvider.Context context) {
        super(context, new GoldenWolflordModel(context.bakeLayer(GoldenWolflordModel.LAYER_LOCATION)), 2.5F);
        this.addLayer(new GoldenWolflordGlowLayer(this));
    }

    @Override
    protected void scale(GoldenWolflordBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenWolflordBossEntity entity) {
        return TEXTURE;
    }
}
