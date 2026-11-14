package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinWarriorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoblinWarriorRenderer extends MobRenderer<GoblinWarriorEntity, GoblinWarriorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_warrior.png");
    private static final net.minecraft.client.renderer.RenderType GLOW = net.minecraft.client.renderer.RenderType.eyes(ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_warrior_glow.png"));

    public GoblinWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinWarriorModel(context.bakeLayer(GoblinWarriorModel.LAYER_LOCATION)), 0.42F);
        // the eyes (and the shaman's orb) glow in the dark
        this.addLayer(new net.minecraft.client.renderer.entity.layers.EyesLayer<>(this) {
            @Override
            public net.minecraft.client.renderer.RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinWarriorEntity entity) {
        return TEXTURE;
    }
}
