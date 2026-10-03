package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenGuildScreenPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<OpenGuildScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_guild_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenGuildScreenPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeNbt(payload.data()),
            buf -> new OpenGuildScreenPayload(buf.readNbt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
