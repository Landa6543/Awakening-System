package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenDungeonChestPayload(
        String boxId,
        String nameKey,
        int tier,
        int cost
) implements CustomPacketPayload {
    public static final Type<OpenDungeonChestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_dungeon_chest"));

    public static final StreamCodec<FriendlyByteBuf, OpenDungeonChestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenDungeonChestPayload::boxId,
            ByteBufCodecs.STRING_UTF8, OpenDungeonChestPayload::nameKey,
            ByteBufCodecs.VAR_INT, OpenDungeonChestPayload::tier,
            ByteBufCodecs.VAR_INT, OpenDungeonChestPayload::cost,
            OpenDungeonChestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
