package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Gestor de Auras de Maná y Sombras para Cazadores de Alto Rango (A, S, Monarcas).
 * Despliega suaves estelas de niebla de maná cian y humo de sombras violeta/obsidiana
 * alrededor de los pies y silueta del jugador sin saturar los FPS ni obstruir la vista en 1ra persona.
 */
public final class PlayerRankAuraRenderer {
    private PlayerRankAuraRenderer() {}

    public static void tickParticles(Minecraft mc) {
        if (mc.level == null) return;
        List<? extends Player> players = mc.level.players();
        if (players.isEmpty()) return;

        RandomSource random = mc.level.random;

        for (Player player : players) {
            if (!player.isAlive() || player.isInvisible()) continue;

            SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data == null) continue;

            int level = data.level();
            if (level < 40) continue; // Requiere mínimo Rango A (Nivel 40)

            boolean isSOrHigher = level >= 50;
            boolean isMonarchOrShadow = level >= 70 || "shadow_monarch".equalsIgnoreCase(data.hunterClass()) || "necromancer".equalsIgnoreCase(data.hunterClass());

            boolean isLocalFirstPerson = (player instanceof LocalPlayer && mc.options.getCameraType().isFirstPerson());

            // Tasa de generación suave: cada 2-3 ticks en reposo, más activo si corre o está en combate
            boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.005D;
            boolean active = moving || player.isSprinting() || player.attackAnim > 0.0F || player.hurtTime > 0;
            int chance = active ? 2 : 4;

            if (random.nextInt(chance) != 0) continue;

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.35D + random.nextDouble() * 0.25D;

            double px = player.getX() + Math.cos(angle) * radius;
            double py = player.getY() + 0.05D + random.nextDouble() * 0.4D;
            double pz = player.getZ() + Math.sin(angle) * radius;

            // En 1ra persona para el jugador local, emitimos ligeramente abajo para no tapar la mira
            if (isLocalFirstPerson) {
                py = player.getY() + 0.02D;
                px = player.getX() + (random.nextDouble() - 0.5D) * 0.4D;
                pz = player.getZ() + (random.nextDouble() - 0.5D) * 0.4D;
            }

            double vx = (random.nextDouble() - 0.5D) * 0.015D;
            double vy = 0.025D + random.nextDouble() * 0.02D;
            double vz = (random.nextDouble() - 0.5D) * 0.015D;

            if (isMonarchOrShadow) {
                // Sombra de Monarca / Rango Superior: Humo de sombras con destellos de maná
                if (random.nextBoolean()) {
                    mc.level.addParticle(ModParticles.SHADOW_AURA.get(), px, py, pz, vx, vy, vz);
                } else {
                    mc.level.addParticle(ModParticles.MANA_AURA.get(), px, py, pz, vx, vy * 1.2D, vz);
                }
            } else if (isSOrHigher) {
                // Rango S: Maná denso y sutil estela de sombras
                if (random.nextInt(3) == 0) {
                    mc.level.addParticle(ModParticles.SHADOW_AURA.get(), px, py, pz, vx, vy, vz);
                } else {
                    mc.level.addParticle(ModParticles.MANA_AURA.get(), px, py, pz, vx, vy, vz);
                }
            } else {
                // Rango A: Maná cian puro
                mc.level.addParticle(ModParticles.MANA_AURA.get(), px, py, pz, vx, vy, vz);
            }
        }
    }
}
