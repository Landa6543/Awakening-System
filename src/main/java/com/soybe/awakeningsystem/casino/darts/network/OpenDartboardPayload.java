package com.soybe.awakeningsystem.casino.darts.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenDartboardPayload(
        BlockPos pos,
        long coins,
        int pvpState,
        String challengerName,
        int challengerBet,
        int pot,
        String statusMessage
) implements CustomPacketPayload {

    public static final Type<OpenDartboardPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_dartboard"));

    public static final StreamCodec<ByteBuf, OpenDartboardPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_LONG.encode(buf, p.coins());
                ByteBufCodecs.VAR_INT.encode(buf, p.pvpState());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.challengerName());
                ByteBufCodecs.VAR_INT.encode(buf, p.challengerBet());
                ByteBufCodecs.VAR_INT.encode(buf, p.pot());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.statusMessage());
            },
            buf -> new OpenDartboardPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
