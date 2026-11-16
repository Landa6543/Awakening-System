package com.soybe.awakeningsystem.tower.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.hash;
import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.n;

/**
 * Floors 21 to 25 of the Great Tower:
 * <ol start="21">
 *   <li>Village of the Autumn Valley: gentle golden hills under orange crowns (the game's own oaks, coloured by the
 *       floor's autumn), the second town of the tower in the middle, its ground kept flat and clear.</li>
 *   <li>Dry Marshes of the Drowned Roots: mud flats and dried pools, rooted dirt and moss, the game's own mangroves.</li>
 *   <li>White Salt Flats of the Titans' Graveyard: a cracked white plain of salt, low sandstone mesas and the
 *       bones of giants lying in it (ribcages, skulls, spines).</li>
 *   <li>Copper Peaks of the Endless Storm: ragged ridges of tuff and deepslate veined with weathered copper,
 *       basalt spires crowned with lightning rods, a few spruces in the valleys.</li>
 *   <li>Cavern of the Hundred Veins (mining): a closed cavern of deepslate and tuff, its walls thick with ores,
 *       amethyst geodes and glow berries for light. No monsters.</li>
 * </ol>
 */
final class TowerFloorTerrainAscent {
    private TowerFloorTerrainAscent() {}

    /** The town of floor 21 stands in this radius round the middle: kept flat and free of trees. */
    private static final int TOWN = 80;

    static int height(int floor, int x, int z) {
        return switch (floor) {
            case 21 -> {
                double d = Math.hypot(x, z);
                double hills = 6 * n(21, x, z, 0.007, 3) + 2 * n(21, x + 300, z, 0.03, 2);
                double flatten = TowerFloorTerrainBeyond.smooth((d - TOWN) / 60.0);   // 0 in the town, 1 beyond
                yield 70 + (int) Math.round(hills * flatten + 1.5 * n(21, x, z, 0.02, 1) * (1 - flatten) * 0.3);
            }
            case 22 -> 66 + (int) Math.round(3 * n(22, x, z, 0.01, 3) + 1.5 * n(22, x + 80, z, 0.05, 1));
            case 23 -> {
                double mesa = Math.max(0, n(23, x - 400, z, 0.006, 2) - 0.35) * 30;
                yield 68 + (int) Math.round(Math.min(10, mesa)) / 5 * 5 + (int) Math.round(0.8 * n(23, x, z, 0.04, 1));
            }
            case 24 -> {
                double ridge = 1.0 - Math.abs(n(24, x, z, 0.006, 3));
                yield 70 + (int) Math.round(42 * ridge * ridge * ridge + 4 * n(24, x + 90, z, 0.03, 2));
            }
            default -> caveGround(x, z);
        };
    }

    static boolean isCave(int floor) {
        return floor == 25;
    }

    // ── floor 25's cavern ────────────────────────────────────────────────────

    private static int caveGround(int x, int z) {
        return 14 + (int) Math.round(8 * n(25, x, z, 0.012, 3) + 3 * n(25, x - 70, z, 0.05, 1));
    }

    private static int caveRoof(int x, int z) {
        return 92 + (int) Math.round(12 * n(25, x + 777, z, 0.009, 3));
    }

    // ── filling ──────────────────────────────────────────────────────────────

