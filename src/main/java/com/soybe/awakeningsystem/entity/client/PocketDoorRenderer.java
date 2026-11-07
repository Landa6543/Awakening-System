package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.PocketDoorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador 3D de la Puerta Física de Bolsillo (Roadmap Sección 29).
 * Renderiza el marco de madera y la hoja batiente física articulada sobre bisagras con picaporte 3D.
 * Al abrirse la puerta física, el cazador cruza directamente el vano para acceder a su dimensión.
 */
public class PocketDoorRenderer extends EntityRenderer<PocketDoorEntity> {

    private static final ResourceLocation DOOR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/pocket_door.png");

    private final PocketDoorModel model;

    public PocketDoorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new PocketDoorModel(context.bakeLayer(PocketDoorModel.LAYER_LOCATION));
    }

    @Override
    public void render(PocketDoorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Alinear con la orientación del jugador/entidad
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
        // Invertir Y para coordenadas estándar del modelo (Y hacia abajo)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.5D, 0.0D);

        float angle = entity.getDoorAngle(partialTicks);
        this.model.setDoorAngle(angle);

        // Renderizado del marco físico de madera y la hoja batiente de la puerta
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PocketDoorEntity entity) {
        return DOOR_TEXTURE;
    }
}
