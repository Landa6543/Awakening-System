package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClaimAbyssRewardPayload(int floorNumber) implements CustomPacketPayload {
    public static final Type<ClaimAbyssRewardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "claim_abyss_reward")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimAbyssRewardPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeVarInt(p.floorNumber),
            buf -> new ClaimAbyssRewardPayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
