package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenFishingStationPayload(
        int totalFishCaught,
        int coins,
        int medakaCount,
        int aizenMedakaCount,
        int sweetFlowerMedakaCount,
        int glazeMedakaCount,
        int crystalfishCount,
        int venomspineCount,
        int abidingAngelfishCount,
        int raimeiAngelfishCount
) implements CustomPacketPayload {
    public static final Type<OpenFishingStationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_fishing_station"));

    public static final StreamCodec<FriendlyByteBuf, OpenFishingStationPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeInt(payload.totalFishCaught());
                buf.writeInt(payload.coins());
                buf.writeInt(payload.medakaCount());
                buf.writeInt(payload.aizenMedakaCount());
                buf.writeInt(payload.sweetFlowerMedakaCount());
                buf.writeInt(payload.glazeMedakaCount());
                buf.writeInt(payload.crystalfishCount());
                buf.writeInt(payload.venomspineCount());
                buf.writeInt(payload.abidingAngelfishCount());
                buf.writeInt(payload.raimeiAngelfishCount());
            },
            buf -> new OpenFishingStationPayload(
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
