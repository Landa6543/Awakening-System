package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CompanionRenderer extends MobRenderer<CompanionEntity, CompanionModel> {
    private final CompanionModel defaultModel;
    private final CompanionModel slimModel;

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new CompanionModel(context.bakeLayer(CompanionModel.LAYER_LOCATION)), 0.5F);
        this.defaultModel = this.model;
        this.slimModel = new CompanionModel(context.bakeLayer(CompanionModel.SLIM_LAYER_LOCATION));
        this.addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(CompanionEntity entity, PoseStack poseStack, float partialTickTime) {
        float s = entity.getCompanionProfile() != null ? entity.getCompanionProfile().getModelScale() : 1.0F;
        poseStack.scale(s, s, s);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public void render(CompanionEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = entity.getCompanionProfile().isSlim() ? this.slimModel : this.defaultModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        return entity.getCompanionProfile().getTextureLocation();
    }
}
