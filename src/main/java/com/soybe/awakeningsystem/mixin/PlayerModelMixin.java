package com.soybe.awakeningsystem.mixin;

import com.soybe.awakeningsystem.entity.FlyingSwordEntity;
import com.soybe.awakeningsystem.entity.MeditationSeatEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void awakening_system$setupCustomPoses(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.getVehicle() instanceof FlyingSwordEntity) {
            this.riding = false;
            this.leftLeg.xRot = 0.0F;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = -0.05F;
            this.rightLeg.xRot = 0.0F;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.05F;

            this.leftArm.xRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = -0.08F;
            this.rightArm.xRot = 0.0F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.08F;
        } else if (entity.getVehicle() instanceof MeditationSeatEntity) {
            this.riding = true;
            this.leftLeg.xRot = -1.45F;
            this.leftLeg.yRot = 0.52F;
            this.leftLeg.zRot = 0.35F;
            this.rightLeg.xRot = -1.45F;
            this.rightLeg.yRot = -0.52F;
            this.rightLeg.zRot = -0.35F;
            this.leftArm.xRot = -0.70F;
            this.leftArm.yRot = 0.35F;
            this.leftArm.zRot = -0.25F;
            this.rightArm.xRot = -0.70F;
            this.rightArm.yRot = -0.35F;
            this.rightArm.zRot = 0.25F;
        } else if (entity.getVehicle() instanceof com.soybe.awakeningsystem.entity.CelestialDragonEntity || entity.getVehicle() instanceof com.soybe.awakeningsystem.entity.AncientDragonEntity) {
            this.riding = true;
            if (entity.getMainHandItem().isEmpty() && entity.getOffhandItem().isEmpty()) {
                this.leftArm.xRot = -0.62F;
                this.leftArm.yRot = 0.18F;
                this.leftArm.zRot = -0.10F;
                this.rightArm.xRot = -0.62F;
                this.rightArm.yRot = -0.18F;
                this.rightArm.zRot = 0.10F;
            }
        }
    }
}
