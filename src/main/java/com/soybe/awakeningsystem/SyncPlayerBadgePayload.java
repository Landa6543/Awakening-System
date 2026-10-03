package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SyncPlayerBadgePayload(
        UUID playerId,
        String hunterClass,
        int level,
        String rank,
        String guildRank,
        String constellationModifier,
        boolean isConstellation
) implements CustomPacketPayload {
    public static final Type<SyncPlayerBadgePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_player_badge"));

    public static final StreamCodec<FriendlyByteBuf, SyncPlayerBadgePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeUUID(payload.playerId());
                buf.writeUtf(payload.hunterClass() != null ? payload.hunterClass() : "", 64);
                buf.writeInt(payload.level());
                buf.writeUtf(payload.rank() != null ? payload.rank() : "F", 16);
                buf.writeUtf(payload.guildRank() != null ? payload.guildRank() : "F", 16);
                buf.writeUtf(payload.constellationModifier() != null ? payload.constellationModifier() : "", 128);
                buf.writeBoolean(payload.isConstellation());
            },
            buf -> new SyncPlayerBadgePayload(
                    buf.readUUID(),
                    buf.readUtf(64),
                    buf.readInt(),
                    buf.readUtf(16),
                    buf.readUtf(16),
                    buf.readUtf(128),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
