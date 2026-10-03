package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.entity.AncientDragonEggBlockEntity;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AncientDragonEggRenderer implements BlockEntityRenderer<AncientDragonEggBlockEntity> {
    public static final ResourceLocation TEXTURE_FIRE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_egg_fire.png");
    public static final ResourceLocation TEXTURE_ICE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_egg_ice.png");
    public static final ResourceLocation TEXTURE_FOREST =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_egg_forest.png");
    public static final ResourceLocation TEXTURE_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_egg_shadow.png");
    public static final ResourceLocation TEXTURE_LIGHTNING =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_egg_lightning.png");
    public static final ResourceLocation TEXTURE_CELESTIAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_egg_celestial.png");

    private final AncientDragonEggModel model;

    public AncientDragonEggRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new AncientDragonEggModel(context.bakeLayer(AncientDragonEggModel.LAYER_LOCATION));
    }

    public static ResourceLocation getTextureForVariant(int variant) {
        return switch (variant) {
            case AncientDragonEntity.VARIANT_ICE -> TEXTURE_ICE;
            case AncientDragonEntity.VARIANT_FOREST -> TEXTURE_FOREST;
            case AncientDragonEntity.VARIANT_SHADOW -> TEXTURE_SHADOW;
            case AncientDragonEntity.VARIANT_LIGHTNING -> TEXTURE_LIGHTNING;
            case AncientDragonEntity.VARIANT_CELESTIAL -> TEXTURE_CELESTIAL;
            default -> TEXTURE_FIRE;
        };
    }

    @Override
    public void render(AncientDragonEggBlockEntity egg, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Centrar en el bloque (X=0.5, Z=0.5, apoyado en el suelo Y=0.0)
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // Oscilación sutil de vida al incubar en condiciones cómodas
        if (egg.isComfortable() && egg.getLevel() != null) {
            float gameTime = egg.getLevel().getGameTime() + partialTick;
            float wobble = Mth.sin(gameTime * 0.12F) * 1.5F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(wobble));
        }

        ResourceLocation tex = getTextureForVariant(egg.getVariant());
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
        this.model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);

        poseStack.popPose();
    }
}
