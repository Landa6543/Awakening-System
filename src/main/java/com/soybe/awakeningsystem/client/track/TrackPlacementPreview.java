package com.soybe.awakeningsystem.client.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.block.SubwayRailTrackBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.network.PlaceTrackLinePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de previsualización holográfica continua estilo Create para el tendido de vías del metro.
 * Funciona exclusivamente con las vías funcionales (SubwayRailTrackBlock):
 * - Al hacer clic derecho en una vía existente con una vía en la mano, se fija el punto de anclaje.
 * - Se proyecta una línea láser volumétrica holográfica 3D continua hacia el bloque apuntado.
 * - Muestra el número de vías necesarias y marcadores holográficos.
 * - Al hacer clic derecho en el destino, se colocan las vías automáticamente con conexiones y giros fluidos.
 * - Presionar Shift cancela el trazado en cualquier momento.
 */
public final class TrackPlacementPreview {

    public static BlockPos anchorPos = null;

    private TrackPlacementPreview() {}

    public static boolean hasAnchor() {
        return anchorPos != null;
    }

    public static void setAnchor(BlockPos pos) {
        anchorPos = pos.immutable();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.4F);
            mc.player.displayClientMessage(Component.translatable("awakening_system.track_placement.start_set", pos.getX(), pos.getY(), pos.getZ()), false);
        }
    }

    public static void reset() {
        if (anchorPos != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.DISPENSER_FAIL, 0.8F, 1.1F);
                mc.player.displayClientMessage(Component.translatable("awakening_system.track_placement.cancelled"), true);
            }
        }
        anchorPos = null;
    }

    public static boolean isHoldingTrackItem(Player player) {
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return isTrackItem(main) || isTrackItem(off);
    }

    public static boolean isTrackItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.is(SystemBlocks.SUBWAY_RAIL_TRACK.get().asItem());
    }

    public static boolean onRightClick(Player player, InteractionHand hand, BlockPos clickedPos) {
        if (!isHoldingTrackItem(player)) return false;

        if (player.isShiftKeyDown()) {
            if (hasAnchor()) {
                reset();
                return true;
            }
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        if (!hasAnchor()) {
            var state = mc.level.getBlockState(clickedPos);
            if (state.getBlock() instanceof SubwayRailTrackBlock) {
                setAnchor(clickedPos);
                return true;
            }
            return false;
        }

        BlockPos targetPos = clickedPos;
        var targetState = mc.level.getBlockState(targetPos);
        if (!(targetState.getBlock() instanceof SubwayRailTrackBlock)) {
            targetPos = targetPos.above();
        }

        if (targetPos.equals(anchorPos)) {
            reset();
            return true;
        }

        int dist = Math.abs(targetPos.getX() - anchorPos.getX()) + Math.abs(targetPos.getZ() - anchorPos.getZ());
        if (dist > 48) {
            player.displayClientMessage(Component.translatable("awakening_system.track_placement.max_distance"), true);
            player.playSound(SoundEvents.DISPENSER_FAIL, 1.0F, 0.8F);
            return true;
        }

        PacketDistributor.sendToServer(new PlaceTrackLinePayload(anchorPos, targetPos, false));
        anchorPos = null;
        return true;
    }

    public static List<BlockPos> calculatePath(BlockPos start, BlockPos end) {
        List<BlockPos> path = new ArrayList<>();
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        int dy = end.getY() - start.getY();

        if (dx == 0 && dz == 0) return path;

        if (dx == 0) {
            int stepZ = dz > 0 ? 1 : -1;
            int count = Math.abs(dz);
            for (int i = 1; i <= count; i++) {
                int yOff = (count > 0) ? (dy * i) / count : 0;
                path.add(start.offset(0, yOff, i * stepZ));
            }
            return path;
        }

        if (dz == 0) {
            int stepX = dx > 0 ? 1 : -1;
            int count = Math.abs(dx);
            for (int i = 1; i <= count; i++) {
                int yOff = (count > 0) ? (dy * i) / count : 0;
                path.add(start.offset(i * stepX, yOff, 0));
            }
            return path;
        }

        if (Math.abs(dz) >= Math.abs(dx)) {
            int stepZ = dz > 0 ? 1 : -1;
            int stepX = dx > 0 ? 1 : -1;
            int zCount = Math.abs(dz);
            int xCount = Math.abs(dx);

            for (int i = 1; i <= zCount; i++) {
                path.add(start.offset(0, 0, i * stepZ));
            }
            BlockPos corner = start.offset(0, 0, zCount * stepZ);
            for (int j = 1; j <= xCount; j++) {
                path.add(corner.offset(j * stepX, dy, 0));
            }
        } else {
            int stepX = dx > 0 ? 1 : -1;
            int stepZ = dz > 0 ? 1 : -1;
            int xCount = Math.abs(dx);
            int zCount = Math.abs(dz);

            for (int i = 1; i <= xCount; i++) {
                path.add(start.offset(i * stepX, 0, 0));
            }
            BlockPos corner = start.offset(xCount * stepX, 0, 0);
            for (int j = 1; j <= zCount; j++) {
                path.add(corner.offset(0, dy, j * stepZ));
            }
        }

        return path;
    }

    public static void renderPreview(RenderLevelStageEvent event) {
        if (!hasAnchor()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!isHoldingTrackItem(mc.player) || mc.player.isShiftKeyDown()) {
            reset();
            return;
        }

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr)) return;

        BlockPos targetPos = bhr.getBlockPos();
        var targetState = mc.level.getBlockState(targetPos);
        if (!(targetState.getBlock() instanceof SubwayRailTrackBlock)) {
            targetPos = targetPos.above();
        }

        int dist = Math.abs(targetPos.getX() - anchorPos.getX()) + Math.abs(targetPos.getZ() - anchorPos.getZ());
        boolean isValidDist = (dist > 0 && dist <= 48);

        List<BlockPos> path = calculatePath(anchorPos, targetPos);
        if (path.isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float gameTime = mc.level.getGameTime() + partialTick;
        Vec3 camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();

        int r = isValidDist ? 34 : 255;
        int g = isValidDist ? 255 : 50;
        int b = isValidDist ? 170 : 50;
        float pulse = 0.85F + 0.15F * (float) Math.sin(gameTime * 0.25F);
        int alpha = (int) (180 * pulse);

        VertexConsumer beamBuffer = bufferSource.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();

        double prevX = anchorPos.getX() + 0.5D - camPos.x;
        double prevY = anchorPos.getY() + 0.25D - camPos.y;
        double prevZ = anchorPos.getZ() + 0.5D - camPos.z;

        for (BlockPos p : path) {
            double curX = p.getX() + 0.5D - camPos.x;
            double curY = p.getY() + 0.25D - camPos.y;
            double curZ = p.getZ() + 0.5D - camPos.z;

            drawBeamSegment(mat, beamBuffer, prevX, prevY, prevZ, curX, curY, curZ, 0.12F, r, g, b, alpha);
            prevX = curX;
            prevY = curY;
            prevZ = curZ;
        }

        for (BlockPos p : path) {
            double px = p.getX() + 0.5D - camPos.x;
            double py = p.getY() + 0.08D - camPos.y;
            double pz = p.getZ() + 0.5D - camPos.z;

            drawHologramDisc(mat, beamBuffer, px, py, pz, 0.45F, r, g, b, (int) (alpha * 0.45F));
        }

        drawHologramDisc(mat, beamBuffer, anchorPos.getX() + 0.5D - camPos.x, anchorPos.getY() + 0.12D - camPos.y, anchorPos.getZ() + 0.5D - camPos.z, 0.6F, 255, 215, 0, (int) (220 * pulse));
        drawHologramDisc(mat, beamBuffer, targetPos.getX() + 0.5D - camPos.x, targetPos.getY() + 0.12D - camPos.y, targetPos.getZ() + 0.5D - camPos.z, 0.6F, r, g, b, (int) (220 * pulse));

        poseStack.popPose();

        Component status = isValidDist 
                ? Component.translatable("awakening_system.track_placement.status_valid")
                : Component.translatable("awakening_system.track_placement.status_too_long");
        mc.player.displayClientMessage(Component.translatable("awakening_system.track_placement.guidance",
                path.size(), status), true);
    }

    private static void drawBeamSegment(Matrix4f m, VertexConsumer b,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       float width, int red, int green, int blue, int alpha) {
        float half = width * 0.5F;
        b.addVertex(m, (float) (x1 - half), (float) y1, (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (x1 + half), (float) y1, (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (x2 + half), (float) y2, (float) z2).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (x2 - half), (float) y2, (float) z2).setColor(red, green, blue, alpha);

        b.addVertex(m, (float) x1, (float) (y1 - half), (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) x1, (float) (y1 + half), (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) x2, (float) (y2 + half), (float) z2).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) x2, (float) (y2 - half), (float) z2).setColor(red, green, blue, alpha);
    }

    private static void drawHologramDisc(Matrix4f m, VertexConsumer b,
                                         double cx, double cy, double cz,
                                         float radius, int red, int green, int blue, int alpha) {
        float r = radius;
        b.addVertex(m, (float) (cx - r), (float) cy, (float) (cz - r)).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (cx + r), (float) cy, (float) (cz - r)).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (cx + r), (float) cy, (float) (cz + r)).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (cx - r), (float) cy, (float) (cz + r)).setColor(red, green, blue, alpha);
    }
}
