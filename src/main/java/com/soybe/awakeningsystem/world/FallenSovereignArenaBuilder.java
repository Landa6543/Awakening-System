package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Arena del Soberano Caído: coliseo en ruinas pensado para un combate de jefe, sin ningún jefe.
 * Anillo de gradas, muralla con torres y portón, doce pilares con braseros, altar central y
 * trono elevado frente a la entrada. Mide unos 60 x 60 bloques.
 * Todas las coordenadas son relativas al centro; el portón se orienta hacia quien la invoca.
 */
public final class FallenSovereignArenaBuilder {

    private static final int FLOOR_RADIUS = 17;
    private static final int TIER_RINGS = 8;
    private static final int WALL_RADIUS = 26;
    private static final int WALL_HEIGHT = 12;
    private static final int GATE_HALF_WIDTH = 3;
    private static final int GATE_HEIGHT = 7;
    private static final int PILLAR_RADIUS = 14;
    private static final int PILLAR_HEIGHT = 9;
    private static final int THRONE_HALF_WIDTH = 4;
    private static final int THRONE_STEPS = 4;
    private static final int CLEAR_RADIUS = 31;
    private static final int CLEAR_HEIGHT = 26;

    private final ServerLevel level;
    private final BlockPos center;
    private final Rotation rotation;

    private FallenSovereignArenaBuilder(ServerLevel level, BlockPos center, Rotation rotation) {
        this.level = level;
        this.center = center;
        this.rotation = rotation;
    }

    public static void build(ServerLevel level, BlockPos origin, Direction facing) {
        Direction forward = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        Rotation rotation = switch (forward) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.CLOCKWISE_90;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        FallenSovereignArenaBuilder builder = new FallenSovereignArenaBuilder(level, origin.relative(forward, 36), rotation);
        builder.clearSite();
        builder.buildFoundationAndFloor();
        builder.buildTiers();
        builder.buildWall();
        builder.buildTowers();
        builder.buildGate();
        builder.buildPillars();
        builder.buildAltar();
        builder.buildThrone();
    }

    private void set(int x, int y, int z, BlockState state) {
        BlockPos pos = center.offset(new BlockPos(x, y, z).rotate(rotation));
        level.setBlock(pos, state.rotate(rotation), Block.UPDATE_CLIENTS);
    }

    private void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    private static double radius(int x, int z) {
        return Math.sqrt(x * x + z * z);
    }

    /** Ángulo alrededor del centro: 0 es el portón y 180 el trono. */
    private static double angle(int x, int z) {
        double degrees = Math.toDegrees(Math.atan2(x, z));
        return degrees < 0.0D ? degrees + 360.0D : degrees;
    }

    private static boolean nearAngle(int x, int z, double every, double tolerance) {
        double offset = angle(x, z) % every;
        return offset <= tolerance || offset >= every - tolerance;
    }

    private static int noise(int x, int y, int z) {
        return Math.floorMod(x * 73 + y * 31 + z * 19 + x * z, 29);
    }

    private static boolean inGateLane(int x, int z) {
        return z > 0 && Math.abs(x) <= GATE_HALF_WIDTH;
    }

    private static boolean inThroneLane(int x, int z) {
        return z < 0 && Math.abs(x) <= THRONE_HALF_WIDTH;
    }

