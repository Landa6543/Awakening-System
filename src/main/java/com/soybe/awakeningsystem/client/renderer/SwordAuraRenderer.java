package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.item.AwakeningWeaponItem;
import com.soybe.awakeningsystem.item.CreatorClaymoreItem;
import com.soybe.awakeningsystem.item.LaDoncellaSwordItem;
import com.soybe.awakeningsystem.item.PlumBlossomSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Gestor de efectos de Aura de Espada y Qi Espiritual para Awakening System.
 * Genera estelas de Qi, destellos astrales y pétalos flotantes directamente
 * desde la hoja del arma empuñada, sin interferir con la cámara ni envolver
 * el cuerpo del jugador con mallas invasivas.
 */
public final class SwordAuraRenderer {

    private SwordAuraRenderer() {}

    public record AuraStyle(
            ParticleOptions primaryParticle,
            ParticleOptions accentParticle,
            double speed,
            int density
    ) {}

    public static boolean hasSwordAura(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof LaDoncellaSwordItem
                || item instanceof CreatorClaymoreItem
                || item instanceof PlumBlossomSwordItem
                || item instanceof AwakeningWeaponItem;
    }

    public static AuraStyle getAuraStyle(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof LaDoncellaSwordItem) {
            boolean transformed = LaDoncellaSwordItem.isTransformed(stack);
            if (transformed) {
                // Forma Despierta: Trono de Ceniza y Luna Carmesí (Esporas de sangre y ceniza ardiente)
                return new AuraStyle(
                        ParticleTypes.CRIMSON_SPORE,
                        ParticleTypes.SMOKE,
                        0.03D,
                        2
                );
            } else {
                // Forma Pura: Acero de las Rosas (Pétalos de cerezo sagrados y destellos estelares)
                return new AuraStyle(
                        ParticleTypes.CHERRY_LEAVES,
                        ParticleTypes.END_ROD,
                        0.02D,
                        2
                );
            }
        }
        if (item instanceof CreatorClaymoreItem) {
            // Mandoble del Creador: Amatista cósmica y partículas astrales de portal
            return new AuraStyle(
                    ParticleTypes.PORTAL,
                    ParticleTypes.ENCHANT,
                    0.04D,
                    2
            );
        }
        if (item instanceof PlumBlossomSwordItem) {
            // Monte Hua: Danza de pétalos de ciruelo en flor
            return new AuraStyle(
                    ParticleTypes.CHERRY_LEAVES,
                    ParticleTypes.EFFECT,
                    0.02D,
                    2
            );
        }
        String name = item.getDescriptionId().toLowerCase();
        if (name.contains("demon") || name.contains("shadow") || name.contains("dread")) {
            return new AuraStyle(
                    ParticleTypes.SMOKE,
                    ParticleTypes.SOUL_FIRE_FLAME,
                    0.03D,
                    2
            );
        }
        if (name.contains("radiant") || name.contains("holy") || name.contains("faith") || name.contains("asrahan")) {
            return new AuraStyle(
                    ParticleTypes.ELECTRIC_SPARK,
                    ParticleTypes.WAX_OFF,
                    0.02D,
                    2
            );
        }
        // General para cualquier AwakeningWeaponItem
        return new AuraStyle(
                ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.ENCHANTED_HIT,
                0.02D,
                1
        );
    }

    /**
     * Emite partículas de Qi a lo largo de la hoja del arma cada tick.
     */
    public static void tickParticles(Minecraft mc) {
    }
}
