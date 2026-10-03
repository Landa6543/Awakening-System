package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectHunterClassPayload(String hunterClass) implements CustomPacketPayload {
    public static final Type<SelectHunterClassPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "select_hunter_class"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectHunterClassPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(48), SelectHunterClassPayload::hunterClass, SelectHunterClassPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
