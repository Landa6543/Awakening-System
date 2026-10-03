package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
import com.soybe.awakeningsystem.entity.AbyssalNecromancerEntity;
import com.soybe.awakeningsystem.entity.FrostElfAssassinEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.ShadowInfantryEntity;
import com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity;
import com.soybe.awakeningsystem.entity.IgrisBossEntity;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DungeonManager {
    private DungeonManager() {}

    public static final ResourceKey<Level> DUNGEON_REALM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dungeon_realm")
    );

    public static final class DungeonSession {
        public final UUID playerId;
        public final String rank;
        public final ResourceKey<Level> returnDimension;
        public final Vec3 returnPos;
        public final float returnYaw;
        public final float returnPitch;
        public final ResourceKey<Level> dungeonDimension;
        public final BlockPos arenaCenter;
        public final Set<UUID> enemyUUIDs = new HashSet<>();
        public int totalEnemies;
        public ServerBossEvent bossEvent;
        public BlockPos chestPos;
        public BlockPos gatePos;
        public boolean chestSpawned = false;
        public boolean chestClaimed = false;
        public int returnCountdown = -1;
        public int maxReturnCountdown = 1200;
        public int claimedTicks = 0;
        public boolean combatActive = true;
        public int combatTimer = 3600;
        public int maxCombatTimer = 3600;
        public final Set<BlockPos> arenaBlocks = new HashSet<>();

        public int currentFloor = 1;
        public int maxFloors = 3;
        public BlockPos descentPortalPos = null;
        public boolean floorCleared = false;

        public final Set<BlockPos> barrier1Blocks = new HashSet<>();
        public final Set<BlockPos> barrier2Blocks = new HashSet<>();
        public final Set<BlockPos> barrier3Blocks = new HashSet<>();
        public final Set<BlockPos> barrier4Blocks = new HashSet<>();
        public boolean barrier1Open = false;
        public boolean barrier2Open = false;
        public boolean barrier3Open = false;
        public boolean barrier4Open = false;
        public BlockPos barrier1Pos = null;
        public BlockPos barrier2Pos = null;
        public BlockPos barrier3Pos = null;
        public BlockPos barrier4Pos = null;
        public BlockPos room2Center = null;
        public BlockPos room3Center = null;
        public BlockPos room4Center = null;
        public BlockPos room5Center = null;
        public BlockPos thronePos = null;
        public WorldRift sourceRift = null;
        public long lastBarrierWarningTick = 0;

        // Ruleta Física en el Mundo (Estilo Crate de Servidores P2W)
        public boolean rouletteSpinning = false;
        public int rouletteTicks = 0;
        public ItemEntity rouletteItemEntity = null;
        public ItemStack finalWinningStack = ItemStack.EMPTY;
        public int finalCoinsWon = 0;
        public int finalExpWon = 0;
        public final List<ItemStack> finalRewardStacks = new ArrayList<>();

        public final String biome;

        public final boolean isRedGate;
        public BlockPos exitPortalPos = null;
        public long lastExitWarningTick = 0;
        public boolean lastEnemyNotified = false;
        public UUID lastEnemyUUID = null;

        public DungeonSession(UUID playerId, String rank, String biome, ResourceKey<Level> returnDimension,
                              Vec3 returnPos, float returnYaw, float returnPitch,
                              ResourceKey<Level> dungeonDimension, BlockPos arenaCenter, int maxFloors, boolean isRedGate) {
            this.playerId = playerId;
            this.rank = rank;
            this.biome = (biome == null || biome.isEmpty()) ? "crypt" : biome;
            this.returnDimension = returnDimension;
            this.returnPos = returnPos;
            this.returnYaw = returnYaw;
            this.returnPitch = returnPitch;
            this.dungeonDimension = dungeonDimension;
            this.arenaCenter = arenaCenter;
            this.maxFloors = maxFloors;
            this.isRedGate = isRedGate;

            // Generación Procedural 3D No Lineal de Cavernas:
            // Cada mazmorra presenta giros izquierda/derecha, pendientes y desniveles aleatorios
            java.util.Random rng = new java.util.Random(playerId.getLeastSignificantBits() ^ System.currentTimeMillis());
            int dX2 = rng.nextInt(15) - 7;
            int dY2 = rng.nextInt(7) - 2;
            int dZ2 = 38 + rng.nextInt(9) - 4;
            this.room2Center = arenaCenter.offset(dX2, dY2, dZ2);

            int dX3 = dX2 + (rng.nextBoolean() ? 1 : -1) * (6 + rng.nextInt(8));
            int dY3 = dY2 + (rng.nextInt(7) - 3);
            int dZ3 = dZ2 + 42 + rng.nextInt(9) - 4;
            this.room3Center = arenaCenter.offset(dX3, dY3, dZ3);

            int dX4 = dX3 + (rng.nextBoolean() ? 1 : -1) * (6 + rng.nextInt(8));
            int dY4 = dY3 + (rng.nextInt(7) - 3);
            int dZ4 = dZ3 + 46 + rng.nextInt(9) - 4;
            this.room4Center = arenaCenter.offset(dX4, dY4, dZ4);

            int dX5 = dX4 + (rng.nextInt(9) - 4);
            int dY5 = dY4 + (rng.nextInt(5) - 1);
            int dZ5 = dZ4 + 50 + rng.nextInt(7) - 3;
            this.room5Center = arenaCenter.offset(dX5, dY5, dZ5);
            this.thronePos = this.room5Center.offset(0, 2, 13);
            calculateBunkerBounds();
        }

        public int bunkerMinX = Integer.MIN_VALUE;
        public int bunkerMaxX = Integer.MAX_VALUE;
        public int bunkerMinY = Integer.MIN_VALUE;
        public int bunkerMaxY = Integer.MAX_VALUE;
        public int bunkerMinZ = Integer.MIN_VALUE;
        public int bunkerMaxZ = Integer.MAX_VALUE;
        public boolean bunkerBoundsCalculated = false;

        public void calculateBunkerBounds() {
            int minX = arenaCenter.getX(), maxX = arenaCenter.getX();
            int minY = arenaCenter.getY(), maxY = arenaCenter.getY();
            int minZ = arenaCenter.getZ() - 24, maxZ = arenaCenter.getZ() + 24;

            BlockPos[] rooms = new BlockPos[]{arenaCenter, room2Center, room3Center, room4Center, room5Center};
            for (BlockPos r : rooms) {
                if (r != null) {
                    minX = Math.min(minX, r.getX() - 24);
                    maxX = Math.max(maxX, r.getX() + 24);
                    minY = Math.min(minY, r.getY() - 8);
                    maxY = Math.max(maxY, r.getY() + 22);
                    minZ = Math.min(minZ, r.getZ() - 24);
                    maxZ = Math.max(maxZ, r.getZ() + 28);
                }
            }
            this.bunkerMinX = minX;
            this.bunkerMaxX = maxX;
            this.bunkerMinY = minY - 4;
            this.bunkerMaxY = maxY + 4;
            this.bunkerMinZ = minZ;
            this.bunkerMaxZ = maxZ;
            this.bunkerBoundsCalculated = true;
        }

        public DungeonSession(UUID playerId, String rank, String biome, ResourceKey<Level> returnDimension,
                              Vec3 returnPos, float returnYaw, float returnPitch,
                              ResourceKey<Level> dungeonDimension, BlockPos arenaCenter, int maxFloors) {
            this(playerId, rank, biome, returnDimension, returnPos, returnYaw, returnPitch, dungeonDimension, arenaCenter, maxFloors, false);
        }

        public DungeonSession(UUID playerId, String rank, ResourceKey<Level> returnDimension,
                              Vec3 returnPos, float returnYaw, float returnPitch,
                              ResourceKey<Level> dungeonDimension, BlockPos arenaCenter, int maxFloors) {
            this(playerId, rank, "crypt", returnDimension, returnPos, returnYaw, returnPitch, dungeonDimension, arenaCenter, maxFloors, false);
        }
    }

    public static final class WorldRift {
        public final BlockPos pos;
        public final String rank;
        public final String biome;
        public final ResourceKey<Level> dimension;
        public final long expireTick;
        public final boolean isRedGate;
        public final float yaw;

        public WorldRift(BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick, boolean isRedGate, float yaw) {
            this.pos = pos;
            this.rank = rank;
            this.biome = (biome == null || biome.isEmpty()) ? "crypt" : biome;
            this.dimension = dimension;
            this.expireTick = expireTick;
            this.isRedGate = isRedGate;
            this.yaw = yaw;
        }

        public WorldRift(BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick, boolean isRedGate) {
            this(pos, rank, biome, dimension, expireTick, isRedGate, (Math.abs(pos.getX() * 31 + pos.getZ()) % 2 == 0) ? 0.0F : 90.0F);
        }

        public WorldRift(BlockPos pos, String rank, String biome, ResourceKey<Level> dimension, long expireTick) {
            this(pos, rank, biome, dimension, expireTick, false);
        }

        public WorldRift(BlockPos pos, String rank, ResourceKey<Level> dimension, long expireTick) {
            this(pos, rank, "crypt", dimension, expireTick, false);
        }
    }

    private static final Map<UUID, DungeonSession> SESSIONS = new ConcurrentHashMap<>();
    private static final List<WorldRift> WORLD_RIFTS = new CopyOnWriteArrayList<>();
    public static final Map<UUID, Long> EXIT_COOLDOWNS = new ConcurrentHashMap<>();

    public static boolean isInDungeon(UUID playerId) {
        return SESSIONS.containsKey(playerId);
    }

    public static DungeonSession getSession(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    public static DungeonSession getSessionByChestPos(BlockPos pos) {
        for (DungeonSession session : SESSIONS.values()) {
            if (session.chestPos != null && session.chestPos.equals(pos)) {
                return session;
            }
        }
        return null;
    }

    public static DungeonSession getSessionAt(BlockPos pos) {
        if (pos == null) return null;
        for (DungeonSession session : SESSIONS.values()) {
            if (session.bunkerBoundsCalculated) {
                if (pos.getX() >= session.bunkerMinX && pos.getX() <= session.bunkerMaxX &&
                    pos.getZ() >= session.bunkerMinZ && pos.getZ() <= session.bunkerMaxZ) {
                    return session;
                }
            } else if (session.arenaCenter != null) {
                if (Math.abs(pos.getX() - session.arenaCenter.getX()) <= 200 &&
                    Math.abs(pos.getZ() - session.arenaCenter.getZ()) <= 400) {
                    return session;
                }
            }
        }
        return null;
    }

    public static int getRankBaseLevel(String rank) {
        int tier = getRankTier(rank);
        return switch (tier) {
            case 13 -> 150;
            case 12 -> 135;
            case 11 -> 120;
            case 10 -> 100;
            case 9  -> 90;
            case 8  -> 80;
            case 7  -> 70;
            case 6  -> 55;
            case 5  -> 40;
            case 4  -> 25;
            case 3  -> 15;
            case 2  -> 8;
            default -> 3;
        };
    }

    public static String getRankLevelColor(int level) {
        if (level < 20) return "§a";
        if (level < 40) return "§e";
        if (level < 60) return "§6";
        if (level < 80) return "§c";
        if (level < 100) return "§4§l";
        return "§d§l";
    }

    public static void applyDungeonMobMetadata(Monster mob, String rank, int floor, boolean isBoss) {
        if (mob == null) return;
        int baseLvl = getRankBaseLevel(rank);
        int mobLevel = isBoss ? (baseLvl + 6) : (baseLvl + Math.max(0, floor - 1) * 2);

        mob.addTag("dungeon_mob");
        mob.addTag("rpg_scaled");
        mob.getPersistentData().putInt("rpg_mob_level", mobLevel);
        mob.getPersistentData().putString("dungeon_rank", rank != null ? rank : "D");
        mob.getPersistentData().putInt("dungeon_floor", floor);

        String color = getRankLevelColor(mobLevel);
        Component currentName = mob.getCustomName();
        if (currentName == null) {
            currentName = mob.getType().getDescription();
        }
        String nameStr = currentName.getString();
        if (!nameStr.contains("[Nv.")) {
            Component formattedName = Component.literal(color + "[Nv. " + mobLevel + "] §r").append(currentName);
            mob.setCustomName(formattedName);
        }
        mob.setCustomNameVisible(true);
    }

    public static BlockPos getCurrentRoomCenter(DungeonSession session) {
        if (session == null) return null;
        return switch (session.currentFloor) {
            case 2 -> session.room2Center != null ? session.room2Center : session.arenaCenter;
            case 3 -> session.room3Center != null ? session.room3Center : session.arenaCenter;
            case 4 -> session.room4Center != null ? session.room4Center : session.arenaCenter;
            case 5 -> session.room5Center != null ? session.room5Center : session.arenaCenter;
            default -> session.arenaCenter;
        };
    }

    public static boolean isPlayerOutOfBounds(DungeonSession session, ServerPlayer player) {
        if (session == null || player == null) return false;
        if (!session.bunkerBoundsCalculated) {
            session.calculateBunkerBounds();
        }
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        if (y < session.bunkerMinY - 2.0 || y < -50.0) return true;
        if (x <= session.bunkerMinX + 1.0 || x >= session.bunkerMaxX - 1.0) return true;
        if (z <= session.bunkerMinZ + 1.0 || z >= session.bunkerMaxZ - 1.0) return true;
        if (y >= session.bunkerMaxY + 2.0) return true;

        BlockPos headPos = BlockPos.containing(x, y + 1.0, z);
        BlockPos feetPos = BlockPos.containing(x, y, z);
        if (player.level().getBlockState(headPos).is(Blocks.BEDROCK) || player.level().getBlockState(feetPos).is(Blocks.BEDROCK)) {
            return true;
        }

        return false;
    }

    public static boolean isTargetOutOfBounds(DungeonSession session, double x, double y, double z) {
        if (session == null) return false;
        if (!session.bunkerBoundsCalculated) {
            session.calculateBunkerBounds();
        }
        if (y < session.bunkerMinY - 2.0 || y > session.bunkerMaxY + 2.0) return true;
        if (x <= session.bunkerMinX + 1.5 || x >= session.bunkerMaxX - 1.5) return true;
        if (z <= session.bunkerMinZ + 1.5 || z >= session.bunkerMaxZ - 1.5) return true;
        return false;
    }

    public static void rescuePlayerToBounds(DungeonSession session, ServerPlayer player) {
        if (session == null || player == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(session.dungeonDimension);
        if (level == null) level = player.serverLevel();

        BlockPos center = getCurrentRoomCenter(session);
        if (center == null) center = session.arenaCenter;

        double safeY = findSafeFloorY(level, center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D);
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0.0F;
        player.teleportTo(level, center.getX() + 0.5D, safeY, center.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.dungeon.out_of_bounds_rescued"), true);
    }

    public static void rescueStrandedPlayer(ServerPlayer player) {
        if (player == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ResourceKey<Level> respawnDim = player.getRespawnDimension();
        ServerLevel returnLevel = (respawnDim != null && !respawnDim.equals(DUNGEON_REALM) && !respawnDim.equals(TowerManager.RANKER_TOWER) && !respawnDim.equals(PagodaTrialManager.PAGODA_REALM))
                ? server.getLevel(respawnDim) : server.overworld();
        if (returnLevel == null) returnLevel = server.overworld();

        BlockPos respawnPos = player.getRespawnPosition();
        Vec3 targetVec;
        float yaw = player.getYRot();
        if (respawnPos != null) {
            targetVec = new Vec3(respawnPos.getX() + 0.5D, respawnPos.getY() + 0.1D, respawnPos.getZ() + 0.5D);
        } else {
            BlockPos shared = returnLevel.getSharedSpawnPos();
            targetVec = new Vec3(shared.getX() + 0.5D, shared.getY() + 0.1D, shared.getZ() + 0.5D);
        }

        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0.0F;
        player.teleportTo(returnLevel, targetVec.x, targetVec.y, targetVec.z, yaw, 0.0F);
        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.9F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.dungeon.stranded_rescued"), false);
    }

    public static void onPlayerLogin(ServerPlayer player) {
        if (player.level().dimension().equals(DUNGEON_REALM)) {
            if (!isInDungeon(player.getUUID()) && !player.isSpectator()) {
                rescueStrandedPlayer(player);
            }
        }
    }

    public static int getRankTier(String rank) {
        if (rank == null || rank.isEmpty()) return 3;
        return switch (rank.toUpperCase(java.util.Locale.ROOT)) {
            case "ABSOLUTE_GOD", "LIMITLESS", "SINGULARITY", "ASTRAL_DEITY", "TIMELESS_MONARCH", "CHAOS_SOVEREIGN" -> 13;
            case "ABSOLUTE_CREATOR", "SUPREME_CREATOR", "ABSOLUTE", "ABSOLUTE_BEING", "OMNIPOTENT", "INFINITE" -> 12;
            case "PRIMORDIAL", "OMNIVERSAL", "MULTIVERSAL", "COSMIC", "ANCESTRAL", "DEITY", "GOD", "DEMIGOD" -> 11;
            case "SUPREME_BEING", "RULER", "MONARCH" -> 10;
            case "TRANSCENDENTAL", "TRANSCENDENT", "GR", "MR", "SLR", "LR", "UR+", "UR", "NATIONAL" -> 9;
            case "SSS", "SS", "EX" -> 8;
            case "S" -> 7;
            case "A" -> 6;
            case "B" -> 5;
            case "C" -> 4;
            case "D" -> 3;
            case "E" -> 2;
            default -> 1;
        };
    }

    public static BossEvent.BossBarColor getGateBossBarColor(String rank, boolean isRedGate) {
        if (isRedGate) return BossEvent.BossBarColor.RED;
        int tier = getRankTier(rank);
        if (tier >= 12) return BossEvent.BossBarColor.YELLOW;
        if (tier >= 10) return BossEvent.BossBarColor.PURPLE;
        if (tier >= 7) return BossEvent.BossBarColor.RED;
        if (tier == 6) return BossEvent.BossBarColor.YELLOW;
        if (tier >= 4) return BossEvent.BossBarColor.GREEN;
        return BossEvent.BossBarColor.BLUE;
    }

    public static Component getGatePrefix(String rank, boolean isRedGate) {
        if (isRedGate) {
            return Component.translatable("awakening_system.dungeon.gate.red_gate_prefix", rank);
        }
        int tier = getRankTier(rank);
        if (tier >= 12) return Component.translatable("awakening_system.dungeon.gate.celestial_prefix", rank);
        if (tier >= 10) return Component.translatable("awakening_system.dungeon.gate.divine_prefix", rank);
        if (tier >= 8) return Component.translatable("awakening_system.dungeon.gate.abyssal_prefix", rank);
        if (tier == 7) return Component.translatable("awakening_system.dungeon.gate.crimson_prefix", rank);
        if (tier == 6) return Component.translatable("awakening_system.dungeon.gate.amber_prefix", rank);
        if (tier >= 4) return Component.translatable("awakening_system.dungeon.gate.emerald_prefix", rank);
        return Component.translatable("awakening_system.dungeon.gate.default_prefix", rank);
    }

    public static Component getGateTitle(String rank, boolean isRedGate) {
        if (isRedGate) {
            return Component.translatable("awakening_system.dungeon.gate.title.red_gate", rank);
        }
        int tier = getRankTier(rank);
        if (tier >= 12) return Component.translatable("awakening_system.dungeon.gate.title.celestial", rank);
        if (tier >= 10) return Component.translatable("awakening_system.dungeon.gate.title.divine", rank);
        if (tier >= 8) return Component.translatable("awakening_system.dungeon.gate.title.abyssal", rank);
        if (tier == 7) return Component.translatable("awakening_system.dungeon.gate.title.crimson", rank);
        if (tier == 6) return Component.translatable("awakening_system.dungeon.gate.title.amber", rank);
        if (tier >= 4) return Component.translatable("awakening_system.dungeon.gate.title.emerald", rank);
        return Component.translatable("awakening_system.dungeon.gate.title.default", rank);
    }

    public static Component formatPhaseBossName(String rank, boolean isRedGate, int floor, int enemies, String timeStr) {
        if (floor == 5) {
            return getGatePrefix(rank, isRedGate).copy()
                    .append(Component.literal("§4§l"))
                    .append(Component.translatable("awakening_system.dungeon.phase.5"))
                    .append(Component.literal(" §8| §c⏳ " + timeStr));
        }
        return getGatePrefix(rank, isRedGate).copy()
                .append(Component.literal("§e"))
                .append(Component.translatable("awakening_system.dungeon.phase." + floor))
                .append(Component.literal(" §7"))
                .append(Component.translatable("awakening_system.dungeon.phase.enemies_format", enemies))
                .append(Component.literal(" §8| §c⏳ " + timeStr));
    }

    public static int getPhase5BossTimer(int tier, boolean isRedGate) {
        int baseTicks = switch (tier) {
            case 13 -> 12000; // 10 min (600s) - Para jefes supremos de ~25.000-33.000 HP (Absolute God, Singularity, etc.)
            case 12 -> 11400; // 9.5 min (570s) - Supreme Creator, etc.
            case 11 -> 10800; // 9 min (540s) - Monarch, Ruler, etc.
            case 10 -> 10200; // 8.5 min (510s) - National, Deity, etc.
            case 9  -> 9600;  // 8 min (480s) - SSS
            case 8  -> 9000;  // 7.5 min (450s) - SS, EX
            case 7  -> 8400;  // 7 min (420s) - S
            case 6  -> 7800;  // 6.5 min (390s) - A
            case 5  -> 7200;  // 6 min (360s) - B
            case 4  -> 6600;  // 5.5 min (330s) - C
            case 3  -> 6000;  // 5 min (300s) - D
            case 2  -> 5400;  // 4.5 min (270s) - E
            default -> 4800;  // 4 min (240s) - F
        };
        return isRedGate ? (int) (baseTicks * 1.2) : baseTicks; // +20% tiempo en Red Gates debido al +35% de vida
    }

    public static int getFloorTimer(String rank, int floor) {
        int tier = getRankTier(rank);
        if (floor >= 5) {
            return getPhase5BossTimer(tier, false);
        }
        int base = (floor == 2) ? 1800 : (floor == 3 ? 2400 : (floor == 4 ? 3000 : 1800));
        if (tier >= 9) { // SSS, NATIONAL, MONARCH, GOD
            base += (tier - 8) * 600;
        }
        return base;
    }

    public static void startDungeon(ServerPlayer player, String rank) {
        startDungeon(player, rank, "crypt", false, null);
    }

    public static void startDungeon(ServerPlayer player, String rank, String biome) {
        startDungeon(player, rank, biome, false, null);
    }

    public static void startDungeon(ServerPlayer player, String rank, String biome, boolean isRedGate) {
        startDungeon(player, rank, biome, isRedGate, null);
    }

    public static void startDungeon(ServerPlayer player, String rank, String biome, boolean isRedGate, WorldRift sourceRift) {
        if (isInDungeon(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.dungeon.already_in_dungeon"), true);
            return;
        }

        String r = (rank == null || rank.isEmpty()) ? "D" : rank.toUpperCase();
        String b = (biome == null || biome.isEmpty()) ? "crypt" : biome;
        int tier = getRankTier(r);

        // 15% de probabilidad de mutación a Red Gate en rangos B en adelante
        if (!isRedGate && tier >= 5 && player.getRandom().nextFloat() < 0.15F) {
            isRedGate = true;
            AwakeningSystemMod.notify(player, "awakening_system.notification.red_gate.title", "awakening_system.notification.red_gate.message", 0xFFFF0000);
            player.displayClientMessage(Component.translatable("awakening_system.notification.red_gate.message"), true);
            player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 0.6F);
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel dungeonLevel = server.getLevel(DUNGEON_REALM);
        if (dungeonLevel == null) {
            dungeonLevel = server.getLevel(Level.NETHER);
            if (dungeonLevel == null) dungeonLevel = server.getLevel(Level.OVERWORLD);
        }
        if (dungeonLevel == null) return;
        ResourceKey<Level> dimKey = dungeonLevel.dimension();

        int runId = DungeonWorldSavedData.get(server).getNextRunIndexAndIncrement();
        int gridCol = runId % 1000;
        int gridRow = runId / 1000;
        int arenaX = 10000 + gridCol * 1000;
        int arenaY = 60;
        int arenaZ = 10000 + gridRow * 1000;
        BlockPos center = new BlockPos(arenaX, arenaY, arenaZ);

        // Purgar de forma exhaustiva cualquier entidad o residuo previo en este volumen antes de construir
        purgeDungeonVolume(dungeonLevel, center);

        int maxFloors = 5;

        DungeonSession session = new DungeonSession(
                player.getUUID(),
                r,
                b,
                player.level().dimension(),
                player.position(),
                player.getYRot(),
                player.getXRot(),
                dimKey,
                center,
                maxFloors,
                isRedGate
        );
        session.sourceRift = sourceRift;

        String theme = (b == null || b.isEmpty()) ? "" : b.toLowerCase(java.util.Locale.ROOT);
        if (theme.isEmpty() || "random".equals(theme)) {
            if (isRedGate) {
                theme = "igris";
            } else if (tier >= 10) {
                theme = (player.getRandom().nextBoolean()) ? "baran" : "igris";
            } else if (tier >= 7) {
                theme = "igris";
            } else {
                theme = "crypt";
            }
        }

        // 1. Construir la Mazmorra Continua de 5 Cavernas Procedurales con Murallas Mágicas
        buildContinuousDungeon(dungeonLevel, session, center, r, theme);

        // 2. Plataforma segura de inicio en Sala 1 (Antecámara) y Santuario del Portal de Retorno
        BlockPos spawnPos = center.offset(0, 1, -8);
        ensureSolidSpawnPlatform(dungeonLevel, session, spawnPos);
        BlockPos exitFloorPos = spawnPos.offset(0, 0, -4);
        session.exitPortalPos = exitFloorPos;
        buildExitPortal(dungeonLevel, session, exitFloorPos, isRedGate);

        // 3. Teletransportar y resetear inercia de caída
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0.0F;
        player.teleportTo(dungeonLevel, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);

        // 4. Efectos de sonido y partículas
        dungeonLevel.playSound(null, center.getX(), center.getY() + 1, center.getZ(),
                ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.PLAYERS, 1.3F, 1.0F);
        dungeonLevel.sendParticles(ParticleTypes.PORTAL, center.getX() + 0.5D, center.getY() + 1.5D, center.getZ() + 0.5D,
                120, 3.0D, 1.5D, 3.0D, 0.5D);

        // 5. Invocar ÚNICAMENTE a los guardianes de la Fase 1 en Sala 1 (Salas 2, 3, 4 y 5 quedan 100% vacías)
        spawnPhase1Enemies(dungeonLevel, session);

        // 6. BossBar y Temporizador de Combate escalado por rango
        session.combatTimer = getFloorTimer(r, 1);
        session.maxCombatTimer = session.combatTimer;
        session.combatActive = true;

        int secRemaining = (session.combatTimer + 19) / 20;
        int mins = secRemaining / 60;
        int secs = secRemaining % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);

        ServerBossEvent bossEvent = new ServerBossEvent(
                formatPhaseBossName(r, isRedGate, 1, session.totalEnemies, timeStr),
                getGateBossBarColor(r, isRedGate),
                BossEvent.BossBarOverlay.PROGRESS
        );
        bossEvent.setProgress(1.0F);
        bossEvent.addPlayer(player);
        session.bossEvent = bossEvent;

        SESSIONS.put(player.getUUID(), session);

        int notifyCol = isRedGate ? 0xFFFF3333 : (tier >= 12 ? 0xFFFFD700 : (tier >= 10 ? 0xFFC755FF : 0xFF70E8FF));
        AwakeningSystemMod.notify(player, "awakening_system.notification.dungeon_gate.title",
                "awakening_system.notification.dungeon_gate.message", notifyCol, r);
        SystemEvents.triggerConstellationReaction(player, ConstellationDefinitions.ReactionType.DUNGEON_ENTER);
        syncRiftsToPlayer(player);
    }

    private static void ensureSolidSpawnPlatform(ServerLevel level, DungeonSession session, BlockPos spawnPos) {
        for (int sx = -2; sx <= 2; sx++) {
            for (int sz = -2; sz <= 2; sz++) {
                for (int sy = -3; sy <= -1; sy++) {
                    BlockPos bp = spawnPos.offset(sx, sy, sz);
                    level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    session.arenaBlocks.add(bp);
                }
                level.setBlock(spawnPos.offset(sx, 0, sz), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(spawnPos.offset(sx, 1, sz), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(spawnPos.offset(sx, 2, sz), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    public static void buildExitPortal(ServerLevel level, DungeonSession session, BlockPos portalPos, boolean isRedGate) {
        String theme = (session.biome != null) ? session.biome.toLowerCase(java.util.Locale.ROOT) : "crypt";
        boolean isBaran = "baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme);

        BlockState baseBlock = isBaran ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState pillarBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState wallBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState() : Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState();
        BlockState stairsBlock = isBaran ? Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState();
        BlockState floorTile = isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState runeBlock = isRedGate ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : (isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState());

        // 1. Pedestal / Tarima ceremonial escalonada (5x5, dy = 0) que se funde con el suelo natural
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Cimiento sólido debajo
                for (int dy = -3; dy <= -1; dy++) {
                    BlockPos p = portalPos.offset(dx, dy, dz);
                    level.setBlock(p, pillarBlock, 2);
                    session.arenaBlocks.add(p);
                }
                BlockPos floorP = portalPos.offset(dx, 0, dz);
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    level.setBlock(floorP, floorTile, 2);
                } else {
                    level.setBlock(floorP, stairsBlock, 2);
                }
                session.arenaBlocks.add(floorP);
            }
        }

        // 2. Pilares del Arco Monumental (dx = -2 y dx = 2, dy = 1 a 5)
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos leftCol = portalPos.offset(-2, dy, 0);
            BlockPos rightCol = portalPos.offset(2, dy, 0);
            BlockState st = (dy == 1 || dy == 5) ? baseBlock : wallBlock;
            level.setBlock(leftCol, st, 2);
            level.setBlock(rightCol, st, 2);
            session.arenaBlocks.add(leftCol);
            session.arenaBlocks.add(rightCol);
        }

        // Linternas de almas mágicas en los pilares (dy = 3, dz = 1)
        BlockPos lightL = portalPos.offset(-2, 3, 1);
        BlockPos lightR = portalPos.offset(2, 3, 1);
        level.setBlock(lightL, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(lightR, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        session.arenaBlocks.add(lightL);
        session.arenaBlocks.add(lightR);

        // 3. Corona del Arco Monumental (dy = 6)
        BlockPos archL = portalPos.offset(-1, 6, 0);
        BlockPos archM = portalPos.offset(0, 6, 0);
        BlockPos archR = portalPos.offset(1, 6, 0);
        level.setBlock(archL, stairsBlock, 2);
        level.setBlock(archM, runeBlock, 2); // Piedra angular rúnica (Crying Obsidian / Gilded Blackstone)
        level.setBlock(archR, stairsBlock, 2);
        session.arenaBlocks.add(archL);
        session.arenaBlocks.add(archM);
        session.arenaBlocks.add(archR);

        // 4. Nicho / Hornacina rúnica detrás del portal (dz = -1) que se integra en la roca
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                BlockPos nicheP = portalPos.offset(dx, dy, -1);
                level.setBlock(nicheP, runeBlock, 2);
                session.arenaBlocks.add(nicheP);
            }
        }

        // 5. Despeje de aire del portal (dz = 0 a 3, dx = -1 a 1, dy = 1 a 5)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 3; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    BlockPos airP = portalPos.offset(dx, dy, dz);
                    level.setBlock(airP, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    public static double getDungeonBossHp(int tier, boolean isRedGate) {
        double baseHp = switch (tier) {
            case 13 -> 22000.0D; // ABSOLUTE_GOD, LIMITLESS, SINGULARITY
            case 12 -> 16000.0D; // SUPREME_CREATOR, CHAOS_SOVEREIGN, ASTRAL_DEITY
            case 11 -> 12000.0D; // MONARCH, RULER, ABSOLUTE
            case 10 -> 9000.0D;  // NATIONAL / COSMIC / DEITY
            case 9  -> 6800.0D;  // SSS
            case 8  -> 5000.0D;  // SS / EX
            case 7  -> 3500.0D;  // S
            case 6  -> 2200.0D;  // A
            case 5  -> 1400.0D;  // B
            case 4  -> 850.0D;   // C
            case 3  -> 500.0D;   // D
            case 2  -> 300.0D;   // E
            default -> 180.0D;   // F
        };
        return isRedGate ? baseHp * 1.35D : baseHp;
    }

    public static double getDungeonBossDmg(int tier, boolean isRedGate) {
        double baseDmg = switch (tier) {
            case 13 -> 80.0D;
            case 12 -> 68.0D;
            case 11 -> 56.0D;
            case 10 -> 46.0D;
            case 9  -> 38.0D;
            case 8  -> 32.0D;
            case 7  -> 26.0D;
            case 6  -> 20.0D;
            case 5  -> 15.0D;
            case 4  -> 11.0D;
            case 3  -> 8.0D;
            case 2  -> 5.5D;
            default -> 4.0D;
        };
        return isRedGate ? baseDmg * 1.25D : baseDmg;
    }

    private static Monster createFinalBoss(ServerLevel level, String rank) {
        return createFinalBoss(level, rank, false);
    }

    private static Monster createFinalBoss(ServerLevel level, String rank, boolean isRedGate) {
        int tier = getRankTier(rank);
        if (tier >= 7) { // S, SS, SSS, NATIONAL, MONARCH, CREATOR, GOD, etc.
            var boss = new ShadowMonarchBossEntity(ModEntities.SHADOW_MONARCH_BOSS.get(), level);
            int variant = level.random.nextInt(2);
            boss.setVariant(variant);
            double hp = getDungeonBossHp(tier, isRedGate);
            double dmg = getDungeonBossDmg(tier, isRedGate);
            double armor = Math.min(30.0D, 16.0D + (tier - 7) * 2.5D);
            double kbRes = 0.9D + Math.min(0.095D, (tier - 7) * 0.02D);

            Component prefixComp = isRedGate ? Component.translatable("awakening_system.dungeon.mob.anomaly_prefix", rank) :
                    (tier >= 13 ? Component.translatable("awakening_system.dungeon.mob.godslayer_prefix", rank) :
                    (tier == 12 ? Component.translatable("awakening_system.dungeon.mob.divine_boss_prefix", rank) : Component.translatable("awakening_system.dungeon.mob.boss_prefix", rank)));
            Component nameComp = (tier >= 13) ? (variant == 1 ? Component.translatable("awakening_system.dungeon.mob.ashborn_supreme") : Component.translatable("awakening_system.dungeon.mob.antares_supreme")) :
                    ((tier == 12) ? (variant == 1 ? Component.translatable("awakening_system.dungeon.mob.ashborn_primordial") : Component.translatable("awakening_system.dungeon.mob.antares_chaos")) :
                    (variant == 1 ? Component.translatable("awakening_system.dungeon.mob.ashborn_monarch") : Component.translatable("awakening_system.dungeon.mob.antares_monarch")));

            boss.setCustomName(prefixComp.copy().append(nameComp));
            boss.setCustomNameVisible(true);
            buffMob(boss, hp, dmg, armor, kbRes);
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, tier >= 12 ? 3 : (tier >= 9 ? 2 : 1)));
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, tier >= 12 ? 3 : (tier >= 10 ? 2 : 1)));
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, tier >= 12 ? 2 : 1));
            boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 99999, 0));
            boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 99999, 0));
            return boss;
        } else if (tier == 6) { // A
            var boss = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
            boss.setCustomName(Component.translatable(isRedGate ? "awakening_system.dungeon.mob.anomaly_prefix" : "awakening_system.dungeon.mob.boss_prefix", "A").append(Component.translatable("awakening_system.dungeon.mob.orc_warlord")));
            boss.setCustomNameVisible(true);
            buffMob(boss, getDungeonBossHp(tier, isRedGate), getDungeonBossDmg(tier, isRedGate), 16.0D, 0.75D);
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, 0));
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 1));
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 0));
            boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 99999, 0));
            return boss;
        } else if (tier == 5) { // B
            var boss = new ShadowMonarchBossEntity(ModEntities.SHADOW_MONARCH_BOSS.get(), level);
            int variant = level.random.nextInt(2);
            boss.setVariant(variant);
            boss.setCustomName(Component.translatable(isRedGate ? "awakening_system.dungeon.mob.anomaly_prefix" : "awakening_system.dungeon.mob.boss_prefix", "B").append(Component.translatable(variant == 1 ? "awakening_system.dungeon.mob.vestige_ashborn" : "awakening_system.dungeon.mob.vestige_antares")));
            boss.setCustomNameVisible(true);
            buffMob(boss, getDungeonBossHp(tier, isRedGate), getDungeonBossDmg(tier, isRedGate), 14.0D, 0.6D);
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0));
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 0));
            return boss;
        } else if (tier == 4) { // C
            var boss = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
            boss.setCustomName(Component.translatable(isRedGate ? "awakening_system.dungeon.mob.anomaly_prefix" : "awakening_system.dungeon.mob.boss_prefix", "C").append(Component.translatable("awakening_system.dungeon.mob.devastating_blood_orc")));
            boss.setCustomNameVisible(true);
            buffMob(boss, getDungeonBossHp(tier, isRedGate), getDungeonBossDmg(tier, isRedGate), 10.0D, 0.45D);
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0));
            return boss;
        } else if (tier == 3) { // D
            var boss = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            boss.setCustomName(Component.translatable(isRedGate ? "awakening_system.dungeon.mob.anomaly_prefix" : "awakening_system.dungeon.mob.boss_prefix", "D").append(Component.translatable("awakening_system.dungeon.mob.shadow_infantry_champion")));
            boss.setCustomNameVisible(true);
            buffMob(boss, getDungeonBossHp(tier, isRedGate), getDungeonBossDmg(tier, isRedGate), 6.0D, 0.35D);
            return boss;
        } else { // E, F
            var boss = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            boss.setCustomName(Component.translatable(isRedGate ? "awakening_system.dungeon.mob.anomaly_prefix" : "awakening_system.dungeon.mob.boss_prefix", rank).append(Component.translatable("awakening_system.dungeon.mob.renegade_shadow_infantry")));
            boss.setCustomNameVisible(true);
            buffMob(boss, getDungeonBossHp(tier, isRedGate), getDungeonBossDmg(tier, isRedGate), 3.0D, 0.20D);
            return boss;
        }
    }

    private static Monster createFloorMinion(ServerLevel level, String rank, int floor, int index) {
        if (floor == 2 && index == 0) {
            return createFloor2Elite(level, rank);
        }
        int tier = getRankTier(rank);

        // Escalado de dificultad según rango del Gate
        double hpMult = 1.0D + Math.max(0, tier - 3) * 0.35D;
        double dmgMult = 1.0D + Math.max(0, tier - 3) * 0.28D;

        if (tier >= 7) { // Rango S, SS, SSS, NATIONAL, MONARCH, DIVINE
            hpMult = 1.0D + (tier - 7) * 0.50D;
            dmgMult = 1.0D + (tier - 7) * 0.38D;

            if (floor == 1) {
                // FASE 1: Exclusivo de Infantería Sombría de Choque y Caballeros Carmesí
                if (index % 2 == 0) {
                    var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                    infantry.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.deicide_shadow_infantry" : "awakening_system.dungeon.mob.vanguard_shadow_infantry"));
                    buffMob(infantry, 220.0D * hpMult, 14.0D * dmgMult, Math.min(22.0D, 10.0D + (tier - 7) * 1.5D), 0.45D);
                    return infantry;
                } else {
                    var w = new WitherSkeleton(EntityType.WITHER_SKELETON, level);
                    w.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.abyssal_blood_knight" : "awakening_system.dungeon.mob.vanguard_crimson_knight"));
                    w.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    buffMob(w, 200.0D * hpMult, 13.0D * dmgMult, Math.min(20.0D, 9.0D + (tier - 7) * 1.5D), 0.35D);
                    return w;
                }
            } else if (floor == 2) {
                // FASE 2: Exclusivo de Asesinos Elfos de la Escarcha y Acechadores
                if (index % 2 == 0) {
                    var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                    assassin.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.supreme_spectral_elf_assassin" : "awakening_system.dungeon.mob.frost_elf_assassin"));
                    buffMob(assassin, 240.0D * hpMult, 19.0D * dmgMult, Math.min(22.0D, 12.0D + (tier - 7) * 1.5D), 0.50D);
                    return assassin;
                } else {
                    var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                    infantry.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.void_shadow_stalker" : "awakening_system.dungeon.mob.shadow_stalker"));
                    buffMob(infantry, 230.0D * hpMult, 15.0D * dmgMult, Math.min(20.0D, 10.0D + (tier - 7) * 1.5D), 0.45D);
                    return infantry;
                }
            } else if (floor == 3) {
                // FASE 3: Exclusivo de Nigromantes Oscuros y Guerreros Orcos del Abismo
                if (index % 2 == 0) {
                    var necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
                    necro.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.dark_celestial_necromancer" : "awakening_system.dungeon.mob.dark_abyssal_necromancer"));
                    buffMob(necro, 270.0D * hpMult, 16.0D * dmgMult, Math.min(22.0D, 12.0D + (tier - 7) * 1.5D), 0.50D);
                    return necro;
                } else {
                    var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                    orc.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.deicide_orc_warrior" : "awakening_system.dungeon.mob.abyssal_orc_warrior"));
                    buffMob(orc, 350.0D * hpMult, 18.0D * dmgMult, Math.min(24.0D, 14.0D + (tier - 7) * 1.5D), 0.60D);
                    return orc;
                }
            } else {
                // FASE 4: Exclusivo de Soldados Hormiga Quimérica y Ojos Vigilantes del Vacío (Pre-Jefe)
                if (index % 2 == 0) {
                    var ant = new com.soybe.awakeningsystem.entity.AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
                    ant.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.deicide_chimera_ant" : "awakening_system.dungeon.mob.black_swarm_ant"));
                    buffMob(ant, 300.0D * hpMult, 19.0D * dmgMult, Math.min(25.0D, 14.0D + (tier - 7) * 1.5D), 0.55D);
                    return ant;
                } else {
                    var beholder = new com.soybe.awakeningsystem.entity.AbyssalBeholderEntity(ModEntities.ABYSSAL_BEHOLDER.get(), level);
                    beholder.setCustomName(Component.translatable(tier >= 12 ? "awakening_system.dungeon.mob.deicide_void_eye" : "awakening_system.dungeon.mob.primordial_abyssal_eye"));
                    buffMob(beholder, 280.0D * hpMult, 18.0D * dmgMult, Math.min(23.0D, 13.0D + (tier - 7) * 1.5D), 0.70D);
                    return beholder;
                }
            }
        } else if (tier == 6) { // Rango A
            if (floor == 1) {
                var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.vanguard_shadow_infantry"));
                buffMob(infantry, 160.0D, 12.0D, 8.0D, 0.35D);
                return infantry;
            } else if (floor == 2) {
                var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.glacial_assassin"));
                buffMob(assassin, 190.0D, 15.0D, 10.0D, 0.40D);
                return assassin;
            } else if (floor == 3) {
                var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.bloody_orc_brute"));
                buffMob(orc, 250.0D, 16.0D, 12.0D, 0.45D);
                return orc;
            } else {
                if (index % 2 == 0) {
                    var ant = new com.soybe.awakeningsystem.entity.AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
                    ant.setCustomName(Component.translatable("awakening_system.dungeon.mob.jeju_chimera_ant"));
                    buffMob(ant, 220.0D, 15.0D, 11.0D, 0.40D);
                    return ant;
                } else {
                    var beholder = new com.soybe.awakeningsystem.entity.AbyssalBeholderEntity(ModEntities.ABYSSAL_BEHOLDER.get(), level);
                    beholder.setCustomName(Component.translatable("awakening_system.dungeon.mob.abyssal_watcher_eye"));
                    buffMob(beholder, 200.0D, 14.0D, 10.0D, 0.65D);
                    return beholder;
                }
            }
        } else if (tier == 5) { // Rango B
            if (floor == 1) {
                var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_soldier"));
                buffMob(infantry, 130.0D, 9.0D, 6.0D, 0.30D);
                return infantry;
            } else if (floor == 2) {
                var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.frost_stalker"));
                buffMob(assassin, 140.0D, 11.0D, 7.0D, 0.35D);
                return assassin;
            } else if (floor == 3) {
                var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.abyssal_orc"));
                buffMob(orc, 160.0D, 11.0D, 8.0D, 0.35D);
                return orc;
            } else {
                var necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
                necro.setCustomName(Component.translatable("awakening_system.dungeon.mob.abyssal_summoner"));
                buffMob(necro, 170.0D, 11.0D, 8.0D, 0.35D);
                return necro;
            }
        } else if (tier == 4) { // Rango C
            if (floor <= 2) {
                var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.lesser_shadow_infantry"));
                buffMob(infantry, 90.0D, 7.0D, 5.0D, 0.25D);
                return infantry;
            } else {
                var husk = new Husk(EntityType.HUSK, level);
                husk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                buffMob(husk, 80.0D, 6.0D, 4.0D, 0.2D);
                return husk;
            }
        } else if (tier == 3) { // Rango D
            if (floor <= 2) {
                var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_wraith"));
                buffMob(infantry, 60.0D, 5.0D, 3.0D, 0.15D);
                return infantry;
            } else {
                var zombie = new Zombie(EntityType.ZOMBIE, level);
                zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                buffMob(zombie, 50.0D, 4.0D, 2.0D, 0.1D);
                return zombie;
            }
        } else { // E, F
            if (floor <= 2) {
                var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.lesser_shadow"));
                buffMob(infantry, 35.0D, 3.0D, 1.0D, 0.0D);
                return infantry;
            } else {
                var zombie = new Zombie(EntityType.ZOMBIE, level);
                zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
                buffMob(zombie, 30.0D, 2.5D, 0.0D, 0.0D);
                return zombie;
            }
        }
    }

    private static Monster createFloor2Elite(ServerLevel level, String rank) {
        int tier = getRankTier(rank);
        if (tier >= 7) {
            double hpMult = 1.0D + (tier - 7) * 0.55D;
            double dmgMult = 1.0D + (tier - 7) * 0.38D;
            var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
            Component prefix = (tier >= 12) ? Component.translatable("awakening_system.dungeon.mob.deicide_elite_prefix", rank) :
                    Component.translatable("awakening_system.dungeon.mob.elite_prefix", rank);
            Component name = (tier >= 12) ? Component.translatable("awakening_system.dungeon.mob.orc_titan_doomsday") :
                    Component.translatable("awakening_system.dungeon.mob.orc_overlord_abyssal");
            orc.setCustomName(prefix.copy().append(name));
            orc.setCustomNameVisible(true);
            buffMob(orc, 500.0D * hpMult, 22.0D * dmgMult, Math.min(26.0D, 15.0D + (tier - 7) * 2.0D), 0.85D);
            orc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, tier >= 12 ? 2 : (tier >= 9 ? 1 : 0)));
            orc.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, tier >= 12 ? 1 : 0));
            return orc;
        } else if (tier == 6) { // A
            var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
            orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.elite_prefix", "A").append(Component.translatable("awakening_system.dungeon.mob.abyssal_orc_warrior")));
            orc.setCustomNameVisible(true);
            buffMob(orc, 300.0D, 16.0D, 12.0D, 0.55D);
            orc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0));
            return orc;
        } else if (tier == 5) { // B
            var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.elite_prefix", "B").append(Component.translatable("awakening_system.dungeon.mob.shadow_infantry_commander")));
            infantry.setCustomNameVisible(true);
            buffMob(infantry, 200.0D, 12.0D, 9.0D, 0.45D);
            infantry.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 0));
            return infantry;
        } else if (tier == 4) { // C
            var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
            orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.elite_prefix", "C").append(Component.translatable("awakening_system.dungeon.mob.blood_berserker_orc")));
            orc.setCustomNameVisible(true);
            buffMob(orc, 140.0D, 9.0D, 6.0D, 0.35D);
            return orc;
        } else if (tier == 3) { // D
            var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.elite_prefix", rank).append(Component.translatable("awakening_system.dungeon.mob.veteran_shadow_infantry")));
            infantry.setCustomNameVisible(true);
            buffMob(infantry, 95.0D, 6.0D, 4.0D, 0.20D);
            return infantry;
        } else { // E, F
            var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
            infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.elite_prefix", rank).append(Component.translatable("awakening_system.dungeon.mob.veteran_shadow_infantry")));
            infantry.setCustomNameVisible(true);
            buffMob(infantry, 65.0D, 4.5D, 2.0D, 0.15D);
            return infantry;
        }
    }

    private static void buffMob(Monster mob, double maxHp, double totalDmg, double armor, double kbRes) {
        mob.setPersistenceRequired();
        mob.setCanPickUpLoot(false);
        var hpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.setBaseValue(maxHp);
            mob.setHealth((float) maxHp);
        }
        var dmgAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.setBaseValue(totalDmg);
        }
        var armorAttr = mob.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(armor);
        }
        var kbAttr = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.setBaseValue(kbRes);
        }
        mob.addTag("dungeon_mob");
        mob.addTag("rpg_scaled");
    }

    public static void onEntityKilled(UUID entityId) {
        for (DungeonSession session : SESSIONS.values()) {
            if (session.enemyUUIDs.remove(entityId)) {
                if (session.lastEnemyUUID != null && session.lastEnemyUUID.equals(entityId)) {
                    session.lastEnemyNotified = false;
                    session.lastEnemyUUID = null;
                }
                int remaining = session.enemyUUIDs.size();
                MinecraftServer server = getServer();
                if (server == null) return;
                ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);

                // Aumento de tiempo al matar enemigos (+8s esbirro, +20s élite)
                boolean isElite = (session.currentFloor == 2 && remaining == session.totalEnemies - 1);
                int timeBonus = isElite ? 400 : 160; // 20s o 8s en ticks
                session.combatTimer = Math.min(session.maxCombatTimer + 2400, session.combatTimer + timeBonus);

                if (player != null) {
                    Component bonusText = isElite 
                            ? Component.translatable("awakening_system.action.elite_purified")
                            : Component.translatable("awakening_system.action.enemy_purified");
                    player.displayClientMessage(bonusText, true);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.4F);
                }

                if (session.bossEvent != null) {
                    session.bossEvent.setProgress((float) remaining / Math.max(1, session.totalEnemies));
                    int secRemaining = Math.max(0, (session.combatTimer + 19) / 20);
                    int mins = secRemaining / 60;
                    int secs = secRemaining % 60;
                    String timeStr = String.format("%02d:%02d", mins, secs);
                    session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, session.currentFloor, remaining, timeStr));
                }

                if (remaining == 0) {
                    if (session.currentFloor == 1) {
                        openPhase1Barrier(session, player);
                    } else if (session.currentFloor == 2) {
                        openPhase2Barrier(session, player);
                    } else if (session.currentFloor == 3) {
                        openPhase3Barrier(session, player);
                    } else if (session.currentFloor == 4) {
                        openPhase4Barrier(session, player);
                    } else {
                        completeDungeonCombat(session, player);
                    }
                }
                return;
            }
        }
    }


    // =========================================================================
    // MAZMORRA CONTINUA DE 5 CAVERNAS PROCEDURALES CON MURALLAS MÁGICAS
    // =========================================================================

    public static double findSafeFloorY(ServerLevel level, double x, int baseY, double z) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        for (int y = baseY + 4; y >= baseY - 4; y--) {
            BlockPos p = new BlockPos(ix, y, iz);
            if (!level.isEmptyBlock(p) && level.isEmptyBlock(p.above()) && level.isEmptyBlock(p.above(2))) {
                return y + 1.0;
            }
        }
        return baseY + 1.0;
    }

    public static boolean isPlainsTheme(String theme) {
        if (theme == null) return false;
        String t = theme.toLowerCase(java.util.Locale.ROOT);
        return t.contains("plain") || t.contains("cherry") || t.contains("sakura") || t.contains("autumn") || t.contains("forest");
    }

    private static void buildOuterBunkerEnvelope(ServerLevel level, DungeonSession session, BlockPos center, String theme) {
        int minX = center.getX(), maxX = center.getX();
        int minY = center.getY(), maxY = center.getY();
        int minZ = center.getZ() - 24, maxZ = center.getZ() + 24;

        BlockPos[] rooms = new BlockPos[]{center, session.room2Center, session.room3Center, session.room4Center, session.room5Center};
        for (BlockPos r : rooms) {
            if (r != null) {
                minX = Math.min(minX, r.getX() - 24);
                maxX = Math.max(maxX, r.getX() + 24);
                minY = Math.min(minY, r.getY() - 8);
                maxY = Math.max(maxY, r.getY() + 22);
                minZ = Math.min(minZ, r.getZ() - 24);
                maxZ = Math.max(maxZ, r.getZ() + 28);
            }
        }

        boolean plains = isPlainsTheme(theme);
        // Sellado perimetral absoluto del calabozo completo adaptado a la morfologa 3D
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos pFloor = new BlockPos(x, minY - 4, z);
                level.setBlock(pFloor, Blocks.BEDROCK.defaultBlockState(), 2);
                session.arenaBlocks.add(pFloor);

                // En biomas de llanura, el techo exterior usa barreras invisibles para permitir la vista libre del cielo y entrada de luz solar
                BlockPos pCeil = new BlockPos(x, maxY + 4, z);
                level.setBlock(pCeil, plains ? Blocks.BARRIER.defaultBlockState() : Blocks.BEDROCK.defaultBlockState(), 2);
                session.arenaBlocks.add(pCeil);

                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    for (int y = minY - 3; y <= maxY + 3; y++) {
                        BlockPos pWall = new BlockPos(x, y, z);
                        level.setBlock(pWall, Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(pWall);
                    }
                }
            }
        }
    }

    public static void buildContinuousDungeon(ServerLevel level, DungeonSession session, BlockPos center, String rank, String theme) {
        // 0. Sellar el perímetro exterior adaptado a la morfología 3D del calabozo
        buildOuterBunkerEnvelope(level, session, center, theme);

        // 1. Tallar las 5 Cavernas Orgánicas Procedurales (posiciones 3D aleatorias con curvas y pendientes)
        carveOrganicCaveChamber(level, session, center, 13, 12, 9, theme, 1);
        carveOrganicCaveChamber(level, session, session.room2Center, 15, 13, 10, theme, 2);
        carveOrganicCaveChamber(level, session, session.room3Center, 17, 15, 11, theme, 3);
        carveOrganicCaveChamber(level, session, session.room4Center, 18, 16, 13, theme, 4);
        carveOrganicCaveChamber(level, session, session.room5Center, 20, 19, 15, theme, 5);

        // 2. Conectar las salas con túneles 3D sinuosos, pendientes/rampas y desniveles
        carveOrganic3DWindingTunnel(level, session, center.offset(0, 0, 8), session.room2Center.offset(0, 0, -8), 3.5D, 5, theme, 1);
        carveOrganic3DWindingTunnel(level, session, session.room2Center.offset(0, 0, 8), session.room3Center.offset(0, 0, -8), 3.5D, 5, theme, 2);
        carveOrganic3DWindingTunnel(level, session, session.room3Center.offset(0, 0, 8), session.room4Center.offset(0, 0, -8), 3.5D, 5, theme, 3);
        carveOrganic3DWindingTunnel(level, session, session.room4Center.offset(0, 0, 8), session.room5Center.offset(0, 0, -8), 3.5D, 5, theme, 4);

        // 3. Trono natural del jefe en el fondo de la caverna 5
        buildNaturalCaveThrone(level, session, session.thronePos, theme);
    }

    private static BlockState getThemeFloorBlock(String theme, int stage, int dx, int dy, int dz) {
        int rMod = Math.abs(dx * 3 + dz * 7 + dy * 5) % 12;
        if ("tundra".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod == 0) return Blocks.BLUE_ICE.defaultBlockState();
                if (rMod <= 3) return Blocks.PACKED_ICE.defaultBlockState();
                return Blocks.SNOW_BLOCK.defaultBlockState();
            }
            return Blocks.PACKED_ICE.defaultBlockState();
        } else if ("lush_plains".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod <= 3) return Blocks.MOSS_BLOCK.defaultBlockState();
                if (rMod <= 5) return Blocks.ROOTED_DIRT.defaultBlockState();
                if (rMod == 6) return Blocks.MUD.defaultBlockState();
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            return Blocks.ROOTED_DIRT.defaultBlockState();
        } else if ("cherry_plains".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod <= 2) return Blocks.MOSS_BLOCK.defaultBlockState();
                if (rMod == 3) return Blocks.COARSE_DIRT.defaultBlockState();
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            return Blocks.DIRT.defaultBlockState();
        } else if ("autumn_plains".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod <= 3) return Blocks.PODZOL.defaultBlockState();
                if (rMod <= 6) return Blocks.COARSE_DIRT.defaultBlockState();
                if (rMod == 7) return Blocks.PACKED_MUD.defaultBlockState();
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            return Blocks.DIRT.defaultBlockState();
        } else if ("plains".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod == 0) return Blocks.MOSS_BLOCK.defaultBlockState();
                if (rMod <= 2) return Blocks.COARSE_DIRT.defaultBlockState();
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            return Blocks.DIRT.defaultBlockState();
        } else if ("baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod <= 2) return Blocks.CRIMSON_NYLIUM.defaultBlockState();
                if (rMod == 3) return Blocks.WARPED_NYLIUM.defaultBlockState();
                if (rMod == 4) return Blocks.RED_NETHER_BRICKS.defaultBlockState();
                if (rMod <= 6) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
                return Blocks.NETHERRACK.defaultBlockState();
            }
            return Blocks.BLACKSTONE.defaultBlockState();
        } else if ("ancient_temple".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod <= 2) return Blocks.CUT_SANDSTONE.defaultBlockState();
                if (rMod == 3) return Blocks.CHISELED_SANDSTONE.defaultBlockState();
                if (rMod == 4) return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
                if (rMod == 5) return Blocks.GOLD_BLOCK.defaultBlockState();
                return Blocks.SANDSTONE.defaultBlockState();
            }
            return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        } else if ("crystal_cave".equalsIgnoreCase(theme)) {
            if (dy == 0) {
                if (rMod <= 2) return Blocks.AMETHYST_BLOCK.defaultBlockState();
                if (rMod == 3) return Blocks.CALCITE.defaultBlockState();
                if (rMod <= 6) return Blocks.SMOOTH_BASALT.defaultBlockState();
                return Blocks.DEEPSLATE.defaultBlockState();
            }
            return Blocks.DEEPSLATE.defaultBlockState();
        } else {
            // "igris" o "crypt"
            if (dy == 0) {
                if (stage >= 4) {
                    if (rMod <= 2) return Blocks.DEEPSLATE_TILES.defaultBlockState();
                    if (rMod == 3) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                } else {
                    if (rMod == 0) return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                    if (rMod <= 3) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
                    if (rMod <= 6) return Blocks.STONE_BRICKS.defaultBlockState();
                    return Blocks.DEEPSLATE.defaultBlockState();
                }
            }
            return Blocks.DEEPSLATE.defaultBlockState();
        }
    }

    private static BlockState getThemeWallBlock(String theme, int stage, int dx, int dy, int dz, int h) {
        int rMod = Math.abs(dx * 11 + dz * 13 + dy * 7) % 24;
        if ("tundra".equalsIgnoreCase(theme)) {
            if (rMod <= 5) return Blocks.PACKED_ICE.defaultBlockState();
            if (rMod <= 9) return Blocks.BLUE_ICE.defaultBlockState();
            if (rMod <= 13) return Blocks.SNOW_BLOCK.defaultBlockState();
            return Blocks.STONE.defaultBlockState();
        } else if ("lush_plains".equalsIgnoreCase(theme)) {
            if (rMod <= 4) return Blocks.MOSS_BLOCK.defaultBlockState();
            if (rMod <= 7) return Blocks.CLAY.defaultBlockState();
            if (rMod <= 10) return Blocks.ROOTED_DIRT.defaultBlockState();
            if (rMod <= 15) return Blocks.STONE.defaultBlockState();
            return Blocks.COBBLESTONE.defaultBlockState();
        } else if ("cherry_plains".equalsIgnoreCase(theme)) {
            if (rMod <= 4) return Blocks.MOSS_BLOCK.defaultBlockState();
            if (rMod <= 7) return Blocks.CHERRY_WOOD.defaultBlockState();
            if (rMod <= 12) return Blocks.STONE.defaultBlockState();
            return Blocks.COBBLESTONE.defaultBlockState();
        } else if ("autumn_plains".equalsIgnoreCase(theme)) {
            if (rMod <= 4) return Blocks.PODZOL.defaultBlockState();
            if (rMod <= 7) return Blocks.PACKED_MUD.defaultBlockState();
            if (rMod <= 12) return Blocks.STONE.defaultBlockState();
            return Blocks.MUD_BRICKS.defaultBlockState();
        } else if ("plains".equalsIgnoreCase(theme)) {
            if (rMod <= 3) return Blocks.MOSS_BLOCK.defaultBlockState();
            if (rMod <= 6) return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            if (rMod <= 8) return Blocks.COAL_ORE.defaultBlockState();
            if (rMod <= 10) return Blocks.IRON_ORE.defaultBlockState();
            if (rMod <= 16) return Blocks.STONE.defaultBlockState();
            return Blocks.COBBLESTONE.defaultBlockState();
        } else if ("baran".equalsIgnoreCase(theme) || "nether".equalsIgnoreCase(theme)) {
            if (rMod <= 4) return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            if (rMod <= 7) return Blocks.CRIMSON_NYLIUM.defaultBlockState();
            if (rMod <= 9) return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            if (rMod <= 11) return Blocks.RED_NETHER_BRICKS.defaultBlockState();
            if (rMod == 12) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
            return Blocks.BLACKSTONE.defaultBlockState();
        } else if ("ancient_temple".equalsIgnoreCase(theme)) {
            if (rMod <= 4) return Blocks.CUT_SANDSTONE.defaultBlockState();
            if (rMod <= 8) return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            if (rMod <= 11) return Blocks.CHISELED_SANDSTONE.defaultBlockState();
            if (rMod == 12) return Blocks.GOLD_BLOCK.defaultBlockState();
            return Blocks.SANDSTONE.defaultBlockState();
        } else if ("crystal_cave".equalsIgnoreCase(theme)) {
            if (rMod <= 5) return Blocks.AMETHYST_BLOCK.defaultBlockState();
            if (rMod <= 8) return Blocks.BUDDING_AMETHYST.defaultBlockState();
            if (rMod <= 12) return Blocks.CALCITE.defaultBlockState();
            if (rMod <= 16) return Blocks.SMOOTH_BASALT.defaultBlockState();
            return Blocks.DEEPSLATE.defaultBlockState();
        } else {
            // "igris" o "crypt"
            if (rMod == 0) return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
            if (rMod == 1) return Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
            if (rMod <= 3) return Blocks.DEEPSLATE_TILES.defaultBlockState();
            if (rMod <= 6) return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            if (rMod <= 9) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            if (rMod <= 12) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
            if (rMod == 13) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
            return Blocks.DEEPSLATE.defaultBlockState();
        }
    }

    private static void placeFloorDecorations(ServerLevel level, DungeonSession session, BlockPos p, String theme, int dx, int dz) {
        if ("plains".equalsIgnoreCase(theme)) {
            int flowerMod = Math.abs(dx * 13 + dz * 17) % 18;
            if (flowerMod == 0) level.setBlock(p, Blocks.POPPY.defaultBlockState(), 3);
            else if (flowerMod == 1) level.setBlock(p, Blocks.DANDELION.defaultBlockState(), 3);
            else if (flowerMod == 2) level.setBlock(p, Blocks.CORNFLOWER.defaultBlockState(), 3);
            else if (flowerMod <= 5) level.setBlock(p, Blocks.SHORT_GRASS.defaultBlockState(), 3);
        } else if ("tundra".equalsIgnoreCase(theme)) {
            if (Math.abs(dx * 5 + dz * 9) % 6 == 0) {
                level.setBlock(p, Blocks.SNOW.defaultBlockState(), 3);
            }
        } else {
            // Sin fogatas ni fuego que emitan humo o causen dao involuntario
        }
    }

    private static BlockState getThemeLeavesBlock(String theme, int randSeed) {
        if ("cherry_plains".equalsIgnoreCase(theme)) {
            return Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        } else if ("lush_plains".equalsIgnoreCase(theme)) {
            return (randSeed % 3 == 0)
                    ? Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)
                    : Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        } else if ("autumn_plains".equalsIgnoreCase(theme)) {
            return (randSeed % 2 == 0)
                    ? Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)
                    : Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        } else {
            return (randSeed % 3 == 0)
                    ? Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)
                    : Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        }
    }

    private static void carveOrganicCaveChamber(ServerLevel level, DungeonSession session, BlockPos chamberCenter, int rx, int rz, int h, String theme, int stage) {
        int cx = chamberCenter.getX();
        int cy = chamberCenter.getY();
        int cz = chamberCenter.getZ();
        boolean plains = isPlainsTheme(theme);

        // Claraboyas naturales al cielo abierto para biomas de llanura con frondoso follaje
        double s1Radius = Math.max(3.6D, Math.min(rx, rz) * 0.52D);
        int s2X = (stage % 2 == 0) ? -3 : 3;
        int s2Z = (stage % 3 == 0) ? -2 : 2;
        double s2Radius = Math.max(2.6D, Math.min(rx, rz) * 0.36D);

        for (int dx = -rx - 3; dx <= rx + 3; dx++) {
            for (int dz = -rz - 3; dz <= rz + 3; dz++) {
                double normX = (double) dx / rx;
                double normZ = (double) dz / rz;
                double hDist = normX * normX + normZ * normZ;

                double dShaft1 = Math.sqrt(dx * dx + dz * dz);
                double dShaft2 = Math.sqrt((dx - s2X) * (dx - s2X) + (dz - s2Z) * (dz - s2Z));
                boolean inSkylight = plains && (dShaft1 < s1Radius || dShaft2 < s2Radius);
                boolean isSkylightRim = plains && ((dShaft1 >= s1Radius - 1.4D && dShaft1 <= s1Radius + 1.2D) || (dShaft2 >= s2Radius - 1.3D && dShaft2 <= s2Radius + 1.1D));

                for (int dy = -4; dy <= h + 4; dy++) {
                    BlockPos p = chamberCenter.offset(dx, dy, dz);

                    if (dy <= -3) {
                        level.setBlock(p, Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(p);
                    } else if (Math.abs(dx) >= rx + 2 || Math.abs(dz) >= rz + 2) {
                        level.setBlock(p, Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(p);
                    } else if (dy >= h + 3) {
                        // Techo exterior superior: sellado impenetrable
                        level.setBlock(p, plains ? Blocks.BARRIER.defaultBlockState() : Blocks.BEDROCK.defaultBlockState(), 2);
                        session.arenaBlocks.add(p);
                    } else if (dy <= 0) {
                        level.setBlock(p, getThemeFloorBlock(theme, stage, dx, dy, dz), 2);
                        session.arenaBlocks.add(p);
                    } else if (dy <= h) {
                        double normY = (double) (dy - 1) / Math.max(1, h - 1);
                        double domeDist = hDist + normY * normY;
                        double noise = Math.sin(dx * 0.42 + stage) * Math.cos(dz * 0.42 + stage) * 0.12;
                        double effective = domeDist + noise;

                        if (inSkylight && dy >= h - 1) {
                            if (dy == h) {
                                // Cúpula cenital: si es borde, hojas ornamentales; si es centro, BARRERA INVISIBLE sellada para que no escapen
                                if (isSkylightRim) {
                                    BlockState leaves = getThemeLeavesBlock(theme, Math.abs(dx * 13 + dz * 17 + dy * 7));
                                    level.setBlock(p, leaves, 2);
                                } else {
                                    level.setBlock(p, Blocks.BARRIER.defaultBlockState(), 2);
                                }
                                session.arenaBlocks.add(p);
                            } else {
                                // dy == h - 1: Aire interior para que se vea el cielo a través de las barreras
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                                session.arenaBlocks.add(p);
                                if (isSkylightRim) {
                                    BlockState leaves = getThemeLeavesBlock(theme, Math.abs(dx * 13 + dz * 17 + dy * 7));
                                    level.setBlock(p, leaves, 2);
                                    session.arenaBlocks.add(p);
                                }
                            }
                        } else if (effective < 0.82) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            session.arenaBlocks.add(p);
                            if (dy == 1) {
                                placeFloorDecorations(level, session, p, theme, dx, dz);
                            }
                        } else {
                            level.setBlock(p, getThemeWallBlock(theme, stage, dx, dy, dz, h), 2);
                            session.arenaBlocks.add(p);
                        }
                    } else {
                        // dy entre h + 1 y h + 2:
                        // Capas superiores por encima de la cúpula:
                        // Si es claraboya plains: BARRERAS INVISIBLES (u hojas en el borde) para permitir el paso de luz solar sin hueco físico
                        if (inSkylight) {
                            if (isSkylightRim) {
                                BlockState leaves = getThemeLeavesBlock(theme, Math.abs(dx * 13 + dz * 17 + dy * 7));
                                level.setBlock(p, leaves, 2);
                            } else {
                                level.setBlock(p, Blocks.BARRIER.defaultBlockState(), 2);
                            }
                            session.arenaBlocks.add(p);
                        } else {
                            level.setBlock(p, getThemeWallBlock(theme, stage, dx, dy, dz, h), 2);
                            session.arenaBlocks.add(p);
                        }
                    }
                }
            }
        }
    }

    private static BlockPos carveOrganic3DWindingTunnel(ServerLevel level, DungeonSession session,
            BlockPos start, BlockPos end, double tunnelRadius, int tunnelHeight, String theme, int barrierIndex) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();
        double horizDist = Math.max(0.01D, Math.sqrt(dx * dx + dz * dz));
        double totalDist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = (int) Math.ceil(totalDist * 1.6D);
        steps = Math.max(12, steps);

        double perpX = -dz / horizDist;
        double perpZ = dx / horizDist;
        double sideAmplitude = 2.6D;

        BlockPos midBarrierPos = null;

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double meander = Math.sin(t * Math.PI * 2.0D) * sideAmplitude;

            double cx = start.getX() + dx * t + perpX * meander;
            double cy = start.getY() + dy * t;
            double cz = start.getZ() + dz * t + perpZ * meander;

            int intCx = (int) Math.round(cx);
            int intCy = (int) Math.round(cy);
            int intCz = (int) Math.round(cz);

            if (i == steps / 2) {
                midBarrierPos = new BlockPos(intCx, intCy, intCz);
            }

            int intR = (int) Math.ceil(tunnelRadius);
            for (int ox = -intR - 2; ox <= intR + 2; ox++) {
                for (int oy = -3; oy <= tunnelHeight + 2; oy++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        BlockPos p = new BlockPos(intCx + ox, intCy + oy, intCz + oz);
                        double dH = Math.sqrt(ox * ox + oz * oz);
                        double dist = Math.sqrt(dH * dH + Math.max(0, oy - 2) * Math.max(0, oy - 2));

                        if (oy <= 0) {
                            level.setBlock(p, getThemeFloorBlock(theme, 1, ox, oy, intCz), 2);
                        } else if (dist < tunnelRadius) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        } else if (dist <= tunnelRadius + 2.0D) {
                            level.setBlock(p, getThemeWallBlock(theme, 1, ox, oy, intCz, tunnelHeight), 2);
                        }
                        session.arenaBlocks.add(p);
                    }
                }
            }
        }

        if (midBarrierPos != null) {
            placeMagicalBarrierThreshold(level, session, midBarrierPos, barrierIndex, theme);
        }
        return midBarrierPos;
    }

    private static void placeMagicalBarrierThreshold(ServerLevel level, DungeonSession session, BlockPos pos, int barrierIndex, String theme) {
        Set<BlockPos> targetSet = switch (barrierIndex) {
            case 1 -> session.barrier1Blocks;
            case 2 -> session.barrier2Blocks;
            case 3 -> session.barrier3Blocks;
            default -> session.barrier4Blocks;
        };
        switch (barrierIndex) {
            case 1 -> session.barrier1Pos = pos;
            case 2 -> session.barrier2Pos = pos;
            case 3 -> session.barrier3Pos = pos;
            default -> session.barrier4Pos = pos;
        }

        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();

        // Muralla mgica 100% INVISIBLE (Blocks.BARRIER) entre salas:
        // Sin columnas de piedra, sin marcos opacos y sin linternas visibles.
        // Sella de forma hermtica el corte transversal del tnel para impedir el paso a la siguiente sala.
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 1; dy <= 7; dy++) {
                    BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                    if (level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.BARRIER.defaultBlockState(), 2);
                        targetSet.add(p);
                        session.arenaBlocks.add(p);
                    }
                }
            }
        }
    }

    private static void buildNaturalCaveThrone(ServerLevel level, DungeonSession session, BlockPos throneCenter, String theme) {
        int cx = throneCenter.getX();
        int cy = throneCenter.getY();
        int cz = throneCenter.getZ();
        boolean isBaran = "baran".equalsIgnoreCase(theme);

        BlockState step1 = isBaran ? Blocks.POLISHED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState step2 = isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState seatBlock = isBaran ? Blocks.BLACKSTONE_STAIRS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState();
        BlockState backBlock = isBaran ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -3; dz <= 4; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy - 1, cz + dz);
                level.setBlock(p, step1, 2);
                session.arenaBlocks.add(p);
                if (Math.abs(dx) <= 3 && dz >= -1) {
                    BlockPos p2 = new BlockPos(cx + dx, cy, cz + dz);
                    level.setBlock(p2, step2, 2);
                    session.arenaBlocks.add(p2);
                }
            }
        }

        BlockPos seat = new BlockPos(cx, cy + 1, cz + 2);
        level.setBlock(seat, seatBlock, 2);
        session.arenaBlocks.add(seat);

        for (int dy = 1; dy <= 4; dy++) {
            BlockPos back = new BlockPos(cx, cy + dy, cz + 3);
            level.setBlock(back, backBlock, 2);
            session.arenaBlocks.add(back);
        }

        BlockPos armL = new BlockPos(cx - 1, cy + 1, cz + 2);
        BlockPos armR = new BlockPos(cx + 1, cy + 1, cz + 2);
        level.setBlock(armL, backBlock, 2);
        level.setBlock(armR, backBlock, 2);
        session.arenaBlocks.add(armL);
        session.arenaBlocks.add(armR);

        // Linternas ceremoniales sin humo molesto
        BlockPos fireL = new BlockPos(cx - 3, cy + 1, cz + 2);
        BlockPos fireR = new BlockPos(cx + 3, cy + 1, cz + 2);
        level.setBlock(fireL, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(fireR, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        session.arenaBlocks.add(fireL);
        session.arenaBlocks.add(fireR);
    }

    private static void dissolveBarrierSweep(ServerLevel level, DungeonSession session, Set<BlockPos> barrierBlocks, BlockPos barrierPos) {
        if (barrierPos == null) {
            dissolveBarrierSweep(level, session, barrierBlocks, session.arenaCenter.getZ() + 20);
            return;
        }
        int cx = barrierPos.getX();
        int cy = barrierPos.getY();
        int cz = barrierPos.getZ();

        for (BlockPos p : barrierBlocks) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            level.sendParticles(ParticleTypes.FLASH, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.0D);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.05D);
        }
        barrierBlocks.clear();

        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = 0; dy <= 8; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos bp = new BlockPos(cx + dx, cy + dy, cz + dz);
                    if (level.getBlockState(bp).is(Blocks.BARRIER)) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void dissolveBarrierSweep(ServerLevel level, DungeonSession session, Set<BlockPos> barrierBlocks, int barrierZ) {
        int cx = session.arenaCenter.getX();
        int cy = session.arenaCenter.getY();

        for (BlockPos p : barrierBlocks) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            level.sendParticles(ParticleTypes.FLASH, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.0D);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.05D);
        }
        barrierBlocks.clear();

        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = 0; dy <= 8; dy++) {
                for (int dz = barrierZ - 3; dz <= barrierZ + 3; dz++) {
                    BlockPos bp = new BlockPos(cx + dx, cy + dy, dz);
                    if (level.getBlockState(bp).is(Blocks.BARRIER)) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // =========================================================================
    // SPAWN DINÁMICO DE ENEMIGOS POR FASES (1 -> 2 -> 3 -> 4 -> 5 JEFE)
    // =========================================================================

    public static void spawnPhase1Enemies(ServerLevel level, DungeonSession session) {
        session.enemyUUIDs.clear();
        BlockPos center = session.arenaCenter;
        String theme = (session.biome != null) ? session.biome.toLowerCase(java.util.Locale.ROOT) : "crypt";
        int tier = getRankTier(session.rank);
        double hpMult = 1.0D + Math.max(0, tier - 3) * 0.35D;
        double dmgMult = 1.0D + Math.max(0, tier - 3) * 0.25D;

        int count = 6 + Math.min(tier, 4);

        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            double dist = 4.0 + (i % 3) * 1.5;
            double sx = center.getX() + Math.cos(angle) * dist;
            double sz = center.getZ() + Math.sin(angle) * dist;
            double sy = findSafeFloorY(level, sx, center.getY(), sz);

            Monster mob;
            if ("baran".equalsIgnoreCase(theme)) {
                if (i % 2 == 0) {
                    var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                    orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.flame_demon_warrior"));
                    buffMob(orc, 180.0 * hpMult, 12.0 * dmgMult, 10.0, 0.4);
                    mob = orc;
                } else {
                    var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                    infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.baran_fiery_legionnaire"));
                    buffMob(infantry, 140.0 * hpMult, 10.0 * dmgMult, 8.0, 0.3);
                    mob = infantry;
                }
            } else if ("igris".equalsIgnoreCase(theme)) {
                if (i % 2 == 0) {
                    var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                    infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_infantry"));
                    buffMob(infantry, 150.0 * hpMult, 10.0 * dmgMult, 8.0, 0.3);
                    mob = infantry;
                } else {
                    var w = new WitherSkeleton(EntityType.WITHER_SKELETON, level);
                    w.setCustomName(Component.translatable("awakening_system.dungeon.mob.igris_crimson_knight"));
                    w.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    buffMob(w, 130.0 * hpMult, 11.0 * dmgMult, 6.0, 0.2);
                    mob = w;
                }
            } else {
                mob = createFloorMinion(level, session.rank, 1, i);
            }

            applyDungeonMobMetadata(mob, session.rank, 1, false);
            mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();
            mob.setCanPickUpLoot(false);
            level.addFreshEntity(mob);
            session.enemyUUIDs.add(mob.getUUID());
        }
        session.totalEnemies = session.enemyUUIDs.size();
    }

    public static void spawnPhase2Enemies(ServerLevel level, DungeonSession session) {
        session.enemyUUIDs.clear();
        BlockPos center = (session.room2Center != null) ? session.room2Center : session.arenaCenter.offset(0, 0, 40);
        String theme = (session.biome != null) ? session.biome.toLowerCase(java.util.Locale.ROOT) : "crypt";
        int tier = getRankTier(session.rank);
        double hpMult = 1.0D + Math.max(0, tier - 3) * 0.40D;
        double dmgMult = 1.0D + Math.max(0, tier - 3) * 0.30D;

        int count = 6;

        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            double dist = 4.5 + (i % 2) * 2.0;
            double sx = center.getX() + Math.cos(angle) * dist;
            double sz = center.getZ() + Math.sin(angle) * dist;
            double sy = findSafeFloorY(level, sx, center.getY(), sz);

            Monster mob;
            if ("baran".equalsIgnoreCase(theme)) {
                var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.shadow_demon_assassin"));
                buffMob(assassin, 200.0 * hpMult, 15.0 * dmgMult, 10.0, 0.4);
                mob = assassin;
            } else if ("igris".equalsIgnoreCase(theme)) {
                if (i % 2 == 0) {
                    var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                    assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.throne_crimson_assassin"));
                    buffMob(assassin, 210.0 * hpMult, 16.0 * dmgMult, 10.0, 0.4);
                    mob = assassin;
                } else {
                    var w = new WitherSkeleton(EntityType.WITHER_SKELETON, level);
                    w.setCustomName(Component.translatable("awakening_system.dungeon.mob.crypt_veteran"));
                    w.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                    buffMob(w, 180.0 * hpMult, 14.0 * dmgMult, 8.0, 0.3);
                    mob = w;
                }
            } else {
                mob = createFloorMinion(level, session.rank, 2, i);
            }

            applyDungeonMobMetadata(mob, session.rank, 2, (i == 0));
            mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();
            mob.setCanPickUpLoot(false);
            level.addFreshEntity(mob);
            session.enemyUUIDs.add(mob.getUUID());
        }
        session.totalEnemies = session.enemyUUIDs.size();
    }

    public static void spawnPhase3Enemies(ServerLevel level, DungeonSession session) {
        session.enemyUUIDs.clear();
        BlockPos center = (session.room3Center != null) ? session.room3Center : session.arenaCenter.offset(0, 0, 84);
        String theme = (session.biome != null) ? session.biome.toLowerCase(java.util.Locale.ROOT) : "crypt";
        int tier = getRankTier(session.rank);
        double hpMult = 1.0D + Math.max(0, tier - 3) * 0.45D;
        double dmgMult = 1.0D + Math.max(0, tier - 3) * 0.35D;

        int count = 5;

        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            double dist = 4.0 + (i % 2) * 2.0;
            double sx = center.getX() + Math.cos(angle) * dist;
            double sz = center.getZ() + Math.sin(angle) * dist;
            double sy = findSafeFloorY(level, sx, center.getY(), sz);

            Monster mob;
            if ("baran".equalsIgnoreCase(theme)) {
                if (i == 0) {
                    var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                    orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.mini_boss_prefix").append(Component.translatable("awakening_system.dungeon.mob.baran_thunder_commander")));
                    buffMob(orc, 450.0 * hpMult, 20.0 * dmgMult, 14.0, 0.7);
                    mob = orc;
                } else {
                    var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                    orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.pit_elite_guard"));
                    buffMob(orc, 240.0 * hpMult, 16.0 * dmgMult, 10.0, 0.45);
                    mob = orc;
                }
            } else if ("igris".equalsIgnoreCase(theme)) {
                if (i == 0) {
                    var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
                    orc.setCustomName(Component.translatable("awakening_system.dungeon.mob.mini_boss_prefix").append(Component.translatable("awakening_system.dungeon.mob.crimson_guard_captain")));
                    buffMob(orc, 420.0 * hpMult, 19.0 * dmgMult, 14.0, 0.65);
                    mob = orc;
                } else {
                    var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                    infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.pit_crimson_guardian"));
                    buffMob(infantry, 220.0 * hpMult, 15.0 * dmgMult, 10.0, 0.4);
                    mob = infantry;
                }
            } else {
                mob = createFloorMinion(level, session.rank, 3, i);
            }

            boolean isMiniBoss = (i == 0 && ("baran".equalsIgnoreCase(theme) || "igris".equalsIgnoreCase(theme)));
            applyDungeonMobMetadata(mob, session.rank, 3, isMiniBoss);
            mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();
            mob.setCanPickUpLoot(false);
            level.addFreshEntity(mob);
            session.enemyUUIDs.add(mob.getUUID());
        }
        session.totalEnemies = session.enemyUUIDs.size();
    }

    public static void spawnPhase4Enemies(ServerLevel level, DungeonSession session) {
        session.enemyUUIDs.clear();
        BlockPos center = (session.room4Center != null) ? session.room4Center : session.arenaCenter.offset(0, 0, 135);
        String theme = (session.biome != null) ? session.biome.toLowerCase(java.util.Locale.ROOT) : "crypt";
        int tier = getRankTier(session.rank);
        double hpMult = 1.0D + Math.max(0, tier - 3) * 0.50D;
        double dmgMult = 1.0D + Math.max(0, tier - 3) * 0.40D;

        int count = 6;

        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            double dist = 5.0 + (i % 2) * 2.0;
            double sx = center.getX() + Math.cos(angle) * dist;
            double sz = center.getZ() + Math.sin(angle) * dist;
            double sy = findSafeFloorY(level, sx, center.getY(), sz);

            Monster mob;
            if ("baran".equalsIgnoreCase(theme)) {
                if (i % 2 == 0) {
                    var necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
                    necro.setCustomName(Component.translatable("awakening_system.dungeon.mob.arch_lightning_necromancer"));
                    buffMob(necro, 300.0 * hpMult, 16.0 * dmgMult, 10.0, 0.45);
                    mob = necro;
                } else {
                    var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
                    assassin.setCustomName(Component.translatable("awakening_system.dungeon.mob.cathedral_executioner"));
                    buffMob(assassin, 250.0 * hpMult, 18.0 * dmgMult, 11.0, 0.45);
                    mob = assassin;
                }
            } else if ("igris".equalsIgnoreCase(theme)) {
                if (i % 2 == 0) {
                    var necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
                    necro.setCustomName(Component.translatable("awakening_system.dungeon.mob.crimson_blood_necromancer"));
                    buffMob(necro, 300.0 * hpMult, 16.0 * dmgMult, 10.0, 0.45);
                    mob = necro;
                } else {
                    var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
                    infantry.setCustomName(Component.translatable("awakening_system.dungeon.mob.imperial_guard_paladin"));
                    buffMob(infantry, 260.0 * hpMult, 17.0 * dmgMult, 12.0, 0.45);
                    mob = infantry;
                }
            } else {
                mob = createFloorMinion(level, session.rank, 4, i);
            }

            applyDungeonMobMetadata(mob, session.rank, 4, false);
            mob.moveTo(sx, sy, sz, level.random.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();
            mob.setCanPickUpLoot(false);
            level.addFreshEntity(mob);
            session.enemyUUIDs.add(mob.getUUID());
        }
        session.totalEnemies = session.enemyUUIDs.size();
    }

    public static void spawnPhase5Boss(ServerLevel level, DungeonSession session) {
        session.enemyUUIDs.clear();
        BlockPos pos = (session.thronePos != null) ? session.thronePos.offset(0, 0, -2) : session.room5Center.offset(0, 2, 11);
        String theme = (session.biome != null) ? session.biome.toLowerCase(java.util.Locale.ROOT) : "crypt";
        int tier = getRankTier(session.rank);

        Monster boss;
        if ("baran".equalsIgnoreCase(theme)) {
            var b = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
            double hp = getDungeonBossHp(tier, session.isRedGate);
            double dmg = getDungeonBossDmg(tier, session.isRedGate);
            buffMob(b, hp, dmg, 18.0, 0.9);
            boss = b;
        } else if ("igris".equalsIgnoreCase(theme)) {
            var igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
            double hp = getDungeonBossHp(tier, session.isRedGate);
            double dmg = getDungeonBossDmg(tier, session.isRedGate);
            buffMob(igris, hp, dmg, 16.0, 0.85);
            boss = igris;
        } else {
            if (tier >= 7) {
                var igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
                double hp = getDungeonBossHp(tier, session.isRedGate);
                double dmg = getDungeonBossDmg(tier, session.isRedGate);
                buffMob(igris, hp, dmg, 16.0, 0.85);
                boss = igris;
            } else {
                boss = createFinalBoss(level, session.rank, session.isRedGate);
            }
        }

        applyDungeonMobMetadata(boss, session.rank, 5, true);
        boss.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 180.0F, 0.0F);
        boss.setPersistenceRequired();
        boss.setCanPickUpLoot(false);
        level.addFreshEntity(boss);
        session.enemyUUIDs.add(boss.getUUID());
        session.totalEnemies = 1;

        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2F, 0.8F);
        level.sendParticles(ParticleTypes.FLASH, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 3, 0, 0, 0, 0);
    }

    public static void openPhase1Barrier(DungeonSession session, ServerPlayer player) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(session.dungeonDimension);
        if (level == null) return;

        session.barrier1Open = true;
        dissolveBarrierSweep(level, session, session.barrier1Blocks, session.barrier1Pos);

        if (player != null) {
            player.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.0F);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.2F);
            AwakeningSystemMod.notify(player, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_1_unlocked", 0xFF50FA7B, 1);
        }

        session.currentFloor = 2;
        spawnPhase2Enemies(level, session);
        session.combatTimer = Math.min(session.combatTimer + 1800, 5400);
        session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
        if (session.bossEvent != null) {
            session.bossEvent.setProgress(1.0F);
            int sec = Math.max(0, (session.combatTimer + 19) / 20);
            String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
            session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 2, session.totalEnemies, timeStr));
        }
    }

    public static void openPhase2Barrier(DungeonSession session, ServerPlayer player) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(session.dungeonDimension);
        if (level == null) return;

        session.barrier2Open = true;
        dissolveBarrierSweep(level, session, session.barrier2Blocks, session.barrier2Pos);

        if (player != null) {
            player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 0.8F);
            AwakeningSystemMod.notify(player, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_2_unlocked", 0xFF50FA7B, 2);
        }

        session.currentFloor = 3;
        spawnPhase3Enemies(level, session);
        session.combatTimer = Math.min(session.combatTimer + 2400, 6000);
        session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
        if (session.bossEvent != null) {
            session.bossEvent.setProgress(1.0F);
            int sec = Math.max(0, (session.combatTimer + 19) / 20);
            String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
            session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 3, session.totalEnemies, timeStr));
        }
    }

    public static void openPhase3Barrier(DungeonSession session, ServerPlayer player) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(session.dungeonDimension);
        if (level == null) return;

        session.barrier3Open = true;
        dissolveBarrierSweep(level, session, session.barrier3Blocks, session.barrier3Pos);

        if (player != null) {
            player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2F, 0.6F);
            AwakeningSystemMod.notify(player, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_3_unlocked", 0xFF50FA7B, 3);
        }

        session.currentFloor = 4;
        spawnPhase4Enemies(level, session);
        session.combatTimer = Math.min(session.combatTimer + 2400, 6600);
        session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
        if (session.bossEvent != null) {
            session.bossEvent.setProgress(1.0F);
            int sec = Math.max(0, (session.combatTimer + 19) / 20);
            String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
            session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 4, session.totalEnemies, timeStr));
        }
    }

    public static void openPhase4Barrier(DungeonSession session, ServerPlayer player) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(session.dungeonDimension);
        if (level == null) return;

        session.barrier4Open = true;
        dissolveBarrierSweep(level, session, session.barrier4Blocks, session.barrier4Pos);

        if (player != null) {
            player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 0.75F);
            AwakeningSystemMod.notify(player, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_4_unlocked", 0xFFFF4444, 4);
        }

        session.currentFloor = 5;
        spawnPhase5Boss(level, session);
        int tier = getRankTier(session.rank);
        int bossTimer = getPhase5BossTimer(tier, session.isRedGate);
        session.combatTimer = Math.max(session.combatTimer, bossTimer);
        session.maxCombatTimer = Math.max(session.maxCombatTimer, session.combatTimer);
        if (session.bossEvent != null) {
            session.bossEvent.setProgress(1.0F);
            session.bossEvent.setColor(session.isRedGate ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
            int sec = Math.max(0, (session.combatTimer + 19) / 20);
            String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
            session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 5, 1, timeStr));
        }
    }

    private static void completeDungeonCombat(DungeonSession session, ServerPlayer player) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(session.dungeonDimension);
        if (level == null) return;

        BlockPos finalFloorCenter = (session.thronePos != null) ? session.thronePos.offset(0, -1, -5) : ((session.room5Center != null) ? session.room5Center : session.arenaCenter);
        BlockPos altarPos = finalFloorCenter;
        session.chestPos = altarPos;
        session.chestSpawned = true;
        session.arenaBlocks.add(altarPos);

        // Altar de la Ruleta del Sistema
        level.setBlock(altarPos, Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 3);

        int[][] ring = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] offset : ring) {
            BlockPos bp = altarPos.offset(offset[0], 0, offset[1]);
            level.setBlock(bp, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
            session.arenaBlocks.add(bp);
        }

        // Desbloquear portal de salida en Sala 1 si era Red Gate
        if (session.isRedGate && session.exitPortalPos != null) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = 1; dy <= 7; dy++) {
                    level.setBlock(session.exitPortalPos.offset(dx, dy, 0), Blocks.AIR.defaultBlockState(), 3);
                }
            }
            level.playSound(null, session.exitPortalPos.getX(), session.exitPortalPos.getY() + 2, session.exitPortalPos.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.2F, 0.8F);
            level.playSound(null, session.exitPortalPos.getX(), session.exitPortalPos.getY() + 2, session.exitPortalPos.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.2F);
            level.sendParticles(ParticleTypes.EXPLOSION, session.exitPortalPos.getX() + 0.5D, session.exitPortalPos.getY() + 2.0D, session.exitPortalPos.getZ() + 0.5D, 15, 0.8D, 1.0D, 0.5D, 0.1D);
            if (player != null) {
                AwakeningSystemMod.notify(player, "awakening_system.notification.seal_broken.title", "awakening_system.notification.seal_broken.desc", 0xFF50FA7B);
                player.displayClientMessage(Component.translatable("awakening_system.action.red_gate_open"), true);
            }
        }

        session.combatActive = false;
        if (player != null) {
            player.playNotifySound(ModSounds.SYSTEM_QUEST_COMPLETE.value(), SoundSource.PLAYERS, 1.2F, 1.0F);
            player.playNotifySound(ModSounds.SYSTEM_RANK_UP.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("awakening_system.action.dungeon_cleared"), true);
            SystemEvents.triggerConstellationReaction(player, ConstellationDefinitions.ReactionType.BOSS_KILL);
            ConstellationAscension.checkRedGateClear(player, session.rank);

            SystemData data = SystemEvents.data(player);
            if (getRankTier(session.rank) >= 5 && data.level() >= 100 && (data.sponsorConstellation() == null || data.sponsorConstellation().isEmpty())) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenSponsorScreenPayload(false));
                AwakeningSystemMod.notify(player, "awakening_system.notification.constellation_ceremony.title",
                        "awakening_system.notification.constellation_ceremony.message", 0xFFFFD700);
            }
        }

        if (session.bossEvent != null) {
            session.bossEvent.setColor(BossEvent.BossBarColor.GREEN);
            session.bossEvent.setProgress(1.0F);
            session.bossEvent.setName(Component.translatable("awakening_system.dungeon.total_victory"));
        }

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, altarPos.getX() + 0.5D, altarPos.getY() + 1.2D, altarPos.getZ() + 0.5D,
                120, 0.8D, 1.2D, 0.8D, 0.4D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, altarPos.getX() + 0.5D, altarPos.getY() + 0.6D, altarPos.getZ() + 0.5D,
                60, 0.5D, 0.6D, 0.5D, 0.05D);
        level.sendParticles(ParticleTypes.PORTAL, altarPos.getX() + 0.5D, altarPos.getY() + 1.0D, altarPos.getZ() + 0.5D,
                80, 0.6D, 0.8D, 0.6D, 0.1D);
    }

    public static boolean onRightClickChest(ServerPlayer player, BlockPos clickedPos) {
        DungeonSession session = SESSIONS.get(player.getUUID());
        if (session == null) return false;

        // Clic derecho en el portal de retorno en Sala 1
        if (session.exitPortalPos != null && Math.abs(clickedPos.getX() - session.exitPortalPos.getX()) <= 2
                && Math.abs(clickedPos.getZ() - session.exitPortalPos.getZ()) <= 2
                && clickedPos.getY() >= session.exitPortalPos.getY() && clickedPos.getY() <= session.exitPortalPos.getY() + 4) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                if (session.isRedGate && !session.chestSpawned && !session.chestClaimed) {
                    player.displayClientMessage(Component.translatable("awakening_system.action.red_gate_blocked"), true);
                    player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 1.0F, 0.8F);
                    player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.7F);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.action.crossing_exit"), true);
                    exitDungeon(server, player.getUUID());
                }
                return true;
            }
        }


        if (!session.chestSpawned || session.chestClaimed) return false;
        if (!session.chestPos.equals(clickedPos)) return false;

        // Iniciar la Ruleta Física en el Mundo (Estilo Crate de Servidores P2W - Sin UI que tape la pantalla)
        if (!session.rouletteSpinning) {
            startInWorldRoulette(player, session);
            return true;
        }

        return true;
    }

    private static void startInWorldRoulette(ServerPlayer player, DungeonSession session) {
        session.rouletteSpinning = true;
        session.rouletteTicks = 0;
        session.claimedTicks = 0;
        session.returnCountdown = -1; // Asegurar que NO corre ningún temporizador de salida mientras gira

        if (session.bossEvent != null) {
            session.bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
            session.bossEvent.setProgress(1.0F);
            session.bossEvent.setName(Component.translatable("awakening_system.action.roulette_spinning_short"));
        }

        ServerLevel level = (ServerLevel) player.level();
        var rng = player.getRandom();
        int tier = getRankTier(session.rank);

        int coinsWon = switch (tier) {
            case 13 -> 60000;
            case 12 -> 45000;
            case 11 -> 30000;
            case 10 -> 22000;
            case 9 -> 16000;
            case 8 -> 12000;
            case 7 -> 8000;
            case 6 -> 5000;
            case 5 -> 3500;
            case 4 -> 1500;
            case 3 -> 800;
            case 2 -> 450;
            case 1 -> 250;
            default -> 120;
        };

        int expWon = switch (tier) {
            case 13 -> 120000;
            case 12 -> 85000;
            case 11 -> 55000;
            case 10 -> 38000;
            case 9 -> 26000;
            case 8 -> 20000;
            case 7 -> 14000;
            case 6 -> 9000;
            case 5 -> 6000;
            case 4 -> 3200;
            case 3 -> 1600;
            case 2 -> 900;
            case 1 -> 500;
            default -> 250;
        };

        session.finalCoinsWon = coinsWon;
        session.finalExpWon = expWon;
        session.finalRewardStacks.clear();

        List<ItemStack> pool = new ArrayList<>();
        if (tier >= 12) {
            // SUPREME CREATOR & ABSOLUTE GOD: Recompensas divinas definitivas
            pool.add(new ItemStack(Items.NETHERITE_BLOCK, tier >= 13 ? 8 : 5));
            pool.add(new ItemStack(Items.MACE, 1));
            pool.add(new ItemStack(Items.HEAVY_CORE, tier >= 13 ? 4 : 3));
            pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, tier >= 13 ? 32 : 24));
            pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, tier >= 13 ? 8 : 6));
            pool.add(new ItemStack(Items.NETHER_STAR, tier >= 13 ? 8 : 6));
            pool.add(new ItemStack(Items.ELYTRA, 2));
            pool.add(new ItemStack(Items.DIAMOND_BLOCK, tier >= 13 ? 32 : 20));
        } else if (tier >= 10) {
            // MONARCH & NATIONAL: Solo items supremos / míticos
            pool.add(new ItemStack(Items.NETHERITE_BLOCK, 3));
            pool.add(new ItemStack(Items.MACE, 1));
            pool.add(new ItemStack(Items.HEAVY_CORE, 2));
            pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 16));
            pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 4));
            pool.add(new ItemStack(Items.NETHER_STAR, 4));
            pool.add(new ItemStack(Items.ELYTRA, 1));
            pool.add(new ItemStack(Items.DIAMOND_BLOCK, 12));
        } else if (tier >= 8) {
            // SSS & SS: Recompensas legendarias de élite
            pool.add(new ItemStack(Items.NETHERITE_BLOCK, 1));
            pool.add(new ItemStack(Items.NETHERITE_INGOT, 4));
            pool.add(new ItemStack(Items.MACE, 1));
            pool.add(new ItemStack(Items.HEAVY_CORE, 1));
            pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 8));
            pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 3));
            pool.add(new ItemStack(Items.NETHER_STAR, 2));
            pool.add(new ItemStack(Items.ELYTRA, 1));
            pool.add(new ItemStack(Items.DIAMOND_BLOCK, 8));
        } else if (tier == 7) {
            // S: Solo recompensas legendarias / jackpot garantizado
            pool.add(new ItemStack(Items.NETHERITE_INGOT, 3));
            pool.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 6));
            pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 2));
            pool.add(new ItemStack(Items.HEAVY_CORE, 1));
            pool.add(new ItemStack(Items.MACE, 1));
            pool.add(new ItemStack(Items.ELYTRA, 1));
            pool.add(new ItemStack(Items.DIAMOND_BLOCK, 4));
            pool.add(new ItemStack(Items.TRIDENT, 1));
            pool.add(new ItemStack(Items.NETHER_STAR, 1));
        } else if (tier == 6) {
            // A: Se mantiene como estaba
            pool.add(new ItemStack(Items.NETHERITE_SCRAP, 4));
            pool.add(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
            pool.add(new ItemStack(Items.DIAMOND_BLOCK, 2));
            pool.add(new ItemStack(Items.GOLDEN_APPLE, 6));
            pool.add(new ItemStack(Items.ENDER_PEARL, 16));
            pool.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 32));
            pool.add(new ItemStack(Items.DIAMOND, 16));
        } else if (tier == 5) {
            // B: Se mantiene como estaba
            pool.add(new ItemStack(Items.DIAMOND, 8));
            pool.add(new ItemStack(Items.GOLDEN_APPLE, 4));
            pool.add(new ItemStack(Items.ENDER_PEARL, 8));
            pool.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 16));
            pool.add(new ItemStack(Items.EMERALD_BLOCK, 3));
            pool.add(new ItemStack(Items.GOLD_BLOCK, 4));
        } else if (tier == 4) {
            // C: Se mantiene como estaba
            pool.add(new ItemStack(Items.DIAMOND, 4));
            pool.add(new ItemStack(Items.GOLDEN_APPLE, 2));
            pool.add(new ItemStack(Items.GOLD_INGOT, 12));
            pool.add(new ItemStack(Items.ENDER_PEARL, 4));
            pool.add(new ItemStack(Items.IRON_BLOCK, 3));
        } else if (tier == 3) {
            // D: Se mantiene como estaba
            pool.add(new ItemStack(Items.GOLD_INGOT, 8));
            pool.add(new ItemStack(Items.IRON_INGOT, 16));
            pool.add(new ItemStack(Items.GOLDEN_APPLE, 1));
            pool.add(new ItemStack(Items.LAPIS_LAZULI, 24));
            pool.add(new ItemStack(Items.COOKED_BEEF, 16));
        } else {
            // E / F: Se mantiene como estaba
            pool.add(new ItemStack(Items.IRON_INGOT, 10));
            pool.add(new ItemStack(Items.GOLDEN_APPLE, 1));
            pool.add(new ItemStack(Items.BREAD, 16));
            pool.add(new ItemStack(Items.TORCH, 32));
            pool.add(new ItemStack(Items.COAL, 16));
        }

        session.finalWinningStack = pool.get(rng.nextInt(pool.size()));
        // Entrega de 1 SOLO ITEM ESTRICTO (sin acumular otros objetos)
        session.finalRewardStacks.add(session.finalWinningStack);

        // Crear la entidad de ítem flotante sobre el Altar
        double ix = session.chestPos.getX() + 0.5D;
        double iy = session.chestPos.getY() + 1.25D;
        double iz = session.chestPos.getZ() + 0.5D;

        ItemEntity item = new ItemEntity(level, ix, iy, iz, new ItemStack(Items.DIAMOND));
        item.setNoGravity(true);
        item.setNeverPickUp();
        item.setInvulnerable(true);
        item.setGlowingTag(true);
        item.setDeltaMovement(0, 0.015, 0);
        level.addFreshEntity(item);
        session.rouletteItemEntity = item;

        level.playSound(null, ix, iy, iz, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.3F);
        level.playSound(null, ix, iy, iz, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.8F, 1.5F);

        player.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning"), true);
    }

    public static void tick(MinecraftServer server) {
        List<UUID> toExit = new ArrayList<>();

        for (DungeonSession session : SESSIONS.values()) {
            ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);
            if (player == null || !player.isAlive()) {
                toExit.add(session.playerId);
                continue;
            }

            ServerLevel level = server.getLevel(session.dungeonDimension);
            if (level == null) continue;

            // Failsafe Watchdog: Reubicar al jugador si terminó fuera de los límites o en el vacío/bloques
            if (isPlayerOutOfBounds(session, player)) {
                rescuePlayerToBounds(session, player);
            }

            // Reconciliación continua de entidades y apertura automática de barreras
            if (session.combatActive) {
                if (session.enemyUUIDs != null && !session.enemyUUIDs.isEmpty()) {
                    boolean changed = session.enemyUUIDs.removeIf(uuid -> {
                        net.minecraft.world.entity.Entity ent = level.getEntity(uuid);
                        return ent == null || !ent.isAlive() || ent.isRemoved();
                    });
                    if (changed && session.bossEvent != null) {
                        int remaining = session.enemyUUIDs.size();
                        session.bossEvent.setProgress((float) remaining / Math.max(1, session.totalEnemies));
                    }
                }

                // Marcado en ROJO del ÚLTIMO enemigo restante de la fase
                if (session.enemyUUIDs != null && session.enemyUUIDs.size() == 1) {
                    UUID lastEnemyId = session.enemyUUIDs.iterator().next();
                    net.minecraft.world.entity.Entity lastEnt = level.getEntity(lastEnemyId);
                    if (lastEnt instanceof net.minecraft.world.entity.LivingEntity lastLiving && lastLiving.isAlive()) {
                        lastLiving.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.GLOWING, 100, 0, false, false));
                        try {
                            net.minecraft.world.scores.Scoreboard sb = level.getScoreboard();
                            net.minecraft.world.scores.PlayerTeam team = sb.getPlayerTeam("dungeon_last_target");
                            if (team == null) {
                                team = sb.addPlayerTeam("dungeon_last_target");
                                team.setColor(net.minecraft.ChatFormatting.RED);
                            }
                            String member = lastLiving.getScoreboardName();
                            if (!team.getPlayers().contains(member)) {
                                sb.addPlayerToTeam(member, team);
                            }
                        } catch (Exception ignored) {}

                        if (!session.lastEnemyNotified) {
                            session.lastEnemyNotified = true;
                            session.lastEnemyUUID = lastEnemyId;
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("awakening_system.action.last_enemy_marked"), true);
                                player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.9F, 1.4F);
                                player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 1.2F);
                            }
                        }

                        if (server.getTickCount() % 5 == 0) {
                            level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                                    lastLiving.getX(), lastLiving.getY() + lastLiving.getEyeHeight() + 0.4D, lastLiving.getZ(),
                                    6, 0.25D, 0.4D, 0.25D, 0.05D);
                            level.sendParticles(ParticleTypes.FLAME,
                                    lastLiving.getX(), lastLiving.getY() + 1.0D, lastLiving.getZ(),
                                    2, 0.15D, 0.2D, 0.15D, 0.02D);
                        }
                    }
                }

                if (session.enemyUUIDs != null && session.enemyUUIDs.isEmpty()) {
                    if (session.currentFloor == 1 && !session.barrier1Open) {
                        openPhase1Barrier(session, player);
                    } else if (session.currentFloor == 2 && !session.barrier2Open) {
                        openPhase2Barrier(session, player);
                    } else if (session.currentFloor == 3 && !session.barrier3Open) {
                        openPhase3Barrier(session, player);
                    } else if (session.currentFloor == 4 && !session.barrier4Open) {
                        openPhase4Barrier(session, player);
                    } else if (session.currentFloor >= 5 && session.combatActive && !session.chestSpawned) {
                        completeDungeonCombat(session, player);
                    }
                }
            }

            // Tiempo límite de combate para derrotar a los enemigos de la sala
            if (session.combatActive && !session.enemyUUIDs.isEmpty()) {
                session.combatTimer--;
                if (session.bossEvent != null && session.combatTimer % 10 == 0) {
                    int remaining = session.enemyUUIDs.size();
                    int secRemaining = Math.max(0, (session.combatTimer + 19) / 20);
                    int mins = secRemaining / 60;
                    int secs = secRemaining % 60;
                    String timeStr = String.format("%02d:%02d", mins, secs);
                    if (session.currentFloor == 5) {
                        session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, 5, 1, timeStr));
                        if (!session.enemyUUIDs.isEmpty()) {
                            UUID bossUuid = session.enemyUUIDs.iterator().next();
                            net.minecraft.world.entity.Entity e = level.getEntity(bossUuid);
                            if (e instanceof net.minecraft.world.entity.LivingEntity le && le.getMaxHealth() > 0) {
                                session.bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, le.getHealth() / le.getMaxHealth())));
                            }
                        }
                    } else {
                        session.bossEvent.setName(formatPhaseBossName(session.rank, session.isRedGate, session.currentFloor, remaining, timeStr));
                        session.bossEvent.setProgress((float) remaining / Math.max(1, session.totalEnemies));
                    }
                }

                if (session.combatTimer == 1200) {
                    player.displayClientMessage(Component.translatable("awakening_system.action.warning_one_minute"), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
                } else if (session.combatTimer <= 100 && session.combatTimer % 20 == 0 && session.combatTimer > 0) {
                    int sec = (session.combatTimer + 19) / 20;
                    player.displayClientMessage(Component.translatable("awakening_system.action.imminent_collapse", sec), true);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, 1.5F);
                }

                if (session.combatTimer <= 0) {
                    player.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.9F, 0.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.action.time_expired"), true);
                    toExit.add(session.playerId);
                    continue;
                }
            }

            // 1. Lógica de la Ruleta Física estilo P2W Server Crate
            if (session.rouletteSpinning && session.rouletteItemEntity != null) {
                session.rouletteTicks++;

                ItemStack[] candidates = new ItemStack[]{
                        new ItemStack(Items.NETHERITE_INGOT, 2),
                        new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2),
                        new ItemStack(Items.TOTEM_OF_UNDYING, 1),
                        new ItemStack(Items.DIAMOND_BLOCK, 2),
                        new ItemStack(Items.GOLDEN_APPLE, 4),
                        new ItemStack(Items.DIAMOND, 8),
                        new ItemStack(Items.EXPERIENCE_BOTTLE, 16),
                        new ItemStack(Items.NETHERITE_SCRAP, 3),
                        new ItemStack(Items.GOLD_INGOT, 12),
                        new ItemStack(Items.ENDER_PEARL, 6),
                        new ItemStack(Items.EMERALD_BLOCK, 2)
                };

                // Desaceleración del ciclo de ítems
                int cycleInterval = session.rouletteTicks < 30 ? 2 : (session.rouletteTicks < 50 ? 4 : (session.rouletteTicks < 65 ? 7 : 12));
                if (session.rouletteTicks % cycleInterval == 0 && session.rouletteTicks < 75) {
                    ItemStack nextCandidate = candidates[(session.rouletteTicks / cycleInterval) % candidates.length];
                    session.rouletteItemEntity.setItem(nextCandidate.copy());
                    level.playSound(null, session.chestPos.getX(), session.chestPos.getY(), session.chestPos.getZ(),
                            ModSounds.ROULETTE_SPIN.value(), SoundSource.BLOCKS, 0.7F, 1.0F + (float) session.rouletteTicks / 75.0F);
                    level.sendParticles(ParticleTypes.ENCHANT,
                            session.rouletteItemEntity.getX(), session.rouletteItemEntity.getY() + 0.3, session.rouletteItemEntity.getZ(),
                            8, 0.2, 0.2, 0.2, 0.1);
                }

                // El ítem flota hacia arriba poco a poco
                session.rouletteItemEntity.setDeltaMovement(0, Math.max(0.002, 0.016 * (1.0 - (double) session.rouletteTicks / 80.0)), 0);

                // Ruleta se detiene exactamente en el ítem ganador (tick 75)
                if (session.rouletteTicks == 75) {
                    session.rouletteSpinning = false;
                    session.chestClaimed = true;

                    session.rouletteItemEntity.setItem(session.finalWinningStack.copy());
                    session.rouletteItemEntity.setDeltaMovement(0, 0, 0);

                    // Explosión de partículas y fanfarria
                    level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            session.rouletteItemEntity.getX(), session.rouletteItemEntity.getY() + 0.3, session.rouletteItemEntity.getZ(),
                            100, 0.5, 0.6, 0.5, 0.3);
                    level.sendParticles(ParticleTypes.FIREWORK,
                            session.rouletteItemEntity.getX(), session.rouletteItemEntity.getY() + 0.3, session.rouletteItemEntity.getZ(),
                            50, 0.4, 0.5, 0.4, 0.15);
                    level.sendParticles(ParticleTypes.PORTAL,
                            session.rouletteItemEntity.getX(), session.rouletteItemEntity.getY() + 0.3, session.rouletteItemEntity.getZ(),
                            60, 0.6, 0.6, 0.6, 0.2);

                    level.playSound(null, session.chestPos.getX(), session.chestPos.getY(), session.chestPos.getZ(),
                            ModSounds.ROULETTE_WIN.value(), SoundSource.PLAYERS, 1.3F, 1.0F);
                    level.playSound(null, session.chestPos.getX(), session.chestPos.getY(), session.chestPos.getZ(),
                            ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    // Otorgar recompensas al inventario y sistema
                    SystemData data = SystemEvents.data(player);
                    data.addCoins(session.finalCoinsWon);
                    data.addExperience(session.finalExpWon);
                    data.recordStatistic("missions_completed", 1);
                    SystemEvents.checkAchievements(player, data);
                    AwakeningSystemMod.sync(player);

                    for (ItemStack stack : session.finalRewardStacks) {
                        if (!player.getInventory().add(stack.copy())) {
                            player.drop(stack.copy(), false);
                        }
                    }

                    player.displayClientMessage(Component.translatable("awakening_system.dungeon.roulette_prize", session.finalWinningStack.getHoverName().getString(), session.finalWinningStack.getCount(), session.finalCoinsWon, session.finalExpWon), true);

                    // El Altar pasa a ser el Portal de Retorno sin límite de tiempo (sales tú mismo por el portal)
                    session.returnCountdown = -1;
                    session.claimedTicks = 0;

                    if (session.bossEvent != null) {
                        session.bossEvent.setColor(BossEvent.BossBarColor.BLUE);
                        session.bossEvent.setProgress(1.0F);
                        session.bossEvent.setName(Component.translatable("awakening_system.dungeon.gate_conquered"));
                    }
                }
            }

            // Partículas y alerta de murallas mágicas (4 Murallas Continuas)
            if (!session.barrier1Open && !session.barrier1Blocks.isEmpty()) {
                if (server.getTickCount() % 10 == 0) {
                    for (BlockPos bp : session.barrier1Blocks) {
                        if (level.random.nextFloat() < 0.20F) {
                            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.2D, 0.2D, 0.2D, 0.02D);
                            level.sendParticles(ParticleTypes.PORTAL, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                    }
                }
                if (player != null && session.barrier1Pos != null) {
                    double distSq = player.distanceToSqr(session.barrier1Pos.getX() + 0.5D, session.barrier1Pos.getY() + 1.0D, session.barrier1Pos.getZ() + 0.5D);
                    if (distSq <= 25.0D) {
                        if (session.enemyUUIDs.isEmpty()) {
                            openPhase1Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30) {
                            session.lastBarrierWarningTick = server.getTickCount();
                            int rem = session.enemyUUIDs.size();
                            player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_1", rem), true);
                            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                    }
                    if (player.getZ() > session.barrier1Pos.getZ() + 1.0D && !session.enemyUUIDs.isEmpty()) {
                        player.teleportTo(level, session.barrier1Pos.getX() + 0.5D, session.barrier1Pos.getY() + 1.0D, session.barrier1Pos.getZ() - 2.5D, player.getYRot(), player.getXRot());
                        player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_1"), true);
                    }
                }
            } else if (session.barrier1Open && !session.barrier2Open && !session.barrier2Blocks.isEmpty()) {
                if (server.getTickCount() % 10 == 0) {
                    for (BlockPos bp : session.barrier2Blocks) {
                        if (level.random.nextFloat() < 0.20F) {
                            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.2D, 0.2D, 0.2D, 0.02D);
                            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                    }
                }
                if (player != null && session.barrier2Pos != null) {
                    double distSq = player.distanceToSqr(session.barrier2Pos.getX() + 0.5D, session.barrier2Pos.getY() + 1.0D, session.barrier2Pos.getZ() + 0.5D);
                    if (distSq <= 25.0D) {
                        if (session.enemyUUIDs.isEmpty()) {
                            openPhase2Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30) {
                            session.lastBarrierWarningTick = server.getTickCount();
                            int rem = session.enemyUUIDs.size();
                            player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_2", rem), true);
                            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                    }
                    if (player.getZ() > session.barrier2Pos.getZ() + 1.0D && !session.enemyUUIDs.isEmpty()) {
                        player.teleportTo(level, session.barrier2Pos.getX() + 0.5D, session.barrier2Pos.getY() + 1.0D, session.barrier2Pos.getZ() - 2.5D, player.getYRot(), player.getXRot());
                        player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_2"), true);
                    }
                }
            } else if (session.barrier2Open && !session.barrier3Open && !session.barrier3Blocks.isEmpty()) {
                if (server.getTickCount() % 10 == 0) {
                    for (BlockPos bp : session.barrier3Blocks) {
                        if (level.random.nextFloat() < 0.20F) {
                            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.2D, 0.2D, 0.2D, 0.02D);
                            level.sendParticles(ParticleTypes.FLAME, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                    }
                }
                if (player != null && session.barrier3Pos != null) {
                    double distSq = player.distanceToSqr(session.barrier3Pos.getX() + 0.5D, session.barrier3Pos.getY() + 1.0D, session.barrier3Pos.getZ() + 0.5D);
                    if (distSq <= 25.0D) {
                        if (session.enemyUUIDs.isEmpty()) {
                            openPhase3Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30) {
                            session.lastBarrierWarningTick = server.getTickCount();
                            int rem = session.enemyUUIDs.size();
                            player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_3", rem), true);
                            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                    }
                    if (player.getZ() > session.barrier3Pos.getZ() + 1.0D && !session.enemyUUIDs.isEmpty()) {
                        player.teleportTo(level, session.barrier3Pos.getX() + 0.5D, session.barrier3Pos.getY() + 1.0D, session.barrier3Pos.getZ() - 2.5D, player.getYRot(), player.getXRot());
                        player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_3"), true);
                    }
                }
            } else if (session.barrier3Open && !session.barrier4Open && !session.barrier4Blocks.isEmpty()) {
                if (server.getTickCount() % 10 == 0) {
                    for (BlockPos bp : session.barrier4Blocks) {
                        if (level.random.nextFloat() < 0.20F) {
                            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.2D, 0.2D, 0.2D, 0.02D);
                            level.sendParticles(ParticleTypes.DRAGON_BREATH, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                    }
                }
                if (player != null && session.barrier4Pos != null) {
                    double distSq = player.distanceToSqr(session.barrier4Pos.getX() + 0.5D, session.barrier4Pos.getY() + 1.0D, session.barrier4Pos.getZ() + 0.5D);
                    if (distSq <= 25.0D) {
                        if (session.enemyUUIDs.isEmpty()) {
                            openPhase4Barrier(session, player);
                        } else if (server.getTickCount() - session.lastBarrierWarningTick > 30) {
                            session.lastBarrierWarningTick = server.getTickCount();
                            int rem = session.enemyUUIDs.size();
                            player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_4", rem), true);
                            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F);
                        }
                    }
                    if (player.getZ() > session.barrier4Pos.getZ() + 1.0D && !session.enemyUUIDs.isEmpty()) {
                        player.teleportTo(level, session.barrier4Pos.getX() + 0.5D, session.barrier4Pos.getY() + 1.0D, session.barrier4Pos.getZ() - 2.5D, player.getYRot(), player.getXRot());
                        player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_4"), true);
                    }
                }
            }

            // Partículas del Altar de la Ruleta (antes de girar)
            if (session.chestSpawned && !session.chestClaimed && session.chestPos != null) {
                level.sendParticles(ParticleTypes.PORTAL,
                        session.chestPos.getX() + 0.5D, session.chestPos.getY() + 0.8D, session.chestPos.getZ() + 0.5D,
                        8, 0.4D, 0.5D, 0.4D, 0.1D);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        session.chestPos.getX() + 0.5D, session.chestPos.getY() + 0.6D, session.chestPos.getZ() + 0.5D,
                        4, 0.3D, 0.3D, 0.3D, 0.02D);
            }

            // Partículas del portal de retorno y temporizador de salida (sólo tras conseguir el objeto)
            if (session.chestClaimed && session.chestPos != null) {
                session.claimedTicks++;

                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        session.chestPos.getX() + 0.5D, session.chestPos.getY() + 0.8D, session.chestPos.getZ() + 0.5D,
                        6, 0.4D, 0.5D, 0.4D, 0.05D);
                level.sendParticles(ParticleTypes.DRAGON_BREATH,
                        session.chestPos.getX() + 0.5D, session.chestPos.getY() + 0.6D, session.chestPos.getZ() + 0.5D,
                        2, 0.2D, 0.2D, 0.2D, 0.01D);

            }

            // Portal de Salida Seguro en Sala 1 (Antecámara)
            if (session.exitPortalPos != null) {
                double dx = player.getX() - (session.exitPortalPos.getX() + 0.5D);
                double dy = player.getY() - session.exitPortalPos.getY();
                double dz = player.getZ() - (session.exitPortalPos.getZ() + 0.5D);
                double distSq = dx * dx + dy * dy + dz * dz;

                // Partículas ambientales del portal
                if (server.getTickCount() % 6 == 0) {
                    if (session.isRedGate && !session.chestSpawned && !session.chestClaimed) {
                        level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                                session.exitPortalPos.getX() + 0.5D, session.exitPortalPos.getY() + 2.0D, session.exitPortalPos.getZ() + 0.5D,
                                5, 0.6D, 0.8D, 0.2D, 0.02D);
                        level.sendParticles(ParticleTypes.SMOKE,
                                session.exitPortalPos.getX() + 0.5D, session.exitPortalPos.getY() + 2.0D, session.exitPortalPos.getZ() + 0.5D,
                                3, 0.4D, 0.6D, 0.2D, 0.01D);
                    } else {
                        level.sendParticles(ParticleTypes.PORTAL,
                                session.exitPortalPos.getX() + 0.5D, session.exitPortalPos.getY() + 2.0D, session.exitPortalPos.getZ() + 0.5D,
                                8, 0.6D, 0.8D, 0.2D, 0.05D);
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                session.exitPortalPos.getX() + 0.5D, session.exitPortalPos.getY() + 2.0D, session.exitPortalPos.getZ() + 0.5D,
                                4, 0.5D, 0.7D, 0.2D, 0.02D);
                    }
                }

                // Interacción al cruzar el portal (< 1.0 bloque horizontal para no ser absorbido al pasar al lado)
                double horizDistSq = dx * dx + dz * dz;
                if (horizDistSq <= 1.0D * 1.0D && dy >= -0.5D && dy <= 3.2D) {
                    if (session.isRedGate && !session.chestSpawned && !session.chestClaimed) {
                        if (server.getTickCount() - session.lastExitWarningTick > 40) {
                            session.lastExitWarningTick = server.getTickCount();
                            player.displayClientMessage(Component.translatable("awakening_system.action.red_gate_blocked"), true);
                            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 1.0F, 0.8F);
                            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.7F);
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("awakening_system.action.crossing_exit"), true);
                        toExit.add(session.playerId);
                        continue;
                    }
                }
            }
        }

        for (UUID id : toExit) {
            exitDungeon(server, id);
        }

        // Failsafe Watchdog: Rescatar periódicamente a jugadores varados en DUNGEON_REALM sin sesión
        if (server.getTickCount() % 20 == 0) {
            ServerLevel dLevel = server.getLevel(DUNGEON_REALM);
            if (dLevel != null) {
                for (ServerPlayer sp : new ArrayList<>(dLevel.players())) {
                    if (!sp.isSpectator() && !isInDungeon(sp.getUUID())) {
                        rescueStrandedPlayer(sp);
                    }
                }
            }
        }

        // Desborde de Mazmorras (Dungeon Break) cuando expira el tiempo de una Falla sin conquistar
        long currentTick = server.overworld().getGameTime();
        List<WorldRift> expiredRifts = new ArrayList<>();
        for (WorldRift rift : WORLD_RIFTS) {
            if (currentTick > rift.expireTick) {
                expiredRifts.add(rift);
            }
        }
        for (WorldRift expired : expiredRifts) {
            triggerDungeonBreak(server, expired);
        }

        for (WorldRift rift : WORLD_RIFTS) {
            ServerLevel riftLevel = server.getLevel(rift.dimension);
            if (riftLevel == null) continue;

            double cx = rift.pos.getX() + 0.5D;
            double cy = rift.pos.getY() + 2.2D;
            double cz = rift.pos.getZ() + 0.5D;
            boolean isRedGate = rift.isRedGate;

            // Sonido ambiental de vórtice dimensional (sonido custom profundo, sin pitidos)
            if (riftLevel.getGameTime() % 40 == 0) {
                riftLevel.playSound(null, cx, cy, cz, ModSounds.GATE_AMBIENT.value(), SoundSource.AMBIENT, 0.45F, isRedGate ? 0.7F : 0.85F);
            }

            for (ServerPlayer player : riftLevel.players()) {
                if (isInDungeon(player.getUUID())) continue;
                if (EXIT_COOLDOWNS.getOrDefault(player.getUUID(), 0L) > server.getTickCount()) continue;
                double dx = player.getX() - cx;
                double dy = (player.getY() + 1.0D) - cy;
                double dz = player.getZ() - cz;
                double horizDistSq = dx * dx + dz * dz;
                // Entrada al vórtice: exactamente a 1 bloque de distancia para evitar ser absorbido al pasar al lado
                if (horizDistSq <= 1.0D * 1.0D && Math.abs(dy) < 3.5D) {
                    player.displayClientMessage(Component.translatable("awakening_system.dungeon.entering_rift", getGateTitle(rift.rank, rift.isRedGate)), true);
                    player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    // NO removemos la falla de WORLD_RIFTS para que persista si el jugador sale
                    startDungeon(player, rift.rank, rift.biome, rift.isRedGate, rift);
                    break;
                } else if (horizDistSq < 6.0D * 6.0D && player.tickCount % 40 == 0) {
                    player.displayClientMessage(Component.translatable("awakening_system.dungeon.rift_nearby_prompt", getGateTitle(rift.rank, rift.isRedGate)), true);
                }
            }
        }
        if (server.getTickCount() % 40 == 0) {
            syncRiftsToAll(server);
        }
    }

    public static void exitDungeon(MinecraftServer server, UUID playerId) {
        DungeonSession session = SESSIONS.remove(playerId);
        if (session == null) return;

        if (session.lastEnemyUUID != null) {
            try {
                net.minecraft.world.scores.Scoreboard sb = server.getScoreboard();
                net.minecraft.world.scores.PlayerTeam team = sb.getPlayerTeam("dungeon_last_target");
                if (team != null) {
                    sb.removePlayerFromTeam(session.lastEnemyUUID.toString(), team);
                }
            } catch (Exception ignored) {}
        }

        if (session.rouletteItemEntity != null) {
            session.rouletteItemEntity.discard();
            session.rouletteItemEntity = null;
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        ServerLevel returnLevel = server.getLevel(session.returnDimension);
        if (returnLevel == null) returnLevel = server.getLevel(Level.OVERWORLD);

        EXIT_COOLDOWNS.put(playerId, server.getTickCount() + 140L); // 7 segundos de inmunidad a ser reabsorbido por fallas

        if (session.bossEvent != null) {
            if (player != null) session.bossEvent.removePlayer(player);
            session.bossEvent.removeAllPlayers();
            session.bossEvent.setVisible(false);
        }

        // Posición de reaparición segura alejada 4.5 bloques del portal
        double spawnX = session.returnPos.x;
        double spawnY = session.returnPos.y;
        double spawnZ = session.returnPos.z;
        if (session.sourceRift != null && session.sourceRift.pos != null) {
            spawnX = session.sourceRift.pos.getX() + 0.5D;
            spawnZ = session.sourceRift.pos.getZ() - 4.5D;
            spawnY = findSafeFloorY(returnLevel, spawnX, session.sourceRift.pos.getY(), spawnZ);
        }

        if (player != null && returnLevel != null) {
            player.teleportTo(returnLevel, spawnX, spawnY, spawnZ, session.returnYaw, session.returnPitch);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.7F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.action.returned_safe"), true);
        }

        // Gestión de persistencia del Portal en el Overworld
        if (session.sourceRift != null) {
            if (session.chestClaimed || !session.combatActive) {
                // Calabozo conquistado (jefe derrotado): se cierra la falla dimensional definitivamente
                WORLD_RIFTS.remove(session.sourceRift);
                syncRiftsToAll(server);
                if (returnLevel != null && session.sourceRift.pos != null) {
                    returnLevel.playSound(null, session.sourceRift.pos.getX() + 0.5D, session.sourceRift.pos.getY() + 1.0D, session.sourceRift.pos.getZ() + 0.5D,
                            ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.BLOCKS, 1.0F, 0.9F);
                    returnLevel.sendParticles(ParticleTypes.END_ROD,
                            session.sourceRift.pos.getX() + 0.5D, session.sourceRift.pos.getY() + 2.0D, session.sourceRift.pos.getZ() + 0.5D,
                            60, 1.5D, 2.0D, 1.5D, 0.1D);
                }
            } else {
                // Salió sin conquistar: el portal dimensional permanece activo en el exterior
                if (player != null) {
                    player.displayClientMessage(Component.translatable("awakening_system.dungeon.left_unconquered"), false);
                }
            }
        }

        ServerLevel dungeonLevel = server.getLevel(session.dungeonDimension);
        if (dungeonLevel != null) {
            // 1. Descartar entidades enemigas rastreadas
            for (UUID enemyId : session.enemyUUIDs) {
                Entity enemy = dungeonLevel.getEntity(enemyId);
                if (enemy != null) {
                    enemy.discard();
                }
            }
            // 2. Limpiar todos los bloques registrados de la mazmorra
            for (BlockPos p : session.arenaBlocks) {
                dungeonLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            }
            // 3. Purgar entidades residuales (drops, mobs, etc.) y bloques en el volumen completo
            if (session.arenaCenter != null) {
                purgeDungeonVolume(dungeonLevel, session.arenaCenter);
            }
        }
    }

    public static void purgeDungeonVolume(ServerLevel dungeonLevel, BlockPos center) {
        if (dungeonLevel == null || center == null) return;
        int minX = center.getX() - 95;
        int maxX = center.getX() + 95;
        int minY = 20;
        int maxY = 115;
        int minZ = center.getZ() - 65;
        int maxZ = center.getZ() + 330;

        // 1. Purgar todas las entidades no jugadoras (mobs residuales, drops, bloques cayendo, orbes XP, etc.)
        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        List<Entity> entities = dungeonLevel.getEntities((Entity) null, box, entity -> !(entity instanceof net.minecraft.world.entity.player.Player));
        for (Entity e : entities) {
            e.discard();
        }

        // 2. Limpieza rpida y exhaustiva de bloques no aire mediante secciones de chunk cargadas
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;
        BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!dungeonLevel.hasChunk(cx, cz)) continue;
                net.minecraft.world.level.chunk.LevelChunk chunk = dungeonLevel.getChunk(cx, cz);
                net.minecraft.world.level.chunk.LevelChunkSection[] sections = chunk.getSections();
                for (int secIdx = 0; secIdx < sections.length; secIdx++) {
                    net.minecraft.world.level.chunk.LevelChunkSection sec = sections[secIdx];
                    if (sec != null && !sec.hasOnlyAir()) {
                        int secMinY = dungeonLevel.getMinBuildHeight() + (secIdx << 4);
                        int secMaxY = secMinY + 15;
                        if (secMaxY < minY || secMinY > maxY) continue;
                        int startY = Math.max(minY, secMinY);
                        int endY = Math.min(maxY, secMaxY);
                        int startX = Math.max(minX, cx << 4);
                        int endX = Math.min(maxX, (cx << 4) + 15);
                        int startZ = Math.max(minZ, cz << 4);
                        int endZ = Math.min(maxZ, (cz << 4) + 15);
                        for (int bx = startX; bx <= endX; bx++) {
                            for (int bz = startZ; bz <= endZ; bz++) {
                                for (int by = startY; by <= endY; by++) {
                                    mbp.set(bx, by, bz);
                                    if (!dungeonLevel.isEmptyBlock(mbp)) {
                                        dungeonLevel.setBlock(mbp, Blocks.AIR.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Desborde de Mazmorra (Dungeon Break):
     * Ocurre cuando expira el temporizador de una falla dimensional activa sin que los cazadores la hayan conquistado.
     * Los monstruos del calabozo colapsan el portal e invaden en horda el mundo exterior.
     */
    public static void triggerDungeonBreak(MinecraftServer server, WorldRift rift) {
        WORLD_RIFTS.remove(rift);
        syncRiftsToAll(server);

        ServerLevel riftLevel = server.getLevel(rift.dimension);
        if (riftLevel == null) riftLevel = server.getLevel(Level.OVERWORLD);
        if (riftLevel == null) return;

        BlockPos center = rift.pos;

        // Si había jugadores dentro de la sesión de este portal, expulsarlos inmediatamente
        for (DungeonSession session : new ArrayList<>(SESSIONS.values())) {
            if (session.sourceRift == rift) {
                ServerPlayer sp = server.getPlayerList().getPlayer(session.playerId);
                if (sp != null) {
                    sp.displayClientMessage(Component.translatable("awakening_system.dungeon.dungeon_break_imminent"), false);
                    sp.playNotifySound(ModSounds.DUNGEON_BREAK.value(), SoundSource.PLAYERS, 1.5F, 0.8F);
                }
                exitDungeon(server, session.playerId);
            }
        }

        // Anuncio a jugadores del mundo
        Component breakMsg = Component.translatable("awakening_system.dungeon.break_broadcast",
                rift.rank, center.getX(), center.getY(), center.getZ())
                .withStyle(ChatFormatting.BOLD);
        for (ServerPlayer player : riftLevel.players()) {
            player.sendSystemMessage(breakMsg);
            player.playNotifySound(ModSounds.DUNGEON_BREAK.value(), SoundSource.HOSTILE, 1.8F, 0.9F);
        }

        // Efectos cataclísmicos en el portal
        riftLevel.playSound(null, center.getX() + 0.5D, center.getY() + 1.5D, center.getZ() + 0.5D,
                ModSounds.DUNGEON_BREAK.value(), SoundSource.HOSTILE, 2.0F, 0.8F);
        riftLevel.playSound(null, center.getX() + 0.5D, center.getY() + 1.5D, center.getZ() + 0.5D,
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 0.6F);

        riftLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 3, 0.5, 0.5, 0.5, 0.1);
        riftLevel.sendParticles(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 60, 2.5, 2.0, 2.5, 0.15);
        riftLevel.sendParticles(ParticleTypes.DRAGON_BREATH, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 80, 2.0, 2.5, 2.0, 0.2);
        riftLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 100, 3.0, 2.5, 3.0, 0.25);

        // Generación de Horda invasora de 12 a 24 monstruos según rango
        int tier = getRankTier(rift.rank);
        int mobCount = 10 + Math.min(14, tier * 2);
        var rng = riftLevel.random;

        for (int i = 0; i < mobCount; i++) {
            double ox = (rng.nextDouble() - 0.5) * 6.0;
            double oz = (rng.nextDouble() - 0.5) * 6.0;
            double oy = rng.nextDouble() * 1.5;

            Monster mob = null;
            if (rift.isRedGate || tier >= 8) {
                float pick = rng.nextFloat();
                if (pick < 0.35F) {
                    mob = ModEntities.FROST_ELF_ASSASSIN.get().create(riftLevel);
                } else if (pick < 0.65F) {
                    mob = ModEntities.ABYSSAL_ORC.get().create(riftLevel);
                } else if (pick < 0.85F) {
                    mob = ModEntities.ABYSSAL_NECROMANCER.get().create(riftLevel);
                } else {
                    mob = EntityType.WITHER_SKELETON.create(riftLevel);
                }
            } else if (tier >= 5) {
                float pick = rng.nextFloat();
                if (pick < 0.40F) {
                    mob = ModEntities.ABYSSAL_ORC.get().create(riftLevel);
                } else if (pick < 0.70F) {
                    mob = EntityType.HUSK.create(riftLevel);
                } else {
                    mob = EntityType.STRAY.create(riftLevel);
                }
            } else {
                float pick = rng.nextFloat();
                if (pick < 0.40F) {
                    mob = EntityType.ZOMBIE.create(riftLevel);
                } else if (pick < 0.70F) {
                    mob = EntityType.SKELETON.create(riftLevel);
                } else {
                    mob = EntityType.SPIDER.create(riftLevel);
                }
            }

            if (mob != null) {
                mob.moveTo(center.getX() + 0.5D + ox, center.getY() + 1.0D + oy, center.getZ() + 0.5D + oz, rng.nextFloat() * 360.0F, 0.0F);
                double vx = (ox != 0 ? ox / Math.abs(ox) : 1.0) * (0.3 + rng.nextDouble() * 0.4);
                double vy = 0.25 + rng.nextDouble() * 0.35;
                double vz = (oz != 0 ? oz / Math.abs(oz) : 1.0) * (0.3 + rng.nextDouble() * 0.4);
                mob.setDeltaMovement(vx, vy, vz);
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1, false, false));
                applyDungeonMobMetadata(mob, rift.rank, 1, false);
                riftLevel.addFreshEntity(mob);
            }
        }
    }

    public static void checkSpawnWorldRift(ServerPlayer player) {
        if (isInDungeon(player.getUUID())) return;
        if (player.level().dimension() != Level.OVERWORLD) return;
        if (WORLD_RIFTS.size() >= 15) return;

        var rng = player.getRandom();
        if (rng.nextFloat() > 0.12F) return;

        SystemData data = SystemEvents.data(player);
        int lvl = data.level();
        String rank = lvl >= 70 ? (rng.nextBoolean() ? "S" : "A") :
                (lvl >= 50 ? (rng.nextBoolean() ? "A" : "B") :
                        (lvl >= 30 ? (rng.nextBoolean() ? "B" : "C") :
                                (lvl >= 15 ? (rng.nextBoolean() ? "C" : "D") : "D")));

        double angle = rng.nextDouble() * Math.PI * 2;
        double dist = 30.0D + rng.nextDouble() * 160.0D;
        int rx = (int) (player.getX() + Math.cos(angle) * dist);
        int rz = (int) (player.getZ() + Math.sin(angle) * dist);
        int ry = (int) player.getY();

        ServerLevel level = (ServerLevel) player.level();
        BlockPos target = null;
        int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, rx, rz);
        BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos(rx, Math.min(surfaceY, level.getMaxBuildHeight() - 5), rz);
        while (mbp.getY() > level.getMinBuildHeight() + 5) {
            BlockState state = level.getBlockState(mbp);
            BlockState below = level.getBlockState(mbp.below());
            // Si el bloque actual es aire y el de abajo es sólido (tierra, piedra, etc.) y NO son hojas
            if (state.isAir() && !below.isAir() && !(below.getBlock() instanceof LeavesBlock) && below.isSolidRender(level, mbp.below())) {
                target = mbp.immutable();
                break;
            }
            mbp.move(0, -1, 0);
        }

        if (target != null) {
            long expireTick = level.getGameTime() + 36000L; // 30 minutos mínimo activas en el mundo
            boolean isRed = (lvl >= 40 && rng.nextFloat() < 0.15F);

            String[] THEMES = {
                    "crypt", "plains", "lush_plains", "cherry_plains", "autumn_plains",
                    "tundra", "ancient_temple", "crystal_cave", "abyssal_chasm", "magma_forge"
            };
            String biome = THEMES[rng.nextInt(THEMES.length)];

            WORLD_RIFTS.add(new WorldRift(target, rank, biome, Level.OVERWORLD, expireTick, isRed));
            if (player.getServer() != null) syncRiftsToAll(player.getServer());

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.AMBIENT, 1.2F, 1.0F);
            level.sendParticles(ParticleTypes.PORTAL, target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D,
                    50, 0.5D, 0.8D, 0.5D, 0.2D);

            AwakeningSystemMod.notify(player, "awakening_system.notification.dungeon_gate.title",
                    "awakening_system.notification.rift_spawn.message", 0xFFD055FF, rank);
            player.displayClientMessage(Component.translatable("awakening_system.dungeon.rift_coords", rank, target.getX(), target.getY(), target.getZ()), false);
        }
    }

    public static void spawnRiftNear(ServerPlayer player, String rank) {
        spawnRiftNear(player, rank, "crypt");
    }

    public static void spawnRiftNear(ServerPlayer player, String rank, String biome) {
        if (isInDungeon(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.action.in_dungeon_already"), true);
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            double rad = Math.toRadians(player.getYRot());
            double lookX = -Math.sin(rad);
            double lookZ = Math.cos(rad);
            double targetX = player.getX() + lookX * 4.8D;
            double targetZ = player.getZ() + lookZ * 4.8D;
            int ry = (int) player.getY();
            int rx = (int) Math.floor(targetX);
            int rz = (int) Math.floor(targetZ);

            BlockPos target = null;
            for (int dy = 3; dy >= -5; dy--) {
                BlockPos p = new BlockPos(rx, ry + dy, rz);
                if (level.getBlockState(p).isSolid() && level.getBlockState(p.above()).isAir() && level.getBlockState(p.above(2)).isAir()) {
                    target = p.above();
                    break;
                }
            }
            if (target == null) {
                target = new BlockPos(rx, ry, rz);
            }

            long expireTick = level.getGameTime() + 36000L; // 30 minutos mínimo
            WORLD_RIFTS.add(new WorldRift(target, rank, biome, level.dimension(), expireTick, false, player.getYRot()));
            if (player.getServer() != null) syncRiftsToAll(player.getServer());

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.DUNGEON_GATE_OPEN.value(), SoundSource.AMBIENT, 1.2F, 1.0F);

            AwakeningSystemMod.notify(player, "awakening_system.notification.dungeon_gate.title",
                    "awakening_system.notification.rift_spawn.message", 0xFFD055FF, rank);
        }
    }

    public static void syncRiftsToAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncRiftsToPlayer(player);
        }
    }

    public static void syncRiftsToPlayer(ServerPlayer player) {
        List<SyncRiftsPayload.RiftEntry> entries = new ArrayList<>();
        for (WorldRift rift : WORLD_RIFTS) {
            if (rift.dimension.equals(player.level().dimension())) {
                entries.add(new SyncRiftsPayload.RiftEntry(rift.pos, rift.rank, rift.isRedGate, 0, rift.yaw));
            }
        }
        if (TowerManager.isInTower(player.getUUID())) {
            TowerManager.TowerSession ts = TowerManager.getSession(player.getUUID());
            if (ts != null && ts.isekaiPortalSpawned && ts.isekaiPortalPos != null) {
                entries.add(new SyncRiftsPayload.RiftEntry(ts.isekaiPortalPos, "ISEKAI", false, 99));
            }
        }
        if (player.level().dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
            // 1. Portal de Retorno al Overworld en las Raíces de Yggdrasil (gate_isekai.png)
            entries.add(new SyncRiftsPayload.RiftEntry(YggdrasilTreeGenerator.OVERWORLD_RETURN_GATE_POS, "ISEKAI", false, 99));
        }
        if (player.level().dimension().equals(MurimWorldManager.MURIM_WORLD)) {
            // Portal de Retorno al Reino Isekai en la Gran Pagoda de Murim (gate_murim.png unificado)
            entries.add(new SyncRiftsPayload.RiftEntry(MurimWorldManager.RETURN_PORTAL_POS.below(), "MURIM", false, 98));
        }
        // Portales de la Espiral del Abismo de Teyvat (en cualquier dimensión y posición registrada)
        for (TeyvatWorldManager.AbyssPortalEntry portal : TeyvatWorldManager.REGISTERED_PORTALS) {
            if (player.level().dimension().equals(portal.dimension)) {
                if (portal.active) {
                    // Estado ACTIVO (Imagen 1): Vórtice Cósmico Cian
                    entries.add(new SyncRiftsPayload.RiftEntry(
                            portal.centerPos,
                            "TEYVAT_ABYSS",
                            false,
                            97,
                            portal.facing.toYRot()
                    ));
                } else {
                    // Estado BLOQUEADO (Imagen 2): Sello Carmesí Perimetral y vano abierto al cielo
                    entries.add(new SyncRiftsPayload.RiftEntry(
                            portal.centerPos,
                            "TEYVAT_ABYSS_BLOCKED",
                            false,
                            96,
                            portal.facing.toYRot()
                    ));
                }
            }
        }
        DungeonSession session = SESSIONS.get(player.getUUID());
        if (session != null && session.exitPortalPos != null && player.level().dimension().equals(session.dungeonDimension)) {
            // Portal de Salida en la Sala 1 del Calabozo (tipo 1 para escala ajustada al vano del templo)
            entries.add(new SyncRiftsPayload.RiftEntry(session.exitPortalPos, session.rank, session.isRedGate, 1));
        }
        PacketDistributor.sendToPlayer(player, new SyncRiftsPayload(entries));
    }

    private static MinecraftServer getServer() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
    }
}
