package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Tells a client that a meteorite is falling: from (sx, sy, sz) high in the sky down to (ex, ey, ez) on the
 * ground, over {@code durationTicks}. The client draws it at its real place in the world, however far away.
 * {@code seed} shapes the rock, so everyone sees the same one.
 */
public record MeteorStreakPayload(double sx, double sy, double sz, double ex, double ey, double ez, int durationTicks, long seed)
        implements CustomPacketPayload {
    public static final Type<MeteorStreakPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "meteor_streak"));

    public static final StreamCodec<FriendlyByteBuf, MeteorStreakPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeDouble(p.sx); buf.writeDouble(p.sy); buf.writeDouble(p.sz);
                buf.writeDouble(p.ex); buf.writeDouble(p.ey); buf.writeDouble(p.ez);
                buf.writeVarInt(p.durationTicks);
                buf.writeLong(p.seed);
            },
            buf -> new MeteorStreakPayload(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt(), buf.readLong())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
