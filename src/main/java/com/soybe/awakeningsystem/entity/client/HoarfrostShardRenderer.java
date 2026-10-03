package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.HoarfrostShardEntity;
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

public class HoarfrostShardRenderer extends EntityRenderer<HoarfrostShardEntity> {
    private final ItemRenderer itemRenderer;

    public HoarfrostShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(HoarfrostShardEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.15D, 0.0D);

        float time = entity.tickCount + partialTick;
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 35.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 25.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);

        ItemStack stack = SystemItems.HOARFROST_CORE.isBound() ? new ItemStack(SystemItems.HOARFROST_CORE.get()) : new ItemStack(Items.SNOWBALL);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728880,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HoarfrostShardEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
