package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CompanionAscensionActionPayload(int entityId, int contractSlot, int actionType, int extra) implements CustomPacketPayload {
    public static final Type<CompanionAscensionActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ascension_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionAscensionActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId());
                buf.writeVarInt(p.contractSlot());
                buf.writeVarInt(p.actionType());
                buf.writeVarInt(p.extra());
            },
            buf -> new CompanionAscensionActionPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
