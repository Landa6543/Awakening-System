package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S para equipar o desequipar un Cono de Luz en la ranura dedicada de SystemData.
 * invSlot >= 0 equipa desde el inventario del jugador (intercambiando el anterior).
 * invSlot == -1 desequipa el cono actual al inventario.
 */
public record EquipLightConePayload(int invSlot) implements CustomPacketPayload {
    public static final Type<EquipLightConePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "equip_light_cone"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipLightConePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeVarInt(p.invSlot),
            buf -> new EquipLightConePayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
