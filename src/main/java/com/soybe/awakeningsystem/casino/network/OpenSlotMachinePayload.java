package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenSlotMachinePayload(
        BlockPos pos,
        long moraBalance,
        int betBase,
        int linesMode,
        boolean isIllegal,
        int chipsAvailable,
        int reserveChips,
        boolean isOwner
) implements CustomPacketPayload {
    public static final Type<OpenSlotMachinePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_slot_machine"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSlotMachinePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeVarLong(p.moraBalance);
                buf.writeVarInt(p.betBase);
                buf.writeVarInt(p.linesMode);
                buf.writeBoolean(p.isIllegal);
                buf.writeVarInt(p.chipsAvailable);
                buf.writeVarInt(p.reserveChips);
                buf.writeBoolean(p.isOwner);
            },
            buf -> new OpenSlotMachinePayload(
                    buf.readBlockPos(),
                    buf.readVarLong(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
