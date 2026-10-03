package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CancelMagicCirclePayload(int casterEntityId) implements CustomPacketPayload {
    public static final Type<CancelMagicCirclePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cancel_magic_circle"));

    public static final StreamCodec<FriendlyByteBuf, CancelMagicCirclePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeVarInt(payload.casterEntityId),
            buf -> new CancelMagicCirclePayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
