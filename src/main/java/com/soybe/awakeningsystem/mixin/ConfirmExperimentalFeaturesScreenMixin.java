package com.soybe.awakeningsystem.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmExperimentalFeaturesScreen.class)
public abstract class ConfirmExperimentalFeaturesScreenMixin {

    @Shadow
    @Final
    private BooleanConsumer callback;

    @org.spongepowered.asm.mixin.Unique
    private boolean awakening_system$hasConfirmed = false;

    /**
     * Automatically confirms and bypasses the vanilla 'Experimental Settings' warning screen
     * when creating or loading worlds with custom dimensions/datapacks.
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void awakening_system$autoConfirmExperimental(CallbackInfo ci) {
        if (!this.awakening_system$hasConfirmed && this.callback != null) {
            this.awakening_system$hasConfirmed = true;
            this.callback.accept(true);
            ci.cancel();
        }
    }
}
