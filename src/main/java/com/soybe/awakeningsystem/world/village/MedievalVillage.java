package com.soybe.awakeningsystem.world.village;

import com.mojang.brigadier.CommandDispatcher;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static net.minecraft.core.Direction.*;

/**
 * A medieval town built by the mod, every house furnished inside. A grid of streets (a high street and a
 * cross street through a square with a well and market stalls, and a ring road around the centre) lined on
 * both sides with houses of four shapes and ten palettes, so neighbours differ in wood, walls, base and roof.
 * Fixed landmarks: the chapel at the north end of the high street and the manor at its south end, the tavern,
 * smithy, library and bakery round the square, the barn with its paddock and the fields outside, and
 * watchtowers at the ends of the cross street.
 * It stands on floor 11 of the Great Tower (the "Serene Village") and can be raised anywhere with
 * {@code /medieval_village}.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class MedievalVillage {
    private MedievalVillage() {}

    private enum Kind { SMALL, TALL, LONG, LHOUSE, TAVERN, SMITHY, CHAPEL, LIBRARY, BARN, TOWER, WELL, STALL, BAKERY, MANOR }

    /** A building: kind, footprint corner relative to the centre, which way its door faces, style and options. */
    private record Site(Kind kind, int x, int z, Direction front, VillageBuildings.Style style, String job, Block work,
                        Block wool, Block wool2, boolean across, boolean gable, int extras) {
        Site(Kind kind, int x, int z, Direction front) { this(kind, x, z, front, VillageBuildings.SPRUCE, "", null, null, null, false, false, 0); }
        Site(Kind kind, int x, int z, Direction front, VillageBuildings.Style style, String job, Block work, Block wool, Block wool2, boolean across, boolean gable) {
            this(kind, x, z, front, style, job, work, wool, wool2, across, gable, 0);
        }
    }

    private static int localW(Kind k) {
        return switch (k) {
            case SMALL, TALL, BAKERY, LHOUSE -> 9;
            case LONG, SMITHY, CHAPEL, LIBRARY -> 11;
            case TAVERN, BARN -> 13;
            case MANOR -> 15;
            case TOWER, WELL -> 5;
            case STALL -> 4;
        };
    }

    private static int localD(Kind k) {
        return switch (k) {
            case SMALL, LONG, BAKERY -> 7;
            case TALL, SMITHY, LIBRARY -> 9;
            case TAVERN, BARN, MANOR, LHOUSE -> 11;
            case CHAPEL -> 17;
            case TOWER, WELL -> 5;
            case STALL -> 3;
        };
    }

    /** Local x of the door, so a path can lead from it to the street. */
    private static int doorX(Kind k) {
        return switch (k) {
            case SMALL, TALL, BAKERY -> 4;
            case LHOUSE -> 5;
            case LONG -> 3;
            case TAVERN, BARN -> 6;
            case SMITHY -> 3;
            case CHAPEL, LIBRARY -> 5;
            case MANOR -> 7;
            case TOWER -> 2;
            default -> -1;
        };
    }

    /** A street: from (x0, z0) to (x1, z1), straight along x or z, three blocks wide. */
    private record Street(int x0, int z0, int x1, int z1) {
        boolean alongZ() { return x0 == x1; }
        boolean covers(int x, int z, int margin) {
            return x >= Math.min(x0, x1) - 1 - margin && x <= Math.max(x0, x1) + 1 + margin
                    && z >= Math.min(z0, z1) - 1 - margin && z <= Math.max(z0, z1) + 1 + margin;
        }
    }

    private static final List<Street> STREETS = List.of(
            new Street(0, -101, 0, 96),      // the high street, chapel to manor
            new Street(-96, 0, 95, 0),       // the cross street, tower to tower
            new Street(-50, -45, 50, -45),   // the ring road
            new Street(-50, 45, 50, 45),
            new Street(-50, -45, -50, 45),
            new Street(50, -45, 50, 45));

    private static final int PLAZA = 10;

    /** The landmarks: placed first, the houses fill in around them. */
    private static final List<Site> LANDMARKS = List.of(
            new Site(Kind.WELL, -2, -2, NORTH),
            new Site(Kind.STALL, 4, -7, SOUTH, VillageBuildings.SPRUCE, "butcher", Blocks.SMOKER, Blocks.RED_WOOL, Blocks.WHITE_WOOL, false, false),
            new Site(Kind.STALL, -7, -7, SOUTH, VillageBuildings.SPRUCE, "fisherman", Blocks.BARREL, Blocks.BLUE_WOOL, Blocks.WHITE_WOOL, false, false),
            new Site(Kind.STALL, 4, 5, NORTH, VillageBuildings.SPRUCE, "shepherd", Blocks.LOOM, Blocks.YELLOW_WOOL, Blocks.WHITE_WOOL, false, false),
            new Site(Kind.STALL, -7, 5, NORTH, VillageBuildings.SPRUCE, "farmer", Blocks.COMPOSTER, Blocks.GREEN_WOOL, Blocks.WHITE_WOOL, false, false),
            new Site(Kind.TAVERN, 4, -26, WEST),
            new Site(Kind.SMITHY, 16, -13, SOUTH),
            new Site(Kind.LIBRARY, 30, -13, SOUTH),
            new Site(Kind.BAKERY, -20, -11, SOUTH),
            new Site(Kind.CHAPEL, -5, -119, SOUTH),
            new Site(Kind.MANOR, -7, 98, NORTH),
            new Site(Kind.BARN, -86, 5, NORTH),
            new Site(Kind.TOWER, 97, -8, WEST),
            new Site(Kind.TOWER, -102, 3, EAST),
            new Site(Kind.TOWER, 3, -96, WEST),
            new Site(Kind.TOWER, -8, 90, EAST));

    /** Fields: x, z, width, depth, and the side their gate opens to. */
    private static final int[][] FIELDS = {{58, 55, 16, 22}, {77, 55, 14, 22}, {-74, 55, 16, 22}, {-92, 55, 15, 22},
            {58, -82, 16, 22}, {-74, -82, 16, 22}};

    private static final int PADDOCK_X = -86, PADDOCK_Z = 17;

    // ── building ─────────────────────────────────────────────────────────────

    /** Builds the whole town around (cx, cz). Returns how many people and animals moved in. */
    public static int build(ServerLevel level, int cx, int cz) {
        drain(level, cx, cz);
        VillageCanvas c = new VillageCanvas(level);
        clearTrees(c, cx, cz);
        Random rng = new Random(cx * 341873128712L + cz * 132897987541L + 11);
        List<int[]> taken = new ArrayList<>();       // footprints with room for the roof overhang, relative
        for (int[] f : FIELDS) taken.add(new int[]{f[0] - 2, f[1] - 2, f[0] + f[2] + 1, f[1] + f[3] + 1});
        taken.add(new int[]{PADDOCK_X - 2, PADDOCK_Z - 2, PADDOCK_X + 14, PADDOCK_Z + 11});
        taken.add(new int[]{-PLAZA - 2, -PLAZA - 2, PLAZA + 2, PLAZA + 2});
        List<Runnable> settle = new ArrayList<>();
        for (Site s : LANDMARKS) place(c, cx, cz, s, taken, settle);
        for (Site s : houses(rng, taken)) place(c, cx, cz, s, taken, settle);
        square(c, cx, cz);
        streets(c, cx, cz, taken);
        for (int[] f : FIELDS) field(c, cx + f[0], cz + f[1], f[2], f[3], f[0] > 0 ? WEST : EAST);
        paddock(c, cx + PADDOCK_X, cz + PADDOCK_Z, settle);
        lamps(c, cx, cz, taken);
        c.joinFences();
        c.fillLoot();
        // every townsperson is a different person: the landmarks' trades choose first, then the houses
        townsfolk = new java.util.HashSet<>();
        settle.forEach(Runnable::run);
        townsfolk = null;
        return settle.size();
    }

    private static void place(VillageCanvas c, int cx, int cz, Site s, List<int[]> taken, List<Runnable> settle) {
        int lw = localW(s.kind()), ld = localD(s.kind());
        int ww = Plot.worldW(lw, ld, s.front()), wd = Plot.worldD(lw, ld, s.front());
        int x0 = cx + s.x(), z0 = cz + s.z();
        // the well and the stalls stand on the square's own level, not on mounds of their own
        int floor = s.kind() == Kind.WELL || s.kind() == Kind.STALL ? plazaLevel(c, cx, cz) : medianGround(c, x0, z0, ww, wd);
        Plot p = new Plot(c, x0, z0, floor, lw, ld, s.front());
        if (s.kind() != Kind.WELL && s.kind() != Kind.STALL) taken.add(new int[]{s.x() - 2, s.z() - 2, s.x() + ww + 1, s.z() + wd + 1});
        List<VillageBuildings.Resident> people = switch (s.kind()) {
            case SMALL -> VillageBuildings.smallHouse(p, s.style(), s.job(), s.work(), s.across(), s.gable(), s.extras());
            case TALL -> VillageBuildings.tallHouse(p, s.style(), s.job(), s.work(), s.across(), s.gable(), s.extras());
            case LONG -> VillageBuildings.longHouse(p, s.style(), s.job(), s.across(), s.extras());
            case LHOUSE -> VillageBuildings.lHouse(p, s.style(), s.job(), s.extras());
            case TAVERN -> VillageBuildings.tavern(p);
            case SMITHY -> VillageBuildings.smithy(p);
            case CHAPEL -> VillageBuildings.chapel(p);
            case LIBRARY -> VillageBuildings.library(p);
            case BARN -> VillageBuildings.barn(p);
            case TOWER -> VillageBuildings.tower(p);
            case BAKERY -> VillageBuildings.bakery(p);
            case MANOR -> VillageBuildings.manor(p);
            case WELL -> { VillageBuildings.well(p); yield List.of(); }
            case STALL -> VillageBuildings.stall(p, s.wool(), s.wool2(), s.work(), s.job());
        };
        for (VillageBuildings.Resident r : people) {
            BlockPos at = p.pos(r.pos().getX(), r.pos().getY(), r.pos().getZ());
            settle.add(() -> spawn(c.level, at, r.job()));
        }
        int dx = doorX(s.kind());
        if (dx >= 0) {
            int dz = s.kind() == Kind.CHAPEL ? -2 : -1;
            Direction out = p.dir(NORTH);
            pathToStreet(c, p.pos(dx, 0, dz).relative(out), out, cx, cz);
        }
    }

    private static final String[] JOBS = {"", "", "", "farmer", "fisherman", "shepherd", "fletcher", "mason", "leatherworker", "cartographer", "butcher"};

    /** Houses along both sides of every street, wherever there is room: shape, palette and roof picked at random. */
    private static List<Site> houses(Random rng, List<int[]> taken) {
        List<Site> out = new ArrayList<>();
        for (Street st : STREETS)
            for (int side = -1; side <= 1; side += 2) {
                int from = st.alongZ() ? Math.min(st.z0(), st.z1()) : Math.min(st.x0(), st.x1());
                int to = st.alongZ() ? Math.max(st.z0(), st.z1()) : Math.max(st.x0(), st.x1());
                int at = from + 3;
                while (at < to - 6) {
                    int r = rng.nextInt(100);
                    Kind kind = r < 28 ? Kind.SMALL : r < 54 ? Kind.TALL : r < 74 ? Kind.LONG : Kind.LHOUSE;
                    int lw = localW(kind), ld = localD(kind);
                    int yard = 3 + rng.nextInt(2);
                    Direction front;
                    int x, z, ww, wd;
                    if (st.alongZ()) {
                        front = side > 0 ? WEST : EAST;
                        ww = ld; wd = lw;
                        x = side > 0 ? st.x0() + 2 + yard : st.x0() - 2 - yard - ww + 1;
                        z = at;
                    } else {
                        front = side > 0 ? NORTH : SOUTH;
                        ww = lw; wd = ld;
                        z = side > 0 ? st.z0() + 2 + yard : st.z0() - 2 - yard - wd + 1;
                        x = at;
                    }
                    int[] rect = {x - 2, z - 2, x + ww + 1, z + wd + 1};
                    boolean ok = rng.nextInt(100) < 80 && free(rect, taken);
                    if (ok) {
                        VillageBuildings.Style style = VillageBuildings.HOUSE_STYLES[rng.nextInt(VillageBuildings.HOUSE_STYLES.length)];
                        boolean across = rng.nextBoolean();
                        boolean gable = across && kind != Kind.LONG && rng.nextInt(100) < 55;
                        String job = JOBS[rng.nextInt(JOBS.length)];
                        int extras = 0;
                        if ((kind == Kind.SMALL || kind == Kind.LONG) && !gable && rng.nextInt(100) < 45) extras |= VillageBuildings.PORCH;
                        if (rng.nextInt(100) < 55) extras |= VillageBuildings.CHIMNEY;
                        if (kind == Kind.TALL && rng.nextInt(100) < 35) extras |= VillageBuildings.TOWER;
                        out.add(new Site(kind, x, z, front, style, job, null, null, null, across, gable, extras));
                        taken.add(rect);
                        at += (st.alongZ() ? wd : ww) + 2 + rng.nextInt(4);
                    } else at += 2;
                }
            }
        return out;
    }

    /** True when the rectangle touches no other building, field, square or street. */
    private static boolean free(int[] r, List<int[]> taken) {
        for (int[] t : taken) if (r[0] <= t[2] && r[2] >= t[0] && r[1] <= t[3] && r[3] >= t[1]) return false;
        for (Street s : STREETS)
            for (int x = r[0] + 1; x <= r[2] - 1; x++)
                for (int z = r[1] + 1; z <= r[3] - 1; z++) if (s.covers(x, z, 0)) return false;
        return true;
    }

    /** Ponds and puddles inside the town are filled with earth, so no street or house ends up under water. */
    private static void drain(ServerLevel level, int cx, int cz) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int start = Math.min(level.getMaxBuildHeight() - 1, 200);
        for (int x = -108; x <= 103; x++)
            for (int z = -123; z <= 110; z++) {
                int y = start;
                m.set(cx + x, y, cz + z);
                while (y > level.getMinBuildHeight() && level.getBlockState(m).isAir()) m.setY(--y);
                if (level.getFluidState(m).isEmpty()) continue;
                boolean top = true;
                while (y > level.getMinBuildHeight() && !level.getFluidState(m).isEmpty()) {
                    level.setBlock(m, (top ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    top = false;
                    m.setY(--y);
                }
            }
    }

    /** Trees inside the town are felled (trunks and leaves), so streets and yards stay open; the woods around stay. */
    private static void clearTrees(VillageCanvas c, int cx, int cz) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = -110; x <= 105; x++)
            for (int z = -125; z <= 112; z++) {
                int g = c.ground(cx + x, cz + z);
                for (int y = g + 1; y <= g + 24; y++) {
                    m.set(cx + x, y, cz + z);
                    BlockState s = c.get(m);
                    if (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || s.is(Blocks.VINE)) c.set(m, Blocks.AIR.defaultBlockState());
                }
            }
    }

    private static int medianGround(VillageCanvas c, int x0, int z0, int w, int d) {
        int[] hs = new int[w * d];
        int i = 0;
        for (int x = 0; x < w; x++) for (int z = 0; z < d; z++) hs[i++] = c.ground(x0 + x, z0 + z);
        Arrays.sort(hs);
        return hs[hs.length / 2];
    }

    private static boolean onStreet(int rx, int rz) {
        for (Street s : STREETS) if (s.covers(rx, rz, 0)) return true;
        return false;
    }

    private static BlockState roadBlock(int x, int z) {
        int h = (x * 734287 ^ z * 912931) & 15;
        return (h < 9 ? Blocks.DIRT_PATH : h < 11 ? Blocks.COARSE_DIRT : h < 13 ? Blocks.GRAVEL : Blocks.COBBLESTONE).defaultBlockState();
    }

    /** Lays a road block on the natural ground at (x, z) and clears plants and branches above it. */
    private static void road(VillageCanvas c, int x, int z, BlockState s) {
        int g = c.ground(x, z);
        BlockPos at = new BlockPos(x, g, z);
        if (!VillageCanvas.clearable(c.get(at)) && !c.get(at).is(Blocks.GRASS_BLOCK)) return;
        c.set(at, s);
        for (int y = 1; y <= 5; y++) {
            BlockPos up = at.above(y);
            if (VillageCanvas.clearable(c.get(up))) c.set(up, Blocks.AIR.defaultBlockState());
        }
    }

    /**
     * Lays a road block at a chosen height: earth filled in under it where the ground dips, the hillside cut
     * away over it where the ground rises, so the street runs smooth instead of in steps between dirt walls.
     */
    private static void roadAt(VillageCanvas c, int x, int z, int y, BlockState s) {
        int g = c.ground(x, z);
        BlockPos at = new BlockPos(x, y, z);
        BlockState here = c.get(at);
        if (!here.isAir() && !VillageCanvas.clearable(here) && !here.is(Blocks.GRASS_BLOCK)) return;   // a building's step or wall
        for (int k = g + 1; k < y; k++) {
            BlockPos q = new BlockPos(x, k, z);
            // stone under a raised street or square: its sides read as a retaining wall, not a heap of earth
            if (c.get(q).isAir() || VillageCanvas.clearable(c.get(q)))
                c.set(q, (((x * 31 + k * 7 + z) & 3) == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE).defaultBlockState());
        }
        c.set(at, s);
        for (int k = 1; k <= Math.max(5, g - y + 2); k++) {
            BlockPos up = at.above(k);
            if (VillageCanvas.clearable(c.get(up))) c.set(up, Blocks.AIR.defaultBlockState());
        }
    }

    /** The side of a yard higher than the street at {@code y}: its earth face from y + 1 up becomes stone. */
    private static void retain(VillageCanvas c, int x, int z, int y) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, y + 1, z);
        int top = y;
        while (top < y + 6 && !c.get(m.setY(top + 1)).isAir() && c.get(m).blocksMotion()) top++;
        for (int k = y + 1; k < top; k++) {
            BlockState s = c.get(m.setY(k));
            if (s.is(BlockTags.DIRT) || s.is(BlockTags.BASE_STONE_OVERWORLD))
                c.set(m.immutable(), ((x * 31 + k * 7 + z) & 3) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
        }
    }

    private static void streets(VillageCanvas c, int cx, int cz, List<int[]> taken) {
        for (Street s : STREETS) {
            int from = s.alongZ() ? Math.min(s.z0(), s.z1()) : Math.min(s.x0(), s.x1());
            int to = s.alongZ() ? Math.max(s.z0(), s.z1()) : Math.max(s.x0(), s.x1());
            // the street's height at each step: the ground averaged over nine blocks along it, so it rises and falls gently
            int[] raw = new int[to - from + 1];
            for (int a = from; a <= to; a++) {
                int sum = 0;
                for (int w = -1; w <= 1; w++) sum += s.alongZ() ? c.ground(cx + s.x0() + w, cz + a) : c.ground(cx + a, cz + s.z0() + w);
                raw[a - from] = Math.round(sum / 3f);
            }
            for (int a = from; a <= to; a++) {
                int sum = 0, n = 0;
                for (int k = -4; k <= 4; k++) if (a + k >= from && a + k <= to) { sum += raw[a + k - from]; n++; }
                int y = Math.round(sum / (float) n);
                for (int w = -1; w <= 1; w++) {
                    int x = s.alongZ() ? s.x0() + w : a, z = s.alongZ() ? a : s.z0() + w;
                    if (x * x + z * z <= PLAZA * PLAZA) continue;
                    roadAt(c, cx + x, cz + z, y, roadBlock(cx + x, cz + z));
                }
                // where a yard stands higher than the street, its edge becomes a low stone retaining wall, not bare earth
                for (int w : new int[]{-2, 2}) {
                    int x = s.alongZ() ? s.x0() + w : a, z = s.alongZ() ? a : s.z0() + w;
                    if (onStreet(x, z) || x * x + z * z <= (PLAZA + 1) * (PLAZA + 1)) continue;
                    retain(c, cx + x, cz + z, y);
                }
            }
        }
    }

    /** The square: stone paving in a circle round the well. */
    private static int plazaLevel(VillageCanvas c, int cx, int cz) {
        int y = c.ground(cx + PLAZA, cz) + c.ground(cx - PLAZA, cz) + c.ground(cx, cz + PLAZA) + c.ground(cx, cz - PLAZA);
        return Math.round(y / 4f);
    }

    private static void square(VillageCanvas c, int cx, int cz) {
        int level = plazaLevel(c, cx, cz);
        for (int x = -PLAZA; x <= PLAZA; x++)
            for (int z = -PLAZA; z <= PLAZA; z++) {
                if (x * x + z * z > PLAZA * PLAZA + 4 || Math.abs(x) <= 3 && Math.abs(z) <= 3) continue;
                int h = (x * 31 + z * 17 + x * z) & 7;
                Block b = h < 3 ? Blocks.STONE_BRICKS : h < 5 ? Blocks.COBBLESTONE : h < 6 ? Blocks.ANDESITE : h < 7 ? Blocks.MOSSY_COBBLESTONE : Blocks.POLISHED_ANDESITE;
                roadAt(c, cx + x, cz + z, level, b.defaultBlockState());
            }
    }

    /** From a door step straight out until a street (at most 16 blocks). */
    private static void pathToStreet(VillageCanvas c, BlockPos from, Direction out, int cx, int cz) {
        BlockPos p = from;
        for (int i = 0; i < 16; i++) {
            if (onStreet(p.getX() - cx, p.getZ() - cz)) return;
            road(c, p.getX(), p.getZ(), Blocks.DIRT_PATH.defaultBlockState());
            p = p.relative(out);
        }
    }

    /** Lamp posts along every street, every ten blocks, alternating sides. */
    private static void lamps(VillageCanvas c, int cx, int cz, List<int[]> taken) {
        int n = 0;
        for (Street s : STREETS) {
            int from = s.alongZ() ? Math.min(s.z0(), s.z1()) : Math.min(s.x0(), s.x1());
            int to = s.alongZ() ? Math.max(s.z0(), s.z1()) : Math.max(s.x0(), s.x1());
            for (int a = from + 5; a <= to - 3; a += 10) {
                int off = (n++ % 2 == 0) ? 2 : -2;
                int x = s.alongZ() ? s.x0() + off : a, z = s.alongZ() ? a : s.z0() + off;
                if (x * x + z * z <= (PLAZA + 1) * (PLAZA + 1)) continue;
                lamp(c, cx + x, cz + z, cx, cz);
            }
        }
        for (int[] o : new int[][]{{8, 2}, {-8, -2}, {2, 8}, {-2, -8}}) lamp(c, cx + o[0], cz + o[1], cx, cz);
    }

    private static void lamp(VillageCanvas c, int x, int z, int cx, int cz) {
        if (onStreet(x - cx, z - cz)) return;
        int g = c.ground(x, z);
        BlockPos at = new BlockPos(x, g + 1, z);
        if (!c.get(at).isAir() && !VillageCanvas.clearable(c.get(at))) return;
        if (!c.get(at.below()).isSolidRender(c.level, at.below()) && !c.get(at.below()).is(Blocks.DIRT_PATH)) return;
        c.set(at, Blocks.COBBLESTONE_WALL.defaultBlockState());
        c.set(at.above(), Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.set(at.above(2), Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.set(at.above(3), Blocks.LANTERN.defaultBlockState());
    }

    /** A fenced field with water channels, four crops in bands and a scarecrow. */
    private static void field(VillageCanvas c, int x0, int z0, int w, int d, Direction gateSide) {
        CropBlock[] crops = {(CropBlock) Blocks.WHEAT, (CropBlock) Blocks.CARROTS, (CropBlock) Blocks.POTATOES, (CropBlock) Blocks.BEETROOTS};
        // the whole field lies flat at one height: filled up where the ground dips, cut where it rises
        int fy = medianGround(c, x0, z0, w, d);
        for (int x = -1; x <= w; x++)
            for (int z = -1; z <= d; z++) {
                int wx = x0 + x, wz = z0 + z, g = c.ground(wx, wz);
                boolean bank = x == -1 || z == -1 || x == w || z == d;
                for (int y = Math.min(g, fy) - 3; y < fy; y++) {
                    BlockPos q = new BlockPos(wx, y, wz);
                    if (y > g || c.get(q).getFluidState().isSource() || c.get(q).isAir()) c.set(q, Blocks.DIRT.defaultBlockState());
                }
                for (int y = fy + 1; y <= Math.max(g, fy) + 8; y++) {
                    BlockPos q = new BlockPos(wx, y, wz);
                    if (VillageCanvas.clearable(c.get(q)) || !c.get(q).getFluidState().isEmpty()) c.set(q, Blocks.AIR.defaultBlockState());
                }
                // a grass bank all round, so a lake beside the field cannot spill into it
                if (bank) c.set(new BlockPos(wx, fy, wz), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        for (int x = 0; x < w; x++)
            for (int z = 0; z < d; z++) {
                int wx = x0 + x, wz = z0 + z, g = fy;
                BlockPos at = new BlockPos(wx, g, wz);
                boolean edge = x == 0 || z == 0 || x == w - 1 || z == d - 1;
                if (edge) {
                    boolean gate = (gateSide == EAST ? x == w - 1 : x == 0) && z == d / 2;
                    c.set(at, Blocks.GRASS_BLOCK.defaultBlockState());
                    c.set(at.above(), gate ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, gateSide)
                            : Blocks.SPRUCE_FENCE.defaultBlockState());
                    continue;
                }
                boolean water = (x % 5 == 3) && z > 1 && z < d - 2;
                if (water) {
                    c.set(at, Blocks.WATER.defaultBlockState());
                    continue;
                }
                CropBlock crop = crops[(x / 5) % crops.length];
                c.set(at, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
                c.set(at.above(), crop.getStateForAge(crop.getMaxAge()));
            }
        int sx = x0 + w / 2, sz = z0 + d / 2, g = fy;
        BlockPos s = new BlockPos(sx, g, sz);
        c.set(s, Blocks.GRASS_BLOCK.defaultBlockState());
        c.set(s.above(), Blocks.SPRUCE_FENCE.defaultBlockState());
        c.set(s.above(2), Blocks.HAY_BLOCK.defaultBlockState());
        c.set(s.above(3), Plot.facing(Blocks.CARVED_PUMPKIN, gateSide));
    }

    /** The fenced paddock behind the barn, with a few animals. */
    private static void paddock(VillageCanvas c, int x0, int z0, List<Runnable> settle) {
        int w = 13, d = 10;
        for (int x = 0; x < w; x++)
            for (int z = 0; z < d; z++) {
                int wx = x0 + x, wz = z0 + z, g = c.ground(wx, wz);
                BlockPos at = new BlockPos(wx, g, wz);
                for (int y = 1; y <= 6; y++) if (VillageCanvas.clearable(c.get(at.above(y)))) c.set(at.above(y), Blocks.AIR.defaultBlockState());
                if (x == 0 || z == d - 1 || x == w - 1 || z == 0) {
                    if (z == 0 && x == 6) continue;          // the barn's back gate opens here
                    c.set(at.above(), Blocks.OAK_FENCE.defaultBlockState());
                }
            }
        c.set(new BlockPos(x0 + 3, c.ground(x0 + 3, z0 + 6) + 1, z0 + 6), Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(BlockStateProperties.LEVEL_CAULDRON, 3));
        c.set(new BlockPos(x0 + 9, c.ground(x0 + 9, z0 + 6) + 1, z0 + 6), Blocks.HAY_BLOCK.defaultBlockState());
        ServerLevel level = c.level;
        for (int i = 0; i < 5; i++) {
            int ax = x0 + 2 + (i * 2), az = z0 + 3 + (i % 3) * 2;
            BlockPos at = new BlockPos(ax, c.ground(ax, az) + 1, az);
            EntityType<? extends Mob> type = i < 3 ? EntityType.COW : EntityType.SHEEP;
            settle.add(() -> spawnAnimal(level, type, at));
        }
        BlockPos horse = new BlockPos(x0 + 6, 0, z0 - 8);
        settle.add(() -> spawnAnimal(level, EntityType.HORSE, horse.atY(c.ground(horse.getX(), horse.getZ()) + 1)));
    }

    // ── people ───────────────────────────────────────────────────────────────

    private static void spawnAnimal(ServerLevel level, EntityType<? extends Mob> type, BlockPos at) {
        Mob m = type.create(level);
        if (m == null) return;
        m.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
        m.setPersistenceRequired();
        level.addFreshEntity(m);
    }

    /** The skins already living in the town being built (each skin is one named person). */
    private static java.util.Set<Integer> townsfolk;

    private static void spawn(ServerLevel level, BlockPos at, String job) {
        switch (job) {
            case "golem" -> {
                var g = EntityType.IRON_GOLEM.create(level);
                if (g == null) return;
                g.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
                g.setPersistenceRequired();
                level.addFreshEntity(g);
            }
            case "cat" -> {
                var cat = EntityType.CAT.create(level);
                if (cat == null) return;
                cat.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
                cat.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.STRUCTURE, null);
                cat.setPersistenceRequired();
                level.addFreshEntity(cat);
            }
            default -> {
                // the town's own people, not vanilla villagers: workshop trades wear the smith's skin, the rest either
                int seed = at.getX() * 31 + at.getZ() * 17;
                int skin = townsfolk == null ? TownNpcEntity.skinFor(job, seed) : TownNpcEntity.uniqueSkin(job, seed, townsfolk);
                if (skin < 0) return;                          // everyone in town already lives here
                if (townsfolk != null) townsfolk.add(skin);
                // someone who got another trade's look takes that trade, so no role shows up twice
                String trade = TownNpcEntity.roleOfSkin(skin).equals(TownNpcEntity.roleOfJob(job)) ? job : "";
                TownNpcEntity npc = TownNpcContent.TOWN_NPC.create(level);
                if (npc == null) return;
                npc.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
                npc.settle(at, trade, skin);
                level.addFreshEntity(npc);
            }
        }
    }

    // ── floor 11 and the command ─────────────────────────────────────────────

    private static final class Built extends SavedData {
        boolean built;

        static Built load(CompoundTag tag, HolderLookup.Provider p) {
            Built b = new Built();
            b.built = tag.getBoolean("built");
            return b;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider p) {
            tag.putBoolean("built", built);
            return tag;
        }
    }

    /** Raises the town in the middle of floor 11 the first time anyone arrives there. */
    public static void ensureOnFloor(ServerLevel level) {
        Built b = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Built::new, Built::load, null), "awakening_medieval_town");
        if (b.built) return;
        b.built = true;
        b.setDirty();
        int people = build(level, 0, 0);
        AwakeningSystemMod.LOGGER.info("Medieval town raised on floor 11 with {} inhabitants", people);
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("medieval_village").requires(s -> s.hasPermission(2))
                .executes(ctx -> run(ctx.getSource(), false))
                .then(Commands.literal("dump").executes(ctx -> run(ctx.getSource(), true))));
    }

    private static int run(CommandSourceStack src, boolean dump) {
        ServerLevel level = src.getLevel();
        BlockPos at = BlockPos.containing(src.getPosition());
        long t0 = System.currentTimeMillis();
        int people = build(level, at.getX(), at.getZ());
        long ms = System.currentTimeMillis() - t0;
        src.sendSuccess(() -> Component.literal("Medieval village built at " + at.getX() + ", " + at.getZ() + " (" + people + " inhabitants, " + ms + " ms)"), true);
        if (dump) {
            Path out = level.getServer().getServerDirectory().resolve("village_dump.txt");
            try {
                dump(level, at.getX(), at.getZ(), out);
                src.sendSuccess(() -> Component.literal("VILLAGE_DUMP " + out.toAbsolutePath()), false);
            } catch (IOException e) {
                src.sendFailure(Component.literal("dump failed: " + e));
            }
        }
        return people;
    }

    /** Every non-air block of the town area, "x y z block", relative to the centre (for pictures outside the game). */
    private static void dump(ServerLevel level, int cx, int cz, Path out) throws IOException {
        VillageCanvas c = new VillageCanvas(level);
        int minY = Integer.MAX_VALUE;
        for (int x = -110; x <= 105; x += 8) for (int z = -125; z <= 112; z += 8) minY = Math.min(minY, c.ground(cx + x, cz + z));
        StringBuilder sb = new StringBuilder();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = -110; x <= 105; x++)
            for (int z = -125; z <= 112; z++)
                for (int y = minY - 2; y <= minY + 45; y++) {
                    m.set(cx + x, y, cz + z);
                    BlockState s = level.getBlockState(m);
                    if (s.isAir()) continue;
                    sb.append(x).append(' ').append(y - minY).append(' ').append(z).append(' ').append(BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath()).append('\n');
                }
        Files.writeString(out, sb.toString());
    }
}
