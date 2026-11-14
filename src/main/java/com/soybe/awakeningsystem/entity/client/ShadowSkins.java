package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shadow soldiers wear a shadow of their old skin: for any texture "x.png" a "x_shadow.png" next to it (black
 * and violet, the same details) is used while the creature is one of the Monarch's shadows. If a creature has
 * no shadow skin yet, it keeps its own.
 */
public final class ShadowSkins {
    private ShadowSkins() {}

    private static final Map<ResourceLocation, ResourceLocation> SHADOW = new ConcurrentHashMap<>();

    public static boolean isShadow(LivingEntity e) {
        return e != null && AwakeningSystemClient.isShadowSoldier(e);
    }

    /** The shadow version of {@code normal} for a shadow soldier, otherwise {@code normal}. */
    public static ResourceLocation pick(LivingEntity e, ResourceLocation normal) {
        if (!isShadow(e)) return normal;
        return SHADOW.computeIfAbsent(normal, n -> {
            ResourceLocation s = ResourceLocation.fromNamespaceAndPath(n.getNamespace(), n.getPath().replace(".png", "_shadow.png"));
            return Minecraft.getInstance().getResourceManager().getResource(s).isPresent() ? s : n;
        });
    }

    /** Glowing eyes (or any glow texture) that switch to their shadow version, violet, on a shadow soldier. */
    public static final class Eyes<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        private final ResourceLocation glow;

        public Eyes(RenderLayerParent<T, M> parent, ResourceLocation glow) {
            super(parent);
            this.glow = glow;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            RenderType type = RenderType.eyes(pick(entity, glow));
            getParentModel().renderToBuffer(poseStack, buffer.getBuffer(type), 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }
}
