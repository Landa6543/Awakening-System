package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public final class ClassSkillTreeScreen extends Screen {
    private final Screen parentScreen;
    private String selectedSkillId = null;

    // Cámara, Navegación y Zoom (MU Online Master Skill Tree)
    private double camX = 0.0;
    private double camY = 0.0;
    private float zoom = 1.0f;
    private boolean isDragging = false;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private int tickCount = 0;

    public ClassSkillTreeScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.screen.skill_tree.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Evita el desenfoque vanilla
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    private void playClickSound() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 1.1F);
    }

    private void playUpgradeSound() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_SKILL_UNLOCK.value(), 1.0F, 1.0F);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 640.0F;
        float scaleY = (float) this.height / 380.0F;
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

    private void enableScissorScaled(GuiGraphics g, int minX, int minY, int maxX, int maxY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) {
            g.enableScissor(minX, minY, maxX, maxY);
        } else {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            int sMinX = (int) Math.floor(cx + (minX - cx) * scale);
            int sMinY = (int) Math.floor(cy + (minY - cy) * scale);
            int sMaxX = (int) Math.ceil(cx + (maxX - cx) * scale);
            int sMaxY = (int) Math.ceil(cy + (maxY - cy) * scale);
            sMinX = Math.max(0, sMinX);
            sMinY = Math.max(0, sMinY);
            sMaxX = Math.min(this.width, Math.max(sMinX, sMaxX));
            sMaxY = Math.min(this.height, Math.max(sMinY, sMaxY));
            g.enableScissor(sMinX, sMinY, sMaxX, sMaxY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x95000000, 0xD5000000);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            graphics.pose().translate(cx, cy, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-cx, -cy, 0.0F);
        }

        int panelW = 620;
        int panelH = 360;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data == null || !data.hasHunterClass()) {
            graphics.fill(left, top, left + panelW, top + panelH, 0xF008121A);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.skill_tree.no_class"), left + panelW / 2, top + panelH / 2 - 10, 0xFFFF5555);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.skill_tree.reach_level30"), left + panelW / 2, top + panelH / 2 + 6, 0xFFAAAAAA);
            return;
        }

        boolean sys30 = data.system30Updated();
        boolean sys20 = (data.systemUpdated() || data.isSystemUpdating()) && !sys30;

        // Marco exterior
        int borderCol = sys30 ? 0xC01B5E20 : (sys20 ? 0xC04A148C : 0xC000838F);
        int bgCol = sys30 ? 0xF0081814 : (sys20 ? 0xF0120822 : 0xF007131D);
        graphics.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, borderCol);
        graphics.fill(left, top, left + panelW, top + panelH, bgCol);

        // Esquinas holográficas
        int cornerColor = (sys30 || sys20) ? 0xFFFFD700 : 0xFF00E5FF;
        graphics.fill(left, top, left + 10, top + 2, cornerColor);
        graphics.fill(left, top, left + 2, top + 10, cornerColor);
        graphics.fill(left + panelW - 10, top, left + panelW, top + 2, cornerColor);
        graphics.fill(left + panelW - 2, top, left + panelW, top + 10, cornerColor);
        graphics.fill(left, top + panelH - 2, left + 10, top + panelH, cornerColor);
        graphics.fill(left, top + panelH - 10, left + 2, top + panelH, cornerColor);
        graphics.fill(left + panelW - 10, top + panelH - 2, left + panelW, top + panelH, cornerColor);
        graphics.fill(left + panelW - 2, top + panelH - 10, left + panelW, top + panelH, cornerColor);

        // 1. Cabecera Estilo MU Master Skill Tree
        Component className = Component.translatable("awakening_system.class." + data.hunterClass() + ".name");
        Component headerTitle = Component.translatable("awakening_system.skill_tree.header", className).withStyle(ChatFormatting.BOLD);
        graphics.drawString(font, headerTitle, left + 14, top + 8, 0xFFFFD700);

        int sp = data.skillPoints();
        int treeCount = data.getUnlockedTreeSkillsCount();
        Component subHeader = Component.translatable("awakening_system.skill_tree.subheader", sp, treeCount);
        graphics.drawString(font, subHeader, left + 14, top + 20, 0xFFCCE3EC);

        // Botón Cerrar
        int closeBtnW = 60;
        int closeBtnH = 18;
        int closeBtnX = left + panelW - closeBtnW - 10;
        int closeBtnY = top + 8;
        boolean closeHov = mX >= closeBtnX && mX < closeBtnX + closeBtnW && mY >= closeBtnY && mY < closeBtnY + closeBtnH;
        graphics.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnW, closeBtnY + closeBtnH, closeHov ? 0xFF882222 : 0xFF142430);
        drawBorder(graphics, closeBtnX, closeBtnY, closeBtnW, closeBtnH, closeHov ? 0xFFFF5555 : 0xFF2D7691);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.close_x"), closeBtnX + closeBtnW / 2, closeBtnY + 5, closeHov ? 0xFFFFFFFF : 0xFFCCE3EC);

        // Definición de Áreas
        int canvasX = left + 8;
        int canvasY = top + 32;
        int canvasW = panelW - 232;
        int canvasH = panelH - 74;

        int inspX = left + panelW - 220;
        int inspY = top + 32;
        int inspW = 212;
        int inspH = panelH - 74;

        // 2. Lienzo MU Online con 3 Columnas, Zoom real y Niebla de Guerra
        renderTreeCanvas(graphics, data, canvasX, canvasY, canvasW, canvasH, mX, mY);

        // 3. Consola de Inspección Lateral (Anti-Desborde)
        renderInspectionPanel(graphics, data, inspX, inspY, inspW, inspH, mX, mY);

        // 4. Panel Inferior de Ranuras de Combate
        int slotBarX = left + 8;
        int slotBarY = top + panelH - 38;
        int slotBarW = panelW - 16;
        int slotBarH = 32;
        renderSlotProgressTracker(graphics, data, slotBarX, slotBarY, slotBarW, slotBarH);

        graphics.pose().popPose();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderTreeCanvas(GuiGraphics graphics, SystemData data, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Fondo del visor
        graphics.fill(x, y, x + w, y + h, 0xFF060B10);
        drawBorder(graphics, x, y, w, h, 0xFF153344);

        // Tijera de recorte para el lienzo navegable
        enableScissorScaled(graphics, x + 1, y + 1, x + w - 1, y + h - 1);

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + camX, y + camY, 0);
        pose.scale(zoom, zoom, 1.0f);

        // Fondo de cuadrícula y columnas MU Online
        int gridSpacing = 24;
        for (int gx = -100; gx < 620; gx += gridSpacing) {
            for (int gy = -50; gy < 420; gy += gridSpacing) {
                graphics.fill(gx, gy, gx + 1, gy + 1, 0x221E4258);
            }
        }

        // Líneas divisorias de columnas en el lienzo (Solidity | Fighting Spirit | Ultimatum)
        graphics.fill(170, 48, 171, 360, 0x503A4E5C);
        graphics.fill(340, 48, 341, 360, 0x503A4E5C);

        // Puntos invertidos por rama (MU Online: Solidity, Fighting Spirit, Ultimatum)
        int ptsSolidity = ClassSkillTree.getPointsInBranch(data, data.hunterClass(), "branch_c");
        int ptsFighting = ClassSkillTree.getPointsInBranch(data, data.hunterClass(), "branch_a");
        int ptsUltimatum = ClassSkillTree.getPointsInBranch(data, data.hunterClass(), "branch_b");

        // Banners de cabecera de rama en coordenadas del mundo
        renderBranchHeader(graphics, 85, 52, Component.translatable("awakening_system.skill_tree.branch.solidity").getString() + ": " + ptsSolidity, 0xFF2A5060, 0xFF50FA7B);
        renderBranchHeader(graphics, 255, 52, Component.translatable("awakening_system.skill_tree.branch.fighting_spirit").getString() + ": " + ptsFighting, 0xFF602A2A, 0xFFFF7666);
        renderBranchHeader(graphics, 425, 52, Component.translatable("awakening_system.skill_tree.branch.ultimatum").getString() + ": " + ptsUltimatum, 0xFF502A60, 0xFFE056FD);

        // Transformación de ratón a coordenadas locales del mundo
        float worldMouseX = (float) ((mouseX - (x + camX)) / zoom);
        float worldMouseY = (float) ((mouseY - (y + camY)) / zoom);

        List<ClassSkillTree.SkillNode> nodes = ClassSkillTree.getNodesForClass(data.hunterClass());

        // A. Dibujar líneas de conexión entre nodos revelados
        for (ClassSkillTree.SkillNode node : nodes) {
            if (!ClassSkillTree.isRevealed(data, node)) continue; // Niebla de guerra

            int nx = node.gridX();
            int ny = node.gridY();

            for (String prereqId : node.prerequisiteIds()) {
                ClassSkillTree.SkillNode parent = ClassSkillTree.getNode(data.hunterClass(), prereqId);
                if (parent == null || !ClassSkillTree.isRevealed(data, parent)) continue;

                int px = parent.gridX();
                int py = parent.gridY();

                boolean bothUnlocked = data.skills().containsKey(node.skillId()) && data.skills().containsKey(parent.skillId());
                boolean parentUnlocked = data.skills().containsKey(parent.skillId());

                if (bothUnlocked) {
                    // Oro brillante para camino activo
                    drawThickLine(graphics, px, py, nx, ny, 2, 0xFFFFD700);
                    drawDownwardArrow(graphics, nx, ny, 14, 0xFFFFD700);
                } else if (parentUnlocked) {
                    // Cian suave para camino disponible
                    drawThickLine(graphics, px, py, nx, ny, 1, 0x9000E5FF);
                    drawDownwardArrow(graphics, nx, ny, 14, 0x9000E5FF);
                }
            }
        }

        // B. Dibujar Nodos Cuadrados RPG (Estilo MU Online Master Skill Tree)
        for (ClassSkillTree.SkillNode node : nodes) {
            if (!ClassSkillTree.isRevealed(data, node)) continue; // Oculto en la niebla ("no se deberían de ver")

            int nx = node.gridX();
            int ny = node.gridY();
            int half = node.isRoot() ? 14 : (node.isAttribute() ? 10 : 12);

            boolean hovered = worldMouseX >= nx - half - 1 && worldMouseX <= nx + half + 1 && worldMouseY >= ny - half - 1 && worldMouseY <= ny + half + 1;
            boolean isUnlocked = data.skills().containsKey(node.skillId());
            boolean canUnlock = !isUnlocked && ClassSkillTree.canUnlock(data, node.skillId());
            boolean isSelected = node.skillId().equals(selectedSkillId);

            int slotBg = isUnlocked ? (hovered ? 0xFF2A2006 : 0xFF161205) : (canUnlock ? (hovered ? 0xFF092E3D : 0xFF061B24) : (hovered ? 0xFF22262B : 0xFF121417));
            int borderColor = isSelected ? 0xFFFFFFFF : (isUnlocked ? 0xFFFFD700 : (canUnlock ? 0xFF00E5FF : 0xFF3E4F5B));

            if (canUnlock && !isSelected) {
                int alpha = 180 + (int) (Math.sin(tickCount * 0.15D) * 75);
                borderColor = (alpha << 24) | 0x00E5FF;
            }

            // Ranura Cuadrada con bisel RPG
            drawSquareSlot(graphics, nx, ny, half, slotBg, borderColor, node.isRoot());

            // Icono / Glifo
            String glyph = node.iconSymbol();
            int glyphColor = isUnlocked ? 0xFFFFD700 : (canUnlock ? 0xFF50FA7B : 0xFF88A0B0);
            graphics.drawCenteredString(font, Component.literal(glyph), nx, ny - 4, glyphColor);

            // Contador de rango estilo MU Online al lado del cuadro (ej. 15, 10, 3, 0)
            int rankNum;
            if (node.isRoot()) {
                rankNum = isUnlocked ? 1 : 0;
            } else if (node.isAttribute()) {
                rankNum = isUnlocked ? 1 : 0;
            } else {
                rankNum = data.skills().getOrDefault(node.skillId(), 0);
            }

            int rankColor = isUnlocked ? 0xFFFFD700 : (canUnlock ? 0xFF00E5FF : 0xFF667788);
            graphics.drawString(font, Component.literal(String.valueOf(rankNum)), nx + half + 3, ny - 4, rankColor);

            // Indicador de subida disponible
            if (!node.isAttribute() && !node.isRoot() && isUnlocked && ClassSkillTree.canUpgrade(data, node.skillId())) {
                graphics.drawCenteredString(font, Component.literal("▲").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), nx, ny - half - 8, 0xFF50FA7B);
            }
        }

        pose.popPose();
        graphics.disableScissor();

        // Controles de interfaz fijos sobre el lienzo
        // Indicador de navegación y nivel de Zoom
        String zoomPercent = String.format(Locale.US, "%.0f%%", zoom * 100);
        graphics.drawString(font, Component.translatable("awakening_system.skill_tree.zoom_controls", zoomPercent), x + 8, y + h - 14, 0xFF446677);

        // Botón de recentrado [ ⌖ ]
        int resetBtnX = x + w - 24;
        int resetBtnY = y + 6;
        boolean resetHov = mouseX >= resetBtnX && mouseX < resetBtnX + 18 && mouseY >= resetBtnY && mouseY < resetBtnY + 18;
        graphics.fill(resetBtnX, resetBtnY, resetBtnX + 18, resetBtnY + 18, resetHov ? 0xFF1C382E : 0xFF0A1822);
        drawBorder(graphics, resetBtnX, resetBtnY, 18, 18, resetHov ? 0xFFFFD700 : 0xFF2D7691);
        graphics.drawCenteredString(font, Component.literal("⌖"), resetBtnX + 9, resetBtnY + 5, resetHov ? 0xFFFFD700 : 0xFF88AABB);
    }

    private void renderBranchHeader(GuiGraphics g, int cx, int cy, String text, int bg, int textColor) {
        int halfW = 56;
        g.fill(cx - halfW, cy - 6, cx + halfW, cy + 8, bg);
        drawBorder(g, cx - halfW, cy - 6, halfW * 2, 14, 0x80FFA500);
        g.drawCenteredString(font, Component.literal(text).withStyle(ChatFormatting.BOLD), cx, cy - 4, textColor);
    }

    private void drawSquareSlot(GuiGraphics g, int cx, int cy, int half, int bg, int border, boolean isRoot) {
        // Fondo
        g.fill(cx - half, cy - half, cx + half + 1, cy + half + 1, bg);
        // Marco exterior
        drawBorder(g, cx - half, cy - half, half * 2 + 1, half * 2 + 1, border);
        if (isRoot) {
            // Doble marco para raíz
            drawBorder(g, cx - half + 2, cy - half + 2, half * 2 - 3, half * 2 - 3, 0x80FFA500);
        }
    }

    private void drawDownwardArrow(GuiGraphics g, int nx, int ny, int half, int color) {
        // Flecha direccional hacia abajo sobre el nodo objetivo
        int tipY = ny - half - 1;
        g.fill(nx - 2, tipY - 2, nx + 3, tipY - 1, color);
        g.fill(nx - 1, tipY - 1, nx + 2, tipY, color);
        g.fill(nx, tipY, nx + 1, tipY + 1, color);
    }

    private void renderInspectionPanel(GuiGraphics graphics, SystemData data, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Fondo y marco
        graphics.fill(x, y, x + w, y + h, 0xF5060D14);
        drawBorder(graphics, x, y, w, h, 0xFF1D3B4E);
        graphics.fill(x, y, x + w, y + 2, 0xFFFFD700);

        int innerW = w - 16;

        if (selectedSkillId == null) {
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.inspection").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), x + w / 2, y + 6, 0xFFFFD700);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.select_skill"), x + w / 2, y + h / 2 - 12, 0xFF888888);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.select_skill_sub1"), x + w / 2, y + h / 2, 0xFF555555);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.select_skill_sub2"), x + w / 2, y + h / 2 + 12, 0xFF555555);
            return;
        }

        ClassSkillTree.SkillNode node = ClassSkillTree.getNode(data.hunterClass(), selectedSkillId);
        if (node == null) {
            selectedSkillId = null;
            return;
        }

        int curY = y + 8;

        // 1. Título (Anti-Desborde con split estricto)
        List<FormattedCharSequence> titleLines = font.split(node.getName().copy().withStyle(ChatFormatting.BOLD), innerW);
        for (FormattedCharSequence line : titleLines) {
            graphics.drawString(font, line, x + 8, curY, rarityColor(node.getRarity()));
            curY += 10;
        }
        curY += 2;

        // 2. Tipo y Rango
        Component typeStr = node.isRoot()
                ? Component.translatable("awakening_system.skill_tree.node.master")
                : (node.isAttribute()
                ? Component.translatable("awakening_system.skill_tree.node.attribute")
                : (node.isActive()
                ? Component.translatable("awakening_system.skill_tree.node.active")
                : Component.translatable("awakening_system.skill_tree.node.passive")));
        graphics.drawString(font, typeStr, x + 8, curY, 0xFF00E5FF);
        curY += 10;

        if (!node.isAttribute() && !node.isRoot()) {
            int curLvl = data.skills().getOrDefault(node.skillId(), 0);
            if (curLvl > 0) {
                Component rankTitle = ClassSkillTree.getRankTitle(curLvl);
                String stars = ClassSkillTree.getRankStars(curLvl);
                graphics.drawString(font, Component.translatable("awakening_system.skill_tree.rank_format", rankTitle, stars), x + 8, curY, 0xFFFFD700);
                curY += 11;
            } else {
                graphics.drawString(font, Component.translatable("awakening_system.skill_tree.not_learned"), x + 8, curY, 0xFFAAAAAA);
                curY += 11;
            }
        }

        // Separador
        graphics.fill(x + 8, curY, x + w - 8, curY + 1, 0xFF1D3B4E);
        curY += 5;

        // 3. Descripción (Wrap limpio)
        List<FormattedCharSequence> descLines = font.split(node.getDescription(), innerW);
        for (int i = 0; i < Math.min(3, descLines.size()); i++) {
            graphics.drawString(font, descLines.get(i), x + 8, curY, 0xFFCCE3EC);
            curY += 10;
        }
        curY += 3;

        // 4. Efecto
        Component eff = node.getEffect();
        if (!eff.getString().isEmpty()) {
            graphics.drawString(font, Component.translatable("awakening_system.skill_tree.effect_label"), x + 8, curY, 0xFF70E8FF);
            curY += 10;
            List<FormattedCharSequence> effLines = font.split(eff, innerW);
            for (FormattedCharSequence line : effLines) {
                graphics.drawString(font, line, x + 8, curY, 0xFF88C999);
                curY += 10;
            }
            curY += 3;
        }

        // 5. Cuadro de Mejora de Rango (si aplica)
        if (!node.isAttribute() && !node.isRoot()) {
            int curLvl = data.skills().getOrDefault(node.skillId(), 0);
            if (curLvl >= 1 && curLvl < ClassSkillTree.MAX_SKILL_LEVEL) {
                int nextRank = curLvl + 1;
                String nextRoman = nextRank == 2 ? "II" : "III";
                Component bonusDesc = ClassSkillTree.getUpgradeBonusDescription(node, nextRank);

                int boxY = y + h - 68;
                int boxH = 34;
                graphics.fill(x + 8, boxY, x + w - 8, boxY + boxH, 0x80181500);
                drawBorder(graphics, x + 8, boxY, w - 16, boxH, 0x80FFD700);

                graphics.drawString(font, Component.translatable("awakening_system.skill_tree.upgrade_to_rank", nextRoman), x + 12, boxY + 4, 0xFFFFD700);
                List<FormattedCharSequence> bLines = font.split(bonusDesc, innerW - 8);
                int by = boxY + 14;
                for (FormattedCharSequence bl : bLines) {
                    graphics.drawString(font, bl, x + 12, by, 0xFF50FA7B);
                    by += 9;
                }
            }
        }

        // 6. Botón de Acción Interactivo
        int btnX = x + 8;
        int btnY = y + h - 28;
        int btnW = w - 16;
        int btnH = 22;

        boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        boolean isUnlocked = data.skills().containsKey(node.skillId());
        int curLvl = data.skills().getOrDefault(node.skillId(), 0);

        if (node.isRoot() || (node.isAttribute() && isUnlocked) || curLvl >= ClassSkillTree.MAX_SKILL_LEVEL) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF14241C);
            drawBorder(graphics, btnX, btnY, btnW, btnH, 0xFF50FA7B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.mastered").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, 0xFFFFD700);
        } else if (isUnlocked && ClassSkillTree.canUpgrade(data, node.skillId())) {
            int nextRank = curLvl + 1;
            String nextRoman = nextRank == 2 ? "II" : "III";
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF2E6B45 : 0xFF1A462C);
            drawBorder(graphics, btnX, btnY, btnW, btnH, btnHovered ? 0xFFFFFFFF : 0xFF50FA7B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.upgrade_btn", nextRoman).withStyle(ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, btnHovered ? 0xFFFFFFFF : 0xFFFFD700);
        } else if (isUnlocked) {
            int nextRank = curLvl + 1;
            int reqLvl = ClassSkillTree.getRequiredLevelForRank(node, nextRank);
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF241414);
            drawBorder(graphics, btnX, btnY, btnW, btnH, 0xFF663333);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.req_lvl_btn", reqLvl).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, 0xFFFF7666);
        } else if (ClassSkillTree.canUnlock(data, node.skillId())) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF007A99 : 0xFF00556B);
            drawBorder(graphics, btnX, btnY, btnW, btnH, btnHovered ? 0xFFFFFFFF : 0xFF00E5FF);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.unlock_btn").withStyle(ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, btnHovered ? 0xFFFFFFFF : 0xFF00E5FF);
        } else {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF1A1A1A);
            drawBorder(graphics, btnX, btnY, btnW, btnH, 0xFF333333);
            Component reason = data.level() < node.requiredLevel()
                    ? Component.translatable("awakening_system.skill_tree.req_lvl", node.requiredLevel())
                    : Component.translatable("awakening_system.skill_tree.insufficient_sp");
            graphics.drawCenteredString(font, Component.literal("🔒 ").append(reason).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, 0xFF888888);
        }
    }

    private void renderSlotProgressTracker(GuiGraphics graphics, SystemData data, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xE0060D12);
        drawBorder(graphics, x, y, w, h, 0xFF153344);

        int activeUnlocked = data.getUnlockedActiveSkillSlots();
        int passiveUnlocked = data.getUnlockedPassiveSkillSlots();

        Component title = Component.translatable("awakening_system.skill_tree.combat_slots_title");
        graphics.drawString(font, title, x + 8, y + 4, 0xFFFFD700);

        String[] actKeys = {"V", "R", "G", "B", "X"};
        int[] actReqs = {0, 0, 1, 3, 5};
        int actStartX = x + 8;
        int actY = y + 16;
        int activeBoxW = 44;

        for (int i = 0; i < 5; i++) {
            int sx = actStartX + i * (activeBoxW + 4);
            boolean isUnlocked = i < activeUnlocked;
            int bg = isUnlocked ? 0xFF142E24 : 0xFF10161A;
            int border = isUnlocked ? 0xFF50FA7B : 0xFF334450;
            graphics.fill(sx, actY, sx + activeBoxW, actY + 12, bg);
            drawBorder(graphics, sx, actY, activeBoxW, 12, border);

            Component label = isUnlocked
                    ? Component.literal("[" + actKeys[i] + "] ✔")
                    : Component.translatable("awakening_system.skill_tree.slot_req", actReqs[i]);
            graphics.drawCenteredString(font, label, sx + activeBoxW / 2, actY + 2, isUnlocked ? 0xFF50FA7B : 0xFF777777);
        }

        int sepX = actStartX + 5 * (activeBoxW + 4) + 6;
        graphics.fill(sepX, actY, sepX + 1, actY + 12, 0xFF2D7691);

        int[] pasReqs = {0, 2, 4};
        int pasStartX = sepX + 8;
        int passiveBoxW = 54;

        for (int i = 0; i < 3; i++) {
            int sx = pasStartX + i * (passiveBoxW + 4);
            boolean isUnlocked = i < passiveUnlocked;
            int bg = isUnlocked ? 0xFF2A1538 : 0xFF10161A;
            int border = isUnlocked ? 0xFFE056FD : 0xFF334450;
            graphics.fill(sx, actY, sx + passiveBoxW, actY + 12, bg);
            drawBorder(graphics, sx, actY, passiveBoxW, 12, border);

            Component label = isUnlocked
                    ? Component.translatable("awakening_system.skill_tree.passive_slot", (i + 1))
                    : Component.translatable("awakening_system.skill_tree.slot_req", pasReqs[i]);
            graphics.drawCenteredString(font, label, sx + passiveBoxW / 2, actY + 2, isUnlocked ? 0xFFE056FD : 0xFF777777);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int panelW = 620;
        int panelH = 360;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        int closeBtnW = 60;
        int closeBtnH = 18;
        int closeBtnX = left + panelW - closeBtnW - 10;
        int closeBtnY = top + 8;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY < closeBtnY + closeBtnH) {
            playClickSound();
            onClose();
            return true;
        }

        int canvasX = left + 8;
        int canvasY = top + 32;
        int canvasW = panelW - 232;
        int canvasH = panelH - 74;

        // Botón recentrar [ ⌖ ]
        int resetBtnX = canvasX + canvasW - 24;
        int resetBtnY = canvasY + 6;
        if (mouseX >= resetBtnX && mouseX < resetBtnX + 18 && mouseY >= resetBtnY && mouseY < resetBtnY + 18) {
            playClickSound();
            camX = 0;
            camY = 0;
            zoom = 1.0f;
            return true;
        }

        // Clic en el botón de acción de la consola lateral
        int inspX = left + panelW - 220;
        int inspY = top + 32;
        int inspW = 212;
        int inspH = panelH - 74;
        int btnX = inspX + 8;
        int btnY = inspY + inspH - 28;
        int btnW = inspW - 16;
        int btnH = 22;

        if (selectedSkillId != null && mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null) {
                if (ClassSkillTree.canUpgrade(data, selectedSkillId)) {
                    playUpgradeSound();
                    PacketDistributor.sendToServer(new UpgradeSkillTreeNodePayload(selectedSkillId));
                    return true;
                } else if (ClassSkillTree.canUnlock(data, selectedSkillId)) {
                    playClickSound();
                    PacketDistributor.sendToServer(new UnlockSkillTreeNodePayload(selectedSkillId));
                    return true;
                }
            }
        }

        // Clic en nodos dentro del lienzo (Transformando con zoom y cámara)
        if (mouseX >= canvasX && mouseX < canvasX + canvasW && mouseY >= canvasY && mouseY < canvasY + canvasH) {
            float worldMouseX = (float) ((mouseX - (canvasX + camX)) / zoom);
            float worldMouseY = (float) ((mouseY - (canvasY + camY)) / zoom);

            SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null && data.hasHunterClass()) {
                List<ClassSkillTree.SkillNode> nodes = ClassSkillTree.getNodesForClass(data.hunterClass());
                for (ClassSkillTree.SkillNode node : nodes) {
                    if (!ClassSkillTree.isRevealed(data, node)) continue;

                    int nx = node.gridX();
                    int ny = node.gridY();
                    int half = node.isRoot() ? 14 : (node.isAttribute() ? 10 : 12);

                    if (worldMouseX >= nx - half - 1 && worldMouseX <= nx + half + 1 && worldMouseY >= ny - half - 1 && worldMouseY <= ny + half + 1) {
                        playClickSound();
                        selectedSkillId = node.skillId();
                        return true;
                    }
                }
            }

            // Si se hace clic en área vacía del lienzo, iniciar arrastre
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = getGuiScale();
        if (isDragging) {
            camX += dragX / scale;
            camY += dragY / scale;

            // Límites para evitar perder el árbol
            camX = Math.max(-500.0 * zoom, Math.min(300.0, camX));
            camY = Math.max(-400.0 * zoom, Math.min(250.0, camY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int panelW = 620;
        int panelH = 360;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int canvasX = left + 8;
        int canvasY = top + 32;
        int canvasW = panelW - 232;
        int canvasH = panelH - 74;

        if (mouseX >= canvasX && mouseX < canvasX + canvasW && mouseY >= canvasY && mouseY < canvasY + canvasH) {
            float oldZoom = zoom;
            if (scrollY > 0) {
                zoom = Math.min(2.2f, zoom + 0.15f);
            } else if (scrollY < 0) {
                zoom = Math.max(0.55f, zoom - 0.15f);
            }

            if (zoom != oldZoom) {
                double relX = mouseX - canvasX;
                double relY = mouseY - canvasY;
                camX = relX - (relX - camX) * (zoom / oldZoom);
                camY = relY - (relY - camY) * (zoom / oldZoom);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void drawThickLine(GuiGraphics g, int x1, int y1, int x2, int y2, int thickness, int color) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        float xInc = (float) dx / steps;
        float yInc = (float) dy / steps;
        float curX = x1;
        float curY = y1;
        int halfT = thickness / 2;
        for (int i = 0; i <= steps; i++) {
            int ix = Math.round(curX);
            int iy = Math.round(curY);
            g.fill(ix - halfT, iy - halfT, ix + halfT + 1, iy + halfT + 1, color);
            curX += xInc;
            curY += yInc;
        }
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private int rarityColor(SkillDefinitions.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFAAAAAA;
            case UNCOMMON -> 0xFF55FF55;
            case RARE -> 0xFF55FFFF;
            case EPIC -> 0xFFFF55FF;
            case LEGENDARY -> 0xFFFFAA00;
            case MYTHIC -> 0xFFFF3333;
            case UNIQUE -> 0xFFFFD700;
        };
    }
}
