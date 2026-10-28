package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlackjackPvPActionC2SPayload(
        BlockPos pos,
        int action,
        long betAmount
) implements CustomPacketPayload {

    public static final int ACTION_CREATE = 0;
    public static final int ACTION_JOIN = 1;
    public static final int ACTION_CANCEL = 2;
    public static final int ACTION_HIT = 3;
    public static final int ACTION_STAND = 4;

    public static final Type<BlackjackPvPActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "blackjack_pvp_action"));

    public static final StreamCodec<ByteBuf, BlackjackPvPActionC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_INT.encode(buf, p.action());
                ByteBufCodecs.VAR_LONG.encode(buf, p.betAmount());
            },
            buf -> new BlackjackPvPActionC2SPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
