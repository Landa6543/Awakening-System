package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public record CameraShakePayload(float intensity, int durationTicks) implements CustomPacketPayload {
    public static final Type<CameraShakePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "camera_shake"));

    public static final StreamCodec<FriendlyByteBuf, CameraShakePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeFloat(payload.intensity);
                buf.writeVarInt(payload.durationTicks);
            },
            buf -> new CameraShakePayload(buf.readFloat(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendToNearby(ServerLevel level, double x, double y, double z, double radius, float intensity, int durationTicks) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, new CameraShakePayload(intensity, durationTicks));
    }
}
