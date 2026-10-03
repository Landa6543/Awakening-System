package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BossCinematicPayload(BlockPos altarPos, int durationTicks, int dragonEntityId) implements CustomPacketPayload {
    public static final Type<BossCinematicPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "boss_cinematic"));

    public static final StreamCodec<FriendlyByteBuf, BossCinematicPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BossCinematicPayload::altarPos,
            ByteBufCodecs.VAR_INT, BossCinematicPayload::durationTicks,
            ByteBufCodecs.VAR_INT, BossCinematicPayload::dragonEntityId,
            BossCinematicPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
