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
        STATUS_CARD("✦ Tarjeta de Estado (HP / MP / Stats)"),
        SKILL_BAR("⚔ Barra de Habilidades Activas"),
        APPRAISAL("👁 Visor de Análisis de Mobs"),
        COMBO("🔥 Medidor de Combo y Racha"),
        CONSTELLATION("✦ Notif. Constelaciones (Star Stream)");

        private final String title;

        HudElementType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
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
        super(Component.literal("Editor de HUD"));
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
        // Tinte oscuro translúcido suave sin desenfoque para ver el mundo y el juego nítido
        graphics.fill(0, 0, width, height, 0x45000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        SystemData data = player != null ? player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM) : null;

        // Actualizar elemento bajo el ratón si no se está arrastrando
        if (draggedElement == null) {
            hoveredElement = getElementAt(mouseX, mouseY);
        }

        // 1. Líneas guía si se está arrastrando
        if (draggedElement != null) {
            renderAlignmentGuides(g);
        }

        // 2. Renderizar previews de los elementos del HUD y sus cajas de interacción
        renderElementBox(g, HudElementType.STATUS_CARD, mouseX, mouseY);
        renderElementBox(g, HudElementType.SKILL_BAR, mouseX, mouseY);
        renderElementBox(g, HudElementType.APPRAISAL, mouseX, mouseY);
        renderElementBox(g, HudElementType.COMBO, mouseX, mouseY);
        renderElementBox(g, HudElementType.CONSTELLATION, mouseX, mouseY);

        // 3. Barra de control superior flotante (se oculta automáticamente al arrastrar)
        if (draggedElement != null) {
            int indW = 280;
            int indH = 18;
            int indX = (width - indW) / 2;
            int indY = height - 26;
            g.fill(indX - 1, indY - 1, indX + indW + 1, indY + indH + 1, 0x884EE2EC);
            g.fill(indX, indY, indX + indW, indY + indH, 0xD008111A);
            g.drawCenteredString(font, "§e✦ Arrastrando: " + draggedElement.getTitle(), indX + indW / 2, indY + 5, 0xFFFFD700);
        } else {
            renderTopToolbar(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderAlignmentGuides(GuiGraphics g) {
        int centerX = width / 2;
        int centerY = height / 2;

        // Línea central vertical
        for (int y = 0; y < height; y += 8) {
            g.fill(centerX, y, centerX + 1, y + 4, 0x334EE2EC);
        }
        // Línea central horizontal
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
            g.drawCenteredString(font, "§6[+] Mostrar Opciones", miniX + miniW / 2, miniY + 5, hov ? 0xFFFFFFFF : 0xFFDFB76C);
            return;
        }

        int tbW = 440;
        int tbH = 46;
        int tbX = (width - tbW) / 2;
        int tbY = 10;

        // Panel de vidrio oscuro holográfico
        g.fill(tbX - 1, tbY - 1, tbX + tbW + 1, tbY + tbH + 1, 0xAA4EE2EC);
        g.fill(tbX, tbY, tbX + tbW, tbY + tbH, 0xDD08111A);

        // Botón minimizar [-]
        int minBtnX = tbX + tbW - 20;
        int minBtnY = tbY + 4;
        int minBtnSize = 14;
        boolean minHov = mouseX >= minBtnX && mouseX < minBtnX + minBtnSize && mouseY >= minBtnY && mouseY < minBtnY + minBtnSize;
        g.fill(minBtnX, minBtnY, minBtnX + minBtnSize, minBtnY + minBtnSize, minHov ? 0xAA6E2020 : 0x66162533);
        g.renderOutline(minBtnX, minBtnY, minBtnSize, minBtnSize, minHov ? 0xFFFF6666 : 0xFF4EE2EC);
        g.drawCenteredString(font, "—", minBtnX + minBtnSize / 2, minBtnY + 2, 0xFFFFFFFF);

        // Título e instrucciones
        g.drawCenteredString(font, "§6✦ MODO EDICIÓN LIBRE DE HUD CON EL RATÓN ✦", tbX + tbW / 2, tbY + 5, 0xFFDFB76C);
        g.drawCenteredString(font, "§7Haz clic sobre cualquier elemento y arrástralo donde quieras. Pulsa ESC para salir.", tbX + tbW / 2, tbY + 16, 0xFFAAAAAA);

        // Botones de acción
        int btnY = tbY + 28;
        int btnW = 95;
        int btnH = 14;

        // 1. Imán / Snapping
        int b1X = tbX + 10;
        boolean b1Hov = mouseX >= b1X && mouseX < b1X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b1X, btnY, b1X + btnW, btnY + btnH, snapToEdges ? (b1Hov ? 0xDD28864E : 0xAA1A5D3E) : (b1Hov ? 0xDD444444 : 0x88222222));
        g.renderOutline(b1X, btnY, btnW, btnH, snapToEdges ? 0xFF50FA7B : 0xFF666666);
        g.drawCenteredString(font, snapToEdges ? "📐 Imán: SÍ" : "📐 Imán: NO", b1X + btnW / 2, btnY + 3, snapToEdges ? 0xFF50FA7B : 0xFFAAAAAA);

        // 2. Restablecer Posiciones
        int b2X = tbX + 115;
        boolean b2Hov = mouseX >= b2X && mouseX < b2X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b2X, btnY, b2X + btnW, btnY + btnH, b2Hov ? 0xAA6E2020 : 0x773B1111);
        g.renderOutline(b2X, btnY, btnW, btnH, b2Hov ? 0xFFFF6666 : 0xFF882222);
        g.drawCenteredString(font, "⟲ Restablecer", b2X + btnW / 2, btnY + 3, b2Hov ? 0xFFFFDDDD : 0xFFFFAAAA);

        // 3. Configuración GUI
        int b3X = tbX + 220;
        boolean b3Hov = mouseX >= b3X && mouseX < b3X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b3X, btnY, b3X + btnW, btnY + btnH, b3Hov ? 0xAA2B4055 : 0x66162533);
        g.renderOutline(b3X, btnY, btnW, btnH, b3Hov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, "⚙ Opciones", b3X + btnW / 2, btnY + 3, 0xFF70E8FF);

        // 4. Guardar y Salir
        int b4X = tbX + 325;
        int b4W = 105;
        boolean b4Hov = mouseX >= b4X && mouseX < b4X + b4W && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(b4X, btnY, b4X + b4W, btnY + btnH, b4Hov ? 0xDD28864E : 0xAA1A5D3E);
        g.renderOutline(b4X, btnY, b4W, btnH, b4Hov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, "✔ Guardar y Salir", b4X + b4W / 2, btnY + 3, 0xFF50FA7B);
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

        // Renderizado del contenido / preview real del elemento
        renderElementPreview(g, type, bx, by, bw, bh);

        // Marco y borde interactivo
        int borderColor;
        int bgColor;
        if (isDragged) {
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

        // Esquinas resaltadas
        int cornerColor = isDragged ? 0xFFFFD700 : (isHovered ? 0xFF00E5FF : 0xFF70E8FF);
        g.fill(bx, by, bx + 5, by + 2, cornerColor);
        g.fill(bx, by, bx + 2, by + 5, cornerColor);
        g.fill(bx + bw - 5, by, bx + bw, by + 2, cornerColor);
        g.fill(bx + bw - 2, by, bx + bw, by + 5, cornerColor);
        g.fill(bx, by + bh - 2, bx + 5, by + bh, cornerColor);
        g.fill(bx, by + bh - 5, bx + 2, by + bh, cornerColor);
        g.fill(bx + bw - 5, by + bh - 2, bx + bw, by + bh, cornerColor);
        g.fill(bx + bw - 2, by + bh - 5, bx + bw, by + bh, cornerColor);

        // Etiqueta superior
        String label = type.getTitle();
        if (isDragged) {
            label += " §e[X: " + bx + ", Y: " + by + "]";
        }
        int labelW = font.width(label) + 8;
        int labelX = Math.max(2, Math.min(width - labelW - 2, bx));
        int labelY = by >= 14 ? (by - 12) : (by + bh + 3);

        g.fill(labelX, labelY, labelX + labelW, labelY + 11, 0xEE08111A);
        g.renderOutline(labelX, labelY, labelW, 11, borderColor);
        g.drawString(font, label, labelX + 4, labelY + 2, isDragged ? 0xFFFFD700 : (isHovered ? 0xFF00E5FF : 0xFFE0E0E0), false);
    }

    private void renderElementPreview(GuiGraphics g, HudElementType type, int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        SystemData data = player != null ? player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM) : null;

        switch (type) {
            case STATUS_CARD -> {
                // Tarjeta de estado (HP, MP, Nivel, Rango)
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xD008111A);
                String name = player != null ? player.getScoreboardName() : "Cazador";
                int lvl = data != null ? data.level() : 1;
                String rank = data != null ? data.rank() : "E";

                g.drawString(font, "§6[" + rank + "] §fNv. " + lvl + " §e" + name, x + 6, y + 6, 0xFFFFFFFF);
                g.fill(x + 6, y + 16, x + w - 6, y + 17, 0x444EE2EC);

                // Barra HP preview
                g.drawString(font, "§c❤ HP  20/20", x + 6, y + 20, 0xFFFF4D4D);
                g.fill(x + 6, y + 30, x + w - 6, y + 35, 0xFF350E14);
                g.fill(x + 6, y + 30, x + (int) ((w - 12) * 0.85F), y + 35, 0xFFFF2D55);

                // Barra MP preview
                g.drawString(font, "§b✦ MP  150/150", x + 6, y + 38, 0xFF00E5FF);
                g.fill(x + 6, y + 48, x + w - 6, y + 53, 0xFF0B2538);
                g.fill(x + 6, y + 48, x + (int) ((w - 12) * 0.9F), y + 53, 0xFF00C6FF);

                // Mini stats
                g.drawString(font, "§b🛡 15  §c⚔ 24  §e🪙 1,250", x + 6, y + 57, 0xFFE0E0E0);
            }
            case SKILL_BAR -> {
                // Barra de habilidades con 5 ranuras
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
                // Visor de análisis de mobs
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xD808111A);
                g.drawString(font, "§c⚔ Dragón Ancestral §e[Jefe]", x + 6, y + 4, 0xFFFFFFFF);
                g.drawString(font, "§a2,500/2,500 §e(100%)", x + w - 90, y + 4, 0xFF50FA7B);
                g.fill(x + 6, y + 16, x + w - 6, y + 22, 0xFF2E0C11);
                g.fill(x + 6, y + 16, x + w - 6, y + 22, 0xFFFF1E56);
            }
            case COMBO -> {
                // Contador de combo
                g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xD008111A);
                g.drawCenteredString(font, "15 COMBO", x + w / 2, y + 3, 0xFFFFD866);
                g.fill(x + 4, y + 14, x + w - 4, y + 17, 0xFFFFD866);
            }
            case CONSTELLATION -> {
                // Notificación Star Stream
                g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xE80D111A);
                g.fill(x + 1, y + 1, x + w - 1, y + 3, 0xFFFFD700);
                g.drawString(font, "§6[Dragón del Abismo]", x + 4, y + 5, 0xFFFFD700, false);
                g.drawString(font, "§e✦ +100 Monedas", x + 4, y + 17, 0xFFFFF0A0, false);
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
                int totalW = 5 * slotW + 4 * gap; // ~112
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
                int defaultLeft = width - scaledW - 10;
                int defaultTop = 10;
                int x = GuiConfig.constellationNotifX >= 0 ? GuiConfig.constellationNotifX : defaultLeft;
                int y = GuiConfig.constellationNotifY >= 0 ? GuiConfig.constellationNotifY : defaultTop;
                return new int[]{x, y, scaledW, scaledH};
            }
        }
        return new int[]{0, 0, 10, 10};
    }

    private HudElementType getElementAt(double mouseX, double mouseY) {
        // Comprobar elementos en orden inverso para priorizar el superior
        HudElementType[] types = {HudElementType.CONSTELLATION, HudElementType.COMBO, HudElementType.APPRAISAL, HudElementType.SKILL_BAR, HudElementType.STATUS_CARD};
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
        if (button == 0) {
            // Si la barra está colapsada, verificar clic en el botón de expandir
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
                // Verificar clics en la barra de herramientas superior
                int tbW = 440;
                int tbH = 46;
                int tbX = (width - tbW) / 2;
                int tbY = 10;

                // Clic en botón minimizar [-]
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

            // 1. Imán / Snapping
            int b1X = tbX + 10;
            if (mouseX >= b1X && mouseX < b1X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                snapToEdges = !snapToEdges;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }

            // 2. Restablecer Posiciones
            int b2X = tbX + 115;
            if (mouseX >= b2X && mouseX < b2X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                GuiConfig.resetHudPositions();
                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.3F);
                return true;
            }

            // 3. Configuración GUI
            int b3X = tbX + 220;
            if (mouseX >= b3X && mouseX < b3X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                GuiConfig.save();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                GuiConfigScreen.open(parentScreen);
                return true;
            }

            // 4. Guardar y Salir
            int b4X = tbX + 325;
            int b4W = 105;
            if (mouseX >= b4X && mouseX < b4X + b4W && mouseY >= btnY && mouseY < btnY + btnH) {
                closeAndSave();
                return true;
            }
            }

            // Verificar clic en elemento del HUD para arrastrar
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

            // Imán / Snapping inteligente a bordes y centro
            if (snapToEdges) {
                int snapDist = 12;
                // Borde izquierdo
                if (Math.abs(newX - 8) < snapDist) newX = 8;
                // Borde derecho
                if (Math.abs(newX - (width - elemW - 8)) < snapDist) newX = width - elemW - 8;
                // Centro horizontal
                int screenCenterX = (width - elemW) / 2;
                if (Math.abs(newX - screenCenterX) < snapDist) newX = screenCenterX;

                // Borde superior
                if (Math.abs(newY - 8) < snapDist) newY = 8;
                // Borde inferior
                if (Math.abs(newY - (height - elemH - 8)) < snapDist) newY = height - elemH - 8;
                // Arriba de la hotbar de Minecraft (~height - elemH - 48)
                int aboveHotbarY = height - elemH - 48;
                if (Math.abs(newY - aboveHotbarY) < snapDist) newY = aboveHotbarY;
            }

            // Limitar dentro de la pantalla
            newX = Math.max(2, Math.min(width - elemW - 2, newX));
            newY = Math.max(2, Math.min(height - elemH - 2, newY));

            // Guardar en GuiConfig
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
        if (keyCode == 256) { // ESC
            closeAndSave();
            return true;
        }
        // Flechas del teclado para ajuste fino píxel a píxel del elemento seleccionado
        if (selectedElement != null) {
            int step = Screen.hasShiftDown() ? 5 : 1;
            int[] b = getElementBounds(selectedElement);
            int newX = b[0];
            int newY = b[1];

            if (keyCode == 263) newX -= step; // Izquierda
            if (keyCode == 262) newX += step; // Derecha
            if (keyCode == 265) newY -= step; // Arriba
            if (keyCode == 264) newY += step; // Abajo

            if (keyCode >= 262 && keyCode <= 265) {
                newX = Math.max(2, Math.min(width - b[2] - 2, newX));
                newY = Math.max(2, Math.min(height - b[3] - 2, newY));

                switch (selectedElement) {
                    case STATUS_CARD -> { GuiConfig.statusCardX = newX; GuiConfig.statusCardY = newY; }
                    case SKILL_BAR -> { GuiConfig.skillBarX = newX; GuiConfig.skillBarY = newY; }
                    case APPRAISAL -> { GuiConfig.appraisalX = newX; GuiConfig.appraisalY = newY; }
                    case COMBO -> { GuiConfig.comboX = newX; GuiConfig.comboY = newY; }
                    case CONSTELLATION -> { GuiConfig.constellationNotifX = newX; GuiConfig.constellationNotifY = newY; }
                }
                GuiConfig.save();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
