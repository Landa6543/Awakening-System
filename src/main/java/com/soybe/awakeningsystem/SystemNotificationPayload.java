package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SystemNotificationPayload(String titleKey, String messageKey, String argument, int color) implements CustomPacketPayload {
    public static final Type<SystemNotificationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_notification"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SystemNotificationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), SystemNotificationPayload::titleKey,
            ByteBufCodecs.stringUtf8(512), SystemNotificationPayload::messageKey,
            ByteBufCodecs.stringUtf8(512), SystemNotificationPayload::argument,
            ByteBufCodecs.VAR_INT, SystemNotificationPayload::color,
            SystemNotificationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
