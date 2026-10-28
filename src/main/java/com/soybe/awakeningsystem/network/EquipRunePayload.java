package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S para engarzar o desengarzar una Piedra de Runa en ranuras Activas (0..1) o Pasivas (0..1).
 * invSlot >= 0 engarza desde el inventario.
 * invSlot == -1 desengarza la runa al inventario.
 */
public record EquipRunePayload(boolean isActiveRune, int slot, int invSlot) implements CustomPacketPayload {
    public static final Type<EquipRunePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "equip_rune"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipRunePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBoolean(p.isActiveRune);
                buf.writeVarInt(p.slot);
                buf.writeVarInt(p.invSlot);
            },
            buf -> new EquipRunePayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
