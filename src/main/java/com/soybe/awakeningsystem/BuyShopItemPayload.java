package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyShopItemPayload(String entryId) implements CustomPacketPayload {
    public static final Type<BuyShopItemPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "buy_shop_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BuyShopItemPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(48), BuyShopItemPayload::entryId, BuyShopItemPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
