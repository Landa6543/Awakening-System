package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.entity.FallingSideriteMeteorEntity;
import net.minecraft.world.phys.Vec3;
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

    // Cerca para que el juego tenga cargada la zona y se vea caer el bloque de verdad; lejos para tener que ir a buscarlo
    private static final int MIN_IMPACT_DISTANCE = 70;
    private static final int MAX_IMPACT_DISTANCE = 130;
    private static final int MIN_DISTANCE_TO_PLAYERS = 48;
    // Desde dónde entra: muy alto y algo apartado del punto de impacto, hacia el lado del jugador
    private static final int ENTRY_HEIGHT = 130;
    private static final int ENTRY_OFFSET = 45;
    private static final int SITE_ATTEMPTS = 6;
    private static final int CRATER_CLEARANCE = 10;
    private static final int MAX_TERRAIN_STEP = 6;

    private static final int FALL_TICKS = 90;
    // El cráter humea un minuto: una columna alta que sirve de pista para encontrarlo
    private static final int SMOKE_TICKS = 1200;
    private static final double SMOKE_VIEW_DISTANCE = 640.0D;

    private static final List<FallingMeteor> FALLING = new ArrayList<>();
    private static final List<SmokingCrater> SMOKING = new ArrayList<>();

    private record SmokingCrater(ServerLevel level, BlockPos center, long startTime) {}

    /** Estruendo que todavía viaja hacia un jugador: el sonido llega después del destello. */
    private record PendingBoom(ServerPlayer player, long arrivalTime, float volume, boolean echo) {}
    private static final List<PendingBoom> BOOMS = new ArrayList<>();
    // El sonido recorre unos 17 bloques por tick (343 m/s); a 300 bloques llega casi un segundo tarde
    private static final double SOUND_BLOCKS_PER_TICK = 17.0D;

    private SideriteMeteorManager() {}

    private static final class FallingMeteor {
        final ServerLevel level;
        final BlockPos target;
        final Vec3 start, end;
        FallingSideriteMeteorEntity entity;
        int age = 0;

        FallingMeteor(ServerLevel level, BlockPos target, Vec3 start) {
            this.level = level;
            this.target = target;
            this.start = start;
            this.end = Vec3.atBottomCenterOf(target.above());
        }

        /** Where it is after {@code age} ticks: a straight line, starting slow and speeding up. */
        Vec3 at(int age) {
            double t = Math.min(1.0D, age / (double) FALL_TICKS);
            return start.lerp(end, Math.pow(t, 1.4D));
        }
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        tickFallingMeteors(server);
        tickSmokingCraters(server);
        tickBooms(server);

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
        launch(sl, impactSite, player.position());
        return true;
    }

    /**
     * Prueba desde la consola: un meteorito que cae a unos 90 bloques del punto de aparición del mundo.
     * Devuelve dónde cae, o null si no encontró suelo natural y llano.
     */
    public static BlockPos spawnTestMeteor(ServerLevel sl) {
        BlockPos spawn = sl.getSharedSpawnPos();
        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            int dist = 70 + RANDOM.nextInt(150);
            int x = spawn.getX() + (int) Math.round(Math.cos(angle) * dist), z = spawn.getZ() + (int) Math.round(Math.sin(angle) * dist);
            // the ground check looks 10 blocks around: those chunks must be loaded too
            for (int cx = (x - CRATER_CLEARANCE) >> 4; cx <= (x + CRATER_CLEARANCE) >> 4; cx++)
                for (int cz = (z - CRATER_CLEARANCE) >> 4; cz <= (z + CRATER_CLEARANCE) >> 4; cz++) sl.getChunk(cx, cz);
            BlockPos pos = new BlockPos(x, sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            if (isUntouchedGround(sl, pos)) {
                launch(sl, pos, Vec3.atCenterOf(spawn));
                return pos;
            }
        }
        return null;
    }

    /** Lanza el meteorito hacia {@code impactSite}, entrando por el lado de {@code viewer}. */
    private static void launch(ServerLevel sl, BlockPos impactSite, Vec3 viewer) {
        // Entra por el cielo desde el lado del jugador, así lo ve caer en diagonal hacia el impacto
        Vec3 toPlayer = new Vec3(viewer.x - impactSite.getX(), 0, viewer.z - impactSite.getZ());
        toPlayer = toPlayer.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : toPlayer.normalize();
        double sideways = (RANDOM.nextDouble() - 0.5D) * 0.8D;
        Vec3 offset = new Vec3(toPlayer.x - toPlayer.z * sideways, 0, toPlayer.z + toPlayer.x * sideways).normalize().scale(ENTRY_OFFSET);
        double startY = Math.min(sl.getMaxBuildHeight() + 40, impactSite.getY() + ENTRY_HEIGHT);
        Vec3 start = new Vec3(impactSite.getX() + 0.5D + offset.x, startY, impactSite.getZ() + 0.5D + offset.z);
        FallingMeteor meteor = new FallingMeteor(sl, impactSite, start);
        meteor.entity = new FallingSideriteMeteorEntity(sl, start, impactSite);
        sl.addFreshEntity(meteor.entity);
        FALLING.add(meteor);

        Component skyAnnouncement = Component.translatable("awakening_system.announcement.meteor_entry");
        for (ServerPlayer sp : sl.players()) {
            sp.sendSystemMessage(skyAnnouncement);
            // Rugido grave de aire al cruzar la atmósfera, con un siseo de fuego; se corta al impactar
            sp.playNotifySound(SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, 0.55F, 0.45F);
            sp.playNotifySound(SoundEvents.FIRECHARGE_USE, SoundSource.WEATHER, 0.35F, 0.5F);
        }

        lastMeteorTime = sl.getGameTime();
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
            // Lo lleva por su recta tick a tick (así cae aunque su chunk no esté activo); la velocidad es
            // para que el cliente lo dibuje suave entre un tick y otro
            if (meteor.entity != null && meteor.entity.isAlive()) {
                Vec3 now = meteor.at(meteor.age), next = meteor.at(meteor.age + 1);
                meteor.entity.setPos(now.x, now.y, now.z);
                meteor.entity.setDeltaMovement(next.subtract(now));
            }
            if (meteor.age >= FALL_TICKS) {
                if (meteor.entity != null) {
                    com.soybe.awakeningsystem.AwakeningSystemMod.LOGGER.info("[Meteor] reached {} (aimed at {})",
                            meteor.entity.blockPosition().toShortString(), meteor.target.toShortString());
                    meteor.entity.discard();
                }
                FallingSideriteMeteorEntity.createImpact(meteor.level, meteor.target);
                int ores = 0, blocks = 0;
                for (BlockPos p : BlockPos.betweenClosed(meteor.target.offset(-9, -6, -9), meteor.target.offset(9, 3, 9))) {
                    BlockState st = meteor.level.getBlockState(p);
                    if (st.is(com.soybe.awakeningsystem.block.SystemBlocks.SIDERITE_ORE.get())) ores++;
                    if (st.is(com.soybe.awakeningsystem.block.SystemBlocks.SIDERITE_BLOCK.get())) blocks++;
                }
                com.soybe.awakeningsystem.AwakeningSystemMod.LOGGER.info("[Meteor] crater at {}: {} siderite ore, {} siderite blocks",
                        meteor.target.toShortString(), ores, blocks);
                MeteorImpactData.get(server).setLastImpact(meteor.level.dimension(), meteor.target);
                SMOKING.add(new SmokingCrater(meteor.level, meteor.target, meteor.level.getGameTime()));
                long now = meteor.level.getGameTime();
                for (ServerPlayer player : meteor.level.players()) {
                    player.connection.send(new ClientboundStopSoundPacket(SoundEvents.ELYTRA_FLYING.getLocation(), SoundSource.WEATHER));
                    double distance = Math.sqrt(player.distanceToSqr(meteor.target.getCenter()));
                    long delay = Math.min(60L, Math.round(distance / SOUND_BLOCKS_PER_TICK));
                    float volume = (float) Math.max(0.45D, Math.min(1.0D, 1.25D - distance / 600.0D));
                    BOOMS.add(new PendingBoom(player, now + delay, volume, false));
                    // eco que rueda por el paisaje un poco después
                    BOOMS.add(new PendingBoom(player, now + delay + 14, volume * 0.6F, true));
                }
                iterator.remove();
            }
        }
    }

    private static void tickBooms(MinecraftServer server) {
        Iterator<PendingBoom> iterator = BOOMS.iterator();
        while (iterator.hasNext()) {
            PendingBoom boom = iterator.next();
            ServerPlayer player = boom.player();
            if (player.hasDisconnected() || player.getServer() != server) {
                iterator.remove();
                continue;
            }
            if (player.level().getGameTime() < boom.arrivalTime()) continue;
            if (boom.echo()) {
                player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, boom.volume(), 0.42F);
            } else {
                // golpe seco, retumbo profundo y desgarro de fuego, todo a la vez
                player.playNotifySound(SoundEvents.GENERIC_EXPLODE.value(), SoundSource.WEATHER, boom.volume(), 0.5F);
                player.playNotifySound(SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.WEATHER, boom.volume() * 0.8F, 0.55F);
                player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, boom.volume() * 0.7F, 0.6F);
            }
            iterator.remove();
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
