package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinSentinelEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinSentinelRenderer extends MobRenderer<RuinSentinelEntity, RuinSentinelModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_sentinel"), "main");

    private static final ResourceLocation TEX_CRUISER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_cruiser.png");
    private static final ResourceLocation TEX_DEFENDER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_defender.png");
    private static final ResourceLocation TEX_DESTROYER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_destroyer.png");
    private static final ResourceLocation TEX_SCOUT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_scout.png");

    public RuinSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinSentinelModel(context.bakeLayer(LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinSentinelEntity entity) {
        return switch (entity.getSentinelType()) {
            case CRUISER -> TEX_CRUISER;
            case DEFENDER -> TEX_DEFENDER;
            case DESTROYER -> TEX_DESTROYER;
            case SCOUT -> TEX_SCOUT;
        };
    }
}
