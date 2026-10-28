package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenCasinoCashierPayload(long moraBalance) implements CustomPacketPayload {
    public static final Type<OpenCasinoCashierPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_casino_cashier"));

    public static final StreamCodec<ByteBuf, OpenCasinoCashierPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, OpenCasinoCashierPayload::moraBalance,
            OpenCasinoCashierPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
