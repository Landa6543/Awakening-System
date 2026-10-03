package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LunarShadowEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LunarShadowRenderer extends MobRenderer<LunarShadowEntity, LunarShadowModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/lunar_shadow.png");

    public LunarShadowRenderer(EntityRendererProvider.Context context) {
        super(context, new LunarShadowModel(context.bakeLayer(LunarShadowModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    protected RenderType getRenderType(LunarShadowEntity entity, boolean bodyVisible, boolean translucent, boolean appearsGlowing) {
        ResourceLocation res = this.getTextureLocation(entity);
        if (bodyVisible) {
            return RenderType.entityCutoutNoCull(res);
        }
        return super.getRenderType(entity, bodyVisible, false, appearsGlowing);
    }

    @Override
    public ResourceLocation getTextureLocation(LunarShadowEntity entity) {
        return TEXTURE;
    }
}
