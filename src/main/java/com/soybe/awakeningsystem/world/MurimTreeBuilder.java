package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.block.FruitLeavesBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generador procedural de árboles espirituales y antiguos de Murim:
 * 1. Ciruelo Espiritual del Monte Hua (Spirit Plum Blossom Tree)
 * 2. Pino Celestial Milenario del Dao (Celestial Ancient Pine Tree)
 */
public final class MurimTreeBuilder {
    private MurimTreeBuilder() {}

    private static boolean canReplace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(BlockTags.REPLACEABLE)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(Blocks.SNOW);
    }

    private static void setLog(ServerLevel level, BlockPos pos, BlockState logState) {
        if (canReplace(level, pos) || level.getBlockState(pos).is(BlockTags.DIRT)) {
            level.setBlock(pos, logState, 3);
        }
    }

    private static void setPlumLeaves(ServerLevel level, BlockPos pos, RandomSource rand) {
        if (canReplace(level, pos)) {
            boolean hasFruit = rand.nextInt(5) == 0;
            BlockState leafState = SystemBlocks.SPIRIT_PLUM_LEAVES.get().defaultBlockState()
                    .setValue(FruitLeavesBlock.DISTANCE, 1)
                    .setValue(FruitLeavesBlock.PERSISTENT, false)
                    .setValue(FruitLeavesBlock.HAS_FRUIT, hasFruit);
            level.setBlock(pos, leafState, 3);
        }
    }

    private static void setPineLeaves(ServerLevel level, BlockPos pos) {
        if (canReplace(level, pos)) {
            BlockState leafState = SystemBlocks.CELESTIAL_PINE_LEAVES.get().defaultBlockState()
                    .setValue(LeavesBlock.DISTANCE, 1)
                    .setValue(LeavesBlock.PERSISTENT, false);
            level.setBlock(pos, leafState, 3);
        }
    }

    public static boolean generateSpiritPlumTree(ServerLevel level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        BlockPos ground = origin.below();
        if (!fromSapling && !level.getBlockState(ground).is(BlockTags.DIRT)) {
            return false;
        }

        int height = 7 + rand.nextInt(5);
        BlockState log = SystemBlocks.SPIRIT_PLUM_LOG.get().defaultBlockState();

        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
        BlockPos current = origin;
        for (int y = 0; y < height; y++) {
            setLog(level, current, log);
            if (y > 2 && rand.nextFloat() < 0.45F) {
                current = current.relative(lean);
            }
            current = current.above();
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (rand.nextBoolean()) {
                setLog(level, origin.relative(d), log.setValue(RotatedPillarBlock.AXIS, d.getAxis()));
            }
        }

        BlockPos top = current.below();
        int canopyRadius = 3 + rand.nextInt(2);

        for (int dx = -canopyRadius; dx <= canopyRadius; dx++) {
            for (int dz = -canopyRadius; dz <= canopyRadius; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.5D);
                    if (dist <= canopyRadius + 0.5D && rand.nextFloat() < 0.88F) {
                        setPlumLeaves(level, top.offset(dx, dy, dz), rand);
                    }
                }
            }
        }

        if (rand.nextFloat() < 0.65F) {
            BlockPos lanternPos = top.relative(lean, canopyRadius - 1).below();
            if (level.isEmptyBlock(lanternPos) && level.getBlockState(lanternPos.above()).is(BlockTags.LEAVES)) {
                level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 3);
            }
        }

        return true;
    }

    public static boolean generateLargeSpiritPlumTree(ServerLevel level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 15 + rand.nextInt(8);
        BlockState log = SystemBlocks.SPIRIT_PLUM_LOG.get().defaultBlockState();

        for (int y = 0; y < height; y++) {
            setLog(level, origin.offset(0, y, 0), log);
            setLog(level, origin.offset(1, y, 0), log);
            setLog(level, origin.offset(0, y, 1), log);
            setLog(level, origin.offset(1, y, 1), log);
        }

        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction d : dirs) {
            int branchLen = 3 + rand.nextInt(3);
            BlockPos bPos = origin.offset(
                    d == Direction.EAST ? 1 : 0,
                    height - 4,
                    d == Direction.SOUTH ? 1 : 0
            );
            for (int i = 1; i <= branchLen; i++) {
                bPos = bPos.relative(d).above(i % 2 == 0 ? 1 : 0);
                setLog(level, bPos, log.setValue(RotatedPillarBlock.AXIS, d.getAxis()));

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        setPlumLeaves(level, bPos.offset(dx, 1, dz), rand);
                    }
                }

                if (i >= branchLen - 1) {
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            for (int dy = -1; dy <= 2; dy++) {
                                if (dx * dx + dz * dz + dy * dy <= 7) {
                                    setPlumLeaves(level, bPos.offset(dx, dy, dz), rand);
                                }
                            }
                        }
                    }
                }
            }
        }

        BlockPos apex = origin.above(height);
        for (int dx = -4; dx <= 5; dx++) {
            for (int dz = -4; dz <= 5; dz++) {
                for (int dy = -2; dy <= 3; dy++) {
                    if (Math.sqrt(dx * dx + dz * dz + dy * dy * 1.4D) <= 4.5D) {
                        setPlumLeaves(level, apex.offset(dx, dy, dz), rand);
                    }
                }
            }
        }

        return true;
    }

    public static boolean generateCelestialPineTree(ServerLevel level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        BlockPos ground = origin.below();
        if (!fromSapling && !level.getBlockState(ground).is(BlockTags.DIRT)) {
            return false;
        }

        int height = 11 + rand.nextInt(7);
        BlockState log = SystemBlocks.CELESTIAL_PINE_LOG.get().defaultBlockState();

        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
        BlockPos curr = origin;
        for (int y = 0; y < height; y++) {
            setLog(level, curr, log);
            if (y > 4 && y % 3 == 0) {
                curr = curr.relative(lean);
            }
            curr = curr.above();
        }

        BlockPos topOfTrunk = curr.below();

        int levels = 3 + rand.nextInt(2);
        for (int i = 0; i < levels; i++) {
            int padY = height - 2 - (i * 3);
            if (padY < 4) continue;
            int radius = 2 + (i % 2 == 0 ? 1 : 0);
            int shiftBlocks = (padY > 4 ? (padY / 3) : 0);
            BlockPos padCenter = origin.above(padY).relative(lean, shiftBlocks);

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= radius + 1) {
                        setPineLeaves(level, padCenter.offset(dx, 0, dz));
                        if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                            setPineLeaves(level, padCenter.offset(dx, 1, dz));
                        }
                    }
                }
            }
        }

        setPineLeaves(level, topOfTrunk.above());
        setPineLeaves(level, topOfTrunk.above(2));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            setPineLeaves(level, topOfTrunk.relative(d));
            setPineLeaves(level, topOfTrunk.above().relative(d));
        }

        return true;
    }

    public static boolean generateLargeCelestialPineTree(ServerLevel level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 20 + rand.nextInt(10);
        BlockState log = SystemBlocks.CELESTIAL_PINE_LOG.get().defaultBlockState();

        for (int y = 0; y < height; y++) {
            setLog(level, origin.offset(0, y, 0), log);
            setLog(level, origin.offset(1, y, 0), log);
            setLog(level, origin.offset(0, y, 1), log);
            setLog(level, origin.offset(1, y, 1), log);
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            setLog(level, origin.relative(d).above(0), log);
            setLog(level, origin.relative(d).above(1), log);
        }

        int padCount = 4 + rand.nextInt(3);
        int minPadY = Math.max(6, height / 3);
        int maxPadY = height - 4;
        int stepY = Math.max(2, (maxPadY - minPadY) / Math.max(1, padCount));

        for (int i = 0; i < padCount; i++) {
            int padY = minPadY + (i * stepY);
            if (padY > maxPadY) break;

            Direction branchDir = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            int branchLen = 2 + rand.nextInt(3);
            BlockPos bPos = origin.offset(
                    branchDir == Direction.EAST ? 1 : 0,
                    padY,
                    branchDir == Direction.SOUTH ? 1 : 0
            );

            for (int step = 1; step <= branchLen; step++) {
                bPos = bPos.relative(branchDir);
                setLog(level, bPos, log.setValue(RotatedPillarBlock.AXIS, branchDir.getAxis()));
                if (step >= 2) {
                    setPineLeaves(level, bPos.above());
                }
            }

            int r = 2 + rand.nextInt(2);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r + 1) {
                        setPineLeaves(level, bPos.offset(dx, 0, dz));
                        setPineLeaves(level, bPos.offset(dx, 1, dz));
                    }
                }
            }
        }

        for (int dx = -3; dx <= 4; dx++) {
            for (int dz = -3; dz <= 4; dz++) {
                for (int dy = -1; dy <= 3; dy++) {
                    double dist = Math.sqrt((dx - 0.5) * (dx - 0.5) + (dz - 0.5) * (dz - 0.5) + (dy * 1.5) * (dy * 1.5));
                    if (dist <= 3.8D) {
                        setPineLeaves(level, origin.offset(dx, height + dy, dz));
                    }
                }
            }
        }

        return true;
    }
}
