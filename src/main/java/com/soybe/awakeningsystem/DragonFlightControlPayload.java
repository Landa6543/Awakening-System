package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DragonFlightControlPayload(boolean space, boolean shift, boolean sprint) implements CustomPacketPayload {
    public static final Type<DragonFlightControlPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dragon_flight_control"));

    public static final StreamCodec<FriendlyByteBuf, DragonFlightControlPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, DragonFlightControlPayload::space,
                    ByteBufCodecs.BOOL, DragonFlightControlPayload::shift,
                    ByteBufCodecs.BOOL, DragonFlightControlPayload::sprint,
                    DragonFlightControlPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
