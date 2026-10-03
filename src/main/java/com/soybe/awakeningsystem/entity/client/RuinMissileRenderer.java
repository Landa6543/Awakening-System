package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.RuinMissileEntity;
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

public class RuinMissileRenderer extends EntityRenderer<RuinMissileEntity> {
    private final ItemRenderer itemRenderer;

    public RuinMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(RuinMissileEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, 0.0D);

        float time = entity.tickCount + partialTick;
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 30.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 20.0F));
        poseStack.scale(0.85F, 0.85F, 0.85F);

        ItemStack stack = SystemItems.CHAOS_DEVICE.isBound() ? new ItemStack(SystemItems.CHAOS_DEVICE.get()) : new ItemStack(Items.FIRE_CHARGE);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728880,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinMissileEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
