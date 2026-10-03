package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CloseSlotMachineC2SPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<CloseSlotMachineC2SPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "close_slot_machine_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseSlotMachineC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeBlockPos(p.pos),
            buf -> new CloseSlotMachineC2SPayload(buf.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
