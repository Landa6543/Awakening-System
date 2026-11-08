package com.soybe.awakeningsystem.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Cámaras de Desafío del Sistema. Bajo tierra: una sala central de tuff y cobre con su generador,
 * y tres salas temáticas (Murim al este, Sombras al oeste, Elemental al norte), cada una con su
 * generador y su Bóveda Rúnica. Una escalera sube hacia el sur hasta un arco rúnico en superficie.
 * Todas las coordenadas son relativas al centro del suelo de la sala central.
 */
public final class TrialChamberBuilder {

    private static final int DEPTH = 24;
    private static final int HALL = 11;          // medio ancho interior de la sala central
    private static final int HALL_H = 10;
    private static final int ROOM = 7;           // medio ancho interior de las salas temáticas
    private static final int ROOM_H = 8;
    private static final int ROOM_OFFSET = 29;   // distancia del centro de la sala central a cada sala

    private final ServerLevel level;
    private final int ox, oy, oz;

    private TrialChamberBuilder(ServerLevel level, int ox, int oy, int oz) {
        this.level = level;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
    }

    /**
     * Construye las cámaras bajo {@code surface} y devuelve la posición de la entrada en superficie.
     */
    public static BlockPos build(ServerLevel level, BlockPos surface) {
        int floorY = Math.max(level.getMinBuildHeight() + 8, surface.getY() - DEPTH);
        TrialChamberBuilder b = new TrialChamberBuilder(level, surface.getX(), floorY, surface.getZ());
        b.buildHall();
        b.buildMurimRoom(ROOM_OFFSET, 0);
        b.buildShadowRoom(-ROOM_OFFSET, 0);
        b.buildElementalRoom(0, -ROOM_OFFSET);
        // los pasillos van al final: abren las puertas en las paredes ya construidas
        int near = HALL + 1, far = ROOM_OFFSET - ROOM - 1;
        b.buildCorridor(near, far, true);
        b.buildCorridor(-far, -near, true);
        b.buildCorridor(-far, -near, false);
        return b.buildStairsAndEntrance();
    }

    // ------------------------------------------------------------------ helpers

    private BlockPos at(int x, int y, int z) {
        return new BlockPos(ox + x, oy + y, oz + z);
    }

