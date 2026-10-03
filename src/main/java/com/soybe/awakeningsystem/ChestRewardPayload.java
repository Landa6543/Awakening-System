package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ChestRewardPayload(
        String boxId,
        int tier,
        String rewardSummary,
        int coinsWon,
        List<String> itemIds,
        List<Integer> itemCounts
) implements CustomPacketPayload {
    public static final Type<ChestRewardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chest_reward"));

    public static final StreamCodec<FriendlyByteBuf, ChestRewardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ChestRewardPayload::boxId,
            ByteBufCodecs.VAR_INT, ChestRewardPayload::tier,
            ByteBufCodecs.STRING_UTF8, ChestRewardPayload::rewardSummary,
            ByteBufCodecs.VAR_INT, ChestRewardPayload::coinsWon,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ChestRewardPayload::itemIds,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ChestRewardPayload::itemCounts,
            ChestRewardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
