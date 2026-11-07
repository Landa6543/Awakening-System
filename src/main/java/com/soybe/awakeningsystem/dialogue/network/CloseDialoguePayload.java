package com.soybe.awakeningsystem.dialogue.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete para cerrar la sesión de diálogo de manera sincronizada.
 */
public record CloseDialoguePayload(int entityId) implements CustomPacketPayload {

    public static final Type<CloseDialoguePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "close_dialogue"));

    public static final StreamCodec<FriendlyByteBuf, CloseDialoguePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeVarInt(payload.entityId()),
            buf -> new CloseDialoguePayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
