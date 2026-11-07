package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.entity.FallingSideriteMeteorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * Gestor del evento periódico de meteoritos y estrellas fugaces de Siderita.
 * Durante la noche en el Overworld, evalúa la probabilidad de invocar una caída
 * diagonal directamente frente al campo de visión del jugador para máxima espectacularidad.
 */
public final class SideriteMeteorManager {

    private static final Random RANDOM = new Random();
    private static long lastMeteorTime = 0L;
    // Cooldown base natural entre meteoritos: 6000 ticks = 5 minutos
    private static final long NATURAL_COOLDOWN_TICKS = 6000L;

    private SideriteMeteorManager() {}

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        long gameTime = server.overworld().getGameTime();

        // Chequeo cada 200 ticks (10 segundos)
        if (gameTime % 200L != 0L) return;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        List<ServerPlayer> players = overworld.players();
        if (players.isEmpty()) return;

        long dayTime = overworld.getDayTime() % 24000L;
        boolean isNight = dayTime >= 13000L && dayTime <= 23000L;

        // Solo cae de noche de forma natural
        if (!isNight) return;

        if (gameTime - lastMeteorTime < NATURAL_COOLDOWN_TICKS) return;

        // 25% de probabilidad en cada chequeo nocturno
        if (RANDOM.nextFloat() < 0.25F) {
            ServerPlayer targetPlayer = players.get(RANDOM.nextInt(players.size()));
            if (targetPlayer != null && targetPlayer.isAlive()) {
                if (spawnMeteorNearPlayer(targetPlayer, false)) {
                    lastMeteorTime = gameTime;
                }
            }
        }
    }

    /**
     * Hace caer un meteorito de Siderita garantizando que cruce diagonalmente el campo de visión del jugador.
     * @param player Jugador de referencia
     * @param force Si es true, ignora chequeos nocturnos o cooldowns (ideal para comandos de prueba)
     * @return true si se pudo invocar con éxito
     */
    public static boolean spawnMeteorNearPlayer(ServerPlayer player, boolean force) {
        if (player == null || !(player.level() instanceof ServerLevel sl)) return false;

        // 1. Vector de visión horizontal del jugador para garantizar que caiga en su campo visual
        Vec3 look = player.getLookAngle();
        double lookX = look.x;
        double lookZ = look.z;
        double lookLen = Math.sqrt(lookX * lookX + lookZ * lookZ);
        if (lookLen < 0.1D) {
            lookX = 0.0;
            lookZ = 1.0;
            lookLen = 1.0;
        }
        double normLookX = lookX / lookLen;
        double normLookZ = lookZ / lookLen;
        // Vector perpendicular hacia la derecha
        double perpX = -normLookZ;
        double perpZ = normLookX;

        // Punto de impacto objetivo: 75 a 110 bloques en la distancia (lejos del jugador, cinemático y visible)
        double forwardDist = 75.0 + RANDOM.nextDouble() * 35.0;
        double sideDist = (RANDOM.nextDouble() - 0.5) * 40.0;

        int targetX = (int) Math.round(player.getX() + normLookX * forwardDist + perpX * sideDist);
        int targetZ = (int) Math.round(player.getZ() + normLookZ * forwardDist + perpZ * sideDist);

        int targetY = sl.getHeight(Heightmap.Types.MOTION_BLOCKING, targetX, targetZ);
        if (targetY <= sl.getMinBuildHeight() + 5) {
            targetY = (int) player.getY();
        }

        BlockPos targetPos = new BlockPos(targetX, Math.max(sl.getMinBuildHeight() + 2, targetY - 1), targetZ);

        // Origen en la alta atmósfera: 95 a 120 bloques arriba, cruzando diagonalmente el firmamento
        // permitiendo que el jugador observe el descenso durante varios segundos a lo lejos
        double startOffsetY = 95.0 + RANDOM.nextDouble() * 25.0;
        double startBackward = 45.0 + RANDOM.nextDouble() * 20.0;
        double startLateral = (RANDOM.nextBoolean() ? 1.0 : -1.0) * (30.0 + RANDOM.nextDouble() * 20.0);

        double startX = targetX - normLookX * startBackward + perpX * startLateral;
        double startZ = targetZ - normLookZ * startBackward + perpZ * startLateral;
        double startY = Math.min(sl.getMaxBuildHeight() - 10, targetY + startOffsetY);

        Vec3 startPos = new Vec3(startX, startY, startZ);

        FallingSideriteMeteorEntity meteor = new FallingSideriteMeteorEntity(sl, startPos, targetPos);
        sl.addFreshEntity(meteor);

        // 2. Alerta atmosférica en el firmamento: Trueno celestial distante y rugido atmosférico
        sl.playSound(null, player.getX(), player.getY() + 35.0, player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 4.0F, 1.6F);
        sl.playSound(null, player.getX(), player.getY() + 30.0, player.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.WEATHER, 3.5F, 0.55F);

        // 3. Notificación de Etapa 1: Entrada en la atmósfera terrestre para que los jugadores observen el cielo
        Component skyAnnouncement = Component.translatable("awakening_system.announcement.meteor_entry");
        for (ServerPlayer sp : sl.players()) {
            sp.sendSystemMessage(skyAnnouncement);
        }

        lastMeteorTime = sl.getGameTime();
        return true;
    }
}
