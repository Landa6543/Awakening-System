package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenClassSelectionPayload() implements CustomPacketPayload {
    public static final Type<OpenClassSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_class_selection"));

    public static final StreamCodec<FriendlyByteBuf, OpenClassSelectionPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenClassSelectionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
