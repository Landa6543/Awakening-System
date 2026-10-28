package com.soybe.awakeningsystem.uma.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UmaHubActionPayload(
        String action,
        BlockPos trackPos,
        String paramString,
        int paramInt
) implements CustomPacketPayload {

    public static final Type<UmaHubActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_hub_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UmaHubActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeUtf(payload.action());
                buf.writeBlockPos(payload.trackPos() != null ? payload.trackPos() : BlockPos.ZERO);
                buf.writeUtf(payload.paramString() != null ? payload.paramString() : "");
                buf.writeVarInt(payload.paramInt());
            },
            buf -> new UmaHubActionPayload(
                    buf.readUtf(),
                    buf.readBlockPos(),
                    buf.readUtf(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
