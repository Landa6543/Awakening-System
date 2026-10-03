package com.soybe.awakeningsystem;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class TowerManager {
    private TowerManager() {}

    public static final int MAX_FLOORS = 100;
    public static final int ARENA_RADIUS = 16;
    public static final int CHAMBER_HEIGHT = 10;

    public static final ResourceKey<Level> RANKER_TOWER = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ranker_tower")
    );

    public static final class TowerSession {
        public final UUID playerId;
        public int floor = 1;
        public final ResourceKey<Level> returnDimension;
        public final Vec3 returnPos;
        public final float returnYaw;
        public final float returnPitch;
        public final BlockPos baseOrigin;
        public BlockPos floorOrigin;
        public final Set<UUID> enemyUUIDs = ConcurrentHashMap.newKeySet();
        public int totalEnemies;
        public ServerBossEvent bossEvent;
        public BlockPos elevatorPos;
        public BlockPos returnPortalPos;
        public BlockPos isekaiPortalPos;
        public boolean elevatorSpawned = false;
        public boolean isekaiPortalSpawned = false;
        public boolean isekaiTriggered = false;
        public int returnCountdown = -1;
        public int floorTicks = 0;

        public TowerSession(UUID playerId, ResourceKey<Level> returnDimension, Vec3 returnPos, float returnYaw, float returnPitch, BlockPos baseOrigin) {
            this.playerId = playerId;
            this.returnDimension = returnDimension;
            this.returnPos = returnPos;
            this.returnYaw = returnYaw;
            this.returnPitch = returnPitch;
            this.baseOrigin = baseOrigin;
            this.floorOrigin = baseOrigin;
        }
    }

    private static final Map<UUID, TowerSession> SESSIONS = new ConcurrentHashMap<>();
    private static final AtomicInteger ARENA_INDEX = new AtomicInteger(1);

    public static boolean isInTower(UUID playerId) {
        return SESSIONS.containsKey(playerId);
    }

    public static TowerSession getSession(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    public static boolean isTowerEnemy(UUID entityId) {
        if (entityId == null || SESSIONS.isEmpty()) return false;
        for (TowerSession session : SESSIONS.values()) {
            if (session.enemyUUIDs.contains(entityId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerOutOfBounds(TowerSession session, ServerPlayer player) {
        if (session == null || player == null || session.floorOrigin == null) return false;
        double dx = player.getX() - (session.floorOrigin.getX() + 0.5D);
        double dz = player.getZ() - (session.floorOrigin.getZ() + 0.5D);
        double distSq = dx * dx + dz * dz;
        double y = player.getY();
        double minY = session.floorOrigin.getY() - 1.0;
        double maxY = session.floorOrigin.getY() + CHAMBER_HEIGHT + 3.0;

        if (distSq > (ARENA_RADIUS + 2.0) * (ARENA_RADIUS + 2.0)) return true;
        if (y < minY || y > maxY) return true;

        BlockPos headPos = BlockPos.containing(player.getX(), y + 1.0, player.getZ());
        BlockPos feetPos = BlockPos.containing(player.getX(), y, player.getZ());
        if (player.level().getBlockState(headPos).is(Blocks.BEDROCK) || player.level().getBlockState(feetPos).is(Blocks.BEDROCK)) {
            return true;
        }
        return false;
    }

    public static void rescuePlayerToBounds(TowerSession session, ServerPlayer player) {
        if (session == null || player == null || session.floorOrigin == null) return;
        BlockPos spawnPos = session.floorOrigin.offset(0, 1, 0);
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0.0F;
        player.teleportTo(player.serverLevel(), spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.tower.out_of_bounds_rescued"), true);
    }

    public static void enterTower(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int targetFloor = Math.max(1, Math.min(MAX_FLOORS, data.savedTowerFloor()));
        enterTowerAtFloor(player, targetFloor);
    }

    public static void enterTowerAtFloor(ServerPlayer player, int startFloor) {
        if (isInTower(player.getUUID())) {
            warpFloor(player, startFloor);
            return;
        }
        if (DungeonManager.isInDungeon(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.tower.must_exit_dungeon"), true);
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel towerLevel = server.getLevel(RANKER_TOWER);
        if (towerLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.tower.dimension_unavailable"), true);
            return;
        }

        int index = ARENA_INDEX.getAndIncrement();
        BlockPos baseOrigin = new BlockPos(index * 2500, 64, 0);

        ResourceKey<Level> returnDim = player.level().dimension().equals(RANKER_TOWER) ? Level.OVERWORLD : player.level().dimension();
        Vec3 returnPos = player.level().dimension().equals(RANKER_TOWER) ?
                (player.getRespawnPosition() != null ? new Vec3(player.getRespawnPosition().getX() + 0.5D, player.getRespawnPosition().getY() + 0.1D, player.getRespawnPosition().getZ() + 0.5D) : new Vec3(0.5D, 70.0D, 0.5D))
                : player.position();

        TowerSession session = new TowerSession(
                player.getUUID(),
                returnDim,
                returnPos,
                player.getYRot(),
                player.getXRot(),
                baseOrigin
        );
        SESSIONS.put(player.getUUID(), session);

        buildFloor(towerLevel, session, startFloor);

        BlockPos spawnPos = session.floorOrigin.offset(0, 1, -10);
        player.teleportTo(towerLevel, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);

        if (startFloor > 1) {
            AwakeningSystemMod.notify(player,
                    "awakening_system.tower.checkpoint_resumed_title",
                    "awakening_system.tower.checkpoint_resumed_desc",
                    0xFFFFD700, startFloor);
        } else {
            AwakeningSystemMod.notify(player,
                    "awakening_system.tower.welcome_title",
                    "awakening_system.tower.welcome_desc",
                    0xFFFFD700);
        }
        player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 0.9F);
    }

    public static void warpFloor(ServerPlayer player, int targetFloor) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel towerLevel = server.getLevel(RANKER_TOWER);
        if (towerLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.tower.dimension_unavailable"), true);
            return;
        }

        TowerSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            enterTowerAtFloor(player, targetFloor);
            return;
        }

        clearExistingEnemies(towerLevel, session);

        SystemData data = SystemEvents.data(player);
        data.setSavedTowerFloor(Math.max(data.savedTowerFloor(), targetFloor));
        AwakeningSystemMod.sync(player);

        buildFloor(towerLevel, session, targetFloor);

        BlockPos spawnPos = session.floorOrigin.offset(0, 1, -10);
        player.teleportTo(towerLevel, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);

        AwakeningSystemMod.notify(player,
                "awakening_system.tower.warp_title",
                "awakening_system.tower.warp_desc",
                0xFF70E8FF, targetFloor);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
    }

    private static void clearExistingEnemies(ServerLevel level, TowerSession session) {
        for (UUID uuid : session.enemyUUIDs) {
            var entity = level.getEntity(uuid);
            if (entity != null && entity.isAlive()) {
                entity.discard();
            }
        }
        session.enemyUUIDs.clear();
        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }
    }

    public static void buildFloor(ServerLevel level, TowerSession session, int floor) {
        clearExistingEnemies(level, session);

        session.floor = floor;
        session.elevatorSpawned = false;
        session.isekaiPortalSpawned = false;
        session.isekaiTriggered = false;
        session.returnCountdown = -1;
        session.floorTicks = 0;

        int f = floor - 1;
        int gridX = (f % 10) * 120;
        int gridZ = (f / 10) * 120;
        BlockPos origin = session.baseOrigin.offset(gridX, 0, gridZ);
        session.floorOrigin = origin;

        int radius = ARENA_RADIUS;
        int height = CHAMBER_HEIGHT;
        int tier = Math.min(10, (floor - 1) / 10 + 1);

        // 1. Generación de Cámara Cerrada (Suelo, Muros Sólidos, Techo y Vaciado de Aire)
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= radius) {
                    BlockPos p = origin.offset(dx, 0, dz);

                    // Suelo y base de bedrock
                    var floorBlock = getFloorBlockForTier(tier, dist, radius, floor);
                    level.setBlockAndUpdate(p, floorBlock.defaultBlockState());
                    level.setBlockAndUpdate(p.below(), Blocks.BEDROCK.defaultBlockState());

                    // Muros sólidos del recinto
                    if (dist >= radius - 1 && dist <= radius) {
                        var wallBlock = getWallBlockForTier(tier, dx, dz, floor);
                        for (int h = 1; h <= height; h++) {
                            level.setBlockAndUpdate(p.above(h), wallBlock.defaultBlockState());
                        }
                    } else {
                        // Interior vacío y seguro
                        for (int h = 1; h <= height; h++) {
                            level.setBlockAndUpdate(p.above(h), Blocks.AIR.defaultBlockState());
                        }
                    }

                    // Techo sólido estructural
                    var ceilingBlock = getCeilingBlockForTier(tier, dist, floor);
                    level.setBlockAndUpdate(p.above(height + 1), ceilingBlock.defaultBlockState());
                    level.setBlockAndUpdate(p.above(height + 2), Blocks.BEDROCK.defaultBlockState());
                }
            }
        }

        // 2. Cuatro Pilares Arquitectónicos
        int[][] pillarOffsets = {{-9, -9}, {9, -9}, {-9, 9}, {9, 9}};
        for (int[] offset : pillarOffsets) {
            int px = offset[0];
            int pz = offset[1];
            var pillarBlock = getPillarBlockForTier(tier, floor);
            for (int h = 1; h <= height; h++) {
                level.setBlockAndUpdate(origin.offset(px, h, pz), pillarBlock.defaultBlockState());
            }
            // Lámparas adosadas a los pilares
            level.setBlockAndUpdate(origin.offset(px, 3, pz + (pz > 0 ? -1 : 1)), getTierLantern(tier).defaultBlockState());
        }

        // 3. Candelabro Central y Lámparas de Techo
        level.setBlockAndUpdate(origin.offset(0, height, 0), getCeilingLightForTier(tier).defaultBlockState());
        level.setBlockAndUpdate(origin.offset(0, height - 1, 0), Blocks.CHAIN.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(0, height - 2, 0), getCeilingLightForTier(tier).defaultBlockState());

        // Iluminación montada en los muros perimetrales (sin faroles en el suelo)
        for (int i = 0; i < 360; i += 45) {
            double rad = Math.toRadians(i);
            int lx = (int) (Math.cos(rad) * (radius - 1));
            int lz = (int) (Math.sin(rad) * (radius - 1));
            level.setBlockAndUpdate(origin.offset(lx, 3, lz), getTierLantern(tier).defaultBlockState());
        }

        // 4. Portal de Retorno al Overworld (Al sur del recinto, enmarcado con arco ceremonial)
        BlockPos returnPos = origin.offset(0, 1, -13);
        session.returnPortalPos = returnPos;
        buildPortalFrame(level, returnPos, Blocks.CRYING_OBSIDIAN, Blocks.OBSIDIAN);

        // 5. Ubicación del Portal de Ascensión (Al norte del recinto)
        session.elevatorPos = origin.offset(0, 1, 13);

        // 6. Spawn de Enemigos con Escalado Estrictamente Monótono
        spawnFloorEnemies(level, session, floor, origin, tier);
    }

    private static void buildPortalFrame(ServerLevel level, BlockPos center, net.minecraft.world.level.block.Block pillarMat, net.minecraft.world.level.block.Block lintelMat) {
        // Base
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlockAndUpdate(center.offset(dx, -1, 0), lintelMat.defaultBlockState());
        }
        // Columnas
        for (int h = 0; h <= 2; h++) {
            level.setBlockAndUpdate(center.offset(-1, h, 0), pillarMat.defaultBlockState());
            level.setBlockAndUpdate(center.offset(1, h, 0), pillarMat.defaultBlockState());
            level.setBlockAndUpdate(center.offset(0, h, 0), Blocks.AIR.defaultBlockState());
        }
        // Dintel superior
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlockAndUpdate(center.offset(dx, 3, 0), lintelMat.defaultBlockState());
        }
    }

    private static net.minecraft.world.level.block.Block getFloorBlockForTier(int tier, double dist, int radius, int floor) {
        boolean edge = dist > radius - 2;
        if (floor == 100) {
            return edge ? Blocks.GOLD_BLOCK : Blocks.QUARTZ_BRICKS;
        }
        return switch (tier) {
            case 1 -> edge ? Blocks.CRYING_OBSIDIAN : Blocks.POLISHED_DEEPSLATE;
            case 2 -> edge ? Blocks.RED_NETHER_BRICKS : Blocks.NETHER_BRICKS;
            case 3 -> edge ? Blocks.DARK_PRISMARINE : Blocks.PRISMARINE_BRICKS;
            case 4 -> edge ? Blocks.GOLD_BLOCK : Blocks.POLISHED_BLACKSTONE_BRICKS;
            case 5 -> edge ? Blocks.PURPUR_PILLAR : Blocks.PURPUR_BLOCK;
            case 6 -> edge ? Blocks.OBSIDIAN : Blocks.SMOOTH_STONE;
            case 7 -> edge ? Blocks.SCULK_CATALYST : Blocks.DEEPSLATE_TILES;
            case 8 -> edge ? Blocks.CRYING_OBSIDIAN : Blocks.GILDED_BLACKSTONE;
            case 9 -> edge ? Blocks.DIAMOND_BLOCK : Blocks.QUARTZ_BRICKS;
            default -> edge ? Blocks.GOLD_BLOCK : Blocks.QUARTZ_BRICKS;
        };
    }

    private static net.minecraft.world.level.block.Block getWallBlockForTier(int tier, int dx, int dz, int floor) {
        boolean accent = (dx + dz) % 3 == 0;
        if (floor == 100) {
            return accent ? Blocks.GOLD_BLOCK : Blocks.QUARTZ_PILLAR;
        }
        return switch (tier) {
            case 1 -> accent ? Blocks.CHISELED_STONE_BRICKS : Blocks.DEEPSLATE_BRICKS;
            case 2 -> accent ? Blocks.RED_NETHER_BRICKS : Blocks.NETHER_BRICKS;
            case 3 -> accent ? Blocks.DARK_PRISMARINE : Blocks.PRISMARINE_BRICKS;
            case 4 -> accent ? Blocks.GILDED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS;
            case 5 -> accent ? Blocks.PURPUR_PILLAR : Blocks.END_STONE_BRICKS;
            case 6 -> accent ? Blocks.POLISHED_ANDESITE : Blocks.SMOOTH_STONE;
            case 7 -> accent ? Blocks.BONE_BLOCK : Blocks.DEEPSLATE_TILES;
            case 8 -> accent ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.GILDED_BLACKSTONE;
            case 9 -> accent ? Blocks.CHISELED_QUARTZ_BLOCK : Blocks.QUARTZ_BRICKS;
            default -> accent ? Blocks.GOLD_BLOCK : Blocks.QUARTZ_PILLAR;
        };
    }

    private static net.minecraft.world.level.block.Block getCeilingBlockForTier(int tier, double dist, int floor) {
        if (floor == 100) return Blocks.QUARTZ_BRICKS;
        return switch (tier) {
            case 1 -> Blocks.POLISHED_DEEPSLATE;
            case 2 -> Blocks.NETHER_BRICKS;
            case 3 -> Blocks.DARK_PRISMARINE;
            case 4 -> Blocks.POLISHED_BLACKSTONE;
            case 5 -> Blocks.PURPUR_BLOCK;
            case 6 -> Blocks.SMOOTH_STONE;
            case 7 -> Blocks.DEEPSLATE_TILES;
            case 8 -> Blocks.POLISHED_BLACKSTONE_BRICKS;
            case 9 -> Blocks.QUARTZ_BRICKS;
            default -> Blocks.QUARTZ_BRICKS;
        };
    }

    private static net.minecraft.world.level.block.Block getPillarBlockForTier(int tier, int floor) {
        if (floor == 100) return Blocks.QUARTZ_PILLAR;
        return switch (tier) {
            case 1 -> Blocks.CHISELED_DEEPSLATE;
            case 2 -> Blocks.CHISELED_NETHER_BRICKS;
            case 3 -> Blocks.DARK_PRISMARINE;
            case 4 -> Blocks.GILDED_BLACKSTONE;
            case 5 -> Blocks.PURPUR_PILLAR;
            case 6 -> Blocks.POLISHED_ANDESITE;
            case 7 -> Blocks.BONE_BLOCK;
            case 8 -> Blocks.CRYING_OBSIDIAN;
            case 9 -> Blocks.QUARTZ_PILLAR;
            default -> Blocks.QUARTZ_PILLAR;
        };
    }

    private static net.minecraft.world.level.block.Block getTierLantern(int tier) {
        return switch (tier) {
            case 2, 6, 8 -> Blocks.SOUL_LANTERN;
            case 3, 9, 10 -> Blocks.SEA_LANTERN;
            default -> Blocks.LANTERN;
        };
    }

    private static net.minecraft.world.level.block.Block getCeilingLightForTier(int tier) {
        return switch (tier) {
            case 2, 6 -> Blocks.SHROOMLIGHT;
            case 3, 9, 10 -> Blocks.SEA_LANTERN;
            default -> Blocks.GLOWSTONE;
        };
    }

    public static Component getFloorTitle(int floor) {
        int tier = Math.min(10, (floor - 1) / 10 + 1);
        return Component.translatable("awakening_system.tower.tier." + tier);
    }

    public static void configureTowerMobAI(Mob mob, TowerSession session, ServerPlayer player) {
        if (mob == null) return;

        // 1. Limpiar todos los objetivos de ataque vanilla (defensa de aldea, agresión a monstruos/golems, represalias mutuas)
        mob.targetSelector.getAvailableGoals().removeIf(wrappedGoal ->
                wrappedGoal.getGoal() instanceof TargetGoal
        );

        // 2. Añadir meta prioritaria hacia el jugador escalador de la torre
        mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                mob, Player.class, 10, true, false,
                target -> target != null && target.getUUID().equals(session.playerId)
        ));

        // 3. Meta de respaldo para cualquier jugador
        mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                mob, Player.class, true
        ));

        // 4. Si el mob es un IronGolem, agregar MeleeAttackGoal agresivo hacia jugadores
        if (mob instanceof IronGolem golem) {
            golem.goalSelector.addGoal(2, new MeleeAttackGoal(golem, 1.25D, true));
        }

        // 5. Enfocar inmediatamente al jugador desafiante si está presente
        if (player != null && player.isAlive()) {
            mob.setTarget(player);
        }
    }

    private static void spawnFloorEnemies(ServerLevel level, TowerSession session, int floor, BlockPos origin, int tier) {
        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);

        // Fórmulas de escalado monótono garantizado (Pisos 1 al 100)
        double championHp;
        if (floor <= 10) {
            championHp = 80.0D + (floor * 15.0D); // 95 -> 230 HP
        } else if (floor <= 20) {
            championHp = 230.0D + ((floor - 10) * 25.0D); // 255 -> 480 HP
        } else if (floor <= 50) {
            championHp = 480.0D + ((floor - 20) * 35.0D); // 515 -> 1530 HP
        } else if (floor <= 80) {
            championHp = 1530.0D + ((floor - 50) * 45.0D); // 1575 -> 2880 HP
        } else if (floor < 100) {
            championHp = 2880.0D + ((floor - 80) * 60.0D); // 2940 -> 4020 HP
        } else {
            championHp = 6500.0D; // Gran Administrador Piso 100
        }

        double championDmg = 5.0D + (floor * 0.35D);
        double championArmor = Math.min(30.0D, 2.0D + (floor * 0.28D));

        double minionHp;
        if (floor <= 10) {
            minionHp = 20.0D + (floor * 5.0D); // 25 -> 70 HP
        } else if (floor <= 50) {
            minionHp = 70.0D + ((floor - 10) * 10.0D); // 80 -> 470 HP
        } else {
            minionHp = 470.0D + ((floor - 50) * 15.0D); // 485 -> 1205 HP
        }
        double minionDmg = 3.0D + (floor * 0.20D);
        double minionArmor = Math.min(24.0D, 1.0D + (floor * 0.22D));

        if (floor == 100) {
            session.bossEvent = new ServerBossEvent(
                    Component.translatable("awakening_system.tower.bossbar_summit"),
                    BossEvent.BossBarColor.YELLOW,
                    BossEvent.BossBarOverlay.NOTCHED_20
            );

            Ravager boss = EntityType.RAVAGER.create(level);
            if (boss != null) {
                boss.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
                boss.setCustomName(Component.translatable("awakening_system.tower.grand_administrator"));
                boss.setCustomNameVisible(true);
                applyAttributes(boss, championHp, championDmg, championArmor);
                boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 72000, 1, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false));
                configureTowerMobAI(boss, session, player);
                level.addFreshEntity(boss);
                session.enemyUUIDs.add(boss.getUUID());
            }

            for (int i = 0; i < 4; i++) {
                double ang = Math.toRadians(i * 90);
                WitherSkeleton guard = EntityType.WITHER_SKELETON.create(level);
                if (guard != null) {
                    guard.setPos(origin.getX() + Math.cos(ang) * 8.0D, origin.getY() + 1.0D, origin.getZ() + Math.sin(ang) * 8.0D);
                    guard.setCustomName(Component.translatable("awakening_system.tower.divine_guardian"));
                    guard.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                    guard.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                    applyAttributes(guard, 1200.0D, 24.0D, 20.0D);
                    configureTowerMobAI(guard, session, player);
                    level.addFreshEntity(guard);
                    session.enemyUUIDs.add(guard.getUUID());
                }
            }
        } else {
            BossEvent.BossBarColor barColor = switch (tier) {
                case 1, 2, 3 -> BossEvent.BossBarColor.BLUE;
                case 4, 5, 6 -> BossEvent.BossBarColor.GREEN;
                case 7, 8 -> BossEvent.BossBarColor.RED;
                default -> BossEvent.BossBarColor.PURPLE;
            };

            session.bossEvent = new ServerBossEvent(
                    Component.translatable("awakening_system.tower.bossbar", floor, getFloorTitle(floor)),
                    barColor,
                    BossEvent.BossBarOverlay.PROGRESS
            );

            int mobCount = Math.min(8, 3 + (floor % 10) / 3 + tier / 3);

            spawnFloorChampion(level, session, origin, tier, floor, championHp, championDmg, championArmor);

            for (int i = 0; i < mobCount; i++) {
                double ang = Math.toRadians(i * (360.0D / mobCount));
                double dist = 6.0D + (i % 3) * 2.0D;
                double mx = origin.getX() + Math.cos(ang) * dist;
                double mz = origin.getZ() + Math.sin(ang) * dist;

                spawnTierMinion(level, session, mx, origin.getY() + 1.0D, mz, tier, floor, minionHp, minionDmg, minionArmor);
            }
        }

        session.totalEnemies = session.enemyUUIDs.size();
        if (player != null && session.bossEvent != null) {
            session.bossEvent.addPlayer(player);
            session.bossEvent.setProgress(1.0F);
        }
    }

    private static void applyAttributes(Mob mob, double hp, double damage, double armor) {
        var hpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.setBaseValue(hp);
            mob.setHealth((float) hp);
        }
        var atkAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkAttr != null) {
            atkAttr.setBaseValue(damage);
        }
        var armAttr = mob.getAttribute(Attributes.ARMOR);
        if (armAttr != null) {
            armAttr.setBaseValue(armor);
        }
    }

    private static void spawnFloorChampion(ServerLevel level, TowerSession session, BlockPos origin, int tier, int floor, double hp, double dmg, double armor) {
        Mob champion = switch (tier) {
            case 1 -> {
                IronGolem g = EntityType.IRON_GOLEM.create(level);
                if (g != null) g.setCustomName(Component.translatable("awakening_system.tower.mob.headon_beast"));
                yield g;
            }
            case 2 -> {
                Blaze b = EntityType.BLAZE.create(level);
                if (b != null) b.setCustomName(Component.translatable("awakening_system.tower.mob.evankhell_judge"));
                yield b;
            }
            case 3 -> {
                ElderGuardian eg = EntityType.ELDER_GUARDIAN.create(level);
                if (eg != null) eg.setCustomName(Component.translatable("awakening_system.tower.mob.shinsoo_eye"));
                yield eg;
            }
            case 4 -> {
                IronGolem g = EntityType.IRON_GOLEM.create(level);
                if (g != null) g.setCustomName(Component.translatable("awakening_system.tower.mob.crown_champion"));
                yield g;
            }
            case 5 -> {
                var maulerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "mauler")).orElse(null);
                if (maulerType != null) {
                    var m = maulerType.create(level);
                    if (m instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§c§l✦ MAULER ALFA - DEPREDADOR DE LA TORRE ✦"));
                        yield mob;
                    }
                }
                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                if (ws != null) {
                    ws.setCustomName(Component.translatable("awakening_system.tower.mob.name_hunter"));
                    ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                }
                yield ws;
            }
            case 6 -> {
                var iceologerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "iceologer")).orElse(null);
                if (iceologerType != null) {
                    var ice = iceologerType.create(level);
                    if (ice instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§b§l✦ ICEOLOGER ARCHIMAGO - SEÑOR DE LA ESCARCHA ✦"));
                        yield mob;
                    }
                }
                Ravager r = EntityType.RAVAGER.create(level);
                if (r != null) r.setCustomName(Component.translatable("awakening_system.tower.mob.train_express_beast"));
                yield r;
            }
            case 7 -> {
                var illusionerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "illusioner")).orElse(null);
                if (illusionerType != null) {
                    var ill = illusionerType.create(level);
                    if (ill instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§d§l✦ ILLUSIONER SOMBRÍO - MAESTRO DEL ESPEJISMO ✦"));
                        yield mob;
                    }
                }
                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                if (ws != null) {
                    ws.setCustomName(Component.translatable("awakening_system.tower.mob.floor_death_warden"));
                    ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                }
                yield ws;
            }
            case 8 -> {
                // JEFE DE PISO 80: Wildfire (Hovering Inferno con escudos)
                var wildfireType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "wildfire")).orElse(null);
                if (wildfireType != null) {
                    var wf = wildfireType.create(level);
                    if (wf instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§6§l✦ WILDFIRE - MONARCA DE LAS LLAMAS ✦"));
                        yield mob;
                    }
                }
                // Blaze Titánico equivalente con escudos protectores
                Blaze b = EntityType.BLAZE.create(level);
                if (b != null) {
                    b.setCustomName(Component.literal("§6§l✦ WILDFIRE - MONARCA DE LAS LLAMAS ✦"));
                    b.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 72000, 0, false, false));
                    b.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 72000, 1, false, false));
                }
                yield b;
            }
            default -> {
                Ravager r = EntityType.RAVAGER.create(level);
                if (r != null) r.setCustomName(Component.translatable("awakening_system.tower.mob.high_ranker_avatar"));
                yield r;
            }
        };

        if (champion != null) {
            champion.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
            champion.setCustomNameVisible(true);
            applyAttributes(champion, hp, dmg, armor);
            champion.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0, false, false));
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
            configureTowerMobAI(champion, session, player);
            level.addFreshEntity(champion);
            session.enemyUUIDs.add(champion.getUUID());
        }
    }

    private static void spawnTierMinion(ServerLevel level, TowerSession session, double x, double y, double z, int tier, int floor, double hp, double dmg, double armor) {
        Mob minion = switch (tier) {
            case 1, 2 -> {
                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                if (ws != null) {
                    ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(tier == 1 ? Items.IRON_SWORD : Items.DIAMOND_AXE));
                    ws.setCustomName(Component.translatable("awakening_system.tower.mob.tower_regular"));
                }
                yield ws;
            }
            case 3 -> {
                IronGolem g = EntityType.IRON_GOLEM.create(level);
                if (g != null) g.setCustomName(Component.translatable("awakening_system.tower.mob.shinsoo_construct"));
                yield g;
            }
            case 4, 5 -> {
                var maulerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "mauler")).orElse(null);
                if (maulerType != null && tier == 4) {
                    var m = maulerType.create(level);
                    if (m instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§cEsbirro Mauler de la Torre"));
                        yield mob;
                    }
                }
                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                if (ws != null) {
                    ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                    ws.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
                    ws.setCustomName(Component.translatable("awakening_system.tower.mob.tower_ranker_aspirant"));
                }
                yield ws;
            }
            case 6, 7 -> {
                var iceologerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "iceologer")).orElse(null);
                if (iceologerType != null && tier == 6) {
                    var ice = iceologerType.create(level);
                    if (ice instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§bEsbirro Iceologer de la Torre"));
                        yield mob;
                    }
                }
                Blaze b = EntityType.BLAZE.create(level);
                if (b != null) b.setCustomName(Component.translatable("awakening_system.tower.mob.hell_flame_guard"));
                yield b;
            }
            default -> {
                var illusionerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("friendsandfoes", "illusioner")).orElse(null);
                if (illusionerType != null && tier == 8) {
                    var ill = illusionerType.create(level);
                    if (ill instanceof Mob mob) {
                        mob.setCustomName(Component.literal("§dEsbirro Illusioner de la Torre"));
                        yield mob;
                    }
                }
                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                if (ws != null) {
                    ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                    ws.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                    ws.setCustomName(Component.translatable("awakening_system.tower.mob.high_ranker_guard"));
                }
                yield ws;
            }
        };

        if (minion != null) {
            minion.setPos(x, y, z);
            applyAttributes(minion, hp, dmg, armor);
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
            configureTowerMobAI(minion, session, player);
            level.addFreshEntity(minion);
            session.enemyUUIDs.add(minion.getUUID());
        }
    }

    private static void buildElevator(ServerLevel level, TowerSession session) {
        BlockPos pos = session.elevatorPos;

        // Construir arco ceremonial de ascensión con energía de Shinsoo
        int tier = Math.min(10, (session.floor - 1) / 10 + 1);
        var frameBlock = tier >= 8 ? Blocks.GILDED_BLACKSTONE : Blocks.CRYING_OBSIDIAN;
        var topBlock = tier >= 8 ? Blocks.GOLD_BLOCK : Blocks.SEA_LANTERN;

        buildPortalFrame(level, pos, frameBlock, topBlock);

        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 70, 0.5D, 1.2D, 0.5D, 0.08D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 30, 0.4D, 0.5D, 0.4D, 0.03D);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5F, 1.2F);
        level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.4F);
    }

    private static void spawnIsekaiPortal(ServerLevel level, TowerSession session) {
        session.isekaiPortalSpawned = true;
        BlockPos center = session.floorOrigin.offset(0, 1, 0);
        session.isekaiPortalPos = center;

        // Despejar el área alrededor del Gran Portal Isekai para que flote limpiamente
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 0; dy <= 6; dy++) {
                    level.setBlockAndUpdate(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        // Base rúnica sutil en el suelo
        level.setBlockAndUpdate(center.below(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(center.below().north(), Blocks.GILDED_BLACKSTONE.defaultBlockState());
        level.setBlockAndUpdate(center.below().south(), Blocks.GILDED_BLACKSTONE.defaultBlockState());
        level.setBlockAndUpdate(center.below().east(), Blocks.GILDED_BLACKSTONE.defaultBlockState());
        level.setBlockAndUpdate(center.below().west(), Blocks.GILDED_BLACKSTONE.defaultBlockState());

        level.sendParticles(ParticleTypes.FLASH, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 4, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 100, 2.0D, 2.0D, 2.0D, 0.1D);
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.6F, 0.9F);
        level.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.4F, 1.4F);

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
        if (player != null) {
            SystemData data = SystemEvents.data(player);
            data.setSavedTowerFloor(100);
            data.setConqueredRankerTower(true);
            AwakeningSystemMod.sync(player);

            DungeonManager.syncRiftsToPlayer(player);
            AwakeningSystemMod.notify(player,
                    "awakening_system.tower.isekai_gate_opened_title",
                    "awakening_system.tower.isekai_gate_opened_desc",
                    0xFFFFD700);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);

            AwakeningSystemMod.notifyAll(player.getServer(),
                    "awakening_system.tower.universal_broadcast_title",
                    "awakening_system.tower.universal_broadcast_desc",
                    0xFFFFD700, player.getName().getString());
        }
    }

    public static void onEntityKilled(UUID entityId) {
        for (TowerSession session : SESSIONS.values()) {
            if (session.enemyUUIDs.remove(entityId)) {
                if (session.bossEvent != null && session.totalEnemies > 0) {
                    float prog = (float) session.enemyUUIDs.size() / (float) session.totalEnemies;
                    session.bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, prog)));
                }

                if (session.enemyUUIDs.isEmpty()) {
                    var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);
                        if (player != null) {
                            ServerLevel level = player.serverLevel();
                            if (session.floor < MAX_FLOORS) {
                                session.elevatorSpawned = true;
                                buildElevator(level, session);

                                // Guardar progreso de piso en SystemData
                                SystemData data = SystemEvents.data(player);
                                data.setSavedTowerFloor(session.floor + 1);
                                AwakeningSystemMod.sync(player);

                                AwakeningSystemMod.notify(player,
                                        "awakening_system.tower.floor_conquered_title",
                                        "awakening_system.tower.floor_conquered_desc",
                                        0xFF50FA7B, session.floor);
                                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                            } else {
                                spawnIsekaiPortal(level, session);
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    private static void triggerIsekaiAscension(ServerPlayer player, TowerSession session) {
        session.isekaiTriggered = true;
        IsekaiWorldManager.teleportToIsekai(player);
    }

    public static void exitTowerToDimension(ServerPlayer player, ServerLevel targetDim, BlockPos targetPos) {
        TowerSession session = SESSIONS.remove(player.getUUID());
        if (session != null && session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }
        SystemData data = SystemEvents.data(player);
        data.setSavedTowerFloor(100);
        AwakeningSystemMod.sync(player);
    }

    public static void exitTower(MinecraftServer server, UUID playerId) {
        TowerSession session = SESSIONS.remove(playerId);
        if (session == null) return;

        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            // Guardar checkpoint actual sin degradar progreso
            SystemData data = SystemEvents.data(player);
            int floorToSave = session.elevatorSpawned ? Math.min(MAX_FLOORS, session.floor + 1) : session.floor;
            data.setSavedTowerFloor(Math.max(data.savedTowerFloor(), floorToSave));
            AwakeningSystemMod.sync(player);

            ServerLevel returnLevel = server.getLevel(session.returnDimension);
            if (returnLevel == null || returnLevel.dimension().equals(RANKER_TOWER)) returnLevel = server.overworld();

            player.teleportTo(returnLevel, session.returnPos.x, session.returnPos.y, session.returnPos.z, session.returnYaw, session.returnPitch);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.tower.returned_safely"), true);
        }
    }

    public static void exitTowerToSpawn(MinecraftServer server, UUID playerId) {
        TowerSession session = SESSIONS.remove(playerId);
        if (session == null) return;

        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            SystemData data = SystemEvents.data(player);
            int floorToSave = session.elevatorSpawned ? Math.min(MAX_FLOORS, session.floor + 1) : session.floor;
            data.setSavedTowerFloor(Math.max(data.savedTowerFloor(), floorToSave));
            AwakeningSystemMod.sync(player);

            ResourceKey<Level> respawnDim = player.getRespawnDimension();
            ServerLevel returnLevel = (respawnDim != null && !respawnDim.equals(RANKER_TOWER)) ? server.getLevel(respawnDim) : server.overworld();
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

            player.teleportTo(returnLevel, targetVec.x, targetVec.y, targetVec.z, yaw, 0.0F);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.tower.returned_to_spawn"), true);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        TowerSession session = SESSIONS.remove(player.getUUID());
        if (session != null) {
            if (session.bossEvent != null) {
                session.bossEvent.removeAllPlayers();
            }
            SystemData data = SystemEvents.data(player);
            int floorToSave = session.elevatorSpawned ? Math.min(MAX_FLOORS, session.floor + 1) : session.floor;
            data.setSavedTowerFloor(Math.max(data.savedTowerFloor(), floorToSave));
            AwakeningSystemMod.sync(player);
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        TowerSession session = SESSIONS.remove(player.getUUID());
        if (session != null) {
            if (session.bossEvent != null) {
                session.bossEvent.removeAllPlayers();
            }
            SystemData data = SystemEvents.data(player);
            int floorToSave = session.elevatorSpawned ? Math.min(MAX_FLOORS, session.floor + 1) : session.floor;
            data.setSavedTowerFloor(Math.max(data.savedTowerFloor(), floorToSave));
            AwakeningSystemMod.sync(player);
        }
    }

    public static void onPlayerLogin(ServerPlayer player) {
        if (!player.level().dimension().equals(RANKER_TOWER)) return;
        if (!isInTower(player.getUUID())) {
            SystemData data = SystemEvents.data(player);
            int targetFloor = Math.max(1, Math.min(MAX_FLOORS, data.savedTowerFloor()));
            enterTowerAtFloor(player, targetFloor);
        }
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;

        // Failsafe Watchdog: Rescatar periódicamente a jugadores varados en RANKER_TOWER sin sesión
        if (server.getTickCount() % 20 == 0) {
            ServerLevel towerLevel = server.getLevel(RANKER_TOWER);
            if (towerLevel != null) {
                for (ServerPlayer sp : new ArrayList<>(towerLevel.players())) {
                    if (!sp.isSpectator() && !isInTower(sp.getUUID())) {
                        exitTowerToSpawn(server, sp.getUUID());
                    }
                }
            }
        }

        if (SESSIONS.isEmpty()) return;

        long gameTime = server.overworld().getGameTime();

        for (Map.Entry<UUID, TowerSession> entry : SESSIONS.entrySet()) {
            UUID playerId = entry.getKey();
            TowerSession session = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);

            if (player == null || !player.isAlive()) {
                exitTower(server, playerId);
                continue;
            }

            ServerLevel level = player.serverLevel();
            if (!level.dimension().equals(RANKER_TOWER)) {
                exitTower(server, playerId);
                continue;
            }

            session.floorTicks++;

            // Failsafe Watchdog: Reubicar al jugador si terminó fuera de los límites o en el vacío/bloques
            if (isPlayerOutOfBounds(session, player)) {
                rescuePlayerToBounds(session, player);
            }

            if (gameTime % 20 == 0) {
                // Reafirmar objetivo hacia el jugador en todos los enemigos vivos de la torre
                if (!session.enemyUUIDs.isEmpty()) {
                    for (UUID enemyId : session.enemyUUIDs) {
                        var entity = level.getEntity(enemyId);
                        if (entity instanceof Mob mob && mob.isAlive()) {
                            if (mob.getTarget() == null || !mob.getTarget().equals(player)) {
                                mob.setTarget(player);
                            }
                        }
                    }
                }

                SystemData data = SystemEvents.data(player);
                int will = data.willpower();
                boolean isRanker = "ranker_of_the_tower".equals(data.equippedTitle()) || "high_ranker_of_the_tower".equals(data.equippedTitle());

                if (will >= 25 || isRanker) {
                    int amp = isRanker ? 2 : 1;
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 45, amp, false, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, amp - 1, false, false, false));
                    if (isRanker) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 45, 1, false, false, false));
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 0, false, false, false));
                }

                level.sendParticles(ParticleTypes.GLOW,
                        player.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D,
                        player.getY() + player.getRandom().nextDouble() * 2.0D,
                        player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0D,
                        2, 0.1D, 0.1D, 0.1D, 0.01D);
            }

            if (session.returnPortalPos != null && gameTime % 10 == 0) {
                BlockPos p = session.returnPortalPos;
                level.sendParticles(ParticleTypes.PORTAL, p.getX() + 0.5D, p.getY() + 1.2D, p.getZ() + 0.5D, 8, 0.4D, 0.6D, 0.4D, 0.05D);
            }

            if (session.floorTicks > 40 && session.returnPortalPos != null) {
                double distSq = player.distanceToSqr(session.returnPortalPos.getX() + 0.5D, session.returnPortalPos.getY() + 0.5D, session.returnPortalPos.getZ() + 0.5D);
                if (distSq <= 3.5D) {
                    exitTower(server, playerId);
                    continue;
                }
            }

            if (session.elevatorSpawned && session.elevatorPos != null && session.floor < MAX_FLOORS) {
                if (gameTime % 10 == 0) {
                    BlockPos p = session.elevatorPos;
                    level.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5D, p.getY() + 1.2D, p.getZ() + 0.5D, 10, 0.4D, 0.8D, 0.4D, 0.03D);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX() + 0.5D, p.getY() + 0.8D, p.getZ() + 0.5D, 4, 0.3D, 0.3D, 0.3D, 0.01D);
                }

                double distSq = player.distanceToSqr(session.elevatorPos.getX() + 0.5D, session.elevatorPos.getY() + 0.5D, session.elevatorPos.getZ() + 0.5D);
                if (distSq <= 3.5D) {
                    int nextFloor = session.floor + 1;
                    buildFloor(level, session, nextFloor);

                    SystemData data = SystemEvents.data(player);
                    data.setSavedTowerFloor(nextFloor);
                    AwakeningSystemMod.sync(player);

                    BlockPos nextSpawn = session.floorOrigin.offset(0, 1, -10);
                    player.teleportTo(level, nextSpawn.getX() + 0.5D, nextSpawn.getY(), nextSpawn.getZ() + 0.5D, 0.0F, 0.0F);

                    AwakeningSystemMod.notify(player,
                            "awakening_system.tower.ascend_title",
                            "awakening_system.tower.ascend_desc",
                            0xFF70E8FF, session.floor);
                    player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
                }
            }

            if (session.isekaiPortalSpawned && !session.isekaiTriggered && session.isekaiPortalPos != null) {
                if (gameTime % 8 == 0) {
                    BlockPos p = session.isekaiPortalPos;
                    level.sendParticles(ParticleTypes.PORTAL, p.getX() + 0.5D, p.getY() + 1.0D, p.getZ() + 0.5D, 15, 0.6D, 1.2D, 0.6D, 0.1D);
                    level.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5D, p.getY() + 1.5D, p.getZ() + 0.5D, 6, 0.4D, 1.0D, 0.4D, 0.03D);
                }

                double distSq = player.distanceToSqr(session.isekaiPortalPos.getX() + 0.5D, session.isekaiPortalPos.getY() + 0.5D, session.isekaiPortalPos.getZ() + 0.5D);
                if (distSq <= 4.0D) {
                    triggerIsekaiAscension(player, session);
                }
            }

            if (session.returnCountdown > 0) {
                session.returnCountdown--;
                if (session.returnCountdown % 40 == 0 && session.returnCountdown > 0) {
                    player.displayClientMessage(Component.translatable("awakening_system.tower.returning_countdown", (session.returnCountdown / 20)), true);
                }
                if (session.returnCountdown <= 0) {
                    exitTower(server, playerId);
                }
            }
        }
    }
}
