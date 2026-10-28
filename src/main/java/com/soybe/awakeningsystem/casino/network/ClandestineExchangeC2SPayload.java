package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClandestineExchangeC2SPayload(
        BlockPos pos,
        int action,
        int amount
) implements CustomPacketPayload {
    public static final Type<ClandestineExchangeC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "clandestine_exchange"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClandestineExchangeC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeVarInt(p.action);
                buf.writeVarInt(p.amount);
            },
            buf -> new ClandestineExchangeC2SPayload(
                    buf.readBlockPos(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
