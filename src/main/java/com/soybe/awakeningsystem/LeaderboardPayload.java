package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record LeaderboardPayload(List<LeaderboardEntry> entries) implements CustomPacketPayload {
    public static final Type<LeaderboardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "leaderboard"));

    public record LeaderboardEntry(String name, int level, int prestige, int kills, int coins) {
        public static final StreamCodec<FriendlyByteBuf, LeaderboardEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, LeaderboardEntry::name,
                ByteBufCodecs.VAR_INT, LeaderboardEntry::level,
                ByteBufCodecs.VAR_INT, LeaderboardEntry::prestige,
                ByteBufCodecs.VAR_INT, LeaderboardEntry::kills,
                ByteBufCodecs.VAR_INT, LeaderboardEntry::coins,
                LeaderboardEntry::new
        );
    }

    public static final StreamCodec<FriendlyByteBuf, LeaderboardPayload> STREAM_CODEC = StreamCodec.composite(
            LeaderboardEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), LeaderboardPayload::entries,
            LeaderboardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
