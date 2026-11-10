package com.soybe.awakeningsystem.tower;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.world.SeriesStructureWorldGen;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * There is one Great Tower per world, a short walk from spawn. It is raised the first time the server
 * runs, the Hunter Association office goes up beside it, and until a player awakens at its Altar of
 * Origin the System keeps calling them towards it.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class GreatTowerSite {
    private GreatTowerSite() {}

    private static final int[] DISTANCES = {190, 230, 270, 320};
    private static final int ANGLES = 12;
    private static final int CALL_EVERY = 20 * 150;     // the System's reminder, every 2.5 minutes

    // site search state (one candidate a tick, so the first start does not freeze)
    private static int candidate = -1;
    private static BlockPos best;
    private static int bestSpread = Integer.MAX_VALUE;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        Data data = Data.get(server);
        if (!data.built) {
            if (server.getTickCount() > 40) searchStep(overworld, data);
            return;
        }
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) callPlayer(player, data);
    }

    // ── choosing the site ────────────────────────────────────────────────────

    private static void searchStep(ServerLevel level, Data data) {
        BlockPos spawn = level.getSharedSpawnPos();
        int total = DISTANCES.length * ANGLES;
        if (candidate < 0) {
            candidate = 0;
            best = null;
            bestSpread = Integer.MAX_VALUE;
        }
        if (candidate < total) {
            int dist = DISTANCES[candidate / ANGLES];
            // the first angle depends on the seed, so each world has its tower somewhere else
            double a = Math.toRadians((level.getSeed() & 359L) + (candidate % ANGLES) * (360.0D / ANGLES));
            int x = spawn.getX() + (int) Math.round(Math.cos(a) * dist);
            int z = spawn.getZ() + (int) Math.round(Math.sin(a) * dist);
            candidate++;
            int[] result = evaluate(level, x, z);
            if (result != null && result[1] < bestSpread) {
                bestSpread = result[1];
                best = new BlockPos(x, result[0], z);
            }
            // a flat enough spot ends the search early
            if (bestSpread > 5 && candidate < total) return;
        }
        if (best == null) {
            // nothing usable (an ocean world?): build next to spawn on whatever is there
            int x = spawn.getX() + DISTANCES[0];
            best = new BlockPos(x, Math.max(level.getSeaLevel() + 1, surface(level, x, spawn.getZ())), spawn.getZ());
        }
        build(level, data, best);
        candidate = -1;
    }

    /** {groundY, spread} for a site, or null if it is water or someone's base. */
    private static int[] evaluate(ServerLevel level, int x, int z) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, sum = 0, n = 0;
        for (int i = -1; i < 8; i++) {
            int sx = x, sz = z;
            if (i >= 0) {
                double a = i * Math.PI / 4.0D;
                sx += (int) Math.round(Math.cos(a) * 22.0D);
                sz += (int) Math.round(Math.sin(a) * 22.0D);
            }
            int y = surface(level, sx, sz);
            BlockState top = level.getBlockState(new BlockPos(sx, y, sz));
            if (!top.getFluidState().isEmpty() || top.is(Blocks.ICE)) return null;
            if (looksBuilt(top)) return null;
            min = Math.min(min, y);
            max = Math.max(max, y);
            sum += y;
            n++;
        }
        if (min < level.getSeaLevel() || max > 150) return null;
        return new int[]{Math.round(sum / (float) n), max - min};
    }

    private static int surface(ServerLevel level, int x, int z) {
        // getHeight reads nothing from a chunk that is not loaded yet, so generate it first
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
    }

    /** Blocks a player would place: a site with these on top is someone's home. */
    private static boolean looksBuilt(BlockState s) {
        return s.is(BlockTags.PLANKS) || s.is(BlockTags.WOOL) || s.is(BlockTags.BEDS) || s.is(Blocks.GLASS)
                || s.is(Blocks.COBBLESTONE) || s.is(Blocks.CRAFTING_TABLE) || s.is(Blocks.CHEST) || s.is(Blocks.FURNACE)
                || s.is(Blocks.TORCH) || s.is(Blocks.STONE_BRICKS) || s.is(BlockTags.WOODEN_SLABS) || s.is(BlockTags.WOODEN_STAIRS);
    }

    private static void build(ServerLevel level, Data data, BlockPos origin) {
        BlockPos spawn = level.getSharedSpawnPos();
        int dx = spawn.getX() - origin.getX(), dz = spawn.getZ() - origin.getZ();
        Direction front = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
        long t0 = System.currentTimeMillis();
        GreatTowerBuilder.Layout layout = GreatTowerBuilder.build(level, origin, front);
        data.built = true;
        data.origin = origin;
        data.front = front;
        data.altar = layout.altar();
        data.portal = layout.portal();
        SeriesStructureWorldGen.SeriesStructureData.get(level.getServer()).recordStructure("great_tower", origin, Level.OVERWORLD);

        raiseAgency(level, data);
        data.setDirty();
        AwakeningSystemMod.LOGGER.info("Great Tower raised at {} facing {} in {} ms; Hunter Association at {}",
                origin, front, System.currentTimeMillis() - t0, data.office);
    }

    /** The Hunter Association agency stands a little way off the road to spawn, its door towards the tower. */
    private static void raiseAgency(ServerLevel level, Data data) {
        BlockPos origin = data.origin;
        Direction front = data.front;
        Direction right = front.getClockWise();
        BlockPos guess = origin.relative(front, 54).relative(right, 30);
        for (int cx = -2; cx <= 2; cx++)
            for (int cz = -2; cz <= 2; cz++) level.getChunk((guess.getX() >> 4) + cx, (guess.getZ() >> 4) + cz);
        int gy = surface(level, guess.getX(), guess.getZ());
        BlockPos office = new BlockPos(guess.getX(), gy, guess.getZ());
        placeAgency(level, office, origin);
        SeriesStructureWorldGen.SeriesStructureData.get(level.getServer()).recordStructure("hunter_association", office, Level.OVERWORLD);
        data.office = office;
        data.setDirty();
    }

    /**
     * The Hunter Association agency (the user's own hunter_agency structure: a reception hall with the desk
     * at the back and the door on its +Z side), turned so its door faces the tower, with the inspector
     * behind the desk and a dirt path to the plaza.
     */
    private static void placeAgency(ServerLevel level, BlockPos center, BlockPos tower) {
        var template = level.getStructureManager().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                AwakeningSystemMod.MOD_ID, "hunter_agency")).orElse(null);
        if (template == null) {
            SeriesStructureWorldGen.generateStructure(level, center, "hunter_office");
            return;
        }
        int dx = tower.getX() - center.getX(), dz = tower.getZ() - center.getZ();
        Direction door = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
        net.minecraft.world.level.block.Rotation rotation = switch (door) {
            case WEST -> net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
            case NORTH -> net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
            case EAST -> net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
            default -> net.minecraft.world.level.block.Rotation.NONE;
        };
        var size = template.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        var settings = new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings()
                .setRotation(rotation).setRotationPivot(pivot);
        BlockPos corner = center.subtract(pivot);
        // footing under the floor and a clear space above it
        for (int x = -1; x <= size.getX(); x++) {
            for (int z = -1; z <= size.getZ(); z++) {
                BlockPos col = net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
                        .calculateRelativePosition(settings, new BlockPos(x, 0, z)).offset(corner);
                for (int y = -1; y >= -10; y--) {
                    BlockPos p = col.above(y);
                    if (level.getBlockState(p).isSolid() && y < -1) break;
                    level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
                for (int y = 0; y <= size.getY() + 4; y++) {
                    BlockPos p = col.above(y);
                    if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        template.placeInWorld(level, corner, corner, settings, level.getRandom(), 2);
        // the inspector waits inside the U of the reception desk
        BlockPos desk = net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
                .calculateRelativePosition(settings, new BlockPos(8, 1, 3)).offset(corner);
        com.soybe.awakeningsystem.world.SeriesStructureBuilder.spawnNpcIfAbsent(level, desk,
                com.soybe.awakeningsystem.entity.ModEntities.HUNTER_EVALUATOR.get(), com.soybe.awakeningsystem.entity.NpcProfile.HUNTER_EVALUATOR);
        // a dirt path from the door to the tower plaza
        BlockPos doorPos = net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
                .calculateRelativePosition(settings, new BlockPos(8, 0, size.getZ())).offset(corner);
        double len = Math.sqrt(doorPos.distSqr(new BlockPos(tower.getX(), doorPos.getY(), tower.getZ())));
        for (int i = 0; i < len - 30; i++) {
            double t = i / len;
            int px = (int) Math.round(doorPos.getX() + (tower.getX() - doorPos.getX()) * t);
            int pz = (int) Math.round(doorPos.getZ() + (tower.getZ() - doorPos.getZ()) * t);
            for (int w = -1; w <= 1; w++) {
                int qx = px + (Math.abs(dz) > Math.abs(dx) ? w : 0), qz = pz + (Math.abs(dz) > Math.abs(dx) ? 0 : w);
                BlockPos top = new BlockPos(qx, surface(level, qx, qz), qz);
                BlockState s = level.getBlockState(top);
                if (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.COARSE_DIRT)) {
                    level.setBlock(top, Blocks.DIRT_PATH.defaultBlockState(), 2);
                    if (!level.getBlockState(top.above()).isAir()) level.setBlock(top.above(), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    // ── the System's call ────────────────────────────────────────────────────

    private static void callPlayer(ServerPlayer player, Data data) {
        SystemData sd = SystemEvents.data(player);
        if (sd.isAwakened()) return;
        if (!sd.towerHintKnown() || sd.towerHintX() != data.origin.getX() || sd.towerHintZ() != data.origin.getZ()) {
            sd.setTowerHint(data.origin.getX(), data.origin.getZ());
            AwakeningSystemMod.sync(player);
        }
        if (!player.level().dimension().equals(Level.OVERWORLD)) return;
        int dx = data.origin.getX() - player.getBlockX(), dz = data.origin.getZ() - player.getBlockZ();
        int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
        int age = player.tickCount;
        if (dist < 26) {
            if (age % 200 < 20) player.displayClientMessage(Component.translatable("awakening_system.tower.call_inside"), true);
            return;
        }
        boolean firstCall = age >= 100 && age < 120;
        if (!firstCall && age % CALL_EVERY >= 20) return;
        Component dir = Component.translatable("awakening_system.direction." + direction(dx, dz));
        if (firstCall) {
            player.sendSystemMessage(Component.translatable("awakening_system.tower.call_chat", String.valueOf(dist), dir));
        }
        player.displayClientMessage(Component.translatable("awakening_system.tower.call_actionbar", String.valueOf(dist), dir), true);
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.AMBIENT, 0.8F, 0.5F);
    }

    /** n, ne, e, se, s, sw, w, nw from an offset (north is -Z). */
    public static String direction(int dx, int dz) {
        double a = (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0D) % 360.0D;
        String[] names = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
        return names[(int) Math.round(a / 45.0D) % 8];
    }

    public static BlockPos towerOrigin(MinecraftServer server) {
        Data data = Data.get(server);
        return data.built ? data.origin : null;
    }

    // ── commands ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("greattower").requires(s -> s.hasPermission(2))
                .then(Commands.literal("info").executes(ctx -> {
                    Data d = Data.get(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(d.built
                            ? "Great Tower at " + d.origin.toShortString() + " facing " + d.front + ", altar " + d.altar.toShortString()
                              + ", Hunter Association " + (d.office == null ? "-" : d.office.toShortString())
                            : "The Great Tower has not been raised yet"), false);
                    return 1;
                }))
                .then(Commands.literal("snapshot").executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    Data d = Data.get(server);
                    if (!d.built) return 0;
                    try {
                        java.nio.file.Path dir = server.getServerDirectory().resolve("tower_snapshot");
                        java.nio.file.Files.createDirectories(dir);
                        GreatTowerSnapshot.write(server.overworld(), d.origin, d.front, dir);
                        if (d.office != null) GreatTowerSnapshot.plan(server.overworld(), d.office, 14, new int[]{0, 1, 2}, dir, "office");
                        long npcs = server.overworld().getEntities(com.soybe.awakeningsystem.entity.ModEntities.HUNTER_EVALUATOR.get(),
                                new net.minecraft.world.phys.AABB(d.office).inflate(32), e -> true).size();
                        ctx.getSource().sendSuccess(() -> Component.literal("Snapshot written to " + dir + "; inspectors near the office: " + npcs), false);
                    } catch (java.io.IOException e) {
                        ctx.getSource().sendFailure(Component.literal(e.toString()));
                    }
                    return 1;
                }))
                .then(Commands.literal("view").then(Commands.argument("structure", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(Commands.argument("half", IntegerArgumentType.integer(4, 120)).executes(ctx -> {
                    // debug: raise a structure far away and take pictures of it
                    MinecraftServer server = ctx.getSource().getServer();
                    ServerLevel lvl = server.overworld();
                    String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "structure");
                    int half = IntegerArgumentType.getInteger(ctx, "half");
                    int x = 3000 + (name.hashCode() & 1023), z = 3000;
                    BlockPos at = new BlockPos(x, surface(lvl, x, z), z);
                    SeriesStructureWorldGen.generateStructure(lvl, at, name);
                    BlockPos seen = at;
                    for (var e : SeriesStructureWorldGen.SeriesStructureData.get(server).getAll().entrySet())
                        if (e.getValue().getX() == x && e.getValue().getZ() == z) seen = e.getValue();
                    try {
                        java.nio.file.Path dir = server.getServerDirectory().resolve("tower_snapshot");
                        java.nio.file.Files.createDirectories(dir);
                        GreatTowerSnapshot.view(lvl, seen, half, half / 2 + 10, half + 20, dir, name);
                        BlockPos fs = seen;
                        ctx.getSource().sendSuccess(() -> Component.literal("Viewed " + name + " at " + fs.toShortString()), false);
                    } catch (java.io.IOException e) {
                        ctx.getSource().sendFailure(Component.literal(e.toString()));
                    }
                    return 1;
                }))))
                .then(Commands.literal("rebuild").executes(ctx -> {
                    // rebuilds the tower where it stands with the current design
                    Data d = Data.get(ctx.getSource().getServer());
                    if (!d.built) return 0;
                    GreatTowerBuilder.Layout l = GreatTowerBuilder.build(ctx.getSource().getServer().overworld(), d.origin, d.front);
                    d.altar = l.altar();
                    d.portal = l.portal();
                    d.setDirty();
                    ctx.getSource().sendSuccess(() -> Component.literal("Great Tower rebuilt"), false);
                    return 1;
                }))
                .then(Commands.literal("altar").executes(ctx -> {
                    Data d = Data.get(ctx.getSource().getServer());
                    if (!d.built) return 0;
                    d.altar = GreatTowerBuilder.rebuildAltar(ctx.getSource().getServer().overworld(), d.origin, d.front);
                    d.setDirty();
                    ctx.getSource().sendSuccess(() -> Component.literal("Altar of Origin rebuilt"), false);
                    return 1;
                }))
                .then(Commands.literal("agency").executes(ctx -> {
                    // for worlds whose tower was raised before the agency existed
                    Data d = Data.get(ctx.getSource().getServer());
                    if (!d.built) return 0;
                    raiseAgency(ctx.getSource().getServer().overworld(), d);
                    ctx.getSource().sendSuccess(() -> Component.literal("Hunter Association agency raised at " + d.office.toShortString()), false);
                    return 1;
                }))
                .then(Commands.literal("tp").executes(ctx -> {
                    Data d = Data.get(ctx.getSource().getServer());
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    if (!d.built) return 0;
                    BlockPos e = d.origin.relative(d.front, 26);
                    p.teleportTo(ctx.getSource().getServer().overworld(), e.getX() + 0.5D, e.getY() + 1, e.getZ() + 0.5D, d.front.getOpposite().toYRot(), 0.0F);
                    return 1;
                }))
                .then(Commands.literal("build").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Data d = Data.get(ctx.getSource().getServer());
                    build(p.serverLevel(), d, p.blockPosition().below());
                    return 1;
                }))
                .then(Commands.literal("unawaken").executes(ctx -> {
                    // testing only: forget the player's awakening and potential
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    SystemData sd = SystemEvents.data(p);
                    sd.resetForTesting();
                    AwakeningSystemMod.sync(p);
                    ctx.getSource().sendSuccess(() -> Component.literal("Awakening and potential cleared"), false);
                    return 1;
                }))
                .then(Commands.literal("measure").then(Commands.argument("rank", IntegerArgumentType.integer(0, 7)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Data d = Data.get(ctx.getSource().getServer());
                    if (!d.built) return 0;
                    AltarOfOrigin.touchWithResult(p, d.altar, IntegerArgumentType.getInteger(ctx, "rank"));
                    return 1;
                }))));
    }

    // ── saved state ──────────────────────────────────────────────────────────

    static final class Data extends SavedData {
        boolean built;
        BlockPos origin = BlockPos.ZERO, altar = BlockPos.ZERO, portal = BlockPos.ZERO, office;
        Direction front = Direction.SOUTH;

        static Data get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(Data::new, Data::load, null), "awakening_great_tower");
        }

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            d.built = tag.getBoolean("built");
            d.origin = BlockPos.of(tag.getLong("origin"));
            d.altar = BlockPos.of(tag.getLong("altar"));
            d.portal = BlockPos.of(tag.getLong("portal"));
            if (tag.contains("office")) d.office = BlockPos.of(tag.getLong("office"));
            d.front = Direction.from2DDataValue(tag.getInt("front"));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("built", built);
            tag.putLong("origin", origin.asLong());
            tag.putLong("altar", altar.asLong());
            tag.putLong("portal", portal.asLong());
            if (office != null) tag.putLong("office", office.asLong());
            tag.putInt("front", front.get2DDataValue());
            return tag;
        }
    }
}
