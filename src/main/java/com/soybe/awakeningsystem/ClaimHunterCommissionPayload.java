package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClaimHunterCommissionPayload() implements CustomPacketPayload {
    public static final Type<ClaimHunterCommissionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "claim_hunter_commission"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimHunterCommissionPayload> STREAM_CODEC =
            StreamCodec.unit(new ClaimHunterCommissionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
