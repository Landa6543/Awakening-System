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

    public record RiftEntry(BlockPos pos, String rank, boolean isRedGate, int type, float yaw, int beamTicks, int remainingTicks) {
        public RiftEntry(BlockPos pos, String rank, boolean isRedGate, int type, float yaw, int beamTicks) {
            this(pos, rank, isRedGate, type, yaw, beamTicks, -1);
        }

        public RiftEntry(BlockPos pos, String rank, boolean isRedGate, int type, float yaw) {
            this(pos, rank, isRedGate, type, yaw, 0, -1);
        }

        public RiftEntry(BlockPos pos, String rank, boolean isRedGate, int type) {
            this(pos, rank, isRedGate, type, 0.0F, 0, -1);
        }

        public static final StreamCodec<FriendlyByteBuf, RiftEntry> STREAM_CODEC = StreamCodec.of(
                (buf, val) -> {
                    BlockPos.STREAM_CODEC.encode(buf, val.pos);
                    ByteBufCodecs.STRING_UTF8.encode(buf, val.rank);
                    ByteBufCodecs.BOOL.encode(buf, val.isRedGate);
                    ByteBufCodecs.VAR_INT.encode(buf, val.type);
                    ByteBufCodecs.FLOAT.encode(buf, val.yaw);
                    ByteBufCodecs.VAR_INT.encode(buf, val.beamTicks);
                    ByteBufCodecs.VAR_INT.encode(buf, val.remainingTicks);
                },
                buf -> new RiftEntry(
                        BlockPos.STREAM_CODEC.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.BOOL.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.FLOAT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf)
                )
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
