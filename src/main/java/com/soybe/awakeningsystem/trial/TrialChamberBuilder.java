package com.soybe.awakeningsystem.trial;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Cámaras de Desafío del Sistema (sección 25 del roadmap). Bajo tierra, en piedra abisal y cobre:
 * <ul>
 *   <li>Gran atrio de dos niveles con balcón, generador central y bóveda.</li>
 *   <li>Pasillos con trampas de flechas mágicas.</li>
 *   <li>Tres salas temáticas en orden aleatorio: Murim (bambú de hierro y muñecos de Qi),
 *       Sombras (niebla que obliga a llevar antorcha) y Elemental (plataformas de fuego y hielo
 *       sobre un foso, y una corriente de viento hasta la bóveda).</li>
 *   <li>Torre de Ascenso: parkour en vertical hasta la gran bóveda.</li>
 * </ul>
 * Cada generador cierra sus puertas con barreras de fuerza mientras dura la prueba.
 * Coordenadas relativas al centro del suelo del atrio.
 */
public final class TrialChamberBuilder {

    private static final int DEPTH = 32;
    private static final int ATRIUM = 12;
    private static final int ATRIUM_H = 16;
    private static final int BALCONY = 7;          // altura del suelo del balcón
    private static final int ROOM = 9;
    private static final int ROOM_H = 10;
    private static final int ELEMENTAL_H = 16;
    private static final int ROOM_DIST = 36;       // del centro del atrio al centro de cada sala
    private static final int TOWER_CX = 11, TOWER_CZ = 20, TOWER = 5, TOWER_H = 30;

    private final ServerLevel level;
    private final int ox, oy, oz;
    private final Random random;
    private final BlockState abyssal, runic, abyssalBlock;

    private TrialChamberBuilder(ServerLevel level, int ox, int oy, int oz) {
        this.level = level;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.random = new Random(ox * 341873128712L + oz * 132897987541L);
        this.abyssal = SystemBlocks.ABYSSAL_STONE.get().defaultBlockState();
        this.runic = TrialRegistry.RUNIC_ABYSSAL_STONE.get().defaultBlockState();
        this.abyssalBlock = SystemBlocks.ABYSSAL_BLOCK.get().defaultBlockState();
    }

