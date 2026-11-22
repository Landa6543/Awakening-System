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
        // Desactivado: Partículas de aura de rango eliminadas por completo a petición del usuario
    }
}
