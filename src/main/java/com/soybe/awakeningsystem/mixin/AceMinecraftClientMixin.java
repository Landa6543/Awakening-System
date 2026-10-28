package com.soybe.awakeningsystem.mixin;

import com.soybe.awakeningsystem.combat.AceAttackInteractor;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into Minecraft client to route attack clicks and block-breaking
 * to the Awakening Combat Engine (ACE).
 */
@Mixin(Minecraft.class)
public abstract class AceMinecraftClientMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void ace$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (AceAttackInteractor.getInstance().onDoAttack()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void ace$onContinueAttack(boolean leftClick, CallbackInfo ci) {
        if (AceAttackInteractor.getInstance().onHandleBlockBreaking()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ace$onClientTick(CallbackInfo ci) {
        AceAttackInteractor.getInstance().onClientTick();
    }
}
