package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client: play one of the mod's own player animations (player_animations/*.json) on a player.
 * Weapon skills send it when Epic Fight is not installed, so the skill still has a body animation.
 */
public record PlaySkillAnimationPayload(int entityId, String animation) implements CustomPacketPayload {
    public static final Type<PlaySkillAnimationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "play_skill_animation"));

    public static final StreamCodec<FriendlyByteBuf, PlaySkillAnimationPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PlaySkillAnimationPayload::entityId,
                    ByteBufCodecs.STRING_UTF8, PlaySkillAnimationPayload::animation,
                    PlaySkillAnimationPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
