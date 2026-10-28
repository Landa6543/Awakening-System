package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncGuildClaimsPayload(CompoundTag claimsTag) implements CustomPacketPayload {
    public static final Type<SyncGuildClaimsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sync_guild_claims"));

    public static final StreamCodec<FriendlyByteBuf, SyncGuildClaimsPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> buf.writeNbt(payload.claimsTag()),
            buf -> new SyncGuildClaimsPayload(buf.readNbt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
