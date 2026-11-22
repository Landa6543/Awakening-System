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
 * Driven from {@link AethergardGate}'s tick; only its message registration below listens to events itself.
 */
public final class RingLiveView {
    private RingLiveView() {}

    /** Half sizes of the box: across the opening, up and down, and through it (each side). */
    static final int HALF_U = 96, HALF_V = 48, HALF_W = 160;
    static final double VIEW_RANGE = 220;
    private static final int MAX_ENTITIES = 80;
    /** The ring's surroundings, out to this distance, are looked at again more often than the far land. */
    private static final int NEAR = 64;
    private static final int NEAR_PER_TICK = 4, FAR_PER_TICK = 3, FIRST_PASS_PER_TICK = 12;
    /** Keeps the far ring's land loaded (and its creatures moving) for a while after each renewal. */
    private static final net.minecraft.server.level.TicketType<Long> VIEW_TICKET =
            net.minecraft.server.level.TicketType.create("awakening_ring_view", Long::compare, 300);

    /** One world being looked into: the box round its ring and what is in it. */
    private static final class Remote {
        final AethergardGate.End there;
        final int hx, hy, hz, sx, sy, sz, nx, ny, nz;
        final int[] cells;
        /** Sections never read yet (nearest first), then the near and the far ones read over and over. */
        final ArrayDeque<Integer> unread = new ArrayDeque<>();
        final int[] near, far;
        int nearCursor, farCursor;
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
            nx = (sx + 15) / 16;
            ny = (sy + 15) / 16;
            nz = (sz + 15) / 16;
            cells = new int[sx * sy * sz];
            List<Integer> all = new ArrayList<>();
            for (int s = 0; s < nx * ny * nz; s++) all.add(s);
            all.sort(Comparator.comparingInt(this::distanceSq));
            unread.addAll(all);
            near = all.stream().filter(s -> distanceSq(s) <= NEAR * NEAR).mapToInt(Integer::intValue).toArray();
            far = all.stream().filter(s -> distanceSq(s) > NEAR * NEAR).mapToInt(Integer::intValue).toArray();
        }

        int distanceSq(int s) {
            int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
            int dx = cx * 16 + 8 - hx, dy = cy * 16 + 8 - hy, dz = cz * 16 + 8 - hz;
            return dx * dx + dy * dy + dz * dz;
        }

        BlockPos origin() {
            return there.centre().offset(-hx, -hy, -hz);
        }

