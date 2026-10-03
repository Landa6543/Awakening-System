package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.IsekaiSlimeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class IsekaiSlimeRenderer extends MobRenderer<IsekaiSlimeEntity, IsekaiSlimeModel> {

    private static final ResourceLocation TEX_AZURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_azure.png");
    private static final ResourceLocation TEX_PINK =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_pink.png");
    private static final ResourceLocation TEX_GREEN =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_green.png");
    private static final ResourceLocation TEX_GOLD =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_gold.png");
    private static final ResourceLocation TEX_PURPLE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_purple.png");
    private static final ResourceLocation TEX_RUBY =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_ruby.png");
    private static final ResourceLocation TEX_AMBER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_amber.png");
    private static final ResourceLocation TEX_MINT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_mint.png");
    private static final ResourceLocation TEX_FROST =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_frost.png");
    private static final ResourceLocation TEX_OBSIDIAN =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/isekai_slime/slime_obsidian.png");

    public IsekaiSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new IsekaiSlimeModel(context.bakeLayer(IsekaiSlimeModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(IsekaiSlimeEntity entity) {
        return switch (entity.getVariant()) {
            case AZURE -> TEX_AZURE;
            case SAKURA -> TEX_PINK;
            case EMERALD -> TEX_GREEN;
            case GOLD -> TEX_GOLD;
            case PURPLE -> TEX_PURPLE;
            case RUBY -> TEX_RUBY;
            case AMBER -> TEX_AMBER;
            case MINT -> TEX_MINT;
            case FROST -> TEX_FROST;
            case OBSIDIAN -> TEX_OBSIDIAN;
        };
    }

    @Override
    protected void scale(IsekaiSlimeEntity entity, PoseStack poseStack, float partialTickTime) {
        float baseScale = entity.isBig() ? 2.2F : 0.95F;
        poseStack.scale(baseScale, baseScale, baseScale);
        this.shadowRadius = entity.isBig() ? 1.0F : 0.45F;
    }
}
