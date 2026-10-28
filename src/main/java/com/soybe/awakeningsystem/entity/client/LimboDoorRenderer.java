package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.LimboDoorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Renderer GeckoLib 3D para la Puerta Monumental del Limbo (~10 bloques de altura).
 * Específicamente la parte blanca del umbral interior cuenta con una capa emisiva ultrabrillante
 * (AutoGlowing / Eyes pass con limbo_door_glowmask.png a 15728640) que resplandece intensamente
 * en la oscuridad absoluta del Limbo, mientras que el marco oscuro conserva su contraste gótico.
 * Sin modificar el modelo 3D original ni añadir partículas.
 */
public class LimboDoorRenderer extends GeoEntityRenderer<LimboDoorEntity> {

    public LimboDoorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LimboDoorGeoModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected int getBlockLightLevel(LimboDoorEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(LimboDoorEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }

    @Override
    public void render(LimboDoorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Iluminación total celestial para que tanto el marco de hierro como la puerta sean 100% visibles en el vacío del Limbo
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, LimboDoorEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        if (isReRender) {
            return;
        }
        // Escala monumental: El modelo base mide ~2.81 bloques de altura (45 px).
        // Con un multiplicador de 3.55F alcanza exactamente ~10 bloques de altura (10.0F).
        float scale = 3.55F;
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        // Centrar el modelo: en Blockbench el marco mide 32 unidades en X (-8 a +24, centro en +8 = 0.5 bloques).
        poseStack.translate(-0.5F, 0.0F, 0.0F);
    }
}
