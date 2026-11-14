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

import static net.minecraft.core.Direction.*;

/**
 * A medieval village built by the mod, every house furnished inside: a square with a well and market stalls,
 * two crossing streets, the tavern, the smithy, the chapel with its bell tower at the end of the high street,
 * the library, houses of one and two storeys, the barn with its paddock, fields and two watchtowers.
 * It stands on floor 11 of the Great Tower (the "Serene Village") and can be raised anywhere with
 * {@code /medieval_village}.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class MedievalVillage {
    private MedievalVillage() {}

    private enum Kind { SMALL, TALL, TAVERN, SMITHY, CHAPEL, LIBRARY, BARN, TOWER, WELL, STALL }

    /** A building: kind, its world footprint corner relative to the centre, which way its door faces, style, job. */
    private record Site(Kind kind, int x, int z, Direction front, VillageBuildings.Style style, String job, Block work, Block wool, Block wool2) {
        Site(Kind kind, int x, int z, Direction front) { this(kind, x, z, front, VillageBuildings.SPRUCE, "", null, null, null); }
        Site(Kind kind, int x, int z, Direction front, VillageBuildings.Style style, String job, Block work) { this(kind, x, z, front, style, job, work, null, null); }
    }

    private static int localW(Kind k) {
        return switch (k) {
            case SMALL, TALL -> 9;
            case TAVERN, BARN -> 13;
            case SMITHY, CHAPEL, LIBRARY -> 11;
            case TOWER, WELL -> 5;
            case STALL -> 4;
        };
    }

    private static int localD(Kind k) {
        return switch (k) {
            case SMALL -> 7;
            case TALL, SMITHY, LIBRARY -> 9;
            case TAVERN, BARN -> 11;
            case CHAPEL -> 17;
            case TOWER, WELL -> 5;
            case STALL -> 3;
        };
    }

    /** Local x of the door, so a path can lead from it to the street. */
    private static int doorX(Kind k) {
        return switch (k) {
            case SMALL, TALL -> 4;
            case TAVERN, BARN -> 6;
            case SMITHY -> 3;
            case CHAPEL, LIBRARY -> 5;
            case TOWER -> 2;
            default -> -1;
        };
    }

    private static final List<Site> SITES = List.of(
            new Site(Kind.WELL, -2, -2, NORTH),
            new Site(Kind.STALL, 4, -7, SOUTH, VillageBuildings.SPRUCE, "butcher", Blocks.SMOKER, Blocks.RED_WOOL, Blocks.WHITE_WOOL),
            new Site(Kind.STALL, -7, -7, SOUTH, VillageBuildings.SPRUCE, "fisherman", Blocks.BARREL, Blocks.BLUE_WOOL, Blocks.WHITE_WOOL),
            new Site(Kind.STALL, 4, 5, NORTH, VillageBuildings.SPRUCE, "shepherd", Blocks.LOOM, Blocks.YELLOW_WOOL, Blocks.WHITE_WOOL),
            new Site(Kind.STALL, -7, 5, NORTH, VillageBuildings.SPRUCE, "farmer", Blocks.COMPOSTER, Blocks.GREEN_WOOL, Blocks.WHITE_WOOL),
            // the high street, north
            new Site(Kind.TAVERN, 4, -26, WEST),
            new Site(Kind.SMALL, -11, -22, EAST, VillageBuildings.SPRUCE, "", null),
            new Site(Kind.TALL, -13, -36, EAST, VillageBuildings.DARK, "", null),
            new Site(Kind.SMALL, 4, -38, WEST, VillageBuildings.BRICK, "", null),
            new Site(Kind.CHAPEL, -5, -69, SOUTH),
            // east street
            new Site(Kind.SMITHY, 16, -13, SOUTH),
            new Site(Kind.LIBRARY, 30, -13, SOUTH),
            new Site(Kind.TALL, 16, 5, NORTH, VillageBuildings.BRICK, "", null),
            new Site(Kind.SMALL, 29, 5, NORTH, VillageBuildings.SLATE, "", null),
            new Site(Kind.TOWER, 46, -8, WEST),
            // west street
            new Site(Kind.SMALL, -22, -11, SOUTH, VillageBuildings.DARK, "", null),
            new Site(Kind.TALL, -36, -13, SOUTH, VillageBuildings.SPRUCE, "", null),
            new Site(Kind.BARN, -30, 5, NORTH),
            new Site(Kind.TOWER, -51, 3, EAST),
            // south street: the farmer's house by the fields
            new Site(Kind.SMALL, 4, 14, WEST, VillageBuildings.SPRUCE, "farmer", Blocks.COMPOSTER),
            new Site(Kind.TALL, -13, 44, EAST, VillageBuildings.DARK, "", null)
    );

    private static final int ROAD_N = -52, ROAD_S = 47, ROAD_W = -46, ROAD_E = 45;

    // ── building ─────────────────────────────────────────────────────────────

    /** Builds the whole village around (cx, cz). Returns how many villagers moved in. */
    public static int build(ServerLevel level, int cx, int cz) {
        VillageCanvas c = new VillageCanvas(level);
        clearTrees(c, cx, cz);
        List<int[]> footprints = new ArrayList<>();
        List<Runnable> settle = new ArrayList<>();
        for (Site s : SITES) {
            int lw = localW(s.kind()), ld = localD(s.kind());
            int ww = Plot.worldW(lw, ld, s.front()), wd = Plot.worldD(lw, ld, s.front());
            int x0 = cx + s.x(), z0 = cz + s.z();
            int floor = medianGround(c, x0, z0, ww, wd);
            Plot p = new Plot(c, x0, z0, floor, lw, ld, s.front());
            footprints.add(new int[]{x0 - 1, z0 - 1, x0 + ww, z0 + wd});
            List<VillageBuildings.Resident> people = switch (s.kind()) {
                case SMALL -> VillageBuildings.smallHouse(p, s.style(), s.job(), s.work());
                case TALL -> VillageBuildings.tallHouse(p, s.style(), s.job(), s.work());
                case TAVERN -> VillageBuildings.tavern(p);
                case SMITHY -> VillageBuildings.smithy(p);
                case CHAPEL -> VillageBuildings.chapel(p);
                case LIBRARY -> VillageBuildings.library(p);
                case BARN -> VillageBuildings.barn(p);
                case TOWER -> VillageBuildings.tower(p);
                case WELL -> { VillageBuildings.well(p); yield List.of(); }
                case STALL -> VillageBuildings.stall(p, s.wool(), s.wool2(), s.work(), s.job());
            };
            for (VillageBuildings.Resident r : people) {
                BlockPos at = p.pos(r.pos().getX(), r.pos().getY(), r.pos().getZ());
                settle.add(() -> spawn(level, at, r.job()));
            }
            int dx = doorX(s.kind());
            if (dx >= 0) {
                int dz = s.kind() == Kind.CHAPEL ? -2 : -1;
                BlockPos step = p.pos(dx, 0, dz);
                Direction out = p.dir(NORTH);
                pathToStreet(c, step.relative(out), out, cx, cz);
            }
        }
        square(c, cx, cz, footprints);
        streets(c, cx, cz, footprints);
        fields(c, cx, cz);
        paddock(c, cx, cz, settle);
        lamps(c, cx, cz, footprints);
        c.fillLoot();
        settle.forEach(Runnable::run);
        return settle.size();
    }

    /** Trees inside the village are felled (trunks and leaves), so streets and yards stay open; the woods around stay. */
    private static void clearTrees(VillageCanvas c, int cx, int cz) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = ROAD_W - 6; x <= ROAD_E + 6; x++)
            for (int z = ROAD_N - 20; z <= ROAD_S + 6; z++) {
                int g = c.ground(cx + x, cz + z);
                for (int y = g + 1; y <= g + 24; y++) {
                    m.set(cx + x, y, cz + z);
                    BlockState s = c.get(m);
                    if (s.is(net.minecraft.tags.BlockTags.LOGS) || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(Blocks.VINE))
                        c.set(m, Blocks.AIR.defaultBlockState());
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

    private static boolean inside(List<int[]> fps, int x, int z) {
        for (int[] f : fps) if (x >= f[0] && x <= f[2] && z >= f[1] && z <= f[3]) return true;
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

    private static void streets(VillageCanvas c, int cx, int cz, List<int[]> fps) {
        for (int z = ROAD_N; z <= ROAD_S; z++)
            for (int x = -1; x <= 1; x++) if (!inside(fps, cx + x, cz + z) || Math.abs(z) < 10) {
                if (x * x + z * z <= 81) continue;
                road(c, cx + x, cz + z, roadBlock(cx + x, cz + z));
            }
        for (int x = ROAD_W; x <= ROAD_E; x++)
            for (int z = -1; z <= 1; z++) if (!inside(fps, cx + x, cz + z)) {
                if (x * x + z * z <= 81) continue;
                road(c, cx + x, cz + z, roadBlock(cx + x, cz + z));
            }
    }

    /** The square: stone paving in a circle round the well. */
    private static void square(VillageCanvas c, int cx, int cz, List<int[]> fps) {
        for (int x = -9; x <= 9; x++)
            for (int z = -9; z <= 9; z++) {
                if (x * x + z * z > 85 || Math.abs(x) <= 3 && Math.abs(z) <= 3) continue;
                int h = (x * 31 + z * 17 + x * z) & 7;
                Block b = h < 3 ? Blocks.STONE_BRICKS : h < 5 ? Blocks.COBBLESTONE : h < 6 ? Blocks.ANDESITE : h < 7 ? Blocks.MOSSY_COBBLESTONE : Blocks.POLISHED_ANDESITE;
                BlockPos at = new BlockPos(cx + x, c.ground(cx + x, cz + z), cz + z);
                if (VillageCanvas.clearable(c.get(at)) || c.get(at).is(Blocks.GRASS_BLOCK)) road(c, at.getX(), at.getZ(), b.defaultBlockState());
            }
    }

    /** From a door step straight out until a street (at most 14 blocks). */
    private static void pathToStreet(VillageCanvas c, BlockPos from, Direction out, int cx, int cz) {
        BlockPos p = from;
        for (int i = 0; i < 14; i++) {
            int rx = p.getX() - cx, rz = p.getZ() - cz;
            if ((Math.abs(rx) <= 1 && rz >= ROAD_N && rz <= ROAD_S) || (Math.abs(rz) <= 1 && rx >= ROAD_W && rx <= ROAD_E)) return;
            road(c, p.getX(), p.getZ(), Blocks.DIRT_PATH.defaultBlockState());
            p = p.relative(out);
        }
    }

    /** Lamp posts along both streets, every nine blocks, alternating sides. */
    private static void lamps(VillageCanvas c, int cx, int cz, List<int[]> fps) {
        int n = 0;
        for (int z = ROAD_N + 4; z <= ROAD_S - 2; z += 9) if (Math.abs(z) > 10) lamp(c, cx + ((n++ % 2 == 0) ? 2 : -2), cz + z, fps);
        for (int x = ROAD_W + 4; x <= ROAD_E - 2; x += 9) if (Math.abs(x) > 10) lamp(c, cx + x, cz + ((n++ % 2 == 0) ? 2 : -2), fps);
        for (int[] o : new int[][]{{7, 0}, {-7, 0}, {0, 7}, {0, -7}}) lamp(c, cx + o[0] + (o[0] == 0 ? 2 : 0), cz + o[1] + (o[1] == 0 ? 2 : 0), fps);
    }

    private static void lamp(VillageCanvas c, int x, int z, List<int[]> fps) {
        if (inside(fps, x, z)) return;
        int g = c.ground(x, z);
        BlockPos at = new BlockPos(x, g + 1, z);
        if (!c.get(at).isAir() && !VillageCanvas.clearable(c.get(at))) return;
        c.set(at, Blocks.COBBLESTONE_WALL.defaultBlockState());
        c.set(at.above(), Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.set(at.above(2), Blocks.DARK_OAK_FENCE.defaultBlockState());
        c.set(at.above(3), Blocks.LANTERN.defaultBlockState());
    }

    /** Fenced fields either side of the south street, with water channels and a scarecrow. */
    private static void fields(VillageCanvas c, int cx, int cz) {
        field(c, cx - 16, cz + 14, 13, 26, EAST, cx, cz);
        field(c, cx + 5, cz + 26, 16, 17, WEST, cx, cz);
    }

    private static void field(VillageCanvas c, int x0, int z0, int w, int d, Direction gateSide, int cx, int cz) {
        CropBlock[] crops = {(CropBlock) Blocks.WHEAT, (CropBlock) Blocks.CARROTS, (CropBlock) Blocks.POTATOES, (CropBlock) Blocks.BEETROOTS};
        for (int x = 0; x < w; x++)
            for (int z = 0; z < d; z++) {
                int wx = x0 + x, wz = z0 + z, g = c.ground(wx, wz);
                BlockPos at = new BlockPos(wx, g, wz);
                for (int y = 1; y <= 6; y++) if (VillageCanvas.clearable(c.get(at.above(y)))) c.set(at.above(y), Blocks.AIR.defaultBlockState());
                boolean edge = x == 0 || z == 0 || x == w - 1 || z == d - 1;
                if (edge) {
                    boolean gate = (gateSide == EAST ? x == w - 1 : x == 0) && z == d / 2;
                    boolean ns = x == 0 || x == w - 1;
                    BlockState f = gate ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, gateSide)
                            : VillageBuildings.fence(Blocks.SPRUCE_FENCE, ns && z > 0, !ns && x < w - 1, ns && z < d - 1, !ns && x > 0);
                    c.set(at, Blocks.GRASS_BLOCK.defaultBlockState());
                    c.set(at.above(), f);
                    continue;
                }
                boolean water = (x % 5 == 3) && z > 1 && z < d - 2;
                if (water && c.ground(wx + 1, wz) >= g && c.ground(wx - 1, wz) >= g && c.ground(wx, wz + 1) >= g && c.ground(wx, wz - 1) >= g) {
                    c.set(at, Blocks.WATER.defaultBlockState());
                    continue;
                }
                CropBlock crop = crops[(x / 5) % crops.length];
                c.set(at, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
                c.set(at.above(), crop.getStateForAge(crop.getMaxAge()));
            }
        // a scarecrow in the middle
        int sx = x0 + w / 2, sz = z0 + d / 2, g = c.ground(sx, sz);
        BlockPos s = new BlockPos(sx, g, sz);
        c.set(s, Blocks.GRASS_BLOCK.defaultBlockState());
        c.set(s.above(), Blocks.SPRUCE_FENCE.defaultBlockState());
        c.set(s.above(2), Blocks.HAY_BLOCK.defaultBlockState());
        c.set(s.above(3), Plot.facing(Blocks.CARVED_PUMPKIN, gateSide));
    }

    /** The fenced paddock behind the barn, with a few animals. */
    private static void paddock(VillageCanvas c, int cx, int cz, List<Runnable> settle) {
        int x0 = cx - 30, z0 = cz + 17, w = 13, d = 10;
        for (int x = 0; x < w; x++)
            for (int z = 0; z < d; z++) {
                int wx = x0 + x, wz = z0 + z, g = c.ground(wx, wz);
                BlockPos at = new BlockPos(wx, g, wz);
                for (int y = 1; y <= 6; y++) if (VillageCanvas.clearable(c.get(at.above(y)))) c.set(at.above(y), Blocks.AIR.defaultBlockState());
                if (x == 0 || z == d - 1 || x == w - 1 || z == 0) {
                    if (z == 0 && x == 6) continue;          // the barn's back gate opens here
                    boolean ns = x == 0 || x == w - 1;
                    c.set(at.above(), VillageBuildings.fence(Blocks.OAK_FENCE, ns && z > 0, !ns && x < w - 1, ns && z < d - 1, !ns && x > 0));
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
        BlockPos horse = new BlockPos(cx - 24, 0, cz + 9);
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
                Villager v = EntityType.VILLAGER.create(level);
                if (v == null) return;
                v.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
                VillagerProfession prof = job.isEmpty() ? VillagerProfession.NONE
                        : BuiltInRegistries.VILLAGER_PROFESSION.getOptional(ResourceLocation.withDefaultNamespace(job)).orElse(VillagerProfession.NONE);
                v.setVillagerData(v.getVillagerData().setType(VillagerType.PLAINS).setProfession(prof).setLevel(prof == VillagerProfession.NONE ? 1 : 2));
                if (prof != VillagerProfession.NONE) v.setVillagerXp(10);
                v.setPersistenceRequired();
                level.addFreshEntity(v);
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

    /** Raises the village in the middle of floor 11 the first time anyone arrives there. */
    public static void ensureOnFloor(ServerLevel level) {
        Built b = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Built::new, Built::load, null), "awakening_medieval_village");
        if (b.built) return;
        b.built = true;
        b.setDirty();
        int people = build(level, 0, 0);
        AwakeningSystemMod.LOGGER.info("Medieval village raised on floor 11 with {} inhabitants", people);
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
        int people = build(level, at.getX(), at.getZ());
        src.sendSuccess(() -> Component.literal("Medieval village built at " + at.getX() + ", " + at.getZ() + " (" + people + " inhabitants)"), true);
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

    /** Every non-air block of the village area, "x y z block", relative to the centre (for pictures outside the game). */
    private static void dump(ServerLevel level, int cx, int cz, Path out) throws IOException {
        VillageCanvas c = new VillageCanvas(level);
        int minY = Integer.MAX_VALUE;
        for (int x = -60; x <= 60; x += 6) for (int z = -75; z <= 55; z += 6) minY = Math.min(minY, c.ground(cx + x, cz + z));
        StringBuilder sb = new StringBuilder();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = -60; x <= 60; x++)
            for (int z = -75; z <= 55; z++)
                for (int y = minY - 4; y <= minY + 45; y++) {
                    m.set(cx + x, y, cz + z);
                    BlockState s = level.getBlockState(m);
                    if (s.isAir()) continue;
                    sb.append(x).append(' ').append(y - minY).append(' ').append(z).append(' ').append(BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath());
                    if (s.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) sb.append(' ').append(s.getValue(BlockStateProperties.HORIZONTAL_FACING).getName());
                    sb.append('\n');
                }
        Files.writeString(out, sb.toString());
    }
}
