package com.soybe.awakeningsystem.uma.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record UmaGachaResultPayload(
        int remainingCarats,
        List<GachaCardEntry> entries
) implements CustomPacketPayload {

    public static final Type<UmaGachaResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_gacha_result"));

    public record GachaCardEntry(
            String umaId,
            int rarity,
            boolean isNew,
            int shardsGained
    ) {
        public static void encode(RegistryFriendlyByteBuf buf, GachaCardEntry e) {
            buf.writeUtf(e.umaId);
            buf.writeVarInt(e.rarity);
            buf.writeBoolean(e.isNew);
            buf.writeVarInt(e.shardsGained);
        }

        public static GachaCardEntry decode(RegistryFriendlyByteBuf buf) {
            return new GachaCardEntry(
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readVarInt()
            );
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, UmaGachaResultPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeVarInt(payload.remainingCarats());
                buf.writeVarInt(payload.entries().size());
                for (GachaCardEntry e : payload.entries()) {
                    GachaCardEntry.encode(buf, e);
                }
            },
            buf -> {
                int carats = buf.readVarInt();
                int size = buf.readVarInt();
                List<GachaCardEntry> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(GachaCardEntry.decode(buf));
                }
                return new UmaGachaResultPayload(carats, list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
