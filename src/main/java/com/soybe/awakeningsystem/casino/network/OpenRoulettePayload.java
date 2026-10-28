package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenRoulettePayload(
        BlockPos pos,
        long moraBalance,
        int bronzeChips,
        int silverChips,
        int goldChips,
        int platinumChips,
        int blackChips,
        boolean isSpinning,
        int remainingSpinTicks,
        int totalSpinTicks,
        int winningNumber,
        long currentPayout,
        boolean isIllegal,
        boolean isVip
) implements CustomPacketPayload {

    public static final Type<OpenRoulettePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_roulette"));

    public static final StreamCodec<ByteBuf, OpenRoulettePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_LONG.encode(buf, p.moraBalance());
                ByteBufCodecs.VAR_INT.encode(buf, p.bronzeChips());
                ByteBufCodecs.VAR_INT.encode(buf, p.silverChips());
                ByteBufCodecs.VAR_INT.encode(buf, p.goldChips());
                ByteBufCodecs.VAR_INT.encode(buf, p.platinumChips());
                ByteBufCodecs.VAR_INT.encode(buf, p.blackChips());
                ByteBufCodecs.BOOL.encode(buf, p.isSpinning());
                ByteBufCodecs.VAR_INT.encode(buf, p.remainingSpinTicks());
                ByteBufCodecs.VAR_INT.encode(buf, p.totalSpinTicks());
                ByteBufCodecs.VAR_INT.encode(buf, p.winningNumber());
                ByteBufCodecs.VAR_LONG.encode(buf, p.currentPayout());
                ByteBufCodecs.BOOL.encode(buf, p.isIllegal());
                ByteBufCodecs.BOOL.encode(buf, p.isVip());
            },
            buf -> new OpenRoulettePayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
