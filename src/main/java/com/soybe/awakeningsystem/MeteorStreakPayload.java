package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Tells a client to draw a shooting star in its sky, falling towards the given compass bearing. */
public record MeteorStreakPayload(float bearingDegrees, float driftSign, int durationTicks) implements CustomPacketPayload {
    public static final Type<MeteorStreakPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "meteor_streak"));

    public static final StreamCodec<FriendlyByteBuf, MeteorStreakPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeFloat(payload.bearingDegrees);
                buf.writeFloat(payload.driftSign);
                buf.writeVarInt(payload.durationTicks);
            },
            buf -> new MeteorStreakPayload(buf.readFloat(), buf.readFloat(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
