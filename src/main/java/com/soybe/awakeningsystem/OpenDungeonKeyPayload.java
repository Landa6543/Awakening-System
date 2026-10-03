package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenDungeonKeyPayload(int playerLevel, String currentRank) implements CustomPacketPayload {
    public static final Type<OpenDungeonKeyPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_dungeon_key"));

    public static final StreamCodec<FriendlyByteBuf, OpenDungeonKeyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenDungeonKeyPayload::playerLevel,
            ByteBufCodecs.STRING_UTF8, OpenDungeonKeyPayload::currentRank,
            OpenDungeonKeyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
