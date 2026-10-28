package com.soybe.awakeningsystem.casino.pool.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.casino.pool.PoolBall;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record PoolTableStateS2CPayload(
        BlockPos tablePos,
        List<PoolBall> balls,
        int gameMode,
        int betAmount,
        String player1Name,
        String player2Name,
        int currentTurn,
        int p1Suit,
        int p2Suit,
        int gameStatus,
        String statusMessage,
        float cueAimAngle,
        float cuePower,
        boolean isAiming,
        boolean isIllegal
) implements CustomPacketPayload {
    public static final Type<PoolTableStateS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pool_table_state"));

    public static final StreamCodec<FriendlyByteBuf, PoolTableStateS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.tablePos());
                buf.writeByte(p.balls().size());
                for (PoolBall ball : p.balls()) {
                    ball.writeToBuf(buf);
                }
                buf.writeByte(p.gameMode());
                buf.writeInt(p.betAmount());
                buf.writeUtf(p.player1Name(), 32);
                buf.writeUtf(p.player2Name(), 32);
                buf.writeByte(p.currentTurn());
                buf.writeByte(p.p1Suit());
                buf.writeByte(p.p2Suit());
                buf.writeByte(p.gameStatus());
                buf.writeUtf(p.statusMessage(), 128);
                buf.writeFloat(p.cueAimAngle());
                buf.writeFloat(p.cuePower());
                buf.writeBoolean(p.isAiming());
                buf.writeBoolean(p.isIllegal());
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                int ballCount = buf.readByte();
                List<PoolBall> balls = new ArrayList<>(ballCount);
                for (int i = 0; i < ballCount; i++) {
                    balls.add(PoolBall.readFromBuf(buf));
                }
                int mode = buf.readByte();
                int bet = buf.readInt();
                String p1 = buf.readUtf(32);
                String p2 = buf.readUtf(32);
                int turn = buf.readByte();
                int p1S = buf.readByte();
                int p2S = buf.readByte();
                int status = buf.readByte();
                String msg = buf.readUtf(128);
                float aimAngle = buf.readFloat();
                float power = buf.readFloat();
                boolean aiming = buf.readBoolean();
                boolean illegal = buf.readBoolean();
                return new PoolTableStateS2CPayload(
                        pos, balls, mode, bet, p1, p2, turn, p1S, p2S, status, msg, aimAngle, power, aiming, illegal
                );
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
