package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TeyvatCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Gruta Abisal (roadmap sección 20): una cueva que se encuentra explorando el mundo, sin puerta.
 * <ul>
 *   <li>Una dolina en la superficie con una rampa en espiral que baja pegada a la pared.</li>
 *   <li>Una caverna grande e irregular con estalactitas y estalagmitas, cristales de maná,
 *       amatista, liquen brillante, una cascada y un abismo de lava cruzado por un puente de roca.</li>
 *   <li>Nidos de monstruos (arañas de cueva y esqueletos) con generadores.</li>
 *   <li>Una cámara del jefe en forma de domo, con gradas de roca, un minijefe y el cofre del tesoro.</li>
 * </ul>
 * Los cofres llevan una tabla de botín y no objetos sueltos, como los de las mazmorras: así Lootr
 * los convierte en cofres por jugador.
 */
public final class AbyssalGrottoBuilder {
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/abyssal_grotto"));
    public static final ResourceKey<LootTable> TREASURE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/abyssal_grotto_treasure"));

    private static final int RX = 22, RY = 12, RZ = 18;     // main cavern radii
    private static final int DOME_R = 11;                    // boss dome radius

    private final ServerLevel level;
    private final Random rand;
    private final double[] phase = new double[9];
    private final Set<Long> air = new HashSet<>();
    private final List<BlockPos> carvedList = new ArrayList<>();
    /** Walking space (tunnels, ramp, bridge): kept free of dripstone, crystals, nests and chests. */
    private final Set<Long> keep = new HashSet<>();
    /** Floor blocks laid under tunnels once all carving is done. */
    private final List<BlockPos> tunnelFloors = new ArrayList<>();
    /** Terrain height of each column before the grotto touched it, to tell open sky from caves to seal. */
    private final java.util.Map<Long, Integer> surface = new java.util.HashMap<>();
    private final List<BlockPos> chestList = new ArrayList<>();
    private BlockState wallA, wallB, wallC;
    private int chests, spawners, sealed;

    private AbyssalGrottoBuilder(ServerLevel level, long seed) {
        this.level = level;
        this.rand = new Random(seed);
        for (int i = 0; i < phase.length; i++) phase[i] = rand.nextDouble() * Math.PI * 2;
        this.wallA = Blocks.DEEPSLATE.defaultBlockState();
        this.wallB = Blocks.TUFF.defaultBlockState();
        this.wallC = SystemBlocks.ABYSSAL_STONE.get().defaultBlockState();
    }

