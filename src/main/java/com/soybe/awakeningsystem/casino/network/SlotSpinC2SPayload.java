package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SlotSpinC2SPayload(BlockPos pos, int betBase, int linesMode) implements CustomPacketPayload {
    public static final Type<SlotSpinC2SPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "slot_spin_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotSpinC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeVarInt(p.betBase);
                buf.writeVarInt(p.linesMode);
            },
            buf -> new SlotSpinC2SPayload(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
