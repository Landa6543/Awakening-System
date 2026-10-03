package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GuildActionPayload(String action, String target, int amount) implements CustomPacketPayload {
    public static final Type<GuildActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "guild_action"));

    public static final StreamCodec<FriendlyByteBuf, GuildActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeUtf(payload.action());
                buf.writeUtf(payload.target());
                buf.writeInt(payload.amount());
            },
            buf -> new GuildActionPayload(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
