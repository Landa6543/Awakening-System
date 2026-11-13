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
    public enum Section { STATUS, SKILLS, TITLES, MISSIONS, ARTIFACTS, RANKING, PROGRESS, SHOP, SELL, TRAVEL }

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
            case ARTIFACTS -> SHIELD;
            case RANKING -> TROPHY;
            case PROGRESS -> BARS;
            case SHOP, SELL -> COIN;
            case TRAVEL -> DOOR;
        };
        shinyDisc(g, x + 12, y + 11, 7.5F, ORANGE, a);
        icon(g, icon, x + 7, y + 6, alpha(WHITE, a));
        g.drawString(this.font, title(), x + 25, y + 7, alpha(GREY_DARK, a), false);
        String lv = section == Section.SHOP || section == Section.SELL ? data().coins() + " G"
                : tr("level", "Lv. %s").getString().replace("%s", String.valueOf(data().level()));
        g.drawString(this.font, lv, x + PW - 10 - this.font.width(lv), y + 7, alpha(ORANGE_DARK, a), false);

        roundButton(g, backX(), backY(), 10, BACK, ORANGE, true, mouseX, mouseY, a);

        switch (section) {
            case STATUS -> renderStatus(g, x, y, mouseX, mouseY, a);
            case SKILLS -> renderSkills(g, x, y, mouseX, mouseY, a);
            case TITLES -> renderTitles(g, x, y, mouseX, mouseY, a);
            case MISSIONS -> renderMissions(g, x, y, mouseX, mouseY, a);
            case ARTIFACTS -> renderArtifacts(g, x, y, mouseX, mouseY, a);
            case RANKING -> renderRanking(g, x, y, mouseX, mouseY, a);
            case PROGRESS -> renderProgress(g, x, y, mouseX, mouseY, a);
            case SHOP -> renderShop(g, x, y, mouseX, mouseY, a);
            case SELL -> renderSell(g, x, y, mouseX, mouseY, a);
            case TRAVEL -> renderTravel(g, x, y, mouseX, mouseY, a);
        }
    }

    private Component title() {
        String key = switch (section) {
            case STATUS -> "status";
            case SKILLS -> "skills";
            case TITLES -> "titles";
            case MISSIONS -> "missions";
            case ARTIFACTS -> "artifacts";
            case RANKING -> "ranking";
            case PROGRESS -> "progress";
            case SHOP, SELL -> "shop";
            case TRAVEL -> "travel";
        };
        if (section == Section.SELL) return tr("sell_title", "Sell");
        if (section == Section.TRAVEL) return tr("travel_title", "Worlds");
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
            bar(g, x + 96, ry + 2, PW - 206, 8, vals[i] / (float) tgts[i], vals[i] >= tgts[i] ? 0xFF4FA84A : ORANGE, a);
            g.drawString(this.font, num, x + PW - 88 - this.font.width(num) + 50, ry + 2, alpha(GREY, a), false);
            if (i < 3 && vals[i] < tgts[i]) roundButton(g, x + PW - 22, ry + 6, 7, FIST, ORANGE, true, mouseX, mouseY, a);
        }
        if (d.isDailyQuestComplete() && !d.isDailyClaimed()) claimButton(g, x + PW - 112, cy + 86, mouseX, mouseY, a);

        int hy = cy + 106;
        g.fill(x + 10, hy - 6, x + PW - 10, hy - 5, alpha(0xFFC9C9CE, a));
        g.drawString(this.font, tr("guild", "Guild commission"), x + 14, hy, alpha(GREY_DARK, a), false);
        int target = Math.max(1, d.dailyHunterTarget());
        g.drawString(this.font, this.font.plainSubstrByWidth(tr("hunts", "Monsters hunted").getString(), 112), x + 14, hy + 16, alpha(GREY, a), false);
        bar(g, x + 130, hy + 16, PW - 224, 8, d.dailyHunterKills() / (float) target, d.isDailyHunterComplete() ? 0xFF4FA84A : ORANGE, a);
        String num = d.dailyHunterKills() + "/" + target;
        g.drawString(this.font, num, x + PW - 38 - this.font.width(num), hy + 16, alpha(GREY, a), false);
        if (d.isDailyHunterClaimed()) g.drawString(this.font, tr("done", "Completed today"), x + 14, hy + 32, alpha(0xFF4FA84A, a), false);
        else if (d.isDailyHunterComplete()) claimButton(g, x + PW - 112, hy + 30, mouseX, mouseY, a);
    }

    // ── Artifacts ────────────────────────────────────────────────────────────

    private static final int ART_ROW = 22;

    private List<int[]> equipmentInInventory() {
        List<int[]> out = new ArrayList<>();
        var p = Minecraft.getInstance().player;
        if (p == null) return out;
        for (int i = 0; i < p.getInventory().items.size(); i++) {
            net.minecraft.world.item.ItemStack st = p.getInventory().getItem(i);
            if (st.isEmpty()) continue;
            boolean art = st.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
            boolean cone = com.soybe.awakeningsystem.item.EquipmentSystemManager.isLightCone(st);
            if (art || cone) out.add(new int[]{i, art ? 1 : 0});
        }
        return out;
    }

    private void renderArtifacts(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int ry0 = y + HEADER + 6;
        for (int i = 0; i < 6; i++) {
            int ry = ry0 + i * ART_ROW;
            boolean cone = i == 5;
            net.minecraft.world.item.ItemStack st = cone ? d.getEquippedLightCone() : d.getArtifact(i);
            boolean hot = in(mouseX, mouseY, x + 10, ry, PW - 50, ART_ROW - 2);
            Component label;
            if (cone) label = st.isEmpty() ? tr("cone_empty", "Light cone: empty") : st.getHoverName();
            else {
                var type = com.soybe.awakeningsystem.artifact.ArtifactType.fromSlot(i);
                label = st.isEmpty() ? type.getDisplayNameComponent().copy().append(Component.literal(" — ")).append(tr("empty", "empty")) : st.getHoverName();
            }
            row(g, this.font, x + 10, ry, PW - 50, ART_ROW - 2, Component.empty(), null, hot && !st.isEmpty(), a);
            if (!st.isEmpty()) g.renderItem(st, x + 15, ry + 1);
            else icon(g, cone ? STAR : SHIELD, x + 18, ry + 4, alpha(GREY_LIGHT, a));
            Component shown = label;
            String txt = this.font.plainSubstrByWidth(shown.getString(), PW - 130);
            g.drawString(this.font, txt, x + 36, ry + 6, alpha(st.isEmpty() ? GREY_LIGHT : (hot ? WHITE : 0xFF4A4A52), a), false);
            if (!st.isEmpty() && !cone) {
                int lvl = com.soybe.awakeningsystem.artifact.GenshinArtifactItem.getLevel(st);
                String lv = "+" + lvl;
                g.drawString(this.font, lv, x + PW - 46 - this.font.width(lv), ry + 6, alpha(hot ? WHITE : ORANGE_DARK, a), false);
                if (lvl < 20) roundButton(g, x + PW - 26, ry + 9, 8, UP, 0xFF4FA84A, true, mouseX, mouseY, a);
            }
        }
        // what can be put on, straight from the inventory
        int iy = ry0 + 6 * ART_ROW + 4;
        g.drawString(this.font, tr("bag", "In your bag:"), x + 12, iy + 5, alpha(GREY, a), false);
        List<int[]> inv = equipmentInInventory();
        int ix0 = x + 16 + this.font.width(tr("bag", "In your bag:"));
        var p = Minecraft.getInstance().player;
        for (int k = 0; k < inv.size() && ix0 + k * 19 < x + PW - 16; k++) {
            int ix = ix0 + k * 19;
            boolean hov = in(mouseX, mouseY, ix, iy, 18, 18);
            ring(g, ix + 9, iy + 9, 9.5F, 1.0F, alpha(hov ? ORANGE : 0xFFC9C9CE, a));
            g.renderItem(p.getInventory().getItem(inv.get(k)[0]), ix + 1, iy + 1);
        }
        if (inv.isEmpty()) g.drawString(this.font, tr("bag_empty", "nothing to equip"), ix0, iy + 5, alpha(GREY_LIGHT, a), false);
        String totals = "HP +" + (int) d.getArtifactBonusHp() + "   ATK +" + String.format(Locale.ROOT, "%.1f", d.getArtifactBonusAttack())
                + "   CRIT +" + String.format(Locale.ROOT, "%.1f", d.getArtifactBonusCritRate()) + "%   CDMG +" + String.format(Locale.ROOT, "%.1f", d.getArtifactBonusCritDmg()) + "%";
        g.fill(x + 10, y + PH - 18, x + PW - 10, y + PH - 17, alpha(0xFFC9C9CE, a));
        g.drawString(this.font, totals, x + PW / 2 - this.font.width(totals) / 2, y + PH - 12, alpha(ORANGE_DARK, a), false);
    }

    private boolean clickArtifacts(double mx, double my) {
        int x = left(), y = top();
        SystemData d = data();
        int ry0 = y + HEADER + 6;
        for (int i = 0; i < 6; i++) {
            int ry = ry0 + i * ART_ROW;
            boolean cone = i == 5;
            net.minecraft.world.item.ItemStack st = cone ? d.getEquippedLightCone() : d.getArtifact(i);
            if (st.isEmpty()) continue;
            if (!cone && com.soybe.awakeningsystem.artifact.GenshinArtifactItem.getLevel(st) < 20 && inCircle(mx, my, x + PW - 26, ry + 9, 9)) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.UpgradeArtifactPayload(false, 0, i));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_USE, 1.2F));
                return true;
            }
            if (in(mx, my, x + 10, ry, PW - 50, ART_ROW - 2)) {
                click();
                if (cone) net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.EquipLightConePayload(-1));
                else net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.EquipArtifactPayload(false, 0, i, -1));
                return true;
            }
        }
        int iy = ry0 + 6 * ART_ROW + 4;
        List<int[]> inv = equipmentInInventory();
        int ix0 = x + 16 + this.font.width(tr("bag", "In your bag:"));
        var p = Minecraft.getInstance().player;
        for (int k = 0; k < inv.size() && ix0 + k * 19 < x + PW - 16; k++) {
            int ix = ix0 + k * 19;
            if (!in(mx, my, ix, iy, 18, 18)) continue;
            click();
            int slot = inv.get(k)[0];
            if (inv.get(k)[1] == 1) {
                var art = (com.soybe.awakeningsystem.artifact.GenshinArtifactItem) p.getInventory().getItem(slot).getItem();
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.EquipArtifactPayload(false, 0, art.getArtifactType().getSlotIndex(), slot));
            } else {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.EquipLightConePayload(slot));
            }
            return true;
        }
        return false;
    }

    // ── Ranking ──────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        if (section == Section.RANKING) AwakeningSystemClient.requestLeaderboard();
    }

    private void renderRanking(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        var list = AwakeningSystemClient.LEADERBOARD;
        int hy = y + HEADER + 8;
        g.drawString(this.font, "#", x + 16, hy, alpha(GREY, a), false);
        g.drawString(this.font, tr("col.name", "Hunter"), x + 40, hy, alpha(GREY, a), false);
        g.drawString(this.font, tr("col.level", "Lv."), x + 170, hy, alpha(GREY, a), false);
        g.drawString(this.font, tr("col.kills", "Kills"), x + 214, hy, alpha(GREY, a), false);
        g.drawString(this.font, "G", x + 274, hy, alpha(GREY, a), false);
        if (list.isEmpty()) {
            g.drawString(this.font, tr("ranking_empty", "No hunters ranked yet."), x + 16, hy + 20, alpha(GREY_LIGHT, a), false);
            return;
        }
        var me = Minecraft.getInstance().player;
        String myName = me == null ? "" : me.getGameProfile().getName();
        int rows = 7;
        scroll = Mth.clamp(scroll, 0, Math.max(0, list.size() - rows));
        for (int i = 0; i < rows && i + scroll < list.size(); i++) {
            var e = list.get(i + scroll);
            int idx = i + scroll;
            int ry = hy + 12 + i * (ROW + 2);
            boolean mine = myName.equalsIgnoreCase(e.name());
            row(g, this.font, x + 10, ry, PW - 24, ROW, Component.empty(), null, mine, a);
            int medal = idx == 0 ? 0xFFE8B83A : idx == 1 ? 0xFFB9BCC4 : idx == 2 ? 0xFFC07A3E : 0xFF5E5E66;
            shinyDisc(g, x + 23, ry + ROW / 2.0F, 7.2F, mine ? WHITE : medal, a);
            String num = String.valueOf(idx + 1);
            g.drawString(this.font, num, x + 23 - this.font.width(num) / 2, ry + 6, alpha(mine ? ORANGE : WHITE, a), false);
            int c = alpha(mine ? WHITE : 0xFF4A4A52, a);
            g.drawString(this.font, this.font.plainSubstrByWidth(e.name(), 120), x + 40, ry + 6, c, false);
            g.drawString(this.font, String.valueOf(e.level()), x + 170, ry + 6, c, false);
            g.drawString(this.font, String.valueOf(e.kills()), x + 214, ry + 6, c, false);
            g.drawString(this.font, String.valueOf(e.coins()), x + 274, ry + 6, alpha(mine ? WHITE : ORANGE_DARK, a), false);
        }
    }

    // ── Progress ─────────────────────────────────────────────────────────────

    private void renderProgress(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int py = y + HEADER + 8;
        g.drawString(this.font, tr("prestige", "Prestige %s", d.prestigeLevel()), x + 14, py, alpha(GREY_DARK, a), false);
        if (d.canPrestige()) claimButtonText(g, x + PW - 112, py - 3, tr("prestige_btn", "Prestige"), mouseX, mouseY, a);
        String line1 = tr("kills", "Kills %s", d.creaturesDefeated()).getString() + "   " + tr("bosses", "Bosses %s", d.bossKills()).getString()
                + "   " + tr("deaths", "Deaths %s", d.deaths()).getString();
        String line2 = tr("mined", "Blocks %s", d.blocksMined()).getString() + "   " + tr("ores", "Ores %s", d.oresMined()).getString()
                + "   " + tr("missions_done", "Missions %s", d.missionsCompleted()).getString();
        g.drawString(this.font, line1, x + 14, py + 16, alpha(GREY, a), false);
        g.drawString(this.font, line2, x + 14, py + 27, alpha(GREY, a), false);

        var list = com.soybe.awakeningsystem.AchievementDefinitions.definitions();
        int lt = py + 42;
        int rows = 4;
        scroll = Mth.clamp(scroll, 0, Math.max(0, list.size() - rows));
        for (int i = 0; i < rows && i + scroll < list.size(); i++) {
            var ach = list.get(i + scroll);
            int ry = lt + i * (ROW + 2);
            boolean done = d.hasAchievement(ach.id());
            boolean hot = ach.id().equals(selected);
            row(g, this.font, x + 10, ry, PW - 24, ROW, Component.translatable(ach.nameKey()), done ? CHECK : TROPHY, hot, a);
            String st = done ? "✔" : com.soybe.awakeningsystem.AchievementDefinitions.statValue(d, ach.statRequired()) + "/" + ach.target();
            g.drawString(this.font, st, x + PW - 22 - this.font.width(st), ry + 6, alpha(done ? 0xFF4FA84A : (hot ? WHITE : ORANGE_DARK), a), false);
        }
        Component desc = null;
        for (var ach : list) if (ach.id().equals(selected)) desc = Component.translatable(ach.descriptionKey());
        footer(g, x, y, desc, a);
    }

    private boolean clickProgress(double mx, double my) {
        SystemData d = data();
        int x = left(), py = top() + HEADER + 8;
        if (d.canPrestige() && in(mx, my, x + PW - 112, py - 3, 100, 15)) {
            click();
            AwakeningSystemClient.prestige();
            return true;
        }
        var list = com.soybe.awakeningsystem.AchievementDefinitions.definitions();
        int lt = py + 42;
        for (int i = 0; i < 4 && i + scroll < list.size(); i++) {
            if (in(mx, my, x + 10, lt + i * (ROW + 2), PW - 24, ROW)) {
                click();
                String id = list.get(i + scroll).id();
                selected = id.equals(selected) ? "" : id;
                return true;
            }
        }
        return false;
    }

    // ── Shop ─────────────────────────────────────────────────────────────────

    private static final String[] SHOP_CATS = {"all", "boxes", "gear", "consumables", "materials"};
    private String shopCat = "all";

    private List<com.soybe.awakeningsystem.ShopDefinitions.Entry> shopEntries() {
        try {
            return com.soybe.awakeningsystem.ShopDefinitions.availableEntries(shopCat);
        } catch (Throwable t) {
            return List.of();
        }
    }

    private int catX(int c) {
        int cx = left() + 12;
        for (int i = 0; i < c; i++) cx += this.font.width(Component.translatable("awakening_system.screen.shop.cat." + SHOP_CATS[i])) + 14;
        return cx;
    }

    private void renderShop(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int cy = y + HEADER + 6;
        for (int c = 0; c < SHOP_CATS.length; c++) {
            Component lbl = Component.translatable("awakening_system.screen.shop.cat." + SHOP_CATS[c]);
            int cx = catX(c), w = this.font.width(lbl) + 10;
            boolean on = SHOP_CATS[c].equals(shopCat);
            g.fillGradient(cx, cy, cx + w, cy + 14, alpha(on ? 0xFFF7B52A : 0xFFF4F4F6, a), alpha(on ? 0xFFE29400 : 0xFFDCDCE0, a));
            g.drawString(this.font, lbl, cx + 5, cy + 3, alpha(on ? WHITE : GREY, a), false);
        }
        var list = shopEntries();
        int lt = cy + 20, rows = 5;
        scroll = Mth.clamp(scroll, 0, Math.max(0, list.size() - rows));
        com.soybe.awakeningsystem.ShopDefinitions.Entry sel = null;
        for (var e : list) if (e.id().equals(selected)) sel = e;
        for (int i = 0; i < rows && i + scroll < list.size(); i++) {
            var e = list.get(i + scroll);
            int ry = lt + i * (ROW + 2);
            boolean hot = e.id().equals(selected);
            boolean ok = com.soybe.awakeningsystem.ShopDefinitions.isUnlocked(e, d);
            row(g, this.font, x + 10, ry, PW - 24, ROW, Component.empty(), null, hot, a);
            shopIcon(g, e, x + 15, ry + 2);
            String name = this.font.plainSubstrByWidth(Component.translatable(e.nameKey()).getString(), 170);
            g.drawString(this.font, name, x + 36, ry + 6, alpha(!ok ? GREY_LIGHT : (hot ? WHITE : 0xFF4A4A52), a), false);
            String price = ok ? e.cost() + " G" : tr("rank", "Rank %s").getString().replace("%s", com.soybe.awakeningsystem.ShopDefinitions.requiredRank(e));
            g.drawString(this.font, price, x + PW - 22 - this.font.width(price), ry + 6, alpha(!ok ? 0xFFD6473E : (hot ? WHITE : ORANGE_DARK), a), false);
        }
        int fy = y + PH - 40;
        g.fill(x + 10, fy - 4, x + PW - 10, fy - 3, alpha(0xFFC9C9CE, a));
        if (sel == null) {
            g.drawString(this.font, tr("shop_hint", "Choose something to buy."), x + 14, fy + 4, alpha(GREY_LIGHT, a), false);
            return;
        }
        shopIcon(g, sel, x + 14, fy + 4);
        g.drawString(this.font, Component.translatable(sel.nameKey()), x + 36, fy + 4, alpha(GREY_DARK, a), false);
        g.drawString(this.font, (sel.amount() > 1 ? "x" + sel.amount() + "  " : "") + sel.cost() + " G", x + 36, fy + 15, alpha(ORANGE_DARK, a), false);
        boolean ok = com.soybe.awakeningsystem.ShopDefinitions.isUnlocked(sel, d);
        boolean afford = d.coins() >= sel.cost();
        if (ok) claimButtonText(g, x + PW - 112, fy + 6, afford ? tr("buy", "Buy") : tr("no_coins", "Not enough G"), mouseX, mouseY, afford ? a : a * 0.5F);
        else g.drawString(this.font, tr("needs_rank", "Needs rank %s", com.soybe.awakeningsystem.ShopDefinitions.requiredRank(sel)), x + PW - 112, fy + 10, alpha(0xFFD6473E, a), false);
    }

    private boolean clickShop(double mx, double my) {
        int x = left(), y = top();
        int cy = y + HEADER + 6;
        for (int c = 0; c < SHOP_CATS.length; c++) {
            int w = this.font.width(Component.translatable("awakening_system.screen.shop.cat." + SHOP_CATS[c])) + 10;
            if (in(mx, my, catX(c), cy, w, 14)) {
                click();
                shopCat = SHOP_CATS[c];
                scroll = 0;
                selected = "";
                return true;
            }
        }
        var list = shopEntries();
        int lt = cy + 20;
        for (int i = 0; i < 5 && i + scroll < list.size(); i++) {
            if (in(mx, my, x + 10, lt + i * (ROW + 2), PW - 24, ROW)) {
                click();
                selected = list.get(i + scroll).id();
                return true;
            }
        }
        SystemData d = data();
        int fy = y + PH - 40;
        for (var e : list) {
            if (!e.id().equals(selected)) continue;
            if (!com.soybe.awakeningsystem.ShopDefinitions.isUnlocked(e, d) || !in(mx, my, x + PW - 112, fy + 6, 100, 15)) return false;
            if (d.coins() < e.cost()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                return true;
            }
            if (com.soybe.awakeningsystem.ShopDefinitions.isBoxId(e.id())) {
                int tier = switch (e.id()) {
                    case "box_rare" -> 2;
                    case "box_epic" -> 3;
                    case "box_legendary" -> 4;
                    case "box_mythic" -> 5;
                    case "box_primordial" -> 6;
                    default -> 1;
                };
                AwakeningSystemClient.buyShopItem(e.id());
                Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.ChestOpeningScreen(this, e.id(), e.nameKey(), tier, e.cost()));
                return true;
            }
            AwakeningSystemClient.buyShopItem(e.id());
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F));
            return true;
        }
        return false;
    }

    // ── Shop icons, Sell and Travel ──────────────────────────────────────────

    private void shopIcon(GuiGraphics g, com.soybe.awakeningsystem.ShopDefinitions.Entry e, int x, int y) {
        net.minecraft.resources.ResourceLocation box = com.soybe.awakeningsystem.SystemScreen.getShopBoxTexture(e.id());
        if (box != null) {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.blit(box, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
        } else g.renderItem(com.soybe.awakeningsystem.ShopDefinitions.createStack(e), x, y);
    }

    private static final String[] SELL_CATS = {"all", "gear", "ores", "drops", "farming", "blocks"};
    private String sellCat = "all";

    private List<com.soybe.awakeningsystem.SellDefinitions.SellEntry> sellEntries() {
        try {
            var p = Minecraft.getInstance().player;
            List<com.soybe.awakeningsystem.SellDefinitions.SellEntry> out = new ArrayList<>();
            for (var e : com.soybe.awakeningsystem.SellDefinitions.availableEntries(sellCat))
                if (p == null || com.soybe.awakeningsystem.SellDefinitions.countInInventory(p, e) > 0) out.add(e);
            return out;
        } catch (Throwable t) {
            return List.of();
        }
    }

    private int pillX(String[] cats, String prefix, int c) {
        int cx = left() + 12;
        for (int i = 0; i < c; i++) cx += this.font.width(Component.translatable(prefix + cats[i])) + 12;
        return cx;
    }

    private void pills(GuiGraphics g, String[] cats, String prefix, String current, int y, float a) {
        for (int c = 0; c < cats.length; c++) {
            Component lbl = Component.translatable(prefix + cats[c]);
            int cx = pillX(cats, prefix, c), w = this.font.width(lbl) + 8;
            boolean on = cats[c].equals(current);
            g.fillGradient(cx, y, cx + w, y + 14, alpha(on ? 0xFFF7B52A : 0xFFF4F4F6, a), alpha(on ? 0xFFE29400 : 0xFFDCDCE0, a));
            g.drawString(this.font, lbl, cx + 4, y + 3, alpha(on ? WHITE : GREY, a), false);
        }
    }

    private void renderSell(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        int cy = y + HEADER + 6;
        pills(g, SELL_CATS, "awakening_system.screen.shop.sell_cat.", sellCat, cy, a);
        var list = sellEntries();
        var p = Minecraft.getInstance().player;
        int lt = cy + 20, rows = 5;
        scroll = Mth.clamp(scroll, 0, Math.max(0, list.size() - rows));
        if (list.isEmpty()) g.drawString(this.font, tr("sell_nothing", "Nothing in your bag to sell here."), x + 14, lt + 6, alpha(GREY_LIGHT, a), false);
        for (int i = 0; i < rows && i + scroll < list.size(); i++) {
            var e = list.get(i + scroll);
            int ry = lt + i * (ROW + 2);
            boolean hot = e.id().equals(selected);
            row(g, this.font, x + 10, ry, PW - 24, ROW, Component.empty(), null, hot, a);
            g.renderItem(com.soybe.awakeningsystem.SellDefinitions.createStack(e), x + 15, ry + 2);
            int have = p == null ? 0 : com.soybe.awakeningsystem.SellDefinitions.countInInventory(p, e);
            String name = this.font.plainSubstrByWidth(Component.translatable(e.nameKey()).getString(), 150) + "  x" + have;
            g.drawString(this.font, name, x + 36, ry + 6, alpha(hot ? WHITE : 0xFF4A4A52, a), false);
            String price = "+" + e.unitPrice() + " G" + (e.amount() > 1 ? " / " + e.amount() : "");
            g.drawString(this.font, price, x + PW - 22 - this.font.width(price), ry + 6, alpha(hot ? WHITE : 0xFF4FA84A, a), false);
        }
        int fy = y + PH - 40;
        g.fill(x + 10, fy - 4, x + PW - 10, fy - 3, alpha(0xFFC9C9CE, a));
        if (selected.isEmpty() || list.stream().noneMatch(e -> e.id().equals(selected))) {
            g.drawString(this.font, tr("sell_hint", "Choose something to sell."), x + 14, fy + 4, alpha(GREY_LIGHT, a), false);
            return;
        }
        claimButtonText(g, x + PW - 222, fy + 6, tr("sell_one", "Sell some"), mouseX, mouseY, a);
        claimButtonText(g, x + PW - 112, fy + 6, tr("sell_all", "Sell all"), mouseX, mouseY, a);
    }

    private boolean clickSell(double mx, double my) {
        int x = left(), y = top(), cy = y + HEADER + 6;
        for (int c = 0; c < SELL_CATS.length; c++) {
            int w = this.font.width(Component.translatable("awakening_system.screen.shop.sell_cat." + SELL_CATS[c])) + 8;
            if (in(mx, my, pillX(SELL_CATS, "awakening_system.screen.shop.sell_cat.", c), cy, w, 14)) {
                click();
                sellCat = SELL_CATS[c];
                scroll = 0;
                selected = "";
                return true;
            }
        }
        var list = sellEntries();
        int lt = cy + 20;
        for (int i = 0; i < 5 && i + scroll < list.size(); i++) {
            if (in(mx, my, x + 10, lt + i * (ROW + 2), PW - 24, ROW)) {
                click();
                selected = list.get(i + scroll).id();
                return true;
            }
        }
        int fy = y + PH - 40;
        if (!selected.isEmpty()) {
            boolean one = in(mx, my, x + PW - 222, fy + 6, 100, 15), all = in(mx, my, x + PW - 112, fy + 6, 100, 15);
            if (one || all) {
                AwakeningSystemClient.sellShopItem(selected, all);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
                return true;
            }
        }
        return false;
    }

    private record World(String id, String key, net.minecraft.resources.ResourceLocation tex) {}

    private List<World> worlds() {
        SystemData d = data();
        boolean s40 = d.system40Updated() || d.isSystem40Updating() || d.teyvatUnlocked();
        boolean s30 = s40 || d.system30Updated() || d.isSystem30Updating() || d.murimUnlocked();
        boolean s20 = s30 || d.systemUpdated() || d.isSystemUpdating() || d.isekaiUnlocked();
        var mc = Minecraft.getInstance();
        boolean away = mc.level != null && !mc.level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD);
        List<World> out = new ArrayList<>();
        String root = "textures/gui/tab_";
        if (away || s20) out.add(new World("overworld", "earth", net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, root + "overworld.png")));
        if (s20) out.add(new World("isekai", "isekai", net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, root + "isekai.png")));
        if (s30) out.add(new World("murim", "murim", net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, root + "murim.png")));
        if (s40) out.add(new World("teyvat", "teyvat", net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, root + "teyvat.png")));
        out.add(new World("subspace", "subspace", null));
        out.add(new World("archive", "archive", null));
        return out;
    }

    private void renderTravel(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        var list = worlds();
        int lt = y + HEADER + 8;
        for (int i = 0; i < list.size(); i++) {
            World w = list.get(i);
            int ry = lt + i * (ROW + 6);
            boolean hot = in(mouseX, mouseY, x + 10, ry, PW - 24, ROW + 2);
            Component name = switch (w.id) {
                case "subspace" -> tr("subspace", "Personal subspace");
                case "archive" -> Component.translatable("awakening_system.world_archive.tab_title");
                default -> Component.translatable("awakening_system.screen.dim." + w.key + ".title");
            };
            row(g, this.font, x + 10, ry, PW - 24, ROW + 2, Component.empty(), null, hot, a);
            if (w.tex != null) {
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                g.blit(w.tex, x + 15, ry + 3, 0.0F, 0.0F, 16, 16, 16, 16);
            } else icon(g, w.id.equals("archive") ? SCROLL : DOOR, x + 18, ry + 6, alpha(hot ? WHITE : GREY, a));
            g.drawString(this.font, name, x + 38, ry + 7, alpha(hot ? WHITE : 0xFF4A4A52, a), false);
        }
    }

    private boolean clickTravel(double mx, double my) {
        var list = worlds();
        int x = left(), lt = top() + HEADER + 8;
        for (int i = 0; i < list.size(); i++) {
            if (!in(mx, my, x + 10, lt + i * (ROW + 6), PW - 24, ROW + 2)) continue;
            click();
            World w = list.get(i);
            Minecraft mc = Minecraft.getInstance();
            if (w.id.equals("archive")) {
                mc.setScreen(new com.soybe.awakeningsystem.worldarchive.WorldArchiveScreen(this));
                return true;
            }
            boolean sealed = mc.level != null && (com.soybe.awakeningsystem.penalty.PenaltyZoneManager.isPenaltyDimension(mc.level)
                    || com.soybe.awakeningsystem.limbo.LimboWorldManager.isLimboDimension(mc.level));
            if (sealed && !w.id.equals("subspace")) {
                if (mc.player != null) mc.player.displayClientMessage(Component.translatable("awakening_system.screen.dim.sealed_desc"), true);
                return true;
            }
            mc.setScreen(null);
            if (w.id.equals("subspace")) net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.OpenSubspaceC2SPayload());
            else net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.soybe.awakeningsystem.TravelDimensionPayload(w.id));
            return true;
        }
        return false;
    }

    private void claimButtonText(GuiGraphics g, int x, int y, Component text, int mouseX, int mouseY, float a) {        boolean hover = mouseX >= x && mouseX < x + 100 && mouseY >= y && mouseY < y + 15;
        g.fillGradient(x, y, x + 100, y + 15, alpha(hover ? 0xFFFFC44A : 0xFFF7B52A, a), alpha(0xFFE29400, a));
        g.drawString(this.font, text, x + 50 - this.font.width(text) / 2, y + 4, alpha(WHITE, a), false);
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
            case ARTIFACTS -> { if (clickArtifacts(mx, my)) return true; }
            case PROGRESS -> { if (clickProgress(mx, my)) return true; }
            case SHOP -> { if (clickShop(mx, my)) return true; }
            case SELL -> { if (clickSell(mx, my)) return true; }
            case TRAVEL -> { if (clickTravel(mx, my)) return true; }
            case RANKING -> {}
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
