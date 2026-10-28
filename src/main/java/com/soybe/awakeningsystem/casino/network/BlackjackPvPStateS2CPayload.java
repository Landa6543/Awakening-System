package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlackjackPvPStateS2CPayload(
        BlockPos pos,
        int stateOrdinal,
        int resultOrdinal,
        String hostName,
        String opponentName,
        boolean isHost,
        boolean isMyTurn,
        long betAmount,
        long pot,
        int myScore,
        int opponentScore,
        int[] myCards,
        int[] opponentCards,
        String resultMessage,
        long updatedMora
) implements CustomPacketPayload {

    public static final Type<BlackjackPvPStateS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "blackjack_pvp_state"));

    public static final StreamCodec<ByteBuf, int[]> INT_ARRAY_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public int[] decode(ByteBuf buf) {
            int len = ByteBufCodecs.VAR_INT.decode(buf);
            int[] arr = new int[len];
            for (int i = 0; i < len; i++) {
                arr[i] = ByteBufCodecs.VAR_INT.decode(buf);
            }
            return arr;
        }

        @Override
        public void encode(ByteBuf buf, int[] value) {
            ByteBufCodecs.VAR_INT.encode(buf, value.length);
            for (int v : value) {
                ByteBufCodecs.VAR_INT.encode(buf, v);
            }
        }
    };

    public static final StreamCodec<ByteBuf, BlackjackPvPStateS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_INT.encode(buf, p.stateOrdinal());
                ByteBufCodecs.VAR_INT.encode(buf, p.resultOrdinal());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.hostName());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.opponentName());
                ByteBufCodecs.BOOL.encode(buf, p.isHost());
                ByteBufCodecs.BOOL.encode(buf, p.isMyTurn());
                ByteBufCodecs.VAR_LONG.encode(buf, p.betAmount());
                ByteBufCodecs.VAR_LONG.encode(buf, p.pot());
                ByteBufCodecs.VAR_INT.encode(buf, p.myScore());
                ByteBufCodecs.VAR_INT.encode(buf, p.opponentScore());
                INT_ARRAY_STREAM_CODEC.encode(buf, p.myCards());
                INT_ARRAY_STREAM_CODEC.encode(buf, p.opponentCards());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.resultMessage());
                ByteBufCodecs.VAR_LONG.encode(buf, p.updatedMora());
            },
            buf -> new BlackjackPvPStateS2CPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    INT_ARRAY_STREAM_CODEC.decode(buf),
                    INT_ARRAY_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
