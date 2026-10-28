package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.BlackjackPart;
import com.soybe.awakeningsystem.block.BlackjackTableBlock;
import com.soybe.awakeningsystem.block.CasinoCashierBlock;
import com.soybe.awakeningsystem.block.CasinoChairBlock;
import com.soybe.awakeningsystem.block.CasinoTrophyBlock;
import com.soybe.awakeningsystem.block.RoulettePart;
import com.soybe.awakeningsystem.block.RouletteTableBlock;
import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.block.entity.CasinoTrophyBlockEntity;
import com.soybe.awakeningsystem.casino.CasinoJsonHelper;
import com.soybe.awakeningsystem.block.CasinoReceptionGateBlock;
import com.soybe.awakeningsystem.block.CasinoVipGateBlock;
import com.soybe.awakeningsystem.block.CasinoWallDisplayStandBlock;
import com.soybe.awakeningsystem.block.entity.CasinoWallDisplayStandBlockEntity;
import com.soybe.awakeningsystem.casino.darts.DartboardBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTablePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TeyvatCasinoBuilder {

    public static final BlockPos CASINO_POS = new BlockPos(220, 68, -220);
    private static volatile boolean casinoBuilt = false;

    private TeyvatCasinoBuilder() {}

    public static boolean isInsideCasino(net.minecraft.world.level.Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        if (!level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) return false;
        MinecraftServer server = (level instanceof ServerLevel sl) ? sl.getServer() : null;
        if (server == null) return false;
        var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        if (tData == null) return false;

        for (BlockPos origin : tData.casinos) {
            int cx = origin.getX();
            int cy = origin.getY();
            int cz = origin.getZ();
            if (pos.getX() >= cx - 14 && pos.getX() <= cx + 35 &&
                pos.getZ() >= cz - 38 && pos.getZ() <= cz + 32 &&
                pos.getY() >= cy - 5 && pos.getY() <= cy + 28) {
                return true;
            }
        }

        BlockPos named = tData.getNamedStructure("casino_origin");
        if (named != null) {
            int cx = named.getX();
            int cy = named.getY();
            int cz = named.getZ();
            if (pos.getX() >= cx - 14 && pos.getX() <= cx + 35 &&
                pos.getZ() >= cz - 38 && pos.getZ() <= cz + 32 &&
                pos.getY() >= cy - 5 && pos.getY() <= cy + 28) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInsideVipZone(net.minecraft.world.level.Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        if (!isInsideCasino(level, pos)) return false;

        MinecraftServer server = (level instanceof ServerLevel sl) ? sl.getServer() : null;
        if (server == null) return false;
        var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(server);
        if (tData == null) return false;

        java.util.List<BlockPos> origins = new java.util.ArrayList<>(tData.casinos);
        BlockPos named = tData.getNamedStructure("casino_origin");
        if (named != null && !origins.contains(named)) origins.add(named);
        if (origins.isEmpty()) origins.add(CASINO_POS);

        for (BlockPos origin : origins) {
            int cx = origin.getX();
            int cy = origin.getY();
            int cz = origin.getZ();

            // Salón VIP en casino monumental por defecto (ala este tras la cancela):
            if (pos.getX() >= cx + 15 && pos.getX() <= cx + 32 &&
                pos.getZ() >= cz - 26 && pos.getZ() <= cz - 8 &&
                pos.getY() >= cy - 1 && pos.getY() <= cy + 10) {
                return true;
            }

            // Salón VIP en configuración personalizada (área de mesas VIP dz=-32..-16):
            if (pos.getX() >= cx - 10 && pos.getX() <= cx + 10 &&
                pos.getZ() >= cz - 32 && pos.getZ() <= cz - 16 &&
                pos.getY() >= cy - 1 && pos.getY() <= cy + 10) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSolidMainlandArea(ServerLevel level, int cx, int cz) {
        int[] testOffsetsX = {-10, 0, 10, 20, 26, 30};
        int[] testOffsetsZ = {-34, -20, 0, 15, 27};
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int ox : testOffsetsX) {
            for (int oz : testOffsetsZ) {
                int tx = cx + ox;
                int tz = cz + oz;
                int surfY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, tx, tz);
                if (surfY < 64 || surfY > 120) return false;
                BlockPos groundPos = new BlockPos(tx, surfY - 1, tz);
                BlockState ground = level.getBlockState(groundPos);
                if (ground.liquid() || !ground.isSolid()) return false;
                minY = Math.min(minY, surfY);
                maxY = Math.max(maxY, surfY);
            }
        }
        return (maxY - minY) <= 10;
    }

    public static BlockPos findMainlandCasinoPos(ServerLevel level) {
        for (int r = 0; r <= 300; r += 20) {
            for (int dx = -r; dx <= r; dx += (r == 0 ? 1 : 40)) {
                for (int dz = -r; dz <= r; dz += (r == 0 ? 1 : 40)) {
                    int candX = 220 + dx;
                    int candZ = -220 + dz;
                    if (isSolidMainlandArea(level, candX, candZ)) {
                        int surfY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, candX, candZ);
                        return new BlockPos(candX, Math.max(68, surfY), candZ);
                    }
                }
            }
        }

        for (int r = 100; r <= 600; r += 32) {
            for (int dx = -r; dx <= r; dx += 64) {
                for (int dz = -r; dz <= r; dz += 64) {
                    if (isSolidMainlandArea(level, dx, dz)) {
                        int surfY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, dx, dz);
                        return new BlockPos(dx, Math.max(68, surfY), dz);
                    }
                }
            }
        }

        int fbY = Math.max(68, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 200, -200));
        return new BlockPos(200, fbY, -200);
    }

    public static BlockPos getOrFindCasinoPos(ServerLevel level) {
        if (level.getServer() != null) {
            var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getServer());
            if (tData != null) {
                BlockPos recorded = tData.getNamedStructure("casino_origin");
                if (recorded != null && isSolidMainlandArea(level, recorded.getX(), recorded.getZ())) {
                    return recorded;
                }
            }
        }
        BlockPos found = findMainlandCasinoPos(level);
        if (level.getServer() != null) {
            var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getServer());
            if (tData != null) {
                tData.recordNamedStructure("casino_origin", found);
                tData.setDirty();
            }
        }
        return found;
    }

    public static void buildCasinoIfAbsent(ServerLevel level) {
        if (!level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) return;
        if (casinoBuilt) return;

        BlockPos origin = getOrFindCasinoPos(level);

        MinecraftServer server = level.getServer();
        if (server != null) {
            try {
                var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                if (data.isCasinoBuilt()) {
                    casinoBuilt = true;
                    return;
                }
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error checking TeyvatAbyssSavedData for casino: ", e);
            }
        }

        BlockPos checkPos = new BlockPos(origin.getX(), origin.getY() + 3, origin.getZ());
        if (level.getBlockState(checkPos).is(SystemBlocks.CASINO_TROPHY.get())) {
            casinoBuilt = true;
            if (server != null) {
                try {
                    TeyvatWorldManager.TeyvatAbyssSavedData.get(server).setCasinoBuilt(true);
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for casino: ", e);
                }
            }
            return;
        }

        buildCasino(level, origin);

        casinoBuilt = true;
        if (server != null) {
            try {
                TeyvatWorldManager.TeyvatAbyssSavedData.get(server).setCasinoBuilt(true);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for casino: ", e);
            }
        }
    }

    public static void forceRebuild(ServerLevel level) {
        if (!level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) return;
        casinoBuilt = false;
        TeyvatStructureManager.clearCache();

        BlockPos oldSeaPos = new BlockPos(180, 68, 60);
        for (int x = -10; x <= 26; x++) {
            for (int z = -34; z <= 27; z++) {
                for (int y = 0; y <= 25; y++) {
                    BlockPos p = new BlockPos(oldSeaPos.getX() + x, oldSeaPos.getY() + y, oldSeaPos.getZ() + z);
                    if (level.getBlockState(p).is(Blocks.STONE_BRICKS) || level.getBlockState(p).is(SystemBlocks.SLOT_MACHINE.get())) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        BlockPos origin = getOrFindCasinoPos(level);

        MinecraftServer server = level.getServer();
        if (server != null) {
            try {
                TeyvatWorldManager.TeyvatAbyssSavedData.get(server).setCasinoBuilt(false);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error resetting TeyvatAbyssSavedData for casino: ", e);
            }
        }
        buildCasino(level, origin);
        casinoBuilt = true;
        if (server != null) {
            try {
                TeyvatWorldManager.TeyvatAbyssSavedData.get(server).setCasinoBuilt(true);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for casino: ", e);
            }
        }
    }

    public static void buildCasino(ServerLevel level, BlockPos origin) {
        if (!level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) return;
        final int cx = origin.getX();
        final int cy = origin.getY();
        final int cz = origin.getZ();

        AwakeningSystemMod.LOGGER.info("[Gran Casino] Iniciando construcción en ({}, {}, {})...", cx, cy, cz);

        for (int x = -10; x <= 26; x++) {
            for (int z = -34; z <= 27; z++) {
                BlockPos colTop = new BlockPos(cx + x, cy - 1, cz + z);
                for (int dy = 0; dy <= 14; dy++) {
                    BlockPos p = colTop.below(dy);
                    BlockState existing = level.getBlockState(p);
                    if (existing.isAir() || existing.is(Blocks.WATER) || existing.is(BlockTags.REPLACEABLE) || existing.is(BlockTags.LEAVES) || existing.is(Blocks.SHORT_GRASS) || existing.is(Blocks.TALL_GRASS)) {
                        level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                    } else {
                        break;
                    }
                }
                for (int y = 0; y <= 21; y++) {
                    BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                    BlockState existing = level.getBlockState(p);
                    if (existing.is(BlockTags.LEAVES) || existing.is(BlockTags.LOGS) || existing.is(BlockTags.FLOWERS) || existing.is(Blocks.SHORT_GRASS) || existing.is(Blocks.TALL_GRASS)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        boolean nbtSuccess = TeyvatStructureManager.placeNbtStructure(level, origin, "grand_casino", -10, 0, -34);
        if (nbtSuccess) {
            AwakeningSystemMod.LOGGER.info("[Gran Casino] Estructura NBT 'grand_casino.nbt' colocada con éxito.");
        } else {
            AwakeningSystemMod.LOGGER.warn("[Gran Casino] No se pudo cargar 'grand_casino.nbt', continuando con equipamiento...");
        }

        if (CasinoJsonHelper.hasCustomLayout()) {
            int imported = CasinoJsonHelper.importFurniture(level, origin, "casino_custom.json");
            AwakeningSystemMod.LOGGER.info("[Gran Casino] Disposición personalizada cargada: {} muebles colocados.", imported);
        } else {
            populateDefaultFurniture(level, origin);
        }

        BlockPos waypointPos = new BlockPos(cx + 30, cy + 1, cz);

        for (int px = cx + 25; px <= cx + 32; px++) {
            for (int pz = cz - 3; pz <= cz + 3; pz++) {
                BlockPos p = new BlockPos(px, cy, pz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int dy = 1; dy <= 25; dy++) {
                    BlockPos bp = p.below(dy);
                    BlockState bs = level.getBlockState(bp);
                    if (bs.isSolid() && !bs.liquid() && !bs.is(BlockTags.LEAVES) && !bs.is(BlockTags.LOGS)) {
                        break;
                    }
                    level.setBlock(bp, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        try {
            TeyvatWorldGenManager.generateTeleportWaypoint(level, waypointPos);
            var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getServer());
            if (tData != null) {
                tData.recordNamedStructure("casino_origin", origin);
                tData.recordNamedStructure("casino", waypointPos);
                tData.recordNamedStructure("gran_casino", waypointPos);
                if (!tData.casinos.contains(origin)) {
                    tData.casinos.add(origin);
                }
                tData.setDirty();
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("[Gran Casino] Error al generar waypoint exterior: ", e);
        }

        AwakeningSystemMod.LOGGER.info("[Gran Casino] ¡Palacio monumental erigido exitosamente en ({}, {}, {})!", cx, cy, cz);
    }

    public static void populateDefaultFurniture(ServerLevel level, BlockPos origin) {
        final int cx = origin.getX();
        final int cy = origin.getY();
        final int cz = origin.getZ();

        placeWallStandWithTrophy(level, spos(cx, cy, cz, 10, 3, 34), Direction.EAST, 1,
                "Grand Teyvat Casino Cup", "Supreme Accolade for Historic Earnings Record");
        placeWallStandWithTrophy(level, spos(cx, cy, cz, 10, 3, 33), Direction.EAST, 2,
                "Blackjack 21 Trophy", "Strategic Mastery on the Royal Felt");
        placeWallStandWithTrophy(level, spos(cx, cy, cz, 10, 3, 35), Direction.EAST, 3,
                "Fortune Roulette Trophy", "Award for Perfect Single Number Hit on the Great Wheel");
        placeWallStandWithTrophy(level, spos(cx, cy, cz, 10, 3, 32), Direction.EAST, 4,
                "8-Ball Pool Trophy", "Invincible Champion of Cues and Cushions");
        placeWallStandWithTrophy(level, spos(cx, cy, cz, 10, 3, 36), Direction.EAST, 5,
                "Sisal PDC 180 Trophy", "Perfect Bullseye Darts in the Casino");

        level.setBlock(spos(cx, cy, cz, 10, 6, 34), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(spos(cx, cy, cz, 10, 6, 32), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(spos(cx, cy, cz, 10, 6, 36), Blocks.SEA_LANTERN.defaultBlockState(), 2);

        if (SystemBlocks.CASINO_CASHIER.isBound()) {
            level.setBlock(spos(cx, cy, cz, 18, 2, 28), SystemBlocks.CASINO_CASHIER.get().defaultBlockState().setValue(CasinoCashierBlock.FACING, Direction.SOUTH), 2);
        }
        if (SystemBlocks.CASINO_RECEPTION_GATE.isBound()) {
            level.setBlock(spos(cx, cy, cz, 19, 2, 28), SystemBlocks.CASINO_RECEPTION_GATE.get().defaultBlockState().setValue(CasinoReceptionGateBlock.FACING, Direction.SOUTH), 2);
        }
        spawnReceptionistIfAbsent(level, spos(cx, cy, cz, 18, 2, 27));

        BlockPos vaultChest1 = spos(cx, cy, cz, 17, 2, 28);
        level.setBlock(vaultChest1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        BlockEntity vBe1 = level.getBlockEntity(vaultChest1);
        if (vBe1 instanceof Container vChest) {
            vChest.setItem(0, new ItemStack(Items.GOLD_INGOT, 48));
            if (SystemItems.CASINO_CHIP_GOLD.isBound()) {
                vChest.setItem(1, new ItemStack(SystemItems.CASINO_CHIP_GOLD.get(), 16));
            }
            if (SystemItems.CASINO_CHIP_PLATINUM.isBound()) {
                vChest.setItem(2, new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get(), 4));
            }
            vChest.setItem(3, SystemItems.coin(50000));
        }

        int[] centralSlotX = {11, 13, 15, 17};
        for (int sx : centralSlotX) {
            placeSlotWithStool(level, spos(cx, cy, cz, sx, 2, 45), spos(cx, cy, cz, sx, 2, 46), Direction.SOUTH, false);
            placeSlotWithStool(level, spos(cx, cy, cz, sx, 2, 49), spos(cx, cy, cz, sx, 2, 48), Direction.NORTH, false);
            placeSlotWithStool(level, spos(cx, cy, cz, sx, 2, 51), spos(cx, cy, cz, sx, 2, 52), Direction.SOUTH, false);
            placeSlotWithStool(level, spos(cx, cy, cz, sx, 2, 55), spos(cx, cy, cz, sx, 2, 54), Direction.NORTH, false);
        }

        for (int sz = 44; sz <= 54; sz += 2) {
            placeSlotWithStool(level, spos(cx, cy, cz, 8, 2, sz), spos(cx, cy, cz, 9, 2, sz), Direction.EAST, false);
            placeSlotWithStool(level, spos(cx, cy, cz, 20, 2, sz), spos(cx, cy, cz, 19, 2, sz), Direction.WEST, false);
        }

        int[] galleryX = {12, 14, 16};
        for (int sx : galleryX) {
            placeSlotWithStool(level, spos(cx, cy, cz, sx, 2, 39), spos(cx, cy, cz, sx, 2, 40), Direction.SOUTH, false);
        }

        placePoolTable(level, spos(cx, cy, cz, 13, 2, 7), Direction.SOUTH, false);
        placePoolTable(level, spos(cx, cy, cz, 17, 2, 7), Direction.SOUTH, false);

        placeDartboard(level, spos(cx, cy, cz, 9, 3, 7), Direction.EAST);
        placeDartboard(level, spos(cx, cy, cz, 9, 3, 9), Direction.EAST);
        placeDartboard(level, spos(cx, cy, cz, 19, 3, 7), Direction.WEST);
        placeDartboard(level, spos(cx, cy, cz, 19, 3, 9), Direction.WEST);
        level.setBlock(spos(cx, cy, cz, 12, 1, 7), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(spos(cx, cy, cz, 12, 1, 9), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(spos(cx, cy, cz, 16, 1, 7), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(spos(cx, cy, cz, 16, 1, 9), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        placeBlackjackTable(level, spos(cx, cy, cz, 11, 2, 18), Direction.SOUTH, false);
        placeCasinoChair(level, spos(cx, cy, cz, 11, 2, 20), Direction.NORTH, false);
        placeCasinoChair(level, spos(cx, cy, cz, 10, 2, 20), Direction.NORTH, false);

        placeRouletteTable(level, spos(cx, cy, cz, 14, 2, 18), Direction.SOUTH, false);
        placeCasinoChair(level, spos(cx, cy, cz, 15, 2, 19), Direction.WEST, false);
        placeCasinoChair(level, spos(cx, cy, cz, 15, 2, 20), Direction.WEST, false);

        if (SystemBlocks.CASINO_VIP_GATE.isBound()) {
            level.setBlock(spos(cx, cy, cz, 18, 2, 16), SystemBlocks.CASINO_VIP_GATE.get().defaultBlockState().setValue(CasinoVipGateBlock.FACING, Direction.WEST), 2);
        }
        if (SystemBlocks.CASINO_VIP_BARRIER.isBound()) {
            level.setBlock(spos(cx, cy, cz, 18, 2, 15), SystemBlocks.CASINO_VIP_BARRIER.get().defaultBlockState(), 2);
            level.setBlock(spos(cx, cy, cz, 18, 2, 17), SystemBlocks.CASINO_VIP_BARRIER.get().defaultBlockState(), 2);
        }

        placeVipBlackjackTable(level, spos(cx, cy, cz, 20, 2, 13), Direction.SOUTH);
        placeCasinoChair(level, spos(cx, cy, cz, 20, 2, 15), Direction.NORTH, false);
        placeCasinoChair(level, spos(cx, cy, cz, 19, 2, 15), Direction.NORTH, false);

        placeVipRouletteTable(level, spos(cx, cy, cz, 22, 2, 13), Direction.EAST);
        placeCasinoChair(level, spos(cx, cy, cz, 23, 2, 15), Direction.NORTH, false);
        placeCasinoChair(level, spos(cx, cy, cz, 24, 2, 15), Direction.NORTH, false);
    }

    private static BlockPos spos(int cx, int cy, int cz, int sx, int sy, int sz) {
        return new BlockPos(cx - 10 + sx, cy + sy, cz - 34 + sz);
    }

    private static void placeSlotWithStool(ServerLevel level, BlockPos slotPos, BlockPos stoolPos, Direction facing, boolean illegal) {
        placeSlotMachine(level, slotPos, facing, illegal);
        placeCasinoChair(level, stoolPos, facing.getOpposite(), illegal);
    }

    public static void placeCasinoChair(ServerLevel level, BlockPos pos, Direction facing, boolean illegal) {
        var supplier = illegal ? SystemBlocks.CLANDESTINE_CHAIR : SystemBlocks.CASINO_CHAIR;
        if (!supplier.isBound()) return;
        level.setBlock(pos, supplier.get().defaultBlockState().setValue(CasinoChairBlock.FACING, facing), 3);
    }

    public static void placeBlackjackTable(ServerLevel level, BlockPos masterPos, Direction facing, boolean illegal) {
        var supplier = illegal ? SystemBlocks.ILLEGAL_BLACKJACK_TABLE : SystemBlocks.BLACKJACK_TABLE;
        if (!supplier.isBound()) return;
        BlockState base = supplier.get().defaultBlockState().setValue(BlackjackTableBlock.FACING, facing);
        for (BlackjackPart part : BlackjackPart.values()) {
            BlockPos partPos = BlackjackPart.getPartPos(masterPos, part, facing);
            level.setBlock(partPos, base.setValue(BlackjackTableBlock.PART, part), 3);
        }
    }

    public static void placeRouletteTable(ServerLevel level, BlockPos masterPos, Direction facing, boolean illegal) {
        var supplier = illegal ? SystemBlocks.ILLEGAL_ROULETTE_TABLE : SystemBlocks.ROULETTE_TABLE;
        if (!supplier.isBound()) return;
        BlockState base = supplier.get().defaultBlockState().setValue(RouletteTableBlock.FACING, facing);
        for (RoulettePart part : RoulettePart.values()) {
            BlockPos partPos = RoulettePart.getPartPos(masterPos, part, facing);
            level.setBlock(partPos, base.setValue(RouletteTableBlock.PART, part), 3);
        }
    }

    public static void placePoolTable(ServerLevel level, BlockPos masterPos, Direction facing, boolean illegal) {
        var supplier = illegal ? SystemBlocks.ILLEGAL_POOL_TABLE : SystemBlocks.POOL_TABLE;
        if (!supplier.isBound()) return;
        BlockState base = supplier.get().defaultBlockState().setValue(PoolTableBlock.FACING, facing);
        for (PoolTablePart part : PoolTablePart.values()) {
            BlockPos partPos = PoolTablePart.getPartPos(masterPos, part, facing);
            level.setBlock(partPos, base.setValue(PoolTableBlock.PART, part), 3);
        }
    }

    public static void placeSlotMachine(ServerLevel level, BlockPos pos, Direction facing, boolean illegal) {
        var supplier = illegal ? SystemBlocks.ILLEGAL_SLOT_MACHINE : SystemBlocks.SLOT_MACHINE;
        if (!supplier.isBound()) return;
        level.setBlock(pos, supplier.get().defaultBlockState()
                .setValue(SlotMachineBlock.FACING, facing)
                .setValue(SlotMachineBlock.PART, SlotMachineBlock.SlotMachinePart.FRONT), 3);
        BlockPos backPos = pos.relative(facing.getOpposite());
        level.setBlock(backPos, supplier.get().defaultBlockState()
                .setValue(SlotMachineBlock.FACING, facing)
                .setValue(SlotMachineBlock.PART, SlotMachineBlock.SlotMachinePart.BACK), 3);
    }

    public static void placeDartboard(ServerLevel level, BlockPos pos, Direction facing) {
        if (!SystemBlocks.DARTBOARD.isBound()) return;
        level.setBlock(pos, SystemBlocks.DARTBOARD.get().defaultBlockState().setValue(DartboardBlock.FACING, facing), 3);
    }

    public static void placeTrophy(ServerLevel level, BlockPos pos, Direction facing, int trophyType, String title, String desc) {
        var supplier = switch (trophyType) {
            case 2 -> SystemBlocks.BLACKJACK_TROPHY;
            case 3 -> SystemBlocks.ROULETTE_TROPHY;
            case 4 -> SystemBlocks.POOL_TROPHY;
            case 5 -> SystemBlocks.DARTS_TROPHY;
            default -> SystemBlocks.CASINO_TROPHY;
        };
        if (!supplier.isBound()) return;

        BlockState state = supplier.get().defaultBlockState()
                .setValue(CasinoTrophyBlock.FACING, facing)
                .setValue(CasinoTrophyBlock.TROPHY_TYPE, trophyType);
        level.setBlock(pos, state, 3);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CasinoTrophyBlockEntity cbe) {
            cbe.setTrophyType(trophyType);
            cbe.setWinnerName("Royal Palace of Teyvat");
            cbe.setAchievementDesc(desc);
            cbe.setAwardedDate("Official Version 2.1");
        }
    }

    public static void placeWallStandWithTrophy(ServerLevel level, BlockPos pos, Direction facing, int trophyType, String title, String desc) {
        if (!SystemBlocks.CASINO_WALL_DISPLAY_STAND.isBound()) return;
        level.setBlock(pos, SystemBlocks.CASINO_WALL_DISPLAY_STAND.get().defaultBlockState()
                .setValue(CasinoWallDisplayStandBlock.FACING, facing), 3);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CasinoWallDisplayStandBlockEntity sbe) {
            if (com.soybe.awakeningsystem.casino.CasinoTrophyManager.isTrophyClaimed(level, trophyType)) {
                // El trofeo ya fue reclamado por su campeón único, el soporte permanece vacío
                return;
            }
            var itemSupplier = switch (trophyType) {
                case 2 -> SystemItems.BLACKJACK_TROPHY;
                case 3 -> SystemItems.ROULETTE_TROPHY;
                case 4 -> SystemItems.POOL_TROPHY;
                case 5 -> SystemItems.DARTS_TROPHY;
                default -> SystemItems.CASINO_TROPHY;
            };
            if (itemSupplier.isBound()) {
                ItemStack stack = new ItemStack(itemSupplier.get());
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                tag.putInt("trophy_type", trophyType);
                tag.putString("winner_name", "Royal Palace of Teyvat");
                tag.putString("achievement_desc", desc);
                tag.putString("awarded_date", "Official Version 2.1");
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                sbe.setDisplayedItem(stack);
            }
        }
    }

    public static void placeVipBlackjackTable(ServerLevel level, BlockPos masterPos, Direction facing) {
        if (!SystemBlocks.VIP_BLACKJACK_TABLE.isBound()) return;
        BlockState base = SystemBlocks.VIP_BLACKJACK_TABLE.get().defaultBlockState().setValue(BlackjackTableBlock.FACING, facing);
        for (BlackjackPart part : BlackjackPart.values()) {
            BlockPos partPos = BlackjackPart.getPartPos(masterPos, part, facing);
            level.setBlock(partPos, base.setValue(BlackjackTableBlock.PART, part), 3);
        }
    }

    public static void placeVipRouletteTable(ServerLevel level, BlockPos masterPos, Direction facing) {
        if (!SystemBlocks.VIP_ROULETTE_TABLE.isBound()) return;
        BlockState base = SystemBlocks.VIP_ROULETTE_TABLE.get().defaultBlockState().setValue(RouletteTableBlock.FACING, facing);
        for (RoulettePart part : RoulettePart.values()) {
            BlockPos partPos = RoulettePart.getPartPos(masterPos, part, facing);
            level.setBlock(partPos, base.setValue(RouletteTableBlock.PART, part), 3);
        }
    }

    public static void spawnReceptionistIfAbsent(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        var existing = level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.SeriesNpcEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(4.0D),
                e -> e.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.CASINO_RECEPTIONIST);
        if (existing.isEmpty()) {
            var npcType = com.soybe.awakeningsystem.entity.ModEntities.SERIES_NPC.get();
            var npc = new com.soybe.awakeningsystem.entity.SeriesNpcEntity(npcType, level, com.soybe.awakeningsystem.entity.NpcProfile.CASINO_RECEPTIONIST);
            npc.setPos(x, y, z);
            npc.setHomePos(pos);
            npc.setYRot(180.0F);
            level.addFreshEntity(npc);
        }
    }
}
