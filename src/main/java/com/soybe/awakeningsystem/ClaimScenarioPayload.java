package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClaimScenarioPayload() implements CustomPacketPayload {
    public static final Type<ClaimScenarioPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "claim_scenario"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimScenarioPayload> STREAM_CODEC = StreamCodec.unit(new ClaimScenarioPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
