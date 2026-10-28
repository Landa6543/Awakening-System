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
                return new AuraStyle(
                        ParticleTypes.CRIMSON_SPORE,
                        ParticleTypes.SMOKE,
                        0.03D,
                        2
                );
            } else {
                return new AuraStyle(
                        ParticleTypes.CHERRY_LEAVES,
                        ParticleTypes.END_ROD,
                        0.02D,
                        2
                );
            }
        }
        if (item instanceof CreatorClaymoreItem) {
            return new AuraStyle(
                    ParticleTypes.PORTAL,
                    ParticleTypes.ENCHANT,
                    0.04D,
                    2
            );
        }
        if (item instanceof PlumBlossomSwordItem) {
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
        return new AuraStyle(
                ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.ENCHANTED_HIT,
                0.02D,
                1
        );
    }

    /**
     * Emite partículas de Qi a lo largo del arma empuñada cada tick del cliente.
     */
    public static void tickParticles(Minecraft mc) {
        if (mc.isPaused() || mc.level == null) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!hasSwordAura(stack)) return;
        if (player.tickCount % 3 != 0) return;
        AuraStyle style = getAuraStyle(stack);
        if (style == null) return;

        double yaw = Math.toRadians(player.yBodyRot);
        double px = player.getX() - Math.sin(yaw) * 0.45D;
        double py = player.getY() + 0.75D + (player.getRandom().nextDouble() * 0.45D);
        double pz = player.getZ() + Math.cos(yaw) * 0.45D;
        player.level().addParticle(style.primaryParticle(), px, py, pz,
                (player.getRandom().nextDouble() - 0.5D) * style.speed(),
                player.getRandom().nextDouble() * style.speed(),
                (player.getRandom().nextDouble() - 0.5D) * style.speed());
        if (style.accentParticle() != null && player.getRandom().nextFloat() < 0.35F) {
            player.level().addParticle(style.accentParticle(), px, py + 0.1D, pz,
                    (player.getRandom().nextDouble() - 0.5D) * style.speed() * 0.5D,
                    player.getRandom().nextDouble() * style.speed() * 0.5D,
                    (player.getRandom().nextDouble() - 0.5D) * style.speed() * 0.5D);
        }
    }
}
