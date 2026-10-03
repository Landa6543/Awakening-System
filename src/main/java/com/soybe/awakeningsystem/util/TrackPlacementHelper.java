package com.soybe.awakeningsystem.util;

import com.soybe.awakeningsystem.block.SubwayRailTrackBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor del tendido automático de vías en el servidor para Awakening System (Estilo Create Track Placer).
 * Coloca tramos continuos de vías funcionales (SubwayRailTrackBlock), conectando rectas, curvas de 90° y pendientes.
 */
public final class TrackPlacementHelper {

    private TrackPlacementHelper() {}

    public static void executeTrackPlacement(ServerPlayer player, BlockPos startPos, BlockPos endPos, boolean isWideTrack) {
        ServerLevel level = player.serverLevel();
        if (startPos.equals(endPos)) return;

        int dist = Math.abs(endPos.getX() - startPos.getX()) + Math.abs(endPos.getZ() - startPos.getZ());
        if (dist > 48) {
            player.displayClientMessage(Component.literal("§c⚠ [Tendido Create] La distancia máxima es de 48 bloques."), true);
            level.playSound(null, startPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 0.8F);
            return;
        }

        List<BlockPos> path = calculatePath(startPos, endPos);
        if (path.isEmpty()) return;

        Block trackBlock = SystemBlocks.SUBWAY_RAIL_TRACK.get();
        int required = path.size();
        int available = player.isCreative() ? required : countItemsInInventory(player, trackBlock);

        if (available < required) {
            player.displayClientMessage(Component.literal(String.format("§c[Tendido Create] ⚠ Necesitas §e%d §cvías pero solo tienes §e%d §cen inventario.", required, available)), true);
            level.playSound(null, startPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 0.8F);
            return;
        }

        int placedCount = 0;

        for (BlockPos current : path) {
            BlockState targetState = level.getBlockState(current);
            if (targetState.isAir() || targetState.canBeReplaced() || targetState.getBlock() instanceof SubwayRailTrackBlock) {
                // Colocar la vía con flag 3 para actualizar conexiones y curvas automáticamente
                level.setBlock(current, trackBlock.defaultBlockState(), 3);
                placedCount++;
                if (!player.isCreative()) {
                    consumeOneItem(player, trackBlock);
                }
            }
        }

        if (placedCount > 0) {
            level.playSound(null, endPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.7F, 1.3F);
            level.playSound(null, endPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, endPos.getX() + 0.5D, endPos.getY() + 0.6D, endPos.getZ() + 0.5D, 18, 0.8D, 0.3D, 0.8D, 0.05D);
            player.displayClientMessage(Component.literal(String.format("§6✦ [Trazado Create] §a¡Tendido completado! §e%d §fvías colocadas con éxito.", placedCount)), false);
        }
    }

    public static List<BlockPos> calculatePath(BlockPos start, BlockPos end) {
        List<BlockPos> path = new ArrayList<>();
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        int dy = end.getY() - start.getY();

        if (dx == 0 && dz == 0) return path;

        if (dx == 0) {
            int stepZ = dz > 0 ? 1 : -1;
            int count = Math.abs(dz);
            for (int i = 1; i <= count; i++) {
                int yOff = (count > 0) ? (dy * i) / count : 0;
                path.add(start.offset(0, yOff, i * stepZ));
            }
            return path;
        }

        if (dz == 0) {
            int stepX = dx > 0 ? 1 : -1;
            int count = Math.abs(dx);
            for (int i = 1; i <= count; i++) {
                int yOff = (count > 0) ? (dy * i) / count : 0;
                path.add(start.offset(i * stepX, yOff, 0));
            }
            return path;
        }

        // Tramo con curva en L (90 grados)
        if (Math.abs(dz) >= Math.abs(dx)) {
            int stepZ = dz > 0 ? 1 : -1;
            int stepX = dx > 0 ? 1 : -1;
            int zCount = Math.abs(dz);
            int xCount = Math.abs(dx);

            for (int i = 1; i <= zCount; i++) {
                path.add(start.offset(0, 0, i * stepZ));
            }
            BlockPos corner = start.offset(0, 0, zCount * stepZ);
            for (int j = 1; j <= xCount; j++) {
                path.add(corner.offset(j * stepX, dy, 0));
            }
        } else {
            int stepX = dx > 0 ? 1 : -1;
            int stepZ = dz > 0 ? 1 : -1;
            int xCount = Math.abs(dx);
            int zCount = Math.abs(dz);

            for (int i = 1; i <= xCount; i++) {
                path.add(start.offset(i * stepX, 0, 0));
            }
            BlockPos corner = start.offset(xCount * stepX, 0, 0);
            for (int j = 1; j <= zCount; j++) {
                path.add(corner.offset(0, dy, j * stepZ));
            }
        }

        return path;
    }

    private static int countItemsInInventory(ServerPlayer player, Block block) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(block.asItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeOneItem(ServerPlayer player, Block block) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(block.asItem())) {
                stack.shrink(1);
                return;
            }
        }
    }
}
