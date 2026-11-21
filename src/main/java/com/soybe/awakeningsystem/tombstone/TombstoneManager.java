package com.soybe.awakeningsystem.tombstone;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TombstoneBlock;
import com.soybe.awakeningsystem.block.entity.TombstoneBlockEntity;
import com.soybe.awakeningsystem.compat.CuriosCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Gestor del Sistema de Tumbas (Deadlock Tombstone) y Modo Fantasma.
 * Administra el guardado seguro del inventario, armadura, curios y EXP en la entidad Lápida,
 * y coordina la transición hacia y desde el Modo Fantasma al reaparecer.
 */
public final class TombstoneManager {

    private TombstoneManager() {}

    public static boolean isGhostMode(Player player) {
        if (player == null) return false;
        try {
            SystemData data = player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null && data.isGhostMode()) {
                return true;
            }
        } catch (Exception ignored) {}
        return player.getPersistentData().getBoolean("awakening_ghost_mode");
    }

    public static void setGhostMode(ServerPlayer player, boolean ghost) {
        if (player == null) return;
        SystemData data = SystemEvents.data(player);
        data.setGhostMode(ghost);
        if (!ghost) {
            data.setTombstonePos(BlockPos.ZERO);
            data.setTombstoneDimension("");
            player.getPersistentData().remove("awakening_ghost_mode");
            player.getPersistentData().remove("awakening_has_tombstone");
        } else {
            player.getPersistentData().putBoolean("awakening_ghost_mode", true);
        }
        AwakeningSystemMod.sync(player);
    }

    public static void handlePlayerDeath(ServerPlayer player) {
        if (player == null || player.level().isClientSide) return;

        ServerLevel level = player.serverLevel();
        BlockPos deathPos = findSafeTombstonePos(level, player.blockPosition());

        // 1. Guardar inventario completo (0..35 main, 36..39 armor, 40 offhand = 41 slots)
        var inv = player.getInventory();
        NonNullList<ItemStack> savedItems = NonNullList.withSize(41, ItemStack.EMPTY);
        boolean hasItems = false;
        for (int i = 0; i < savedItems.size() && i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                savedItems.set(i, stack.copy());
                hasItems = true;
            }
        }

        // 2. Guardar accesorios de Curios API
        CompoundTag savedCurios = CuriosCompat.saveCurios(player);

        // 3. Guardar experiencia
        int expLevel = player.experienceLevel;
        int totalExp = player.totalExperience;
        float expProgress = player.experienceProgress;

        // Limpiar inventario del jugador para que no dropee ítems al suelo
        inv.clearContent();
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);

        // 4. Colocar el bloque Tombstone en la posición segura con orientación hacia el jugador
        Direction facing = player.getDirection().getOpposite();
        BlockState tombState = SystemBlocks.TOMBSTONE.get().defaultBlockState().setValue(TombstoneBlock.FACING, facing);
        level.setBlock(deathPos, tombState, 3);

        // Reproducir sonido de emergencia espectral de la lápida (alcance cercano)
        level.playSound(null, deathPos.getX() + 0.5D, deathPos.getY() + 0.5D, deathPos.getZ() + 0.5D,
                ModSounds.TOMBSTONE_SPAWN.get(), SoundSource.BLOCKS, 0.8F, 1.0F);

        if (level.getBlockEntity(deathPos) instanceof TombstoneBlockEntity tombstone) {
            tombstone.setOwnerUUID(player.getUUID());
            tombstone.setOwnerName(player.getGameProfile().getName());
            for (int i = 0; i < savedItems.size(); i++) {
                tombstone.getStoredInventory().set(i, savedItems.get(i));
            }
            tombstone.setStoredCurios(savedCurios);
            tombstone.setStoredExperience(totalExp, expLevel, expProgress);
            tombstone.setChanged();
            level.sendBlockUpdated(deathPos, tombState, tombState, 3);
        }

        // 5. Registrar coordenadas en SystemData para el Modo Fantasma
        SystemData data = SystemEvents.data(player);
        data.setTombstoneDimension(level.dimension().location().toString());
        data.setTombstonePos(deathPos);
        player.getPersistentData().putBoolean("awakening_has_tombstone", true);
        player.getPersistentData().putLong("awakening_tombstone_pos", deathPos.asLong());
        player.getPersistentData().putString("awakening_tombstone_dim", level.dimension().location().toString());

        AwakeningSystemMod.LOGGER.info("Tombstone spawned for {} at {} in {}",
                player.getGameProfile().getName(), deathPos, level.dimension().location());
    }

    public static void handlePlayerRespawn(ServerPlayer player) {
        if (player == null) return;

        SystemData data = SystemEvents.data(player);
        boolean hasTombstone = player.getPersistentData().getBoolean("awakening_has_tombstone")
                || (data.getTombstonePos() != null && !data.getTombstonePos().equals(BlockPos.ZERO));

        if (hasTombstone) {
            player.getPersistentData().remove("awakening_has_tombstone");
            // a player who turned ghosts off in their settings comes back as themself; the tombstone still keeps their things
            boolean ghost = GhostPreference.ghostOnDeath(player);
            if (ghost) setGhostMode(player, true);
            BlockPos tPos = data.getTombstonePos();
            if (tPos == null || tPos.equals(BlockPos.ZERO)) {
                if (player.getPersistentData().contains("awakening_tombstone_pos")) {
                    tPos = BlockPos.of(player.getPersistentData().getLong("awakening_tombstone_pos"));
                    data.setTombstonePos(tPos);
                }
            }

            String dim = data.getTombstoneDimension();
            if (dim == null || dim.isEmpty()) {
                dim = player.getPersistentData().getString("awakening_tombstone_dim");
            }

            // Mensajes inmersivos del Sistema
            if (ghost) player.sendSystemMessage(Component.translatable("awakening_system.tombstone.ghost_enter"));
            if (tPos != null && !tPos.equals(BlockPos.ZERO)) {
                player.sendSystemMessage(Component.translatable("awakening_system.tombstone.ghost_coords",
                        tPos.getX(), tPos.getY(), tPos.getZ(), dim.isEmpty() ? "Mundo" : dim));
            }
            if (ghost) player.sendSystemMessage(Component.translatable("awakening_system.tombstone.ghost_instructions"));
        }
    }

    private static BlockPos findSafeTombstonePos(ServerLevel level, BlockPos initial) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        // 1. Evitar el vacío absoluto (por ejemplo caída en el Fin o vacíos profundos)
        if (initial.getY() < minY + 2) {
            BlockPos safePos = new BlockPos(initial.getX(), minY + 4, initial.getZ());
            // Colocar plataforma etérea de piedra para que la tumba no caiga al vacío
            level.setBlockAndUpdate(safePos.below(), Blocks.CRYING_OBSIDIAN.defaultBlockState());
            return safePos;
        }

        BlockPos current = initial;

        // 2. Si está en lava, subir hasta la superficie de la lava
        while (current.getY() < maxY - 2 && level.getFluidState(current).is(Fluids.LAVA)) {
            current = current.above();
        }

        // 3. Si está en el aire, bajar hasta encontrar un bloque de soporte sólido
        int scanDown = 0;
        while (current.getY() > minY + 2 && level.isEmptyBlock(current.below()) && scanDown < 32) {
            current = current.below();
            scanDown++;
        }

        // Si después de escanear 32 bloques hacia abajo sigue flotando en el vacío, crear soporte
        if (level.isEmptyBlock(current.below())) {
            level.setBlockAndUpdate(current.below(), Blocks.DEEPSLATE_TILES.defaultBlockState());
        }

        return current;
    }
}
