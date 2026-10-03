package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaChickenEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ManaChickenRenderer extends MobRenderer<ManaChickenEntity, ChickenModel<ManaChickenEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/animal/mana_chicken.png");

    public ManaChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaChickenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getBob(ManaChickenEntity livingBase, float partialTicks) {
        float f = Mth.lerp(partialTicks, livingBase.oFlap, livingBase.flap);
        float f1 = Mth.lerp(partialTicks, livingBase.oFlapSpeed, livingBase.flapSpeed);
        return (Mth.sin(f) + 1.0F) * f1;
    }
}
