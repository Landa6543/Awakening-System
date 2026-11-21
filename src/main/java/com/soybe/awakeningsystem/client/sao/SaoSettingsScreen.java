package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.client.gui.GuiConfig;
import com.soybe.awakeningsystem.client.gui.HudEditorScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The interface settings in the full-dive look: the same three pages (window, System tabs, HUD) and the same
 * switches as the normal settings window, as rows of white cards with an orange pill for what is on.
 */
public class SaoSettingsScreen extends SaoWindow {
    /** A row: a switch, a stepper (− value +) or a button that does something and shows its current choice. */
    private record Opt(Component label, BooleanSupplier on, Supplier<String> value, Runnable act, Runnable dec, Runnable inc) {
        static Opt toggle(Component label, BooleanSupplier on, Runnable flip) { return new Opt(label, on, null, flip, null, null); }
        static Opt step(Component label, Supplier<String> value, Runnable dec, Runnable inc) { return new Opt(label, null, value, null, dec, inc); }
        static Opt action(Component label, Supplier<String> value, Runnable act) { return new Opt(label, null, value, act, null, null); }
    }

    private static final int COLS = 2, ROW_H = 18;
    private int page = 0;

    public SaoSettingsScreen(Screen parent) {
        super(parent, 360, 232);
    }

    private static Component k(String key) {
        return Component.translatable("awakening_system.gui_config." + key);
    }

    @Override
    protected Component heading() {
        return tr("interface", "Interface");
    }

    @Override
    protected String[] glyph() {
        return GEAR;
    }

    private static String pct(float v) {
        return Math.round(v * 100) + "%";
    }

    private static Runnable saved(Runnable r) {
        return () -> { r.run(); GuiConfig.save(); };
    }

