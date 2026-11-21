package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.fable.FableDefinitions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/** The fables in the full-dive look: filters by grade, the list on the left and the chosen fable on the right. */
public class SaoFablesScreen extends SaoWindow {
    private static final FableDefinitions.Grade[] GRADES = {null, FableDefinitions.Grade.HISTORICAL, FableDefinitions.Grade.LEGENDARY,
            FableDefinitions.Grade.SEMI_MYTH, FableDefinitions.Grade.MYTH, FableDefinitions.Grade.GIANT_STORY};
    private static final String[] FILTER_KEYS = {"all", "historical", "legendary", "semi_myth", "myth", "giant"};
    private static final int ROWS = 8, ROW_H = 19, LIST_W = 168;

    private int filter = 0;
    private String chosen = "";

    public SaoFablesScreen(Screen parent) {
        super(parent, 380, 236);
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.fables_screen." + k, args);
    }

    @Override
    protected Component heading() {
        return tr("fables", "Fables");
    }

    @Override
    protected String[] glyph() {
        return SCROLL;
    }

    @Override
    protected String headerRight() {
        SystemData d = data();
        return d.unlockedFables().size() + " · " + key("weight", d.fableWeight()).getString().replaceAll("§.", "");
    }

    private List<FableDefinitions.Fable> fables() {
        List<FableDefinitions.Fable> out = new ArrayList<>();
        for (FableDefinitions.Fable f : FableDefinitions.getAll())
            if (GRADES[filter] == null || f.grade() == GRADES[filter]) out.add(f);
        // the ones written first, then the rest
        SystemData d = data();
        out.sort((p, q) -> Boolean.compare(!d.unlockedFables().contains(p.id()), !d.unlockedFables().contains(q.id())));
        return out;
    }

    private int filterW() { return (pw - 24 - 5 * 3) / 6; }
    private int filterX(int i) { return left() + 12 + i * (filterW() + 3); }
    private int filterY() { return top() + HEADER + 8; }
    private int listY() { return top() + HEADER + 30; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        for (int i = 0; i < GRADES.length; i++)
            softButton(g, filterX(i), filterY(), filterW(), 15, key("filter." + FILTER_KEYS[i]), filter == i, mouseX, mouseY, a);

        List<FableDefinitions.Fable> list = fables();
        scroll = Math.min(scroll, Math.max(0, list.size() - ROWS));
        int ly = listY();
        if (list.isEmpty()) g.drawString(this.font, key("no_fables"), x + 16, ly + 8, alpha(GREY, a), false);
        for (int r = 0; r < ROWS && scroll + r < list.size(); r++) {
            FableDefinitions.Fable f = list.get(scroll + r);
            int ry = ly + r * ROW_H;
            boolean have = d.unlockedFables().contains(f.id());
            boolean hot = in(mouseX, mouseY, x + 12, ry, LIST_W, ROW_H - 2) || f.id().equals(chosen);
            card(g, x + 12, ry, LIST_W, ROW_H - 2, hot, a);
            disc(g, x + 21, ry + 8.5F, 3.0F, alpha(have ? 0xFF000000 | f.grade().hexColor() : 0xFFC9C9CE, a));
            String t = this.font.plainSubstrByWidth(f.getTitleComponent().getString(), LIST_W - 26);
            g.drawString(this.font, t, x + 28, ry + 5, alpha(have ? GREY_DARK : GREY_LIGHT, a), false);
        }
        scrollBar(g, x + 12 + LIST_W + 3, ly, ROWS * ROW_H - 2, list.size(), ROWS, a);

        // the chosen fable
        int dx = x + LIST_W + 22, dw = pw - LIST_W - 34, dy = ly;
        g.fillGradient(dx, dy, dx + dw, dy + ROWS * ROW_H - 2, alpha(0xFAFFFFFF, a), alpha(0xF0ECECF0, a));
        FableDefinitions.Fable f = FableDefinitions.get(chosen);
        if (f == null) {
            int ty = dy + 30;
            for (FormattedCharSequence line : this.font.split(key("empty_desc1"), dw - 16)) {
                g.drawString(this.font, line, dx + 8, ty, alpha(GREY, a), false);
                ty += 10;
            }
            return;
        }
        boolean have = d.unlockedFables().contains(f.id());
        int col = 0xFF000000 | f.grade().hexColor();
        g.fill(dx, dy, dx + dw, dy + 2, alpha(col, a));
        int cy = dy + 7;
        g.drawString(this.font, f.grade().getDisplayName().getString().toUpperCase(java.util.Locale.ROOT), dx + 8, cy, alpha(shade(col, -0.2F), a), false);
        String w = key("weight", f.weight()).getString().replaceAll("§.", "");
        g.drawString(this.font, w, dx + dw - 8 - this.font.width(w), cy, alpha(ORANGE_DARK, a), false);
        cy += 13;
        for (FormattedCharSequence line : this.font.split(f.getTitleComponent(), dw - 16)) {
            g.drawString(this.font, line, dx + 8, cy, alpha(GREY_DARK, a), false);
            cy += 10;
        }
        cy += 3;
        if (f.isWorldFirst()) {
            g.drawString(this.font, key("world_first_title"), dx + 8, cy, alpha(ORANGE_DARK, a), false);
            cy += 12;
        }
        g.fill(dx + 8, cy, dx + dw - 8, cy + 1, alpha(0xFFDADADE, a));
        cy += 5;
        int bottom = dy + ROWS * ROW_H - 22;
        for (FormattedCharSequence line : this.font.split(f.getDescriptionComponent(), dw - 16)) {
            if (cy > bottom - 10) break;
            g.drawString(this.font, line, dx + 8, cy, alpha(GREY, a), false);
            cy += 10;
        }
        Component state = key(have ? "soul_engraved" : "unwritten");
        int sw = this.font.width(state) + 14;
        g.fillGradient(dx + 8, bottom + 2, dx + 8 + sw, bottom + 16, alpha(have ? 0xFFF7B52A : 0xFFE2E2E6, a), alpha(have ? 0xFFE29400 : 0xFFD0D0D6, a));
        g.drawString(this.font, state, dx + 15, bottom + 5, alpha(have ? WHITE : GREY, a), false);
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        for (int i = 0; i < GRADES.length; i++) {
            if (in(mx, my, filterX(i), filterY(), filterW(), 15)) {
                filter = i;
                scroll = 0;
                SaoSounds.click();
                return true;
            }
        }
        List<FableDefinitions.Fable> list = fables();
        int ly = listY();
        for (int r = 0; r < ROWS && scroll + r < list.size(); r++) {
            if (in(mx, my, x + 12, ly + r * ROW_H, LIST_W, ROW_H - 2)) {
                chosen = list.get(scroll + r).id();
                SaoSounds.click();
                return true;
            }
        }
        return false;
    }
}
