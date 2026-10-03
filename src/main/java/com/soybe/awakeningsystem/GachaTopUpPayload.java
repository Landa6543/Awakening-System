package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GachaTopUpPayload(int actionType, int packId) implements CustomPacketPayload {
    public static final Type<GachaTopUpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_top_up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaTopUpPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.actionType);
                buf.writeVarInt(p.packId);
            },
            buf -> new GachaTopUpPayload(buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
