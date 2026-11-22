package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * The live view through a giant ring, server side. For each world being looked into it keeps the blocks round
 * the other ring (each cell: block state and its sky and block light). A player who comes near a ring gets the
 * whole box once; then, every second, only the 16-block sections that changed; and ten times a second the
 * creatures and players over there, where they are and which way they look. The client draws all of it inside
 * the ring (see {@code client.AethergardWindow}): the other world, live.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class RingLiveView {
    private RingLiveView() {}

    /** Half sizes of the box: across the opening, up and down, and through it (each side). */
    static final int HALF_U = 36, HALF_V = 34, HALF_W = 56;
    static final double VIEW_RANGE = 200;
    private static final int MAX_ENTITIES = 80;
    /** Keeps the far ring's land loaded (and its creatures moving) for a while after each renewal. */
    private static final net.minecraft.server.level.TicketType<Long> VIEW_TICKET =
            net.minecraft.server.level.TicketType.create("awakening_ring_view", Long::compare, 300);

    /** One world being looked into: the box round its ring and what is in it. */
    private static final class Remote {
        final AethergardGate.End there;
        final int hx, hy, hz, sx, sy, sz;
        int[] cells;
        int cursor;
        final Set<Integer> dirty = new LinkedHashSet<>();

        Remote(AethergardGate.End there) {
            this.there = there;
            boolean alongX = there.facing().getAxis() == Direction.Axis.X;
            hx = alongX ? HALF_W : HALF_U;
            hz = alongX ? HALF_U : HALF_W;
            hy = HALF_V;
            sx = hx * 2 + 1;
            sy = hy * 2 + 1;
            sz = hz * 2 + 1;
        }

        BlockPos origin() {
            return there.centre().offset(-hx, -hy, -hz);
        }

        /** Reads every cell of the box from the world (loading its chunks if needed). */
        int[] read() {
            int[] out = new int[sx * sy * sz];
            BlockPos o = origin();
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            ServerLevel level = there.level();
            int i = 0;
            for (int y = 0; y < sy; y++)
                for (int z = 0; z < sz; z++)
                    for (int x = 0; x < sx; x++) {
                        m.set(o.getX() + x, o.getY() + y, o.getZ() + z);
                        int id = Block.getId(level.getBlockState(m));
                        int light = (level.getBrightness(LightLayer.SKY, m) << 4) | level.getBrightness(LightLayer.BLOCK, m);
                        out[i++] = (id << 8) | light;
                    }
            return out;
        }
    }

    private static final Map<String, Remote> REMOTES = new HashMap<>();
    /** Which world each player is looking into, and at which ring here. */
    private static final Map<UUID, String> VIEWING = new HashMap<>();
    private static final Map<UUID, BlockPos> VIEW_AT = new HashMap<>();

    private static String key(AethergardGate.End end) {
        return end.level().dimension().location().toString();
    }

    /** Called for a player near a ring: starts their view when they come close, ends it when they leave. */
    static void update(ServerPlayer p, AethergardGate.End here, AethergardGate.End there) {
        double dist = p.position().distanceTo(Vec3.atCenterOf(here.centre()));
        if (dist > VIEW_RANGE) {
            stop(p);
            return;
        }
        String key = key(there);
        if (key.equals(VIEWING.get(p.getUUID())) && here.centre().equals(VIEW_AT.get(p.getUUID()))) return;
        Remote r = REMOTES.computeIfAbsent(key, k -> new Remote(there));
        if (r.cells == null) r.cells = r.read();
        String biome = there.level().dimension().equals(AethergardGate.AETHERGARD) ? "awakening_system:aethelgard_oakhaven_plains" : "minecraft:plains";
        PacketDistributor.sendToPlayer(p, new FullPayload(true, here.centre(), there.facing(), r.hx, r.hy, r.hz, pack(r.cells), biome));
        VIEWING.put(p.getUUID(), key);
        VIEW_AT.put(p.getUUID(), here.centre());
        AwakeningSystemMod.LOGGER.debug("Ring view: {} looks into {}", p.getName().getString(), key);
    }

    static void stop(ServerPlayer p) {
        if (VIEWING.remove(p.getUUID()) != null) {
            VIEW_AT.remove(p.getUUID());
            PacketDistributor.sendToPlayer(p, FullPayload.NONE);
        }
    }

    /** Every tick: the changed sections once a second, the creatures ten times a second. */
    static void tick(MinecraftServer server) {
        if (VIEWING.isEmpty()) {
            REMOTES.clear();
            return;
        }
        int t = server.getTickCount();
        Map<String, List<ServerPlayer>> viewers = new HashMap<>();
        for (var e : new ArrayList<>(VIEWING.entrySet())) {
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p == null) {
                VIEWING.remove(e.getKey());
                VIEW_AT.remove(e.getKey());
                continue;
            }
            viewers.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(p);
        }
        REMOTES.keySet().retainAll(viewers.keySet());
        for (var e : viewers.entrySet()) {
            Remote r = REMOTES.get(e.getKey());
            if (r == null || r.cells == null) continue;
            // the other side stays awake while someone looks at it, so its creatures move and its water flows
            if (t % 100 == 0) r.there.level().getChunkSource().addRegionTicket(VIEW_TICKET,
                    new net.minecraft.world.level.ChunkPos(r.there.centre()), 6, r.there.centre().asLong());
            scan(r);
            if (t % 20 == 0) sendChanges(r, e.getValue());
            if (t % 2 == 0) sendEntities(r, e.getValue());
        }
    }

    /** Sections looked at again each tick: the whole box is gone over every second and a half or so. */
    private static final int SECTIONS_PER_TICK = 6;

    /** Reads a few more sections of the box from the world; those that changed wait to be sent. */
    private static void scan(Remote r) {
        int nx = (r.sx + 15) / 16, ny = (r.sy + 15) / 16, nz = (r.sz + 15) / 16, total = nx * ny * nz;
        BlockPos o = r.origin();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        ServerLevel level = r.there.level();
        for (int k = 0; k < SECTIONS_PER_TICK; k++) {
            int s = r.cursor++ % total;
            int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
            boolean diff = false;
            for (int y = cy * 16; y < Math.min(r.sy, cy * 16 + 16); y++)
                for (int z = cz * 16; z < Math.min(r.sz, cz * 16 + 16); z++)
                    for (int x = cx * 16; x < Math.min(r.sx, cx * 16 + 16); x++) {
                        m.set(o.getX() + x, o.getY() + y, o.getZ() + z);
                        if (!level.isLoaded(m)) continue;
                        int light = (level.getBrightness(LightLayer.SKY, m) << 4) | level.getBrightness(LightLayer.BLOCK, m);
                        int v = (Block.getId(level.getBlockState(m)) << 8) | light;
                        int i = (y * r.sz + z) * r.sx + x;
                        if (r.cells[i] != v) {
                            r.cells[i] = v;
                            diff = true;
                        }
                    }
            if (diff) r.dirty.add(s);
        }
    }

    private static void sendChanges(Remote r, List<ServerPlayer> to) {
        if (r.dirty.isEmpty()) return;
        int nx = (r.sx + 15) / 16, nz = (r.sz + 15) / 16;
        List<SectionPayload.Section> changed = new ArrayList<>();
        for (int s : r.dirty) {
            int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
            int[] part = new int[16 * 16 * 16];
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    for (int x = 0; x < 16; x++) {
                        int gx = cx * 16 + x, gy = cy * 16 + y, gz = cz * 16 + z;
                        part[(y * 16 + z) * 16 + x] = gx < r.sx && gy < r.sy && gz < r.sz ? r.cells[(gy * r.sz + gz) * r.sx + gx] : 0;
                    }
            changed.add(new SectionPayload.Section(cx, cy, cz, pack(part)));
        }
        r.dirty.clear();
        SectionPayload payload = new SectionPayload(changed);
        for (ServerPlayer p : to) PacketDistributor.sendToPlayer(p, payload);
    }

    private static void sendEntities(Remote r, List<ServerPlayer> to) {
        BlockPos o = r.origin();
        AABB box = new AABB(o.getX(), o.getY(), o.getZ(), o.getX() + r.sx, o.getY() + r.sy, o.getZ() + r.sz);
        Vec3 c = Vec3.atCenterOf(r.there.centre());
        List<EntitiesPayload.Ghost> ghosts = new ArrayList<>();
        for (Entity e : r.there.level().getEntities((Entity) null, box, Entity::isAlive)) {
            if (ghosts.size() >= MAX_ENTITIES) break;
            float head = e instanceof LivingEntity le ? le.getYHeadRot() : e.getYRot();
            float body = e instanceof LivingEntity le ? le.yBodyRot : e.getYRot();
            boolean player = e instanceof Player;
            ghosts.add(new EntitiesPayload.Ghost(e.getId(), BuiltInRegistries.ENTITY_TYPE.getId(e.getType()),
                    (float) (e.getX() - c.x), (float) (e.getY() - c.y), (float) (e.getZ() - c.z),
                    e.getYRot(), e.getXRot(), head, body, player, player ? e.getUUID() : new UUID(0, 0),
                    player ? e.getName().getString() : ""));
        }
        EntitiesPayload payload = new EntitiesPayload(ghosts);
        for (ServerPlayer p : to) PacketDistributor.sendToPlayer(p, payload);
    }

    // ---- packing cells: a palette and runs, deflated

    static byte[] pack(int[] cells) {
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Integer> index = new HashMap<>();
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int run = 0, runIdx = -1;
        for (int v : cells) {
            int idx = index.computeIfAbsent(v, k -> {
                palette.add(k);
                return palette.size() - 1;
            });
            if (idx == runIdx) {
                run++;
            } else {
                if (run > 0) {
                    writeVar(raw, runIdx);
                    writeVar(raw, run);
                }
                runIdx = idx;
                run = 1;
            }
        }
        if (run > 0) {
            writeVar(raw, runIdx);
            writeVar(raw, run);
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        writeVar(body, palette.size());
        for (int v : palette) writeVar(body, v);
        body.writeBytes(raw.toByteArray());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); DeflaterOutputStream def = new DeflaterOutputStream(out)) {
            def.write(body.toByteArray());
            def.finish();
            return out.toByteArray();
        } catch (java.io.IOException e) {
            return new byte[0];
        }
    }

    public static int[] unpack(byte[] data, int size) {
        int[] out = new int[size];
        try (InflaterInputStream in = new InflaterInputStream(new java.io.ByteArrayInputStream(data))) {
            byte[] raw = in.readAllBytes();
            int[] pos = {0};
            int n = readVar(raw, pos);
            int[] palette = new int[n];
            for (int i = 0; i < n; i++) palette[i] = readVar(raw, pos);
            int at = 0;
            while (pos[0] < raw.length && at < size) {
                int idx = readVar(raw, pos), count = readVar(raw, pos);
                for (int i = 0; i < count && at < size; i++) out[at++] = palette[idx];
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("Ring view could not be read: {}", e.toString());
        }
        return out;
    }

    private static void writeVar(ByteArrayOutputStream out, int v) {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    private static int readVar(byte[] raw, int[] pos) {
        int v = 0, shift = 0;
        while (true) {
            byte b = raw[pos[0]++];
            v |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return v;
            shift += 7;
        }
    }

    // ---- messages

    /** The whole box: where to draw it (the centre of the ring the player is near), which way it faces, its half sizes, the cells. */
    public record FullPayload(boolean on, BlockPos centre, Direction facing, int hx, int hy, int hz, byte[] data, String biome) implements CustomPacketPayload {
        public static final FullPayload NONE = new FullPayload(false, BlockPos.ZERO, Direction.NORTH, 0, 0, 0, new byte[0], "minecraft:plains");
        public static final Type<FullPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ring_view_full"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FullPayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeBoolean(p.on);
                    buf.writeBlockPos(p.centre);
                    buf.writeEnum(p.facing);
                    buf.writeVarInt(p.hx);
                    buf.writeVarInt(p.hy);
                    buf.writeVarInt(p.hz);
                    buf.writeByteArray(p.data);
                    buf.writeUtf(p.biome);
                },
                buf -> new FullPayload(buf.readBoolean(), buf.readBlockPos(), buf.readEnum(Direction.class), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readByteArray(1 << 20), buf.readUtf()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** The 16-block sections of the box that changed. */
    public record SectionPayload(List<Section> sections) implements CustomPacketPayload {
        public record Section(int cx, int cy, int cz, byte[] data) {}

        public static final Type<SectionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ring_view_sections"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SectionPayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.sections.size());
                    for (Section s : p.sections) {
                        buf.writeVarInt(s.cx);
                        buf.writeVarInt(s.cy);
                        buf.writeVarInt(s.cz);
                        buf.writeByteArray(s.data);
                    }
                },
                buf -> {
                    int n = buf.readVarInt();
                    List<Section> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) list.add(new Section(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readByteArray(1 << 18)));
                    return new SectionPayload(list);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** The creatures and players over there, relative to the other ring's centre. */
    public record EntitiesPayload(List<Ghost> ghosts) implements CustomPacketPayload {
        public record Ghost(int id, int type, float x, float y, float z, float yRot, float xRot, float headRot, float bodyRot,
                            boolean player, UUID uuid, String name) {}

        public static final Type<EntitiesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ring_view_entities"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntitiesPayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.ghosts.size());
                    for (Ghost g : p.ghosts) {
                        buf.writeVarInt(g.id);
                        buf.writeVarInt(g.type);
                        buf.writeFloat(g.x);
                        buf.writeFloat(g.y);
                        buf.writeFloat(g.z);
                        buf.writeFloat(g.yRot);
                        buf.writeFloat(g.xRot);
                        buf.writeFloat(g.headRot);
                        buf.writeFloat(g.bodyRot);
                        buf.writeBoolean(g.player);
                        if (g.player) {
                            buf.writeUUID(g.uuid);
                            buf.writeUtf(g.name);
                        }
                    }
                },
                buf -> {
                    int n = buf.readVarInt();
                    List<Ghost> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        int id = buf.readVarInt(), type = buf.readVarInt();
                        float x = buf.readFloat(), y = buf.readFloat(), z = buf.readFloat();
                        float yr = buf.readFloat(), xr = buf.readFloat(), hr = buf.readFloat(), br = buf.readFloat();
                        boolean pl = buf.readBoolean();
                        UUID u = pl ? buf.readUUID() : new UUID(0, 0);
                        String name = pl ? buf.readUtf() : "";
                        list.add(new Ghost(id, type, x, y, z, yr, xr, hr, br, pl, u, name));
                    }
                    return new EntitiesPayload(list);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Sent just before a player crosses, so their client skips the loading screen. */
    public record CrossPayload() implements CustomPacketPayload {
        public static final Type<CrossPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ring_cross"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CrossPayload> STREAM_CODEC = StreamCodec.unit(new CrossPayload());

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
            var r = event.registrar("1").optional();
            r.playToClient(FullPayload.TYPE, FullPayload.STREAM_CODEC, (p, c) -> c.enqueueWork(() -> com.soybe.awakeningsystem.world.client.AethergardWindow.receive(p)));
            r.playToClient(SectionPayload.TYPE, SectionPayload.STREAM_CODEC, (p, c) -> c.enqueueWork(() -> com.soybe.awakeningsystem.world.client.AethergardWindow.receive(p)));
            r.playToClient(EntitiesPayload.TYPE, EntitiesPayload.STREAM_CODEC, (p, c) -> c.enqueueWork(() -> com.soybe.awakeningsystem.world.client.AethergardWindow.receive(p)));
            r.playToClient(CrossPayload.TYPE, CrossPayload.STREAM_CODEC, (p, c) -> c.enqueueWork(com.soybe.awakeningsystem.world.client.AethergardWindow::crossing));
        }
    }
}
