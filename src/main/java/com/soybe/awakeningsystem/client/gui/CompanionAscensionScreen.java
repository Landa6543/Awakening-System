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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.soybe.awakeningsystem.AwakeningSystemClient;
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
    private int activeTab = 0;

    private static final int W = 410;
    private static final int H = 250;

    public CompanionAscensionScreen(CompoundTag tag) {
        super(Component.translatable("awakening_system.screen.companion_ascension.title"));
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
    protected void init() {
        super.init();
        if (!companions.isEmpty() && selectedIndex >= 0 && selectedIndex < companions.size()) {
            CompanionEntry comp = companions.get(selectedIndex);
            com.soybe.awakeningsystem.entity.CompanionProfile prof = com.soybe.awakeningsystem.entity.CompanionProfile.getById(comp.profileId);
            if (prof != null) {
                AwakeningSystemClient.playUiSound(prof.getAmbientSound(), 1.0F, 1.0F);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (W + 20.0F);
        float scaleY = (float) this.height / (H + 20.0F);
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(transformMouseX(mouseX), transformMouseY(mouseY), button, dragX, dragY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x60000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            g.pose().translate(cxF, cyF, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cxF, -cyF, 0.0F);
        }

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        g.fill(left, top, left + W, top + H, 0xF50D1117);
        g.renderOutline(left, top, W, H, 0xFF2A3950);
        g.renderOutline(left + 1, top + 1, W - 2, H - 2, 0xFFE5B842);

        g.fill(left + 2, top + 2, left + W - 2, top + 24, 0x90161F2E);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.header"), left + 10, top + 8, 0xFFFFFF);
        Component moraComp = Component.translatable("awakening_system.screen.companion_ascension.mora_info", String.format("%,d", playerMora), playerLevel);
        g.drawString(font, moraComp, left + W - font.width(moraComp) - 10, top + 8, 0xFFFFFF);

        if (companions.isEmpty()) {
            int boxW = 340;
            int boxH = 140;
            int bx = left + (W - boxW) / 2;
            int by = top + 52;
            g.fill(bx, by, bx + boxW, by + boxH, 0x80141C2B);
            g.renderOutline(bx, by, boxW, boxH, 0xFF31435C);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.empty_title"), bx + boxW / 2, by + 18, 0xFFFFFF);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.empty_desc"), bx + boxW / 2, by + 38, 0xDDDDDD);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.empty_instructions"), bx + boxW / 2, by + 56, 0xAAAAAA);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.empty_step1"), bx + boxW / 2, by + 74, 0xFFFFFF);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.empty_step2"), bx + boxW / 2, by + 90, 0xFFFFFF);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.empty_hint"), bx + boxW / 2, by + 112, 0x888888);
            super.render(g, mX, mY, partialTick);
            g.pose().popPose();
            return;
        }

        CompanionEntry comp = companions.get(selectedIndex);

        int panelLeft = left + 10;
        int panelTop = top + 30;
        int panelW = 115;
        int panelH = H - 40;
        g.fill(panelLeft, panelTop, panelLeft + panelW, panelTop + panelH, 0x60141C2B);
        g.renderOutline(panelLeft, panelTop, panelW, panelH, 0xFF31435C);

        if (companions.size() > 1) {
            boolean hoverPrev = mX >= panelLeft + 4 && mX < panelLeft + 18 && mY >= panelTop + 4 && mY < panelTop + 18;
            g.fill(panelLeft + 4, panelTop + 4, panelLeft + 18, panelTop + 18, hoverPrev ? 0xFF354B6B : 0xFF1E2838);
            g.drawCenteredString(font, "§e<", panelLeft + 11, panelTop + 6, 0xFFFFFF);

            boolean hoverNext = mX >= panelLeft + panelW - 18 && mX < panelLeft + panelW - 4 && mY >= panelTop + 4 && mY < panelTop + 18;
            g.fill(panelLeft + panelW - 18, panelTop + 4, panelLeft + panelW - 4, panelTop + 18, hoverNext ? 0xFF354B6B : 0xFF1E2838);
            g.drawCenteredString(font, "§e>", panelLeft + panelW - 11, panelTop + 6, 0xFFFFFF);

            g.drawCenteredString(font, "§7" + (selectedIndex + 1) + "/" + companions.size(), panelLeft + panelW / 2, panelTop + 6, 0xAAAAAA);
        }

        String starsStr = "★".repeat(comp.stars);
        g.drawCenteredString(font, comp.colorCode + "§l" + comp.displayName, panelLeft + panelW / 2, panelTop + 17, 0xFFFFFF);
        g.drawCenteredString(font, "§e" + starsStr, panelLeft + panelW / 2, panelTop + 27, 0xFFFFFF);
        String elemStr = comp.element;
        if (font.width(elemStr) > 100 && elemStr.contains("/")) {
            elemStr = elemStr.split("/")[0].trim();
        }
        g.drawCenteredString(font, "§b" + elemStr, panelLeft + panelW / 2, panelTop + 37, 0xFFFFFF);

        int boxX = panelLeft + 10;
        int boxY = panelTop + 49;
        int boxW = panelW - 20;
        int boxH = 76;
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
                    32, 0.0625F, (float) mX, (float) mY, entityToRender
            );
        } else {
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.contract_label"), boxX + boxW / 2, boxY + 34, 0x888888);
        }

        int statY = boxY + boxH + 6;
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.level_stat", comp.level, playerLevel), panelLeft + 6, statY, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.phase_stat", comp.ascensionRank), panelLeft + 6, statY + 11, 0xFFFFFF);
        String constelStr = comp.constellation > 6 ? "§bC" + comp.constellation + " §d(C∞)" : "§dC" + comp.constellation;
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.constel_stat", constelStr), panelLeft + 6, statY + 22, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.health_stat", (int) comp.maxHealth), panelLeft + 6, statY + 33, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.attack_stat", String.format("%.1f", comp.attackDamage)), panelLeft + 6, statY + 44, 0xFFFFFF);

        int tabStartX = left + 132;
        int tabY = top + 30;
        Component[] tabs = {
                Component.translatable("awakening_system.screen.companion_ascension.tab_level"),
                Component.translatable("awakening_system.screen.companion_ascension.tab_talents"),
                Component.translatable("awakening_system.screen.companion_ascension.tab_constellations")
        };
        int[] tabWidths = {88, 72, 98};
        int curTabX = tabStartX;
        for (int i = 0; i < 3; i++) {
            int tw = tabWidths[i];
            boolean isSel = (activeTab == i);
            boolean hoverTab = mX >= curTabX && mX < curTabX + tw && mY >= tabY && mY < tabY + 18;
            g.fill(curTabX, tabY, curTabX + tw, tabY + 18, isSel ? 0xFF2A3D59 : (hoverTab ? 0xFF1E2B3E : 0xFF141D2B));
            g.renderOutline(curTabX, tabY, tw, 18, isSel ? 0xFFFFD700 : 0xFF2B3A4D);
            g.drawCenteredString(font, tabs[i], curTabX + tw / 2, tabY + 5, 0xFFFFFF);
            curTabX += tw + 3;
        }

        int contentX = left + 132;
        int contentY = top + 52;
        int contentW = W - 142;
        int contentH = H - 62;
        g.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0x60121924);
        g.renderOutline(contentX, contentY, contentW, contentH, 0xFF2E3E54);

        switch (activeTab) {
            case 0 -> renderTabLevelAndAscension(g, comp, contentX, contentY, contentW, contentH, mX, mY);
            case 1 -> renderTabTalents(g, comp, contentX, contentY, contentW, contentH, mX, mY);
            case 2 -> renderTabConstellations(g, comp, contentX, contentY, contentW, contentH, mX, mY);
        }

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private void renderTabLevelAndAscension(GuiGraphics g, CompanionEntry comp, int x, int y, int w, int h, int mouseX, int mouseY) {
        int rankCap = CompanionAscensionManager.getRankMaxLevel(comp.ascensionRank);
        boolean atRankCap = comp.level >= rankCap;
        boolean atPlayerCap = comp.level >= playerLevel;

        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.progression_title"), x + 10, y + 10, 0xFFFFFF);

        int barX = x + 10;
        int barY = y + 26;
        int barW = w - 20;
        g.fill(barX, barY, barX + barW, barY + 12, 0xFF101721);
        g.renderOutline(barX, barY, barW, 12, 0xFF354860);

        float progress = Math.min(1.0F, (float) comp.level / Math.max(1, rankCap));
        int fillW = (int) (barW * progress);
        g.fill(barX + 1, barY + 1, barX + fillW - 1, barY + 11, 0xFF00C853);

        MutableComponent barText = Component.literal("§f").append(Component.translatable("awakening_system.screen.companion_ascension.level_bar", comp.level, rankCap));
        if (atRankCap) {
            barText.append(Component.literal(" §c")).append(Component.translatable("awakening_system.screen.companion_ascension.rank_cap_suffix"));
        }
        g.drawCenteredString(font, barText, barX + barW / 2, barY + 2, 0xFFFFFF);

        int expNeeded = 100 + (comp.level - 1) * 50;
        int moraNeeded = comp.level * 1000;

        int infoY = y + 38;
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.hunter_limit", playerLevel), x + 10, infoY, 0xCCCCCC);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.cost_exp", String.format("%,d", moraNeeded), expNeeded), x + 10, infoY + 11, 0xCCCCCC);

        if (atPlayerCap) {
            g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.at_player_cap"), x + 10, infoY + 23, 0xFFFF77);
            g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.raise_hunter_level"), x + 10, infoY + 34, 0x888888);
        } else if (atRankCap) {
            g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.at_rank_cap"), x + 10, infoY + 23, 0xFFA500);
        }

        int btnW = 118;
        int btnH = 20;
        int btnY = y + 88;

        boolean canLvl = !atRankCap && !atPlayerCap;
        int btn1X = x + 10;
        boolean hover1 = mouseX >= btn1X && mouseX < btn1X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btn1X, btnY, btn1X + btnW, btnY + btnH, canLvl ? (hover1 ? 0xFF2A5038 : 0xFF1C3626) : 0xFF1A1A1A);
        g.renderOutline(btn1X, btnY, btnW, btnH, canLvl ? (hover1 ? 0xFF00FF7F : 0xFF2ECC71) : 0xFF444444);
        MutableComponent btn1Text = Component.literal(canLvl ? "§a" : "§8").append(Component.translatable("awakening_system.screen.companion_ascension.btn_level_up"));
        g.drawCenteredString(font, btn1Text, btn1X + btnW / 2, btnY + 6, 0xFFFFFF);

        int btn2X = x + 136;
        boolean hover2 = mouseX >= btn2X && mouseX < btn2X + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btn2X, btnY, btn2X + btnW, btnY + btnH, canLvl ? (hover2 ? 0xFF2F4568 : 0xFF1D2C42) : 0xFF1A1A1A);
        g.renderOutline(btn2X, btnY, btnW, btnH, canLvl ? (hover2 ? 0xFF64B5F6 : 0xFF2196F3) : 0xFF444444);
        MutableComponent btn2Text = Component.literal(canLvl ? "§b" : "§8").append(Component.translatable("awakening_system.screen.companion_ascension.btn_fast_level_up"));
        g.drawCenteredString(font, btn2Text, btn2X + btnW / 2, btnY + 6, 0xFFFFFF);

        int ascBoxY = y + 118;
        g.fill(x + 10, ascBoxY, x + w - 10, ascBoxY + 64, 0x40182433);
        g.renderOutline(x + 10, ascBoxY, w - 20, 64, atRankCap ? 0xFFFFD700 : 0xFF2A3A4E);

        int nextRank = comp.ascensionRank + 1;
        int nextCap = CompanionAscensionManager.getRankMaxLevel(nextRank);
        com.soybe.awakeningsystem.entity.CompanionProfile prof = com.soybe.awakeningsystem.entity.CompanionProfile.getById(comp.profileId);
        CompanionAscensionManager.AscensionRequirement req = CompanionAscensionManager.getAscensionRequirement(prof, comp.ascensionRank);

        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.phase_rank", comp.ascensionRank, nextRank), x + 16, ascBoxY + 6, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.phase_benefit", nextCap), x + 16, ascBoxY + 18, 0xCCCCCC);
        String bossSuffix = req.bossDropCount() > 0 ? " " + Component.translatable("awakening_system.screen.companion_ascension.boss_cost_suffix", req.bossDropCount()).getString() : "";
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.ascension_cost", String.format("%,d", req.moraCost()), req.gemCount(), req.commonCount(), bossSuffix), x + 16, ascBoxY + 29, 0xCCCCCC);

        int ascBtnW = 210;
        int ascBtnH = 18;
        int ascBtnX = x + (w - ascBtnW) / 2;
        int ascBtnY = ascBoxY + 41;
        boolean hoverAsc = mouseX >= ascBtnX && mouseX < ascBtnX + ascBtnW && mouseY >= ascBtnY && mouseY < ascBtnY + ascBtnH;
        g.fill(ascBtnX, ascBtnY, ascBtnX + ascBtnW, ascBtnY + ascBtnH, atRankCap ? (hoverAsc ? 0xFF8A5A10 : 0xFF5C3B07) : 0xFF1C1C1C);
        g.renderOutline(ascBtnX, ascBtnY, ascBtnW, ascBtnH, atRankCap ? 0xFFFFD700 : 0xFF444444);
        Component ascText = atRankCap ? Component.translatable("awakening_system.screen.companion_ascension.btn_ascend") : Component.translatable("awakening_system.screen.companion_ascension.btn_ascend_locked", rankCap);
        g.drawCenteredString(font, ascText, ascBtnX + ascBtnW / 2, ascBtnY + 5, 0xFFFFFF);

        if ((hoverAsc || (mouseX >= x + 10 && mouseX < x + w - 10 && mouseY >= ascBoxY && mouseY < ascBoxY + 40)) && minecraft != null && minecraft.player != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("awakening_system.screen.companion_ascension.tip_req_header", comp.ascensionRank, nextRank));
            boolean hasMora = playerMora >= req.moraCost();
            tip.add(Component.literal(hasMora ? "§a✔ " : "§c✖ ").append(Component.translatable("awakening_system.screen.companion_ascension.tip_mora", String.format("%,d", req.moraCost()), String.format("%,d", playerMora))));

            int curGem = CompanionAscensionManager.countItem(minecraft.player, req.primaryGem());
            boolean hasGem = curGem >= req.gemCount();
            tip.add(Component.literal(hasGem ? "§a✔ " : "§c✖ ").append(Component.translatable("awakening_system.screen.companion_ascension.tip_gem", req.gemCount(), req.primaryGem().getName(ItemStack.EMPTY).getString(), curGem, req.gemCount())));

            if (req.bossDropCount() > 0 && !req.bossDrops().isEmpty()) {
                int curBoss = CompanionAscensionManager.countAnyItem(minecraft.player, req.bossDrops());
                boolean hasBoss = curBoss >= req.bossDropCount();
                String bossName = req.bossDrops().get(0).getName(ItemStack.EMPTY).getString();
                tip.add(Component.literal(hasBoss ? "§a✔ " : "§c✖ ").append(Component.translatable("awakening_system.screen.companion_ascension.tip_boss", req.bossDropCount(), bossName, curBoss, req.bossDropCount())));
            }

            int curCommon = CompanionAscensionManager.countAnyItem(minecraft.player, req.commonMaterials());
            boolean hasCommon = curCommon >= req.commonCount();
            String commonName = req.commonMaterials().get(0).getName(ItemStack.EMPTY).getString();
            tip.add(Component.literal(hasCommon ? "§a✔ " : "§c✖ ").append(Component.translatable("awakening_system.screen.companion_ascension.tip_common", req.commonCount(), commonName, curCommon, req.commonCount())));

            if (!atRankCap) {
                tip.add(Component.literal(""));
                tip.add(Component.translatable("awakening_system.screen.companion_ascension.tip_reach_lvl", rankCap));
            } else {
                tip.add(Component.literal(""));
                tip.add(Component.translatable("awakening_system.screen.companion_ascension.tip_click_ascend", nextCap));
            }
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    private void renderTabTalents(GuiGraphics g, CompanionEntry comp, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.talents_title"), x + 10, y + 10, 0xFFFFFF);
        int maxTalent = 1 + comp.ascensionRank * 2;
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.talent_limit", maxTalent), x + 10, y + 22, 0xAAAAAA);

        Component[] tNames = {
                Component.translatable("awakening_system.screen.companion_ascension.talent_normal_attack"),
                Component.translatable("awakening_system.screen.companion_ascension.talent_skill"),
                Component.translatable("awakening_system.screen.companion_ascension.talent_burst")
        };
        int[] tLevels = {comp.talentAttack, comp.talentSkill, comp.talentBurst};
        Component[] tDesc = {
                Component.translatable("awakening_system.screen.companion_ascension.talent_normal_attack_desc"),
                Component.translatable("awakening_system.screen.companion_ascension.talent_skill_desc"),
                Component.translatable("awakening_system.screen.companion_ascension.talent_burst_desc")
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

            MutableComponent nameWithLv = Component.literal("§f").append(tNames[i]).append(Component.literal(" §e[Nv. " + tLevels[i] + "]"));
            g.drawString(font, nameWithLv, x + 16, cy + 6, 0xFFFFFF);
            g.drawString(font, tDesc[i], x + 16, cy + 18, 0x88BBDD);
            g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.cost_mora", String.format("%,d", tCosts[i])), x + 16, cy + 29, 0xAAAAAA);

            int bx = x + 10 + cardW - btnW - 6;
            int by = cy + 12;
            boolean canUp = tLevels[i] < maxTalent;
            boolean hover = mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH;
            g.fill(bx, by, bx + btnW, by + btnH, canUp ? (hover ? 0xFF2E4B6E : 0xFF1D3047) : 0xFF1A1A1A);
            g.renderOutline(bx, by, btnW, btnH, canUp ? (hover ? 0xFF64B5F6 : 0xFF2196F3) : 0xFF444444);
            Component btnTxt = canUp ? Component.translatable("awakening_system.screen.companion_ascension.btn_upgrade") : Component.translatable("awakening_system.screen.companion_ascension.btn_max");
            g.drawCenteredString(font, btnTxt, bx + btnW / 2, by + 6, 0xFFFFFF);
        }
    }

    private void renderTabConstellations(GuiGraphics g, CompanionEntry comp, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.constel_title"), x + 10, y + 8, 0xFFFFFF);

        Component[] constelNames = {
                Component.translatable("awakening_system.screen.companion_ascension.constel_1"),
                Component.translatable("awakening_system.screen.companion_ascension.constel_2"),
                Component.translatable("awakening_system.screen.companion_ascension.constel_3"),
                Component.translatable("awakening_system.screen.companion_ascension.constel_4"),
                Component.translatable("awakening_system.screen.companion_ascension.constel_5"),
                Component.translatable("awakening_system.screen.companion_ascension.constel_6")
        };

        int listY = y + 20;
        for (int i = 0; i < 6; i++) {
            boolean unlocked = comp.constellation > i;
            int cy = listY + i * 14;
            String prefix = unlocked ? "§a✔ " : "§8○ ";
            String color = unlocked ? "§e" : "§7";
            MutableComponent line = Component.literal(prefix + color).append(constelNames[i]);
            g.drawString(font, line, x + 12, cy, 0xFFFFFF);
        }

        int infY = y + 106;
        int infH = 30;
        g.fill(x + 10, infY, x + w - 10, infY + infH, 0x40251633);
        g.renderOutline(x + 10, infY, w - 20, infH, comp.constellation >= 6 ? 0xFFAB47BC : 0xFF2D2036);

        int over = Math.max(0, comp.constellation - 6);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.transcendence_title", over), x + 14, infY + 4, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.companion_ascension.transcendence_bonus", (over * 5), (over * 50), (over * 3)), x + 14, infY + 16, 0xCCCCCC);

        int nextC = comp.constellation + 1;
        int nextCostMora = 30000 + (comp.constellation > 6 ? (comp.constellation - 6) * 10000 : 0);

        int btnW = 230;
        int btnH = 20;
        int btnX = x + (w - btnW) / 2;
        int btnY = y + 141;

        boolean hoverC = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverC ? 0xFF581D73 : 0xFF3D1252);
        g.renderOutline(btnX, btnY, btnW, btnH, hoverC ? 0xFFE040FB : 0xFFAB47BC);

        Component cBtnText = nextC <= 6 ?
                Component.translatable("awakening_system.screen.companion_ascension.btn_activate_c", nextC) :
                Component.translatable("awakening_system.screen.companion_ascension.btn_transcend_c", nextC);
        g.drawCenteredString(font, cBtnText, btnX + btnW / 2, btnY + 6, 0xFFFFFF);

        g.drawCenteredString(font, Component.translatable("awakening_system.screen.companion_ascension.constel_cost", String.format("%,d", nextCostMora), comp.displayName), btnX + btnW / 2, btnY + 23, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0 || companions.isEmpty()) return super.mouseClicked(mouseX, mouseY, button);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        CompanionEntry comp = companions.get(selectedIndex);

        int panelLeft = left + 10;
        int panelTop = top + 30;
        int panelW = 115;
        if (companions.size() > 1) {
            if (mouseX >= panelLeft + 4 && mouseX < panelLeft + 18 && mouseY >= panelTop + 4 && mouseY < panelTop + 18) {
                selectedIndex = (selectedIndex - 1 + companions.size()) % companions.size();
                AwakeningSystemClient.playButtonClick();
                CompanionEntry compNow = companions.get(selectedIndex);
                com.soybe.awakeningsystem.entity.CompanionProfile prof = com.soybe.awakeningsystem.entity.CompanionProfile.getById(compNow.profileId);
                if (prof != null) {
                    AwakeningSystemClient.playUiSound(prof.getAmbientSound(), 1.0F, 1.0F);
                }
                return true;
            }
            if (mouseX >= panelLeft + panelW - 18 && mouseX < panelLeft + panelW - 4 && mouseY >= panelTop + 4 && mouseY < panelTop + 18) {
                selectedIndex = (selectedIndex + 1) % companions.size();
                AwakeningSystemClient.playButtonClick();
                CompanionEntry compNow = companions.get(selectedIndex);
                com.soybe.awakeningsystem.entity.CompanionProfile prof = com.soybe.awakeningsystem.entity.CompanionProfile.getById(compNow.profileId);
                if (prof != null) {
                    AwakeningSystemClient.playUiSound(prof.getAmbientSound(), 1.0F, 1.0F);
                }
                return true;
            }
        }

        int tabStartX = left + 132;
        int tabY = top + 30;
        int[] tabWidths = {88, 72, 98};
        int curTabX = tabStartX;
        for (int i = 0; i < 3; i++) {
            int tw = tabWidths[i];
            if (mouseX >= curTabX && mouseX < curTabX + tw && mouseY >= tabY && mouseY < tabY + 18) {
                activeTab = i;
                AwakeningSystemClient.playButtonClick();
                return true;
            }
            curTabX += tw + 3;
        }

        int contentX = left + 132;
        int contentY = top + 52;
        int contentW = W - 142;

        switch (activeTab) {
            case 0 -> {
                int rankCap = CompanionAscensionManager.getRankMaxLevel(comp.ascensionRank);
                boolean atRankCap = comp.level >= rankCap;
                boolean atPlayerCap = comp.level >= playerLevel;

                int btnW = 118;
                int btnH = 20;
                int btnY = contentY + 88;

                int btn1X = contentX + 10;
                if (!atRankCap && !atPlayerCap && mouseX >= btn1X && mouseX < btn1X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 0, 0));
                    AwakeningSystemClient.playButtonClick();
                    return true;
                }

                int btn2X = contentX + 136;
                if (!atRankCap && !atPlayerCap && mouseX >= btn2X && mouseX < btn2X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 1, 0));
                    AwakeningSystemClient.playButtonClick();
                    return true;
                }

                int ascBoxY = contentY + 118;
                int ascBtnW = 210;
                int ascBtnH = 18;
                int ascBtnX = contentX + (contentW - ascBtnW) / 2;
                int ascBtnY = ascBoxY + 41;
                if (atRankCap && mouseX >= ascBtnX && mouseX < ascBtnX + ascBtnW && mouseY >= ascBtnY && mouseY < ascBtnY + ascBtnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 2, 0));
                    AwakeningSystemClient.playButtonClick();
                    AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                    return true;
                }
            }
            case 1 -> {
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
                        AwakeningSystemClient.playButtonClick();
                        AwakeningSystemClient.playUiSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.2F);
                        return true;
                    }
                }
            }
            case 2 -> {
                int btnW = 230;
                int btnH = 20;
                int btnX = contentX + (contentW - btnW) / 2;
                int btnY = contentY + 141;

                if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    PacketDistributor.sendToServer(new CompanionAscensionActionPayload(comp.entityId, comp.contractSlot, 6, 0));
                    AwakeningSystemClient.playButtonClick();
                    AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.9F, 1.2F);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
