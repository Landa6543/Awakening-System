package com.soybe.awakeningsystem.blackmarket.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlackMarketActionC2SPayload(int tradeId, int quantity) implements CustomPacketPayload {
    public static final Type<BlackMarketActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "black_market_action"));

    public static final StreamCodec<FriendlyByteBuf, BlackMarketActionC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeInt(p.tradeId());
                buf.writeInt(p.quantity());
            },
            buf -> new BlackMarketActionC2SPayload(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