    private static Direction outward(int x, int z) {
        if (Math.abs(x) > Math.abs(z)) return x > 0 ? Direction.EAST : Direction.WEST;
        return z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private BlockState masonry(int x, int y, int z) {
        int n = noise(x, y, z);
        if (n < 5) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        if (n < 9) return Blocks.DEEPSLATE_TILES.defaultBlockState();
        if (n == 9) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    }

    private void clearSite() {
        for (int x = -CLEAR_RADIUS; x <= CLEAR_RADIUS; x++) {
            for (int z = -CLEAR_RADIUS; z <= CLEAR_RADIUS; z++) {
                if (radius(x, z) > CLEAR_RADIUS) continue;
                for (int y = 0; y <= CLEAR_HEIGHT; y++) {
                    set(x, y, z, Blocks.AIR);
                }
            }
        }
    }

    private void buildFoundationAndFloor() {
        for (int x = -CLEAR_RADIUS; x <= CLEAR_RADIUS; x++) {
            for (int z = -CLEAR_RADIUS; z <= CLEAR_RADIUS; z++) {
                double r = radius(x, z);
                if (r > WALL_RADIUS + 3.0D) continue;
                for (int y = -2; y >= -5; y--) {
                    set(x, y, z, Blocks.COBBLED_DEEPSLATE);
                }
                set(x, -1, z, r <= FLOOR_RADIUS + 0.5D ? floorBlock(x, z, r) : Blocks.POLISHED_DEEPSLATE);
            }
        }
    }

    private Block floorBlock(int x, int z, double r) {
        boolean ring = Math.abs(r - 9.0D) < 0.6D || Math.abs(r - 16.0D) < 0.6D;
        if (Math.abs(r - 9.0D) < 0.6D && nearAngle(x, z, 30.0D, 5.0D)) return SystemBlocks.SIDERITE_ORE.get();
        if (ring) return Blocks.POLISHED_BLACKSTONE_BRICKS;
        if (r > 7.0D && nearAngle(x, z, 30.0D, 2.5D)) return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS;
        int n = noise(x, 0, z);
        if (n == 0) return Blocks.GILDED_BLACKSTONE;
        if (n < 8) return Blocks.DEEPSLATE_TILES;
        if (n < 14) return Blocks.CRACKED_DEEPSLATE_TILES;
        return Blocks.POLISHED_DEEPSLATE;
    }

    /** Gradas que suben hacia la muralla, con asientos de escalera mirando a la arena. */
    private void buildTiers() {
        for (int x = -WALL_RADIUS; x <= WALL_RADIUS; x++) {
            for (int z = -WALL_RADIUS; z <= WALL_RADIUS; z++) {
                int ring = (int) Math.floor(radius(x, z) - FLOOR_RADIUS - 0.5D);
                if (ring < 0 || ring >= TIER_RINGS) continue;
                if (inGateLane(x, z) || inThroneLane(x, z)) continue;

                int top = ring / 2;
                for (int y = 0; y < top; y++) {
                    set(x, y, z, masonry(x, y, z));
                }
                if (ring % 2 == 0) {
                    set(x, top, z, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, outward(x, z)));
                } else {
                    set(x, top, z, Blocks.POLISHED_DEEPSLATE);
                }
                // Faroles en lo alto de las gradas, uno cada 30 grados
                if (ring == TIER_RINGS - 1 && nearAngle(x, z, 30.0D, 1.5D)) {
                    set(x, top + 1, z, Blocks.SOUL_LANTERN);
                }
            }
        }
    }

