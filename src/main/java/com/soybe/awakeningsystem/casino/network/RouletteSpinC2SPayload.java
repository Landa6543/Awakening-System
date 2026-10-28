package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.casino.roulette.RouletteBetEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record RouletteSpinC2SPayload(
        BlockPos pos,
        List<RouletteBetEntry> bets
) implements CustomPacketPayload {

    public static final Type<RouletteSpinC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "roulette_spin_c2s"));

    public static final StreamCodec<ByteBuf, RouletteSpinC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                BlockPos.STREAM_CODEC.encode(buf, p.pos());
                ByteBufCodecs.VAR_INT.encode(buf, p.bets().size());
                for (RouletteBetEntry entry : p.bets()) {
                    RouletteBetEntry.STREAM_CODEC.encode(buf, entry);
                }
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                int count = ByteBufCodecs.VAR_INT.decode(buf);
                List<RouletteBetEntry> bets = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    bets.add(RouletteBetEntry.STREAM_CODEC.decode(buf));
                }
                return new RouletteSpinC2SPayload(pos, bets);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
