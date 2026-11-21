package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Locale;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * Murim cultivation in the full-dive look: the realm and Qi on top with meditation, the physique and the
 * breakthrough; below, the twelve meridians to open and the Dao skills to choose and use. The same actions as
 * the normal cultivation window.
 */
public class SaoCultivationScreen extends SaoWindow {
    /** Dao skills: id, realm needed, Qi cost, cooldown in seconds (as the normal window lists them). */
    private static final int[][] DAO = {{0, 1, 20, 7}, {1, 2, 35, 10}, {2, 3, 50, 18}, {3, 4, 75, 25},
            {4, 5, 45, 8}, {5, 3, 30, 12}, {6, 4, 65, 20}, {7, 5, 80, 30}};
    private int tab = 0;
    private int meridian = 0;

    public SaoCultivationScreen(Screen parent) {
        super(parent, 450, 244);
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.screen.cultivation." + k, args);
    }

    private static String plain(Component c) {
        return c.getString().replaceAll("§.", "");
    }

    @Override
    protected Component heading() {
        return tr("cultivation", "Cultivation");
    }

    @Override
    protected String[] glyph() {
        return FIST;
    }

    @Override
    protected String headerRight() {
        SystemData d = data();
        return plain(key("realm_sublevel", SystemEvents.getDantianRealmComponent(d.dantianRealm()), d.cultivationSubLevel(), 9));
    }

    // layout
    private int cardY() { return top() + HEADER + 6; }
    private int actW() { return 128; }
    private int actX() { return left() + pw - 12 - actW(); }
    private int tabY() { return top() + HEADER + 54; }
    private int bodyY() { return top() + HEADER + 74; }

    private int breakthroughState(SystemData d) {
        // 0 sub-level ready/not, 1 realm ready/not, 2 max
        return d.cultivationSubLevel() < 9 ? 0 : d.dantianRealm() < 14 ? 1 : 2;
    }

    private boolean canBreak(SystemData d) {
        int qi = d.cultivationQi(), max = d.maxCultivationQi(), realm = d.dantianRealm();
        return switch (breakthroughState(d)) {
            case 0 -> qi >= max;
            case 1 -> {
                int reqM = Math.min(12, (realm + 1) * 2);
                int reqQi = switch (realm + 1) { case 10 -> 2500; case 11 -> 4000; case 12 -> 6000; case 13 -> 8500; case 14 -> 12000; default -> (realm + 1) * 150; };
                yield d.getOpenedMeridiansCount() >= reqM && qi >= reqQi;
            }
            default -> false;
        };
    }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int cy = cardY();
        // Qi
        int qi = d.cultivationQi(), max = Math.max(1, d.maxCultivationQi());
        float density = this.minecraft != null && this.minecraft.player != null ? SystemEvents.getZoneQiDensity(this.minecraft.player) : 1;
        g.drawString(this.font, "Qi  " + qi + " / " + max, x + 14, cy + 2, alpha(GREY_DARK, a), false);
        // the bar ends before the Meditate button
        bar(g, x + 14, cy + 13, actX() - actW() - 4 - 10 - (x + 14), 6, qi / (float) max, 0xFF3E9BD6, a);
        String info = String.format(Locale.US, "%.1fx", density) + "  ·  " + tr("impurities", "Impurities %s%%", d.bodyImpurities()).getString();
        g.drawString(this.font, info, x + 14, cy + 24, alpha(GREY, a), false);
        String phys = d.murimPhysique();
        if (phys != null && !phys.isEmpty())
            g.drawString(this.font, plain(Component.translatable("awakening_system.physique." + phys + ".short")), x + 14, cy + 35, alpha(0xFF9B4FD6, a), false);

