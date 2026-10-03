package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectDungeonPayload(String rank, String biome) implements CustomPacketPayload {
    public static final Type<SelectDungeonPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "select_dungeon"));

    public static final StreamCodec<FriendlyByteBuf, SelectDungeonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SelectDungeonPayload::rank,
            ByteBufCodecs.STRING_UTF8, SelectDungeonPayload::biome,
            SelectDungeonPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
