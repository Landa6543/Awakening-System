package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EquipSkillSlotPayload(boolean isPassive, int slot, String skillId) implements CustomPacketPayload {
    public static final Type<EquipSkillSlotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "equip_skill_slot"));

    public static final StreamCodec<FriendlyByteBuf, EquipSkillSlotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    EquipSkillSlotPayload::isPassive,
                    ByteBufCodecs.VAR_INT,
                    EquipSkillSlotPayload::slot,
                    ByteBufCodecs.STRING_UTF8,
                    EquipSkillSlotPayload::skillId,
                    EquipSkillSlotPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
