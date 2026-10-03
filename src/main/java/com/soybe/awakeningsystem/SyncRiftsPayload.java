package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncRiftsPayload(List<RiftEntry> rifts) implements CustomPacketPayload {
    public static final Type<SyncRiftsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_rifts"));

    public record RiftEntry(BlockPos pos, String rank, boolean isRedGate, int type, float yaw) {
        public RiftEntry(BlockPos pos, String rank, boolean isRedGate, int type) {
            this(pos, rank, isRedGate, type, 0.0F);
        }

        public static final StreamCodec<FriendlyByteBuf, RiftEntry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, RiftEntry::pos,
                ByteBufCodecs.STRING_UTF8, RiftEntry::rank,
                ByteBufCodecs.BOOL, RiftEntry::isRedGate,
                ByteBufCodecs.VAR_INT, RiftEntry::type,
                ByteBufCodecs.FLOAT, RiftEntry::yaw,
                RiftEntry::new
        );
    }

    public static final StreamCodec<FriendlyByteBuf, SyncRiftsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, RiftEntry.STREAM_CODEC), SyncRiftsPayload::rifts,
            SyncRiftsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
