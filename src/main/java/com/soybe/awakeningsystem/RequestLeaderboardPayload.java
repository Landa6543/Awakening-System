package com.soybe.awakeningsystem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestLeaderboardPayload() implements CustomPacketPayload {
    public static final Type<RequestLeaderboardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "request_leaderboard"));
    public static final StreamCodec<ByteBuf, RequestLeaderboardPayload> STREAM_CODEC = StreamCodec.unit(new RequestLeaderboardPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
