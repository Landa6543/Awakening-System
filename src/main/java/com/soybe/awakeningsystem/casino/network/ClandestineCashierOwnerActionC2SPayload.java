package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClandestineCashierOwnerActionC2SPayload(
        BlockPos pos,
        int action,
        int amount
) implements CustomPacketPayload {
    public static final Type<ClandestineCashierOwnerActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "clandestine_cashier_owner_action_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClandestineCashierOwnerActionC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeVarInt(p.action);
                buf.writeVarInt(p.amount);
            },
            buf -> new ClandestineCashierOwnerActionC2SPayload(
                    buf.readBlockPos(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
