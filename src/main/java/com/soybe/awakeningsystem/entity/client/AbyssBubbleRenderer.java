package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.entity.AbyssBubbleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AbyssBubbleRenderer extends EntityRenderer<AbyssBubbleEntity> {
    private final ItemRenderer itemRenderer;

    public AbyssBubbleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(AbyssBubbleEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, 0.0D);

        float time = entity.tickCount + partialTick;
        float pulse = 0.85F + Mth.sin(time * 0.2F) * 0.1F;
        poseStack.scale(pulse, pulse, pulse);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 15.0F));

        ItemStack stack = new ItemStack(Items.HEART_OF_THE_SEA);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728880,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssBubbleEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
