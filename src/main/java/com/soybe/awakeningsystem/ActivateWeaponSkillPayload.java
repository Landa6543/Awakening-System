package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivateWeaponSkillPayload(int handOrdinal) implements CustomPacketPayload {
    public static final Type<ActivateWeaponSkillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "activate_weapon_skill"));

    public static final StreamCodec<FriendlyByteBuf, ActivateWeaponSkillPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ActivateWeaponSkillPayload::handOrdinal,
                    ActivateWeaponSkillPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
