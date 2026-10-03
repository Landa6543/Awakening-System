package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LunarGuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LunarGuardianRenderer extends MobRenderer<LunarGuardianEntity, LunarGuardianModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/lunar_guardian.png");

    public LunarGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new LunarGuardianModel(context.bakeLayer(LunarGuardianModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    protected void scale(LunarGuardianEntity entity, PoseStack poseStack, float partialTickTime) {
        // Escala imponente de jefe (1.45x)
        poseStack.scale(1.45F, 1.45F, 1.45F);
    }

    @Override
    protected RenderType getRenderType(LunarGuardianEntity entity, boolean bodyVisible, boolean translucent, boolean appearsGlowing) {
        ResourceLocation res = this.getTextureLocation(entity);
        if (bodyVisible) {
            return RenderType.entityCutoutNoCull(res);
        }
        return super.getRenderType(entity, bodyVisible, false, appearsGlowing);
    }

    @Override
    public ResourceLocation getTextureLocation(LunarGuardianEntity entity) {
        return TEXTURE;
    }
}
