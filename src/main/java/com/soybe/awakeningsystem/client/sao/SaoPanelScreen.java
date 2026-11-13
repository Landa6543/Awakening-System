package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SkillDefinitions;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.TitleDefinitions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The full-dive versions of the System's sections, drawn in the same floating white-and-orange style as the
 * menu: Status (spend attribute points), Skills (equip, unequip, evolve), Titles (wear one) and Missions
 * (daily training and the guild commission). They use the same actions as the normal windows.
 */
public class SaoPanelScreen extends Screen {
    public enum Section { STATUS, SKILLS, TITLES, MISSIONS }

    private static final String[] ATTRS = {"strength", "vitality", "agility", "perception", "intelligence", "willpower"};
    private static final String[] ATTR_NAMES = {"Strength", "Vitality", "Agility", "Perception", "Intelligence", "Willpower"};
    private static final int PW = 330, PH = 206, HEADER = 22, ROW = 20;

    private final Screen parent;
    private final Section section;
    private int ticks;
    private boolean passives;
    private int scroll;
    private String selected = "";

    public SaoPanelScreen(Screen parent, Section section) {
        super(Component.empty());
        this.parent = parent;
        this.section = section;
    }

    private static Component tr(String key, String fallback, Object... args) {
        return Component.translatableWithFallback("awakening_system.sao." + key, fallback, args);
    }

