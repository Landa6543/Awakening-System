package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StartWelkinQuestPayload() implements CustomPacketPayload {
    public static final Type<StartWelkinQuestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "start_welkin_quest"));

    public static final StreamCodec<FriendlyByteBuf, StartWelkinQuestPayload> STREAM_CODEC =
            StreamCodec.unit(new StartWelkinQuestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
