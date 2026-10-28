package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinSentinelEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinSentinelRenderer extends MobRenderer<RuinSentinelEntity, EntityModel<RuinSentinelEntity>> {

    public static final ModelLayerLocation CRUISER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_cruiser"), "main");
    public static final ModelLayerLocation DEFENDER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_defender"), "main");
    public static final ModelLayerLocation DESTROYER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_destroyer"), "main");
    public static final ModelLayerLocation SCOUT_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_scout"), "main");

    private static final ResourceLocation TEX_CRUISER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_cruiser.png");
    private static final ResourceLocation TEX_DEFENDER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_defender.png");
    private static final ResourceLocation TEX_DESTROYER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_destroyer.png");
    private static final ResourceLocation TEX_SCOUT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_scout.png");

    private final RuinCruiserModel<RuinSentinelEntity> cruiserModel;
    private final RuinDefenderModel<RuinSentinelEntity> defenderModel;
    private final RuinDestroyerModel<RuinSentinelEntity> destroyerModel;
    private final RuinScoutModel<RuinSentinelEntity> scoutModel;

    public RuinSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinCruiserModel<>(context.bakeLayer(CRUISER_LAYER)), 0.8F);
        this.cruiserModel = new RuinCruiserModel<>(context.bakeLayer(CRUISER_LAYER));
        this.defenderModel = new RuinDefenderModel<>(context.bakeLayer(DEFENDER_LAYER));
        this.destroyerModel = new RuinDestroyerModel<>(context.bakeLayer(DESTROYER_LAYER));
        this.scoutModel = new RuinScoutModel<>(context.bakeLayer(SCOUT_LAYER));
    }

    @Override
    public void render(RuinSentinelEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = switch (entity.getSentinelType()) {
            case CRUISER -> this.cruiserModel;
            case DEFENDER -> this.defenderModel;
            case DESTROYER -> this.destroyerModel;
            case SCOUT -> this.scoutModel;
        };
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
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
