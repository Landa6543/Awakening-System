package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StatueActionC2SPayload(int actionId, String element) implements CustomPacketPayload {
    public static final int ACTION_BLESSING = 1;
    public static final int ACTION_OFFER_OCULI = 2;
    public static final int ACTION_RESONANCE = 3;

    public static final Type<StatueActionC2SPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "statue_action"));

    public static final StreamCodec<FriendlyByteBuf, StatueActionC2SPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StatueActionC2SPayload::actionId,
            ByteBufCodecs.STRING_UTF8, StatueActionC2SPayload::element,
            StatueActionC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
