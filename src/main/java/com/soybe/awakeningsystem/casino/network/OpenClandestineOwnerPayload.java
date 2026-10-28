package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenClandestineOwnerPayload(
        BlockPos pos,
        String ownerName,
        int reserveChips,
        int vaultChips,
        int totalCustomerSpins,
        int chipsInPlayerInventory
) implements CustomPacketPayload {
    public static final Type<OpenClandestineOwnerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_clandestine_owner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenClandestineOwnerPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeUtf(p.ownerName, 64);
                buf.writeVarInt(p.reserveChips);
                buf.writeVarInt(p.vaultChips);
                buf.writeVarInt(p.totalCustomerSpins);
                buf.writeVarInt(p.chipsInPlayerInventory);
            },
            buf -> new OpenClandestineOwnerPayload(
                    buf.readBlockPos(),
                    buf.readUtf(64),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
