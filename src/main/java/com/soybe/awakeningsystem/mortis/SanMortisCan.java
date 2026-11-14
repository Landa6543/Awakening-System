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
            centre = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, centre);
            build(level, centre, facing.getOpposite());
            src.sendSuccess(() -> Component.translatable("awakening_system.sanmortis.built"), true);
            return 1;
        }).then(Commands.literal("view").executes(ctx -> {
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

        // the jurels that live on the roof
        BlockPos roof = base.above(HEIGHT + 1);
        for (int i = 0; i < 7; i++) {
            WalkingJurelEntity j = MortisContent.WALKING_JUREL.create(level);
            if (j == null) continue;
            double a = i * Math.PI * 2 / 7;
            j.moveTo(roof.getX() + 0.5 + Math.cos(a) * 3, roof.getY(), roof.getZ() + 0.5 + Math.sin(a) * 3, (float) Math.toDegrees(a), 0);
            j.setHome(roof);
            j.setPersistenceRequired();
            j.finalizeSpawn(level, level.getCurrentDifficultyAt(roof), MobSpawnType.STRUCTURE, null);
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
}
