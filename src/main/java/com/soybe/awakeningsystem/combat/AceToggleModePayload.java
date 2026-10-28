package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload sent to synchronize combat stance mode changes between client and server.
 */
public record AceToggleModePayload(String modeId) implements CustomPacketPayload {
    public static final Type<AceToggleModePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ace_toggle_mode"));

    public static final StreamCodec<FriendlyByteBuf, AceToggleModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    AceToggleModePayload::modeId,
                    AceToggleModePayload::new
            );

    public AceCombatMode getMode() {
        return AceCombatMode.fromId(modeId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
