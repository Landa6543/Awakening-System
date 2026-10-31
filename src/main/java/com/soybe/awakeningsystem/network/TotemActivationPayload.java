package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Paquete S2C para activar la animación 3D flotante del tótem en la pantalla del cliente.
 */
public record TotemActivationPayload(ItemStack totemStack) implements CustomPacketPayload {
    public static final Type<TotemActivationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "totem_activation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TotemActivationPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, p.totemStack()),
            buf -> new TotemActivationPayload(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
