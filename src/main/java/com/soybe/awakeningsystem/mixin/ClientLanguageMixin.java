package com.soybe.awakeningsystem.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A creature from another mod whose name that mod never translated shows a clean name made from its id
 * ("entity.block_factorys_bosses.cinematic_kraken" reads "Cinematic Kraken") instead of the bare key.
 */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {

    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void awakening$cleanEntityName(String key, String fallback, CallbackInfoReturnable<String> cir) {
        String got = cir.getReturnValue();
        if (got == null || !got.equals(key) || !key.startsWith("entity.")) return;
        int dot = key.indexOf('.', 7);
        if (dot < 0 || dot == key.length() - 1 || key.indexOf('.', dot + 1) >= 0) return;   // only entity.<mod>.<name>
        StringBuilder sb = new StringBuilder();
        for (String word : key.substring(dot + 1).split("_")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        cir.setReturnValue(sb.toString());
    }
}
