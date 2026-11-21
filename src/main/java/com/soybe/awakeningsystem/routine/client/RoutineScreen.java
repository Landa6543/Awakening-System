package com.soybe.awakeningsystem.routine.client;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import com.soybe.awakeningsystem.routine.NpcRoutine;
import com.soybe.awakeningsystem.routine.NpcRoutine.Stop;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A guild NPC's routine in the System look: one row per stop (its time, with buttons to move it half an hour,
 * its place, and a button to remove it), and below: add a stop where the player stands, pick places in the
 * world, turn the routine on or off, and save. In full-dive mode {@link com.soybe.awakeningsystem.client.sao.SaoRoutineScreen}
 * opens instead.
 */
public final class RoutineScreen extends Screen {
    private static final int PW = 400, PH = 326, ROW = 17;
    private final RoutineClient.Draft draft;

    public RoutineScreen(RoutineClient.Draft draft) {
        super(Component.translatable("awakening_system.routine.title", draft.npcName));
        this.draft = draft;
    }

    static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.routine." + k, args);
    }

    private float scale() {
        return Math.min(1.0F, Math.min(this.width / (PW + 20.0F), this.height / (PH + 20.0F)));
    }

    private double un(double v, int size) {
        float s = scale();
        return s >= 1 ? v : size / 2.0 + (v - size / 2.0) / s;
    }

    private int left() { return (this.width - PW) / 2; }
    private int top() { return (this.height - PH) / 2; }
    private int rowsY() { return top() + 50; }
    private int buttonsY() { return top() + PH - 52; }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x95000000, 0xD5000000);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, Component text, boolean on, int mx, int my) {
        boolean hot = in(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, on ? (hot ? 0xFF2E6B45 : 0xFF1A462C) : (hot ? 0xFF007A99 : 0xFF0A2430));
        int border = hot ? 0xFFFFFFFF : on ? 0xFF50FA7B : 0xFF00E5FF;
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        g.fill(x, y, x + 1, y + h, border);
        g.fill(x + w - 1, y, x + w, y + h, border);
        String s = this.font.plainSubstrByWidth(text.getString(), w - 6);
        g.drawString(this.font, s, x + w / 2 - this.font.width(s) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    public void render(GuiGraphics g, int rawX, int rawY, float partialTick) {
        super.render(g, rawX, rawY, partialTick);
        float s = scale();
        int mx = (int) Math.round(un(rawX, this.width)), my = (int) Math.round(un(rawY, this.height));
        g.pose().pushPose();
        if (s < 1) {
            g.pose().translate(this.width / 2.0F, this.height / 2.0F, 0);
            g.pose().scale(s, s, 1);
            g.pose().translate(-this.width / 2.0F, -this.height / 2.0F, 0);
        }
        int x = left(), y = top();
        var p = Minecraft.getInstance().player;
        SystemData d = p == null ? new SystemData() : p.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        ModernGuiHelper.GuiTheme theme = ModernGuiHelper.getTheme(d);
        ModernGuiHelper.drawModernWindow(g, x, y, PW, PH, theme);
        ModernGuiHelper.drawModernHeader(g, x + 24, y + 10, PW - 48, 26, null, 0, theme, this.font);
        g.drawString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), x + 34, y + 19, 0xFFFFD700);
        String now = key("now", NpcRoutine.clock((int) (p == null ? 0 : p.level().getDayTime()))).getString();
        g.drawString(this.font, now, x + PW - 34 - this.font.width(now), y + 19, 0xFFCCE3EC);

        int ry = rowsY();
        if (draft.stops.isEmpty()) {
            for (var l : this.font.split(key("empty"), PW - 80)) {
                g.drawCenteredString(this.font, l, x + PW / 2, ry + 40, 0xFFAAAAAA);
                ry += 11;
            }
        }
        for (int i = 0; i < draft.stops.size(); i++) {
            Stop st = draft.stops.get(i);
            int rowY = rowsY() + i * ROW;
            boolean hot = in(mx, my, x + 24, rowY, PW - 48, ROW - 2);
            g.fill(x + 24, rowY, x + PW - 24, rowY + ROW - 2, hot ? 0xC00A1822 : 0xA0060D14);
            g.drawString(this.font, "#" + (i + 1), x + 30, rowY + 4, 0xFF88AABB);
            g.drawString(this.font, NpcRoutine.clock(st.time()), x + 54, rowY + 4, 0xFFFFD700);
            button(g, x + 90, rowY + 1, 13, 13, Component.literal("-"), false, mx, my);
            button(g, x + 105, rowY + 1, 13, 13, Component.literal("+"), false, mx, my);
            g.drawString(this.font, st.pos().getX() + ", " + st.pos().getY() + ", " + st.pos().getZ(), x + 128, rowY + 4, 0xFFCCE3EC);
            button(g, x + PW - 44, rowY + 1, 16, 13, Component.literal("✕"), false, mx, my);
        }

        int by = buttonsY(), bw = (PW - 48 - 12) / 3;
        button(g, x + 24, by, bw, 18, key("add_here"), false, mx, my);
        button(g, x + 30 + bw, by, bw, 18, key("pick"), false, mx, my);
        button(g, x + 36 + 2 * bw, by, bw, 18, key(draft.on ? "is_on" : "is_off"), draft.on, mx, my);
        button(g, x + 24, by + 22, PW - 48, 18, key("save"), true, mx, my);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double rawX, double rawY, int b) {
        if (b != 0) return super.mouseClicked(rawX, rawY, b);
        double mx = un(rawX, this.width), my = un(rawY, this.height);
        int x = left();
        for (int i = 0; i < draft.stops.size(); i++) {
            int rowY = rowsY() + i * ROW;
            if (in(mx, my, x + 90, rowY + 1, 13, 13)) { draft.shift(i, -500); click(); return true; }
            if (in(mx, my, x + 105, rowY + 1, 13, 13)) { draft.shift(i, 500); click(); return true; }
            if (in(mx, my, x + PW - 44, rowY + 1, 16, 13)) { draft.stops.remove(i); click(); return true; }
        }
        int by = buttonsY(), bw = (PW - 48 - 12) / 3;
        if (in(mx, my, x + 24, by, bw, 18)) {
            var p = Minecraft.getInstance().player;
            if (p != null && !draft.add(p.blockPosition())) p.displayClientMessage(key("full", NpcRoutine.MAX_STOPS), true);
            click();
            return true;
        }
        if (in(mx, my, x + 30 + bw, by, bw, 18)) { click(); RoutineClient.startPicking(draft); return true; }
        if (in(mx, my, x + 36 + 2 * bw, by, bw, 18)) { draft.on = !draft.on; click(); return true; }
        if (in(mx, my, x + 24, by + 22, PW - 48, 18)) { click(); draft.save(); onClose(); return true; }
        return super.mouseClicked(rawX, rawY, b);
    }

    private static void click() {
        AwakeningSystemClient.playButtonClick();
    }
}
