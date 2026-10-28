package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class GuildScreen extends Screen {
    private static final int GUI_WIDTH = 380;
    private static final int GUI_HEIGHT = 260;

    private CompoundTag rawData;
    private boolean inGuild;
    private Guild guild;
    private CompoundTag allClaimsTag;
    private ListTag allGuildsTag;

    private static int lastTab = 0;
    private int currentTab = lastTab;

    private EditBox createNameBox;
    private EditBox createTagBox;

    private EditBox inviteBox;
    private EditBox portalFeeBox;
    private boolean editingPortalFeeModal = false;
    private String editingPortalPos = "";
    private String editingPortalRank = "";

    private int memberScrollOffset = 0;
    private int perkScrollOffset = 0;
    private int allianceScrollOffset = 0;
    private int portalScrollOffset = 0;

    private ListTag guildPortalsTag;
    private CompoundTag nearbyPortalTag;

    public GuildScreen(CompoundTag data) {
        super(Component.translatable("awakening_system.guild.screen_title"));
        this.rawData = data;
        this.inGuild = data.getBoolean("InGuild");
        if (inGuild && data.contains("Guild")) {
            this.guild = Guild.load(data.getCompound("Guild"));
        }
        if (data.contains("AllClaims")) {
            this.allClaimsTag = data.getCompound("AllClaims");
        } else if (AwakeningSystemClient.cachedClientClaims != null) {
            this.allClaimsTag = AwakeningSystemClient.cachedClientClaims;
        } else {
            this.allClaimsTag = new CompoundTag();
        }
        if (data.contains("AllGuilds")) {
            this.allGuildsTag = data.getList("AllGuilds", Tag.TAG_COMPOUND);
        } else {
            this.allGuildsTag = new ListTag();
        }
        if (data.contains("GuildPortals")) {
            this.guildPortalsTag = data.getList("GuildPortals", Tag.TAG_COMPOUND);
        } else {
            this.guildPortalsTag = new ListTag();
        }
        if (data.contains("NearbyPortal")) {
            this.nearbyPortalTag = data.getCompound("NearbyPortal");
        } else {
            this.nearbyPortalTag = null;
        }
        if (data.contains("InitialTab")) {
            this.currentTab = data.getInt("InitialTab");
            lastTab = this.currentTab;
        }
    }

    public void updateData(CompoundTag data) {
        if (data == null) return;
        this.rawData = data;
        this.inGuild = data.getBoolean("InGuild");
        if (inGuild && data.contains("Guild")) {
            this.guild = Guild.load(data.getCompound("Guild"));
        }
        if (data.contains("AllClaims")) {
            this.allClaimsTag = data.getCompound("AllClaims");
        }
        if (data.contains("AllGuilds")) {
            this.allGuildsTag = data.getList("AllGuilds", Tag.TAG_COMPOUND);
        }
        if (data.contains("GuildPortals")) {
            this.guildPortalsTag = data.getList("GuildPortals", Tag.TAG_COMPOUND);
        }
        if (data.contains("NearbyPortal")) {
            this.nearbyPortalTag = data.getCompound("NearbyPortal");
        }
    }

    public static void open(CompoundTag data) {
        Minecraft.getInstance().setScreen(new GuildScreen(data));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        if (!inGuild) {
            this.createNameBox = new EditBox(font, left + 40, top + 100, 180, 18, Component.translatable("awakening_system.guild.name_field"));
            this.createNameBox.setHint(Component.translatable("awakening_system.guild.name_hint"));
            this.createNameBox.setMaxLength(16);
            addRenderableWidget(createNameBox);

            this.createTagBox = new EditBox(font, left + 230, top + 100, 100, 18, Component.translatable("awakening_system.guild.tag_field"));
            this.createTagBox.setHint(Component.translatable("awakening_system.guild.tag_hint"));
            this.createTagBox.setMaxLength(4);
            addRenderableWidget(createTagBox);
        } else {
            this.inviteBox = new EditBox(font, left + 20, top + GUI_HEIGHT - 38, 140, 16, Component.translatable("awakening_system.guild.invite_field"));
            this.inviteBox.setHint(Component.translatable("awakening_system.guild.invite_hint"));
            this.inviteBox.setMaxLength(16);
            this.inviteBox.visible = (currentTab == 0);
            this.inviteBox.active = (currentTab == 0);
            addRenderableWidget(inviteBox);

            this.portalFeeBox = new EditBox(font, left + (GUI_WIDTH - 180) / 2, top + (GUI_HEIGHT - 126) / 2 + 44, 180, 18, Component.translatable("awakening_system.guild.fee_field"));
            this.portalFeeBox.setHint(Component.translatable("awakening_system.guild.fee_hint"));
            this.portalFeeBox.setMaxLength(10);
            this.portalFeeBox.setFilter(s -> s.matches("\\d*"));
            this.portalFeeBox.visible = false;
            this.portalFeeBox.active = false;
            addRenderableWidget(portalFeeBox);
        }
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 400.0F;
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(transformMouseX(mouseX), transformMouseY(mouseY), button, dragX, dragY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

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

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFFDFB76C);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF208111A);
        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF141F2B);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFFDFB76C);

        boolean closeHover = mX >= left + GUI_WIDTH - 22 && mX < left + GUI_WIDTH - 6 && mY >= top + 6 && mY < top + 20;
        g.fill(left + GUI_WIDTH - 22, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 14, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (!inGuild || guild == null) {
            renderCreateGuildView(g, left, top, mX, mY);
        } else {
            renderGuildView(g, left, top, mX, mY);
        }

        super.render(g, mX, mY, partialTick);

        if (editingPortalFeeModal) {
            renderPortalFeeModal(g, left, top, mX, mY);
        }

        g.pose().popPose();
    }

    private void renderCreateGuildView(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("awakening_system.guild.screen.header_found"), left + 14, top + 9, 0xFFFFFFFF);

        g.drawCenteredString(font, Component.translatable("awakening_system.guild.screen.form_title"), left + GUI_WIDTH / 2, top + 40, 0xFFFFD700);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.screen.form_desc1"), left + GUI_WIDTH / 2, top + 53, 0xFFAAAAAA);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.screen.form_desc2"), left + GUI_WIDTH / 2, top + 64, 0xFFAAAAAA);

        g.drawString(font, Component.translatable("awakening_system.guild.screen.name_label"), left + 40, top + 88, 0xFFFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.guild.screen.tag_label"), left + 230, top + 88, 0xFFFFFFFF);

        int reqY = top + 135;
        g.fill(left + 40, reqY, left + GUI_WIDTH - 40, reqY + 50, 0x880E1C28);
        g.renderOutline(left + 40, reqY, GUI_WIDTH - 80, 50, 0xFF2A4255);

        g.drawString(font, Component.translatable("awakening_system.guild.screen.req_title"), left + 48, reqY + 8, 0xFFFFD700);
        g.drawString(font, Component.translatable("awakening_system.guild.screen.req_cost"), left + 48, reqY + 22, 0xFFFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.guild.screen.req_leader"), left + 48, reqY + 34, 0xFFCCCCCC);

        int btnW = 160;
        int btnH = 22;
        int btnX = left + (GUI_WIDTH - btnW) / 2;
        int btnY = top + 200;

        boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHov ? 0xDD28864E : 0xAA1A5D3E);
        g.renderOutline(btnX, btnY, btnW, btnH, btnHov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.screen.btn_found"), btnX + btnW / 2, btnY + 7, btnHov ? 0xFFFFFFFF : 0xFF50FA7B);
    }

    private void renderGuildView(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Component titleComp = Component.literal("§6⚔ [" + guild.getTag() + "] §f" + guild.getName() + " ")
                .append(Component.translatable("awakening_system.guild.level_tag", guild.getLevel()));
        g.drawString(font, titleComp, left + 14, top + 9, 0xFFFFFFFF);

        long exp = guild.getExp();
        long expNeeded = guild.getExpForNextLevel();
        float ratio = Math.max(0.0F, Math.min(1.0F, (float) exp / expNeeded));
        int barW = 120;
        int barH = 8;
        int barX = left + GUI_WIDTH - 150;
        int barY = top + 10;
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF0D1721);
        g.fill(barX, barY, barX + (int) (barW * ratio), barY + barH, 0xFF00E5FF);
        g.renderOutline(barX, barY, barW, barH, 0xFF4EE2EC);

        int tabW = (GUI_WIDTH - 28) / 6;
        Component[] tabs = {
                Component.translatable("awakening_system.guild.tab.members"),
                Component.translatable("awakening_system.guild.tab.blessings"),
                Component.translatable("awakening_system.guild.tab.treasury"),
                Component.translatable("awakening_system.guild.tab.hunts"),
                Component.translatable("awakening_system.guild.tab.portals"),
                Component.translatable("awakening_system.guild.tab.alliances")
        };
        for (int i = 0; i < 6; i++) {
            int tX = left + 14 + i * tabW;
            int tY = top + 32;
            int tH = 18;
            boolean hov = mouseX >= tX && mouseX < tX + tabW && mouseY >= tY && mouseY < tY + tH;
            g.fill(tX, tY, tX + tabW, tY + tH, currentTab == i ? 0xCC2B3D4D : (hov ? 0x881E2B38 : 0x550F1822));
            g.renderOutline(tX, tY, tabW, tH, currentTab == i ? 0xFFDFB76C : 0xFF2A4255);
            g.drawCenteredString(font, tabs[i], tX + tabW / 2, tY + 5, currentTab == i ? 0xFFFFD700 : 0xFFAAAAAA);
        }

        if (this.inviteBox != null) {
            boolean showInvite = inGuild && (currentTab == 0);
            this.inviteBox.visible = showInvite;
            this.inviteBox.active = showInvite;
        }

        int contentY = top + 56;
        switch (currentTab) {
            case 0 -> renderMembersTab(g, left, contentY, mouseX, mouseY);
            case 1 -> renderPerksTab(g, left, contentY, mouseX, mouseY);
            case 2 -> renderTreasuryTab(g, left, contentY, mouseX, mouseY);
            case 3 -> renderHuntingTab(g, left, contentY, mouseX, mouseY);
            case 4 -> renderPortalsTab(g, left, contentY, mouseX, mouseY);
            case 5 -> renderAlliancesTab(g, left, contentY, mouseX, mouseY);
        }
    }

    private void renderMembersTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean isLeader = guild.getLeaderUuid().equals(myUuid);

        g.drawString(font, "§8●", left + 22, top + 2, 0x888888);
        g.drawString(font, Component.translatable("awakening_system.guild.members.rank"), left + 38, top + 2, 0x888888);
        g.drawString(font, Component.translatable("awakening_system.guild.members.name"), left + 110, top + 2, 0x888888);
        g.drawString(font, Component.translatable("awakening_system.guild.members.donated"), left + 220, top + 2, 0x888888);
        if (isLeader) {
            g.drawString(font, Component.translatable("awakening_system.guild.members.actions"), left + 285, top + 2, 0x888888);
        }

        g.fill(left + 16, top + 13, left + GUI_WIDTH - 16, top + 14, 0x334EE2EC);

        List<GuildMember> membersList = new ArrayList<>(guild.getMembers().values());
        int listH = 140;
        int rowH = 18;
        int maxVisible = listH / rowH;

        for (int i = 0; i < maxVisible; i++) {
            int idx = i + memberScrollOffset;
            if (idx >= membersList.size()) break;
            GuildMember m = membersList.get(idx);

            int rY = top + 18 + i * rowH;
            boolean rHov = mouseX >= left + 16 && mouseX < left + GUI_WIDTH - 16 && mouseY >= rY && mouseY < rY + rowH - 2;
            if (rHov) {
                g.fill(left + 16, rY, left + GUI_WIDTH - 16, rY + rowH - 2, 0x224EE2EC);
            }

            boolean isOnline = mc.getConnection() != null && mc.getConnection().getPlayerInfo(m.getUuid()) != null;
            g.drawString(font, isOnline ? "§a●" : "§7○", left + 22, rY + 3, isOnline ? 0xFF50FA7B : 0xFF777777);

            g.drawString(font, m.getRole().getLabel(), left + 38, rY + 3, 0xFFFFFFFF);

            String nameStr = m.getName();
            if (m.getUuid().equals(myUuid)) nameStr += " " + Component.translatable("awakening_system.guild.you").getString();
            g.drawString(font, nameStr, left + 110, rY + 3, 0xFFFFFFFF);

            g.drawString(font, "§e" + m.getContributedMora() + " 🪙", left + 220, rY + 3, 0xFFFFD700);

            if (isLeader && !m.getUuid().equals(myUuid)) {
                boolean hovUp = mouseX >= left + 285 && mouseX < left + 301 && mouseY >= rY && mouseY < rY + 14;
                g.fill(left + 285, rY, left + 301, rY + 14, hovUp ? 0xDD28864E : 0x881A5D3E);
                g.drawCenteredString(font, "▲", left + 293, rY + 2, 0xFF50FA7B);

                boolean hovDown = mouseX >= left + 305 && mouseX < left + 321 && mouseY >= rY && mouseY < rY + 14;
                g.fill(left + 305, rY, left + 321, rY + 14, hovDown ? 0xDDAA6622 : 0x88663311);
                g.drawCenteredString(font, "▼", left + 313, rY + 2, 0xFFFFBB44);

                boolean hovKick = mouseX >= left + 325 && mouseX < left + 341 && mouseY >= rY && mouseY < rY + 14;
                g.fill(left + 325, rY, left + 341, rY + 14, hovKick ? 0xDD992222 : 0x88551111);
                g.drawCenteredString(font, "✕", left + 333, rY + 2, 0xFFFF6666);
            }
        }

        int btmY = top + GUI_HEIGHT - 38 - 56;
        if (myRole != null && myRole.canInvite()) {
            int invBtnX = left + 165;
            int invBtnW = 75;
            int invBtnH = 16;
            boolean invHov = mouseX >= invBtnX && mouseX < invBtnX + invBtnW && mouseY >= btmY && mouseY < btmY + invBtnH;
            g.fill(invBtnX, btmY, invBtnX + invBtnW, btmY + invBtnH, invHov ? 0xDD225577 : 0x88113344);
            g.renderOutline(invBtnX, btmY, invBtnW, invBtnH, invHov ? 0xFF00E5FF : 0xFF2A4255);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.btn_invite"), invBtnX + invBtnW / 2, btmY + 4, 0xFF70E8FF);
        }

        int leaveBtnX = left + GUI_WIDTH - 110;
        int leaveBtnW = 94;
        int leaveBtnH = 16;
        boolean leaveHov = mouseX >= leaveBtnX && mouseX < leaveBtnX + leaveBtnW && mouseY >= btmY && mouseY < btmY + leaveBtnH;
        g.fill(leaveBtnX, btmY, leaveBtnX + leaveBtnW, btmY + leaveBtnH, leaveHov ? 0xAA6E2020 : 0x663B1111);
        g.renderOutline(leaveBtnX, btmY, leaveBtnW, leaveBtnH, leaveHov ? 0xFFFF6666 : 0xFF882222);
        g.drawCenteredString(font, isLeader ? Component.translatable("awakening_system.guild.btn_disband") : Component.translatable("awakening_system.guild.btn_leave"), leaveBtnX + leaveBtnW / 2, btmY + 4, 0xFFFF8888);
    }

    private void renderPerksTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        boolean isLeader = guild.getLeaderUuid().equals(myUuid);

        GuildPerk[] perks = GuildPerk.values();
        int cardW = GUI_WIDTH - 32;
        int cardH = 24;

        for (int i = 0; i < perks.length; i++) {
            GuildPerk p = perks[i];
            int pY = top + i * (cardH + 4);
            if (pY + cardH > top + 190) break;

            boolean unlocked = guild.hasPerk(p);
            boolean canUnlock = isLeader && !unlocked && guild.getLevel() >= p.getRequiredGuildLevel() && guild.getMoraVault() >= p.getCostMora();

            int borderCol = unlocked ? 0xFF50FA7B : (canUnlock ? 0xFFFFD700 : 0xFF3D566E);
            int bgCol = unlocked ? 0x401A5D3E : 0x30101C28;

            g.fill(left + 16, pY, left + 16 + cardW, pY + cardH, bgCol);
            g.renderOutline(left + 16, pY, cardW, cardH, borderCol);

            Component perkTitle = Component.literal(p.getIcon() + " §f").append(p.getTitleComponent());
            g.drawString(font, perkTitle, left + 22, pY + 4, unlocked ? 0xFF50FA7B : 0xFFFFFFFF);
            Component perkDesc = Component.literal("§7").append(p.getDescriptionComponent());
            g.drawString(font, perkDesc, left + 22, pY + 14, 0xFFAAAAAA);

            if (unlocked) {
                g.drawString(font, Component.translatable("awakening_system.guild.perk.active"), left + cardW - 55, pY + 8, 0xFF50FA7B);
            } else {
                if (guild.getLevel() < p.getRequiredGuildLevel()) {
                    g.drawString(font, Component.translatable("awakening_system.guild.perk.req_level", p.getRequiredGuildLevel()), left + cardW - 65, pY + 8, 0xFFFF6666);
                } else if (isLeader) {
                    int btnX = left + cardW - 75;
                    int btnW = 85;
                    int btnH = 16;
                    boolean bHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= pY + 4 && mouseY < pY + 4 + btnH;
                    g.fill(btnX, pY + 4, btnX + btnW, pY + 4 + btnH, bHov ? 0xDD6E5515 : 0xAA453308);
                    g.renderOutline(btnX, pY + 4, btnW, btnH, 0xFFFFD700);
                    g.drawCenteredString(font, "🪙 " + p.getCostMora(), btnX + btnW / 2, pY + 8, 0xFFFFD700);
                } else {
                    g.drawString(font, "§e" + p.getCostMora() + " 🪙", left + cardW - 55, pY + 8, 0xFFFFD700);
                }
            }
        }
    }

    private void drawScaledCenteredString(GuiGraphics g, String text, int centerX, int centerY, int maxWidth, int color) {
        int strW = font.width(text);
        if (strW <= maxWidth) {
            g.drawCenteredString(font, text, centerX, centerY, color);
        } else {
            float scale = (float) maxWidth / (float) strW;
            var pose = g.pose();
            pose.pushPose();
            pose.translate(centerX, centerY + (font.lineHeight * (1.0f - scale)) / 2.0f, 0);
            pose.scale(scale, scale, 1.0f);
            g.drawCenteredString(font, text, 0, 0, color);
            pose.popPose();
        }
    }

    private void renderTreasuryTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean isLeader = guild.getLeaderUuid().equals(myUuid);
        boolean canWithdraw = myRole != null && myRole.canWithdraw();
        boolean canClaim = myRole != null && myRole.canClaimTerritory();

        g.fill(left + 16, top, left + GUI_WIDTH - 16, top + 88, 0x4010202E);
        g.renderOutline(left + 16, top, GUI_WIDTH - 32, 88, 0xFFDFB76C);

        g.drawString(font, Component.translatable("awakening_system.guild.treasury.vault_title"), left + 24, top + 6, 0xFFFFD700);
        g.drawString(font, Component.translatable("awakening_system.guild.treasury.balances", String.format("%,d", guild.getMoraVault()), String.format("%,d", guild.getCoinVault())), left + 24, top + 18, 0xFFFFFFFF);

        int btnW = 23;
        int btnH = 15;
        int gap = 2;
        int depStartX = left + 68;
        int withStartX = depStartX + 5 * (btnW + gap) + 4;
        int allW = 38;
        int allX = withStartX + 5 * (btnW + gap) + 2;

        // --- ROW 1: MORA ---
        g.drawString(font, Component.translatable("awakening_system.guild.treasury.mora_label"), left + 22, top + 34, 0xFFFFD700);
        String[] depMoraLabels = {"+1k", "+10k", "+50k", "+250k", "+1M"};
        for (int i = 0; i < depMoraLabels.length; i++) {
            int bX = depStartX + i * (btnW + gap);
            int bY = top + 31;
            boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
            g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD28864E : 0x881A5D3E);
            g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFF50FA7B : 0xFF28864E);
            drawScaledCenteredString(g, depMoraLabels[i], bX + btnW / 2, bY + 3, btnW - 2, 0xFF50FA7B);
        }

        if (canWithdraw) {
            String[] withMoraLabels = {"-1k", "-10k", "-50k", "-250k", "-1M"};
            for (int i = 0; i < withMoraLabels.length; i++) {
                int bX = withStartX + i * (btnW + gap);
                int bY = top + 31;
                boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
                g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD8B2B2B : 0x885A1A1A);
                g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFFFFB86C : 0xFF8B2B2B);
                drawScaledCenteredString(g, withMoraLabels[i], bX + btnW / 2, bY + 3, btnW - 2, 0xFFFFB86C);
            }

            int allY = top + 31;
            boolean allHov = mouseX >= allX && mouseX < allX + allW && mouseY >= allY && mouseY < allY + btnH;
            boolean hasMora = guild.getMoraVault() > 0;
            g.fill(allX, allY, allX + allW, allY + btnH, hasMora ? (allHov ? 0xDD992222 : 0x88661111) : 0x55333333);
            g.renderOutline(allX, allY, allW, btnH, hasMora ? (allHov ? 0xFFFF5555 : 0xFFAA2222) : 0xFF555555);
            drawScaledCenteredString(g, "§l" + Component.translatable("awakening_system.guild.treasury.btn_withdraw_all").getString(), allX + allW / 2, allY + 3, allW - 2, hasMora ? (allHov ? 0xFFFFFFFF : 0xFFFFD700) : 0xFF777777);
            if (allHov) {
                if (hasMora) {
                    g.renderTooltip(font, Component.translatable("awakening_system.guild.treasury.tooltip_withdraw_all", String.format("%,d", guild.getMoraVault())), mouseX, mouseY);
                } else {
                    g.renderTooltip(font, Component.translatable("awakening_system.guild.treasury.tooltip_vault_empty"), mouseX, mouseY);
                }
            }
        } else {
            int reqW = (left + GUI_WIDTH - 18) - withStartX;
            int reqH = 15;
            int rX = withStartX;
            int rY = top + 31;
            boolean rHov = mouseX >= rX && mouseX < rX + reqW && mouseY >= rY && mouseY < rY + reqH;
            g.fill(rX, rY, rX + reqW, rY + reqH, rHov ? 0xDD7A5A1A : 0x884A3510);
            g.renderOutline(rX, rY, reqW, reqH, rHov ? 0xFFFFD700 : 0xFF7A5A1A);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_request_withdraw"), rX + reqW / 2, rY + 3, rHov ? 0xFFFFFFFF : 0xFFFFD700);
        }

        // --- ROW 2: MONEDAS (SYSTEM MONEY) ---
        g.drawString(font, Component.translatable("awakening_system.guild.treasury.coins_label"), left + 22, top + 56, 0xFF00E5FF);
        String[] depCoinLabels = {"+50", "+250", "+1k", "+10k", "+50k"};
        for (int i = 0; i < depCoinLabels.length; i++) {
            int bX = depStartX + i * (btnW + gap);
            int bY = top + 53;
            boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
            g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD1B5E7D : 0x88123B4E);
            g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFF00E5FF : 0xFF1B5E7D);
            drawScaledCenteredString(g, depCoinLabels[i], bX + btnW / 2, bY + 3, btnW - 2, 0xFF00E5FF);
        }

        if (canWithdraw) {
            String[] withCoinLabels = {"-50", "-250", "-1k", "-10k", "-50k"};
            for (int i = 0; i < withCoinLabels.length; i++) {
                int bX = withStartX + i * (btnW + gap);
                int bY = top + 53;
                boolean bHov = mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH;
                g.fill(bX, bY, bX + btnW, bY + btnH, bHov ? 0xDD7A2855 : 0x884D1A36);
                g.renderOutline(bX, bY, btnW, btnH, bHov ? 0xFFFF79C6 : 0xFF7A2855);
                drawScaledCenteredString(g, withCoinLabels[i], bX + btnW / 2, bY + 3, btnW - 2, 0xFFFF79C6);
            }

            int allY = top + 53;
            boolean allHov = mouseX >= allX && mouseX < allX + allW && mouseY >= allY && mouseY < allY + btnH;
            boolean hasCoins = guild.getCoinVault() > 0;
            g.fill(allX, allY, allX + allW, allY + btnH, hasCoins ? (allHov ? 0xDD7A2855 : 0x884D1A36) : 0x55333333);
            g.renderOutline(allX, allY, allW, btnH, hasCoins ? (allHov ? 0xFFFF79C6 : 0xFFBD93F9) : 0xFF555555);
            drawScaledCenteredString(g, "§l" + Component.translatable("awakening_system.guild.treasury.btn_withdraw_all").getString(), allX + allW / 2, allY + 3, allW - 2, hasCoins ? (allHov ? 0xFFFFFFFF : 0xFF00E5FF) : 0xFF777777);
            if (allHov) {
                if (hasCoins) {
                    g.renderTooltip(font, Component.translatable("awakening_system.guild.treasury.tooltip_withdraw_all", String.format("%,d", guild.getCoinVault())), mouseX, mouseY);
                } else {
                    g.renderTooltip(font, Component.translatable("awakening_system.guild.treasury.tooltip_vault_empty"), mouseX, mouseY);
                }
            }
        } else {
            int reqW = (left + GUI_WIDTH - 18) - withStartX;
            int reqH = 15;
            int rX = withStartX;
            int rY = top + 53;
            boolean rHov = mouseX >= rX && mouseX < rX + reqW && mouseY >= rY && mouseY < rY + reqH;
            g.fill(rX, rY, rX + reqW, rY + reqH, rHov ? 0xDD1B5E7D : 0x88123B4E);
            g.renderOutline(rX, rY, reqW, reqH, rHov ? 0xFF00E5FF : 0xFF1B5E7D);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_request_withdraw"), rX + reqW / 2, rY + 3, rHov ? 0xFFFFFFFF : 0xFF00E5FF);
        }

        int hqY = top + 94;
        int hqH = 90;
        g.fill(left + 16, hqY, left + GUI_WIDTH - 16, hqY + hqH, 0x4010202E);
        g.renderOutline(left + 16, hqY, GUI_WIDTH - 32, hqH, 0xFF4EE2EC);

        g.drawString(font, Component.translatable("awakening_system.guild.treasury.hq_title"), left + 24, hqY + 6, 0xFF00E5FF);
        if (guild.hasHq()) {
            g.drawString(font, Component.translatable("awakening_system.guild.treasury.hq_coords", (int) guild.getHqX(), (int) guild.getHqY(), (int) guild.getHqZ(), guild.getHqDimension()), left + 24, hqY + 20, 0xFFFFFFFF);
        } else {
            g.drawString(font, Component.translatable("awakening_system.guild.treasury.no_hq"), left + 24, hqY + 20, 0xFFFF6666);
        }

        g.drawString(font, Component.translatable("awakening_system.guild.treasury.chunks_status", guild.getClaimedChunks().size(), guild.getMaxClaims(), guild.getLevel()), left + 24, hqY + 34, 0xFF50FA7B);

        int actionY = hqY + 54;
        int actBtnH = 18;
        int curX = left + 22;

        int warpBtnW = 58;
        boolean warpHov = mouseX >= curX && mouseX < curX + warpBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
        g.fill(curX, actionY, curX + warpBtnW, actionY + actBtnH, warpHov ? 0xDD225577 : 0x88113344);
        g.renderOutline(curX, actionY, warpBtnW, actBtnH, warpHov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_warp"), curX + warpBtnW / 2, actionY + 5, 0xFF70E8FF);
        curX += warpBtnW + 4;

        if (isLeader) {
            int setHqW = 72;
            boolean setHov = mouseX >= curX && mouseX < curX + setHqW && mouseY >= actionY && mouseY < actionY + actBtnH;
            g.fill(curX, actionY, curX + setHqW, actionY + actBtnH, setHov ? 0xDD6E5515 : 0x88453308);
            g.renderOutline(curX, actionY, setHqW, actBtnH, setHov ? 0xFFFFD700 : 0xFF886622);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_set_hq"), curX + setHqW / 2, actionY + 5, 0xFFFFD700);
            curX += setHqW + 4;
        }

        int mapBtnW = 58;
        boolean mapHov = mouseX >= curX && mouseX < curX + mapBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
        g.fill(curX, actionY, curX + mapBtnW, actionY + actBtnH, mapHov ? 0xDD5A3A8E : 0x8838235A);
        g.renderOutline(curX, actionY, mapBtnW, actBtnH, mapHov ? 0xFFBD93F9 : 0xFF5A3A8E);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_chunk_map"), curX + mapBtnW / 2, actionY + 5, 0xFFFFD700);
        curX += mapBtnW + 4;

        if (canClaim) {
            int claimBtnW = 66;
            boolean claimHov = mouseX >= curX && mouseX < curX + claimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
            g.fill(curX, actionY, curX + claimBtnW, actionY + actBtnH, claimHov ? 0xDD28864E : 0x881A5D3E);
            g.renderOutline(curX, actionY, claimBtnW, actBtnH, claimHov ? 0xFF50FA7B : 0xFF28864E);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_claim"), curX + claimBtnW / 2, actionY + 5, 0xFF50FA7B);
            curX += claimBtnW + 4;

            int unclaimBtnW = 66;
            boolean unclaimHov = mouseX >= curX && mouseX < curX + unclaimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH;
            g.fill(curX, actionY, curX + unclaimBtnW, actionY + actBtnH, unclaimHov ? 0xDD8B2B2B : 0x885A1A1A);
            g.renderOutline(curX, actionY, unclaimBtnW, actBtnH, unclaimHov ? 0xFFFF5555 : 0xFF8B2B2B);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.treasury.btn_unclaim"), curX + unclaimBtnW / 2, actionY + 5, 0xFFFF6666);
        }
    }

    private void renderHuntingTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean canWithdraw = myRole != null && myRole.canWithdraw();

        g.drawString(font, Component.translatable("awakening_system.guild.hunts.title"), left + 18, top + 2, 0xFFFFD700);
        g.drawString(font, Component.translatable("awakening_system.guild.hunts.desc"), left + 18, top + 13, 0xFFAAAAAA);

        int cardW = GUI_WIDTH - 32;
        int cardH = 28;
        int startY = top + 26;

        Component[] questTitles = {
                Component.translatable("awakening_system.guild.hunts.quest1.title"),
                Component.translatable("awakening_system.guild.hunts.quest2.title"),
                Component.translatable("awakening_system.guild.hunts.quest3.title"),
                Component.translatable("awakening_system.guild.hunts.quest4.title")
        };
        Component[] questTargets = {
                Component.translatable("awakening_system.guild.hunts.quest1.target"),
                Component.translatable("awakening_system.guild.hunts.quest2.target"),
                Component.translatable("awakening_system.guild.hunts.quest3.target"),
                Component.translatable("awakening_system.guild.hunts.quest4.target")
        };
        int[] questProgs = {
                guild.getHuntAbyssProgress(),
                guild.getHuntBossProgress(),
                guild.getHuntEliteProgress(),
                guild.getHuntChestsProgress()
        };
        int[] questGoals = {
                guild.getHuntAbyssGoal(),
                guild.getHuntBossGoal(),
                guild.getHuntEliteGoal(),
                guild.getHuntChestsGoal()
        };
        String[] questRewards = {
                "+1,500 EXP | +25k 🪙 | +250 ⭐",
                "+2,500 EXP | +50k 🪙 | +500 ⭐",
                "+1,000 EXP | +15k 🪙 | +150 ⭐",
                "+2,000 EXP | +30k 🪙 | +300 ⭐"
        };
        int[] barColors = {0xFF50FA7B, 0xFFFFD700, 0xFFFF79C6, 0xFF00E5FF};

        for (int i = 0; i < 4; i++) {
            int qY = startY + i * (cardH + 4);
            boolean cardHov = mouseX >= left + 16 && mouseX < left + 16 + cardW && mouseY >= qY && mouseY < qY + cardH;
            g.fill(left + 16, qY, left + 16 + cardW, qY + cardH, cardHov ? 0x60162A3C : 0x4010202E);
            g.renderOutline(left + 16, qY, cardW, cardH, cardHov ? 0xFF4EE2EC : 0xFF2A4255);

            g.drawString(font, questTitles[i], left + 22, qY + 4, 0xFFFFFFFF);
            g.drawString(font, "§a" + questRewards[i], left + 22, qY + 16, 0xFF50FA7B);

            int prog = questProgs[i];
            int goal = questGoals[i];
            float ratio = Math.min(1.0F, (float) prog / Math.max(1, goal));
            int bW = 80;
            int bH = 10;
            int bX = left + cardW - bW - 8;
            int bY = qY + 9;
            g.fill(bX, bY, bX + bW, bY + bH, 0xFF0D1721);
            g.fill(bX, bY, bX + (int) (bW * ratio), bY + bH, barColors[i]);
            g.renderOutline(bX, bY, bW, bH, 0xFF4EE2EC);
            g.drawCenteredString(font, prog + " / " + goal, bX + bW / 2, bY + 1, 0xFFFFFFFF);

            if (cardHov && mouseX < bX) {
                g.renderTooltip(font, Component.translatable("awakening_system.guild.hunts.target_prefix").append(questTargets[i]), mouseX, mouseY);
            }
        }

        int botY = startY + 4 * (cardH + 4) + 4;
        int botH = 34;
        g.fill(left + 16, botY, left + 16 + cardW, botY + botH, 0x30101C28);
        g.renderOutline(left + 16, botY, cardW, botH, 0xFFDFB76C);

        int buyBtnW = 126;
        int buyBtnX = left + 16 + cardW - buyBtnW - 8;
        int maxTextW = buyBtnX - (left + 22) - 8;

        List<FormattedCharSequence> boardLines = font.split(Component.translatable("awakening_system.guild.hunts.physical_board"), maxTextW);
        int textStartY = botY + Math.max(4, (botH - boardLines.size() * 10) / 2);
        for (int l = 0; l < boardLines.size(); l++) {
            g.drawString(font, boardLines.get(l), left + 22, textStartY + l * 10, 0xFFFFFFFF);
        }

        if (canWithdraw) {
            int buyBtnH = 20;
            int buyBtnY = botY + (botH - buyBtnH) / 2;
            boolean bHov = mouseX >= buyBtnX && mouseX < buyBtnX + buyBtnW && mouseY >= buyBtnY && mouseY < buyBtnY + buyBtnH;
            g.fill(buyBtnX, buyBtnY, buyBtnX + buyBtnW, buyBtnY + buyBtnH, bHov ? 0xDD6E5515 : 0x88453308);
            g.renderOutline(buyBtnX, buyBtnY, buyBtnW, buyBtnH, bHov ? 0xFFFFD700 : 0xFF886622);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.hunts.btn_buy"), buyBtnX + buyBtnW / 2, buyBtnY + 6, 0xFFFFD700);
        } else {
            Component offComp = Component.translatable("awakening_system.guild.hunts.officers_only");
            int offW = font.width(offComp);
            g.drawString(font, offComp, left + 16 + cardW - offW - 12, botY + (botH - 9) / 2, 0xFFAAAAAA);
        }
    }

    private void renderPortalsTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        GuildRole myRole = guild.getMemberRole(myUuid);
        boolean canManage = myRole != null && myRole.canClaimTerritory();

        // 1. SECCIÓN PORTAL CERCANO
        int nearCardH = 50;
        int nearCardW = GUI_WIDTH - 32;
        int nearX = left + 16;
        int nearY = top;

        g.fill(nearX, nearY, nearX + nearCardW, nearY + nearCardH, 0x880E1C28);
        g.renderOutline(nearX, nearY, nearCardW, nearCardH, nearbyPortalTag != null ? 0xFFDFB76C : 0xFF2A4255);

        if (nearbyPortalTag != null) {
            String rank = nearbyPortalTag.getString("rank");
            boolean ownedByUs = nearbyPortalTag.getBoolean("isOwnedByMyGuild");
            String ownerId = nearbyPortalTag.getString("owningGuildId");
            boolean isFree = ownerId.isEmpty();
            boolean cooling = nearbyPortalTag.getBoolean("cooling");
            int fee = nearbyPortalTag.getInt("fee");
            boolean feeInCoins = nearbyPortalTag.getBoolean("feeInCoins");

            g.drawString(font, Component.translatable("awakening_system.guild.portals.nearby_title", rank), nearX + 8, nearY + 6, 0xFFFFFFFF);

            if (isFree) {
                g.drawString(font, Component.translatable("awakening_system.guild.portals.available"), nearX + nearCardW - 74, nearY + 6, 0xFF50FA7B);
                g.drawString(font, Component.translatable("awakening_system.guild.portals.free_portal"), nearX + 8, nearY + 20, 0xFFAAAAAA);
                g.drawString(font, Component.translatable("awakening_system.guild.portals.claim_hint"), nearX + 8, nearY + 32, 0xFF777777);

                if (canManage) {
                    int btnW = 96;
                    int btnH = 20;
                    int btnX = nearX + nearCardW - btnW - 8;
                    int btnY = nearY + 20;
                    boolean hov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
                    g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xDD28864E : 0xAA1A5D3E);
                    g.renderOutline(btnX, btnY, btnW, btnH, hov ? 0xFF50FA7B : 0xFF28864E);
                    g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_claim"), btnX + btnW / 2, btnY + 6, hov ? 0xFFFFFFFF : 0xFF50FA7B);
                }
            } else if (ownedByUs) {
                String curr = feeInCoins ? Component.translatable("awakening_system.guild.portals.currency_coins").getString() : Component.translatable("awakening_system.guild.portals.currency_mora").getString();
                String st = cooling ? Component.translatable("awakening_system.guild.portals.regenerating").getString() : Component.translatable("awakening_system.guild.portals.active").getString();
                g.drawString(font, Component.translatable("awakening_system.guild.portals.monopoly"), nearX + nearCardW - 98, nearY + 6, 0xFF4EE2EC);
                g.drawString(font, Component.translatable("awakening_system.guild.portals.status", st), nearX + 8, nearY + 20, 0xFFFFFFFF);
                g.drawString(font, Component.translatable("awakening_system.guild.portals.fee_status", String.format("%,d", fee), curr), nearX + 8, nearY + 32, 0xFFDDDDDD);

                if (canManage) {
                    int btnH = 18;
                    int relW = 58;
                    int relX = nearX + nearCardW - relW - 8;
                    int relY = nearY + 20;
                    boolean relHov = mouseX >= relX && mouseX < relX + relW && mouseY >= relY && mouseY < relY + btnH;
                    g.fill(relX, relY, relX + relW, relY + btnH, relHov ? 0xDD8B2B2B : 0xAA5A1A1A);
                    g.renderOutline(relX, relY, relW, btnH, relHov ? 0xFFFF5555 : 0xFF8B2B2B);
                    g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_release"), relX + relW / 2, relY + 5, relHov ? 0xFFFFFFFF : 0xFFFF6666);

                    int feeW = 58;
                    int feeX = relX - 4 - feeW;
                    boolean feeHov = mouseX >= feeX && mouseX < feeX + feeW && mouseY >= relY && mouseY < relY + btnH;
                    g.fill(feeX, relY, feeX + feeW, relY + btnH, feeHov ? 0xDD1F4E68 : 0xAA143242);
                    g.renderOutline(feeX, relY, feeW, btnH, feeHov ? 0xFF4EE2EC : 0xFF2A556B);
                    g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_fee"), feeX + feeW / 2, relY + 5, feeHov ? 0xFFFFFFFF : 0xFF4EE2EC);
                }
            } else {
                String ownerName = nearbyPortalTag.getString("ownerName");
                String ownerTag = nearbyPortalTag.getString("ownerTag");
                String curr = feeInCoins ? "🪙" : "Mora";
                g.drawString(font, Component.translatable("awakening_system.guild.portals.claimed"), nearX + nearCardW - 70, nearY + 6, 0xFFFF5555);
                g.drawString(font, Component.translatable("awakening_system.guild.portals.owner", ownerTag, ownerName), nearX + 8, nearY + 20, 0xFFFF7777);
                g.drawString(font, Component.translatable("awakening_system.guild.portals.rent_fee", String.format("%,d", fee), curr), nearX + 8, nearY + 32, 0xFFAAAAAA);
                g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.protected"), nearX + nearCardW - 50, nearY + 22, 0xFF888888);
            }
        } else {
            g.drawString(font, Component.translatable("awakening_system.guild.portals.no_nearby1"), nearX + 8, nearY + 10, 0xFF888888);
            g.drawString(font, Component.translatable("awakening_system.guild.portals.no_nearby2"), nearX + 8, nearY + 22, 0xFF777777);
            g.drawString(font, Component.translatable("awakening_system.guild.portals.no_nearby3"), nearX + 8, nearY + 33, 0xFF555555);
        }

        // 2. SECCIÓN PORTALES DEL GREMIO
        int listStartY = nearY + nearCardH + 6;
        int count = guildPortalsTag != null ? guildPortalsTag.size() : 0;
        g.drawString(font, Component.translatable("awakening_system.guild.portals.list_title", count), left + 18, listStartY, 0xFFFFD700);
        g.fill(left + 16, listStartY + 10, left + GUI_WIDTH - 16, listStartY + 11, 0x334EE2EC);

        int cardsY = listStartY + 14;
        if (count == 0) {
            int emptyH = 50;
            g.fill(left + 16, cardsY, left + GUI_WIDTH - 16, cardsY + emptyH, 0x4010202E);
            g.renderOutline(left + 16, cardsY, GUI_WIDTH - 32, emptyH, 0xFF2A4255);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.empty_list1"), left + GUI_WIDTH / 2, cardsY + 14, 0xFFAAAAAA);
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.empty_list2"), left + GUI_WIDTH / 2, cardsY + 28, 0xFF666666);
            return;
        }

        int cardW = GUI_WIDTH - 32;
        int cardH = 36;
        int maxVisible = 3;

        for (int i = 0; i < Math.min(maxVisible, count); i++) {
            int idx = i + portalScrollOffset;
            if (idx >= count) break;
            CompoundTag pTag = guildPortalsTag.getCompound(idx);

            String rank = pTag.getString("rank");
            int fee = pTag.getInt("fee");
            boolean feeInCoins = pTag.getBoolean("feeInCoins");
            boolean cooling = pTag.getBoolean("cooling");

            int cY = cardsY + i * (cardH + 4);
            boolean cardHov = mouseX >= left + 16 && mouseX < left + 16 + cardW && mouseY >= cY && mouseY < cY + cardH;

            g.fill(left + 16, cY, left + 16 + cardW, cY + cardH, cardHov ? 0x60162A3C : 0x4010202E);
            g.renderOutline(left + 16, cY, cardW, cardH, 0xFF2A4255);

            String st = cooling ? Component.translatable("awakening_system.guild.portals.regenerating").getString() : Component.translatable("awakening_system.guild.portals.active").getString();
            g.drawString(font, Component.translatable("awakening_system.guild.portals.item_header", rank, st), left + 22, cY + 5, 0xFFFFFFFF);

            String currLabel = feeInCoins ? Component.translatable("awakening_system.guild.portals.item_coins", String.format("%,d", fee)).getString() : Component.translatable("awakening_system.guild.portals.item_mora", String.format("%,d", fee)).getString();
            g.drawString(font, Component.translatable("awakening_system.guild.portals.item_fee", currLabel), left + 22, cY + 19, 0xFFDDDDDD);

            if (canManage) {
                int btnH = 17;
                int rightX = left + 16 + cardW - 8;

                // 1. Botón Liberar
                int unclaimW = 46;
                int unclaimX = rightX - unclaimW;
                boolean uncHov = mouseX >= unclaimX && mouseX < unclaimX + unclaimW && mouseY >= cY + 9 && mouseY < cY + 9 + btnH;
                g.fill(unclaimX, cY + 9, unclaimX + unclaimW, cY + 9 + btnH, uncHov ? 0xDD8B2B2B : 0x885A1A1A);
                g.renderOutline(unclaimX, cY + 9, unclaimW, btnH, uncHov ? 0xFFFF5555 : 0xFF8B2B2B);
                g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_release"), unclaimX + unclaimW / 2, cY + 13, uncHov ? 0xFFFFFFFF : 0xFFFF7777);

                // 2. Botón Fijar Tarifa Personalizada
                int feeBtnW = 54;
                int feeBtnX = unclaimX - 4 - feeBtnW;
                boolean feeHov = mouseX >= feeBtnX && mouseX < feeBtnX + feeBtnW && mouseY >= cY + 9 && mouseY < cY + 9 + btnH;
                g.fill(feeBtnX, cY + 9, feeBtnX + feeBtnW, cY + 9 + btnH, feeHov ? 0xCC1F4E68 : 0x88143242);
                g.renderOutline(feeBtnX, cY + 9, feeBtnW, btnH, feeHov ? 0xFF4EE2EC : 0xFF2A556B);
                g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_fee"), feeBtnX + feeBtnW / 2, cY + 13, feeHov ? 0xFFFFFFFF : 0xFF4EE2EC);

                // 3. Botón Moneda (🪙 Coin / Mora)
                int currBtnW = 48;
                int currBtnX = feeBtnX - 4 - currBtnW;
                boolean currHov = mouseX >= currBtnX && mouseX < currBtnX + currBtnW && mouseY >= cY + 9 && mouseY < cY + 9 + btnH;
                g.fill(currBtnX, cY + 9, currBtnX + currBtnW, cY + 9 + btnH, currHov ? 0xCC6A4E1A : 0x884D3712);
                g.renderOutline(currBtnX, cY + 9, currBtnW, btnH, currHov ? 0xFFFFD700 : 0xFFDFB76C);
                String btnText = feeInCoins ? "🪙 Coin" : "🪙 Mora";
                g.drawCenteredString(font, btnText, currBtnX + currBtnW / 2, cY + 13, currHov ? 0xFFFFD700 : 0xFFE0E0E0);
            }
        }
    }

    private void renderAlliancesTab(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
        boolean isLeader = guild != null && guild.getLeaderUuid().equals(myUuid);

        g.drawString(font, Component.translatable("awakening_system.guild.alliances.title"), left + 18, top + 2, 0xFFFFD700);
        g.drawString(font, Component.translatable("awakening_system.guild.alliances.desc"), left + 18, top + 13, 0xFFAAAAAA);

        int cardW = GUI_WIDTH - 32;
        int cardH = 30;
        int startY = top + 26;

        if (allGuildsTag == null || allGuildsTag.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("awakening_system.guild.alliances.empty"), left + GUI_WIDTH / 2, startY + 40, 0xFFAAAAAA);
            return;
        }

        int maxVisible = 5;
        int total = allGuildsTag.size();
        for (int i = 0; i < Math.min(maxVisible, total); i++) {
            int idx = i + allianceScrollOffset;
            if (idx >= total) break;
            CompoundTag gTag = allGuildsTag.getCompound(idx);
            String gName = gTag.getString("Name");
            String gCode = gTag.getString("Tag");
            int gLevel = gTag.getInt("Level");
            int gMembers = gTag.getInt("MemberCount");
            String gLeader = gTag.getString("LeaderName");
            boolean isAlly = gTag.getBoolean("IsAlly");
            boolean isSelf = gTag.getBoolean("IsSelf");

            int cY = startY + i * (cardH + 4);
            boolean cardHov = mouseX >= left + 16 && mouseX < left + 16 + cardW && mouseY >= cY && mouseY < cY + cardH;

            int outlineCol = isSelf ? 0xFFDFB76C : (isAlly ? 0xFF50FA7B : 0xFF2A4255);
            g.fill(left + 16, cY, left + 16 + cardW, cY + cardH, cardHov ? 0x60162A3C : 0x4010202E);
            g.renderOutline(left + 16, cY, cardW, cardH, outlineCol);

            g.drawString(font, Component.translatable("awakening_system.guild.alliances.guild_info", gCode, gName, gLevel, gMembers), left + 22, cY + 4, 0xFFFFFFFF);
            String leaderStr = Component.translatable("awakening_system.guild.alliances.leader", gLeader).getString() + (isAlly ? "  " + Component.translatable("awakening_system.guild.alliances.pact_active").getString() : "");
            g.drawString(font, leaderStr, left + 22, cY + 16, 0xFFAAAAAA);

            if (isSelf) {
                g.drawString(font, Component.translatable("awakening_system.guild.alliances.your_guild"), left + cardW - 68, cY + 11, 0xFFFFD700);
            } else {
                int btnW = 54;
                int btnH = 16;
                int btnX = left + cardW - btnW - 6;
                int btnY = cY + 7;

                if (isLeader) {
                    boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
                    if (isAlly) {
                        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHov ? 0xDD8B2B2B : 0x885A1A1A);
                        g.renderOutline(btnX, btnY, btnW, btnH, btnHov ? 0xFFFF5555 : 0xFF8B2B2B);
                        g.drawCenteredString(font, Component.translatable("awakening_system.guild.alliances.btn_break"), btnX + btnW / 2, btnY + 4, 0xFFFF6666);
                    } else {
                        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHov ? 0xDD28864E : 0x881A5D3E);
                        g.renderOutline(btnX, btnY, btnW, btnH, btnHov ? 0xFF50FA7B : 0xFF28864E);
                        g.drawCenteredString(font, Component.translatable("awakening_system.guild.alliances.btn_pact"), btnX + btnW / 2, btnY + 4, 0xFF50FA7B);
                    }
                } else {
                    if (isAlly) {
                        g.drawString(font, Component.translatable("awakening_system.guild.alliances.ally"), left + cardW - 48, cY + 11, 0xFF50FA7B);
                    } else {
                        g.drawString(font, Component.translatable("awakening_system.guild.alliances.neutral"), left + cardW - 50, cY + 11, 0xFF888888);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int left = (width - GUI_WIDTH) / 2;
            int top = (height - GUI_HEIGHT) / 2;

            if (editingPortalFeeModal) {
                int mw = 220;
                int mh = 126;
                int mx = left + (GUI_WIDTH - mw) / 2;
                int my = top + (GUI_HEIGHT - mh) / 2;

                if (portalFeeBox != null && portalFeeBox.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }

                // Preset Pills
                String[] presets = {"0", "10000", "100000", "500000", "1000000"};
                int pillW = 34;
                int pillH = 14;
                for (int p = 0; p < 5; p++) {
                    int px = mx + 16 + p * (pillW + 4);
                    int py = my + 66;
                    if (mouseX >= px && mouseX < px + pillW && mouseY >= py && mouseY < py + pillH) {
                        if (portalFeeBox != null) {
                            portalFeeBox.setValue(presets[p]);
                        }
                        AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.4F);
                        return true;
                    }
                }

                // Botón Confirmar
                int confW = 90;
                int confH = 20;
                int confX = mx + 16;
                int confY = my + 88;
                if (mouseX >= confX && mouseX < confX + confW && mouseY >= confY && mouseY < confY + confH) {
                    confirmPortalFee();
                    return true;
                }

                // Botón Cancelar
                int cancW = 82;
                int cancH = 20;
                int cancX = mx + mw - 16 - cancW;
                int cancY = my + 88;
                if (mouseX >= cancX && mouseX < cancX + cancW && mouseY >= cancY && mouseY < cancY + cancH) {
                    closePortalFeeModal();
                    return true;
                }

                // Clic fuera del diálogo cierra el modal
                if (mouseX < mx || mouseX > mx + mw || mouseY < my || mouseY > my + mh) {
                    closePortalFeeModal();
                }
                return true;
            }

            if (mouseX >= left + GUI_WIDTH - 22 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20) {
                onClose();
                return true;
            }

            if (!inGuild || guild == null) {
                int btnW = 160;
                int btnH = 22;
                int btnX = left + (GUI_WIDTH - btnW) / 2;
                int btnY = top + 200;
                if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    if (createNameBox != null && createTagBox != null) {
                        String name = createNameBox.getValue().trim();
                        String tag = createTagBox.getValue().trim();
                        if (!name.isEmpty() && !tag.isEmpty()) {
                            PacketDistributor.sendToServer(new GuildActionPayload("CREATE", name + ":" + tag, 0));
                            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                            onClose();
                            return true;
                        }
                    }
                }
            } else {
                int tabW = (GUI_WIDTH - 28) / 6;
                for (int i = 0; i < 6; i++) {
                    int tX = left + 14 + i * tabW;
                    int tY = top + 32;
                    int tH = 18;
                    if (mouseX >= tX && mouseX < tX + tabW && mouseY >= tY && mouseY < tY + tH) {
                        currentTab = i;
                        lastTab = i;
                        if (this.inviteBox != null) {
                            this.inviteBox.visible = (currentTab == 0);
                            this.inviteBox.active = (currentTab == 0);
                        }
                        closePortalFeeModal();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                }

                int contentY = top + 56;
                Minecraft mc = Minecraft.getInstance();
                UUID myUuid = mc.player != null ? mc.player.getUUID() : UUID.randomUUID();
                boolean isLeader = guild.getLeaderUuid().equals(myUuid);

                if (currentTab == 0) {
                    List<GuildMember> membersList = new ArrayList<>(guild.getMembers().values());
                    int rowH = 18;
                    for (int i = 0; i < Math.min(7, membersList.size()); i++) {
                        int idx = i + memberScrollOffset;
                        if (idx >= membersList.size()) break;
                        GuildMember m = membersList.get(idx);
                        int rY = contentY + 18 + i * rowH;

                        if (isLeader && !m.getUuid().equals(myUuid)) {
                            if (mouseX >= left + 285 && mouseX < left + 301 && mouseY >= rY && mouseY < rY + 14) {
                                PacketDistributor.sendToServer(new GuildActionPayload("PROMOTE", m.getUuid().toString(), 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.5F);
                                return true;
                            }
                            if (mouseX >= left + 305 && mouseX < left + 321 && mouseY >= rY && mouseY < rY + 14) {
                                PacketDistributor.sendToServer(new GuildActionPayload("DEMOTE", m.getUuid().toString(), 0));
                                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.8F);
                                return true;
                            }
                            if (mouseX >= left + 325 && mouseX < left + 341 && mouseY >= rY && mouseY < rY + 14) {
                                PacketDistributor.sendToServer(new GuildActionPayload("KICK", m.getUuid().toString(), 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_DESTROY, 0.7F, 1.2F);
                                return true;
                            }
                        }
                    }

                    int btmY = contentY + GUI_HEIGHT - 38 - 56;
                    int invBtnX = left + 165;
                    int invBtnW = 75;
                    int invBtnH = 16;
                    if (mouseX >= invBtnX && mouseX < invBtnX + invBtnW && mouseY >= btmY && mouseY < btmY + invBtnH) {
                        if (inviteBox != null && !inviteBox.getValue().trim().isEmpty()) {
                            PacketDistributor.sendToServer(new GuildActionPayload("INVITE", inviteBox.getValue().trim(), 0));
                            inviteBox.setValue("");
                            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                            return true;
                        }
                    }

                    int leaveBtnX = left + GUI_WIDTH - 110;
                    int leaveBtnW = 94;
                    int leaveBtnH = 16;
                    if (mouseX >= leaveBtnX && mouseX < leaveBtnX + leaveBtnW && mouseY >= btmY && mouseY < btmY + leaveBtnH) {
                        if (isLeader) {
                            PacketDistributor.sendToServer(new GuildActionPayload("DISBAND", "", 0));
                        } else {
                            PacketDistributor.sendToServer(new GuildActionPayload("LEAVE", "", 0));
                        }
                        AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.8F, 1.0F);
                        onClose();
                        return true;
                    }
                } else if (currentTab == 1) {
                    GuildPerk[] perks = GuildPerk.values();
                    int cardW = GUI_WIDTH - 32;
                    int cardH = 24;

                    for (int i = 0; i < perks.length; i++) {
                        GuildPerk p = perks[i];
                        int pY = contentY + i * (cardH + 4);
                        if (isLeader && !guild.hasPerk(p) && guild.getLevel() >= p.getRequiredGuildLevel() && guild.getMoraVault() >= p.getCostMora()) {
                            int btnX = left + cardW - 75;
                            int btnW = 85;
                            int btnH = 16;
                            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= pY + 4 && mouseY < pY + 4 + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("PERK", p.getId(), 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                                return true;
                            }
                        }
                    }
                } else if (currentTab == 2) {
                    int btnW = 23;
                    int btnH = 15;
                    int gap = 2;
                    int depStartX = left + 68;
                    int withStartX = depStartX + 5 * (btnW + gap) + 4;
                    int allW = 38;
                    int allX = withStartX + 5 * (btnW + gap) + 2;

                    int[] depMora = {1000, 10000, 50000, 250000, 1000000};
                    int[] depCoins = {50, 250, 1000, 10000, 50000};

                    for (int i = 0; i < depMora.length; i++) {
                        int bX = depStartX + i * (btnW + gap);
                        int bY = contentY + 31;
                        if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("DEPOSIT", "", depMora[i]));
                            AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.4F);
                            return true;
                        }
                    }

                    for (int i = 0; i < depCoins.length; i++) {
                        int bX = depStartX + i * (btnW + gap);
                        int bY = contentY + 53;
                        if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("DEPOSIT_COINS", "", depCoins[i]));
                            AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.4F);
                            return true;
                        }
                    }

                    GuildRole myRole = guild.getMemberRole(myUuid);
                    if (myRole != null && myRole.canWithdraw()) {
                        for (int i = 0; i < depMora.length; i++) {
                            int bX = withStartX + i * (btnW + gap);
                            int bY = contentY + 31;
                            if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("WITHDRAW_MORA", "", depMora[i]));
                                AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.9F);
                                return true;
                            }
                        }

                        // Sacar Todo Mora (TODO)
                        if (mouseX >= allX && mouseX < allX + allW && mouseY >= contentY + 31 && mouseY < contentY + 31 + btnH) {
                            if (guild.getMoraVault() > 0) {
                                PacketDistributor.sendToServer(new GuildActionPayload("WITHDRAW_ALL_MORA", "", 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                            }
                            return true;
                        }

                        for (int i = 0; i < depCoins.length; i++) {
                            int bX = withStartX + i * (btnW + gap);
                            int bY = contentY + 53;
                            if (mouseX >= bX && mouseX < bX + btnW && mouseY >= bY && mouseY < bY + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("WITHDRAW_COINS", "", depCoins[i]));
                                AwakeningSystemClient.playUiSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.9F);
                                return true;
                            }
                        }

                        // Sacar Todo Coins (TODO)
                        if (mouseX >= allX && mouseX < allX + allW && mouseY >= contentY + 53 && mouseY < contentY + 53 + btnH) {
                            if (guild.getCoinVault() > 0) {
                                PacketDistributor.sendToServer(new GuildActionPayload("WITHDRAW_ALL_COINS", "", 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                            }
                            return true;
                        }
                    } else {
                        int reqW = (left + GUI_WIDTH - 18) - withStartX;
                        int reqH = 15;
                        int rX = withStartX;
                        if (mouseX >= rX && mouseX < rX + reqW && mouseY >= contentY + 31 && mouseY < contentY + 31 + reqH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("REQUEST_WITHDRAW", "MORA", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                            return true;
                        }
                        if (mouseX >= rX && mouseX < rX + reqW && mouseY >= contentY + 53 && mouseY < contentY + 53 + reqH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("REQUEST_WITHDRAW", "COINS", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                            return true;
                        }
                    }

                    int hqY = contentY + 94;
                    int actionY = hqY + 54;
                    int actBtnH = 18;
                    int curX = left + 22;

                    int warpBtnW = 58;
                    if (mouseX >= curX && mouseX < curX + warpBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                        PacketDistributor.sendToServer(new GuildActionPayload("HOME", "", 0));
                        AwakeningSystemClient.playUiSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        onClose();
                        return true;
                    }
                    curX += warpBtnW + 4;

                    if (isLeader) {
                        int setHqW = 72;
                        if (mouseX >= curX && mouseX < curX + setHqW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("SET_HOME", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                            return true;
                        }
                        curX += setHqW + 4;
                    }

                    int mapBtnW = 58;
                    if (mouseX >= curX && mouseX < curX + mapBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                        Minecraft.getInstance().setScreen(new GuildChunkMapScreen(this.guild, this.allClaimsTag, this));
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                        return true;
                    }
                    curX += mapBtnW + 4;

                    if (myRole != null && myRole.canClaimTerritory()) {
                        int claimBtnW = 66;
                        if (mouseX >= curX && mouseX < curX + claimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("CLAIM_CHUNK", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                            return true;
                        }
                        curX += claimBtnW + 4;

                        int unclaimBtnW = 66;
                        if (mouseX >= curX && mouseX < curX + unclaimBtnW && mouseY >= actionY && mouseY < actionY + actBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("UNCLAIM_CHUNK", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                            return true;
                        }
                    }
                } else if (currentTab == 3) {
                    GuildRole myRole = guild.getMemberRole(myUuid);
                    if (myRole != null && myRole.canWithdraw()) {
                        int cardW = GUI_WIDTH - 32;
                        int botY = contentY + 26 + 4 * (28 + 4) + 4;
                        int botH = 34;
                        int buyBtnW = 126;
                        int buyBtnX = left + 16 + cardW - buyBtnW - 8;
                        int buyBtnH = 20;
                        int buyBtnY = botY + (botH - buyBtnH) / 2;
                        if (mouseX >= buyBtnX && mouseX < buyBtnX + buyBtnW && mouseY >= buyBtnY && mouseY < buyBtnY + buyBtnH) {
                            PacketDistributor.sendToServer(new GuildActionPayload("BUY_HUNTING_BOARD", "", 0));
                            AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                            return true;
                        }
                    }
                } else if (currentTab == 4) {
                    GuildRole myRole = guild.getMemberRole(myUuid);
                    boolean canManage = myRole != null && myRole.canClaimTerritory();

                    // 1. Botón de Portal Cercano
                    if (nearbyPortalTag != null && canManage) {
                        int nearCardH = 50;
                        int nearCardW = GUI_WIDTH - 32;
                        int nearX = left + 16;
                        int nearY = contentY;

                        boolean ownedByUs = nearbyPortalTag.getBoolean("isOwnedByMyGuild");
                        boolean isFree = nearbyPortalTag.getString("owningGuildId").isEmpty();
                        String posStr = nearbyPortalTag.getInt("x") + ":" + nearbyPortalTag.getInt("y") + ":" + nearbyPortalTag.getInt("z");
                        String rank = nearbyPortalTag.getString("rank");
                        int fee = nearbyPortalTag.getInt("fee");

                        if (isFree) {
                            int btnW = 96;
                            int btnH = 20;
                            int btnX = nearX + nearCardW - btnW - 8;
                            int btnY = nearY + 20;
                            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("CLAIM_PORTAL", posStr, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                                return true;
                            }
                        } else if (ownedByUs) {
                            int btnH = 18;
                            int relW = 58;
                            int relX = nearX + nearCardW - relW - 8;
                            int relY = nearY + 20;
                            if (mouseX >= relX && mouseX < relX + relW && mouseY >= relY && mouseY < relY + btnH) {
                                PacketDistributor.sendToServer(new GuildActionPayload("UNCLAIM_PORTAL", posStr, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                                return true;
                            }

                            int feeW = 58;
                            int feeX = relX - 4 - feeW;
                            if (mouseX >= feeX && mouseX < feeX + feeW && mouseY >= relY && mouseY < relY + btnH) {
                                openPortalFeeModal(posStr, rank, fee);
                                return true;
                            }
                        }
                    }

                    // 2. Botones de Portales del Gremio
                    if (canManage && guildPortalsTag != null && !guildPortalsTag.isEmpty()) {
                        int nearCardH = 50;
                        int listStartY = contentY + nearCardH + 6;
                        int cardsY = listStartY + 14;
                        int cardW = GUI_WIDTH - 32;
                        int cardH = 36;
                        int maxVisible = 3;
                        int count = guildPortalsTag.size();

                        for (int i = 0; i < Math.min(maxVisible, count); i++) {
                            int idx = i + portalScrollOffset;
                            if (idx >= count) break;
                            CompoundTag pTag = guildPortalsTag.getCompound(idx);
                            int px = pTag.getInt("x");
                            int py = pTag.getInt("y");
                            int pz = pTag.getInt("z");
                            String rank = pTag.getString("rank");
                            int fee = pTag.getInt("fee");
                            String posStr = px + ":" + py + ":" + pz;

                            int cY = cardsY + i * (cardH + 4);
                            int btnH = 17;
                            int rightX = left + 16 + cardW - 8;

                            int unclaimW = 46;
                            int unclaimX = rightX - unclaimW;

                            int feeBtnW = 54;
                            int feeBtnX = unclaimX - 4 - feeBtnW;

                            int currBtnW = 48;
                            int currBtnX = feeBtnX - 4 - currBtnW;

                            if (mouseY >= cY + 9 && mouseY < cY + 9 + btnH) {
                                if (mouseX >= unclaimX && mouseX < unclaimX + unclaimW) {
                                    PacketDistributor.sendToServer(new GuildActionPayload("UNCLAIM_PORTAL", posStr, 0));
                                    AwakeningSystemClient.playUiSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
                                    return true;
                                }
                                if (mouseX >= feeBtnX && mouseX < feeBtnX + feeBtnW) {
                                    openPortalFeeModal(posStr, rank, fee);
                                    return true;
                                }
                                if (mouseX >= currBtnX && mouseX < currBtnX + currBtnW) {
                                    PacketDistributor.sendToServer(new GuildActionPayload("TOGGLE_PORTAL_CURRENCY", posStr, 0));
                                    AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
                                    return true;
                                }
                            }
                        }
                    }
                } else if (currentTab == 5) {
                    if (isLeader && allGuildsTag != null) {
                        int cardW = GUI_WIDTH - 32;
                        int cardH = 30;
                        int startY = contentY + 26;
                        int maxVisible = 5;
                        int total = allGuildsTag.size();
                        for (int i = 0; i < Math.min(maxVisible, total); i++) {
                            int idx = i + allianceScrollOffset;
                            if (idx >= total) break;
                            CompoundTag gTag = allGuildsTag.getCompound(idx);
                            if (gTag.getBoolean("IsSelf")) continue;

                            int cY = startY + i * (cardH + 4);
                            int btnW = 54;
                            int btnH = 16;
                            int btnX = left + cardW - btnW - 6;
                            int btnY = cY + 7;

                            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                                String targetId = gTag.getString("Id");
                                PacketDistributor.sendToServer(new GuildActionPayload("ALLY", targetId, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (currentTab == 0) {
            memberScrollOffset = Math.max(0, memberScrollOffset - (int) scrollY);
            return true;
        } else if (currentTab == 1) {
            perkScrollOffset = Math.max(0, perkScrollOffset - (int) scrollY);
            return true;
        } else if (currentTab == 4) {
            portalScrollOffset = Math.max(0, portalScrollOffset - (int) scrollY);
            return true;
        } else if (currentTab == 5) {
            allianceScrollOffset = Math.max(0, allianceScrollOffset - (int) scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void openPortalFeeModal(String posStr, String rank, int currentFee) {
        this.editingPortalFeeModal = true;
        this.editingPortalPos = posStr;
        this.editingPortalRank = rank;
        if (this.portalFeeBox != null) {
            this.portalFeeBox.setVisible(true);
            this.portalFeeBox.setEditable(true);
            this.portalFeeBox.setFocused(true);
            this.portalFeeBox.setValue(String.valueOf(Math.max(0, currentFee)));
        }
        AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
    }

    private void closePortalFeeModal() {
        this.editingPortalFeeModal = false;
        this.editingPortalPos = "";
        this.editingPortalRank = "";
        if (this.portalFeeBox != null) {
            this.portalFeeBox.setVisible(false);
            this.portalFeeBox.setFocused(false);
        }
        AwakeningSystemClient.playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 0.8F);
    }

    private void confirmPortalFee() {
        if (!editingPortalFeeModal || editingPortalPos.isEmpty()) return;
        int fee = 0;
        if (portalFeeBox != null) {
            try {
                String val = portalFeeBox.getValue().trim();
                if (!val.isEmpty()) {
                    long parsed = Long.parseLong(val);
                    fee = (int) Math.min(100_000_000L, Math.max(0L, parsed));
                }
            } catch (Exception ignored) {}
        }
        PacketDistributor.sendToServer(new GuildActionPayload("SET_PORTAL_FEE", editingPortalPos, fee));
        AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.4F);
        closePortalFeeModal();
    }

    private void renderPortalFeeModal(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        // Fondo oscurecido para concentrar atención
        g.fillGradient(0, 0, width, height, 0xCC000000, 0xDD0A1018);

        int mw = 220;
        int mh = 126;
        int mx = left + (GUI_WIDTH - mw) / 2;
        int my = top + (GUI_HEIGHT - mh) / 2;

        // Tarjeta del modal con borde cian brillante
        g.fill(mx, my, mx + mw, my + mh, 0xF20D1B2A);
        g.renderOutline(mx, my, mw, mh, 0xFF4EE2EC);

        // Cabecera
        g.drawString(font, Component.translatable("awakening_system.guild.portals.modal_title"), mx + 14, my + 10, 0xFFFFD700);
        g.drawString(font, Component.translatable("awakening_system.guild.portals.modal_subtitle", editingPortalRank), mx + 14, my + 22, 0xFF888888);

        // Input Box
        if (portalFeeBox != null) {
            portalFeeBox.setX(mx + 14);
            portalFeeBox.setY(my + 36);
            portalFeeBox.setWidth(mw - 28);
            portalFeeBox.render(g, mouseX, mouseY, 0);
        }

        // Preset Pills
        String[] presets = {"0", "10000", "100000", "500000", "1000000"};
        Component[] labels = {
                Component.translatable("awakening_system.guild.portals.free"),
                Component.literal("10k"),
                Component.literal("100k"),
                Component.literal("500k"),
                Component.literal("1M")
        };
        int pillW = 34;
        int pillH = 14;
        for (int p = 0; p < 5; p++) {
            int px = mx + 14 + p * (pillW + 5);
            int py = my + 64;
            boolean pHov = mouseX >= px && mouseX < px + pillW && mouseY >= py && mouseY < py + pillH;
            g.fill(px, py, px + pillW, py + pillH, pHov ? 0xCC2A4255 : 0x88162A3C);
            g.renderOutline(px, py, pillW, pillH, pHov ? 0xFF4EE2EC : 0xFF2A556B);
            g.drawCenteredString(font, labels[p], px + pillW / 2, py + 3, pHov ? 0xFFFFFFFF : 0xFFAAAAAA);
        }

        // Botón Confirmar
        int confW = 90;
        int confH = 20;
        int confX = mx + 14;
        int confY = my + 88;
        boolean confHov = mouseX >= confX && mouseX < confX + confW && mouseY >= confY && mouseY < confY + confH;
        g.fill(confX, confY, confX + confW, confY + confH, confHov ? 0xDD28864E : 0xAA1A5D3E);
        g.renderOutline(confX, confY, confW, confH, confHov ? 0xFF50FA7B : 0xFF28864E);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_confirm"), confX + confW / 2, confY + 6, confHov ? 0xFFFFFFFF : 0xFF50FA7B);

        // Botón Cancelar
        int cancW = 84;
        int cancH = 20;
        int cancX = mx + mw - 14 - cancW;
        int cancY = my + 88;
        boolean cancHov = mouseX >= cancX && mouseX < cancX + cancW && mouseY >= cancY && mouseY < cancY + cancH;
        g.fill(cancX, cancY, cancX + cancW, cancY + cancH, cancHov ? 0xDD8B2B2B : 0xAA5A1A1A);
        g.renderOutline(cancX, cancY, cancW, cancH, cancHov ? 0xFFFF5555 : 0xFF8B2B2B);
        g.drawCenteredString(font, Component.translatable("awakening_system.guild.portals.btn_cancel"), cancX + cancW / 2, cancY + 6, cancHov ? 0xFFFFFFFF : 0xFFFF6666);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingPortalFeeModal) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closePortalFeeModal();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                confirmPortalFee();
                return true;
            }
            if (portalFeeBox != null && portalFeeBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
