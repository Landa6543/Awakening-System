package com.soybe.awakeningsystem.casino.darts.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DartboardActionC2SPayload(
        BlockPos pos,
        int action,
        int bet
) implements CustomPacketPayload {

    public static final int ACTION_START_SOLO = 0;
    public static final int ACTION_CREATE_PVP = 1;
    public static final int ACTION_JOIN_PVP = 2;
    public static final int ACTION_CANCEL_PVP = 3;

    public static final Type<DartboardActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dartboard_action"));

    public static final StreamCodec<ByteBuf, DartboardActionC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_INT.encode(buf, p.action());
                ByteBufCodecs.VAR_INT.encode(buf, p.bet());
            },
            buf -> new DartboardActionC2SPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
