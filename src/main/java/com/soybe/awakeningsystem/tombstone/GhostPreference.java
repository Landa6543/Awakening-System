package com.soybe.awakeningsystem.tombstone;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Each player's choice of whether dying turns them into a ghost until they reach their tombstone (on by default).
 * The choice lives in the player's own game settings and is told to the server when they join and when it changes;
 * the server keeps the players who turned it off, so it holds even while they are offline.
 */
public final class GhostPreference {
    private GhostPreference() {}

    /** Whether this player becomes a ghost on death. */
    public static boolean ghostOnDeath(ServerPlayer player) {
        return !Store.get(player.server).off.contains(player.getUUID());
    }

    public record Payload(boolean ghostOnDeath) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ghost_preference"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> buf.writeBoolean(p.ghostOnDeath), buf -> new Payload(buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").optional().playToServer(Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sp) {
                    Store store = Store.get(sp.server);
                    boolean changed = payload.ghostOnDeath() ? store.off.remove(sp.getUUID()) : store.off.add(sp.getUUID());
                    if (changed) store.setDirty();
                }
            }));
        }
    }

    static final class Store extends SavedData {
        final Set<UUID> off = new HashSet<>();

        static Store get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(Store::new, Store::load, null), "awakening_ghost_preference");
        }

        static Store load(CompoundTag tag, HolderLookup.Provider registries) {
            Store s = new Store();
            for (var t : tag.getList("off", 8)) {
                try { s.off.add(UUID.fromString(t.getAsString())); } catch (IllegalArgumentException ignored) {}
            }
            return s;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (UUID u : off) list.add(StringTag.valueOf(u.toString()));
            tag.put("off", list);
            return tag;
        }
    }
}