    static void fill(int floor, ChunkAccess chunk) {
        TowerFloorTerrain.Writer w = new TowerFloorTerrain.Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        if (floor == 25) {
            fillCavern(chunk, w, x0, z0, p);
            return;
        }
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int h = height(floor, x, z);
                int slope = Math.abs(height(floor, x + 1, z) - height(floor, x - 1, z)) + Math.abs(height(floor, x, z + 1) - height(floor, x, z - 1));
                boolean steep = slope > 4;
                for (int y = -64; y <= h; y++) {
                    BlockState s = y == -64 ? Blocks.BEDROCK.defaultBlockState() : y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : ground(floor, x, y, z, h, steep);
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                if (!steep) plants(floor, w, x, h + 1, z);
            }
        switch (floor) {
            case 23 -> {
                scatter(23, w, x0, z0, 90, 30, 0.55, 10, TowerFloorTerrainAscent::titanBones);
                scatter(23, w, x0, z0, 26, 4, 0.35, 20, (wr, x, y, z, a, b) -> boneSpike(wr, x, y, z, a));
            }
            case 24 -> {
                scatter(24, w, x0, z0, 34, 5, 0.5, 10, (wr, x, y, z, a, b) -> stormSpire(wr, x, y, z, a));
                scatter(24, w, x0, z0, 20, 4, 0.45, 20, (wr, x, y, z, a, b) -> copperOutcrop(wr, x, y, z, a));
            }
            default -> {}
        }
    }

    private static boolean flat(int floor, int x, int z) {
        return Math.abs(height(floor, x + 2, z) - height(floor, x - 2, z)) + Math.abs(height(floor, x, z + 2) - height(floor, x, z - 2)) <= 3;
    }

