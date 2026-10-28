package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CasinoExchangeC2SPayload(int action, int tierIndex, int amount) implements CustomPacketPayload {
    public static final Type<CasinoExchangeC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_exchange"));

    public static final StreamCodec<ByteBuf, CasinoExchangeC2SPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CasinoExchangeC2SPayload::action,
            ByteBufCodecs.VAR_INT, CasinoExchangeC2SPayload::tierIndex,
            ByteBufCodecs.VAR_INT, CasinoExchangeC2SPayload::amount,
            CasinoExchangeC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
