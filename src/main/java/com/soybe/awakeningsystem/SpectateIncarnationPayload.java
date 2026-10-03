package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SpectateIncarnationPayload(UUID targetPlayerId) implements CustomPacketPayload {
    public static final Type<SpectateIncarnationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spectate_incarnation"));

    public static final StreamCodec<FriendlyByteBuf, SpectateIncarnationPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeUUID(payload.targetPlayerId()),
            buf -> new SpectateIncarnationPayload(buf.readUUID())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
