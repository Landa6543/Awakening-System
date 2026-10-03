package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivateArmorSkillPayload() implements CustomPacketPayload {
    public static final Type<ActivateArmorSkillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "activate_armor_skill"));

    public static final StreamCodec<FriendlyByteBuf, ActivateArmorSkillPayload> STREAM_CODEC =
            StreamCodec.unit(new ActivateArmorSkillPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
