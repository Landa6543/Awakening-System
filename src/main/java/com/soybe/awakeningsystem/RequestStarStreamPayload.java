package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestStarStreamPayload() implements CustomPacketPayload {
    public static final Type<RequestStarStreamPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "request_star_stream"));

    public static final StreamCodec<FriendlyByteBuf, RequestStarStreamPayload> STREAM_CODEC = StreamCodec.unit(new RequestStarStreamPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
