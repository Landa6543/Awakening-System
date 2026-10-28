package com.soybe.awakeningsystem.blackmarket.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBlackMarketPayload(long moraBalance, int blackChipsCount) implements CustomPacketPayload {
    public static final Type<OpenBlackMarketPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_black_market"));

    public static final StreamCodec<FriendlyByteBuf, OpenBlackMarketPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeLong(p.moraBalance());
                buf.writeInt(p.blackChipsCount());
            },
            buf -> new OpenBlackMarketPayload(buf.readLong(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
