package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.ClassSelectionScreen;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SelectHunterClassPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * Choosing a hunter class in the full-dive look: the classes by family on the left, the chosen one's story,
 * signature skill and perks on the right, and a confirm step before it is sealed (a class is for good).
 */
public class SaoClassChoiceScreen extends SaoWindow {
    private static final String[] TABS = {"tab_warriors", "tab_arcane", "tab_murim", "tab_all"};
    private static final int LIST_W = 140, ROW_H = 19;
    private int family = 3;
    private int picked = 0;
    private boolean confirming;

    public SaoClassChoiceScreen(Screen parent) {
        super(parent, 420, 240);
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.screen.class_select." + k, args);
    }

    @Override
    protected Component heading() {
        return tr("class_pick", "Choose a class");
    }

    @Override
    protected String[] glyph() {
        return STAR;
    }

    private List<ClassSelectionScreen.ClassOption> shown() {
        return family == 3 ? ClassSelectionScreen.classes()
                : ClassSelectionScreen.classes().stream().filter(c -> c.category() == family).toList();
    }

    private ClassSelectionScreen.ClassOption current() {
        List<ClassSelectionScreen.ClassOption> l = shown();
        return l.get(Math.max(0, Math.min(picked, l.size() - 1)));
    }

    private int tabW() { return (pw - 24 - 3 * 3) / 4; }
    private int tabX(int i) { return left() + 12 + i * (tabW() + 3); }
    private int tabY() { return top() + HEADER + 8; }
    private int listY() { return top() + HEADER + 30; }
    private int btnY() { return top() + ph - 26; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        for (int i = 0; i < 4; i++) softButton(g, tabX(i), tabY(), tabW(), 15, key(TABS[i]), family == i, mouseX, mouseY, a);
        List<ClassSelectionScreen.ClassOption> list = shown();
        int ly = listY(), rows = 9;
        scroll = Math.min(scroll, Math.max(0, list.size() - rows));
        for (int r = 0; r < rows && scroll + r < list.size(); r++) {
            var c = list.get(scroll + r);
            int ry = ly + r * ROW_H;
            boolean on = scroll + r == picked;
            card(g, x + 12, ry, LIST_W, ROW_H - 2, on || in(mouseX, mouseY, x + 12, ry, LIST_W, ROW_H - 2), a);
            disc(g, x + 21, ry + 8.5F, 3.2F, alpha(c.color(), a));
            g.drawString(this.font, this.font.plainSubstrByWidth(c.getName().getString(), LIST_W - 26), x + 28, ry + 5, alpha(on ? GREY_DARK : GREY, a), false);
        }
        scrollBar(g, x + 12 + LIST_W + 3, ly, rows * ROW_H - 2, list.size(), rows, a);

        var c = current();
        int dx = x + LIST_W + 22, dw = pw - LIST_W - 34, dy = ly, dh = btnY() - ly - 6;
        g.fillGradient(dx, dy, dx + dw, dy + dh, alpha(0xFAFFFFFF, a), alpha(0xF0ECECF0, a));
        g.fill(dx, dy, dx + dw, dy + 2, alpha(c.color(), a));
        int ty = dy + 7;
        g.drawString(this.font, c.getName(), dx + 8, ty, alpha(GREY_DARK, a), false);
        String src = c.getShortManhwa().getString();
        g.drawString(this.font, src, dx + dw - 8 - this.font.width(src), ty, alpha(GREY_LIGHT, a), false);
        ty += 11;
        g.drawString(this.font, this.font.plainSubstrByWidth(c.getRoleTag().getString(), dw - 16), dx + 8, ty, alpha(ORANGE_DARK, a), false);
        ty += 12;
        for (FormattedCharSequence l : this.font.split(c.getDescription(), dw - 16)) {
            if (ty > dy + 70) break;
            g.drawString(this.font, l, dx + 8, ty, alpha(GREY, a), false);
            ty += 9;
        }
        ty += 3;
        g.drawString(this.font, this.font.plainSubstrByWidth(Component.translatable("awakening_system.class.signature_skill_header", c.getSignatureSkillName()).getString(), dw - 16),
                dx + 8, ty, alpha(GREY_DARK, a), false);
        ty += 12;
        g.drawString(this.font, key("perks_header").getString().replaceAll("§.", ""), dx + 8, ty, alpha(ORANGE_DARK, a), false);
        ty += 10;
        for (Component p : c.getPerks()) {
            for (FormattedCharSequence l : this.font.split(Component.literal("• ").append(p), dw - 16)) {
                if (ty > dy + dh - 9) break;
                g.drawString(this.font, l, dx + 8, ty, alpha(GREY, a), false);
                ty += 9;
            }
        }

        int by = btnY();
        if (d.hasHunterClass()) {
            Component name = Component.translatable("awakening_system.class." + d.hunterClass() + ".name");
            button(g, dx, by, dw, 18, Component.literal(key("sealed_label", name).getString().replaceAll("§.", "")), false, mouseX, mouseY, a);
        } else if (confirming) {
            g.drawString(this.font, this.font.plainSubstrByWidth(key("confirm_warning_1").getString().replaceAll("§.", ""), dw - 140), dx, by + 5, alpha(0xFFD6473E, a), false);
            button(g, dx + dw - 132, by, 64, 18, key("confirm_yes"), true, mouseX, mouseY, a);
            softButton(g, dx + dw - 64, by, 64, 18, key("confirm_no"), false, mouseX, mouseY, a);
        } else {
            button(g, dx, by, dw, 18, key("confirm_btn"), true, mouseX, mouseY, a);
        }
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        for (int i = 0; i < 4; i++) {
            if (in(mx, my, tabX(i), tabY(), tabW(), 15)) { family = i; picked = 0; scroll = 0; confirming = false; SaoSounds.click(); return true; }
        }
        List<ClassSelectionScreen.ClassOption> list = shown();
        int ly = listY();
        for (int r = 0; r < 9 && scroll + r < list.size(); r++) {
            if (in(mx, my, x + 12, ly + r * ROW_H, LIST_W, ROW_H - 2)) { picked = scroll + r; confirming = false; SaoSounds.click(); return true; }
        }
        if (data().hasHunterClass()) return false;
        int dx = x + LIST_W + 22, dw = pw - LIST_W - 34, by = btnY();
        if (confirming) {
            if (in(mx, my, dx + dw - 132, by, 64, 18)) {
                PacketDistributor.sendToServer(new SelectHunterClassPayload(current().id()));
                SaoSounds.confirm();
                onClose();
                return true;
            }
            if (in(mx, my, dx + dw - 64, by, 64, 18)) { confirming = false; SaoSounds.back(); return true; }
        } else if (in(mx, my, dx, by, dw, 18)) {
            confirming = true;
            SaoSounds.click();
            return true;
        }
        return false;
    }
}
