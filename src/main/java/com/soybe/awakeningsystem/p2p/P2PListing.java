package com.soybe.awakeningsystem.p2p;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Representa una oferta de compra/venta en el Mercado de Jugadores (P2P).
 * Almacena el ítem exacto con todos sus componentes de datos, encantamientos y NBT.
 */
public record P2PListing(
        String id,
        UUID sellerUuid,
        String sellerName,
        ItemStack item,
        long price,
        long createdAt
) {
    public CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putUUID("SellerUuid", sellerUuid);
        tag.putString("SellerName", sellerName);
        if (!item.isEmpty()) {
            tag.put("Item", item.save(registries));
        }
        tag.putLong("Price", price);
        tag.putLong("CreatedAt", createdAt);
        return tag;
    }

    public static P2PListing fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        String id = tag.getString("Id");
        UUID sellerUuid = tag.getUUID("SellerUuid");
        String sellerName = tag.getString("SellerName");
        ItemStack item = ItemStack.EMPTY;
        if (tag.contains("Item")) {
            item = ItemStack.parse(registries, tag.getCompound("Item")).orElse(ItemStack.EMPTY);
        }
        long price = tag.getLong("Price");
        long createdAt = tag.getLong("CreatedAt");
        return new P2PListing(id, sellerUuid, sellerName, item, price, createdAt);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUUID(sellerUuid);
        buf.writeUtf(sellerName);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
        buf.writeVarLong(price);
        buf.writeVarLong(createdAt);
    }

    public static P2PListing decode(RegistryFriendlyByteBuf buf) {
        String id = buf.readUtf();
        UUID sellerUuid = buf.readUUID();
        String sellerName = buf.readUtf();
        ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        long price = buf.readVarLong();
        long createdAt = buf.readVarLong();
        return new P2PListing(id, sellerUuid, sellerName, item, price, createdAt);
    }
}
