package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

public final class SystemHudOverlay {
    public enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        ABOVE_HOTBAR
    }

    public static HudPosition HUD_POSITION = HudPosition.TOP_LEFT;

    public enum SkillBarPosition {
        BOTTOM_LEFT,
        ABOVE_HOTBAR
    }

    public static SkillBarPosition SKILL_BAR_POSITION = SkillBarPosition.BOTTOM_LEFT;

    public static float CUSTOM_HUD_SCALE = 0.0F;

    public static float getEffectiveHudScale(int screenW, int screenH) {
        float userFactor = com.soybe.awakeningsystem.client.gui.GuiConfig.hudScale;
        if (CUSTOM_HUD_SCALE > 0.05F) {
            return Math.max(0.4F, Math.min(2.0F, CUSTOM_HUD_SCALE * userFactor));
        }
        float scaleX = (float) screenW / 620.0F;
        float scaleY = (float) screenH / 350.0F;
        float factor = Math.min(scaleX, scaleY);
        return Math.max(0.4F, Math.min(2.0F, factor * userFactor));
    }

    private static float displayHealth = -1.0F;
    private static float damageLagHealth = -1.0F;
    private static float displayMp = -1.0F;
    private static float damageLagMp = -1.0F;
    private static float displayQi = -1.0F;
    private static float damageLagQi = -1.0F;
    private static long lastHealthTime = 0L;

    private static float animatedCardY = -1.0F;
    private static long lastCardYRenderTime = 0L;

    private SystemHudOverlay() {}

    public static void render(GuiGraphics graphics, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        boolean isChat = mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
        if (mc.player == null || mc.options.hideGui || (mc.screen != null && !isChat)) return;

        SystemData data = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data == null) return;

        boolean isSys40 = data.system40Updated() || data.isSystem40Updating();
        boolean isSys30 = (data.system30Updated() || data.isSystem30Updating()) && !isSys40;
        boolean isSys20 = (data.systemUpdated() || data.isSystemUpdating()) && !isSys30 && !isSys40;
        boolean hasCultivation = (data.dantianRealm() > 0 || data.cultivationQi() > 0 || data.cultivationSubLevel() > 1
                || "demonic_cultivator".equals(data.hunterClass()) || "sword_saint".equals(data.hunterClass())
                || data.murimUnlocked() || data.system30Updated() || data.getOpenedMeridiansCount() > 0 || (mc.player != null && mc.player.level().dimension().location().getPath().contains("murim")));

        updateBarInterpolation(mc.player, data);

        if (data.isAnySystemUpdating()) {
            renderSystemUpdatingBanner(mc, graphics, data, width);
        }

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudStatusCard) {
            switch (com.soybe.awakeningsystem.client.gui.GuiConfig.healthDisplayMode) {
                case CARD -> renderHunterStatusCard(mc, graphics, data, width, height, isSys40, isSys30, isSys20, hasCultivation);
                case VANILLA -> renderCompactManaBar(mc, graphics, data, width, height, isSys40, isSys30, isSys20, hasCultivation);
                case COMPACT_BAR -> renderCompactHotbarBars(mc, graphics, data, width, height, isSys40, isSys30, isSys20, hasCultivation);
            }
        }

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudSkillBar) {
            renderSkillHotbar(mc, graphics, data, width, height, isSys40, isSys30, isSys20);
        }

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudAppraisal) {
            renderEntityAppraisal(mc, graphics, data, width, isSys40, isSys30, isSys20);
        }

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudCombo) {
            renderCombatCombo(mc, graphics, data, width, height);
        }

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudDokkaebiScenario && data.isDokkaebiScenarioActive()) {
            renderDokkaebiScenario(mc, graphics, data, width);
        }
    }

    private static void updateBarInterpolation(Player player, SystemData data) {
        if (player == null) return;
        float actualHp = player.getHealth();
        float actualMp = data != null ? data.mana() : 0;
        float actualQi = data != null ? data.cultivationQi() : 0;
        long now = System.currentTimeMillis();

        if (displayHealth < 0 || lastHealthTime == 0L) {
            displayHealth = actualHp;
            damageLagHealth = actualHp;
            displayMp = actualMp;
            damageLagMp = actualMp;
            displayQi = actualQi;
            damageLagQi = actualQi;
            lastHealthTime = now;
            return;
        }

        float dt = Math.min(0.1F, (now - lastHealthTime) / 1000.0F);
        lastHealthTime = now;

        if (actualHp < displayHealth) {
            displayHealth = actualHp;
        } else if (actualHp > displayHealth) {
            displayHealth = actualHp;
            damageLagHealth = actualHp;
        }

        if (damageLagHealth > actualHp) {
            damageLagHealth -= (damageLagHealth - actualHp) * Math.min(1.0F, dt * 4.5F);
            if (Math.abs(damageLagHealth - actualHp) < 0.15F) {
                damageLagHealth = actualHp;
            }
        }

        if (actualMp < displayMp) {
            displayMp = actualMp;
        } else if (actualMp > displayMp) {
            displayMp = actualMp;
            damageLagMp = actualMp;
        }

        if (damageLagMp > actualMp) {
            damageLagMp -= (damageLagMp - actualMp) * Math.min(1.0F, dt * 4.5F);
            if (Math.abs(damageLagMp - actualMp) < 0.25F) {
                damageLagMp = actualMp;
            }
        }

        if (actualQi < displayQi) {
            displayQi = actualQi;
        } else if (actualQi > displayQi) {
            displayQi = actualQi;
            damageLagQi = actualQi;
        }

        if (damageLagQi > actualQi) {
            damageLagQi -= (damageLagQi - actualQi) * Math.min(1.0F, dt * 4.5F);
            if (Math.abs(damageLagQi - actualQi) < 0.25F) {
                damageLagQi = actualQi;
            }
        }
    }

    private static int getRankColor(String rank) {
        if (rank == null || rank.isEmpty()) return 0xFFB0BEC5;
        String r = rank.toUpperCase(java.util.Locale.ROOT);
        if (r.contains("GOD") || r.contains("CREATOR") || r.contains("ABSOLUTE") || r.contains("LIMITLESS") || r.contains("SINGULARITY") || r.contains("NATIONAL") || r.contains("RULER") || r.contains("MONARCH") || r.contains("SOVEREIGN") || r.contains("ASTRAL") || r.contains("DEITY") || r.contains("COSMIC") || r.contains("TRANSCENDENT") || r.contains("OMNIPOTENT") || r.contains("INFINITE") || r.contains("OMNIVERSAL") || r.contains("MULTIVERSAL") || r.contains("PRIMORDIAL")) {
            return 0xFFFFD700;
        }
        if (r.contains("SSS") || r.contains("SS") || r.contains("UR") || r.contains("LR") || r.contains("MR") || r.contains("DEMIGOD")) {
            return 0xFFB388FF;
        }
        if (r.contains("S") || r.contains("EX")) {
            return 0xFFFF3366;
        }
        if (r.contains("A")) {
            return 0xFFFF9900;
        }
        if (r.contains("B")) {
            return 0xFF00E5FF;
        }
        if (r.contains("C")) {
            return 0xFF50FA7B;
        }
        if (r.contains("D")) {
            return 0xFF8BE9FD;
        }
        return 0xFFB0BEC5;
    }

    private static void renderHunterStatusCard(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20, boolean hasCultivation) {
        Player player = mc.player;
        if (player == null) return;

        float scale = getEffectiveHudScale(screenW, screenH);

        String rank = data.rank();
        int rankColor = getRankColor(rank);

        String rankTag = "[" + rank + "]";
        int rankTagW = mc.font.width(rankTag);
        String lvlPrefix = Component.translatable("awakening_system.hud.level_prefix").getString();
        String headRight = "\u00A76" + lvlPrefix + " " + data.level() + " \u00A7f" + player.getScoreboardName();
        int headerRequiredW = 12 + rankTagW + mc.font.width(headRight) + 12;

        float curHp = player.getHealth();
        float maxHp = Math.max(1.0F, player.getMaxHealth());
        float absorp = player.getAbsorptionAmount();

        String hpNumStr = (int) Math.ceil(curHp) + "/" + (int) Math.ceil(maxHp);
        if (absorp > 0) {
            hpNumStr += " \u00A76[+" + (int) Math.ceil(absorp) + "]";
        }
        int hpRequiredW = mc.font.width("\u00A7c\u2764 HP") + mc.font.width(hpNumStr) + 24;

        int curMp = data.mana();
        int maxMp = Math.max(1, data.maxMana());
        String mpNumStr = curMp + "/" + maxMp;
        int mpRequiredW = mc.font.width("\u00A7b\u2726 MP") + mc.font.width(mpNumStr) + 24;

        int qiRequiredW = 0;
        String qiNumStr = "";
        if (hasCultivation) {
            int curQi = data.cultivationQi();
            int maxQi = Math.max(1, data.maxCultivationQi());
            String compactRealm = Component.translatable("awakening_system.realm.compact." + data.dantianRealm()).getString();
            qiNumStr = curQi + "/" + maxQi + " (" + compactRealm + ")";
            qiRequiredW = mc.font.width("\u00A7e\u262F QI") + mc.font.width(qiNumStr) + 24;
        }

        boolean showFood = com.soybe.awakeningsystem.client.gui.GuiConfig.isHudFoodBarVisible();
        int foodRequiredW = 0;
        String foodNumStr = "";
        if (showFood) {
            var foodData = player.getFoodData();
            int foodLvl = foodData.getFoodLevel();
            float satLvl = foodData.getSaturationLevel();
            foodNumStr = foodLvl + "/20";
            if (satLvl > 0) {
                foodNumStr += " \u00A7e[+" + (int) Math.ceil(satLvl) + "]";
            }
            foodRequiredW = mc.font.width("\u00A76\u25C6 " + Component.translatable("awakening_system.hud.food").getString()) + mc.font.width(foodNumStr) + 24;
        }

        int maxContentW = Math.max(headerRequiredW, Math.max(hpRequiredW, Math.max(mpRequiredW, Math.max(qiRequiredW, foodRequiredW))));
        int cardW = Math.max(168, maxContentW);
        int maxScreenW = (int) (screenW / scale) - 16;
        if (maxScreenW > 168) {
            cardW = Math.min(cardW, maxScreenW);
        }

        int cardH = 72;
        if (hasCultivation) cardH += 16;
        if (showFood) cardH += 16;

        int cardX;
        int cardY;

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.statusCardX >= 0 && com.soybe.awakeningsystem.client.gui.GuiConfig.statusCardY >= 0) {
            cardX = (int) (com.soybe.awakeningsystem.client.gui.GuiConfig.statusCardX / scale);
            cardY = (int) (com.soybe.awakeningsystem.client.gui.GuiConfig.statusCardY / scale);
        } else {
            cardX = 8;
            cardY = 8;
            switch (HUD_POSITION) {
                case BOTTOM_LEFT -> {
                    cardX = 8;
                    cardY = (int) (screenH / scale) - cardH - 10;
                }
                case ABOVE_HOTBAR -> {
                    cardX = (int) ((screenW / scale - cardW) / 2);
                    cardY = (int) ((screenH - 54) / scale - cardH);
                }
                case TOP_RIGHT -> {
                    cardX = (int) (screenW / scale) - cardW - 8;
                    cardY = 8;
                }
                case TOP_LEFT -> {
                    cardX = 8;
                    cardY = 8;
                }
            }
        }

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);

        // Modern Glassmorphism & Cybernetic Arcane Framing
        int borderColor = isSys40 ? 0xD0DFB76C : (isSys30 ? 0xC000FF88 : (isSys20 ? 0xC09D4EDD : 0xC000E5FF));
        int glowColor = isSys40 ? 0x404EE2EC : (isSys30 ? 0x3000FF88 : (isSys20 ? 0x309D4EDD : 0x3000E5FF));
        int bgTop = isSys40 ? 0xEE0B1828 : (isSys30 ? 0xEE081A16 : (isSys20 ? 0xEE180C2A : 0xEE091422));
        int bgBottom = isSys40 ? 0xF6050C16 : (isSys30 ? 0xF6040F0D : (isSys20 ? 0xF60D0518 : 0xF6040810));

        // Double soft outer aura
        graphics.fill(cardX - 2, cardY - 2, cardX + cardW + 2, cardY + cardH + 2, (glowColor & 0x00FFFFFF) | 0x12000000);
        graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, (glowColor & 0x00FFFFFF) | 0x2A000000);

        // High-tech border frame
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, borderColor);

        // Frosted glass vertical gradient
        graphics.fillGradient(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, bgTop, bgBottom);

        // Subtle top glass specular shine
        graphics.fill(cardX + 2, cardY + 1, cardX + cardW - 2, cardY + 2, 0x40FFFFFF);

        // Left glowing rank accent stripe
        graphics.fill(cardX + 1, cardY + 1, cardX + 3, cardY + cardH - 1, rankColor);

        // Stylized cybernetic corner L-brackets
        int cornerCol = isSys40 ? 0xFFFFD700 : (isSys30 ? 0xFF00FF88 : (isSys20 ? 0xFFE0AAFF : 0xFF70E8FF));
        graphics.fill(cardX, cardY, cardX + 5, cardY + 1, cornerCol);
        graphics.fill(cardX, cardY, cardX + 1, cardY + 5, cornerCol);
        graphics.fill(cardX + cardW - 5, cardY, cardX + cardW, cardY + 1, cornerCol);
        graphics.fill(cardX + cardW - 1, cardY, cardX + cardW, cardY + 5, cornerCol);
        graphics.fill(cardX, cardY + cardH - 1, cardX + 5, cardY + cardH, cornerCol);
        graphics.fill(cardX, cardY + cardH - 5, cardX + 1, cardY + cardH, cornerCol);
        graphics.fill(cardX + cardW - 5, cardY + cardH - 1, cardX + cardW, cardY + cardH, cornerCol);
        graphics.fill(cardX + cardW - 1, cardY + cardH - 5, cardX + cardW, cardY + cardH, cornerCol);

        int curY = cardY + 5;

        // Rank pill badge
        int badgeX = cardX + 6;
        int badgeW = rankTagW + 6;
        graphics.fill(badgeX, curY - 1, badgeX + badgeW, curY + 10, 0xEE0D1520);
        graphics.renderOutline(badgeX, curY - 1, badgeW, 11, (rankColor & 0x00FFFFFF) | 0xDD000000);
        graphics.drawString(mc.font, rankTag, badgeX + 3, curY + 1, rankColor, false);

        // Level & Name
        int headX = badgeX + badgeW + 5;
        String lvlStr = "\u00A76" + lvlPrefix + " " + data.level() + " ";
        graphics.drawString(mc.font, lvlStr, headX, curY + 1, 0xFFFFD700, false);
        graphics.drawString(mc.font, player.getScoreboardName(), headX + mc.font.width(lvlStr), curY + 1, 0xFFFFFFFF, false);

        // Top-right Armor Badge (if armor > 0)
        int armor = player.getArmorValue();
        if (armor > 0) {
            String armorHeader = "§b🛡 " + armor;
            graphics.drawString(mc.font, armorHeader, cardX + cardW - 7 - mc.font.width(armorHeader), curY + 1, 0xFF00E5FF, false);
        }
        curY += 13;

        // Elegant separator line
        graphics.fill(cardX + 6, curY, cardX + cardW - 6, curY + 1, isSys40 ? 0x60DFB76C : (isSys30 ? 0x5000FF88 : (isSys20 ? 0x509D4EDD : 0x5000E5FF)));
        curY += 4;

        // HP Label & Numbers
        graphics.drawString(mc.font, "\u00A7c\u2764 HP", cardX + 7, curY, 0xFFFF4D4D, false);
        graphics.drawString(mc.font, hpNumStr, cardX + cardW - 7 - mc.font.width(hpNumStr), curY, 0xFFF0F0F0, false);
        curY += 9;

        int barX = cardX + 7;
        int barW = cardW - 14;
        int hpBarH = 5;

        // HP Dark track
        graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + hpBarH + 1, 0xFF350E14);
        graphics.fill(barX, curY, barX + barW, curY + hpBarH, 0xE0150406);

        // Lag damage trail
        float lagRatio = Math.max(0.0F, Math.min(1.0F, damageLagHealth / maxHp));
        int lagW = (int) (barW * lagRatio);
        if (lagW > 0) {
            graphics.fill(barX, curY, barX + lagW, curY + hpBarH, 0xFFFF9F43);
        }

        // Current HP fill + Top gloss
        float hpRatio = Math.max(0.0F, Math.min(1.0F, curHp / maxHp));
        int curHpW = (int) (barW * hpRatio);
        if (curHpW > 0) {
            graphics.fillGradient(barX, curY, barX + curHpW, curY + hpBarH, 0xFFFF385C, 0xFFD6183C);
            graphics.fill(barX, curY, barX + curHpW, curY + 1, 0xFFFF85A1);
        }

        // Absorption bar
        if (absorp > 0) {
            float absRatio = Math.max(0.0F, Math.min(1.0F, absorp / maxHp));
            int absW = (int) (barW * absRatio);
            if (absW > 0) {
                graphics.fill(barX, curY + hpBarH - 2, barX + Math.min(barW, absW), curY + hpBarH, 0xFFFFD700);
            }
        }
        curY += 8;

        // MP Label & Numbers
        graphics.drawString(mc.font, "\u00A7b\u2726 MP", cardX + 7, curY, 0xFF00E5FF, false);
        graphics.drawString(mc.font, mpNumStr, cardX + cardW - 7 - mc.font.width(mpNumStr), curY, 0xFFB0E0E6, false);
        curY += 9;

        int mpBarH = 5;
        graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + mpBarH + 1, 0xFF0B2538);
        graphics.fill(barX, curY, barX + barW, curY + mpBarH, 0xE005111B);

        float lagMpRatio = Math.max(0.0F, Math.min(1.0F, damageLagMp / maxMp));
        int lagMpW = (int) (barW * lagMpRatio);
        if (lagMpW > 0) {
            int lagMpFill = isSys20 ? 0x907B3DAA : (isSys30 ? 0x901D7D4B : 0x9000789E);
            graphics.fill(barX, curY, barX + lagMpW, curY + mpBarH, lagMpFill);
        }

        float mpRatio = Math.max(0.0F, Math.min(1.0F, (float) curMp / maxMp));
        int curMpW = (int) (barW * mpRatio);
        if (curMpW > 0) {
            int mpFillTop = isSys20 ? 0xFFD1B3FF : (isSys30 ? 0xFF50FA7B : 0xFF00E5FF);
            int mpFillBot = isSys20 ? 0xFF8A2BE2 : (isSys30 ? 0xFF1DB954 : 0xFF0080FF);
            graphics.fillGradient(barX, curY, barX + curMpW, curY + mpBarH, mpFillTop, mpFillBot);
            graphics.fill(barX, curY, barX + curMpW, curY + 1, isSys20 ? 0xFFE8D7FF : (isSys30 ? 0xFFA8FFD1 : 0xFF88F2FF));
        }
        curY += 8;

        // QI Bar (Cultivation)
        if (hasCultivation) {
            int curQi = data.cultivationQi();
            int maxQi = Math.max(1, data.maxCultivationQi());
            graphics.drawString(mc.font, "\u00A7e\u262F QI", cardX + 7, curY, 0xFFFFD700, false);
            graphics.drawString(mc.font, qiNumStr, cardX + cardW - 7 - mc.font.width(qiNumStr), curY, 0xFFFFF0A0, false);
            curY += 9;

            int qiBarH = 5;
            graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + qiBarH + 1, 0xFF382A0B);
            graphics.fill(barX, curY, barX + barW, curY + qiBarH, 0xE0181205);

            float lagQiRatio = Math.max(0.0F, Math.min(1.0F, damageLagQi / maxQi));
            int lagQiW = (int) (barW * lagQiRatio);
            if (lagQiW > 0) {
                graphics.fill(barX, curY, barX + lagQiW, curY + qiBarH, 0x90B38000);
            }

            float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) curQi / maxQi));
            int curQiW = (int) (barW * qiRatio);
            if (curQiW > 0) {
                graphics.fillGradient(barX, curY, barX + curQiW, curY + qiBarH, 0xFFFFD700, 0xFFFF8C00);
                graphics.fill(barX, curY, barX + curQiW, curY + 1, 0xFFFFF5B8);
            }
            curY += 8;
        }

        // FOOD Bar (Integrated Hunger)
        if (showFood) {
            var foodData = player.getFoodData();
            int foodLvl = foodData.getFoodLevel();
            float satLvl = foodData.getSaturationLevel();

            boolean isStarving = foodLvl <= 6;
            int foodLabelCol = isStarving ? 0xFFFF4444 : 0xFFFF9800;
            String foodLabel = Component.translatable("awakening_system.hud.food").getString();

            graphics.drawString(mc.font, "\u00A76\u25C6 " + foodLabel, cardX + 7, curY, foodLabelCol, false);
            graphics.drawString(mc.font, foodNumStr, cardX + cardW - 7 - mc.font.width(foodNumStr), curY, 0xFFFFE082, false);
            curY += 9;

            int foodBarH = 5;
            graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + foodBarH + 1, 0xFF351F0A);
            graphics.fill(barX, curY, barX + barW, curY + foodBarH, 0xE01A0E04);

            float foodRatio = Math.max(0.0F, Math.min(1.0F, foodLvl / 20.0F));
            int curFoodW = (int) (barW * foodRatio);
            if (curFoodW > 0) {
                graphics.fillGradient(barX, curY, barX + curFoodW, curY + foodBarH, 0xFFFF9800, 0xFFE65100);
                graphics.fill(barX, curY, barX + curFoodW, curY + 1, 0xFFFFD084);
            }

            // Saturation layer overlay
            if (satLvl > 0) {
                float satRatio = Math.max(0.0F, Math.min(1.0F, satLvl / 20.0F));
                int satW = (int) (barW * satRatio);
                if (satW > 0) {
                    graphics.fill(barX, curY + foodBarH - 2, barX + Math.min(barW, satW), curY + foodBarH, 0xFFFFEE58);
                }
            }

            // Starving alert pulse outline
            if (isStarving) {
                float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.009) * 0.5 + 0.5);
                int pulseAlpha = (int) (pulse * 200);
                if (pulseAlpha > 30) {
                    graphics.renderOutline(barX - 1, curY - 1, barW + 2, foodBarH + 2, (pulseAlpha << 24) | 0xFF3333);
                }
            }
            curY += 8;
        }

        // Modern Glass Pill Footer: Defense | Attack | Gold/Primos
        int attack = (int) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        int coins = data.coins();
        String coinStr = coins >= 1000000 ? String.format(java.util.Locale.US, "%.1fM", coins / 1000000.0) :
                (coins >= 1000 ? String.format(java.util.Locale.US, "%.1fK", coins / 1000.0) : String.valueOf(coins));

        int footerH = 11;
        graphics.fill(cardX + 6, curY, cardX + cardW - 6, curY + footerH, 0xD0081018);
        graphics.renderOutline(cardX + 6, curY, cardW - 12, footerH, 0x50276A82);

        int colW = (cardW - 14) / 3;
        graphics.drawString(mc.font, "§b🛡 " + armor, cardX + 9, curY + 2, 0xFFFFFFFF, false);
        graphics.drawString(mc.font, "§c⚔ " + attack, cardX + 9 + colW, curY + 2, 0xFFFFFFFF, false);
        if (isSys40 && data.primogems() > 0) {
            graphics.drawString(mc.font, "§b✦ " + data.primogems(), cardX + 9 + colW * 2, curY + 2, 0xFFFFFFFF, false);
        } else {
            graphics.drawString(mc.font, "§e🪙 " + coinStr, cardX + 9 + colW * 2, curY + 2, 0xFFFFFFFF, false);
        }

        graphics.pose().popPose();
    }

    public static void renderCompactManaBar(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20, boolean hasCultivation) {
        if (data == null) return;
        int barW = 81;
        int barH = 5;
        int barX = screenW / 2 + 10;
        int barY = screenH - 49;

        int curMp = data.mana();
        int maxMp = Math.max(1, data.maxMana());
        float mpRatio = Math.max(0.0F, Math.min(1.0F, (float) curMp / maxMp));

        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF0B2538);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xE005111B);

        float lagMpRatio = Math.max(0.0F, Math.min(1.0F, damageLagMp / maxMp));
        int lagMpW = (int) (barW * lagMpRatio);
        if (lagMpW > 0) {
            int lagMpFill = isSys20 ? 0x907B3DAA : (isSys30 ? 0x901D7D4B : 0x9000789E);
            graphics.fill(barX, barY, barX + lagMpW, barY + barH, lagMpFill);
        }

        int filledW = (int) (barW * mpRatio);
        if (filledW > 0) {
            int mpFillTop = isSys20 ? 0xFFD1B3FF : (isSys30 ? 0xFF50FA7B : 0xFF00E5FF);
            int mpFillBot = isSys20 ? 0xFF8A2BE2 : (isSys30 ? 0xFF1DB954 : 0xFF0080FF);
            graphics.fillGradient(barX, barY, barX + filledW, barY + barH, mpFillTop, mpFillBot);
            graphics.fill(barX, barY, barX + filledW, barY + 1, isSys20 ? 0xFFE8D7FF : (isSys30 ? 0xFFA8FFD1 : 0xFF88F2FF));
        }

        String mpText = "\u2726 " + curMp + "/" + maxMp;
        graphics.drawString(mc.font, mpText, barX + (barW - mc.font.width(mpText)) / 2, barY - 8, 0xFFB0E0E6, true);

        if (hasCultivation) {
            int qiY = barY - 14;
            int curQi = data.cultivationQi();
            int maxQi = Math.max(1, data.maxCultivationQi());
            float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) curQi / maxQi));

            graphics.fill(barX - 1, qiY - 1, barX + barW + 1, qiY + 4, 0xFF382A0B);
            graphics.fill(barX, qiY, barX + barW, qiY + 3, 0xE0181205);

            int curQiW = (int) (barW * qiRatio);
            if (curQiW > 0) {
                graphics.fillGradient(barX, qiY, barX + curQiW, qiY + 3, 0xFFFFD700, 0xFFFF8C00);
            }
            String qiText = "\u262F " + curQi + "/" + maxQi;
            graphics.drawString(mc.font, qiText, barX + (barW - mc.font.width(qiText)) / 2, qiY - 8, 0xFFFFF0A0, true);
        }
    }

    public static void renderCompactHotbarBars(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20, boolean hasCultivation) {
        Player player = mc.player;
        if (player == null || data == null) return;

        int barW = 81;
        int barH = 6;

        int hpX = screenW / 2 - 91;
        int hpY = screenH - 39;

        float curHp = player.getHealth();
        float maxHp = Math.max(1.0F, player.getMaxHealth());
        float absorp = player.getAbsorptionAmount();

        graphics.fill(hpX - 1, hpY - 1, hpX + barW + 1, hpY + barH + 1, 0xFF350E14);
        graphics.fill(hpX, hpY, hpX + barW, hpY + barH, 0xE0150406);

        float lagRatio = Math.max(0.0F, Math.min(1.0F, damageLagHealth / maxHp));
        int lagW = (int) (barW * lagRatio);
        if (lagW > 0) {
            graphics.fill(hpX, hpY, hpX + lagW, hpY + barH, 0xFFFF9F43);
        }

        float hpRatio = Math.max(0.0F, Math.min(1.0F, curHp / maxHp));
        int curHpW = (int) (barW * hpRatio);
        if (curHpW > 0) {
            graphics.fillGradient(hpX, hpY, hpX + curHpW, hpY + barH, 0xFFFF385C, 0xFFD6183C);
            graphics.fill(hpX, hpY, hpX + curHpW, hpY + 1, 0xFFFF85A1);
        }
        if (absorp > 0) {
            float absRatio = Math.max(0.0F, Math.min(1.0F, absorp / maxHp));
            int absW = (int) (barW * absRatio);
            if (absW > 0) {
                graphics.fill(hpX, hpY + barH - 2, hpX + Math.min(barW, absW), hpY + barH, 0xFFFFD700);
            }
        }

        String hpText = "\u2764 " + (int) Math.ceil(curHp) + "/" + (int) Math.ceil(maxHp);
        if (absorp > 0) hpText += " \u00A76[+" + (int) Math.ceil(absorp) + "]";
        graphics.drawString(mc.font, hpText, hpX + 2, hpY - 8, 0xFFFF85A1, true);

        int armor = player.getArmorValue();
        if (armor > 0) {
            String armText = "§b🛡 " + armor + "  §6Nv." + data.level();
            graphics.drawString(mc.font, armText, hpX + 2, hpY - 16, 0xFFFFFFFF, true);
        }

        // Compact Hunger Bar on right side of hotbar (mirrors HP on left)
        boolean showFood = com.soybe.awakeningsystem.client.gui.GuiConfig.isHudFoodBarVisible();
        if (showFood) {
            int foodX = screenW / 2 + 10;
            int foodY = screenH - 39;
            var foodData = player.getFoodData();
            int foodLvl = foodData.getFoodLevel();
            float satLvl = foodData.getSaturationLevel();

            graphics.fill(foodX - 1, foodY - 1, foodX + barW + 1, foodY + barH + 1, 0xFF351F0A);
            graphics.fill(foodX, foodY, foodX + barW, foodY + barH, 0xE01A0E04);

            float foodRatio = Math.max(0.0F, Math.min(1.0F, foodLvl / 20.0F));
            int curFoodW = (int) (barW * foodRatio);
            if (curFoodW > 0) {
                graphics.fillGradient(foodX, foodY, foodX + curFoodW, foodY + barH, 0xFFFF9800, 0xFFE65100);
                graphics.fill(foodX, foodY, foodX + curFoodW, foodY + 1, 0xFFFFD084);
            }

            if (satLvl > 0) {
                float satRatio = Math.max(0.0F, Math.min(1.0F, satLvl / 20.0F));
                int satW = (int) (barW * satRatio);
                if (satW > 0) {
                    graphics.fill(foodX, foodY + barH - 2, foodX + Math.min(barW, satW), foodY + barH, 0xFFFFEE58);
                }
            }

            String foodText = "\u25C6 " + foodLvl + "/20";
            if (satLvl > 0) foodText += " \u00A7e[+" + (int) Math.ceil(satLvl) + "]";
            graphics.drawString(mc.font, foodText, foodX + 2, foodY - 8, 0xFFFFD084, true);
        }

        renderCompactManaBar(mc, graphics, data, screenW, screenH, isSys40, isSys30, isSys20, hasCultivation);
    }

    private static void renderSystemUpdatingBanner(Minecraft mc, GuiGraphics graphics, SystemData data, int width) {
        boolean is40 = data.isSystem40Updating();
        boolean is30 = data.isSystem30Updating();
        int ticks = is40 ? data.system40UpdateRemainingTicks() : (is30 ? data.system30UpdateRemainingTicks() : data.systemUpdateRemainingTicks());
        int totalSec = Math.max(0, ticks / 20);
        int mins = totalSec / 60;
        int secs = totalSec % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);

        Component bannerText = is40
                ? Component.translatable("awakening_system.hud.system_40_update_active", timeStr)
                : (is30
                ? Component.translatable("awakening_system.hud.system_30_update_active", timeStr)
                : Component.translatable("awakening_system.hud.system_update_active", timeStr));
        int textW = mc.font.width(bannerText);
        int bannerW = textW + 20;
        int bannerH = 16;
        int bannerX = (width - bannerW) / 2;
        int bossBottom = getBossBarOffset(mc);
        int bannerY = bossBottom > 0 ? (bossBottom + 4) : 8;

        int borderColor = is40 ? 0xFFDFB76C : (is30 ? 0xFF00FF88 : 0xFFFFAA00);
        int textColor = is40 ? 0xFFDFB76C : (is30 ? 0xFF50FA7B : 0xFFFFD700);

        graphics.fill(bannerX - 1, bannerY - 1, bannerX + bannerW + 1, bannerY + bannerH + 1, borderColor);
        graphics.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, is40 ? 0xDD08111D : 0xDD0C141D);

        graphics.drawCenteredString(mc.font, bannerText, width / 2, bannerY + 4, textColor);
    }

    private static int calculateDesiredTargetHudY(Minecraft mc, SystemData data) {
        int bossBottom = getBossBarOffset(mc);
        int y = bossBottom > 0 ? (bossBottom + 6) : 8;

        if (com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.isDangerAlertActive()) {
            int dangerBottom = com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.getDangerAlertBottom(mc);
            y = Math.max(y, dangerBottom + 5);
        }

        if (data != null && data.isAnySystemUpdating()) {
            int bannerBottom = (bossBottom > 0 ? (bossBottom + 6) : 8) + 16;
            y = Math.max(y, bannerBottom + 6);
        }

        if (data != null && data.isDokkaebiScenarioActive()) {
            int bannerOffset = (data.isAnySystemUpdating() ? 32 : 0) + (com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.isDangerAlertActive() ? 18 : 0);
            int dokkaebiBottom = Math.max(12, bossBottom + bannerOffset + 8) + 28;
            y = Math.max(y, dokkaebiBottom + 6);
        }

        int notifBottom = AwakeningSystemClient.getNotificationHeight();
        if (notifBottom > 0) {
            y = Math.max(y, notifBottom + 6);
        }

        return y;
    }

    private static void renderEntityAppraisal(Minecraft mc, GuiGraphics graphics, SystemData data, int width, boolean isSys40, boolean isSys30, boolean isSys20) {
        if (mc.hitResult instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living
                && living.isAlive()) {
            if (living instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature) {
                return;
            }
            Component name = living.getName();
            if (!living.hasCustomName() && living instanceof net.minecraft.world.entity.monster.Enemy && !(living instanceof com.soybe.awakeningsystem.entity.LimboOphanimEntity)) {
                int clientLevel = data != null ? Math.max(1, data.level()) : 1;
                int perceived = AwakeningSystemClient.getPerceivedMobLevel(living, clientLevel);
                net.minecraft.ChatFormatting color = perceived >= 80 ? net.minecraft.ChatFormatting.DARK_RED : (perceived >= 50 ? net.minecraft.ChatFormatting.RED : (perceived >= 25 ? net.minecraft.ChatFormatting.GOLD : (perceived >= 15 ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.GREEN)));
                name = Component.literal("[Nv. " + perceived + "] ").withStyle(color)
                        .append(living.getType().getDescription().copy().withStyle(net.minecraft.ChatFormatting.WHITE));
            }

            boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(living);
            boolean isFriendly = living.getType().getCategory().isFriendly() || living instanceof net.minecraft.world.entity.animal.Animal || living instanceof net.minecraft.world.entity.npc.Npc;

            long now = System.currentTimeMillis();
            if (lastCardYRenderTime == 0L) lastCardYRenderTime = now;
            float dt = Math.min(0.1F, (now - lastCardYRenderTime) / 1000.0F);
            lastCardYRenderTime = now;

            float targetY = (float) calculateDesiredTargetHudY(mc, data);
            if (isBoss) {
                targetY += 4.0F;
            }
            if (animatedCardY < 0.0F) {
                animatedCardY = targetY;
            } else {
                animatedCardY += (targetY - animatedCardY) * Math.min(1.0F, dt * 14.0F);
            }
            int cardY = Math.round(animatedCardY);

            if (isFriendly) {
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();
                float hpRatio = Math.max(0.0F, Math.min(1.0F, hp / maxHp));
                int hpPercent = (int) (hpRatio * 100);

                Component titleComp = Component.literal("§a♥ §f" + name.getString());
                int fullTitleW = mc.font.width(titleComp);
                Component hpComp = Component.literal("§a" + (int) hp + "§7/§2" + (int) maxHp + " §a(" + hpPercent + "%)");
                int fullHpW = mc.font.width(hpComp);

                int maxFriendlyW = (com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.isMinimapActive() && width < 500)
                        ? Math.min(width - 32, 210)
                        : width - 16;
                int cardW = Math.min(Math.max(150, fullTitleW + fullHpW + 28), maxFriendlyW);
                int cardH = 24;
                int cardX = (com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalX >= 0)
                        ? com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalX
                        : (width - cardW) / 2;
                if (com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalY >= 0) {
                    cardY = com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalY;
                }

                graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, isSys30 ? 0x90D4AF37 : (isSys20 ? 0x909D4EDD : 0x80276A82));
                graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, isSys30 ? 0xD00B1513 : (isSys20 ? 0xD0170A24 : 0xD008131C));

                int cornerColor = (isSys30 || isSys20) ? 0xFFFFD700 : 0xFF70E8FF;
                graphics.fill(cardX, cardY, cardX + 4, cardY + 1, cornerColor);
                graphics.fill(cardX, cardY, cardX + 1, cardY + 4, cornerColor);
                graphics.fill(cardX + cardW - 4, cardY, cardX + cardW, cardY + 1, cornerColor);
                graphics.fill(cardX + cardW - 1, cardY, cardX + cardW, cardY + 4, cornerColor);

                graphics.drawString(mc.font, titleComp, cardX + 8, cardY + 4, 0xFFFFFFFF);
                graphics.drawString(mc.font, hpComp, cardX + cardW - fullHpW - 8, cardY + 4, 0xFFFFFFFF);

                int barW = cardW - 16;
                int barH = 5;
                int barX = cardX + 8;
                int barY = cardY + 15;

                graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF142416);
                graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF0D180F);

                int filled = (int) (barW * hpRatio);
                if (filled > 0) {
                    graphics.fill(barX, barY, barX + filled, barY + barH, 0xFF2ECC71);
                    graphics.fill(barX, barY, barX + filled, barY + 1, 0xFFA8E6CF);
                }
            } else {
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();
                float hpRatio = Math.max(0.0F, Math.min(1.0F, hp / maxHp));
                int hpPercent = (int) (hpRatio * 100);

                Component bossPrefix = isBoss ? Component.literal("§4☠ §c[").append(Component.translatable("awakening_system.hud.boss")).append(Component.literal("] ")) : Component.literal("§c⚔ ");
                Component titleComp = bossPrefix.copy().append(name);
                int fullTitleW = mc.font.width(titleComp);

                Component hpText = Component.literal((int) hp + "§7/§c" + (int) maxHp + " §e(" + hpPercent + "%)");
                int fullHpW = mc.font.width(hpText);

                int minW = isBoss ? 240 : 190;
                int neededW = Math.max(fullTitleW, fullHpW) + 24;
                int cardW = Math.min(Math.max(minW, neededW), (com.soybe.awakeningsystem.client.map.HunterMinimapRenderer.isMinimapActive() && width < 500) ? Math.min(width - 32, 210) : width - 16);
                int cardH = isBoss ? 32 : 28;
                int cardX = (com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalX >= 0)
                        ? com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalX
                        : (width - cardW) / 2;
                if (com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalY >= 0) {
                    cardY = com.soybe.awakeningsystem.client.gui.GuiConfig.appraisalY;
                }

                int borderColor = isBoss ? 0xFFFF3366 : (isSys40 ? 0xFFDFB76C : (isSys30 ? 0xFFD4AF37 : (isSys20 ? 0xFF9D4EDD : 0xFF276A82)));
                int bgColor = isBoss ? 0xE81A050B : (isSys40 ? 0xD80A1524 : (isSys30 ? 0xD8081613 : (isSys20 ? 0xD8140A22 : 0xD808131C)));

                graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, borderColor);
                graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, bgColor);

                int accentCol = isBoss ? 0xFFFF5555 : (isSys30 ? 0xFFFFD700 : (isSys20 ? 0xFFE0AAFF : 0xFF00E5FF));
                graphics.fill(cardX, cardY, cardX + 5, cardY + 2, accentCol);
                graphics.fill(cardX, cardY, cardX + 2, cardY + 5, accentCol);
                graphics.fill(cardX + cardW - 5, cardY, cardX + cardW, cardY + 2, accentCol);
                graphics.fill(cardX + cardW - 2, cardY, cardX + cardW, cardY + 5, accentCol);

                graphics.drawString(mc.font, titleComp, cardX + 8, cardY + 4, 0xFFFFFFFF);

                int barW = cardW - 16;
                int barH = isBoss ? 9 : 8;
                int barX = cardX + 8;
                int barY = cardY + (isBoss ? 17 : 15);

                graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF2E0C11);
                graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF1A0507);

                int filled = (int) (barW * hpRatio);
                if (filled > 0) {
                    int barColor = isBoss ? 0xFFFF1E56 : 0xFFE74C3C;
                    graphics.fill(barX, barY, barX + filled, barY + barH, barColor);
                    graphics.fill(barX, barY, barX + filled, barY + 1, 0xFFFF85A1);
                }

                int textX = barX + (barW - fullHpW) / 2;
                int textY = barY + (barH - 8) / 2;
                graphics.drawString(mc.font, hpText, textX, textY, 0xFFFFFFFF, true);
            }
        }
    }

    private static void renderCombatCombo(Minecraft mc, GuiGraphics graphics, SystemData data, int width, int height) {
        int combo = data.comboCount();
        if (combo > 1) {
            int comboW = 86;
            int comboH = 20;
            int comboX = (com.soybe.awakeningsystem.client.gui.GuiConfig.comboX >= 0)
                    ? com.soybe.awakeningsystem.client.gui.GuiConfig.comboX
                    : Math.max(6, width - comboW - 12);
            int comboY = (com.soybe.awakeningsystem.client.gui.GuiConfig.comboY >= 0)
                    ? com.soybe.awakeningsystem.client.gui.GuiConfig.comboY
                    : Math.max(6, height - 42);

            int borderCol = combo >= 10 ? 0xFFFF4444 : (combo >= 5 ? 0xFFFF8800 : 0xFF276A82);
            graphics.fill(comboX - 1, comboY - 1, comboX + comboW + 1, comboY + comboH + 1, borderCol);
            graphics.fill(comboX, comboY, comboX + comboW, comboY + comboH, 0xB00C141D);

            String text = combo + " COMBO";
            graphics.drawString(mc.font, Component.literal(text), comboX + 4, comboY + 2, 0xFFFFD866);

            float decayRatio = Math.max(0.0F, Math.min(1.0F, data.comboTicks() / 80.0F));
            int decayW = (int) (78 * decayRatio);
            graphics.fill(comboX + 4, comboY + 13, comboX + 82, comboY + 16, 0xFF1C2833);
            if (decayW > 0) {
                graphics.fill(comboX + 4, comboY + 13, comboX + 4 + decayW, comboY + 16, 0xFFFFD866);
            }
        }
    }

    private static java.lang.reflect.Field BOSS_EVENTS_FIELD = null;

    public static int getBossBarOffset(Minecraft mc) {
        int count = 0;
        if (BOSS_EVENTS_FIELD == null) {
            try {
                for (java.lang.reflect.Field f : net.minecraft.client.gui.components.BossHealthOverlay.class.getDeclaredFields()) {
                    if (java.util.Map.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        BOSS_EVENTS_FIELD = f;
                        break;
                    }
                }
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Error reflecting BossHealthOverlay: {}", t.getMessage());
            }
        }
        if (BOSS_EVENTS_FIELD != null && mc.gui != null) {
            try {
                var overlay = mc.gui.getBossOverlay();
                if (overlay != null) {
                    var map = (java.util.Map<?, ?>) BOSS_EVENTS_FIELD.get(overlay);
                    if (map != null && !map.isEmpty()) {
                        count = map.size();
                    }
                }
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Error obtaining active boss bar count: {}", t.getMessage());
            }
        }
        if (count > 0) {
            return 12 + Math.min(count, 4) * 19 + 14;
        }
        if (mc.level != null) {
            String path = mc.level.dimension().location().getPath();
            if (path.contains("tower") || path.contains("dungeon") || path.contains("pagoda")) {
                return 48;
            }
        }
        return 0;
    }

    private static void renderDokkaebiScenario(Minecraft mc, GuiGraphics graphics, SystemData data, int width) {
        int totalSec = Math.max(0, data.dokkaebiScenarioTimer() / 20);
        int mins = totalSec / 60;
        int secs = totalSec % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);

        int cardW = Math.min(240, width - 16);
        int cardH = 28;
        int cardX = (width - cardW) / 2;
        int bannerOffset = data.isAnySystemUpdating() ? 32 : 0;
        int cardY = Math.max(12, getBossBarOffset(mc) + bannerOffset + 8);

        int extraKills = data.dokkaebiScenarioKills() - data.dokkaebiScenarioTargetKills();
        int borderCol = extraKills > 0 ? 0xFFFFD700 : 0xFFE74C3C;
        graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, borderCol);
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xEE1A0A0D);

        int gold = 0xFFFFD700;
        graphics.fill(cardX, cardY, cardX + 4, cardY + 2, gold);
        graphics.fill(cardX + cardW - 4, cardY, cardX + cardW, cardY + 2, gold);
        graphics.fill(cardX, cardY + cardH - 2, cardX + 4, cardY + cardH, gold);
        graphics.fill(cardX + cardW - 4, cardY + cardH - 2, cardX + cardW, cardY + cardH, gold);

        Component header = Component.translatable("awakening_system.hud.dokkaebi_scenario").copy().withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD);
        graphics.drawCenteredString(mc.font, header, cardX + cardW / 2, cardY + 3, extraKills > 0 ? 0xFFFFD700 : 0xFFFF5555);

        Component progress;
        if (extraKills > 0) {
            progress = Component.translatable("awakening_system.hud.dokkaebi_extra_kills", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills(), extraKills);
        } else {
            String titleKey = data.dokkaebiScenarioTitle();
            if (titleKey != null && titleKey.contains("iron")) {
                progress = Component.translatable("awakening_system.hud.dokkaebi_objective_iron", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills());
            } else if (titleKey != null && titleKey.contains("diamonds")) {
                progress = Component.translatable("awakening_system.hud.dokkaebi_objective_diamonds", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills());
            } else if (titleKey != null && titleKey.contains("combo")) {
                progress = Component.translatable("awakening_system.hud.dokkaebi_objective_combo", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills());
            } else if (titleKey != null && titleKey.contains("survive")) {
                progress = Component.translatable("awakening_system.hud.dokkaebi_objective_survive", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills());
            } else {
                progress = Component.translatable("awakening_system.hud.dokkaebi_objective_kills", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills());
            }
        }
        graphics.drawCenteredString(mc.font, progress, cardX + cardW / 2, cardY + 14, 0xFFFFFFFF);
    }

    private static void renderLockOverlay(GuiGraphics graphics, int sx, int sy, int sw, int sh) {
        graphics.fill(sx, sy, sx + sw, sy + sh, 0xB8090B10);

        graphics.fill(sx + 3, sy + 3, sx + 6, sy + 5, 0xFF999EA8);
        graphics.fill(sx + 7, sy + 6, sx + 10, sy + 8, 0xFFB8BCC6);
        graphics.fill(sx + 11, sy + 9, sx + 14, sy + 11, 0xFF7F848E);
        graphics.fill(sx + sw - 6, sy + 3, sx + sw - 3, sy + 5, 0xFF999EA8);
        graphics.fill(sx + sw - 10, sy + 6, sx + sw - 7, sy + 8, 0xFFB8BCC6);
        graphics.fill(sx + sw - 14, sy + 9, sx + sw - 11, sy + 11, 0xFF7F848E);

        int cx = sx + sw / 2;
        int cy = sy + sh / 2 + 1;
        graphics.fill(cx - 3, cy - 6, cx + 3, cy - 2, 0xFFC5CBD4);
        graphics.fill(cx - 2, cy - 5, cx + 2, cy - 2, 0xFF14171D);
        graphics.fill(cx - 4, cy - 2, cx + 4, cy + 4, 0xFFE5B030);
        graphics.fill(cx - 3, cy - 1, cx + 3, cy + 3, 0xFFF5C542);
        graphics.fill(cx - 1, cy, cx + 1, cy + 2, 0xFF2A1C0A);
        graphics.fill(cx, cy + 2, cx + 1, cy + 3, 0xFF2A1C0A);
    }

    private static void renderSkillHotbar(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20) {
        long gameTime = mc.level == null ? 0 : mc.level.getGameTime();
        net.minecraft.client.KeyMapping[] mappings = {
                AwakeningSystemClient.ACTIVATE_SKILL,
                AwakeningSystemClient.SKILL_SLOT_2,
                AwakeningSystemClient.SKILL_SLOT_3,
                AwakeningSystemClient.SKILL_SLOT_4,
                AwakeningSystemClient.SKILL_SLOT_5
        };
        String[] defaultKeys = {"V", "R", "G", "B", "X"};

        boolean hasStigma = data.sponsorConstellation() != null && !data.sponsorConstellation().isEmpty();
        boolean hasAnyActiveRune = !data.getActiveRune(0).isEmpty() || !data.getActiveRune(1).isEmpty();
        int slotW = 20;
        int slotH = 19;
        int gap = 3;
        int totalSlots = 5 + (hasStigma ? 1 : 0) + (hasAnyActiveRune ? 2 : 0);
        int hotbarW = totalSlots * slotW + (totalSlots - 1) * gap;

        int vanillaHotbarLeft = (screenW - 182) / 2;
        int maxAvailW = vanillaHotbarLeft - 14;

        boolean forceAbove = (SKILL_BAR_POSITION == SkillBarPosition.ABOVE_HOTBAR) || (maxAvailW <= 70);

        int startX;
        int startY;
        float skillScale = 1.0F;

        if (com.soybe.awakeningsystem.client.gui.GuiConfig.skillBarX >= 0 && com.soybe.awakeningsystem.client.gui.GuiConfig.skillBarY >= 0) {
            startX = com.soybe.awakeningsystem.client.gui.GuiConfig.skillBarX;
            startY = com.soybe.awakeningsystem.client.gui.GuiConfig.skillBarY;
        } else if (forceAbove) {
            startX = (screenW - hotbarW) / 2;
            boolean isSurvival = mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator();
            startY = isSurvival ? (screenH - 58) : (screenH - 46);
            if (screenW - 16 < hotbarW) {
                skillScale = Math.max(0.65F, (float) (screenW - 16) / hotbarW);
            }
        } else {
            startX = 8;
            if (maxAvailW < hotbarW) {
                skillScale = Math.max(0.65F, (float) maxAvailW / hotbarW);
            }
            if (HUD_POSITION == HudPosition.BOTTOM_LEFT) {
                float cardScale = getEffectiveHudScale(screenW, screenH);
                boolean hasCultivation = (data.dantianRealm() > 0 || data.cultivationQi() > 0 || data.cultivationSubLevel() > 1
                        || "demonic_cultivator".equals(data.hunterClass()) || "sword_saint".equals(data.hunterClass())
                        || data.murimUnlocked() || data.system30Updated() || data.getOpenedMeridiansCount() > 0 || (mc.player != null && mc.player.level().dimension().location().getPath().contains("murim")));
                int cardH = hasCultivation ? 86 : 70;
                int effectiveCardH = (int) (cardH * cardScale);
                startY = screenH - effectiveCardH - (int) (slotH * skillScale) - 14;
            } else {
                boolean isChat = mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
                int bottomPadding = isChat ? 20 : 6;
                if (!isChat && net.neoforged.fml.ModList.get().isLoaded("irons_spellbooks")) {
                    bottomPadding += 26;
                }
                startY = screenH - (int) (slotH * skillScale) - bottomPadding;
            }
        }

        graphics.pose().pushPose();
        graphics.pose().translate(startX, startY, 0.0F);
        if (skillScale != 1.0F) {
            graphics.pose().scale(skillScale, skillScale, 1.0F);
        }

        for (int i = 0; i < 5; i++) {
            int sx = i * (slotW + gap);
            String skillId = data.getEquippedActiveSkill(i);
            boolean hasSkill = skillId != null && !skillId.isEmpty() && SkillDefinitions.isAvailable(skillId);
            int cd = hasSkill ? data.activeCooldownSeconds(skillId, gameTime) : 0;

            int normalBorder = isSys40 ? 0xC0DFB76C : (isSys30 ? 0xB0D4AF37 : (isSys20 ? 0xB08A2BE2 : 0xB0276A82));
            int normalBg = isSys40 ? 0xD80D1D2F : (isSys30 ? 0xD00E261E : (isSys20 ? 0xD0170A24 : 0xD00A151E));
            int borderColor = hasSkill ? (cd > 0 ? 0xFF883333 : normalBorder) : (isSys40 ? 0xFF1B2E45 : (isSys30 ? 0xFF142E24 : (isSys20 ? 0xFF3A1A54 : 0xFF1C2833)));
            int bgColor = hasSkill ? (cd > 0 ? 0xD0200808 : normalBg) : (isSys40 ? 0x9008111D : (isSys30 ? 0x900A1815 : (isSys20 ? 0x90190C2B : 0x900B1015)));
            graphics.fill(sx, 0, sx + slotW, slotH, borderColor);
            graphics.fill(sx + 1, 1, sx + slotW - 1, slotH - 1, bgColor);

            String keyLabel = defaultKeys[i];
            if (mappings[i] != null && !mappings[i].isUnbound()) {
                String k = mappings[i].getTranslatedKeyMessage().getString().toUpperCase(java.util.Locale.ROOT);
                if (!k.isEmpty()) {
                    keyLabel = k.length() > 2 ? k.substring(0, 2) : k;
                }
            }
            graphics.drawString(mc.font, keyLabel, sx + 2, 1, hasSkill ? 0xFFFFD866 : (isSys30 ? 0xFF88C999 : (isSys20 ? 0xFF9D7CB5 : 0xFF667788)), false);

            if (hasSkill) {
                if (cd > 0) {
                    graphics.fill(sx + 1, 1, sx + slotW - 1, slotH - 1, 0x88000000);
                    String cdStr = cd >= 60 ? (cd / 60) + "m" : cd + "s";
                    graphics.drawString(mc.font, cdStr, sx + (slotW - mc.font.width(cdStr)) / 2, 6, 0xFFFF5555, false);
                } else {
                    ClassSkillTree.SkillNode node = ClassSkillTree.getNode(skillId);
                    String symbol = (node != null && node.iconSymbol() != null && !node.iconSymbol().isEmpty()) ? node.iconSymbol() : null;
                    if (symbol == null) {
                        SkillDefinitions.Definition def = SkillDefinitions.get(skillId);
                        String name = def != null ? def.name().getString() : skillId;
                        symbol = name.length() > 2 ? name.substring(0, 2) : name;
                    }
                    int symW = mc.font.width(symbol);
                    int symX = sx + (slotW - symW) / 2;
                    graphics.drawString(mc.font, symbol, symX, 7, isSys30 ? 0xFF50FA7B : (isSys20 ? 0xFFFFD700 : 0xFF70E8FF), false);
                }
            }

            if (data.isSystemUpdating() || data.isSystem30Updating()) {
                renderLockOverlay(graphics, sx, 0, slotW, slotH);
            }
        }

        String sponsor = data.sponsorConstellation();
        if (sponsor != null && !sponsor.isEmpty()) {
            int stigmaX = 5 * (slotW + gap);
            int cd = (int) Math.max(0, (data.stigmaCooldownUntil() - gameTime + 19) / 20);
            int borderColor = cd > 0 ? 0xFF884400 : 0xFFFFD700;
            int bgColor = cd > 0 ? 0xD0251505 : 0xD01E1805;
            graphics.fill(stigmaX, 0, stigmaX + slotW, slotH, borderColor);
            graphics.fill(stigmaX + 1, 1, stigmaX + slotW - 1, slotH - 1, bgColor);

            String stigmaKey = "Y";
            if (AwakeningSystemClient.ACTIVATE_STIGMA != null && !AwakeningSystemClient.ACTIVATE_STIGMA.isUnbound()) {
                String k = AwakeningSystemClient.ACTIVATE_STIGMA.getTranslatedKeyMessage().getString().toUpperCase(java.util.Locale.ROOT);
                if (!k.isEmpty()) {
                    stigmaKey = k.length() > 2 ? k.substring(0, 2) : k;
                }
            }
            graphics.drawString(mc.font, stigmaKey, stigmaX + 2, 1, 0xFFFFD700, false);
            if (cd > 0) {
                graphics.fill(stigmaX + 1, 1, stigmaX + slotW - 1, slotH - 1, 0x88000000);
                String cdStr = cd >= 60 ? (cd / 60) + "m" : cd + "s";
                graphics.drawString(mc.font, cdStr, stigmaX + (slotW - mc.font.width(cdStr)) / 2, 6, 0xFFFF8844, false);
            } else {
                int symW = mc.font.width("✨");
                graphics.drawString(mc.font, "✨", stigmaX + (slotW - symW) / 2, 7, 0xFFFFE066, false);
            }

            if (data.isSystemUpdating() || data.isSystem30Updating()) {
                renderLockOverlay(graphics, stigmaX, 0, slotW, slotH);
            }
        }

        if (hasAnyActiveRune) {
            int runeStartIndex = 5 + (hasStigma ? 1 : 0);
            net.minecraft.client.KeyMapping[] runeMappings = {
                    AwakeningSystemClient.ACTIVATE_RUNE_1,
                    AwakeningSystemClient.ACTIVATE_RUNE_2
            };
            String[] defaultRuneKeys = {"N", "M"};

            for (int r = 0; r < 2; r++) {
                int rx = (runeStartIndex + r) * (slotW + gap);
                ItemStack runeStack = data.getActiveRune(r);
                boolean hasRune = !runeStack.isEmpty();
                int cd = hasRune ? data.getActiveRuneCooldownSeconds(r, gameTime) : 0;

                int borderColor = hasRune ? (cd > 0 ? 0xFF883333 : 0xFF9D4EDD) : 0x80554477;
                int bgColor = hasRune ? (cd > 0 ? 0xD0200808 : 0xD0170A24) : 0x60120B1C;
                graphics.fill(rx, 0, rx + slotW, slotH, borderColor);
                graphics.fill(rx + 1, 1, rx + slotW - 1, slotH - 1, bgColor);

                String keyLabel = defaultRuneKeys[r];
                if (runeMappings[r] != null && !runeMappings[r].isUnbound()) {
                    String k = runeMappings[r].getTranslatedKeyMessage().getString().toUpperCase(java.util.Locale.ROOT);
                    if (!k.isEmpty()) {
                        keyLabel = k.length() > 2 ? k.substring(0, 2) : k;
                    }
                }

                if (hasRune) {
                    graphics.renderItem(runeStack, rx + 2, 2);
                    if (cd > 0) {
                        graphics.fill(rx + 1, 1, rx + slotW - 1, slotH - 1, 0x88000000);
                        String cdStr = cd >= 60 ? (cd / 60) + "m" : cd + "s";
                        graphics.pose().pushPose();
                        graphics.pose().translate(0, 0, 200.0F);
                        graphics.drawString(mc.font, cdStr, rx + (slotW - mc.font.width(cdStr)) / 2, 6, 0xFFFF5555, false);
                        graphics.pose().popPose();
                    }
                } else {
                    int symW = mc.font.width("◇");
                    graphics.drawString(mc.font, "◇", rx + (slotW - symW) / 2, 6, 0x669D4EDD, false);
                }

                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 200.0F);
                graphics.drawString(mc.font, keyLabel, rx + 2, 1, hasRune ? 0xFF00E5FF : 0xFF777777, false);
                graphics.pose().popPose();

                if (data.isSystemUpdating() || data.isSystem30Updating()) {
                    renderLockOverlay(graphics, rx, 0, slotW, slotH);
                }
            }
        }

        graphics.pose().popPose();
    }
}

