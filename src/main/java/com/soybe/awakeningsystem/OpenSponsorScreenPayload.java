package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenSponsorScreenPayload(boolean forced) implements CustomPacketPayload {
    public static final Type<OpenSponsorScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_sponsor_screen"));
    public static final StreamCodec<FriendlyByteBuf, OpenSponsorScreenPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeBoolean(payload.forced()),
            buf -> new OpenSponsorScreenPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
