package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * Pantalla interactiva para mover, arrastrar y posicionar libremente cualquier
 * elemento del HUD con el ratón en tiempo real.
 */
public class HudEditorScreen extends Screen {
    private final Screen parentScreen;

    public enum HudElementType {
        STATUS_CARD("✦ Status Card (HP / MP / Stats)"),
        SKILL_BAR("⚔ Active Skills Bar"),
        APPRAISAL("👁 Mob Appraisal Visor"),
        COMBO("🔥 Combo & Streak Meter"),
        CONSTELLATION("✦ Constellation Notifs (Star Stream)"),
        BOSS_ALERT("▲ Boss & Threat Alert / Notificación de Jefe");

        private final String title;

        HudElementType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public Component getComponent() {
            return Component.translatable("awakening_system.hud_editor.element." + name().toLowerCase(java.util.Locale.ROOT));
        }

        public boolean isEnabled() {
            return switch (this) {
                case STATUS_CARD -> GuiConfig.showHudStatusCard;
                case SKILL_BAR -> GuiConfig.showHudSkillBar;
                case APPRAISAL -> GuiConfig.showHudAppraisal;
                case COMBO -> GuiConfig.showHudCombo;
                case CONSTELLATION -> GuiConfig.showConstellationPopups;
                case BOSS_ALERT -> GuiConfig.showBossAlert;
            };
        }

        public void toggleEnabled() {
            switch (this) {
                case STATUS_CARD -> GuiConfig.showHudStatusCard = !GuiConfig.showHudStatusCard;
                case SKILL_BAR -> GuiConfig.showHudSkillBar = !GuiConfig.showHudSkillBar;
                case APPRAISAL -> GuiConfig.showHudAppraisal = !GuiConfig.showHudAppraisal;
                case COMBO -> GuiConfig.showHudCombo = !GuiConfig.showHudCombo;
                case CONSTELLATION -> GuiConfig.showConstellationPopups = !GuiConfig.showConstellationPopups;
                case BOSS_ALERT -> GuiConfig.showBossAlert = !GuiConfig.showBossAlert;
            }
            GuiConfig.save();
        }
    }

    private HudElementType hoveredElement = null;
    private HudElementType draggedElement = null;
    private HudElementType selectedElement = null;

    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean snapToEdges = true;
    private boolean toolbarCollapsed = false;

