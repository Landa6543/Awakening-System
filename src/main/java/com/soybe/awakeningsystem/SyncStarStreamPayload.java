package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncStarStreamPayload(List<IncarnationEntry> incarnations) implements CustomPacketPayload {
    public static final Type<SyncStarStreamPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_star_stream"));

    public record IncarnationEntry(UUID uuid, String name, int level, String rank, int hp, int maxHp, String dimension, String activity) {
        public static final StreamCodec<FriendlyByteBuf, IncarnationEntry> STREAM_CODEC = StreamCodec.of(
                (buf, val) -> {
                    buf.writeUUID(val.uuid);
                    buf.writeUtf(val.name);
                    buf.writeInt(val.level);
                    buf.writeUtf(val.rank);
                    buf.writeInt(val.hp);
                    buf.writeInt(val.maxHp);
                    buf.writeUtf(val.dimension);
                    buf.writeUtf(val.activity);
                },
                buf -> new IncarnationEntry(
                        buf.readUUID(),
                        buf.readUtf(),
                        buf.readInt(),
                        buf.readUtf(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readUtf(),
                        buf.readUtf()
                )
        );
    }

    public static final StreamCodec<FriendlyByteBuf, SyncStarStreamPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, IncarnationEntry.STREAM_CODEC), SyncStarStreamPayload::incarnations,
            SyncStarStreamPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
