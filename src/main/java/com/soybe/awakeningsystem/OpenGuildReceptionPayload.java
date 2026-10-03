package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenGuildReceptionPayload(
        String guildRank,
        int completedBounties,
        int nextRankNeeded,
        String nextRankName,
        boolean canPromote,
        boolean canClaimBlessing
) implements CustomPacketPayload {
    public static final Type<OpenGuildReceptionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_guild_reception"));

    public static final StreamCodec<FriendlyByteBuf, OpenGuildReceptionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeUtf(payload.guildRank());
                buf.writeInt(payload.completedBounties());
                buf.writeInt(payload.nextRankNeeded());
                buf.writeUtf(payload.nextRankName());
                buf.writeBoolean(payload.canPromote());
                buf.writeBoolean(payload.canClaimBlessing());
            },
            buf -> new OpenGuildReceptionPayload(
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