    /** Construye las cámaras bajo {@code surface} y devuelve la entrada en superficie. */
    public static BlockPos build(ServerLevel level, BlockPos surface) {
        int floorY = Math.max(level.getMinBuildHeight() + 12, surface.getY() - DEPTH);
        TrialChamberBuilder b = new TrialChamberBuilder(level, surface.getX(), floorY, surface.getZ());

        // Salas temáticas repartidas al azar entre este, oeste y norte
        List<Integer> themes = new ArrayList<>(List.of(TrialSpawnerBlockEntity.THEME_MURIM,
                TrialSpawnerBlockEntity.THEME_SHADOW, TrialSpawnerBlockEntity.THEME_ELEMENTAL));
        Collections.shuffle(themes, b.random);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, -1}};

        TrialSpawnerBlockEntity atriumSpawner = b.buildAtrium();
        for (int i = 0; i < 3; i++) {
            int dx = dirs[i][0], dz = dirs[i][1];
            b.buildRoom(themes.get(i), dx * ROOM_DIST, dz * ROOM_DIST, -dx, -dz);
        }
        b.buildTower(atriumSpawner);
        for (int i = 0; i < 3; i++) {
            int dx = dirs[i][0], dz = dirs[i][1];
            b.buildCorridor(dx, dz, themes.get(i));
            // las puertas del atrio también se cierran durante su prueba
            for (int w = -2; w <= 2; w++) for (int y = 0; y < 5; y++) {
                atriumSpawner.addBarrier(b.at(dx * (ATRIUM + 1) + (dz != 0 ? w : 0), y, dz * (ATRIUM + 1) + (dx != 0 ? w : 0)));
            }
        }
        return b.buildStairsAndEntrance(atriumSpawner);
    }

    // ------------------------------------------------------------------ helpers

    BlockPos at(int x, int y, int z) {
        return new BlockPos(ox + x, oy + y, oz + z);
    }

    private void set(int x, int y, int z, BlockState state) {
        level.setBlock(at(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    private void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    private BlockState get(int x, int y, int z) {
        return level.getBlockState(at(x, y, z));
    }

    /** Capa exterior de sellado: solo rellena huecos y líquidos, para que no entre agua ni lava. */
    private void seal(int x, int y, int z) {
        BlockState current = get(x, y, z);
        if (current.isAir() || !current.getFluidState().isEmpty() || current.canBeReplaced()) {
            set(x, y, z, Blocks.DEEPSLATE_BRICKS);
        }
    }

    /** Vacía un volumen y lo rodea de suelo, paredes, techo y una capa de sellado. Interior y0..y1. */
    private void shell(int x1, int z1, int x2, int z2, int y0, int y1, BlockState floor, BlockState wall, BlockState ceiling) {
        for (int x = x1 - 2; x <= x2 + 2; x++) {
            for (int z = z1 - 2; z <= z2 + 2; z++) {
                for (int y = y0 - 2; y <= y1 + 2; y++) {
                    boolean outer = x == x1 - 2 || x == x2 + 2 || z == z1 - 2 || z == z2 + 2 || y == y0 - 2 || y == y1 + 2;
                    boolean inner = x == x1 - 1 || x == x2 + 1 || z == z1 - 1 || z == z2 + 1 || y == y0 - 1 || y == y1 + 1;
                    if (outer) seal(x, y, z);
                    else if (inner) set(x, y, z, y == y0 - 1 ? floor : y == y1 + 1 ? ceiling : wall);
                    else set(x, y, z, Blocks.AIR);
                }
            }
        }
    }

    private BlockState lit(Block bulb) {
        return bulb.defaultBlockState().setValue(BlockStateProperties.LIT, true);
    }

    private BlockState hanging(Block lantern) {
        return lantern.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    private TrialSpawnerBlockEntity placeSpawner(int x, int y, int z, int theme) {
        set(x, y, z, TrialRegistry.SHADOW_TRIAL_SPAWNER.get().defaultBlockState());
        if (level.getBlockEntity(at(x, y, z)) instanceof TrialSpawnerBlockEntity be) {
            be.setTheme(theme);
            return be;
        }
        throw new IllegalStateException("Trial spawner block entity missing");
    }

    private void placeVault(int x, int y, int z, Direction facing) {
        set(x, y, z, TrialRegistry.AWAKENING_VAULT.get().defaultBlockState().setValue(TrialVaultBlock.FACING, facing));
    }

    private static Direction horizontal(int dx, int dz) {
        if (dx > 0) return Direction.EAST;
        if (dx < 0) return Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // ------------------------------------------------------------------ atrium

    private TrialSpawnerBlockEntity buildAtrium() {
        shell(-ATRIUM, -ATRIUM, ATRIUM, ATRIUM, 0, ATRIUM_H - 1, Blocks.POLISHED_TUFF.defaultBlockState(), abyssal, abyssal);

        // bandas de piedra rúnica y friso de cobre en las paredes; bombillas cada 6 bloques
        for (int i = -ATRIUM; i <= ATRIUM; i++) {
            for (int[] w : new int[][]{{i, -ATRIUM - 1}, {i, ATRIUM + 1}, {-ATRIUM - 1, i}, {ATRIUM + 1, i}}) {
                set(w[0], 0, w[1], runic);
                set(w[0], BALCONY + 1, w[1], runic);
                set(w[0], 12, w[1], Blocks.WAXED_OXIDIZED_CUT_COPPER);
                set(w[0], ATRIUM_H - 1, w[1], runic);
                if (Math.floorMod(i, 6) == 3) {
                    set(w[0], 3, w[1], lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
                    set(w[0], 11, w[1], lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
                }
            }
        }
        // suelo: borde abisal y cruz de cobre hacia las puertas
        for (int x = -ATRIUM; x <= ATRIUM; x++) for (int z = -ATRIUM; z <= ATRIUM; z++) {
            int edge = Math.min(ATRIUM - Math.abs(x), ATRIUM - Math.abs(z));
            if (edge == 0) set(x, -1, z, abyssalBlock);
            else if ((x == 0 || z == 0) && Math.abs(x) + Math.abs(z) > 4) set(x, -1, z, Blocks.WAXED_OXIDIZED_CUT_COPPER);
        }
        // techo con linternas marinas
        for (int x = -8; x <= 8; x += 8) for (int z = -8; z <= 8; z += 8) set(x, ATRIUM_H, z, Blocks.SEA_LANTERN);

        // balcón de dos bloques alrededor de todo el atrio, con barandilla
        for (int x = -ATRIUM; x <= ATRIUM; x++) for (int z = -ATRIUM; z <= ATRIUM; z++) {
            int edge = Math.min(ATRIUM - Math.abs(x), ATRIUM - Math.abs(z));
            if (edge <= 1) set(x, BALCONY, z, Blocks.POLISHED_TUFF);
            if (edge == 2) set(x, BALCONY + 1, z, Blocks.TUFF_BRICK_WALL);
        }
        // escalera al balcón por el lado oeste
        BlockState stair = Blocks.TUFF_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        for (int k = 0; k < BALCONY; k++) {
            int z = 6 - k;
            for (int x = -10; x <= -9; x++) {
                for (int y = 0; y < k; y++) set(x, y, z, Blocks.TUFF_BRICKS);
                set(x, k, z, stair);
                for (int y = k + 1; y <= BALCONY + 2; y++) if (y != BALCONY) set(x, y, z, Blocks.AIR);
            }
        }
        for (int x = -10; x <= -9; x++) {
            set(x, BALCONY, -1, Blocks.POLISHED_TUFF);
            set(x, BALCONY + 1, -1, Blocks.AIR);
            set(x, BALCONY + 1, 0, Blocks.AIR);
        }

        // cuatro grandes pilares de piedra rúnica con bombillas
        for (int[] p : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}}) {
            for (int y = 0; y < ATRIUM_H; y++) {
                set(p[0], y, p[1], y % 5 == 0 ? runic : Blocks.POLISHED_TUFF.defaultBlockState());
            }
            set(p[0], 4, p[1], lit(Blocks.WAXED_COPPER_BULB));
            set(p[0], 10, p[1], lit(Blocks.WAXED_COPPER_BULB));
        }

        // estrado del generador
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            boolean rim = Math.abs(x) == 3 || Math.abs(z) == 3;
            set(x, 0, z, rim ? Blocks.CHISELED_TUFF_BRICKS.defaultBlockState() : abyssalBlock);
        }
        set(0, 0, 0, Blocks.WAXED_OXIDIZED_COPPER);
        TrialSpawnerBlockEntity spawner = placeSpawner(0, 1, 0, TrialSpawnerBlockEntity.THEME_HALL);

        // bóveda del atrio en el balcón norte
        placeVault(0, BALCONY + 1, -ATRIUM, Direction.SOUTH);
        set(0, BALCONY + 2, -ATRIUM, runic);
        return spawner;
    }

    // ------------------------------------------------------------------ corridors with traps

    /** Pasillo de 5x5 del atrio a la sala en la dirección (dx, dz), con trampas de flechas en las paredes. */
    private void buildCorridor(int dx, int dz, int theme) {
        int near = ATRIUM + 1, far = ROOM_DIST - ROOM - 1;
        boolean alongX = dx != 0;
        int sign = alongX ? dx : dz;
        for (int i = near; i <= far; i++) {
            for (int w = -4; w <= 4; w++) {
                for (int y = -2; y <= 6; y++) {
                    int x = alongX ? i * sign : w, z = alongX ? w : i * sign;
                    boolean outer = Math.abs(w) == 4 || y == -2 || y == 6;
                    boolean inner = Math.abs(w) == 3 || y == -1 || y == 5;
                    if (outer) seal(x, y, z);
                    else if (inner) {
                        BlockState s;
                        if (y == -1) s = w == 0 ? Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState() : Blocks.POLISHED_TUFF.defaultBlockState();
                        else if (y == 5) s = w == 0 && Math.floorMod(i, 4) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : abyssal;
                        else s = y == 0 || y == 4 ? runic : abyssal;
                        set(x, y, z, s);
                    } else set(x, y, z, Blocks.AIR);
                }
            }
        }
        // trampas de flechas mágicas, alternando paredes, en tramos al azar
        int traps = 3 + random.nextInt(2);
        for (int t = 0; t < traps; t++) {
            int i = near + 2 + random.nextInt(far - near - 3);
            int side = t % 2 == 0 ? 1 : -1;
            int x = alongX ? i * sign : 3 * side, z = alongX ? 3 * side : i * sign;
            Direction facing = alongX ? (side > 0 ? Direction.NORTH : Direction.SOUTH) : (side > 0 ? Direction.WEST : Direction.EAST);
            set(x, 1, z, TrialRegistry.RUNE_ARROW_TRAP.get().defaultBlockState().setValue(RuneArrowTrapBlock.FACING, facing));
        }
        // antes de la sala de Sombras, un barril con antorchas tácticas
        if (theme == TrialSpawnerBlockEntity.THEME_SHADOW) {
            int i = far - 1;
            int x = alongX ? i * sign : -2, z = alongX ? -2 : i * sign;
            set(x, 0, z, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
            if (level.getBlockEntity(at(x, 0, z)) instanceof BarrelBlockEntity barrel) {
                barrel.setItem(0, new ItemStack(Items.TORCH, 16));
                barrel.setItem(1, new ItemStack(Items.SOUL_TORCH, 16));
                barrel.setItem(2, new ItemStack(Items.LANTERN, 2));
            }
            set(alongX ? i * sign : -2, 1, alongX ? -2 : i * sign, Blocks.SOUL_LANTERN);
        }
    }

    // ------------------------------------------------------------------ themed rooms

    /**
     * Sistema local de cada sala: v avanza de la puerta (v = -ROOM) al fondo (v = +ROOM) y u es el
     * eje transversal. (bx, bz) es la dirección de la puerta al fondo.
     */
    private int wx(int cx, int bx, int bz, int u, int v) {
        return cx + u * -bz + v * bx;
    }

    private int wz(int cz, int bx, int bz, int u, int v) {
        return cz + u * bx + v * bz;
    }

    private void buildRoom(int theme, int cx, int cz, int doorDx, int doorDz) {
        int bx = -doorDx, bz = -doorDz;   // de la puerta hacia el fondo
        Direction toDoor = horizontal(doorDx, doorDz);
        TrialSpawnerBlockEntity spawner = switch (theme) {
            case TrialSpawnerBlockEntity.THEME_MURIM -> buildMurim(cx, cz, bx, bz, toDoor);
            case TrialSpawnerBlockEntity.THEME_SHADOW -> buildShadow(cx, cz, bx, bz, toDoor);
            default -> buildElemental(cx, cz, bx, bz, toDoor);
        };
        // barreras de fuerza en la puerta de la sala
        for (int u = -2; u <= 2; u++) for (int y = 0; y < 5; y++) {
            spawner.addBarrier(at(wx(cx, bx, bz, u, -ROOM - 1), y, wz(cz, bx, bz, u, -ROOM - 1)));
        }
    }

    private TrialSpawnerBlockEntity buildMurim(int cx, int cz, int bx, int bz, Direction toDoor) {
        shell(cx - ROOM, cz - ROOM, cx + ROOM, cz + ROOM, 0, ROOM_H - 1, Blocks.BAMBOO_MOSAIC.defaultBlockState(),
                Blocks.BAMBOO_BLOCK.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        // bambú de hierro: postes de madera oscura y barrotes de hierro delante de cada tramo de pared
        for (int i = -ROOM; i <= ROOM; i++) {
            for (int[] p : new int[][]{{i, -ROOM}, {i, ROOM}, {-ROOM, i}, {ROOM, i}}) {
                // coordenadas locales de la celda: el hueco de la puerta queda libre
                int u = -bz * p[0] + bx * p[1], v = bx * p[0] + bz * p[1];
                if (v == -ROOM && Math.abs(u) <= 2) continue;
                int x = cx + p[0], z = cz + p[1];
                for (int y = 0; y < ROOM_H - 1; y++) {
                    if (Math.floorMod(i, 3) == 0) set(x, y, z, log);
                    else if (y < ROOM_H - 2) set(x, y, z, Blocks.IRON_BARS);
                }
            }
        }
        // vigas bajo el techo y farolillos colgantes
        for (int i = -ROOM; i <= ROOM; i++) {
            set(cx + i, ROOM_H - 1, cz, Blocks.DARK_OAK_PLANKS);
            set(cx, ROOM_H - 1, cz + i, Blocks.DARK_OAK_PLANKS);
        }
        for (int u = -5; u <= 5; u += 5) for (int v = -5; v <= 5; v += 5) {
            if (u == 0 && v == 0) continue;
            set(wx(cx, bx, bz, u, v), ROOM_H - 2, wz(cz, bx, bz, u, v), hanging(Blocks.LANTERN));
        }
        // borde del suelo en abeto
        for (int x = -ROOM; x <= ROOM; x++) for (int z = -ROOM; z <= ROOM; z++) {
            if (Math.abs(x) == ROOM || Math.abs(z) == ROOM) set(cx + x, -1, cz + z, Blocks.SPRUCE_PLANKS);
        }
        // tarima de entrenamiento con el generador y muñecos de Qi alrededor
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) set(cx + x, 0, cz + z, Blocks.STRIPPED_DARK_OAK_WOOD);
        TrialSpawnerBlockEntity spawner = placeSpawner(cx, 1, cz, TrialSpawnerBlockEntity.THEME_MURIM);
        for (int[] d : new int[][]{{-5, 4}, {5, 4}, {-5, -3}, {5, -3}}) {
            qiDummy(wx(cx, bx, bz, d[0], d[1]), wz(cz, bx, bz, d[0], d[1]), toDoor);
        }
        int vx = wx(cx, bx, bz, 0, ROOM), vz = wz(cz, bx, bz, 0, ROOM);
        placeVault(vx, 0, vz, toDoor);
        set(vx, 1, vz, Blocks.CHISELED_BOOKSHELF);
        return spawner;
    }

    /** Muñeco de entrenamiento imbuido en Qi: brilla y lleva su nombre a la vista. */
    private void qiDummy(int x, int z, Direction facing) {
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand == null) return;
        BlockPos p = at(x, 0, z);
        stand.moveTo(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, facing.toYRot(), 0.0F);
        stand.setShowArms(true);
        stand.setGlowingTag(true);
        stand.setCustomName(Component.translatable("awakening_system.trial.qi_dummy"));
        stand.setCustomNameVisible(true);
        level.addFreshEntity(stand);
    }

    private TrialSpawnerBlockEntity buildShadow(int cx, int cz, int bx, int bz, Direction toDoor) {
        shell(cx - ROOM, cz - ROOM, cx + ROOM, cz + ROOM, 0, ROOM_H - 1, Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState());
        // suelo de losas: en la oscuridad no aparecen monstruos por su cuenta
        BlockState slab = Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        for (int x = -ROOM; x <= ROOM; x++) for (int z = -ROOM; z <= ROOM; z++) set(cx + x, 0, cz + z, slab);
        // vetas de obsidiana llorosa en los pilares de las paredes
        for (int i = -ROOM; i <= ROOM; i += 3) {
            for (int[] p : new int[][]{{i, -ROOM - 1}, {i, ROOM + 1}, {-ROOM - 1, i}, {ROOM + 1, i}}) {
                for (int y = 1; y < ROOM_H; y++) {
                    set(cx + p[0], y, cz + p[1], y % 3 == 1 ? Blocks.CRYING_OBSIDIAN : Blocks.CHISELED_POLISHED_BLACKSTONE);
                }
            }
        }
        // apenas dos faroles de almas junto a la puerta: el resto es niebla
        set(wx(cx, bx, bz, -3, -ROOM + 1), ROOM_H - 1, wz(cz, bx, bz, -3, -ROOM + 1), hanging(Blocks.SOUL_LANTERN));
        set(wx(cx, bx, bz, 3, -ROOM + 1), ROOM_H - 1, wz(cz, bx, bz, 3, -ROOM + 1), hanging(Blocks.SOUL_LANTERN));
        // anillo de obsidiana y estrado de piedra negra dorada
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            set(cx + x, 0, cz + z, (Math.abs(x) == 2 || Math.abs(z) == 2) ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        }
        set(cx, 0, cz, Blocks.GILDED_BLACKSTONE);
        TrialSpawnerBlockEntity spawner = placeSpawner(cx, 1, cz, TrialSpawnerBlockEntity.THEME_SHADOW);
        int vx = wx(cx, bx, bz, 0, ROOM), vz = wz(cz, bx, bz, 0, ROOM);
        placeVault(vx, 1, vz, toDoor);
        set(vx, 0, vz, Blocks.CHISELED_POLISHED_BLACKSTONE);
        return spawner;
    }

    /**
     * Sala Elemental: cornisa de entrada, foso de magma, plataformas de hielo y de fuego hasta una
     * plataforma central con el generador, y una corriente de viento que sube a la cornisa de la bóveda.
     */
    private TrialSpawnerBlockEntity buildElemental(int cx, int cz, int bx, int bz, Direction toDoor) {
        int pit = -6;
        shell(cx - ROOM, cz - ROOM, cx + ROOM, cz + ROOM, pit, ELEMENTAL_H - 1, Blocks.MAGMA_BLOCK.defaultBlockState(),
                Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // fondo del foso: magma con vetas de hielo
        for (int x = -ROOM; x <= ROOM; x++) for (int z = -ROOM; z <= ROOM; z++) {
            if (random.nextInt(5) == 0) set(cx + x, pit - 1, cz + z, Blocks.PACKED_ICE);
        }
        // cornisa de entrada (3 de fondo) a nivel de la puerta
        fillLocal(cx, cz, bx, bz, -ROOM, ROOM, -ROOM, -ROOM + 2, pit, -1, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // saltos de un bloque: cornisa -> hielo (resbala) -> fuego (quema si no vas agachado) -> centro
        platform(cx, cz, bx, bz, -4, -4, 1, pit, Blocks.BLUE_ICE.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState());
        platform(cx, cz, bx, bz, 0, -1, 1, pit, Blocks.MAGMA_BLOCK.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState());
        // plataforma central alargada hacia el fondo, con el generador y el respiradero Anemo
        fillLocal(cx, cz, bx, bz, -2, 2, 2, 7, pit, -1, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        fillLocal(cx, cz, bx, bz, -2, 2, 2, 7, -1, -1, Blocks.SMOOTH_QUARTZ.defaultBlockState());
        TrialSpawnerBlockEntity spawner = placeSpawner(wx(cx, bx, bz, 0, 3), 0, wz(cz, bx, bz, 0, 3), TrialSpawnerBlockEntity.THEME_ELEMENTAL);
        set(wx(cx, bx, bz, 0, 7), -1, wz(cz, bx, bz, 0, 7), TrialRegistry.ANEMO_VENT.get().defaultBlockState());

        // cornisa alta de la bóveda en el fondo
        int ledge = 7;
        fillLocal(cx, cz, bx, bz, -3, 3, ROOM - 1, ROOM, ledge, ledge, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        int vx = wx(cx, bx, bz, 0, ROOM), vz = wz(cz, bx, bz, 0, ROOM);
        placeVault(vx, ledge + 1, vz, toDoor);

        // columnas elementales en las paredes laterales: fuego, viento y hielo
        for (int v = -6; v <= 6; v += 6) {
            Block element = v < 0 ? Blocks.MAGMA_BLOCK : v == 0 ? Blocks.CALCITE : Blocks.BLUE_ICE;
            for (int y = pit; y < ELEMENTAL_H; y++) {
                set(wx(cx, bx, bz, -ROOM - 1, v), y, wz(cz, bx, bz, -ROOM - 1, v), element);
                set(wx(cx, bx, bz, ROOM + 1, v), y, wz(cz, bx, bz, ROOM + 1, v), element);
            }
        }
        // luces de rana del color de cada elemento en el techo
        for (int u = -5; u <= 5; u += 5) for (int v = -5; v <= 5; v += 5) {
            Block light = u < 0 ? Blocks.OCHRE_FROGLIGHT : u == 0 ? Blocks.VERDANT_FROGLIGHT : Blocks.PEARLESCENT_FROGLIGHT;
            set(wx(cx, bx, bz, u, v), ELEMENTAL_H, wz(cz, bx, bz, u, v), light);
        }
        return spawner;
    }

    /** Rellena un rectángulo local (u1..u2, v1..v2) entre las alturas y1..y2. */
    private void fillLocal(int cx, int cz, int bx, int bz, int u1, int u2, int v1, int v2, int y1, int y2, BlockState state) {
        for (int u = u1; u <= u2; u++) for (int v = v1; v <= v2; v++) for (int y = y1; y <= y2; y++) {
            set(wx(cx, bx, bz, u, v), y, wz(cz, bx, bz, u, v), state);
        }
    }

    /** Plataforma de (2r+1)² sobre un pilar que sale del foso; la superficie queda a la altura de la puerta. */
    private void platform(int cx, int cz, int bx, int bz, int u, int v, int r, int pit, BlockState top, BlockState body) {
        fillLocal(cx, cz, bx, bz, u - r, u + r, v - r, v + r, pit, -2, body);
        fillLocal(cx, cz, bx, bz, u - r, u + r, v - r, v + r, -1, -1, top);
    }

    // ------------------------------------------------------------------ tower of ascent

    /** Torre de Ascenso: plataformas en espiral hasta la gran bóveda, con un estanque al fondo. */
    private void buildTower(TrialSpawnerBlockEntity atriumSpawner) {
        int x1 = TOWER_CX - TOWER, x2 = TOWER_CX + TOWER, z1 = TOWER_CZ - TOWER, z2 = TOWER_CZ + TOWER;
        shell(x1, z1, x2, z2, 0, TOWER_H - 1, abyssalBlock, abyssal, abyssal);
        // estanque de un bloque para amortiguar las caídas
        for (int x = x1; x <= x2; x++) for (int z = z1; z <= z2; z++) set(x, 0, z, Blocks.WATER);
        // luces en las paredes cada 5 bloques de altura
        for (int y = 3; y < TOWER_H; y += 5) {
            set(x1 - 1, y, TOWER_CZ, Blocks.SEA_LANTERN);
            set(x2 + 1, y, TOWER_CZ, Blocks.SEA_LANTERN);
            set(TOWER_CX, y, z2 + 1, Blocks.SEA_LANTERN);
            set(TOWER_CX, y, z1 - 1, Blocks.SEA_LANTERN);
        }
        // puerta desde el atrio (dos muros de grosor) con su entrada al estanque
        for (int x = TOWER_CX - 1; x <= TOWER_CX + 1; x++) for (int y = 1; y < 4; y++) {
            set(x, y, ATRIUM + 1, Blocks.AIR);
            set(x, y, z1 - 1, Blocks.AIR);
            atriumSpawner.addBarrier(at(x, y, ATRIUM + 1));
        }
        for (int x = TOWER_CX - 1; x <= TOWER_CX + 1; x++) set(x, 0, ATRIUM + 1, Blocks.POLISHED_TUFF);

        // espiral: recorre el anillo interior subiendo un bloque por salto
        int ring = TOWER - 1;
        List<int[]> path = new ArrayList<>();
        for (int i = -ring; i < ring; i += 2) path.add(new int[]{i, -ring});
        for (int i = -ring; i < ring; i += 2) path.add(new int[]{ring, i});
        for (int i = ring; i > -ring; i -= 2) path.add(new int[]{i, ring});
        for (int i = ring; i > -ring; i -= 2) path.add(new int[]{-ring, i});
        BlockState[] steps = {Blocks.WAXED_OXIDIZED_COPPER_GRATE.defaultBlockState(), runic, Blocks.CHISELED_TUFF_BRICKS.defaultBlockState(), abyssalBlock};
        int top = TOWER_H - 5;
        // el primer escalón sale del estanque: se sube desde el fondo con un salto normal
        int y = 0;
        int k = 0;
        while (y < top) {
            int[] p = path.get(k % path.size());
            set(TOWER_CX + p[0], y, TOWER_CZ + p[1], steps[k % steps.length]);
            y++;
            k++;
        }
        // rellano final con la gran bóveda
        for (int x = TOWER_CX - 2; x <= TOWER_CX + 2; x++) for (int z = TOWER_CZ - 2; z <= TOWER_CZ + 2; z++) {
            set(x, top, z, (Math.abs(x - TOWER_CX) == 2 || Math.abs(z - TOWER_CZ) == 2) ? runic : abyssalBlock);
        }
        int[] last = path.get((k - 1) % path.size());
        // puente del último escalón al rellano
        int bx = Integer.signum(-last[0]), bz = Integer.signum(-last[1]);
        for (int s = 0; s <= 3; s++) set(TOWER_CX + last[0] + bx * s, top, TOWER_CZ + last[1] + bz * s, abyssalBlock);
        placeVault(TOWER_CX, top + 1, TOWER_CZ, Direction.NORTH);
        set(TOWER_CX - 2, top + 1, TOWER_CZ - 2, lit(Blocks.WAXED_COPPER_BULB));
        set(TOWER_CX + 2, top + 1, TOWER_CZ + 2, lit(Blocks.WAXED_COPPER_BULB));
    }

    // ------------------------------------------------------------------ stairs and surface entrance

    private BlockPos buildStairsAndEntrance(TrialSpawnerBlockEntity atriumSpawner) {
        for (int x = -1; x <= 1; x++) for (int y = 0; y < 4; y++) {
            set(x, y, ATRIUM + 1, Blocks.AIR);
            atriumSpawner.addBarrier(at(x, y, ATRIUM + 1));
        }

        BlockState step = Blocks.TUFF_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        int k = 0;
        int z = ATRIUM + 2;
        while (k < 72) {
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox, oz + z) - oy;
            if (k >= surfaceY) break;
            for (int x = -2; x <= 2; x++) {
                for (int y = k - 1; y <= k + 4; y++) {
                    boolean wall = Math.abs(x) == 2 || y == k + 4;
                    BlockState current = get(x, y, z);
                    if (wall) {
                        if (!current.isAir()) set(x, y, z, y == k + 4 ? abyssal : runic);
                    } else if (y == k - 1) {
                        set(x, y, z, abyssal);
                    } else if (y == k) {
                        set(x, y, z, step);
                    } else {
                        set(x, y, z, Blocks.AIR);
                    }
                }
            }
            if (k % 6 == 3) {
                if (!get(-2, k + 2, z).isAir()) set(-2, k + 2, z, lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
                if (!get(2, k + 2, z).isAir()) set(2, k + 2, z, lit(Blocks.WAXED_WEATHERED_COPPER_BULB));
            }
            k++;
            z++;
        }

        // arco rúnico en la superficie
        int topY = k;
        for (int y = 0; y < 5; y++) {
            set(-3, topY + y, z, y == 4 ? runic : abyssal);
            set(3, topY + y, z, y == 4 ? runic : abyssal);
        }
        for (int x = -3; x <= 3; x++) set(x, topY + 5, z, x == 0 ? Blocks.WAXED_OXIDIZED_CHISELED_COPPER.defaultBlockState() : runic);
        for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) set(x, topY + y, z, Blocks.AIR);
        set(-2, topY + 4, z, hanging(Blocks.SOUL_LANTERN));
        set(2, topY + 4, z, hanging(Blocks.SOUL_LANTERN));
        return at(0, topY, z + 1);
    }
}