    public HudEditorScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.hud_editor.title"));
        this.parentScreen = parentScreen;
    }

    public static void open(Screen parent) {
        Minecraft.getInstance().setScreen(new HudEditorScreen(parent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x45000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        SystemData data = player != null ? player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM) : null;

        if (draggedElement == null) {
            hoveredElement = getElementAt(mouseX, mouseY);
        }

        if (draggedElement != null) {
            renderAlignmentGuides(g);
        }

        renderElementBox(g, HudElementType.STATUS_CARD, mouseX, mouseY);
        renderElementBox(g, HudElementType.SKILL_BAR, mouseX, mouseY);
        renderElementBox(g, HudElementType.APPRAISAL, mouseX, mouseY);
        renderElementBox(g, HudElementType.COMBO, mouseX, mouseY);
        renderElementBox(g, HudElementType.CONSTELLATION, mouseX, mouseY);
        renderElementBox(g, HudElementType.BOSS_ALERT, mouseX, mouseY);

        if (draggedElement != null) {
            int indW = 280;
            int indH = 18;
            int indX = (width - indW) / 2;
            int indY = height - 26;
            g.fill(indX - 1, indY - 1, indX + indW + 1, indY + indH + 1, 0x884EE2EC);
            g.fill(indX, indY, indX + indW, indY + indH, 0xD008111A);
            g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.dragging", draggedElement.getComponent()), indX + indW / 2, indY + 5, 0xFFFFD700);
        } else {
            renderTopToolbar(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderAlignmentGuides(GuiGraphics g) {
        int centerX = width / 2;
        int centerY = height / 2;

        for (int y = 0; y < height; y += 8) {
            g.fill(centerX, y, centerX + 1, y + 4, 0x334EE2EC);
        }
        for (int x = 0; x < width; x += 8) {
            g.fill(x, centerY, x + 4, centerY + 1, 0x334EE2EC);
        }
    }

    private void renderTopToolbar(GuiGraphics g, int mouseX, int mouseY) {
        if (toolbarCollapsed) {
            int miniW = 140;
            int miniH = 18;
            int miniX = (width - miniW) / 2;
            int miniY = 6;
            boolean hov = mouseX >= miniX && mouseX < miniX + miniW && mouseY >= miniY && mouseY < miniY + miniH;
            g.fill(miniX - 1, miniY - 1, miniX + miniW + 1, miniY + miniH + 1, 0xAA4EE2EC);
            g.fill(miniX, miniY, miniX + miniW, miniY + miniH, hov ? 0xDD1B3347 : 0xDD08111A);
            g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.show_options"), miniX + miniW / 2, miniY + 5, hov ? 0xFFFFFFFF : 0xFFDFB76C);
            return;
        }

        int tbW = 440;
        int tbH = 46;
        int tbX = (width - tbW) / 2;
        int tbY = 10;

        g.fill(tbX - 1, tbY - 1, tbX + tbW + 1, tbY + tbH + 1, 0xAA4EE2EC);
        g.fill(tbX, tbY, tbX + tbW, tbY + tbH, 0xDD08111A);

        int minBtnX = tbX + tbW - 20;
        int minBtnY = tbY + 4;
        int minBtnSize = 14;
        boolean minHov = mouseX >= minBtnX && mouseX < minBtnX + minBtnSize && mouseY >= minBtnY && mouseY < minBtnY + minBtnSize;
        g.fill(minBtnX, minBtnY, minBtnX + minBtnSize, minBtnY + minBtnSize, minHov ? 0xAA6E2020 : 0x66162533);
        g.renderOutline(minBtnX, minBtnY, minBtnSize, minBtnSize, minHov ? 0xFFFF6666 : 0xFF4EE2EC);
        g.drawCenteredString(font, "—", minBtnX + minBtnSize / 2, minBtnY + 2, 0xFFFFFFFF);

        g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.header"), tbX + tbW / 2, tbY + 5, 0xFFDFB76C);
        g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.instructions"), tbX + tbW / 2, tbY + 16, 0xFFAAAAAA);

        int btnY = tbY + 28;
        int btnW = 95;
        int btnH = 14;

        int b1X = tbX + 10;
        boolean b1Hov = mouseX >= b1X && mouseX < b1X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b1X, btnY, b1X + btnW, btnY + btnH, snapToEdges ? (b1Hov ? 0xDD28864E : 0xAA1A5D3E) : (b1Hov ? 0xDD444444 : 0x88222222));
        g.renderOutline(b1X, btnY, btnW, btnH, snapToEdges ? 0xFF50FA7B : 0xFF666666);
        g.drawCenteredString(font, snapToEdges ? Component.translatable("awakening_system.hud_editor.magnet_on") : Component.translatable("awakening_system.hud_editor.magnet_off"), b1X + btnW / 2, btnY + 3, snapToEdges ? 0xFF50FA7B : 0xFFAAAAAA);

        int b2X = tbX + 115;
        boolean b2Hov = mouseX >= b2X && mouseX < b2X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b2X, btnY, b2X + btnW, btnY + btnH, b2Hov ? 0xAA6E2020 : 0x773B1111);
        g.renderOutline(b2X, btnY, btnW, btnH, b2Hov ? 0xFFFF6666 : 0xFF882222);
        g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.btn.reset"), b2X + btnW / 2, btnY + 3, b2Hov ? 0xFFFFDDDD : 0xFFFFAAAA);

        int b3X = tbX + 220;
        boolean b3Hov = mouseX >= b3X && mouseX < b3X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b3X, btnY, b3X + btnW, btnY + btnH, b3Hov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(b3X, btnY, btnW, btnH, b3Hov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.btn.options"), b3X + btnW / 2, btnY + 3, 0xFF70E8FF);

        int b4X = tbX + 325;
        int b4W = 105;
        boolean b4Hov = mouseX >= b4X && mouseX < b4X + b4W && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b4X, btnY, b4X + b4W, btnY + btnH, b4Hov ? 0xDD28864E : 0xAA1A5D3E);
        g.renderOutline(b4X, btnY, b4W, btnH, b4Hov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, Component.translatable("awakening_system.hud_editor.btn.save_exit"), b4X + b4W / 2, btnY + 3, 0xFF50FA7B);
    }

    private void renderElementBox(GuiGraphics g, HudElementType type, int mouseX, int mouseY) {
        int[] bounds = getElementBounds(type);
        int bx = bounds[0];
        int by = bounds[1];
        int bw = bounds[2];
        int bh = bounds[3];

        boolean isHovered = (hoveredElement == type);
        boolean isDragged = (draggedElement == type);
        boolean isSelected = (selectedElement == type);

        renderElementPreview(g, type, bx, by, bw, bh);

        boolean isEnabled = type.isEnabled();
        int borderColor;
        int bgColor;
        if (!isEnabled) {
            borderColor = 0xFFAA4444;
            bgColor = 0x25AA2222;
        } else if (isDragged) {
            borderColor = 0xFFFFD700;
            bgColor = 0x35FFD700;
        } else if (isHovered || isSelected) {
            borderColor = 0xFF00E5FF;
            bgColor = 0x2500E5FF;
        } else {
            borderColor = 0x804EE2EC;
            bgColor = 0x154EE2EC;
        }

        g.fill(bx, by, bx + bw, by + bh, bgColor);
        g.renderOutline(bx, by, bw, bh, borderColor);

        int cornerColor = !isEnabled ? 0xFFFF6666 : (isDragged ? 0xFFFFD700 : (isHovered ? 0xFF00E5FF : 0xFF70E8FF));
        g.fill(bx, by, bx + 5, by + 2, cornerColor);
        g.fill(bx, by, bx + 2, by + 5, cornerColor);
        g.fill(bx + bw - 5, by, bx + bw, by + 2, cornerColor);
        g.fill(bx + bw - 2, by, bx + bw, by + 5, cornerColor);
        g.fill(bx, by + bh - 2, bx + 5, by + bh, cornerColor);
        g.fill(bx, by + bh - 5, bx + 2, by + bh, cornerColor);
        g.fill(bx + bw - 5, by + bh - 2, bx + bw, by + bh, cornerColor);
        g.fill(bx + bw - 2, by + bh - 5, bx + bw, by + bh, cornerColor);

        String label = type.getComponent().getString();
        if (!isEnabled) {
            label += " §c" + Component.translatable("awakening_system.hud_editor.hidden_tag").getString();
        } else if (isDragged) {
            label += " §e[X: " + bx + ", Y: " + by + "]";
        }
        int labelW = font.width(label) + 8;
        int labelX = Math.max(2, Math.min(width - labelW - 2, bx));
        int labelY = by >= 14 ? (by - 12) : (by + bh + 3);

        g.fill(labelX, labelY, labelX + labelW, labelY + 11, 0xEE08111A);
        g.renderOutline(labelX, labelY, labelW, 11, borderColor);
        g.drawString(font, label, labelX + 4, labelY + 2, !isEnabled ? 0xFFFFAAAA : (isDragged ? 0xFFFFD700 : (isHovered ? 0xFF00E5FF : 0xFFE0E0E0)), false);
    }

    private void renderElementPreview(GuiGraphics g, HudElementType type, int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        SystemData data = player != null ? player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM) : null;

        switch (type) {
            case STATUS_CARD -> {
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xD008111A);
                String defName = Component.translatable("awakening_system.hud_editor.preview.hunter_default").getString();
                String name = player != null ? player.getScoreboardName() : defName;
                int lvl = data != null ? data.level() : 1;
                String rank = data != null ? data.rank() : "E";
                String lvlPrefix = Component.translatable("awakening_system.hud.level_prefix").getString();

                g.drawString(font, "§6[" + rank + "] §f" + lvlPrefix + " " + lvl + " §e" + name, x + 6, y + 6, 0xFFFFFFFF);
                g.fill(x + 6, y + 16, x + w - 6, y + 17, 0x444EE2EC);

                g.drawString(font, "§c❤ HP  20/20", x + 6, y + 20, 0xFFFF4D4D);
                g.fill(x + 6, y + 30, x + w - 6, y + 35, 0xFF350E14);
                g.fill(x + 6, y + 30, x + (int) ((w - 12) * 0.85F), y + 35, 0xFFFF2D55);

                g.drawString(font, "§b✦ MP  150/150", x + 6, y + 38, 0xFF00E5FF);
                g.fill(x + 6, y + 48, x + w - 6, y + 53, 0xFF0B2538);
                g.fill(x + 6, y + 48, x + (int) ((w - 12) * 0.9F), y + 53, 0xFF00C6FF);

                g.drawString(font, "§b🛡 15  §c⚔ 24  §e🪙 1,250", x + 6, y + 57, 0xFFE0E0E0);
            }
            case SKILL_BAR -> {
                int slotW = 20;
                int slotH = 19;
                int gap = 3;
                String[] keys = {"V", "R", "G", "B", "X"};
                String[] icons = {"⚔", "⚡", "❄", "🔥", "✦"};

                for (int i = 0; i < 5; i++) {
                    int sx = x + i * (slotW + gap);
                    g.fill(sx, y, sx + slotW, y + slotH, 0xD008111A);
                    g.renderOutline(sx, y, slotW, slotH, 0xFF4EE2EC);
                    g.drawString(font, keys[i], sx + 2, y + 1, 0xFFFFD866);
                    g.drawCenteredString(font, icons[i], sx + slotW / 2, y + 7, 0xFF00E5FF);
                }
            }
            case APPRAISAL -> {
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xD808111A);
                Component bossTitle = Component.translatable("awakening_system.hud_editor.preview.boss_title");
                g.drawString(font, bossTitle, x + 6, y + 4, 0xFFFFFFFF);

                int barY = y + 15;
                int barH = 8;
                g.fill(x + 6, barY, x + w - 6, barY + barH, 0xFF2E0C11);
                g.fill(x + 6, barY, x + w - 6, barY + barH, 0xFFFF1E56);
                g.drawCenteredString(font, "2,500 / 2,500 (100%)", x + w / 2, barY, 0xFFFFFFFF);
            }
            case COMBO -> {
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xD008111A);
                g.drawCenteredString(font, "15 COMBO", x + w / 2, y + 3, 0xFFFFD866);
                g.fill(x + 4, y + 14, x + w - 4, y + 17, 0xFFFFD866);
            }
            case CONSTELLATION -> {
                float scale = Math.max(0.4F, Math.min(1.5F, GuiConfig.constellationNotifScale));
                g.pose().pushPose();
                g.pose().translate(x, y, 0);
                g.pose().scale(scale, scale, 1.0F);

                int boxW = GuiConfig.compactConstellationPopups ? 190 : 250;
                int boxH = 32;

                g.fill(-1, -1, boxW + 1, boxH + 1, 0xFFD4AF37);
                g.fill(0, 0, boxW, boxH, 0xD00D111A);
                g.fill(0, 0, boxW, 2, 0xFFFFD700);

                Component cTitle = Component.translatable("awakening_system.hud_editor.preview.constellation_title");
                Component cReward = Component.translatable("awakening_system.hud_editor.preview.constellation_reward");

                g.drawString(font, cTitle, 6, 5, 0xFFFFD700, false);
                g.drawString(font, cReward, 6, 17, 0xFFFFF0A0, false);

                g.pose().popPose();
            }
            case BOSS_ALERT -> {
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xEE330008);
                g.renderOutline(x + 2, y + 2, w - 4, h - 4, 0xFFFF2244);
                g.drawString(font, "§c§l▲ JEFE: §f[Nv. 4] Ángel Ofanim (44m)", x + 6, y + 4, 0xFFFFFFFF, true);
            }
        }
    }

    private int[] getElementBounds(HudElementType type) {
        float hudScale = SystemHudOverlay.getEffectiveHudScale(width, height);

        switch (type) {
            case STATUS_CARD -> {
                int cardW = 160;
                int cardH = 70;
                int scaledW = (int) (cardW * hudScale);
                int scaledH = (int) (cardH * hudScale);

                int x = GuiConfig.statusCardX >= 0 ? GuiConfig.statusCardX : 8;
                int y = GuiConfig.statusCardY >= 0 ? GuiConfig.statusCardY : 8;
                return new int[]{x, y, scaledW, scaledH};
            }
            case SKILL_BAR -> {
                int slotW = 20;
                int gap = 3;
                int totalW = 5 * slotW + 4 * gap;
                int totalH = 19;

                int x = GuiConfig.skillBarX >= 0 ? GuiConfig.skillBarX : 8;
                int y = GuiConfig.skillBarY >= 0 ? GuiConfig.skillBarY : (height - totalH - 8);
                return new int[]{x, y, totalW, totalH};
            }
            case APPRAISAL -> {
                int appW = 210;
                int appH = 28;
                int x = GuiConfig.appraisalX >= 0 ? GuiConfig.appraisalX : (width - appW) / 2;
                int y = GuiConfig.appraisalY >= 0 ? GuiConfig.appraisalY : 64;
                return new int[]{x, y, appW, appH};
            }
            case COMBO -> {
                int comboW = 86;
                int comboH = 20;
                int x = GuiConfig.comboX >= 0 ? GuiConfig.comboX : (width - comboW - 12);
                int y = GuiConfig.comboY >= 0 ? GuiConfig.comboY : (height - 42);
                return new int[]{x, y, comboW, comboH};
            }
            case CONSTELLATION -> {
                float scale = Math.max(0.4F, Math.min(1.5F, GuiConfig.constellationNotifScale));
                int boxW = GuiConfig.compactConstellationPopups ? 190 : 250;
                int scaledW = (int) (boxW * scale);
                int scaledH = (int) (32 * scale);
                int defaultLeft = GuiConfig.showMinimap ? (width - scaledW) / 2 : (width - scaledW - 10);
                int defaultTop = 10;
                int x = GuiConfig.constellationNotifX >= 0 ? GuiConfig.constellationNotifX : defaultLeft;
                int y = GuiConfig.constellationNotifY >= 0 ? GuiConfig.constellationNotifY : defaultTop;
                return new int[]{x, y, scaledW, scaledH};
            }
            case BOSS_ALERT -> {
                int alertW = 190;
                int alertH = 16;
                int defaultLeft = Math.max(16, width - alertW - 16);
                int defaultTop = 16;
                int x = GuiConfig.bossAlertX >= 0 ? GuiConfig.bossAlertX : defaultLeft;
                int y = GuiConfig.bossAlertY >= 0 ? GuiConfig.bossAlertY : defaultTop;
                return new int[]{x, y, alertW, alertH};
            }
        }
        return new int[]{0, 0, 10, 10};
    }

    private HudElementType getElementAt(double mouseX, double mouseY) {
        HudElementType[] types = {HudElementType.BOSS_ALERT, HudElementType.CONSTELLATION, HudElementType.COMBO, HudElementType.APPRAISAL, HudElementType.SKILL_BAR, HudElementType.STATUS_CARD};
        for (HudElementType type : types) {
            int[] b = getElementBounds(type);
            if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                return type;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            HudElementType clicked = getElementAt(mouseX, mouseY);
            if (clicked != null) {
                clicked.toggleEnabled();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }
        }

        if (button == 0) {
            if (toolbarCollapsed) {
                int miniW = 140;
                int miniH = 18;
                int miniX = (width - miniW) / 2;
                int miniY = 6;
                if (mouseX >= miniX && mouseX < miniX + miniW && mouseY >= miniY && mouseY < miniY + miniH) {
                    toolbarCollapsed = false;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
            } else {
                int tbW = 440;
                int tbH = 46;
                int tbX = (width - tbW) / 2;
                int tbY = 10;

                int minBtnX = tbX + tbW - 20;
                int minBtnY = tbY + 4;
                int minBtnSize = 14;
                if (mouseX >= minBtnX && mouseX < minBtnX + minBtnSize && mouseY >= minBtnY && mouseY < minBtnY + minBtnSize) {
                    toolbarCollapsed = true;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.8F);
                    return true;
                }

                int btnY = tbY + 28;
                int btnW = 95;
                int btnH = 14;

            int b1X = tbX + 10;
            if (mouseX >= b1X && mouseX < b1X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                snapToEdges = !snapToEdges;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }

            int b2X = tbX + 115;
            if (mouseX >= b2X && mouseX < b2X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                GuiConfig.resetHudPositions();
                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                return true;
            }

            int b3X = tbX + 220;
            if (mouseX >= b3X && mouseX < b3X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                GuiConfig.save();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                GuiConfigScreen.open(parentScreen);
                return true;
            }

            int b4X = tbX + 325;
            int b4W = 105;
            if (mouseX >= b4X && mouseX < b4X + b4W && mouseY >= btnY && mouseY < btnY + btnH) {
                closeAndSave();
                return true;
            }
            }

            HudElementType clicked = getElementAt(mouseX, mouseY);
            if (clicked != null) {
                draggedElement = clicked;
                selectedElement = clicked;
                int[] b = getElementBounds(clicked);
                dragOffsetX = (int) (mouseX - b[0]);
                dragOffsetY = (int) (mouseY - b[1]);
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.4F);
                return true;
            } else {
                selectedElement = null;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggedElement != null) {
            int[] b = getElementBounds(draggedElement);
            int elemW = b[2];
            int elemH = b[3];

            int newX = (int) (mouseX - dragOffsetX);
            int newY = (int) (mouseY - dragOffsetY);

            if (snapToEdges) {
                int snapDist = 12;
                if (Math.abs(newX - 8) < snapDist) newX = 8;
                if (Math.abs(newX - (width - elemW - 8)) < snapDist) newX = width - elemW - 8;
                int screenCenterX = (width - elemW) / 2;
                if (Math.abs(newX - screenCenterX) < snapDist) newX = screenCenterX;

                if (Math.abs(newY - 8) < snapDist) newY = 8;
                if (Math.abs(newY - (height - elemH - 8)) < snapDist) newY = height - elemH - 8;
                int aboveHotbarY = height - elemH - 48;
                if (Math.abs(newY - aboveHotbarY) < snapDist) newY = aboveHotbarY;
            }

            newX = Math.max(2, Math.min(width - elemW - 2, newX));
            newY = Math.max(2, Math.min(height - elemH - 2, newY));

            switch (draggedElement) {
                case STATUS_CARD -> {
                    GuiConfig.statusCardX = newX;
                    GuiConfig.statusCardY = newY;
                }
                case SKILL_BAR -> {
                    GuiConfig.skillBarX = newX;
                    GuiConfig.skillBarY = newY;
                }
                case APPRAISAL -> {
                    GuiConfig.appraisalX = newX;
                    GuiConfig.appraisalY = newY;
                }
                case COMBO -> {
                    GuiConfig.comboX = newX;
                    GuiConfig.comboY = newY;
                }
                case CONSTELLATION -> {
                    GuiConfig.constellationNotifX = newX;
                    GuiConfig.constellationNotifY = newY;
                }
                case BOSS_ALERT -> {
                    GuiConfig.bossAlertX = newX;
                    GuiConfig.bossAlertY = newY;
                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedElement != null) {
            draggedElement = null;
            GuiConfig.save();
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.2F);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void closeAndSave() {
        GuiConfig.save();
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
        if (parentScreen != null) {
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            closeAndSave();
            return true;
        }
        if (selectedElement != null) {
            int step = Screen.hasShiftDown() ? 5 : 1;
            int[] b = getElementBounds(selectedElement);
            int newX = b[0];
            int newY = b[1];

            if (keyCode == 263) newX -= step;
            if (keyCode == 262) newX += step;
            if (keyCode == 265) newY -= step;
            if (keyCode == 264) newY += step;

            if (keyCode >= 262 && keyCode <= 265) {
                newX = Math.max(2, Math.min(width - b[2] - 2, newX));
                newY = Math.max(2, Math.min(height - b[3] - 2, newY));

                switch (selectedElement) {
                    case STATUS_CARD -> { GuiConfig.statusCardX = newX; GuiConfig.statusCardY = newY; }
                    case SKILL_BAR -> { GuiConfig.skillBarX = newX; GuiConfig.skillBarY = newY; }
                    case APPRAISAL -> { GuiConfig.appraisalX = newX; GuiConfig.appraisalY = newY; }
                    case COMBO -> { GuiConfig.comboX = newX; GuiConfig.comboY = newY; }
                    case CONSTELLATION -> { GuiConfig.constellationNotifX = newX; GuiConfig.constellationNotifY = newY; }
                    case BOSS_ALERT -> { GuiConfig.bossAlertX = newX; GuiConfig.bossAlertY = newY; }
                }
                GuiConfig.save();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
