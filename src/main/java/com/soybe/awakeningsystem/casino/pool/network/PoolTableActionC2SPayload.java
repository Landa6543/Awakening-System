package com.soybe.awakeningsystem.casino.pool.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PoolTableActionC2SPayload(
        BlockPos tablePos,
        int actionType,
        float aimAngle,
        float power,
        int param1,
        int param2
) implements CustomPacketPayload {
    public static final Type<PoolTableActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pool_table_action"));

    public static final int ACTION_SHOOT = 0;
    public static final int ACTION_AIM = 1;
    public static final int ACTION_RESET_GAME = 2;
    public static final int ACTION_JOIN_P2 = 3;
    public static final int ACTION_LEAVE = 4;

    public static final StreamCodec<FriendlyByteBuf, PoolTableActionC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.tablePos());
                buf.writeByte(p.actionType());
                buf.writeFloat(p.aimAngle());
                buf.writeFloat(p.power());
                buf.writeInt(p.param1());
                buf.writeInt(p.param2());
            },
            buf -> new PoolTableActionC2SPayload(
                    buf.readBlockPos(),
                    buf.readByte(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readInt(),
                    buf.readInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
