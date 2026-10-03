package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShadowEmergencePayload(int entityId, double x, double y, double z, int durationTicks, float height) implements CustomPacketPayload {
    public static final Type<ShadowEmergencePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_emergence"));

    public static final StreamCodec<FriendlyByteBuf, ShadowEmergencePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ShadowEmergencePayload::entityId,
            ByteBufCodecs.DOUBLE, ShadowEmergencePayload::x,
            ByteBufCodecs.DOUBLE, ShadowEmergencePayload::y,
            ByteBufCodecs.DOUBLE, ShadowEmergencePayload::z,
            ByteBufCodecs.VAR_INT, ShadowEmergencePayload::durationTicks,
            ByteBufCodecs.FLOAT, ShadowEmergencePayload::height,
            ShadowEmergencePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
