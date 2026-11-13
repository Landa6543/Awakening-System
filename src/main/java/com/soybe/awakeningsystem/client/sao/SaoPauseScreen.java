package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.SystemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The pause menu in the full-dive look: the game's own choices (back to game, options, advancements,
 * statistics, leave) as a floating list, with leaving the world asked twice, as a log-out should be.
 */
public class SaoPauseScreen extends Screen {
    private record Entry(Component label, String[] icon, Runnable action) {}

    private static final int ROW_W = 150, ROW_H = 22;
    private final List<Entry> entries = new ArrayList<>();
    private boolean confirmLeave;
    private int ticks;

    public SaoPauseScreen() {
        super(Component.translatable("menu.game"));
    }

    private static Component tr(String key, String fallback) {
        return Component.translatableWithFallback("awakening_system.sao." + key, fallback);
    }

    @Override
    protected void init() {
        entries.clear();
        entries.add(new Entry(Component.translatable("menu.returnToGame"), BACK, this::onClose));
        entries.add(new Entry(tr("system", "System"), PERSON, () -> minecraft.setScreen(new SaoMenuScreen())));
        entries.add(new Entry(Component.translatable("gui.advancements"), STAR,
                () -> minecraft.setScreen(new AdvancementsScreen(minecraft.player.connection.getAdvancements(), this))));
        entries.add(new Entry(Component.translatable("gui.stats"), BARS,
                () -> minecraft.setScreen(new StatsScreen(this, minecraft.player.getStats()))));
        entries.add(new Entry(Component.translatable("menu.options"), GEAR,
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
        entries.add(new Entry(tr("classic_pause", "Classic menu"), SWAP, () -> {
            UiStyle.passThrough = true;
            minecraft.setScreen(new net.minecraft.client.gui.screens.PauseScreen(true));
        }));
        entries.add(new Entry(tr("logout", "Log out"), DOOR, () -> {
            confirmLeave = true;
            ticks = 0;
        }));
    }

    @Override
    public void tick() {
        ticks++;
    }

    private int listX() { return this.width / 2 - ROW_W / 2 + 14; }
    private int listTop() { return this.height / 2 - (entries.size() * (ROW_H + 2)) / 2; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        backdrop(g, this.width, this.height, 1.0F);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        float open = Mth.clamp((ticks + partialTick) / 5.0F, 0.0F, 1.0F);
        float a = open * open * (3 - 2 * open);

        if (confirmLeave) {
            int w = 190, h = 78, x = this.width / 2 - w / 2, y = this.height / 2 - h / 2 - Math.round((1 - a) * 8);
            panel(g, x, y, w, h, 20, a);
            Component title = tr("logout", "Log out");
            g.drawString(this.font, title, x + (w - this.font.width(title)) / 2, y + 7, alpha(GREY_DARK, a), false);
            g.fill(x + 16, y + 20, x + w - 16, y + 21, alpha(GREY, a));
            Component ask = tr("logout_confirm", "Leave this world?");
            g.drawString(this.font, ask, x + (w - this.font.width(ask)) / 2, y + 38, alpha(GREY_DARK, a), false);
            for (int i = 0; i < 2; i++) {
                float bx = x + w / 2.0F + (i == 0 ? -34 : 34), by = y + 62;
                boolean hover = (mouseX - bx) * (mouseX - bx) + (mouseY - by) * (mouseY - by) <= 121;
                int col = i == 0 ? 0xFF3E9BD6 : 0xFFD6473E;
                disc(g, bx, by, 11, alpha(hover ? col : WHITE, a));
                ring(g, bx, by, 11, 1.2F, alpha(col, a));
                icon(g, i == 0 ? CHECK : CROSS, Math.round(bx) - 5, Math.round(by) - 5, alpha(hover ? WHITE : col, a));
            }
            return;
        }

        int lx = listX(), ly = listTop();
        float cyc = ly + (entries.size() * (ROW_H + 2)) / 2.0F;
        shinyDisc(g, lx - 30, cyc, 14, ORANGE, a);
        ring(g, lx - 30, cyc, 18, 1.2F, alpha(ORANGE, a));
        icon(g, GEAR, lx - 35, Math.round(cyc) - 5, alpha(WHITE, a));
        pointerLeft(g, lx - 10, Math.round(cyc), 6, alpha(ORANGE, a));
        for (int i = 0; i < entries.size(); i++) {
            float mine = Mth.clamp((ticks + partialTick - i * 0.8F) / 4.0F, 0.0F, 1.0F);
            int y = ly + i * (ROW_H + 2);
            int x = lx - Math.round((1 - mine) * 12);
            boolean hot = mouseX >= lx && mouseX < lx + ROW_W && mouseY >= y && mouseY < y + ROW_H;
            row(g, this.font, x, y, ROW_W, ROW_H, entries.get(i).label, entries.get(i).icon, hot, mine);
        }
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.35F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (confirmLeave) {
            int w = 190, h = 78, x = this.width / 2 - w / 2, y = this.height / 2 - h / 2;
            for (int i = 0; i < 2; i++) {
                double bx = x + w / 2.0 + (i == 0 ? -34 : 34), by = y + 62;
                if ((mouseX - bx) * (mouseX - bx) + (mouseY - by) * (mouseY - by) <= 144) {
                    click();
                    if (i == 0) leaveWorld();
                    else {
                        confirmLeave = false;
                        ticks = 0;
                    }
                    return true;
                }
            }
            return true;
        }
        int lx = listX(), ly = listTop();
        for (int i = 0; i < entries.size(); i++) {
            int y = ly + i * (ROW_H + 2);
            if (mouseX >= lx && mouseX < lx + ROW_W && mouseY >= y && mouseY < y + ROW_H) {
                click();
                entries.get(i).action.run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (confirmLeave && keyCode == 256) {       // Escape steps back from the question first
            confirmLeave = false;
            ticks = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** Leaves the world the way the game's own pause menu does. */
    private void leaveWorld() {
        Minecraft mc = Minecraft.getInstance();
        boolean local = mc.isLocalServer();
        if (mc.level != null) mc.level.disconnect();
        if (local) mc.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
        else mc.disconnect();
        TitleScreen title = new TitleScreen();
        mc.setScreen(local ? title : new JoinMultiplayerScreen(title));
    }
}
