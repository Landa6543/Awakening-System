package com.soybe.awakeningsystem.routine;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A guild NPC's daily routine: places to be at given times of the day. From the time of one stop to the time
 * of the next the NPC goes to that stop and stays around it, every in-game day (the idea of Townfolk's
 * schedules, MIT, written anew for these NPCs). Set from the NPC's dialogue, in a small editor window.
 */
public final class NpcRoutine {
    private NpcRoutine() {}

    public static final int MAX_STOPS = 12;
    /** How far a stop may be from the NPC when it is set. */
    public static final int MAX_DISTANCE = 256;

    /** A place and the time of day (0-23999 ticks; 0 is 6:00) the NPC heads there. */
    public record Stop(BlockPos pos, int time) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Stop> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Stop::pos, ByteBufCodecs.VAR_INT, Stop::time, Stop::new);
    }

    /** The clock time of a time of day, as "HH:MM". */
    public static String clock(int time) {
        int t = Math.floorMod(time, 24000);
        int minutes = (t * 60 / 1000 + 6 * 60) % (24 * 60);
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /** The stop the NPC should be at now: the last one whose time has come today (or the day's last one, before the first). */
    public static Stop current(List<Stop> stops, long dayTime) {
        if (stops.isEmpty()) return null;
        int now = (int) Math.floorMod(dayTime, 24000L);
        Stop best = null, latest = null;
        for (Stop s : stops) {
            if (latest == null || s.time() > latest.time()) latest = s;
            if (s.time() <= now && (best == null || s.time() > best.time())) best = s;
        }
        return best != null ? best : latest;
    }

    public static ListTag write(List<Stop> stops) {
        ListTag list = new ListTag();
        for (Stop s : stops) {
            CompoundTag t = new CompoundTag();
            t.putInt("X", s.pos().getX());
            t.putInt("Y", s.pos().getY());
            t.putInt("Z", s.pos().getZ());
            t.putInt("Time", s.time());
            list.add(t);
        }
        return list;
    }

    public static List<Stop> read(ListTag list) {
        List<Stop> out = new ArrayList<>();
        for (int i = 0; i < list.size() && out.size() < MAX_STOPS; i++) {
            CompoundTag t = list.getCompound(i);
            out.add(new Stop(new BlockPos(t.getInt("X"), t.getInt("Y"), t.getInt("Z")), Math.floorMod(t.getInt("Time"), 24000)));
        }
        return out;
    }

    public static List<Stop> read(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_LIST) ? read(tag.getList(key, Tag.TAG_COMPOUND)) : new ArrayList<>();
    }

    // ---- the editor's messages

    /** Server → client: open the routine editor for this NPC. */
    public record OpenPayload(int npcId, String npcName, boolean on, List<Stop> stops) implements CustomPacketPayload {
        public static final Type<OpenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "routine_open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, OpenPayload::npcId, ByteBufCodecs.STRING_UTF8, OpenPayload::npcName, ByteBufCodecs.BOOL, OpenPayload::on,
                Stop.CODEC.apply(ByteBufCodecs.list(MAX_STOPS)), OpenPayload::stops, OpenPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client → server: the routine as edited. */
    public record SavePayload(int npcId, boolean on, List<Stop> stops) implements CustomPacketPayload {
        public static final Type<SavePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "routine_save"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SavePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SavePayload::npcId, ByteBufCodecs.BOOL, SavePayload::on,
                Stop.CODEC.apply(ByteBufCodecs.list(MAX_STOPS)), SavePayload::stops, SavePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Opens the editor for a player (the NPC's owner). */
    public static void open(ServerPlayer player, SeriesNpcEntity npc) {
        PacketDistributor.sendToPlayer(player, new OpenPayload(npc.getId(), npc.getNpcProfile().displayName(), npc.isRoutineOn(), npc.getRoutine()));
    }

    private static void save(ServerPlayer player, SavePayload p) {
        if (!(player.level().getEntity(p.npcId()) instanceof SeriesNpcEntity npc)) return;
        if (npc.getOwnerUuid() == null || !npc.getOwnerUuid().equals(player.getUUID())) return;
        if (npc.distanceToSqr(player) > 64 * 64 && !player.hasPermissions(2)) return;
        List<Stop> stops = new ArrayList<>();
        for (Stop s : p.stops()) {
            if (stops.size() >= MAX_STOPS) break;
            if (s.pos().distSqr(npc.blockPosition()) > (double) MAX_DISTANCE * MAX_DISTANCE) continue;
            stops.add(new Stop(s.pos(), Math.floorMod(s.time(), 24000)));
        }
        npc.setRoutine(stops, p.on() && !stops.isEmpty());
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(npc.isRoutineOn()
                ? "awakening_system.routine.saved_on" : "awakening_system.routine.saved_off", npc.getNpcProfile().displayName(), stops.size()), true);
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            var r = event.registrar("1").optional();
            r.playToClient(OpenPayload.TYPE, OpenPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                    () -> com.soybe.awakeningsystem.routine.client.RoutineClient.open(payload)));
            r.playToServer(SavePayload.TYPE, SavePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sp) save(sp, payload);
            }));
        }
    }
}
