package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.ShadowRank;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.network.ManageShadowSquadPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The shadow command in the full-dive look: the stored army by squad, each shadow's rank, level, health and
 * kills, buttons to move it between the four squads, rename a squad, and deploy or recall. The same actions as
 * the normal shadow command window.
 */
public class SaoShadowScreen extends SaoWindow {
    private static final String[] ROMAN = {"I", "II", "III", "IV"};
    private static final int ROWS = 4, ROW_H = 26;

    private int squad = 0;   // -1 = all
    private boolean renaming;
    private EditBox nameBox;

    private record Entry(int index, CompoundTag tag) {}

    public SaoShadowScreen(Screen parent) {
        super(parent, 360, 232);
    }

    private static Component key(String k) {
        return Component.translatable("gui.awakening_system.shadow_command." + k);
    }

    @Override
    protected void init() {
        super.init();
        nameBox = new EditBox(this.font, 0, 0, 150, 14, Component.empty());
        nameBox.setMaxLength(22);
        nameBox.setVisible(false);
        nameBox.setBordered(false);
        nameBox.setTextColor(0xFF484848);
        addWidget(nameBox);
    }

    @Override
    protected Component heading() {
        return Component.literal(capital(key("title").getString()));
    }

    private static String capital(String s) {
        return s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    protected String[] glyph() {
        return PEOPLE;
    }

    @Override
    protected String headerRight() {
        SystemData d = data();
        return key("stored").getString() + " " + d.storedShadowCount() + "/" + d.getMaxShadowCapacity();
    }

    private List<Entry> shown(SystemData d) {
        List<Entry> out = new ArrayList<>();
        List<CompoundTag> army = d.getStoredShadowArmy();
        for (int i = 0; i < army.size(); i++) {
            CompoundTag t = army.get(i);
            int sq = t.contains("SquadId") ? t.getInt("SquadId") : -1;
            if (squad == -1 || sq == squad) out.add(new Entry(i, t));
        }
        return out;
    }

    private int[] deployed() {
        Minecraft mc = Minecraft.getInstance();
        int all = 0, mine = 0;
        if (mc.player != null && mc.level != null) {
            String master = "shadow_master_" + mc.player.getUUID();
            for (Mob m : mc.level.getEntitiesOfClass(Mob.class, mc.player.getBoundingBox().inflate(128.0),
                    m -> m.isAlive() && m.getTags().contains("shadow_soldier") && m.getTags().contains(master))) {
                all++;
                if (squad >= 0 && com.soybe.awakeningsystem.AwakeningSystemClient.getShadowSquadId(m.getId()) == squad) mine++;
            }
        }
        return new int[]{all, mine};
    }

    // layout
    private int tabW() { return (pw - 24 - 4 * 3) / 5; }
    private int tabX(int i) { return left() + 12 + i * (tabW() + 3); }
    private int tabY() { return top() + HEADER + 8; }
    private int subY() { return top() + HEADER + 30; }
    private int listY() { return top() + HEADER + 50; }
    private int btnY() { return top() + ph - 26; }
    private int btnW() { return (pw - 24 - 3 * 4) / 4; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        int[] dep = deployed();

        // squad tabs
        for (int i = -1; i < 4; i++) {
            String label = i == -1 ? key("tab_all").getString() + " " + d.storedShadowCount()
                    : ROMAN[i] + " · " + d.getShadowCountInSquad(i);
            softButton(g, tabX(i + 1), tabY(), tabW(), 15, Component.literal(label), squad == i, mouseX, mouseY, a);
        }

        // the chosen squad: its name (or the box to rename it) and how many are out
        int sy = subY();
        String field = key("in_field").getString();
        if (renaming) {
            g.fillGradient(x + 12, sy, x + 170, sy + 15, alpha(0xFFFFFFFF, a), alpha(0xFFF0F0F3, a));
            g.fill(x + 12, sy + 14, x + 170, sy + 15, alpha(ORANGE, a));
            nameBox.setX(x + 16);
            nameBox.setY(sy + 4);
            nameBox.render(g, mouseX, mouseY, 0);
            button(g, x + 176, sy, 60, 15, key("save"), true, mouseX, mouseY, a);
            softButton(g, x + 240, sy, 60, 15, key("cancel"), false, mouseX, mouseY, a);
        } else {
            String name = squad == -1 ? key("all_shadows").getString() : ROMAN[squad] + ". " + d.getShadowSquadName(squad);
            String line = name + "  ·  " + (squad == -1 ? dep[0] : dep[1]) + " " + field;
            g.drawString(this.font, this.font.plainSubstrByWidth(line, pw - 120), x + 14, sy + 4, alpha(GREY_DARK, a), false);
            if (squad >= 0) softButton(g, x + pw - 94, sy, 82, 15, key("rename"), false, mouseX, mouseY, a);
        }

        // the shadows
        List<Entry> list = shown(d);
        int max = Math.max(0, list.size() - ROWS);
        scroll = Math.min(scroll, max);
        int ly = listY();
        if (list.isEmpty()) {
            Component empty1 = key("no_shadows");
            Component empty2 = key("no_shadows_sub");
            g.drawString(this.font, empty1, x + (pw - this.font.width(empty1)) / 2, ly + 30, alpha(GREY_DARK, a), false);
            g.drawString(this.font, empty2, x + (pw - this.font.width(empty2)) / 2, ly + 44, alpha(GREY, a), false);
        }
        for (int r = 0; r < ROWS && scroll + r < list.size(); r++) {
            Entry e = list.get(scroll + r);
            CompoundTag t = e.tag();
            int ry = ly + r * ROW_H, rw = pw - 28;
            boolean hot = in(mouseX, mouseY, x + 12, ry, rw, ROW_H - 3);
            card(g, x + 12, ry, rw, ROW_H - 3, hot, a);
            ShadowRank rank = ShadowRank.byLevel(t.contains("ShadowRank") ? t.getInt("ShadowRank") : 0);
            String rk = Component.translatable(rank.getLangKey()).getString();
            g.drawString(this.font, rk, x + 18, ry + 4, alpha(saoRankColor(rank), a), false);
            String name = t.contains("ShadowBaseName") ? t.getString("ShadowBaseName") : t.contains("MobName") ? t.getString("MobName") : "Shadow";
            int lvl = t.getInt("RpgMobLevel");
            String title = (lvl > 0 ? tr("level", "Lv. %s", lvl).getString() + "  " : "") + name;
            int nx = x + 22 + this.font.width(rk);
            g.drawString(this.font, this.font.plainSubstrByWidth(title, rw - 110 - (nx - x - 12)), nx, ry + 4, alpha(GREY_DARK, a), false);
            float hp = t.getFloat("Health"), maxHp = t.getFloat("MaxHealth");
            if (maxHp <= 0) maxHp = 20;
            bar(g, x + 18, ry + 15, 90, 3, Math.max(0, Math.min(1, hp / maxHp)), hpColor(hp / maxHp), a);
            g.pose().pushPose();
            g.pose().translate(x + 112, ry + 13.5F, 0);
            g.pose().scale(0.75F, 0.75F, 1);
            g.drawString(this.font, key("kills").getString() + " " + t.getInt("ShadowKills"), 0, 0, alpha(GREY, a), false);
            g.pose().popPose();
            int cur = t.contains("SquadId") ? t.getInt("SquadId") : -1;
            for (int s = 0; s < 4; s++)
                softButton(g, pillX(s), ry + 4, 18, 14, Component.literal(ROMAN[s]), s == cur, mouseX, mouseY, a);
            boolean hovDel = in(mouseX, mouseY, delX(), ry + 4, 16, 14);
            g.fill(delX(), ry + 4, delX() + 16, ry + 18, alpha(hovDel ? 0xFFD94A4A : 0xFFEAEAEF, a));
            g.drawCenteredString(this.font, "✕", delX() + 8, ry + 7, alpha(hovDel ? 0xFFFFFFFF : 0xFFC04040, a));
        }
        scrollBar(g, x + pw - 12, ly, ROWS * ROW_H - 3, list.size(), ROWS, a);

        // commands
        int by = btnY(), bw = btnW();
        button(g, x + 12, by, bw, 18, key(squad >= 0 ? "deploy_squad" : "deploy_all"), true, mouseX, mouseY, a);
        softButton(g, x + 12 + (bw + 4), by, bw, 18, key(squad >= 0 ? "recall_squad" : "recall_all"), false, mouseX, mouseY, a);
        button(g, x + 12 + 2 * (bw + 4), by, bw, 18, key("deploy_all"), true, mouseX, mouseY, a);
        softButton(g, x + 12 + 3 * (bw + 4), by, bw, 18, key("recall_all"), false, mouseX, mouseY, a);
    }

    private static int saoRankColor(ShadowRank rank) {
        return switch (rank) {
            case GRAND_MARSHAL -> 0xFFB86B00;
            case COMMANDER -> 0xFFC02828;
            case KNIGHT -> 0xFF7B2CBF;
            case ELITE -> 0xFF1D6FA5;
            case SOLDIER -> 0xFF2B6CB0;
            default -> 0xFF4A5568;
        };
    }

    private int pillX(int s) {
        return left() + pw - 36 - 4 * 20 + s * 20;
    }

    private int delX() {
        return left() + pw - 32;
    }

    private void send(int action, int target, int index, String text) {
        PacketDistributor.sendToServer(new ManageShadowSquadPayload(action, target, index, text));
    }

    private void saveName() {
        String n = nameBox.getValue().trim();
        if (!n.isEmpty() && squad >= 0) {
            send(ManageShadowSquadPayload.ACTION_RENAME_SQUAD, squad, 0, n);
            data().setShadowSquadName(squad, n);
        }
        renaming = false;
        nameBox.setVisible(false);
        SaoSounds.confirm();
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        for (int i = -1; i < 4; i++) {
            if (in(mx, my, tabX(i + 1), tabY(), tabW(), 15)) {
                squad = i;
                scroll = 0;
                renaming = false;
                nameBox.setVisible(false);
                send(ManageShadowSquadPayload.ACTION_SELECT_SQUAD, i, 0, "");
                SaoSounds.click();
                return true;
            }
        }
        int sy = subY();
        if (renaming) {
            if (in(mx, my, x + 176, sy, 60, 15)) { saveName(); return true; }
            if (in(mx, my, x + 240, sy, 60, 15)) { renaming = false; nameBox.setVisible(false); SaoSounds.back(); return true; }
            if (in(mx, my, x + 12, sy, 158, 15)) { nameBox.setFocused(true); return true; }
        } else if (squad >= 0 && in(mx, my, x + pw - 94, sy, 82, 15)) {
            renaming = true;
            nameBox.setValue(data().getShadowSquadName(squad));
            nameBox.setVisible(true);
            nameBox.setFocused(true);
            SaoSounds.click();
            return true;
        }
        SystemData d = data();
        List<Entry> list = shown(d);
        int ly = listY();
        for (int r = 0; r < ROWS && scroll + r < list.size(); r++) {
            Entry e = list.get(scroll + r);
            int ry = ly + r * ROW_H;
            int cur = e.tag().contains("SquadId") ? e.tag().getInt("SquadId") : -1;
            for (int s = 0; s < 4; s++) {
                if (s != cur && in(mx, my, pillX(s), ry + 4, 18, 14)) {
                    send(ManageShadowSquadPayload.ACTION_MOVE_SHADOW, s, e.index(), "");
                    d.setShadowSquad(e.index(), s);
                    e.tag().putInt("SquadId", s);
                    SaoSounds.click();
                    return true;
                }
            }
            if (in(mx, my, delX(), ry + 4, 16, 14)) {
                send(ManageShadowSquadPayload.ACTION_DELETE_SHADOW, 0, e.index(), "");
                if (e.index() >= 0 && e.index() < d.getStoredShadowArmy().size()) {
                    d.getStoredShadowArmy().remove(e.index());
                }
                SaoSounds.back();
                return true;
            }
        }
        int by = btnY(), bw = btnW(), target = Math.max(0, squad);
        for (int b = 0; b < 4; b++) {
            if (!in(mx, my, x + 12 + b * (bw + 4), by, bw, 18)) continue;
            int action = switch (b) {
                case 0 -> squad >= 0 ? ManageShadowSquadPayload.ACTION_DEPLOY_SQUAD : ManageShadowSquadPayload.ACTION_DEPLOY_ALL;
                case 1 -> squad >= 0 ? ManageShadowSquadPayload.ACTION_RECALL_SQUAD : ManageShadowSquadPayload.ACTION_RECALL_ALL;
                case 2 -> ManageShadowSquadPayload.ACTION_DEPLOY_ALL;
                default -> ManageShadowSquadPayload.ACTION_RECALL_ALL;
            };
            send(action, b >= 2 ? 0 : target, 0, "");
            SaoSounds.confirm();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (renaming) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { saveName(); return true; }
            if (key == GLFW.GLFW_KEY_ESCAPE) { renaming = false; nameBox.setVisible(false); return true; }
            if (nameBox.keyPressed(key, scan, mods)) return true;
            return true;   // typing a name: no other keys
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (renaming && nameBox.charTyped(c, mods)) return true;
        return super.charTyped(c, mods);
    }
}
