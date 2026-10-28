package com.soybe.awakeningsystem.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete de Red S2C para Anuncios Globales en Pantalla Estilo ARK.
 * Muestra un banner cinemático superior translúcido para eventos de clase mítica.
 */
public record SystemAnnouncementPayload(String title, String message, int rarityColor) implements CustomPacketPayload {
    public static final Type<SystemAnnouncementPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_announcement"));

    public static final StreamCodec<ByteBuf, SystemAnnouncementPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SystemAnnouncementPayload::title,
            ByteBufCodecs.STRING_UTF8, SystemAnnouncementPayload::message,
            ByteBufCodecs.VAR_INT, SystemAnnouncementPayload::rarityColor,
            SystemAnnouncementPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
