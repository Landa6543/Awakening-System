package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FishingStationActionPayload(int actionType, int itemId, int quantity) implements CustomPacketPayload {
    public static final Type<FishingStationActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "fishing_station_action"));

    public static final StreamCodec<FriendlyByteBuf, FishingStationActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeInt(payload.actionType());
                buf.writeInt(payload.itemId());
                buf.writeInt(payload.quantity());
            },
            buf -> new FishingStationActionPayload(buf.readInt(), buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
