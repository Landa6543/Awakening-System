package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectBountyPayload(int bountyIndex, boolean claim, boolean refresh) implements CustomPacketPayload {
    public static final Type<SelectBountyPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "select_bounty"));

    public static final StreamCodec<FriendlyByteBuf, SelectBountyPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeInt(payload.bountyIndex());
                buf.writeBoolean(payload.claim());
                buf.writeBoolean(payload.refresh());
            },
            buf -> new SelectBountyPayload(buf.readInt(), buf.readBoolean(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}