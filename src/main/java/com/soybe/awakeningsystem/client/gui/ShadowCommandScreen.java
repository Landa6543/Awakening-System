package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.ShadowRank;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.network.ManageShadowSquadPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ShadowCommandScreen extends Screen {
    private static final int W = 380;
    private static final int H = 240;

    private int selectedSquad = 0;
    private int scrollOffset = 0;
    private EditBox renameBox;
    private boolean isRenaming = false;

    private static final String[] SQUAD_ICONS = {"⚔", "🛡", "🦅", "👑"};
    private static final String[] SQUAD_ROMAN = {"I", "II", "III", "IV"};
    private static final int[] SQUAD_COLORS = {0xFF5588FF, 0xFFE67E22, 0xFF00E5FF, 0xFFFFD700};

    public ShadowCommandScreen() {
        super(Component.translatable("gui.awakening_system.shadow_command.title"));
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;
        int subHeaderY = top + 64;

        this.renameBox = new EditBox(this.font, left + 80, subHeaderY, 150, 16, Component.literal(""));
        this.renameBox.setMaxLength(22);
        this.renameBox.setVisible(false);
        this.addWidget(this.renameBox);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isRenaming) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveSquadRename();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                isRenaming = false;
                renameBox.setVisible(false);
                return true;
            }
            if (renameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveSquadRename() {
        String newName = renameBox.getValue().trim();
        if (!newName.isEmpty()) {
            PacketDistributor.sendToServer(new ManageShadowSquadPayload(
                    ManageShadowSquadPayload.ACTION_RENAME_SQUAD,
                    selectedSquad,
                    0,
                    newName
            ));
            SystemData d = getSystemData();
            if (d != null) {
                d.setShadowSquadName(selectedSquad, newName);
            }
        }
        isRenaming = false;
        renameBox.setVisible(false);
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
    }

    private SystemData getSystemData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return SystemEvents.data(mc.player);
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;
        int listY = top + 88;
        int listH = 108;
        if (mouseX >= left + 14 && mouseX <= left + W - 14 && mouseY >= listY && mouseY <= listY + listH) {
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                scrollOffset++;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x70000000);
    }

    private record ShadowEntry(int globalIndex, CompoundTag tag) {}

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

        ModernGuiHelper.drawModernWindow(g, left, top, W, H, ModernGuiHelper.GuiTheme.SOLO_LEVELING);
        ModernGuiHelper.drawModernHeader(g, left + 4, top + 4, W - 8, 22, null, 0, ModernGuiHelper.GuiTheme.SOLO_LEVELING, font);

        // Header Title
        String title = "§b⚔ §l" + Component.translatable("gui.awakening_system.shadow_command.title").getString() + " §8| §3" + Component.translatable("gui.awakening_system.shadow_command.subtitle").getString();
        g.drawString(font, title, left + 14, top + 10, 0xFFFFFF);

        SystemData data = getSystemData();
        int storedTotal = data != null ? data.storedShadowCount() : 0;
        int maxCapacity = data != null ? data.getMaxShadowCapacity() : 100;

        // Count active deployed shadows
        int activeDeployedTotal = 0;
        int activeInSquad = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            String masterTag = "shadow_master_" + mc.player.getUUID();
            List<Mob> activeMobs = mc.level.getEntitiesOfClass(
                    Mob.class,
                    mc.player.getBoundingBox().inflate(128.0),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(masterTag)
            );
            activeDeployedTotal = activeMobs.size();
            for (Mob m : activeMobs) {
                if (m.getPersistentData().getInt("shadow_squad_id") == selectedSquad) {
                    activeInSquad++;
                }
            }
        }

        // Top Summary Bar
        int barY = top + 26;
        g.fill(left + 14, barY, left + W - 14, barY + 14, 0x6009131F);
        g.renderOutline(left + 14, barY, W - 28, 14, 0xFF1C344D);

        String storedStr = "§7" + Component.translatable("gui.awakening_system.shadow_command.stored").getString() + ": §b" + storedTotal + "§8/§3" + maxCapacity;
        String deployedStr = "§7" + Component.translatable("gui.awakening_system.shadow_command.active").getString() + ": §a" + activeDeployedTotal;
        g.drawString(font, storedStr, left + 20, barY + 3, 0xFFFFFF);
        int deployedW = font.width(deployedStr);
        g.drawString(font, deployedStr, left + W - deployedW - 20, barY + 3, 0xFFFFFF);

        // 5 Filter Tabs: [ ALL ] [ SQ-I ] [ SQ-II ] [ SQ-III ] [ SQ-IV ]
        int tabY = top + 42;
        int tabSpacing = 3;
        int tabW = (W - 28 - (tabSpacing * 4)) / 5;
        for (int i = -1; i < 4; i++) {
            int tabIndex = i + 1;
            int tx = left + 14 + tabIndex * (tabW + tabSpacing);
            boolean isSel = (i == selectedSquad);
            boolean hov = mX >= tx && mX < tx + tabW && mY >= tabY && mY < tabY + 18;

            int tabBg = isSel ? 0xFF1B324D : (hov ? 0xFF142436 : 0xFF0E1724);
            int tabBorder = isSel ? (i == -1 ? 0xFF00E5FF : SQUAD_COLORS[i]) : (hov ? 0xFF355E8A : 0xFF1C3048);
            g.fill(tx, tabY, tx + tabW, tabY + 18, tabBg);
            g.renderOutline(tx, tabY, tabW, 18, tabBorder);

            String tabLabel;
            if (i == -1) {
                tabLabel = Component.translatable("gui.awakening_system.shadow_command.tab_all").getString() + " (" + storedTotal + ")";
            } else {
                int sqCount = data != null ? data.getShadowCountInSquad(i) : 0;
                tabLabel = Component.translatable("gui.awakening_system.shadow_command.tab_squad").getString() + SQUAD_ROMAN[i] + " (" + sqCount + ")";
            }
            g.drawCenteredString(font, Component.literal(tabLabel), tx + tabW / 2, tabY + 5, isSel ? 0xFFFFFF : (hov ? 0xDDDDDD : 0x8899AA));
        }

        // Active Squad Subheader
        int subHeaderY = top + 64;
        String inBattleStr = Component.translatable("gui.awakening_system.shadow_command.in_field").getString();
        if (selectedSquad == -1) {
            String allTitle = Component.translatable("gui.awakening_system.shadow_command.all_shadows").getString();
            String subHeaderStr = "§b✦ " + allTitle + " §8[§a" + activeDeployedTotal + " " + inBattleStr + "§8]";
            g.drawString(font, subHeaderStr, left + 14, subHeaderY + 4, 0xFFFFFF);
        } else if (!isRenaming) {
            String currentSquadName = data != null ? data.getShadowSquadName(selectedSquad) : ("Squad " + (selectedSquad + 1));
            String subHeaderStr = "§e" + SQUAD_ICONS[selectedSquad] + " " + SQUAD_ROMAN[selectedSquad] + ". §f" + currentSquadName + " §8[§a" + activeInSquad + " " + inBattleStr + "§8]";
            int maxTextW = W - 110;
            if (font.width(subHeaderStr) > maxTextW) {
                subHeaderStr = font.plainSubstrByWidth(subHeaderStr, maxTextW - 8) + "…";
            }
            g.drawString(font, subHeaderStr, left + 14, subHeaderY + 4, 0xFFFFFF);

            // Rename Button
            int renameBtnX = left + W - 92;
            int renameBtnW = 78;
            int renameBtnH = 15;
            boolean hovRename = mX >= renameBtnX && mX < renameBtnX + renameBtnW && mY >= subHeaderY && mY < subHeaderY + renameBtnH;
            g.fill(renameBtnX, subHeaderY, renameBtnX + renameBtnW, subHeaderY + renameBtnH, hovRename ? 0xFF283A52 : 0xFF162232);
            g.renderOutline(renameBtnX, subHeaderY, renameBtnW, renameBtnH, hovRename ? 0xFF00E5FF : 0xFF2A4260);
            g.drawCenteredString(font, "§e✏ " + Component.translatable("gui.awakening_system.shadow_command.rename").getString(), renameBtnX + renameBtnW / 2, subHeaderY + 3, 0xFFFFFF);
        } else {
            // Renaming Mode: clean input row without overlapping text
            g.drawString(font, "§e✏ " + Component.translatable("gui.awakening_system.shadow_command.rename").getString() + ":", left + 14, subHeaderY + 4, 0xFFD700);
            renameBox.render(g, mX, mY, partialTick);

            // Save Button
            int saveBtnX = left + 236;
            int saveBtnW = 60;
            int saveBtnH = 16;
            boolean hovSave = mX >= saveBtnX && mX < saveBtnX + saveBtnW && mY >= subHeaderY && mY < subHeaderY + saveBtnH;
            g.fill(saveBtnX, subHeaderY, saveBtnX + saveBtnW, subHeaderY + saveBtnH, hovSave ? 0xFF1A5235 : 0xFF103622);
            g.renderOutline(saveBtnX, subHeaderY, saveBtnW, saveBtnH, hovSave ? 0xFF2ECC71 : 0xFF1E7541);
            g.drawCenteredString(font, "§a✔ " + Component.translatable("gui.awakening_system.shadow_command.save").getString(), saveBtnX + saveBtnW / 2, subHeaderY + 4, 0xFFFFFF);

            // Cancel Button
            int cancelBtnX = left + 300;
            int cancelBtnW = 66;
            int cancelBtnH = 16;
            boolean hovCancel = mX >= cancelBtnX && mX < cancelBtnX + cancelBtnW && mY >= subHeaderY && mY < subHeaderY + cancelBtnH;
            g.fill(cancelBtnX, subHeaderY, cancelBtnX + cancelBtnW, subHeaderY + cancelBtnH, hovCancel ? 0xFF522222 : 0xFF361414);
            g.renderOutline(cancelBtnX, subHeaderY, cancelBtnW, cancelBtnH, hovCancel ? 0xFFE74C3C : 0xFF8A2B2B);
            g.drawCenteredString(font, "§c✖ " + Component.translatable("gui.awakening_system.shadow_command.cancel").getString(), cancelBtnX + cancelBtnW / 2, subHeaderY + 4, 0xFFFFFF);
        }

        // Shadow Cards List
        int listX = left + 14;
        int listY = top + 83;
        int listW = W - 28;
        int listH = 111;

        g.fill(listX, listY, listX + listW, listY + listH, 0x40070D14);
        g.renderOutline(listX, listY, listW, listH, 0xFF142436);

        List<ShadowEntry> squadShadows = new ArrayList<>();
        if (data != null) {
            List<CompoundTag> army = data.getStoredShadowArmy();
            for (int idx = 0; idx < army.size(); idx++) {
                CompoundTag tag = army.get(idx);
                int sq = tag.contains("SquadId") ? tag.getInt("SquadId") : 0;
                if (selectedSquad == -1 || sq == selectedSquad) {
                    squadShadows.add(new ShadowEntry(idx, tag));
                }
            }
        }

        int visibleItems = 4;
        int itemH = 26;
        int maxScroll = Math.max(0, squadShadows.size() - visibleItems);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        if (squadShadows.isEmpty()) {
            String emptyHint = Component.translatable("gui.awakening_system.shadow_command.no_shadows").getString();
            g.drawCenteredString(font, emptyHint, listX + listW / 2, listY + 36, 0x667788);
            String emptySub = Component.translatable("gui.awakening_system.shadow_command.no_shadows_sub").getString();
            g.drawCenteredString(font, emptySub, listX + listW / 2, listY + 52, 0x445566);
        } else {
            for (int i = 0; i < visibleItems; i++) {
                int itemIdx = scrollOffset + i;
                if (itemIdx >= squadShadows.size()) break;

                ShadowEntry entry = squadShadows.get(itemIdx);
                CompoundTag sTag = entry.tag();
                int iy = listY + 2 + i * itemH;

                boolean hovCard = mX >= listX + 2 && mX < listX + listW - 2 && mY >= iy && mY < iy + (itemH - 2);
                g.fill(listX + 2, iy, listX + listW - 2, iy + itemH - 2, hovCard ? 0xFF162538 : 0xFF0D1724);
                g.renderOutline(listX + 2, iy, listW - 4, itemH - 2, hovCard ? 0xFF356291 : 0xFF1B2F46);

                // Rank badge
                int rankVal = sTag.contains("ShadowRank") ? sTag.getInt("ShadowRank") : 0;
                ShadowRank rank = ShadowRank.byLevel(rankVal);
                String rankStr = "[" + Component.translatable(rank.getLangKey()).getString() + "]";
                int rankW = font.width(rankStr);
                g.drawString(font, rankStr, listX + 6, iy + 4, rank.getColor());

                // Name & Level - dynamic X without overlap!
                int nameX = listX + 6 + rankW + 5;
                String mobName = sTag.contains("ShadowBaseName") ? sTag.getString("ShadowBaseName") : (sTag.contains("MobName") ? sTag.getString("MobName") : "Shadow");
                int mobLvl = sTag.getInt("RpgMobLevel");
                String levelStr = mobLvl > 0 ? "§e[Lv. " + mobLvl + "] " : "";
                String fullMobTitle = levelStr + "§f" + mobName;

                // 4 Custom Squad Pills on right: [ I ] [ II ] [ III ] [ IV ]
                int squadBtnBaseX = listX + listW - 96;
                int maxNameW = Math.max(20, squadBtnBaseX - nameX - 6);
                String displayTitle = font.plainSubstrByWidth(fullMobTitle, maxNameW);
                g.drawString(font, displayTitle, nameX, iy + 4, 0xFFFFFF);

                // Kills & HP line below
                int kills = sTag.getInt("ShadowKills");
                float hp = sTag.getFloat("Health");
                float maxHp = sTag.getFloat("MaxHealth");
                if (maxHp <= 0) maxHp = 20.0F;
                int hpPercent = Math.min(100, Math.max(0, (int) ((hp / maxHp) * 100)));
                String statsStr = "§7HP: §a" + hpPercent + "% §8| §7" + Component.translatable("gui.awakening_system.shadow_command.kills").getString() + ": §c" + kills;
                g.drawString(font, statsStr, listX + 6, iy + 14, 0xAAAAAA);

                // 4 Custom Squad Pills: [ I ] [ II ] [ III ] [ IV ]
                int currentMobSquad = sTag.contains("SquadId") ? sTag.getInt("SquadId") : 0;
                for (int sqTarget = 0; sqTarget < 4; sqTarget++) {
                    int sqX = squadBtnBaseX + sqTarget * 22;
                    int sqY = iy + 4;
                    int sqW = 20;
                    int sqH = 15;
                    boolean isCurrent = (sqTarget == currentMobSquad);
                    boolean hovSq = mX >= sqX && mX < sqX + sqW && mY >= sqY && mY < sqY + sqH;

                    int pillBg = isCurrent ? 0xFF003D5B : (hovSq ? 0xFF1C2D40 : 0xFF0E1724);
                    int pillBorder = isCurrent ? 0xFF00E5FF : (hovSq ? 0xFF3D6B94 : 0xFF1A2B3D);
                    g.fill(sqX, sqY, sqX + sqW, sqY + sqH, pillBg);
                    g.renderOutline(sqX, sqY, sqW, sqH, pillBorder);

                    int textCol = isCurrent ? 0xFF00FFFF : (hovSq ? 0xFFFFFFFF : 0xFF7A8B9E);
                    g.drawCenteredString(font, SQUAD_ROMAN[sqTarget], sqX + sqW / 2, sqY + 4, textCol);
                }
            }

            // Scrollbar
            if (maxScroll > 0) {
                int scrollTrackH = listH - 6;
                int thumbH = Math.max(12, scrollTrackH / (maxScroll + 1));
                int thumbY = listY + 3 + (int) (((float) scrollOffset / maxScroll) * (scrollTrackH - thumbH));
                g.fill(listX + listW - 5, listY + 3, listX + listW - 2, listY + listH - 3, 0xFF101B29);
                g.fill(listX + listW - 5, thumbY, listX + listW - 2, thumbY + thumbH, 0xFF00E5FF);
            }
        }

        // Bottom Tactical Command Buttons
        int btnY = top + 198;
        int btnW = 86;
        int btnH = 20;

        // Button 1: Deploy Squad
        int b1X = left + 14;
        boolean hovB1 = mX >= b1X && mX < b1X + btnW && mY >= btnY && mY < btnY + btnH;
        g.fill(b1X, btnY, b1X + btnW, btnY + btnH, hovB1 ? 0xFF144D66 : 0xFF0D3244);
        g.renderOutline(b1X, btnY, btnW, btnH, hovB1 ? 0xFF00E5FF : 0xFF2082A6);
        g.drawCenteredString(font, "§b🔥 " + Component.translatable("gui.awakening_system.shadow_command.deploy_squad").getString(), b1X + btnW / 2, btnY + 6, 0xFFFFFF);

        // Button 2: Recall Squad
        int b2X = b1X + btnW + 4;
        boolean hovB2 = mX >= b2X && mX < b2X + btnW && mY >= btnY && mY < btnY + btnH;
        g.fill(b2X, btnY, b2X + btnW, btnY + btnH, hovB2 ? 0xFF4D1C5E : 0xFF31123D);
        g.renderOutline(b2X, btnY, btnW, btnH, hovB2 ? 0xFFBA55D3 : 0xFF7B2D96);
        g.drawCenteredString(font, "§d🌀 " + Component.translatable("gui.awakening_system.shadow_command.recall_squad").getString(), b2X + btnW / 2, btnY + 6, 0xFFFFFF);

        // Button 3: Deploy All
        int b3X = b2X + btnW + 4;
        boolean hovB3 = mX >= b3X && mX < b3X + btnW && mY >= btnY && mY < btnY + btnH;
        g.fill(b3X, btnY, b3X + btnW, btnY + btnH, hovB3 ? 0xFF1A5235 : 0xFF103622);
        g.renderOutline(b3X, btnY, btnW, btnH, hovB3 ? 0xFF2ECC71 : 0xFF1E7541);
        g.drawCenteredString(font, "§a⚡ " + Component.translatable("gui.awakening_system.shadow_command.deploy_all").getString(), b3X + btnW / 2, btnY + 6, 0xFFFFFF);

        // Button 4: Recall All
        int b4X = b3X + btnW + 4;
        boolean hovB4 = mX >= b4X && mX < b4X + btnW && mY >= btnY && mY < btnY + btnH;
        g.fill(b4X, btnY, b4X + btnW, btnY + btnH, hovB4 ? 0xFF522222 : 0xFF361414);
        g.renderOutline(b4X, btnY, btnW, btnH, hovB4 ? 0xFFE74C3C : 0xFF8A2B2B);
        g.drawCenteredString(font, "§c🛡 " + Component.translatable("gui.awakening_system.shadow_command.recall_all").getString(), b4X + btnW / 2, btnY + 6, 0xFFFFFF);

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        if (button == 0) {
            // Check 5 Tabs: [ ALL ] [ SQ-I ] [ SQ-II ] [ SQ-III ] [ SQ-IV ]
            int tabY = top + 42;
            int tabSpacing = 3;
            int tabW = (W - 28 - (tabSpacing * 4)) / 5;
            for (int i = -1; i < 4; i++) {
                int tabIndex = i + 1;
                int tx = left + 14 + tabIndex * (tabW + tabSpacing);
                if (mouseX >= tx && mouseX < tx + tabW && mouseY >= tabY && mouseY < tabY + 18) {
                    selectedSquad = i;
                    scrollOffset = 0;
                    isRenaming = false;
                    renameBox.setVisible(false);
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }
            }

            // Rename button / Save button
            int subHeaderY = top + 64;
            if (selectedSquad >= 0 && !isRenaming) {
                int renameBtnX = left + W - 92;
                int renameBtnW = 78;
                int renameBtnH = 15;
                if (mouseX >= renameBtnX && mouseX < renameBtnX + renameBtnW && mouseY >= subHeaderY && mouseY < subHeaderY + renameBtnH) {
                    isRenaming = true;
                    renameBox.setVisible(true);
                    SystemData d = getSystemData();
                    renameBox.setValue(d != null ? d.getShadowSquadName(selectedSquad) : "");
                    renameBox.setFocused(true);
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
            } else if (isRenaming) {
                int saveBtnX = left + 236;
                int saveBtnW = 60;
                int saveBtnH = 16;
                if (mouseX >= saveBtnX && mouseX < saveBtnX + saveBtnW && mouseY >= subHeaderY && mouseY < subHeaderY + saveBtnH) {
                    saveSquadRename();
                    return true;
                }
                int cancelBtnX = left + 300;
                int cancelBtnW = 66;
                int cancelBtnH = 16;
                if (mouseX >= cancelBtnX && mouseX < cancelBtnX + cancelBtnW && mouseY >= subHeaderY && mouseY < subHeaderY + cancelBtnH) {
                    isRenaming = false;
                    renameBox.setVisible(false);
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.9F);
                    return true;
                }
            }

            // Shadow cards custom squad pills
            int listX = left + 14;
            int listY = top + 83;
            int listW = W - 28;
            SystemData data = getSystemData();
            if (data != null) {
                List<ShadowEntry> squadShadows = new ArrayList<>();
                List<CompoundTag> army = data.getStoredShadowArmy();
                for (int idx = 0; idx < army.size(); idx++) {
                    CompoundTag tag = army.get(idx);
                    int sq = tag.contains("SquadId") ? tag.getInt("SquadId") : 0;
                    if (selectedSquad == -1 || sq == selectedSquad) {
                        squadShadows.add(new ShadowEntry(idx, tag));
                    }
                }

                int visibleItems = 4;
                int itemH = 26;
                for (int i = 0; i < visibleItems; i++) {
                    int itemIdx = scrollOffset + i;
                    if (itemIdx >= squadShadows.size()) break;

                    ShadowEntry entry = squadShadows.get(itemIdx);
                    int iy = listY + 2 + i * itemH;
                    int squadBtnBaseX = listX + listW - 96;

                    for (int sqTarget = 0; sqTarget < 4; sqTarget++) {
                        int sqX = squadBtnBaseX + sqTarget * 22;
                        int sqY = iy + 4;
                        int sqW = 20;
                        int sqH = 15;
                        if (mouseX >= sqX && mouseX < sqX + sqW && mouseY >= sqY && mouseY < sqY + sqH) {
                            int currentSq = entry.tag().contains("SquadId") ? entry.tag().getInt("SquadId") : 0;
                            if (sqTarget != currentSq) {
                                PacketDistributor.sendToServer(new ManageShadowSquadPayload(
                                        ManageShadowSquadPayload.ACTION_MOVE_SHADOW,
                                        sqTarget,
                                        entry.globalIndex(),
                                        ""
                                ));
                                data.setShadowSquad(entry.globalIndex(), sqTarget);
                                entry.tag().putInt("SquadId", sqTarget);
                                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                                return true;
                            }
                        }
                    }
                }
            }

            // Bottom Command Buttons
            int btnY = top + 198;
            int btnW = 86;
            int btnH = 20;

            // Deploy Squad
            int b1X = left + 14;
            if (mouseX >= b1X && mouseX < b1X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                int targetSquad = selectedSquad >= 0 ? selectedSquad : 0;
                PacketDistributor.sendToServer(new ManageShadowSquadPayload(
                        selectedSquad >= 0 ? ManageShadowSquadPayload.ACTION_DEPLOY_SQUAD : ManageShadowSquadPayload.ACTION_DEPLOY_ALL,
                        targetSquad,
                        0,
                        ""
                ));
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.4F);
                return true;
            }

            // Recall Squad
            int b2X = b1X + btnW + 4;
            if (mouseX >= b2X && mouseX < b2X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                int targetSquad = selectedSquad >= 0 ? selectedSquad : 0;
                PacketDistributor.sendToServer(new ManageShadowSquadPayload(
                        selectedSquad >= 0 ? ManageShadowSquadPayload.ACTION_RECALL_SQUAD : ManageShadowSquadPayload.ACTION_RECALL_ALL,
                        targetSquad,
                        0,
                        ""
                ));
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.9F);
                return true;
            }

            // Deploy All
            int b3X = b2X + btnW + 4;
            if (mouseX >= b3X && mouseX < b3X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                PacketDistributor.sendToServer(new ManageShadowSquadPayload(
                        ManageShadowSquadPayload.ACTION_DEPLOY_ALL,
                        0,
                        0,
                        ""
                ));
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.4F);
                return true;
            }

            // Recall All
            int b4X = b3X + btnW + 4;
            if (mouseX >= b4X && mouseX < b4X + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                PacketDistributor.sendToServer(new ManageShadowSquadPayload(
                        ManageShadowSquadPayload.ACTION_RECALL_ALL,
                        0,
                        0,
                        ""
                ));
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.8F);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
