package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GachaWishPayload(int bannerId, int pullCount) implements CustomPacketPayload {
    public static final Type<GachaWishPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_wish"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaWishPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.bannerId);
                buf.writeVarInt(p.pullCount);
            },
            buf -> new GachaWishPayload(buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
