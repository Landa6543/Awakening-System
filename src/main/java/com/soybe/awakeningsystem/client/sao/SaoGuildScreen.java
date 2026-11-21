package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.guild.Guild;
import com.soybe.awakeningsystem.guild.GuildActionPayload;
import com.soybe.awakeningsystem.guild.GuildMember;
import com.soybe.awakeningsystem.guild.GuildPerk;
import com.soybe.awakeningsystem.guild.GuildRole;
import com.soybe.awakeningsystem.guild.GuildScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The guild in the full-dive look: members (promote, demote, kick, teleport, invite, leave), the blessings to
 * buy and the treasury with the headquarters and territory. Hunts, portals, alliances and founding a guild open
 * the normal guild window. The same actions as the normal window.
 */
public class SaoGuildScreen extends SaoWindow {
    private static final int ROWS = 7, ROW_H = 20;
    private CompoundTag raw;
    private Guild guild;
    private int tab = 0;
    private EditBox invite;

    public SaoGuildScreen(Screen parent, CompoundTag data) {
        super(parent, 420, 244);
        updateData(data);
    }

    public void updateData(CompoundTag data) {
        if (data == null) return;
        this.raw = data;
        this.guild = data.getBoolean("InGuild") && data.contains("Guild") ? Guild.load(data.getCompound("Guild")) : null;
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.guild." + k, args);
    }

    private static String plain(Component c) {
        return c.getString().replaceAll("§.", "");
    }

    @Override
    protected void init() {
        super.init();
        invite = new EditBox(this.font, 0, 0, 140, 12, Component.empty());
        invite.setMaxLength(16);
        invite.setBordered(false);
        invite.setTextColor(0xFF484848);
        invite.setHint(key("invite_hint"));
        addWidget(invite);
    }

    @Override
    protected Component heading() {
        return guild == null ? tr("guild_menu", "Guild") : Component.literal("[" + guild.getTag() + "] " + guild.getName());
    }

    @Override
    protected String[] glyph() {
        return PEOPLE;
    }

    @Override
    protected String headerRight() {
        return guild == null ? null : plain(key("level_tag", guild.getLevel()));
    }

