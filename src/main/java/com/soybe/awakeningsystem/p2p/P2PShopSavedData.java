package com.soybe.awakeningsystem.p2p;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.p2p.network.SyncP2PShopPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Gestor de persistencia en disco del Mercado P2P de Awakening System.
 * Mantiene todas las ofertas activas de jugadores y las ganancias pendientes de reclamar.
 */
public class P2PShopSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_p2p_shop";

    private final Map<String, P2PListing> listings = new LinkedHashMap<>();
    private final Map<UUID, Long> pendingEarnings = new HashMap<>();

    public P2PShopSavedData() {}

    public static P2PShopSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(P2PShopSavedData::new, P2PShopSavedData::load, null),
                DATA_NAME
        );
    }

    public static P2PShopSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        P2PShopSavedData data = new P2PShopSavedData();
        if (tag.contains("Listings", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Listings", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                P2PListing listing = P2PListing.fromTag(list.getCompound(i), registries);
                if (listing != null && !listing.item().isEmpty()) {
                    data.listings.put(listing.id(), listing);
                }
            }
        }
        if (tag.contains("PendingEarnings", Tag.TAG_COMPOUND)) {
            CompoundTag earningsTag = tag.getCompound("PendingEarnings");
            for (String key : earningsTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long val = earningsTag.getLong(key);
                    if (val > 0) {
                        data.pendingEarnings.put(uuid, val);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (P2PListing listing : listings.values()) {
            listTag.add(listing.toTag(registries));
        }
        tag.put("Listings", listTag);

        CompoundTag earningsTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : pendingEarnings.entrySet()) {
            if (entry.getValue() > 0) {
                earningsTag.putLong(entry.getKey().toString(), entry.getValue());
            }
        }
        tag.put("PendingEarnings", earningsTag);

        return tag;
    }

    public synchronized boolean createListing(ServerPlayer seller, int slotIndex, int count, long price) {
        if (seller == null) return false;

        if (price < 1) {
            seller.sendSystemMessage(Component.translatable("awakening_system.p2p.err.invalid_price").withStyle(ChatFormatting.RED));
            return false;
        }

        ItemStack toList;
        if (slotIndex >= 0 && slotIndex < seller.getInventory().getContainerSize()) {
            ItemStack current = seller.getInventory().getItem(slotIndex);
            if (current.isEmpty()) {
                seller.sendSystemMessage(Component.translatable("awakening_system.p2p.err.no_item").withStyle(ChatFormatting.RED));
                return false;
            }
            int actualCount = Math.max(1, Math.min(count, current.getCount()));
            toList = seller.getInventory().removeItem(slotIndex, actualCount);
            seller.getInventory().setChanged();
            seller.inventoryMenu.broadcastChanges();
        } else {
            ItemStack held = seller.getMainHandItem();
            if (held.isEmpty()) {
                seller.sendSystemMessage(Component.translatable("awakening_system.p2p.err.no_item").withStyle(ChatFormatting.RED));
                return false;
            }
            int actualCount = Math.max(1, Math.min(count, held.getCount()));
            toList = held.split(actualCount);
            seller.getInventory().setChanged();
            seller.inventoryMenu.broadcastChanges();
        }

        if (toList.isEmpty()) {
            seller.sendSystemMessage(Component.translatable("awakening_system.p2p.err.no_item").withStyle(ChatFormatting.RED));
            return false;
        }

        long cappedPrice = Math.min(price, 999_999_999L);
        String id = UUID.randomUUID().toString();
        P2PListing listing = new P2PListing(
                id,
                seller.getUUID(),
                seller.getScoreboardName(),
                toList,
                cappedPrice,
                seller.level().getGameTime()
        );

        listings.put(id, listing);
        setDirty();

        seller.sendSystemMessage(Component.translatable("awakening_system.p2p.msg.published", toList.getHoverName(), cappedPrice).withStyle(ChatFormatting.GREEN));
        seller.playNotifySound(ModSounds.SYSTEM_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);

        syncToAll(seller.server);
        return true;
    }

    public synchronized boolean createListing(ServerPlayer seller, long price) {
        return createListing(seller, -1, 64, price);
    }

    public synchronized boolean buyListing(ServerPlayer buyer, String listingId) {
        if (buyer == null || listingId == null) return false;

        P2PListing listing = listings.get(listingId);
        if (listing == null) {
            buyer.sendSystemMessage(Component.translatable("awakening_system.p2p.err.already_sold").withStyle(ChatFormatting.RED));
            syncToPlayer(buyer);
            return false;
        }

        if (listing.sellerUuid().equals(buyer.getUUID())) {
            buyer.sendSystemMessage(Component.translatable("awakening_system.p2p.err.cannot_buy_own").withStyle(ChatFormatting.RED));
            return false;
        }

        SystemData buyerData = SystemEvents.data(buyer);
        if (buyerData.coins() < listing.price()) {
            buyer.sendSystemMessage(Component.translatable("awakening_system.p2p.err.not_enough_coins").withStyle(ChatFormatting.RED));
            return false;
        }

        buyerData.setCoins((int) Math.max(0, buyerData.coins() - listing.price()));
        AwakeningSystemMod.sync(buyer);

        ServerPlayer onlineSeller = buyer.server.getPlayerList().getPlayer(listing.sellerUuid());
        if (onlineSeller != null) {
            SystemData sellerData = SystemEvents.data(onlineSeller);
            sellerData.setCoins((int) Math.min(Integer.MAX_VALUE, (long) sellerData.coins() + listing.price()));
            AwakeningSystemMod.sync(onlineSeller);
            onlineSeller.sendSystemMessage(Component.translatable("awakening_system.p2p.msg.sold_notification", buyer.getScoreboardName(), listing.item().getHoverName(), listing.price()).withStyle(ChatFormatting.GOLD));
            onlineSeller.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.2F);
        } else {
            pendingEarnings.merge(listing.sellerUuid(), listing.price(), Long::sum);
        }

        ItemStack boughtStack = listing.item().copy();
        if (!buyer.getInventory().add(boughtStack)) {
            buyer.drop(boughtStack, false);
        }

        listings.remove(listingId);
        setDirty();

        buyer.sendSystemMessage(Component.translatable("awakening_system.p2p.msg.bought", listing.item().getHoverName(), listing.price()).withStyle(ChatFormatting.GREEN));
        buyer.playNotifySound(ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

        syncToAll(buyer.server);
        return true;
    }

    public synchronized boolean cancelListing(ServerPlayer seller, String listingId) {
        if (seller == null || listingId == null) return false;

        P2PListing listing = listings.get(listingId);
        if (listing == null) return false;

        if (!listing.sellerUuid().equals(seller.getUUID())) {
            return false;
        }

        listings.remove(listingId);
        setDirty();

        ItemStack itemToReturn = listing.item().copy();
        if (!seller.getInventory().add(itemToReturn)) {
            seller.drop(itemToReturn, false);
        }

        seller.sendSystemMessage(Component.translatable("awakening_system.p2p.msg.cancelled").withStyle(ChatFormatting.YELLOW));
        seller.playNotifySound(ModSounds.SYSTEM_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

        syncToAll(seller.server);
        return true;
    }

    public synchronized boolean claimEarnings(ServerPlayer player) {
        if (player == null) return false;

        long amount = pendingEarnings.getOrDefault(player.getUUID(), 0L);
        if (amount > 0) {
            pendingEarnings.remove(player.getUUID());
            setDirty();

            SystemData d = SystemEvents.data(player);
            d.setCoins((int) Math.min(Integer.MAX_VALUE, (long) d.coins() + amount));
            AwakeningSystemMod.sync(player);

            player.sendSystemMessage(Component.translatable("awakening_system.p2p.msg.claimed", amount).withStyle(ChatFormatting.GOLD));
            player.playNotifySound(ModSounds.SYSTEM_COINS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

            syncToPlayer(player);
            return true;
        }
        return false;
    }

    public void syncToPlayer(ServerPlayer player) {
        if (player == null) return;
        long earnings = pendingEarnings.getOrDefault(player.getUUID(), 0L);
        List<P2PListing> all = new ArrayList<>(listings.values());
        PacketDistributor.sendToPlayer(player, new SyncP2PShopPayload(all, earnings));
    }

    public void syncToAll(MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }
}
