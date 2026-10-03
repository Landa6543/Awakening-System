package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.CelestialDragonBreathEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renderizador para el proyectil de Aliento de Qi Celestial.
 * Rota en 3D un orbe de plasma dorado y relámpago con brillo máximo constante.
 */
public class CelestialDragonBreathRenderer extends EntityRenderer<CelestialDragonBreathEntity> {
    private final ItemRenderer itemRenderer;

    public CelestialDragonBreathRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(CelestialDragonBreathEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, 0.0D);

        float time = entity.tickCount + partialTick;
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 25.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 18.0F));
        poseStack.scale(1.2F, 1.2F, 1.2F);

        // Renderizar perla/orbe místico con brillo emisivo completo (15728880)
        ItemStack stack = new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get());
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728880,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CelestialDragonBreathEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
