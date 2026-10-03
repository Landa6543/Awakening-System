package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenCompanionScreenPayload(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final Type<OpenCompanionScreenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_companion_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCompanionScreenPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId);
                buf.writeNbt(p.data);
            },
            buf -> new OpenCompanionScreenPayload(buf.readVarInt(), buf.readNbt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
