package com.soybe.awakeningsystem.fable;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class FablesScreen extends Screen {
    private final Screen parentScreen;
    private FableDefinitions.Grade selectedGradeFilter = null; // null = TODAS
    private String selectedFableId = "first_netherite";
    private int scrollOffset = 0;

    public FablesScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.fables_screen.window_title"));
        this.parentScreen = parentScreen;
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

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    private void playClickSound() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Evita desenfoque vanilla
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 560.0F;
        float scaleY = (float) this.height / 360.0F;
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

        int panelW = 540;
        int panelH = 340;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        int unlockedCount = data != null ? data.unlockedFables().size() : 0;
        int fableWeight = data != null ? data.fableWeight() : 0;
        boolean isConstellation = data != null && data.isConstellation();

        // Marco y Fondo principal (Estilo Star Stream / Nebulosa cósmica)
        graphics.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xC000838F);
        graphics.fill(left, top, left + panelW, top + panelH, 0xF207131D);

        // Esquinas holográficas
        int cornerColor = 0xFFFFD700;
        graphics.fill(left, top, left + 10, top + 2, cornerColor);
        graphics.fill(left, top, left + 2, top + 10, cornerColor);
        graphics.fill(left + panelW - 10, top, left + panelW, top + 2, cornerColor);
        graphics.fill(left + panelW - 2, top, left + panelW, top + 10, cornerColor);
        graphics.fill(left, top + panelH - 2, left + 10, top + panelH, cornerColor);
        graphics.fill(left, top + panelH - 10, left + 2, top + panelH, cornerColor);
        graphics.fill(left + panelW - 10, top + panelH - 2, left + panelW, top + panelH, cornerColor);
        graphics.fill(left + panelW - 2, top + panelH - 10, left + panelW, top + panelH, cornerColor);

        // 1. Cabecera
        graphics.drawString(font, Component.translatable("awakening_system.fables_screen.header").withStyle(ChatFormatting.BOLD), left + 14, top + 10, 0xFFFFD700);

        Component ascComp = isConstellation ? Component.translatable("awakening_system.fables_screen.constellation_ascended") :
                (unlockedCount >= 5 && fableWeight >= 150 ? Component.translatable("awakening_system.fables_screen.ready_for_ascension") : Component.translatable("awakening_system.fables_screen.req_ascension"));
        Component subHeader = Component.translatable("awakening_system.fables_screen.stats", unlockedCount, fableWeight, ascComp);
        graphics.drawString(font, subHeader, left + 14, top + 22, 0xFFCCE3EC);

        // Botón Cerrar
        int closeBtnW = 60;
        int closeBtnH = 18;
        int closeBtnX = left + panelW - closeBtnW - 10;
        int closeBtnY = top + 8;
        boolean closeHov = mX >= closeBtnX && mX < closeBtnX + closeBtnW && mY >= closeBtnY && mY < closeBtnY + closeBtnH;
        graphics.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnW, closeBtnY + closeBtnH, closeHov ? 0xFF882222 : 0xFF142430);
        drawBorder(graphics, closeBtnX, closeBtnY, closeBtnW, closeBtnH, closeHov ? 0xFFFF5555 : 0xFF2D7691);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.close"), closeBtnX + closeBtnW / 2, closeBtnY + 5, closeHov ? 0xFFFFFFFF : 0xFFCCE3EC);

        // 2. Barra de Filtros de Grado
        int filterY = top + 36;
        renderFilterBar(graphics, left + 12, filterY, panelW - 24, mX, mY);

        // 3. Panel Izquierdo: Lista de Fábulas
        int listX = left + 12;
        int listY = top + 58;
        int listW = panelW - 228;
        int listH = panelH - 68;
        renderFableList(graphics, data, listX, listY, listW, listH, mX, mY);

        // 4. Panel Derecho: "Libro de los Relatos" (Detalle e Inspección)
        int inspX = left + panelW - 210;
        int inspY = top + 58;
        int inspW = 198;
        int inspH = panelH - 68;
        renderFableDetail(graphics, data, inspX, inspY, inspW, inspH);

        graphics.pose().popPose();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderFilterBar(GuiGraphics g, int x, int y, int w, int mouseX, int mouseY) {
        Component[] filterLabels = {
                Component.translatable("awakening_system.fables_screen.filter.all"),
                Component.translatable("awakening_system.fables_screen.filter.historical"),
                Component.translatable("awakening_system.fables_screen.filter.legendary"),
                Component.translatable("awakening_system.fables_screen.filter.semi_myth"),
                Component.translatable("awakening_system.fables_screen.filter.myth"),
                Component.translatable("awakening_system.fables_screen.filter.giant")
        };
        FableDefinitions.Grade[] filterGrades = {null, FableDefinitions.Grade.HISTORICAL, FableDefinitions.Grade.LEGENDARY, FableDefinitions.Grade.SEMI_MYTH, FableDefinitions.Grade.MYTH, FableDefinitions.Grade.GIANT_STORY};

        int btnW = (w - (filterLabels.length - 1) * 4) / filterLabels.length;
        int btnH = 16;

        for (int i = 0; i < filterLabels.length; i++) {
            int bx = x + i * (btnW + 4);
            boolean selected = (selectedGradeFilter == filterGrades[i]);
            boolean hov = mouseX >= bx && mouseX < bx + btnW && mouseY >= y && mouseY < y + btnH;

            int bg = selected ? 0xFF1E4258 : (hov ? 0xFF142E3D : 0xFF0D1C26);
            int border = selected ? 0xFFFFD700 : (hov ? 0xFF00E5FF : 0xFF223E4F);
            g.fill(bx, y, bx + btnW, y + btnH, bg);
            drawBorder(g, bx, y, btnW, btnH, border);

            int textColor = selected ? 0xFFFFD700 : (hov ? 0xFFFFFFFF : 0xFF88AABB);
            g.drawCenteredString(font, filterLabels[i].copy().withStyle(ChatFormatting.BOLD), bx + btnW / 2, y + 4, textColor);
        }
    }

    private List<FableDefinitions.Fable> getFilteredFables() {
        List<FableDefinitions.Fable> list = new ArrayList<>();
        for (FableDefinitions.Fable f : FableDefinitions.getAll()) {
            if (selectedGradeFilter == null || f.grade() == selectedGradeFilter) {
                list.add(f);
            }
        }
        return list;
    }

    private void renderFableList(GuiGraphics g, SystemData data, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.fill(x, y, x + w, y + h, 0xFF050E14);
        drawBorder(g, x, y, w, h, 0xFF153344);

        List<FableDefinitions.Fable> fables = getFilteredFables();
        int itemH = 34;
        int maxVisible = h / itemH;
        int maxScroll = Math.max(0, fables.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

        enableScissorScaled(g, x + 1, y + 1, x + w - 1, y + h - 1);

        for (int i = 0; i < fables.size(); i++) {
            int iy = y + 2 + (i - scrollOffset) * itemH;
            if (iy < y - itemH || iy > y + h) continue;

            FableDefinitions.Fable fable = fables.get(i);
            boolean isUnlocked = data != null && data.unlockedFables().contains(fable.id());
            boolean isSelected = fable.id().equals(selectedFableId);
            boolean hov = mouseX >= x + 2 && mouseX < x + w - 2 && mouseY >= iy && mouseY < iy + itemH - 2;

            int bg = isSelected ? 0xFF1A384A : (isUnlocked ? (hov ? 0xFF162B38 : 0xFF0B1B24) : (hov ? 0xFF16191D : 0xFF0C0E10));
            int border = isSelected ? 0xFFFFFFFF : (isUnlocked ? 0xFFFFD700 : (hov ? 0xFF00E5FF : 0xFF243B4A));

            g.fill(x + 2, iy, x + w - 2, iy + itemH - 2, bg);
            drawBorder(g, x + 2, iy, w - 4, itemH - 2, border);

            // Icono
            int iconBoxW = 24;
            int iconBg = isUnlocked ? 0xFF241C06 : 0xFF121416;
            g.fill(x + 4, iy + 2, x + 4 + iconBoxW, iy + itemH - 4, iconBg);
            drawBorder(g, x + 4, iy + 2, iconBoxW, itemH - 6, isUnlocked ? 0xFFFFD700 : 0xFF354450);
            g.drawCenteredString(font, Component.literal(fable.icon()), x + 4 + iconBoxW / 2, iy + 9, isUnlocked ? 0xFFFFD700 : 0xFF778899);

            // Título
            int titleX = x + 34;
            int titleCol = isUnlocked ? fable.grade().hexColor() : 0xFF888888;
            g.drawString(font, fable.getTitleComponent().copy().withStyle(ChatFormatting.BOLD), titleX, iy + 4, titleCol);

            // Sub-línea: Estado y Peso Narrativo
            Component statusComp = isUnlocked ? Component.translatable("awakening_system.fables_screen.unlocked") : Component.translatable("awakening_system.fables_screen.locked");
            Component weightComp = Component.translatable("awakening_system.fables_screen.weight", fable.weight());
            Component firstComp = fable.isWorldFirst() ? Component.translatable("awakening_system.fables_screen.world_first_badge") : Component.empty();
            Component itemSub = statusComp.copy().append(Component.literal(" ")).append(weightComp).append(firstComp);
            g.drawString(font, itemSub, titleX, iy + 17, 0xFFCCE3EC);
        }

        g.disableScissor();

        // Barra de Scroll si hay más elementos
        if (fables.size() > maxVisible) {
            int sbX = x + w - 5;
            int sbH = h - 4;
            int thumbH = Math.max(16, (int) ((float) maxVisible / fables.size() * sbH));
            int thumbY = y + 2 + (int) ((float) scrollOffset / maxScroll * (sbH - thumbH));
            g.fill(sbX, y + 2, sbX + 3, y + h - 2, 0xFF0D1C26);
            g.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, 0xFF00E5FF);
        }
    }

    private void renderFableDetail(GuiGraphics g, SystemData data, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xF5060E16);
        drawBorder(g, x, y, w, h, 0xFF1D3B4E);
        g.fill(x, y, x + w, y + 2, 0xFFFFD700);

        FableDefinitions.Fable fable = FableDefinitions.get(selectedFableId);
        if (fable == null) {
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.empty_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), x + w / 2, y + 8, 0xFFFFD700);
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.empty_desc1"), x + w / 2, y + h / 2 - 8, 0xFF888888);
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.empty_desc2"), x + w / 2, y + h / 2 + 4, 0xFF555555);
            return;
        }

        boolean isUnlocked = data != null && data.unlockedFables().contains(fable.id());
        int innerW = w - 16;
        int curY = y + 8;

        // Grado
        Component gradeBadge = Component.translatable("awakening_system.fables_screen.grade_badge", fable.grade().getDisplayName()).withStyle(ChatFormatting.BOLD);
        g.drawString(font, gradeBadge, x + 8, curY, fable.grade().hexColor());
        curY += 12;

        // Título de la Fábula
        List<FormattedCharSequence> titleLines = font.split(fable.getTitleComponent().copy().withStyle(ChatFormatting.BOLD), innerW);
        for (FormattedCharSequence tl : titleLines) {
            g.drawString(font, tl, x + 8, curY, 0xFFFFFFFF);
            curY += 10;
        }
        curY += 3;

        // Peso Narrativo
        g.drawString(font, Component.translatable("awakening_system.fables_screen.weight_detail", fable.weight()), x + 8, curY, 0xFFFFD700);
        curY += 12;

        // Banner de Primacía Mundial si aplica
        if (fable.isWorldFirst()) {
            g.fill(x + 6, curY, x + w - 6, curY + 22, 0x802B1E05);
            drawBorder(g, x + 6, curY, w - 12, 22, 0xFFFFD700);
            g.drawString(font, Component.translatable("awakening_system.fables_screen.world_first_title"), x + 10, curY + 3, 0xFFFFD700);
            g.drawString(font, Component.translatable("awakening_system.fables_screen.world_first_desc"), x + 10, curY + 12, 0xFFDDEEFF);
            curY += 26;
        }

        // Separador
        g.fill(x + 8, curY, x + w - 8, curY + 1, 0xFF1D3B4E);
        curY += 6;

        // Descripción / Lore
        g.drawString(font, Component.translatable("awakening_system.fables_screen.recorded_story"), x + 8, curY, 0xFF70E8FF);
        curY += 10;

        List<FormattedCharSequence> descLines = font.split(fable.getDescriptionComponent(), innerW);
        for (int i = 0; i < Math.min(6, descLines.size()); i++) {
            g.drawString(font, descLines.get(i), x + 8, curY, 0xFFCCE3EC);
            curY += 10;
        }
        curY += 4;

        // Estado al pie
        int stateBoxY = y + h - 26;
        int stateBg = isUnlocked ? 0xFF162D1D : 0xFF241414;
        int stateBorder = isUnlocked ? 0xFF50FA7B : 0xFF663333;
        g.fill(x + 8, stateBoxY, x + w - 8, stateBoxY + 20, stateBg);
        drawBorder(g, x + 8, stateBoxY, w - 16, 20, stateBorder);

        Component stateLabel = isUnlocked ? Component.translatable("awakening_system.fables_screen.soul_engraved") : Component.translatable("awakening_system.fables_screen.unwritten");
        int stateCol = isUnlocked ? 0xFF50FA7B : 0xFFFF7666;
        g.drawCenteredString(font, stateLabel.copy().withStyle(ChatFormatting.BOLD), x + w / 2, stateBoxY + 6, stateCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int panelW = 540;
        int panelH = 340;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        // Botón Cerrar
        int closeBtnW = 60;
        int closeBtnH = 18;
        int closeBtnX = left + panelW - closeBtnW - 10;
        int closeBtnY = top + 8;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY < closeBtnY + closeBtnH) {
            playClickSound();
            onClose();
            return true;
        }

        // Filtros de Grado
        int filterY = top + 36;
        int filterW = panelW - 24;
        String[] filterNames = {"TODAS", "HISTÓRICAS", "LEGENDARIAS", "SEMI-MÍTICAS", "MÍTICAS", "GIGANTES"};
        FableDefinitions.Grade[] filterGrades = {null, FableDefinitions.Grade.HISTORICAL, FableDefinitions.Grade.LEGENDARY, FableDefinitions.Grade.SEMI_MYTH, FableDefinitions.Grade.MYTH, FableDefinitions.Grade.GIANT_STORY};
        int btnW = (filterW - (filterNames.length - 1) * 4) / filterNames.length;

        for (int i = 0; i < filterNames.length; i++) {
            int bx = left + 12 + i * (btnW + 4);
            if (mouseX >= bx && mouseX < bx + btnW && mouseY >= filterY && mouseY < filterY + 16) {
                playClickSound();
                selectedGradeFilter = filterGrades[i];
                scrollOffset = 0;
                return true;
            }
        }

        // Clic en elementos de la lista de fábulas
        int listX = left + 12;
        int listY = top + 58;
        int listW = panelW - 228;
        int listH = panelH - 68;

        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            List<FableDefinitions.Fable> fables = getFilteredFables();
            int itemH = 34;
            for (int i = 0; i < fables.size(); i++) {
                int iy = listY + 2 + (i - scrollOffset) * itemH;
                if (mouseY >= iy && mouseY < iy + itemH - 2) {
                    playClickSound();
                    selectedFableId = fables.get(i).id();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int panelW = 540;
        int panelH = 340;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int listX = left + 12;
        int listY = top + 58;
        int listW = panelW - 228;
        int listH = panelH - 68;

        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            List<FableDefinitions.Fable> fables = getFilteredFables();
            int maxVisible = listH / 34;
            int maxScroll = Math.max(0, fables.size() - maxVisible);
            if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            } else if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
