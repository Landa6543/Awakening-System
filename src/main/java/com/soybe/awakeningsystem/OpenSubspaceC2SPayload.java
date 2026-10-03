package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenSubspaceC2SPayload() implements CustomPacketPayload {
    public static final Type<OpenSubspaceC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_subspace"));

    public static final StreamCodec<FriendlyByteBuf, OpenSubspaceC2SPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenSubspaceC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
