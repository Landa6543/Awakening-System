package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
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
    protected void init() {
        super.init();
        AwakeningSystemClient.playUiSound(com.soybe.awakeningsystem.entity.NpcProfile.ELENA_RECEPTIONIST.getVoiceSound(), 1.0F, 1.0F);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new SystemScreen());
        }
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

        ModernGuiHelper.drawModernWindow(g, left, top, guiW, guiH, ModernGuiHelper.GuiTheme.SOLO_LEVELING);

        drawScaledCenteredString(g, Component.translatable("awakening_system.reception.title").copy().withStyle(net.minecraft.ChatFormatting.BOLD), left + guiW / 2, top + 10, guiW - 48, 0xFF70E8FF);
        drawScaledCenteredString(g, Component.translatable("awakening_system.reception.subtitle"), left + guiW / 2, top + 22, guiW - 48, 0xFFFFD700);

        int cardX = left + 16;
        int cardY = top + 38;
        int cardW = guiW - 32;
        int cardH = 86;

        g.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0x400A1626);
        g.renderOutline(cardX, cardY, cardW, cardH, 0x5500E5FF);
        g.fill(cardX + 2, cardY, cardX + cardW - 2, cardY + 1, 0x8800E5FF);

        int sealColor = switch (payload.guildRank()) {
            case "F" -> 0xFF888888;
            case "E" -> 0xFF55AA55;
            case "D" -> 0xFF33BB77;
            case "C" -> 0xFF2299DD;
            case "B" -> 0xFFDDAA11;
            case "A" -> 0xFFEE5522;
            default  -> 0xFFAA22CC;
        };
        g.fill(cardX + 10, cardY + 10, cardX + 42, cardY + 42, (sealColor & 0x00FFFFFF) | 0x33000000);
        g.renderOutline(cardX + 10, cardY + 10, 32, 32, sealColor);
        g.drawCenteredString(this.font, "§l" + payload.guildRank(), cardX + 26, cardY + 22, sealColor);

        g.drawString(this.font, Component.translatable("awakening_system.reception.record"), cardX + 48, cardY + 10, 0xFFFFD866, false);
        g.drawString(this.font, Component.translatable("awakening_system.reception.current_rank", payload.guildRank()), cardX + 48, cardY + 22, 0xFFFFFFFF, false);
        g.drawString(this.font, Component.translatable("awakening_system.reception.completed", payload.completedBounties()), cardX + 48, cardY + 34, 0xFFB0E0E6, false);

        if (!payload.guildRank().equals("S")) {
            int need = payload.nextRankNeeded();
            int comp = payload.completedBounties();
            int remain = Math.max(0, need - comp);
            drawScaledString(g, Component.translatable("awakening_system.reception.progress", payload.nextRankName(), comp, need, remain), cardX + 12, cardY + 52, cardW - 24, 0xFFE0E0E0, false);

            g.fill(cardX + 12, cardY + 66, cardX + cardW - 12, cardY + 74, 0xFF081420);
            g.renderOutline(cardX + 12, cardY + 66, cardW - 24, 8, 0x402D7691);
            int fillW = need > 0 ? Math.min(cardW - 24, (cardW - 24) * comp / need) : cardW - 24;
            g.fill(cardX + 12, cardY + 66, cardX + 12 + fillW, cardY + 74, comp >= need ? 0xFF2ECC71 : 0xFF00E5FF);
        } else {
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.rank_s_max"), cardX + cardW / 2, cardY + 58, cardW - 24, 0xFFFFD700);
        }

        int btn1X = left + 16;
        int btn1Y = top + 132;
        int btn1W = guiW - 32;
        int btn1H = 22;
        int maxBtn1W = btn1W - 12;

        if (payload.canPromote()) {
            boolean hov = mX >= btn1X && mX <= btn1X + btn1W && mY >= btn1Y && mY <= btn1Y + btn1H;
            g.fill(btn1X, btn1Y, btn1X + btn1W, btn1Y + btn1H, hov ? 0xDD2ECC71 : 0xAA1E7E34);
            g.renderOutline(btn1X, btn1Y, btn1W, btn1H, hov ? 0xFFFFFFFF : 0xFF50FA7B);
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.btn_promote").copy().withStyle(net.minecraft.ChatFormatting.BOLD), btn1X + btn1W / 2, btn1Y + 7, maxBtn1W, 0xFFFFFFFF);
        } else {
            g.fill(btn1X, btn1Y, btn1X + btn1W, btn1Y + btn1H, 0x30102536);
            g.renderOutline(btn1X, btn1Y, btn1W, btn1H, 0x402D7691);
            Component label = payload.guildRank().equals("S")
                    ? Component.translatable("awakening_system.reception.btn_max_rank")
                    : Component.translatable("awakening_system.reception.btn_locked");
            drawScaledCenteredString(g, label, btn1X + btn1W / 2, btn1Y + 7, maxBtn1W, 0xFF888888);
        }

        int btn2X = left + 16;
        int btn2Y = top + 160;
        int btn2W = guiW - 32;
        int btn2H = 20;
        int maxBtn2W = btn2W - 12;

        if (payload.canClaimBlessing()) {
            boolean hov = mX >= btn2X && mX <= btn2X + btn2W && mY >= btn2Y && mY <= btn2Y + btn2H;
            g.fill(btn2X, btn2Y, btn2X + btn2W, btn2Y + btn2H, hov ? 0xDD2277AA : 0xAA144466);
            g.renderOutline(btn2X, btn2Y, btn2W, btn2H, hov ? 0xFFFFFFFF : 0xFF00E5FF);
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.btn_blessing").copy().withStyle(net.minecraft.ChatFormatting.BOLD), btn2X + btn2W / 2, btn2Y + 6, maxBtn2W, 0xFFFFFFFF);
        } else {
            g.fill(btn2X, btn2Y, btn2X + btn2W, btn2Y + btn2H, 0x30102536);
            g.renderOutline(btn2X, btn2Y, btn2W, btn2H, 0x302D7691);
            drawScaledCenteredString(g, Component.translatable("awakening_system.reception.btn_claimed"), btn2X + btn2W / 2, btn2Y + 6, maxBtn2W, 0xFF666666);
        }

        int closeX = left + guiW - 22;
        int closeY = top + 10;
        boolean hovClose = mX >= closeX && mX <= closeX + 14 && mY >= closeY && mY <= closeY + 14;
        g.fill(closeX, closeY, closeX + 14, closeY + 14, hovClose ? 0x88FF4444 : 0x33000000);
        g.renderOutline(closeX, closeY, 14, 14, hovClose ? 0xFFFF6666 : 0x5500E5FF);
        g.drawCenteredString(this.font, "✕", closeX + 7, closeY + 3, hovClose ? 0xFFFFFFFF : 0xFF70E8FF);

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

        int closeX = left + guiW - 20;
        int closeY = top + 14;
        if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12) {
            AwakeningSystemClient.playButtonClick();
            onClose();
            return true;
        }

        int btn1X = left + 16;
        int btn1Y = top + 132;
        int btn1W = guiW - 32;
        int btn1H = 22;
        if (mouseX >= btn1X && mouseX <= btn1X + btn1W && mouseY >= btn1Y && mouseY <= btn1Y + btn1H) {
            if (payload.canPromote()) {
                AwakeningSystemClient.playButtonClick();
                PacketDistributor.sendToServer(new GuildReceptionActionPayload(1));
                onClose();
                return true;
            }
        }

        int btn2X = left + 16;
        int btn2Y = top + 160;
        int btn2W = guiW - 32;
        int btn2H = 20;
        if (mouseX >= btn2X && mouseX <= btn2X + btn2W && mouseY >= btn2Y && mouseY <= btn2Y + btn2H) {
            if (payload.canClaimBlessing()) {
                AwakeningSystemClient.playButtonClick();
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
