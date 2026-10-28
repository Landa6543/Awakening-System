package com.soybe.awakeningsystem.uma.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UmaRaceActionC2SPayload(
        String action,
        String chosenUmaId,
        int inventorySlotIndex,
        int amountToWager
) implements CustomPacketPayload {

    public static final Type<UmaRaceActionC2SPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_race_action"));

    public static final StreamCodec<FriendlyByteBuf, UmaRaceActionC2SPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeUtf(payload.action());
                buf.writeUtf(payload.chosenUmaId());
                buf.writeVarInt(payload.inventorySlotIndex());
                buf.writeVarInt(payload.amountToWager());
            },
            buf -> new UmaRaceActionC2SPayload(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
