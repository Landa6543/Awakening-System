package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador del Dragón Ancestral Occidental.
 * Soporta las 4 variantes elementales (Fuego, Hielo, Bosque, Sombra)
 * con capa de brillo emisivo independiente para ojos, cuernos y crestas.
 */
public class AncientDragonRenderer extends MobRenderer<AncientDragonEntity, AncientDragonModel> {
    private static final ResourceLocation TEXTURE_FIRE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_fire.png");
    private static final ResourceLocation TEXTURE_ICE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_ice.png");
    private static final ResourceLocation TEXTURE_FOREST =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_forest.png");
    private static final ResourceLocation TEXTURE_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_shadow.png");
    private static final ResourceLocation TEXTURE_LIGHTNING =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_lightning.png");
    private static final ResourceLocation TEXTURE_CELESTIAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_celestial.png");

    public AncientDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new AncientDragonModel(context.bakeLayer(AncientDragonModel.LAYER_LOCATION)), 4.2F);
        this.addLayer(new AncientDragonGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(AncientDragonEntity entity) {
        return switch (entity.getVariant()) {
            case AncientDragonEntity.VARIANT_ICE -> TEXTURE_ICE;
            case AncientDragonEntity.VARIANT_FOREST -> TEXTURE_FOREST;
            case AncientDragonEntity.VARIANT_SHADOW -> TEXTURE_SHADOW;
            case AncientDragonEntity.VARIANT_LIGHTNING -> TEXTURE_LIGHTNING;
            case AncientDragonEntity.VARIANT_CELESTIAL -> TEXTURE_CELESTIAL;
            default -> TEXTURE_FIRE;
        };
    }

    @Override
    protected void scale(AncientDragonEntity entity, PoseStack poseStack, float partialTickTime) {
        float dragonScale = entity.getDragonScale();
        poseStack.scale(dragonScale, dragonScale, dragonScale);
        poseStack.translate(0.0D, -0.8125D, 0.0D);
        this.shadowRadius = 1.05F * dragonScale;
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    protected net.minecraft.client.renderer.RenderType getRenderType(AncientDragonEntity entity, boolean bodyVisible, boolean translucent, boolean appearsGlowing) {
        return net.minecraft.client.renderer.RenderType.entityCutoutNoCull(this.getTextureLocation(entity));
    }
}
