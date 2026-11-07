package com.soybe.awakeningsystem.dialogue.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S enviado por el cliente al descartar o cerrar la pantalla de diálogo (ej. con ESC o fuera de foco).
 */
public record DismissDialoguePayload(int entityId) implements CustomPacketPayload {

    public static final Type<DismissDialoguePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dismiss_dialogue"));

    public static final StreamCodec<FriendlyByteBuf, DismissDialoguePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeVarInt(payload.entityId()),
            buf -> new DismissDialoguePayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
