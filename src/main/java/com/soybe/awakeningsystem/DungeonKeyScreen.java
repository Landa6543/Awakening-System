package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DungeonKeyScreen extends Screen {
    private final int playerLevel;
    private final String currentRank;

    private static final List<String> RANKS = List.of(
            "F", "E", "D", "C", "B", "A", "S", "EX", "SS", "SSS", "NATIONAL", "UR", "LR", "MR", "TRANSCENDENT", "TRANSCENDENTAL", "MONARCH", "RULER", "DEMIGOD", "GOD", "COSMIC", "PRIMORDIAL", "SUPREME_CREATOR", "ABSOLUTE_GOD"
    );

    private static final List<String> BIOMES = List.of(
            "crypt", "plains", "lush_plains", "cherry_plains", "autumn_plains", "tundra",
            "ancient_temple", "crystal_cave", "abyssal_chasm", "magma_forge", "nether", "igris", "baran", "beru"
    );

    private String selectedRank = "E";
    private String selectedBiome = "crypt";

    public DungeonKeyScreen(int playerLevel, String currentRank) {
        super(Component.translatable("awakening_system.screen.dungeon_key.title"));
        this.playerLevel = playerLevel;
        this.currentRank = (currentRank == null || currentRank.isEmpty()) ? "F" : currentRank;
        this.selectedRank = isRankUnlocked(currentRank) ? currentRank : "E";
    }

    private int getRankMinLevel(String rank) {
        return switch (rank) {
            case "F" -> 1;
            case "E" -> 5;
            case "D" -> 10;
            case "C" -> 20;
            case "B" -> 30;
            case "A" -> 40;
            case "S" -> 50;
            case "EX" -> 70;
            case "SS" -> 90;
            case "SSS" -> 110;
            case "NATIONAL" -> 130;
            case "UR" -> 150;
            case "LR" -> 190;
            case "MR" -> 230;
            case "TRANSCENDENT" -> 270;
            case "TRANSCENDENTAL" -> 290;
            case "MONARCH" -> 310;
            case "RULER" -> 330;
            case "DEMIGOD" -> 370;
            case "GOD" -> 400;
            case "COSMIC" -> 490;
            case "PRIMORDIAL" -> 580;
            case "SUPREME_CREATOR" -> 730;
            case "ABSOLUTE_GOD" -> 1000;
            default -> 1;
        };
    }

    private boolean isRankUnlocked(String rank) {
        return playerLevel >= getRankMinLevel(rank);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private void drawAutoScaledCentered(GuiGraphics g, Component label, int centerX, int centerY, int maxWidth, int color) {
        int textW = font.width(label);
        if (textW <= maxWidth) {
            g.drawCenteredString(font, label, centerX, centerY, color);
            return;
        }
        float scale = Math.max(0.80F, (float) maxWidth / textW);
        var pose = g.pose();
        pose.pushPose();
        pose.translate(centerX, centerY + (1.0F - scale) * 4.0F, 0);
        pose.scale(scale, scale, 1.0F);
        g.drawCenteredString(font, label, 0, 0, color);
        pose.popPose();
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 400.0F;
        float scaleY = (float) this.height / 260.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.75F, scale));
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
        g.fillGradient(0, 0, this.width, this.height, 0x90000000, 0xB0000000);

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

        int panelW = 380;
        int panelH = 266;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        g.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF276A82);
        g.fill(left, top, left + panelW, top + panelH, 0xEE10202D);
        g.fill(left, top, left + panelW, top + 28, 0xFF183042);
        g.fill(left, top + 27, left + panelW, top + 29, 0xFF39B9D5);

        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.title"), left + 14, top + 6, 0xFF70E8FF, false);
        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.subtitle"), left + 14, top + 16, 0xFF8AAAB8, false);

        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.portal_rank", playerLevel), left + 14, top + 34, 0xFFFFD866, false);
        int rankBtnW = 42;
        int rankBtnH = 15;
        int rStartX = left + 14;
        int rStartY = top + 46;

        for (int i = 0; i < RANKS.size(); i++) {
            String rk = RANKS.get(i);
            int rx = rStartX + (i % 8) * (rankBtnW + 2);
            int ry = rStartY + (i / 8) * (rankBtnH + 2);

            boolean unlocked = isRankUnlocked(rk);
            boolean isSelected = rk.equals(selectedRank);
            boolean hovered = mX >= rx && mX <= rx + rankBtnW && mY >= ry && mY <= ry + rankBtnH;

            int borderCol = isSelected ? 0xFF39B9D5 : (hovered ? 0xFF58C8E8 : (unlocked ? 0xFF27556A : 0xFF332025));
            int bgCol = isSelected ? 0xFF1C4258 : (unlocked ? (hovered ? 0xFF1E384C : 0xFF142736) : 0xFF201518);
            int textCol = isSelected ? 0xFFFFFFFF : (unlocked ? 0xFFDDECF2 : 0xFF775555);

            String label = (unlocked ? "" : "🔒") + getRankShortLabel(rk);
            g.fill(rx, ry, rx + rankBtnW, ry + rankBtnH, borderCol);
            g.fill(rx + 1, ry + 1, rx + rankBtnW - 1, ry + rankBtnH - 1, bgCol);
            drawAutoScaledCentered(g, Component.literal(label), rx + rankBtnW / 2, ry + 5, rankBtnW - 4, textCol);

            if (hovered) {
                if (!unlocked) {
                    g.renderTooltip(font, Component.translatable("awakening_system.screen.dungeon_key.locked_tooltip", rk, getRankMinLevel(rk)), mX, mY);
                } else {
                    g.renderTooltip(font, Component.translatable("awakening_system.screen.dungeon_key.unlocked_tooltip", rk, getRankMinLevel(rk), getDangerRating(rk).getString()), mX, mY);
                }
            }
        }

        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.environment"), left + 14, top + 102, 0xFFFFD866, false);
        int bioCols = 7;
        int bioBtnW = 48;
        int bioBtnH = 15;
        int bStartX = left + 14;
        int bStartY = top + 114;

        for (int i = 0; i < BIOMES.size(); i++) {
            String bio = BIOMES.get(i);
            int bx = bStartX + (i % bioCols) * (bioBtnW + 2);
            int by = bStartY + (i / bioCols) * (bioBtnH + 2);

            boolean isSelected = bio.equals(selectedBiome);
            boolean hovered = mX >= bx && mX <= bx + bioBtnW && mY >= by && mY <= by + bioBtnH;

            int borderCol = isSelected ? 0xFF39B9D5 : (hovered ? 0xFF58C8E8 : 0xFF27556A);
            int bgCol = isSelected ? 0xFF1C4258 : (hovered ? 0xFF1E384C : 0xFF142736);
            int textCol = isSelected ? 0xFF70E8FF : 0xFFDDECF2;

            g.fill(bx, by, bx + bioBtnW, by + bioBtnH, borderCol);
            g.fill(bx + 1, by + 1, bx + bioBtnW - 1, by + bioBtnH - 1, bgCol);

            Component label = Component.translatable("awakening_system.screen.dungeon_key.biome." + bio);
            drawAutoScaledCentered(g, label, bx + bioBtnW / 2, by + 4, bioBtnW - 4, textCol);

            if (hovered) {
                g.renderTooltip(font, getBiomeDescription(bio), mX, mY);
            }
        }

        int prevX = left + 14;
        int prevY = top + 152;
        int prevW = panelW - 28;
        int prevH = 62;
        g.fill(prevX, prevY, prevX + prevW, prevY + prevH, 0xFF0D1922);
        g.fill(prevX, prevY, prevX + prevW, prevY + 1, 0xFF276A82);

        Component danger = getDangerRating(selectedRank);
        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.portal_details"), prevX + 8, prevY + 4, 0xFF70E8FF, false);
        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.rank_detail", selectedRank, getRankMinLevel(selectedRank)), prevX + 8, prevY + 17, 0xFFFFFFFF, false);
        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.threat_detail", danger.getString()), prevX + 8, prevY + 30, 0xFFFFFFFF, false);
        g.drawString(font, Component.translatable("awakening_system.screen.dungeon_key.env_detail", getBiomeDescription(selectedBiome).getString()), prevX + 8, prevY + 43, 0xFFDDECF2, false);

        int btnW = 200;
        int btnH = 24;
        int btnX = left + (panelW - btnW) / 2;
        int btnY = top + 224;
        boolean btnHover = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;

        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? 0xFF58C8E8 : 0xFF39B9D5);
        g.fill(btnX + 1, btnY + 1, btnX + btnW - 1, btnY + btnH - 1, btnHover ? 0xFF2A5570 : 0xFF1C3A4D);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.dungeon_key.open_btn"), btnX + btnW / 2, btnY + 8, btnHover ? 0xFFFFFFFF : 0xFF70E8FF);

        g.pose().popPose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private String getRankShortLabel(String rank) {
        return switch (rank) {
            case "NATIONAL" -> "NATL";
            case "MONARCH" -> "MNR";
            case "RULER" -> "RLR";
            case "DEMIGOD" -> "DMG";
            case "SUPREME_CREATOR" -> "CREAT";
            case "EX" -> "EX";
            case "UR" -> "UR";
            case "LR" -> "LR";
            case "MR" -> "MR";
            case "TRANSCENDENT" -> "TR";
            case "TRANSCENDENTAL" -> "TR+";
            case "GOD" -> "GOD";
            case "COSMIC" -> "COS";
            case "PRIMORDIAL" -> "PRI";
            case "ABSOLUTE_GOD" -> "A.GOD";
            default -> rank;
        };
    }

    private Component getDangerRating(String rank) {
        int tier = DungeonManager.getRankTier(rank);
        if (tier >= 13) return Component.translatable("awakening_system.screen.dungeon_key.danger.omnipotent");
        if (tier >= 12) return Component.translatable("awakening_system.screen.dungeon_key.danger.deicide_creation");
        if (tier >= 10) return Component.translatable("awakening_system.screen.dungeon_key.danger.deicide_lethal");
        if (tier >= 8) return Component.translatable("awakening_system.screen.dungeon_key.danger.extreme");
        if (tier >= 6) return Component.translatable("awakening_system.screen.dungeon_key.danger.high");
        if (tier >= 4) return Component.translatable("awakening_system.screen.dungeon_key.danger.moderate");
        return Component.translatable("awakening_system.screen.dungeon_key.danger.low");
    }

    private Component getBiomeDescription(String biome) {
        return switch (biome) {
            case "igris" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.igris");
            case "baran" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.baran");
            case "beru" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.beru");
            case "plains" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.plains");
            case "lush_plains" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.lush_plains");
            case "cherry_plains" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.cherry_plains");
            case "autumn_plains" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.autumn_plains");
            case "tundra" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.tundra");
            case "ancient_temple" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.ancient_temple");
            case "crystal_cave" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.crystal_cave");
            case "abyssal_chasm" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.abyssal_chasm");
            case "magma_forge" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.magma_forge");
            case "nether" -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.nether");
            default -> Component.translatable("awakening_system.screen.dungeon_key.biome_desc.crypt");
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int panelW = 380;
            int panelH = 266;
            int left = (this.width - panelW) / 2;
            int top = (this.height - panelH) / 2;

            int rankBtnW = 42;
            int rankBtnH = 15;
            int rStartX = left + 14;
            int rStartY = top + 46;
            for (int i = 0; i < RANKS.size(); i++) {
                String rk = RANKS.get(i);
                int rx = rStartX + (i % 8) * (rankBtnW + 2);
                int ry = rStartY + (i / 8) * (rankBtnH + 2);
                if (mouseX >= rx && mouseX <= rx + rankBtnW && mouseY >= ry && mouseY <= ry + rankBtnH) {
                    if (isRankUnlocked(rk)) {
                        this.selectedRank = rk;
                        Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F);
                        return true;
                    }
                }
            }

            int bioCols = 7;
            int bioBtnW = 48;
            int bioBtnH = 15;
            int bStartX = left + 14;
            int bStartY = top + 114;
            for (int i = 0; i < BIOMES.size(); i++) {
                String bio = BIOMES.get(i);
                int bx = bStartX + (i % bioCols) * (bioBtnW + 2);
                int by = bStartY + (i / bioCols) * (bioBtnH + 2);
                if (mouseX >= bx && mouseX <= bx + bioBtnW && mouseY >= by && mouseY <= by + bioBtnH) {
                    this.selectedBiome = bio;
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F);
                    return true;
                }
            }

            int btnW = 200;
            int btnH = 24;
            int btnX = left + (panelW - btnW) / 2;
            int btnY = top + 224;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                PacketDistributor.sendToServer(new SelectDungeonPayload(selectedRank, selectedBiome));
                Minecraft.getInstance().player.playSound(ModSounds.DUNGEON_GATE_OPEN.value(), 1.0F, 0.9F);
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
