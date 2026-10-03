package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpgradeArtifactPayload(boolean isCompanion, int entityId, int slot) implements CustomPacketPayload {
    public static final Type<UpgradeArtifactPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "upgrade_artifact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeArtifactPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBoolean(p.isCompanion);
                buf.writeVarInt(p.entityId);
                buf.writeVarInt(p.slot);
            },
            buf -> new UpgradeArtifactPayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
