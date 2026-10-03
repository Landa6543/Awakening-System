package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EvolveSkillPayload(String skillId) implements CustomPacketPayload {
    public static final Type<EvolveSkillPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "evolve_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EvolveSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(48), EvolveSkillPayload::skillId, EvolveSkillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
