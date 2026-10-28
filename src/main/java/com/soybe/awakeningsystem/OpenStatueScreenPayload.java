package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenStatueScreenPayload(
        int anemoLevel, int anemoProgress,
        int geoLevel, int geoProgress,
        int electroLevel, int electroProgress,
        int dendroLevel, int dendroProgress
) implements CustomPacketPayload {
    public static final Type<OpenStatueScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_statue_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenStatueScreenPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.anemoLevel());
                buf.writeVarInt(payload.anemoProgress());
                buf.writeVarInt(payload.geoLevel());
                buf.writeVarInt(payload.geoProgress());
                buf.writeVarInt(payload.electroLevel());
                buf.writeVarInt(payload.electroProgress());
                buf.writeVarInt(payload.dendroLevel());
                buf.writeVarInt(payload.dendroProgress());
            },
            buf -> new OpenStatueScreenPayload(
                    buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
