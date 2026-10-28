package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.world.SeriesStructureBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PagodaTrialManager {
    private PagodaTrialManager() {}

    public static final int MAX_STAGES = 9;
    public static final int ARENA_RADIUS = 20;
    public static final int ARENA_HEIGHT = 14;

    public static final ResourceKey<Level> PAGODA_REALM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pagoda_realm")
    );

    public static final class PagodaSession {
        public final UUID playerId;
        public int stage = 1;
        public final ResourceKey<Level> returnDimension;
        public final Vec3 returnPos;
        public final float returnYaw;
        public final float returnPitch;
        public final BlockPos baseOrigin;
        public BlockPos stageOrigin;
        public final Set<UUID> enemyUUIDs = ConcurrentHashMap.newKeySet();
        public int totalEnemies;
        public ServerBossEvent bossEvent;
        public BlockPos progressionDoorPos;
        public BlockPos returnDoorPos;
        public boolean stageCleared = false;
        public boolean ascensionTriggered = false;
        public int stageTicks = 0;

        public PagodaSession(UUID playerId, ResourceKey<Level> returnDimension, Vec3 returnPos, float returnYaw, float returnPitch, BlockPos baseOrigin) {
            this.playerId = playerId;
            this.returnDimension = returnDimension;
            this.returnPos = returnPos;
            this.returnYaw = returnYaw;
            this.returnPitch = returnPitch;
            this.baseOrigin = baseOrigin;
            this.stageOrigin = baseOrigin;
        }
    }

    private static final Map<UUID, PagodaSession> SESSIONS = new ConcurrentHashMap<>();
    private static final AtomicInteger ARENA_INDEX = new AtomicInteger(1);

    public static boolean isInTrial(UUID playerId) {
        return SESSIONS.containsKey(playerId);
    }

    public static PagodaSession getSession(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    public static boolean isTrialEnemy(UUID entityId) {
        if (entityId == null || SESSIONS.isEmpty()) return false;
        for (PagodaSession session : SESSIONS.values()) {
            if (session.enemyUUIDs.contains(entityId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerOutOfBounds(PagodaSession session, ServerPlayer player) {
        if (session == null || player == null || session.stageOrigin == null) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        double dx = player.getX() - (session.stageOrigin.getX() + 0.5D);
        double dz = player.getZ() - (session.stageOrigin.getZ() + 0.5D);
        double distSq = dx * dx + dz * dz;
        int maxR = (session.stage == 9 ? 28 : ARENA_RADIUS) + 2;
        int maxH = (session.stage == 9 ? 26 : ARENA_HEIGHT) + 3;
        double y = player.getY();
        double minY = session.stageOrigin.getY() - 1.0;
        double maxY = session.stageOrigin.getY() + maxH;

        if (distSq > maxR * maxR) return true;
        if (y < minY || y > maxY) return true;

        BlockPos headPos = BlockPos.containing(player.getX(), y + 1.0, player.getZ());
        BlockPos feetPos = BlockPos.containing(player.getX(), y, player.getZ());
        if (player.level().getBlockState(headPos).is(Blocks.BEDROCK) || player.level().getBlockState(feetPos).is(Blocks.BEDROCK)) {
            return true;
        }
        return false;
    }

    public static void rescuePlayerToBounds(PagodaSession session, ServerPlayer player) {
        if (session == null || player == null || session.stageOrigin == null) return;
        BlockPos spawnPos = session.stageOrigin.offset(0, 1, 0);
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0.0F;
        player.teleportTo(player.serverLevel(), spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.pagoda.out_of_bounds_rescued"), true);
    }

    public static void onPlayerLogin(ServerPlayer player) {
        if (player.level().dimension().equals(PAGODA_REALM)) {
            if (!isInTrial(player.getUUID()) && !player.isSpectator()) {
                if (player.getServer() != null) {
                    exitTrial(player.getServer(), player.getUUID());
                }
            }
        }
    }

    public static ServerLevel getTrialLevel(MinecraftServer server) {
        if (server == null) return null;
        ServerLevel lvl = server.getLevel(PAGODA_REALM);
        if (lvl != null) return lvl;
        lvl = server.getLevel(MurimWorldManager.MURIM_WORLD);
        if (lvl != null) return lvl;
        lvl = server.getLevel(TowerManager.RANKER_TOWER);
        if (lvl != null) return lvl;
        return server.overworld();
    }

    public static void enterTrial(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int targetStage = Math.max(1, Math.min(MAX_STAGES, data.savedPagodaStage()));
        enterTrialAtStage(player, targetStage);
    }

    public static void enterTrialAtStage(ServerPlayer player, int startStage) {
        if (isInTrial(player.getUUID())) {
            warpStage(player, startStage);
            return;
        }
        if (DungeonManager.isInDungeon(player.getUUID()) || TowerManager.isInTower(player.getUUID())) {
            player.displayClientMessage(Component.translatable("awakening_system.pagoda.cannot_enter_busy"), true);
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel trialLevel = getTrialLevel(server);
        if (trialLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.pagoda.dimension_unavailable"), true);
            return;
        }

        int index = ARENA_INDEX.getAndIncrement();
        BlockPos baseOrigin = new BlockPos(index * 3000, 64, 5000);

        ResourceKey<Level> returnDim = player.level().dimension().equals(PAGODA_REALM) ? MurimWorldManager.MURIM_WORLD : player.level().dimension();
        Vec3 returnPos = player.position();

        PagodaSession session = new PagodaSession(
                player.getUUID(),
                returnDim,
                returnPos,
                player.getYRot(),
                player.getXRot(),
                baseOrigin
        );
        SESSIONS.put(player.getUUID(), session);

        buildStage(trialLevel, session, startStage);

        BlockPos spawnPos = session.stageOrigin.offset(0, 1, -12);
        player.teleportTo(trialLevel, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);

        AwakeningSystemMod.notify(player,
                "awakening_system.pagoda.welcome_title",
                "awakening_system.pagoda.welcome_desc",
                0xFFFFD700, startStage);
        player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2F, 0.9F);
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    public static void warpStage(ServerPlayer player, int targetStage) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel trialLevel = getTrialLevel(server);
        if (trialLevel == null) return;

        PagodaSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            enterTrialAtStage(player, targetStage);
            return;
        }

        clearExistingEnemies(trialLevel, session);

        SystemData data = SystemEvents.data(player);
        data.setSavedPagodaStage(Math.max(data.savedPagodaStage(), targetStage));
        AwakeningSystemMod.sync(player);

        buildStage(trialLevel, session, targetStage);

        BlockPos spawnPos = session.stageOrigin.offset(0, 1, -12);
        player.teleportTo(trialLevel, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);

        AwakeningSystemMod.notify(player,
                "awakening_system.pagoda.stage_warp_title",
                "awakening_system.pagoda.stage_warp_desc",
                0xFF70E8FF, targetStage);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
    }

    private static void clearExistingEnemies(ServerLevel level, PagodaSession session) {
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

    public static StructureTemplate getArenaTemplate(ServerLevel level, int stage) {
        StructureTemplateManager stm = level.getStructureManager();
        ResourceLocation[] candidates = {
                ResourceLocation.fromNamespaceAndPath("awakening_system", "pagoda_arena_stage_" + stage),
                ResourceLocation.fromNamespaceAndPath("awakening_system", "pagoda_arena")
        };

        for (ResourceLocation loc : candidates) {
            Optional<StructureTemplate> opt = stm.get(loc);
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        String[] resourceNames = {
                "pagoda_arena_stage_" + stage,
                "pagoda_arena"
        };
        for (String name : resourceNames) {
            String[] paths = {
                    "/data/awakening_system/structures/" + name + ".nbt",
                    "/data/awakening_system/structure/" + name + ".nbt"
            };
            for (String p : paths) {
                try (InputStream in = PagodaTrialManager.class.getResourceAsStream(p)) {
                    if (in != null) {
                        CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                        StructureTemplate template = new StructureTemplate();
                        template.load(level.holderLookup(Registries.BLOCK), tag);
                        return template;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static boolean loadCustomArena(ServerLevel level, BlockPos origin, int stage) {
        try {
            StructureTemplate template = getArenaTemplate(level, stage);
            if (template == null) return false;

            Vec3i size = template.getSize();
            BlockPos placePos = origin.offset(-size.getX() / 2, -1, -size.getZ() / 2);

            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setIgnoreEntities(false)
                    .setKnownShape(true)
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE);

            template.placeInWorld(level, placePos, placePos, settings, level.getRandom(), 2 | 16);
            return true;
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error loading custom pagoda arena template: {}", e.getMessage());
            return false;
        }
    }

    public static void buildProceduralArena(ServerLevel level, BlockPos origin, int stage) {
        int radius = (stage == 9) ? 28 : ARENA_RADIUS;
        int height = (stage == 9) ? 26 : ARENA_HEIGHT;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= radius) {
                    BlockPos p = origin.offset(dx, 0, dz);

                    level.setBlockAndUpdate(p.below(), Blocks.BEDROCK.defaultBlockState());

                    var floorBlock = getFloorBlockForStage(stage, dist, radius);
                    level.setBlockAndUpdate(p, floorBlock.defaultBlockState());

                    for (int h = 1; h <= height; h++) {
                        level.setBlockAndUpdate(p.above(h), Blocks.AIR.defaultBlockState());
                    }

                    if (dist >= radius - 1 && dist <= radius) {
                        var wallBlock = getWallBlockForStage(stage, dx, dz);
                        for (int h = 1; h <= height; h++) {
                            if (h >= 4 && h <= 7 && (Math.abs(dx) % 3 == 0 || Math.abs(dz) % 3 == 0)) {
                                level.setBlockAndUpdate(p.above(h), Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState());
                            } else {
                                level.setBlockAndUpdate(p.above(h), wallBlock.defaultBlockState());
                            }
                        }
                    }

                    var ceilingBlock = getCeilingBlockForStage(stage);
                    level.setBlockAndUpdate(p.above(height + 1), ceilingBlock.defaultBlockState());
                }
            }
        }

        if (stage != 9) {
            int[][] pillarCoords = {
                    {0, 13}, {0, -13}, {13, 0}, {-13, 0},
                    {9, 9}, {9, -9}, {-9, 9}, {-9, -9}
            };
            for (int[] coord : pillarCoords) {
                BlockPos pilBase = origin.offset(coord[0], 1, coord[1]);
                for (int h = 0; h <= height; h++) {
                    level.setBlockAndUpdate(pilBase.above(h), getPillarBlockForStage(stage).defaultBlockState());
                }
                level.setBlockAndUpdate(pilBase.above(8).north(), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        } else {
            int[][] dragonPillars = {
                    {0, 24}, {0, -24}, {24, 0}, {-24, 0},
                    {17, 17}, {17, -17}, {-17, 17}, {-17, -17}
            };
            for (int[] coord : dragonPillars) {
                BlockPos pilBase = origin.offset(coord[0], 1, coord[1]);
                for (int h = 0; h <= height; h++) {
                    level.setBlockAndUpdate(pilBase.above(h), Blocks.GILDED_BLACKSTONE.defaultBlockState());
                }
                level.setBlockAndUpdate(pilBase.above(12).north(), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= 3.2D) {
                    BlockPos cp = origin.offset(dx, 0, dz);
                    level.setBlockAndUpdate(cp, Blocks.GILDED_BLACKSTONE.defaultBlockState());
                }
            }
        }

        BlockPos returnPos = origin.offset(0, 1, -16);
        buildReturnArch(level, returnPos);

        level.setBlockAndUpdate(origin.offset(0, height, 0), Blocks.SEA_LANTERN.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(6, height, 6), Blocks.SOUL_LANTERN.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(-6, height, 6), Blocks.SOUL_LANTERN.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(6, height, -6), Blocks.SOUL_LANTERN.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(-6, height, -6), Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private static void ensureReturnDoor(ServerLevel level, BlockPos returnPos) {
        BlockState state = level.getBlockState(returnPos);
        if (!state.is(BlockTags.DOORS)) {
            buildReturnArch(level, returnPos);
        }
    }

    public static void generateArenaInWorld(ServerLevel level, BlockPos origin, int stage, boolean placeStructureBlock) {
        int radius = (stage == 9) ? 28 : ARENA_RADIUS;
        int height = (stage == 9) ? 26 : ARENA_HEIGHT;

        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                for (int h = 0; h <= height + 2; h++) {
                    BlockPos p = origin.offset(dx, h, dz);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        boolean loadedCustom = loadCustomArena(level, origin, stage);
        if (!loadedCustom) {
            buildProceduralArena(level, origin, stage);
        }

        BlockPos returnPos = origin.offset(0, 1, -16);
        ensureReturnDoor(level, returnPos);

        if (placeStructureBlock) {
            BlockPos sbPos = origin.offset(0, 1, -radius - 2);
            level.setBlockAndUpdate(sbPos.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            level.setBlockAndUpdate(sbPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());

            if (level.getBlockEntity(sbPos) instanceof StructureBlockEntity sb) {
                sb.setMode(StructureMode.SAVE);
                sb.setStructureName("awakening_system:pagoda_arena_stage_" + stage);
                sb.setStructurePos(new BlockPos(-radius, -2, 2));
                sb.setStructureSize(new Vec3i(radius * 2 + 1, height + 3, radius * 2 + 1));
                sb.setShowBoundingBox(true);
                sb.setIgnoreEntities(true);
                sb.setChanged();
            }
        }
    }

    public static void buildStage(ServerLevel level, PagodaSession session, int stage) {
        clearExistingEnemies(level, session);

        session.stage = stage;
        session.stageCleared = false;
        session.ascensionTriggered = false;
        session.progressionDoorPos = null;
        session.stageTicks = 0;

        int offsetZ = (stage - 1) * 200;
        BlockPos origin = session.baseOrigin.offset(0, 0, offsetZ);
        session.stageOrigin = origin;

        boolean loadedCustom = loadCustomArena(level, origin, stage);
        if (!loadedCustom) {
            buildProceduralArena(level, origin, stage);
        }

        BlockPos returnPos = origin.offset(0, 1, -16);
        session.returnDoorPos = returnPos;
        ensureReturnDoor(level, returnPos);

        spawnStageEnemies(level, session, stage, origin);
    }

    private static void buildReturnArch(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(pos.west(), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState());
        level.setBlockAndUpdate(pos.west().above(), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState());
        level.setBlockAndUpdate(pos.east().above(), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState());
        level.setBlockAndUpdate(pos.above(2), Blocks.CHISELED_DEEPSLATE.defaultBlockState());

        level.setBlockAndUpdate(pos, Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.FACING, Direction.NORTH));
        level.setBlockAndUpdate(pos.above(), Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.FACING, Direction.NORTH));

        level.setBlockAndUpdate(pos.above(3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    private static net.minecraft.world.level.block.Block getFloorBlockForStage(int stage, double dist, int radius) {
        boolean inner = dist <= 6.0D;
        boolean edge = dist >= radius - 3;
        return switch (stage) {
            case 1 -> inner ? Blocks.DARK_PRISMARINE : (edge ? Blocks.STRIPPED_BAMBOO_BLOCK : Blocks.POLISHED_ANDESITE);
            case 2 -> inner ? Blocks.RED_NETHER_BRICKS : (edge ? Blocks.NETHER_BRICKS : Blocks.POLISHED_BLACKSTONE_BRICKS);
            case 3 -> inner ? Blocks.BLUE_ICE : (edge ? Blocks.PACKED_ICE : Blocks.DEEPSLATE_TILES);
            case 4 -> inner ? Blocks.REINFORCED_DEEPSLATE : (edge ? Blocks.MUD_BRICKS : Blocks.GILDED_BLACKSTONE);
            case 5 -> inner ? Blocks.CRYING_OBSIDIAN : (edge ? Blocks.PURPUR_BLOCK : Blocks.POLISHED_BLACKSTONE);
            case 6 -> inner ? Blocks.SCULK : (edge ? Blocks.WARPED_WART_BLOCK : Blocks.DEEPSLATE_BRICKS);
            case 7 -> inner ? Blocks.OBSIDIAN : (edge ? Blocks.END_STONE_BRICKS : Blocks.PURPUR_BLOCK);
            case 8 -> inner ? (dist <= 3 ? Blocks.SMOOTH_QUARTZ : Blocks.POLISHED_BLACKSTONE) : Blocks.QUARTZ_BRICKS;
            case 9 -> inner ? Blocks.GOLD_BLOCK : (edge ? Blocks.CHISELED_QUARTZ_BLOCK : Blocks.GILDED_BLACKSTONE);
            default -> Blocks.POLISHED_BLACKSTONE_BRICKS;
        };
    }

    private static net.minecraft.world.level.block.Block getWallBlockForStage(int stage, int dx, int dz) {
        boolean corner = (Math.abs(dx) + Math.abs(dz)) % 4 == 0;
        return switch (stage) {
            case 1 -> corner ? Blocks.DARK_PRISMARINE : Blocks.STRIPPED_BAMBOO_BLOCK;
            case 2 -> corner ? Blocks.CRIMSON_STEM : Blocks.POLISHED_BLACKSTONE_BRICKS;
            case 3 -> corner ? Blocks.SNOW_BLOCK : Blocks.DEEPSLATE_TILES;
            case 4 -> corner ? Blocks.MUD_BRICKS : Blocks.POLISHED_DEEPSLATE;
            case 5 -> corner ? Blocks.PURPUR_PILLAR : Blocks.POLISHED_BLACKSTONE;
            case 6 -> corner ? Blocks.STRIPPED_WARPED_STEM : Blocks.DEEPSLATE_TILES;
            case 7 -> corner ? Blocks.PURPUR_PILLAR : Blocks.END_STONE_BRICKS;
            case 8 -> corner ? Blocks.SMOOTH_QUARTZ : Blocks.POLISHED_BLACKSTONE_BRICKS;
            case 9 -> corner ? Blocks.GOLD_BLOCK : Blocks.GILDED_BLACKSTONE;
            default -> Blocks.POLISHED_BLACKSTONE_BRICKS;
        };
    }

    private static net.minecraft.world.level.block.Block getCeilingBlockForStage(int stage) {
        return switch (stage) {
            case 1 -> Blocks.DARK_PRISMARINE;
            case 2 -> Blocks.NETHER_BRICKS;
            case 3 -> Blocks.DEEPSLATE_TILES;
            case 4 -> Blocks.POLISHED_BLACKSTONE;
            case 5 -> Blocks.PURPUR_BLOCK;
            case 6 -> Blocks.DEEPSLATE_TILES;
            case 7 -> Blocks.END_STONE_BRICKS;
            case 8 -> Blocks.QUARTZ_BRICKS;
            case 9 -> Blocks.GOLD_BLOCK;
            default -> Blocks.POLISHED_BLACKSTONE;
        };
    }

    private static net.minecraft.world.level.block.Block getPillarBlockForStage(int stage) {
        return switch (stage) {
            case 1 -> Blocks.DARK_PRISMARINE;
            case 2 -> Blocks.STRIPPED_CRIMSON_STEM;
            case 3 -> Blocks.PACKED_ICE;
            case 4 -> Blocks.GILDED_BLACKSTONE;
            case 5 -> Blocks.PURPUR_PILLAR;
            case 6 -> Blocks.STRIPPED_WARPED_STEM;
            case 7 -> Blocks.CRYING_OBSIDIAN;
            case 8 -> Blocks.QUARTZ_PILLAR;
            case 9 -> Blocks.CHISELED_QUARTZ_BLOCK;
            default -> Blocks.STRIPPED_CRIMSON_STEM;
        };
    }

    private static void spawnStageEnemies(ServerLevel level, PagodaSession session, int stage, BlockPos origin) {
        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);

        Component stageTitle = Component.translatable("awakening_system.pagoda.stage." + stage + ".name");
        session.bossEvent = new ServerBossEvent(
                Component.translatable("awakening_system.pagoda.bossbar_format", stage, stageTitle),
                BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarOverlay.NOTCHED_10
        );
        if (player != null) {
            session.bossEvent.addPlayer(player);
        }

        switch (stage) {
            case 1 -> {
                spawnMinion(level, session, EntityType.BREEZE, origin.offset(-5, 1, 5), "awakening_system.pagoda.mob.wind_spirit", 70, 8.0, 10.0);
                spawnMinion(level, session, EntityType.BREEZE, origin.offset(5, 1, 5), "awakening_system.pagoda.mob.wind_spirit", 70, 8.0, 10.0);
                spawnMinion(level, session, EntityType.BREEZE, origin.offset(0, 1, 8), "awakening_system.pagoda.mob.wind_spirit", 70, 8.0, 10.0);
                spawnBoss(level, session, EntityType.WITHER_SKELETON, origin.offset(0, 1, 4), "awakening_system.pagoda.mob.wind_master", 160, 12.0, 16.0, Items.DIAMOND_SWORD);
            }
            case 2 -> {
                spawnMinion(level, session, EntityType.BLAZE, origin.offset(-6, 1, 4), "awakening_system.pagoda.mob.flame_spirit", 85, 9.0, 10.0);
                spawnMinion(level, session, EntityType.BLAZE, origin.offset(6, 1, 4), "awakening_system.pagoda.mob.flame_spirit", 85, 9.0, 10.0);
                spawnMinion(level, session, EntityType.BLAZE, origin.offset(0, 1, 7), "awakening_system.pagoda.mob.flame_spirit", 85, 9.0, 10.0);
                spawnBoss(level, session, EntityType.BLAZE, origin.offset(0, 1, 3), "awakening_system.pagoda.mob.flame_venerable", 200, 14.0, 18.0, null);
            }
            case 3 -> {
                spawnMinion(level, session, ModEntities.FROST_ELF_ASSASSIN.get(), origin.offset(-5, 1, 5), "awakening_system.pagoda.mob.frost_assassin", 95, 10.0, 12.0);
                spawnMinion(level, session, ModEntities.FROST_ELF_ASSASSIN.get(), origin.offset(5, 1, 5), "awakening_system.pagoda.mob.frost_assassin", 95, 10.0, 12.0);
                spawnMinion(level, session, EntityType.STRAY, origin.offset(0, 1, 8), "awakening_system.pagoda.mob.frost_archer", 90, 9.0, 10.0);
                spawnBoss(level, session, ModEntities.FROST_ELF_ASSASSIN.get(), origin.offset(0, 1, 3), "awakening_system.pagoda.mob.frost_sovereign", 240, 15.0, 20.0, Items.NETHERITE_SWORD);
            }
            case 4 -> {
                spawnMinion(level, session, EntityType.IRON_GOLEM, origin.offset(-6, 1, 4), "awakening_system.pagoda.mob.earth_guardian", 150, 12.0, 15.0);
                spawnMinion(level, session, EntityType.IRON_GOLEM, origin.offset(6, 1, 4), "awakening_system.pagoda.mob.earth_guardian", 150, 12.0, 15.0);
                spawnBoss(level, session, EntityType.IRON_GOLEM, origin.offset(0, 1, 4), "awakening_system.pagoda.mob.earth_patriarch", 320, 18.0, 24.0, null);
            }
            case 5 -> {
                spawnMinion(level, session, ModEntities.WHITE_THUNDER_TIGER.get(), origin.offset(-5, 1, 5), "awakening_system.pagoda.mob.thunder_tiger", 160, 14.0, 16.0);
                spawnMinion(level, session, ModEntities.WHITE_THUNDER_TIGER.get(), origin.offset(5, 1, 5), "awakening_system.pagoda.mob.thunder_tiger", 160, 14.0, 16.0);
                spawnBoss(level, session, EntityType.WITHER_SKELETON, origin.offset(0, 1, 4), "awakening_system.pagoda.mob.thunder_lord", 350, 20.0, 24.0, Items.NETHERITE_AXE);
            }
            case 6 -> {
                spawnMinion(level, session, ModEntities.ABYSSAL_NECROMANCER.get(), origin.offset(-6, 1, 5), "awakening_system.pagoda.mob.poison_necromancer", 170, 15.0, 18.0);
                spawnMinion(level, session, ModEntities.ABYSSAL_NECROMANCER.get(), origin.offset(6, 1, 5), "awakening_system.pagoda.mob.poison_necromancer", 170, 15.0, 18.0);
                spawnBoss(level, session, EntityType.WITHER_SKELETON, origin.offset(0, 1, 4), "awakening_system.pagoda.mob.asura_monarch", 400, 22.0, 26.0, Items.NETHERITE_SWORD);
            }
            case 7 -> {
                spawnMinion(level, session, ModEntities.SHADOW_INFANTRY.get(), origin.offset(-5, 1, 4), "awakening_system.pagoda.mob.inner_shadow", 180, 16.0, 20.0);
                spawnMinion(level, session, ModEntities.SHADOW_INFANTRY.get(), origin.offset(5, 1, 4), "awakening_system.pagoda.mob.inner_shadow", 180, 16.0, 20.0);
                spawnMinion(level, session, ModEntities.SHADOW_INFANTRY.get(), origin.offset(0, 1, 7), "awakening_system.pagoda.mob.inner_shadow", 180, 16.0, 20.0);
                spawnBoss(level, session, ModEntities.SHADOW_INFANTRY.get(), origin.offset(0, 1, 3), "awakening_system.pagoda.mob.heart_demon", 450, 24.0, 28.0, Items.NETHERITE_SWORD);
            }
            case 8 -> {
                spawnBoss(level, session, EntityType.WITHER_SKELETON, origin.offset(-4, 1, 5), "awakening_system.pagoda.mob.yin_guardian", 380, 22.0, 26.0, Items.DIAMOND_SWORD);
                spawnBoss(level, session, EntityType.IRON_GOLEM, origin.offset(4, 1, 5), "awakening_system.pagoda.mob.yang_guardian", 420, 22.0, 26.0, null);
            }
            case 9 -> {
                spawnBoss(level, session, ModEntities.ANCIENT_CELESTIAL_DRAGON.get(), origin.offset(0, 5, 0), "awakening_system.pagoda.mob.celestial_dragon_apex", 1200, 32.0, 30.0, null);
            }
        }

        session.totalEnemies = session.enemyUUIDs.size();
        if (session.bossEvent != null) {
            session.bossEvent.setProgress(1.0F);
        }
    }

    private static void spawnMinion(ServerLevel level, PagodaSession session, EntityType<?> type, BlockPos pos, String nameKey, double hp, double dmg, double armor) {
        var entity = type.create(level);
        if (entity instanceof Mob mob) {
            mob.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            mob.setCustomName(Component.translatable(nameKey));
            mob.setCustomNameVisible(true);
            mob.addTag("no_block_destroy");
            mob.addTag("no_shadow_extraction");
            mob.addTag("pagoda_trial_mob");

            applyAttributes(mob, hp, dmg, armor);
            configurePagodaMobAI(mob, session);

            level.addFreshEntity(mob);
            session.enemyUUIDs.add(mob.getUUID());
        }
    }

    private static void spawnBoss(ServerLevel level, PagodaSession session, EntityType<?> type, BlockPos pos, String nameKey, double hp, double dmg, double armor, net.minecraft.world.item.Item weapon) {
        var entity = type.create(level);
        if (entity instanceof Mob mob) {
            mob.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            mob.setCustomName(Component.translatable(nameKey));
            mob.setCustomNameVisible(true);
            mob.addTag("no_block_destroy");
            mob.addTag("no_shadow_extraction");
            mob.addTag("pagoda_trial_mob");

            if (weapon != null) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
            }

            applyAttributes(mob, hp, dmg, armor);
            configurePagodaMobAI(mob, session);

            if (mob instanceof AncientCelestialDragonBossEntity dragon) {
                dragon.setHomeSanctuaryPos(session.stageOrigin);
                dragon.triggerSingleRoar();
            }

            level.addFreshEntity(mob);
            session.enemyUUIDs.add(mob.getUUID());
        }
    }

    private static void applyAttributes(Mob mob, double maxHp, double damage, double armor) {
        var hpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.setBaseValue(maxHp);
            mob.setHealth((float) maxHp);
        }
        var dmgAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.setBaseValue(damage);
        }
        var armorAttr = mob.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(armor);
        }
    }

    public static void configurePagodaMobAI(Mob mob, PagodaSession session) {
        if (mob == null) return;
        if (mob instanceof AncientCelestialDragonBossEntity) {
            return;
        }
        mob.targetSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof TargetGoal);
        mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                mob, Player.class, 10, true, false,
                target -> target != null && target.getUUID().equals(session.playerId)
        ));
        mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                mob, Player.class, true
        ));
        if (mob instanceof IronGolem golem) {
            golem.goalSelector.addGoal(2, new MeleeAttackGoal(golem, 1.25D, true));
        }
    }

    public static void onEntityKilled(UUID entityId) {
        for (PagodaSession session : SESSIONS.values()) {
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
                            session.stageCleared = true;

                            if (session.bossEvent != null) {
                                session.bossEvent.setName(Component.translatable("awakening_system.pagoda.bossbar_cleared"));
                                session.bossEvent.setColor(BossEvent.BossBarColor.GREEN);
                                session.bossEvent.setProgress(0.0F);
                            }

                            manifestProgressionDoor(level, session, player);
                        }
                    }
                }
                break;
            }
        }
    }

    private static void manifestProgressionDoor(ServerLevel level, PagodaSession session, ServerPlayer player) {
        BlockPos doorPos = session.stageOrigin.offset(0, 1, 8);
        session.progressionDoorPos = doorPos;

        level.setBlockAndUpdate(doorPos.below(), Blocks.GILDED_BLACKSTONE.defaultBlockState());
        level.setBlockAndUpdate(doorPos.below().north(), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        level.setBlockAndUpdate(doorPos.below().south(), Blocks.POLISHED_BLACKSTONE.defaultBlockState());

        var doorBlock = session.stage == MAX_STAGES ? Blocks.WARPED_DOOR : Blocks.CRIMSON_DOOR;
        level.setBlockAndUpdate(doorPos, doorBlock.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.OPEN, false));
        level.setBlockAndUpdate(doorPos.above(), doorBlock.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.OPEN, false));

        level.setBlockAndUpdate(doorPos.west(), Blocks.DARK_OAK_FENCE.defaultBlockState());
        level.setBlockAndUpdate(doorPos.west().above(), Blocks.SOUL_LANTERN.defaultBlockState());
        level.setBlockAndUpdate(doorPos.east(), Blocks.DARK_OAK_FENCE.defaultBlockState());
        level.setBlockAndUpdate(doorPos.east().above(), Blocks.SOUL_LANTERN.defaultBlockState());

        double px = doorPos.getX() + 0.5D;
        double py = doorPos.getY() + 1.0D;
        double pz = doorPos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.FLASH, px, py, pz, 4, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 80, 0.8D, 1.2D, 0.8D, 0.1D);
        level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 40, 0.5D, 1.0D, 0.5D, 0.05D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, px, py, pz, 40, 0.6D, 0.8D, 0.6D, 0.05D);

        level.playSound(null, doorPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.6F, 1.0F);
        level.playSound(null, doorPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.3F, 1.2F);
        level.playSound(null, doorPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.4F, 1.4F);

        if (session.stage < MAX_STAGES) {
            AwakeningSystemMod.notify(player,
                    "awakening_system.pagoda.stage_cleared_title",
                    "awakening_system.pagoda.door_manifested",
                    0xFF50FA7B, session.stage);
        } else {
            AwakeningSystemMod.notify(player,
                    "awakening_system.pagoda.apex_conquered_title",
                    "awakening_system.pagoda.ascension_door_manifested",
                    0xFFFFD700);
        }
    }

    public static void handleDoorInteraction(ServerPlayer player, BlockPos clickedPos) {
        PagodaSession session = SESSIONS.get(player.getUUID());
        if (session == null) return;

        if (session.returnDoorPos != null && (clickedPos.equals(session.returnDoorPos) || clickedPos.equals(session.returnDoorPos.above()))) {
            exitTrial(player.getServer(), player.getUUID());
            return;
        }

        if (session.stageCleared && session.progressionDoorPos != null &&
                (clickedPos.equals(session.progressionDoorPos) || clickedPos.equals(session.progressionDoorPos.above()))) {
            advanceToNextStageOrAscend(player, session);
        }
    }

    public static void advanceToNextStageOrAscend(ServerPlayer player, PagodaSession session) {
        ServerLevel level = player.serverLevel();

        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
        player.playNotifySound(SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.2F, 1.0F);
        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);

        if (session.stage < MAX_STAGES) {
            int nextStage = session.stage + 1;

            if (SystemItems.SOUL_TRANSFORMATION_PILL.isBound()) {
                ItemStack stagePill = new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get(), 2);
                if (!player.addItem(stagePill)) {
                    player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5D, player.getZ(), stagePill));
                }
            }

            buildStage(level, session, nextStage);

            SystemData data = SystemEvents.data(player);
            data.setSavedPagodaStage(nextStage);
            AwakeningSystemMod.sync(player);

            BlockPos nextSpawn = session.stageOrigin.offset(0, 1, -12);
            player.teleportTo(level, nextSpawn.getX() + 0.5D, nextSpawn.getY(), nextSpawn.getZ() + 0.5D, 0.0F, 0.0F);

            AwakeningSystemMod.notify(player,
                    "awakening_system.pagoda.advance_title",
                    "awakening_system.pagoda.advance_desc",
                    0xFF70E8FF, nextStage);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
        } else {
            triggerAscensionConquest(player, session);
        }
    }

    private static void triggerAscensionConquest(ServerPlayer player, PagodaSession session) {
        session.ascensionTriggered = true;
        MinecraftServer server = player.getServer();

        SystemData data = SystemEvents.data(player);
        data.setConqueredNineTribulationsPagoda(true);
        data.setSavedPagodaStage(MAX_STAGES);
        data.addCoins(500000);
        data.addCultivationQi(5000);
        data.setEquippedTitle("nine_tribulations_ascendant");
        AwakeningSystemMod.sync(player);

        ItemStack rewardScroll = SeriesStructureBuilder.makeRelic(Items.PAPER, 1,
                "§6§l✦ Nine Tribulations Ascension Scroll ✦",
                "Murim Pagoda Pinnacle",
                "You have conquered the 9 celestial tribulations. Your Dantian has transcended mortal bounds.");
        if (!player.addItem(rewardScroll)) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5D, player.getZ(), rewardScroll));
        }

        if (SystemItems.SOUL_TRANSFORMATION_PILL.isBound()) {
            ItemStack pills = new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get(), 16);
            if (!player.addItem(pills)) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5D, player.getZ(), pills));
            }
        }

        if (SystemItems.IMMORTAL_DRAGON_SCALE.isBound()) {
            ItemStack scales = new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 16);
            if (!player.addItem(scales)) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5D, player.getZ(), scales));
            }
        }

        if (SystemItems.CELESTIAL_DRAGON_PEARL.isBound()) {
            ItemStack pearls = new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get(), 4);
            if (!player.addItem(pearls)) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5D, player.getZ(), pearls));
            }
        }

        AwakeningSystemMod.notify(player,
                "awakening_system.pagoda.apex_cleared_title",
                "awakening_system.pagoda.apex_cleared_desc",
                0xFFFFD700);
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.6F, 0.9F);

        exitTrial(server, player.getUUID());
    }

    public static void exitTrial(MinecraftServer server, UUID playerId) {
        PagodaSession session = SESSIONS.remove(playerId);
        if (session == null || server == null) return;

        if (session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            ServerLevel returnLevel = server.getLevel(session.returnDimension);
            if (returnLevel == null || returnLevel.dimension().equals(PAGODA_REALM)) {
                returnLevel = server.getLevel(MurimWorldManager.MURIM_WORLD);
                if (returnLevel == null) returnLevel = server.overworld();
            }

            player.getPersistentData().putLong("pagoda_exit_cooldown", returnLevel.getGameTime() + 100L);
            player.teleportTo(returnLevel, session.returnPos.x, session.returnPos.y, session.returnPos.z, session.returnYaw, session.returnPitch);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.pagoda.returned_safely"), true);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        PagodaSession session = SESSIONS.remove(player.getUUID());
        if (session != null && session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        PagodaSession session = SESSIONS.remove(player.getUUID());
        if (session != null && session.bossEvent != null) {
            session.bossEvent.removeAllPlayers();
        }
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;

        if (server.getTickCount() % 20 == 0) {
            ServerLevel pagodaLevel = server.getLevel(PAGODA_REALM);
            if (pagodaLevel != null) {
                for (ServerPlayer sp : new java.util.ArrayList<>(pagodaLevel.players())) {
                    if (!sp.isSpectator() && !sp.isCreative() && !isInTrial(sp.getUUID())) {
                        exitTrial(server, sp.getUUID());
                    }
                }
            }
        }

        if (SESSIONS.isEmpty()) return;

        long gameTime = server.overworld().getGameTime();

        for (Map.Entry<UUID, PagodaSession> entry : SESSIONS.entrySet()) {
            UUID playerId = entry.getKey();
            PagodaSession session = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);

            if (player == null || !player.isAlive()) {
                exitTrial(server, playerId);
                continue;
            }

            ServerLevel level = player.serverLevel();
            session.stageTicks++;

            if (isPlayerOutOfBounds(session, player)) {
                rescuePlayerToBounds(session, player);
            }

            if (gameTime % 20 == 0 && !session.enemyUUIDs.isEmpty()) {
                session.enemyUUIDs.removeIf(uuid -> {
                    var entity = level.getEntity(uuid);
                    return entity == null || !entity.isAlive();
                });
                if (session.enemyUUIDs.isEmpty() && !session.stageCleared) {
                    onEntityKilled(UUID.randomUUID());
                } else {
                    for (UUID enemyId : session.enemyUUIDs) {
                        var entity = level.getEntity(enemyId);
                        if (entity instanceof Mob mob && mob.isAlive()) {
                            if (mob.getTarget() == null || !mob.getTarget().equals(player)) {
                                mob.setTarget(player);
                            }
                        }
                    }
                }
            }

            if (session.returnDoorPos != null && gameTime % 12 == 0) {
                BlockPos p = session.returnDoorPos;
                level.sendParticles(ParticleTypes.PORTAL, p.getX() + 0.5D, p.getY() + 1.0D, p.getZ() + 0.5D, 4, 0.3D, 0.5D, 0.3D, 0.02D);
            }

            if (!player.isCreative() && !player.isSpectator() && session.stageTicks > 40 && session.returnDoorPos != null) {
                BlockPos pPos = player.blockPosition();
                if (pPos.equals(session.returnDoorPos) || pPos.equals(session.returnDoorPos.above())) {
                    exitTrial(server, playerId);
                    continue;
                }
            }

            if (session.stageCleared && session.progressionDoorPos != null) {
                if (gameTime % 10 == 0) {
                    BlockPos p = session.progressionDoorPos;
                    level.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5D, p.getY() + 1.2D, p.getZ() + 0.5D, 6, 0.3D, 0.6D, 0.3D, 0.02D);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX() + 0.5D, p.getY() + 0.8D, p.getZ() + 0.5D, 3, 0.3D, 0.4D, 0.3D, 0.01D);
                }

                double distSq = player.distanceToSqr(session.progressionDoorPos.getX() + 0.5D, session.progressionDoorPos.getY() + 0.5D, session.progressionDoorPos.getZ() + 0.5D);
                if (distSq <= 2.2D) {
                    advanceToNextStageOrAscend(player, session);
                }
            }
        }
    }

    public static void clearAll() {
        SESSIONS.clear();
    }
}
