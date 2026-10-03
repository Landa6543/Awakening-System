package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Pantalla interactiva de Recepción del Gremio de Aventureros atendida por Elena.
 * Muestra el carné oficial del aventurero, rango de gremio (F a S),
 * progreso numérico y visual hacia el siguiente ascenso, y opciones de promoción y bendición.
 */
public final class GuildReceptionScreen extends Screen {
    private final OpenGuildReceptionPayload payload;

    public GuildReceptionScreen(OpenGuildReceptionPayload payload) {
        super(Component.translatable("awakening_system.reception.title"));
        this.payload = payload;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 340.0F;
        float scaleY = (float) this.height / 230.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.35F, scale));
    }

    public double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    public double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        int guiW = 320;
        int guiH = 210;
        int left = (this.width - guiW) / 2;
        int top = (this.height - guiH) / 2;

        // 1. Marco exterior de madera de taberna
        g.fill(left - 5, top - 5, left + guiW + 5, top + guiH + 5, 0xFF1C1107);
        g.fill(left - 3, top - 3, left + guiW + 3, top + guiH + 3, 0xFF3D2510);
        g.fill(left, top, left + guiW, top + guiH, 0xFF2A180B);

        // Fondo de pergamino
        g.fill(left + 6, top + 6, left + guiW - 6, top + guiH - 6, 0xFFFAF3E6);
        g.fill(left + 8, top + 8, left + guiW - 8, top + guiH - 8, 0xFFF5EBD7);

        // 2. Cabecera dorada
        g.fill(left + 12, top + 12, left + guiW - 12, top + 34, 0xFF8B2500);
        drawScaledCenteredString(g, Component.translatable("awakening_system.reception.title"), left + guiW / 2, top + 16, guiW - 36, 0xFFFFD700);
        drawScaledCenteredString(g, Component.translatable("awakening_system.reception.subtitle"), left + guiW / 2, top + 25, guiW - 36, 0xFFE0E0E0);

        // 3. Carné de Aventurero
        int cardX = left + 16;
        int cardY = top + 42;
        int cardW = guiW - 32;
        int cardH = 82;

        g.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xFFEEDBB7);
        g.fill(cardX, cardY, cardX + 4, cardY + cardH, 0xFFC29B58);
        g.fill(cardX + cardW - 4, cardY, cardX + cardW, cardY + cardH, 0xFFC29B58);

        // Sello de Rango
        int sealColor = switch (payload.guildRank()) {
            case "F" -> 0xFF888888;
            case "E" -> 0xFF55AA55;
            case "D" -> 0xFF33BB77;
            case "C" -> 0xFF2299DD;
            case "B" -> 0xFFDDAA11;
            case "A" -> 0xFFEE5522;
            default  -> 0xFFAA22CC;
        };
        g.fill(cardX + 10, cardY + 10, cardX + 42, cardY + 42, sealColor);
        g.drawCenteredString(this.font, "§l" + payload.guildRank(), cardX + 26, cardY + 22, 0xFFFFFFFF);

        // Datos del Aventurero
        g.drawString(this.font, Component.translatable("awakening_system.reception.record"), cardX + 48, cardY + 10, 0xFF880000, false);
        g.drawString(this.font, Component.translatable("awakening_system.reception.current_rank", payload.guildRank()), cardX + 48, cardY + 22, 0xFF000000, false);
        g.drawString(this.font, Component.translatable("awakening_system.reception.completed", payload.completedBounties()), cardX + 48, cardY + 34, 0xFF000000, false);

        // Barra de progreso hacia el siguiente rango
        if (!payload.guildRank().equals("S")) {
            int need = payload.nextRankNeeded();
            int comp = payload.completedBounties();
            int remain = Math.max(0, need - comp);
            drawScaledString(g, Component.translatable("awakening_system.reception.progress", payload.nextRankName(), comp, need, remain), cardX + 12, cardY + 50, cardW - 24, 0xFF333333, false);

            g.fill(cardX + 12, cardY + 62, cardX + cardW - 12, cardY + 72, 0xFFCCCCCC);
            int fillW = need > 0 ? Math.min(cardW - 24, (cardW - 24) * comp / need) : cardW - 24;
            g.fill(cardX + 12, cardY + 62, cardX + 12 + fillW, cardY + 72, comp >= need ? 0xFF2ECC71 : 0xFFE5A823);
        } else {
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.rank_s_max"), cardX + cardW / 2, cardY + 58, cardW - 24, 0xFF886600);
        }

        // 4. Botón 1: Solicitar Ascenso de Rango
        int btn1X = left + 16;
        int btn1Y = top + 132;
        int btn1W = guiW - 32;
        int btn1H = 22;
        int maxBtn1W = btn1W - 12;

        if (payload.canPromote()) {
            boolean hov = mX >= btn1X && mX <= btn1X + btn1W && mY >= btn1Y && mY <= btn1Y + btn1H;
            g.fill(btn1X, btn1Y, btn1X + btn1W, btn1Y + btn1H, hov ? 0xFF2ECC71 : 0xFF27AE60);
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.btn_promote"), btn1X + btn1W / 2, btn1Y + 7, maxBtn1W, 0xFFFFFFFF);
        } else {
            g.fill(btn1X, btn1Y, btn1X + btn1W, btn1Y + btn1H, 0xFF9E9E9E);
            Component label = payload.guildRank().equals("S")
                    ? Component.translatable("awakening_system.reception.btn_max_rank")
                    : Component.translatable("awakening_system.reception.btn_locked");
            drawScaledCenteredString(g, label, btn1X + btn1W / 2, btn1Y + 7, maxBtn1W, 0xFF333333);
        }

        // 5. Botón 2: Bendición Diaria de Elena
        int btn2X = left + 16;
        int btn2Y = top + 160;
        int btn2W = guiW - 32;
        int btn2H = 20;
        int maxBtn2W = btn2W - 12;

        if (payload.canClaimBlessing()) {
            boolean hov = mX >= btn2X && mX <= btn2X + btn2W && mY >= btn2Y && mY <= btn2Y + btn2H;
            g.fill(btn2X, btn2Y, btn2X + btn2W, btn2Y + btn2H, hov ? 0xFF3498DB : 0xFF2980B9);
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.btn_blessing"), btn2X + btn2W / 2, btn2Y + 6, maxBtn2W, 0xFFFFFFFF);
        } else {
            g.fill(btn2X, btn2Y, btn2X + btn2W, btn2Y + btn2H, 0xFFBDC3C7);
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.btn_claimed"), btn2X + btn2W / 2, btn2Y + 6, maxBtn2W, 0xFF555555);
        }

        // 6. Botón Cerrar
        int closeX = left + guiW - 20;
        int closeY = top + 14;
        boolean hovClose = mX >= closeX && mX <= closeX + 12 && mY >= closeY && mY <= closeY + 12;
        g.fill(closeX, closeY, closeX + 12, closeY + 12, hovClose ? 0xFFE74C3C : 0xFFC0392B);
        g.drawCenteredString(this.font, "§f✕", closeX + 6, closeY + 2, 0xFFFFFFFF);

        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int guiW = 320;
        int guiH = 210;
        int left = (this.width - guiW) / 2;
        int top = (this.height - guiH) / 2;

        // Botón Cerrar
        int closeX = left + guiW - 20;
        int closeY = top + 14;
        if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12) {
            onClose();
            return true;
        }

        // Botón 1: Ascenso
        int btn1X = left + 16;
        int btn1Y = top + 132;
        int btn1W = guiW - 32;
        int btn1H = 22;
        if (mouseX >= btn1X && mouseX <= btn1X + btn1W && mouseY >= btn1Y && mouseY <= btn1Y + btn1H) {
            if (payload.canPromote()) {
                PacketDistributor.sendToServer(new GuildReceptionActionPayload(1));
                onClose();
                return true;
            }
        }

        // Botón 2: Bendición
        int btn2X = left + 16;
        int btn2Y = top + 160;
        int btn2W = guiW - 32;
        int btn2H = 20;
        if (mouseX >= btn2X && mouseX <= btn2X + btn2W && mouseY >= btn2Y && mouseY <= btn2Y + btn2H) {
            if (payload.canClaimBlessing()) {
                PacketDistributor.sendToServer(new GuildReceptionActionPayload(2));
                onClose();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawScaledCenteredString(GuiGraphics g, Component component, int centerX, int centerY, int maxWidth, int color) {
        int strWidth = this.font.width(component);
        if (strWidth <= maxWidth) {
            g.drawCenteredString(this.font, component, centerX, centerY, color);
        } else {
            float scale = (float) maxWidth / (float) strWidth;
            g.pose().pushPose();
            g.pose().translate(centerX, centerY + (this.font.lineHeight * (1.0f - scale) / 2.0f), 0);
            g.pose().scale(scale, scale, 1.0f);
            g.drawCenteredString(this.font, component, 0, 0, color);
            g.pose().popPose();
        }
    }

    private void drawScaledString(GuiGraphics g, Component component, int x, int y, int maxWidth, int color, boolean dropShadow) {
        int strWidth = this.font.width(component);
        if (strWidth <= maxWidth) {
            g.drawString(this.font, component, x, y, color, dropShadow);
        } else {
            float scale = (float) maxWidth / (float) strWidth;
            g.pose().pushPose();
            g.pose().translate(x, y + (this.font.lineHeight * (1.0f - scale) / 2.0f), 0);
            g.pose().scale(scale, scale, 1.0f);
            g.drawString(this.font, component, 0, 0, color, dropShadow);
            g.pose().popPose();
        }
    }
}
