package com.soybe.awakeningsystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BankTransactionPayload(String action, int amount) implements CustomPacketPayload {
    public static final Type<BankTransactionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "bank_transaction"));

    public static final StreamCodec<FriendlyByteBuf, BankTransactionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BankTransactionPayload::action,
                    ByteBufCodecs.VAR_INT, BankTransactionPayload::amount,
                    BankTransactionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
