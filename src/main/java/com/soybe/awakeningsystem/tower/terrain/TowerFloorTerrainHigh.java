package com.soybe.awakeningsystem.tower.terrain;

import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TeyvatCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.hash;
import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.n;

/**
 * Floors 11 to 15 of the Great Tower:
 * <ol start="11">
 *   <li>Serene Meadow: soft rolling meadow full of flowers, birch and oak groves and a few clear ponds. Peaceful.</li>
 *   <li>Crimson Grove: nylium hills under giant crimson fungi, weeping vines, netherrack crags and shroomlights.</li>
 *   <li>Stone Peaks: tall ridged mountains of stone, andesite and tuff, snow on the summits, ore-rich cliffs.</li>
 *   <li>Shadow Forest: a dense dark-oak wood with giant mushrooms, podzol and moss.</li>
 *   <li>Ancestral Mineral Cavern: one immense, lit cave of ore veins, mana crystals, glow berries and moss. Peaceful.</li>
 * </ol>
 */
final class TowerFloorTerrainHigh {
    private TowerFloorTerrainHigh() {}

    // ── heights ──────────────────────────────────────────────────────────────

    static int height(int floor, int x, int z) {
        return switch (floor) {
            case 11 -> 70 + (int) Math.round(6 * n(11, x, z, 0.006, 3) + 2 * n(11, x + 300, z, 0.03, 2));
            case 12 -> {
                double crag = Math.max(0, n(12, x - 900, z, 0.012, 2) - 0.35);
                yield 66 + (int) Math.round(7 * n(12, x, z, 0.008, 3) + 3 * n(12, x, z + 200, 0.04, 2) + 60 * crag);
            }
            case 13 -> {
                double r = 1.0 - Math.abs(n(13, x, z, 0.0045, 4));
                yield 66 + (int) Math.round(80 * r * r * r * r + 6 * n(13, x + 777, z, 0.02, 2));
            }
            case 14 -> 66 + (int) Math.round(9 * n(14, x, z, 0.007, 3) + 3 * n(14, x - 400, z, 0.03, 2));
            default -> caveFloor(x, z);
        };
    }

    private static int caveFloor(int x, int z) {
        return 22 + (int) Math.round(8 * n(15, x, z, 0.01, 3) + 3 * n(15, x + 80, z, 0.06, 2));
    }

    private static int caveRoof(int x, int z) {
        return 100 + (int) Math.round(12 * n(15, x + 999, z, 0.007, 3));
    }

    private static boolean pond(int x, int z) {
        return n(11, x + 5000, z - 2000, 0.01, 2) > 0.55;
    }

    // ── filling ──────────────────────────────────────────────────────────────

