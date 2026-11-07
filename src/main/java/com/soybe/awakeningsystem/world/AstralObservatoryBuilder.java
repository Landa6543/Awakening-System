package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Observatorio Astral: torre circular de dos plantas con escalera en espiral,
 * biblioteca, mirador con cúpula de cristal y telescopio. Mide unos 21 x 21 bloques y 19 de alto.
 * Todas las coordenadas son relativas al centro; la puerta se orienta hacia quien la invoca.
 */
public final class AstralObservatoryBuilder {

    private static final int TOWER_RADIUS = 5;
    private static final int PLAZA_RADIUS = 9;
    private static final int UPPER_FLOOR_Y = 5;
    private static final int DECK_Y = 11;
    private static final int CLEAR_RADIUS = 11;
    private static final int CLEAR_HEIGHT = 22;

    // La escalera da casi una vuelta entera pegada a la pared, empezando junto a la puerta
    private static final double STAIR_START_DEGREES = 25.0D;
    private static final double STAIR_STEP_DEGREES = 27.0D;
    private static final int STAIR_STEPS = 12;

    private final ServerLevel level;
    private final BlockPos center;
    private final Rotation rotation;

    private AstralObservatoryBuilder(ServerLevel level, BlockPos center, Rotation rotation) {
        this.level = level;
        this.center = center;
        this.rotation = rotation;
    }

    /** Construye el observatorio frente a la posición dada, con la puerta mirando hacia ella. */
    public static void build(ServerLevel level, BlockPos origin, Direction facing) {
        Direction forward = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        Rotation rotation = switch (forward) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.CLOCKWISE_90;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        AstralObservatoryBuilder builder = new AstralObservatoryBuilder(level, origin.relative(forward, 18), rotation);
        builder.clearSite();
        builder.buildPlaza();
        builder.buildTower();
        builder.buildUpperFloor();
        builder.buildStairs();
        builder.buildGroundFloor();
        builder.buildLibrary();
        builder.buildDeck();
        builder.buildDome();
        builder.buildTelescope();
        builder.buildBeacons();
    }

    private void set(int x, int y, int z, BlockState state) {
        BlockPos pos = center.offset(new BlockPos(x, y, z).rotate(rotation));
        // Sin avisar a los vecinos: así las piezas colgantes no se caen mientras se construye
        level.setBlock(pos, state.rotate(rotation), Block.UPDATE_CLIENTS);
    }

    private void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    private static double radius(int x, int z) {
        return Math.sqrt(x * x + z * z);
    }

    /** Ángulo alrededor de la torre: 0 es la puerta y crece en sentido horario visto desde arriba. */
    private static double angle(int x, int z) {
        double degrees = Math.toDegrees(Math.atan2(x, z));
        return degrees < 0.0D ? degrees + 360.0D : degrees;
    }

    private static boolean nearAngle(int x, int z, double every, double tolerance) {
        double offset = angle(x, z) % every;
        return offset <= tolerance || offset >= every - tolerance;
    }

    private static boolean onRing(int x, int z, double ringRadius) {
        double r = radius(x, z);
        return r > ringRadius - 0.7D && r <= ringRadius + 0.3D;
    }

