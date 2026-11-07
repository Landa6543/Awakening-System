package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete S2C que notifica al cliente que su tumba ha sido recuperada,
 * eliminando automáticamente el marcador de muerte de Diorama y Awakening System.
 */
public record ClearDeathWaypointPayload() implements CustomPacketPayload {
    public static final Type<ClearDeathWaypointPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "clear_death_waypoint"));
    public static final StreamCodec<FriendlyByteBuf, ClearDeathWaypointPayload> STREAM_CODEC =
            StreamCodec.unit(new ClearDeathWaypointPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
