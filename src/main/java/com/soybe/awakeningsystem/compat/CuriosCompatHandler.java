package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manejador de compatibilidad para Curios API y Accessories.
 * Previene la pérdida total de ítems equipados en accesorios (Curios) al morir dentro de mazmorras del mod.
 */
public final class CuriosCompatHandler {

    private static final Map<UUID, List<CuriosSlotEntry>> MEMORY_PRESERVED_CURIOS = new ConcurrentHashMap<>();

    private record CuriosSlotEntry(int slot, ItemStack stack) {}

    private CuriosCompatHandler() {}

    /**
     * Guarda de forma segura los ítems de Curios equipados antes de la muerte del jugador en una mazmorra.
     */
    public static void saveCuriosOnDeath(ServerPlayer player) {
        if (player == null) return;
        UUID uuid = player.getUUID();

        if (ModList.get().isLoaded("curios")) {
            try {
                Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getInvMethod = apiClass.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                Object optInv = getInvMethod.invoke(null, player);

                if (optInv instanceof Optional<?> opt && opt.isPresent()) {
                    Object handler = opt.get();
                    Method getEquippedMethod = handler.getClass().getMethod("getEquippedCurios");
                    Object itemHandler = getEquippedMethod.invoke(handler);

                    if (itemHandler != null) {
                        Method getSlotsMethod = itemHandler.getClass().getMethod("getSlots");
                        Method getStackMethod = itemHandler.getClass().getMethod("getStackInSlot", int.class);

                        int slots = (int) getSlotsMethod.invoke(itemHandler);
                        List<CuriosSlotEntry> entries = new ArrayList<>();
                        ListTag listTag = new ListTag();

                        for (int i = 0; i < slots; i++) {
                            ItemStack stack = (ItemStack) getStackMethod.invoke(itemHandler, i);
                            if (stack != null && !stack.isEmpty()) {
                                entries.add(new CuriosSlotEntry(i, stack.copy()));
                                CompoundTag slotTag = new CompoundTag();
                                slotTag.putInt("Slot", i);
                                slotTag.put("Item", stack.save(player.registryAccess()));
                                listTag.add(slotTag);
                            }
                        }

                        if (!entries.isEmpty()) {
                            MEMORY_PRESERVED_CURIOS.put(uuid, entries);
                            player.getPersistentData().put("dungeon_preserved_curios", listTag);
                            AwakeningSystemMod.LOGGER.info("Curios preservados exitosamente para {}: {} ítems", player.getName().getString(), entries.size());
                        }
                    }
                }
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.warn("No se pudo preservar Curios por reflexión: {}", t.getMessage());
            }
        }
    }

    /**
     * Restaura los ítems de Curios guardados al reaparecer tras morir en una mazmorra.
     */
    public static void restoreCuriosOnRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        if (newPlayer == null) return;
        UUID uuid = newPlayer.getUUID();

        List<CuriosSlotEntry> entries = MEMORY_PRESERVED_CURIOS.remove(uuid);
        if (entries == null && oldPlayer != null && oldPlayer.getPersistentData().contains("dungeon_preserved_curios", Tag.TAG_LIST)) {
            ListTag listTag = oldPlayer.getPersistentData().getList("dungeon_preserved_curios", Tag.TAG_COMPOUND);
            entries = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag slotTag = listTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                ItemStack stack = ItemStack.parse(newPlayer.registryAccess(), slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    entries.add(new CuriosSlotEntry(slot, stack));
                }
            }
        }

        if (oldPlayer != null) {
            oldPlayer.getPersistentData().remove("dungeon_preserved_curios");
        }
        newPlayer.getPersistentData().remove("dungeon_preserved_curios");

        if (entries == null || entries.isEmpty()) return;

        if (ModList.get().isLoaded("curios")) {
            try {
                Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getInvMethod = apiClass.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                Object optInv = getInvMethod.invoke(null, newPlayer);

                if (optInv instanceof Optional<?> opt && opt.isPresent()) {
                    Object handler = opt.get();
                    Method getEquippedMethod = handler.getClass().getMethod("getEquippedCurios");
                    Object itemHandler = getEquippedMethod.invoke(handler);

                    if (itemHandler != null) {
                        Method getSlotsMethod = itemHandler.getClass().getMethod("getSlots");
                        Method setStackMethod = itemHandler.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);

                        int totalSlots = (int) getSlotsMethod.invoke(itemHandler);
                        int restoredCount = 0;

                        for (CuriosSlotEntry entry : entries) {
                            if (entry.slot >= 0 && entry.slot < totalSlots) {
                                setStackMethod.invoke(itemHandler, entry.slot, entry.stack.copy());
                                restoredCount++;
                            } else {
                                newPlayer.getInventory().add(entry.stack.copy());
                            }
                        }

                        AwakeningSystemMod.LOGGER.info("Curios restaurados exitosamente para {}: {} ítems", newPlayer.getName().getString(), restoredCount);
                    }
                }
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.warn("No se pudo restaurar Curios por reflexión: {}", t.getMessage());
                for (CuriosSlotEntry entry : entries) {
                    newPlayer.getInventory().add(entry.stack.copy());
                }
            }
        } else {
            for (CuriosSlotEntry entry : entries) {
                newPlayer.getInventory().add(entry.stack.copy());
            }
        }
    }
}
