package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetGachaPathPayload(int bannerType, int targetIndex) implements CustomPacketPayload {
    public static final Type<SetGachaPathPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "set_gacha_path"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGachaPathPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.bannerType);
                buf.writeVarInt(p.targetIndex);
            },
            buf -> new SetGachaPathPayload(buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
