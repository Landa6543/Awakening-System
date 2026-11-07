package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.MeteorStreakPayload;
import com.soybe.awakeningsystem.entity.FallingSideriteMeteorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
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
import net.neoforged.neoforge.network.PacketDistributor;

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

    private static final int FALL_TICKS = 80;
    // El cráter humea un minuto: una columna alta que sirve de pista para encontrarlo
    private static final int SMOKE_TICKS = 1200;
    private static final double SMOKE_VIEW_DISTANCE = 640.0D;

    private static final List<FallingMeteor> FALLING = new ArrayList<>();
    private static final List<SmokingCrater> SMOKING = new ArrayList<>();

    private record SmokingCrater(ServerLevel level, BlockPos center, long startTime) {}

    private SideriteMeteorManager() {}

    private static final class FallingMeteor {
        final ServerLevel level;
        final BlockPos target;
        int age = 0;

        FallingMeteor(ServerLevel level, BlockPos target) {
            this.level = level;
            this.target = target;
        }
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        tickFallingMeteors(server);
        tickSmokingCraters(server);

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

        FALLING.add(new FallingMeteor(sl, impactSite));
        float driftSign = RANDOM.nextBoolean() ? 1.0F : -1.0F;

        Component skyAnnouncement = Component.translatable("awakening_system.announcement.meteor_entry");
        for (ServerPlayer sp : sl.players()) {
            sp.sendSystemMessage(skyAnnouncement);
            // Cada jugador ve la estrella caer hacia la dirección real del impacto
            float bearing = (float) Math.toDegrees(Math.atan2(-(impactSite.getX() - sp.getX()), impactSite.getZ() - sp.getZ()));
            PacketDistributor.sendToPlayer(sp, new MeteorStreakPayload(bearing, driftSign, FALL_TICKS));
            // Rugido de aire al cruzar la atmósfera; se corta en el momento del impacto
            sp.playNotifySound(SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, 0.45F, 0.6F);
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
            if (meteor.age >= FALL_TICKS) {
                FallingSideriteMeteorEntity.createImpact(meteor.level, meteor.target);
                MeteorImpactData.get(server).setLastImpact(meteor.level.dimension(), meteor.target);
                SMOKING.add(new SmokingCrater(meteor.level, meteor.target, meteor.level.getGameTime()));
                for (ServerPlayer player : meteor.level.players()) {
                    player.connection.send(new ClientboundStopSoundPacket(SoundEvents.ELYTRA_FLYING.getLocation(), SoundSource.WEATHER));
                    player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.6F, 0.5F);
                }
                iterator.remove();
            }
        }
    }

    private static void tickSmokingCraters(MinecraftServer server) {
        Iterator<SmokingCrater> iterator = SMOKING.iterator();
        while (iterator.hasNext()) {
            SmokingCrater crater = iterator.next();
            long age = crater.level().getGameTime() - crater.startTime();
            if (crater.level().getServer() != server || age < 0 || age > SMOKE_TICKS) {
                iterator.remove();
                continue;
            }
            if (age % 4 != 0) continue;

            double x = crater.center().getX() + 0.5D;
            double y = crater.center().getY() + 1.0D;
            double z = crater.center().getZ() + 0.5D;
            for (ServerPlayer player : crater.level().players()) {
                if (player.distanceToSqr(x, y, z) > SMOKE_VIEW_DISTANCE * SMOKE_VIEW_DISTANCE) continue;
                // Forzadas: se ven desde lejos, que es justo cuando hacen falta
                crater.level().sendParticles(player, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, x, y, z, 2, 0.8D, 0.2D, 0.8D, 0.01D);
                if (age < 200) {
                    crater.level().sendParticles(player, ParticleTypes.LAVA, true, x, y, z, 2, 1.5D, 0.3D, 1.5D, 0.0D);
                }
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
