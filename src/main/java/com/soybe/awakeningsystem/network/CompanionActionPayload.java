package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CompanionActionPayload(int entityId, int actionType) implements CustomPacketPayload {
    public static final Type<CompanionActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId);
                buf.writeVarInt(p.actionType);
            },
            buf -> new CompanionActionPayload(buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