        boolean med = this.minecraft != null && this.minecraft.player != null && this.minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity;
        int ax = actX(), aw = actW();
        softButton(g, ax - aw - 4, cy, aw, 16, Component.literal(plain(key(med ? "meditating" : "start_meditation_short"))), med, mouseX, mouseY, a);
        boolean noPhys = phys == null || phys.isEmpty();
        if (noPhys) button(g, ax, cy, aw, 16, Component.literal(plain(key("awaken_physique"))), true, mouseX, mouseY, a);
        else softButton(g, ax, cy, aw, 16, Component.literal(plain(Component.translatable("awakening_system.physique." + phys + ".short"))), false, mouseX, mouseY, a);
        Component brk = switch (breakthroughState(d)) {
            case 0 -> key("breakthrough_sublevel", d.cultivationSubLevel() + 1);
            case 1 -> key("breakthrough");
            default -> key("max_realm");
        };
        button(g, ax, cy + 20, aw, 16, Component.literal(plain(brk)), canBreak(d), mouseX, mouseY, a);

        // tabs
        int tw = (pw - 24 - 3) / 2;
        softButton(g, x + 12, tabY(), tw, 15, Component.literal(plain(key("tab_meridians"))), tab == 0, mouseX, mouseY, a);
        softButton(g, x + 15 + tw, tabY(), tw, 15, Component.literal(plain(key("tab_dao"))), tab == 1, mouseX, mouseY, a);

