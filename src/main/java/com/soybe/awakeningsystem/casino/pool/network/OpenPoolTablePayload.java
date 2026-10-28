package com.soybe.awakeningsystem.casino.pool.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenPoolTablePayload(BlockPos tablePos, Direction facing, boolean isIllegal) implements CustomPacketPayload {
    public static final Type<OpenPoolTablePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_pool_table"));

    public static final StreamCodec<FriendlyByteBuf, OpenPoolTablePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.tablePos());
                buf.writeEnum(p.facing());
                buf.writeBoolean(p.isIllegal());
            },
            buf -> new OpenPoolTablePayload(buf.readBlockPos(), buf.readEnum(Direction.class), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
