package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClaimDailyMissionPayload() implements CustomPacketPayload {
    public static final Type<ClaimDailyMissionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "claim_daily_mission"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimDailyMissionPayload> STREAM_CODEC = StreamCodec.unit(new ClaimDailyMissionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
