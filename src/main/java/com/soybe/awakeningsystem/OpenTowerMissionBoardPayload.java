package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenTowerMissionBoardPayload(
        long currentDay,
        int playerSavedFloor,
        int[] dailyIndices,
        boolean hasActiveMission,
        int activeIndex,
        int activeProgress,
        int activeNeeded,
        boolean activeReadyToClaim,
        boolean[] completedToday
) implements CustomPacketPayload {

    public static final Type<OpenTowerMissionBoardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_tower_mission_board"));

    public static final StreamCodec<FriendlyByteBuf, OpenTowerMissionBoardPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeLong(payload.currentDay());
                buf.writeInt(payload.playerSavedFloor());
                buf.writeVarIntArray(payload.dailyIndices());
                buf.writeBoolean(payload.hasActiveMission());
                buf.writeInt(payload.activeIndex());
                buf.writeInt(payload.activeProgress());
                buf.writeInt(payload.activeNeeded());
                buf.writeBoolean(payload.activeReadyToClaim());

                boolean[] completed = payload.completedToday();
                buf.writeByte(completed != null ? completed.length : 0);
                if (completed != null) {
                    for (boolean b : completed) {
                        buf.writeBoolean(b);
                    }
                }
            },
            buf -> {
                long day = buf.readLong();
                int floor = buf.readInt();
                int[] indices = buf.readVarIntArray();
                boolean hasActive = buf.readBoolean();
                int activeIdx = buf.readInt();
                int prog = buf.readInt();
                int need = buf.readInt();
                boolean ready = buf.readBoolean();

                int compLen = buf.readByte();
                boolean[] completed = new boolean[compLen];
                for (int i = 0; i < compLen; i++) {
                    completed[i] = buf.readBoolean();
                }

                return new OpenTowerMissionBoardPayload(day, floor, indices, hasActive, activeIdx, prog, need, ready, completed);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
