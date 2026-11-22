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
 * The giant stone ring: the gate to Aethergard, standing in Teyvat. About 60 blocks tall, pale stone overgrown
 * with moss, vines hanging from its underside. It comes in two states: ruined (broken arcs, fallen pieces,
 * thick moss) and repaired (whole, with a band of glowing runes round its inner edge, on a stepped plinth).
 * Later the repaired ring will open onto Aethergard; for now it is the structure, placed with
 * {@code /giantportal ruined} or {@code /giantportal repaired}.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class GiantPortalRing {
    /** Outer radius, thickness of the band, depth of the ring, and how far its foot is sunk into the ground. */
    public static final int R = 30, T = 5, DEPTH = 6, SUNK = 3;

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
        BlockState stone = Blocks.CALCITE.defaultBlockState();
        // the band
        for (int u = -R; u <= R; u++)
            for (int v = -R; v <= R; v++) {
                double d = Math.sqrt(u * u + v * v);
                if (d > R + 0.5 || d < R - T + 0.5) continue;
                double deg = Math.toDegrees(Math.atan2(v, u));
                if (deg < 0) deg += 360;
                double edge = Math.abs(Math.sin(u * 0.37 + v * 0.61)) ;
                if (broken(deg, edge)) continue;
                for (int w = -DEPTH / 2; w < DEPTH - DEPTH / 2; w++) {
                    // the ruin is chipped at its faces and edges
                    if (!repaired && (Math.abs(w) == DEPTH / 2 || d > R - 0.6) && rand.nextInt(100) < 9) continue;
                    BlockPos p = at(u, v, w);
                    ring.add(p.asLong());
                    set(p, pick(u, v, w, d));
                }
            }
        // ground under the foot, so it never floats
        for (int u = -6; u <= 6; u++)
            for (int w = -DEPTH; w <= DEPTH; w++) {
                BlockPos top = at(u, -(R - SUNK) - 1, w);
                for (int y = 0; y < 8; y++) {
                    BlockPos p = top.below(y);
                    if (!level.getBlockState(p).isSolid()) set(p, Blocks.STONE.defaultBlockState());
                }
            }
        if (repaired) plinthAndRunes();
        else debris();
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

    /** Moss on everything that faces up, patches on the sides, and vines hanging from what faces down. */
    private void overgrow() {
        int mossChance = repaired ? 35 : 80;
        int vineChance = repaired ? 12 : 45;
        for (long key : ring) {
            BlockPos p = BlockPos.of(key);
            boolean openAbove = !ring.contains(p.above().asLong()) && level.getBlockState(p.above()).isAir();
            boolean openBelow = !ring.contains(p.below().asLong()) && level.getBlockState(p.below()).isAir();
            if (openAbove && rand.nextInt(100) < mossChance) {
                set(p, Blocks.MOSS_BLOCK.defaultBlockState());
                if (rand.nextInt(100) < 30) set(p.above(), Blocks.MOSS_CARPET.defaultBlockState());
            } else if (!repaired && rand.nextInt(100) < 7) {
                set(p, Blocks.MOSS_BLOCK.defaultBlockState());
            }
            if (openBelow && p.getY() > base.getY() + 4 && rand.nextInt(100) < vineChance) {
                int len = 2 + rand.nextInt(repaired ? 3 : 7);
                for (int i = 1; i <= len; i++) {
                    BlockPos q = p.below(i);
                    if (!level.getBlockState(q).isAir()) break;
                    set(q, Blocks.VINE.defaultBlockState().setValue(VineBlock.UP, true));
                }
            }
        }
    }

    /** The ruin's broken pieces, lying on the ground in front of and behind it. */
    private void debris() {
        for (int i = 0; i < 9; i++) {
            int u = rand.nextInt(R * 2 + 1) - R;
            int w = (rand.nextBoolean() ? 1 : -1) * (DEPTH + 2 + rand.nextInt(10));
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, at(u, 0, w));
            int sx = 2 + rand.nextInt(4), sy = 1 + rand.nextInt(3), sz = 2 + rand.nextInt(3);
            for (int x = 0; x < sx; x++)
                for (int y = 0; y < sy; y++)
                    for (int z = 0; z < sz; z++) {
                        if (rand.nextInt(100) < 15) continue;
                        BlockPos p = ground.offset(x, y - 1, z);
                        set(p, y == sy - 1 && rand.nextBoolean() ? Blocks.MOSS_BLOCK.defaultBlockState() : pick(0, 0, 0, 0));
                    }
        }
    }

    /** The repaired ring: a stepped plinth at its foot and a band of glowing runes round its inner edge. */
    private void plinthAndRunes() {
        // steps up to the opening, on both sides
        for (int s = 0; s < 4; s++) {
            int half = 9 - s;
            for (int u = -half; u <= half; u++)
                for (int w = -(DEPTH / 2 + 4 - s); w <= DEPTH / 2 + 4 - s; w++) {
                    BlockPos p = at(u, -(R - SUNK) + s, w);
                    if (!ring.contains(p.asLong())) set(p, s == 3 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState());
                }
        }
        // runes: every few degrees along the inner edge, on both faces
        for (int a = 0; a < 360; a += 6) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(Math.cos(rad) * (R - T + 1)), v = (int) Math.round(Math.sin(rad) * (R - T + 1));
            for (int w : new int[]{-DEPTH / 2, DEPTH - DEPTH / 2 - 1}) {
                BlockPos p = at(u, v, w);
                if (ring.contains(p.asLong())) set(p, (a / 6) % 3 == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            }
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
                .then(Commands.literal("repaired").executes(ctx -> place(ctx.getSource(), true))));
    }

    private static int place(CommandSourceStack src, boolean repaired) {
        ServerLevel level = src.getLevel();
        Direction facing = src.getEntity() != null ? src.getEntity().getDirection() : Direction.NORTH;
        // in front of the player, far enough to see it whole, its foot on the ground
        BlockPos at = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, BlockPos.containing(src.getPosition()).relative(facing, R + 20));
        build(level, at, facing, repaired);
        src.sendSuccess(() -> Component.translatable(repaired ? "awakening_system.giant_portal.built_repaired" : "awakening_system.giant_portal.built_ruined",
                at.getX(), at.getY(), at.getZ()), true);
        return 1;
    }
}
