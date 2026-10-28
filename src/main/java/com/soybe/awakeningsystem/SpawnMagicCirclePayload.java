package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnMagicCirclePayload(
        int casterEntityId,
        double x, double y, double z,
        float lookX, float lookY, float lookZ,
        float radius,
        int durationTicks,
        int color,
        int circleType,
        boolean isCharging
) implements CustomPacketPayload {
    public static final Type<SpawnMagicCirclePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spawn_magic_circle"));

    public SpawnMagicCirclePayload(double x, double y, double z, float lookX, float lookY, float lookZ, float radius, int durationTicks, int color, int circleType) {
        this(-1, x, y, z, lookX, lookY, lookZ, radius, durationTicks, color, circleType, false);
    }

    public static final StreamCodec<FriendlyByteBuf, SpawnMagicCirclePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.casterEntityId);
                buf.writeDouble(payload.x);
                buf.writeDouble(payload.y);
                buf.writeDouble(payload.z);
                buf.writeFloat(payload.lookX);
                buf.writeFloat(payload.lookY);
                buf.writeFloat(payload.lookZ);
                buf.writeFloat(payload.radius);
                buf.writeVarInt(payload.durationTicks);
                buf.writeInt(payload.color);
                buf.writeVarInt(payload.circleType);
                buf.writeBoolean(payload.isCharging);
            },
            buf -> new SpawnMagicCirclePayload(
                    buf.readVarInt(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readInt(),
                    buf.readVarInt(),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
