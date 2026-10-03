package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record OpenGachaScreenPayload(
        int primogems,
        int intertwinedFates,
        int acquaintFates,
        int stardust,
        int starglitter,
        int pity5Star,
        int pity4Star,
        boolean guaranteed,
        int beginnerPulls,
        int mora,
        int weaponBannerTarget,
        int weaponBannerFatePoints,
        int characterBannerTarget,
        int coins,
        double usdBalance,
        int genesisCrystals,
        int stellarJade,
        int welkinDays,
        int totalWishes,
        List<String> wishHistory
) implements CustomPacketPayload {
    public static final Type<OpenGachaScreenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_gacha_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGachaScreenPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.primogems);
                buf.writeVarInt(p.intertwinedFates);
                buf.writeVarInt(p.acquaintFates);
                buf.writeVarInt(p.stardust);
                buf.writeVarInt(p.starglitter);
                buf.writeVarInt(p.pity5Star);
                buf.writeVarInt(p.pity4Star);
                buf.writeBoolean(p.guaranteed);
                buf.writeVarInt(p.beginnerPulls);
                buf.writeVarInt(p.mora);
                buf.writeVarInt(p.weaponBannerTarget);
                buf.writeVarInt(p.weaponBannerFatePoints);
                buf.writeVarInt(p.characterBannerTarget);
                buf.writeVarInt(p.coins);
                buf.writeDouble(p.usdBalance);
                buf.writeVarInt(p.genesisCrystals);
                buf.writeVarInt(p.stellarJade);
                buf.writeVarInt(p.welkinDays);
                buf.writeVarInt(p.totalWishes);
                buf.writeVarInt(p.wishHistory.size());
                for (String entry : p.wishHistory) {
                    buf.writeUtf(entry);
                }
            },
            buf -> {
                int primos = buf.readVarInt();
                int intertwined = buf.readVarInt();
                int acquaint = buf.readVarInt();
                int stardust = buf.readVarInt();
                int starglitter = buf.readVarInt();
                int pity5 = buf.readVarInt();
                int pity4 = buf.readVarInt();
                boolean guaranteed = buf.readBoolean();
                int beginner = buf.readVarInt();
                int mora = buf.readVarInt();
                int weaponTarget = buf.readVarInt();
                int weaponFatePts = buf.readVarInt();
                int charTarget = buf.readVarInt();
                int coins = buf.readVarInt();
                double usdBalance = buf.readDouble();
                int genesisCrystals = buf.readVarInt();
                int stellarJade = buf.readVarInt();
                int welkinDays = buf.readVarInt();
                int totalWishes = buf.readVarInt();
                int histSize = buf.readVarInt();
                List<String> history = new ArrayList<>(histSize);
                for (int i = 0; i < histSize; i++) {
                    history.add(buf.readUtf());
                }
                return new OpenGachaScreenPayload(primos, intertwined, acquaint, stardust, starglitter, pity5, pity4, guaranteed, beginner, mora, weaponTarget, weaponFatePts, charTarget, coins, usdBalance, genesisCrystals, stellarJade, welkinDays, totalWishes, history);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