    private static BlockState ground(int floor, int x, int y, int z, int h, boolean steep) {
        double v = n(floor, x, z, 0.06, 2);
        switch (floor) {
            case 21 -> {
                if (y == h && !steep) return v > 0.55 ? Blocks.PODZOL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
                if (y > h - 4) return Blocks.DIRT.defaultBlockState();
                return Blocks.STONE.defaultBlockState();
            }
            case 22 -> {
                if (y == h) return v > 0.35 ? Blocks.MUD.defaultBlockState() : v > -0.1 ? Blocks.PACKED_MUD.defaultBlockState()
                        : v > -0.45 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
                if (y > h - 3) return Blocks.MUD.defaultBlockState();
                if (y > h - 7) return Blocks.ROOTED_DIRT.defaultBlockState();
                return Blocks.STONE.defaultBlockState();
            }
            case 23 -> {
                if (y > h - 3) {
                    if (steep || h > 72) return y % 3 == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                    // the salt crust, cracked into plates
                    boolean crack = Math.floorMod(x + (int) (3 * n(23, x, z, 0.12, 1)), 7) == 0 || Math.floorMod(z + (int) (3 * n(23, z, x, 0.12, 1)), 7) == 0;
                    if (y == h) return crack ? Blocks.LIGHT_GRAY_CONCRETE_POWDER.defaultBlockState() : v > 0.4 ? Blocks.CALCITE.defaultBlockState() : Blocks.WHITE_CONCRETE_POWDER.defaultBlockState();
                    return Blocks.CALCITE.defaultBlockState();
                }
                return y > h - 12 ? Blocks.SANDSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
            default -> {
                if (y == h && !steep && h < 92) return v > 0.3 ? Blocks.GRAVEL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
                if (y == h && h >= 104) return Blocks.CALCITE.defaultBlockState();
                double vein = n(24, x + y * 3, z - y * 2, 0.09, 2);
                if (vein > 0.55) return Blocks.OXIDIZED_COPPER.defaultBlockState();
                if (vein > 0.5) return Blocks.WEATHERED_COPPER.defaultBlockState();
                return y % 7 < 3 ? Blocks.TUFF.defaultBlockState() : y < 80 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
        }
    }

    private static void plants(int floor, TowerFloorTerrain.Writer w, int x, int y, int z) {
        double r = hash(floor, x, z, 77);
        BlockState below = w.get(x, y - 1, z);
        switch (floor) {
            case 21 -> {
                if (!below.is(Blocks.GRASS_BLOCK)) return;
                if (r < 0.2) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                else if (r < 0.24) w.put(x, y, z, Blocks.FERN.defaultBlockState());
                else if (r < 0.255) w.put(x, y, z, Blocks.ORANGE_TULIP.defaultBlockState());
                else if (r < 0.265) w.put(x, y, z, Blocks.RED_TULIP.defaultBlockState());
                else if (r < 0.27 && Math.hypot(x, z) > TOWN) w.put(x, y, z, Blocks.PUMPKIN.defaultBlockState());
            }
            case 22 -> {
                if (r < 0.04 && below.is(Blocks.MOSS_BLOCK)) w.put(x, y, z, Blocks.MOSS_CARPET.defaultBlockState());
                else if (r < 0.06) w.put(x, y, z, Blocks.DEAD_BUSH.defaultBlockState());
                else if (r < 0.08 && below.is(Blocks.MOSS_BLOCK)) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
            }
            case 23 -> {
                if (r < 0.004) w.put(x, y, z, Blocks.DEAD_BUSH.defaultBlockState());
            }
            default -> {
                if (r < 0.03 && below.is(Blocks.COARSE_DIRT)) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
            }
        }
    }

    // ── features ─────────────────────────────────────────────────────────────

    private interface Feature { void place(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2); }

    private static void scatter(int floor, TowerFloorTerrain.Writer w, int x0, int z0, int cell, int margin, double chance, int salt, Feature f) {
        int c0 = Math.floorDiv(x0 - margin, cell), c1 = Math.floorDiv(x0 + 15 + margin, cell);
        int d0 = Math.floorDiv(z0 - margin, cell), d1 = Math.floorDiv(z0 + 15 + margin, cell);
        for (int a = c0; a <= c1; a++)
            for (int b = d0; b <= d1; b++) {
                if (hash(floor, a, b, salt) > chance) continue;
                int x = a * cell + (int) (hash(floor, a, b, salt + 1) * cell);
                int z = b * cell + (int) (hash(floor, a, b, salt + 2) * cell);
                f.place(w, x, height(floor, x, z) + 1, z, hash(floor, a, b, salt + 3), hash(floor, a, b, salt + 4));
            }
    }

    /** A dead titan: a long spine half sunk in the salt, great ribs arching over it and a skull at one end. */
    private static void titanBones(TowerFloorTerrain.Writer w, int x, int ground, int z, double r1, double r2) {
        boolean alongX = r2 < 0.5;
        int len = 34 + (int) (r1 * 26), rib = 11 + (int) (r1 * 6);
        BlockState spine = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, alongX ? Direction.Axis.X : Direction.Axis.Z);
        for (int k = 0; k < len; k++) {
            int sx = alongX ? x + k : x, sz = alongX ? z : z + k;
            int gy = height(23, sx, sz);
            w.force(sx, gy, sz, spine);
            w.force(sx, gy + 1, sz, spine);
            if (k % 5 != 2 || k > len - 8) continue;
            double size = rib * (1 - Math.abs(k - len * 0.35) / len);
            for (int side = -1; side <= 1; side += 2)
                for (double a = 0; a <= Math.PI * 0.92; a += 0.08) {
                    int off = (int) Math.round(Math.sin(a) * size) * side, up = (int) Math.round((1 - Math.cos(a)) * size * 0.85);
                    int rx = alongX ? sx : sx + off, rz = alongX ? sz + off : sz;
                    w.force(rx, gy + 1 + up, rz, Blocks.BONE_BLOCK.defaultBlockState());
                }
        }
        // the skull at the head end: a rounded mass of bone with two dark sockets
        int hx = alongX ? x - 5 : x, hz = alongX ? z : z - 5, hy = height(23, hx, hz) + 3;
        for (int dx = -5; dx <= 5; dx++) for (int dy = -3; dy <= 5; dy++) for (int dz = -5; dz <= 5; dz++) {
            double d = Math.sqrt(dx * dx * 0.8 + dy * dy * 1.3 + dz * dz * 0.8);
            if (d > 5.2) continue;
            boolean socket = dy == 2 && (alongX ? (dx == -5 || dx == -4) && Math.abs(dz) == 2 : (dz == -5 || dz == -4) && Math.abs(dx) == 2);
            w.force(hx + dx, hy + dy, hz + dz, socket ? Blocks.BLACKSTONE.defaultBlockState() : d > 4.4 ? Blocks.BONE_BLOCK.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
        }
    }

    /** A bone spike rising out of the salt at a slant. */
    private static void boneSpike(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 3 + (int) (r * 6);
        Direction lean = Direction.from2DDataValue((int) (r * 40) % 4);
        for (int i = -1; i < h; i++) w.force(x + (i * lean.getStepX()) / 3, y + i, z + (i * lean.getStepZ()) / 3, Blocks.BONE_BLOCK.defaultBlockState());
    }

    /** A basalt spire veined with copper, a lightning rod on top drawing the storm. */
    private static void stormSpire(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int tall = 9 + (int) (r * 16);
        double rad = 1.6 + r * 1.4;
        for (int k = -2; k < tall; k++) {
            double rr = rad * (1 - 0.6 * Math.max(0, k) / tall);
            int ri = (int) Math.ceil(rr);
            for (int dx = -ri; dx <= ri; dx++) for (int dz = -ri; dz <= ri; dz++)
                if (dx * dx + dz * dz <= rr * rr + 0.3)
                    w.force(x + dx, y + k, z + dz, hash(24, x + dx, z + dz, k) < 0.18 ? Blocks.OXIDIZED_COPPER.defaultBlockState() : Blocks.POLISHED_BASALT.defaultBlockState());
        }
        w.force(x, y + tall, z, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.UP));
    }

    /** A knot of copper breaking through the ground, green with age. */
    private static void copperOutcrop(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        TowerFloorTerrain.boulder(w, x, y - 1, z, r, Blocks.WEATHERED_COPPER.defaultBlockState(), Blocks.OXIDIZED_COPPER.defaultBlockState());
    }

    // ── floor 25: the mining cavern ─────────────────────────────────────────

    private static final BlockState[] ORES = {
            Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.DEEPSLATE_COAL_ORE.defaultBlockState(), Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(),
            Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(), Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState(), Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(),
            Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(), Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState()};

    private static BlockState caveRock(int x, int y, int z) {
        double o = hash(25, x, z, y);
        if (o < 0.035) {
            double pick = hash(25, z, x, y + 11);
            int i = pick < 0.28 ? 0 : pick < 0.48 ? 1 : pick < 0.64 ? 2 : pick < 0.76 ? 3 : pick < 0.86 ? 4 : pick < 0.93 ? 5 : pick < 0.985 ? 6 : 7;
            return ORES[i];
        }
        return y % 9 < 3 ? Blocks.TUFF.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
    }

    private static void fillCavern(ChunkAccess chunk, TowerFloorTerrain.Writer w, int x0, int z0, BlockPos.MutableBlockPos p) {
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int g = caveGround(x, z), roof = caveRoof(x, z);
                for (int y = 0; y <= 127; y++) {
                    BlockState s;
                    if (y <= 1 || y >= 126) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y <= g || y >= roof) s = caveRock(x, y, z);
                    else s = Blocks.AIR.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                double r = hash(25, x, z, 3);
                if (r < 0.05) w.put(x, g + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
                if (hash(25, x, z, 4) < 0.03) {
                    // glow berries hanging from the roof light the cavern
                    int len = 2 + (int) (hash(25, x, z, 5) * 6);
                    for (int k = 1; k <= len; k++)
                        w.put(x, roof - k, z, (k == len ? Blocks.CAVE_VINES : Blocks.CAVE_VINES_PLANT).defaultBlockState()
                                .setValue(net.minecraft.world.level.block.CaveVines.BERRIES, hash(25, x, z, k) < 0.5));
                }
            }
        scatter(25, w, x0, z0, 28, 8, 0.55, 10, (wr, x, y, z, a, b) -> { if (Math.hypot(Math.abs(x) - 160, Math.abs(z) - 160) > 22) rockPillar(wr, x, z, a); });
        scatter(25, w, x0, z0, 46, 8, 0.4, 20, (wr, x, y, z, a, b) -> geode(wr, x, caveGround(x, z) + 4, z, a));
    }

    /** A pillar from floor to roof, thick with ore. */
    private static void rockPillar(TowerFloorTerrain.Writer w, int x, int z, double r) {
        int bottom = caveGround(x, z), top = caveRoof(x, z);
        double rad = 2.5 + r * 3.5;
        int ri = (int) Math.ceil(rad + 1);
        for (int y = bottom - 1; y <= top; y++) {
            double t = (y - bottom) / (double) Math.max(1, top - bottom);
            double rr = rad * (0.65 + 0.7 * Math.abs(t - 0.5));
            for (int dx = -ri; dx <= ri; dx++) for (int dz = -ri; dz <= ri; dz++)
                if (dx * dx + dz * dz <= rr * rr) w.force(x + dx, y, z + dz, caveRock(x + dx, y + 1000, z + dz));
        }
    }

    /** An amethyst geode half open on the floor: calcite shell, amethyst inside, clusters glowing. */
    private static void geode(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int rad = 3 + (int) (r * 2);
        for (int dx = -rad - 1; dx <= rad + 1; dx++) for (int dy = -rad - 1; dy <= rad + 1; dy++) for (int dz = -rad - 1; dz <= rad + 1; dz++) {
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > rad + 1 || (dz < -1 && dy > 0)) continue;                    // open on one side
            if (d > rad) w.force(x + dx, y + dy, z + dz, Blocks.SMOOTH_BASALT.defaultBlockState());
            else if (d > rad - 1) w.force(x + dx, y + dy, z + dz, Blocks.CALCITE.defaultBlockState());
            else if (d > rad - 2) w.force(x + dx, y + dy, z + dz, hash(25, x + dx, z + dz, y + dy) < 0.1 ? Blocks.BUDDING_AMETHYST.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState());
            else w.force(x + dx, y + dy, z + dz, Blocks.AIR.defaultBlockState());
        }
        w.put(x, y - rad + 2, z, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
    }

    // ── the game's own trees (decoration step) ──────────────────────────────

    /** Floor 21's oaks (orange with the floor's autumn) outside the town, and floor 22's mangroves. */
    static void vanillaTrees(int floor, WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        if (floor != 21 && floor != 22 && floor != 24) return;
        var features = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        int cell = floor == 21 ? 10 : floor == 22 ? 11 : 14;
        double chance = floor == 21 ? 0.5 : floor == 22 ? 0.28 : 0.3;
        for (int a = Math.floorDiv(x0, cell); a <= Math.floorDiv(x0 + 15, cell); a++)
            for (int b = Math.floorDiv(z0, cell); b <= Math.floorDiv(z0 + 15, cell); b++) {
                if (hash(floor, a, b, 40) > chance) continue;
                int x = a * cell + (int) (hash(floor, a, b, 41) * cell), z = b * cell + (int) (hash(floor, a, b, 42) * cell);
                if (x < x0 || x > x0 + 15 || z < z0 || z > z0 + 15 || !flat(floor, x, z)) continue;
                if (floor == 21 && Math.hypot(x, z) < TOWN + 8) continue;
                int h = height(floor, x, z);
                if (floor == 24 && h > 90) continue;
                ResourceKey<ConfiguredFeature<?, ?>> key = switch (floor) {
                    case 21 -> hash(floor, a, b, 43) < 0.3 ? TreeFeatures.FANCY_OAK : hash(floor, a, b, 43) < 0.55 ? TreeFeatures.DARK_OAK : TreeFeatures.OAK;
                    case 22 -> TreeFeatures.MANGROVE;
                    default -> TreeFeatures.SPRUCE;
                };
                var tree = features.getHolder(key);
                if (tree.isEmpty()) continue;
                BlockPos pos = new BlockPos(x, h + 1, z);
                tree.get().value().place(level, generator, RandomSource.create(((long) a << 32) ^ b ^ floor), pos);
            }
    }
}
