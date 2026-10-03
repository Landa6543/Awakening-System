package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpinPhysicalRoulettePayload() implements CustomPacketPayload {
    public static final Type<SpinPhysicalRoulettePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spin_physical_roulette"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpinPhysicalRoulettePayload> STREAM_CODEC = StreamCodec.unit(new SpinPhysicalRoulettePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
