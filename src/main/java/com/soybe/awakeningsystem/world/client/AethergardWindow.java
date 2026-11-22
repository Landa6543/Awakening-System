package com.soybe.awakeningsystem.world.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.world.GiantPortalRing;
import com.soybe.awakeningsystem.world.RingLiveView;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Draws the other world inside the giant ring, live. The server sends the box of blocks round the other ring
 * (each with its light), then the 16-block sections that change, and ten times a second the creatures and
 * players there ({@link RingLiveView}). Here each section becomes block and fluid meshes, two per section (the
 * part beyond the opening seen from the front, and from the back), rebuilt a few a frame when they change, and
 * placed so the other ring's centre sits on this ring's centre. The creatures are copies made on this side that
 * follow the real ones smoothly. Each frame the ring's hole is marked in the stencil buffer where it is visible,
 * filled with the sky at the far depth, and the other world (blocks, water, creatures) is drawn only there.
 * Our own way of doing a "frameless" portal.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class AethergardWindow {
    private AethergardWindow() {}

    private static final int SECTIONS_PER_FRAME = 12;
    /** At most this much of a frame (2.5 ms) goes to building the view's sections. */
    private static final long BUILD_BUDGET_NS = 2_500_000L;
    /** How long after a crossing the loading screen is skipped. */
    private static final long CROSS_WINDOW_MS = 6000;

    // ---- what is known of the other side

    private static boolean on;
    private static BlockPos centre = BlockPos.ZERO;
    private static Direction facing = Direction.NORTH;
    private static int hx, hy, hz, sx, sy, sz, nx, ny, nz;
    private static int[] cells = new int[0];
    private static Biome biome;
    private static String biomeId = "minecraft:plains";
    /** Meshes by section, then by side: [0] the blocks behind the opening seen from its front, [1] from its back. */
    private static Map<RenderType, VertexBuffer>[][] meshes = new Map[0][];
    private static final LinkedHashSet<Integer> DIRTY = new LinkedHashSet<>();
    private static final Map<Integer, Ghost> GHOSTS = new HashMap<>();
    private static long crossedAt;

    public static void receive(RingLiveView.FullPayload p) {
        clear();
        if (!p.on()) return;
        // the first ring seen after crossing is the one arrived at: the kept land sits round it
        if (arrival != null && arrivalOrigin == null && Minecraft.getInstance().level != null) {
            arrivalOrigin = p.centre().offset(-arrHx, -arrHy, -arrHz);
            arrivalLevel = Minecraft.getInstance().level;
        }
        on = true;
        centre = p.centre();
        facing = p.facing();
        hx = p.hx();
        hy = p.hy();
        hz = p.hz();
        sx = hx * 2 + 1;
        sy = hy * 2 + 1;
        sz = hz * 2 + 1;
        nx = (sx + 15) / 16;
        ny = (sy + 15) / 16;
        nz = (sz + 15) / 16;
        cells = RingLiveView.unpack(p.data(), sx * sy * sz);
        biomeId = p.biome();
        biome = null;
        meshes = new Map[nx * ny * nz][];
        // the sections nearest the opening first, so the view fills from the middle outwards
        List<Integer> order = new ArrayList<>();
        for (int s = 0; s < meshes.length; s++) order.add(s);
        order.sort(Comparator.comparingInt(AethergardWindow::distanceToMiddle));
        DIRTY.addAll(order);
    }

    private static int distanceToMiddle(int s) {
        int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
        int dx = cx * 16 + 8 - hx, dy = cy * 16 + 8 - hy, dz = cz * 16 + 8 - hz;
        return dx * dx + dy * dy + dz * dz;
    }

    public static void receive(RingLiveView.SectionPayload p) {
        if (!on) return;
        for (RingLiveView.SectionPayload.Section s : p.sections()) {
            if (s.cx() >= nx || s.cy() >= ny || s.cz() >= nz) continue;
            int[] part = RingLiveView.unpack(s.data(), 16 * 16 * 16);
            // what really changed: a section sent again as it was is not rebuilt (that rebuilding, many times a
            // second, was the drop in frames near the ring), and a neighbour only when the change touches it
            boolean changed = false;
            boolean[] edge = new boolean[6];   // -x, +x, -y, +y, -z, +z
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    for (int x = 0; x < 16; x++) {
                        int gx = s.cx() * 16 + x, gy = s.cy() * 16 + y, gz = s.cz() * 16 + z;
                        if (gx >= sx || gy >= sy || gz >= sz) continue;
                        int i = (gy * sz + gz) * sx + gx, v = part[(y * 16 + z) * 16 + x];
                        if (cells[i] == v) continue;
                        cells[i] = v;
                        changed = true;
                        if (x == 0) edge[0] = true;
                        if (x == 15) edge[1] = true;
                        if (y == 0) edge[2] = true;
                        if (y == 15) edge[3] = true;
                        if (z == 0) edge[4] = true;
                        if (z == 15) edge[5] = true;
                    }
            if (!changed) continue;
            int[][] steps = {{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
            DIRTY.add((s.cy() * nz + s.cz()) * nx + s.cx());
            for (int k = 0; k < 6; k++) {
                if (!edge[k]) continue;
                int cx = s.cx() + steps[k][0], cy = s.cy() + steps[k][1], cz = s.cz() + steps[k][2];
                if (cx < 0 || cy < 0 || cz < 0 || cx >= nx || cy >= ny || cz >= nz) continue;
                DIRTY.add((cy * nz + cz) * nx + cx);
            }
        }
    }

    public static void receive(RingLiveView.EntitiesPayload p) {
        if (!on) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Set<Integer> seen = new HashSet<>();
        Vec3 c = Vec3.atCenterOf(centre);
        long now = System.currentTimeMillis();
        for (RingLiveView.EntitiesPayload.Ghost g : p.ghosts()) {
            seen.add(g.id());
            Ghost ghost = GHOSTS.get(g.id());
            if (ghost == null || ghost.type != g.type()) {
                Entity e = make(g, mc);
                if (e == null) continue;
                ghost = new Ghost(e, g.type());
                ghost.from = ghost.to = new Vec3(c.x + g.x(), c.y + g.y(), c.z + g.z());
                ghost.fromRot = ghost.toRot = new float[]{g.yRot(), g.xRot(), g.headRot(), g.bodyRot()};
                GHOSTS.put(g.id(), ghost);
            } else {
                ghost.from = ghost.at(now);
                ghost.fromRot = ghost.rotAt(now);
                ghost.to = new Vec3(c.x + g.x(), c.y + g.y(), c.z + g.z());
                ghost.toRot = new float[]{g.yRot(), g.xRot(), g.headRot(), g.bodyRot()};
            }
            ghost.since = now;
        }
        GHOSTS.keySet().retainAll(seen);
    }

    private static Entity make(RingLiveView.EntitiesPayload.Ghost g, Minecraft mc) {
        try {
            if (g.player()) return new RemotePlayer(mc.level, new GameProfile(g.uuid(), g.name()));
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(g.type());
            return type.create(mc.level);
        } catch (Throwable t) {
            return null;
        }
    }

    /** A copy of a creature over there, following it smoothly between updates. */
    private static final class Ghost {
        final Entity entity;
        final int type;
        Vec3 from, to;
        float[] fromRot, toRot;
        long since;

        Ghost(Entity entity, int type) {
            this.entity = entity;
            this.type = type;
        }

        /** How far along from the last update to the next (they come every tenth of a second). */
        float t(long now) {
            return Math.min(1F, (now - since) / 100F);
        }

        Vec3 at(long now) {
            return from.lerp(to, t(now));
        }

        float[] rotAt(long now) {
            float t = t(now);
            float[] r = new float[4];
            for (int i = 0; i < 4; i++) r[i] = fromRot[i] + net.minecraft.util.Mth.wrapDegrees(toRot[i] - fromRot[i]) * t;
            return r;
        }
    }

    /** Sent by the server just before this player goes through the ring. */
    public static void crossing() {
        crossedAt = System.currentTimeMillis();
        // what the ring was showing is the land the player is about to stand in: keep it, and draw it over
        // there until the game's own sections of that land are ready, so the new world is simply there on
        // arrival instead of loading in round the player (the way the portal mods do it: the other side is
        // already on this computer when you step through)
        dropArrival();
        if (on && meshes.length > 0) {
            arrival = meshes;
            arrNx = nx;
            arrNy = ny;
            arrNz = nz;
            arrHx = hx;
            arrHy = hy;
            arrHz = hz;
            arrivalOrigin = null;
            arrivalLevel = null;
            arrivalSince = crossedAt;
            meshes = new Map[0][];   // so clearing the view does not close them
        }
    }

    // ---- after crossing: the land over there, as the ring showed it, until the game's own is drawn

    private static Map<RenderType, VertexBuffer>[][] arrival;
    private static int arrNx, arrNy, arrNz, arrHx, arrHy, arrHz;
    /** Where the kept land sits in the new world (the ring arrived at, less the box's half), once known. */
    private static BlockPos arrivalOrigin;
    private static net.minecraft.client.multiplayer.ClientLevel arrivalLevel;
    private static long arrivalSince;
    private static final long ARRIVAL_WAIT_MS = 10000, ARRIVAL_MAX_MS = 45000;

    private static void dropArrival() {
        if (arrival != null)
            for (Map<RenderType, VertexBuffer>[] sides : arrival)
                if (sides != null)
                    for (Map<RenderType, VertexBuffer> m : sides) m.values().forEach(VertexBuffer::close);
        arrival = null;
        arrivalOrigin = null;
        arrivalLevel = null;
    }

    /** True when the game has its own sections drawn all over this box (so the kept copy is not needed there). */
    private static boolean covered(Minecraft mc, double x0, double y0, double z0) {
        for (int k = 0; k < 8; k++) {
            BlockPos p = BlockPos.containing(x0 + ((k & 1) == 0 ? 0.5 : 15.5), y0 + ((k & 2) == 0 ? 0.5 : 15.5), z0 + ((k & 4) == 0 ? 0.5 : 15.5));
            if (mc.level.isOutsideBuildHeight(p.getY())) continue;
            if (!mc.levelRenderer.isSectionCompiled(p)) return false;
        }
        return true;
    }

    private static void drawArrival(Minecraft mc, RenderLevelStageEvent event) {
        if (arrival == null) return;
        long now = System.currentTimeMillis();
        if (arrivalOrigin == null) {
            if (now - arrivalSince > ARRIVAL_WAIT_MS) dropArrival();
            return;
        }
        if (mc.level != arrivalLevel || now - arrivalSince > ARRIVAL_MAX_MS) {
            dropArrival();
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        var frustum = event.getFrustum();
        List<Integer> draw = new ArrayList<>();
        boolean anyLeft = false;
        for (int s = 0; s < arrival.length; s++) {
            if (arrival[s] == null || (arrival[s][0].isEmpty() && arrival[s][1].isEmpty())) continue;
            int cx = s % arrNx, cz = (s / arrNx) % arrNz, cy = s / (arrNx * arrNz);
            double x0 = arrivalOrigin.getX() + cx * 16, y0 = arrivalOrigin.getY() + cy * 16, z0 = arrivalOrigin.getZ() + cz * 16;
            if (covered(mc, x0, y0, z0)) continue;
            anyLeft = true;
            if (frustum != null && !frustum.isVisible(new net.minecraft.world.phys.AABB(x0, y0, z0, x0 + 16, y0 + 16, z0 + 16))) continue;
            draw.add(s);
        }
        // the game has drawn all of it: the copy has done its job
        if (!anyLeft) {
            dropArrival();
            return;
        }
        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        for (RenderType rt : RenderType.chunkBufferLayers()) {
            rt.setupRenderState();
            var shader = RenderSystem.getShader();
            if (shader != null) {
                shader.setDefaultUniforms(VertexFormat.Mode.QUADS, modelView, projection, mc.getWindow());
                shader.apply();
                var offset = shader.CHUNK_OFFSET;
                for (int s : draw) {
                    int cx = s % arrNx, cz = (s / arrNx) % arrNz, cy = s / (arrNx * arrNz);
                    if (offset != null) {
                        offset.set((float) (arrivalOrigin.getX() + cx * 16 - cam.x), (float) (arrivalOrigin.getY() + cy * 16 - cam.y),
                                (float) (arrivalOrigin.getZ() + cz * 16 - cam.z));
                        offset.upload();
                    }
                    // both halves: in front of the ring and behind it
                    for (int side = 0; side < 2; side++) {
                        VertexBuffer vb = arrival[s][side].get(rt);
                        if (vb == null) continue;
                        vb.bind();
                        vb.draw();
                    }
                }
                if (offset != null) offset.set(0.0F, 0.0F, 0.0F);
                shader.clear();
                VertexBuffer.unbind();
            }
            rt.clearRenderState();
        }
    }

    /** Right after crossing the ring, the new world comes in round the player with no loading screen. */
    @SubscribeEvent
    public static void onScreen(ScreenEvent.Opening event) {
        // the game opens it more than once while changing worlds: every one of them is skipped
        if (event.getNewScreen() instanceof ReceivingLevelScreen && System.currentTimeMillis() - crossedAt < CROSS_WINDOW_MS)
            event.setCanceled(true);
    }

    private static void clear() {
        for (Map<RenderType, VertexBuffer>[] sides : meshes)
            if (sides != null)
                for (Map<RenderType, VertexBuffer> m : sides) m.values().forEach(VertexBuffer::close);
        meshes = new Map[0][];
        DIRTY.clear();
        GHOSTS.clear();
        cells = new int[0];
        on = false;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
        dropArrival();
    }

    /** The creatures' legs and arms move as they walk. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (GHOSTS.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (Ghost g : GHOSTS.values()) {
            g.entity.tickCount++;
            if (g.entity instanceof LivingEntity le) {
                double moved = Math.sqrt(Math.pow(g.to.x - g.from.x, 2) + Math.pow(g.to.z - g.from.z, 2)) / 2.0;
                le.walkAnimation.update(Math.min((float) moved * 4F, 1F), 0.4F);
            }
        }
    }

    // ---- the other side as a small world for the block renderer

    private static final class Remote implements BlockAndTintGetter {
        @Override
        public BlockState getBlockState(BlockPos pos) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            // beyond the box's sides and floor the land goes on: solid, so the edge shows no cut-away walls
            if (y >= sy) return Blocks.AIR.defaultBlockState();
            if (x < 0 || y < 0 || z < 0 || x >= sx || z >= sz) return Blocks.STONE.defaultBlockState();
            return Block.stateById(cells[(y * sz + z) * sx + x] >>> 8);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public float getShade(Direction direction, boolean shade) {
            return Minecraft.getInstance().level.getShade(direction, shade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return Minecraft.getInstance().level.getLightEngine();
        }

        @Override
        public int getBrightness(LightLayer layer, BlockPos pos) {
            return light(layer, pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        public int getRawBrightness(BlockPos pos, int darkening) {
            return Math.max(light(LightLayer.SKY, pos.getX(), pos.getY(), pos.getZ()) - darkening, light(LightLayer.BLOCK, pos.getX(), pos.getY(), pos.getZ()));
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver resolver) {
            return resolver.getColor(biome, pos.getX(), pos.getZ());
        }

        @Override
        public int getHeight() {
            return sy;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    }

    /** The light over there at a cell of the box (outside it: open sky). */
    private static int light(LightLayer layer, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= sx || y >= sy || z >= sz) return layer == LightLayer.SKY ? 15 : 0;
        int v = cells[(y * sz + z) * sx + x];
        return layer == LightLayer.SKY ? (v >> 4) & 15 : v & 15;
    }

    private static final Remote REMOTE = new Remote();

    /** Rebuilds one section's meshes from what is known of it. */
    private static void build(int s, Minecraft mc) {
        Map<RenderType, VertexBuffer>[] old = meshes[s];
        if (old != null) for (Map<RenderType, VertexBuffer> m : old) m.values().forEach(VertexBuffer::close);
        Map<RenderType, VertexBuffer>[] sides = new Map[]{new HashMap<>(), new HashMap<>()};
        meshes[s] = sides;
        int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        RandomSource rand = RandomSource.create();
        int mid = facing.getAxis() == Direction.Axis.X ? hx : hz;
        int sign = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
        List<RenderType> layers = RenderType.chunkBufferLayers();
        for (int side = 0; side < 2; side++) {
            Map<RenderType, ByteBufferBuilder> memory = new HashMap<>();
            Map<RenderType, BufferBuilder> builders = new HashMap<>();
            PoseStack pose = new PoseStack();
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int ly = 0; ly < 16; ly++)
                for (int lz = 0; lz < 16; lz++)
                    for (int lx = 0; lx < 16; lx++) {
                        int x = cx * 16 + lx, y = cy * 16 + ly, z = cz * 16 + lz;
                        if (x >= sx || y >= sy || z >= sz) continue;
                        // how far through the opening this block is: side 0 draws what is behind the front (-w)
                        int w = ((facing.getAxis() == Direction.Axis.X ? x : z) - mid) * sign;
                        if (side == 0 ? w > 0 : w < 0) continue;
                        m.set(x, y, z);
                        BlockState st = REMOTE.getBlockState(m);
                        if (st.isAir()) continue;
                        FluidState fluid = st.getFluidState();
                        if (!fluid.isEmpty()) {
                            RenderType rt = ItemBlockRenderTypes.getRenderLayer(fluid);
                            // the fluid renderer places its faces by the position within the section, as we do
                            dispatcher.renderLiquid(m, REMOTE, builder(builders, memory, rt), st, fluid);
                        }
                        if (st.getRenderShape() != RenderShape.MODEL) continue;
                        BakedModel model = dispatcher.getBlockModel(st);
                        rand.setSeed(st.getSeed(m));
                        for (RenderType rt : model.getRenderTypes(st, rand, ModelData.EMPTY)) {
                            if (!layers.contains(rt)) continue;
                            pose.pushPose();
                            pose.translate(lx, ly, lz);
                            dispatcher.renderBatched(st, m, REMOTE, pose, builder(builders, memory, rt), true, rand, ModelData.EMPTY, rt);
                            pose.popPose();
                        }
                    }
            for (var e : builders.entrySet()) {
                MeshData mesh = e.getValue().build();
                if (mesh != null) {
                    VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    vb.bind();
                    vb.upload(mesh);
                    VertexBuffer.unbind();
                    sides[side].put(e.getKey(), vb);
                }
            }
            memory.values().forEach(ByteBufferBuilder::close);
        }
    }

    private static BufferBuilder builder(Map<RenderType, BufferBuilder> builders, Map<RenderType, ByteBufferBuilder> memory, RenderType rt) {
        return builders.computeIfAbsent(rt, k -> {
            ByteBufferBuilder mem = new ByteBufferBuilder(1 << 16);
            memory.put(k, mem);
            return new BufferBuilder(mem, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        });
    }

    // ---- drawing

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        try {
            drawArrival(mc, event);
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.warn("Kept ring land could not be drawn: {}", t.toString());
            dropArrival();
        }
        if (!on) return;
        if (biome == null) {
            biome = mc.level.registryAccess().registryOrThrow(Registries.BIOME).get(ResourceLocation.parse(biomeId));
            if (biome == null) biome = mc.level.getBiome(mc.player.blockPosition()).value();
        }
        try {
            // a little of this frame's time, not a fixed count: a frame never waits long for the ring's view
            long until = System.nanoTime() + BUILD_BUDGET_NS;
            for (int i = 0; i < SECTIONS_PER_FRAME && !DIRTY.isEmpty() && (i == 0 || System.nanoTime() < until); i++) {
                Iterator<Integer> it = DIRTY.iterator();
                int s = it.next();
                it.remove();
                build(s, mc);
            }
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.warn("Ring view could not be built: {}", t.toString());
            clear();
            return;
        }
        // nothing to draw while the ring's opening is out of sight
        {
            Vec3 c0 = Vec3.atCenterOf(centre);
            double r0 = GiantPortalRing.R;
            boolean alongX = facing.getAxis() == Direction.Axis.X;
            var openingBox = new net.minecraft.world.phys.AABB(c0.x - (alongX ? 2 : r0), c0.y - r0, c0.z - (alongX ? r0 : 2),
                    c0.x + (alongX ? 2 : r0), c0.y + r0, c0.z + (alongX ? r0 : 2));
            if (event.getFrustum() != null && !event.getFrustum().isVisible(openingBox)) return;
        }
        var target = mc.getMainRenderTarget();
        if (!target.isStencilEnabled()) target.enableStencil();

        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Vec3 mid = Vec3.atCenterOf(centre);
        double camW = (cam.x - mid.x) * facing.getStepX() + (cam.z - mid.z) * facing.getStepZ();
        int side = camW >= 0 ? 0 : 1;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 skyRgb = mc.level.getSkyColor(cam, partial);
        int sky = 0xFF000000 | ((int) (skyRgb.x * 255) << 16) | ((int) (skyRgb.y * 255) << 8) | (int) (skyRgb.z * 255);

        // the model-view stack already holds the camera's turn here; positions are given relative to the camera
        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        float hole = GiantPortalRing.R - GiantPortalRing.T + 0.5F;

        // 1. mark the hole where it can be seen
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableCull();
        disc(mid.subtract(cam), hole, 0xFF000000);

        // 2. fill it with the sky, as far away as anything can be
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        GL11.glDepthRange(1.0, 1.0);
        disc(mid.subtract(cam), hole, sky);
        GL11.glDepthRange(0.0, 1.0);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();

        try {
            // 3. the other world's blocks and water, only inside the hole
            BlockPos origin = centre.offset(-hx, -hy, -hz);
            // only the sections with something in them that the camera can see
            net.minecraft.client.renderer.culling.Frustum frustum = event.getFrustum();
            List<Integer> visible = new ArrayList<>();
            for (int s = 0; s < meshes.length; s++) {
                if (meshes[s] == null || meshes[s][side].isEmpty()) continue;
                int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
                double x0 = origin.getX() + cx * 16, y0 = origin.getY() + cy * 16, z0 = origin.getZ() + cz * 16;
                if (frustum != null && !frustum.isVisible(new net.minecraft.world.phys.AABB(x0, y0, z0, x0 + 16, y0 + 16, z0 + 16))) continue;
                visible.add(s);
            }
            for (RenderType rt : RenderType.chunkBufferLayers()) {
                rt.setupRenderState();
                var shader = RenderSystem.getShader();
                if (shader != null) {
                    // as the game draws its own chunks: the shader set up once, then only each section's offset
                    shader.setDefaultUniforms(VertexFormat.Mode.QUADS, modelView, projection, mc.getWindow());
                    shader.apply();
                    var offset = shader.CHUNK_OFFSET;
                    for (int s : visible) {
                        VertexBuffer vb = meshes[s][side].get(rt);
                        if (vb == null) continue;
                        int cx = s % nx, cz = (s / nx) % nz, cy = s / (nx * nz);
                        if (offset != null) {
                            offset.set((float) (origin.getX() + cx * 16 - cam.x), (float) (origin.getY() + cy * 16 - cam.y),
                                    (float) (origin.getZ() + cz * 16 - cam.z));
                            offset.upload();
                        }
                        vb.bind();
                        vb.draw();
                    }
                    if (offset != null) offset.set(0.0F, 0.0F, 0.0F);
                    shader.clear();
                    VertexBuffer.unbind();
                }
                rt.clearRenderState();
            }

            // 4. its creatures and players
            if (!GHOSTS.isEmpty()) drawGhosts(mc, event, cam, mid, camW, origin);
        } finally {
            GL11.glStencilMask(0xFF);
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        }
    }

    private static void drawGhosts(Minecraft mc, RenderLevelStageEvent event, Vec3 cam, Vec3 mid, double camW, BlockPos origin) {
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack() != null ? event.getPoseStack() : new PoseStack();
        boolean shadows = mc.options.entityShadows().get();
        dispatcher.setRenderShadow(false);
        long now = System.currentTimeMillis();
        try {
            for (Ghost g : GHOSTS.values()) {
                Vec3 at = g.at(now);
                double w = (at.x - mid.x) * facing.getStepX() + (at.z - mid.z) * facing.getStepZ();
                // only what is beyond the opening, as seen from here
                if (camW >= 0 ? w > 0.5 : w < -0.5) continue;
                float[] r = g.rotAt(now);
                Entity e = g.entity;
                e.setPosRaw(at.x, at.y, at.z);
                e.xo = e.xOld = at.x;
                e.yo = e.yOld = at.y;
                e.zo = e.zOld = at.z;
                e.setYRot(r[0]);
                e.yRotO = r[0];
                e.setXRot(r[1]);
                e.xRotO = r[1];
                if (e instanceof LivingEntity le) {
                    le.yHeadRot = le.yHeadRotO = r[2];
                    le.yBodyRot = le.yBodyRotO = r[3];
                }
                int bx = (int) Math.floor(at.x) - origin.getX(), by = (int) Math.floor(at.y + 0.5) - origin.getY(), bz = (int) Math.floor(at.z) - origin.getZ();
                int packed = LightTexture.pack(light(LightLayer.BLOCK, bx, by, bz), light(LightLayer.SKY, bx, by, bz));
                dispatcher.render(e, at.x - cam.x, at.y - cam.y, at.z - cam.z, r[0], 1.0F, pose, buffers, packed);
            }
            buffers.endBatch();
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.debug("Ring view: a creature could not be drawn: {}", t.toString());
            buffers.endBatch();
        } finally {
            dispatcher.setRenderShadow(shadows);
        }
    }

    /** A flat disc in the ring's plane, round its centre (given relative to the camera). */
    private static void disc(Vec3 c, float radius, int argb) {
        Direction right = facing.getClockWise();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        b.addVertex((float) c.x, (float) c.y, (float) c.z).setColor(argb);
        int n = 72;
        for (int i = 0; i <= n; i++) {
            double a = i * Math.PI * 2 / n;
            double u = Math.cos(a) * radius, v = Math.sin(a) * radius;
            b.addVertex((float) (c.x + right.getStepX() * u), (float) (c.y + v), (float) (c.z + right.getStepZ() * u)).setColor(argb);
        }
        BufferUploader.drawWithShader(b.buildOrThrow());
    }
}
