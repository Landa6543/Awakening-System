package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Class evolution in the System look: what is still needed (the level and the trial, with the button that calls
 * the trial boss) and one card per evolution of the base class, each with its button to evolve. In full-dive
 * mode {@link com.soybe.awakeningsystem.client.sao.SaoEvolutionScreen} opens instead.
 */
public final class ClassEvolutionScreen extends Screen {
    private final Screen parent;

    public ClassEvolutionScreen(Screen parent) {
        super(Component.translatable("awakening_system.evolution.title"));
        this.parent = parent;
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.evolution." + k, args);
    }

    private SystemData data() {
        var p = Minecraft.getInstance().player;
        return p == null ? new SystemData() : p.getData(AwakeningSystemMod.PLAYER_SYSTEM);
    }

    private float scale() {
        return Math.min(1.0F, Math.min(this.width / 560.0F, this.height / 330.0F));
    }

    private double un(double v, int size) {
        float s = scale();
        return s >= 1 ? v : size / 2.0 + (v - size / 2.0) / s;
    }

    private static final int PW = 540, PH = 310;

    private int left() { return (this.width - PW) / 2; }
    private int top() { return (this.height - PH) / 2; }

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

    private void button(GuiGraphics g, int x, int y, int w, int h, Component text, boolean enabled, int mx, int my) {
        boolean hot = enabled && in(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, !enabled ? 0xFF1A1A1A : hot ? 0xFF007A99 : 0xFF00556B);
        int border = !enabled ? 0xFF333333 : hot ? 0xFFFFFFFF : 0xFF00E5FF;
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        g.fill(x, y, x + 1, y + h, border);
        g.fill(x + w - 1, y, x + w, y + h, border);
        String s = this.font.plainSubstrByWidth(text.getString(), w - 8);
        g.drawString(this.font, s, x + w / 2 - this.font.width(s) / 2, y + (h - 8) / 2, enabled ? 0xFFFFFFFF : 0xFF888888);
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
        SystemData d = data();
        int x = left(), y = top();
        ModernGuiHelper.GuiTheme theme = ModernGuiHelper.getTheme(d);
        ModernGuiHelper.drawModernWindow(g, x, y, PW, PH, theme);
        ModernGuiHelper.drawModernHeader(g, x + 24, y + 10, PW - 48, 26, null, 0, theme, this.font);
        g.drawString(this.font, key("title").copy().withStyle(ChatFormatting.BOLD), x + 34, y + 19, 0xFFFFD700);
        if (d.hasHunterClass()) {
            String cls = Component.translatable("awakening_system.class." + d.hunterClass() + ".name").getString();
            g.drawString(this.font, cls, x + PW - 34 - this.font.width(cls), y + 19, 0xFFCCE3EC);
        }

        ClassEvolution.Base base = ClassEvolution.base(d.hunterClass());
        if (base == null) {
            int ty = y + PH / 2 - 10;
            for (FormattedCharSequence l : this.font.split(d.hasHunterClass() ? key("already") : key("no_class"), PW - 60)) {
                g.drawCenteredString(this.font, l, x + PW / 2, ty, 0xFFAAAAAA);
                ty += 11;
            }
            g.pose().popPose();
            return;
        }
        // what is still needed
        int ry = y + 44;
        g.fill(x + 20, ry, x + PW - 20, ry + 40, 0xF5060D14);
        boolean lvlOk = d.level() >= ClassEvolution.EVOLVE_LEVEL, won = d.evolutionTrialPassed();
        g.drawString(this.font, (lvlOk ? "✔ " : "✘ ") + key("req_level", d.level(), ClassEvolution.EVOLVE_LEVEL).getString(), x + 30, ry + 8,
                lvlOk ? 0xFF50FA7B : 0xFFFF5555);
        g.drawString(this.font, (won ? "✔ " : "✘ ") + key(won ? "req_trial_won" : "req_trial").getString(), x + 30, ry + 23,
                won ? 0xFF50FA7B : 0xFFFF5555);
        if (!won) button(g, x + PW - 158, ry + 10, 130, 20, key("trial_button"), lvlOk, mx, my);

        // the evolutions
        List<String> evo = base.evolutions();
        int gap = 10, n = evo.size(), cx = x + 20, cw = PW - 40;
        int w = (cw - gap * (n - 1)) / n, top = ry + 50, h = y + PH - 22 - top;
        boolean can = ClassEvolution.canEvolve(d);
        for (int i = 0; i < n; i++) {
            int bx = cx + i * (w + gap);
            ClassSelectionScreen.ClassOption c = ClassSelectionScreen.advanced(evo.get(i));
            int color = c == null ? 0xFFFFD700 : c.color();
            boolean hot = in(mx, my, bx, top, w, h);
            g.fill(bx, top, bx + w, top + h, hot ? 0xF50A1822 : 0xF5060D14);
            g.fill(bx, top, bx + w, top + 2, color);
            int ty = top + 8;
            Component name = c == null ? Component.literal(evo.get(i)) : c.getName();
            g.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), w - 16), bx + 8, ty, color);
            ty += 13;
            if (c != null) {
                g.drawString(this.font, this.font.plainSubstrByWidth(c.getRoleTag().getString(), w - 16), bx + 8, ty, 0xFF00E5FF);
                ty += 13;
                for (FormattedCharSequence l : this.font.split(c.getDescription(), w - 16)) {
                    if (ty > top + h - 36) break;
                    g.drawString(this.font, l, bx + 8, ty, 0xFFCCE3EC);
                    ty += 10;
                }
            }
            button(g, bx + 8, top + h - 28, w - 16, 20, key("evolve_button"), can, mx, my);
        }
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double rawX, double rawY, int b) {
        double mx = un(rawX, this.width), my = un(rawY, this.height);
        SystemData d = data();
        ClassEvolution.Base base = ClassEvolution.base(d.hunterClass());
        if (b != 0 || base == null) return super.mouseClicked(rawX, rawY, b);
        int x = left(), y = top(), ry = y + 44;
        if (!d.evolutionTrialPassed() && d.level() >= ClassEvolution.EVOLVE_LEVEL && in(mx, my, x + PW - 158, ry + 10, 130, 20)) {
            PacketDistributor.sendToServer(new ClassEvolution.Payload("trial", ""));
            AwakeningSystemClient.playButtonClick();
            onClose();
            return true;
        }
        if (ClassEvolution.canEvolve(d)) {
            List<String> evo = base.evolutions();
            int gap = 10, n = evo.size(), cx = x + 20, cw = PW - 40;
            int w = (cw - gap * (n - 1)) / n, top = ry + 50, h = y + PH - 22 - top;
            for (int i = 0; i < n; i++) {
                int bx = cx + i * (w + gap);
                if (in(mx, my, bx + 8, top + h - 28, w - 16, 20)) {
                    PacketDistributor.sendToServer(new ClassEvolution.Payload("evolve", evo.get(i)));
                    AwakeningSystemClient.playButtonClick();
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(rawX, rawY, b);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