    /** Builds the grotto with its sinkhole near {@code surface}; returns the sinkhole mouth, or null if it does not fit. */
    public static BlockPos build(ServerLevel level, BlockPos surface) {
        int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, surface.getX(), surface.getZ());
        int cy = Math.max(level.getMinBuildHeight() + 34, Math.min(40, surfaceY - 34));
        if (surfaceY - cy < 26) return null;
        AbyssalGrottoBuilder b = new AbyssalGrottoBuilder(level, level.getSeed() ^ surface.asLong());
        int dir = b.rand.nextBoolean() ? 1 : -1;   // boss dome on +x or -x, sinkhole on the other side
        BlockPos center = new BlockPos(surface.getX(), cy, surface.getZ());
        return b.buildAt(center, dir);
    }

    private BlockPos buildAt(BlockPos c, int dir) {
        int cx = c.getX(), cy = c.getY(), cz = c.getZ();

        // 1. main cavern, irregular ellipsoid with a lumpy floor
        for (int dx = -RX - 3; dx <= RX + 3; dx++) for (int dz = -RZ - 3; dz <= RZ + 3; dz++) {
            int floor = (int) Math.round(-8 + 1.6 * noise(dx * 0.21, 0, dz * 0.21));
            for (int dy = -RY; dy <= RY + 2; dy++) {
                double n = noise(dx * 0.13, dy * 0.17, dz * 0.13);
                double d = sq(dx / (double) RX) + sq(dy / (double) RY) + sq(dz / (double) RZ);
                if (d < 1.0 + 0.22 * n && dy > floor) carve(cx + dx, cy + dy, cz + dz);
            }
        }

        // 2. the abyss: a lava trench across the cavern, crossed by a natural rock bridge
        int abyssBottom = cy - 22;
        for (int dz = -RZ; dz <= RZ; dz++) for (int dx = -4; dx <= 4; dx++) {
            double wobble = 1.6 * noise(0, 3.1, dz * 0.25);
            if (Math.abs(dx - wobble) > 3.2) continue;
            if (!isAir(cx + dx, cy - 6, cz + dz) && !isAir(cx + dx, cy - 4, cz + dz)) continue;
            for (int y = abyssBottom; y <= cy - 6; y++) carve(cx + dx, y, cz + dz);
        }
        for (int dz = -RZ; dz <= RZ; dz++) for (int dx = -4; dx <= 4; dx++) {
            for (int y = abyssBottom; y <= abyssBottom + 1; y++)
                if (isAir(cx + dx, y, cz + dz)) set(cx + dx, y, cz + dz, Blocks.LAVA.defaultBlockState(), 3);
        }

        // 3. sinkhole shaft with a spiral ramp, then a wide tunnel into the cavern
        int sx = cx - dir * (RX + 9), sz = cz;
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
        int bottomY = cy - 7;
        int shaftR = 6;
        for (int y = bottomY; y <= topY + 2; y++) for (int dx = -shaftR - 1; dx <= shaftR + 1; dx++) for (int dz = -shaftR - 1; dz <= shaftR + 1; dz++) {
            double r = Math.sqrt(dx * dx + dz * dz) + 0.7 * noise(dx * 0.3, y * 0.2, dz * 0.3);
            if (r <= shaftR) carve(sx + dx, y, sz + dz);
        }
        spiralRamp(sx, sz, shaftR, topY, bottomY);
        // landing ring at the bottom of the shaft
        for (int dx = -shaftR; dx <= shaftR; dx++) for (int dz = -shaftR; dz <= shaftR; dz++)
            if (dx * dx + dz * dz <= shaftR * shaftR) tunnelFloors.add(new BlockPos(sx + dx, bottomY - 1, sz + dz));
        int entryX = cx - dir * (RX - 6);
        tunnelX(sx, entryX, sz, bottomY, cavernWalk(entryX, cy, cz, bottomY));

        // 4. boss dome beyond the cavern, joined by a tunnel that climbs to its outer terrace
        int bx = cx + dir * (RX + DOME_R + 4), by = cy - 6, bz = cz;
        for (int dx = -DOME_R; dx <= DOME_R; dx++) for (int dz = -DOME_R; dz <= DOME_R; dz++) for (int dy = -3; dy <= DOME_R; dy++) {
            double d = Math.sqrt(dx * dx + dy * dy * 1.4 + dz * dz) + 0.6 * noise(dx * 0.2, dy * 0.2, dz * 0.2);
            if (d <= DOME_R) carve(bx + dx, by + dy, bz + dz);
        }
        int domeExitX = cx + dir * (RX - 6);
        tunnelX(domeExitX, bx - dir * (DOME_R - 2), bz, cavernWalk(domeExitX, cy, cz, by), by + 2);

        // 5. skin every exposed wall with the grotto's stone and seal any cave, water or lava it cut into
        skin();

        // floors that stay walkable: fill the dome's bottom and its terraces
        for (int dx = -DOME_R; dx <= DOME_R; dx++) for (int dz = -DOME_R; dz <= DOME_R; dz++) {
            double r = Math.sqrt(dx * dx + dz * dz);
            if (r > DOME_R) continue;
            int floorY = by - 1 + (r > DOME_R - 3 ? 2 : (r > DOME_R - 6 ? 1 : 0));   // stepped rings
            for (int y = by - 3; y <= floorY; y++) set(bx + dx, y, bz + dz, r > DOME_R - 6 ? wallB : polished(), 2);
        }
        layRamp();
        for (BlockPos f : tunnelFloors) {
            BlockState st = level.getBlockState(f);
            if (st.isAir() || !st.getFluidState().isEmpty() || !isSolid(f.getX(), f.getY(), f.getZ())) set(f.getX(), f.getY(), f.getZ(), wallA, 2);
        }

        // 6. rock bridge over the abyss: 5 wide, thick in the middle, meeting the floor on both sides
        // the trench runs along z, so the bridge crosses it along x
        int walkL = cavernWalk(cx - 8, cy, cz, cy - 7), walkR = cavernWalk(cx + 8, cy, cz, cy - 7);
        for (int dx = -8; dx <= 8; dx++) {
            int walk = (int) Math.round(walkL + (walkR - walkL) * ((dx + 8) / 16.0));
            for (int dz = -2; dz <= 2; dz++) {
                int x = cx + dx, z = cz + dz;
                int depth = Math.abs(dz) == 2 ? 2 : (Math.abs(dx) <= 4 ? 4 : 3);   // a keel under the middle
                for (int k = 1; k <= depth; k++) set(x, walk - k, z, k == 1 ? (Math.abs(dz) == 2 ? wallA : wallB) : wallA, 2);
                for (int h = 0; h <= 3; h++) { carve(x, walk + h, z); keep(x, walk + h, z); }
            }
        }

        // 7. decoration
        decorate(cx, cy, cz);

        // 8. waterfall from a wall alcove into the cavern
        waterfall(cx, cy, cz, -dir);

        // 9. nests with spawners, chests in the cavern, treasure in the dome
        List<BlockPos> taken = new ArrayList<>();
        BlockPos spiderNest = floorSpot(cx, cy, cz, -RX + 3, RX - 3, 3, RZ - 2, taken);
        if (spiderNest != null) nest(spiderNest, EntityType.CAVE_SPIDER, true);
        BlockPos boneNest = floorSpot(cx, cy, cz, -RX + 3, RX - 3, -RZ + 2, -3, taken);
        if (boneNest != null) nest(boneNest, EntityType.SKELETON, false);
        for (int[] zone : new int[][]{{3, RZ - 2}, {-RZ + 2, -3}, {-RZ + 2, RZ - 2}}) {
            BlockPos spot = floorSpot(cx, cy, cz, -RX + 3, RX - 3, zone[0], zone[1], taken);
            if (spot != null) placeChest(spot, Direction.from2DDataValue(rand.nextInt(4)), LOOT);
        }
        BlockPos treasure = new BlockPos(bx + dir * (DOME_R - 4), by + 1, bz);
        clearAbove(treasure, 3);
        placeChest(treasure, dir > 0 ? Direction.WEST : Direction.EAST, TREASURE);
        set(treasure.getX(), treasure.getY(), treasure.getZ() + 2, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        set(treasure.getX(), treasure.getY(), treasure.getZ() - 2, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        // lanterns at the dome's mouth
        int mouthX = cx + dir * (RX + 2);
        set(mouthX, by, bz + 3, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        set(mouthX, by, bz - 3, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        // ring of shroomlights in the dome's ceiling
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2 / 10;
            int x = bx + (int) Math.round(Math.cos(a) * 6), z = bz + (int) Math.round(Math.sin(a) * 6);
            for (int y = by + DOME_R + 1; y > by + 2; y--) {
                if (isAir(x, y - 1, z) && !isAir(x, y, z)) { set(x, y, z, Blocks.SHROOMLIGHT.defaultBlockState(), 2); break; }
            }
        }
        SeriesStructureWorldGen.spawnOverworldMiniboss(level, new BlockPos(bx, by - 1, bz));

        // 10. sinkhole rim: mossy stone and hanging vines around the mouth
        for (int dx = -shaftR - 2; dx <= shaftR + 2; dx++) for (int dz = -shaftR - 2; dz <= shaftR + 2; dz++) {
            double r = Math.sqrt(dx * dx + dz * dz);
            if (r <= shaftR || r > shaftR + 2) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx + dx, sz + dz) - 1;
            if (y < topY - 4) continue;
            set(sx + dx, y, sz + dz, rand.nextInt(3) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState(), 2);
        }
        for (int i = 0; i < 14; i++) {
            double a = rand.nextDouble() * Math.PI * 2;
            int x = sx + (int) Math.round(Math.cos(a) * (shaftR + 0.4)), z = sz + (int) Math.round(Math.sin(a) * (shaftR + 0.4));
            int y = topY - 1 - rand.nextInt(3);
            Direction toWall = Direction.getNearest(Math.cos(a), 0, Math.sin(a));
            int len = 2 + rand.nextInt(5);
            for (int k = 0; k < len; k++) {
                BlockPos p = new BlockPos(x, y - k, z).relative(toWall.getOpposite());
                if (!isAir(p.getX(), p.getY(), p.getZ())) break;
                set(p.getX(), p.getY(), p.getZ(), Blocks.CAVE_VINES.defaultBlockState(), 2);
            }
        }
        AwakeningSystemMod.LOGGER.info("[Gruta Abisal] construida en {} {} {}: {} bloques excavados, {} huecos sellados, {} cofres, {} generadores",
                cx, cy, cz, carvedList.size(), sealed, chests, spawners);
        if (System.getProperty("awakening.debugGrotto") != null) {
            debugSlices(cx, cy, cz, sx, topY);
            walkCheck(cx, cy, cz, sx, sz, shaftR, topY, new BlockPos(bx, by, bz));
        }
        return new BlockPos(sx, topY, sz);
    }

    /**
     * Dev aid: walks the grotto like a player (steps of one block up, drops of at most three) from the
     * surface next to the sinkhole, and back out from the boss dome, and logs what can and cannot be reached.
     */
    private void walkCheck(int cx, int cy, int cz, int sx, int sz, int shaftR, int topY, BlockPos dome) {
        RampCell top = ramp.get(0);   // the ramp's first step, just under the sinkhole's rim
        BlockPos start = new BlockPos(top.x(), top.y(), top.z());
        int[] box = {cx - RX - 32, cx + RX + 32, cy - 26, topY + 6, cz - RZ - 10, cz + RZ + 10};
        Set<Long> in = walk(start, box);
        Set<Long> out = walk(dome, box);
        int chestsIn = 0;
        for (BlockPos c : chestList) if (touches(in, c)) chestsIn++;
        int floors = 0, lost = 0;
        for (BlockPos p : carvedList) {
            if (!standable(p.getX(), p.getY(), p.getZ())) continue;
            floors++;
            if (!in.contains(p.asLong())) lost++;
        }
        AwakeningSystemMod.LOGGER.info("[Gruta Abisal] recorrido: domo alcanzable={}, salida desde el domo={}, cofres alcanzables {}/{}, suelo sin acceso {}/{} (inicio {} {} {}, domo {} {} -> {} y {} celdas)",
                in.contains(dome.asLong()), out.contains(start.asLong()), chestsIn, chestList.size(), lost, floors,
                start.toShortString(), standable(start.getX(), start.getY(), start.getZ()), level.getBlockState(start.below()),
                dome.toShortString(), standable(dome.getX(), dome.getY(), dome.getZ()), in.size(), out.size());
    }

    private boolean touches(Set<Long> reached, BlockPos c) {
        for (Direction d : Direction.Plane.HORIZONTAL) for (int dy = -1; dy <= 1; dy++)
            if (reached.contains(c.relative(d).above(dy).asLong())) return true;
        return reached.contains(c.above().asLong());
    }

    private Set<Long> walk(BlockPos start, int[] box) {
        Set<Long> seen = new HashSet<>();
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        if (!standable(start.getX(), start.getY(), start.getZ())) return seen;
        seen.add(start.asLong());
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            boolean headroom = passable(p.getX(), p.getY() + 2, p.getZ());
            for (Direction d : Direction.Plane.HORIZONTAL) {
                int x = p.getX() + d.getStepX(), z = p.getZ() + d.getStepZ();
                if (x < box[0] || x > box[1] || z < box[4] || z > box[5]) continue;
                int found = Integer.MIN_VALUE;
                if (headroom && standable(x, p.getY() + 1, z)) found = p.getY() + 1;
                else if (passable(x, p.getY(), z) && passable(x, p.getY() + 1, z)) {
                    for (int k = 0; k <= 3; k++) {
                        int y = p.getY() - k;
                        if (y < box[2]) break;
                        if (standable(x, y, z)) { found = y; break; }
                        if (!passable(x, y, z)) break;
                    }
                }
                if (found == Integer.MIN_VALUE || found > box[3]) continue;
                BlockPos n = new BlockPos(x, found, z);
                if (seen.add(n.asLong())) queue.add(n);
            }
        }
        return seen;
    }

    private boolean passable(int x, int y, int z) {
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = level.getBlockState(p);
        return s.getCollisionShape(level, p).isEmpty() && !s.is(Blocks.LAVA);
    }

    private boolean standable(int x, int y, int z) {
        BlockPos below = new BlockPos(x, y - 1, z);
        return passable(x, y, z) && passable(x, y + 1, z) && !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    /** Dev aid: writes a side cut (through the sinkhole, cavern and dome) and a floor plan as PNGs. */
    private void debugSlices(int cx, int cy, int cz, int sx, int topY) {
        try {
            int x0 = cx - RX - 30, x1 = cx + RX + 30, s = 4;
            int y0 = cy - 26, y1 = topY + 4;
            java.awt.image.BufferedImage side = new java.awt.image.BufferedImage((x1 - x0) * s, (y1 - y0) * s, java.awt.image.BufferedImage.TYPE_INT_RGB);
            for (int x = x0; x < x1; x++) for (int y = y0; y < y1; y++) fillCell(side, (x - x0) * s, (y1 - 1 - y) * s, s, colour(level.getBlockState(new BlockPos(x, y, cz))));
            int z0 = cz - RZ - 6, z1 = cz + RZ + 6;
            java.awt.image.BufferedImage plan = new java.awt.image.BufferedImage((x1 - x0) * s, (z1 - z0) * s, java.awt.image.BufferedImage.TYPE_INT_RGB);
            for (int x = x0; x < x1; x++) for (int z = z0; z < z1; z++) {
                int col = 0x202020;
                for (int y = cy + 2; y > cy - 24; y--) {       // first thing below the cavern's middle
                    BlockState st = level.getBlockState(new BlockPos(x, y, z));
                    if (!st.isAir()) { col = colour(st); break; }
                }
                fillCell(plan, (x - x0) * s, (z - z0) * s, s, col);
            }
            java.io.File dir = level.getServer().getServerDirectory().resolve("grotto_debug").toFile();
            dir.mkdirs();
            javax.imageio.ImageIO.write(side, "png", new java.io.File(dir, "side.png"));
            javax.imageio.ImageIO.write(plan, "png", new java.io.File(dir, "plan.png"));
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("[Gruta Abisal] debug slices failed", e);
        }
    }

    private static void fillCell(java.awt.image.BufferedImage img, int px, int py, int s, int col) {
        for (int i = 0; i < s; i++) for (int j = 0; j < s; j++) img.setRGB(px + i, py + j, col);
    }

    private static int colour(BlockState st) {
        if (st.is(Blocks.CAVE_AIR)) return 0x1A2230;
        if (st.isAir()) return 0x86B4E8;
        if (st.is(Blocks.CHEST)) return 0xFFE040;
        if (st.is(Blocks.SPAWNER)) return 0xFF40FF;
        if (st.is(Blocks.LAVA)) return 0xFF6A10;
        if (st.is(Blocks.WATER)) return 0x3070FF;
        if (st.is(Blocks.SHROOMLIGHT) || st.is(Blocks.SOUL_LANTERN)) return 0xFFB060;
        if (st.is(Blocks.POINTED_DRIPSTONE) || st.is(Blocks.DRIPSTONE_BLOCK)) return 0x9A6E50;
        if (st.is(Blocks.AMETHYST_CLUSTER) || st.getBlock() instanceof TeyvatCrystalBlock) return 0x40F0FF;
        if (st.is(Blocks.MOSS_BLOCK) || st.is(Blocks.MOSSY_COBBLESTONE) || st.is(Blocks.CAVE_VINES) || st.is(Blocks.GLOW_LICHEN)) return 0x50B050;
        if (st.is(Blocks.COBWEB) || st.is(Blocks.BONE_BLOCK)) return 0xF0F0F0;
        if (st.is(Blocks.TUFF)) return 0x707468;
        if (st.is(Blocks.POLISHED_DEEPSLATE) || st.is(Blocks.CHISELED_DEEPSLATE)) return 0x8A8A9A;
        if (st.is(Blocks.DEEPSLATE) || st.is(Blocks.COBBLED_DEEPSLATE)) return 0x4A4A55;
        if (st.getBlock() == SystemBlocks.ABYSSAL_STONE.get()) return 0x3A2E5A;
        if (st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.DIRT)) return 0x5E8A3A;
        return 0x9A9A9A;
    }

    // ---------------------------------------------------------------- decoration
    private void decorate(int cx, int cy, int cz) {
        BlockState crystal = SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get().defaultBlockState();
        for (BlockPos p : carvedList) {
            int x = p.getX(), y = p.getY(), z = p.getZ();
            if (!isAir(x, y, z)) continue;
            boolean kept = keep.contains(p.asLong());
            boolean floor = !isAir(x, y - 1, z) && isSolid(x, y - 1, z);
            boolean ceiling = !isAir(x, y + 1, z) && isSolid(x, y + 1, z);
            int roll = rand.nextInt(1000);
            if (ceiling && roll < 14) dripstone(x, y, z, Direction.DOWN, 2 + rand.nextInt(5));
            else if (ceiling && roll < 22) set(x, y + 1, z, Blocks.SHROOMLIGHT.defaultBlockState(), 2);
            else if (kept) continue;
            else if (floor && roll < 6 && y > cy - 8 && !nearKeep(x, y, z)) dripstone(x, y, z, Direction.UP, 1 + rand.nextInt(3));
            else if (floor && roll < 16 && y > cy - 8) set(x, y, z, crystal.setValue(TeyvatCrystalBlock.FACING, Direction.UP), 2);
            else if (floor && roll < 22 && y > cy - 8) set(x, y, z, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
            else if (roll < 60) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (isSolid(x + d.getStepX(), y, z + d.getStepZ())) {
                        if (roll < 34) set(x, y, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(d), true), 2);
                        else if (roll < 40) set(x, y, z, crystal.setValue(TeyvatCrystalBlock.FACING, d.getOpposite()), 2);
                        break;
                    }
                }
            }
        }
    }

    private void dripstone(int x, int y, int z, Direction tip, int len) {
        int step = tip == Direction.DOWN ? -1 : 1;
        // shorten it so it never reaches into a path, and stalactites leave head room below them
        int fit = 0;
        for (int i = 0; i < len; i++) {
            int yy = y + step * i;
            if (!isAir(x, yy, z) || keep.contains(BlockPos.asLong(x, yy, z))) break;
            if (tip == Direction.DOWN) {
                boolean clear = true;
                for (int k = 1; k <= 3; k++) if (!isAir(x, yy - k, z)) { clear = false; break; }
                if (!clear) break;
            }
            fit++;
        }
        if (fit == 0) return;
        len = fit;
        int anchorY = y - step;
        set(x, anchorY, z, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
        for (int i = 0; i < len; i++) {
            int yy = y + step * i;
            DripstoneThickness t = len == 1 ? DripstoneThickness.TIP
                    : i == len - 1 ? DripstoneThickness.TIP
                    : i == len - 2 ? DripstoneThickness.FRUSTUM
                    : i == 0 ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            set(x, yy, z, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, tip)
                    .setValue(PointedDripstoneBlock.THICKNESS, t), 2);
        }
    }

    private void waterfall(int cx, int cy, int cz, int side) {
        // a source in the wall above the cavern floor on the sinkhole side, falling into a small pool
        int x = cx + side * (RX - 5), z = cz + RZ / 2;
        for (int y = cy + RY - 2; y > cy - 6; y--) {
            if (!isAir(x, y, z)) continue;
            // find the wall towards +z
            int wz = z;
            while (wz < z + 8 && isAir(x, y, wz + 1)) wz++;
            if (wz == z + 8) continue;
            set(x, y, wz + 1, Blocks.STONE.defaultBlockState(), 2);
            set(x, y, wz, Blocks.WATER.defaultBlockState(), 3);
            // pool where it lands
            for (int fy = y; fy > cy - 12; fy--) {
                if (!isAir(x, fy - 1, wz)) {
                    for (int px = -1; px <= 1; px++) for (int pz = -1; pz <= 1; pz++)
                        if (isAir(x + px, fy, wz + pz) || px == 0 && pz == 0) set(x + px, fy - 1, wz + pz, Blocks.WATER.defaultBlockState(), 3);
                    break;
                }
            }
            // a chest tucked behind the falling water
            chestOnFloor(x + side, y, wz, LOOT);
            return;
        }
    }

    /** A random open floor spot in the main cavern (off the abyss), at least 7 blocks from earlier ones. */
    private BlockPos floorSpot(int cx, int cy, int cz, int minX, int maxX, int minZ, int maxZ, List<BlockPos> taken) {
        List<BlockPos> options = new ArrayList<>();
        for (BlockPos p : carvedList) {
            int dx = p.getX() - cx, dz = p.getZ() - cz;
            if (dx < minX || dx > maxX || dz < minZ || dz > maxZ || Math.abs(dx) <= 9 || p.getY() > cy) continue;
            if (nearKeep(p.getX(), p.getY(), p.getZ())) continue;
            if (!isAir(p.getX(), p.getY(), p.getZ()) || !isAir(p.getX(), p.getY() + 1, p.getZ()) || !isSolid(p.getX(), p.getY() - 1, p.getZ())) continue;
            boolean crowded = false;
            for (BlockPos t : taken) if (t.distSqr(p) < 49) { crowded = true; break; }
            if (!crowded) options.add(p);
        }
        if (options.isEmpty()) return null;
        BlockPos pick = options.get(rand.nextInt(options.size()));
        taken.add(pick);
        return pick;
    }

    private void nest(BlockPos at, EntityType<?> type, boolean spider) {
        int x = at.getX(), y = at.getY(), z = at.getZ();
        set(x, y, z, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof SpawnerBlockEntity s) {
            s.setEntityId(type, level.getRandom());
            spawners++;
        }
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            if (dx == 0 && dz == 0) continue;
            if (rand.nextInt(10) > 4) continue;
            int fy = floorAt(x + dx, y + 2, z + dz);
            if (fy == Integer.MIN_VALUE || Math.abs(fy - y) > 2) continue;
            if (spider) set(x + dx, fy, z + dz, Blocks.COBWEB.defaultBlockState(), 2);
            else set(x + dx, fy - 1, z + dz, rand.nextBoolean() ? Blocks.BONE_BLOCK.defaultBlockState() : Blocks.SOUL_SOIL.defaultBlockState(), 2);
        }
        if (!spider) set(x + 1, y, z, Blocks.SKELETON_SKULL.defaultBlockState(), 2);
    }

    private void chestOnFloor(int x, int cy, int z, ResourceKey<LootTable> loot) {
        int y = floorAt(x, cy, z);
        if (y == Integer.MIN_VALUE) return;
        Direction facing = Direction.from2DDataValue(rand.nextInt(4));
        placeChest(new BlockPos(x, y, z), facing, loot);
    }

    private void placeChest(BlockPos p, Direction facing, ResourceKey<LootTable> loot) {
        level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        if (level.getBlockEntity(p) instanceof ChestBlockEntity chest) {
            chest.setLootTable(loot, rand.nextLong());
            chests++;
            chestList.add(p);
        }
    }

    // ---------------------------------------------------------------- carving helpers
    private void carve(int x, int y, int z) {
        if (y <= level.getMinBuildHeight() + 4) return;
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = level.getBlockState(p);
        if (s.is(Blocks.BEDROCK)) return;
        surface.computeIfAbsent(BlockPos.asLong(x, 0, z), k -> level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z));
        long key = p.asLong();
        if (air.add(key)) carvedList.add(p);
        if (!s.isAir()) level.setBlock(p, Blocks.CAVE_AIR.defaultBlockState(), 2);
    }

    private void keep(int x, int y, int z) {
        keep.add(BlockPos.asLong(x, y, z));
    }

    /** True when a path cell is right next to this one (same level or one up), so nothing blocks the way. */
    private boolean nearKeep(int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = 0; dy <= 1; dy++)
            if (keep.contains(BlockPos.asLong(x + dx, y + dy, z + dz))) return true;
        return false;
    }

    /** Terrain height of a column as it was before carving. */
    private int surfaceAt(int x, int z) {
        Integer h = surface.get(BlockPos.asLong(x, 0, z));
        return h != null ? h : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    /**
     * Gives every wall the grotto's stone. Air, water or lava next to the grotto that is not open sky
     * (another cave, an aquifer, a lake bed) gets walled off too, so the grotto never floods or has holes.
     */
    private void skin() {
        for (BlockPos p : carvedList) {
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (air.contains(n.asLong())) continue;
                BlockState s = level.getBlockState(n);
                if (s.is(Blocks.BEDROCK) || s.hasBlockEntity()) continue;
                boolean open = s.isAir() || !s.getFluidState().isEmpty() || s.canBeReplaced();
                if (open) {
                    if (n.getY() >= surfaceAt(n.getX(), n.getZ())) continue;   // open sky above the sinkhole
                    sealed++;
                }
                double v = noise(n.getX() * 0.18, n.getY() * 0.18, n.getZ() * 0.18);
                level.setBlock(n, v > 0.45 ? wallC : (v > -0.1 ? wallA : wallB), 2);
            }
        }
    }

    /**
     * Walking surface of the cavern at a column: the first air block standing on something solid, searched
     * downwards from the cavern's middle. Falls back to {@code fallback} when the column has no floor.
     */
    private int cavernWalk(int x, int cy, int z, int fallback) {
        for (int y = cy; y > cy - RY - 2; y--)
            if (air.contains(BlockPos.asLong(x, y, z)) && !air.contains(BlockPos.asLong(x, y - 1, z))) return y;
        return fallback;
    }

    /**
     * A wide tunnel along x from {@code x0} to {@code x1}: 7 blocks wide, 5 tall with a rounded roof. Its
     * floor slopes evenly from {@code walk0} to {@code walk1} (never more than one block per step) and is laid
     * once all the carving is done. The walking space is kept clear of decoration.
     */
    private void tunnelX(int x0, int x1, int z, int walk0, int walk1) {
        int len = Math.abs(x1 - x0), step = Integer.signum(x1 - x0);
        if (Math.abs(walk1 - walk0) > len) walk1 = walk0 + Integer.signum(walk1 - walk0) * len;
        for (int t = 0; t <= len; t++) {
            int x = x0 + step * t;
            int walk = (int) Math.round(walk0 + (walk1 - walk0) * (t / (double) Math.max(1, len)));
            int wob = (int) Math.round(0.8 * noise(x * 0.15, 7.0, z * 0.1));   // gentle sideways wander
            for (int dz = -3; dz <= 3; dz++) {
                int zz = z + dz + wob;
                int top = Math.abs(dz) == 3 ? 3 : (Math.abs(dz) == 2 ? 4 : 5);
                for (int h = 0; h < top; h++) carve(x, walk + h, zz);
                for (int h = 0; h < Math.min(top, 3); h++) keep(x, walk + h, zz);
                tunnelFloors.add(new BlockPos(x, walk - 1, zz));
            }
        }
    }

    /**
     * Ramp spiralling down the sinkhole wall: 3 blocks wide and 4 tall, one block lower every three blocks
     * along it. Each drop is a stair so the climb back up reads as a path rather than a ledge.
     */
    private record RampCell(int x, int y, int z, boolean stair, Direction up) {}
    private final List<RampCell> ramp = new ArrayList<>();

    private void spiralRamp(int sx, int sz, int shaftR, int topY, int bottomY) {
        List<RampCell> cells = ramp;
        double angle = 0, rr = shaftR - 0.9;
        for (int y = topY - 1; y > bottomY; y--) {
            for (int k = 0; k < 3; k++) {
                Direction up = Direction.getNearest(Math.sin(angle), 0, -Math.cos(angle));
                for (double w = -1.2; w <= 1.2; w += 0.4) {
                    int lx = sx + (int) Math.round(Math.cos(angle) * (rr + w));
                    int lz = sz + (int) Math.round(Math.sin(angle) * (rr + w));
                    cells.add(new RampCell(lx, y, lz, k == 0 && y < topY - 1, up));
                }
                angle += 1.0 / rr;
            }
        }
        for (RampCell c : cells) for (int h = 0; h <= 3; h++) { carve(c.x, c.y + h, c.z); if (h <= 2) keep(c.x, c.y + h, c.z); }
    }

    /** Lays the ramp's floor and stairs; runs after {@link #skin()} so the stone pass does not cover them. */
    private void layRamp() {
        for (RampCell c : ramp) set(c.x, c.y - 1, c.z, ledgeBlock(), 2);
        for (RampCell c : ramp) if (c.stair) set(c.x, c.y, c.z, Blocks.COBBLED_DEEPSLATE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, c.up), 2);
    }

    private BlockState ledgeBlock() {
        return rand.nextInt(4) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    }

    private BlockState polished() {
        return rand.nextInt(5) == 0 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    }

    private void clearAbove(BlockPos p, int h) {
        for (int i = 0; i < h; i++) carve(p.getX(), p.getY() + i, p.getZ());
        set(p.getX(), p.getY() - 1, p.getZ(), polished(), 2);
    }

    /** First air block standing on solid ground, searching down from {@code fromY}. */
    private int floorAt(int x, int fromY, int z) {
        for (int y = fromY + 4; y > fromY - 14; y--) {
            if (isAir(x, y, z) && isSolid(x, y - 1, z) && isAir(x, y + 1, z)) return y;
        }
        return Integer.MIN_VALUE;
    }

    private boolean isAir(int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).isAir();
    }

    private boolean isSolid(int x, int y, int z) {
        BlockState s = level.getBlockState(new BlockPos(x, y, z));
        return !s.isAir() && s.getFluidState().isEmpty() && s.isSolidRender(level, new BlockPos(x, y, z));
    }

    private void set(int x, int y, int z, BlockState state, int flags) {
        if (y <= level.getMinBuildHeight()) return;
        level.setBlock(new BlockPos(x, y, z), state, flags | Block.UPDATE_KNOWN_SHAPE);
    }

    private double noise(double x, double y, double z) {
        return (Math.sin(x * 1.7 + phase[0]) * Math.cos(y * 1.3 + phase[1])
                + Math.sin(z * 1.5 + phase[2]) * Math.cos(x * 0.9 + phase[3])
                + Math.sin(y * 1.1 + phase[4]) * Math.cos(z * 1.2 + phase[5])
                + 0.5 * Math.sin((x + z) * 2.3 + phase[6]) + 0.5 * Math.sin((y - x) * 2.1 + phase[7])) / 4.0;
    }

    private static double sq(double v) {
        return v * v;
    }
}
