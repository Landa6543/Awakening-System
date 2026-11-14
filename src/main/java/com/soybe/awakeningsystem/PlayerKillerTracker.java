package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Player killers, as in the series: a player who has killed {@value #RED_AT} or more other players (enough
 * that it was no accident in a crowded fight) is marked, and in the full-dive style their cursor turns from
 * green to red for everyone. The count is kept with the world and the list of marked players is sent to every
 * client when it changes and when someone joins.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class PlayerKillerTracker {
    private PlayerKillerTracker() {}

    public static final int RED_AT = 5;

    /** On the client: who is marked red right now. */
    public static final Set<UUID> RED = new HashSet<>();

    // ── what the server sends ──
    public record RedListPayload(List<UUID> reds) implements CustomPacketPayload {
        public static final Type<RedListPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "player_killers"));
        public static final StreamCodec<FriendlyByteBuf, RedListPayload> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), RedListPayload::reds, RedListPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Network {
        private Network() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(RedListPayload.TYPE, RedListPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                RED.clear();
                RED.addAll(payload.reds());
            }));
        }
    }

    // ── the count, saved with the world ──
    public static final class Data extends SavedData {
        final Map<UUID, Integer> kills = new HashMap<>();

        static Data get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(Data::new, Data::load, null), "awakening_player_killers");
        }

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            for (String k : tag.getAllKeys()) {
                try {
                    d.kills.put(UUID.fromString(k), tag.getInt(k));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            kills.forEach((id, n) -> tag.putInt(id.toString(), n));
            return tag;
        }

        List<UUID> reds() {
            List<UUID> out = new ArrayList<>();
            kills.forEach((id, n) -> {
                if (n >= RED_AT) out.add(id);
            });
            return out;
        }
    }

    public static int kills(MinecraftServer server, UUID player) {
        return Data.get(server).kills.getOrDefault(player, 0);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer) || killer == victim) return;
        MinecraftServer server = killer.getServer();
        if (server == null) return;
        Data d = Data.get(server);
        int n = d.kills.getOrDefault(killer.getUUID(), 0) + 1;
        d.kills.put(killer.getUUID(), n);
        d.setDirty();
        if (n == RED_AT) PacketDistributor.sendToAllPlayers(new RedListPayload(d.reds()));
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.getServer() != null) {
            PacketDistributor.sendToPlayer(p, new RedListPayload(Data.get(p.getServer()).reds()));
        }
    }
}
