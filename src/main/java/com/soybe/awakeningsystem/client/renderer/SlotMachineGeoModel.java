package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.block.entity.SlotMachineBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@SuppressWarnings("removal")
public class SlotMachineGeoModel extends GeoModel<SlotMachineBlockEntity> {
    private static final ResourceLocation MODEL_LEGAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/slot_machine.geo.json");
    private static final ResourceLocation MODEL_ILLEGAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/illegal_slot_machine.geo.json");

    private static final ResourceLocation TEXTURE_LEGAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/slot_machine.png");
    private static final ResourceLocation TEXTURE_LEGAL_B =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/slot_machine_b.png");
    private static final ResourceLocation TEXTURE_ILLEGAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/illegal_slot_machine.png");
    private static final ResourceLocation TEXTURE_ILLEGAL_B =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/illegal_slot_machine_b.png");

    @Override
    public ResourceLocation getModelResource(SlotMachineBlockEntity animatable, software.bernie.geckolib.renderer.GeoRenderer<SlotMachineBlockEntity> renderer) {
        boolean isIllegal = animatable.getBlockState().getBlock() instanceof SlotMachineBlock smb && smb.isIllegal();
        return isIllegal ? MODEL_ILLEGAL : MODEL_LEGAL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(SlotMachineBlockEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Override
    public ResourceLocation getTextureResource(SlotMachineBlockEntity animatable, software.bernie.geckolib.renderer.GeoRenderer<SlotMachineBlockEntity> renderer) {
        boolean isIllegal = animatable.getBlockState().getBlock() instanceof SlotMachineBlock smb && smb.isIllegal();
        boolean frameB = animatable.getLevel() != null && ((animatable.getLevel().getGameTime() / 8L) % 2L == 1L);
        if (isIllegal) {
            return frameB ? TEXTURE_ILLEGAL_B : TEXTURE_ILLEGAL;
        } else {
            return frameB ? TEXTURE_LEGAL_B : TEXTURE_LEGAL;
        }
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(SlotMachineBlockEntity animatable) {
        return getTextureResource(animatable, null);
    }

    @Override
    public ResourceLocation getAnimationResource(SlotMachineBlockEntity animatable) {
        return null;
    }

    @Override
    public void setCustomAnimations(SlotMachineBlockEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<SlotMachineBlockEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        float partialTick = animationState.getPartialTick();

        getBone("bone4").ifPresent(lever -> {
            float angle = animatable.getInterpolatedLeverAngle(partialTick);
            lever.setRotZ(angle * net.minecraft.util.Mth.DEG_TO_RAD);
        });

        getBone("bone").ifPresent(reel1 -> {
            float angle = animatable.getInterpolatedReelAngle(0, partialTick);
            reel1.setRotZ(angle * net.minecraft.util.Mth.DEG_TO_RAD);
        });

        getBone("bone2").ifPresent(reel2 -> {
            float angle = animatable.getInterpolatedReelAngle(1, partialTick);
            reel2.setRotZ(angle * net.minecraft.util.Mth.DEG_TO_RAD);
        });

        getBone("bone3").ifPresent(reel3 -> {
            float angle = animatable.getInterpolatedReelAngle(2, partialTick);
            reel3.setRotZ(angle * net.minecraft.util.Mth.DEG_TO_RAD);
        });
    }
}
