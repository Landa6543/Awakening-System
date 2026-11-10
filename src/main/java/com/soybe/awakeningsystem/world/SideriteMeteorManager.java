package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.MeteorStreakPayload;
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

    // Lejos: hay que ir a buscarlo y no deja cráteres junto a la casa. Cada cliente lo dibuja en su sitio
    // real del mundo (MeteorSkyRenderer), así que se ve caer aunque esté fuera del terreno cargado
    private static final int MIN_IMPACT_DISTANCE = 180;
    private static final int MAX_IMPACT_DISTANCE = 320;   // the explosion is game particles: past the render distance it would not show
    private static final int MIN_DISTANCE_TO_PLAYERS = 150;
    // Desde dónde entra: muy alto y desplazado de lado respecto al jugador, para que se vea caer en diagonal
    private static final int ENTRY_HEIGHT = 230;
    private static final int ENTRY_OFFSET = 150;
    // Los jugadores a esta distancia lo ven caer
    private static final double VIEW_DISTANCE = 1500.0D;
    private static final int SITE_ATTEMPTS = 12;
    private static final int CRATER_CLEARANCE = 10;
    private static final int MAX_TERRAIN_STEP = 6;

    private static final int FALL_TICKS = 140;
    // El cráter humea un minuto: una columna alta que sirve de pista para encontrarlo
    private static final int SMOKE_TICKS = 1200;
    private static final double SMOKE_VIEW_DISTANCE = 640.0D;

    private static final List<FallingMeteor> FALLING = new ArrayList<>();
    private static final List<SmokingCrater> SMOKING = new ArrayList<>();

    private record SmokingCrater(ServerLevel level, BlockPos center, long startTime) {}

    /** Estruendo que todavía viaja hacia un jugador: el sonido llega después del destello. */
    private record PendingBoom(ServerPlayer player, long arrivalTime, float volume, boolean echo, Vec3 from) {}
    private static final List<PendingBoom> BOOMS = new ArrayList<>();
    // El sonido recorre unos 17 bloques por tick (343 m/s); a 300 bloques llega casi un segundo tarde
    private static final double SOUND_BLOCKS_PER_TICK = 17.0D;

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
            int dist = MIN_IMPACT_DISTANCE + RANDOM.nextInt(MAX_IMPACT_DISTANCE - MIN_IMPACT_DISTANCE);
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

    /**
     * Lanza el meteorito hacia {@code impactSite}. Cada uno entra distinto: casi recto desde arriba, en diagonal
     * o rasante (casi de lado), y desde cualquier lado, aunque nunca por detrás del jugador que lo mira.
     */
    private static void launch(ServerLevel sl, BlockPos impactSite, Vec3 viewer) {
        Vec3 toPlayer = new Vec3(viewer.x - impactSite.getX(), 0, viewer.z - impactSite.getZ());
        toPlayer = toPlayer.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : toPlayer.normalize();
        // horizontal distance per block of height: 0.1 almost vertical, 0.7 diagonal, 1.6 grazing
        double slope = switch (RANDOM.nextInt(3)) {
            case 0 -> 0.05D + RANDOM.nextDouble() * 0.15D;
            case 1 -> 0.5D + RANDOM.nextDouble() * 0.4D;
            default -> 1.3D + RANDOM.nextDouble() * 0.6D;
        };
        double height = ENTRY_HEIGHT * (slope > 1.0D ? 0.6D : 1.0D);   // grazing ones come in lower
        // the side it comes from: anywhere from across the player's view to towards the player
        double angle = (RANDOM.nextDouble() - 0.5D) * Math.PI * 1.4D;
        Vec3 from = new Vec3(toPlayer.x * Math.cos(angle) - toPlayer.z * Math.sin(angle), 0, toPlayer.x * Math.sin(angle) + toPlayer.z * Math.cos(angle));
        Vec3 offset = from.scale(height * slope);
        Vec3 start = new Vec3(impactSite.getX() + 0.5D + offset.x, impactSite.getY() + height, impactSite.getZ() + 0.5D + offset.z);
        Vec3 end = Vec3.atBottomCenterOf(impactSite.above());
        FALLING.add(new FallingMeteor(sl, impactSite));
        MeteorStreakPayload fall = new MeteorStreakPayload(start.x, start.y, start.z, end.x, end.y, end.z, FALL_TICKS, RANDOM.nextLong());

        Component skyAnnouncement = Component.translatable("awakening_system.announcement.meteor_entry");
        for (ServerPlayer sp : sl.players()) {
            sp.sendSystemMessage(skyAnnouncement);
            if (sp.distanceToSqr(end) < VIEW_DISTANCE * VIEW_DISTANCE) PacketDistributor.sendToPlayer(sp, fall);
            // Rugido grave de aire al cruzar la atmósfera, con un siseo de fuego; se corta al impactar
            soundFrom(sp, start, SoundEvents.ELYTRA_FLYING, 0.55F, 0.45F);
            soundFrom(sp, start, SoundEvents.FIRECHARGE_USE, 0.35F, 0.5F);
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
            // the fall itself is drawn by each client; here only the impact happens, on time
            if (meteor.age >= FALL_TICKS) {
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
                    BOOMS.add(new PendingBoom(player, now + delay, volume, false, meteor.target.getCenter()));
                    // eco que rueda por el paisaje un poco después
                    BOOMS.add(new PendingBoom(player, now + delay + 14, volume * 0.6F, true, meteor.target.getCenter()));
                }
                iterator.remove();
            }
        }
    }

    /**
     * Plays a sound for one player as coming from {@code source}. The real spot is hundreds of blocks off,
     * beyond hearing range, so the sound is placed 20 blocks from the player in that direction: the game then
     * pans it left, right or ahead as it should, and it is still loud.
     */
    private static void soundFrom(ServerPlayer player, Vec3 source, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = source.subtract(eye);
        Vec3 at = dir.lengthSqr() < 1.0E-4 ? eye : eye.add(dir.normalize().scale(Math.min(20.0D, dir.length())));
        // range is 16 blocks per point of volume: at least 2 so it carries over those 20 blocks
        float vol = Math.max(2.0F, volume * 3.0F);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.WEATHER,
                at.x, at.y, at.z, vol, pitch, player.getRandom().nextLong()));
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
                soundFrom(player, boom.from(), SoundEvents.LIGHTNING_BOLT_THUNDER, boom.volume(), 0.42F);
            } else {
                // golpe seco, retumbo profundo y desgarro de fuego, todo a la vez
                soundFrom(player, boom.from(), SoundEvents.GENERIC_EXPLODE.value(), boom.volume(), 0.5F);
                soundFrom(player, boom.from(), SoundEvents.DRAGON_FIREBALL_EXPLODE, boom.volume() * 0.8F, 0.55F);
                soundFrom(player, boom.from(), SoundEvents.LIGHTNING_BOLT_THUNDER, boom.volume() * 0.7F, 0.6F);
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
            // that far out the chunks may not be loaded yet, and the ground check needs their heightmaps
            for (int cx = (x - CRATER_CLEARANCE) >> 4; cx <= (x + CRATER_CLEARANCE) >> 4; cx++)
                for (int cz = (z - CRATER_CLEARANCE) >> 4; cz <= (z + CRATER_CLEARANCE) >> 4; cz++) level.getChunk(cx, cz);
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
