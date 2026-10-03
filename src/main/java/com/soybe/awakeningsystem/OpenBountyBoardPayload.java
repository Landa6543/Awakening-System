package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBountyBoardPayload(String guildRank, int completedBounties, int nextRankNeeded,
                                     boolean hasActiveBounty, int activeIndex, int activeKilled, int activeNeeded,
                                     int[] availableBounties)
        implements CustomPacketPayload {
    public static final Type<OpenBountyBoardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_bounty_board"));

    public static final StreamCodec<FriendlyByteBuf, OpenBountyBoardPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeUtf(payload.guildRank());
                buf.writeInt(payload.completedBounties());
                buf.writeInt(payload.nextRankNeeded());
                buf.writeBoolean(payload.hasActiveBounty());
                buf.writeInt(payload.activeIndex());
                buf.writeInt(payload.activeKilled());
                buf.writeInt(payload.activeNeeded());
                buf.writeVarIntArray(payload.availableBounties());
            },
            buf -> new OpenBountyBoardPayload(
                    buf.readUtf(), buf.readInt(), buf.readInt(), buf.readBoolean(),
                    buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readVarIntArray()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}