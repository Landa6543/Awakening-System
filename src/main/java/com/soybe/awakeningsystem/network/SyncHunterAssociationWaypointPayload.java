package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete S2C que sincroniza y crea el marcador de mapa y haz de luz vertical
 * hacia la Sede de la Asociación de Cazadores en el cliente del jugador.
 */
public record SyncHunterAssociationWaypointPayload(int x, int y, int z) implements CustomPacketPayload {
    public static final Type<SyncHunterAssociationWaypointPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_hunter_association_waypoint"));

    public static final StreamCodec<FriendlyByteBuf, SyncHunterAssociationWaypointPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    (payload, buf) -> {
                        buf.writeInt(payload.x());
                        buf.writeInt(payload.y());
                        buf.writeInt(payload.z());
                    },
                    buf -> new SyncHunterAssociationWaypointPayload(buf.readInt(), buf.readInt(), buf.readInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
