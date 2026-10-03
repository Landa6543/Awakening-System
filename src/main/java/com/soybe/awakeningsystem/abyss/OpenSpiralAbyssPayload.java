package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenSpiralAbyssPayload(
        int currentUnlockedFloor,
        int[] starsPerFloor,
        boolean[] claimedRewards,
        int totalStars,
        int playerLevel,
        int primogems,
        int coins,
        int intertwinedFates
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
                return new OpenSpiralAbyssPayload(unlockedFloor, stars, claimed, totalStars, playerLvl, primos, coins, fates);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
