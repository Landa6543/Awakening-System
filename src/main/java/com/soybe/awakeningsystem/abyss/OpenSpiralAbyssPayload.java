package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record OpenSpiralAbyssPayload(
        int currentUnlockedFloor,
        int[] starsPerFloor,
        boolean[] claimedRewards,
        int totalStars,
        int playerLevel,
        int primogems,
        int coins,
        int intertwinedFates,
        /** Milliseconds until the Spire's floors start again. */
        long spireResetMs,
        /** The latest clears of every floor, oldest first. */
        List<SpiralAbyssManager.ClearRecord> history
) implements CustomPacketPayload {
    public static final Type<OpenSpiralAbyssPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_spiral_abyss")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSpiralAbyssPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.currentUnlockedFloor);
                buf.writeVarInt(p.starsPerFloor.length);
                for (int star : p.starsPerFloor) {
                    buf.writeVarInt(star);
                }
                buf.writeVarInt(p.claimedRewards.length);
                for (boolean claimed : p.claimedRewards) {
                    buf.writeBoolean(claimed);
                }
                buf.writeVarInt(p.totalStars);
                buf.writeVarInt(p.playerLevel);
                buf.writeVarInt(p.primogems);
                buf.writeVarInt(p.coins);
                buf.writeVarInt(p.intertwinedFates);
                buf.writeVarLong(p.spireResetMs);
                buf.writeVarInt(p.history.size());
                for (SpiralAbyssManager.ClearRecord r : p.history) {
                    buf.writeVarInt(r.floor());
                    buf.writeUtf(r.player(), 64);
                    buf.writeVarInt(r.stars());
                    buf.writeLong(r.timeMs());
                    buf.writeVarInt(r.team().size());
                    for (String s : r.team()) buf.writeUtf(s, 128);
                }
            },
            buf -> {
                int unlockedFloor = buf.readVarInt();
                int starsLen = buf.readVarInt();
                int[] stars = new int[starsLen];
                for (int i = 0; i < starsLen; i++) {
                    stars[i] = buf.readVarInt();
                }
                int claimedLen = buf.readVarInt();
                boolean[] claimed = new boolean[claimedLen];
                for (int i = 0; i < claimedLen; i++) {
                    claimed[i] = buf.readBoolean();
                }
                int totalStars = buf.readVarInt();
                int playerLvl = buf.readVarInt();
                int primos = buf.readVarInt();
                int coins = buf.readVarInt();
                int fates = buf.readVarInt();
                long reset = buf.readVarLong();
                int n = buf.readVarInt();
                List<SpiralAbyssManager.ClearRecord> history = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    int floor = buf.readVarInt();
                    String player = buf.readUtf(64);
                    int st = buf.readVarInt();
                    long time = buf.readLong();
                    int tn = buf.readVarInt();
                    List<String> team = new ArrayList<>();
                    for (int k = 0; k < tn; k++) team.add(buf.readUtf(128));
                    history.add(new SpiralAbyssManager.ClearRecord(floor, player, st, time, team));
                }
                return new OpenSpiralAbyssPayload(unlockedFloor, stars, claimed, totalStars, playerLvl, primos, coins, fates, reset, history);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
