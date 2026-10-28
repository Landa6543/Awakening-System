package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S para activar la Habilidad de la Runa Activa en la ranura especificada (0 o 1).
 */
public record ActivateRunePayload(int slot) implements CustomPacketPayload {
    public static final Type<ActivateRunePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "activate_rune"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivateRunePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeVarInt(p.slot),
            buf -> new ActivateRunePayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
