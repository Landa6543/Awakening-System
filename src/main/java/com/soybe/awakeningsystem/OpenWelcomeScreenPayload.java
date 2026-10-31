package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenWelcomeScreenPayload() implements CustomPacketPayload {
    public static final Type<OpenWelcomeScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_welcome_screen"));
    public static final StreamCodec<FriendlyByteBuf, OpenWelcomeScreenPayload> STREAM_CODEC = StreamCodec.unit(new OpenWelcomeScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
