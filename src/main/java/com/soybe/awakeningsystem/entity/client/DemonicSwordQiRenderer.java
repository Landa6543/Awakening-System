package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.entity.DemonicSwordQiEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DemonicSwordQiRenderer extends EntityRenderer<DemonicSwordQiEntity> {
    private final ItemRenderer itemRenderer;

    public DemonicSwordQiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(DemonicSwordQiEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.15D, 0.0D);

        float time = entity.tickCount + partialTick;
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 35.0F));
        poseStack.scale(1.1F, 1.1F, 1.1F);

        ItemStack stack = (com.soybe.awakeningsystem.SystemItems.SKY_DEMON_SWORD != null && com.soybe.awakeningsystem.SystemItems.SKY_DEMON_SWORD.isBound())
                ? new ItemStack(com.soybe.awakeningsystem.SystemItems.SKY_DEMON_SWORD.get())
                : new ItemStack(Items.NETHERITE_SWORD);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728880,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DemonicSwordQiEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
