package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbstractAbyssMageEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador de los Magos del Abismo (Pyro, Hydro, Cryo, Electro).
 * Conmuta automáticamente modelos y texturas según el estado de escudo elemental y aturdimiento.
 */
public class AbyssMageRenderer<T extends AbstractAbyssMageEntity> extends MobRenderer<T, AbyssMageModel<T>> {

    private final AbyssMageModel<T> unshieldedModel;
    private final AbyssMageShieldedModel<T> shieldedModel;

    public AbyssMageRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssMageModel<>(context.bakeLayer(AbyssMageModel.LAYER_LOCATION)), 0.5F);
        this.unshieldedModel = this.model;
        this.shieldedModel = new AbyssMageShieldedModel<>(context.bakeLayer(AbyssMageShieldedModel.LAYER_LOCATION));
        this.addLayer(new AbyssMageGlowLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isShielded()) {
            this.model = this.shieldedModel;
        } else {
            this.model = this.unshieldedModel;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        String elem = entity.getMageElement().name().toLowerCase();
        if (entity.isShielded()) {
            return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                    "textures/entity/abyssmage_shielded_" + elem + ".png");
        } else {
            return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                    "textures/entity/abyssmage_" + elem + ".png");
        }
    }
}
