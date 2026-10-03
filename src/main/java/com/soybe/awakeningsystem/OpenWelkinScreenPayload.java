package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenWelkinScreenPayload(
        int daysRemaining,
        int rewardPrimos,
        boolean canClaimToday,
        boolean isEnhanced,
        int questStep,
        int questProgress
) implements CustomPacketPayload {
    public static final Type<OpenWelkinScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_welkin_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenWelkinScreenPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.daysRemaining);
                buf.writeVarInt(p.rewardPrimos);
                buf.writeBoolean(p.canClaimToday);
                buf.writeBoolean(p.isEnhanced);
                buf.writeVarInt(p.questStep);
                buf.writeVarInt(p.questProgress);
            },
            buf -> new OpenWelkinScreenPayload(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
