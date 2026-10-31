package com.soybe.awakeningsystem.penalty;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.entity.GiantSandwormEntity;
import com.soybe.awakeningsystem.entity.GiantSandwormHeadPart;
import com.soybe.awakeningsystem.entity.GiantSandwormBodyPart;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PenaltyZoneManager {
    private PenaltyZoneManager() {}

    public static final ResourceKey<Level> PENALTY_ZONE = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "penalty_zone")
    );

    public static final int SURVIVAL_DURATION_TICKS = 6000; // 5 minutes (300 seconds * 20 ticks)
    public static final int WORMS_REQUIRED_FOR_EARLY_ESCAPE = 2;

    public static class PenaltySession {
        public int ticksRemaining = SURVIVAL_DURATION_TICKS;
        public int wormsKilled = 0;
        public ServerBossEvent bossBar;
        public ResourceKey<Level> previousDimension = Level.OVERWORLD;
        public BlockPos previousPos = BlockPos.ZERO;
        public int wormSpawnCooldown = 60; // 3 seconds after arrival
    }

    private static final Map<UUID, PenaltySession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    public static boolean isInPenaltyZone(ServerPlayer player) {
        return player != null && player.level().dimension().equals(PENALTY_ZONE);
    }

    public static boolean isInPenaltyZone(net.minecraft.world.entity.Entity entity) {
        return entity != null && entity.level() != null && entity.level().dimension().equals(PENALTY_ZONE);
    }

    public static boolean isPenaltyDimension(Level level) {
        return level != null && level.dimension().equals(PENALTY_ZONE);
    }

    public static void onPlayerLeftPenaltyZone(ServerPlayer player) {
        if (player == null) return;
        PenaltySession session = ACTIVE_SESSIONS.remove(player.getUUID());
        if (session != null && session.bossBar != null) {
            session.bossBar.removeAllPlayers();
        }
    }

    public static void teleportToPenaltyZone(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel penaltyLevel = server.getLevel(PENALTY_ZONE);
        if (penaltyLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.penalty.unavailable"), true);
            return;
        }

        PenaltySession session = new PenaltySession();
        session.previousDimension = player.level().dimension();
        session.previousPos = player.blockPosition();

        ServerBossEvent bossBar = new ServerBossEvent(
                Component.translatable("awakening_system.penalty.bossbar", "05:00", 0, WORMS_REQUIRED_FOR_EARLY_ESCAPE)
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.addPlayer(player);
        session.bossBar = bossBar;

        ACTIVE_SESSIONS.put(player.getUUID(), session);

        // Find safe spawn pos on surface of desert
        int spawnX = (int) player.getX();
        int spawnZ = (int) player.getZ();
        penaltyLevel.getChunk(spawnX >> 4, spawnZ >> 4);
        int surfaceY = penaltyLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
        if (surfaceY <= penaltyLevel.getMinBuildHeight() || surfaceY < 50) {
            surfaceY = 72;
        }

        player.teleportTo(penaltyLevel, spawnX + 0.5D, surfaceY + 1.0D, spawnZ + 0.5D, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.translatable("awakening_system.penalty.teleported"));

        // Spawn controlled set of 5 worms: encircling the player within loaded distance
        spawnInitialWorms(penaltyLevel, player);

        // System Alert Announcement
        sendTitle(player,
                Component.translatable("awakening_system.penalty.title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.translatable("awakening_system.penalty.subtitle", WORMS_REQUIRED_FOR_EARLY_ESCAPE).withStyle(ChatFormatting.GOLD),
                10, 80, 20);

        player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 0.75F);
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
    }

    public static void onSandwormKilled(ServerLevel level, ServerPlayer player) {
        if (player == null) return;
        PenaltySession session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session != null) {
            session.wormsKilled++;
            if (session.bossBar != null) {
                updateBossBarTitle(session);
            }

            if (session.wormsKilled >= WORMS_REQUIRED_FOR_EARLY_ESCAPE) {
                completePenaltyZone(player, true);
            }
        }
    }

    public static void serverTick(MinecraftServer server) {
        ServerLevel penaltyLevel = server.getLevel(PENALTY_ZONE);
        if (penaltyLevel == null) {
            for (PenaltySession session : ACTIVE_SESSIONS.values()) {
                if (session.bossBar != null) {
                    session.bossBar.removeAllPlayers();
                }
            }
            ACTIVE_SESSIONS.clear();
            return;
        }

        // Clean up any session whose player logged out, left the dimension, or is in Creative/Spectator
        for (UUID uuid : new java.util.ArrayList<>(ACTIVE_SESSIONS.keySet())) {
            ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
            if (sp == null || !sp.level().dimension().equals(PENALTY_ZONE) || sp.isCreative() || sp.isSpectator()) {
                PenaltySession session = ACTIVE_SESSIONS.remove(uuid);
                if (session != null && session.bossBar != null) {
                    session.bossBar.removeAllPlayers();
                }
            }
        }

        for (ServerPlayer player : penaltyLevel.players()) {
            if (player.isCreative() || player.isSpectator()) {
                PenaltySession s = ACTIVE_SESSIONS.remove(player.getUUID());
                if (s != null && s.bossBar != null) {
                    s.bossBar.removeAllPlayers();
                }
                continue;
            }

            PenaltySession session = ACTIVE_SESSIONS.computeIfAbsent(player.getUUID(), uuid -> {
                PenaltySession s = new PenaltySession();
                s.bossBar = new ServerBossEvent(
                        Component.translatable("awakening_system.penalty.bossbar", "05:00", 0, WORMS_REQUIRED_FOR_EARLY_ESCAPE)
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                        BossEvent.BossBarColor.RED,
                        BossEvent.BossBarOverlay.PROGRESS
                );
                s.bossBar.addPlayer(player);
                return s;
            });

            session.ticksRemaining--;

            // Update boss bar
            float progress = Math.max(0.0F, Math.min(1.0F, (float) session.ticksRemaining / SURVIVAL_DURATION_TICKS));
            session.bossBar.setProgress(progress);
            if (session.ticksRemaining % 20 == 0) {
                updateBossBarTitle(session);
            }

            // Maintain up to 5 worms across the desert if needed
            if (session.wormSpawnCooldown > 0) {
                session.wormSpawnCooldown--;
            } else {
                AABB checkArea = player.getBoundingBox().inflate(200.0D);
                int totalWorms = penaltyLevel.getEntitiesOfClass(GiantSandwormEntity.class, checkArea).size();
                if (totalWorms < 2 && session.wormsKilled < WORMS_REQUIRED_FOR_EARLY_ESCAPE) {
                    double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
                    double dist = 45.0D + player.getRandom().nextDouble() * 25.0D;
                    SystemData pData = SystemEvents.data(player);
                    spawnWormAtOffset(penaltyLevel, player, angle, dist, pData != null ? pData.level() : 1);
                    session.wormSpawnCooldown = 400; // 20s cooldown before checking again
                }
            }

            // Victory condition: Timer reached 0!
            if (session.ticksRemaining <= 0) {
                completePenaltyZone(player, false);
            }
        }
    }

    private static void updateBossBarTitle(PenaltySession session) {
        int seconds = Math.max(0, session.ticksRemaining / 20);
        int mins = seconds / 60;
        int secs = seconds % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);

        session.bossBar.setName(
                Component.translatable("awakening_system.penalty.bossbar", timeStr, session.wormsKilled, WORMS_REQUIRED_FOR_EARLY_ESCAPE)
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
        );
    }

    private static void spawnInitialWorms(ServerLevel level, ServerPlayer player) {
        // Clear any existing worms and orphaned parts in the area first
        AABB cleanupArea = player.getBoundingBox().inflate(400.0D);
        for (GiantSandwormEntity existing : level.getEntitiesOfClass(GiantSandwormEntity.class, cleanupArea)) {
            existing.discard();
        }
        for (GiantSandwormHeadPart existing : level.getEntitiesOfClass(GiantSandwormHeadPart.class, cleanupArea)) {
            existing.discard();
        }
        for (GiantSandwormBodyPart existing : level.getEntitiesOfClass(GiantSandwormBodyPart.class, cleanupArea)) {
            existing.discard();
        }

        double baseAngle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        SystemData pData = SystemEvents.data(player);
        int playerLevel = pData != null ? pData.level() : 1;

        // Deploy exactly 2 worms flanking the player within loaded distance (40 to 55 blocks away)
        for (int i = 0; i < 2; i++) {
            double angle = baseAngle + (i * Math.PI) + (player.getRandom().nextDouble() * 0.35D - 0.175D);
            double dist = 42.0D + (player.getRandom().nextDouble() * 10.0D);
            spawnWormAtOffset(level, player, angle, dist, playerLevel);
        }
    }

    private static void spawnWormAtOffset(ServerLevel level, ServerPlayer player, double angle, double dist, int playerLevel) {
        double spawnX = player.getX() + Math.cos(angle) * dist;
        double spawnZ = player.getZ() + Math.sin(angle) * dist;
        spawnSandwormAt(level, spawnX, spawnZ, playerLevel);
    }

    private static void spawnSandwormAt(ServerLevel level, double x, double z, int playerLevel) {
        int cx = ((int) x) >> 4;
        int cz = ((int) z) >> 4;
        level.getChunk(cx, cz);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
        if (surfaceY <= level.getMinBuildHeight() || surfaceY < 50) surfaceY = 70;

        GiantSandwormEntity worm = new GiantSandwormEntity(ModEntities.GIANT_SANDWORM.get(), level);
        worm.setPos(x, surfaceY - 4.0D, z);
        worm.setYRot(level.random.nextFloat() * 360.0F);
        worm.scaleToPlayerLevel(playerLevel);
        worm.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, surfaceY, z)),
                net.minecraft.world.entity.MobSpawnType.EVENT, null);
        level.addFreshEntity(worm);
    }

    public static void completePenaltyZone(ServerPlayer player, boolean killedBoss) {
        PenaltySession session = ACTIVE_SESSIONS.remove(player.getUUID());
        if (session != null && session.bossBar != null) {
            session.bossBar.removeAllPlayers();
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Rewards and Full Recovery
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);

        SystemData data = SystemEvents.data(player);
        data.clearDailyPenalty(server.overworld().getGameTime());

        // Title and fanfare
        Component subtitle = killedBoss ?
                Component.translatable("awakening_system.penalty.complete_killed") :
                Component.translatable("awakening_system.penalty.complete_survived");

        sendTitle(player,
                Component.translatable("awakening_system.penalty.complete_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                subtitle.copy().withStyle(ChatFormatting.GREEN),
                10, 100, 20);

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.5F, 1.0F);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.2F, 1.0F);

        // Teleport back
        ResourceKey<Level> returnDim = session != null ? session.previousDimension : Level.OVERWORLD;
        ServerLevel returnLevel = server.getLevel(returnDim);
        if (returnLevel == null) returnLevel = server.overworld();

        BlockPos targetPos = session != null && session.previousPos != null ? session.previousPos : returnLevel.getSharedSpawnPos();
        player.teleportTo(returnLevel, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
    }

    public static void failPenaltyZone(ServerPlayer player) {
        if (player == null) return;
        PenaltySession session = ACTIVE_SESSIONS.remove(player.getUUID());
        if (session != null && session.bossBar != null) {
            session.bossBar.removeAllPlayers();
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Restore health and immunity to prevent death loops
        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.fallDistance = 0.0F;
        player.setDeltaMovement(0.0, 0.0, 0.0);
        player.invulnerableTime = 80;

        // Exhaustion penalty debuffs
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 3600, 1)); // 3 mins Weakness II
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2400, 0)); // 2 mins Slowness I
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2400, 0)); // 2 mins Mining Fatigue
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 1)); // 1 min Hunger

        player.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.7F);
        player.playNotifySound(SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.8F);

        sendTitle(player,
                Component.translatable("awakening_system.penalty.failed_title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.translatable("awakening_system.penalty.failed_subtitle").withStyle(ChatFormatting.GRAY),
                10, 90, 20);

        player.sendSystemMessage(Component.translatable("awakening_system.penalty.failed_chat"));

        // Teleport back safely to previous location with all items intact
        ResourceKey<Level> returnDim = session != null ? session.previousDimension : Level.OVERWORLD;
        ServerLevel returnLevel = server.getLevel(returnDim);
        if (returnLevel == null) returnLevel = server.overworld();

        BlockPos targetPos = session != null && session.previousPos != null ? session.previousPos : returnLevel.getSharedSpawnPos();
        player.teleportTo(returnLevel, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
    }

    private static void sendTitle(ServerPlayer player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        PenaltySession session = ACTIVE_SESSIONS.remove(player.getUUID());
        if (session != null && session.bossBar != null) {
            session.bossBar.removeAllPlayers();
        }
    }
}
