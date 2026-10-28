package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenClandestineCashierPayload(
        BlockPos pos,
        long moraBalance,
        int stockChips,
        boolean isOwner
) implements CustomPacketPayload {
    public static final Type<OpenClandestineCashierPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_clandestine_cashier"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenClandestineCashierPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeVarLong(p.moraBalance);
                buf.writeVarInt(p.stockChips);
                buf.writeBoolean(p.isOwner);
            },
            buf -> new OpenClandestineCashierPayload(
                    buf.readBlockPos(),
                    buf.readVarLong(),
                    buf.readVarInt(),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
