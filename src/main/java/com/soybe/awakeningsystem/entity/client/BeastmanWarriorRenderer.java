package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class BeastmanWarriorRenderer extends MobRenderer<BeastmanWarriorEntity, BeastmanWarriorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/beastman/beastman_warrior.png");
    private static final RenderType EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/beastman/beastman_warrior_eyes.png"));

    public BeastmanWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastmanWarriorModel(context.bakeLayer(BeastmanWarriorModel.LAYER_LOCATION)), 0.75F);
        // the amber eyes glow in the dark
        this.addLayer(new ShadowSkins.Eyes<>(this, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/beastman/beastman_warrior_eyes.png")));

    }

    @Override
    protected void scale(BeastmanWarriorEntity entity, PoseStack poseStack, float partialTick) {
        // the model is drawn at full size (about 3.6 blocks); in the world it stands about 2.45 tall
        poseStack.scale(0.68F, 0.68F, 0.68F);
    }

    @Override
    public ResourceLocation getTextureLocation(BeastmanWarriorEntity entity) {
        return ShadowSkins.pick(entity, TEXTURE);
    }
}
