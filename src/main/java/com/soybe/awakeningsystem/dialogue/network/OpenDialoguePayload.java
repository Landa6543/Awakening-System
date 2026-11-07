package com.soybe.awakeningsystem.dialogue.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.dialogue.DialogueChoice;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Paquete S2C para iniciar o actualizar una escena de diálogo interactiva estilo RPG / manhwa.
 */
public record OpenDialoguePayload(
        int entityId,
        String dialogueId,
        String nodeId,
        String nodeType,
        String speaker,
        String text,
        List<DialogueChoice> choices
) implements CustomPacketPayload {

    public static final Type<OpenDialoguePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_dialogue"));

    public static final StreamCodec<FriendlyByteBuf, OpenDialoguePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeVarInt(payload.entityId());
                buf.writeUtf(payload.dialogueId());
                buf.writeUtf(payload.nodeId());
                buf.writeUtf(payload.nodeType());
                buf.writeUtf(payload.speaker());
                buf.writeUtf(payload.text());
                buf.writeVarInt(payload.choices().size());
                for (DialogueChoice choice : payload.choices()) {
                    DialogueChoice.STREAM_CODEC.encode(buf, choice);
                }
            },
            buf -> {
                int entityId = buf.readVarInt();
                String dialogueId = buf.readUtf();
                String nodeId = buf.readUtf();
                String nodeType = buf.readUtf();
                String speaker = buf.readUtf();
                String text = buf.readUtf();
                int count = buf.readVarInt();
                List<DialogueChoice> choices = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    choices.add(DialogueChoice.STREAM_CODEC.decode(buf));
                }
                return new OpenDialoguePayload(entityId, dialogueId, nodeId, nodeType, speaker, text, choices);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