    private List<Opt> options() {
        List<Opt> o = new ArrayList<>();
        switch (page) {
            case 0 -> {
                o.add(Opt.step(k("system.gui_scale"), () -> pct(GuiConfig.systemGuiScale),
                        saved(() -> GuiConfig.systemGuiScale = (float) Math.max(0.5, GuiConfig.systemGuiScale - 0.05)),
                        saved(() -> GuiConfig.systemGuiScale = (float) Math.min(1.8, GuiConfig.systemGuiScale + 0.05))));
                o.add(Opt.action(k("system.center_btn"), () -> GuiConfig.systemGuiOffsetX + ", " + GuiConfig.systemGuiOffsetY,
                        saved(() -> { GuiConfig.systemGuiOffsetX = 0; GuiConfig.systemGuiOffsetY = 0; })));
                o.add(Opt.toggle(k("system.toggle_3d"), () -> GuiConfig.showPlayer3DModel, saved(() -> GuiConfig.showPlayer3DModel = !GuiConfig.showPlayer3DModel)));
                o.add(Opt.toggle(k("system.toggle_side_tabs"), () -> GuiConfig.showDimensionSideTabs, saved(() -> GuiConfig.showDimensionSideTabs = !GuiConfig.showDimensionSideTabs)));
                o.add(Opt.toggle(k("system.toggle_free_move"), () -> GuiConfig.freeMoveEnabled, saved(() -> GuiConfig.freeMoveEnabled = !GuiConfig.freeMoveEnabled)));
                o.add(Opt.toggle(k("system.toggle_sfx"), () -> GuiConfig.uiSoundsEnabled, saved(() -> GuiConfig.uiSoundsEnabled = !GuiConfig.uiSoundsEnabled)));
                o.add(Opt.toggle(k("system.toggle_font"), () -> GuiConfig.useSystemFont, saved(() -> GuiConfig.useSystemFont = !GuiConfig.useSystemFont)));
                o.add(Opt.toggle(tr("style_toggle", "Full-dive menu style"), UiStyle::fullDive, () -> UiStyle.setFullDive(!UiStyle.fullDive())));
                o.add(Opt.toggle(k("system.toggle_ghost"), com.soybe.awakeningsystem.client.GhostPreferenceClient::ghostOnDeath,
                        com.soybe.awakeningsystem.client.GhostPreferenceClient::toggle));
            }
            case 1 -> {
                tab(o, "status", () -> GuiConfig.showTabStatus, v -> GuiConfig.showTabStatus = v);
                tab(o, "skills", () -> GuiConfig.showTabSkills, v -> GuiConfig.showTabSkills = v);
                tab(o, "missions", () -> GuiConfig.showTabMissions, v -> GuiConfig.showTabMissions = v);
                tab(o, "titles", () -> GuiConfig.showTabTitles, v -> GuiConfig.showTabTitles = v);
                tab(o, "shop", () -> GuiConfig.showTabShop, v -> GuiConfig.showTabShop = v);
                tab(o, "progress", () -> GuiConfig.showTabProgress, v -> GuiConfig.showTabProgress = v);
                tab(o, "ranking", () -> GuiConfig.showTabRanking, v -> GuiConfig.showTabRanking = v);
                tab(o, "artifacts", () -> GuiConfig.showTabArtifacts, v -> GuiConfig.showTabArtifacts = v);
            }
            default -> {
                o.add(Opt.step(k("hud.scale"), () -> pct(GuiConfig.hudScale),
                        saved(() -> GuiConfig.hudScale = (float) Math.max(0.5, GuiConfig.hudScale - 0.05)),
                        saved(() -> GuiConfig.hudScale = (float) Math.min(2.0, GuiConfig.hudScale + 0.05))));
                o.add(Opt.step(k("hud.popup_scale"), () -> pct(GuiConfig.constellationNotifScale),
                        saved(() -> GuiConfig.constellationNotifScale = Math.max(0.4F, GuiConfig.constellationNotifScale - 0.05F)),
                        saved(() -> GuiConfig.constellationNotifScale = Math.min(1.5F, GuiConfig.constellationNotifScale + 0.05F))));
                o.add(Opt.action(Component.literal("❤"), () -> GuiConfig.healthDisplayMode.getComponent().getString(), saved(() ->
                        GuiConfig.healthDisplayMode = switch (GuiConfig.healthDisplayMode) {
                            case CARD -> GuiConfig.HealthDisplayMode.VANILLA;
                            case COMPACT_BAR -> GuiConfig.HealthDisplayMode.CARD;
                            case VANILLA -> GuiConfig.HealthDisplayMode.COMPACT_BAR;
                        })));
                o.add(Opt.action(Component.literal("🍗"), () -> GuiConfig.foodDisplayMode.getComponent().getString(), saved(() -> {
                    GuiConfig.foodDisplayMode = switch (GuiConfig.foodDisplayMode) {
                        case BAR -> GuiConfig.FoodDisplayMode.VANILLA;
                        case VANILLA -> GuiConfig.FoodDisplayMode.OFF;
                        case OFF -> GuiConfig.FoodDisplayMode.BAR;
                    };
                    GuiConfig.showHudFood = GuiConfig.isHudFoodBarVisible();
                })));
                o.add(Opt.action(k("hud.move_btn"), () -> "", () -> HudEditorScreen.open(this)));
                o.add(Opt.action(k("hud.reset_coords"), () -> "", GuiConfig::resetHudPositions));
                o.add(Opt.toggle(k("hud.toggle_status_card"), () -> GuiConfig.showHudStatusCard, saved(() -> GuiConfig.showHudStatusCard = !GuiConfig.showHudStatusCard)));
                o.add(Opt.toggle(k("hud.toggle_skill_bar"), () -> GuiConfig.showHudSkillBar, saved(() -> GuiConfig.showHudSkillBar = !GuiConfig.showHudSkillBar)));
                o.add(Opt.toggle(k("hud.toggle_appraisal"), () -> GuiConfig.showHudAppraisal, saved(() -> GuiConfig.showHudAppraisal = !GuiConfig.showHudAppraisal)));
                o.add(Opt.toggle(k("hud.toggle_combo"), () -> GuiConfig.showHudCombo, saved(() -> GuiConfig.showHudCombo = !GuiConfig.showHudCombo)));
                o.add(Opt.toggle(k("hud.toggle_constellation_popups"), () -> GuiConfig.showConstellationPopups, saved(() -> GuiConfig.showConstellationPopups = !GuiConfig.showConstellationPopups)));
                o.add(Opt.toggle(k("hud.toggle_compact_constellations"), () -> GuiConfig.compactConstellationPopups, saved(() -> GuiConfig.compactConstellationPopups = !GuiConfig.compactConstellationPopups)));
                o.add(Opt.toggle(k("hud.toggle_dokkaebi_scenario"), () -> GuiConfig.showHudDokkaebiScenario, saved(() -> GuiConfig.showHudDokkaebiScenario = !GuiConfig.showHudDokkaebiScenario)));
                o.add(Opt.toggle(k("hud.toggle_minimap"), () -> dev.diorama.Settings.get().minimap, () -> {
                    dev.diorama.Settings ds = dev.diorama.Settings.get();
                    ds.minimap = !ds.minimap;
                    ds.save();
                }));
            }
        }
        return o;
    }

    private static void tab(List<Opt> o, String id, BooleanSupplier on, java.util.function.Consumer<Boolean> set) {
        o.add(Opt.toggle(Component.translatable("awakening_system.screen.tab." + id), on, () -> {
            if (on.getAsBoolean() && GuiConfig.countActiveTabs() <= 1) return;   // the last tab stays
            set.accept(!on.getAsBoolean());
            GuiConfig.save();
        }));
    }

