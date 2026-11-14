package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinScoutEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoblinScoutRenderer extends MobRenderer<GoblinScoutEntity, GoblinScoutModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_scout.png");
    private static final net.minecraft.client.renderer.RenderType GLOW = net.minecraft.client.renderer.RenderType.eyes(ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_scout_glow.png"));

    public GoblinScoutRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinScoutModel(context.bakeLayer(GoblinScoutModel.LAYER_LOCATION)), 0.35F);
        // the eyes (and the shaman's orb) glow in the dark
        this.addLayer(new net.minecraft.client.renderer.entity.layers.EyesLayer<>(this) {
            @Override
            public net.minecraft.client.renderer.RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinScoutEntity entity) {
        return TEXTURE;
    }
}