    private void set(int x, int y, int z, BlockState state) {
        level.setBlock(at(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    private void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    /** Capa exterior de sellado: solo rellena huecos y líquidos, para que no entre agua ni lava. */
    private void seal(int x, int y, int z) {
        BlockState current = level.getBlockState(at(x, y, z));
        if (current.isAir() || !current.getFluidState().isEmpty() || current.canBeReplaced()) {
            set(x, y, z, Blocks.DEEPSLATE_BRICKS);
        }
    }

    /**
     * Vacía un volumen rectangular y lo rodea con suelo, paredes y techo, más una capa de sellado.
     * El interior va de (x1, 0, z1) a (x2, height - 1, z2).
     */
    private void shell(int x1, int z1, int x2, int z2, int height, BlockState floor, BlockState wall, BlockState ceiling) {
        for (int x = x1 - 2; x <= x2 + 2; x++) {
            for (int z = z1 - 2; z <= z2 + 2; z++) {
                for (int y = -2; y <= height + 1; y++) {
                    boolean outer = x == x1 - 2 || x == x2 + 2 || z == z1 - 2 || z == z2 + 2 || y == -2 || y == height + 1;
                    boolean inner = x == x1 - 1 || x == x2 + 1 || z == z1 - 1 || z == z2 + 1 || y == -1 || y == height;
                    if (outer) seal(x, y, z);
                    else if (inner) set(x, y, z, y == -1 ? floor : y == height ? ceiling : wall);
                    else set(x, y, z, Blocks.AIR);
                }
            }
        }
    }

    private void pillar(int x, int z, int height, BlockState body, BlockState cap) {
        for (int y = 0; y < height; y++) set(x, y, z, y == 0 || y == height - 1 ? cap : body);
    }

    private BlockState lit(Block bulb) {
        return bulb.defaultBlockState().setValue(BlockStateProperties.LIT, true);
    }

    private BlockState hangingLantern(Block lantern) {
        return lantern.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    private void placeSpawner(int x, int y, int z, int theme) {
        set(x, y, z, TrialRegistry.SHADOW_TRIAL_SPAWNER.get().defaultBlockState());
        if (level.getBlockEntity(at(x, y, z)) instanceof TrialSpawnerBlockEntity be) be.setTheme(theme);
    }

    private void placeVault(int x, int y, int z, Direction facing) {
        set(x, y, z, TrialRegistry.AWAKENING_VAULT.get().defaultBlockState().setValue(TrialVaultBlock.FACING, facing));
    }

    // ------------------------------------------------------------------ central hall

    private void buildHall() {
        BlockState tuffBricks = Blocks.TUFF_BRICKS.defaultBlockState();
        shell(-HALL, -HALL, HALL, HALL, HALL_H, Blocks.POLISHED_TUFF.defaultBlockState(), tuffBricks,
                Blocks.POLISHED_TUFF.defaultBlockState());

        // decorated walls: chiselled base band, copper frieze at mid height
        for (int i = -HALL; i <= HALL; i++) {
            for (int[] side : new int[][]{{i, -HALL - 1}, {i, HALL + 1}, {-HALL - 1, i}, {HALL + 1, i}}) {
                set(side[0], 0, side[1], Blocks.CHISELED_TUFF_BRICKS);
                set(side[0], 6, side[1], Blocks.WAXED_OXIDIZED_CUT_COPPER);
                if (Math.floorMod(i, 6) == 3) set(side[0], 3, side[1], lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
            }
        }
        // floor: border ring and copper inlay lines toward the doors
        for (int x = -HALL; x <= HALL; x++) {
            for (int z = -HALL; z <= HALL; z++) {
                int edge = Math.min(HALL - Math.abs(x), HALL - Math.abs(z));
                if (edge == 0) set(x, -1, z, Blocks.TUFF_BRICKS);
                else if ((x == 0 || z == 0) && Math.abs(x) + Math.abs(z) > 4) set(x, -1, z, Blocks.WAXED_OXIDIZED_CUT_COPPER);
            }
        }
        // ceiling lights in a grid
        for (int x = -6; x <= 6; x += 6) for (int z = -6; z <= 6; z += 6) set(x, HALL_H, z, Blocks.SEA_LANTERN);

        // four great pillars with a lit bulb at mid height
        for (int[] p : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}}) {
            pillar(p[0], p[1], HALL_H, Blocks.POLISHED_TUFF.defaultBlockState(), Blocks.CHISELED_TUFF.defaultBlockState());
            set(p[0], 4, p[1], lit(Blocks.WAXED_COPPER_BULB));
        }

        // raised dais for the spawner
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            boolean rim = Math.abs(x) == 3 || Math.abs(z) == 3;
            set(x, 0, z, rim ? Blocks.CHISELED_TUFF_BRICKS : Blocks.POLISHED_TUFF);
        }
        set(0, 0, 0, Blocks.WAXED_OXIDIZED_COPPER);
        placeSpawner(0, 1, 0, TrialSpawnerBlockEntity.THEME_HALL);

        // vault alcove on the north-west wall, a lit grate in the matching alcove on the north-east
        placeVault(-8, 0, -HALL, Direction.SOUTH);
        set(-8, 1, -HALL, Blocks.CHISELED_TUFF_BRICKS);
        set(8, 0, -HALL, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
        set(8, 1, -HALL, lit(Blocks.WAXED_COPPER_BULB));
    }

    // ------------------------------------------------------------------ corridors

    /**
     * Pasillo de 5 de ancho y 5 de alto sobre el eje X (o Z) que pasa por el centro, de {@code from}
     * a {@code to}, ambos incluidos. Los extremos son las paredes de las salas: ahí abre la puerta.
     */
    private void buildCorridor(int from, int to, boolean alongX) {
        for (int i = from; i <= to; i++) {
            for (int w = -4; w <= 4; w++) {
                for (int y = -2; y <= 6; y++) {
                    int x = alongX ? i : w, z = alongX ? w : i;
                    boolean outer = Math.abs(w) == 4 || y == -2 || y == 6;
                    boolean inner = Math.abs(w) == 3 || y == -1 || y == 5;
                    if (outer) seal(x, y, z);
                    else if (inner) {
                        BlockState s = y == -1 ? (w == 0 ? Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState() : Blocks.POLISHED_TUFF.defaultBlockState())
                                : y == 5 ? (w == 0 && Math.floorMod(i, 4) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.TUFF_BRICKS.defaultBlockState())
                                : Blocks.TUFF_BRICKS.defaultBlockState();
                        set(x, y, z, s);
                    } else set(x, y, z, Blocks.AIR);
                }
            }
        }
    }

    // ------------------------------------------------------------------ themed rooms

    private void buildMurimRoom(int cx, int cz) {
        shell(cx - ROOM, cz - ROOM, cx + ROOM, cz + ROOM, ROOM_H, Blocks.BAMBOO_MOSAIC.defaultBlockState(),
                Blocks.BAMBOO_BLOCK.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        // dark wood frame: corner and wall posts, a beam ring under the ceiling
        for (int i = -ROOM - 1; i <= ROOM + 1; i += 4) {
            for (int y = 0; y < ROOM_H; y++) {
                set(cx + i, y, cz - ROOM - 1, log);
                set(cx + i, y, cz + ROOM + 1, log);
                set(cx - ROOM - 1, y, cz + i, log);
                set(cx + ROOM + 1, y, cz + i, log);
            }
        }
        for (int i = -ROOM; i <= ROOM; i++) {
            set(cx + i, ROOM_H - 1, cz - ROOM, Blocks.DARK_OAK_PLANKS);
            set(cx + i, ROOM_H - 1, cz + ROOM, Blocks.DARK_OAK_PLANKS);
            set(cx - ROOM, ROOM_H - 1, cz + i, Blocks.DARK_OAK_PLANKS);
            set(cx + ROOM, ROOM_H - 1, cz + i, Blocks.DARK_OAK_PLANKS);
        }
        // spruce border on the floor
        for (int x = -ROOM; x <= ROOM; x++) for (int z = -ROOM; z <= ROOM; z++) {
            if (Math.abs(x) == ROOM || Math.abs(z) == ROOM) set(cx + x, -1, cz + z, Blocks.SPRUCE_PLANKS);
        }
        // hanging lanterns
        for (int x = -4; x <= 4; x += 4) for (int z = -4; z <= 4; z += 4) {
            if (x == 0 && z == 0) continue;
            set(cx + x, ROOM_H - 1, cz + z, hangingLantern(Blocks.LANTERN));
        }
        // training ground: spawner on a dark wood platform, two training dummies at the back
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) set(cx + x, 0, cz + z, Blocks.STRIPPED_DARK_OAK_WOOD);
        placeSpawner(cx, 1, cz, TrialSpawnerBlockEntity.THEME_MURIM);
        dummy(cx + 5, cz - 5);
        dummy(cx + 5, cz + 5);
        placeVault(cx + ROOM, 0, cz, Direction.WEST);
        set(cx + ROOM, 1, cz, Blocks.CHISELED_BOOKSHELF);
    }

    private void dummy(int x, int z) {
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand == null) return;
        BlockPos p = at(x, 0, z);
        stand.moveTo(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, 270.0F, 0.0F);
        stand.setShowArms(true);
        level.addFreshEntity(stand);
    }

    private void buildShadowRoom(int cx, int cz) {
        shell(cx - ROOM, cz - ROOM, cx + ROOM, cz + ROOM, ROOM_H, Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState());
        // crying obsidian veins in the wall posts, deepslate tile border on the floor
        for (int i = -ROOM - 1; i <= ROOM + 1; i += 4) {
            for (int y = 0; y < ROOM_H; y++) {
                BlockState post = y % 3 == 1 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
                set(cx + i, y, cz - ROOM - 1, post);
                set(cx + i, y, cz + ROOM + 1, post);
                set(cx - ROOM - 1, y, cz + i, post);
            }
        }
        for (int x = -ROOM; x <= ROOM; x++) for (int z = -ROOM; z <= ROOM; z++) {
            if (Math.abs(x) == ROOM || Math.abs(z) == ROOM) set(cx + x, -1, cz + z, Blocks.DEEPSLATE_TILES);
        }
        for (int x = -4; x <= 4; x += 4) for (int z = -4; z <= 4; z += 4) {
            if (x == 0 && z == 0) continue;
            set(cx + x, ROOM_H - 1, cz + z, hangingLantern(Blocks.SOUL_LANTERN));
        }
        // obsidian ring around the spawner
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if (Math.abs(x) == 2 || Math.abs(z) == 2) set(cx + x, -1, cz + z, Blocks.OBSIDIAN);
        }
        set(cx, 0, cz, Blocks.GILDED_BLACKSTONE);
        placeSpawner(cx, 1, cz, TrialSpawnerBlockEntity.THEME_SHADOW);
        placeVault(cx - ROOM, 0, cz, Direction.EAST);
        set(cx - ROOM, 1, cz, Blocks.CHISELED_POLISHED_BLACKSTONE);
    }

    private void buildElementalRoom(int cx, int cz) {
        shell(cx - ROOM, cz - ROOM, cx + ROOM, cz + ROOM, ROOM_H, Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // three elemental columns on the back wall: fire, ice and wind
        int back = cz - ROOM - 1;
        for (int y = 0; y < ROOM_H; y++) {
            set(cx - 4, y, back, Blocks.MAGMA_BLOCK);
            set(cx + 4, y, back, Blocks.BLUE_ICE);
            set(cx, y, back, y % 2 == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.CYAN_STAINED_GLASS.defaultBlockState());
        }
        // matching froglights in the ceiling above each element
        set(cx - 4, ROOM_H, cz - 4, Blocks.OCHRE_FROGLIGHT);
        set(cx + 4, ROOM_H, cz - 4, Blocks.PEARLESCENT_FROGLIGHT);
        set(cx, ROOM_H, cz - 4, Blocks.VERDANT_FROGLIGHT);
        set(cx - 4, ROOM_H, cz + 4, Blocks.OCHRE_FROGLIGHT);
        set(cx + 4, ROOM_H, cz + 4, Blocks.PEARLESCENT_FROGLIGHT);
        set(cx, ROOM_H, cz + 4, Blocks.VERDANT_FROGLIGHT);
        // smooth quartz floor with a calcite border
        for (int x = -ROOM; x <= ROOM; x++) for (int z = -ROOM; z <= ROOM; z++) {
            boolean border = Math.abs(x) == ROOM || Math.abs(z) == ROOM;
            set(cx + x, -1, cz + z, border ? Blocks.CALCITE : Blocks.SMOOTH_QUARTZ);
        }
        set(cx, 0, cz, Blocks.LODESTONE);
        placeSpawner(cx, 1, cz, TrialSpawnerBlockEntity.THEME_ELEMENTAL);
        placeVault(cx - 2, 0, cz - ROOM, Direction.SOUTH);
        set(cx - 2, 1, cz - ROOM, Blocks.CHISELED_DEEPSLATE);
    }

    // ------------------------------------------------------------------ stairs and surface entrance

    /** Escalera que sube hacia el sur desde la sala central hasta la superficie. Devuelve la entrada. */
    private BlockPos buildStairsAndEntrance() {
        // opening in the south wall of the hall
        for (int x = -1; x <= 1; x++) for (int y = 0; y < 4; y++) set(x, y, HALL + 1, Blocks.AIR);

        BlockState step = Blocks.TUFF_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        int k = 0;
        int z = HALL + 2;
        while (k < 64) {
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox, oz + z) - oy;
            if (k >= surfaceY) break;
            for (int x = -2; x <= 2; x++) {
                for (int y = k - 1; y <= k + 4; y++) {
                    boolean wall = Math.abs(x) == 2 || y == k + 4;
                    BlockState current = level.getBlockState(at(x, y, z));
                    if (wall) {
                        // only replace ground: near the exit the tunnel opens into a trench
                        if (!current.isAir()) set(x, y, z, Blocks.TUFF_BRICKS);
                    } else if (y == k - 1) {
                        set(x, y, z, Blocks.TUFF_BRICKS);
                    } else if (y == k) {
                        set(x, y, z, step);
                    } else {
                        set(x, y, z, Blocks.AIR);
                    }
                }
            }
            if (k % 6 == 3) {
                if (!level.getBlockState(at(-2, k + 2, z)).isAir()) set(-2, k + 2, z, lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
                if (!level.getBlockState(at(2, k + 2, z)).isAir()) set(2, k + 2, z, lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
            }
            k++;
            z++;
        }

        // rune arch at the top of the stairs
        int topY = k;
        for (int y = 0; y < 5; y++) {
            BlockState post = y == 4 ? Blocks.CHISELED_TUFF_BRICKS.defaultBlockState() : Blocks.TUFF_BRICKS.defaultBlockState();
            set(-3, topY + y, z, post);
            set(3, topY + y, z, post);
        }
        for (int x = -3; x <= 3; x++) set(x, topY + 5, z, x == 0 ? Blocks.WAXED_OXIDIZED_CHISELED_COPPER.defaultBlockState() : Blocks.TUFF_BRICK_SLAB.defaultBlockState());
        for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) set(x, topY + y, z, Blocks.AIR);
        // faroles de almas colgando del dintel
        set(-2, topY + 4, z, hangingLantern(Blocks.SOUL_LANTERN));
        set(2, topY + 4, z, hangingLantern(Blocks.SOUL_LANTERN));
        return at(0, topY, z + 1);
    }
}
