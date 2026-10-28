package com.soybe.awakeningsystem.item.client;

import com.soybe.awakeningsystem.item.ManhwaArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class AstraliteArmorRenderer extends GeoArmorRenderer<ManhwaArmorItem> {
    public AstraliteArmorRenderer() {
        super(new AstraliteArmorGeoModel());
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);

        // Eliminate vanilla humanoid idle breathing sway on the arms and shoulder pads
        // (Vanilla adds: rightArm.zRot += cos(age * 0.09) * 0.05 + 0.05; rightArm.xRot += sin(age * 0.067) * 0.05)
        float ageInTicks = this.currentEntity instanceof LivingEntity living
                ? (living.tickCount + this.partialTick)
                : 0.0F;
        float idleZ = (float) (Math.cos(ageInTicks * 0.09F) * 0.05D + 0.05D);
        float idleX = (float) (Math.sin(ageInTicks * 0.067F) * 0.05D);

        if (this.rightArm != null) {
            this.rightArm.updateRotation(
                    this.rightArm.getRotX() + idleX,
                    this.rightArm.getRotY(),
                    this.rightArm.getRotZ() - idleZ
            );
        }
        if (this.leftArm != null) {
            this.leftArm.updateRotation(
                    this.leftArm.getRotX() - idleX,
                    this.leftArm.getRotY(),
                    this.leftArm.getRotZ() + idleZ
            );
        }

        // Match hips/tassets to body rotations and position
        getGeoModel().getBone("armorHips").ifPresent(b -> {
            RenderUtil.matchModelPartRot(baseModel.body, b);
            b.updatePosition(baseModel.body.x, -baseModel.body.y, baseModel.body.z);
        });
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        getGeoModel().getBone("armorHips").ifPresent(hips -> {
            setBoneVisible(hips, currentSlot == EquipmentSlot.LEGS);
        });
    }
}
