package com.soybe.awakeningsystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SystemSyncPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<SystemSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SystemSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.TRUSTED_COMPOUND_TAG, SystemSyncPayload::data, SystemSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
