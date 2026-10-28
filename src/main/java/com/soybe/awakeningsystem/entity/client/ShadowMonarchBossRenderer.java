package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadowMonarchBossRenderer extends MobRenderer<ShadowMonarchBossEntity, HierarchicalModel<ShadowMonarchBossEntity>> {
    private static final ResourceLocation ASTRA_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/shadow_monarch_boss.png");
    private static final ResourceLocation CLASSIC_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/classic_monarch_boss.png");

    private final ShadowMonarchBossModel astraModel;
    private final ClassicMonarchBossModel classicModel;

    public ShadowMonarchBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowMonarchBossModel(context.bakeLayer(ShadowMonarchBossModel.LAYER_LOCATION)), 0.85F);
        this.astraModel = (ShadowMonarchBossModel) this.model;
        this.classicModel = new ClassicMonarchBossModel(context.bakeLayer(ClassicMonarchBossModel.LAYER_LOCATION));
    }

    @Override
    public void render(ShadowMonarchBossEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.getVariant() == 1) {
            this.model = this.classicModel;
        } else {
            this.model = this.astraModel;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(ShadowMonarchBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowMonarchBossEntity entity) {
        return entity.getVariant() == 1 ? CLASSIC_TEXTURE : ASTRA_TEXTURE;
    }
}
