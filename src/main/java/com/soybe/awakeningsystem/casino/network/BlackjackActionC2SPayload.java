package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlackjackActionC2SPayload(BlockPos pos, int actionType, long betAmount, int chipTier) implements CustomPacketPayload {
    public static final Type<BlackjackActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "blackjack_action"));

    public static final int ACTION_BET = 0;
    public static final int ACTION_HIT = 1;
    public static final int ACTION_STAND = 2;
    public static final int ACTION_DOUBLE = 3;
    public static final int ACTION_NEW_ROUND = 4;

    public static final StreamCodec<ByteBuf, BlackjackActionC2SPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlackjackActionC2SPayload::pos,
            ByteBufCodecs.VAR_INT, BlackjackActionC2SPayload::actionType,
            ByteBufCodecs.VAR_LONG, BlackjackActionC2SPayload::betAmount,
            ByteBufCodecs.VAR_INT, BlackjackActionC2SPayload::chipTier,
            BlackjackActionC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
