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
    private Component hoverTip;
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
        List<Entry> me = new ArrayList<>(List.of(
                new Entry(tab("status"), BARS, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.STATUS))),
                new Entry(tab("skills"), SWORD, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.SKILLS)))));
        SystemData dd = minecraft.player == null ? new SystemData() : minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (dd.hasHunterClass()) me.add(new Entry(tr("class_tree", "Class tree"), STAR, () -> minecraft.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(this))));
        else if (dd.level() >= 50) me.add(new Entry(tr("class_pick", "Choose a class"), STAR, com.soybe.awakeningsystem.AwakeningSystemClient::openClassSelectionScreen));
        me.add(new Entry(tab("artifacts"), SHIELD, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.ARTIFACTS))));
        me.add(new Entry(tab("titles"), TROPHY, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.TITLES))));
        categories.add(new Category(PERSON, me));
        categories.add(new Category(PEOPLE, List.of(
                new Entry(tab("ranking"), TROPHY, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.RANKING))),
                new Entry(tr("guild_menu", "Guild"), PEOPLE,
                        com.soybe.awakeningsystem.AwakeningSystemClient::requestOpenGuild),
                new Entry(tr("constellations", "Constellations"), CHAT, () -> minecraft.setScreen(new ConstellationChannelScreen(this))))));
        categories.add(new Category(SCROLL, List.of(
                new Entry(tab("missions"), SCROLL, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.MISSIONS))),
                new Entry(tab("progress"), BARS, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.PROGRESS))))));
        categories.add(new Category(COIN, List.of(
                new Entry(tr("buy", "Buy"), COIN, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.SHOP))),
                new Entry(tr("sell_title", "Sell"), SWAP, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.SELL))))));
        categories.add(new Category(DOOR, List.of(
                new Entry(tr("travel_title", "Worlds"), DOOR, () -> minecraft.setScreen(new SaoPanelScreen(this, SaoPanelScreen.Section.TRAVEL))))));
        categories.add(new Category(GEAR, List.of(
                new Entry(tr("interface", "Interface"), GEAR, () -> minecraft.setScreen(new GuiConfigScreen(this))),
                new Entry(tr("options", "Options"), GEAR, () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))),
                new Entry(tr("style_normal", "Normal style"), SWAP, () -> {
                    UiStyle.setFullDive(false);
                    minecraft.setScreen(new SystemScreen(0));
                }),
                new Entry(tr("logout", "Log out"), DOOR, () -> minecraft.setScreen(new SaoPauseScreen(true))))));        selected = Mth.clamp(selected, 0, categories.size() - 1);
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
        frame(mouseX, mouseY, ticks + partialTick);
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
            if (sel) ring(g, cx, cy, R + 4, 1.2F, alpha(ORANGE, mine));
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

        hoverTip = null;
        renderCard(g, cx - R - 18 - 176, this.height / 2 - 120, a, partialTick, mouseX, mouseY);
        if (hoverTip != null) g.renderTooltip(this.font, hoverTip, mouseX, mouseY);
    }

    /** The player's card: name, the player in 3D ringed by the skills they have equipped, and the main numbers. */
    private void renderCard(GuiGraphics g, int x, int y, float a, float partialTick, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        SystemData d = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        int w = 176;
        x -= Math.round((1.0F - a) * 14);
        panel(g, x, y, w, 240, 20, a);
        pointerRight(g, x + w + 7, y + 70, 7, alpha(PANEL, a));
        String name = mc.player.getName().getString();
        g.drawString(this.font, name, x + (w - this.font.width(name)) / 2, y + 6, alpha(GREY_DARK, a), false);
        for (int hy = y + 26; hy < y + 150; hy += 8)
            for (int hx = x + 8 + ((hy / 8) % 2) * 5; hx < x + w - 8; hx += 10) g.fill(hx, hy, hx + 3, hy + 1, alpha(0x10000000, a));

        int fx = x + w / 2, fy = y + 28;
        // a soft floor disc and the player themself, turning to follow the mouse
        g.fillGradient(fx - 26, fy + 108, fx + 26, fy + 112, alpha(0x30000000, a), 0);
        if (a > 0.6F) net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse(
                g, fx - 30, fy + 4, fx + 30, fy + 112, 46, 0.0625F, mouseX, mouseY, mc.player);

        // equipped skills on an orbit: actives down the left, passives down the right
        Component hoverName = null;
        int actives = d.getUnlockedActiveSkillSlots(), passives = d.getUnlockedPassiveSkillSlots();
        for (int i = 0; i < 8; i++) {
            boolean passive = i >= 5;
            int slot = passive ? i - 5 : i;
            int count = passive ? 3 : 5;
            double t = count == 1 ? 0.5 : slot / (double) (count - 1);
            double ang = passive ? (-Math.PI / 2 + 0.55 + t * (Math.PI - 1.1)) : (Math.PI * 1.5 - 0.35 - t * (Math.PI - 0.7));
            float px = fx + (float) Math.cos(ang) * 62, py = fy + 58 + (float) Math.sin(ang) * 54;
            boolean open = slot < (passive ? passives : actives);
            String id = open ? (passive ? d.equippedPassiveSkill(slot) : d.equippedActiveSkill(slot)) : "";
            boolean has = !id.isEmpty();
            int color = !open ? 0xFFCACACF : has ? (passive ? 0xFF3E8AD6 : ORANGE) : 0xFFF2F2F4;
            ring(g, px, py, 9.5F, 1.0F, alpha(has ? color : 0xFFB8B8BE, a));
            shinyDisc(g, px, py, 7.8F, color, a);
            if (has) {
                com.soybe.awakeningsystem.SkillDefinitions.Definition def = com.soybe.awakeningsystem.SkillDefinitions.get(id);
                Component nm = def == null ? Component.literal(id) : Component.translatable(def.nameKey());
                String ini = nm.getString().isEmpty() ? "?" : nm.getString().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
                g.drawString(this.font, ini, Math.round(px) - this.font.width(ini) / 2, Math.round(py) - 4, alpha(WHITE, a), false);
                if ((mouseX - px) * (mouseX - px) + (mouseY - py) * (mouseY - py) <= 81) hoverName = nm;
            } else if (!open) icon(g, CROSS, Math.round(px) - 5, Math.round(py) - 5, alpha(0xFFA0A0A6, a));
            else icon(g, PLUS, Math.round(px) - 5, Math.round(py) - 5, alpha(0xFFC4C4CA, a));
        }
        g.drawString(this.font, tr("actives", "Active"), x + 8, y + 140, alpha(ORANGE_DARK, a), false);
        Component pv = tr("passives", "Passive");
        g.drawString(this.font, pv, x + w - 8 - this.font.width(pv), y + 140, alpha(0xFF3E8AD6, a), false);
        y += 22;        int by = y + 132;
        g.fill(x + 6, by, x + w - 6, by + 1, alpha(0xFFC9C9CE, a));
        int text = alpha(GREY_DARK, a), soft = alpha(GREY, a);
        g.drawString(this.font, tr("level", "Lv. %s").getString().replace("%s", String.valueOf(d.level())), x + 10, by + 7, text, false);
        String rank = tr("rank", "Rank %s").getString().replace("%s", d.evaluatedRank());
        g.drawString(this.font, rank, x + w - 10 - this.font.width(rank), by + 7, alpha(ORANGE_DARK, a), false);

        if (hoverName != null) hoverTip = hoverName;
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
