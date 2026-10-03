package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    // Escala del HUD y comportamiento adaptativo según resolución / GUI Scale
    public static float CUSTOM_HUD_SCALE = 0.0F; // 0.0F = Automático dinámico

    public static float getEffectiveHudScale(int screenW, int screenH) {
        float userFactor = com.soybe.awakeningsystem.client.gui.GuiConfig.hudScale;
        if (CUSTOM_HUD_SCALE > 0.05F) {
            return Math.max(0.4F, Math.min(2.0F, CUSTOM_HUD_SCALE * userFactor));
        }
        // Cálculo adaptativo:
        // Pantallas compactas (427x240 o 512x288): escala suave a 0.72x - 0.85x
        // Pantallas medianas (640x360 a 854x480): escala 0.90x - 1.0x
        // Pantallas grandes o GUI scale baja: hasta 1.15x
        float scaleX = (float) screenW / 620.0F;
        float scaleY = (float) screenH / 350.0F;
        float factor = Math.min(scaleX, scaleY);
        return Math.max(0.4F, Math.min(2.0F, factor * userFactor));
    }

    // Interpolación suave y daño/consumo rezagado (Damage & Resource Lag / Ghost Bar)
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
                || data.murimUnlocked() || data.system30Updated());

        // Actualizar daño y suavizado de vida, maná y qi
        updateBarInterpolation(mc.player, data);

        // 0. Banner de Actualización del Sistema 2.0 / 3.0 (si está en mantenimiento o ascensión)
        if (data.isAnySystemUpdating()) {
            renderSystemUpdatingBanner(mc, graphics, data, width);
        }

        // 1. Tarjeta HUD del Cazador o Modos Compactos de Salud/Maná
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudStatusCard) {
            switch (com.soybe.awakeningsystem.client.gui.GuiConfig.healthDisplayMode) {
                case CARD -> renderHunterStatusCard(mc, graphics, data, width, height, isSys40, isSys30, isSys20, hasCultivation);
                case VANILLA -> renderCompactManaBar(mc, graphics, data, width, height, isSys40, isSys30, isSys20, hasCultivation);
                case COMPACT_BAR -> renderCompactHotbarBars(mc, graphics, data, width, height, isSys40, isSys30, isSys20, hasCultivation);
            }
        }

        // 2. Barra de Habilidades Activas (Abajo adaptativa y protegida contra solapamiento)
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudSkillBar) {
            renderSkillHotbar(mc, graphics, data, width, height, isSys40, isSys30, isSys20);
        }

        // 3. Visor de Análisis Holográfico (Centro Superior al mirar un mob)
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudAppraisal) {
            renderEntityAppraisal(mc, graphics, data, width, isSys40, isSys30, isSys20);
        }

        // 3. Racha de Combate & Modo Trance (Ubicado a la DERECHA, sin solapar)
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showHudCombo) {
            renderCombatCombo(mc, graphics, data, width, height);
        }

        // 4. Temporizador de Escenario Forzado del Dokkaebi (ORV)
        if (data.isDokkaebiScenarioActive()) {
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

        // Salud
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

        // Maná
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

        // Qi
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
            return 0xFFFFD700; // Dorado legendario / Deidad
        }
        if (r.contains("SSS") || r.contains("SS") || r.contains("UR") || r.contains("LR") || r.contains("MR") || r.contains("DEMIGOD")) {
            return 0xFFB388FF; // Púrpura místico
        }
        if (r.contains("S") || r.contains("EX")) {
            return 0xFFFF3366; // Carmesí rango S
        }
        if (r.contains("A")) {
            return 0xFFFF9900; // Ámbar rango A
        }
        if (r.contains("B")) {
            return 0xFF00E5FF; // Azul cian rango B
        }
        if (r.contains("C")) {
            return 0xFF50FA7B; // Verde esmeralda rango C
        }
        if (r.contains("D")) {
            return 0xFF8BE9FD; // Celeste rango D
        }
        return 0xFFB0BEC5; // Gris acero rango E / F
    }

    private static void renderHunterStatusCard(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20, boolean hasCultivation) {
        Player player = mc.player;
        if (player == null) return;

        float scale = getEffectiveHudScale(screenW, screenH);

        int cardW = 160;
        int cardH = hasCultivation ? 86 : 70;

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

        // Marco y cristal translúcido oscuro (Estética Solo Leveling / Manhwa)
        int borderColor = isSys40 ? 0xC0DFB76C : (isSys30 ? 0xB0D4AF37 : (isSys20 ? 0xB09D4EDD : 0xB0276A82));
        int glowColor = isSys40 ? 0x304EE2EC : (isSys30 ? 0x20D4AF37 : (isSys20 ? 0x209D4EDD : 0x2000E5FF));
        int bgColor = isSys40 ? 0xD808111D : (isSys30 ? 0xD0081210 : (isSys20 ? 0xD012081E : 0xD0070F16));

        graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, glowColor);
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, borderColor);
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, bgColor);

        // Esquinas cibernéticas biseladas
        int cornerCol = isSys40 ? 0xFFDFB76C : (isSys30 ? 0xFFFFD700 : (isSys20 ? 0xFFE0AAFF : 0xFF00E5FF));
        graphics.fill(cardX, cardY, cardX + 3, cardY + 1, cornerCol);
        graphics.fill(cardX, cardY, cardX + 1, cardY + 3, cornerCol);
        graphics.fill(cardX + cardW - 3, cardY, cardX + cardW, cardY + 1, cornerCol);
        graphics.fill(cardX + cardW - 1, cardY, cardX + cardW, cardY + 3, cornerCol);
        graphics.fill(cardX, cardY + cardH - 1, cardX + 3, cardY + cardH, cornerCol);
        graphics.fill(cardX, cardY + cardH - 3, cardX + 1, cardY + cardH, cornerCol);
        graphics.fill(cardX + cardW - 3, cardY + cardH - 1, cardX + cardW, cardY + cardH, cornerCol);
        graphics.fill(cardX + cardW - 1, cardY + cardH - 3, cardX + cardW, cardY + cardH, cornerCol);

        int curY = cardY + 5;

        // 1. Cabecera de Perfil: [RANGO] Nv. X Nombre (Estilo insignia limpia)
        String rank = data.rank();
        int rankColor = getRankColor(rank);

        String rankTag = "[" + rank + "]";
        int rankTagW = mc.font.width(rankTag);
        graphics.fill(cardX + 6, curY - 1, cardX + 8 + rankTagW, curY + 9, 0xD0101820);
        graphics.renderOutline(cardX + 6, curY - 1, rankTagW + 2, 10, rankColor);
        graphics.drawString(mc.font, rankTag, cardX + 7, curY, rankColor, false);

        int headX = cardX + 12 + rankTagW;
        String lvlPrefix = Component.translatable("awakening_system.hud.level_prefix").getString();
        graphics.drawString(mc.font, "§6" + lvlPrefix + " " + data.level() + " §f" + player.getScoreboardName(), headX, curY, 0xFFFFFFFF, false);
        curY += 12;

        // Separador sutil
        graphics.fill(cardX + 6, curY, cardX + cardW - 6, curY + 1, isSys40 ? 0x50DFB76C : (isSys30 ? 0x40D4AF37 : (isSys20 ? 0x409D4EDD : 0x40276A82)));
        curY += 4;

        // 2. Barra de Salud (HP) con daño fantasma suave
        float curHp = player.getHealth();
        float maxHp = Math.max(1.0F, player.getMaxHealth());
        float absorp = player.getAbsorptionAmount();

        String hpNumStr = (int) Math.ceil(curHp) + "/" + (int) Math.ceil(maxHp);
        if (absorp > 0) {
            hpNumStr += " §6[+" + (int) Math.ceil(absorp) + "]";
        }
        graphics.drawString(mc.font, "§c❤ HP", cardX + 7, curY, 0xFFFF4D4D, false);
        graphics.drawString(mc.font, hpNumStr, cardX + cardW - 7 - mc.font.width(hpNumStr), curY, 0xFFE0E0E0, false);
        curY += 9;

        int barX = cardX + 7;
        int barW = cardW - 14;
        int hpBarH = 5;

        graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + hpBarH + 1, 0xFF350E14);
        graphics.fill(barX, curY, barX + barW, curY + hpBarH, 0xE0150406);

        // Barra de daño rezagado (naranja/ámbar suave)
        float lagRatio = Math.max(0.0F, Math.min(1.0F, damageLagHealth / maxHp));
        int lagW = (int) (barW * lagRatio);
        if (lagW > 0) {
            graphics.fill(barX, curY, barX + lagW, curY + hpBarH, 0xFFFF9F43);
        }

        // Barra de salud actual (carmesí vibrante con brillo superior)
        float hpRatio = Math.max(0.0F, Math.min(1.0F, curHp / maxHp));
        int curHpW = (int) (barW * hpRatio);
        if (curHpW > 0) {
            graphics.fill(barX, curY, barX + curHpW, curY + hpBarH, 0xFFFF2D55);
            graphics.fill(barX, curY, barX + curHpW, curY + 1, 0xFFFF6B81);
        }

        // Capa de absorción dorada
        if (absorp > 0) {
            float absRatio = Math.max(0.0F, Math.min(1.0F, absorp / maxHp));
            int absW = (int) (barW * absRatio);
            if (absW > 0) {
                graphics.fill(barX, curY + hpBarH - 2, barX + Math.min(barW, absW), curY + hpBarH, 0xFFFFD700);
            }
        }
        curY += 8;

        // 3. Barra de Maná (MP) con barra fantasma suave
        int curMp = data.mana();
        int maxMp = Math.max(1, data.maxMana());
        String mpNumStr = curMp + "/" + maxMp;

        graphics.drawString(mc.font, "§b✦ MP", cardX + 7, curY, 0xFF00E5FF, false);
        graphics.drawString(mc.font, mpNumStr, cardX + cardW - 7 - mc.font.width(mpNumStr), curY, 0xFFB0E0E6, false);
        curY += 9;

        int mpBarH = 5;
        graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + mpBarH + 1, 0xFF0B2538);
        graphics.fill(barX, curY, barX + barW, curY + mpBarH, 0xE005111B);

        // Barra fantasma de consumo de Maná (ámbar/azul según sistema)
        float lagMpRatio = Math.max(0.0F, Math.min(1.0F, damageLagMp / maxMp));
        int lagMpW = (int) (barW * lagMpRatio);
        if (lagMpW > 0) {
            int lagMpFill = isSys20 ? 0x907B3DAA : (isSys30 ? 0x901D7D4B : 0x9000789E);
            graphics.fill(barX, curY, barX + lagMpW, curY + mpBarH, lagMpFill);
        }

        float mpRatio = Math.max(0.0F, Math.min(1.0F, (float) curMp / maxMp));
        int curMpW = (int) (barW * mpRatio);
        if (curMpW > 0) {
            int mpFill = isSys20 ? 0xFFB388FF : (isSys30 ? 0xFF38EF7D : 0xFF00C6FF);
            int mpTop = isSys20 ? 0xFFD1B3FF : (isSys30 ? 0xFF88FFB2 : 0xFF70E8FF);
            graphics.fill(barX, curY, barX + curMpW, curY + mpBarH, mpFill);
            graphics.fill(barX, curY, barX + curMpW, curY + 1, mpTop);
        }
        curY += 8;

        // 4. Barra de Qi con barra fantasma suave (si ha desbloqueado cultivo / Murim)
        if (hasCultivation) {
            int curQi = data.cultivationQi();
            int maxQi = Math.max(1, data.maxCultivationQi());
            String compactRealm = Component.translatable("awakening_system.realm.compact." + data.dantianRealm()).getString();
            String qiNumStr = curQi + "/" + maxQi + " (" + compactRealm + ")";

            graphics.drawString(mc.font, "§e☯ QI", cardX + 7, curY, 0xFFFFD700, false);
            graphics.drawString(mc.font, qiNumStr, cardX + cardW - 7 - mc.font.width(qiNumStr), curY, 0xFFFFF0A0, false);
            curY += 9;

            int qiBarH = 4;
            graphics.fill(barX - 1, curY - 1, barX + barW + 1, curY + qiBarH + 1, 0xFF382A0B);
            graphics.fill(barX, curY, barX + barW, curY + qiBarH, 0xE0181205);

            // Barra fantasma de Qi consumido
            float lagQiRatio = Math.max(0.0F, Math.min(1.0F, damageLagQi / maxQi));
            int lagQiW = (int) (barW * lagQiRatio);
            if (lagQiW > 0) {
                graphics.fill(barX, curY, barX + lagQiW, curY + qiBarH, 0x90B38000);
            }

            float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) curQi / maxQi));
            int curQiW = (int) (barW * qiRatio);
            if (curQiW > 0) {
                graphics.fill(barX, curY, barX + curQiW, curY + qiBarH, 0xFFFFB703);
                graphics.fill(barX, curY, barX + curQiW, curY + 1, 0xFFFFE066);
            }
            curY += 8;
        }

        // 5. Mini Estadísticas compactas (Armadura, Daño, Monedas formateadas y distribuidas uniformemente)
        int armor = player.getArmorValue();
        int attack = (int) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        int coins = data.coins();
        String coinStr = coins >= 1000000 ? String.format(java.util.Locale.US, "%.1fM", coins / 1000000.0) :
                (coins >= 1000 ? String.format(java.util.Locale.US, "%.1fK", coins / 1000.0) : String.valueOf(coins));

        int colW = (cardW - 14) / 3;
        graphics.drawString(mc.font, "§b🛡 " + armor, cardX + 7, curY, 0xFFFFFFFF, false);
        graphics.drawString(mc.font, "§c⚔ " + attack, cardX + 7 + colW, curY, 0xFFFFFFFF, false);
        if (isSys40 && data.primogems() > 0) {
            graphics.drawString(mc.font, "§b✦ " + data.primogems(), cardX + 7 + colW * 2, curY, 0xFFFFFFFF, false);
        } else {
            graphics.drawString(mc.font, "§e🪙 " + coinStr, cardX + 7 + colW * 2, curY, 0xFFFFFFFF, false);
        }

        graphics.pose().popPose();
    }

    public static void renderCompactManaBar(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20, boolean hasCultivation) {
        if (data == null) return;
        int barW = 81;
        int barH = 5;
        // Posición estándar sobre la barra de comida (lado derecho de la hotbar)
        int barX = screenW / 2 + 10;
        int barY = screenH - 49;

        int curMp = data.mana();
        int maxMp = Math.max(1, data.maxMana());
        float mpRatio = Math.max(0.0F, Math.min(1.0F, (float) curMp / maxMp));

        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF0B2538);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xE005111B);

        // Barra fantasma lag
        float lagMpRatio = Math.max(0.0F, Math.min(1.0F, damageLagMp / maxMp));
        int lagMpW = (int) (barW * lagMpRatio);
        if (lagMpW > 0) {
            int lagMpFill = isSys20 ? 0x907B3DAA : (isSys30 ? 0x901D7D4B : 0x9000789E);
            graphics.fill(barX, barY, barX + lagMpW, barY + barH, lagMpFill);
        }

        int filledW = (int) (barW * mpRatio);
        if (filledW > 0) {
            int mpFill = isSys20 ? 0xFFB388FF : (isSys30 ? 0xFF38EF7D : 0xFF00C6FF);
            int mpTop = isSys20 ? 0xFFD1B3FF : (isSys30 ? 0xFF88FFB2 : 0xFF70E8FF);
            graphics.fill(barX, barY, barX + filledW, barY + barH, mpFill);
            graphics.fill(barX, barY, barX + filledW, barY + 1, mpTop);
        }

        String mpText = "✦ " + curMp + "/" + maxMp;
        graphics.drawString(mc.font, mpText, barX + (barW - mc.font.width(mpText)) / 2, barY - 8, 0xFFB0E0E6, true);

        // Si tiene cultivo de Qi, barra dorada arriba
        if (hasCultivation) {
            int qiY = barY - 14;
            int curQi = data.cultivationQi();
            int maxQi = Math.max(1, data.maxCultivationQi());
            float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) curQi / maxQi));

            graphics.fill(barX - 1, qiY - 1, barX + barW + 1, qiY + 4, 0xFF382A0B);
            graphics.fill(barX, qiY, barX + barW, qiY + 3, 0xE0181205);

            int curQiW = (int) (barW * qiRatio);
            if (curQiW > 0) {
                graphics.fill(barX, qiY, barX + curQiW, qiY + 3, 0xFFFFB703);
            }
            String qiText = "☯ " + curQi + "/" + maxQi;
            graphics.drawString(mc.font, qiText, barX + (barW - mc.font.width(qiText)) / 2, qiY - 8, 0xFFFFF0A0, true);
        }
    }

    public static void renderCompactHotbarBars(Minecraft mc, GuiGraphics graphics, SystemData data, int screenW, int screenH, boolean isSys40, boolean isSys30, boolean isSys20, boolean hasCultivation) {
        Player player = mc.player;
        if (player == null || data == null) return;

        int barW = 81;
        int barH = 6;

        // 1. Barra de Salud (HP) sobre el lado izquierdo de la hotbar (donde van los corazones vanilla)
        int hpX = screenW / 2 - 91;
        int hpY = screenH - 39;

        float curHp = player.getHealth();
        float maxHp = Math.max(1.0F, player.getMaxHealth());
        float absorp = player.getAbsorptionAmount();

        graphics.fill(hpX - 1, hpY - 1, hpX + barW + 1, hpY + barH + 1, 0xFF350E14);
        graphics.fill(hpX, hpY, hpX + barW, hpY + barH, 0xE0150406);

        // Daño fantasma
        float lagRatio = Math.max(0.0F, Math.min(1.0F, damageLagHealth / maxHp));
        int lagW = (int) (barW * lagRatio);
        if (lagW > 0) {
            graphics.fill(hpX, hpY, hpX + lagW, hpY + barH, 0xFFFF9F43);
        }

        float hpRatio = Math.max(0.0F, Math.min(1.0F, curHp / maxHp));
        int curHpW = (int) (barW * hpRatio);
        if (curHpW > 0) {
            graphics.fill(hpX, hpY, hpX + curHpW, hpY + barH, 0xFFFF2D55);
            graphics.fill(hpX, hpY, hpX + curHpW, hpY + 1, 0xFFFF6B81);
        }
        if (absorp > 0) {
            float absRatio = Math.max(0.0F, Math.min(1.0F, absorp / maxHp));
            int absW = (int) (barW * absRatio);
            if (absW > 0) {
                graphics.fill(hpX, hpY + barH - 2, hpX + Math.min(barW, absW), hpY + barH, 0xFFFFD700);
            }
        }

        String hpText = "❤ " + (int) Math.ceil(curHp) + "/" + (int) Math.ceil(maxHp);
        if (absorp > 0) hpText += " §6[+" + (int) Math.ceil(absorp) + "]";
        graphics.drawString(mc.font, hpText, hpX + 2, hpY - 8, 0xFFFF85A1, true);

        // Insignia de armadura y nivel sobre HP
        int armor = player.getArmorValue();
        if (armor > 0) {
            String armText = "🛡 " + armor + "  §6Nv." + data.level();
            graphics.drawString(mc.font, armText, hpX + 2, hpY - 16, 0xFFFFFFFF, true);
        }

        // 2. Barra de Maná (MP) sobre la comida (lado derecho)
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

        if (data != null && data.isAnySystemUpdating()) {
            int bannerBottom = (bossBottom > 0 ? (bossBottom + 6) : 8) + 16;
            y = Math.max(y, bannerBottom + 6);
        }

        if (data != null && data.isDokkaebiScenarioActive()) {
            int bannerOffset = data.isAnySystemUpdating() ? 32 : 0;
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
            Component name = living.getName();

            boolean isBoss = !living.getType().getCategory().isFriendly() && (living.getMaxHealth() >= 80.0F
                    || living instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                    || living instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon);
            boolean isFriendly = living.getType().getCategory().isFriendly() || living instanceof net.minecraft.world.entity.animal.Animal || living instanceof net.minecraft.world.entity.npc.Npc;

            long now = System.currentTimeMillis();
            if (lastCardYRenderTime == 0L) lastCardYRenderTime = now;
            float dt = Math.min(0.1F, (now - lastCardYRenderTime) / 1000.0F);
            lastCardYRenderTime = now;

            float targetY = (float) calculateDesiredTargetHudY(mc, data);
            if (isBoss) {
                targetY += 4.0F; // Margen adicional de separación para jefes principales
            }
            if (animatedCardY < 0.0F) {
                animatedCardY = targetY; // Ajuste instantáneo sin atravesar la barra del jefe
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

                int cardW = Math.min(Math.max(160, fullTitleW + fullHpW + 28), width - 16);
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
                int cardW = Math.min(Math.max(minW, neededW), width - 16);
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

                // Nombre del objetivo en la línea superior (espacio completo sin colisión)
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

                // Cifras de vida y porcentaje centradas nítidamente dentro de la barra de salud con sombra
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
            } catch (Throwable ignored) {}
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
            } catch (Throwable ignored) {}
        }
        if (count > 0) {
            // Cada BossBar vanilla ocupa 19px a partir de y=12, más margen seguro de 14px
            return 12 + Math.min(count, 4) * 19 + 14;
        }
        if (mc.level != null) {
            String path = mc.level.dimension().location().getPath();
            if (path.contains("tower") || path.contains("dungeon") || path.contains("pagoda")) {
                return 48; // Margen garantizado para mazmorras y torres
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
            progress = Component.translatable("awakening_system.hud.dokkaebi_objective", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills());
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
        int slotW = 20;
        int slotH = 19;
        int gap = 3;
        int totalSlots = hasStigma ? 6 : 5;
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
                        || data.murimUnlocked() || data.system30Updated());
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

        // Estigma de Constelación (Slot Y)
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

        graphics.pose().popPose();
    }
}


