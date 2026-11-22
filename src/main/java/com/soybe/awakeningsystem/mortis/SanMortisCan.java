package com.soybe.awakeningsystem.mortis;

import com.mojang.brigadier.CommandDispatcher;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * "San Mortis": a giant tin of jurel, as big as a silo. Blue base, red body with a huge painted jurel and a
 * white heart, a yellow top band with the brand name in big letters, a railed roof reached by a ladder, and
 * a family of walking jurels living up there. Built with {@code /sanmortis}.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class SanMortisCan {
    private SanMortisCan() {}

    static final int R = 9;                 // radius of the tin
    static final int BLUE = 5, RED = 15, YELLOW = 8;
    static final int HEIGHT = BLUE + RED + YELLOW;

    /** 5-row pixel letters, each 3 or 4 columns wide. */
    private static final String[] FONT_KEYS = {"S", "A", "N", "M", "O", "R", "T", "I", " "};
    private static final String[][] FONT = {
            {"###", "#..", "###", "..#", "###"},
            {".#.", "#.#", "###", "#.#", "#.#"},
            {"#..#", "##.#", "#.##", "#..#", "#..#"},
            {"#...#", "##.##", "#.#.#", "#...#", "#...#"},
            {"###", "#.#", "#.#", "#.#", "###"},
            {"##.", "#.#", "##.", "#.#", "#.#"},
            {"###", ".#.", ".#.", ".#.", ".#."},
            {"#", "#", "#", "#", "#"},
            {"..", "..", "..", "..", ".."},
    };

    /** The painted jurel, 26 wide and 9 tall, head to the right. */
    private static final String[] FISH = {
            "..........###.............",
            "........#######...........",
            "##....###########BBB......",
            "###.####LLLLLLLLLLLBBBBB..",
            ".#######SSSSSSSSSSSSSSBEBB",
            "###.####SSSSSSSSSSSSSSSBBB",
            "##....#WWWWWWWWWWWWWWWWW..",
            "........#WWWWWWWWWW.......",
            "..........................",
    };

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("sanmortis").requires(s -> s.hasPermission(2)).executes(ctx -> {
            CommandSourceStack src = ctx.getSource();
            ServerLevel level = src.getLevel();
            BlockPos at = BlockPos.containing(src.getPosition());
            Direction facing = src.getEntity() != null ? src.getEntity().getDirection() : Direction.NORTH;
            BlockPos centre = at.relative(facing, R + 4);
            level.getChunk(centre);
            centre = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centre);
            build(level, centre, facing.getOpposite());
            src.sendSuccess(() -> Component.translatable("awakening_system.sanmortis.built"), true);
            return 1;
        }).then(Commands.literal("remove").executes(ctx -> {
            // frees the nearest can (within 40 blocks) so it can be broken again
            ServerLevel level = ctx.getSource().getLevel();
            Cans cans = Cans.of(level);
            BlockPos at = BlockPos.containing(ctx.getSource().getPosition());
            Long nearest = null;
            for (long l : cans.bases) if (BlockPos.of(l).distSqr(at) < 40 * 40 && (nearest == null || BlockPos.of(l).distSqr(at) < BlockPos.of(nearest).distSqr(at))) nearest = l;
            if (nearest == null) {
                ctx.getSource().sendFailure(Component.translatable("awakening_system.sanmortis.none_near"));
                return 0;
            }
            cans.bases.remove(nearest);
            cans.freed.add(nearest);
            cans.setDirty();
            ctx.getSource().sendSuccess(() -> Component.translatable("awakening_system.sanmortis.removed"), true);
            return 1;
        })).then(Commands.literal("dance").executes(ctx -> {
            // the jurels of every can start their dance now (for trying it out; the usual one comes every 4 minutes)
            WalkingJurelEntity.forcedStart = ctx.getSource().getLevel().getGameTime();
            ctx.getSource().sendSuccess(() -> Component.translatable("awakening_system.sanmortis.dance"), true);
            return 1;
        })).then(Commands.literal("view").executes(ctx -> {
            // debug: builds one far away and writes pictures of it to tower_snapshot
            ServerLevel level = ctx.getSource().getServer().overworld();
            BlockPos centre = new BlockPos(5000, 0, 5000);
            level.getChunk(centre);
            centre = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, centre);
            build(level, centre, Direction.SOUTH);
            try {
                java.nio.file.Path dir = ctx.getSource().getServer().getServerDirectory().resolve("tower_snapshot");
                java.nio.file.Files.createDirectories(dir);
                com.soybe.awakeningsystem.tower.GreatTowerSnapshot.view(level, centre, 14, 8, 40, dir, "san_mortis");
            } catch (java.io.IOException e) {
                ctx.getSource().sendFailure(Component.literal(e.toString()));
            }
            return 1;
        })));
    }

    /** Builds the tin standing on {@code base}, its label facing {@code front}, and moves the jurels in. */
    public static void build(ServerLevel level, BlockPos base, Direction front) {
        Cans.of(level).add(base);
        double frontAngle = Math.atan2(front.getStepZ(), front.getStepX());
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        // a foundation so it never floats
        for (int dx = -R - 1; dx <= R + 1; dx++)
            for (int dz = -R - 1; dz <= R + 1; dz++) {
                if (dx * dx + dz * dz > (R + 1.2) * (R + 1.2)) continue;
                for (int y = -1; y >= -6; y--) {
                    p.set(base.getX() + dx, base.getY() + y, base.getZ() + dz);
                    if (!level.getBlockState(p).isSolid()) level.setBlock(p, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
            }
        String label = "SAN MORTIS";
        int labelWidth = textWidth(label);
        // The paint is laid along the outer skin of the tin one column at a time, in order round the
        // circle, so no column of a letter is skipped or doubled where the circle steps diagonally.
        java.util.List<int[]> skin = new java.util.ArrayList<>();
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++)
                if (inside(dx, dz) && (!inside(dx + 1, dz) || !inside(dx - 1, dz) || !inside(dx, dz + 1) || !inside(dx, dz - 1)))
                    skin.add(new int[]{dx, dz});
        skin.sort(java.util.Comparator.comparingDouble(c -> relAngle(c[0], c[1], frontAngle)));
        int n = skin.size(), mid = 0;
        for (int i = 1; i < n; i++)
            if (Math.abs(relAngle(skin.get(i)[0], skin.get(i)[1], frontAngle)) < Math.abs(relAngle(skin.get(mid)[0], skin.get(mid)[1], frontAngle))) mid = i;
        java.util.Map<Long, Integer> alongOf = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            int a = Math.floorMod(mid - i + n / 2, n) - n / 2;            // + to the viewer's right
            alongOf.put(key(skin.get(i)[0], skin.get(i)[1]), a);
        }
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++) {
                if (!inside(dx, dz)) continue;
                Integer along = alongOf.get(key(dx, dz));
                boolean wall = along != null;
                for (int y = 0; y < HEIGHT; y++) {
                    p.set(base.getX() + dx, base.getY() + y, base.getZ() + dz);
                    // the tin is solid metal inside
                    BlockState s = wall ? paint(y, along, labelWidth, label) : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
                    level.setBlock(p, s, 2);
                }
                // the lid: a ribbed metal top, slightly domed
                p.set(base.getX() + dx, base.getY() + HEIGHT, base.getZ() + dz);
                level.setBlock(p, wall ? Blocks.YELLOW_CONCRETE.defaultBlockState()
                        : ((Math.abs(dx) + Math.abs(dz)) % 3 == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState()), 2);
                // railing round the edge of the lid
                if (wall) {
                    p.set(base.getX() + dx, base.getY() + HEIGHT + 1, base.getZ() + dz);
                    level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 3);
                }
            }
        // a ladder up the back, with a gap in the railing
        Direction back = front.getOpposite();
        BlockPos ladderBase = base.relative(back, R + 1);
        for (int y = 0; y <= HEIGHT; y++) {
            BlockPos lp = ladderBase.above(y);
            level.setBlock(lp, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, back), 2);
        }
        level.setBlock(base.relative(back, R).above(HEIGHT + 1), Blocks.AIR.defaultBlockState(), 3);
        // a little stair landing at the top of the ladder
        level.setBlock(base.relative(back, R - 1).above(HEIGHT + 1), Blocks.AIR.defaultBlockState(), 3);
        // where the edge of the circle steps diagonally, two bars only touch at a corner and can't join: a bar
        // goes in the inner corner of each step, so the railing runs all the way round
        int ry = base.getY() + HEIGHT + 1;
        java.util.List<BlockPos> corners = new java.util.ArrayList<>();
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++) {
                if (!level.getBlockState(new BlockPos(base.getX() + dx, ry, base.getZ() + dz)).is(Blocks.IRON_BARS)) continue;
                for (int sx = -1; sx <= 1; sx += 2)
                    for (int sz = -1; sz <= 1; sz += 2) {
                        BlockPos diag = new BlockPos(base.getX() + dx + sx, ry, base.getZ() + dz + sz);
                        if (!level.getBlockState(diag).is(Blocks.IRON_BARS)) continue;
                        BlockPos a = new BlockPos(base.getX() + dx + sx, ry, base.getZ() + dz), b = new BlockPos(base.getX() + dx, ry, base.getZ() + dz + sz);
                        if (level.getBlockState(a).is(Blocks.IRON_BARS) || level.getBlockState(b).is(Blocks.IRON_BARS)) continue;
                        // the corner cell nearer the middle is on the lid
                        BlockPos in = a.distSqr(base.atY(ry)) < b.distSqr(base.atY(ry)) ? a : b;
                        corners.add(in);
                    }
            }
        BlockPos gap1 = base.relative(back, R).above(HEIGHT + 1), gap2 = base.relative(back, R - 1).above(HEIGHT + 1);
        for (BlockPos q : corners) {
            if (q.distSqr(gap1) <= 2 || q.distSqr(gap2) <= 2) continue;   // keep the way in from the ladder open
            if (level.getBlockState(q).isAir()) level.setBlock(q, Blocks.IRON_BARS.defaultBlockState(), 2);
        }
        // join the railing: each bar was placed before some of its neighbours, so it is set again with the
        // shape its neighbours give it
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++) {
                p.set(base.getX() + dx, base.getY() + HEIGHT + 1, base.getZ() + dz);
                if (level.getBlockState(p).is(Blocks.IRON_BARS)) {
                    level.setBlock(p, net.minecraft.world.level.block.Block.updateFromNeighbourShapes(Blocks.IRON_BARS.defaultBlockState(), level, p), 2);
                }
            }

        // the jurels that live round its foot; their first dance comes two minutes from now
        long firstDance = level.getGameTime() + 2400;
        for (int i = 0; i < 7; i++) {
            WalkingJurelEntity j = MortisContent.WALKING_JUREL.create(level);
            if (j == null) continue;
            double a = i * Math.PI * 2 / 7;
            BlockPos spot = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(base.getX() + 0.5 + Math.cos(a) * (R + 4), 0, base.getZ() + 0.5 + Math.sin(a) * (R + 4)));
            j.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, (float) Math.toDegrees(a), 0);
            j.setHome(base);
            j.setCycleStart(firstDance);
            j.setPersistenceRequired();
            j.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.STRUCTURE, null);
            level.addFreshEntity(j);
        }
    }

    private static boolean inside(int dx, int dz) {
        return dx * dx + dz * dz <= (R + 0.5) * (R + 0.5);
    }

    private static long key(int dx, int dz) {
        return ((long) dx << 32) ^ (dz & 0xffffffffL);
    }

    /** Angle of a column round the tin, 0 at the middle of the front, in (-pi, pi]. */
    private static double relAngle(int dx, int dz, double frontAngle) {
        double ang = Math.atan2(dz, dx) - frontAngle;
        while (ang > Math.PI) ang -= Math.PI * 2;
        while (ang <= -Math.PI) ang += Math.PI * 2;
        return ang;
    }

    private static int textWidth(String s) {
        int w = 0;
        for (char c : s.toCharArray()) w += glyph(c)[0].length() + 1;
        return w - 1;
    }

    private static String[] glyph(char c) {
        String k = String.valueOf(c);
        for (int i = 0; i < FONT_KEYS.length; i++) if (FONT_KEYS[i].equals(k)) return FONT[i];
        return FONT[FONT.length - 1];
    }

    /** The colour of the tin's wall at height y and position {@code along} the front (0 = centre). */
    private static BlockState paint(int y, int along, int labelWidth, String label) {
        // ribs at the seams
        if (y == 0 || y == BLUE || y == BLUE + RED || y == HEIGHT - 1) return Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        if (y < BLUE) {
            // "JUREL" sea: blue with a wavy white crest along the top
            if (y == BLUE - 1 && Math.floorMod(along, 4) < 2) return Blocks.WHITE_CONCRETE.defaultBlockState();
            return Blocks.BLUE_CONCRETE.defaultBlockState();
        }
        if (y < BLUE + RED) {
            int row = (BLUE + RED - 1) - y;                     // 0 at the top of the red band
            // the jurel, centred a little left of the middle, rows 3..11 of the band
            int fx = along + 16, fy = row - 3;
            if (fy >= 0 && fy < FISH.length && fx >= 0 && fx < FISH[0].length()) {
                char c = FISH[fy].charAt(fx);
                switch (c) {
                    case '#': return Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
                    case 'L': return Blocks.CYAN_CONCRETE.defaultBlockState();
                    case 'B': return Blocks.BLUE_CONCRETE.defaultBlockState();
                    case 'S': return Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
                    case 'W': return Blocks.WHITE_CONCRETE.defaultBlockState();
                    case 'E': return Blocks.BLACK_CONCRETE.defaultBlockState();
                    default: break;
                }
            }
            // a white heart to the right of the fish
            int hx = along - 13, hy = row - 3;
            String[] heart = {".##.##.", "#######", "#######", ".#####.", "..###..", "...#..."};
            if (hy >= 0 && hy < heart.length && hx >= 0 && hx < 7 && heart[hy].charAt(hx) == '#') return Blocks.WHITE_CONCRETE.defaultBlockState();
            return Blocks.RED_CONCRETE.defaultBlockState();
        }
        // yellow band with the name, letters in dark red, rows 1..5 of the band
        int row = (HEIGHT - 2) - y;
        int col = along + labelWidth / 2;
        if (row >= 0 && row < 5 && col >= 0 && col < labelWidth) {
            int x = 0;
            for (char c : label.toCharArray()) {
                String[] gl = glyph(c);
                int gw = gl[0].length();
                if (col >= x && col < x + gw) {
                    if (gl[row].charAt(col - x) == '#') return Blocks.RED_TERRACOTTA.defaultBlockState();
                    break;
                }
                x += gw + 1;
            }
        }
        return Blocks.YELLOW_CONCRETE.defaultBlockState();
    }
    // ---- no one can break a can: not by hand, not with explosions (/sanmortis remove frees the nearest one)

    /** The cans standing in a level, by the middle of their foot. */
    public static final class Cans extends net.minecraft.world.level.saveddata.SavedData {
        final java.util.Set<Long> bases = new java.util.HashSet<>();
        /** Cans set free with /sanmortis remove: their jurels must not make them unbreakable again. */
        final java.util.Set<Long> freed = new java.util.HashSet<>();

        static Cans of(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(new Factory<>(Cans::new, Cans::load, null), "awakening_san_mortis_cans");
        }

        static Cans load(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider r) {
            Cans c = new Cans();
            for (long l : tag.getLongArray("Bases")) c.bases.add(l);
            for (long l : tag.getLongArray("Freed")) c.freed.add(l);
            return c;
        }

        @Override
        public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider r) {
            tag.putLongArray("Bases", bases.stream().mapToLong(Long::longValue).toArray());
            tag.putLongArray("Freed", freed.stream().mapToLong(Long::longValue).toArray());
            return tag;
        }

        void add(BlockPos base) {
            boolean changed = bases.add(base.asLong()) | freed.remove(base.asLong());
            if (changed) setDirty();
        }

        /**
         * A can told about by one of its jurels (their home is the middle of its foot): cans built before the
         * list existed were never on it, so a pickaxe could break them.
         */
        public static void adopt(ServerLevel level, BlockPos base) {
            Cans c = of(level);
            if (!c.freed.contains(base.asLong()) && c.bases.add(base.asLong())) c.setDirty();
        }

        /** Whether a block is part of a can (its wall, inside, lid, railing, ladder or foundation). */
        boolean protects(BlockPos p) {
            for (long l : bases) {
                BlockPos b = BlockPos.of(l);
                int dx = p.getX() - b.getX(), dz = p.getZ() - b.getZ(), dy = p.getY() - b.getY();
                if (dy >= -6 && dy <= HEIGHT + 1 && dx * dx + dz * dz <= (R + 1.6) * (R + 1.6)) return true;
            }
            return false;
        }
    }

    // ---- cans by the sea: one now and then on a beach of the Overworld (any terrain mod: by biome tags)

    /** Each cell of this many blocks has at most one can, and only half of the cells have one. */
    private static final int CELL = 1024;

    /** Where a can is waiting to be built (its land not all loaded yet), and the cells already decided. */
    public static final class Coast extends net.minecraft.world.level.saveddata.SavedData {
        final java.util.Set<Long> cells = new java.util.HashSet<>();
        final java.util.Map<Long, Integer> pending = new java.util.HashMap<>();   // place -> front (2D value)

        static Coast of(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(new Factory<>(Coast::new, Coast::load, null), "awakening_san_mortis_coast");
        }

        static Coast load(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider r) {
            Coast c = new Coast();
            for (long l : tag.getLongArray("Cells")) c.cells.add(l);
            long[] p = tag.getLongArray("Pending");
            int[] f = tag.getIntArray("Fronts");
            for (int i = 0; i < p.length && i < f.length; i++) c.pending.put(p[i], f[i]);
            return c;
        }

        @Override
        public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider r) {
            tag.putLongArray("Cells", cells.stream().mapToLong(Long::longValue).toArray());
            tag.putLongArray("Pending", pending.keySet().stream().mapToLong(Long::longValue).toArray());
            tag.putIntArray("Fronts", pending.values().stream().mapToInt(Integer::intValue).toArray());
            return tag;
        }
    }

    private static boolean isSea(ServerLevel level, BlockPos p) {
        var b = level.getBiome(p);
        return b.is(net.minecraft.tags.BiomeTags.IS_OCEAN) || b.is(net.neoforged.neoforge.common.Tags.Biomes.IS_OCEAN);
    }

    private static boolean isBeach(ServerLevel level, BlockPos p) {
        var b = level.getBiome(p);
        return b.is(net.minecraft.tags.BiomeTags.IS_BEACH) || b.is(net.neoforged.neoforge.common.Tags.Biomes.IS_BEACH);
    }

    /** A newly made chunk of the Overworld on the coast may be chosen for its cell's can. */
    @SubscribeEvent
    public static void onChunk(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        var cp = event.getChunk().getPos();
        int cellX = Math.floorDiv(cp.getMinBlockX(), CELL), cellZ = Math.floorDiv(cp.getMinBlockZ(), CELL);
        long cell = net.minecraft.world.level.ChunkPos.asLong(cellX, cellZ);
        Coast coast = Coast.of(level);
        if (coast.cells.contains(cell)) return;
        // half the cells have no can at all (decided by the seed, so the same in every run)
        if (new java.util.Random(level.getSeed() ^ cell * 0x9E3779B97F4A7C15L).nextBoolean()) {
            coast.cells.add(cell);
            coast.setDirty();
            return;
        }
        int y = event.getChunk().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 8, 8);
        BlockPos mid = new BlockPos(cp.getMinBlockX() + 8, y, cp.getMinBlockZ() + 8);
        if (y <= level.getSeaLevel() || isSea(level, mid) || !event.getChunk().getFluidState(mid.below()).isEmpty()) return;
        // on the coast: a beach, or land with the sea a few blocks off; the label faces inland
        Direction toSea = null;
        for (Direction d : Direction.Plane.HORIZONTAL)
            if (isSea(level, mid.relative(d, 24))) toSea = d;
        if (toSea == null && !isBeach(level, mid)) return;
        if (toSea == null) toSea = Direction.from2DDataValue(new java.util.Random(cell).nextInt(4));
        coast.cells.add(cell);
        coast.pending.put(mid.asLong(), toSea.getOpposite().get2DDataValue());
        coast.setDirty();
    }

    /** Builds the waiting cans whose land round them has loaded. */
    @SubscribeEvent
    public static void onTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 40 != 0) return;
        ServerLevel level = event.getServer().overworld();
        Coast coast = Coast.of(level);
        if (coast.pending.isEmpty()) return;
        for (var e : new java.util.ArrayList<>(coast.pending.entrySet())) {
            BlockPos at = BlockPos.of(e.getKey());
            boolean loaded = true;
            for (int dx = -1; dx <= 1 && loaded; dx++)
                for (int dz = -1; dz <= 1 && loaded; dz++)
                    loaded = level.hasChunk((at.getX() >> 4) + dx, (at.getZ() >> 4) + dz);
            if (!loaded) continue;
            coast.pending.remove(e.getKey());
            coast.setDirty();
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at);
            build(level, ground, Direction.from2DDataValue(e.getValue()));
            AwakeningSystemMod.LOGGER.info("San Mortis can raised on the coast at {}", ground);
        }
    }

    @SubscribeEvent
    public static void onBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && Cans.of(level).protects(event.getPos())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Cans cans = Cans.of(level);
        if (!cans.bases.isEmpty()) event.getAffectedBlocks().removeIf(cans::protects);
    }
}