    // layout
    private int pageW() { return (pw - 24 - 2 * 3) / 3; }
    private int pageX(int i) { return left() + 12 + i * (pageW() + 3); }
    private int pageY() { return top() + HEADER + 8; }
    private int colW() { return (pw - 24 - 4) / 2; }
    private int optX(int i) { return left() + 12 + (i % COLS) * (colW() + 4); }
    private int optY(int i) { return top() + HEADER + 30 + (i / COLS) * ROW_H; }
    private int footY() { return top() + ph - 24; }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        String[] pages = {"tab.system", "tab.tabs", "tab.hud"};
        for (int i = 0; i < 3; i++) softButton(g, pageX(i), pageY(), pageW(), 15, k(pages[i]), page == i, mouseX, mouseY, a);
        List<Opt> opts = options();
        int cw = colW();
        for (int i = 0; i < opts.size(); i++) {
            Opt o = opts.get(i);
            int ox = optX(i), oy = optY(i);
            boolean hot = in(mouseX, mouseY, ox, oy, cw, ROW_H - 2);
            card(g, ox, oy, cw, ROW_H - 2, hot && o.dec() == null, a);
            if (o.on() != null) {
                boolean on = o.on().getAsBoolean();
                int pillW = 26;
                g.drawString(this.font, this.font.plainSubstrByWidth(o.label().getString(), cw - pillW - 14), ox + 7, oy + 4, alpha(on ? GREY_DARK : GREY, a), false);
                int px = ox + cw - pillW - 4;
                g.fillGradient(px, oy + 3, px + pillW, oy + 13, alpha(on ? 0xFFF7B52A : 0xFFE2E2E6, a), alpha(on ? 0xFFE29400 : 0xFFD0D0D6, a));
                String s = k(on ? "toggle.on" : "toggle.off").getString().replaceAll("§.", "");
                g.drawString(this.font, s, px + pillW / 2 - this.font.width(s) / 2, oy + 4, alpha(on ? WHITE : GREY, a), false);
            } else if (o.dec() != null) {
                String v = o.value().get();
                g.drawString(this.font, this.font.plainSubstrByWidth(o.label().getString(), cw - 90), ox + 7, oy + 4, alpha(GREY_DARK, a), false);
                softButton(g, ox + cw - 78, oy + 2, 14, 12, Component.literal("-"), false, mouseX, mouseY, a);
                g.drawString(this.font, v, ox + cw - 42 - this.font.width(v) / 2, oy + 4, alpha(ORANGE_DARK, a), false);
                softButton(g, ox + cw - 20, oy + 2, 14, 12, Component.literal("+"), false, mouseX, mouseY, a);
            } else {
                String v = o.value().get().replaceAll("§.", "");
                String label = o.label().getString();
                g.drawString(this.font, this.font.plainSubstrByWidth(label, cw - 12 - (v.isEmpty() ? 0 : this.font.width(v) + 8)), ox + 7, oy + 4, alpha(GREY_DARK, a), false);
                if (!v.isEmpty()) g.drawString(this.font, v, ox + cw - 6 - this.font.width(v), oy + 4, alpha(ORANGE_DARK, a), false);
            }
        }
        softButton(g, x + 12, footY(), 110, 16, k("btn.reset"), false, mouseX, mouseY, a);
        button(g, x + pw - 122, footY(), 110, 16, k("btn.save"), true, mouseX, mouseY, a);
    }

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        for (int i = 0; i < 3; i++) {
            if (in(mx, my, pageX(i), pageY(), pageW(), 15)) { page = i; SaoSounds.click(); return true; }
        }
        List<Opt> opts = options();
        int cw = colW();
        for (int i = 0; i < opts.size(); i++) {
            Opt o = opts.get(i);
            int ox = optX(i), oy = optY(i);
            if (o.dec() != null) {
                if (in(mx, my, ox + cw - 78, oy + 2, 14, 12)) { o.dec().run(); SaoSounds.click(); return true; }
                if (in(mx, my, ox + cw - 20, oy + 2, 14, 12)) { o.inc().run(); SaoSounds.click(); return true; }
            } else if (in(mx, my, ox, oy, cw, ROW_H - 2)) {
                o.act().run();
                SaoSounds.click();
                return true;
            }
        }
        if (in(mx, my, x + 12, footY(), 110, 16)) { GuiConfig.resetDefaults(); GuiConfig.save(); SaoSounds.click(); return true; }
        if (in(mx, my, x + pw - 122, footY(), 110, 16)) { GuiConfig.save(); SaoSounds.confirm(); onClose(); return true; }
        return false;
    }
}
