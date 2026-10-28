package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Client-to-server attack payload for the Awakening Combat Engine (ACE).
 * Transmits target entity IDs detected by the sweeping arc collision engine.
 */
public record AceAttackPacket(List<Integer> targetIds, int comboStep) implements CustomPacketPayload {
    public static final Type<AceAttackPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ace_attack"));

    public static final StreamCodec<FriendlyByteBuf, AceAttackPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
                    AceAttackPacket::targetIds,
                    ByteBufCodecs.VAR_INT,
                    AceAttackPacket::comboStep,
                    AceAttackPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
