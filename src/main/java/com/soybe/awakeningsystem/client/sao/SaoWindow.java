package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * A full-dive window: the floating white panel with its orange title disc, the round back button and the
 * soft opening, shared by the windows that are not sections of {@link SaoPanelScreen} (the shadow command, the
 * fables, the interface settings...). A window draws its own body inside and answers its own clicks.
 */
public abstract class SaoWindow extends Screen {
    protected static final int HEADER = 22;
    protected final Screen parent;
    protected final int pw, ph;
    protected int ticks;
    protected int scroll;

    protected SaoWindow(Screen parent, int pw, int ph) {
        super(Component.empty());
        this.parent = parent;
        this.pw = pw;
        this.ph = ph;
    }

    /** The title in the header. */
    protected abstract Component heading();

    /** The pictogram in the orange disc. */
    protected abstract String[] glyph();

    /** Text on the right of the header (a count, a level), or null. */
    protected String headerRight() {
        return null;
    }

    protected abstract void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a);

    protected abstract boolean clickBody(double mx, double my, int x, int y);

    protected static Component tr(String key, String fallback, Object... args) {
        return Component.translatableWithFallback("awakening_system.sao." + key, fallback, args);
    }

    protected SystemData data() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? new SystemData() : mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
    }

    /** How much the window shrinks to fit a small screen (1 = full size). */
    protected float fitScale() {
        return Math.min(1.0F, Math.min((this.width - 56.0F) / (pw + 8), (this.height - 10.0F) / ph));
    }

    /** A point on screen as the unshrunk window sees it. */
    protected double unX(double mx) { float s = fitScale(); return s >= 1 ? mx : this.width / 2.0 + (mx - this.width / 2.0) / s; }
    protected double unY(double my) { float s = fitScale(); return s >= 1 ? my : this.height / 2.0 + (my - this.height / 2.0) / s; }

    protected int left() { return this.width / 2 - pw / 2 + 16; }
    protected int top() { return this.height / 2 - ph / 2; }
    private float backX() { return left() - 24; }
    private float backY() { return top() + 11; }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        backdrop(g, this.width, this.height, 1.0F);
    }

    @Override
    public void render(GuiGraphics g, int rawX, int rawY, float partialTick) {
        super.render(g, rawX, rawY, partialTick);
        float s = fitScale();
        int mouseX = (int) Math.round(unX(rawX)), mouseY = (int) Math.round(unY(rawY));
        frame(mouseX, mouseY, ticks + partialTick);
        g.pose().pushPose();
        if (s < 1) {
            g.pose().translate(this.width / 2.0F, this.height / 2.0F, 0);
            g.pose().scale(s, s, 1);
            g.pose().translate(-this.width / 2.0F, -this.height / 2.0F, 0);
        }
        drawWindow(g, mouseX, mouseY, partialTick);
        g.pose().popPose();
    }

    private void drawWindow(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float open = Mth.clamp((ticks + partialTick) / 5.0F, 0.0F, 1.0F);
        float a = open * open * (3 - 2 * open);
        int x = left(), y = top() + Math.round((1 - a) * 8);
        panel(g, x, y, pw, ph, HEADER, a);
        shinyDisc(g, x + 12, y + 11, 7.5F, ORANGE, a);
        icon(g, glyph(), x + 7, y + 6, alpha(WHITE, a));
        g.drawString(this.font, heading(), x + 25, y + 7, alpha(GREY_DARK, a), false);
        String right = headerRight();
        if (right != null) g.drawString(this.font, right, x + pw - 10 - this.font.width(right), y + 7, alpha(ORANGE_DARK, a), false);
        roundButton(g, backX(), backY(), 10, BACK, ORANGE, true, mouseX, mouseY, a);
        body(g, x, y, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(double rx, double ry, int button) {
        double mx = unX(rx), my = unY(ry);
        if (button == 0 && inCircle(mx, my, backX(), backY(), 11)) {
            SaoSounds.click();
            onClose();
            return true;
        }
        if (button == 0 && clickBody(mx, my, left(), top())) return true;
        return super.mouseClicked(rx, ry, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll + (scrollY < 0 ? 1 : -1));
        return true;
    }

    @Override
    public void onClose() {
        SaoSounds.back();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ── pieces ───────────────────────────────────────────────────────────────

    protected static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    protected static boolean inCircle(double mx, double my, float cx, float cy, float r) {
        return (mx - cx) * (mx - cx) + (my - cy) * (my - cy) <= r * r;
    }

    /** The orange action button (grey when it can't be used). */
    protected void button(GuiGraphics g, int bx, int by, int w, int h, Component text, boolean enabled, int mouseX, int mouseY, float a) {
        boolean hot = enabled && in(mouseX, mouseY, bx, by, w, h);
        if (hot) hovering(5000 + bx * 7 + by);
        int top = !enabled ? 0xFFE2E2E6 : hot ? 0xFFFFC44A : 0xFFF7B52A;
        int bot = !enabled ? 0xFFD0D0D6 : 0xFFE29400;
        g.fill(bx + 1, by + 2, bx + w + 1, by + h + 2, alpha(0x22000000, a));
        g.fillGradient(bx, by, bx + w, by + h, alpha(top, a), alpha(bot, a));
        String s = this.font.plainSubstrByWidth(text.getString(), w - 6);
        g.drawString(this.font, s, bx + w / 2 - this.font.width(s) / 2, by + (h - 8) / 2 + 1, alpha(enabled ? WHITE : GREY, a), false);
    }

    /** A white button with grey text, orange when it is the one chosen. */
    protected void softButton(GuiGraphics g, int bx, int by, int w, int h, Component text, boolean on, int mouseX, int mouseY, float a) {
        boolean hot = in(mouseX, mouseY, bx, by, w, h);
        if (hot) hovering(6000 + bx * 7 + by);
        g.fillGradient(bx, by, bx + w, by + h, alpha(on ? 0xFFF7B52A : hot ? 0xFFFFF3DC : 0xFFF4F4F6, a), alpha(on ? 0xFFE29400 : 0xFFDCDCE0, a));
        String s = this.font.plainSubstrByWidth(text.getString(), w - 6);
        g.drawString(this.font, s, bx + w / 2 - this.font.width(s) / 2, by + (h - 8) / 2 + 1, alpha(on ? WHITE : GREY, a), false);
    }

    /** A white card row with an orange edge, as the menus' lists use. */
    protected void card(GuiGraphics g, int cx, int cy, int w, int h, boolean hot, float a) {
        g.fillGradient(cx, cy, cx + w, cy + h, alpha(hot ? 0xFFFFF6E4 : 0xFAFFFFFF, a), alpha(hot ? 0xFFFBE6C2 : 0xF0E6E6EA, a));
        g.fill(cx, cy + h - 1, cx + w, cy + h, alpha(0xFFC9C9CE, a));
        g.fill(cx, cy, cx + 2, cy + h, alpha(ORANGE, a * (hot ? 1.0F : 0.6F)));
    }

    /** Small grey marks on the right edge of a list that scrolls. */
    protected void scrollBar(GuiGraphics g, int x, int y, int h, int total, int shown, float a) {
        if (total <= shown) return;
        int max = total - shown;
        int bh = Math.max(10, h * shown / total);
        int by = y + (h - bh) * Math.min(scroll, max) / Math.max(1, max);
        g.fill(x, y, x + 2, y + h, alpha(0x30000000, a));
        g.fill(x, by, x + 2, by + bh, alpha(ORANGE, a));
    }
}
