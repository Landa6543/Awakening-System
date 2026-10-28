package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleAgilitySpeedPayload(boolean dummy) implements CustomPacketPayload {
    public static final Type<ToggleAgilitySpeedPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "toggle_agility_speed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAgilitySpeedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ToggleAgilitySpeedPayload::dummy, ToggleAgilitySpeedPayload::new);

    public ToggleAgilitySpeedPayload() {
        this(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
