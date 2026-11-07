package com.soybe.awakeningsystem.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Representa una opción de respuesta interactiva en el sistema de diálogo estilo manhwa / RPG.
 */
public record DialogueChoice(int id, String text, String nextNodeId, String action) {
    public static final StreamCodec<FriendlyByteBuf, DialogueChoice> STREAM_CODEC = StreamCodec.ofMember(
            (choice, buf) -> {
                buf.writeVarInt(choice.id());
                buf.writeUtf(choice.text());
                buf.writeUtf(choice.nextNodeId());
                buf.writeUtf(choice.action());
            },
            buf -> new DialogueChoice(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf()
            )
    );
}
