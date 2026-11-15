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
 *   <li>{@code /ruingate dark|temple [rank]} opens a gate in front of the player, drawn like the regular
 *       Gates. Walking into it builds a fresh copy of the dungeon in its own dimension
 *       ({@code awakening_system:ruin_realm}).</li>
 *   <li>The rank (E to S, rolled when not given) sets the monsters' level and the loot: chests give only
 *       that rank's crystals, the boss's chest at most one rank above.</li>
 *   <li>Marker blocks hidden in the pieces become monsters, the boss, the boss's treasure and the exits.</li>
 *   <li>Every chest and barrel gets a loot table, so Lootr turns them into per-player containers.</li>
 *   <li>The exits open, as portals, when the boss falls; {@code /ruingate leave} always gets you out.</li>
 * </ul>
 * Nothing here touches the regular Gates or their dimension.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class RuinGateManager {
    public static final ResourceKey<Level> RUIN_REALM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_realm"));
    private static final String NS = "ruin_gate/";
    private static final int MAX_MONSTERS = 70;
    /** Chests beyond this many get plain supplies, so one run does not hand out a pile of crystals. */
    private static final int MAX_RANK_CHESTS = 12;

    /** Gate ranks, weakest first. A gate's rank sets its monsters' level and the crystals it gives. */
    private static final List<String> RANKS = List.of("E", "D", "C", "B", "A", "S");
    private static final int[] RANK_WEIGHTS = {30, 25, 20, 13, 8, 4};
    private static final ResourceKey<LootTable> SUPPLIES = loot("ruin_gate/supplies");
    /** Rift type that makes the client draw the Ruin Gate's own portal (AwakeningSystemClient.RUIN_GATE_TYPE). */
    private static final int PORTAL_TYPE = 95;

    /** Natural gates: how often one may open near a player, how long it stays and how many at once. */
    private static final int NATURAL_CHECK_TICKS = 6000;        // every 5 minutes
    private static final float NATURAL_CHANCE = 0.35F;
    private static final long NATURAL_LIFETIME = 36000L;        // 30 minutes
    private static final int MAX_NATURAL_GATES = 3;

    /** One dungeon type: its Overworld boss, its entrance ruin, where its pieces start and what each marker block stands for. */
    private record Kind(String id, String boss, String entrance, String startPool, Map<Block, String> spawns) {}

    private static final Map<String, Kind> KINDS = Map.of(
            "dark", new Kind("dark", "minecraft:ravager", "dark_dungeon_entrance", "dark_dungeon/dungeon_spawn", Map.of(
                    Blocks.DIAMOND_BLOCK, "minecraft:wither_skeleton",
                    Blocks.GOLD_BLOCK, "minecraft:skeleton",
                    Blocks.IRON_BLOCK, "minecraft:zombie",
                    Blocks.RAW_GOLD_BLOCK, "minecraft:spider",
                    Blocks.RAW_COPPER_BLOCK, "minecraft:creeper",
                    Blocks.RAW_IRON_BLOCK, "minecraft:witch")),
            "temple", new Kind("temple", "minecraft:evoker", "temple_dungeon_entrance", "temple_dungeon/temple_spawn", Map.of(
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
        String rank = "C";
        ResourceKey<Level> returnDim;
        Vec3 returnPos;
        float returnYaw;
        BlockPos start;
        final List<BlockPos> exits = new ArrayList<>();
        BlockPos bossLoot;
        UUID boss;
        boolean bossDead;
        long lastLockedHint;
        int sampleLevel;   // one monster's level, for the test report
        boolean teyvat;    // entered from Teyvat: the Teyvat variant, with Teyvat's bosses
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private RuinGateManager() {}

    private static ResourceKey<LootTable> loot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/" + name));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, NS + path);
    }

    private static String randomRank(net.minecraft.util.RandomSource random) {
        int total = 0;
        for (int w : RANK_WEIGHTS) total += w;
        int roll = random.nextInt(total);
        for (int i = 0; i < RANK_WEIGHTS.length; i++) {
            roll -= RANK_WEIGHTS[i];
            if (roll < 0) return RANKS.get(i);
        }
        return "C";
    }

    /** The rank typed in a command, or null when it is not one of E to S. */
    private static String parseRank(String typed) {
        String r = typed.toUpperCase(java.util.Locale.ROOT);
        return RANKS.contains(r) ? r : null;
    }

    private static Component gateName(String kind, String rank) {
        return Component.translatable("awakening_system.ruin_gate.name." + kind).append(" [" + rank + "]");
    }

    /** Monster level for a rank, from the same table the regular Gates use. */
    private static int rankLevel(String rank, boolean boss, net.minecraft.util.RandomSource random) {
        int base = com.soybe.awakeningsystem.DungeonManager.getRankBaseLevel(rank);
        return boss ? base + 6 : base + random.nextInt(5);
    }

    /** How big the portal is drawn, in blocks from its centre (same sizes as the regular Gates). */
    private static float portalSize(String rank) {
        return switch (rank) {
            case "S" -> 5.2F;
            case "A" -> 4.6F;
            case "B", "C" -> 4.0F;
            default -> 3.5F;
        };
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
                .then(Commands.literal("locate").executes(ctx -> {
                    // the nearest fixed gate's spot (it is raised once its ground has been generated)
                    ServerLevel world = ctx.getSource().getServer().overworld();
                    Spot s = nearestSpot(world, BlockPos.containing(ctx.getSource().getPosition()));
                    if (s == null) return 0;
                    ctx.getSource().sendSuccess(() -> gateName(s.kind, s.rank).copy().append(": " + s.x + " ~ " + s.z), false);
                    return 1;
                }))
                .then(Commands.literal("close").executes(ctx -> {
                    // closes the gates within 12 blocks
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Data data = Data.get(ctx.getSource().getServer());
                    List<Gate> near = data.gates.stream()
                            .filter(g -> g.dimension.equals(p.level().dimension()) && g.pos.distSqr(p.blockPosition()) < 16 * 16).toList();
                    for (Gate g : near) {
                        closePortal(ctx.getSource().getServer(), g);
                        data.gates.remove(g);
                    }
                    int closed = near.size();
                    data.setDirty();
                    com.soybe.awakeningsystem.DungeonManager.syncRiftsToAll(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.translatable("awakening_system.ruin_gate.closed", closed), false);
                    return closed;
                }))
                .then(Commands.literal("test").then(Commands.argument("kind", StringArgumentType.word())
                        .executes(ctx -> test(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), "C"))
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .executes(ctx -> test(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), StringArgumentType.getString(ctx, "rank"))))))
                .then(Commands.literal("enter").then(Commands.argument("kind", StringArgumentType.word())
                        .suggests((c, b) -> { KINDS.keySet().forEach(b::suggest); return b.buildFuture(); })
                        .executes(ctx -> enterCommand(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), null))
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((c, b) -> { RANKS.forEach(b::suggest); return b.buildFuture(); })
                                .executes(ctx -> enterCommand(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), StringArgumentType.getString(ctx, "rank"))))))
                .then(Commands.argument("kind", StringArgumentType.word())
                        .suggests((c, b) -> { KINDS.keySet().forEach(b::suggest); return b.buildFuture(); })
                        .executes(ctx -> raiseCommand(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), null))
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((c, b) -> { RANKS.forEach(b::suggest); return b.buildFuture(); })
                                .executes(ctx -> raiseCommand(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), StringArgumentType.getString(ctx, "rank"))))));
    }

    /** Reads the kind and rank typed in a command; a missing rank is rolled. Null when either is wrong. */
    private static String[] readKindAndRank(net.minecraft.commands.CommandSourceStack source, String kind, String rank) {
        if (!KINDS.containsKey(kind)) {
            source.sendFailure(Component.translatable("awakening_system.ruin_gate.unknown"));
            return null;
        }
        String r = rank == null ? randomRank(source.getLevel().getRandom()) : parseRank(rank);
        if (r == null) {
            source.sendFailure(Component.translatable("awakening_system.ruin_gate.unknown_rank"));
            return null;
        }
        return new String[]{kind, r};
    }

    private static int raiseCommand(net.minecraft.commands.CommandSourceStack source, String kind, String rank) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String[] kr = readKindAndRank(source, kind, rank);
        if (kr == null) return 0;
        raiseGate(source.getPlayerOrException(), KINDS.get(kr[0]), kr[1]);
        return 1;
    }

    private static int enterCommand(net.minecraft.commands.CommandSourceStack source, String kind, String rank) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String[] kr = readKindAndRank(source, kind, rank);
        if (kr == null) return 0;
        enter(source.getPlayerOrException(), KINDS.get(kr[0]), kr[1]);
        return 1;
    }

    /** Builds a copy without anyone entering it and reports what came out (works from the console). */
    private static int test(net.minecraft.commands.CommandSourceStack source, String kind, String rank) {
        Kind k = KINDS.get(kind);
        String r = parseRank(rank);
        ServerLevel realm = source.getServer().getLevel(RUIN_REALM);
        if (k == null || r == null || realm == null) return 0;
        Data data = Data.get(source.getServer());
        BlockPos origin = new BlockPos(-30000 - (data.nextRun++ % 200) * 1600, 120, 30000);
        List<BoundingBox> boxes = build(realm, k, origin);
        Session s = new Session();
        s.rank = r;
        int mobs = boxes.isEmpty() ? 0 : processMarkers(realm, k, boxes, s);
        Map<String, Integer> tables = new java.util.TreeMap<>();
        for (BoundingBox b : boxes) for (BlockPos p : BlockPos.betweenClosed(b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ()))
            if (realm.getBlockEntity(p) instanceof RandomizableContainerBlockEntity c && c.getLootTable() != null)
                tables.merge(c.getLootTable().location().getPath(), 1, Integer::sum);
        Mob boss = s.boss == null ? null : (realm.getEntity(s.boss) instanceof Mob m ? m : null);
        String msg = "[Ruin Gate] test " + k.id + " rank " + r + " at " + origin.toShortString() + ": " + boxes.size() + " pieces, " + mobs + " monsters (level "
                + s.sampleLevel + "), containers " + tables + ", boss " + (boss == null ? "none" : boss.getType().toShortString() + " level "
                + boss.getPersistentData().getInt("rpg_mob_level")) + ", boss chest spot " + (s.bossLoot != null) + ", " + s.exits.size()
                + " exits, spawn " + standableNear(realm, origin).toShortString();
        AwakeningSystemMod.LOGGER.info(msg);
        source.sendSuccess(() -> Component.literal(msg), false);
        // the entrance ruin with its portal, raised near the world spawn (it closes on the next check)
        ServerLevel world = source.getServer().overworld();
        BlockPos at = world.getSharedSpawnPos().offset(30 + data.nextRun * 20, 0, 0);
        world.getChunk(at.getX() >> 4, at.getZ() >> 4);
        at = new BlockPos(at.getX(), world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at.getX(), at.getZ()), at.getZ());
        boolean opened = openGate(world, k, r, at, world.getGameTime() + 200);
        Gate g = opened ? data.gates.get(data.gates.size() - 1) : null;
        long blocks = g == null ? 0 : g.portal.stream().filter(p -> world.getBlockState(p).is(com.soybe.awakeningsystem.block.SystemBlocks.RUIN_PORTAL.get())).count();
        AwakeningSystemMod.LOGGER.info("[Ruin Gate] entrance at {}: opened {}, {} portal blocks ({}), axis {}", at.toShortString(), opened, blocks,
                g == null ? "-" : g.portal.get(0).toShortString(),
                g == null ? "-" : world.getBlockState(g.portal.get(0)).getOptionalValue(com.soybe.awakeningsystem.block.RuinPortalBlock.AXIS).map(Object::toString).orElse("?"));
        // one roll of each table, to see what a run hands out
        for (String table : List.of(r.toLowerCase(java.util.Locale.ROOT), r.toLowerCase(java.util.Locale.ROOT) + "_boss", "supplies")) {
            net.minecraft.world.level.storage.loot.LootParams params = new net.minecraft.world.level.storage.loot.LootParams.Builder(realm)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                    .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
            List<String> items = new ArrayList<>();
            for (int roll = 0; roll < 3; roll++) {
                for (net.minecraft.world.item.ItemStack st : source.getServer().reloadableRegistries().getLootTable(loot("ruin_gate/" + table)).getRandomItems(params))
                    items.add(st.getCount() + " " + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(st.getItem()).getPath());
                items.add("|");
            }
            AwakeningSystemMod.LOGGER.info("[Ruin Gate] sample {}: {}", table, items);
        }
        return 1;
    }

    // ---------------------------------------------------------------- the gate in the world
    /** Opens a gate a few blocks in front of the player. */
    private static void raiseGate(ServerPlayer player, Kind kind, String rank) {
        ServerLevel level = player.serverLevel();
        BlockPos ahead = player.blockPosition().relative(player.getDirection(), 12);
        level.getChunk(ahead.getX() >> 4, ahead.getZ() >> 4);
        BlockPos pos = new BlockPos(ahead.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ahead.getX(), ahead.getZ()), ahead.getZ());
        if (!openGate(level, kind, rank, pos, 0L)) {   // opened by command: stays until closed
            player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.failed"));
            return;
        }
        player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.spawned", gateName(kind.id, rank)));
    }

    /**
     * Raises the gate's entrance ruin on the ground at {@code pos} (turned at random) and fills the opening
     * the ruin was made for with portal blocks: a doorway in the temple, a pit on top of the mine ruin.
     */
    private static boolean openGate(ServerLevel level, Kind kind, String rank, BlockPos pos, long expireTick) {
        Optional<StructureTemplate> template = level.getStructureManager().get(id(kind.entrance));
        if (template.isEmpty()) return false;
        StructureTemplate t = template.get();
        net.minecraft.world.level.block.Rotation rotation = net.minecraft.world.level.block.Rotation.getRandom(level.getRandom());
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
        BoundingBox local = t.getBoundingBox(settings, BlockPos.ZERO);
        BlockPos corner = new BlockPos(pos.getX() - (local.minX() + local.maxX()) / 2, pos.getY() - 1, pos.getZ() - (local.minZ() + local.maxZ()) / 2);
        BoundingBox box = t.getBoundingBox(settings, corner);
        // clear the trees and plants standing where the ruin goes (and a little round it), so it is not buried in a forest
        for (int x = box.minX() - 4; x <= box.maxX() + 4; x++) for (int z = box.minZ() - 4; z <= box.maxZ() + 4; z++)
            for (int y = box.minY(); y <= box.maxY() + 24; y++) {
                BlockPos p = new BlockPos(x, y, z);
                BlockState st = level.getBlockState(p);
                if (st.is(net.minecraft.tags.BlockTags.LOGS) || st.is(net.minecraft.tags.BlockTags.LEAVES) || st.is(Blocks.VINE)
                        || st.is(Blocks.BAMBOO) || st.is(Blocks.COCOA) || (st.canBeReplaced() && st.getFluidState().isEmpty() && !st.isAir()))
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        t.placeInWorld(level, corner, corner, settings, level.getRandom(), Block.UPDATE_CLIENTS);
        // footings: fill the gaps under the ruin's bottom layer so it does not float on a slope
        for (int x = box.minX(); x <= box.maxX(); x++) for (int z = box.minZ(); z <= box.maxZ(); z++) {
            if (level.getBlockState(new BlockPos(x, box.minY(), z)).isAir()) continue;
            for (int y = box.minY() - 1; y > box.minY() - 8; y--) {
                BlockPos p = new BlockPos(x, y, z);
                BlockState st = level.getBlockState(p);
                if (!st.canBeReplaced() && st.getFluidState().isEmpty()) break;
                level.setBlock(p, (x + z) % 3 == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        List<BlockPos> lights = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()))
            if (level.getBlockState(p).is(Blocks.LIGHT)) lights.add(p.immutable());
        if (lights.isEmpty()) return false;
        List<BlockPos> portal = new ArrayList<>();
        Direction.Axis axis;
        if (lights.size() == 1) {
            // a single marker sits at the bottom of a pit: the portal lies flat and fills the pit's floor
            axis = Direction.Axis.Y;
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) portal.add(lights.get(0).offset(dx, 0, dz));
        } else {
            boolean sameZ = lights.stream().allMatch(p -> p.getZ() == lights.get(0).getZ());
            axis = sameZ ? Direction.Axis.X : Direction.Axis.Z;
            portal.addAll(lights);
        }
        BlockState portalState = com.soybe.awakeningsystem.block.SystemBlocks.RUIN_PORTAL.get().defaultBlockState()
                .setValue(com.soybe.awakeningsystem.block.RuinPortalBlock.AXIS, axis);
        for (BlockPos p : portal) {
            level.setBlock(p, portalState, Block.UPDATE_CLIENTS);
            // the mine ruin has four stone wall posts over the pit's corners: they would stand in the portal
            if (axis == Direction.Axis.Y) level.setBlock(p.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockPos centre = portal.get(portal.size() / 2);
        Data data = Data.get(level.getServer());
        data.gates.add(new Gate(kind.id, rank, level.dimension(), centre, 0F, expireTick, portal));
        data.setDirty();
        level.playSound(null, centre, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.2F);
        com.soybe.awakeningsystem.DungeonManager.syncRiftsToAll(level.getServer());
        return true;
    }

    /** Takes the portal blocks out of a gate that closes; the ruin stays as an empty shell. */
    private static void closePortal(MinecraftServer server, Gate gate) {
        ServerLevel level = server.getLevel(gate.dimension);
        if (level == null) return;
        for (BlockPos p : gate.portal) {
            level.getChunk(p.getX() >> 4, p.getZ() >> 4);
            if (level.getBlockState(p).is(com.soybe.awakeningsystem.block.SystemBlocks.RUIN_PORTAL.get()))
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** Players who touched a portal this tick; they go through on the next server tick, outside their own movement. */
    private static final Map<UUID, BlockPos> TOUCHED = new HashMap<>();

    /** Called by the portal block when a player walks into it. */
    public static void onPortalTouched(ServerPlayer player, BlockPos pos) {
        TOUCHED.putIfAbsent(player.getUUID(), pos.immutable());
    }

    /**
     * Opens the exits once the boss falls: each exit marker becomes a portal block, set across the
     * doorway it stands in (between its walls, or in line with the exit blocks next to it).
     */
    private static void openExits(ServerLevel level, Session s) {
        BlockState base = com.soybe.awakeningsystem.block.SystemBlocks.RUIN_PORTAL.get().defaultBlockState();
        for (BlockPos exit : s.exits) {
            if (!level.getBlockState(exit).canBeReplaced()) continue;
            Direction.Axis axis;
            if (s.exits.contains(exit.east()) || s.exits.contains(exit.west())) axis = Direction.Axis.X;
            else if (s.exits.contains(exit.north()) || s.exits.contains(exit.south())) axis = Direction.Axis.Z;
            else if (level.getBlockState(exit.north()).isSolid() && level.getBlockState(exit.south()).isSolid()) axis = Direction.Axis.Z;
            else axis = Direction.Axis.X;
            level.setBlock(exit, base.setValue(com.soybe.awakeningsystem.block.RuinPortalBlock.AXIS, axis), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Every few minutes a gate may open on its own in the Overworld, 40 to 90 blocks from a player, in a
     * loaded chunk on dry ground. It closes after half an hour; at most a few are open at once.
     */
    private static void tickNaturalGates(MinecraftServer server, Data data) {
        ServerLevel world = server.overworld();
        long now = world.getGameTime();
        List<Gate> expired = data.gates.stream().filter(g -> g.expireTick > 0 && now > g.expireTick).toList();
        boolean removed = !expired.isEmpty();
        for (Gate g : expired) {
            closePortal(server, g);
            data.gates.remove(g);
        }
        if (removed) {
            data.setDirty();
            com.soybe.awakeningsystem.DungeonManager.syncRiftsToAll(server);
        }
        if (server.getTickCount() % 20 == 0) placeFixedGates(server, data);
        if (!RANDOM_GATES || server.getTickCount() % NATURAL_CHECK_TICKS != 0) return;
        long open = data.gates.stream().filter(g -> g.expireTick > 0).count();
        List<ServerPlayer> players = new ArrayList<>(world.players());
        players.removeIf(p -> p.isSpectator() || SESSIONS.containsKey(p.getUUID()));
        if (open >= MAX_NATURAL_GATES || players.isEmpty() || world.getRandom().nextFloat() > NATURAL_CHANCE) return;
        ServerPlayer near = players.get(world.getRandom().nextInt(players.size()));
        for (int attempt = 0; attempt < 12; attempt++) {
            double a = world.getRandom().nextDouble() * Math.PI * 2;
            int dist = 40 + world.getRandom().nextInt(51);
            int x = (int) Math.floor(near.getX() + Math.cos(a) * dist), z = (int) Math.floor(near.getZ() + Math.sin(a) * dist);
            if (!world.hasChunk(x >> 4, z >> 4)) continue;
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState ground = world.getBlockState(pos.below());
            if (y < world.getSeaLevel() - 2 || !ground.getFluidState().isEmpty() || !ground.isSolid()) continue;
            boolean nearOther = false;
            for (Gate g : data.gates) if (g.dimension.equals(world.dimension()) && g.pos.distSqr(pos) < 120 * 120) nearOther = true;
            if (nearOther) continue;
            List<String> kinds = new ArrayList<>(KINDS.keySet());
            Kind kind = KINDS.get(kinds.get(world.getRandom().nextInt(kinds.size())));
            String rank = randomRank(world.getRandom());
            if (!openGate(world, kind, rank, pos, now + NATURAL_LIFETIME)) continue;
            for (ServerPlayer p : players) {
                if (p.distanceToSqr(x, y, z) < 200 * 200)
                    p.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.spawned", gateName(kind.id, rank)));
            }
            AwakeningSystemMod.LOGGER.info("[Ruin Gate] a {} gate of rank {} opened at {}", kind.id, rank, pos.toShortString());
            return;
        }
    }

    // ---------------------------------------------------------------- fixed gates
    /*
     * The gates are part of the world: the Overworld is cut into cells of CELL blocks and most cells hold one gate,
     * at a spot, kind and rank fixed by the world seed. The gate's entrance ruin is raised the first time the chunk
     * holding its spot is generated (once its neighbours are loaded too), and it stays open for good: every run
     * through it builds a fresh copy of its dungeon. Its name and rank float over it (AwakeningSystemClient).
     */
    /** The old gates that opened at random near a player and closed after half an hour. Off: the gates are fixed now. */
    private static final boolean RANDOM_GATES = false;
    private static final int CELL = 576;
    private static final float CELL_CHANCE = 0.6F;
    /** No gates this close to the world spawn (the Great Tower and the Hunters' Association stand there). */
    private static final int SPAWN_CLEARANCE = 320;

    private record Spot(int x, int z, String kind, String rank) {}

    /** The gate of a cell, or null when the cell has none. */
    private static Spot fixedSpot(long seed, BlockPos worldSpawn, int cx, int cz) {
        java.util.Random r = new java.util.Random(seed ^ (cx * 341873128712L + cz * 132897987541L) ^ 0x5EED6A7EL);
        if (r.nextFloat() > CELL_CHANCE) return null;
        int x = cx * CELL + 64 + r.nextInt(CELL - 128), z = cz * CELL + 64 + r.nextInt(CELL - 128);
        if (Math.hypot(x - worldSpawn.getX(), z - worldSpawn.getZ()) < SPAWN_CLEARANCE) return null;
        List<String> kinds = new ArrayList<>(new java.util.TreeSet<>(KINDS.keySet()));
        String kind = kinds.get(r.nextInt(kinds.size()));
        int total = 0;
        for (int w : RANK_WEIGHTS) total += w;
        int roll = r.nextInt(total);
        String rank = "C";
        for (int i = 0; i < RANK_WEIGHTS.length; i++) {
            roll -= RANK_WEIGHTS[i];
            if (roll < 0) { rank = RANKS.get(i); break; }
        }
        return new Spot(x, z, kind, rank);
    }

    /** A newly generated Overworld chunk that holds a cell's gate spot puts that cell in the queue. */
    @SubscribeEvent
    public static void onChunkLoad(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)) return;
        ChunkPos cp = event.getChunk().getPos();
        int cx = Math.floorDiv(cp.getMinBlockX(), CELL), cz = Math.floorDiv(cp.getMinBlockZ(), CELL);
        Spot spot = fixedSpot(level.getSeed(), level.getSharedSpawnPos(), cx, cz);
        if (spot == null || spot.x >> 4 != cp.x || spot.z >> 4 != cp.z) return;
        Data data = Data.get(level.getServer());
        long cell = ChunkPos.asLong(cx, cz);
        if (!data.cells.contains(cell) && data.pending.add(cell)) data.setDirty();
    }

    /** Raises the queued gates whose ground is loaded all round. */
    private static void placeFixedGates(MinecraftServer server, Data data) {
        if (data.pending.isEmpty()) return;
        ServerLevel world = server.overworld();
        boolean changed = false;
        for (long cell : new ArrayList<>(data.pending)) {
            int cx = ChunkPos.getX(cell), cz = ChunkPos.getZ(cell);
            Spot spot = fixedSpot(world.getSeed(), world.getSharedSpawnPos(), cx, cz);
            if (spot == null || data.cells.contains(cell)) {
                data.pending.remove(cell);
                changed = true;
                continue;
            }
            boolean loaded = true;
            for (int dx = -1; dx <= 1 && loaded; dx++)
                for (int dz = -1; dz <= 1 && loaded; dz++)
                    loaded = world.hasChunk((spot.x >> 4) + dx, (spot.z >> 4) + dz);
            if (!loaded) continue;
            data.pending.remove(cell);
            data.cells.add(cell);
            changed = true;
            // the spot itself, or a dry patch near it
            for (int attempt = 0; attempt < 9; attempt++) {
                int x = spot.x + (attempt == 0 ? 0 : (attempt % 3 - 1) * 8), z = spot.z + (attempt == 0 ? 0 : (attempt / 3 - 1) * 8);
                int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockState ground = world.getBlockState(new BlockPos(x, y - 1, z));
                if (y < world.getSeaLevel() - 2 || !ground.getFluidState().isEmpty() || !ground.isSolid()) continue;
                if (openGate(world, KINDS.get(spot.kind), spot.rank, new BlockPos(x, y, z), 0L))
                    AwakeningSystemMod.LOGGER.info("[Ruin Gate] fixed {} gate of rank {} raised at {} {} {}", spot.kind, spot.rank, x, y, z);
                break;
            }
        }
        if (changed) data.setDirty();
    }

    /** The fixed gate nearest to a place (searching a few cells round it), for the locate command. */
    private static Spot nearestSpot(ServerLevel world, BlockPos from) {
        Spot best = null;
        double bestD = Double.MAX_VALUE;
        int cx = Math.floorDiv(from.getX(), CELL), cz = Math.floorDiv(from.getZ(), CELL);
        for (int a = cx - 3; a <= cx + 3; a++)
            for (int b = cz - 3; b <= cz + 3; b++) {
                Spot s = fixedSpot(world.getSeed(), world.getSharedSpawnPos(), a, b);
                if (s == null) continue;
                double d = Math.hypot(s.x - from.getX(), s.z - from.getZ());
                if (d < bestD) { bestD = d; best = s; }
            }
        return best;
    }

    /** The nearest fixed gate's spot as {x, z}, or null (used by the development photographer). */
    public static int[] nearestFixedGate(ServerLevel world, BlockPos from) {
        Spot s = nearestSpot(world, from);
        return s == null ? null : new int[]{s.x, s.z};
    }

    /** Kind code sent to the client in the rift entry, so it can name the gate over it. */
    private static int kindCode(String kind) {
        return switch (kind) {
            case "dark" -> 1;
            case "temple" -> 2;
            default -> 0;
        };
    }

    /** Called when the rift list is sent to a player: the Ruin Gates show on the maps, and the open exits as portals. */
    public static void addRiftEntries(ServerPlayer player, List<com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry> entries) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        for (Gate gate : Data.get(server).gates) {
            if (gate.dimension.equals(player.level().dimension()))
                entries.add(new com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry(gate.pos, gate.rank, false, PORTAL_TYPE, gate.yaw, kindCode(gate.kind),
                        gate.expireTick <= 0 ? -1 : (int) Math.max(0, gate.expireTick - player.serverLevel().getGameTime())));
        }
        Session s = SESSIONS.get(player.getUUID());

    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        Data data = Data.get(server);
        if (!TOUCHED.isEmpty()) {
            Map<UUID, BlockPos> touched = new HashMap<>(TOUCHED);
            TOUCHED.clear();
            for (Map.Entry<UUID, BlockPos> e : touched.entrySet()) {
                ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
                if (player == null) continue;
                Session run = SESSIONS.get(player.getUUID());
                if (run != null) {
                    // inside a dungeon: the open exits lead home
                    if (run.bossDead && player.level().dimension().equals(RUIN_REALM) && run.exits.contains(e.getValue())) leave(player);
                    continue;
                }
                Long left = LEFT_AT.get(player.getUUID());
                if (left != null && server.getTickCount() - left < REENTRY_DELAY) continue;   // just came out
                for (Gate gate : data.gates) {
                    if (gate.dimension.equals(player.level().dimension()) && gate.portal.contains(e.getValue())) {
                        enter(player, KINDS.get(gate.kind), gate.rank);
                        break;
                    }
                }
            }
        }
        if (server.getTickCount() % 5 != 0) return;
        tickNaturalGates(server, data);
        boolean sparkle = server.getTickCount() % 10 == 0;
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
                if (sparkle && !s.bossDead) realm.sendParticles(ParticleTypes.SMOKE, exit.getX() + 0.5, exit.getY() + 0.8, exit.getZ() + 0.5, 4, 0.3, 0.5, 0.3, 0.01);
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
    private static void enter(ServerPlayer player, Kind kind, String rank) {
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
        player.displayClientMessage(Component.translatable("awakening_system.ruin_gate.entering", gateName(kind.id, rank)), true);

        List<BoundingBox> boxes = build(realm, kind, origin);
        if (boxes.isEmpty()) {
            player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.failed"));
            return;
        }
        Session s = new Session();
        s.kind = kind.id;
        s.rank = rank;
        s.returnDim = player.level().dimension();
        s.teyvat = s.returnDim.equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
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
        AwakeningSystemMod.LOGGER.info("[Ruin Gate] {} rank {} built at {} for {}: {} pieces, {} monsters, boss {}, {} exits",
                kind.id, rank, origin.toShortString(), player.getName().getString(), boxes.size(), mobs, s.boss != null, s.exits.size());
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
        List<RandomizableContainerBlockEntity> containers = new ArrayList<>();
        ResourceKey<LootTable> rankLoot = loot("ruin_gate/" + s.rank.toLowerCase(java.util.Locale.ROOT));
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
                    containers.add(container);
                }
            }
        }
        // about a third of the containers (chests first, then barrels and the rest) hold the rank's loot,
        // up to a limit; the others hold plain supplies
        java.util.Random shuffle = new java.util.Random(level.getRandom().nextLong());
        java.util.Collections.shuffle(containers, shuffle);
        containers.sort(java.util.Comparator.comparingInt(c -> c instanceof ChestBlockEntity ? 0 : 1));
        int treasure = Math.min(MAX_RANK_CHESTS, Math.max(Math.min(4, containers.size()), containers.size() / 3));
        for (int i = 0; i < containers.size(); i++) {
            containers.get(i).setLootTable(i < treasure ? rankLoot : SUPPLIES, level.getRandom().nextLong());
        }
        // the pieces hold far more marker blocks than a fight needs (some are plain decoration), so a
        // random share of them, spread over the whole dungeon, gets a monster
        java.util.Collections.shuffle(spots, new java.util.Random(level.getRandom().nextLong()));
        for (Map.Entry<BlockPos, String> spot : spots) {
            if (mobs >= MAX_MONSTERS) break;
            Mob mob = spawn(level, spot.getValue(), spot.getKey(), rankLevel(s.rank, false, level.getRandom()));
            if (mob != null) {
                mobs++;
                s.sampleLevel = mob.getPersistentData().getInt("rpg_mob_level");
            }
        }
        if (bossPos != null) {
            Mob boss = spawnBoss(level, kind, bossPos, s);
            if (boss != null) {
                s.boss = boss.getUUID();
                boss.addTag("no_rpg_scaling");
                com.soybe.awakeningsystem.RpgLevelZoneManager.applyMobLevelAttributes(boss, rankLevel(s.rank, true, level.getRandom()), false);
                if (boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) instanceof net.minecraft.world.entity.ai.attributes.AttributeInstance hp
                        && !s.teyvat) {
                    // a single vanilla monster needs more health than its kind to hold a boss room
                    hp.setBaseValue(hp.getBaseValue() * 2.5);
                    boss.setHealth(boss.getMaxHealth());
                }
            }
        }
        if (s.boss == null) {   // no boss in this copy: the exits are open from the start
            s.bossDead = true;
            openExits(level, s);
        }
        return mobs;
    }

    /**
     * The boss. A gate opened in Teyvat keeps Teyvat's ruin machines and Lawachurls; anywhere else the
     * boss is an Overworld monster, so nothing from the later worlds drops.
     */
    private static Mob spawnBoss(ServerLevel level, Kind kind, BlockPos pos, Session s) {
        if (s.teyvat) {
            SeriesStructureWorldGen.spawnOverworldMiniboss(level, pos.below());
            List<Mob> found = level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(3),
                    m -> m.getPersistentData().getBoolean("is_overworld_miniboss"));
            return found.isEmpty() ? null : found.get(0);
        }
        Mob boss = spawn(level, kind.boss, pos, rankLevel(s.rank, true, level.getRandom()));
        if (boss == null) return null;
        CompoundTag tag = boss.getPersistentData();
        // the same arena leash the other bosses use, so it cannot be dragged out of its room
        tag.putDouble("arena_center_x", pos.getX() + 0.5);
        tag.putDouble("arena_center_y", pos.getY());
        tag.putDouble("arena_center_z", pos.getZ() + 0.5);
        tag.putDouble("arena_max_radius", 20.0);
        tag.putBoolean("is_overworld_miniboss", true);
        return boss;
    }

    /**
     * Spawns one monster at the gate's level. It is kept out of the usual level scaling, which would
     * otherwise match it to the player and make it drop gear from the later worlds.
     */
    private static Mob spawn(ServerLevel level, String typeId, BlockPos pos, int mobLevel) {
        Optional<EntityType<?>> type = EntityType.byString(typeId);
        if (type.isEmpty()) return null;
        Entity e = type.get().create(level);
        if (!(e instanceof Mob mob)) return null;
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
        EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        mob.addTag("no_rpg_scaling");
        com.soybe.awakeningsystem.RpgLevelZoneManager.applyMobLevelAttributes(mob, mobLevel, false);
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
            openExits(level, s);
            BlockPos chestPos = s.bossLoot != null ? s.bossLoot : event.getEntity().blockPosition();
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)
                chest.setLootTable(loot("ruin_gate/" + s.rank.toLowerCase(java.util.Locale.ROOT) + "_boss"), level.getRandom().nextLong());
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(e.getKey());
            if (p != null) {
                com.soybe.awakeningsystem.DungeonManager.syncRiftsToPlayer(p);   // the exit portals appear
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
        // they went in by stepping into the portal, so that is where they came from: set them down
        // clear of it, or they would walk straight back in
        Vec3 to = clearOfPortals(back, s.returnPos);
        LEFT_AT.put(player.getUUID(), (long) player.getServer().getTickCount());
        player.teleportTo(back, to.x, to.y, to.z, s.returnYaw, 0.0F);
        player.fallDistance = 0;
        player.sendSystemMessage(Component.translatable("awakening_system.ruin_gate.left"));
        return true;
    }

    /** When a player last came out of a dungeon: portals ignore them for a few seconds after. */
    private static final Map<UUID, Long> LEFT_AT = new HashMap<>();
    private static final int REENTRY_DELAY = 100;

    /** A standing spot near {@code from} at least three blocks from any Ruin Portal block. */
    private static Vec3 clearOfPortals(ServerLevel level, Vec3 from) {
        BlockPos origin = BlockPos.containing(from);
        net.minecraft.world.level.block.Block portal = com.soybe.awakeningsystem.block.SystemBlocks.RUIN_PORTAL.get();
        if (BlockPos.findClosestMatch(origin, 2, 2, p -> level.getBlockState(p).is(portal)).isEmpty()) return from;
        for (int r = 3; r <= 9; r++) for (int dy = 4; dy >= -6; dy--) for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
            BlockPos p = origin.offset(dx, dy, dz);
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty() || !level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()
                    || level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty()) continue;
            if (!level.getBlockState(p).getFluidState().isEmpty()) continue;
            if (BlockPos.findClosestMatch(p, 2, 2, q -> level.getBlockState(q).is(portal)).isPresent()) continue;
            return Vec3.atBottomCenterOf(p);
        }
        return from.add(0, 3, 0);
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
    /** A gate standing in the world: its entrance ruin's portal blocks, with {@code pos} (their middle) for the maps. */
    private record Gate(String kind, String rank, ResourceKey<Level> dimension, BlockPos pos, float yaw, long expireTick, List<BlockPos> portal) {}

    private static final class Data extends SavedData {
        final List<Gate> gates = new ArrayList<>();
        /** Cells whose fixed gate was already raised (or found no dry ground), and cells waiting for their ground to load. */
        final Set<Long> cells = new HashSet<>(), pending = new HashSet<>();
        int nextRun;

        static Data get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(Data::new, Data::load, null), "awakening_ruin_gates");
        }

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            d.nextRun = tag.getInt("NextRun");
            for (long l : tag.getLongArray("Cells")) d.cells.add(l);
            for (long l : tag.getLongArray("Pending")) d.pending.add(l);
            ListTag list = tag.getList("Gates", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag g = list.getCompound(i);
                ResourceLocation dim = ResourceLocation.tryParse(g.getString("Dimension"));
                if (dim == null) continue;
                List<BlockPos> portal = new ArrayList<>();
                for (long l : g.getLongArray("Portal")) portal.add(BlockPos.of(l));
                if (portal.isEmpty()) continue;   // a floating portal from the previous test version: nothing to walk into
                BlockPos pos = g.contains("Pos") ? BlockPos.of(g.getLong("Pos")) : portal.get(0);
                String rank = parseRank(g.getString("Rank"));
                d.gates.add(new Gate(g.getString("Kind"), rank == null ? "C" : rank, ResourceKey.create(Registries.DIMENSION, dim), pos,
                        g.getFloat("Yaw"), g.getLong("Expire"), portal));
            }
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("NextRun", nextRun);
            tag.putLongArray("Cells", cells.stream().mapToLong(Long::longValue).toArray());
            tag.putLongArray("Pending", pending.stream().mapToLong(Long::longValue).toArray());
            ListTag list = new ListTag();
            for (Gate g : gates) {
                CompoundTag c = new CompoundTag();
                c.putString("Kind", g.kind);
                c.putString("Rank", g.rank);
                c.putString("Dimension", g.dimension.location().toString());
                c.putLong("Pos", g.pos.asLong());
                c.putFloat("Yaw", g.yaw);
                c.putLong("Expire", g.expireTick);
                c.putLongArray("Portal", g.portal.stream().mapToLong(BlockPos::asLong).toArray());
                list.add(c);
            }
            tag.put("Gates", list);
            return tag;
        }
    }
}
