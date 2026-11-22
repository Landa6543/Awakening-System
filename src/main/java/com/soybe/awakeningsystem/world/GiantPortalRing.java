package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * The giant stone ring: the gate to Aethelgard, standing in Teyvat. About 60 blocks across and half sunk in the
 * ground, pale stone overgrown with moss and vines, with old pillars rising from the ground on both sides. It
 * comes in two states: ruined (broken arcs, thick moss, pillars broken off) and repaired (whole, with a band of
 * glowing runes round its inner edge and crowned pillars). Also placed with {@code /giantportal ruined|repaired}.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class GiantPortalRing {
    /** Outer radius, thickness of the band, depth of the ring, and how far its foot is sunk into the ground. */
    public static final int R = 30, T = 5, DEPTH = 6, SUNK = 10;

    private final ServerLevel level;
    private final BlockPos base;
    private final Direction facing;
    private final boolean repaired;
    private final Random rand;
    private final Set<Long> ring = new HashSet<>();

    private GiantPortalRing(ServerLevel level, BlockPos base, Direction facing, boolean repaired) {
        this.level = level;
        this.base = base;
        this.facing = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        this.repaired = repaired;
        this.rand = new Random(base.asLong() * 31 + (repaired ? 7 : 3));
    }

    /** Builds the ring standing on {@code base} (its foot), its opening facing {@code facing}. */
    public static void build(ServerLevel level, BlockPos base, Direction facing, boolean repaired) {
        new GiantPortalRing(level, base, facing, repaired).build();
    }

    /** The centre of the ring's opening. */
    public static BlockPos centre(BlockPos base) {
        return base.above(R - SUNK);
    }

    /**
     * The ground at a column: the first block from the sky that stops movement, leaves ignored. (The "_WG"
     * heightmaps exist only while a chunk is being made; on a finished chunk they read as the bottom of the world.)
     */
    public static BlockPos ground(ServerLevel level, BlockPos column) {
        level.getChunk(column.getX() >> 4, column.getZ() >> 4);
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
    }

    /** The ground under the whole ring: its lowest point across the opening, so no corner hangs in the air. */
    public static BlockPos siteGround(ServerLevel level, BlockPos column, Direction facing) {
        Direction right = facing.getClockWise();
        int y = Integer.MAX_VALUE;
        for (int u = -R + T; u <= R - T; u += 6)
            for (int w = -DEPTH; w <= DEPTH; w += DEPTH)
                y = Math.min(y, ground(level, column.offset(right.getStepX() * u + facing.getStepX() * w, 0,
                        right.getStepZ() * u + facing.getStepZ() * w)).getY());
        return new BlockPos(column.getX(), y, column.getZ());
    }

    /** Clears trees and hills from the ring's place and the ground in front of and behind it. */
    public static void clearSite(ServerLevel level, BlockPos base, Direction facing) {
        Direction right = facing.getClockWise();
        for (int u = -R - 2; u <= R + 2; u++)
            for (int w = -DEPTH - 6; w <= DEPTH + 6; w++)
                for (int y = 0; y <= R * 2; y++) {
                    BlockPos q = base.offset(right.getStepX() * u + facing.getStepX() * w, y, right.getStepZ() * u + facing.getStepZ() * w);
                    if (!level.getBlockState(q).isAir()) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                }
        removeCutTrees(level, base, facing);
    }

    /**
     * Trees the clearing cut through would be left as halves hanging in the air: every log and leaf joined to
     * the cut edge goes too, out to a few blocks past it.
     */
    private static void removeCutTrees(ServerLevel level, BlockPos base, Direction facing) {
        Direction right = facing.getClockWise();
        int uIn = R + 2, wIn = DEPTH + 6, margin = 12;
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        // the edge of the cleared box: the first column outside it, all round
        for (int y = -2; y <= R * 2; y++) {
            for (int u = -uIn - 1; u <= uIn + 1; u++)
                for (int w : new int[]{-wIn - 1, wIn + 1}) queue.add(base.offset(right.getStepX() * u + facing.getStepX() * w, y, right.getStepZ() * u + facing.getStepZ() * w));
            for (int w = -wIn - 1; w <= wIn + 1; w++)
                for (int u : new int[]{-uIn - 1, uIn + 1}) queue.add(base.offset(right.getStepX() * u + facing.getStepX() * w, y, right.getStepZ() * u + facing.getStepZ() * w));
        }
        int removed = 0;
        while (!queue.isEmpty() && removed < 6000) {
            BlockPos p = queue.poll();
            if (!seen.add(p.asLong())) continue;
            int dx = p.getX() - base.getX(), dz = p.getZ() - base.getZ();
            int u = dx * right.getStepX() + dz * right.getStepZ(), w = dx * facing.getStepX() + dz * facing.getStepZ();
            if (Math.abs(u) > uIn + margin || Math.abs(w) > wIn + margin || p.getY() < base.getY() - 3) continue;
            BlockState s = level.getBlockState(p);
            if (!s.is(net.minecraft.tags.BlockTags.LOGS) && !s.is(net.minecraft.tags.BlockTags.LEAVES)) continue;
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            removed++;
            for (Direction d : Direction.values()) queue.add(p.relative(d));
        }
    }

    // a point of the ring's own space: u along the face (right), v up, w through the opening
    private BlockPos at(int u, int v, int w) {
        Direction right = facing.getClockWise();
        return centre(base).offset(right.getStepX() * u + facing.getStepX() * w, v, right.getStepZ() * u + facing.getStepZ() * w);
    }

    /** Where the ruined ring is broken: angles in degrees (0 right, 90 top), with a ragged edge. */
    private boolean broken(double deg, double noise) {
        if (repaired) return false;
        double[][] gaps = {{38, 58}, {112, 131}, {196, 214}};
        for (double[] g : gaps) {
            if (deg > g[0] - noise * 6 && deg < g[1] + noise * 6) return true;
        }
        return false;
    }

    private void build() {
        // the band, its lower part buried in the ground
        for (int u = -R; u <= R; u++)
            for (int v = -R; v <= R; v++) {
                double d = Math.sqrt(u * u + v * v);
                if (d > R + 0.5 || d < R - T + 0.5) continue;
                double deg = Math.toDegrees(Math.atan2(v, u));
                if (deg < 0) deg += 360;
                double edge = Math.abs(Math.sin(u * 0.37 + v * 0.61));
                if (broken(deg, edge)) continue;
                for (int w = -DEPTH / 2; w < DEPTH - DEPTH / 2; w++) {
                    // the ruin is chipped at its faces and edges
                    if (!repaired && (Math.abs(w) == DEPTH / 2 || d > R - 0.6) && rand.nextInt(100) < 9) continue;
                    BlockPos p = at(u, v, w);
                    ring.add(p.asLong());
                    set(p, pick(u, v, w, d));
                }
            }
        pillars();
        if (repaired) runes();
        overgrow();
    }

    /** The band's stone: calcite with streaks of diorite and smooth stone, a little cracked in the ruin. */
    private BlockState pick(int u, int v, int w, double d) {
        int r = rand.nextInt(100);
        if (!repaired && r < 6) return Blocks.TUFF.defaultBlockState();
        if (r < 12) return Blocks.POLISHED_DIORITE.defaultBlockState();
        if (r < 18) return Blocks.SMOOTH_STONE.defaultBlockState();
        return Blocks.CALCITE.defaultBlockState();
    }

    /**
     * Old pillars rising out of the ground on both sides of the ring, two in front and two behind: whole and
     * crowned in the repaired ring, broken off at different heights in the ruin. Their feet go down into the
     * ground, so they look sunk with the ring, not set on top of it.
     */
    private void pillars() {
        int[][] spots = {{-(R + 5), DEPTH / 2 + 4}, {R + 5, DEPTH / 2 + 4}, {-(R + 5), -(DEPTH / 2 + 4)}, {R + 5, -(DEPTH / 2 + 4)}};
        for (int[] s : spots) {
            BlockPos foot = ground(level, at(s[0], 0, s[1]));
            int height = repaired ? 16 : 4 + rand.nextInt(9);
            for (int y = -6; y < height; y++)
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        boolean corner = x != 0 && z != 0;
                        // the ruin's tops are ragged: corners give out first
                        if (!repaired && y >= height - 2 && (corner || rand.nextInt(100) < 40)) continue;
                        BlockState st = corner ? Blocks.POLISHED_DIORITE.defaultBlockState()
                                : (y % 5 == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
                        if (!repaired && y >= 0 && rand.nextInt(100) < 14) st = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                        BlockPos p = foot.offset(x, y, z);
                        ring.add(p.asLong());
                        set(p, st);
                    }
            if (repaired) {
                BlockPos top = foot.above(height);
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) set(top.offset(x, 0, z), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
                set(top.above(), Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
    }

    /** The repaired ring: a band of glowing runes round its inner edge, on both faces. */
    private void runes() {
        for (int a = 0; a < 360; a += 6) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(Math.cos(rad) * (R - T + 1)), v = (int) Math.round(Math.sin(rad) * (R - T + 1));
            for (int w : new int[]{-DEPTH / 2, DEPTH - DEPTH / 2 - 1}) {
                BlockPos p = at(u, v, w);
                if (ring.contains(p.asLong()) && p.getY() >= base.getY())
                    set(p, (a / 6) % 3 == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            }
        }
    }

    /**
     * Moss on everything that faces up, patches on the sides, and vines down the faces. A vine always hangs
     * against the side of a block (or of the vine above it), as vines grow, never lying in the air.
     */
    private void overgrow() {
        int mossChance = repaired ? 35 : 80;
        int vineChance = repaired ? 4 : 14;
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (long key : ring) {
            BlockPos p = BlockPos.of(key);
            if (p.getY() < base.getY()) continue;
            boolean openAbove = !ring.contains(p.above().asLong()) && level.getBlockState(p.above()).isAir();
            if (openAbove && rand.nextInt(100) < mossChance) {
                set(p, Blocks.MOSS_BLOCK.defaultBlockState());
                if (rand.nextInt(100) < 30) set(p.above(), Blocks.MOSS_CARPET.defaultBlockState());
            } else if (!repaired && rand.nextInt(100) < 7) {
                set(p, Blocks.MOSS_BLOCK.defaultBlockState());
            }
            if (p.getY() < base.getY() + 3 || rand.nextInt(100) >= vineChance) continue;
            Direction side = sides[rand.nextInt(4)];
            BlockPos q = p.relative(side);
            if (ring.contains(q.asLong()) || !level.getBlockState(q).isAir()) continue;
            // the vine's face is the one towards the block it hangs on
            BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(side.getOpposite()), true);
            int len = 1 + rand.nextInt(repaired ? 3 : 6);
            for (int i = 0; i < len; i++) {
                BlockPos r = q.below(i);
                if (!level.getBlockState(r).isAir() || r.getY() <= base.getY()) break;
                set(r, vine);
            }
        }
    }

    /**
     * What an older ring left at its site (its stone, moss, vines, runes, plinth and fallen pieces), cleared
     * before the ring is raised again. Only the ring's own kinds of block, so the land round it is left alone.
     */
    public static void removeOld(ServerLevel level, BlockPos base, Direction facing) {
        Direction right = facing.getClockWise();
        java.util.Set<net.minecraft.world.level.block.Block> kinds = java.util.Set.of(Blocks.CALCITE, Blocks.POLISHED_DIORITE,
                Blocks.SMOOTH_STONE, Blocks.TUFF, Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, Blocks.VINE, Blocks.POLISHED_DEEPSLATE,
                Blocks.SEA_LANTERN, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.CHISELED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
        for (int u = -R - 8; u <= R + 8; u++)
            for (int w = -DEPTH - 24; w <= DEPTH + 24; w++)
                for (int y = -12; y <= R * 2 + 4; y++) {
                    BlockPos q = base.offset(right.getStepX() * u + facing.getStepX() * w, y, right.getStepZ() * u + facing.getStepZ() * w);
                    if (kinds.contains(level.getBlockState(q).getBlock()))
                        level.setBlock(q, y < 0 ? Blocks.DIRT.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                }
    }

    private void set(BlockPos p, BlockState s) {
        level.setBlock(p, s, 2);
    }

    // ---- commands

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("giantportal").requires(s -> s.hasPermission(2))
                .then(Commands.literal("ruined").executes(ctx -> place(ctx.getSource(), false)))
                .then(Commands.literal("repaired").executes(ctx -> place(ctx.getSource(), true)))
                .then(Commands.literal("fix").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> GiantRingSite.forceRepair(ctx.getSource().getServer()), true);
                    return 1;
                }))
                .then(Commands.literal("locate").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> GiantRingSite.describe(ctx.getSource().getServer()), false);
                    return 1;
                })));
    }

    private static int place(CommandSourceStack src, boolean repaired) {
        ServerLevel level = src.getLevel();
        Direction facing = src.getEntity() != null ? src.getEntity().getDirection() : Direction.NORTH;
        // in front of the player, far enough to see it whole, its foot on the ground
        BlockPos at = ground(level, BlockPos.containing(src.getPosition()).relative(facing, R + 20));
        build(level, at, facing, repaired);
        src.sendSuccess(() -> Component.translatable(repaired ? "awakening_system.giant_portal.built_repaired" : "awakening_system.giant_portal.built_ruined",
                at.getX(), at.getY(), at.getZ()), true);
        return 1;
    }
}
