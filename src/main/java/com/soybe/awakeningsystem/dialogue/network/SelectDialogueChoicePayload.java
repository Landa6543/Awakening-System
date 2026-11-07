package com.soybe.awakeningsystem.dialogue.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S enviado por el cliente al seleccionar una opción de diálogo.
 */
public record SelectDialogueChoicePayload(
        int entityId,
        String dialogueId,
        String nodeId,
        int choiceId
) implements CustomPacketPayload {

    public static final Type<SelectDialogueChoicePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "select_dialogue_choice"));

    public static final StreamCodec<FriendlyByteBuf, SelectDialogueChoicePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeVarInt(payload.entityId());
                buf.writeUtf(payload.dialogueId());
                buf.writeUtf(payload.nodeId());
                buf.writeVarInt(payload.choiceId());
            },
            buf -> new SelectDialogueChoicePayload(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
