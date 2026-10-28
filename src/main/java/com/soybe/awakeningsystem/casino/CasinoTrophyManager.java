package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.block.entity.CasinoWallDisplayStandBlockEntity;
import com.soybe.awakeningsystem.item.CasinoTrophyItem;
import com.soybe.awakeningsystem.world.TeyvatCasinoBuilder;
import com.soybe.awakeningsystem.world.TeyvatWorldGenManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CasinoTrophyManager {

    private CasinoTrophyManager() {}

    public static class TrophyClaimData extends SavedData {
        public static final String DATA_NAME = "awakening_casino_trophies";

        public final Set<Integer> claimedTrophies = Collections.newSetFromMap(new ConcurrentHashMap<>());
        public final Map<Integer, String> winners = new ConcurrentHashMap<>();
        public final Map<Integer, String> dates = new ConcurrentHashMap<>();
        public final Map<Integer, String> achievements = new ConcurrentHashMap<>();

        public static TrophyClaimData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(TrophyClaimData::new, TrophyClaimData::load, null),
                    DATA_NAME
            );
        }

        public static TrophyClaimData load(CompoundTag tag, HolderLookup.Provider registries) {
            TrophyClaimData data = new TrophyClaimData();
            if (tag.contains("ClaimedList", Tag.TAG_LIST)) {
                ListTag list = tag.getList("ClaimedList", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entry = list.getCompound(i);
                    int type = entry.getInt("Type");
                    data.claimedTrophies.add(type);
                    if (entry.contains("Winner")) data.winners.put(type, entry.getString("Winner"));
                    if (entry.contains("Date")) data.dates.put(type, entry.getString("Date"));
                    if (entry.contains("Achievement")) data.achievements.put(type, entry.getString("Achievement"));
                }
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (Integer type : claimedTrophies) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Type", type);
                entry.putString("Winner", winners.getOrDefault(type, ""));
                entry.putString("Date", dates.getOrDefault(type, ""));
                entry.putString("Achievement", achievements.getOrDefault(type, ""));
                list.add(entry);
            }
            tag.put("ClaimedList", list);
            return tag;
        }
    }

    public static boolean isTrophyClaimed(ServerLevel level, int trophyType) {
        if (level == null || level.getServer() == null) return false;
        return TrophyClaimData.get(level.getServer()).claimedTrophies.contains(trophyType);
    }

    public static String getTrophyWinner(ServerLevel level, int trophyType) {
        if (level == null || level.getServer() == null) return "";
        return TrophyClaimData.get(level.getServer()).winners.getOrDefault(trophyType, "");
    }

    public static Component getTrophyComponent(int trophyType) {
        return switch (trophyType) {
            case 1 -> Component.translatable("item.awakening_system.casino_trophy");
            case 2 -> Component.translatable("item.awakening_system.blackjack_trophy");
            case 3 -> Component.translatable("item.awakening_system.roulette_trophy");
            case 4 -> Component.translatable("item.awakening_system.pool_trophy");
            case 5 -> Component.translatable("item.awakening_system.darts_trophy");
            default -> Component.translatable("item.awakening_system.casino_trophy.cat_default");
        };
    }

    public static String getTrophyTitle(int trophyType) {
        return getTrophyComponent(trophyType).getString();
    }

    /**
     * Intenta reclamar un trofeo único para el jugador.
     * Si no ha sido reclamado previamente, se retira de la vitrina del Casino y se le entrega al jugador.
     */
    public static boolean tryClaimTrophy(ServerLevel level, ServerPlayer player, int trophyType, String achievementDesc) {
        if (level == null || player == null || level.getServer() == null) return false;

        TrophyClaimData data = TrophyClaimData.get(level.getServer());
        if (data.claimedTrophies.contains(trophyType)) {
            // Ya fue reclamado previamente
            String priorWinner = data.winners.getOrDefault(trophyType, "Otro jugador");
            player.displayClientMessage(Component.translatable("casino.awakening_system.trophy_already_claimed", getTrophyComponent(trophyType), priorWinner), false);
            return false;
        }

        // Registrar reclamo
        String playerName = player.getGameProfile().getName();
        String today = LocalDate.now().toString();
        data.claimedTrophies.add(trophyType);
        data.winners.put(trophyType, playerName);
        data.dates.put(trophyType, today);
        data.achievements.put(trophyType, achievementDesc);
        data.setDirty();

        // Buscar y retirar el trofeo del soporte en el Gran Casino
        removeTrophyFromCasinoDisplay(level, trophyType);

        // Crear el trofeo único personalizado
        ItemStack trophy = CasinoTrophyItem.createTrophy(trophyType, playerName, achievementDesc);
        if (!player.getInventory().add(trophy)) {
            player.drop(trophy, false);
        }

        // Efectos y fanfarria al jugador
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 50, 0.6D, 0.8D, 0.6D, 0.15D);

        // Anuncio a todo el servidor
        Component ann1 = Component.translatable("gui.awakening_system.casino.trophy_announcement_1", playerName, getTrophyComponent(trophyType));
        Component ann2 = Component.translatable("gui.awakening_system.casino.trophy_announcement_2", achievementDesc);
        level.getServer().getPlayerList().broadcastSystemMessage(ann1, false);
        level.getServer().getPlayerList().broadcastSystemMessage(ann2, false);

        return true;
    }

    private static void removeTrophyFromCasinoDisplay(ServerLevel level, int trophyType) {
        var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getServer());
        if (tData == null) return;

        List<BlockPos> origins = new ArrayList<>(tData.casinos);
        BlockPos named = tData.getNamedStructure("casino_origin");
        if (named != null && !origins.contains(named)) origins.add(named);
        if (origins.isEmpty()) origins.add(TeyvatCasinoBuilder.CASINO_POS);

        for (BlockPos origin : origins) {
            int cx = origin.getX();
            int cy = origin.getY();
            int cz = origin.getZ();

            // Ubicación esperada según TeyvatCasinoBuilder
            BlockPos expectedPos = switch (trophyType) {
                case 1 -> spos(cx, cy, cz, 10, 3, 34);
                case 2 -> spos(cx, cy, cz, 10, 3, 33);
                case 3 -> spos(cx, cy, cz, 10, 3, 35);
                case 4 -> spos(cx, cy, cz, 10, 3, 32);
                case 5 -> spos(cx, cy, cz, 10, 3, 36);
                default -> null;
            };

            if (expectedPos != null) {
                if (clearStandIfMatches(level, expectedPos, trophyType)) {
                    return;
                }
            }

            // Si no estaba en la posición exacta, buscar en el área de vitrinas del casino
            for (int dx = cx - 14; dx <= cx + 35; dx++) {
                for (int dz = cz - 38; dz <= cz + 32; dz++) {
                    for (int dy = cy; dy <= cy + 8; dy++) {
                        BlockPos checkPos = new BlockPos(dx, dy, dz);
                        if (clearStandIfMatches(level, checkPos, trophyType)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean clearStandIfMatches(ServerLevel level, BlockPos pos, int trophyType) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CasinoWallDisplayStandBlockEntity standBe) {
            ItemStack displayed = standBe.getDisplayedItem();
            if (!displayed.isEmpty()) {
                int displayedType = extractTrophyType(displayed);
                if (displayedType == trophyType) {
                    standBe.setDisplayedItem(ItemStack.EMPTY);
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);

                    // Partículas de desvanecimiento mágico
                    level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 35, 0.4D, 0.4D, 0.4D, 0.12D);
                    level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20, 0.3D, 0.3D, 0.3D, 0.05D);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.2F);
                    return true;
                }
            }
        }
        return false;
    }

    private static int extractTrophyType(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            if (tag.contains("trophy_type")) return tag.getInt("trophy_type");
        }
        return -1;
    }

    private static BlockPos spos(int cx, int cy, int cz, int dx, int dy, int dz) {
        return new BlockPos(cx + dx, cy + dy, cz + dz);
    }
}