        void keepLoaded() {
            there.level().getChunkSource().addRegionTicket(VIEW_TICKET, new net.minecraft.world.level.ChunkPos(there.centre()),
                    Math.max(hx, hz) / 16 + 1, there.centre().asLong());
        }
    }

    private static final Map<String, Remote> REMOTES = new HashMap<>();
    /** Which world each player is looking into, and at which ring here. */
    private static final Map<UUID, String> VIEWING = new HashMap<>();
    private static final Map<UUID, BlockPos> VIEW_AT = new HashMap<>();
    /** For a player who started looking after the land was read: the sections still to send them. */
    private static final Map<UUID, ArrayDeque<Integer>> CATCH_UP = new HashMap<>();
    private static final int CATCH_UP_PER_TICK = 24;

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
        Remote r = REMOTES.computeIfAbsent(key, k -> {
            Remote made = new Remote(there);
            made.keepLoaded();
            return made;
        });
        // an empty box first; then every section read so far, a few at a time, and the rest as the land loads
        String biome = there.level().dimension().equals(AethergardGate.AETHERGARD) ? "awakening_system:aethelgard_oakhaven_plains" : "minecraft:plains";
        PacketDistributor.sendToPlayer(p, new FullPayload(true, here.centre(), there.facing(), r.hx, r.hy, r.hz, new byte[0], biome));
        List<Integer> order = new ArrayList<>();
        for (int s = 0; s < r.nx * r.ny * r.nz; s++) if (!r.unread.contains(s)) order.add(s);
        order.sort(Comparator.comparingInt(r::distanceSq));
        CATCH_UP.put(p.getUUID(), new ArrayDeque<>(order));
        VIEWING.put(p.getUUID(), key);
        VIEW_AT.put(p.getUUID(), here.centre());
        AwakeningSystemMod.LOGGER.debug("Ring view: {} looks into {}", p.getName().getString(), key);
    }

    static void stop(ServerPlayer p) {
        CATCH_UP.remove(p.getUUID());
        if (VIEWING.remove(p.getUUID()) != null) {
            VIEW_AT.remove(p.getUUID());
            PacketDistributor.sendToPlayer(p, FullPayload.NONE);
        }
    }

    /** Every tick: a few sections read, the changed ones sent once a second, the creatures ten times a second. */
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
                CATCH_UP.remove(e.getKey());
                continue;
            }
            viewers.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(p);
        }
        REMOTES.keySet().retainAll(viewers.keySet());
        for (var e : viewers.entrySet()) {
            Remote r = REMOTES.get(e.getKey());
            if (r == null) continue;
            // the other side stays awake while someone looks at it, so its creatures move and its water flows
            if (t % 100 == 0) r.keepLoaded();
            scan(r);
            if (t % 20 == 0) sendChanges(r, e.getValue());
            if (t % 2 == 0) sendEntities(r, e.getValue());
            for (ServerPlayer p : e.getValue()) catchUp(r, p);
        }
    }

    private static void catchUp(Remote r, ServerPlayer p) {
        ArrayDeque<Integer> queue = CATCH_UP.get(p.getUUID());
        if (queue == null) return;
        List<SectionPayload.Section> batch = new ArrayList<>();
        while (batch.size() < CATCH_UP_PER_TICK && !queue.isEmpty()) {
            SectionPayload.Section s = section(r, queue.poll());
            if (s != null) batch.add(s);
        }
        if (queue.isEmpty()) CATCH_UP.remove(p.getUUID());
        if (!batch.isEmpty()) PacketDistributor.sendToPlayer(p, new SectionPayload(batch));
    }

    /** One section's cells, packed; null if it is all empty. */
    private static SectionPayload.Section section(Remote r, int s) {
        int cx = s % r.nx, cz = (s / r.nx) % r.nz, cy = s / (r.nx * r.nz);
        int[] part = new int[16 * 16 * 16];
        boolean any = false;
        for (int y = 0; y < 16; y++)
            for (int z = 0; z < 16; z++)
                for (int x = 0; x < 16; x++) {
                    int gx = cx * 16 + x, gy = cy * 16 + y, gz = cz * 16 + z;
                    int v = gx < r.sx && gy < r.sy && gz < r.sz ? r.cells[(gy * r.sz + gz) * r.sx + gx] : 0;
                    part[(y * 16 + z) * 16 + x] = v;
                    any |= v != 0;
                }
        return any ? new SectionPayload.Section(cx, cy, cz, pack(part)) : null;
    }

    /** Reads a few more sections of the box from the world; those that changed wait to be sent. */
    private static void scan(Remote r) {
        if (!r.unread.isEmpty()) {
            // the first time round, nearest first; a section whose land is not loaded yet waits its turn again
            int tries = r.unread.size();
            for (int done = 0; done < FIRST_PASS_PER_TICK && tries-- > 0 && !r.unread.isEmpty(); ) {
                int s = r.unread.poll();
                if (read(r, s)) done++;
                else r.unread.add(s);
            }
            return;
        }
        for (int k = 0; k < NEAR_PER_TICK && r.near.length > 0; k++) read(r, r.near[r.nearCursor++ % r.near.length]);
        for (int k = 0; k < FAR_PER_TICK && r.far.length > 0; k++) read(r, r.far[r.farCursor++ % r.far.length]);
    }

    /** Reads one section from the world; false if its land is not loaded yet. */
    private static boolean read(Remote r, int s) {
        int cx = s % r.nx, cz = (s / r.nx) % r.nz, cy = s / (r.nx * r.nz);
        BlockPos o = r.origin();
        ServerLevel level = r.there.level();
        int x0 = cx * 16, x1 = Math.min(r.sx, x0 + 16), z0 = cz * 16, z1 = Math.min(r.sz, z0 + 16);
        int y0 = cy * 16, y1 = Math.min(r.sy, y0 + 16);
        // all its columns' chunks must be there (no chunk is made or loaded from here)
        for (int x = x0; x < x1; x += 15)
            for (int z = z0; z < z1; z += 15)
                if (!level.hasChunk((o.getX() + Math.min(x, x1 - 1)) >> 4, (o.getZ() + Math.min(z, z1 - 1)) >> 4)) return false;
        if (!level.hasChunk((o.getX() + x1 - 1) >> 4, (o.getZ() + z1 - 1) >> 4)) return false;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        boolean diff = false;
        for (int y = y0; y < y1; y++)
            for (int z = z0; z < z1; z++)
                for (int x = x0; x < x1; x++) {
                    m.set(o.getX() + x, o.getY() + y, o.getZ() + z);
                    int light = (level.getBrightness(LightLayer.SKY, m) << 4) | level.getBrightness(LightLayer.BLOCK, m);
                    int v = (Block.getId(level.getBlockState(m)) << 8) | light;
                    int i = (y * r.sz + z) * r.sx + x;
                    if (r.cells[i] != v) {
                        r.cells[i] = v;
                        diff = true;
                    }
                }
        if (diff) r.dirty.add(s);
        return true;
    }

    private static void sendChanges(Remote r, List<ServerPlayer> to) {
        if (r.dirty.isEmpty()) return;
        int nx = (r.sx + 15) / 16, nz = (r.sz + 15) / 16;
        List<SectionPayload.Section> changed = new ArrayList<>();
        for (int s : r.dirty) {
            SectionPayload.Section sec = section(r, s);
            if (sec != null) changed.add(sec);
            if (changed.size() >= 64) {
                SectionPayload payload = new SectionPayload(changed);
                for (ServerPlayer p : to) PacketDistributor.sendToPlayer(p, payload);
                changed = new ArrayList<>();
            }
        }
        r.dirty.clear();
        if (changed.isEmpty()) return;
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
        if (data.length == 0) return out;
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
