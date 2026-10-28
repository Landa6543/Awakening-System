package com.soybe.awakeningsystem.p2p.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.p2p.P2PListing;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload enviado desde el servidor al cliente con las ofertas activas del mercado P2P
 * y las ganancias pendientes de reclamar por el jugador.
 */
public record SyncP2PShopPayload(List<P2PListing> listings, long myPendingEarnings) implements CustomPacketPayload {
    public static final Type<SyncP2PShopPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_p2p_shop")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncP2PShopPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.listings.size());
                for (P2PListing listing : payload.listings) {
                    listing.encode(buf);
                }
                buf.writeVarLong(payload.myPendingEarnings);
            },
            buf -> {
                int size = buf.readVarInt();
                List<P2PListing> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(P2PListing.decode(buf));
                }
                long earnings = buf.readVarLong();
                return new SyncP2PShopPayload(list, earnings);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
