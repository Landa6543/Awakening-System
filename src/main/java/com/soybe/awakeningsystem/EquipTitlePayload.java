package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EquipTitlePayload(String titleId) implements CustomPacketPayload {
    public static final Type<EquipTitlePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "equip_title"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipTitlePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(48), EquipTitlePayload::titleId, EquipTitlePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
