package com.soybe.awakeningsystem.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    /**
     * Bypasses the vanilla experimental/deprecated world warning screen when creating a world.
     */
    @Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
    private static void awakening_system$autoConfirmWorldCreation(
            Minecraft minecraft,
            CreateWorldScreen createWorldScreen,
            Lifecycle lifecycle,
            Runnable runnable,
            boolean bl,
            CallbackInfo ci
    ) {
        if (runnable != null) {
            runnable.run();
            ci.cancel();
        }
    }
}