        if (tab == 0) meridians(g, d, x, mouseX, mouseY, a);
        else dao(g, d, x, mouseX, mouseY, a);
    }

    private int pillX(int i) { return left() + 12 + (i % 2) * 92; }
    private int pillY(int i) { return bodyY() + (i / 2) * 19; }

    private void meridians(GuiGraphics g, SystemData d, int x, int mouseX, int mouseY, float a) {
        for (int i = 0; i < 12; i++) {
            boolean open = d.isMeridianOpen(i);
            boolean hot = in(mouseX, mouseY, pillX(i), pillY(i), 88, 17) || i == meridian;
            card(g, pillX(i), pillY(i), 88, 17, hot, a);
            disc(g, pillX(i) + 9, pillY(i) + 8.5F, 3.0F, alpha(open ? ORANGE : 0xFFC9C9CE, a));
            String n = plain(Component.translatable("awakening_system.screen.cultivation.meridian_short_" + i));
            g.drawString(this.font, this.font.plainSubstrByWidth(n, 70), pillX(i) + 16, pillY(i) + 5, alpha(open ? GREY_DARK : GREY, a), false);
        }
        int dx = x + 200, dy = bodyY(), dw = pw - 212, dh = ph - (dy - top()) - 10;
        g.fillGradient(dx, dy, dx + dw, dy + dh, alpha(0xFAFFFFFF, a), alpha(0xF0ECECF0, a));
        g.fill(dx, dy, dx + dw, dy + 2, alpha(ORANGE, a));
        int ty = dy + 7;
        g.drawString(this.font, Component.translatable("awakening_system.meridian." + meridian + ".name"), dx + 8, ty, alpha(GREY_DARK, a), false);
        ty += 11;
        g.drawString(this.font, plain(Component.translatable("awakening_system.meridian." + meridian + ".organ")), dx + 8, ty, alpha(ORANGE_DARK, a), false);
        ty += 13;
        for (FormattedCharSequence l : this.font.split(Component.translatable("awakening_system.meridian." + meridian + ".effect"), dw - 16)) {
            if (ty > dy + dh - 34) break;
            g.drawString(this.font, l, dx + 8, ty, alpha(GREY, a), false);
            ty += 9;
        }
        boolean open = d.isMeridianOpen(meridian);
        int cost = (meridian + 1) * 35;
        Component label = open ? tr("meridian_open", "Open") : tr("meridian_open_cost", "Open (%s Qi)", cost);
        button(g, dx + 8, dy + dh - 24, dw - 16, 18, label, !open && d.cultivationQi() >= cost, mouseX, mouseY, a);
    }

    private int daoY(int r) { return bodyY() + r * 26; }

    private void dao(GuiGraphics g, SystemData d, int x, int mouseX, int mouseY, float a) {
        int rows = 5;
        scroll = Math.min(scroll, DAO.length - rows);
        for (int r = 0; r < rows && scroll + r < DAO.length; r++) {
            int[] s = DAO[scroll + r];
            int ry = daoY(r), rw = pw - 28;
            boolean ok = d.dantianRealm() >= s[1];
            boolean chosen = d.selectedDaoSkill() == s[0];
            card(g, x + 12, ry, rw, 23, chosen, a);
            g.drawString(this.font, Component.translatable("awakening_system.dao_skill." + s[0] + ".name"), x + 18, ry + 3, alpha(ok ? GREY_DARK : GREY_LIGHT, a), false);
            String meta = ok ? s[2] + " Qi  ·  " + s[3] + "s" : plain(key("dao_req_realm", SystemEvents.getDantianRealmComponent(s[1])));
            g.pose().pushPose();
            g.pose().translate(x + 18, ry + 13.5F, 0);
            g.pose().scale(0.75F, 0.75F, 1);
            g.drawString(this.font, meta, 0, 0, alpha(GREY, a), false);
            g.pose().popPose();
            if (ok) {
                softButton(g, x + rw - 104, ry + 4, 56, 15, Component.literal(plain(key(chosen ? "dao_assigned" : "dao_assign"))), chosen, mouseX, mouseY, a);
                button(g, x + rw - 44, ry + 4, 50, 15, Component.literal(plain(key("dao_use"))), true, mouseX, mouseY, a);
            }
        }
        scrollBar(g, x + pw - 12, bodyY(), rows * 26 - 3, DAO.length, rows, a);
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        SystemData d = data();
        int cy = cardY(), ax = actX(), aw = actW();
        if (in(mx, my, ax - aw - 4, cy, aw, 16)) { AwakeningSystemClient.performCultivationAction("toggle_meditation", 0); SaoSounds.click(); return true; }
        if (in(mx, my, ax, cy, aw, 16)) {
            String phys = d.murimPhysique();
            if (phys == null || phys.isEmpty()) { AwakeningSystemClient.performCultivationAction("awaken_physique", 0); SaoSounds.confirm(); }
            return true;
        }
        if (in(mx, my, ax, cy + 20, aw, 16)) {
            if (canBreak(d)) {
                AwakeningSystemClient.performCultivationAction(breakthroughState(d) == 0 ? "breakthrough_sublevel" : "advance_realm", 0);
                SaoSounds.confirm();
            }
            return true;
        }
        int tw = (pw - 24 - 3) / 2;
        if (in(mx, my, x + 12, tabY(), tw, 15)) { tab = 0; SaoSounds.click(); return true; }
        if (in(mx, my, x + 15 + tw, tabY(), tw, 15)) { tab = 1; scroll = 0; SaoSounds.click(); return true; }
        if (tab == 0) {
            for (int i = 0; i < 12; i++) if (in(mx, my, pillX(i), pillY(i), 88, 17)) { meridian = i; SaoSounds.click(); return true; }
            int dx = x + 200, dy = bodyY(), dw = pw - 212, dh = ph - (dy - top()) - 10;
            if (in(mx, my, dx + 8, dy + dh - 24, dw - 16, 18)) {
                if (!d.isMeridianOpen(meridian) && d.cultivationQi() >= (meridian + 1) * 35) {
                    AwakeningSystemClient.performCultivationAction("open_meridian", meridian);
                    SaoSounds.confirm();
                }
                return true;
            }
        } else {
            int rw = pw - 28;
            for (int r = 0; r < 5 && scroll + r < DAO.length; r++) {
                int[] s = DAO[scroll + r];
                if (d.dantianRealm() < s[1]) continue;
                int ry = daoY(r);
                if (in(mx, my, x + rw - 104, ry + 4, 56, 15)) {
                    d.setSelectedDaoSkill(s[0]);
                    AwakeningSystemClient.performCultivationAction("select_dao_skill", s[0]);
                    SaoSounds.click();
                    return true;
                }
                if (in(mx, my, x + rw - 44, ry + 4, 50, 15)) {
                    AwakeningSystemClient.performCultivationAction("use_dao_skill", s[0]);
                    SaoSounds.confirm();
                    onClose();
                    return true;
                }
            }
        }
        return false;
    }
}
