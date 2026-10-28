package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TowerMissionActionPayload(int action, int missionIndex) implements CustomPacketPayload {
    public static final int ACTION_ACCEPT = 0;
    public static final int ACTION_CLAIM = 1;
    public static final int ACTION_ABANDON = 2;

    public static final Type<TowerMissionActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "tower_mission_action"));

    public static final StreamCodec<FriendlyByteBuf, TowerMissionActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeVarInt(payload.action());
                buf.writeInt(payload.missionIndex());
            },
            buf -> new TowerMissionActionPayload(buf.readVarInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
