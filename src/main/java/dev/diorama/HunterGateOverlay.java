package dev.diorama;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry;
import dev.diorama.render.MapViewport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderizador de Gates y Grietas Dimensionales del Awakening System sobre el mapa 3D de Diorama.
 * Proyecta el vórtice dimensional animado idéntico al portal del juego con su respectivo
 * halo de energía según el rango (E, D, C, B, A, S, Puerta Roja, Murim, Isekai).
 */
public final class HunterGateOverlay {

    public record GatePin(RiftEntry rift, float x, float y, int size, int color, boolean clamped) {}

    private HunterGateOverlay() {}

    public static List<GatePin> layout(MapViewport.View view, boolean fullscreen) {
        List<GatePin> pins = new ArrayList<>();
        if (AwakeningSystemClient.ACTIVE_RIFTS.isEmpty()) {
            return pins;
        }

        float cx = view.x() + view.width() / 2.0F;
        float cy = view.y() + view.height() / 2.0F;
        float halfW = Math.max(1, view.width() / 2.0F - 10.0F);
        float halfH = Math.max(1, view.height() / 2.0F - 10.0F);

        // Escalado dinámico y fluido que responde al nivel de zoom y tamaño de pantalla
        int baseSize;
        if (fullscreen) {
            float zoomFactor = (float) Math.pow(Math.max(0.2D, Math.min(4.0D, view.zoom())), 0.38D);
            baseSize = Math.max(13, Math.min(30, Math.round(18.0F * zoomFactor)));
        } else {
            float minDim = Math.min(view.width(), view.height());
            baseSize = Math.max(10, Math.min(18, Math.round(minDim * 0.12F)));
        }

        for (RiftEntry rift : AwakeningSystemClient.ACTIVE_RIFTS) {
            if (rift.pos() == null) continue;

            float x = view.screenX(rift.pos().getX() + 0.5D);
            float y = view.screenY(Projection.v(rift.pos().getY(), rift.pos().getZ() + 0.5D));
            int color = AwakeningSystemClient.getGateColor(rift);

            boolean inside = x >= view.x() + 4 && x <= view.x() + view.width() - 4
                    && y >= view.y() + 4 && y <= view.y() + view.height() - 4;

            // En el minimapa sólo se muestra si el jugador está en el rango visible (sin anclarlo a la esquina/borde desde lejos)
            if (inside) {
                pins.add(new GatePin(rift, x, y, baseSize, color, false));
            }
        }
        return pins;
    }

    public static void draw(GuiGraphics graphics, MapViewport.View view, List<GatePin> pins, boolean fullscreen) {
        if (pins.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        float gameTime = mc.level != null ? mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true) : 0;

        graphics.enableScissor(view.x(), view.y(), view.x() + view.width(), view.y() + view.height());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 100 fotogramas (10x10) de animación fluida del vórtice dimensional
        int frameIndex = Math.floorMod((int) (gameTime * 3.33F), 100);
        int col = frameIndex % 10;
        int row = frameIndex / 10;
        float uOffset = col * 328.0F;
        float vOffset = row * 328.0F;

        for (GatePin pin : pins) {
            int ix = Math.round(pin.x());
            int iy = Math.round(pin.y());
            int themeColor = pin.color();
            ResourceLocation texture = AwakeningSystemClient.getGateTexture(pin.rift());

            int size = pin.size();
            int half = size / 2;

            // Proyección limpia de la textura del vórtice dimensional animado circular (sin cajas ni cruces rígidas)
            graphics.blit(texture, ix - half, iy - half, size, size, uOffset, vOffset, 328, 328, 3280, 3280);

            // Si está clampeado en el minimapa, renderizar una micro-aguja indicadora en el color del portal
            if (pin.clamped()) {
                drawClampedIndicator(graphics, view, ix, iy, themeColor);
            }
        }

        RenderSystem.disableBlend();
        graphics.disableScissor();
    }

    private static void drawClampedIndicator(GuiGraphics graphics, MapViewport.View view, int ix, int iy, int color) {
        float cx = view.x() + view.width() / 2.0F;
        float cy = view.y() + view.height() / 2.0F;
        double angle = Math.atan2(iy - cy, ix - cx);
        int dotX = ix + (int) Math.round(Math.cos(angle) * 4.0D);
        int dotY = iy + (int) Math.round(Math.sin(angle) * 4.0D);

        graphics.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, color);
    }

    public static @Nullable GatePin under(List<GatePin> pins, double mouseX, double mouseY) {
        for (GatePin pin : pins) {
            float hitRadius = Math.max(9.0F, pin.size() / 2.0F + 3.0F);
            if (Math.hypot(pin.x() - mouseX, pin.y() - mouseY) <= hitRadius) {
                return pin;
            }
        }
        return null;
    }

    public static List<Component> describe(RiftEntry rift) {
        List<Component> lines = new ArrayList<>();
        Component title;
        if (rift.isRedGate()) {
            title = Component.translatable("diorama.rift.red_gate");
        } else if (rift.type() == 1) {
            title = Component.translatable("diorama.rift.return_portal");
        } else if (rift.type() == 98 || "MURIM".equalsIgnoreCase(rift.rank())) {
            title = Component.translatable("diorama.rift.dimensional_murim");
        } else if (rift.type() == 99 || "ISEKAI".equalsIgnoreCase(rift.rank())) {
            title = Component.translatable("diorama.rift.dimensional_isekai");
        } else {
            title = Component.translatable("diorama.rift.gate_rank", rift.rank());
        }
        lines.add(title);

        int remSecs = AwakeningSystemClient.getRiftRemainingSeconds(rift);
        if (remSecs > 0) {
            lines.add(Component.translatable("diorama.rift.time_remaining", AwakeningSystemClient.formatRiftRemainingTime(remSecs)));
        }
        if (rift.pos() != null) {
            lines.add(Component.translatable("diorama.rift.coordinates", rift.pos().getX(), rift.pos().getY(), rift.pos().getZ()));
        }
        return lines;
    }

    public static List<net.minecraft.util.FormattedCharSequence> describeLines(RiftEntry rift) {
        return describe(rift).stream().map(Component::getVisualOrderText).toList();
    }
}