    private void buildWall() {
        for (int x = -WALL_RADIUS - 2; x <= WALL_RADIUS + 2; x++) {
            for (int z = -WALL_RADIUS - 2; z <= WALL_RADIUS + 2; z++) {
                double r = radius(x, z);
                if (r < WALL_RADIUS - 0.5D || r > WALL_RADIUS + 1.3D) continue;
                for (int y = 0; y <= WALL_HEIGHT; y++) {
                    boolean arrowSlit = y == 8 && r < WALL_RADIUS + 0.5D && nearAngle(x, z, 15.0D, 1.0D);
                    if (y == 6) {
                        set(x, y, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
                    } else if (arrowSlit) {
                        set(x, y, z, Blocks.IRON_BARS);
                    } else {
                        set(x, y, z, masonry(x, y, z));
                    }
                }
                // Almenas alternas, con huecos sueltos para que parezca en ruinas
                boolean merlon = Math.floorMod((int) Math.round(angle(x, z) / 3.0D), 2) == 0;
                if (merlon && noise(x, 1, z) > 5) {
                    set(x, WALL_HEIGHT + 1, z, masonry(x, WALL_HEIGHT + 1, z));
                }
            }
        }
    }

    private void buildTowers() {
        int[][] towers = {{19, 19}, {-19, 19}, {19, -19}, {-19, -19}};
        for (int[] tower : towers) {
            buildTower(tower[0], tower[1], 4, 18);
        }
        buildTower(7, WALL_RADIUS, 3, 16);
        buildTower(-7, WALL_RADIUS, 3, 16);
    }

    private void buildTower(int cx, int cz, int towerRadius, int height) {
        for (int x = -towerRadius; x <= towerRadius; x++) {
            for (int z = -towerRadius; z <= towerRadius; z++) {
                double r = radius(x, z);
                if (r > towerRadius + 0.3D) continue;
                for (int y = 0; y <= height; y++) {
                    set(cx + x, y, cz + z, y == height - 3 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : masonry(cx + x, y, cz + z));
                }
                if (r > towerRadius - 0.7D && Math.floorMod(x + z, 2) == 0) {
                    set(cx + x, height + 1, cz + z, Blocks.DEEPSLATE_BRICK_WALL);
                }
            }
        }
        set(cx, height + 1, cz, Blocks.SOUL_CAMPFIRE);
    }

    private void buildGate() {
        // Pasillo limpio a través de gradas y muralla
        for (int x = -GATE_HALF_WIDTH; x <= GATE_HALF_WIDTH; x++) {
            for (int z = FLOOR_RADIUS; z <= WALL_RADIUS + 5; z++) {
                set(x, -1, z, x == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE);
                int height = Math.abs(x) == GATE_HALF_WIDTH ? GATE_HEIGHT - 1 : GATE_HEIGHT;
                for (int y = 0; y < height; y++) {
                    set(x, y, z, Blocks.AIR);
                }
            }
            // Rastrillo levantado: solo asoman las puntas de hierro
            set(x, GATE_HEIGHT - 1, WALL_RADIUS, Blocks.IRON_BARS);
            set(x, GATE_HEIGHT, WALL_RADIUS, Blocks.CHISELED_DEEPSLATE);
        }
    }

    private void buildPillars() {
        for (int i = 1; i < 12; i++) {
            double radians = Math.toRadians(i * 30.0D);
            int cx = (int) Math.round(Math.sin(radians) * PILLAR_RADIUS);
            int cz = (int) Math.round(Math.cos(radians) * PILLAR_RADIUS);
            boolean ruined = i == 3 || i == 8;
            int height = ruined ? 4 : PILLAR_HEIGHT;

            for (int y = 0; y <= height; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        boolean corner = x != 0 && z != 0;
                        if (corner && y > 1 && y < height) continue;
                        set(cx + x, y, cz + z, y == 0 || y == height ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : masonry(cx + x, y, cz + z));
                    }
                }
            }
            if (ruined) {
                // Escombros del pilar derrumbado, caídos hacia el centro
                int fallX = (int) Math.round(Math.sin(radians) * (PILLAR_RADIUS - 3));
                int fallZ = (int) Math.round(Math.cos(radians) * (PILLAR_RADIUS - 3));
                set(fallX, 0, fallZ, Blocks.CRACKED_DEEPSLATE_BRICKS);
                set(fallX + 1, 0, fallZ, Blocks.COBBLED_DEEPSLATE);
                set(fallX, 0, fallZ + 1, Blocks.COBBLED_DEEPSLATE_SLAB);
            } else {
                set(cx, height + 1, cz, Blocks.SOUL_CAMPFIRE);
            }
        }
    }

    private void buildAltar() {
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                double r = radius(x, z);
                if (r <= 7.3D) set(x, 0, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
                if (r <= 5.3D) set(x, 1, z, Blocks.POLISHED_DEEPSLATE);
                if (r <= 3.3D) set(x, 2, z, Blocks.DEEPSLATE_TILES);
            }
        }
        set(0, 3, 0, SystemBlocks.SIDERITE_BLOCK.get());
        set(0, 4, 0, Blocks.END_ROD);

        int[][] obelisks = {{4, 4}, {-4, 4}, {4, -4}, {-4, -4}};
        for (int[] obelisk : obelisks) {
            for (int y = 2; y <= 6; y++) {
                set(obelisk[0], y, obelisk[1], y == 2 ? Blocks.CHISELED_DEEPSLATE : Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
            set(obelisk[0], 7, obelisk[1], Blocks.AMETHYST_BLOCK);
        }
    }

    private void buildThrone() {
        // Escalinata que sube desde la arena hasta el trono
        int stairStart = FLOOR_RADIUS + 1;
        for (int x = -THRONE_HALF_WIDTH; x <= THRONE_HALF_WIDTH; x++) {
            for (int step = 0; step < THRONE_STEPS; step++) {
                int z = -(stairStart + step * 2);
                for (int y = 0; y < step; y++) {
                    set(x, y, z, masonry(x, y, z));
                    set(x, y, z - 1, masonry(x, y, z - 1));
                }
                set(x, step, z, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                set(x, step, z - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
        }

        int seatY = THRONE_STEPS;
        int seatZ = -(WALL_RADIUS - 1);
        for (int x = -THRONE_HALF_WIDTH; x <= THRONE_HALF_WIDTH; x++) {
            set(x, seatY - 1, seatZ, Blocks.POLISHED_BLACKSTONE_BRICKS);
            set(x, seatY - 1, seatZ - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
        }

        set(0, seatY, seatZ, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        set(-1, seatY, seatZ, Blocks.POLISHED_BLACKSTONE_WALL);
        set(1, seatY, seatZ, Blocks.POLISHED_BLACKSTONE_WALL);
        for (int y = seatY; y <= seatY + 5; y++) {
            set(0, y, seatZ - 1, y == seatY + 5 ? SystemBlocks.SIDERITE_BLOCK.get().defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
            set(-1, y, seatZ - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
            set(1, y, seatZ - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
        }
        set(-3, seatY, seatZ, Blocks.SOUL_CAMPFIRE);
        set(3, seatY, seatZ, Blocks.SOUL_CAMPFIRE);

        set(-3, seatY, seatZ + 1, Blocks.CHEST);
        BlockPos chestPos = center.offset(new BlockPos(-3, seatY, seatZ + 1).rotate(rotation));
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(11, new ItemStack(SystemBlocks.SIDERITE_BLOCK.get()));
            chest.setItem(13, new ItemStack(Items.GOLDEN_APPLE, 2));
            chest.setItem(15, new ItemStack(Items.ENDER_PEARL, 4));
        }
    }
}
