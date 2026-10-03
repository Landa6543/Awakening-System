package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectVisionPayload(String vision) implements CustomPacketPayload {
    public static final Type<SelectVisionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "select_vision"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectVisionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SelectVisionPayload::vision, SelectVisionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
