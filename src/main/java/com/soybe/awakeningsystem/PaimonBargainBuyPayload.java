package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PaimonBargainBuyPayload(int offerId, int count) implements CustomPacketPayload {
    public static final Type<PaimonBargainBuyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "paimon_bargain_buy"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PaimonBargainBuyPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.offerId);
                buf.writeVarInt(p.count);
            },
            buf -> new PaimonBargainBuyPayload(buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
