package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ChildeBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class ChildeBossRenderer extends MobRenderer<ChildeBossEntity, CompositeChildeModel> {
    private static final ResourceLocation TEXTURE_PHASE1 =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/childe_phase1.png");
    private static final ResourceLocation TEXTURE_PHASE2 =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/childe_phase2.png");

    public ChildeBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CompositeChildeModel(
                new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true),
                new ChildePhase2Model(context.bakeLayer(ChildePhase2Model.LAYER_LOCATION))
        ), 0.6F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(ChildeBossEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.getPhase() == 2) {
            poseStack.scale(1.22F, 1.22F, 1.22F);
        } else {
            poseStack.scale(1.18F, 1.18F, 1.18F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ChildeBossEntity entity) {
        return entity.getPhase() == 2 ? TEXTURE_PHASE2 : TEXTURE_PHASE1;
    }
}
