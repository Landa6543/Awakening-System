package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinSerpentBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinSerpentRenderer extends MobRenderer<RuinSerpentBossEntity, RuinSerpentModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_serpent"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_serpent_remodel_fixed.png");

    public RuinSerpentRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinSerpentModel(context.bakeLayer(LAYER_LOCATION)), 1.6F);
    }

    @Override
    protected void scale(RuinSerpentBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinSerpentBossEntity entity) {
        return TEXTURE;
    }
}
