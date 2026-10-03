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
        if (mc.level == null || mc.player == null) return;
        LocalPlayer player = mc.player;
        if (!player.isAlive()) return;

        ItemStack stack = player.getMainHandItem();
        if (!hasSwordAura(stack)) {
            stack = player.getOffhandItem();
        }
        if (!hasSwordAura(stack)) return;

        AuraStyle style = getAuraStyle(stack);
        if (style.primaryParticle() == null) return;

        Vec3 look = player.getLookAngle();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = look.cross(up).normalize();

        // 1. Partículas pasivas que fluyen suavemente por la hoja
        if (player.getRandom().nextFloat() < 0.70F) {
            double handX = player.getX() + right.x * 0.45D + look.x * 0.50D;
            double handY = player.getY() + player.getEyeHeight() * 0.70D + look.y * 0.50D;
            double handZ = player.getZ() + right.z * 0.45D + look.z * 0.50D;

            float bladeFrac = player.getRandom().nextFloat();
            double px = handX + look.x * bladeFrac * 0.65D + (player.getRandom().nextDouble() - 0.5D) * 0.10D;
            double py = handY + (bladeFrac * 0.40D) + (player.getRandom().nextDouble() - 0.5D) * 0.10D;
            double pz = handZ + look.z * bladeFrac * 0.65D + (player.getRandom().nextDouble() - 0.5D) * 0.10D;

            ParticleOptions p = (player.getRandom().nextFloat() < 0.75F) ? style.primaryParticle() : style.accentParticle();
            if (p != null) {
                mc.level.addParticle(p, px, py, pz,
                        (player.getRandom().nextDouble() - 0.5D) * 0.015D,
                        0.02D + player.getRandom().nextDouble() * 0.02D,
                        (player.getRandom().nextDouble() - 0.5D) * 0.015D);
            }
        }

        // 2. Estela de corte de espada al blandir el arma (Attack Swing Trail)
        if (player.attackAnim > 0.0F && player.attackAnim < 0.85F) {
            for (int i = 0; i < 3; i++) {
                double arcFrac = player.attackAnim + (player.getRandom().nextDouble() - 0.5D) * 0.15D;
                double arcX = player.getX() + look.x * 1.2D + right.x * Math.sin(arcFrac * Math.PI) * 0.8D;
                double arcY = player.getY() + player.getEyeHeight() * 0.75D + Math.cos(arcFrac * Math.PI) * 0.4D;
                double arcZ = player.getZ() + look.z * 1.2D + right.z * Math.sin(arcFrac * Math.PI) * 0.8D;

                mc.level.addParticle(style.primaryParticle(), arcX, arcY, arcZ,
                        right.x * 0.05D, 0.02D, right.z * 0.05D);
            }
        }
    }
}
