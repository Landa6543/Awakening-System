package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.OpenTowerMissionBoardPayload;
import com.soybe.awakeningsystem.TowerMissionActionPayload;
import com.soybe.awakeningsystem.tower.TowerDailyMissionManager;
import com.soybe.awakeningsystem.tower.TowerMissionTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Pantalla diegética del Tablón Diario de Misiones de la Torre del Ranker (Sección 14.3).
 * Presenta 10 contratos repartidos orgánicamente por el tablón (no en fila rígida),
 * con tachuelas metálicas en posiciones variadas.
 * Si el jugador no ha ingresado a la torre, los contratos aparecen como CLASIFICADOS/BLOQUEADOS
 * sin revelar nombres de pisos. Tipografía 100% nítida sin negrita artificial ni sombras borrosas.
 */
public final class TowerMissionBoardScreen extends Screen {

    private static final ResourceLocation BOARD_BG =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/tower_mission_board_bg.png");
    private static final ResourceLocation PARCHMENT_NOTE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/parchment_note.png");
    private static final ResourceLocation PARCHMENT_DOSSIER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/parchment_dossier.png");
    private static final ResourceLocation THUMBTACK =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/thumbtack.png");
    private static final ResourceLocation RED_LOCKED_SEAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/red_locked_seal.png");

    public static final int NOTE_W = 96;
    public static final int NOTE_H = 104;

    private static final class NotePlacement {
        final int relX;
        final int relY;
        final int tackOffX;
        final int tackOffY;

        NotePlacement(int relX, int relY, int tackOffX, int tackOffY) {
            this.relX = relX;
            this.relY = relY;
            this.tackOffX = tackOffX;
            this.tackOffY = tackOffY;
        }
    }

    // 10 notas repartidas orgánicamente por el tablón (posiciones escalonadas y dispersas)
    private static final NotePlacement[] SCATTERED_PLACEMENTS = new NotePlacement[] {
            // Zona superior (Contratos iniciales y medios)
            new NotePlacement(20, 46, -3, 2),    // Slot 0
            new NotePlacement(126, 58, 2, 3),    // Slot 1
            new NotePlacement(232, 42, -1, 1),   // Slot 2
            new NotePlacement(338, 56, 3, 2),    // Slot 3
            new NotePlacement(444, 48, -2, 2),   // Slot 4

            // Zona inferior (Contratos avanzados y élite)
            new NotePlacement(18, 172, 2, 2),    // Slot 5
            new NotePlacement(128, 160, -2, 3),  // Slot 6
            new NotePlacement(236, 176, 1, 1),   // Slot 7
            new NotePlacement(336, 162, -3, 2),  // Slot 8
            new NotePlacement(446, 174, 2, 2)    // Slot 9
    };

    private final OpenTowerMissionBoardPayload payload;
    private int selectedSlot = -1; // -1 = Tablero general, 0..9 = Expediente abierto

