package com.soybe.awakeningsystem.world;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Ruin Gates: a test gate type, separate from the regular Gates, whose dungeons are built from hand-made
 * structure pieces joined with Minecraft's jigsaw system.
 * <p>
 * The pieces and their joining rules come from the DungeonZ mod by Globox_Z (MIT licence, see
 * {@code licenses/DungeonZ-LICENSE.txt}). Two dungeons: the Dark Mines and the Jungle Temple.
 * <ul>
 *   <li>{@code /ruingate dark|temple} raises the gate's entrance in front of the player. Walking into it
 *       builds a fresh copy of the dungeon in its own dimension ({@code awakening_system:ruin_realm}).</li>
 *   <li>Marker blocks hidden in the pieces become monsters, the boss, the boss's treasure and the exit.</li>
 *   <li>Every chest and barrel gets a loot table, so Lootr turns them into per-player containers.</li>
 *   <li>The exit opens when the boss falls; {@code /ruingate leave} always gets you out.</li>
 * </ul>
 * Nothing here touches the regular Gates or their dimension.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class RuinGateManager {
    public static final ResourceKey<Level> RUIN_REALM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_realm"));
    private static final String NS = "ruin_gate/";
    private static final int MAX_MONSTERS = 70;

    private static final List<ResourceKey<LootTable>> CHEST_LOOT = List.of(loot("dungeon_tier_low"), loot("dungeon_tier_mid"), loot("dungeon_tier_mid"), loot("dungeon_tier_high"));
    private static final ResourceKey<LootTable> BOSS_LOOT = loot("abyssal_grotto_treasure");

    /** One dungeon type: where its pieces start and what each marker block stands for. */
    private record Kind(String id, String entrance, String startPool, Map<Block, String> spawns) {}

    private static final Map<String, Kind> KINDS = Map.of(
            "dark", new Kind("dark", "dark_dungeon_entrance", "dark_dungeon/dungeon_spawn", Map.of(
                    Blocks.DIAMOND_BLOCK, "minecraft:wither_skeleton",
                    Blocks.GOLD_BLOCK, "minecraft:skeleton",
                    Blocks.IRON_BLOCK, "minecraft:zombie",
                    Blocks.RAW_GOLD_BLOCK, "minecraft:spider",
                    Blocks.RAW_COPPER_BLOCK, "minecraft:creeper",
                    Blocks.RAW_IRON_BLOCK, "minecraft:witch")),
            "temple", new Kind("temple", "temple_dungeon_entrance", "temple_dungeon/temple_spawn", Map.of(
                    Blocks.WHITE_WOOL, "minecraft:skeleton",
                    Blocks.CYAN_WOOL, "minecraft:zombie",
                    Blocks.LIME_WOOL, "minecraft:creeper",
                    Blocks.GRAY_WOOL, "minecraft:cave_spider",
                    Blocks.RED_WOOL, "minecraft:drowned",
                    Blocks.MAGENTA_WOOL, "minecraft:slime",
                    Blocks.PURPLE_WOOL, "minecraft:phantom",
                    Blocks.PINK_WOOL, "minecraft:vex",
                    Blocks.DIAMOND_BLOCK, "minecraft:witch")));

    /** A player's run through one dungeon copy. */
    private static final class Session {
        String kind;
        ResourceKey<Level> returnDim;
        Vec3 returnPos;
        float returnYaw;
        BlockPos start;
        final List<BlockPos> exits = new ArrayList<>();
        BlockPos bossLoot;
        UUID boss;
        boolean bossDead;
        long lastLockedHint;
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private RuinGateManager() {}

    private static ResourceKey<LootTable> loot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/" + name));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, NS + path);
    }

    // ---------------------------------------------------------------- commands
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ruingate").requires(s -> s.hasPermission(2))
                .then(Commands.literal("leave").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    if (!leave(p)) ctx.getSource().sendFailure(Component.translatable("awakening_system.ruin_gate.not_inside"));
                    return 1;
                }))
                .then(Commands.literal("test").then(Commands.argument("kind", StringArgumentType.word()).executes(ctx -> {
                    // builds a copy without anyone entering it and reports what came out (works from the console)
                    Kind k = KINDS.get(StringArgumentType.getString(ctx, "kind"));
                    ServerLevel realm = ctx.getSource().getServer().getLevel(RUIN_REALM);
                    if (k == null || realm == null) return 0;
                    Data data = Data.get(ctx.getSource().getServer());
                    BlockPos origin = new BlockPos(-30000 - (data.nextRun++ % 200) * 1600, 120, 30000);
                    List<BoundingBox> boxes = build(realm, k, origin);
                    Session s = new Session();
                    int mobs = boxes.isEmpty() ? 0 : processMarkers(realm, k, boxes, s);
                    int chests = 0;
                    for (BoundingBox b : boxes) for (BlockPos p : BlockPos.betweenClosed(b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ()))
                        if (realm.getBlockEntity(p) instanceof RandomizableContainerBlockEntity c && c.getLootTable() != null) chests++;
                    String msg = "[Ruin Gate] test " + k.id + " at " + origin.toShortString() + ": " + boxes.size() + " pieces, " + mobs + " monsters, "
                            + chests + " containers with loot, boss " + (s.boss != null) + ", boss chest spot " + (s.bossLoot != null) + ", " + s.exits.size()
                            + " exits, spawn " + standableNear(realm, origin).toShortString();
                    AwakeningSystemMod.LOGGER.info(msg);
                    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                    return 1;
                })))
                .then(Commands.literal("enter").then(Commands.argument("kind", StringArgumentType.word())
                        .suggests((c, b) -> { KINDS.keySet().forEach(b::suggest); return b.buildFuture(); })
                        .executes(ctx -> {
                            Kind k = KINDS.get(StringArgumentType.getString(ctx, "kind"));
                            if (k == null) { ctx.getSource().sendFailure(Component.translatable("awakening_system.ruin_gate.unknown")); return 0; }
                            enter(ctx.getSource().getPlayerOrException(), k);
                            return 1;
                        })))
                .then(Commands.argument("kind", StringArgumentType.word())
                        .suggests((c, b) -> { KINDS.keySet().forEach(b::suggest); return b.buildFuture(); })
                        .executes(ctx -> {
                            Kind k = KINDS.get(StringArgumentType.getString(ctx, "kind"));
                            if (k == null) { ctx.getSource().sendFailure(Component.translatable("awakening_system.ruin_gate.unknown")); return 0; }
                            raiseGate(ctx.getSource().getPlayerOrException(), k);
                            return 1;
                        })));
    }

    // ---------------------------------------------------------------- the gate in the world
    /** Places the dungeon's entrance a few blocks in front of the player and registers its portal blocks. */
    private static void raiseGate(ServerPlayer player, Kind kind) {
        ServerLevel level = player.serverLevel();
        Optional<StructureTemplate> template = level.getStructureManager().get(id(kind.entrance));
        if (template.isEmpty()) {
            player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.failed"));
            return;
        }
        StructureTemplate t = template.get();
        Direction facing = player.getDirection();
        BlockPos ahead = player.blockPosition().relative(facing, 6 + Math.max(t.getSize().getX(), t.getSize().getZ()) / 2);
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ahead.getX(), ahead.getZ());
        BlockPos corner = new BlockPos(ahead.getX() - t.getSize().getX() / 2, ground - 1, ahead.getZ() - t.getSize().getZ() / 2);
        t.placeInWorld(level, corner, corner, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
        List<BlockPos> portal = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(corner, corner.offset(t.getSize()).offset(-1, -1, -1))) {
            if (level.getBlockState(p).is(Blocks.LIGHT)) portal.add(p.immutable());
        }
        if (portal.isEmpty()) portal.add(corner.offset(t.getSize().getX() / 2, 1, t.getSize().getZ() / 2));
        Data data = Data.get(level.getServer());
        data.gates.add(new Gate(kind.id, level.dimension(), portal));
        data.setDirty();
        level.playSound(null, portal.get(0), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.2F);
        player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.spawned", Component.translatable("awakening_system.ruin_gate.name." + kind.id)));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 5 != 0) return;
        Data data = Data.get(server);
        boolean sparkle = server.getTickCount() % 10 == 0;
        for (Gate gate : new ArrayList<>(data.gates)) {
            ServerLevel level = server.getLevel(gate.dimension);
            if (level == null) continue;
            for (BlockPos p : gate.portal) {
                if (!level.isLoaded(p)) continue;
                if (sparkle) level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.3, 0.4, 0.3, 0.02);
            }
            // a copy: entering teleports the player, which takes them out of this level's player list
            for (ServerPlayer player : new ArrayList<>(level.players())) {
                if (player.isSpectator() || SESSIONS.containsKey(player.getUUID())) continue;
                BlockPos at = player.blockPosition();
                if (gate.portal.contains(at) || gate.portal.contains(at.above())) enter(player, KINDS.get(gate.kind));
            }
        }
        // the exits inside the dungeons
        ServerLevel realm = server.getLevel(RUIN_REALM);
        if (realm == null) return;
        for (ServerPlayer player : new ArrayList<>(realm.players())) {
            Session s = SESSIONS.get(player.getUUID());
            if (s == null) {
                if (!player.isSpectator()) sendHome(player);
                continue;
            }
            for (BlockPos exit : s.exits) {
                if (sparkle) realm.sendParticles(s.bossDead ? ParticleTypes.END_ROD : ParticleTypes.SMOKE, exit.getX() + 0.5, exit.getY() + 0.8, exit.getZ() + 0.5, 4, 0.3, 0.5, 0.3, 0.01);
                if (player.distanceToSqr(Vec3.atBottomCenterOf(exit)) > 2.5) continue;
                if (s.bossDead) {
                    leave(player);
                    break;
                }
                long now = server.getTickCount();
                if (now - s.lastLockedHint > 100) {
                    s.lastLockedHint = now;
                    player.displayClientMessage(Component.translatable("awakening_system.ruin_gate.exit_locked"), true);
                }
            }
        }
    }

    // ---------------------------------------------------------------- building a dungeon copy
    private static void enter(ServerPlayer player, Kind kind) {
        if (kind == null || SESSIONS.containsKey(player.getUUID())) return;
        MinecraftServer server = player.getServer();
        ServerLevel realm = server == null ? null : server.getLevel(RUIN_REALM);
        if (realm == null) {
            player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.failed"));
            return;
        }
        Data data = Data.get(server);
        int run = data.nextRun++;
        data.setDirty();
        BlockPos origin = new BlockPos(-30000 - (run % 200) * 1600, 120, -30000 - (run / 200) * 1600);
        player.displayClientMessage(Component.translatable("awakening_system.ruin_gate.entering", Component.translatable("awakening_system.ruin_gate.name." + kind.id)), true);

        List<BoundingBox> boxes = build(realm, kind, origin);
        if (boxes.isEmpty()) {
            player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.failed"));
            return;
        }
        Session s = new Session();
        s.kind = kind.id;
        s.returnDim = player.level().dimension();
        s.returnPos = player.position();
        s.returnYaw = player.getYRot();
        s.start = origin;
        int mobs = processMarkers(realm, kind, boxes, s);
        if (s.exits.isEmpty()) s.exits.add(origin);
        SESSIONS.put(player.getUUID(), s);

        BlockPos spawn = standableNear(realm, origin);
        // step back from the entrance portal so walking off does not re-enter anything
        player.teleportTo(realm, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), 0.0F);
        player.fallDistance = 0;
        realm.playSound(null, spawn, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.4F, 1.4F);
        AwakeningSystemMod.LOGGER.info("[Ruin Gate] {} built at {} for {}: {} pieces, {} monsters, boss {}, {} exits",
                kind.id, origin.toShortString(), player.getName().getString(), boxes.size(), mobs, s.boss != null, s.exits.size());
    }

    /** Lays out the jigsaw pieces from the start pool and places them; returns each piece's bounds. */
    private static List<BoundingBox> build(ServerLevel level, Kind kind, BlockPos origin) {
        List<BoundingBox> boxes = new ArrayList<>();
        HolderLookup.RegistryLookup<StructureTemplatePool> pools = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        Optional<Holder.Reference<StructureTemplatePool>> start = pools.get(ResourceKey.create(Registries.TEMPLATE_POOL, id(kind.startPool)));
        if (start.isEmpty()) return boxes;
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        Structure.GenerationContext context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
                level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), new ChunkPos(origin), level, b -> true);
        Optional<Structure.GenerationStub> stub = JigsawPlacement.addPieces(context, start.get(), Optional.of(id("spawn")), 64, origin, false,
                Optional.empty(), 512, PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (stub.isEmpty()) return boxes;
        for (StructurePiece piece : stub.get().getPiecesBuilder().build().pieces()) {
            if (piece instanceof PoolElementStructurePiece element) {
                element.place(level, level.structureManager(), generator, level.getRandom(), BoundingBox.infinite(), origin, false);
                boxes.add(element.getBoundingBox());
            }
        }
        return boxes;
    }

    /**
     * Turns the marker blocks into what they stand for (monsters, boss, boss treasure, exit) and gives
     * every chest and barrel a loot table. Returns the number of monsters spawned.
     */
    private static int processMarkers(ServerLevel level, Kind kind, List<BoundingBox> boxes, Session s) {
        int mobs = 0;
        BlockPos bossPos = null;
        Set<BlockPos> done = new HashSet<>();
        List<Map.Entry<BlockPos, String>> spots = new ArrayList<>();
        for (BoundingBox box : boxes) {
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                BlockState st = level.getBlockState(p);
                if (st.isAir()) continue;
                BlockPos pos = p.immutable();
                if (!done.add(pos)) continue;
                String mob = kind.spawns.get(st.getBlock());
                if (mob != null) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    spots.add(Map.entry(pos, mob));
                } else if (st.is(Blocks.NETHERITE_BLOCK)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    bossPos = pos;
                } else if (st.is(Blocks.EMERALD_BLOCK)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    s.bossLoot = pos;
                } else if (st.is(Blocks.QUARTZ_BLOCK)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    s.exits.add(pos);
                } else if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container && container.getLootTable() == null) {
                    container.setLootTable(CHEST_LOOT.get(level.getRandom().nextInt(CHEST_LOOT.size())), level.getRandom().nextLong());
                }
            }
        }
        // the pieces hold far more marker blocks than a fight needs (some are plain decoration), so a
        // random share of them, spread over the whole dungeon, gets a monster
        java.util.Collections.shuffle(spots, new java.util.Random(level.getRandom().nextLong()));
        for (Map.Entry<BlockPos, String> spot : spots) {
            if (mobs >= MAX_MONSTERS) break;
            if (spawn(level, spot.getValue(), spot.getKey()) != null) mobs++;
        }
        if (bossPos != null) {
            SeriesStructureWorldGen.spawnOverworldMiniboss(level, bossPos.below());
            List<Mob> found = level.getEntitiesOfClass(Mob.class, new AABB(bossPos).inflate(3),
                    m -> m.getPersistentData().getBoolean("is_overworld_miniboss"));
            if (!found.isEmpty()) s.boss = found.get(0).getUUID();
        }
        if (s.boss == null) s.bossDead = true;   // no boss in this copy: the exit is open from the start
        return mobs;
    }

    private static Mob spawn(ServerLevel level, String typeId, BlockPos pos) {
        Optional<EntityType<?>> type = EntityType.byString(typeId);
        if (type.isEmpty()) return null;
        Entity e = type.get().create(level);
        if (!(e instanceof Mob mob)) return null;
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
        EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        return mob;
    }

    private static BlockPos standableNear(ServerLevel level, BlockPos origin) {
        for (int r = 0; r <= 6; r++) for (int dy = 3; dy >= -8; dy--) for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
            BlockPos p = origin.offset(dx, dy, dz);
            if (level.getBlockState(p).getCollisionShape(level, p).isEmpty()
                    && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()
                    && !level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty()) return p;
        }
        return origin;
    }

    // ---------------------------------------------------------------- boss, leaving, safety
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !level.dimension().equals(RUIN_REALM)) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            SESSIONS.remove(player.getUUID());   // they respawn at home as usual
            return;
        }
        UUID dead = event.getEntity().getUUID();
        for (Map.Entry<UUID, Session> e : SESSIONS.entrySet()) {
            Session s = e.getValue();
            if (!dead.equals(s.boss) || s.bossDead) continue;
            s.bossDead = true;
            BlockPos chestPos = s.bossLoot != null ? s.bossLoot : event.getEntity().blockPosition();
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) chest.setLootTable(BOSS_LOOT, level.getRandom().nextLong());
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(e.getKey());
            if (p != null) {
                p.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.boss_defeated"));
                p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level().dimension().equals(RUIN_REALM) && !SESSIONS.containsKey(p.getUUID())) sendHome(p);
    }

    /** Takes the player back to where they entered. */
    private static boolean leave(ServerPlayer player) {
        Session s = SESSIONS.remove(player.getUUID());
        if (s == null) return false;
        ServerLevel back = player.getServer().getLevel(s.returnDim);
        if (back == null) back = player.getServer().overworld();
        player.teleportTo(back, s.returnPos.x, s.returnPos.y, s.returnPos.z, s.returnYaw, 0.0F);
        player.fallDistance = 0;
        player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.left"));
        return true;
    }

    /** Someone in the ruin dimension with no run (after a restart): back to their spawn point. */
    private static void sendHome(ServerPlayer player) {
        ServerLevel home = player.getServer().overworld();
        BlockPos spawn = home.getSharedSpawnPos();
        BlockPos respawn = player.getRespawnPosition();
        if (respawn != null && player.getRespawnDimension().equals(Level.OVERWORLD)) spawn = respawn;
        player.teleportTo(home, spawn.getX() + 0.5, spawn.getY() + 0.1, spawn.getZ() + 0.5, player.getYRot(), 0.0F);
        player.fallDistance = 0;
    }

    // ---------------------------------------------------------------- saved gates
    private record Gate(String kind, ResourceKey<Level> dimension, List<BlockPos> portal) {}

    private static final class Data extends SavedData {
        final List<Gate> gates = new ArrayList<>();
        int nextRun;

        static Data get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(Data::new, Data::load, null), "awakening_ruin_gates");
        }

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            d.nextRun = tag.getInt("NextRun");
            ListTag list = tag.getList("Gates", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag g = list.getCompound(i);
                List<BlockPos> portal = new ArrayList<>();
                for (long l : g.getLongArray("Portal")) portal.add(BlockPos.of(l));
                ResourceLocation dim = ResourceLocation.tryParse(g.getString("Dimension"));
                if (dim == null || portal.isEmpty()) continue;
                d.gates.add(new Gate(g.getString("Kind"), ResourceKey.create(Registries.DIMENSION, dim), portal));
            }
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("NextRun", nextRun);
            ListTag list = new ListTag();
            for (Gate g : gates) {
                CompoundTag c = new CompoundTag();
                c.putString("Kind", g.kind);
                c.putString("Dimension", g.dimension.location().toString());
                c.putLongArray("Portal", g.portal.stream().mapToLong(BlockPos::asLong).toArray());
                list.add(c);
            }
            tag.put("Gates", list);
            return tag;
        }
    }
}
