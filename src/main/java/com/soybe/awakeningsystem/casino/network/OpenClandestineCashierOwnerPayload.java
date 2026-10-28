package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenClandestineCashierOwnerPayload(
        BlockPos pos,
        String ownerName,
        int stockChips,
        long collectedMora,
        int totalChipsSold,
        int chipsInPlayerInventory,
        long playerMora
) implements CustomPacketPayload {
    public static final Type<OpenClandestineCashierOwnerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_clandestine_cashier_owner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenClandestineCashierOwnerPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeUtf(p.ownerName, 64);
                buf.writeVarInt(p.stockChips);
                buf.writeVarLong(p.collectedMora);
                buf.writeVarInt(p.totalChipsSold);
                buf.writeVarInt(p.chipsInPlayerInventory);
                buf.writeVarLong(p.playerMora);
            },
            buf -> new OpenClandestineCashierOwnerPayload(
                    buf.readBlockPos(),
                    buf.readUtf(64),
                    buf.readVarInt(),
                    buf.readVarLong(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarLong()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
