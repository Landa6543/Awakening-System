package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class SystemMaintenanceScreen extends Screen {
    private HologramButton closeButton;

    public SystemMaintenanceScreen() {
        super(Component.translatable("awakening_system.maintenance.title"));
    }

    @Override
    protected void init() {
        int btnW = 170;
        int btnH = 22;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.height - 40;

        this.closeButton = new HologramButton(btnX, btnY, btnW, btnH,
                Component.translatable("awakening_system.maintenance.close"),
                btn -> this.onClose());
        this.addRenderableWidget(this.closeButton);
    }

    private static class HologramButton extends Button {
        public HologramButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            boolean is30 = false;
            boolean is40 = false;
            if (mc.player != null) {
                SystemData data = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (data != null) {
                    if (data.isSystem40Updating()) {
                        is40 = true;
                    } else if (data.isSystem30Updating()) {
                        is30 = true;
                    }
                }
            }

            boolean hovered = this.isHoveredOrFocused();
            int bg = is40
                    ? (hovered ? 0xEE1A2F4C : 0xDD0D1C30)
                    : (is30
                    ? (hovered ? 0xEE143D2E : 0xDD0D241B)
                    : (hovered ? 0xEE381E60 : 0xDD20133A));
            int border = is40
                    ? (hovered ? 0xFFDFB76C : 0xFF4EE2EC)
                    : (is30
                    ? (hovered ? 0xFF50FA7B : 0xFFD4AF37)
                    : (hovered ? 0xFFFFD700 : 0xFF9D4EDD));
            int corner = is40 ? 0xFFDFB76C : (is30 ? 0xFFD4AF37 : 0xFFFFD700);

            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);
            graphics.fill(this.getX(), this.getY(), this.getX() + 3, this.getY() + 3, corner);
            graphics.fill(this.getX() + this.width - 3, this.getY(), this.getX() + this.width, this.getY() + 3, corner);
            graphics.fill(this.getX(), this.getY() + this.height - 3, this.getX() + 3, this.getY() + this.height, corner);
            graphics.fill(this.getX() + this.width - 3, this.getY() + this.height - 3, this.getX() + this.width, this.getY() + this.height, corner);

            int textColor = hovered ? 0xFFFFFFFF : (is40 ? 0xFFDFB76C : (is30 ? 0xFF50FA7B : 0xFFFFD700));
            graphics.drawCenteredString(mc.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
        }
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 360.0F;
        float scaleY = (float) this.height / 250.0F;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(transformMouseX(mouseX), transformMouseY(mouseY), button, dragX, dragY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            graphics.pose().translate(cxF, cyF, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-cxF, -cyF, 0.0F);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            graphics.pose().popPose();
            return;
        }
        SystemData data = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data == null) {
            graphics.pose().popPose();
            return;
        }

        boolean is40 = data.isSystem40Updating();
        boolean is30 = data.isSystem30Updating();

        int boxW = Math.min(430, this.width - 24);
        int maxTextW = boxW - 36;

        Component title = is40
                ? Component.translatable("awakening_system.maintenance40.title")
                : (is30
                ? Component.translatable("awakening_system.maintenance30.title")
                : Component.translatable("awakening_system.maintenance.title"));
        Component subtitle = is40
                ? Component.translatable("awakening_system.maintenance40.subtitle")
                : (is30
                ? Component.translatable("awakening_system.maintenance30.subtitle")
                : Component.translatable("awakening_system.maintenance.subtitle"));

        Component desc1 = is40
                ? Component.translatable("awakening_system.maintenance40.desc_1")
                : (is30
                ? Component.translatable("awakening_system.maintenance30.desc_1")
                : Component.translatable("awakening_system.maintenance.desc_1"));
        Component desc2 = is40
                ? Component.translatable("awakening_system.maintenance40.desc_2")
                : (is30
                ? Component.translatable("awakening_system.maintenance30.desc_2")
                : Component.translatable("awakening_system.maintenance.desc_2"));
        Component desc3 = is40
                ? Component.translatable("awakening_system.maintenance40.desc_3")
                : (is30
                ? Component.translatable("awakening_system.maintenance30.desc_3")
                : Component.translatable("awakening_system.maintenance.desc_3"));

        List<FormattedCharSequence> lines1 = this.font.split(desc1, maxTextW);
        List<FormattedCharSequence> lines2 = this.font.split(desc2, maxTextW);
        List<FormattedCharSequence> lines3 = this.font.split(desc3, maxTextW);

        int totalDescLines = lines1.size() + lines2.size() + lines3.size();
        int descBlockH = totalDescLines * 12 + 6;
        int boxH = 150 + descBlockH;
        int boxX = (this.width - boxW) / 2;
        int boxY = Math.max(8, (this.height - boxH) / 2);

        if (this.closeButton != null) {
            this.closeButton.setPosition((this.width - this.closeButton.getWidth()) / 2, boxY + boxH - 30);
        }

        int outerBorder = is40 ? 0xFFDFB76C : (is30 ? 0xFFD4AF37 : 0xFFFFD700);
        int innerBorder = is40 ? 0xFF4EE2EC : (is30 ? 0xFF50FA7B : 0xFF9D4EDD);
        int bg = is40 ? 0xF00A1324 : (is30 ? 0xF00A1815 : 0xF0140B24);
        int cornerAccent = is40 ? 0xFFDFB76C : (is30 ? 0xFFD4AF37 : 0xFFFFD700);
        int divBg = is40 ? 0xFF1D3557 : (is30 ? 0xFF1B4D3E : 0xFF5A2A82);
        int titleCol = is40 ? 0xFFDFB76C : (is30 ? 0xFF50FA7B : 0xFFFFD700);
        int subCol = is40 ? 0xFF4EE2EC : (is30 ? 0xFFD4AF37 : 0xFFE0AAFF);
        int textCol = is40 ? 0xFFE2F1FF : (is30 ? 0xFFD8FBE4 : 0xFFEDE4F8);
        int alertCol = 0xFFFF8080;

        graphics.fill(boxX - 2, boxY - 2, boxX + boxW + 2, boxY + boxH + 2, outerBorder);
        graphics.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, innerBorder);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, bg);

        graphics.fill(boxX, boxY, boxX + 12, boxY + 2, cornerAccent);
        graphics.fill(boxX, boxY, boxX + 2, boxY + 12, cornerAccent);
        graphics.fill(boxX + boxW - 12, boxY, boxX + boxW, boxY + 2, cornerAccent);
        graphics.fill(boxX + boxW - 2, boxY, boxX + boxW, boxY + 12, cornerAccent);
        graphics.fill(boxX, boxY + boxH - 2, boxX + 12, boxY + boxH, cornerAccent);
        graphics.fill(boxX, boxY + boxH - 12, boxX + 2, boxY + boxH, cornerAccent);
        graphics.fill(boxX + boxW - 12, boxY + boxH - 2, boxX + boxW, boxY + boxH, cornerAccent);
        graphics.fill(boxX + boxW - 2, boxY + boxH - 12, boxX + boxW, boxY + boxH, cornerAccent);

        graphics.drawCenteredString(this.font, title, this.width / 2, boxY + 12, titleCol);
        graphics.drawCenteredString(this.font, subtitle, this.width / 2, boxY + 26, subCol);

        graphics.fill(boxX + 20, boxY + 39, boxX + boxW - 20, boxY + 40, divBg);
        graphics.fill(this.width / 2 - 20, boxY + 39, this.width / 2 + 20, boxY + 40, cornerAccent);

        int curY = boxY + 47;
        for (FormattedCharSequence line : lines1) {
            graphics.drawCenteredString(this.font, line, this.width / 2, curY, textCol);
            curY += 12;
        }
        for (FormattedCharSequence line : lines2) {
            graphics.drawCenteredString(this.font, line, this.width / 2, curY, textCol);
            curY += 12;
        }
        for (FormattedCharSequence line : lines3) {
            graphics.drawCenteredString(this.font, line, this.width / 2, curY, alertCol);
            curY += 12;
        }

        int ticks = is40
                ? data.system40UpdateRemainingTicks()
                : (is30 ? data.system30UpdateRemainingTicks() : data.systemUpdateRemainingTicks());
        int totalSec = Math.max(0, ticks / 20);
        int mins = totalSec / 60;
        int secs = totalSec % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);

        Component timeComp = Component.translatable("awakening_system.maintenance.time_remaining", timeStr);
        int timeY = curY + 6;
        graphics.drawCenteredString(this.font, timeComp, this.width / 2, timeY, is40 ? 0xFF8BE9FD : (is30 ? 0xFF88FFB0 : 0xFFFFEE88));

        int barW = Math.min(280, boxW - 40);
        int barH = 14;
        int barX = (this.width - barW) / 2;
        int barY = timeY + 16;

        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, divBg);
        graphics.fill(barX, barY, barX + barW, barY + barH, is40 ? 0xFF0D1C30 : (is30 ? 0xFF0D241B : 0xFF180E2B));

        float progress = Math.max(0.0F, Math.min(1.0F, (6000 - ticks) / 6000.0F));
        int filled = (int) (barW * progress);
        if (filled > 0) {
            int fillCol = is40 ? 0xFFDFB76C : (is30 ? 0xFF50FA7B : 0xFFFFD700);
            int fillHighlight = is40 ? 0xFF8BE9FD : (is30 ? 0xFFB3FFD1 : 0xFFFFEEAA);
            graphics.fill(barX, barY, barX + filled, barY + barH, fillCol);
            graphics.fill(barX, barY, barX + filled, barY + 2, fillHighlight);
        }

        int percent = (int) (progress * 100);
        graphics.drawCenteredString(this.font, Component.literal(percent + "%"), this.width / 2, barY + 3, is40 ? 0xFF07111D : (is30 ? 0xFF0A1815 : 0xFF140B24));

        super.render(graphics, mX, mY, partialTick);
        graphics.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
