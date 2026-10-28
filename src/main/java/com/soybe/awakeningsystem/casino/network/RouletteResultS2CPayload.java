package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RouletteResultS2CPayload(
        BlockPos pos,
        int winningNumber,
        long totalBet,
        long totalPayout,
        long updatedMora
) implements CustomPacketPayload {

    public static final Type<RouletteResultS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "roulette_result_s2c"));

    public static final StreamCodec<ByteBuf, RouletteResultS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_INT.encode(buf, p.winningNumber());
                ByteBufCodecs.VAR_LONG.encode(buf, p.totalBet());
                ByteBufCodecs.VAR_LONG.encode(buf, p.totalPayout());
                ByteBufCodecs.VAR_LONG.encode(buf, p.updatedMora());
            },
            buf -> new RouletteResultS2CPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
