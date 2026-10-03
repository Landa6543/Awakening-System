package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SellShopItemPayload(String entryId, boolean sellAll) implements CustomPacketPayload {
    public static final Type<SellShopItemPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sell_shop_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SellShopItemPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(48), SellShopItemPayload::entryId,
            ByteBufCodecs.BOOL, SellShopItemPayload::sellAll,
            SellShopItemPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
