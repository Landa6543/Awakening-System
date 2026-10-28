package com.soybe.awakeningsystem.uma.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record OpenUmaRacePayload(
        int raceState,
        int stateTimer,
        List<String> participantIds,
        String myBetUmaId,
        ItemStack myWagerItem
) implements CustomPacketPayload {

    public static final Type<OpenUmaRacePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_uma_race"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenUmaRacePayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeVarInt(payload.raceState());
                buf.writeVarInt(payload.stateTimer());
                buf.writeVarInt(payload.participantIds().size());
                for (String id : payload.participantIds()) {
                    buf.writeUtf(id);
                }
                buf.writeUtf(payload.myBetUmaId());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.myWagerItem());
            },
            buf -> {
                int state = buf.readVarInt();
                int timer = buf.readVarInt();
                int size = buf.readVarInt();
                List<String> ids = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ids.add(buf.readUtf());
                }
                String betUma = buf.readUtf();
                ItemStack wager = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                return new OpenUmaRacePayload(state, timer, ids, betUma, wager);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
