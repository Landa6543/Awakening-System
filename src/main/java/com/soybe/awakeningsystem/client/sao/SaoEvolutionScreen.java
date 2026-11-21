package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.ClassEvolution;
import com.soybe.awakeningsystem.ClassSelectionScreen;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * Class evolution in the full-dive look: what is still needed (the level and the trial, with the button that
 * calls the trial boss) and one card per evolution of the base class, each with its button to evolve.
 */
public class SaoEvolutionScreen extends SaoWindow {
    public SaoEvolutionScreen(Screen parent) {
        super(parent, 430, 244);
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.evolution." + k, args);
    }

    @Override
    protected Component heading() {
        return key("title");
    }

    @Override
    protected String[] glyph() {
        return STAR;
    }

    @Override
    protected String headerRight() {
        SystemData d = data();
        return d.hasHunterClass() ? Component.translatable("awakening_system.class." + d.hunterClass() + ".name").getString() : null;
    }

    private int reqY() { return top() + HEADER + 8; }
    private int cardsY() { return reqY() + 46; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        ClassEvolution.Base base = ClassEvolution.base(d.hunterClass());
        int cx = x + 12, cw = pw - 24;
        if (base == null) {
            Component msg = d.hasHunterClass() ? key("already") : key("no_class");
            int ty = y + ph / 2 - 10;
            for (FormattedCharSequence l : this.font.split(msg, cw - 20)) {
                g.drawCenteredString(this.font, l, x + pw / 2, ty, alpha(GREY, a));
                ty += 10;
            }
            return;
        }
        // what is still needed
        int ry = reqY();
        card(g, cx, ry, cw, 38, false, a);
        boolean lvlOk = d.level() >= ClassEvolution.EVOLVE_LEVEL;
        g.drawString(this.font, (lvlOk ? "✔ " : "✘ ") + key("req_level", d.level(), ClassEvolution.EVOLVE_LEVEL).getString(), cx + 8, ry + 6,
                alpha(lvlOk ? 0xFF4FA84A : 0xFFD6473E, a), false);
        boolean won = d.evolutionTrialPassed();
        g.drawString(this.font, (won ? "✔ " : "✘ ") + key(won ? "req_trial_won" : "req_trial").getString(), cx + 8, ry + 20,
                alpha(won ? 0xFF4FA84A : 0xFFD6473E, a), false);
        if (!won) button(g, cx + cw - 118, ry + 10, 110, 18, key("trial_button"), lvlOk, mouseX, mouseY, a);

        // the evolutions
        List<String> evo = base.evolutions();
        int gap = 8, n = evo.size();
        int w = (cw - gap * (n - 1)) / n, top = cardsY(), h = y + ph - 10 - top;
        boolean can = ClassEvolution.canEvolve(d);
        for (int i = 0; i < n; i++) {
            int bx = cx + i * (w + gap);
            ClassSelectionScreen.ClassOption c = ClassSelectionScreen.advanced(evo.get(i));
            boolean hot = in(mouseX, mouseY, bx, top, w, h);
            card(g, bx, top, w, h, hot, a);
            int ty = top + 6;
            g.fill(bx + 8, ty + 1, bx + 14, ty + 7, alpha(c == null ? ORANGE : c.color(), a));
            Component name = c == null ? Component.literal(evo.get(i)) : c.getName();
            g.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), w - 26), bx + 18, ty, alpha(GREY_DARK, a), false);
            ty += 12;
            if (c != null) {
                g.drawString(this.font, this.font.plainSubstrByWidth(c.getRoleTag().getString().replaceAll("§.", ""), w - 16), bx + 8, ty, alpha(ORANGE_DARK, a), false);
                ty += 12;
                for (FormattedCharSequence l : this.font.split(Component.literal(c.getDescription().getString().replaceAll("§.", "")), w - 16)) {
                    if (ty > top + h - 34) break;
                    g.drawString(this.font, l, bx + 8, ty, alpha(GREY, a), false);
                    ty += 9;
                }
            }
            button(g, bx + 8, top + h - 24, w - 16, 18, key("evolve_button"), can, mouseX, mouseY, a);
        }
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        SystemData d = data();
        ClassEvolution.Base base = ClassEvolution.base(d.hunterClass());
        if (base == null) return false;
        int cx = x + 12, cw = pw - 24, ry = reqY();
        if (!d.evolutionTrialPassed() && d.level() >= ClassEvolution.EVOLVE_LEVEL && in(mx, my, cx + cw - 118, ry + 10, 110, 18)) {
            PacketDistributor.sendToServer(new ClassEvolution.Payload("trial", ""));
            SaoSounds.confirm();
            onClose();
            return true;
        }
        if (!ClassEvolution.canEvolve(d)) return false;
        List<String> evo = base.evolutions();
        int gap = 8, n = evo.size();
        int w = (cw - gap * (n - 1)) / n, top = cardsY(), h = top() + ph - 10 - top;
        for (int i = 0; i < n; i++) {
            int bx = cx + i * (w + gap);
            if (in(mx, my, bx + 8, top + h - 24, w - 16, 18)) {
                PacketDistributor.sendToServer(new ClassEvolution.Payload("evolve", evo.get(i)));
                SaoSounds.confirm();
                onClose();
                return true;
            }
        }
        return false;
    }
}
