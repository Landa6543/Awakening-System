package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinShamanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoblinShamanRenderer extends MobRenderer<GoblinShamanEntity, GoblinShamanModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_shaman.png");
    private static final net.minecraft.client.renderer.RenderType GLOW = net.minecraft.client.renderer.RenderType.eyes(ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_shaman_glow.png"));

    public GoblinShamanRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinShamanModel(context.bakeLayer(GoblinShamanModel.LAYER_LOCATION)), 0.38F);
        // the eyes (and the shaman's orb) glow in the dark
        this.addLayer(new ShadowSkins.Eyes<>(this, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_shaman_glow.png")));

    }

    @Override
    public ResourceLocation getTextureLocation(GoblinShamanEntity entity) {
        return ShadowSkins.pick(entity, TEXTURE);
    }
}
