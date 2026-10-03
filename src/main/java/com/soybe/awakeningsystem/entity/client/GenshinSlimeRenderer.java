package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GenshinSlimeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GenshinSlimeRenderer extends MobRenderer<GenshinSlimeEntity, HierarchicalModel<GenshinSlimeEntity>> {

    private static final ResourceLocation TEX_PYRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_pyro.png");
    private static final ResourceLocation TEX_HYDRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_hydro.png");
    private static final ResourceLocation TEX_ANEMO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_anemo.png");
    private static final ResourceLocation TEX_ELECTRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_electro.png");
    private static final ResourceLocation TEX_CRYO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_cryo.png");
    private static final ResourceLocation TEX_GEO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_geo.png");
    private static final ResourceLocation TEX_DENDRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/slime_dendro.png");

    private static final ResourceLocation TEX_LARGE_PYRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_pyro.png");
    private static final ResourceLocation TEX_LARGE_HYDRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_hydro.png");
    private static final ResourceLocation TEX_LARGE_ANEMO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_anemo.png");
    private static final ResourceLocation TEX_LARGE_ELECTRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_electro.png");
    private static final ResourceLocation TEX_LARGE_CRYO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_cryo.png");
    private static final ResourceLocation TEX_LARGE_GEO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_geo.png");
    private static final ResourceLocation TEX_LARGE_DENDRO =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/genshin_slime/large_slime_dendro.png");

    private final GenshinSlimeModel smallModel;
    private final GenshinLargeSlimeModel largeModel;

    public GenshinSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new GenshinSlimeModel(context.bakeLayer(GenshinSlimeModel.LAYER_LOCATION)), 0.40F);
        this.smallModel = (GenshinSlimeModel) this.model;
        this.largeModel = new GenshinLargeSlimeModel(context.bakeLayer(GenshinLargeSlimeModel.LAYER_LOCATION));
    }

    @Override
    public void render(GenshinSlimeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = entity.isBig() ? this.largeModel : this.smallModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GenshinSlimeEntity entity) {
        boolean big = entity.isBig();
        return switch (entity.getElement()) {
            case PYRO -> big ? TEX_LARGE_PYRO : TEX_PYRO;
            case HYDRO -> big ? TEX_LARGE_HYDRO : TEX_HYDRO;
            case ANEMO -> big ? TEX_LARGE_ANEMO : TEX_ANEMO;
            case ELECTRO -> big ? TEX_LARGE_ELECTRO : TEX_ELECTRO;
            case CRYO -> big ? TEX_LARGE_CRYO : TEX_CRYO;
            case GEO -> big ? TEX_LARGE_GEO : TEX_GEO;
            case DENDRO -> big ? TEX_LARGE_DENDRO : TEX_DENDRO;
        };
    }

    @Override
    protected void scale(GenshinSlimeEntity entity, PoseStack poseStack, float partialTickTime) {
        float baseScale = entity.isBig() ? 1.05F : 0.85F;
        poseStack.scale(baseScale, baseScale, baseScale);
        this.shadowRadius = entity.isBig() ? 0.80F : 0.40F;
    }
}
