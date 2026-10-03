package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.HilichurlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HilichurlRenderer<T extends HilichurlEntity> extends MobRenderer<T, HilichurlModel<T>> {

    private static final ResourceLocation TEX_FIGHTER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hilichurl/hilichurl_fighter.png");
    private static final ResourceLocation TEX_BERSERKER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hilichurl/hilichurl_berserker.png");
    private static final ResourceLocation TEX_GUARD =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hilichurl/hilichurl_guard.png");
    private static final ResourceLocation TEX_SHOOTER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hilichurl/hilichurl_shooter.png");

    public HilichurlRenderer(EntityRendererProvider.Context context) {
        super(context, new HilichurlModel<>(context.bakeLayer(HilichurlModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return switch (entity.getRole()) {
            case FIGHTER -> TEX_FIGHTER;
            case BERSERKER -> TEX_BERSERKER;
            case GUARD -> TEX_GUARD;
            case SHOOTER -> TEX_SHOOTER;
        };
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.92F, 0.92F, 0.92F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
