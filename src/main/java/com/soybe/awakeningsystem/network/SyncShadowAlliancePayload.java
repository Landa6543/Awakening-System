package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncShadowAlliancePayload(int entityId, byte allianceType, int squadId) implements CustomPacketPayload {
    public static final Type<SyncShadowAlliancePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_shadow_alliance")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncShadowAlliancePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncShadowAlliancePayload::entityId,
            ByteBufCodecs.BYTE, SyncShadowAlliancePayload::allianceType,
            ByteBufCodecs.VAR_INT, SyncShadowAlliancePayload::squadId,
            SyncShadowAlliancePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
