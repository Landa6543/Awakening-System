package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the Mitachurls as Genshin Nature Revived does (fapdos & team, with permission): the blazing axe one
 * and the wooden shieldwall one each with its own model and texture, the axe's embers glowing in the dark.
 */
public class MitachurlRenderer extends MobRenderer<MitachurlEntity, MitachurlModel> {

    private static final ResourceLocation TEX_AXE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mitachurl/mitachurl_blazing_axe.png");
    private static final ResourceLocation TEX_AXE_GLOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mitachurl/mitachurl_blazing_axe_emissive.png");
    private static final ResourceLocation TEX_SHIELD =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mitachurl/mitachurl_shield.png");

    private final MitachurlModel axeModel, shieldModel;

    public MitachurlRenderer(EntityRendererProvider.Context context) {
        super(context, new MitachurlModel(context.bakeLayer(MitachurlModel.LAYER_LOCATION), false), 0.9F);
        this.axeModel = this.model;
        this.shieldModel = new MitachurlModel(context.bakeLayer(MitachurlModel.SHIELD_LAYER), true);
        this.addLayer(new Glow(this));
    }

    private static boolean isAxe(MitachurlEntity e) {
        return e.getMitachurlType() == MitachurlEntity.MitachurlType.BLAZING_AXE;
    }

    @Override
    public void render(MitachurlEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        // each kind has its own model
        this.model = isAxe(entity) ? this.axeModel : this.shieldModel;
        super.render(entity, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(MitachurlEntity entity) {
        return isAxe(entity) ? TEX_AXE : TEX_SHIELD;
    }

    /** The blazing axe's embers, lit whatever the light. */
    private static final class Glow extends RenderLayer<MitachurlEntity, MitachurlModel> {
        Glow(RenderLayerParent<MitachurlEntity, MitachurlModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, MitachurlEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!isAxe(entity)) return;
            this.getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(TEX_AXE_GLOW)), 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
    }
}
