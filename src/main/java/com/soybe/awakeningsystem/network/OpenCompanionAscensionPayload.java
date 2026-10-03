package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenCompanionAscensionPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<OpenCompanionAscensionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_companion_ascension"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCompanionAscensionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeNbt(p.data()),
            buf -> new OpenCompanionAscensionPayload(buf.readNbt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
