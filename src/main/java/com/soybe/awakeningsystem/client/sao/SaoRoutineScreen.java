package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.routine.NpcRoutine;
import com.soybe.awakeningsystem.routine.NpcRoutine.Stop;
import com.soybe.awakeningsystem.routine.client.RoutineClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * A guild NPC's routine in the full-dive look: one card per stop (its time with buttons to move it half an
 * hour, its place, a button to remove it), and below: add a stop where the player stands, pick places in the
 * world, turn the routine on or off, and save.
 */
public class SaoRoutineScreen extends SaoWindow {
    private static final int ROW = 16;
    private final RoutineClient.Draft draft;

    public SaoRoutineScreen(RoutineClient.Draft draft) {
        super(null, 360, 290);
        this.draft = draft;
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.routine." + k, args);
    }

    @Override
    protected Component heading() {
        return key("title", draft.npcName);
    }

    @Override
    protected String[] glyph() {
        return PEOPLE;
    }

    @Override
    protected String headerRight() {
        var p = Minecraft.getInstance().player;
        return key("now", NpcRoutine.clock((int) (p == null ? 0 : p.level().getDayTime()))).getString();
    }

    private int rowsY() { return top() + HEADER + 8; }
    private int buttonsY() { return top() + ph - 50; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        int cx = x + 12, cw = pw - 24;
        if (draft.stops.isEmpty()) {
            int ty = y + 90;
            for (var l : this.font.split(key("empty"), cw - 40)) {
                g.drawCenteredString(this.font, l, x + pw / 2, ty, alpha(GREY, a));
                ty += 10;
            }
        }
        for (int i = 0; i < draft.stops.size(); i++) {
            Stop st = draft.stops.get(i);
            int ry = rowsY() + i * ROW;
            boolean hot = in(mouseX, mouseY, cx, ry, cw, ROW - 2);
            card(g, cx, ry, cw, ROW - 2, hot, a);
            g.drawString(this.font, "#" + (i + 1), cx + 6, ry + 3, alpha(GREY_LIGHT, a), false);
            g.drawString(this.font, NpcRoutine.clock(st.time()), cx + 28, ry + 3, alpha(ORANGE_DARK, a), false);
            softButton(g, cx + 64, ry + 1, 12, 12, Component.literal("-"), false, mouseX, mouseY, a);
            softButton(g, cx + 78, ry + 1, 12, 12, Component.literal("+"), false, mouseX, mouseY, a);
            g.drawString(this.font, st.pos().getX() + ", " + st.pos().getY() + ", " + st.pos().getZ(), cx + 98, ry + 3, alpha(GREY_DARK, a), false);
            softButton(g, cx + cw - 18, ry + 1, 14, 12, Component.literal("x"), false, mouseX, mouseY, a);
        }
        int by = buttonsY(), bw = (cw - 8) / 3;
        softButton(g, cx, by, bw, 16, key("add_here"), false, mouseX, mouseY, a);
        softButton(g, cx + bw + 4, by, bw, 16, key("pick"), false, mouseX, mouseY, a);
        softButton(g, cx + 2 * bw + 8, by, bw, 16, key(draft.on ? "is_on" : "is_off"), draft.on, mouseX, mouseY, a);
        button(g, cx, by + 20, cw, 18, key("save"), true, mouseX, mouseY, a);
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        int cx = x + 12, cw = pw - 24;
        for (int i = 0; i < draft.stops.size(); i++) {
            int ry = rowsY() + i * ROW;
            if (in(mx, my, cx + 64, ry + 1, 12, 12)) { draft.shift(i, -500); SaoSounds.click(); return true; }
            if (in(mx, my, cx + 78, ry + 1, 12, 12)) { draft.shift(i, 500); SaoSounds.click(); return true; }
            if (in(mx, my, cx + cw - 18, ry + 1, 14, 12)) { draft.stops.remove(i); SaoSounds.click(); return true; }
        }
        int by = buttonsY(), bw = (cw - 8) / 3;
        if (in(mx, my, cx, by, bw, 16)) {
            var p = Minecraft.getInstance().player;
            if (p != null && !draft.add(p.blockPosition())) p.displayClientMessage(key("full", NpcRoutine.MAX_STOPS), true);
            SaoSounds.click();
            return true;
        }
        if (in(mx, my, cx + bw + 4, by, bw, 16)) { SaoSounds.click(); RoutineClient.startPicking(draft); return true; }
        if (in(mx, my, cx + 2 * bw + 8, by, bw, 16)) { draft.on = !draft.on; SaoSounds.click(); return true; }
        if (in(mx, my, cx, by + 20, cw, 18)) { SaoSounds.confirm(); draft.save(); onClose(); return true; }
        return false;
    }
}