    private UUID me() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getUUID() : new UUID(0, 0);
    }

    private void act(String action, String target, int amount) {
        PacketDistributor.sendToServer(new GuildActionPayload(action, target, amount));
    }

    /** Opens the normal guild window at a page, without being swapped back to this one. */
    private void classic(int page) {
        UiStyle.passThrough = true;
        Minecraft.getInstance().setScreen(new GuildScreen(raw).atTab(page));
    }

    // layout
    private int tabW() { return (pw - 24 - 3 * 3) / 4; }
    private int tabX(int i) { return left() + 12 + i * (tabW() + 3); }
    private int tabY() { return top() + HEADER + 8; }
    private int listY() { return top() + HEADER + 30; }
    private int footY() { return top() + ph - 24; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        if (guild == null) {
            Component t1 = key("screen.form_title"), t2 = key("screen.form_desc1");
            g.drawString(this.font, t1, x + pw / 2 - this.font.width(t1) / 2, y + 70, alpha(GREY_DARK, a), false);
            g.drawString(this.font, t2, x + pw / 2 - this.font.width(t2) / 2, y + 86, alpha(GREY, a), false);
            button(g, x + pw / 2 - 80, y + 110, 160, 18, key("screen.btn_found"), true, mouseX, mouseY, a);
            return;
        }
        // experience
        long exp = guild.getExp(), need = Math.max(1, guild.getExpForNextLevel());
        bar(g, x + pw - 140, y + HEADER - 4, 128, 2, Math.min(1, exp / (float) need), ORANGE, a);

        Component[] tabs = {key("tab.members"), key("tab.blessings"), key("tab.treasury"), tr("guild_more", "More")};
        for (int i = 0; i < 4; i++) softButton(g, tabX(i), tabY(), tabW(), 15, Component.literal(plain(tabs[i])), tab == i, mouseX, mouseY, a);
        switch (tab) {
            case 0 -> members(g, x, mouseX, mouseY, a);
            case 1 -> blessings(g, x, mouseX, mouseY, a);
            default -> treasury(g, x, mouseX, mouseY, a);
        }
    }

    private List<GuildMember> memberList() {
        return new ArrayList<>(guild.getMembers().values());
    }

    private void members(GuiGraphics g, int x, int mouseX, int mouseY, float a) {
        UUID me = me();
        boolean leader = guild.getLeaderUuid().equals(me);
        GuildRole myRole = guild.getMemberRole(me);
        Minecraft mc = Minecraft.getInstance();
        List<GuildMember> list = memberList();
        scroll = Math.min(scroll, Math.max(0, list.size() - ROWS));
        int ly = listY(), rw = pw - 28;
        for (int r = 0; r < ROWS && scroll + r < list.size(); r++) {
            GuildMember m = list.get(scroll + r);
            int ry = ly + r * ROW_H;
            card(g, x + 12, ry, rw, ROW_H - 3, in(mouseX, mouseY, x + 12, ry, rw, ROW_H - 3), a);
            boolean online = m.isNpc() || (mc.getConnection() != null && mc.getConnection().getPlayerInfo(m.getUuid()) != null);
            disc(g, x + 20, ry + 8.5F, 2.8F, alpha(m.isNpc() ? ORANGE : online ? 0xFF4FA84A : 0xFFC9C9CE, a));
            g.drawString(this.font, plain(Component.literal(m.getRole().getLabel())), x + 28, ry + 4, alpha(ORANGE_DARK, a), false);
            String name = m.getName() + (m.getUuid().equals(me) ? " " + plain(key("you")) : "");
            g.drawString(this.font, this.font.plainSubstrByWidth(name, 150), x + 110, ry + 4, alpha(GREY_DARK, a), false);
            String don = String.format(Locale.US, "%,d", m.getContributedMora());
            g.drawString(this.font, don, x + 270 - this.font.width(don), ry + 4, alpha(GREY, a), false);
            int bx = x + rw - 90;
            if (leader && !m.getUuid().equals(me)) {
                softButton(g, bx, ry + 2, 18, 13, Component.literal("▲"), false, mouseX, mouseY, a);
                softButton(g, bx + 20, ry + 2, 18, 13, Component.literal("▼"), false, mouseX, mouseY, a);
                softButton(g, bx + 40, ry + 2, 18, 13, Component.literal("✕"), false, mouseX, mouseY, a);
            }
            if (!m.getUuid().equals(me) && online) button(g, bx + 62, ry + 2, 24, 13, Component.literal("⚡"), true, mouseX, mouseY, a);
        }
        scrollBar(g, x + pw - 12, ly, ROWS * ROW_H - 3, list.size(), ROWS, a);

        int fy = footY();
        if (myRole != null && myRole.canInvite()) {
            g.fillGradient(x + 12, fy, x + 172, fy + 16, alpha(0xFFFFFFFF, a), alpha(0xFFF0F0F3, a));
            g.fill(x + 12, fy + 15, x + 172, fy + 16, alpha(ORANGE, a));
            invite.setX(x + 16);
            invite.setY(fy + 4);
            invite.render(g, mouseX, mouseY, 0);
            button(g, x + 176, fy, 70, 16, key("btn_invite"), true, mouseX, mouseY, a);
        }
        softButton(g, x + pw - 112, fy, 100, 16, key(leader ? "btn_disband" : "btn_leave"), false, mouseX, mouseY, a);
    }

    private boolean canManage() {
        UUID me = me();
        GuildRole r = guild.getMemberRole(me);
        return guild.getLeaderUuid().equals(me) || (r != null && r.canManagePerks());
    }

    private void blessings(GuiGraphics g, int x, int mouseX, int mouseY, float a) {
        GuildPerk[] perks = GuildPerk.values();
        int rows = 6, rh = 25;
        scroll = Math.min(scroll, Math.max(0, perks.length - rows));
        int ly = listY(), rw = pw - 28;
        for (int r = 0; r < rows && scroll + r < perks.length; r++) {
            GuildPerk p = perks[scroll + r];
            int ry = ly + r * rh;
            boolean have = guild.hasPerk(p);
            card(g, x + 12, ry, rw, rh - 3, have, a);
            g.drawString(this.font, p.getIcon() + " " + plain(p.getTitleComponent()), x + 18, ry + 3, alpha(have ? GREY_DARK : GREY, a), false);
            g.pose().pushPose();
            g.pose().translate(x + 18, ry + 13, 0);
            g.pose().scale(0.75F, 0.75F, 1);
            g.drawString(this.font, this.font.plainSubstrByWidth(plain(p.getDescriptionComponent()), (int) ((rw - 120) / 0.75F)), 0, 0, alpha(GREY, a), false);
            g.pose().popPose();
            int bx = x + rw - 92;
            if (have) softButton(g, bx, ry + 4, 90, 14, Component.literal(plain(key("perk.active"))), true, mouseX, mouseY, a);
            else if (guild.getLevel() < p.getRequiredGuildLevel()) softButton(g, bx, ry + 4, 90, 14, Component.literal(plain(key("perk.req_level", p.getRequiredGuildLevel()))), false, mouseX, mouseY, a);
            else button(g, bx, ry + 4, 90, 14, Component.literal(String.format(Locale.US, "%,d ⛃", p.getCostCoins())), canManage(), mouseX, mouseY, a);
        }
        scrollBar(g, x + pw - 12, ly, rows * rh - 3, perks.length, rows, a);
    }

    private static final int[] COINS = {50, 250, 1000, 10000, 50000};

    private void treasury(GuiGraphics g, int x, int mouseX, int mouseY, float a) {
        int ly = listY(), rw = pw - 28;
        GuildRole r = guild.getMemberRole(me());
        boolean withdraw = r != null && r.canWithdraw();
        // vaults
        card(g, x + 12, ly, rw, 36, false, a);
        g.drawString(this.font, plain(key("tab.treasury")), x + 18, ly + 4, alpha(GREY, a), false);
        g.drawString(this.font, String.format(Locale.US, "%,d ⛃", guild.getCoinVault()), x + 18, ly + 18, alpha(GREY_DARK, a), false);
        g.drawString(this.font, String.format(Locale.US, "%,d Mora", guild.getMoraVault()), x + 150, ly + 18, alpha(GREY_DARK, a), false);
        // coins in and out
        int by = ly + 44;
        g.drawString(this.font, tr("bank_in", "Put in").getString(), x + 14, by + 3, alpha(0xFF4FA84A, a), false);
        for (int i = 0; i < COINS.length; i++) button(g, x + 70 + i * 52, by, 48, 14, Component.literal(String.format(Locale.US, "%,d", COINS[i])), true, mouseX, mouseY, a);
        by += 18;
        g.drawString(this.font, tr("bank_out", "Take out").getString(), x + 14, by + 3, alpha(ORANGE_DARK, a), false);
        if (withdraw) {
            for (int i = 0; i < COINS.length; i++) softButton(g, x + 70 + i * 52, by, 48, 14, Component.literal(String.format(Locale.US, "%,d", COINS[i])), false, mouseX, mouseY, a);
            softButton(g, x + 70 + 5 * 52, by, 56, 14, tr("all", "All"), false, mouseX, mouseY, a);
        } else {
            softButton(g, x + 70, by, 200, 14, tr("guild_request", "Ask for a withdrawal"), false, mouseX, mouseY, a);
        }
        // headquarters and territory
        by += 28;
        g.drawString(this.font, guild.hasHq() ? String.format(Locale.US, "HQ  %.0f, %.0f, %.0f", guild.getHqX(), guild.getHqY(), guild.getHqZ()) : "HQ  —",
                x + 14, by, alpha(GREY_DARK, a), false);
        String claims = guild.getClaimedChunks().size() + " / " + guild.getMaxClaims();
        g.drawString(this.font, claims, x + pw - 14 - this.font.width(claims), by, alpha(GREY, a), false);
        by += 14;
        boolean leader = guild.getLeaderUuid().equals(me());
        boolean claim = r != null && r.canClaimTerritory();
        button(g, x + 12, by, 74, 16, tr("guild_home", "Go to HQ"), guild.hasHq(), mouseX, mouseY, a);
        if (leader) softButton(g, x + 90, by, 74, 16, tr("guild_set_home", "Set HQ here"), false, mouseX, mouseY, a);
        softButton(g, x + 168, by, 74, 16, tr("guild_map", "Map"), false, mouseX, mouseY, a);
        if (claim) {
            softButton(g, x + 246, by, 74, 16, tr("guild_claim", "Claim chunk"), false, mouseX, mouseY, a);
            softButton(g, x + 324, by, 74, 16, tr("guild_unclaim", "Free chunk"), false, mouseX, mouseY, a);
        }
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        if (guild == null) {
            if (in(mx, my, x + pw / 2 - 80, y + 110, 160, 18)) { classic(0); return true; }
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (in(mx, my, tabX(i), tabY(), tabW(), 15)) {
                if (i == 3) { classic(3); return true; }   // hunts, portals and alliances live in the normal window
                tab = i;
                scroll = 0;
                SaoSounds.click();
                return true;
            }
        }
        UUID me = me();
        boolean leader = guild.getLeaderUuid().equals(me);
        GuildRole role = guild.getMemberRole(me);
        int ly = listY(), rw = pw - 28;
        if (tab == 0) {
            Minecraft mc = Minecraft.getInstance();
            List<GuildMember> list = memberList();
            for (int r = 0; r < ROWS && scroll + r < list.size(); r++) {
                GuildMember m = list.get(scroll + r);
                int ry = ly + r * ROW_H, bx = x + rw - 90;
                String id = m.getUuid().toString();
                if (leader && !m.getUuid().equals(me)) {
                    if (in(mx, my, bx, ry + 2, 18, 13)) { act("PROMOTE", id, 0); SaoSounds.confirm(); return true; }
                    if (in(mx, my, bx + 20, ry + 2, 18, 13)) { act("DEMOTE", id, 0); SaoSounds.click(); return true; }
                    if (in(mx, my, bx + 40, ry + 2, 18, 13)) { act("KICK", id, 0); SaoSounds.back(); return true; }
                }
                boolean online = m.isNpc() || (mc.getConnection() != null && mc.getConnection().getPlayerInfo(m.getUuid()) != null);
                if (!m.getUuid().equals(me) && online && in(mx, my, bx + 62, ry + 2, 24, 13)) { act("TPA", id, 0); SaoSounds.confirm(); return true; }
            }
            int fy = footY();
            if (role != null && role.canInvite()) {
                if (in(mx, my, x + 12, fy, 160, 16)) { invite.setFocused(true); return true; }
                if (in(mx, my, x + 176, fy, 70, 16) && !invite.getValue().trim().isEmpty()) {
                    act("INVITE", invite.getValue().trim(), 0);
                    invite.setValue("");
                    SaoSounds.confirm();
                    return true;
                }
            }
            if (in(mx, my, x + pw - 112, fy, 100, 16)) { act(leader ? "DISBAND" : "LEAVE", "", 0); SaoSounds.back(); onClose(); return true; }
        } else if (tab == 1) {
            GuildPerk[] perks = GuildPerk.values();
            for (int r = 0; r < 6 && scroll + r < perks.length; r++) {
                GuildPerk p = perks[scroll + r];
                if (in(mx, my, x + rw - 92, ly + r * 25 + 4, 90, 14) && canManage() && !guild.hasPerk(p) && guild.getLevel() >= p.getRequiredGuildLevel()) {
                    act("PERK", p.getId(), 0);
                    SaoSounds.confirm();
                    return true;
                }
            }
        } else {
            int by = ly + 44;
            for (int i = 0; i < COINS.length; i++) if (in(mx, my, x + 70 + i * 52, by, 48, 14)) { act("DEPOSIT_COINS", "", COINS[i]); SaoSounds.click(); return true; }
            by += 18;
            boolean withdraw = role != null && role.canWithdraw();
            if (withdraw) {
                for (int i = 0; i < COINS.length; i++) if (in(mx, my, x + 70 + i * 52, by, 48, 14)) { act("WITHDRAW_COINS", "", COINS[i]); SaoSounds.click(); return true; }
                if (in(mx, my, x + 70 + 5 * 52, by, 56, 14)) { if (guild.getCoinVault() > 0) act("WITHDRAW_ALL_COINS", "", 0); SaoSounds.click(); return true; }
            } else if (in(mx, my, x + 70, by, 200, 14)) { act("REQUEST_WITHDRAW", "COINS", 0); SaoSounds.click(); return true; }
            by += 42;
            if (in(mx, my, x + 12, by, 74, 16) && guild.hasHq()) { act("HOME", "", 0); SaoSounds.confirm(); onClose(); return true; }
            if (leader && in(mx, my, x + 90, by, 74, 16)) { act("SET_HOME", "", 0); SaoSounds.confirm(); return true; }
            if (in(mx, my, x + 168, by, 74, 16)) {
                CompoundTag claims = raw.contains("AllClaims") ? raw.getCompound("AllClaims") : new CompoundTag();
                Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.guild.GuildChunkMapScreen(guild, claims, this));
                SaoSounds.click();
                return true;
            }
            if (role != null && role.canClaimTerritory()) {
                if (in(mx, my, x + 246, by, 74, 16)) { act("CLAIM_CHUNK", "", 0); SaoSounds.confirm(); return true; }
                if (in(mx, my, x + 324, by, 74, 16)) { act("UNCLAIM_CHUNK", "", 0); SaoSounds.click(); return true; }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (invite != null && invite.isFocused()) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) { invite.setFocused(false); return true; }
            if (invite.keyPressed(key, scan, mods)) return true;
            if (key != org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (invite != null && invite.isFocused() && invite.charTyped(c, mods)) return true;
        return super.charTyped(c, mods);
    }
}
