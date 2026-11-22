package com.soybe.awakeningsystem.world.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.world.AethergardGate;
import com.soybe.awakeningsystem.world.GiantPortalRing;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the other world inside the giant ring. The server sends a snapshot of the blocks round the other ring
 * ({@link AethergardGate.ViewPayload}); here they become two block meshes (the half beyond the opening seen from
 * the front, and the half seen from the back), placed so the other ring's centre sits on this ring's centre.
 * Each frame the ring's hole is marked in the stencil buffer where it is visible, filled with the other sky at
 * the far depth, and the mesh of the far side is drawn only there: the other world, with real depth, seen
 * through the ring. A still picture (no creatures, refreshed every few minutes); our own way of doing it.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class AethergardWindow {
    private AethergardWindow() {}

    private static AethergardGate.ViewPayload view;
    /** Meshes by side: [0] the blocks behind the opening seen from its front, [1] seen from its back. */
    private static final Map<RenderType, VertexBuffer>[] MESHES = new Map[]{new HashMap<>(), new HashMap<>()};
    private static boolean pending;

    public static void receive(AethergardGate.ViewPayload p) {
        clear();
        if (!p.on()) return;
        view = p;
        pending = true;
    }

    private static void clear() {
        for (Map<RenderType, VertexBuffer> m : MESHES) {
            m.values().forEach(VertexBuffer::close);
            m.clear();
        }
        view = null;
        pending = false;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    // ---- building the meshes

    /** The snapshot as a small world for the block renderer: full daylight, the other side's biome colours. */
    private static final class Snapshot implements BlockAndTintGetter {
        final int sx, sy, sz;
        final int[] ids;
        final Biome biome;

        Snapshot(AethergardGate.ViewPayload p, Biome biome) {
            this.sx = p.hx() * 2 + 1;
            this.sy = p.hy() * 2 + 1;
            this.sz = p.hz() * 2 + 1;
            this.ids = AethergardGate.unpack(p);
            this.biome = biome;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if (x < 0 || y < 0 || z < 0 || x >= sx || y >= sy || z >= sz) return Blocks.AIR.defaultBlockState();
            return Block.stateById(ids[(y * sz + z) * sx + x]);
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
            return layer == LightLayer.SKY ? 15 : 0;
        }

        @Override
        public int getRawBrightness(BlockPos pos, int darkening) {
            return 15;
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

    private static void build() {
        pending = false;
        Minecraft mc = Minecraft.getInstance();
        if (view == null || mc.level == null) return;
        Biome biome = mc.level.registryAccess().registryOrThrow(Registries.BIOME).get(ResourceLocation.parse(view.biome()));
        if (biome == null) biome = mc.level.getBiome(mc.player.blockPosition()).value();
        Snapshot snap = new Snapshot(view, biome);
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        RandomSource rand = RandomSource.create();
        Direction f = view.facing();
        int mid = f.getAxis() == Direction.Axis.X ? view.hx() : view.hz();
        List<RenderType> layers = RenderType.chunkBufferLayers();
        for (int side = 0; side < 2; side++) {
            Map<RenderType, ByteBufferBuilder> memory = new HashMap<>();
            Map<RenderType, BufferBuilder> builders = new HashMap<>();
            PoseStack pose = new PoseStack();
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int y = 0; y < snap.sy; y++)
                for (int z = 0; z < snap.sz; z++)
                    for (int x = 0; x < snap.sx; x++) {
                        // how far through the opening this block is: side 0 draws what is behind the front (-w)
                        int along = (f.getAxis() == Direction.Axis.X ? x : z) - mid;
                        int w = along * (f.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1);
                        if (side == 0 ? w > 0 : w < 0) continue;
                        m.set(x, y, z);
                        BlockState s = snap.getBlockState(m);
                        if (s.isAir() || s.getRenderShape() != RenderShape.MODEL) continue;
                        BakedModel model = dispatcher.getBlockModel(s);
                        rand.setSeed(s.getSeed(m));
                        for (RenderType rt : model.getRenderTypes(s, rand, ModelData.EMPTY)) {
                            if (!layers.contains(rt)) continue;
                            BufferBuilder b = builders.computeIfAbsent(rt, k -> {
                                ByteBufferBuilder mem = new ByteBufferBuilder(1 << 20);
                                memory.put(k, mem);
                                return new BufferBuilder(mem, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                            });
                            pose.pushPose();
                            pose.translate(x, y, z);
                            dispatcher.renderBatched(s, m, snap, pose, b, true, rand, ModelData.EMPTY, rt);
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
                    MESHES[side].put(e.getKey(), vb);
                }
                memory.get(e.getKey()).close();
            }
        }
    }

    // ---- drawing

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (view == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (pending) {
            try {
                build();
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.warn("Ring view could not be built: {}", t.toString());
                clear();
                return;
            }
        }
        var target = mc.getMainRenderTarget();
        if (!target.isStencilEnabled()) target.enableStencil();

        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Direction f = view.facing();
        Vec3 centre = Vec3.atCenterOf(view.centre());
        double camW = (cam.x - centre.x) * f.getStepX() + (cam.z - centre.z) * f.getStepZ();
        Map<RenderType, VertexBuffer> meshes = MESHES[camW >= 0 ? 0 : 1];

        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        float hole = GiantPortalRing.R - GiantPortalRing.T + 0.5F;

        Matrix4fStack mvs = RenderSystem.getModelViewStack();
        mvs.pushMatrix();
        mvs.mul(modelView);
        RenderSystem.applyModelViewMatrix();
        try {
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
            disc(centre.subtract(cam), f, hole, 0xFF000000);

            // 2. fill it with the other sky, as far away as anything can be
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glStencilMask(0x00);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            GL11.glDepthRange(1.0, 1.0);
            disc(centre.subtract(cam), f, hole, 0xFF9CC8F2);
            GL11.glDepthRange(0.0, 1.0);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        } finally {
            mvs.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        // 3. the other world, only inside the hole
        BlockPos origin = view.centre().offset(-view.hx(), -view.hy(), -view.hz());
        Matrix4f mv = new Matrix4f(modelView).translate((float) (origin.getX() - cam.x), (float) (origin.getY() - cam.y), (float) (origin.getZ() - cam.z));
        for (RenderType rt : RenderType.chunkBufferLayers()) {
            VertexBuffer vb = meshes.get(rt);
            if (vb == null) continue;
            rt.setupRenderState();
            var shader = RenderSystem.getShader();
            if (shader != null) {
                vb.bind();
                vb.drawWithShader(mv, projection, shader);
                VertexBuffer.unbind();
            }
            rt.clearRenderState();
        }
        GL11.glStencilMask(0xFF);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.enableCull();
    }

    /** A flat disc in the ring's plane, round its centre (given relative to the camera). */
    private static void disc(Vec3 c, Direction f, float radius, int argb) {
        Direction right = f.getClockWise();
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
