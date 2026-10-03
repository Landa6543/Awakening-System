package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MitachurlRenderer extends MobRenderer<MitachurlEntity, MitachurlModel> {

    private static final ResourceLocation TEX_AXE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mitachurl/mitachurl_blazing_axe.png");
    private static final ResourceLocation TEX_SHIELD =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mitachurl/mitachurl_shield.png");

    public MitachurlRenderer(EntityRendererProvider.Context context) {
        super(context, new MitachurlModel(context.bakeLayer(MitachurlModel.LAYER_LOCATION)), 0.75F);
    }

    @Override
    public ResourceLocation getTextureLocation(MitachurlEntity entity) {
        return (entity.getMitachurlType() == MitachurlEntity.MitachurlType.BLAZING_AXE) ? TEX_AXE : TEX_SHIELD;
    }

    @Override
    protected void scale(MitachurlEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
        this.shadowRadius = 0.75F;
    }
}
