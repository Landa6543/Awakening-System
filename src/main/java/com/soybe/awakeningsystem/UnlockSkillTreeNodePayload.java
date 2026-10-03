package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UnlockSkillTreeNodePayload(String skillId) implements CustomPacketPayload {
    public static final Type<UnlockSkillTreeNodePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "unlock_skill_tree_node"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockSkillTreeNodePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), UnlockSkillTreeNodePayload::skillId, UnlockSkillTreeNodePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
