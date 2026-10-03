package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MurimAlchemyRefinePayload(BlockPos pos, int recipeIndex) implements CustomPacketPayload {
    public static final Type<MurimAlchemyRefinePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "murim_alchemy_refine"));

    public static final StreamCodec<FriendlyByteBuf, MurimAlchemyRefinePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeInt(payload.recipeIndex());
            },
            buf -> new MurimAlchemyRefinePayload(buf.readBlockPos(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