    static void fill(int floor, ChunkAccess chunk) {
        TowerFloorTerrain.Writer w = new TowerFloorTerrain.Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        if (floor == 15) {
            fillCavern(chunk, w, x0, z0, p);
            return;
        }
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int h = height(floor, x, z);
                int slope = Math.abs(height(floor, x + 1, z) - height(floor, x - 1, z)) + Math.abs(height(floor, x, z + 1) - height(floor, x, z - 1));
                boolean steep = slope > 4;
                boolean water = floor == 11 && pond(x, z);
                int top = water ? h - 2 : h;
                for (int y = -64; y <= h; y++) {
                    BlockState s;
                    if (y == -64) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y < 0) s = Blocks.DEEPSLATE.defaultBlockState();
                    else if (water && y > top) s = Blocks.WATER.defaultBlockState();
                    else s = ground(floor, x, y, z, top, steep);
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                if (!water) surface(floor, w, x, h + 1, z, steep);
            }
        }
        switch (floor) {
            case 11 -> {
                scatter(11, w, x0, z0, 9, 4, 0.30, 10, (wr, x, y, z, a, b) -> { if (flat(11, x, z) && !pond(x, z)) broadleaf(wr, x, y, z, a, b); });
                scatter(11, w, x0, z0, 40, 6, 0.35, 20, (wr, x, y, z, a, b) -> { if (flat(11, x, z) && !pond(x, z)) grove(wr, x, z, a); });
            }
            case 12 -> {
                scatter(12, w, x0, z0, 11, 6, 0.55, 10, (wr, x, y, z, a, b) -> { if (flat(12, x, z)) crimsonFungus(wr, x, y, z, a, b); });
                scatter(12, w, x0, z0, 6, 1, 0.18, 20, (wr, x, y, z, a, b) -> { if (flat(12, x, z)) wr.put(x, y, z, Blocks.CRIMSON_ROOTS.defaultBlockState()); });
            }
            case 13 -> {
                scatter(13, w, x0, z0, 10, 3, 0.35, 10, (wr, x, y, z, a, b) -> { if (y < 100 && flat(13, x, z)) mountainPine(wr, x, y, z, a); });
                scatter(13, w, x0, z0, 14, 3, 0.35, 20, (wr, x, y, z, a, b) -> boulder(wr, x, y, z, a));
            }
            case 14 -> {
                scatter(14, w, x0, z0, 6, 4, 0.75, 10, (wr, x, y, z, a, b) -> { if (flat(14, x, z)) darkOak(wr, x, y, z, a); });
                scatter(14, w, x0, z0, 23, 5, 0.45, 20, (wr, x, y, z, a, b) -> { if (flat(14, x, z)) giantMushroom(wr, x, y, z, a, b > 0.5); });
            }
            default -> {}
        }
    }

    private static boolean flat(int floor, int x, int z) {
        return Math.abs(height(floor, x + 2, z) - height(floor, x - 2, z)) + Math.abs(height(floor, x, z + 2) - height(floor, x, z - 2)) <= 3;
    }

    private static BlockState ground(int floor, int x, int y, int z, int h, boolean steep) {
        return switch (floor) {
            case 11 -> y == h ? (steep ? Blocks.STONE.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState())
                    : (y > h - 4 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState());
            case 12 -> {
                if (y == h && !steep) yield n(12, x, z, 0.05, 2) > 0.45 ? Blocks.NETHER_WART_BLOCK.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState();
                if (y > h - 3 && hash(12, x, z, y) < 0.04) yield Blocks.NETHER_GOLD_ORE.defaultBlockState();
                yield y > h - 12 ? Blocks.NETHERRACK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
            }
            case 13 -> {
                if (y == h && y > 128) yield Blocks.SNOW_BLOCK.defaultBlockState();
                if (y == h && !steep && y < 96) yield Blocks.GRASS_BLOCK.defaultBlockState();
                if (y > h - 3 && !steep && y < 96) yield Blocks.DIRT.defaultBlockState();
                double band = n(13, x + y * 3, z - y * 2, 0.02, 1);
                if (hash(13, x, z, y) < 0.012) yield y < 40 ? Blocks.IRON_ORE.defaultBlockState() : Blocks.COAL_ORE.defaultBlockState();
                yield band > 0.35 ? Blocks.ANDESITE.defaultBlockState() : (band < -0.4 ? Blocks.TUFF.defaultBlockState()
                        : (Math.floorMod(y, 7) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.STONE.defaultBlockState()));
            }
            default -> {
                if (y == h) yield steep ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        : (n(14, x, z, 0.06, 2) > 0.2 ? Blocks.PODZOL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                yield y > h - 4 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
        };
    }

    private static void surface(int floor, TowerFloorTerrain.Writer w, int x, int y, int z, boolean steep) {
        if (steep) return;
        double r = hash(floor, x, z, 77);
        switch (floor) {
            case 11 -> {
                if (r < 0.30) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                else if (r < 0.40) {
                    BlockState[] flowers = {Blocks.DANDELION.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState(),
                            Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.ALLIUM.defaultBlockState()};
                    int patch = (int) ((n(11, x, z, 0.05, 1) + 1) * 3) % flowers.length;
                    w.put(x, y, z, flowers[Math.max(0, patch)]);
                }
            }
            case 12 -> {
                if (r < 0.10) w.put(x, y, z, Blocks.CRIMSON_FUNGUS.defaultBlockState());
                else if (r < 0.13 && w.get(x, y - 1, z).is(Blocks.CRIMSON_NYLIUM)) w.put(x, y, z, Blocks.CRIMSON_ROOTS.defaultBlockState());
            }
            case 13 -> {
                if (w.get(x, y - 1, z).is(Blocks.SNOW_BLOCK) && r < 0.7) w.put(x, y, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + (int) (r * 3)));
                else if (w.get(x, y - 1, z).is(Blocks.GRASS_BLOCK) && r < 0.2) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
            }
            case 14 -> {
                if (r < 0.06) w.put(x, y, z, Blocks.BROWN_MUSHROOM.defaultBlockState());
                else if (r < 0.09) w.put(x, y, z, Blocks.RED_MUSHROOM.defaultBlockState());
                else if (r < 0.30) w.put(x, y, z, Blocks.FERN.defaultBlockState());
                else if (r < 0.36) w.put(x, y, z, Blocks.MOSS_CARPET.defaultBlockState());
            }
            default -> {}
        }
    }

    // ── floor 15: the mineral cavern ─────────────────────────────────────────

    private static void fillCavern(ChunkAccess chunk, TowerFloorTerrain.Writer w, int x0, int z0, BlockPos.MutableBlockPos p) {
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int fl = caveFloor(x, z), roof = caveRoof(x, z);
                for (int y = 0; y <= 127; y++) {
                    BlockState s;
                    if (y <= 1 || y >= 126) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y <= fl) s = caveRock(x, y, z, fl, true);
                    else if (y >= roof) s = caveRock(x, y, z, roof, false);
                    else s = Blocks.AIR.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                double r = hash(15, x, z, 5);
                double lush = n(15, x - 300, z + 300, 0.02, 2);
                if (lush > 0.25) {                                         // patches of living moss and azalea
                    w.put(x, fl, z, Blocks.MOSS_BLOCK.defaultBlockState());
                    if (r < 0.25) w.put(x, fl + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
                    else if (r < 0.29) w.put(x, fl + 1, z, Blocks.AZALEA.defaultBlockState());
                    else if (r < 0.31) w.put(x, fl + 1, z, Blocks.FLOWERING_AZALEA.defaultBlockState());
                } else if (r < 0.03) {
                    w.put(x, fl + 1, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(GlowLichenBlock.getFaceProperty(Direction.DOWN), true));
                }
                if (hash(15, x, z, 9) < 0.035) glowBerries(w, x, roof - 1, z, 3 + (int) (hash(15, x, z, 10) * 9));
                else if (hash(15, x, z, 11) < 0.02) w.put(x, roof - 1, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(GlowLichenBlock.getFaceProperty(Direction.UP), true));
            }
        }
        scatter(15, w, x0, z0, 52, 9, 0.45, 10, TowerFloorTerrainHigh::cavePillar);
        scatter(15, w, x0, z0, 10, 2, 0.30, 20, (wr, x, y, z, a, b) -> oreBoulder(wr, x, y, z, a, b));
        scatter(15, w, x0, z0, 6, 0, 0.30, 30, (wr, x, y, z, a, b) -> manaCrystal(wr, x, y, z, a, Direction.UP));
        scatter(15, w, x0, z0, 7, 0, 0.22, 40, (wr, x, y, z, a, b) -> manaCrystal(wr, x, caveRoof(x, z) - 1, z, a, Direction.DOWN));
        scatter(15, w, x0, z0, 18, 0, 0.35, 50, (wr, x, y, z, a, b) -> wr.put(x, caveRoof(x, z), z, Blocks.OCHRE_FROGLIGHT.defaultBlockState()));
    }

    private static BlockState caveRock(int x, int y, int z, int surface, boolean floorSide) {
        double vein = n(15, x + y * 0.7, z - y * 0.5, 0.06, 2);
        double pick = hash(15, x, y, z);
        if (Math.abs(vein) < 0.035) {                                       // thin mineral veins running through the rock
            if (pick < 0.30) return y < 20 ? Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState() : Blocks.GOLD_ORE.defaultBlockState();
            if (pick < 0.55) return y < 20 ? Blocks.DEEPSLATE_IRON_ORE.defaultBlockState() : Blocks.IRON_ORE.defaultBlockState();
            if (pick < 0.75) return Blocks.COPPER_ORE.defaultBlockState();
            if (pick < 0.88) return Blocks.LAPIS_ORE.defaultBlockState();
            if (pick < 0.97) return Blocks.REDSTONE_ORE.defaultBlockState();
            return y < 30 ? Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState() : Blocks.EMERALD_ORE.defaultBlockState();
        }
        if (hash(15, x, z, y + 1000) < 0.01) return Blocks.COAL_ORE.defaultBlockState();
        boolean skin = floorSide ? y >= surface - 1 : y <= surface + 1;
        if (skin) return n(15, x, z, 0.05, 2) > 0.3 ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState();
        return y < 16 ? Blocks.DEEPSLATE.defaultBlockState() : (n(15, x, z + y, 0.04, 1) > 0.5 ? Blocks.GRANITE.defaultBlockState() : Blocks.STONE.defaultBlockState());
    }

    private static void glowBerries(TowerFloorTerrain.Writer w, int x, int y, int z, int len) {
        for (int k = 0; k < len; k++) {
            boolean last = k == len - 1;
            BlockState s = (last ? Blocks.CAVE_VINES : Blocks.CAVE_VINES_PLANT).defaultBlockState()
                    .setValue(CaveVines.BERRIES, hash(15, x, z, k) < 0.35);
            if (!w.get(x, y - k, z).isAir()) break;
            w.put(x, y - k, z, s);
        }
    }

    /** A wide stone pillar joining floor and roof, banded with tuff and studded with ore. */
    private static void cavePillar(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        double rad = 4 + r1 * 4;
        int top = caveRoof(x, z), bottom = y - 2;
        int ri = (int) Math.ceil(rad * 1.4);
        for (int yy = bottom; yy <= top; yy++) {
            double t = (yy - bottom) / (double) Math.max(1, top - bottom);
            double rr = rad * (0.65 + 0.7 * Math.abs(t - 0.5));
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz) + hash(15, x + dx, z + dz, yy / 5) * 1.2;
                    if (d > rr) continue;
                    w.force(x + dx, yy, z + dz, caveRock(x + dx, yy, z + dz, -999, true));
                }
        }
    }

    /** A boulder of ore-shot rock on the cave floor. */
    private static void oreBoulder(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        double rad = 1.3 + r1 * 1.8;
        BlockState ore = r2 < 0.35 ? Blocks.RAW_IRON_BLOCK.defaultBlockState() : r2 < 0.6 ? Blocks.RAW_COPPER_BLOCK.defaultBlockState()
                : r2 < 0.75 ? Blocks.RAW_GOLD_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState();
        int ri = (int) Math.ceil(rad);
        for (int dx = -ri; dx <= ri; dx++)
            for (int dy = -1; dy <= ri; dy++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dy * dy * 1.5 + dz * dz <= rad * rad)
                        w.put(x + dx, y + dy, z + dz, hash(15, x + dx, y + dy, z + dz) < 0.3 ? ore : Blocks.STONE.defaultBlockState());
    }

    private static void manaCrystal(TowerFloorTerrain.Writer w, int x, int y, int z, double r, Direction facing) {
        BlockState c;
        if (r < 0.6) c = SystemBlocks.MANA_CRYSTAL_CLUSTER_E.get().defaultBlockState();
        else if (r < 0.9) c = SystemBlocks.MANA_CRYSTAL_CLUSTER_D.get().defaultBlockState();
        else c = SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get().defaultBlockState();
        if (c.hasProperty(TeyvatCrystalBlock.FACING)) c = c.setValue(TeyvatCrystalBlock.FACING, facing);
        if (w.get(x, y, z).isAir()) w.put(x, y, z, c);
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

    /** An oak or birch with a round crown. */
    private static void broadleaf(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        boolean birch = r2 < 0.45;
        BlockState log = (birch ? Blocks.BIRCH_LOG : Blocks.OAK_LOG).defaultBlockState();
        BlockState leaf = TowerFloorTerrain.leaves(birch ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES);
        int h = (birch ? 6 : 5) + (int) (r1 * 3);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, log);
        TowerFloorTerrain.blob(w, x, y + h - 1, z, birch ? 2.4 : 3.0 + r1, leaf, false);
    }

    /** A loose ring of birches, the meadow's little groves. */
    private static void grove(TowerFloorTerrain.Writer w, int x, int z, double r) {
        for (int i = 0; i < 4; i++) {
            double a = r * 6.28 + i * 1.57;
            int tx = x + (int) Math.round(Math.cos(a) * 4), tz = z + (int) Math.round(Math.sin(a) * 4);
            if (flat(11, tx, tz) && !pond(tx, tz)) broadleaf(w, tx, height(11, tx, tz) + 1, tz, hash(11, tx, tz, 3), 0.1);
        }
    }

    /** A giant crimson fungus: thick stem, wide wart cap, shroomlights and weeping vines underneath. */
    private static void crimsonFungus(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        int h = 8 + (int) (r1 * 10);
        BlockState stem = Blocks.CRIMSON_STEM.defaultBlockState();
        for (int i = 0; i < h; i++) {
            w.trunk(x, y + i, z, stem);
            if (r1 > 0.5 && i < h - 2) { w.trunk(x + 1, y + i, z, stem); w.trunk(x, y + i, z + 1, stem); w.trunk(x + 1, y + i, z + 1, stem); }
        }
        double rad = 3.5 + r1 * 3;
        int ri = (int) Math.ceil(rad);
        for (int dx = -ri; dx <= ri; dx++)
            for (int dz = -ri; dz <= ri; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > rad) continue;
                int capY = y + h - (int) (d * 0.5);
                w.put(x + dx, capY, z + dz, hash(12, x + dx, z + dz, 3) < 0.08 ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.NETHER_WART_BLOCK.defaultBlockState());
                if (d > rad - 1.3) {
                    w.put(x + dx, capY - 1, z + dz, Blocks.NETHER_WART_BLOCK.defaultBlockState());
                    int vines = (int) (hash(12, x + dx, z + dz, 5) * 6) - 1;
                    for (int k = 0; k < vines; k++)
                        w.put(x + dx, capY - 2 - k, z + dz, (k == vines - 1 ? Blocks.WEEPING_VINES : Blocks.WEEPING_VINES_PLANT).defaultBlockState());
                }
            }
    }

    /** A wind-bent spruce clinging to the slopes. */
    private static void mountainPine(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 6 + (int) (r * 6);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, Blocks.SPRUCE_LOG.defaultBlockState());
        BlockState leaf = TowerFloorTerrain.leaves(Blocks.SPRUCE_LEAVES);
        for (int i = 2; i <= h + 1; i++) {
            double rad = (i % 2 == 0 ? 2.4 : 1.5) * (1.0 - (double) i / (h + 3));
            int ri = (int) Math.ceil(rad);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rad * rad + 0.3 && (dx != 0 || dz != 0 || i > h - 1)) w.put(x + dx, y + i, z + dz, leaf);
        }
        w.put(x, y + h + 1, z, leaf);
    }

    private static void boulder(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        double rad = 1.5 + r * 2.5;
        int ri = (int) Math.ceil(rad);
        for (int dx = -ri; dx <= ri; dx++)
            for (int dy = -1; dy <= ri; dy++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dy * dy * 1.6 + dz * dz <= rad * rad)
                        w.put(x + dx, y + dy, z + dz, hash(13, x + dx, y + dy, z + dz) < 0.3 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.ANDESITE.defaultBlockState());
    }

    /** A thick, low dark oak with a broad flat crown. */
    private static void darkOak(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
        BlockState leaf = TowerFloorTerrain.leaves(Blocks.DARK_OAK_LEAVES);
        int h = 6 + (int) (r * 4);
        for (int i = 0; i < h; i++) {
            w.trunk(x, y + i, z, log);
            w.trunk(x + 1, y + i, z, log);
            w.trunk(x, y + i, z + 1, log);
            w.trunk(x + 1, y + i, z + 1, log);
        }
        TowerFloorTerrain.blob(w, x, y + h, z, 4.2 + r, leaf, false);
        TowerFloorTerrain.blob(w, x + 1, y + h + 1, z + 1, 3.0, leaf, false);
    }

    /** A giant red or brown mushroom. */
    private static void giantMushroom(TowerFloorTerrain.Writer w, int x, int y, int z, double r, boolean red) {
        int h = 5 + (int) (r * 5);
        BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, stem);
        BlockState cap = (red ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK).defaultBlockState().setValue(HugeMushroomBlock.DOWN, false);
        int rad = red ? 2 : 3;
        for (int dx = -rad; dx <= rad; dx++)
            for (int dz = -rad; dz <= rad; dz++) {
                if (Math.abs(dx) == rad && Math.abs(dz) == rad) continue;
                w.put(x + dx, y + h, z + dz, cap);
                if (red && (Math.abs(dx) == rad || Math.abs(dz) == rad)) {
                    w.put(x + dx, y + h - 1, z + dz, cap);
                    w.put(x + dx, y + h - 2, z + dz, cap);
                }
            }
    }
}
