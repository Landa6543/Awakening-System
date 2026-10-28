package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class SystemGuidebookScreen extends Screen {
    private enum Tab {
        SYSTEM("system", 0xFF70E8FF),
        RANKS("ranks", 0xFFFFD866),
        DUNGEONS("dungeons", 0xFF39B9D5),
        SKILLS("skills", 0xFFE4A7FF),
        TOWER("tower", 0xFFFFD700),
        CULTIVATION("cultivation", 0xFF50FA7B),
        TEYVAT("teyvat", 0xFFBD93F9),
        CASINO("casino", 0xFFFFB86C),
        TRACEN("tracen", 0xFF58C8E8),
        GUILDS("guilds", 0xFFFF79C6),
        SPONSORS("sponsors", 0xFFFF9966);

        final String id;
        final int color;

        Tab(String id, int color) {
            this.id = id;
            this.color = color;
        }

        Component getLabel() {
            return Component.translatable("awakening_system.screen.guide.tab." + id);
        }
    }

    private static final Tab[] ROW1 = { Tab.SYSTEM, Tab.RANKS, Tab.DUNGEONS, Tab.SKILLS, Tab.TOWER, Tab.CULTIVATION };
    private static final Tab[] ROW2 = { Tab.TEYVAT, Tab.CASINO, Tab.TRACEN, Tab.GUILDS, Tab.SPONSORS };

    private Tab currentTab = Tab.SYSTEM;

    public SystemGuidebookScreen() {
        super(Component.translatable("awakening_system.screen.guide.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 490.0F;
        float scaleY = (float) this.height / 280.0F;
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x9A050D14, 0xBA0A1620);

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

        int panelW = 470;
        int panelH = 265;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        g.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF1D556C);
        g.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF39B9D5);
        g.fill(left, top, left + panelW, top + panelH, 0xFA0B161F);

        g.fill(left, top, left + panelW, top + 24, 0xFF122430);
        g.fill(left, top + 23, left + panelW, top + 24, 0xFF39B9D5);

        int cornerLen = 10;
        g.fill(left, top, left + cornerLen, top + 2, 0xFF70E8FF);
        g.fill(left, top, left + 2, top + cornerLen, 0xFF70E8FF);
        g.fill(left + panelW - cornerLen, top, left + panelW, top + 2, 0xFF70E8FF);
        g.fill(left + panelW - 2, top, left + panelW, top + cornerLen, 0xFF70E8FF);

        g.drawString(font, Component.translatable("awakening_system.screen.guide.title"), left + 12, top + 8, 0xFF70E8FF, false);

        boolean exitHovered = mX >= left + panelW - 28 && mX <= left + panelW - 8 && mY >= top + 5 && mY <= top + 19;
        g.fill(left + panelW - 28, top + 5, left + panelW - 8, top + 19, exitHovered ? 0xFF992222 : 0xFF203545);
        g.fill(left + panelW - 27, top + 6, left + panelW - 9, top + 18, exitHovered ? 0xFFCC3333 : 0xFF14222D);
        g.drawCenteredString(font, "§f✖", left + panelW - 18, top + 7, exitHovered ? 0xFFFFFFFF : 0xFF8AAAB8);

        // Render Tab Row 1 (6 tabs)
        int tabY1 = top + 26;
        int tabH = 16;
        int tabW1 = (panelW - 20) / ROW1.length;
        for (int i = 0; i < ROW1.length; i++) {
            Tab t = ROW1[i];
            int tx = left + 10 + i * tabW1;
            boolean isSelected = (t == currentTab);
            boolean hovered = mX >= tx && mX <= tx + tabW1 - 2 && mY >= tabY1 && mY <= tabY1 + tabH;

            int borderCol = isSelected ? t.color : (hovered ? 0xFF58C8E8 : 0xFF27556A);
            int bgCol = isSelected ? 0xFF1C4258 : (hovered ? 0xFF1E384C : 0xFF142736);

            g.fill(tx, tabY1, tx + tabW1 - 2, tabY1 + tabH, borderCol);
            g.fill(tx + 1, tabY1 + 1, tx + tabW1 - 3, tabY1 + tabH - 1, bgCol);
            g.drawCenteredString(font, t.getLabel(), tx + (tabW1 - 2) / 2, tabY1 + 4, isSelected ? 0xFFFFFFFF : (hovered ? 0xFF70E8FF : 0xFF8AAAB8));
        }

        // Render Tab Row 2 (5 tabs)
        int tabY2 = top + 44;
        int tabW2 = (panelW - 20) / ROW2.length;
        for (int i = 0; i < ROW2.length; i++) {
            Tab t = ROW2[i];
            int tx = left + 10 + i * tabW2;
            boolean isSelected = (t == currentTab);
            boolean hovered = mX >= tx && mX <= tx + tabW2 - 2 && mY >= tabY2 && mY <= tabY2 + tabH;

            int borderCol = isSelected ? t.color : (hovered ? 0xFF58C8E8 : 0xFF27556A);
            int bgCol = isSelected ? 0xFF1C4258 : (hovered ? 0xFF1E384C : 0xFF142736);

            g.fill(tx, tabY2, tx + tabW2 - 2, tabY2 + tabH, borderCol);
            g.fill(tx + 1, tabY2 + 1, tx + tabW2 - 3, tabY2 + tabH - 1, bgCol);
            g.drawCenteredString(font, t.getLabel(), tx + (tabW2 - 2) / 2, tabY2 + 4, isSelected ? 0xFFFFFFFF : (hovered ? 0xFF70E8FF : 0xFF8AAAB8));
        }

        int contentX = left + 10;
        int contentY = top + 63;
        int contentW = panelW - 20;
        int contentH = panelH - 71;
        g.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0xFF0D1922);
        g.fill(contentX, contentY, contentX + contentW, contentY + 1, 0xFF276A82);

        switch (currentTab) {
            case SYSTEM -> renderSystemTab(g, contentX, contentY, contentW, contentH);
            case RANKS -> renderRanksTab(g, contentX, contentY, contentW, contentH);
            case DUNGEONS -> renderDungeonsTab(g, contentX, contentY, contentW, contentH);
            case SKILLS -> renderSkillsTab(g, contentX, contentY, contentW, contentH);
            case TOWER -> renderTowerTab(g, contentX, contentY, contentW, contentH);
            case CULTIVATION -> renderCultivationTab(g, contentX, contentY, contentW, contentH);
            case TEYVAT -> renderTeyvatTab(g, contentX, contentY, contentW, contentH);
            case CASINO -> renderCasinoTab(g, contentX, contentY, contentW, contentH);
            case TRACEN -> renderTracenTab(g, contentX, contentY, contentW, contentH);
            case GUILDS -> renderGuildsTab(g, contentX, contentY, contentW, contentH);
            case SPONSORS -> renderSponsorsTab(g, contentX, contentY, contentW, contentH);
        }

        g.pose().popPose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private int drawTranslatable(GuiGraphics g, String key, int x, int y, int maxWidth, int color, int maxY) {
        for (var line : font.split(Component.translatable(key), maxWidth)) {
            if (y + 8 > maxY) break;
            g.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    private void drawSectionHeader(GuiGraphics g, String key, int x, int y, int color) {
        g.drawString(font, Component.translatable(key), x, y, color, false);
    }

    private void renderTowerTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.tower.header1", x + 10, ty, 0xFFFFD700);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.tower.header2", x + 10, ty, 0xFFFFD866);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.tower.header3", x + 10, ty, 0xFF70E8FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text3_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.tower.header4", x + 10, ty, 0xFFE4A7FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text4_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.tower.text4_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderCultivationTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.cultivation.header1", x + 10, ty, 0xFF50FA7B);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.cultivation.header2", x + 10, ty, 0xFFFFD700);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.cultivation.header3", x + 10, ty, 0xFF70E8FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.cultivation.header4", x + 10, ty, 0xFFFFE066);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text4_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.cultivation.text4_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderSystemTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.system.header1", x + 10, ty, 0xFF70E8FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.system.header2", x + 10, ty, 0xFFFFD866);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text2_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.system.header3", x + 10, ty, 0xFFFF7666);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text3_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.system.header4", x + 10, ty, 0xFFFFB86C);
        ty += 11;
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text4_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 3;
        drawSectionHeader(g, "awakening_system.screen.guide.system.header5", x + 10, ty, 0xFFFFD866);
        ty += 11;
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text5_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.system.text5_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderRanksTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 6;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.ranks.header1", x + 10, ty, 0xFFFFD866);
        ty += 11;
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_4", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_5", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_6", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_7", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_8", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_9", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text1_10", x + 10, ty, maxW, 0xFFFFD866, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.ranks.header2", x + 10, ty, 0xFF70E8FF);
        ty += 11;
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.ranks.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderDungeonsTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.dungeons.header1", x + 10, ty, 0xFF70E8FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text1_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text1_4", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text1_5", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text1_6", x + 10, ty, maxW, 0xFFFFD866, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.dungeons.header2", x + 10, ty, 0xFF70FF90);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.dungeons.header3", x + 10, ty, 0xFFFFD866);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.dungeons.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderSkillsTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.skills.header1", x + 10, ty, 0xFFE4A7FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text1_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text1_4", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text1_5", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.skills.header2", x + 10, ty, 0xFF70E8FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text2_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.skills.header3", x + 10, ty, 0xFFFFD866);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.skills.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderSponsorsTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 6;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.sponsors.header1", x + 10, ty, 0xFFFF9966);
        ty += 11;
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text1_2", x + 10, ty, maxW, 0xFF70E8FF, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text1_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text1_4", x + 10, ty, maxW, 0xFF50FA7B, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text1_5", x + 10, ty, maxW, 0xFFFF79C6, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.sponsors.header2", x + 10, ty, 0xFFFFD866);
        ty += 11;
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text2_3", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text2_4", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.sponsors.text2_5", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderTeyvatTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.teyvat.header1", x + 10, ty, 0xFFBD93F9);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.teyvat.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.teyvat.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.teyvat.header2", x + 10, ty, 0xFFFFD866);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.teyvat.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.teyvat.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.teyvat.header3", x + 10, ty, 0xFF70E8FF);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.teyvat.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.teyvat.text3_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderCasinoTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.casino.header1", x + 10, ty, 0xFFFFB86C);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.casino.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.casino.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.casino.header2", x + 10, ty, 0xFFBD93F9);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.casino.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.casino.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.casino.header3", x + 10, ty, 0xFF50FA7B);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.casino.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderTracenTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.tracen.header1", x + 10, ty, 0xFF58C8E8);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.tracen.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.tracen.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.tracen.header2", x + 10, ty, 0xFFFFD700);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.tracen.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.tracen.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    private void renderGuildsTab(GuiGraphics g, int x, int y, int w, int h) {
        int ty = y + 8;
        int maxW = w - 20;
        int maxY = y + h - 4;

        drawSectionHeader(g, "awakening_system.screen.guide.guilds.header1", x + 10, ty, 0xFFFF79C6);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.guilds.text1_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.guilds.text1_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.guilds.header2", x + 10, ty, 0xFF50FA7B);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.guilds.text2_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
        ty = drawTranslatable(g, "awakening_system.screen.guide.guilds.text2_2", x + 10, ty, maxW, 0xFFDDECF2, maxY);

        ty += 4;
        drawSectionHeader(g, "awakening_system.screen.guide.guilds.header3", x + 10, ty, 0xFFFF5555);
        ty += 12;
        ty = drawTranslatable(g, "awakening_system.screen.guide.guilds.text3_1", x + 10, ty, maxW, 0xFFDDECF2, maxY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int panelW = 470;
            int panelH = 265;
            int left = (this.width - panelW) / 2;
            int top = (this.height - panelH) / 2;

            if (mouseX >= left + panelW - 28 && mouseX <= left + panelW - 8 && mouseY >= top + 5 && mouseY <= top + 19) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
                }
                onClose();
                return true;
            }

            int tabY1 = top + 26;
            int tabY2 = top + 44;
            int tabH = 16;
            int tabW1 = (panelW - 20) / ROW1.length;
            int tabW2 = (panelW - 20) / ROW2.length;

            if (mouseY >= tabY1 && mouseY <= tabY1 + tabH) {
                for (int i = 0; i < ROW1.length; i++) {
                    int tx = left + 10 + i * tabW1;
                    if (mouseX >= tx && mouseX <= tx + tabW1 - 2) {
                        currentTab = ROW1[i];
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F);
                        }
                        return true;
                    }
                }
            } else if (mouseY >= tabY2 && mouseY <= tabY2 + tabH) {
                for (int i = 0; i < ROW2.length; i++) {
                    int tx = left + 10 + i * tabW2;
                    if (mouseX >= tx && mouseX <= tx + tabW2 - 2) {
                        currentTab = ROW2[i];
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
