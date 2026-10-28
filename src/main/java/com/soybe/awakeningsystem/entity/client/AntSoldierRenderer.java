package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AntSoldierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AntSoldierRenderer extends MobRenderer<AntSoldierEntity, AntSoldierModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/ant_soldier/ant_soldier.png");

    public AntSoldierRenderer(EntityRendererProvider.Context context) {
        super(context, new AntSoldierModel(context.bakeLayer(AntSoldierModel.LAYER_LOCATION)), 0.65F);
        this.addLayer(new AntSoldierGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(AntSoldierEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AntSoldierEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
    }
}
