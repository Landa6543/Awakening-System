package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivateStigmaPayload() implements CustomPacketPayload {
    public static final Type<ActivateStigmaPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "activate_stigma"));

    public static final StreamCodec<FriendlyByteBuf, ActivateStigmaPayload> STREAM_CODEC =
            StreamCodec.unit(new ActivateStigmaPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
