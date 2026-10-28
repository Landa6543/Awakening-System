package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S enviado por el cliente para solicitar una sincronización completa
 * de SystemData tras cargar nivel, viajar entre dimensiones o abrir pantallas del Sistema.
 */
public record RequestSyncPayload() implements CustomPacketPayload {
    public static final Type<RequestSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "request_system_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSyncPayload> STREAM_CODEC = StreamCodec.unit(new RequestSyncPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
