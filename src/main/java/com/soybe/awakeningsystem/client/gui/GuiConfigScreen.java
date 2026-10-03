package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de Configuración y Personalización de la Interfaz (GUI & HUD).
 * Permite cambiar la escala, habilitar/deshabilitar elementos y restablecer posiciones.
 */
public class GuiConfigScreen extends Screen {
    private static final int GUI_WIDTH = 340;
    private static final int GUI_HEIGHT = 276;

    private final Screen parentScreen;
    private int currentTab = 0; // 0 = Ventana Sistema, 1 = HUD en Juego

    public GuiConfigScreen(Screen parentScreen) {
        super(Component.literal("Configuración de Interfaz"));
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
        if (GuiConfig.enableBackgroundBlur) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        } else {
            graphics.fill(0, 0, width, height, 0x55060D15);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Fondo y marcos holográficos
        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFF4EE2EC);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF208111A);
        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF0E1F2E);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFF4EE2EC);

        // Título
        g.drawString(font, "§6⚙ §fCONFIGURACIÓN DE INTERFAZ & GUI", left + 14, top + 9, 0xFFDFB76C);

        // Botón cerrar X en esquina superior derecha
        boolean closeHover = mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20;
        g.fill(left + GUI_WIDTH - 22, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 14, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        // Pestañas superiores
        int tabW = (GUI_WIDTH - 28) / 2;
        int t0X = left + 14;
        int t1X = left + 14 + tabW;
        int tY = top + 32;
        int tH = 18;

        boolean hovT0 = mouseX >= t0X && mouseX < t0X + tabW && mouseY >= tY && mouseY < tY + tH;
        boolean hovT1 = mouseX >= t1X && mouseX < t1X + tabW && mouseY >= tY && mouseY < tY + tH;

        g.fill(t0X, tY, t0X + tabW, tY + tH, currentTab == 0 ? 0xCC1A384D : (hovT0 ? 0x881E2B38 : 0x550F1822));
        g.fill(t1X, tY, t1X + tabW, tY + tH, currentTab == 1 ? 0xCC1A384D : (hovT1 ? 0x881E2B38 : 0x550F1822));
        g.renderOutline(t0X, tY, tabW, tH, currentTab == 0 ? 0xFF4EE2EC : 0xFF2A4255);
        g.renderOutline(t1X, tY, tabW, tH, currentTab == 1 ? 0xFF4EE2EC : 0xFF2A4255);

        g.drawCenteredString(font, "✦ Ventana del Sistema", t0X + tabW / 2, tY + 5, currentTab == 0 ? 0xFFFFFFFF : 0xFFAAAAAA);
        g.drawCenteredString(font, "✦ HUD en Juego", t1X + tabW / 2, tY + 5, currentTab == 1 ? 0xFFFFFFFF : 0xFFAAAAAA);

        // Contenido según pestaña
        int contentY = top + 56;
        if (currentTab == 0) {
            renderSystemTab(g, left, contentY, mouseX, mouseY);
        } else {
            renderHudTab(g, left, contentY, mouseX, mouseY);
        }

        // Pie de ventana con botones de acción
        int btnY = top + GUI_HEIGHT - 26;
        int btnResetW = 120;
        int btnSaveW = 160;

        boolean resetHov = mouseX >= left + 14 && mouseX < left + 14 + btnResetW && mouseY >= btnY && mouseY < btnY + 20;
        g.fill(left + 14, btnY, left + 14 + btnResetW, btnY + 20, resetHov ? 0xAA6E2020 : 0x773B1111);
        g.renderOutline(left + 14, btnY, btnResetW, 20, resetHov ? 0xFFFF6666 : 0xFF882222);
        g.drawCenteredString(font, "⟲ Restablecer", left + 14 + btnResetW / 2, btnY + 6, resetHov ? 0xFFFFDDDD : 0xFFFFAAAA);

        int saveX = left + GUI_WIDTH - 14 - btnSaveW;
        boolean saveHov = mouseX >= saveX && mouseX < saveX + btnSaveW && mouseY >= btnY && mouseY < btnY + 20;
        g.fill(saveX, btnY, saveX + btnSaveW, btnY + 20, saveHov ? 0xCC1A5D3E : 0xAA0E3A26);
        g.renderOutline(saveX, btnY, btnSaveW, 20, saveHov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, "✔ Guardar y Volver", saveX + btnSaveW / 2, btnY + 6, 0xFF50FA7B);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderSystemTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        // 1. Escala de la ventana
        g.drawString(font, "§b✦ Escala de la GUI:", left + 16, top + 4, 0xFFFFFFFF);
        int scalePercent = (int) Math.round(GuiConfig.systemGuiScale * 100);
        String scaleStr = "§e" + scalePercent + "%";
        g.drawString(font, scaleStr, left + 130, top + 4, 0xFFFFD700);

        // Botones [-] y [+]
        int minusX = left + 175;
        int plusX = left + 205;
        boolean minusHov = mouseX >= minusX && mouseX < minusX + 24 && mouseY >= top + 1 && mouseY < top + 15;
        boolean plusHov = mouseX >= plusX && mouseX < plusX + 24 && mouseY >= top + 1 && mouseY < top + 15;

        g.fill(minusX, top + 1, minusX + 24, top + 15, minusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, top + 1, 24, 14, minusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 12, top + 4, 0xFFFFFFFF);

        g.fill(plusX, top + 1, plusX + 24, top + 15, plusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, top + 1, 24, 14, plusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 12, top + 4, 0xFFFFFFFF);

        // Botón rápido 100%
        int resetScaleX = left + 235;
        boolean resetScaleHov = mouseX >= resetScaleX && mouseX < resetScaleX + 44 && mouseY >= top + 1 && mouseY < top + 15;
        g.fill(resetScaleX, top + 1, resetScaleX + 44, top + 15, resetScaleHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, top + 1, 44, 14, resetScaleHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "100%", resetScaleX + 22, top + 4, 0xFFEEEEEE);

        // 2. Posición X / Y y botón Centrar
        g.drawString(font, "§b✦ Posición actual:", left + 16, top + 24, 0xFFFFFFFF);
        String posStr = "§7X: §f" + GuiConfig.systemGuiOffsetX + "  §7Y: §f" + GuiConfig.systemGuiOffsetY;
        g.drawString(font, posStr, left + 120, top + 24, 0xFFFFFFFF);

        int centerBtnX = left + 215;
        boolean centerHov = mouseX >= centerBtnX && mouseX < centerBtnX + 64 && mouseY >= top + 21 && mouseY < top + 35;
        g.fill(centerBtnX, top + 21, centerBtnX + 64, top + 35, centerHov ? 0xAA225577 : 0x66113344);
        g.renderOutline(centerBtnX, top + 21, 64, 14, centerHov ? 0xFF70E8FF : 0xFF225577);
        g.drawCenteredString(font, "⟲ Centrar", centerBtnX + 32, top + 24, 0xFF70E8FF);

        // Separador
        g.fill(left + 16, top + 42, left + GUI_WIDTH - 16, top + 43, 0x443D566E);

        // 3. Toggles de elementos
        renderToggle(g, left + 16, top + 48, "Mostrar Modelo 3D del Jugador", GuiConfig.showPlayer3DModel, mouseX, mouseY);
        renderToggle(g, left + 16, top + 69, "Pestañas Laterales de Dimensiones", GuiConfig.showDimensionSideTabs, mouseX, mouseY);
        renderToggle(g, left + 16, top + 90, "Movimiento Libre (Arrastrar ventana)", GuiConfig.freeMoveEnabled, mouseX, mouseY);
        renderToggle(g, left + 16, top + 111, "Efectos de Sonido de la Interfaz (SFX)", GuiConfig.uiSoundsEnabled, mouseX, mouseY);
        renderToggle(g, left + 16, top + 132, "Desenfoque de Fondo (Blur del mundo)", GuiConfig.enableBackgroundBlur, mouseX, mouseY);

        // Guía interactiva
        g.drawCenteredString(font, "§8Tip: Arrastra la barra de título o usa Ctrl + Rueda para escalar", left + GUI_WIDTH / 2, top + 154, 0x888888);
    }

    private void renderHudTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        // 1. Escala del HUD
        g.drawString(font, "§b✦ Escala HUD:", left + 16, top + 1, 0xFFFFFFFF);
        int scalePercent = (int) Math.round(GuiConfig.hudScale * 100);
        String scaleStr = "§e" + scalePercent + "%";
        g.drawString(font, scaleStr, left + 120, top + 1, 0xFFFFD700);

        int minusX = left + 175;
        int plusX = left + 205;
        boolean minusHov = mouseX >= minusX && mouseX < minusX + 24 && mouseY >= top && mouseY < top + 13;
        boolean plusHov = mouseX >= plusX && mouseX < plusX + 24 && mouseY >= top && mouseY < top + 13;

        g.fill(minusX, top, minusX + 24, top + 13, minusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, top, 24, 13, minusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 12, top + 2, 0xFFFFFFFF);

        g.fill(plusX, top, plusX + 24, top + 13, plusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, top, 24, 13, plusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 12, top + 2, 0xFFFFFFFF);

        int resetScaleX = left + 235;
        boolean resetScaleHov = mouseX >= resetScaleX && mouseX < resetScaleX + 44 && mouseY >= top && mouseY < top + 13;
        g.fill(resetScaleX, top, resetScaleX + 44, top + 13, resetScaleHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, top, 44, 13, resetScaleHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "100%", resetScaleX + 22, top + 2, 0xFFEEEEEE);

        // 2. Escala de Constelaciones (Pop-ups de donación)
        int cTop = top + 16;
        g.drawString(font, "§b✦ Escala Pop-ups:", left + 16, cTop + 1, 0xFFFFFFFF);
        int cScalePercent = (int) Math.round(GuiConfig.constellationNotifScale * 100);
        String cScaleStr = "§e" + cScalePercent + "%";
        g.drawString(font, cScaleStr, left + 120, cTop + 1, 0xFFFFD700);

        boolean cMinusHov = mouseX >= minusX && mouseX < minusX + 24 && mouseY >= cTop && mouseY < cTop + 13;
        boolean cPlusHov = mouseX >= plusX && mouseX < plusX + 24 && mouseY >= cTop && mouseY < cTop + 13;

        g.fill(minusX, cTop, minusX + 24, cTop + 13, cMinusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(minusX, cTop, 24, 13, cMinusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "-", minusX + 12, cTop + 2, 0xFFFFFFFF);

        g.fill(plusX, cTop, plusX + 24, cTop + 13, cPlusHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(plusX, cTop, 24, 13, cPlusHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "+", plusX + 12, cTop + 2, 0xFFFFFFFF);

        boolean cResetHov = mouseX >= resetScaleX && mouseX < resetScaleX + 44 && mouseY >= cTop && mouseY < cTop + 13;
        g.fill(resetScaleX, cTop, resetScaleX + 44, cTop + 13, cResetHov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(resetScaleX, cTop, 44, 13, cResetHov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "85%", resetScaleX + 22, cTop + 2, 0xFFEEEEEE);

        // 3. Botón Destacado: Mover Elementos con el Ratón (Modo Editor)
        int editBtnX = left + 16;
        int editBtnY = top + 33;
        int editBtnW = GUI_WIDTH - 32;
        int editBtnH = 16;
        boolean editHov = mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= editBtnY && mouseY < editBtnY + editBtnH;
        g.fill(editBtnX, editBtnY, editBtnX + editBtnW, editBtnY + editBtnH, editHov ? 0xDD225577 : 0xAA143347);
        g.renderOutline(editBtnX, editBtnY, editBtnW, editBtnH, editHov ? 0xFF00E5FF : 0xFF4EE2EC);
        g.drawCenteredString(font, "§e🖱 MOVER ELEMENTOS DEL HUD CON EL RATÓN", editBtnX + editBtnW / 2, editBtnY + 4, 0xFFFFFFFF);

        // 4. Selector de Modo de Vida (Tarjeta Holo, Vanilla, Barras Compactas)
        int modeY = top + 52;
        g.drawString(font, "§b✦ Modo de Salud:", left + 16, modeY + 3, 0xFFFFFFFF);
        int modeBtnX = left + 125;
        int modeBtnW = GUI_WIDTH - 32 - 109;
        int modeBtnH = 15;
        boolean modeHov = mouseX >= modeBtnX && mouseX < modeBtnX + modeBtnW && mouseY >= modeY && mouseY < modeY + modeBtnH;
        g.fill(modeBtnX, modeY, modeBtnX + modeBtnW, modeY + modeBtnH, modeHov ? 0xDD3A2454 : 0xAA241638);
        g.renderOutline(modeBtnX, modeY, modeBtnW, modeBtnH, modeHov ? 0xFFB388FF : 0xFF7D5BA6);
        g.drawCenteredString(font, GuiConfig.healthDisplayMode.getLabel(), modeBtnX + modeBtnW / 2, modeY + 3, 0xFFE0AAFF);

        // 5. Posiciones Libres: Botón Reset
        int resetY = top + 70;
        g.drawString(font, "§7Posiciones Libres:", left + 16, resetY + 3, 0xFFAAAAAA);
        int resetHudX = left + 125;
        int resetHudW = GUI_WIDTH - 32 - 109;
        int resetHudH = 15;
        boolean resetHudHov = mouseX >= resetHudX && mouseX < resetHudX + resetHudW && mouseY >= resetY && mouseY < resetY + resetHudH;
        g.fill(resetHudX, resetY, resetHudX + resetHudW, resetY + resetHudH, resetHudHov ? 0xAA553322 : 0x77331810);
        g.renderOutline(resetHudX, resetY, resetHudW, resetHudH, resetHudHov ? 0xFFFF9944 : 0xFF884422);
        g.drawCenteredString(font, "⟲ Resetear Coordenadas", resetHudX + resetHudW / 2, resetY + 3, resetHudHov ? 0xFFFFD7A0 : 0xFFFFAA66);

        // Separador
        g.fill(left + 16, top + 88, left + GUI_WIDTH - 16, top + 89, 0x443D566E);

        // 6. Toggles de Componentes
        renderToggle(g, left + 16, top + 92, "Tarjeta de Estado (Vida/Maná)", GuiConfig.showHudStatusCard, mouseX, mouseY);
        renderToggle(g, left + 16, top + 110, "Barra de Habilidades Activas", GuiConfig.showHudSkillBar, mouseX, mouseY);
        renderToggle(g, left + 16, top + 128, "Visor de Análisis Holográfico", GuiConfig.showHudAppraisal, mouseX, mouseY);
        renderToggle(g, left + 16, top + 146, "Contador de Racha / Combo", GuiConfig.showHudCombo, mouseX, mouseY);
        renderToggle(g, left + 16, top + 164, "Pop-up Constelación Compacto", GuiConfig.compactConstellationPopups, mouseX, mouseY);

        // Guía interactiva
        g.drawCenteredString(font, "§8Tip: Haz clic en el botón superior para arrastrar con el mouse", left + GUI_WIDTH / 2, top + 184, 0x888888);
    }

    private void renderToggle(GuiGraphics g, int x, int y, String label, boolean active, int mouseX, int mouseY) {
        int toggleW = 54;
        int toggleH = 16;
        int toggleX = x + GUI_WIDTH - 32 - toggleW;

        boolean hovered = mouseX >= toggleX && mouseX < toggleX + toggleW && mouseY >= y && mouseY < y + toggleH;

        g.drawString(font, label, x, y + 4, 0xFFE0E0E0);

        int bg = active ? (hovered ? 0xDD28864E : 0xBB1A5D3E) : (hovered ? 0xDD444444 : 0x992B2B2B);
        int border = active ? 0xFF50FA7B : 0xFF666666;

        g.fill(toggleX, y, toggleX + toggleW, y + toggleH, bg);
        g.renderOutline(toggleX, y, toggleW, toggleH, border);

        String text = active ? "✔ SÍ" : "✕ NO";
        int textCol = active ? 0xFF50FA7B : 0xFFAAAAAA;
        g.drawCenteredString(font, text, toggleX + toggleW / 2, y + 4, textCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - GUI_WIDTH) / 2;
            int top = (height - GUI_HEIGHT) / 2;

            // Botón cerrar X
            if (mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20) {
                close();
                return true;
            }

            // Pestañas
            int tabW = (GUI_WIDTH - 28) / 2;
            int t0X = left + 14;
            int t1X = left + 14 + tabW;
            int tY = top + 32;
            int tH = 18;

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

            // Botón Restablecer
            int btnY = top + GUI_HEIGHT - 26;
            int btnResetW = 120;
            if (mouseX >= left + 14 && mouseX < left + 14 + btnResetW && mouseY >= btnY && mouseY < btnY + 20) {
                GuiConfig.resetDefaults();
                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                return true;
            }

            // Botón Guardar y Volver
            int btnSaveW = 160;
            int saveX = left + GUI_WIDTH - 14 - btnSaveW;
            if (mouseX >= saveX && mouseX < saveX + btnSaveW && mouseY >= btnY && mouseY < btnY + 20) {
                GuiConfig.save();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                close();
                return true;
            }

            // Clics específicos de pestañas
            int contentY = top + 56;
            if (currentTab == 0) {
                // Escala: Menos, Más, 100%
                int minusX = left + 175;
                int plusX = left + 205;
                int resetScaleX = left + 235;

                if (mouseX >= minusX && mouseX < minusX + 24 && mouseY >= contentY + 1 && mouseY < contentY + 15) {
                    GuiConfig.systemGuiScale = (float) Math.max(0.5, Math.min(1.8, GuiConfig.systemGuiScale - 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= plusX && mouseX < plusX + 24 && mouseY >= contentY + 1 && mouseY < contentY + 15) {
                    GuiConfig.systemGuiScale = (float) Math.max(0.5, Math.min(1.8, GuiConfig.systemGuiScale + 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= resetScaleX && mouseX < resetScaleX + 44 && mouseY >= contentY + 1 && mouseY < contentY + 15) {
                    GuiConfig.systemGuiScale = 1.0F;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }

                // Botón Centrar
                int centerBtnX = left + 215;
                if (mouseX >= centerBtnX && mouseX < centerBtnX + 64 && mouseY >= contentY + 21 && mouseY < contentY + 35) {
                    GuiConfig.systemGuiOffsetX = 0;
                    GuiConfig.systemGuiOffsetY = 0;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.4F);
                    return true;
                }

                // Toggles Sistema
                int toggleW = 54;
                int toggleH = 16;
                int toggleX = left + 16 + GUI_WIDTH - 32 - toggleW;

                if (mouseX >= toggleX && mouseX < toggleX + toggleW) {
                    if (mouseY >= contentY + 48 && mouseY < contentY + 48 + toggleH) {
                        GuiConfig.showPlayer3DModel = !GuiConfig.showPlayer3DModel;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 69 && mouseY < contentY + 69 + toggleH) {
                        GuiConfig.showDimensionSideTabs = !GuiConfig.showDimensionSideTabs;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 90 && mouseY < contentY + 90 + toggleH) {
                        GuiConfig.freeMoveEnabled = !GuiConfig.freeMoveEnabled;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 111 && mouseY < contentY + 111 + toggleH) {
                        GuiConfig.uiSoundsEnabled = !GuiConfig.uiSoundsEnabled;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 132 && mouseY < contentY + 132 + toggleH) {
                        GuiConfig.enableBackgroundBlur = !GuiConfig.enableBackgroundBlur;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                }
            } else {
                // 1. Escala HUD
                int minusX = left + 175;
                int plusX = left + 205;
                int resetScaleX = left + 235;

                if (mouseX >= minusX && mouseX < minusX + 24 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.hudScale = (float) Math.max(0.5, Math.min(2.0, GuiConfig.hudScale - 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= plusX && mouseX < plusX + 24 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.hudScale = (float) Math.max(0.5, Math.min(2.0, GuiConfig.hudScale + 0.05));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= resetScaleX && mouseX < resetScaleX + 44 && mouseY >= contentY && mouseY < contentY + 13) {
                    GuiConfig.hudScale = 1.0F;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }

                // 2. Escala Constelaciones
                int cTop = contentY + 16;
                if (mouseX >= minusX && mouseX < minusX + 24 && mouseY >= cTop && mouseY < cTop + 13) {
                    GuiConfig.constellationNotifScale = (float) Math.max(0.4F, Math.min(1.5F, GuiConfig.constellationNotifScale - 0.05F));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= plusX && mouseX < plusX + 24 && mouseY >= cTop && mouseY < cTop + 13) {
                    GuiConfig.constellationNotifScale = (float) Math.max(0.4F, Math.min(1.5F, GuiConfig.constellationNotifScale + 0.05F));
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                if (mouseX >= resetScaleX && mouseX < resetScaleX + 44 && mouseY >= cTop && mouseY < cTop + 13) {
                    GuiConfig.constellationNotifScale = 0.85F;
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }

                // 3. Botón Mover Elementos con el Ratón (Modo Editor)
                int editBtnX = left + 16;
                int editBtnY = contentY + 33;
                int editBtnW = GUI_WIDTH - 32;
                int editBtnH = 16;
                if (mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= editBtnY && mouseY < editBtnY + editBtnH) {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    HudEditorScreen.open(this);
                    return true;
                }

                // 4. Ciclo de Modo de Salud
                int modeBtnX = left + 125;
                int modeBtnY = contentY + 52;
                int modeBtnW = GUI_WIDTH - 32 - 109;
                int modeBtnH = 15;
                if (mouseX >= modeBtnX && mouseX < modeBtnX + modeBtnW && mouseY >= modeBtnY && mouseY < modeBtnY + modeBtnH) {
                    GuiConfig.healthDisplayMode = switch (GuiConfig.healthDisplayMode) {
                        case CARD -> GuiConfig.HealthDisplayMode.VANILLA;
                        case COMPACT_BAR -> GuiConfig.HealthDisplayMode.CARD;
                        case VANILLA -> GuiConfig.HealthDisplayMode.COMPACT_BAR;
                    };
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                    return true;
                }

                // 5. Resetear Coordenadas del HUD
                int resetHudX = left + 125;
                int resetHudY = contentY + 70;
                int resetHudW = GUI_WIDTH - 32 - 109;
                int resetHudH = 15;
                if (mouseX >= resetHudX && mouseX < resetHudX + resetHudW && mouseY >= resetHudY && mouseY < resetHudY + resetHudH) {
                    GuiConfig.resetHudPositions();
                    AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                    return true;
                }

                // 6. Toggles HUD
                int toggleW = 54;
                int toggleH = 16;
                int toggleX = left + 16 + GUI_WIDTH - 32 - toggleW;

                if (mouseX >= toggleX && mouseX < toggleX + toggleW) {
                    if (mouseY >= contentY + 92 && mouseY < contentY + 92 + toggleH) {
                        GuiConfig.showHudStatusCard = !GuiConfig.showHudStatusCard;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 110 && mouseY < contentY + 110 + toggleH) {
                        GuiConfig.showHudSkillBar = !GuiConfig.showHudSkillBar;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 128 && mouseY < contentY + 128 + toggleH) {
                        GuiConfig.showHudAppraisal = !GuiConfig.showHudAppraisal;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 146 && mouseY < contentY + 146 + toggleH) {
                        GuiConfig.showHudCombo = !GuiConfig.showHudCombo;
                        GuiConfig.save();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                        return true;
                    }
                    if (mouseY >= contentY + 164 && mouseY < contentY + 164 + toggleH) {
                        GuiConfig.compactConstellationPopups = !GuiConfig.compactConstellationPopups;
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
        if (keyCode == 256) { // ESC
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