    private BlockState masonry(int x, int y, int z) {
        int noise = Math.floorMod(x * 31 + y * 17 + z * 13, 7);
        if (noise == 0) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        if (noise == 3) return Blocks.DEEPSLATE_TILES.defaultBlockState();
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

    private void buildPlaza() {
        for (int x = -PLAZA_RADIUS; x <= PLAZA_RADIUS; x++) {
            for (int z = -PLAZA_RADIUS; z <= PLAZA_RADIUS; z++) {
                double r = radius(x, z);
                if (r > PLAZA_RADIUS + 0.3D) continue;

                Block floor;
                if (onRing(x, z, PLAZA_RADIUS)) {
                    floor = Blocks.CHISELED_DEEPSLATE;
                } else if (Math.floorMod((int) Math.round(r), 2) == 0) {
                    floor = Blocks.POLISHED_DEEPSLATE;
                } else {
                    floor = Blocks.DEEPSLATE_TILES;
                }
                set(x, -1, z, floor);
                // Cimientos para que no quede flotando sobre terreno irregular
                for (int y = -2; y >= -5; y--) {
                    set(x, y, z, Blocks.COBBLED_DEEPSLATE);
                }
            }
        }

        // Camino de entrada hasta el borde de la plaza
        for (int z = TOWER_RADIUS + 1; z <= PLAZA_RADIUS; z++) {
            for (int x = -1; x <= 1; x++) {
                set(x, -1, z, x == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE);
            }
        }
    }

    private void buildTower() {
        for (int y = 0; y < DECK_Y; y++) {
            for (int x = -TOWER_RADIUS; x <= TOWER_RADIUS; x++) {
                for (int z = -TOWER_RADIUS; z <= TOWER_RADIUS; z++) {
                    if (!onRing(x, z, TOWER_RADIUS)) continue;

                    boolean band = y == 0 || y == UPPER_FLOOR_Y;
                    boolean window = (y == 2 || y == 3 || y == 7 || y == 8)
                            && nearAngle(x, z, 45.0D, 7.0D) && angle(x, z) > 20.0D && angle(x, z) < 340.0D;
                    if (window) {
                        set(x, y, z, Blocks.LIGHT_BLUE_STAINED_GLASS);
                    } else if (band) {
                        set(x, y, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
                    } else {
                        set(x, y, z, masonry(x, y, z));
                    }
                }
            }
        }

        // Puerta con dintel de piedra tallada
        BlockState door = Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        set(0, 0, TOWER_RADIUS, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(0, 1, TOWER_RADIUS, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        set(0, 2, TOWER_RADIUS, Blocks.CHISELED_DEEPSLATE);
    }

    private void buildUpperFloor() {
        for (int x = -TOWER_RADIUS; x <= TOWER_RADIUS; x++) {
            for (int z = -TOWER_RADIUS; z <= TOWER_RADIUS; z++) {
                double r = radius(x, z);
                if (r > TOWER_RADIUS - 0.7D) continue;
                boolean stairOpening = r >= 2.6D && angle(x, z) >= 187.0D && angle(x, z) < 325.0D;
                if (!stairOpening) {
                    set(x, UPPER_FLOOR_Y, z, Blocks.DARK_OAK_PLANKS);
                }
            }
        }
    }

    private void buildStairs() {
        BlockState bottom = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState top = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        for (int x = -TOWER_RADIUS; x <= TOWER_RADIUS; x++) {
            for (int z = -TOWER_RADIUS; z <= TOWER_RADIUS; z++) {
                double r = radius(x, z);
                if (r < 2.6D || r > TOWER_RADIUS - 0.7D) continue;
                int step = (int) ((angle(x, z) - STAIR_START_DEGREES) / STAIR_STEP_DEGREES);
                if (angle(x, z) < STAIR_START_DEGREES || step >= STAIR_STEPS) continue;
                set(x, step / 2, z, step % 2 == 0 ? bottom : top);
            }
        }
    }

    private void buildGroundFloor() {
        // Suelo interior en anillos con el núcleo de siderita en el centro
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if (radius(x, z) > 4.3D) continue;
                set(x, -1, z, radius(x, z) < 1.5D ? Blocks.CRYING_OBSIDIAN : Blocks.POLISHED_DEEPSLATE);
            }
        }
        set(0, 0, 0, Blocks.POLISHED_BLACKSTONE_BRICKS);
        set(0, 1, 0, SystemBlocks.SIDERITE_BLOCK.get());
        set(0, 2, 0, Blocks.END_ROD);

        set(0, 4, 0, Blocks.CHAIN);
        set(0, 3, 0, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

        // Almacén bajo el tramo alto de la escalera
        set(-3, 0, 1, Blocks.BARREL);
        set(-3, 0, 0, Blocks.BARREL);
        set(-3, 1, 0, Blocks.BARREL);
        set(-3, 0, -1, Blocks.CHEST);
        if (level.getBlockEntity(center.offset(new BlockPos(-3, 0, -1).rotate(rotation))) instanceof ChestBlockEntity chest) {
            chest.setItem(11, new ItemStack(Items.SPYGLASS));
            chest.setItem(13, new ItemStack(SystemBlocks.SIDERITE_ORE.get(), 3));
            chest.setItem(15, new ItemStack(Items.MAP));
        }
    }

    private void buildLibrary() {
        int standY = UPPER_FLOOR_Y + 1;
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                double r = radius(x, z);
                double a = angle(x, z);
                boolean ladderGap = x >= 3 && Math.abs(z) <= 1;
                if (r >= 3.6D && r <= 4.4D && a >= 35.0D && a <= 170.0D && !ladderGap) {
                    set(x, standY, z, Blocks.BOOKSHELF);
                    set(x, standY + 1, z, Blocks.BOOKSHELF);
                    set(x, standY + 2, z, Blocks.CHISELED_BOOKSHELF);
                }
                // Baranda alrededor del hueco de la escalera, dejando libre la llegada
                if (r >= 2.0D && r < 2.9D && a >= 190.0D && a <= 295.0D) {
                    set(x, standY, z, Blocks.DARK_OAK_FENCE);
                }
            }
        }

        set(0, standY, 0, Blocks.ENCHANTING_TABLE);
        set(1, standY, 2, Blocks.LECTERN);
        set(-1, standY, 2, Blocks.CARTOGRAPHY_TABLE);

        set(0, 10, 0, Blocks.CHAIN);
        set(0, 9, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

        // Escalera de mano hacia el mirador, apoyada en la pared
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
        for (int y = standY; y <= DECK_Y; y++) {
            set(4, y, 0, ladder);
        }
    }

    private void buildDeck() {
        int deckRadius = TOWER_RADIUS + 1;
        for (int x = -deckRadius; x <= deckRadius; x++) {
            for (int z = -deckRadius; z <= deckRadius; z++) {
                if (radius(x, z) > deckRadius + 0.3D) continue;
                if (x == 4 && z == 0) continue; // salida de la escalera de mano
                set(x, DECK_Y, z, onRing(x, z, deckRadius) ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.DEEPSLATE_TILES);
                if (onRing(x, z, deckRadius) && Math.floorMod((int) Math.round(angle(x, z) / 15.0D), 2) == 0) {
                    set(x, DECK_Y + 1, z, Blocks.DEEPSLATE_BRICK_WALL);
                }
            }
        }
    }

    private void buildDome() {
        double domeRadius = 4.6D;
        int base = DECK_Y + 1;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = 0; y <= 5; y++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance < domeRadius - 0.6D || distance > domeRadius + 0.4D) continue;
                    if (x == 4 && z == 0 && y <= 1) continue; // hueco para subir desde la escalera
                    boolean rib = nearAngle(x, z, 45.0D, 6.0D) || y == 0;
                    set(x, base + y, z, rib ? Blocks.WAXED_OXIDIZED_CUT_COPPER : Blocks.CYAN_STAINED_GLASS);
                }
            }
        }
        set(0, base + 5, 0, Blocks.LIGHTNING_ROD);
    }

    private void buildTelescope() {
        int base = DECK_Y + 1;
        set(0, base, 0, Blocks.POLISHED_BLACKSTONE_BRICKS);
        set(0, base + 1, 0, Blocks.STRIPPED_DARK_OAK_LOG);

        // Tubo inclinado que atraviesa la cúpula apuntando al cielo
        for (int i = 0; i <= 5; i++) {
            int x = -(i + 1) / 2;
            int y = base + 2 + i * 3 / 5;
            int z = -i;
            set(x, y, z, i == 5 ? Blocks.AMETHYST_BLOCK : Blocks.WAXED_CUT_COPPER);
        }
    }

    /** Cuatro faroles en diagonal alrededor de la plaza. */
    private void buildBeacons() {
        int[][] corners = {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}};
        for (int[] corner : corners) {
            int x = corner[0];
            int z = corner[1];
            set(x, 0, z, Blocks.CHISELED_DEEPSLATE);
            set(x, 1, z, Blocks.DEEPSLATE_BRICK_WALL);
            set(x, 2, z, Blocks.DEEPSLATE_BRICK_WALL);
            set(x, 3, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
            set(x, 4, z, Blocks.SOUL_LANTERN);
        }
    }
}
