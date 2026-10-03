package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StartAbyssFloorPayload(int floorNumber) implements CustomPacketPayload {
    public static final Type<StartAbyssFloorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "start_abyss_floor")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StartAbyssFloorPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeVarInt(p.floorNumber),
            buf -> new StartAbyssFloorPayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
