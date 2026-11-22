package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ManageShadowSquadPayload(int action, int squadId, int shadowIndex, String squadName) implements CustomPacketPayload {
    public static final int ACTION_DEPLOY_SQUAD = 0;
    public static final int ACTION_RECALL_SQUAD = 1;
    public static final int ACTION_DEPLOY_ALL = 2;
    public static final int ACTION_RECALL_ALL = 3;
    public static final int ACTION_RENAME_SQUAD = 4;
    public static final int ACTION_MOVE_SHADOW = 5;

    public static final Type<ManageShadowSquadPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "manage_shadow_squad"));
    public static final StreamCodec<ByteBuf, ManageShadowSquadPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ManageShadowSquadPayload::action,
            ByteBufCodecs.VAR_INT, ManageShadowSquadPayload::squadId,
            ByteBufCodecs.VAR_INT, ManageShadowSquadPayload::shadowIndex,
            ByteBufCodecs.STRING_UTF8, ManageShadowSquadPayload::squadName,
            ManageShadowSquadPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
