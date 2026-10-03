package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record DonateConstellationPayload(UUID targetPlayerId, int amount, String commentKey) implements CustomPacketPayload {
    public static final Type<DonateConstellationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "donate_constellation"));

    public static final StreamCodec<FriendlyByteBuf, DonateConstellationPayload> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeUUID(val.targetPlayerId);
                buf.writeInt(val.amount);
                buf.writeUtf(val.commentKey);
            },
            buf -> new DonateConstellationPayload(
                    buf.readUUID(),
                    buf.readInt(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
