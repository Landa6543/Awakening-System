package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * The way between Teyvat's giant ring and Aethergard (floating islands above the clouds). Once the ring is
 * repaired, it works both ways:
 * <ul>
 * <li>a twin ring stands on an island in Aethergard, facing the same way, so the two openings line up;</li>
 * <li>a player near either ring is sent a snapshot of the blocks on the other side, which their client draws
 * inside the opening (see {@code client.AethergardWindow}): the other world, seen through the ring;</li>
 * <li>walking through the opening takes the player to the other ring, at the same place in it.</li>
 * </ul>
 * Our own take on a "frameless" portal view: a still picture of the other side, refreshed every few minutes,
 * without its creatures (yet).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class AethergardGate {
    private AethergardGate() {}

    public static final ResourceKey<Level> AETHERGARD = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "aethergard"));

    /** Where the twin ring's foot stands in Aethergard. */
    private static final BlockPos LANDING = new BlockPos(0, 110, 0);
    /** The snapshot: half sizes across the opening, up and down, and through it (each side). */
    private static final int HALF_U = 36, HALF_V = 34, HALF_W = 56;
    /** A player this close to a ring gets the view through it. */
    private static final double VIEW_RANGE = 200;
    private static final long REFRESH_TICKS = 20 * 60 * 3;

    // ---- the twin ring in Aethergard

    public static final class Data extends SavedData {
        boolean landingBuilt;
        Direction facing = Direction.NORTH;

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            d.landingBuilt = tag.getBoolean("Built");
            d.facing = Direction.from2DDataValue(tag.getInt("Facing"));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("Built", landingBuilt);
            tag.putInt("Facing", facing.get2DDataValue());
            return tag;
        }
    }

    private static Data data(ServerLevel aether) {
        return aether.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), "awakening_aethergard_gate");
    }

    /** Builds the twin ring on its island, facing like Teyvat's, the first time it is needed. */
    private static void ensureLanding(ServerLevel aether, Direction facing) {
        Data d = data(aether);
        if (d.landingBuilt) return;
        // an island of grass and stone under the ring's foot
        int r = 26;
        for (int x = -r; x <= r; x++)
            for (int z = -r; z <= r; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist > r) continue;
                int depth = (int) Math.round((r - dist) * 0.55) + 2;
                for (int y = 0; y < depth; y++) {
                    BlockState s = y == 0 ? Blocks.GRASS_BLOCK.defaultBlockState() : y < 3 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState();
                    aether.setBlock(LANDING.offset(x, -1 - y, z), s, 2);
                }
            }
        GiantPortalRing.build(aether, LANDING, facing, true);
        d.landingBuilt = true;
        d.facing = facing;
        d.setDirty();
        AwakeningSystemMod.LOGGER.info("Aethergard: the twin ring stands at {}", LANDING);
    }

    // ---- the two ends

    /** One end of the way: the level, the centre of its ring's opening and the way the ring faces. */
    private record End(ServerLevel level, BlockPos centre, Direction facing) {}

    /** Both ends, if the Teyvat ring is repaired: [Teyvat, Aethergard]. */
    private static End[] ends(MinecraftServer server) {
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        ServerLevel aether = server.getLevel(AETHERGARD);
        if (teyvat == null || aether == null) return null;
        GiantRingSite.Data site = GiantRingSite.dataOf(teyvat);
        if (site == null || !site.repaired || site.base == null) return null;
        ensureLanding(aether, site.facing);
        return new End[]{new End(teyvat, GiantPortalRing.centre(site.base), site.facing),
                new End(aether, GiantPortalRing.centre(LANDING), data(aether).facing)};
    }

    // ---- crossing and the view, checked a few times a second

    private static final Map<UUID, Double> LAST_SIDE = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();
    /** Which end each player was last shown, and when. */
    private static final Map<UUID, String> SHOWN = new HashMap<>();
    private static final Map<UUID, Long> SHOWN_AT = new HashMap<>();
    /** Snapshots made, by the level they show, kept for a few minutes. */
    private static final Map<String, ViewPayload> CACHE = new HashMap<>();
    private static final Map<String, Long> CACHE_AT = new HashMap<>();

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 2 != 0) return;
        End[] ends = null;
        boolean looked = false;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            boolean inTeyvat = p.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD);
            boolean inAether = p.level().dimension().equals(AETHERGARD);
            if (!inTeyvat && !inAether) {
                clearView(p);
                continue;
            }
            if (!looked) {
                ends = ends(server);
                looked = true;
            }
            if (ends == null) {
                clearView(p);
                continue;
            }
            End here = inTeyvat ? ends[0] : ends[1], there = inTeyvat ? ends[1] : ends[0];
            crossing(p, here, there, server.getTickCount());
            if (server.getTickCount() % 20 == 0) view(p, here, there, server);
        }
    }

    /** Through the opening: from one side of the ring to the other, inside its hole, takes you across. */
    private static void crossing(ServerPlayer p, End here, End there, long tick) {
        Direction f = here.facing;
        Vec3 c = Vec3.atCenterOf(here.centre);
        Vec3 rel = p.position().add(0, 0.9, 0).subtract(c);
        Direction right = f.getClockWise();
        double u = rel.x * right.getStepX() + rel.z * right.getStepZ();
        double v = rel.y;
        double w = rel.x * f.getStepX() + rel.z * f.getStepZ();
        Double last = LAST_SIDE.put(p.getUUID(), w);
        double hole = GiantPortalRing.R - GiantPortalRing.T - 1;
        if (last == null || u * u + v * v > hole * hole || Math.abs(w) > 2) return;
        if (Math.signum(last) == Math.signum(w) || last == 0) return;
        if (COOLDOWN.getOrDefault(p.getUUID(), 0L) > tick) return;
        // arrive at the same place in the other ring, a step past its opening, looking the same way
        Vec3 out = Vec3.atCenterOf(there.centre).add(right.getStepX() * u, v - 0.9, right.getStepZ() * u)
                .add(f.getStepX() * Math.signum(w) * 2.0, 0, f.getStepZ() * Math.signum(w) * 2.0);
        COOLDOWN.put(p.getUUID(), tick + 60);
        LAST_SIDE.remove(p.getUUID());
        p.teleportTo(there.level, out.x, out.y, out.z, p.getYRot(), p.getXRot());
        there.level.playSound(null, out.x, out.y, out.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.6F);
    }

    private static void clearView(ServerPlayer p) {
        if (SHOWN.remove(p.getUUID()) != null) {
            SHOWN_AT.remove(p.getUUID());
            PacketDistributor.sendToPlayer(p, ViewPayload.NONE);
        }
    }

    /** Sends a player near a ring the picture of the other side (again every few minutes). */
    private static void view(ServerPlayer p, End here, End there, MinecraftServer server) {
        double dist = p.position().distanceTo(Vec3.atCenterOf(here.centre));
        if (dist > VIEW_RANGE) {
            clearView(p);
            return;
        }
        String key = there.level.dimension().location().toString();
        long now = server.getTickCount();
        if (key.equals(SHOWN.get(p.getUUID())) && now - SHOWN_AT.getOrDefault(p.getUUID(), 0L) < REFRESH_TICKS) return;
        ViewPayload snap = CACHE.get(key);
        if (snap == null || now - CACHE_AT.getOrDefault(key, 0L) > REFRESH_TICKS) {
            snap = snapshot(there);
            CACHE.put(key, snap);
            CACHE_AT.put(key, now);
        }
        // the picture is placed at this ring: its centre is where the other ring's centre is
        PacketDistributor.sendToPlayer(p, snap.at(here.centre));
        SHOWN.put(p.getUUID(), key);
        SHOWN_AT.put(p.getUUID(), now);
    }

    /** The blocks round the other ring's opening, packed small: a palette of block states and runs of them. */
    private static ViewPayload snapshot(End there) {
        Direction f = there.facing;
        boolean alongX = f.getAxis() == Direction.Axis.X;     // the axis the opening looks along
        int hx = alongX ? HALF_W : HALF_U, hz = alongX ? HALF_U : HALF_W, hy = HALF_V;
        BlockPos c = there.centre;
        int sx = hx * 2 + 1, sy = hy * 2 + 1, sz = hz * 2 + 1;
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Integer> index = new HashMap<>();
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int run = 0, runIdx = -1;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int y = 0; y < sy; y++)
            for (int z = 0; z < sz; z++)
                for (int x = 0; x < sx; x++) {
                    m.set(c.getX() - hx + x, c.getY() - hy + y, c.getZ() - hz + z);
                    BlockState s = there.level.getBlockState(m);   // loads (and makes) the chunk if needed
                    int id = Block.getId(s);
                    int idx = index.computeIfAbsent(id, k -> {
                        palette.add(k);
                        return palette.size() - 1;
                    });
                    if (idx == runIdx && run < 65535) {
                        run++;
                    } else {
                        if (run > 0) writeRun(raw, runIdx, run);
                        runIdx = idx;
                        run = 1;
                    }
                }
        if (run > 0) writeRun(raw, runIdx, run);
        byte[] packed;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); DeflaterOutputStream def = new DeflaterOutputStream(out)) {
            def.write(raw.toByteArray());
            def.finish();
            packed = out.toByteArray();
        } catch (java.io.IOException e) {
            packed = new byte[0];
        }
        int[] pal = palette.stream().mapToInt(Integer::intValue).toArray();
        String biome = there.level.dimension().equals(AETHERGARD) ? "minecraft:cherry_grove" : "minecraft:plains";
        AwakeningSystemMod.LOGGER.info("Ring view of {}: {} block kinds, {} bytes", there.level.dimension().location(), pal.length, packed.length);
        return new ViewPayload(true, BlockPos.ZERO, there.facing, hx, hy, hz, pal, packed, biome);
    }

    private static void writeRun(ByteArrayOutputStream out, int idx, int count) {
        writeVar(out, idx);
        writeVar(out, count);
    }

    private static void writeVar(ByteArrayOutputStream out, int v) {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    /** Unpacks a snapshot's runs into block state ids, x fastest, then z, then y. */
    public static int[] unpack(ViewPayload p) {
        int sx = p.hx * 2 + 1, sy = p.hy * 2 + 1, sz = p.hz * 2 + 1;
        int[] out = new int[sx * sy * sz];
        try (InflaterInputStream in = new InflaterInputStream(new java.io.ByteArrayInputStream(p.data))) {
            byte[] raw = in.readAllBytes();
            int pos = 0, at = 0;
            int[] cur = new int[1];
            while (pos < raw.length && at < out.length) {
                cur[0] = pos;
                int idx = readVar(raw, cur);
                int count = readVar(raw, cur);
                pos = cur[0];
                int id = p.palette[idx];
                for (int i = 0; i < count && at < out.length; i++) out[at++] = id;
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("Ring view could not be read: {}", e.toString());
        }
        return out;
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

    // ---- the message

    /**
     * The view through a ring: where to draw it (the centre of the ring the player is near), the way the ring
     * faces, the half sizes of the box of blocks round the other ring's centre, and the blocks themselves.
     */
    public record ViewPayload(boolean on, BlockPos centre, Direction facing, int hx, int hy, int hz, int[] palette, byte[] data,
                              String biome) implements CustomPacketPayload {
        public static final ViewPayload NONE = new ViewPayload(false, BlockPos.ZERO, Direction.NORTH, 0, 0, 0, new int[0], new byte[0], "minecraft:plains");
        public static final Type<ViewPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ring_view"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ViewPayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeBoolean(p.on);
                    buf.writeBlockPos(p.centre);
                    buf.writeEnum(p.facing);
                    buf.writeVarInt(p.hx);
                    buf.writeVarInt(p.hy);
                    buf.writeVarInt(p.hz);
                    buf.writeVarIntArray(p.palette);
                    buf.writeByteArray(p.data);
                    buf.writeUtf(p.biome);
                },
                buf -> new ViewPayload(buf.readBoolean(), buf.readBlockPos(), buf.readEnum(Direction.class), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarIntArray(), buf.readByteArray(1 << 20), buf.readUtf()));

        /** The same picture, to be drawn at another ring. */
        public ViewPayload at(BlockPos where) {
            return new ViewPayload(on, where, facing, hx, hy, hz, palette, data, biome);
        }

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
            event.registrar("1").optional().playToClient(ViewPayload.TYPE, ViewPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                    () -> com.soybe.awakeningsystem.world.client.AethergardWindow.receive(payload)));
        }
    }
}
