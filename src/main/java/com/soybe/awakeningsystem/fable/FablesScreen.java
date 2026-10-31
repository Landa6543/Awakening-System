package com.soybe.awakeningsystem.fable;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Manhwa / Solo Leveling Fables & Star Stream Chronicles Screen.
 * Fully aligned to the official System Window Frame architecture with expansive proportions.
 */
public final class FablesScreen extends Screen {
    private static final int PANEL_W = 600;
    private static final int PANEL_H = 340;

    private final Screen parentScreen;
    private FableDefinitions.Grade selectedGradeFilter = null;
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
            minecraft.setScreen(parentScreen != null ? parentScreen : new com.soybe.awakeningsystem.SystemScreen(0));
        }
    }

    private void playClickSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Clean - no dark murky screen-wide filter
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 620.0F;
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
        renderBackground(graphics, mouseX, mouseY, partialTick);

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

        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        int unlockedCount = data != null ? data.unlockedFables().size() : 0;
        int fableWeight = data != null ? data.fableWeight() : 0;
        boolean isConstellation = data != null && data.isConstellation();

        ModernGuiHelper.GuiTheme theme = ModernGuiHelper.GuiTheme.SOLO_LEVELING;
        ModernGuiHelper.drawModernWindow(graphics, left, top, PANEL_W, PANEL_H, theme);

        // Header Title (Left) - Clear of the top-left filigree bracket
        int headerX = left + 36;
        int headerY = top + 18;
        Component headerTitle = ModernGuiHelper.manhwa(Component.translatable("awakening_system.fables_screen.header"));
        graphics.drawString(font, headerTitle, headerX, headerY, 0xFF00E5FF, false);

        // Close button [✕] (Right) - seamlessly integrated without dark block
        int closeX = left + PANEL_W - 32;
        int closeY = top + 18;
        int closeW = 16;
        int closeH = 16;
        boolean closeHover = mX >= closeX && mX < closeX + closeW && mY >= closeY && mY < closeY + closeH;
        if (closeHover) {
            graphics.fill(closeX, closeY, closeX + closeW, closeY + closeH, 0xDD7A1515);
            graphics.renderOutline(closeX, closeY, closeW, closeH, 0xFFFF5555);
        }
        graphics.drawCenteredString(font, "✕", closeX + closeW / 2, closeY + 4, closeHover ? 0xFFFFFFFF : 0xFF70E8FF);

        // Narrative Stats Pill Capsule (Center-Right)
        Component ascComp = isConstellation
                ? Component.translatable("awakening_system.fables_screen.constellation_ascended")
                : (com.soybe.awakeningsystem.ConstellationAscension.isEligibleForAscension(data)
                        ? Component.translatable("awakening_system.fables_screen.ready_for_ascension")
                        : Component.translatable("awakening_system.fables_screen.req_ascension"));
        String statsText = Component.translatable("awakening_system.fables_screen.stats", unlockedCount, fableWeight, ascComp.getString()).getString();
        int statW = font.width(statsText) + 14;
        int statX = closeX - 12 - statW;
        int headerEnd = headerX + font.width(headerTitle) + 8;
        if (statX <= headerEnd) {
            statsText = "§e" + unlockedCount + "/61 §8| §e" + fableWeight + " pts §8| " + ascComp.getString();
            statW = font.width(statsText) + 12;
            statX = closeX - 10 - statW;
        }

        if (statX > headerEnd) {
            boolean statHov = mX >= statX && mX < statX + statW && mY >= top + 15 && mY < top + 31;
            graphics.fill(statX, top + 15, statX + statW, top + 31, statHov ? 0x3000E5FF : 0x1500E5FF);
            graphics.renderOutline(statX, top + 15, statW, 16, statHov ? 0xFF00E5FF : 0x4000E5FF);
            graphics.drawString(font, statsText, statX + 6, top + 19, 0xFFCCE3EC, false);

            if (statHov) {
                List<Component> tip = List.of(
                        Component.translatable("awakening_system.fables_screen.tooltip.status_title"),
                        Component.translatable("awakening_system.fables_screen.tooltip.fables_engraved", unlockedCount),
                        Component.translatable("awakening_system.fables_screen.tooltip.fable_weight", fableWeight),
                        isConstellation
                                ? Component.translatable("awakening_system.fables_screen.constellation_ascended")
                                : (com.soybe.awakeningsystem.ConstellationAscension.isEligibleForAscension(data)
                                        ? Component.translatable("awakening_system.fables_screen.ready_for_ascension")
                                        : Component.translatable("awakening_system.fables_screen.req_ascension"))
                );
                graphics.renderComponentTooltip(font, tip, mX, mY);
            }
        }

        // Filter Bar (Tabs)
        int filterY = top + 36;
        renderFilterBar(graphics, left + 16, filterY, PANEL_W - 32, mX, mY);

        // Two-Column Content Area (Generously expanded)
        int listX = left + 16;
        int listY = top + 58;
        int listW = 310;
        int listH = PANEL_H - 70; // Adjusted for shifted header
        renderFableList(graphics, data, listX, listY, listW, listH, mX, mY);

        int detX = listX + listW + 8; // left + 334
        int detY = listY;
        int detW = left + PANEL_W - 16 - detX; // 250px width
        int detH = listH;
        renderFableDetail(graphics, data, detX, detY, detW, detH);

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
        FableDefinitions.Grade[] filterGrades = {
                null,
                FableDefinitions.Grade.HISTORICAL,
                FableDefinitions.Grade.LEGENDARY,
                FableDefinitions.Grade.SEMI_MYTH,
                FableDefinitions.Grade.MYTH,
                FableDefinitions.Grade.GIANT_STORY
        };

        int spacing = 4;
        int btnW = (w - (filterLabels.length - 1) * spacing) / filterLabels.length;
        int btnH = 18;

        for (int i = 0; i < filterLabels.length; i++) {
            int bx = x + i * (btnW + spacing);
            boolean selected = (selectedGradeFilter == filterGrades[i]);
            boolean hov = mouseX >= bx && mouseX < bx + btnW && mouseY >= y && mouseY < y + btnH;

            int bg = selected ? 0x4000E5FF : (hov ? 0x2200E5FF : 0x1000E5FF);
            int border = selected ? 0xFF00E5FF : (hov ? 0x8800E5FF : 0x3000E5FF);

            g.fill(bx, y, bx + btnW, y + btnH, bg);
            g.renderOutline(bx, y, btnW, btnH, border);

            if (selected) {
                // Top neon accent line
                g.fill(bx + 1, y, bx + btnW - 1, y + 1, 0xFF70E8FF);
            }

            int textColor = selected ? 0xFF00E5FF : (hov ? 0xFFFFFFFF : 0xFF8AAAB8);
            Component label = ModernGuiHelper.manhwa(filterLabels[i]);
            g.drawCenteredString(font, label, bx + btnW / 2, y + 5, textColor);
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
        g.fill(x, y, x + w, y + h, 0x0A00E5FF);
        g.renderOutline(x, y, w, h, 0x4400E5FF);

        List<FableDefinitions.Fable> fables = getFilteredFables();
        if (fables.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.no_fables").getString(), x + w / 2, y + h / 2 - 4, 0xFF8AAAB8);
            return;
        }

        int itemH = 42;
        int spacing = 4;
        int visibleRows = (h - 6) / (itemH + spacing);
        int maxScroll = Math.max(0, fables.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

        enableScissorScaled(g, x + 2, y + 2, x + w - 2, y + h - 2);

        for (int i = 0; i < visibleRows && (i + scrollOffset) < fables.size(); i++) {
            int idx = i + scrollOffset;
            FableDefinitions.Fable fable = fables.get(idx);
            int iy = y + 3 + i * (itemH + spacing);
            int cardX = x + 4;
            int cardW = w - 8;

            boolean isUnlocked = data != null && data.unlockedFables().contains(fable.id());
            boolean isSelected = fable.id().equals(selectedFableId);
            boolean hov = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= iy && mouseY < iy + itemH;

            int bg = isSelected ? 0x4000E5FF : (hov ? 0x2200E5FF : (isUnlocked ? 0x0C00E5FF : 0x0600E5FF));
            int border = isSelected ? 0xFF00E5FF : (hov ? 0x8800E5FF : (isUnlocked ? 0x3500E5FF : 0x2000E5FF));

            g.fill(cardX, iy, cardX + cardW, iy + itemH, bg);
            g.renderOutline(cardX, iy, cardW, itemH, border);

            if (isSelected) {
                // Left cyan neon indicator bar
                g.fill(cardX, iy, cardX + 3, iy + itemH, 0xFF00E5FF);
            }

            // Icon Badge
            int iconBoxW = 26;
            int iconBoxH = 30;
            int iconX = cardX + 5;
            int iconY = iy + 6;
            int iconBg = isUnlocked ? 0x25FFD700 : 0x1500E5FF;
            int iconBorder = isUnlocked ? 0x88FFD700 : 0x3000E5FF;
            g.fill(iconX, iconY, iconX + iconBoxW, iconY + iconBoxH, iconBg);
            g.renderOutline(iconX, iconY, iconBoxW, iconBoxH, iconBorder);
            g.drawCenteredString(font, fable.icon(), iconX + iconBoxW / 2, iconY + 10, isUnlocked ? 0xFFFFD700 : 0xFF607080);

            // Title & Information
            int titleX = cardX + 36;
            int titleCol = isUnlocked ? fable.grade().hexColor() : 0xFFFFFFFF;
            Component titleComp = ModernGuiHelper.manhwa(fable.getTitleComponent());
            drawTruncatedString(g, (isUnlocked ? "§l" : "") + titleComp.getString(), titleX, iy + 8, cardW - 44, titleCol);

            Component statusComp = isUnlocked
                    ? Component.translatable("awakening_system.fables_screen.unlocked")
                    : Component.translatable("awakening_system.fables_screen.locked");
            Component weightComp = Component.translatable("awakening_system.fables_screen.weight", fable.weight());
            String firstStr = fable.isWorldFirst() ? Component.translatable("awakening_system.fables_screen.world_first_badge").getString() : "";
            drawTruncatedString(g, statusComp.getString() + " " + weightComp.getString() + firstStr, titleX, iy + 24, cardW - 44, isUnlocked ? 0xFFFFFFFF : 0xFF8AAAB8);
        }

        g.disableScissor();

        // Sleek Scrollbar
        if (fables.size() > visibleRows) {
            int sbX = x + w - 4;
            int sbH = h - 6;
            int thumbH = Math.max(14, (int) ((float) visibleRows / fables.size() * sbH));
            int thumbY = y + 3 + (int) ((float) scrollOffset / maxScroll * (sbH - thumbH));
            g.fill(sbX, y + 3, sbX + 2, y + 3 + sbH, 0x2500E5FF);
            g.fill(sbX, thumbY, sbX + 2, thumbY + thumbH, 0xCC00E5FF);
        }
    }

    private void renderFableDetail(GuiGraphics g, SystemData data, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0x0A00E5FF);
        g.renderOutline(x, y, w, h, 0x4400E5FF);

        FableDefinitions.Fable fable = FableDefinitions.get(selectedFableId);
        if (fable == null) {
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.empty_title").getString(), x + w / 2, y + 16, 0xFFFFD700);
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.empty_desc1").getString(), x + w / 2, y + h / 2 - 8, 0xFF888888);
            g.drawCenteredString(font, Component.translatable("awakening_system.fables_screen.empty_desc2").getString(), x + w / 2, y + h / 2 + 4, 0xFF555555);
            return;
        }

        // Top accent line matching fable grade
        g.fill(x + 1, y + 1, x + w - 1, y + 3, fable.grade().hexColor());

        boolean isUnlocked = data != null && data.unlockedFables().contains(fable.id());
        int innerW = w - 18;
        int curY = y + 8;

        // Grade Badge
        Component gradeBadge = ModernGuiHelper.manhwa(Component.translatable("awakening_system.fables_screen.grade_badge", fable.grade().getDisplayName().getString().toUpperCase()));
        g.drawString(font, gradeBadge, x + 8, curY, fable.grade().hexColor(), false);
        curY += 14;

        // Prominent Fable Title (Bold with colored grade accent)
        String titleStr = "§f§l" + fable.getTitleComponent().getString();
        List<FormattedCharSequence> titleLines = font.split(Component.literal(titleStr), innerW);
        for (FormattedCharSequence tl : titleLines) {
            g.drawString(font, tl, x + 8, curY, 0xFFFFFFFF, false);
            curY += 12;
        }
        curY += 3;

        // Narrative Weight
        Component weightDetail = Component.translatable("awakening_system.fables_screen.weight_detail", fable.weight());
        g.drawString(font, weightDetail.getString(), x + 8, curY, 0xFFFFD700, false);
        curY += 14;

        // World First Pill
        if (fable.isWorldFirst()) {
            g.fill(x + 6, curY, x + w - 6, curY + 24, 0x20FFD700);
            g.renderOutline(x + 6, curY, w - 12, 24, 0x88FFD700);
            Component wfTitle = Component.translatable("awakening_system.fables_screen.world_first_title");
            Component wfDesc = Component.translatable("awakening_system.fables_screen.world_first_desc");
            g.drawString(font, wfTitle.getString(), x + 10, curY + 4, 0xFFFFD700, false);
            g.drawString(font, wfDesc.getString(), x + 10, curY + 14, 0xFFCCE3EC, false);
            curY += 28;
        }

        // Divider
        g.fill(x + 8, curY, x + w - 8, curY + 1, 0x3000E5FF);
        curY += 7;

        // Story Header
        Component recHead = Component.translatable("awakening_system.fables_screen.recorded_story");
        g.drawString(font, recHead.getString(), x + 8, curY, 0xFF70E8FF, false);
        curY += 12;

        // Description lines
        int stateBoxY = y + h - 26;
        int resH = 44;
        int resY = stateBoxY - resH - 6;
        int maxLines = Math.max(1, (resY - 4 - curY) / 10);
        List<FormattedCharSequence> descLines = font.split(fable.getDescriptionComponent(), innerW);
        for (int i = 0; i < Math.min(maxLines, descLines.size()); i++) {
            g.drawString(font, descLines.get(i), x + 8, curY, 0xFFEAF5FA, false);
            curY += 10;
        }

        // Narrative & Soul Resonance Card (Fills empty void with rich cosmic lore)
        g.fill(x + 6, resY, x + w - 6, resY + resH, 0x1800E5FF);
        g.renderOutline(x + 6, resY, w - 12, resH, (fable.grade().hexColor() & 0x00FFFFFF) | 0x88000000);
        Component resTitle = Component.translatable("awakening_system.fables_screen.resonance");
        Component resAuth = Component.translatable("awakening_system.fables_screen.cosmic_authority", fable.weight());
        g.drawString(font, resTitle.getString(), x + 10, resY + 5, 0xFFFFD700, false);
        g.drawString(font, resAuth.getString(), x + 10, resY + 16, 0xFF70E8FF, false);
        String soulNote = isUnlocked
                ? Component.translatable("awakening_system.fables_screen.soul_engraved").getString()
                : Component.translatable("awakening_system.fables_screen.soul_locked").getString();
        g.drawString(font, soulNote, x + 10, resY + 28, isUnlocked ? 0xFF50FA7B : 0xFF9AB2BF, false);

        // Bottom Status Capsule
        int stateBg = isUnlocked ? 0x3050FA7B : 0x20FFAA00;
        int stateBorder = isUnlocked ? 0xFF50FA7B : 0xFFFFAA00;
        g.fill(x + 8, stateBoxY, x + w - 8, stateBoxY + 22, stateBg);
        g.renderOutline(x + 8, stateBoxY, w - 16, 22, stateBorder);

        Component stateLabel = isUnlocked
                ? Component.translatable("awakening_system.fables_screen.soul_engraved")
                : Component.translatable("awakening_system.fables_screen.unwritten");
        int stateCol = isUnlocked ? 0xFF50FA7B : 0xFFFFD866;
        g.drawCenteredString(font, ModernGuiHelper.manhwa(stateLabel), x + w / 2, stateBoxY + 7, stateCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        // Close button [✕]
        int closeX = left + PANEL_W - 32;
        int closeY = top + 14;
        int closeW = 16;
        int closeH = 16;
        if (mouseX >= closeX && mouseX < closeX + closeW && mouseY >= closeY && mouseY < closeY + closeH) {
            playClickSound();
            onClose();
            return true;
        }

        // Filter Bar Tabs
        int filterY = top + 34;
        int filterW = PANEL_W - 32;
        FableDefinitions.Grade[] filterGrades = {
                null,
                FableDefinitions.Grade.HISTORICAL,
                FableDefinitions.Grade.LEGENDARY,
                FableDefinitions.Grade.SEMI_MYTH,
                FableDefinitions.Grade.MYTH,
                FableDefinitions.Grade.GIANT_STORY
        };
        int spacing = 4;
        int btnW = (filterW - (filterGrades.length - 1) * spacing) / filterGrades.length;

        for (int i = 0; i < filterGrades.length; i++) {
            int bx = left + 16 + i * (btnW + spacing);
            if (mouseX >= bx && mouseX < bx + btnW && mouseY >= filterY && mouseY < filterY + 18) {
                playClickSound();
                selectedGradeFilter = filterGrades[i];
                scrollOffset = 0;
                return true;
            }
        }

        // Fable List click
        int listX = left + 16;
        int listY = top + 56;
        int listW = 310;
        int listH = PANEL_H - 68;

        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            List<FableDefinitions.Fable> fables = getFilteredFables();
            int itemH = 42;
            int itemSpacing = 4;
            int visibleRows = (listH - 6) / (itemH + itemSpacing);

            for (int i = 0; i < visibleRows && (i + scrollOffset) < fables.size(); i++) {
                int iy = listY + 3 + i * (itemH + itemSpacing);
                if (mouseY >= iy && mouseY < iy + itemH) {
                    playClickSound();
                    selectedFableId = fables.get(i + scrollOffset).id();
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
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;
        int listX = left + 16;
        int listY = top + 56;
        int listW = 310;
        int listH = PANEL_H - 68;

        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            List<FableDefinitions.Fable> fables = getFilteredFables();
            int itemH = 42;
            int spacing = 4;
            int visibleRows = (listH - 6) / (itemH + spacing);
            int maxScroll = Math.max(0, fables.size() - visibleRows);
            if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            } else if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void drawTruncatedString(GuiGraphics g, String text, int x, int y, int maxW, int color) {
        if (font.width(text) <= maxW) {
            g.drawString(font, text, x, y, color, false);
        } else {
            String trimmed = font.plainSubstrByWidth(text, Math.max(10, maxW - font.width("..."))) + "...";
            g.drawString(font, trimmed, x, y, color, false);
        }
    }
}
