package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JadeGuardianBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class JadeGuardianRenderer extends MobRenderer<JadeGuardianBossEntity, JadeGuardianModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/boss/jade_guardian.png");

    public JadeGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new JadeGuardianModel(context.bakeLayer(JadeGuardianModel.LAYER_LOCATION)), 1.1F);
        this.addLayer(new JadeGuardianGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(JadeGuardianBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(JadeGuardianBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
