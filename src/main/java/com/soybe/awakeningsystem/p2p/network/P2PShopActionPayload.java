package com.soybe.awakeningsystem.p2p.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload enviado desde el cliente al servidor con una acción en la tienda P2P.
 */
public record P2PShopActionPayload(int action, String listingId, long price, int slotIndex, int count) implements CustomPacketPayload {
    public P2PShopActionPayload(int action, String listingId, long price) {
        this(action, listingId, price, -1, 1);
    }

    public static final Type<P2PShopActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "p2p_shop_action")
    );

    public static final int ACTION_BUY = 0;
    public static final int ACTION_CANCEL = 1;
    public static final int ACTION_CREATE = 2;
    public static final int ACTION_CLAIM_EARNINGS = 3;
    public static final int ACTION_REQUEST_SYNC = 4;

    public static final StreamCodec<RegistryFriendlyByteBuf, P2PShopActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.action);
                buf.writeUtf(payload.listingId);
                buf.writeVarLong(payload.price);
                buf.writeVarInt(payload.slotIndex);
                buf.writeVarInt(payload.count);
            },
            buf -> new P2PShopActionPayload(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readVarLong(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
