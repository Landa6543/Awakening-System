package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GuildReceptionActionPayload(int actionId) implements CustomPacketPayload {
    public static final Type<GuildReceptionActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "guild_reception_action"));

    public static final StreamCodec<FriendlyByteBuf, GuildReceptionActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeInt(payload.actionId()),
            buf -> new GuildReceptionActionPayload(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
