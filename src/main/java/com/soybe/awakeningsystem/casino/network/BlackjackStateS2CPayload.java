package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlackjackStateS2CPayload(
        BlockPos pos,
        int stateOrdinal,
        int resultOrdinal,
        long currentBet,
        long payout,
        int[] playerCards,
        int[] dealerCards,
        boolean hideDealerCard,
        long updatedMora
) implements CustomPacketPayload {

    public static final Type<BlackjackStateS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "blackjack_state"));

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

    public static final StreamCodec<ByteBuf, BlackjackStateS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_INT.encode(buf, p.stateOrdinal());
                ByteBufCodecs.VAR_INT.encode(buf, p.resultOrdinal());
                ByteBufCodecs.VAR_LONG.encode(buf, p.currentBet());
                ByteBufCodecs.VAR_LONG.encode(buf, p.payout());
                INT_ARRAY_STREAM_CODEC.encode(buf, p.playerCards());
                INT_ARRAY_STREAM_CODEC.encode(buf, p.dealerCards());
                ByteBufCodecs.BOOL.encode(buf, p.hideDealerCard());
                ByteBufCodecs.VAR_LONG.encode(buf, p.updatedMora());
            },
            buf -> new BlackjackStateS2CPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    INT_ARRAY_STREAM_CODEC.decode(buf),
                    INT_ARRAY_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
