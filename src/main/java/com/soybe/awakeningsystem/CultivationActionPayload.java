package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CultivationActionPayload(String action, int targetIndex) implements CustomPacketPayload {
    public static final Type<CultivationActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cultivation_action"));

    public static final StreamCodec<FriendlyByteBuf, CultivationActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    CultivationActionPayload::action,
                    ByteBufCodecs.VAR_INT,
                    CultivationActionPayload::targetIndex,
                    CultivationActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
