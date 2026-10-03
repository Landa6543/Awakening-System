package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.entity.CompanionAscensionManager;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.network.CompanionAscensionActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CompanionAscensionScreen extends Screen {
    private int playerLevel;
    private long playerMora;
    private int playerExp;

    private static class CompanionEntry {
        int entityId;
        int contractSlot;
        String profileId;
        String displayName;
        String colorCode;
        String element;
        int stars;
        int level;
        int ascensionRank;
        int constellation;
        int talentAttack;
        int talentSkill;
        int talentBurst;
        float health;
        float maxHealth;
        double attackDamage;
    }

    private final List<CompanionEntry> companions = new ArrayList<>();
    private final java.util.Map<com.soybe.awakeningsystem.entity.CompanionProfile, CompanionEntity> dummyCompanions = new java.util.EnumMap<>(com.soybe.awakeningsystem.entity.CompanionProfile.class);
    private int selectedIndex = 0;
    private int activeTab = 0; // 0: Nivel y Ascensión, 1: Talentos, 2: Constelaciones Infinitas

    private static final int W = 410;
    private static final int H = 250;

    public CompanionAscensionScreen(CompoundTag tag) {
        super(Component.literal("Mesa de Ascensión Celestial"));
        this.updateData(tag);
    }

    public void updateData(CompoundTag tag) {
        this.playerLevel = tag.getInt("PlayerLevel");
        this.playerMora = tag.getLong("PlayerMora");
        this.playerExp = tag.getInt("PlayerExp");
        int prefIndex = tag.contains("SelectedIndex") ? tag.getInt("SelectedIndex") : this.selectedIndex;

        this.companions.clear();
        if (tag.contains("Companions")) {
            ListTag list = tag.getList("Companions", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                CompanionEntry e = new CompanionEntry();
                e.entityId = c.getInt("EntityId");
                e.contractSlot = c.getInt("ContractSlot");
                e.profileId = c.getString("ProfileId");
                e.displayName = c.getString("DisplayName");
                e.colorCode = c.getString("ColorCode");
                e.element = c.getString("Element");
                e.stars = Math.max(1, c.getInt("Stars"));
                e.level = Math.max(1, c.getInt("Level"));
                e.ascensionRank = Math.max(0, c.getInt("AscensionRank"));
                e.constellation = Math.max(0, c.getInt("Constellation"));
                e.talentAttack = Math.max(1, c.getInt("TalentAttack"));
                e.talentSkill = Math.max(1, c.getInt("TalentSkill"));
                e.talentBurst = Math.max(1, c.getInt("TalentBurst"));
                e.health = c.getFloat("Health");
                e.maxHealth = Math.max(1.0F, c.getFloat("MaxHealth"));
                e.attackDamage = c.getDouble("AttackDamage");
                this.companions.add(e);
            }
        }
        if (prefIndex >= 0 && prefIndex < this.companions.size()) {
            this.selectedIndex = prefIndex;
        } else if (this.selectedIndex >= this.companions.size()) {
            this.selectedIndex = Math.max(0, this.companions.size() - 1);
        }
    }

    private CompanionEntity getOrCreateDummy(com.soybe.awakeningsystem.entity.CompanionProfile profile) {
        if (minecraft == null || minecraft.level == null || profile == null) return null;
        return dummyCompanions.computeIfAbsent(profile, p -> {
            CompanionEntity ent = new CompanionEntity(com.soybe.awakeningsystem.entity.ModEntities.COMPANION.get(), minecraft.level);
            ent.setCompanionProfile(p);
            ent.setSilent(true);
            ent.setNoAi(true);
            return ent;
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        // Fondo y marcos dorados celestiales
        g.fill(left, top, left + W, top + H, 0xF50D1117);
        g.renderOutline(left, top, W, H, 0xFF2A3950);
        g.renderOutline(left + 1, top + 1, W - 2, H - 2, 0xFFE5B842); // Borde dorado exterior

        // Barra Superior: Título y Datos del Cazador
        g.fill(left + 2, top + 2, left + W - 2, top + 24, 0x90161F2E);
        g.drawString(font, "§6§l✦ ASCENSIÓN CELESTIAL ✦", left + 10, top + 8, 0xFFFFFF);
        String moraStr = "§6Mora: §e" + String.format("%,d", playerMora) + " §8| §bNv. §f" + playerLevel;
        g.drawString(font, moraStr, left + W - font.width(moraStr) - 10, top + 8, 0xFFFFFF);

        if (companions.isEmpty()) {
            int boxW = 340;
            int boxH = 140;
            int bx = left + (W - boxW) / 2;
            int by = top + 52;
            g.fill(bx, by, bx + boxW, by + boxH, 0x80141C2B);
            g.renderOutline(bx, by, boxW, boxH, 0xFF31435C);
            g.drawCenteredString(font, "§6§l✦ MESA DE ASCENSIÓN CELESTIAL DE COMPAÑEROS ✦", bx + boxW / 2, by + 18, 0xFFFFFF);
            g.drawCenteredString(font, "§eNo tienes ningún compañero invocado cerca ni contratos en tu inventario.", bx + boxW / 2, by + 38, 0xDDDDDD);
            g.drawCenteredString(font, "§7Para acceder a las mejoras celestiales de fase, talentos y constelaciones:", bx + boxW / 2, by + 56, 0xAAAAAA);
            g.drawCenteredString(font, "§f1. §aInvoca a tu compañero §fjunto a esta mesa, o", bx + boxW / 2, by + 74, 0xFFFFFF);
            g.drawCenteredString(font, "§f2. §dLleva su Contrato Celestial §fen tu inventario de objetos.", bx + boxW / 2, by + 90, 0xFFFFFF);
            g.drawCenteredString(font, "§8(Obtén compañeros en el Menú de Deseos / Gacha Celestial)", bx + boxW / 2, by + 112, 0x888888);
            return;
        }

        CompanionEntry comp = companions.get(selectedIndex);

        // 1. SELECTOR Y RENDER 3D (Panel Izquierdo: ancho 120)
        int panelLeft = left + 10;
        int panelTop = top + 30;
        int panelW = 115;
        int panelH = H - 40;
        g.fill(panelLeft, panelTop, panelLeft + panelW, panelTop + panelH, 0x60141C2B);
        g.renderOutline(panelLeft, panelTop, panelW, panelH, 0xFF31435C);

        // Flechas selectoras si hay más de 1 compañero
        if (companions.size() > 1) {
            boolean hoverPrev = mouseX >= panelLeft + 4 && mouseX < panelLeft + 18 && mouseY >= panelTop + 4 && mouseY < panelTop + 18;
            g.fill(panelLeft + 4, panelTop + 4, panelLeft + 18, panelTop + 18, hoverPrev ? 0xFF354B6B : 0xFF1E2838);
            g.drawCenteredString(font, "§e<", panelLeft + 11, panelTop + 6, 0xFFFFFF);

            boolean hoverNext = mouseX >= panelLeft + panelW - 18 && mouseX < panelLeft + panelW - 4 && mouseY >= panelTop + 4 && mouseY < panelTop + 18;
            g.fill(panelLeft + panelW - 18, panelTop + 4, panelLeft + panelW - 4, panelTop + 18, hoverNext ? 0xFF354B6B : 0xFF1E2838);
            g.drawCenteredString(font, "§e>", panelLeft + panelW - 11, panelTop + 6, 0xFFFFFF);

            g.drawCenteredString(font, "§7" + (selectedIndex + 1) + "/" + companions.size(), panelLeft + panelW / 2, panelTop + 6, 0xAAAAAA);
        }

        // Nombre y Estrellas
        String starsStr = "★".repeat(comp.stars);
        g.drawCenteredString(font, comp.colorCode + "§l" + comp.displayName, panelLeft + panelW / 2, panelTop + 22, 0xFFFFFF);
        g.drawCenteredString(font, "§e" + starsStr + " §8| §b" + comp.element, panelLeft + panelW / 2, panelTop + 33, 0xFFFFFF);

        // Caja de render 3D
        int boxX = panelLeft + 10;
        int boxY = panelTop + 45;
        int boxW = panelW - 20;
        int boxH = 80;
        g.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x40000000);
        g.renderOutline(boxX, boxY, boxW, boxH, 0xFF2A3950);

        LivingEntity entityToRender = null;
        if (comp.entityId >= 0 && minecraft != null && minecraft.level != null) {
            Entity ent = minecraft.level.getEntity(comp.entityId);
            if (ent instanceof LivingEntity living) {
                entityToRender = living;
            }
        }
        if (entityToRender == null) {
            com.soybe.awakeningsystem.entity.CompanionProfile prof = com.soybe.awakeningsystem.entity.CompanionProfile.getById(comp.profileId);
            entityToRender = getOrCreateDummy(prof);
        }

        if (entityToRender != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    boxX, boxY, boxX + boxW, boxY + boxH,
                    32, 0.0625F, (float) mouseX, (float) mouseY, entityToRender
            );
        } else {
            g.drawCenteredString(font, "§7[Contrato]", boxX + boxW / 2, boxY + 34, 0x888888);
        }

        // Mini estadísticas del compañero
        int statY = boxY + boxH + 6;
        g.drawString(font, "§7Nivel: §e" + comp.level + " §8/ §6" + playerLevel, panelLeft + 6, statY, 0xFFFFFF);
        g.drawString(font, "§7Fase: §b" + comp.ascensionRank, panelLeft + 6, statY + 11, 0xFFFFFF);
        String constelStr = comp.constellation > 6 ? "§bC" + comp.constellation + " §d(C∞)" : "§dC" + comp.constellation;
        g.drawString(font, "§7Constel.: " + constelStr, panelLeft + 6, statY + 22, 0xFFFFFF);
        g.drawString(font, "§7Vida: §a" + (int) comp.maxHealth, panelLeft + 6, statY + 33, 0xFFFFFF);
        g.drawString(font, "§7Ataque: §c" + String.format("%.1f", comp.attackDamage), panelLeft + 6, statY + 44, 0xFFFFFF);

        // 2. PESTAÑAS SUPERIORES DERECHAS
        int tabStartX = left + 132;
        int tabY = top + 30;
        String[] tabs = {"§e✦ Nivel y Fase", "§b⚔ Talentos", "§d✧ Constelaciones"};
        int[] tabWidths = {88, 72, 98}; // Anchos adaptados milimétricamente (264px <= 268px)
        int curTabX = tabStartX;
        for (int i = 0; i < 3; i++) {
            int tw = tabWidths[i];
            boolean isSel = (activeTab == i);
            boolean hoverTab = mouseX >= curTabX && mouseX < curTabX + tw && mouseY >= tabY && mouseY < tabY + 18;
            g.fill(curTabX, tabY, curTabX + tw, tabY + 18, isSel ? 0xFF2A3D59 : (hoverTab ? 0xFF1E2B3E : 0xFF141D2B));
            g.renderOutline(curTabX, tabY, tw, 18, isSel ? 0xFFFFD700 : 0xFF2B3A4D);
            g.drawCenteredString(font, tabs[i], curTabX + tw / 2, tabY + 5, 0xFFFFFF);
            curTabX += tw + 3;
        }

        // Panel de Contenido Derecho
        int contentX = left + 132;
        int contentY = top + 52;
        int contentW = W - 142; // 268px de ancho útil
        int contentH = H - 62;
        g.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0x60121924);
        g.renderOutline(contentX, contentY, contentW, contentH, 0xFF2E3E54);

        switch (activeTab) {
            case 0 -> renderTabLevelAndAscension(g, comp, contentX, contentY, contentW, contentH, mouseX, mouseY);
            case 1 -> renderTabTalents(g, comp, contentX, contentY, contentW, contentH, mouseX, mouseY);
            case 2 -> renderTabConstellations(g, comp, contentX, contentY, contentW, contentH, mouseX, mouseY);
        }
    }

    private void renderTabLevelAndAscension(GuiGraphics g, CompanionEntry comp, int x, int y, int w, int h, int mouseX, int mouseY) {
        int rankCap = CompanionAscensionManager.getRankMaxLevel(comp.ascensionRank);
        boolean atRankCap = comp.level >= rankCap;
        boolean atPlayerCap = comp.level >= playerLevel;

        g.drawString(font, "§6§l✦ PROGRESIÓN DE NIVEL Y FASE", x + 10, y + 10, 0xFFFFFF);

        // Barra de Nivel
        int barX = x + 10;
        int barY = y + 26;
        int barW = w - 20;
        g.fill(barX, barY, barX + barW, barY + 12, 0xFF101721);
        g.renderOutline(barX, barY, barW, 12, 0xFF354860);

        float progress = Math.min(1.0F, (float) comp.level / Math.max(1, rankCap));
        int fillW = (int) (barW * progress);
        g.fill(barX + 1, barY + 1, barX + fillW - 1, barY + 11, 0xFF00C853);

        String barText = "§fNv. " + comp.level + " / " + rankCap + (atRankCap ? " §c(Límite de Fase)" : "");
        g.drawCenteredString(font, barText, barX + barW / 2, barY + 2, 0xFFFFFF);

        // Costes de Nivel (Formateado en líneas limpias sin desbordes)
        int expNeeded = 100 + (comp.level - 1) * 50;
        int moraNeeded = comp.level * 1000;

        int infoY = y + 44;
        g.drawString(font, "§7Límite Cazador: §eNv. " + playerLevel + " §8(Nivel del Jugador)", x + 10, infoY, 0xCCCCCC);
        g.drawString(font, "§7Coste: §6" + String.format("%,d", moraNeeded) + " Mora §7+ §b1x Ingenio §8(§a" + expNeeded + " EXP§8)", x + 10, infoY + 13, 0xCCCCCC);

        if (atPlayerCap) {
            g.drawString(font, "§e✦ Compañero en tu nivel actual de cazador.", x + 10, infoY + 28, 0xFFFF77);
            g.drawString(font, "§7¡Sube tu nivel de cazador para continuar!", x + 10, infoY + 39, 0x888888);
        } else if (atRankCap) {
            g.drawString(font, "§6✦ ¡Límite alcanzado! Asciende de fase abajo.", x + 10, infoY + 28, 0xFFA500);
        }

        // Botones de Nivel
        int btnW = 118;
        int btnH = 20;
        int btnY = y + 88;

        // Botón Subir +1
        boolean canLvl = !atRankCap && !atPlayerCap;
        int btn1X = x + 10;
        boolean hover1 = mouseX >= btn1X && mouseX < btn1X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btn1X, btnY, btn1X + btnW, btnY + btnH, canLvl ? (hover1 ? 0xFF2A5038 : 0xFF1C3626) : 0xFF1A1A1A);
        g.renderOutline(btn1X, btnY, btnW, btnH, canLvl ? (hover1 ? 0xFF00FF7F : 0xFF2ECC71) : 0xFF444444);
        g.drawCenteredString(font, canLvl ? "§a✦ Subir Nivel (+1)" : "§8✦ Subir Nivel (+1)", btn1X + btnW / 2, btnY + 6, 0xFFFFFF);

        // Botón Subida Rápida (+10)
        int btn2X = x + 136;
        boolean hover2 = mouseX >= btn2X && mouseX < btn2X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btn2X, btnY, btn2X + btnW, btnY + btnH, canLvl ? (hover2 ? 0xFF2F4568 : 0xFF1D2C42) : 0xFF1A1A1A);
        g.renderOutline(btn2X, btnY, btnW, btnH, canLvl ? (hover2 ? 0xFF64B5F6 : 0xFF2196F3) : 0xFF444444);
        g.drawCenteredString(font, canLvl ? "§b✦ Subida Rápida (+10)" : "§8✦ Subida Rápida (+10)", btn2X + btnW / 2, btnY + 6, 0xFFFFFF);

        // Sección de Ascensión de Rango
        int ascBoxY = y + 118;
        g.fill(x + 10, ascBoxY, x + w - 10, ascBoxY + 64, 0x40182433);
        g.renderOutline(x + 10, ascBoxY, w - 20, 64, atRankCap ? 0xFFFFD700 : 0xFF2A3A4E);

        int nextRank = comp.ascensionRank + 1;
        int nextCap = CompanionAscensionManager.getRankMaxLevel(nextRank);
        int ascCostMora = nextRank * 15000;

        g.drawString(font, "§6§l✦ RANGO DE FASE (Fase " + comp.ascensionRank + " ➔ " + nextRank + ")", x + 16, ascBoxY + 6, 0xFFFFFF);
        g.drawString(font, "§7Beneficio: §a+15% Vida y Daño §8| §7Límite: §eNv. " + nextCap, x + 16, ascBoxY + 18, 0xCCCCCC);
        g.drawString(font, "§7Coste: §6" + String.format("%,d", ascCostMora) + " Mora §7+ §d1x Polvo §8(§b20 Proto§8)", x + 16, ascBoxY + 29, 0xCCCCCC);

        int ascBtnW = 210;
        int ascBtnH = 18;
        int ascBtnX = x + (w - ascBtnW) / 2;
        int ascBtnY = ascBoxY + 41;
        boolean hoverAsc = mouseX >= ascBtnX && mouseX < ascBtnX + ascBtnW && mouseY >= ascBtnY && mouseY < ascBtnY + ascBtnH;
        g.fill(ascBtnX, ascBtnY, ascBtnX + ascBtnW, ascBtnY + ascBtnH, atRankCap ? (hoverAsc ? 0xFF8A5A10 : 0xFF5C3B07) : 0xFF1C1C1C);
        g.renderOutline(ascBtnX, ascBtnY, ascBtnW, ascBtnH, atRankCap ? 0xFFFFD700 : 0xFF444444);
        g.drawCenteredString(font, atRankCap ? "§6§l✦ ASCENDER DE FASE ✦" : "§8✦ Bloqueada (Alcanza Nv. " + rankCap + ") ✦", ascBtnX + ascBtnW / 2, ascBtnY + 5, 0xFFFFFF);
    }

    private void renderTabTalents(GuiGraphics g, CompanionEntry comp, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.drawString(font, "§b§l⚔ TALENTOS DE COMBATE", x + 10, y + 10, 0xFFFFFF);
        int maxTalent = 1 + comp.ascensionRank * 2;
        g.drawString(font, "§7Límite actual: §eNv. " + maxTalent + " §8(Aumenta al subir Fase)", x + 10, y + 22, 0xAAAAAA);

        String[] tNames = {"⚔ Ataque Normal", "⚡ Habilidad Elemental", "💥 Ráfaga Definitiva"};
        int[] tLevels = {comp.talentAttack, comp.talentSkill, comp.talentBurst};
        String[] tDesc = {
                "+3.5 Daño Físico y combos.",
                "+12% Daño elemental.",
                "+18% Daño masivo de ráfaga."
        };
        int[] tCosts = {comp.talentAttack * 8000, comp.talentSkill * 8000, comp.talentBurst * 12000};

        int cardW = w - 20;
        int cardH = 44;
        int startY = y + 36;
        int btnW = 58;
        int btnH = 20;

        for (int i = 0; i < 3; i++) {
            int cy = startY + i * 48;
            g.fill(x + 10, cy, x + 10 + cardW, cy + cardH, 0x5016202E);
            g.renderOutline(x + 10, cy, cardW, cardH, 0xFF2A3E56);

            g.drawString(font, "§f" + tNames[i] + " §e[Nv. " + tLevels[i] + "]", x + 16, cy + 6, 0xFFFFFF);
            g.drawString(font, "§7" + tDesc[i], x + 16, cy + 18, 0x88BBDD);
            g.drawString(font, "§7Coste: §6" + String.format("%,d", tCosts[i]) + " Mora", x + 16, cy + 29, 0xAAAAAA);

            // Botón Mejorar
            int bx = x + 10 + cardW - btnW - 6;
            int by = cy + 12;
            boolean canUp = tLevels[i] < maxTalent;
            boolean hover = mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH;
            g.fill(bx, by, bx + btnW, by + btnH, canUp ? (hover ? 0xFF2E4B6E : 0xFF1D3047) : 0xFF1A1A1A);
            g.renderOutline(bx, by, btnW, btnH, canUp ? (hover ? 0xFF64B5F6 : 0xFF2196F3) : 0xFF444444);
            g.drawCenteredString(font, canUp ? "§bMejorar" : "§8Máx", bx + btnW / 2, by + 6, 0xFFFFFF);
        }
    }

    private void renderTabConstellations(GuiGraphics g, CompanionEntry comp, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.drawString(font, "§d§l✧ CONSTELACIONES CELESTIALES", x + 10, y + 8, 0xFFFFFF);

        String[] constelNames = {
                "C1: Despertar (+15% Daño Total)",
                "C2: Armonía (Habilidad Potenciada)",
                "C3: Himno (+3 Nv. Definitiva)",
                "C4: Baluarte (+25% Vida Máxima)",
                "C5: Resonancia (+3 Nv. Habilidad)",
                "C6: Apoteosis (Críticos y Poder)"
        };

        int listY = y + 20;
        for (int i = 0; i < 6; i++) {
            boolean unlocked = comp.constellation > i;
            int cy = listY + i * 14;
            String prefix = unlocked ? "§a✔ " : "§8○ ";
            String color = unlocked ? "§e" : "§7";
            g.drawString(font, prefix + color + constelNames[i], x + 12, cy, 0xFFFFFF);
        }

        // Sección Trascendencia Cósmica Infinita (C7... C∞)
        int infY = y + 106;
        int infH = 30;
        g.fill(x + 10, infY, x + w - 10, infY + infH, 0x40251633);
        g.renderOutline(x + 10, infY, w - 20, infH, comp.constellation >= 6 ? 0xFFAB47BC : 0xFF2D2036);

        int over = Math.max(0, comp.constellation - 6);
        g.drawString(font, "§d§l✦ TRASCENDENCIA CÓSMICA §b[C∞ Nivel +" + over + "]", x + 14, infY + 4, 0xFFFFFF);
        g.drawString(font, "§7Bonus: §a+" + (over * 5) + "% Daño §8| §c+" + (over * 50) + " HP §8| §e+" + (over * 3) + "% Crít.", x + 14, infY + 16, 0xCCCCCC);

        // Botón Despertar Constelación
        int nextC = comp.constellation + 1;
        int nextCostMora = 30000 + (comp.constellation > 6 ? (comp.constellation - 6) * 10000 : 0);

        int btnW = 230;
        int btnH = 20;
        int btnX = x + (w - btnW) / 2;
        int btnY = y + 141;

        boolean hoverC = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverC ? 0xFF581D73 : 0xFF3D1252);
        g.renderOutline(btnX, btnY, btnW, btnH, hoverC ? 0xFFE040FB : 0xFFAB47BC);

        String cBtnText = nextC <= 6 ? "§d✦ ACTIVAR CONSTELACIÓN C" + nextC + " ✦" : "§b✦ TRASCENDER A C" + nextC + " (C∞) ✦";
        g.drawCenteredString(font, cBtnText, btnX + btnW / 2, btnY + 6, 0xFFFFFF);

        g.drawCenteredString(font, "§7Coste: §6" + String.format("%,d", nextCostMora) + " Mora §7+ §62x Brillo §8(§b160 Proto§8)", btnX + btnW / 2, btnY + 23, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || companions.isEmpty()) return super.mouseClicked(mouseX, mouseY, button);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        CompanionEntry comp = companions.get(selectedIndex);

        // Selector Anterior / Siguiente
        int panelLeft = left + 10;
        int panelTop = top + 30;
        int panelW = 115;
        if (companions.size() > 1) {
            if (mouseX >= panelLeft + 4 && mouseX < panelLeft + 18 && mouseY >= panelTop + 4 && mouseY < panelTop + 18) {
                selectedIndex = (selectedIndex - 1 + companions.size()) % companions.size();
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
                }
                return true;
            }
            if (mouseX >= panelLeft + panelW - 18 && mouseX < panelLeft + panelW - 4 && mouseY >= panelTop + 4 && mouseY < panelTop + 18) {
                selectedIndex = (selectedIndex + 1) % companions.size();
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
                }
                return true;
            }
        }

        // Pestañas
        int tabStartX = left + 132;
        int tabY = top + 30;
        int[] tabWidths = {88, 72, 98};
        int curTabX = tabStartX;
        for (int i = 0; i < 3; i++) {
            int tw = tabWidths[i];
            if (mouseX >= curTabX && mouseX < curTabX + tw && mouseY >= tabY && mouseY < tabY + 18) {
                activeTab = i;
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
                }
                return true;
            }
            curTabX += tw + 3;
        }

        int contentX = left + 132;
        int contentY = top + 52;
        int contentW = W - 142;

        switch (activeTab) {
            case 0 -> { // Nivel y Ascensión
                int rankCap = CompanionAscensionManager.getRankMaxLevel(comp.ascensionRank);
                boolean atRankCap = comp.level >= rankCap;
                boolean atPlayerCap = comp.level >= playerLevel;

                int btnW = 118;
                int btnH = 20;
                int btnY = contentY + 88;

                // Subir Nivel +1
                int btn1X = contentX + 10;
                if (!atRankCap && !atPlayerCap && mouseX >= btn1X && mouseX < btn1X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 0, 0));
                    return true;
                }

                // Subida Rápida +10
                int btn2X = contentX + 136;
                if (!atRankCap && !atPlayerCap && mouseX >= btn2X && mouseX < btn2X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 1, 0));
                    return true;
                }

                // Ascender Rango
                int ascBoxY = contentY + 118;
                int ascBtnW = 210;
                int ascBtnH = 18;
                int ascBtnX = contentX + (contentW - ascBtnW) / 2;
                int ascBtnY = ascBoxY + 41;
                if (atRankCap && mouseX >= ascBtnX && mouseX < ascBtnX + ascBtnW && mouseY >= ascBtnY && mouseY < ascBtnY + ascBtnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 2, 0));
                    return true;
                }
            }
            case 1 -> { // Talentos
                int maxTalent = 1 + comp.ascensionRank * 2;
                int[] tLevels = {comp.talentAttack, comp.talentSkill, comp.talentBurst};
                int cardW = contentW - 20;
                int btnW = 58;
                int btnH = 20;
                int startY = contentY + 36;

                for (int i = 0; i < 3; i++) {
                    int cy = startY + i * 48;
                    int bx = contentX + 10 + cardW - btnW - 6;
                    int by = cy + 12;
                    if (tLevels[i] < maxTalent && mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH) {
                        PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 3 + i, 0));
                        return true;
                    }
                }
            }
            case 2 -> { // Constelaciones Infinitas
                int btnW = 230;
                int btnH = 20;
                int btnX = contentX + (contentW - btnW) / 2;
                int btnY = contentY + 141;

                if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 6, 0));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
