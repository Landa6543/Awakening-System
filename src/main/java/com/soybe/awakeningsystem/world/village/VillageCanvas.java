package com.soybe.awakeningsystem.world.village;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Map;

/**
 * Where the village is built: a level, with its ground as it was before building (so later buildings do not
 * stand on earlier ones) and without neighbour updates, so doors, beds and hanging lanterns placed one half or
 * one block at a time are never knocked down halfway.
 */
final class VillageCanvas {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    final ServerLevel level;
    private final Map<Long, Integer> ground = new HashMap<>();

    VillageCanvas(ServerLevel level) {
        this.level = level;
    }

    BlockState get(BlockPos p) {
        return level.getBlockState(p);
    }

    void set(BlockPos p, BlockState s) {
        level.setBlock(p, s, FLAGS);
        Block b = s.getBlock();
        if (b instanceof net.minecraft.world.level.block.CrossCollisionBlock || b instanceof net.minecraft.world.level.block.WallBlock
                || b instanceof net.minecraft.world.level.block.FenceGateBlock) joined.add(p.immutable());
    }

    /** Fences, panes, bars, walls and gates placed so far: they get joined to their neighbours at the end. */
    private final java.util.Set<BlockPos> joined = new java.util.LinkedHashSet<>();

    /**
     * Joins every fence, pane, bar, wall and gate to what is around it, the way the game does when a player
     * places them, once everything stands (placing them one by one without updates leaves them unjoined).
     */
    void joinFences() {
        for (BlockPos p : joined) {
            BlockState s = level.getBlockState(p);
            Block b = s.getBlock();
            if (!(b instanceof net.minecraft.world.level.block.CrossCollisionBlock || b instanceof net.minecraft.world.level.block.WallBlock
                    || b instanceof net.minecraft.world.level.block.FenceGateBlock)) continue;
            BlockState t = s;
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                if (d == net.minecraft.core.Direction.DOWN) continue;
                BlockPos n = p.relative(d);
                t = t.updateShape(d, level.getBlockState(n), level, p, n);
            }
            if (t != s) level.setBlock(p, t, FLAGS);
        }
    }

    private final java.util.List<java.util.Map.Entry<BlockPos, String>> loot = new java.util.ArrayList<>();

    /** Fill this chest with the village loot of {@code kind} ("house", "smith", "temple") once everything stands. */
    void lootLater(BlockPos p, String kind) {
        loot.add(java.util.Map.entry(p.immutable(), kind));
    }

    void fillLoot() {
        for (var e : loot) {
            var table = switch (e.getValue()) {
                case "smith" -> net.minecraft.world.level.storage.loot.BuiltInLootTables.VILLAGE_WEAPONSMITH;
                case "temple" -> net.minecraft.world.level.storage.loot.BuiltInLootTables.VILLAGE_TEMPLE;
                default -> net.minecraft.world.level.storage.loot.BuiltInLootTables.VILLAGE_PLAINS_HOUSE;
            };
            net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, level.random, e.getKey(), table);
        }
    }

    /** The y of the top block of the natural ground at x, z (trees, plants and snow are not ground). */
    int ground(int x, int z) {
        return ground.computeIfAbsent(((long) x << 32) ^ (z & 0xffffffffL), k -> {
            // some of the mod's own world generators do not fill the heightmaps, so when it looks empty, search down from the sky
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            if (y <= level.getMinBuildHeight() + 1) y = Math.min(level.getMaxBuildHeight() - 1, level.getMinBuildHeight() + level.getLogicalHeight() - 1);
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, y, z);
            while (y > level.getMinBuildHeight()) {
                m.setY(y);
                BlockState s = level.getBlockState(m);
                boolean plant = s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || s.isAir() || !s.blocksMotion()
                        || s.is(BlockTags.REPLACEABLE) || s.is(BlockTags.FLOWERS) || s.is(BlockTags.SAPLINGS);
                if (!plant) break;
                y--;
            }
            return y;
        });
    }

    /** True for what may be cleared away when building: plants, trees, snow and loose natural ground. */
    static boolean clearable(BlockState s) {
        return !s.isAir() && (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || s.is(BlockTags.REPLACEABLE) || s.is(BlockTags.FLOWERS)
                || s.is(BlockTags.SAPLINGS) || s.is(BlockTags.DIRT) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.SAND)
                || !s.blocksMotion() || s.is(BlockTags.SNOW) || s.is(BlockTags.TERRACOTTA));
    }
}
