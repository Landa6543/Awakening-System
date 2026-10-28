package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenVisionSelectionPayload() implements CustomPacketPayload {
    public static final Type<OpenVisionSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_vision_selection"));

    public static final StreamCodec<FriendlyByteBuf, OpenVisionSelectionPayload> STREAM_CODEC = StreamCodec.unit(new OpenVisionSelectionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
