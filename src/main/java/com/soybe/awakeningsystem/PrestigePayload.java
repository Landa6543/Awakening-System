package com.soybe.awakeningsystem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PrestigePayload() implements CustomPacketPayload {
    public static final Type<PrestigePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "prestige"));
    public static final StreamCodec<ByteBuf, PrestigePayload> STREAM_CODEC = StreamCodec.unit(new PrestigePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
