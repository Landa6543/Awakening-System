package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpendAttributePayload(String attribute) implements CustomPacketPayload {
    public static final Type<SpendAttributePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spend_attribute"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpendAttributePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), SpendAttributePayload::attribute, SpendAttributePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
