package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Pantalla de Configuración y Personalización de la Interfaz (GUI & HUD).
 * Permite redimensionar, activar/desactivar componentes individuales y restablecer posiciones.
 */
public class GuiConfigScreen extends Screen {
    private static final int GUI_WIDTH = 360;
    private static final int GUI_HEIGHT = 258;

    private final Screen parentScreen;
    private int currentTab = 0;

    public GuiConfigScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.gui_config.title"));
        this.parentScreen = parentScreen;
    }

    public static void open(Screen parent) {
        Minecraft.getInstance().setScreen(new GuiConfigScreen(parent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        // Authentic Manhwa System Frame
        g.blit(ModernGuiHelper.SYSTEM_WINDOW_FRAME, left, top, GUI_WIDTH, GUI_HEIGHT, 0.0F, 0.0F, 880, 620, 880, 620);

        // Header Title
        g.drawString(font, Component.translatable("awakening_system.gui_config.header").copy().withStyle(net.minecraft.ChatFormatting.BOLD), left + 20, top + 14, 0xFF00E5FF);

        // [✕] Close button at top-right
        int closeX = left + GUI_WIDTH - 26;
        int closeY = top + 11;
        boolean closeHover = mouseX >= closeX && mouseX < closeX + 16 && mouseY >= closeY && mouseY < closeY + 16;
        g.fill(closeX, closeY, closeX + 16, closeY + 16, closeHover ? 0x88FF4444 : 0x33000000);
        g.renderOutline(closeX, closeY, 16, 16, closeHover ? 0xFFFF6666 : 0x5500E5FF);
        g.drawCenteredString(font, "✕", closeX + 8, closeY + 4, closeHover ? 0xFFFFFFFF : 0xFF70E8FF);

        int tabW = (GUI_WIDTH - 36) / 3;
        int t0X = left + 18;
        int t1X = left + 18 + tabW;
        int t2X = left + 18 + tabW * 2;
        int tY = top + 34;
        int tH = 17;

        boolean hovT0 = mouseX >= t0X && mouseX < t0X + tabW && mouseY >= tY && mouseY < tY + tH;
        boolean hovT1 = mouseX >= t1X && mouseX < t1X + tabW && mouseY >= tY && mouseY < tY + tH;
        boolean hovT2 = mouseX >= t2X && mouseX < t2X + tabW && mouseY >= tY && mouseY < tY + tH;

        g.fill(t0X, tY, t0X + tabW, tY + tH, currentTab == 0 ? 0x881A384D : (hovT0 ? 0x551E2B38 : 0x220F1822));
        g.fill(t1X, tY, t1X + tabW, tY + tH, currentTab == 1 ? 0x881A384D : (hovT1 ? 0x551E2B38 : 0x220F1822));
        g.fill(t2X, tY, t2X + tabW, tY + tH, currentTab == 2 ? 0x881A384D : (hovT2 ? 0x551E2B38 : 0x220F1822));
        g.renderOutline(t0X, tY, tabW, tH, currentTab == 0 ? 0xFF00E5FF : 0x442A4255);
        g.renderOutline(t1X, tY, tabW, tH, currentTab == 1 ? 0xFF00E5FF : 0x442A4255);
        g.renderOutline(t2X, tY, tabW, tH, currentTab == 2 ? 0xFF00E5FF : 0x442A4255);

        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.system"), t0X + tabW / 2, tY + 4, currentTab == 0 ? 0xFFFFFFFF : 0xFFAAAAAA);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.tabs"), t1X + tabW / 2, tY + 4, currentTab == 1 ? 0xFFFFFFFF : 0xFFAAAAAA);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.hud"), t2X + tabW / 2, tY + 4, currentTab == 2 ? 0xFFFFFFFF : 0xFFAAAAAA);

        int contentY = top + 55;
        if (currentTab == 0) {
            renderSystemTab(g, left, contentY, mouseX, mouseY);
        } else if (currentTab == 1) {
            renderTabsConfigTab(g, left, contentY, mouseX, mouseY);
        } else {
            renderHudTab(g, left, contentY, mouseX, mouseY);
        }

        int btnY = top + GUI_HEIGHT - 22;
        int btnResetW = 120;
        int btnSaveW = 150;

        boolean resetHov = mouseX >= left + 18 && mouseX < left + 18 + btnResetW && mouseY >= btnY && mouseY < btnY + 16;
        g.fill(left + 18, btnY, left + 18 + btnResetW, btnY + 16, resetHov ? 0x886E2020 : 0x443B1111);
        g.renderOutline(left + 18, btnY, btnResetW, 16, resetHov ? 0xFFFF6666 : 0x66882222);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.btn.reset"), left + 18 + btnResetW / 2, btnY + 4, resetHov ? 0xFFFFDDDD : 0xFFFFAAAA);

        int saveX = left + GUI_WIDTH - 18 - btnSaveW;
        boolean saveHov = mouseX >= saveX && mouseX < saveX + btnSaveW && mouseY >= btnY && mouseY < btnY + 16;
        g.fill(saveX, btnY, saveX + btnSaveW, btnY + 16, saveHov ? 0x991A5D3E : 0x550E3A26);
        g.renderOutline(saveX, btnY, btnSaveW, 16, saveHov ? 0xFF50FA7B : 0x6628864E);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.btn.save"), saveX + btnSaveW / 2, btnY + 4, 0xFF50FA7B);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderSystemTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.gui_config.system.gui_scale"), left + 16, top + 2, 0xFFFFFFFF);
        int scalePercent = (int) Math.round(GuiConfig.systemGuiScale * 100);
        String scaleStr = "§e" + scalePercent + "%";
        g.drawString(font, scaleStr, left + 140, top + 2, 0xFFFFD700);

        int minusX = left + 185;
        int plusX = left + 211;
        boolean minusHov = mouseX >= minusX && mouseX < minusX + 22 && mouseY >= top && mouseY < top + 13;
        boolean plusHov = mouseX >= plusX && mouseX < plusX + 22 && mouseY >= top && mouseY < top + 13;

        g.fill(minusX, top, minusX + 22, top + 13, minusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, top, 22, 13, minusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 11, top + 2, 0xFFFFFFFF);

        g.fill(plusX, top, plusX + 22, top + 13, plusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, top, 22, 13, plusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 11, top + 2, 0xFFFFFFFF);

        int resetScaleX = left + 237;
        boolean resetScaleHov = mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= top && mouseY < top + 13;
        g.fill(resetScaleX, top, resetScaleX + 42, top + 13, resetScaleHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, top, 42, 13, resetScaleHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "100%", resetScaleX + 21, top + 2, 0xFFEEEEEE);

        g.drawString(font, Component.translatable("awakening_system.gui_config.system.current_pos"), left + 16, top + 18, 0xFFFFFFFF);
        String posStr = "§7X: §f" + GuiConfig.systemGuiOffsetX + "  §7Y: §f" + GuiConfig.systemGuiOffsetY;
        g.drawString(font, posStr, left + 80, top + 18, 0xFFFFFFFF);

        int centerBtnX = left + 215;
        boolean centerHov = mouseX >= centerBtnX && mouseX < centerBtnX + 90 && mouseY >= top + 16 && mouseY < top + 29;
        g.fill(centerBtnX, top + 16, centerBtnX + 90, top + 29, centerHov ? 0xAA225577 : 0x66113344);
        g.renderOutline(centerBtnX, top + 16, 90, 13, centerHov ? 0xFF70E8FF : 0xFF225577);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.system.center_btn"), centerBtnX + 45, top + 18, 0xFF70E8FF);

        g.fill(left + 16, top + 33, left + GUI_WIDTH - 16, top + 34, 0x443D566E);

        int col1X = left + 16;
        int col2X = left + 176;
        int colW = 148;

        renderToggle(g, col1X, top + 40, colW, Component.translatable("awakening_system.gui_config.system.toggle_3d"), GuiConfig.showPlayer3DModel, mouseX, mouseY);
        renderToggle(g, col2X, top + 40, colW, Component.translatable("awakening_system.gui_config.system.toggle_side_tabs"), GuiConfig.showDimensionSideTabs, mouseX, mouseY);

        renderToggle(g, col1X, top + 59, colW, Component.translatable("awakening_system.gui_config.system.toggle_free_move"), GuiConfig.freeMoveEnabled, mouseX, mouseY);
        renderToggle(g, col2X, top + 59, colW, Component.translatable("awakening_system.gui_config.system.toggle_sfx"), GuiConfig.uiSoundsEnabled, mouseX, mouseY);

        renderToggle(g, col1X, top + 78, colW, Component.translatable("awakening_system.gui_config.system.toggle_font"), GuiConfig.useSystemFont, mouseX, mouseY);

        g.fill(left + 16, top + 98, left + GUI_WIDTH - 16, top + 99, 0x443D566E);

        var tipLines = font.split(Component.translatable("awakening_system.gui_config.system.tip"), GUI_WIDTH - 36);
        int ty = top + 104;
        for (var line : tipLines) {
            g.drawCenteredString(font, line, left + GUI_WIDTH / 2, ty, 0x888888);
            ty += 10;
        }
    }

    private void renderTabsConfigTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.gui_config.tabs.header"), left + 16, top + 2, 0xFFDFB76C);
        g.drawString(font, Component.translatable("awakening_system.gui_config.tabs.subheader"), left + 16, top + 14, 0xFFAAAAAA);

        g.fill(left + 16, top + 26, left + GUI_WIDTH - 16, top + 27, 0x443D566E);

        int col1X = left + 16;
        int col2X = left + 176;
        int colW = 148;

        renderToggle(g, col1X, top + 34, colW, Component.translatable("awakening_system.screen.tab.status"), GuiConfig.showTabStatus, mouseX, mouseY);
        renderToggle(g, col2X, top + 34, colW, Component.translatable("awakening_system.screen.tab.skills"), GuiConfig.showTabSkills, mouseX, mouseY);

        renderToggle(g, col1X, top + 52, colW, Component.translatable("awakening_system.screen.tab.missions"), GuiConfig.showTabMissions, mouseX, mouseY);
        renderToggle(g, col2X, top + 52, colW, Component.translatable("awakening_system.screen.tab.titles"), GuiConfig.showTabTitles, mouseX, mouseY);

        renderToggle(g, col1X, top + 70, colW, Component.translatable("awakening_system.screen.tab.shop"), GuiConfig.showTabShop, mouseX, mouseY);
        renderToggle(g, col2X, top + 70, colW, Component.translatable("awakening_system.screen.tab.progress"), GuiConfig.showTabProgress, mouseX, mouseY);

        renderToggle(g, col1X, top + 88, colW, Component.translatable("awakening_system.screen.tab.ranking"), GuiConfig.showTabRanking, mouseX, mouseY);
        renderToggle(g, col2X, top + 88, colW, Component.translatable("awakening_system.screen.tab.artifacts"), GuiConfig.showTabArtifacts, mouseX, mouseY);

        g.fill(left + 16, top + 108, left + GUI_WIDTH - 16, top + 109, 0x443D566E);

        var tipLines = font.split(Component.translatable("awakening_system.gui_config.tabs.tip"), GUI_WIDTH - 36);
        int ty = top + 114;
        for (var line : tipLines) {
            g.drawCenteredString(font, line, left + GUI_WIDTH / 2, ty, 0x888888);
            ty += 10;
        }
    }

    private void renderHudTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.gui_config.hud.scale"), left + 16, top + 1, 0xFFFFFFFF);
        int scalePercent = (int) Math.round(GuiConfig.hudScale * 100);
        String scaleStr = "§e" + scalePercent + "%";
        g.drawString(font, scaleStr, left + 130, top + 1, 0xFFFFD700);

        int minusX = left + 185;
        int plusX = left + 211;
        boolean minusHov = mouseX >= minusX && mouseX < minusX + 22 && mouseY >= top && mouseY < top + 13;
        boolean plusHov = mouseX >= plusX && mouseX < plusX + 22 && mouseY >= top && mouseY < top + 13;

        g.fill(minusX, top, minusX + 22, top + 13, minusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, top, 22, 13, minusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 11, top + 2, 0xFFFFFFFF);

        g.fill(plusX, top, plusX + 22, top + 13, plusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, top, 22, 13, plusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 11, top + 2, 0xFFFFFFFF);

        int resetScaleX = left + 237;
        boolean resetScaleHov = mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= top && mouseY < top + 13;
        g.fill(resetScaleX, top, resetScaleX + 42, top + 13, resetScaleHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, top, 42, 13, resetScaleHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "100%", resetScaleX + 21, top + 2, 0xFFEEEEEE);

        int cTop = top + 15;
        g.drawString(font, Component.translatable("awakening_system.gui_config.hud.popup_scale"), left + 16, cTop + 1, 0xFFFFFFFF);
        int cScalePercent = (int) Math.round(GuiConfig.constellationNotifScale * 100);
        String cScaleStr = "§e" + cScalePercent + "%";
        g.drawString(font, cScaleStr, left + 130, cTop + 1, 0xFFFFD700);

        boolean cMinusHov = mouseX >= minusX && mouseX < minusX + 22 && mouseY >= cTop && mouseY < cTop + 13;
        boolean cPlusHov = mouseX >= plusX && mouseX < plusX + 22 && mouseY >= cTop && mouseY < cTop + 13;

        g.fill(minusX, cTop, minusX + 22, cTop + 13, cMinusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, cTop, 22, 13, cMinusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 11, cTop + 2, 0xFFFFFFFF);

        g.fill(plusX, cTop, plusX + 22, cTop + 13, cPlusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, cTop, 22, 13, cPlusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 11, cTop + 2, 0xFFFFFFFF);

        boolean cResetHov = mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= cTop && mouseY < cTop + 13;
        g.fill(resetScaleX, cTop, resetScaleX + 42, cTop + 13, cResetHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, cTop, 42, 13, cResetHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "85%", resetScaleX + 21, cTop + 2, 0xFFEEEEEE);

        int col1X = left + 16;
        int col2X = left + 176;
        int colW = 148;

        int row2Y = top + 29;
        boolean modeHov = mouseX >= col1X && mouseX < col1X + colW && mouseY >= row2Y && mouseY < row2Y + 14;
        g.fill(col1X, row2Y, col1X + colW, row2Y + 14, modeHov ? 0xDD3A2454 : 0xAA241638);
        g.renderOutline(col1X, row2Y, colW, 14, modeHov ? 0xFFB388FF : 0xFF7D5BA6);
        Component healthComp = Component.literal("❤ ").append(GuiConfig.healthDisplayMode.getComponent());
        g.drawCenteredString(font, healthComp, col1X + colW / 2, row2Y + 3, 0xFFE0AAFF);

        boolean foodModeHov = mouseX >= col2X && mouseX < col2X + colW && mouseY >= row2Y && mouseY < row2Y + 14;
        g.fill(col2X, row2Y, col2X + colW, row2Y + 14, foodModeHov ? 0xDD543818 : 0xAA38220A);
        g.renderOutline(col2X, row2Y, colW, 14, foodModeHov ? 0xFFFFB74D : 0xFFB26A00);
        Component foodComp = Component.literal("🍗 ").append(GuiConfig.foodDisplayMode.getComponent());
        g.drawCenteredString(font, foodComp, col2X + colW / 2, row2Y + 3, 0xFFFFCC80);

        int row3Y = top + 45;
        boolean editHov = mouseX >= col1X && mouseX < col1X + colW && mouseY >= row3Y && mouseY < row3Y + 14;
        g.fill(col1X, row3Y, col1X + colW, row3Y + 14, editHov ? 0xDD225577 : 0xAA143347);
        g.renderOutline(col1X, row3Y, colW, 14, editHov ? 0xFF00E5FF : 0xFF4EE2EC);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.hud.move_btn"), col1X + colW / 2, row3Y + 3, 0xFFFFFFFF);

        boolean resetHudHov = mouseX >= col2X && mouseX < col2X + colW && mouseY >= row3Y && mouseY < row3Y + 14;
        g.fill(col2X, row3Y, col2X + colW, row3Y + 14, resetHudHov ? 0xAA553322 : 0x77331810);
        g.renderOutline(col2X, row3Y, colW, 14, resetHudHov ? 0xFFFF9944 : 0xFF884422);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.hud.reset_coords"), col2X + colW / 2, row3Y + 3, resetHudHov ? 0xFFFFD7A0 : 0xFFFFAA66);

        g.fill(left + 16, top + 61, left + GUI_WIDTH - 16, top + 62, 0x443D566E);
        g.drawString(font, Component.translatable("awakening_system.gui_config.hud.components_header"), left + 16, top + 64, 0xFFDFB76C);

        renderToggle(g, col1X, top + 74, colW, Component.translatable("awakening_system.gui_config.hud.toggle_status_card"), GuiConfig.showHudStatusCard, mouseX, mouseY);
        renderToggle(g, col2X, top + 74, colW, Component.translatable("awakening_system.gui_config.hud.toggle_skill_bar"), GuiConfig.showHudSkillBar, mouseX, mouseY);

        renderToggle(g, col1X, top + 88, colW, Component.translatable("awakening_system.gui_config.hud.toggle_appraisal"), GuiConfig.showHudAppraisal, mouseX, mouseY);
        renderToggle(g, col2X, top + 88, colW, Component.translatable("awakening_system.gui_config.hud.toggle_combo"), GuiConfig.showHudCombo, mouseX, mouseY);

        renderToggle(g, col1X, top + 102, colW, Component.translatable("awakening_system.gui_config.hud.toggle_constellation_popups"), GuiConfig.showConstellationPopups, mouseX, mouseY);
        renderToggle(g, col2X, top + 102, colW, Component.translatable("awakening_system.gui_config.hud.toggle_compact_constellations"), GuiConfig.compactConstellationPopups, mouseX, mouseY);

        renderToggle(g, col1X, top + 116, colW, Component.translatable("awakening_system.gui_config.hud.toggle_dokkaebi_scenario"), GuiConfig.showHudDokkaebiScenario, mouseX, mouseY);
        renderToggle(g, col2X, top + 116, colW, Component.translatable("awakening_system.gui_config.hud.toggle_vanilla_drumsticks"), !GuiConfig.shouldHideVanillaFood(), mouseX, mouseY);

        renderToggle(g, col1X, top + 130, colW, Component.translatable("awakening_system.gui_config.hud.toggle_food"), GuiConfig.isHudFoodBarVisible(), mouseX, mouseY);

        g.fill(left + 16, top + 145, left + GUI_WIDTH - 16, top + 146, 0x443D566E);

        var tipLines = font.split(Component.translatable("awakening_system.gui_config.hud.tip"), GUI_WIDTH - 36);
        int ty = top + 149;
        for (var line : tipLines) {
            g.drawCenteredString(font, line, left + GUI_WIDTH / 2, ty, 0x888888);
            ty += 9;
        }
    }

    private void renderToggle(GuiGraphics g, int x, int y, int width, Component label, boolean active, int mouseX, int mouseY) {
        int toggleW = 34;
        int toggleH = 13;
        int toggleX = x + width - toggleW;

        boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 14;

        int maxLabelW = width - toggleW - 4;
        if (font.width(label) > maxLabelW) {
            String trimmed = font.plainSubstrByWidth(label.getString(), maxLabelW - 6) + "…";
            g.drawString(font, trimmed, x, y + 3, active ? 0xFFFFFFFF : 0xFF888888, false);
        } else {
            g.drawString(font, label, x, y + 3, active ? 0xFFFFFFFF : 0xFF888888, false);
        }

        int bg = active ? (hovered ? 0x991E7E48 : 0x55145732) : (hovered ? 0x885A1818 : 0x40301010);
        int border = active ? (hovered ? 0xFF50FA7B : 0xAA50FA7B) : (hovered ? 0xFFFF6666 : 0x66882222);

        g.fill(toggleX, y, toggleX + toggleW, y + toggleH, bg);
        g.renderOutline(toggleX, y, toggleW, toggleH, border);

        Component text = active ? Component.translatable("awakening_system.gui_config.toggle.on") : Component.translatable("awakening_system.gui_config.toggle.off");
        int textCol = active ? 0xFF50FA7B : 0xFFFFAAAA;
        g.drawCenteredString(font, text, toggleX + toggleW / 2, y + 3, textCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - GUI_WIDTH) / 2;
            int top = (height - GUI_HEIGHT) / 2;

            if (mouseX >= left + GUI_WIDTH - 26 && mouseX < left + GUI_WIDTH - 10 && mouseY >= top + 11 && mouseY < top + 27) {
                close();
                return true;
            }

            int tabW = (GUI_WIDTH - 36) / 3;
            int t0X = left + 18;
            int t1X = left + 18 + tabW;
            int t2X = left + 18 + tabW * 2;
            int tY = top + 34;
            int tH = 17;

            if (mouseX >= t0X && mouseX < t0X + tabW && mouseY >= tY && mouseY < tY + tH) {
                currentTab = 0;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }
            if (mouseX >= t1X && mouseX < t1X + tabW && mouseY >= tY && mouseY < tY + tH) {
                currentTab = 1;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }
            if (mouseX >= t2X && mouseX < t2X + tabW && mouseY >= tY && mouseY < tY + tH) {
                currentTab = 2;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }

            int btnY = top + GUI_HEIGHT - 24;
            int btnResetW = 120;
            if (mouseX >= left + 14 && mouseX < left + 14 + btnResetW && mouseY >= btnY && mouseY < btnY + 18) {
                GuiConfig.resetDefaults();
                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                return true;
            }

            int btnSaveW = 150;
            int saveX = left + GUI_WIDTH - 14 - btnSaveW;
            if (mouseX >= saveX && mouseX < saveX + btnSaveW && mouseY >= btnY && mouseY < btnY + 18) {
                GuiConfig.save();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                close();
                return true;
            }

            int contentY = top + 51;
            int col1X = left + 16;
            int col2X = left + 176;
            int colW = 148;

            if (currentTab == 0) {
                int minusX = left + 185;
                int plusX = left + 211;
                int resetScaleX = left + 237;

                if (mouseX >= minusX && mouseX < minusX + 22 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.systemGuiScale = (float) Math.max(0.5, Math.min(1.8, GuiConfig.systemGuiScale - 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= plusX && mouseX < plusX + 22 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.systemGuiScale = (float) Math.max(0.5, Math.min(1.8, GuiConfig.systemGuiScale + 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.systemGuiScale = 1.0F;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }

                int centerBtnX = left + 215;
                if (mouseX >= centerBtnX && mouseX < centerBtnX + 90 && mouseY >= contentY + 16 && mouseY < contentY + 29) {
                    GuiConfig.systemGuiOffsetX = 0;
                    GuiConfig.systemGuiOffsetY = 0;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.4F);
                    return true;
                }

                // Row 0 toggles
                if (mouseY >= contentY + 40 && mouseY < contentY + 54) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.showPlayer3DModel = !GuiConfig.showPlayer3DModel;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.showDimensionSideTabs = !GuiConfig.showDimensionSideTabs;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 1 toggles
                if (mouseY >= contentY + 59 && mouseY < contentY + 73) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.freeMoveEnabled = !GuiConfig.freeMoveEnabled;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.uiSoundsEnabled = !GuiConfig.uiSoundsEnabled;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 2 toggles: Manhwa Font
                if (mouseY >= contentY + 78 && mouseY < contentY + 92) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.useSystemFont = !GuiConfig.useSystemFont;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
            } else if (currentTab == 1) {
                // System Tabs toggles
                // Row 0 toggles: Status & Skills
                if (mouseY >= contentY + 34 && mouseY < contentY + 48) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        if (GuiConfig.showTabStatus && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabStatus = !GuiConfig.showTabStatus;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        if (GuiConfig.showTabSkills && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabSkills = !GuiConfig.showTabSkills;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 1 toggles: Missions & Titles
                if (mouseY >= contentY + 52 && mouseY < contentY + 66) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        if (GuiConfig.showTabMissions && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabMissions = !GuiConfig.showTabMissions;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        if (GuiConfig.showTabTitles && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabTitles = !GuiConfig.showTabTitles;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 2 toggles: Shop & Progress
                if (mouseY >= contentY + 70 && mouseY < contentY + 84) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        if (GuiConfig.showTabShop && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabShop = !GuiConfig.showTabShop;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        if (GuiConfig.showTabProgress && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabProgress = !GuiConfig.showTabProgress;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 3 toggles: Ranking & Artifacts
                if (mouseY >= contentY + 88 && mouseY < contentY + 102) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        if (GuiConfig.showTabRanking && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabRanking = !GuiConfig.showTabRanking;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        if (GuiConfig.showTabArtifacts && GuiConfig.countActiveTabs() <= 1) return true;
                        GuiConfig.showTabArtifacts = !GuiConfig.showTabArtifacts;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
            } else if (currentTab == 2) {
                int minusX = left + 185;
                int plusX = left + 211;
                int resetScaleX = left + 237;

                // HUD Scale
                if (mouseX >= minusX && mouseX < minusX + 22 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.hudScale = (float) Math.max(0.5, Math.min(2.0, GuiConfig.hudScale - 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= plusX && mouseX < plusX + 22 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.hudScale = (float) Math.max(0.5, Math.min(2.0, GuiConfig.hudScale + 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.hudScale = 1.0F;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }

                // Notification Scale
                int cTop = contentY + 16;
                if (mouseX >= minusX && mouseX < minusX + 22 && mouseY >= cTop && mouseY < cTop + 13) {
                    GuiConfig.constellationNotifScale = (float) Math.max(0.4F, Math.min(1.5F, GuiConfig.constellationNotifScale - 0.05F));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= plusX && mouseX < plusX + 22 && mouseY >= cTop && mouseY < cTop + 13) {
                    GuiConfig.constellationNotifScale = (float) Math.max(0.4F, Math.min(1.5F, GuiConfig.constellationNotifScale + 0.05F));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= cTop && mouseY < cTop + 13) {
                    GuiConfig.constellationNotifScale = 0.85F;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }

                // Health Mode button (row 2 col 1)
                if (mouseX >= col1X && mouseX < col1X + colW && mouseY >= contentY + 29 && mouseY < contentY + 44) {
                    GuiConfig.healthDisplayMode = switch (GuiConfig.healthDisplayMode) {
                        case CARD -> GuiConfig.HealthDisplayMode.VANILLA;
                        case COMPACT_BAR -> GuiConfig.HealthDisplayMode.CARD;
                        case VANILLA -> GuiConfig.HealthDisplayMode.COMPACT_BAR;
                    };
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                    return true;
                }

                // Food Mode button (row 2 col 2)
                if (mouseX >= col2X && mouseX < col2X + colW && mouseY >= contentY + 29 && mouseY < contentY + 44) {
                    GuiConfig.foodDisplayMode = switch (GuiConfig.foodDisplayMode) {
                        case BAR -> GuiConfig.FoodDisplayMode.VANILLA;
                        case VANILLA -> GuiConfig.FoodDisplayMode.OFF;
                        case OFF -> GuiConfig.FoodDisplayMode.BAR;
                    };
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                    return true;
                }

                // Interactive Editor button (row 3 col 1)
                if (mouseX >= col1X && mouseX < col1X + colW && mouseY >= contentY + 45 && mouseY < contentY + 60) {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    HudEditorScreen.open(this);
                    return true;
                }

                // Reset HUD Coords button (row 3 col 2)
                if (mouseX >= col2X && mouseX < col2X + colW && mouseY >= contentY + 45 && mouseY < contentY + 60) {
                    GuiConfig.resetHudPositions();
                    AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                    return true;
                }

                // Row 0 HUD toggles
                if (mouseY >= contentY + 74 && mouseY < contentY + 88) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.showHudStatusCard = !GuiConfig.showHudStatusCard;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.showHudSkillBar = !GuiConfig.showHudSkillBar;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 1 HUD toggles
                if (mouseY >= contentY + 88 && mouseY < contentY + 102) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.showHudAppraisal = !GuiConfig.showHudAppraisal;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.showHudCombo = !GuiConfig.showHudCombo;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 2 HUD toggles
                if (mouseY >= contentY + 102 && mouseY < contentY + 116) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.showConstellationPopups = !GuiConfig.showConstellationPopups;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.compactConstellationPopups = !GuiConfig.compactConstellationPopups;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 3 HUD toggles (contentY + 132)
                if (mouseY >= contentY + 116 && mouseY < contentY + 130) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.showHudDokkaebiScenario = !GuiConfig.showHudDokkaebiScenario;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        if (GuiConfig.foodDisplayMode == GuiConfig.FoodDisplayMode.VANILLA) {
                            GuiConfig.foodDisplayMode = GuiConfig.FoodDisplayMode.BAR;
                        } else {
                            GuiConfig.foodDisplayMode = GuiConfig.FoodDisplayMode.VANILLA;
                        }
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
                // Row 4 HUD toggles (contentY + 148)
                if (mouseY >= contentY + 130 && mouseY < contentY + 144) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        if (GuiConfig.foodDisplayMode == GuiConfig.FoodDisplayMode.BAR) {
                            GuiConfig.foodDisplayMode = GuiConfig.FoodDisplayMode.VANILLA;
                        } else {
                            GuiConfig.foodDisplayMode = GuiConfig.FoodDisplayMode.BAR;
                        }
                        GuiConfig.showHudFood = GuiConfig.isHudFoodBarVisible();
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void close() {
        GuiConfig.save();
        if (parentScreen != null) {
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