    public TowerMissionBoardScreen(OpenTowerMissionBoardPayload payload) {
        super(Component.translatableWithFallback("awakening_system.screen.tower_mission_board.title", "Tablón de Misiones de la Torre"));
        this.payload = payload;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            if (selectedSlot != -1) {
                selectedSlot = -1;
                playPageSound();
                return true;
            }
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 580.0F;
        float scaleY = (float) this.height / 360.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.40F, scale));
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

    private int getSlotX(int left, int slot) {
        return left + SCATTERED_PLACEMENTS[slot % SCATTERED_PLACEMENTS.length].relX;
    }

    private int getSlotY(int top, int slot) {
        return top + SCATTERED_PLACEMENTS[slot % SCATTERED_PLACEMENTS.length].relY;
    }

    private void drawCleanCentered(GuiGraphics g, String text, int centerX, int y, int color) {
        int w = this.font.width(text);
        g.drawString(this.font, text, centerX - w / 2, y, color, false);
    }

    private void drawAutoScaledCleanCentered(GuiGraphics g, String text, int centerX, int y, int color, int maxW) {
        int textW = this.font.width(text);
        if (textW <= maxW) {
            drawCleanCentered(g, text, centerX, y, color);
            return;
        }
        float scale = (float) maxW / (float) textW;
        g.pose().pushPose();
        g.pose().translate(centerX, y, 0);
        g.pose().scale(scale, scale, 1.0F);
        g.drawString(this.font, text, -textW / 2, 0, color, false);
        g.pose().popPose();
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

        int boardW = 560;
        int boardH = 340;
        int left = (this.width - boardW) / 2;
        int top = (this.height - boardH) / 2;

        // 1. Fondo de madera con marco de pizarra profunda y esquineros dorados completos
        g.blit(BOARD_BG, left, top, 0, 0, boardW, boardH, 560, 340);

        // 2. Placa Superior
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.title").getString(), left + boardW / 2, top + 11, 0xFFFFD700);

        boolean hasEntered = payload.playerSavedFloor() > 0;
        String statusStr;
        if (!hasEntered) {
            statusStr = Component.translatable("awakening_system.tower_board.status_unregistered", payload.currentDay() + 1).getString();
            drawCleanCentered(g, statusStr, left + boardW / 2, top + 23, 0xFFE08888);
        } else {
            statusStr = Component.translatable("awakening_system.tower_board.status_registered", payload.currentDay() + 1, payload.playerSavedFloor()).getString();
            drawCleanCentered(g, statusStr, left + boardW / 2, top + 23, 0xFFD0C0A0);
        }

        int[] dailyIndices = payload.dailyIndices();

        // 3. Render de las 10 notas dispersas o Expediente modal
        if (selectedSlot == -1) {
            for (int i = 0; i < TowerDailyMissionManager.DAILY_OFFERS_COUNT; i++) {
                int nX = getSlotX(left, i);
                int nY = getSlotY(top, i);

                boolean isHovered = (mX >= nX && mX <= nX + NOTE_W && mY >= nY && mY <= nY + NOTE_H);
                renderParchmentNote(g, i, nX, nY, NOTE_W, NOTE_H, dailyIndices != null && i < dailyIndices.length ? dailyIndices[i] : -1, isHovered, hasEntered);
            }

            // Barra Inferior de Estado de Misión Activa
            renderActiveMissionStatusBar(g, left, top + 296, boardW, mX, mY, hasEntered);
        } else {
            // the whole board dims behind the dossier, header included, so nothing peeks out from under it
            g.fill(left, top, left + boardW, top + boardH, 0xEE000000);

            if (selectedSlot >= 0 && selectedSlot < TowerDailyMissionManager.DAILY_OFFERS_COUNT && dailyIndices != null && selectedSlot < dailyIndices.length) {
                renderDossierModal(g, left, top, boardW, boardH, dailyIndices[selectedSlot], selectedSlot, mX, mY, hasEntered);
            }
        }

        g.pose().popPose();
    }

    private void renderParchmentNote(GuiGraphics g, int slot, int x, int y, int w, int h, int globalIdx, boolean hovered, boolean hasEntered) {
        if (globalIdx < 0 || globalIdx >= TowerDailyMissionManager.ALL_MISSIONS.size()) return;
        TowerMissionTemplate item = TowerDailyMissionManager.ALL_MISSIONS.get(globalIdx);

        boolean isLocked = !hasEntered || item.floor() > payload.playerSavedFloor();
        boolean isActive = hasEntered && payload.hasActiveMission() && payload.activeIndex() == globalIdx;
        boolean isCompleted = hasEntered && payload.completedToday() != null && slot < payload.completedToday().length && payload.completedToday()[slot];

        // Resplandor de selección suave si el cursor está encima
        if (hovered) {
            g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x66FFDD66);
        }

        // Textura base del pergamino
        g.blit(PARCHMENT_NOTE, x, y, 0, 0, w, h, 150, 110);

        // Tachuela superior en posición artesanal variable
        NotePlacement p = SCATTERED_PLACEMENTS[slot % SCATTERED_PLACEMENTS.length];
        g.blit(THUMBTACK, x + (w - 14) / 2 + p.tackOffX, y + p.tackOffY, 0, 0, 14, 14, 24, 24);

        int cx = x + w / 2;

        // Si NO ha ingresado a la torre, no muestra nombres de piso ni títulos
        if (!hasEntered) {
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.classified_tag").getString(), cx, y + 17, 0xFF8A1515);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.sealed_contract").getString(), cx, y + 30, 0xFF2A1A0A);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.no_tower_record").getString(), cx, y + 43, 0xFF553820);
            drawCleanCentered(g, "??? C | ??? XP", cx, y + 56, 0xFF7A5830);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.unknown_gems").getString(), cx, y + 68, 0xFF4A6B3A);

            // Botón bloqueado limpio
            g.fill(x + 5, y + 83, x + w - 5, y + 99, 0xDD7A1515);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_locked").getString(), cx, y + 87, 0xFFFFFFFF);
            return;
        }

        // Si el jugador YA ingresó a la torre:
        int floorColor = isLocked ? 0xFF8A1515 : (item.floor() >= 50 ? 0xFF5A1C6A : (item.floor() >= 10 ? 0xFF154870 : 0xFF1B5E20));
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.floor_tag", item.floor()).getString(), cx, y + 17, floorColor);

        // Título limpio sin negrita
        String title = trMissionTitle(item);
        if (this.font.width(title) > w - 10) {
            title = this.font.plainSubstrByWidth(title, w - 16) + "..";
        }
        drawCleanCentered(g, title, cx, y + 30, 0xFF1A1A1A);

        // Objetivo
        String target = trMissionTarget(item) + " x" + item.targetCount();
        if (this.font.width(target) > w - 10) {
            target = this.font.plainSubstrByWidth(target, w - 16) + "..";
        }
        drawCleanCentered(g, target, cx, y + 43, 0xFF443322);

        // Recompensas garantizadas
        String rew1 = formatRewardAmount(item.coins()) + " C | " + formatRewardAmount(item.exp()) + " XP";
        drawAutoScaledCleanCentered(g, rew1, cx, y + 56, 0xFF8A5A10, w - 12);

        String rew2 = Component.translatable("awakening_system.tower_board.crystals_short", item.crystalReward().getCount()).getString();
        drawAutoScaledCleanCentered(g, rew2, cx, y + 68, 0xFF1E6E28, w - 12);

        // Insignia inferior
        int btnX = x + 5;
        int btnY = y + 83;
        int btnW = w - 10;
        int btnH = 16;

        if (isLocked) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xDD7A1515);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_locked").getString(), cx, btnY + 4, 0xFFFFFFFF);
        } else if (isActive) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xDD154870);
            drawCleanCentered(g, "(" + payload.activeProgress() + "/" + payload.activeNeeded() + ")", cx, btnY + 4, 0xFFFFFFFF);
        } else if (isCompleted) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xDD1B5E20);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_completed").getString(), cx, btnY + 4, 0xFFFFFFFF);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hovered ? 0xFFC08020 : 0xDD8A5816);
            drawCleanCentered(g, Component.translatable(hovered ? "awakening_system.tower_board.btn_inspect" : "awakening_system.tower_board.btn_available").getString(), cx, btnY + 4, 0xFFFFFFFF);
        }
    }

    private void renderActiveMissionStatusBar(GuiGraphics g, int left, int y, int boardW, int mX, int mY, boolean hasEntered) {
        int barX = left + 24;
        int barW = boardW - 48;

        if (!hasEntered) {
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.unregistered_hint").getString(), barX + barW / 2, y + 8, 0xFFE0A080);
            return;
        }

        if (payload.hasActiveMission() && payload.activeIndex() >= 0 && payload.activeIndex() < TowerDailyMissionManager.ALL_MISSIONS.size()) {
            TowerMissionTemplate activeTemplate = TowerDailyMissionManager.ALL_MISSIONS.get(payload.activeIndex());

            String text = Component.translatable("awakening_system.tower_board.active_bar", trMissionTitle(activeTemplate), activeTemplate.floor(), payload.activeProgress(), payload.activeNeeded()).getString();
            g.drawString(this.font, text, barX + 12, y + 8, 0xFFFFFFFF, false);

            if (payload.activeReadyToClaim()) {
                int btnW = 140;
                int btnX = barX + barW - btnW - 6;
                int btnY = y + 4;
                int btnH = 18;
                boolean hov = (selectedSlot == -1) && (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH);

                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xFF00C050 : 0xFF008835);
                drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_claim").getString(), btnX + btnW / 2, btnY + 5, 0xFFFFFFFF);
            }
        } else {
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.no_active_hint").getString(), barX + barW / 2, y + 8, 0xFFFFE090);
        }
    }

    private void renderDossierModal(GuiGraphics g, int boardX, int boardY, int boardW, int boardH, int globalIdx, int slotIdx, int mX, int mY, boolean hasEntered) {
        if (globalIdx < 0 || globalIdx >= TowerDailyMissionManager.ALL_MISSIONS.size()) return;
        TowerMissionTemplate item = TowerDailyMissionManager.ALL_MISSIONS.get(globalIdx);

        int dosW = 310;
        int dosH = 290;
        int dosX = (this.width - dosW) / 2;
        int dosY = (this.height - dosH) / 2;

        // Hoja del expediente
        // the whole parchment, scaled to the sheet (it used to be cropped, cutting its edges off)
        g.blit(PARCHMENT_DOSSIER, dosX, dosY, dosW, dosH, 0.0F, 0.0F, 320, 330, 320, 330);

        // Botón de Cerrar (X) arriba a la derecha
        int closeX = dosX + dosW - 24;
        int closeY = dosY + 12;
        boolean closeHov = mX >= closeX && mX <= closeX + 16 && mY >= closeY && mY <= closeY + 16;
        g.fill(closeX, closeY, closeX + 16, closeY + 16, closeHov ? 0xFFAA2211 : 0x88552211);
        drawCleanCentered(g, "X", closeX + 8, closeY + 4, 0xFFFFFFFF);

        int cx = dosX + dosW / 2;

        if (!hasEntered) {
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_classified").getString(), cx, dosY + 15, 0xFF8A1515);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_unregistered").getString(), cx, dosY + 28, 0xFF4A3018);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_restricted").getString(), cx, dosY + 42, 0xFF201205);

            g.fill(dosX + 22, dosY + 54, dosX + dosW - 22, dosY + 55, 0xFF8B6B4A);

            String lore = Component.translatable("awakening_system.tower_board.dossier_unregistered_desc").getString();
            List<FormattedCharSequence> lines = this.font.split(Component.literal(lore), dosW - 44);
            int curY = dosY + 70;
            for (FormattedCharSequence line : lines) {
                g.drawString(this.font, line, dosX + 22, curY, 0xFF2C1E12, false);
                curY += 12;
            }

            int btnW = 230;
            int btnH = 22;
            int btnX = dosX + (dosW - btnW) / 2;
            int btnY = dosY + 248;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF7A1515);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_requires_tower").getString(), cx, btnY + 7, 0xFFFFFFFF);
            return;
        }

        // Jugador registrado
        boolean isLocked = item.floor() > payload.playerSavedFloor();
        boolean isActive = payload.hasActiveMission() && payload.activeIndex() == globalIdx;
        boolean isCompleted = payload.completedToday() != null && slotIdx < payload.completedToday().length && payload.completedToday()[slotIdx];
        boolean hov = mX >= (dosX + 40) && mX <= (dosX + dosW - 40) && mY >= (dosY + 252) && mY <= (dosY + 276);

        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_title").getString(), cx, dosY + 14, 0xFF8A1515);
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_floor", item.floor()).getString(), cx, dosY + 26, 0xFF4A3018);
        drawCleanCentered(g, trMissionTitle(item), cx, dosY + 40, 0xFF201205);

        // Línea divisoria
        g.fill(dosX + 22, dosY + 52, dosX + dosW - 22, dosY + 53, 0xFF8B6B4A);

        // Lore narrativo
        List<FormattedCharSequence> lines = this.font.split(Component.literal(trMissionNarrative(item)), dosW - 44);
        int curY = dosY + 58;
        for (FormattedCharSequence line : lines) {
            g.drawString(this.font, line, dosX + 22, curY, 0xFF2C1E12, false);
            curY += 11;
        }

        // Sección de objetivos
        curY = Math.max(curY + 6, dosY + 124);
        g.fill(dosX + 20, curY, dosX + dosW - 20, curY + 38, 0x224A3018);
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_objective_hdr").getString(), cx, curY + 4, 0xFF6E3C18);
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_objective", trMissionTarget(item), item.targetCount()).getString(), cx, curY + 15, 0xFF152A4A);
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_location", item.floor()).getString(), cx, curY + 26, 0xFF1D3D22);

        // Sección de recompensas
        curY += 44;
        g.fill(dosX + 20, curY, dosX + dosW - 20, curY + 44, 0x222E5E28);
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_rewards_hdr").getString(), cx, curY + 4, 0xFF1E5E28);
        drawCleanCentered(g, Component.translatable("awakening_system.tower_board.dossier_rewards_coins", item.coins(), item.exp()).getString(), cx, curY + 16, 0xFF4A3515);
        net.minecraft.world.item.ItemStack crystals = item.crystalReward();
        net.minecraft.world.item.ItemStack material = item.materialReward();
        String rank = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(crystals.getItem()).getPath().replace("mana_crystal_shard_", "").toUpperCase(java.util.Locale.ROOT);
        String line2 = Component.translatable("awakening_system.tower_board.dossier_crystals", rank, crystals.getCount()).getString()
                + (material.isEmpty() ? "" : "  |  " + trMissionMaterial(item) + " x" + material.getCount());
        if (this.font.width(line2) > dosW - 70) line2 = Component.translatable("awakening_system.tower_board.dossier_crystals_short", rank, crystals.getCount()).getString() + (material.isEmpty() ? "" : "  |  +" + material.getCount() + " mat.");
        drawCleanCentered(g, line2, cx, curY + 28, 0xFF1F4430);

        g.renderItem(crystals, dosX + 24, curY + 14);
        if (!material.isEmpty()) g.renderItem(material, dosX + dosW - 40, curY + 14);

        // Botón de acción
        int btnW = 230;
        int btnH = 22;
        int btnX = dosX + (dosW - btnW) / 2;
        int btnY = dosY + 252;

        if (isLocked) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF7A1515);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_floor_locked", item.floor()).getString(), cx, btnY + 7, 0xFFFFFFFF);
        } else if (isCompleted) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF404040);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_completed_today").getString(), cx, btnY + 7, 0xFFE0E0E0);
        } else if (isActive) {
            if (payload.activeReadyToClaim()) {
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xFF00AA44 : 0xFF00772E);
                drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_claim").getString(), cx, btnY + 7, 0xFFFFFFFF);
            } else {
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF154870);
                drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_in_progress", payload.activeProgress(), payload.activeNeeded()).getString(), cx, btnY + 7, 0xFFFFFFFF);
            }
        } else if (payload.hasActiveMission()) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF554433);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_another_active").getString(), cx, btnY + 7, 0xFFCCCCCC);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xFFC88210 : 0xFF945F07);
            drawCleanCentered(g, Component.translatable("awakening_system.tower_board.btn_accept").getString(), cx, btnY + 7, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        int boardW = 560;
        int boardH = 340;
        int left = (this.width - boardW) / 2;
        int top = (this.height - boardH) / 2;
        boolean hasEntered = payload.playerSavedFloor() > 0;

        // 1. Expediente abierto
        if (selectedSlot != -1) {
            int dosW = 310;
            int dosH = 290;
            int dosX = (this.width - dosW) / 2;
            int dosY = (this.height - dosH) / 2;

            int closeX = dosX + dosW - 24;
            int closeY = dosY + 12;
            if ((mX >= closeX && mX <= closeX + 16 && mY >= closeY && mY <= closeY + 16)
                    || mX < dosX || mX > dosX + dosW || mY < dosY || mY > dosY + dosH) {
                selectedSlot = -1;
                playPageSound();
                return true;
            }

            if (!hasEntered) {
                int btnW = 230;
                int btnH = 22;
                int btnX = dosX + (dosW - btnW) / 2;
                int btnY = dosY + 248;
                if (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH) {
                    playDenySound();
                }
                return true;
            }

            int[] dailyIndices = payload.dailyIndices();
            if (dailyIndices != null && selectedSlot < dailyIndices.length) {
                int globalIdx = dailyIndices[selectedSlot];
                TowerMissionTemplate item = TowerDailyMissionManager.ALL_MISSIONS.get(globalIdx);

                boolean isLocked = item.floor() > payload.playerSavedFloor();
                boolean isActive = payload.hasActiveMission() && payload.activeIndex() == globalIdx;
                boolean isCompleted = payload.completedToday() != null && selectedSlot < payload.completedToday().length && payload.completedToday()[selectedSlot];

                int btnW = 230;
                int btnH = 22;
                int btnX = dosX + (dosW - btnW) / 2;
                int btnY = dosY + 252;

                if (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH) {
                    if (isLocked) {
                        playDenySound();
                        return true;
                    }

                    if (isActive && payload.activeReadyToClaim()) {
                        PacketDistributor.sendToServer(new TowerMissionActionPayload(TowerMissionActionPayload.ACTION_CLAIM, globalIdx));
                        selectedSlot = -1;
                        return true;
                    }

                    if (!isActive && !payload.hasActiveMission() && !isCompleted) {
                        PacketDistributor.sendToServer(new TowerMissionActionPayload(TowerMissionActionPayload.ACTION_ACCEPT, globalIdx));
                        playSignSound();
                        selectedSlot = -1;
                        return true;
                    }
                }
            }
            return true;
        }

        // 2. Botón Reclamar en barra inferior
        if (hasEntered && payload.hasActiveMission() && payload.activeReadyToClaim()) {
            int barX = left + 24;
            int barW = boardW - 48;
            int btnW = 140;
            int btnX = barX + barW - btnW - 6;
            int btnY = top + 296 + 4;
            int btnH = 18;
            if (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH) {
                PacketDistributor.sendToServer(new TowerMissionActionPayload(TowerMissionActionPayload.ACTION_CLAIM, payload.activeIndex()));
                return true;
            }
        }

        // 3. Clic en cualquiera de las 10 notas dispersas
        for (int i = 0; i < TowerDailyMissionManager.DAILY_OFFERS_COUNT; i++) {
            int nX = getSlotX(left, i);
            int nY = getSlotY(top, i);

            if (mX >= nX && mX <= nX + NOTE_W && mY >= nY && mY <= nY + NOTE_H) {
                selectedSlot = i;
                playPageSound();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String trMissionTitle(TowerMissionTemplate m) {
        String key = "awakening_system.tower_mission." + m.id() + ".title";
        return net.minecraft.client.resources.language.I18n.exists(key) ? Component.translatable(key).getString() : m.title();
    }

    private static String trMissionNarrative(TowerMissionTemplate m) {
        String key = "awakening_system.tower_mission." + m.id() + ".narrative";
        return net.minecraft.client.resources.language.I18n.exists(key) ? Component.translatable(key).getString() : m.narrative();
    }

    private static String trMissionTarget(TowerMissionTemplate m) {
        String key = "awakening_system.tower_mission." + m.id() + ".target";
        return net.minecraft.client.resources.language.I18n.exists(key) ? Component.translatable(key).getString() : m.targetName();
    }

    private static String trMissionMaterial(TowerMissionTemplate m) {
        String key = "awakening_system.tower_mission." + m.id() + ".material";
        return net.minecraft.client.resources.language.I18n.exists(key) ? Component.translatable(key).getString() : m.rewardMaterialName();
    }

    private static String formatRewardAmount(int val) {
        if (val >= 1000000) return (val / 1000000) + "M";
        if (val >= 1000) return (val / 1000) + (val % 1000 >= 100 ? "." + ((val % 1000) / 100) : "") + "k";
        return String.valueOf(val);
    }

    private void playPageSound() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        }
    }

    private void playSignSound() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
        }
    }

    private void playDenySound() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
        }
    }
}
