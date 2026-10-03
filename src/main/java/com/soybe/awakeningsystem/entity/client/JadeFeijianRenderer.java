package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.JadeFeijianEntity;
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

public class JadeFeijianRenderer extends EntityRenderer<JadeFeijianEntity> {
    private final ItemRenderer itemRenderer;

    public JadeFeijianRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(JadeFeijianEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, 0.0D);

        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot());
        float time = entity.tickCount + partialTick;

        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 30.0F));
        poseStack.scale(1.2F, 1.2F, 1.2F);

        ItemStack stack = (SystemItems.JADE_FEIJIAN != null && SystemItems.JADE_FEIJIAN.isBound())
                ? new ItemStack(SystemItems.JADE_FEIJIAN.get())
                : ItemStack.EMPTY;

        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728880,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(JadeFeijianEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
