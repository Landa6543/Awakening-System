package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GlareEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GlareRenderer extends MobRenderer<GlareEntity, GlareModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/glare.png");

    public GlareRenderer(EntityRendererProvider.Context context) {
        super(context, new GlareModel(context.bakeLayer(GlareModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new GlareEyesLayer(this));
    }

    @Override
    protected RenderType getRenderType(GlareEntity entity, boolean bodyVisible, boolean translucent, boolean appearsGlowing) {
        ResourceLocation res = this.getTextureLocation(entity);
        if (bodyVisible) {
            return RenderType.entityCutoutNoCull(res);
        }
        return super.getRenderType(entity, bodyVisible, false, appearsGlowing);
    }

    @Override
    public ResourceLocation getTextureLocation(GlareEntity entity) {
        return TEXTURE;
    }
}
