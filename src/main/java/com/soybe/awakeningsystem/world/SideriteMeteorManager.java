package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.entity.FallingSideriteMeteorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Evento de meteoritos de Siderita, al estilo de Terraria: de vez en cuando una estrella fugaz
 * cruza el cielo y un meteorito cae lejos, en algún lugar natural que el jugador debe buscar.
 */
public final class SideriteMeteorManager {

    private static final Random RANDOM = new Random();
    private static long lastMeteorTime = 0L;

    // Como mínimo 3 días de juego entre meteoritos naturales
    private static final long NATURAL_COOLDOWN_TICKS = 72000L;
    // Se comprueba cada 10 segundos: cerca de 1 noche de cada 5 una vez pasado el cooldown
    private static final float CHANCE_PER_CHECK = 0.005F;

    private static final int MIN_IMPACT_DISTANCE = 180;
    private static final int MAX_IMPACT_DISTANCE = 400;
    private static final int MIN_DISTANCE_TO_PLAYERS = 96;
    private static final int SITE_ATTEMPTS = 6;
    private static final int CRATER_CLEARANCE = 10;
    private static final int MAX_TERRAIN_STEP = 6;

    private static final int FALL_TICKS = 50;
    // Dentro de la niebla de la distancia de renderizado más común, para que siempre se vea
    private static final double STREAK_DISTANCE = 110.0D;
    private static final double STREAK_DRIFT = 35.0D;
    private static final double STREAK_START_HEIGHT = 115.0D;
    private static final double STREAK_END_HEIGHT = 20.0D;

    private static final List<FallingMeteor> FALLING = new ArrayList<>();

    private SideriteMeteorManager() {}

    private static final class FallingMeteor {
        final ServerLevel level;
        final BlockPos target;
        final double driftSign;
        int age = 0;

        FallingMeteor(ServerLevel level, BlockPos target, double driftSign) {
            this.level = level;
            this.target = target;
            this.driftSign = driftSign;
        }
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        tickFallingMeteors(server);

        long gameTime = server.overworld().getGameTime();
        // El contador es estático: al cambiar a un mundo más nuevo no debe bloquear los meteoritos
        if (lastMeteorTime > gameTime) lastMeteorTime = 0L;

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

        if (RANDOM.nextFloat() < CHANCE_PER_CHECK) {
            ServerPlayer targetPlayer = players.get(RANDOM.nextInt(players.size()));
            if (targetPlayer != null && targetPlayer.isAlive()) {
                spawnMeteorNearPlayer(targetPlayer, false);
            }
        }
    }

    /**
     * Inicia la caída de un meteorito lejos del jugador.
     * @param player Jugador de referencia
     * @param force Reservado para comandos de prueba; la zona de impacto se valida igual
     * @return true si se encontró una zona segura y el meteorito empezó a caer
     */
    public static boolean spawnMeteorNearPlayer(ServerPlayer player, boolean force) {
        if (player == null || !(player.level() instanceof ServerLevel sl)) return false;

        BlockPos impactSite = findImpactSite(sl, player);
        if (impactSite == null) return false;

        FALLING.add(new FallingMeteor(sl, impactSite, RANDOM.nextBoolean() ? 1.0D : -1.0D));

        Component skyAnnouncement = Component.translatable("awakening_system.announcement.meteor_entry");
        for (ServerPlayer sp : sl.players()) {
            sp.sendSystemMessage(skyAnnouncement);
            sp.playNotifySound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.WEATHER, 0.8F, 0.55F);
        }

        lastMeteorTime = sl.getGameTime();
        return true;
    }

    private static void tickFallingMeteors(MinecraftServer server) {
        Iterator<FallingMeteor> iterator = FALLING.iterator();
        while (iterator.hasNext()) {
            FallingMeteor meteor = iterator.next();
            if (meteor.level.getServer() != server) {
                iterator.remove();
                continue;
            }

            meteor.age++;
            for (ServerPlayer player : meteor.level.players()) {
                emitStreak(player, meteor);
            }

            if (meteor.age >= FALL_TICKS) {
                FallingSideriteMeteorEntity.createImpact(meteor.level, meteor.target);
                for (ServerPlayer player : meteor.level.players()) {
                    player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.6F, 0.5F);
                }
                iterator.remove();
            }
        }
    }

    /** Dibuja, solo para este jugador, la estrella fugaz bajando hacia el horizonte. */
    private static void emitStreak(ServerPlayer player, FallingMeteor meteor) {
        double toX = meteor.target.getX() - player.getX();
        double toZ = meteor.target.getZ() - player.getZ();
        double length = Math.sqrt(toX * toX + toZ * toZ);
        if (length < 1.0D) {
            toX = 0.0D;
            toZ = 1.0D;
            length = 1.0D;
        }
        double dirX = toX / length;
        double dirZ = toZ / length;

        for (int step = 0; step < 4; step++) {
            double progress = (meteor.age + step / 4.0D) / FALL_TICKS;
            double drift = meteor.driftSign * STREAK_DRIFT * (1.0D - 2.0D * progress);
            double x = player.getX() + dirX * STREAK_DISTANCE - dirZ * drift;
            double z = player.getZ() + dirZ * STREAK_DISTANCE + dirX * drift;
            double y = player.getY() + STREAK_START_HEIGHT + (STREAK_END_HEIGHT - STREAK_START_HEIGHT) * progress;

            meteor.level.sendParticles(player, ParticleTypes.END_ROD, true, x, y, z, 6, 0.35D, 0.35D, 0.35D, 0.01D);
            meteor.level.sendParticles(player, ParticleTypes.FIREWORK, true, x, y, z, 2, 0.3D, 0.3D, 0.3D, 0.01D);
            if (step == 0 && meteor.age % 3 == 0) {
                meteor.level.sendParticles(player, ParticleTypes.FLASH, true, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static BlockPos findImpactSite(ServerLevel level, ServerPlayer player) {
        for (int attempt = 0; attempt < SITE_ATTEMPTS; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_IMPACT_DISTANCE + RANDOM.nextDouble() * (MAX_IMPACT_DISTANCE - MIN_IMPACT_DISTANCE);
            int x = (int) Math.round(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.round(player.getZ() + Math.sin(angle) * distance);
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);

            if (level.getWorldBorder().isWithinBounds(pos)
                    && level.getNearestPlayer(x, pos.getY(), z, MIN_DISTANCE_TO_PLAYERS, false) == null
                    && isUntouchedGround(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    /** Solo vale terreno natural y llano: así el cráter nunca destruye construcciones ni estructuras. */
    private static boolean isUntouchedGround(ServerLevel level, BlockPos center) {
        if (level.structureManager().hasAnyStructureAt(center)) return false;

        for (int dx = -CRATER_CLEARANCE; dx <= CRATER_CLEARANCE; dx += 2) {
            for (int dz = -CRATER_CLEARANCE; dz <= CRATER_CLEARANCE; dz += 2) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                if (Math.abs(surfaceY - center.getY()) > MAX_TERRAIN_STEP) return false;
                if (!isNaturalGround(level.getBlockState(new BlockPos(x, surfaceY, z)))) return false;
            }
        }
        return true;
    }

    private static boolean isNaturalGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.TERRACOTTA)
                || state.is(BlockTags.SNOW)
                || state.is(BlockTags.LOGS)
                || state.is(Blocks.GRAVEL);
    }
}
