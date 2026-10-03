package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete C2S para la colocación automática continua de vías de tren estilo Create.
 * Envía la posición de anclaje inicial y la posición de destino seleccionada por el jugador.
 */
public record PlaceTrackLinePayload(BlockPos startPos, BlockPos endPos, boolean isWideTrack) implements CustomPacketPayload {
    public static final Type<PlaceTrackLinePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "place_track_line"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceTrackLinePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.startPos);
                buf.writeBlockPos(p.endPos);
                buf.writeBoolean(p.isWideTrack);
            },
            buf -> new PlaceTrackLinePayload(buf.readBlockPos(), buf.readBlockPos(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
