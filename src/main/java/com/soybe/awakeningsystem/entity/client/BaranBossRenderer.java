package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BaranBossRenderer extends HumanoidMobRenderer<BaranBossEntity, PlayerModel<BaranBossEntity>> {
    private static final ResourceLocation BARAN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/baran.png");

    public BaranBossRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.75F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(BaranBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(BaranBossEntity entity) {
        return BARAN_TEXTURE;
    }
}
