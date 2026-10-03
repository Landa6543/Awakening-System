package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBestiaryPayload() implements CustomPacketPayload {
    public static final Type<OpenBestiaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_bestiary"));

    public static final StreamCodec<FriendlyByteBuf, OpenBestiaryPayload> STREAM_CODEC = StreamCodec.unit(new OpenBestiaryPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
