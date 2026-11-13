package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ConstellationChannelScreen;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemScreen;
import com.soybe.awakeningsystem.client.gui.GuiConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The System's main menu in the full-dive look: a column of round buttons, the list of the chosen one
 * opening to its right, and the player's card to the left. Every entry leads to one of the mod's windows.
 */
public class SaoMenuScreen extends Screen {
    private record Entry(Component label, String[] icon, Runnable action) {}
    private record Category(String[] icon, List<Entry> entries) {}

    private static final int R = 14, STEP = 34, ROW_W = 124, ROW_H = 22;

    private final List<Category> categories = new ArrayList<>();
    private int selected = 0;
    private int ticks;
    private int listTicks;

    public SaoMenuScreen() {
        super(Component.translatableWithFallback("awakening_system.sao.title", "System"));
    }

    private static Component tr(String key, String fallback) {
        return Component.translatableWithFallback("awakening_system.sao." + key, fallback);
    }

    private static Component tab(String name) {
        String s = Component.translatable("awakening_system.screen.tab." + name).getString();
        return Component.literal(s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase(java.util.Locale.ROOT));
    }

    private void openTab(int tab) {
        UiStyle.passThrough = true;
        Minecraft.getInstance().setScreen(new SystemScreen(tab));
    }

    @Override
    protected void init() {
        categories.clear();
        categories.add(new Category(PERSON, List.of(
                new Entry(tab("status"), BARS, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.STATUS))),
                new Entry(tab("skills"), SWORD, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.SKILLS))),
                new Entry(tab("artifacts"), SHIELD, () -> openTab(7)),
                new Entry(tab("titles"), STAR, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.TITLES))))));
        categories.add(new Category(PEOPLE, List.of(
                new Entry(tab("ranking"), TROPHY, () -> openTab(6)),
                new Entry(tr("constellations", "Constellations"), CHAT, () -> minecraft.setScreen(new ConstellationChannelScreen(this))))));
        categories.add(new Category(SCROLL, List.of(
                new Entry(tab("missions"), SCROLL, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.MISSIONS))),
                new Entry(tab("progress"), BARS, () -> openTab(5)))));
        categories.add(new Category(COIN, List.of(
                new Entry(tab("shop"), COIN, () -> openTab(4)))));
        categories.add(new Category(GEAR, List.of(
                new Entry(tr("interface", "Interface"), GEAR, () -> minecraft.setScreen(new GuiConfigScreen(this))),
                new Entry(tr("options", "Options"), GEAR, () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))),
                new Entry(tr("style_normal", "Normal style"), SWAP, () -> {
                    UiStyle.setFullDive(false);
                    minecraft.setScreen(new SystemScreen(0));
                }))));
        selected = Mth.clamp(selected, 0, categories.size() - 1);
    }

    @Override
    public void tick() {
        ticks++;
        listTicks++;
    }

    private int columnX() { return this.width / 2 - 6; }
    private int columnTop() { return this.height / 2 - (categories.size() - 1) * STEP / 2; }
    private int listX() { return columnX() + R + 16; }
    private int listTop() {
        int n = categories.get(selected).entries.size();
        int y = columnTop() + selected * STEP - (n * (ROW_H + 2) - 2) / 2;
        return Mth.clamp(y, 6, Math.max(6, this.height - n * (ROW_H + 2) - 6));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        backdrop(g, this.width, this.height, 1.0F);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        float open = Mth.clamp((ticks + partialTick) / 6.0F, 0.0F, 1.0F);
        float a = open * open * (3 - 2 * open);
        int cx = columnX(), top = columnTop();

        // the column of round buttons, dropping into place one after another
        for (int i = 0; i < categories.size(); i++) {
            float mine = Mth.clamp((ticks + partialTick - i * 1.2F) / 5.0F, 0.0F, 1.0F);
            float cy = top + i * STEP - (1.0F - mine) * 10.0F;
            boolean sel = i == selected;
            boolean hover = (mouseX - cx) * (mouseX - cx) + (mouseY - cy) * (mouseY - cy) <= R * R;
            if (sel) {
                float pulse = 0.5F + 0.5F * Mth.sin((ticks + partialTick) * 0.25F);
                ring(g, cx, cy, R + 4 + pulse, 1.2F, alpha(ORANGE, mine * (0.6F + 0.4F * pulse)));
            }
            ring(g, cx, cy, R + 1.5F, 1.0F, alpha(sel ? 0xFFFFD27A : 0xFFB8B8BE, mine));
            shinyDisc(g, cx, cy, R, sel ? ORANGE : (hover ? WHITE : 0xFFE4E4E8), mine);
            icon(g, categories.get(i).icon, cx - 5, Math.round(cy) - 5, alpha(sel ? WHITE : GREY, mine));
        }

        // the chosen button's list, sliding out to the right
        float slide = Mth.clamp((listTicks + partialTick) / 4.0F, 0.0F, 1.0F);
        List<Entry> entries = categories.get(selected).entries;
        int lx = listX() - Math.round((1.0F - slide) * 12), ly = listTop();
        pointerLeft(g, cx + R + 6, top + selected * STEP, 6, alpha(ORANGE, a * slide));
        for (int i = 0; i < entries.size(); i++) {
            int y = ly + i * (ROW_H + 2);
            boolean hot = mouseX >= lx && mouseX < lx + ROW_W && mouseY >= y && mouseY < y + ROW_H;
            row(g, this.font, lx, y, ROW_W, ROW_H, entries.get(i).label, entries.get(i).icon, hot, a * slide);
        }

        renderCard(g, cx - R - 18 - 150, this.height / 2 - 104, a, partialTick);
    }

    /** The player's card: name, a figure ringed with equipment points, and the main numbers underneath. */
    private void renderCard(GuiGraphics g, int x, int y, float a, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        SystemData d = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        int w = 150;
        x -= Math.round((1.0F - a) * 14);
        panel(g, x, y, w, 208, 20, a);
        pointerRight(g, x + w + 7, y + 66, 7, alpha(PANEL, a));
        String name = mc.player.getName().getString();
        g.drawString(this.font, name, x + (w - this.font.width(name)) / 2, y + 6, alpha(GREY_DARK, a), false);
        // a faint hexagon lattice behind the figure
        for (int hy = y + 26; hy < y + 128; hy += 8)
            for (int hx = x + 8 + ((hy / 8) % 2) * 5; hx < x + w - 8; hx += 10) g.fill(hx, hy, hx + 3, hy + 1, alpha(0x10000000, a));

        int fx = x + w / 2, fy = y + 34;
        int body = alpha(GREY_DARK, a);
        disc(g, fx, fy + 6, 5.5F, body);                           // head
        g.fill(fx - 11, fy + 14, fx + 11, fy + 19, body);         // shoulders
        g.fill(fx - 8, fy + 19, fx + 8, fy + 44, body);           // torso
        g.fill(fx - 14, fy + 15, fx - 10, fy + 42, body);         // arms
        g.fill(fx + 10, fy + 15, fx + 14, fy + 42, body);
        g.fill(fx - 8, fy + 44, fx - 1, fy + 76, body);           // legs
        g.fill(fx + 1, fy + 44, fx + 8, fy + 76, body);
        g.fill(fx - 12, fy + 79, fx + 12, fy + 80, alpha(GREY_LIGHT, a));

        int lit = (ticks / 16) % 10;
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI * 2 / 10;
            float px = fx + (float) Math.cos(ang) * 52, py = fy + 42 + (float) Math.sin(ang) * 44;
            ring(g, px, py, 6.0F, 1.0F, alpha(i == lit ? ORANGE : GREY_LIGHT, a));
            shinyDisc(g, px, py, 4.2F, i == lit ? ORANGE : GREY_DARK, a);
        }

        int by = y + 132;
        g.fill(x + 6, by, x + w - 6, by + 1, alpha(0xFFC9C9CE, a));
        int text = alpha(GREY_DARK, a), soft = alpha(GREY, a);
        g.drawString(this.font, tr("level", "Lv. %s").getString().replace("%s", String.valueOf(d.level())), x + 10, by + 7, text, false);
        String rank = tr("rank", "Rank %s").getString().replace("%s", d.evaluatedRank());
        g.drawString(this.font, rank, x + w - 10 - this.font.width(rank), by + 7, alpha(ORANGE_DARK, a), false);

        bar(g, x + 10, by + 20, w - 20, "HP", (int) Math.ceil(mc.player.getHealth()), (int) Math.ceil(mc.player.getMaxHealth()), 0xFF74C23E, a);
        bar(g, x + 10, by + 32, w - 20, "MP", d.mana(), Math.max(1, d.maxMana()), 0xFF3E9BD6, a);
        bar(g, x + 10, by + 44, w - 20, "EXP", d.experience(), Math.max(1, d.nextLevelExperience()), ORANGE, a);

        String stats = "STR " + d.strength() + "  AGI " + d.agility() + "  VIT " + d.vitality();
        String stats2 = "INT " + d.intelligence() + "  PER " + d.perception();
        g.pose().pushPose();
        g.pose().translate(x + 10, by + 57, 0);
        g.pose().scale(0.75F, 0.75F, 1.0F);
        g.drawString(this.font, stats, 0, 0, soft, false);
        g.drawString(this.font, stats2, 0, 11, soft, false);
        String coins = d.coins() + " G";
        g.drawString(this.font, coins, Math.round((w - 20) / 0.75F) - this.font.width(coins), 11, alpha(ORANGE_DARK, a), false);
        g.pose().popPose();
    }

    private void bar(GuiGraphics g, int x, int y, int w, String label, int value, int max, int color, float a) {
        g.pose().pushPose();
        g.pose().translate(x, y + 1, 0);
        g.pose().scale(0.75F, 0.75F, 1.0F);
        g.drawString(this.font, label, 0, 0, alpha(GREY, a), false);
        String num = value + "/" + max;
        g.drawString(this.font, num, Math.round(w / 0.75F) - this.font.width(num), 0, alpha(GREY, a), false);
        g.pose().popPose();
        int bx = x + 22, bw = w - 22 - Math.round(this.font.width(num) * 0.75F) - 4;
        SaoDraw.bar(g, bx, y + 1, bw, 6, value / (float) max, color, a);
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.35F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int cx = columnX(), top = columnTop();
            for (int i = 0; i < categories.size(); i++) {
                double dx = mouseX - cx, dy = mouseY - (top + i * STEP);
                if (dx * dx + dy * dy <= (R + 2) * (R + 2)) {
                    if (selected != i) {
                        selected = i;
                        listTicks = 0;
                    }
                    click();
                    return true;
                }
            }
            List<Entry> entries = categories.get(selected).entries;
            int lx = listX(), ly = listTop();
            for (int i = 0; i < entries.size(); i++) {
                int y = ly + i * (ROW_H + 2);
                if (mouseX >= lx && mouseX < lx + ROW_W && mouseY >= y && mouseY < y + ROW_H) {
                    click();
                    entries.get(i).action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int next = Mth.clamp(selected + (scrollY < 0 ? 1 : -1), 0, categories.size() - 1);
        if (next != selected) {
            selected = next;
            listTicks = 0;
            click();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
