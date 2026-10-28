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

        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFF4EE2EC);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF208111A);
        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF0E1F2E);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFF4EE2EC);

        g.drawString(font, Component.translatable("awakening_system.gui_config.header"), left + 14, top + 9, 0xFFDFB76C);

        boolean closeHover = mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20;
        g.fill(left + GUI_WIDTH - 22, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 14, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        int tabW = (GUI_WIDTH - 28) / 4;
        int t0X = left + 14;
        int t1X = left + 14 + tabW;
        int t2X = left + 14 + tabW * 2;
        int t3X = left + 14 + tabW * 3;
        int tY = top + 30;
        int tH = 17;

        boolean hovT0 = mouseX >= t0X && mouseX < t0X + tabW && mouseY >= tY && mouseY < tY + tH;
        boolean hovT1 = mouseX >= t1X && mouseX < t1X + tabW && mouseY >= tY && mouseY < tY + tH;
        boolean hovT2 = mouseX >= t2X && mouseX < t2X + tabW && mouseY >= tY && mouseY < tY + tH;
        boolean hovT3 = mouseX >= t3X && mouseX < t3X + tabW && mouseY >= tY && mouseY < tY + tH;

        g.fill(t0X, tY, t0X + tabW, tY + tH, currentTab == 0 ? 0xCC1A384D : (hovT0 ? 0x881E2B38 : 0x550F1822));
        g.fill(t1X, tY, t1X + tabW, tY + tH, currentTab == 1 ? 0xCC1A384D : (hovT1 ? 0x881E2B38 : 0x550F1822));
        g.fill(t2X, tY, t2X + tabW, tY + tH, currentTab == 2 ? 0xCC1A384D : (hovT2 ? 0x881E2B38 : 0x550F1822));
        g.fill(t3X, tY, t3X + tabW, tY + tH, currentTab == 3 ? 0xCC1A384D : (hovT3 ? 0x881E2B38 : 0x550F1822));
        g.renderOutline(t0X, tY, tabW, tH, currentTab == 0 ? 0xFF4EE2EC : 0xFF2A4255);
        g.renderOutline(t1X, tY, tabW, tH, currentTab == 1 ? 0xFF4EE2EC : 0xFF2A4255);
        g.renderOutline(t2X, tY, tabW, tH, currentTab == 2 ? 0xFF4EE2EC : 0xFF2A4255);
        g.renderOutline(t3X, tY, tabW, tH, currentTab == 3 ? 0xFF4EE2EC : 0xFF2A4255);

        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.system"), t0X + tabW / 2, tY + 4, currentTab == 0 ? 0xFFFFFFFF : 0xFFAAAAAA);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.tabs"), t1X + tabW / 2, tY + 4, currentTab == 1 ? 0xFFFFFFFF : 0xFFAAAAAA);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.hud"), t2X + tabW / 2, tY + 4, currentTab == 2 ? 0xFFFFFFFF : 0xFFAAAAAA);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.tab.minimap"), t3X + tabW / 2, tY + 4, currentTab == 3 ? 0xFFFFFFFF : 0xFFAAAAAA);

        int contentY = top + 51;
        if (currentTab == 0) {
            renderSystemTab(g, left, contentY, mouseX, mouseY);
        } else if (currentTab == 1) {
            renderTabsConfigTab(g, left, contentY, mouseX, mouseY);
        } else if (currentTab == 2) {
            renderHudTab(g, left, contentY, mouseX, mouseY);
        } else {
            renderMinimapTab(g, left, contentY, mouseX, mouseY);
        }

        int btnY = top + GUI_HEIGHT - 24;
        int btnResetW = 120;
        int btnSaveW = 150;

        boolean resetHov = mouseX >= left + 14 && mouseX < left + 14 + btnResetW && mouseY >= btnY && mouseY < btnY + 18;
        g.fill(left + 14, btnY, left + 14 + btnResetW, btnY + 18, resetHov ? 0xAA6E2020 : 0x773B1111);
        g.renderOutline(left + 14, btnY, btnResetW, 18, resetHov ? 0xFFFF6666 : 0xFF882222);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.btn.reset"), left + 14 + btnResetW / 2, btnY + 5, resetHov ? 0xFFFFDDDD : 0xFFFFAAAA);

        int saveX = left + GUI_WIDTH - 14 - btnSaveW;
        boolean saveHov = mouseX >= saveX && mouseX < saveX + btnSaveW && mouseY >= btnY && mouseY < btnY + 18;
        g.fill(saveX, btnY, saveX + btnSaveW, btnY + 18, saveHov ? 0xCC1A5D3E : 0xAA0E3A26);
        g.renderOutline(saveX, btnY, btnSaveW, 18, saveHov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.btn.save"), saveX + btnSaveW / 2, btnY + 5, 0xFF50FA7B);

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

        g.fill(left + 16, top + 80, left + GUI_WIDTH - 16, top + 81, 0x443D566E);

        var tipLines = font.split(Component.translatable("awakening_system.gui_config.system.tip"), GUI_WIDTH - 36);
        int ty = top + 86;
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

        int cTop = top + 16;
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

        int row2Y = top + 32;
        boolean modeHov = mouseX >= col1X && mouseX < col1X + colW && mouseY >= row2Y && mouseY < row2Y + 15;
        g.fill(col1X, row2Y, col1X + colW, row2Y + 15, modeHov ? 0xDD3A2454 : 0xAA241638);
        g.renderOutline(col1X, row2Y, colW, 15, modeHov ? 0xFFB388FF : 0xFF7D5BA6);
        Component healthComp = Component.literal("❤ ").append(GuiConfig.healthDisplayMode.getComponent());
        g.drawCenteredString(font, healthComp, col1X + colW / 2, row2Y + 3, 0xFFE0AAFF);

        boolean foodModeHov = mouseX >= col2X && mouseX < col2X + colW && mouseY >= row2Y && mouseY < row2Y + 15;
        g.fill(col2X, row2Y, col2X + colW, row2Y + 15, foodModeHov ? 0xDD543818 : 0xAA38220A);
        g.renderOutline(col2X, row2Y, colW, 15, foodModeHov ? 0xFFFFB74D : 0xFFB26A00);
        Component foodComp = Component.literal("🍗 ").append(GuiConfig.foodDisplayMode.getComponent());
        g.drawCenteredString(font, foodComp, col2X + colW / 2, row2Y + 3, 0xFFFFCC80);

        int row3Y = top + 50;
        boolean editHov = mouseX >= col1X && mouseX < col1X + colW && mouseY >= row3Y && mouseY < row3Y + 15;
        g.fill(col1X, row3Y, col1X + colW, row3Y + 15, editHov ? 0xDD225577 : 0xAA143347);
        g.renderOutline(col1X, row3Y, colW, 15, editHov ? 0xFF00E5FF : 0xFF4EE2EC);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.hud.move_btn"), col1X + colW / 2, row3Y + 3, 0xFFFFFFFF);

        boolean resetHudHov = mouseX >= col2X && mouseX < col2X + colW && mouseY >= row3Y && mouseY < row3Y + 15;
        g.fill(col2X, row3Y, col2X + colW, row3Y + 15, resetHudHov ? 0xAA553322 : 0x77331810);
        g.renderOutline(col2X, row3Y, colW, 15, resetHudHov ? 0xFFFF9944 : 0xFF884422);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.hud.reset_coords"), col2X + colW / 2, row3Y + 3, resetHudHov ? 0xFFFFD7A0 : 0xFFFFAA66);

        g.fill(left + 16, top + 68, left + GUI_WIDTH - 16, top + 69, 0x443D566E);
        g.drawString(font, Component.translatable("awakening_system.gui_config.hud.components_header"), left + 16, top + 72, 0xFFDFB76C);

        renderToggle(g, col1X, top + 84, colW, Component.translatable("awakening_system.gui_config.hud.toggle_status_card"), GuiConfig.showHudStatusCard, mouseX, mouseY);
        renderToggle(g, col2X, top + 84, colW, Component.translatable("awakening_system.gui_config.hud.toggle_skill_bar"), GuiConfig.showHudSkillBar, mouseX, mouseY);

        renderToggle(g, col1X, top + 100, colW, Component.translatable("awakening_system.gui_config.hud.toggle_appraisal"), GuiConfig.showHudAppraisal, mouseX, mouseY);
        renderToggle(g, col2X, top + 100, colW, Component.translatable("awakening_system.gui_config.hud.toggle_combo"), GuiConfig.showHudCombo, mouseX, mouseY);

        renderToggle(g, col1X, top + 116, colW, Component.translatable("awakening_system.gui_config.hud.toggle_constellation_popups"), GuiConfig.showConstellationPopups, mouseX, mouseY);
        renderToggle(g, col2X, top + 116, colW, Component.translatable("awakening_system.gui_config.hud.toggle_compact_constellations"), GuiConfig.compactConstellationPopups, mouseX, mouseY);

        renderToggle(g, col1X, top + 132, colW, Component.translatable("awakening_system.gui_config.hud.toggle_dokkaebi_scenario"), GuiConfig.showHudDokkaebiScenario, mouseX, mouseY);
        renderToggle(g, col2X, top + 132, colW, Component.translatable("awakening_system.gui_config.hud.toggle_vanilla_drumsticks"), !GuiConfig.shouldHideVanillaFood(), mouseX, mouseY);

        renderToggle(g, col1X, top + 148, colW, Component.translatable("awakening_system.gui_config.hud.toggle_food"), GuiConfig.isHudFoodBarVisible(), mouseX, mouseY);

        g.fill(left + 16, top + 166, left + GUI_WIDTH - 16, top + 167, 0x443D566E);

        var tipLines = font.split(Component.translatable("awakening_system.gui_config.hud.tip"), GUI_WIDTH - 36);
        int ty = top + 170;
        for (var line : tipLines) {
            g.drawCenteredString(font, line, left + GUI_WIDTH / 2, ty, 0x888888);
            ty += 10;
        }
    }

    private void renderMinimapTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.gui_config.minimap.header"), left + 16, top + 2, 0xFFDFB76C);

        int col1X = left + 16;
        int col2X = left + 176;
        int colW = 148;

        // Row 1: Minimap Display Toggle & Shape Button
        renderToggle(g, col1X, top + 16, colW, Component.translatable("awakening_system.gui_config.minimap.toggle"), GuiConfig.showMinimap, mouseX, mouseY);

        boolean isCirc = "CIRCULAR".equalsIgnoreCase(GuiConfig.minimapShape);
        boolean shapeHov = mouseX >= col2X && mouseX < col2X + colW && mouseY >= top + 16 && mouseY < top + 31;
        g.fill(col2X, top + 16, col2X + colW, top + 31, shapeHov ? 0xDD2A4D66 : 0x88152533);
        g.renderOutline(col2X, top + 16, colW, 15, shapeHov ? 0xFF00E5FF : 0xFF2A4255);
        Component shapeComp = Component.translatable(isCirc ? "awakening_system.gui_config.minimap.shape.circular" : "awakening_system.gui_config.minimap.shape.square");
        g.drawCenteredString(font, Component.literal("§7" + Component.translatable("awakening_system.gui_config.minimap.shape").getString() + " ").append(shapeComp), col2X + colW / 2, top + 19, isCirc ? 0xFF70E8FF : 0xFFFFD700);

        // Row 2: Minimap Scale
        int scaleY = top + 34;
        g.drawString(font, Component.translatable("awakening_system.gui_config.minimap.scale"), left + 16, scaleY + 2, 0xFFFFFFFF);
        int scalePercent = (int) Math.round(GuiConfig.minimapScale * 100);
        g.drawString(font, scalePercent + "%", left + 130, scaleY + 2, 0xFFFFE066);

        int minusX = left + 185;
        int plusX = left + 211;
        int resetScaleX = left + 237;

        boolean minusHov = mouseX >= minusX && mouseX < minusX + 22 && mouseY >= scaleY && mouseY < scaleY + 13;
        boolean plusHov = mouseX >= plusX && mouseX < plusX + 22 && mouseY >= scaleY && mouseY < scaleY + 13;
        boolean resetScaleHov = mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= scaleY && mouseY < scaleY + 13;

        g.fill(minusX, scaleY, minusX + 22, scaleY + 13, minusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, scaleY, 22, 13, minusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 11, scaleY + 2, 0xFFFFFFFF);

        g.fill(plusX, scaleY, plusX + 22, scaleY + 13, plusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, scaleY, 22, 13, plusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 11, scaleY + 2, 0xFFFFFFFF);

        g.fill(resetScaleX, scaleY, resetScaleX + 42, scaleY + 13, resetScaleHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, scaleY, 42, 13, resetScaleHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "100%", resetScaleX + 21, scaleY + 2, 0xFFEEEEEE);

        // Row 3: Radar Zoom
        int zoomY = top + 50;
        g.drawString(font, Component.translatable("awakening_system.gui_config.minimap.zoom"), left + 16, zoomY + 2, 0xFFFFFFFF);
        g.drawString(font, String.format(java.util.Locale.ROOT, "%.1fx", GuiConfig.minimapZoom), left + 130, zoomY + 2, 0xFFFFE066);

        boolean zMinusHov = mouseX >= minusX && mouseX < minusX + 22 && mouseY >= zoomY && mouseY < zoomY + 13;
        boolean zPlusHov = mouseX >= plusX && mouseX < plusX + 22 && mouseY >= zoomY && mouseY < zoomY + 13;
        boolean zResetHov = mouseX >= resetScaleX && mouseX < resetScaleX + 42 && mouseY >= zoomY && mouseY < zoomY + 13;

        g.fill(minusX, zoomY, minusX + 22, zoomY + 13, zMinusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, zoomY, 22, 13, zMinusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 11, zoomY + 2, 0xFFFFFFFF);

        g.fill(plusX, zoomY, plusX + 22, zoomY + 13, zPlusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, zoomY, 22, 13, zPlusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 11, zoomY + 2, 0xFFFFFFFF);

        g.fill(resetScaleX, zoomY, resetScaleX + 42, zoomY + 13, zResetHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, zoomY, 42, 13, zResetHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "1.0x", resetScaleX + 21, zoomY + 2, 0xFFEEEEEE);

        // Row 4: Cave Mode
        int caveY = top + 67;
        g.drawString(font, Component.translatable("awakening_system.gui_config.minimap.cave"), left + 16, caveY + 2, 0xFFFFFFFF);
        String cMode = GuiConfig.minimapCaveMode != null ? GuiConfig.minimapCaveMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
        String cKey = "awakening_system.gui_config.minimap.cave." + cMode.toLowerCase(java.util.Locale.ROOT);
        Component cComp = Component.translatable(cKey);
        int caveBtnX = left + 185;
        int caveBtnW = 94;
        boolean caveHov = mouseX >= caveBtnX && mouseX < caveBtnX + caveBtnW && mouseY >= caveY && mouseY < caveY + 13;
        g.fill(caveBtnX, caveY, caveBtnX + caveBtnW, caveY + 13, caveHov ? 0xDD4A3B66 : 0x882D2244);
        g.renderOutline(caveBtnX, caveY, caveBtnW, 13, caveHov ? 0xFFBD93F9 : 0xFF4A3B66);
        g.drawCenteredString(font, cComp, caveBtnX + caveBtnW / 2, caveY + 2, 0xFFBD93F9);

        // Row 4b: Day/Night Lighting Mode
        int lightY = top + 83;
        g.drawString(font, Component.translatable("awakening_system.gui_config.minimap.lighting"), left + 16, lightY + 2, 0xFFFFFFFF);
        String lMode = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
        String lKey = "awakening_system.gui_config.minimap.lighting." + lMode.toLowerCase(java.util.Locale.ROOT);
        Component lComp = Component.translatable(lKey);
        boolean lightHov = mouseX >= caveBtnX && mouseX < caveBtnX + caveBtnW && mouseY >= lightY && mouseY < lightY + 13;
        g.fill(caveBtnX, lightY, caveBtnX + caveBtnW, lightY + 13, lightHov ? 0xDD5A4A22 : 0x883D3014);
        g.renderOutline(caveBtnX, lightY, caveBtnW, 13, lightHov ? 0xFFFFD700 : 0xFF886611);
        g.drawCenteredString(font, lComp, caveBtnX + caveBtnW / 2, lightY + 2, 0xFFFFE066);

        // Separator
        g.fill(left + 16, top + 98, left + GUI_WIDTH - 16, top + 99, 0x443D566E);

        // Row 5: Hostile and Passive Entity Filters
        renderToggle(g, col1X, top + 102, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_hostiles"), GuiConfig.minimapShowHostiles, mouseX, mouseY);
        renderToggle(g, col2X, top + 102, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_passives"), GuiConfig.minimapShowPassives, mouseX, mouseY);

        // Row 6: Radar Entities & Waypoint icons toggles
        renderToggle(g, col1X, top + 117, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_entities"), GuiConfig.minimapShowEntities, mouseX, mouseY);
        renderToggle(g, col2X, top + 117, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_waypoints"), GuiConfig.minimapShowWaypoints, mouseX, mouseY);

        // Row 6.5: Dropped Items & Perception Radar
        renderToggle(g, col1X, top + 132, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_items"), GuiConfig.minimapShowItems, mouseX, mouseY);
        renderToggle(g, col2X, top + 132, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_perception"), GuiConfig.minimapPerceptionRadar, mouseX, mouseY);

        // Row 6.8: Danger Alert & Gates
        renderToggle(g, col1X, top + 147, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_danger"), GuiConfig.minimapDangerAlert, mouseX, mouseY);
        renderToggle(g, col2X, top + 147, colW, Component.translatable("awakening_system.gui_config.minimap.toggle_gates"), GuiConfig.minimapShowGates, mouseX, mouseY);

        // Row 6.9: Auto Death Waypoints
        renderToggle(g, col1X, top + 162, colW, Component.translatable("awakening_system.gui_config.minimap.auto_death_waypoints"), GuiConfig.autoDeathWaypoints, mouseX, mouseY);

        boolean extLoaded = com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.isExternalMapModLoaded();
        int yOffset = 0;
        if (extLoaded) {
            String extName = com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.getDetectedExternalMapName();
            int bannerY = top + 178;
            g.fill(left + 16, bannerY, left + GUI_WIDTH - 16, bannerY + 14, 0xEE2A1B05);
            g.renderOutline(left + 16, bannerY, GUI_WIDTH - 32, 14, 0xFFFFA500);
            String banner = "§6⚠ Mod detectado: §e" + extName + " §8| §7Minimapa auto-desactivado";
            g.drawString(font, banner, left + 20, bannerY + 3, 0xFFFFA500, false);

            int forceBtnX = left + GUI_WIDTH - 82;
            int forceBtnY = bannerY + 1;
            int forceBtnW = 62;
            int forceBtnH = 12;
            boolean forceHov = mouseX >= forceBtnX && mouseX < forceBtnX + forceBtnW && mouseY >= forceBtnY && mouseY < forceBtnY + forceBtnH;
            g.fill(forceBtnX, forceBtnY, forceBtnX + forceBtnW, forceBtnY + forceBtnH, GuiConfig.forceEnableMinimapWithExternalMods ? (forceHov ? 0xDD28864E : 0xAA1A5D3E) : (forceHov ? 0xDD6E2020 : 0x773B1111));
            g.renderOutline(forceBtnX, forceBtnY, forceBtnW, forceBtnH, GuiConfig.forceEnableMinimapWithExternalMods ? 0xFF50FA7B : 0xFFFF5555);
            g.drawCenteredString(font, GuiConfig.forceEnableMinimapWithExternalMods ? "§aForzar: ON" : "§cForzar: OFF", forceBtnX + forceBtnW / 2, forceBtnY + 2, 0xFFFFFFFF);
            yOffset = 16;
        }

        // Row 7: Interactive HUD Editor & World Map buttons
        int editBtnX = left + 16;
        int editBtnY = top + 179 + yOffset;
        int editBtnW = GUI_WIDTH - 32;
        int editBtnH = 13;
        boolean editHov = mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= editBtnY && mouseY < editBtnY + editBtnH;
        g.fill(editBtnX, editBtnY, editBtnX + editBtnW, editBtnY + editBtnH, editHov ? 0xDD225577 : 0xAA143347);
        g.renderOutline(editBtnX, editBtnY, editBtnW, editBtnH, editHov ? 0xFF00E5FF : 0xFF4EE2EC);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.minimap.btn_edit_pos"), editBtnX + editBtnW / 2, editBtnY + 2, 0xFFFFFFFF);

        int mapBtnY = top + 194 + yOffset;
        boolean mapHov = mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= mapBtnY && mouseY < mapBtnY + editBtnH;
        g.fill(editBtnX, mapBtnY, editBtnX + editBtnW, mapBtnY + editBtnH, mapHov ? 0xDD28864E : 0x881A5D3E);
        g.renderOutline(editBtnX, mapBtnY, editBtnW, editBtnH, mapHov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, Component.translatable("awakening_system.gui_config.minimap.btn_open_map"), editBtnX + editBtnW / 2, mapBtnY + 2, 0xFF50FA7B);
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

        int bg = active ? (hovered ? 0xDD28864E : 0xAA1A5D3E) : (hovered ? 0xAA6E2020 : 0x773B1111);
        int border = active ? (hovered ? 0xFF50FA7B : 0xFF28864E) : (hovered ? 0xFFFF6666 : 0xFF882222);

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

            if (mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20) {
                close();
                return true;
            }

            int tabW = (GUI_WIDTH - 28) / 4;
            int t0X = left + 14;
            int t1X = left + 14 + tabW;
            int t2X = left + 14 + tabW * 2;
            int t3X = left + 14 + tabW * 3;
            int tY = top + 30;
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
            if (mouseX >= t3X && mouseX < t3X + tabW && mouseY >= tY && mouseY < tY + tH) {
                currentTab = 3;
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
                if (mouseX >= col1X && mouseX < col1X + colW && mouseY >= contentY + 32 && mouseY < contentY + 47) {
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
                if (mouseX >= col2X && mouseX < col2X + colW && mouseY >= contentY + 32 && mouseY < contentY + 47) {
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
                if (mouseX >= col1X && mouseX < col1X + colW && mouseY >= contentY + 50 && mouseY < contentY + 65) {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    HudEditorScreen.open(this);
                    return true;
                }

                // Reset HUD Coords button (row 3 col 2)
                if (mouseX >= col2X && mouseX < col2X + colW && mouseY >= contentY + 50 && mouseY < contentY + 65) {
                    GuiConfig.resetHudPositions();
                    AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                    return true;
                }

                // Row 0 HUD toggles
                if (mouseY >= contentY + 84 && mouseY < contentY + 98) {
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
                if (mouseY >= contentY + 100 && mouseY < contentY + 114) {
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
                if (mouseY >= contentY + 116 && mouseY < contentY + 130) {
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
                if (mouseY >= contentY + 132 && mouseY < contentY + 146) {
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
                if (mouseY >= contentY + 148 && mouseY < contentY + 162) {
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
            } else if (currentTab == 3) {
                // Minimap & Radar tab
                // Row 1: Minimap Display Toggle & Shape
                if (mouseY >= contentY + 16 && mouseY < contentY + 31) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.showMinimap = !GuiConfig.showMinimap;
                        try {
                            dev.diorama.Settings s = dev.diorama.Settings.get();
                            if (s != null) {
                                s.minimap = GuiConfig.showMinimap;
                                s.save();
                            }
                        } catch (Throwable ignored) {}
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.minimapShape = "CIRCULAR".equalsIgnoreCase(GuiConfig.minimapShape) ? "SQUARE" : "CIRCULAR";
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                }

                // Row 2: Minimap Scale
                int minusX = left + 185;
                int plusX = left + 211;
                int resetScaleX = left + 237;
                int scaleY = contentY + 34;

                if (mouseY >= scaleY && mouseY < scaleY + 13) {
                    if (mouseX >= minusX && mouseX < minusX + 22) {
                        GuiConfig.minimapScale = (float) Math.max(0.5, Math.min(2.5, GuiConfig.minimapScale - 0.1));
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                    if (mouseX >= plusX && mouseX < plusX + 22) {
                        GuiConfig.minimapScale = (float) Math.max(0.5, Math.min(2.5, GuiConfig.minimapScale + 0.1));
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                    if (mouseX >= resetScaleX && mouseX < resetScaleX + 42) {
                        GuiConfig.minimapScale = 1.0F;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                        return true;
                    }
                }

                // Row 3: Radar Zoom
                int zoomY = contentY + 50;
                if (mouseY >= zoomY && mouseY < zoomY + 13) {
                    if (mouseX >= minusX && mouseX < minusX + 22) {
                        GuiConfig.minimapZoom = (float) Math.max(0.5, Math.min(3.0, GuiConfig.minimapZoom - 0.25));
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                    if (mouseX >= plusX && mouseX < plusX + 22) {
                        GuiConfig.minimapZoom = (float) Math.max(0.5, Math.min(3.0, GuiConfig.minimapZoom + 0.25));
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                    if (mouseX >= resetScaleX && mouseX < resetScaleX + 42) {
                        GuiConfig.minimapZoom = 1.0F;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                        return true;
                    }
                }

                // Row 4: Cave Mode cycle
                int caveY = contentY + 67;
                int caveBtnX = left + 185;
                int caveBtnW = 94;
                if (mouseX >= caveBtnX && mouseX < caveBtnX + caveBtnW && mouseY >= caveY && mouseY < caveY + 13) {
                    String cur = GuiConfig.minimapCaveMode != null ? GuiConfig.minimapCaveMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
                    GuiConfig.minimapCaveMode = switch (cur) {
                        case "AUTO" -> "ALWAYS";
                        case "ALWAYS" -> "NEVER";
                        default -> "AUTO";
                    };
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                    return true;
                }

                // Row 4b: Lighting Mode cycle
                int lightY = contentY + 83;
                if (mouseX >= caveBtnX && mouseX < caveBtnX + caveBtnW && mouseY >= lightY && mouseY < lightY + 13) {
                    String curL = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
                    GuiConfig.minimapLightingMode = switch (curL) {
                        case "AUTO" -> "DAY";
                        case "DAY" -> "NIGHT";
                        default -> "AUTO";
                    };
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }

                // Row 5: Hostile and Passive Entity Filters
                if (mouseY >= contentY + 102 && mouseY < contentY + 116) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.minimapShowHostiles = !GuiConfig.minimapShowHostiles;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.minimapShowPassives = !GuiConfig.minimapShowPassives;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }

                // Row 6: Entities and Waypoint toggles
                if (mouseY >= contentY + 117 && mouseY < contentY + 131) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.minimapShowEntities = !GuiConfig.minimapShowEntities;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.minimapShowWaypoints = !GuiConfig.minimapShowWaypoints;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }

                // Row 6.5: Items and Perception toggles
                if (mouseY >= contentY + 132 && mouseY < contentY + 146) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.minimapShowItems = !GuiConfig.minimapShowItems;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.minimapPerceptionRadar = !GuiConfig.minimapPerceptionRadar;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }

                // Row 6.8: Danger Alert and Gates
                if (mouseY >= contentY + 147 && mouseY < contentY + 161) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.minimapDangerAlert = !GuiConfig.minimapDangerAlert;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseX >= col2X && mouseX < col2X + colW) {
                        GuiConfig.minimapShowGates = !GuiConfig.minimapShowGates;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }

                // Row 6.9: Auto Death Waypoints
                if (mouseY >= contentY + 162 && mouseY < contentY + 176) {
                    if (mouseX >= col1X && mouseX < col1X + colW) {
                        GuiConfig.autoDeathWaypoints = !GuiConfig.autoDeathWaypoints;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }

                boolean extLoaded = com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.isExternalMapModLoaded();
                int yOffset = 0;
                if (extLoaded) {
                    int bannerY = contentY + 178;
                    int forceBtnX = left + GUI_WIDTH - 82;
                    int forceBtnY = bannerY + 1;
                    int forceBtnW = 62;
                    int forceBtnH = 12;
                    if (mouseX >= forceBtnX && mouseX < forceBtnX + forceBtnW && mouseY >= forceBtnY && mouseY < forceBtnY + forceBtnH) {
                        GuiConfig.forceEnableMinimapWithExternalMods = !GuiConfig.forceEnableMinimapWithExternalMods;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, GuiConfig.forceEnableMinimapWithExternalMods ? 1.2F : 0.8F);
                        return true;
                    }
                    yOffset = 16;
                }

                // Row 7: Interactive HUD Editor & World Map buttons
                int editBtnX = left + 16;
                int editBtnY = contentY + 179 + yOffset;
                int editBtnW = GUI_WIDTH - 32;
                int editBtnH = 13;
                if (mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= editBtnY && mouseY < editBtnY + editBtnH) {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    HudEditorScreen.open(this);
                    return true;
                }

                int mapBtnY = contentY + 194 + yOffset;
                if (mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= mapBtnY && mouseY < mapBtnY + editBtnH) {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    Minecraft.getInstance().setScreen(new dev.diorama.MapScreen());
                    return true;
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
