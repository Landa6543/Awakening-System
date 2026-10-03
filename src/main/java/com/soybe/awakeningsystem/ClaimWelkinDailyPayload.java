package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClaimWelkinDailyPayload() implements CustomPacketPayload {
    public static final Type<ClaimWelkinDailyPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "claim_welkin_daily"));

    public static final StreamCodec<FriendlyByteBuf, ClaimWelkinDailyPayload> STREAM_CODEC =
            StreamCodec.unit(new ClaimWelkinDailyPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