    private SystemData data() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? new SystemData() : mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
    }

    private int left() { return this.width / 2 - PW / 2 + 16; }
    private int top() { return this.height / 2 - PH / 2; }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        backdrop(g, this.width, this.height, 1.0F);
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.35F));
    }

    // ── layout shared by drawing and clicking ────────────────────────────────

    private float backX() { return left() - 24; }
    private float backY() { return top() + 11; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        float open = Mth.clamp((ticks + partialTick) / 5.0F, 0.0F, 1.0F);
        float a = open * open * (3 - 2 * open);
        int x = left(), y = top() + Math.round((1 - a) * 8);
        panel(g, x, y, PW, PH, HEADER, a);

        String[] icon = switch (section) {
            case STATUS -> BARS;
            case SKILLS -> SWORD;
            case TITLES -> STAR;
            case MISSIONS -> SCROLL;
        };
        shinyDisc(g, x + 12, y + 11, 7.5F, ORANGE, a);
        icon(g, icon, x + 7, y + 6, alpha(WHITE, a));
        g.drawString(this.font, title(), x + 25, y + 7, alpha(GREY_DARK, a), false);
        String lv = tr("level", "Lv. %s").getString().replace("%s", String.valueOf(data().level()));
        g.drawString(this.font, lv, x + PW - 10 - this.font.width(lv), y + 7, alpha(ORANGE_DARK, a), false);

        roundButton(g, backX(), backY(), 10, BACK, ORANGE, true, mouseX, mouseY, a);

        switch (section) {
            case STATUS -> renderStatus(g, x, y, mouseX, mouseY, a);
            case SKILLS -> renderSkills(g, x, y, mouseX, mouseY, a);
            case TITLES -> renderTitles(g, x, y, mouseX, mouseY, a);
            case MISSIONS -> renderMissions(g, x, y, mouseX, mouseY, a);
        }
    }

    private Component title() {
        String key = switch (section) {
            case STATUS -> "status";
            case SKILLS -> "skills";
            case TITLES -> "titles";
            case MISSIONS -> "missions";
        };
        String s = Component.translatable("awakening_system.screen.tab." + key).getString();
        return Component.literal(s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase(Locale.ROOT));
    }

    // ── Status ───────────────────────────────────────────────────────────────

    private int attrValue(SystemData d, int i) {
        return switch (i) {
            case 0 -> d.strength();
            case 1 -> d.vitality();
            case 2 -> d.agility();
            case 3 -> d.perception();
            case 4 -> d.intelligence();
            default -> d.willpower();
        };
    }

    private int attrX(int i) { return left() + 12 + (i / 3) * 156; }
    private int attrY(int i) { return top() + HEADER + 30 + (i % 3) * 30; }

    private void renderStatus(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int pts = d.attributePoints();
        Component ptsText = tr("points", "Attribute points: %s", pts);
        int pw = this.font.width(ptsText) + 18;
        g.fillGradient(x + 12, y + HEADER + 7, x + 12 + pw, y + HEADER + 21, alpha(pts > 0 ? 0xFFF7B52A : 0xFFE2E2E6, a), alpha(pts > 0 ? 0xFFE29400 : 0xFFD0D0D6, a));
        g.drawString(this.font, ptsText, x + 21, y + HEADER + 10, alpha(pts > 0 ? WHITE : GREY, a), false);
        String rank = tr("rank", "Rank %s").getString().replace("%s", data().evaluatedRank());
        g.drawString(this.font, rank, x + PW - 12 - this.font.width(rank), y + HEADER + 10, alpha(ORANGE_DARK, a), false);

        for (int i = 0; i < 6; i++) {
            int ax = attrX(i), ay = attrY(i);
            g.fillGradient(ax, ay, ax + 148, ay + 26, alpha(0xFAFFFFFF, a), alpha(0xF0E6E6EA, a));
            g.fill(ax, ay + 25, ax + 148, ay + 26, alpha(0xFFC9C9CE, a));
            g.fill(ax, ay, ax + 2, ay + 26, alpha(ORANGE, a * 0.6F));
            g.drawString(this.font, tr("attr." + ATTRS[i], ATTR_NAMES[i]), ax + 8, ay + 4, alpha(GREY, a), false);
            String v = String.valueOf(attrValue(d, i));
            g.pose().pushPose();
            g.pose().translate(ax + 8, ay + 13.5F, 0);
            g.pose().scale(1.15F, 1.15F, 1);
            g.drawString(this.font, v, 0, 0, alpha(GREY_DARK, a), false);
            g.pose().popPose();
            roundButton(g, ax + 134, ay + 13, 8, PLUS, ORANGE, pts > 0, mouseX, mouseY, a);
        }

        Minecraft mc = Minecraft.getInstance();
        int by = y + HEADER + 124;
        g.fill(x + 12, by - 6, x + PW - 12, by - 5, alpha(0xFFC9C9CE, a));
        statBar(g, x + 12, by, PW - 24, "HP", (int) Math.ceil(mc.player.getHealth()), (int) Math.ceil(mc.player.getMaxHealth()), 0xFF74C23E, a);
        statBar(g, x + 12, by + 14, PW - 24, "MP", d.mana(), Math.max(1, d.maxMana()), 0xFF3E9BD6, a);
        statBar(g, x + 12, by + 28, PW - 24, "EXP", d.experience(), Math.max(1, d.nextLevelExperience()), ORANGE, a);
        String coins = d.coins() + " G";
        g.drawString(this.font, coins, x + PW - 12 - this.font.width(coins), by + 44, alpha(ORANGE_DARK, a), false);
    }

    private void statBar(GuiGraphics g, int x, int y, int w, String label, int value, int max, int color, float a) {
        g.drawString(this.font, label, x, y, alpha(GREY, a), false);
        String num = value + " / " + max;
        g.drawString(this.font, num, x + w - this.font.width(num), y, alpha(GREY, a), false);
        bar(g, x + 28, y, w - 34 - this.font.width(num), 8, value / (float) max, color, a);
    }

    // ── Skills ───────────────────────────────────────────────────────────────

    private List<Map.Entry<String, Integer>> skills() {
        SystemData d = data();
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        for (Map.Entry<String, Integer> e : d.skills().entrySet()) {
            if (e.getKey() == null || e.getValue() == null || SkillDefinitions.get(e.getKey()) == null || !SkillDefinitions.isAvailable(e.getKey())) continue;
            if (passives ? SkillDefinitions.isPassive(e.getKey()) : SkillDefinitions.isActive(e.getKey())) list.add(e);
        }
        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        return list;
    }

    private int slots() { return passives ? data().getUnlockedPassiveSkillSlots() : data().getUnlockedActiveSkillSlots(); }
    private int maxSlots() { return passives ? 3 : 5; }
    private String equipped(int s) { return passives ? data().equippedPassiveSkill(s) : data().equippedActiveSkill(s); }

    private int equippedSlotOf(String id) {
        for (int s = 0; s < slots(); s++) if (id.equals(equipped(s))) return s;
        return -1;
    }

    private static Component skillName(String id) {
        SkillDefinitions.Definition def = SkillDefinitions.get(id);
        return def == null ? Component.literal(id) : Component.translatable(def.nameKey());
    }

    private int listTop() { return top() + HEADER + 66; }
    private int visibleRows() { return 4; }

    private float slotX(int s) { return left() + PW / 2.0F - (maxSlots() - 1) * 15 + s * 30; }
    private float slotY() { return top() + HEADER + 44; }

    private void renderSkills(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        // active / passive switch
        for (int i = 0; i < 2; i++) {
            int tx = x + 12 + i * 78, ty = y + HEADER + 7;
            boolean on = (i == 1) == passives;
            g.fillGradient(tx, ty, tx + 74, ty + 14, alpha(on ? 0xFFF7B52A : 0xFFF4F4F6, a), alpha(on ? 0xFFE29400 : 0xFFDCDCE0, a));
            Component lbl = i == 0 ? tr("actives", "Active") : tr("passives", "Passive");
            g.drawString(this.font, lbl, tx + 37 - this.font.width(lbl) / 2, ty + 3, alpha(on ? WHITE : GREY, a), false);
        }
        // equipped slots
        for (int s = 0; s < maxSlots(); s++) {
            float sx = slotX(s), sy = slotY();
            boolean open = s < slots();
            String eq = open ? equipped(s) : "";
            ring(g, sx, sy, 12, 1.0F, alpha(eq.isEmpty() ? 0xFFB8B8BE : ORANGE, a));
            shinyDisc(g, sx, sy, 10.5F, !open ? 0xFFCACACF : eq.isEmpty() ? 0xFFF2F2F4 : 0xFF5E5E66, a);
            if (!open) icon(g, CROSS, Math.round(sx) - 5, Math.round(sy) - 5, alpha(0xFFA0A0A6, a));
            else if (!eq.isEmpty()) {
                String ini = skillName(eq).getString();
                ini = ini.isEmpty() ? "?" : ini.substring(0, 1).toUpperCase(Locale.ROOT);
                g.drawString(this.font, ini, Math.round(sx) - this.font.width(ini) / 2, Math.round(sy) - 4, alpha(WHITE, a), false);
            }
        }
        List<Map.Entry<String, Integer>> list = skills();
        scroll = Mth.clamp(scroll, 0, Math.max(0, list.size() - visibleRows()));
        int lt = listTop();
        if (list.isEmpty()) g.drawString(this.font, tr("no_skills", "No skills yet."), x + 14, lt + 6, alpha(GREY, a), false);
        for (int i = 0; i < visibleRows() && i + scroll < list.size(); i++) {
            Map.Entry<String, Integer> e = list.get(i + scroll);
            int ry = lt + i * (ROW + 2);
            boolean hot = e.getKey().equals(selected);
            row(g, this.font, x + 10, ry, PW - 74, ROW, skillName(e.getKey()), passives ? SHIELD : SWORD, hot, a);
            String lv = tr("level", "Lv. %s").getString().replace("%s", String.valueOf(e.getValue()));
            g.drawString(this.font, lv, x + PW - 70 - this.font.width(lv), ry + 6, alpha(hot ? WHITE : ORANGE_DARK, a), false);
            boolean habit = passives && SkillDefinitions.isHabitSkill(e.getKey());
            if (!habit) roundButton(g, x + PW - 50, ry + ROW / 2.0F, 8, equippedSlotOf(e.getKey()) >= 0 ? MINUS : PLUS, ORANGE, true, mouseX, mouseY, a);
            if (e.getValue() >= 5) roundButton(g, x + PW - 28, ry + ROW / 2.0F, 8, UP, 0xFF8E4FD6, true, mouseX, mouseY, a);
        }
        scrollMarks(g, x + PW - 8, lt, visibleRows() * (ROW + 2), list.size(), a);
        footer(g, x, y, selected.isEmpty() || SkillDefinitions.get(selected) == null ? null
                : Component.translatable(SkillDefinitions.get(selected).descriptionKey()), a);
    }

    private void scrollMarks(GuiGraphics g, int x, int y, int h, int total, float a) {
        if (total <= visibleRows()) return;
        g.fill(x, y, x + 2, y + h, alpha(0xFFDCDCE0, a));
        int th = Math.max(8, h * visibleRows() / total);
        int ty = y + (h - th) * scroll / Math.max(1, total - visibleRows());
        g.fill(x, ty, x + 2, ty + th, alpha(ORANGE, a));
    }

    /** The description box at the bottom of the panel. */
    private void footer(GuiGraphics g, int x, int y, Component text, float a) {
        int fy = y + PH - 40;
        g.fill(x + 10, fy - 4, x + PW - 10, fy - 3, alpha(0xFFC9C9CE, a));
        if (text == null) {
            g.drawString(this.font, tr("pick_hint", "Choose one to see what it does."), x + 14, fy + 4, alpha(GREY_LIGHT, a), false);
            return;
        }
        List<FormattedCharSequence> lines = this.font.split(text, PW - 28);
        for (int i = 0; i < Math.min(3, lines.size()); i++) g.drawString(this.font, lines.get(i), x + 14, fy + i * 10, alpha(GREY_DARK, a), false);
    }

    // ── Titles ───────────────────────────────────────────────────────────────

    private static int rarityColor(SkillDefinitions.Rarity r) {
        if (r == null) return GREY;
        return switch (r) {
            case COMMON -> 0xFF8A8A92;
            case UNCOMMON -> 0xFF4FA84A;
            case RARE -> 0xFF3E8AD6;
            case EPIC -> 0xFF9B4FD6;
            case LEGENDARY -> 0xFFE29400;
            case MYTHIC -> 0xFFD6473E;
            case UNIQUE -> 0xFFD63EA8;
        };
    }

    private void renderTitles(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        String worn = d.equippedTitle();
        TitleDefinitions.Definition wornDef = null;
        List<TitleDefinitions.Definition> list = TitleDefinitions.availableDefinitions(d);
        for (TitleDefinitions.Definition t : list) if (t.id().equals(worn)) wornDef = t;
        Component wearing = tr("wearing", "Wearing: %s", wornDef == null ? tr("none", "nothing") : Component.translatable(wornDef.translationKey()));
        g.drawString(this.font, wearing, x + 14, y + HEADER + 10, alpha(GREY, a), false);

        scroll = Mth.clamp(scroll, 0, Math.max(0, list.size() - visibleRows() - 1));
        int lt = top() + HEADER + 28;
        if (list.isEmpty()) g.drawString(this.font, tr("no_titles", "No titles yet."), x + 14, lt + 6, alpha(GREY, a), false);
        for (int i = 0; i < visibleRows() + 1 && i + scroll < list.size(); i++) {
            TitleDefinitions.Definition t = list.get(i + scroll);
            int ry = lt + i * (ROW + 2);
            boolean hot = t.id().equals(selected);
            row(g, this.font, x + 10, ry, PW - 46, ROW, Component.translatable(t.translationKey()), STAR, hot, a);
            disc(g, x + PW - 52, ry + ROW / 2.0F, 3.0F, alpha(rarityColor(t.rarity()), a));
            if (t.id().equals(worn)) roundButton(g, x + PW - 24, ry + ROW / 2.0F, 8, CHECK, 0xFF4FA84A, true, -99, -99, a);
        }
        if (list.size() > visibleRows() + 1) {
            g.fill(x + PW - 8, lt, x + PW - 6, lt + (visibleRows() + 1) * (ROW + 2), alpha(0xFFDCDCE0, a));
        }
        TitleDefinitions.Definition sel = null;
        for (TitleDefinitions.Definition t : list) if (t.id().equals(selected)) sel = t;
        Component desc = null;
        if (sel != null) {
            desc = Component.translatable(sel.descriptionKey());
            if (sel.bonusKey() != null && !sel.bonusKey().isEmpty())
                desc = desc.copy().append("  ").append(Component.translatable(sel.bonusKey()).withStyle(s -> s.withColor(0xC98700)));
        }
        footer(g, x, y, desc, a);
    }

    // ── Missions ─────────────────────────────────────────────────────────────

    private void renderMissions(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int cy = y + HEADER + 8;
        g.drawString(this.font, tr("daily", "Daily training"), x + 14, cy, alpha(GREY_DARK, a), false);
        Component state = d.isDailyClaimed() ? tr("done", "Completed today") : d.isDailyQuestComplete() ? tr("ready", "Reward ready") : tr("in_progress", "In progress");
        g.drawString(this.font, state, x + PW - 14 - this.font.width(state), cy, alpha(d.isDailyQuestComplete() ? 0xFF4FA84A : ORANGE_DARK, a), false);
        String[] keys = {"pushups", "situps", "squats", "running"};
        String[] names = {"Push-ups", "Sit-ups", "Squats", "Running"};
        int[] vals = {d.dailyPushUps(), d.dailySitUps(), d.dailySquats(), d.dailyRunning()};
        int[] tgts = {100, 100, 100, 1000};
        for (int i = 0; i < 4; i++) {
            int ry = cy + 14 + i * 18;
            g.drawString(this.font, tr("train." + keys[i], names[i]), x + 14, ry + 2, alpha(GREY, a), false);
            String num = vals[i] + "/" + tgts[i] + (i == 3 ? " m" : "");
            bar(g, x + 96, ry + 2, PW - 190, 8, vals[i] / (float) tgts[i], vals[i] >= tgts[i] ? 0xFF4FA84A : ORANGE, a);
            g.drawString(this.font, num, x + PW - 88 - this.font.width(num) + 50, ry + 2, alpha(GREY, a), false);
            if (i < 3 && vals[i] < tgts[i]) roundButton(g, x + PW - 22, ry + 6, 7, FIST, ORANGE, true, mouseX, mouseY, a);
        }
        if (d.isDailyQuestComplete() && !d.isDailyClaimed()) claimButton(g, x + PW - 112, cy + 86, mouseX, mouseY, a);

        int hy = cy + 106;
        g.fill(x + 10, hy - 6, x + PW - 10, hy - 5, alpha(0xFFC9C9CE, a));
        g.drawString(this.font, tr("guild", "Guild commission"), x + 14, hy, alpha(GREY_DARK, a), false);
        int target = Math.max(1, d.dailyHunterTarget());
        g.drawString(this.font, tr("hunts", "Monsters hunted"), x + 14, hy + 16, alpha(GREY, a), false);
        bar(g, x + 96, hy + 16, PW - 190, 8, d.dailyHunterKills() / (float) target, d.isDailyHunterComplete() ? 0xFF4FA84A : ORANGE, a);
        String num = d.dailyHunterKills() + "/" + target;
        g.drawString(this.font, num, x + PW - 38 - this.font.width(num), hy + 16, alpha(GREY, a), false);
        if (d.isDailyHunterClaimed()) g.drawString(this.font, tr("done", "Completed today"), x + 14, hy + 32, alpha(0xFF4FA84A, a), false);
        else if (d.isDailyHunterComplete()) claimButton(g, x + PW - 112, hy + 30, mouseX, mouseY, a);
    }

    private void claimButton(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        boolean hover = mouseX >= x && mouseX < x + 100 && mouseY >= y && mouseY < y + 15;
        g.fillGradient(x, y, x + 100, y + 15, alpha(hover ? 0xFFFFC44A : 0xFFF7B52A, a), alpha(0xFFE29400, a));
        Component c = tr("claim", "Claim reward");
        g.drawString(this.font, c, x + 50 - this.font.width(c) / 2, y + 4, alpha(WHITE, a), false);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static boolean inCircle(double mx, double my, float cx, float cy, float r) {
        return (mx - cx) * (mx - cx) + (my - cy) * (my - cy) <= r * r;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        if (inCircle(mx, my, backX(), backY(), 11)) {
            click();
            onClose();
            return true;
        }
        int x = left(), y = top();
        SystemData d = data();
        switch (section) {
            case STATUS -> {
                for (int i = 0; i < 6; i++) {
                    if (inCircle(mx, my, attrX(i) + 134, attrY(i) + 13, 9) && d.attributePoints() > 0) {
                        click();
                        AwakeningSystemClient.spend(ATTRS[i]);
                        return true;
                    }
                }
            }
            case SKILLS -> {
                for (int i = 0; i < 2; i++) {
                    if (in(mx, my, x + 12 + i * 78, y + HEADER + 7, 74, 14)) {
                        passives = i == 1;
                        scroll = 0;
                        selected = "";
                        click();
                        return true;
                    }
                }
                for (int s = 0; s < slots(); s++) {
                    if (inCircle(mx, my, slotX(s), slotY(), 12) && !equipped(s).isEmpty()) {
                        click();
                        AwakeningSystemClient.equipSkillSlot(passives, s, "");
                        return true;
                    }
                }
                List<Map.Entry<String, Integer>> list = skills();
                int lt = listTop();
                for (int i = 0; i < visibleRows() && i + scroll < list.size(); i++) {
                    Map.Entry<String, Integer> e = list.get(i + scroll);
                    int ry = lt + i * (ROW + 2);
                    boolean habit = passives && SkillDefinitions.isHabitSkill(e.getKey());
                    if (!habit && inCircle(mx, my, x + PW - 50, ry + ROW / 2.0F, 9)) {
                        click();
                        int at = equippedSlotOf(e.getKey());
                        if (at >= 0) AwakeningSystemClient.equipSkillSlot(passives, at, "");
                        else {
                            int free = 0;
                            for (int s = 0; s < slots(); s++) if (equipped(s).isEmpty()) { free = s; break; }
                            AwakeningSystemClient.equipSkillSlot(passives, free, e.getKey());
                        }
                        return true;
                    }
                    if (e.getValue() >= 5 && inCircle(mx, my, x + PW - 28, ry + ROW / 2.0F, 9)) {
                        click();
                        AwakeningSystemClient.evolve(e.getKey());
                        return true;
                    }
                    if (in(mx, my, x + 10, ry, PW - 74, ROW)) {
                        click();
                        selected = e.getKey().equals(selected) ? "" : e.getKey();
                        return true;
                    }
                }
            }
            case TITLES -> {
                List<TitleDefinitions.Definition> list = TitleDefinitions.availableDefinitions(d);
                int lt = top() + HEADER + 28;
                for (int i = 0; i < visibleRows() + 1 && i + scroll < list.size(); i++) {
                    TitleDefinitions.Definition t = list.get(i + scroll);
                    int ry = lt + i * (ROW + 2);
                    if (in(mx, my, x + 10, ry, PW - 46, ROW)) {
                        click();
                        if (t.id().equals(selected)) AwakeningSystemClient.equipTitle(t.id().equals(d.equippedTitle()) ? "none" : t.id());
                        selected = t.id();
                        return true;
                    }
                    if (t.id().equals(d.equippedTitle()) && inCircle(mx, my, x + PW - 24, ry + ROW / 2.0F, 9)) {
                        click();
                        AwakeningSystemClient.equipTitle("none");
                        return true;
                    }
                }
            }
            case MISSIONS -> {
                int cy = y + HEADER + 8;
                int[] vals = {d.dailyPushUps(), d.dailySitUps(), d.dailySquats()};
                for (int i = 0; i < 3; i++) {
                    if (vals[i] < 100 && inCircle(mx, my, x + PW - 22, cy + 14 + i * 18 + 6, 8)) {
                        click();
                        AwakeningSystemClient.startWorkout(i);
                        onClose();
                        return true;
                    }
                }
                if (d.isDailyQuestComplete() && !d.isDailyClaimed() && in(mx, my, x + PW - 112, cy + 86, 100, 15)) {
                    click();
                    AwakeningSystemClient.claimDailyMission();
                    return true;
                }
                int hy = cy + 106;
                if (d.isDailyHunterComplete() && !d.isDailyHunterClaimed() && in(mx, my, x + PW - 112, hy + 30, 100, 15)) {
                    click();
                    AwakeningSystemClient.claimHunterCommission();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll + (scrollY < 0 ? 1 : -1));
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
