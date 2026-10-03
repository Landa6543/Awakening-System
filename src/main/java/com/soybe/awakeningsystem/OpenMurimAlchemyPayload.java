package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenMurimAlchemyPayload(BlockPos pos, boolean hasHeat) implements CustomPacketPayload {
    public static final Type<OpenMurimAlchemyPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_murim_alchemy"));

    public static final StreamCodec<FriendlyByteBuf, OpenMurimAlchemyPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeBoolean(payload.hasHeat());
            },
            buf -> new OpenMurimAlchemyPayload(buf.readBlockPos(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
