package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.GuiConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Official client-side configuration for Awakening System (config/awakening_system-client.toml).
 * Allows players and modpack creators to enable or disable specific HUD components
 * (active skill bar, status card, mob appraisal overlay, combo counter, constellation popups, etc.),
 * configure which tabs are visible in the System window (/system),
 * and adjust scales and interface behaviors globally.
 */
public final class SystemClientConfig {
    public static final ModConfigSpec SPEC;

    // HUD Elements
    public static final ModConfigSpec.BooleanValue SHOW_STATUS_CARD;
    public static final ModConfigSpec.BooleanValue SHOW_SKILL_BAR;
    public static final ModConfigSpec.BooleanValue SHOW_APPRAISAL_OVERLAY;
    public static final ModConfigSpec.BooleanValue SHOW_COMBO_COUNTER;
    public static final ModConfigSpec.BooleanValue SHOW_DOKKAEBI_SCENARIO;
    public static final ModConfigSpec.BooleanValue SHOW_CONSTELLATION_POPUPS;
    public static final ModConfigSpec.ConfigValue<String> HEALTH_DISPLAY_MODE;
    public static final ModConfigSpec.ConfigValue<String> FOOD_DISPLAY_MODE;
    public static final ModConfigSpec.DoubleValue HUD_SCALE;

    // Notifications
    public static final ModConfigSpec.DoubleValue NOTIFICATION_SCALE;
    public static final ModConfigSpec.BooleanValue COMPACT_CONSTELLATION_POPUPS;

    // System GUI Window Settings (/system)
    public static final ModConfigSpec.DoubleValue WINDOW_SCALE;
    public static final ModConfigSpec.BooleanValue SHOW_3D_PLAYER_MODEL;
    public static final ModConfigSpec.BooleanValue SHOW_DIMENSION_SIDE_TABS;
    public static final ModConfigSpec.BooleanValue UI_SOUNDS;
    public static final ModConfigSpec.BooleanValue FREE_DRAGGING;

    // System Window Tabs (/system)
    public static final ModConfigSpec.BooleanValue SHOW_TAB_STATUS;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_SKILLS;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_MISSIONS;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_TITLES;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_SHOP;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_PROGRESS;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_RANKING;
    public static final ModConfigSpec.BooleanValue SHOW_TAB_ARTIFACTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("=====================================================",
                        " Awakening System - Client HUD & Interface Settings",
                        "=====================================================").push("hud");

        SHOW_STATUS_CARD = builder.comment("Enable or disable the Hunter Status Card (HP, MP, Level, Job overlay).",
                        "Set to false if you only want skills or vanilla bars.")
                .define("show_status_card", true);

        SHOW_SKILL_BAR = builder.comment("Enable or disable the Active Skill Bar HUD element.")
                .define("show_skill_bar", true);

        SHOW_APPRAISAL_OVERLAY = builder.comment("Enable or disable the Mob Appraisal overlay when looking at entities.")
                .define("show_appraisal_overlay", true);

        SHOW_COMBO_COUNTER = builder.comment("Enable or disable the Combat Combo Counter overlay.")
                .define("show_combo_counter", true);

        SHOW_DOKKAEBI_SCENARIO = builder.comment("Enable or disable the Dokkaebi Sub-Scenario quest tracker overlay.")
                .define("show_dokkaebi_scenario", true);

        SHOW_CONSTELLATION_POPUPS = builder.comment("Enable or disable Constellation sponsor and reaction message popups.")
                .define("show_constellation_popups", true);

        HEALTH_DISPLAY_MODE = builder.comment("Health display mode:",
                        "\"CARD\" - Holographic Status Card with circular HP/MP orbs.",
                        "\"VANILLA\" - Vanilla Minecraft Hearts with compact MP bar above hotbar.",
                        "\"COMPACT_BAR\" - Sleek compact HP/MP bars above hotbar.")
                .define("health_display_mode", "CARD");

        FOOD_DISPLAY_MODE = builder.comment("Food / Hunger display mode:",
                        "\"BAR\" - Custom orange hunger bar integrated in Status Card / Compact Hotbar.",
                        "\"VANILLA\" - Classic Minecraft hunger drumsticks (muslitos vainilla).",
                        "\"BOTH\" - Both custom bar and vanilla drumsticks.",
                        "\"OFF\" - Hunger is hidden from HUD.")
                .define("food_display_mode", "BAR");

        HUD_SCALE = builder.comment("Overall scale multiplier for HUD elements (0.5 to 2.0).")
                .defineInRange("hud_scale", 1.0D, 0.5D, 2.0D);

        builder.pop();

        builder.comment("Notification Popups").push("notifications");

        NOTIFICATION_SCALE = builder.comment("Scale multiplier for Constellation notifications (0.4 to 1.5).")
                .defineInRange("notification_scale", 0.85D, 0.4D, 1.5D);

        COMPACT_CONSTELLATION_POPUPS = builder.comment("Whether Constellation popups should use compact styling.")
                .define("compact_constellation_popups", false);

        builder.pop();

        builder.comment("System GUI Window Settings (/system)").push("system_window");

        WINDOW_SCALE = builder.comment("Scale multiplier for the main System GUI window (0.5 to 1.8).")
                .defineInRange("window_scale", 1.0D, 0.5D, 1.8D);

        SHOW_3D_PLAYER_MODEL = builder.comment("Show 3D animated player preview inside the System window.")
                .define("show_3d_player_model", true);

        SHOW_DIMENSION_SIDE_TABS = builder.comment("Show quick-navigation side tabs (Subspace, Guild, Dungeons, etc.).")
                .define("show_dimension_side_tabs", true);

        UI_SOUNDS = builder.comment("Play interface sound effects on clicks and tabs.")
                .define("ui_sounds", true);

        FREE_DRAGGING = builder.comment("Allow free dragging of the System window anywhere on screen with mouse.")
                .define("free_dragging", true);

        builder.comment("Visible Tabs in the System Window (/system)").push("tabs");
        SHOW_TAB_STATUS = builder.comment("Show or hide the Status & Attributes tab.")
                .define("show_tab_status", true);
        SHOW_TAB_SKILLS = builder.comment("Show or hide the Skills tab.")
                .define("show_tab_skills", true);
        SHOW_TAB_MISSIONS = builder.comment("Show or hide the Missions & Daily Quests tab.")
                .define("show_tab_missions", true);
        SHOW_TAB_TITLES = builder.comment("Show or hide the Titles & Perks tab.")
                .define("show_tab_titles", true);
        SHOW_TAB_SHOP = builder.comment("Show or hide the System Shop tab.")
                .define("show_tab_shop", true);
        SHOW_TAB_PROGRESS = builder.comment("Show or hide the Progress & Achievements tab.")
                .define("show_tab_progress", true);
        SHOW_TAB_RANKING = builder.comment("Show or hide the Ranking / Leaderboard tab.")
                .define("show_tab_ranking", true);
        SHOW_TAB_ARTIFACTS = builder.comment("Show or hide the Artifacts tab.")
                .define("show_tab_artifacts", true);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

    public static void syncToGuiConfig() {
        if (!SPEC.isLoaded()) return;
        try {
            GuiConfig.showHudStatusCard = SHOW_STATUS_CARD.get();
            GuiConfig.showHudSkillBar = SHOW_SKILL_BAR.get();
            GuiConfig.showHudAppraisal = SHOW_APPRAISAL_OVERLAY.get();
            GuiConfig.showHudCombo = SHOW_COMBO_COUNTER.get();
            GuiConfig.showHudDokkaebiScenario = SHOW_DOKKAEBI_SCENARIO.get();
            GuiConfig.showConstellationPopups = SHOW_CONSTELLATION_POPUPS.get();

            String mode = HEALTH_DISPLAY_MODE.get();
            if (mode != null) {
                try {
                    GuiConfig.healthDisplayMode = GuiConfig.HealthDisplayMode.valueOf(mode.toUpperCase(java.util.Locale.ROOT));
                } catch (Exception ignored) {
                    GuiConfig.healthDisplayMode = GuiConfig.HealthDisplayMode.CARD;
                }
            }

            String foodMode = FOOD_DISPLAY_MODE.get();
            if (foodMode != null) {
                try {
                    GuiConfig.foodDisplayMode = GuiConfig.FoodDisplayMode.valueOf(foodMode.toUpperCase(java.util.Locale.ROOT));
                } catch (Exception ignored) {
                    GuiConfig.foodDisplayMode = GuiConfig.FoodDisplayMode.BAR;
                }
            }

            GuiConfig.hudScale = (float) (double) HUD_SCALE.get();
            GuiConfig.constellationNotifScale = (float) (double) NOTIFICATION_SCALE.get();
            GuiConfig.compactConstellationPopups = COMPACT_CONSTELLATION_POPUPS.get();

            GuiConfig.systemGuiScale = (float) (double) WINDOW_SCALE.get();
            GuiConfig.showPlayer3DModel = SHOW_3D_PLAYER_MODEL.get();
            GuiConfig.showDimensionSideTabs = SHOW_DIMENSION_SIDE_TABS.get();
            GuiConfig.uiSoundsEnabled = UI_SOUNDS.get();
            GuiConfig.freeMoveEnabled = FREE_DRAGGING.get();

            GuiConfig.showTabStatus = SHOW_TAB_STATUS.get();
            GuiConfig.showTabSkills = SHOW_TAB_SKILLS.get();
            GuiConfig.showTabMissions = SHOW_TAB_MISSIONS.get();
            GuiConfig.showTabTitles = SHOW_TAB_TITLES.get();
            GuiConfig.showTabShop = SHOW_TAB_SHOP.get();
            GuiConfig.showTabProgress = SHOW_TAB_PROGRESS.get();
            GuiConfig.showTabRanking = SHOW_TAB_RANKING.get();
            GuiConfig.showTabArtifacts = SHOW_TAB_ARTIFACTS.get();
        } catch (Exception ignored) {}
    }

    public static void syncFromGuiConfig() {
        if (!SPEC.isLoaded()) return;
        try {
            SHOW_STATUS_CARD.set(GuiConfig.showHudStatusCard);
            SHOW_SKILL_BAR.set(GuiConfig.showHudSkillBar);
            SHOW_APPRAISAL_OVERLAY.set(GuiConfig.showHudAppraisal);
            SHOW_COMBO_COUNTER.set(GuiConfig.showHudCombo);
            SHOW_DOKKAEBI_SCENARIO.set(GuiConfig.showHudDokkaebiScenario);
            SHOW_CONSTELLATION_POPUPS.set(GuiConfig.showConstellationPopups);
            HEALTH_DISPLAY_MODE.set(GuiConfig.healthDisplayMode.name());
            FOOD_DISPLAY_MODE.set(GuiConfig.foodDisplayMode.name());
            HUD_SCALE.set((double) GuiConfig.hudScale);
            NOTIFICATION_SCALE.set((double) GuiConfig.constellationNotifScale);
            COMPACT_CONSTELLATION_POPUPS.set(GuiConfig.compactConstellationPopups);
            WINDOW_SCALE.set((double) GuiConfig.systemGuiScale);
            SHOW_3D_PLAYER_MODEL.set(GuiConfig.showPlayer3DModel);
            SHOW_DIMENSION_SIDE_TABS.set(GuiConfig.showDimensionSideTabs);
            UI_SOUNDS.set(GuiConfig.uiSoundsEnabled);
            FREE_DRAGGING.set(GuiConfig.freeMoveEnabled);

            SHOW_TAB_STATUS.set(GuiConfig.showTabStatus);
            SHOW_TAB_SKILLS.set(GuiConfig.showTabSkills);
            SHOW_TAB_MISSIONS.set(GuiConfig.showTabMissions);
            SHOW_TAB_TITLES.set(GuiConfig.showTabTitles);
            SHOW_TAB_SHOP.set(GuiConfig.showTabShop);
            SHOW_TAB_PROGRESS.set(GuiConfig.showTabProgress);
            SHOW_TAB_RANKING.set(GuiConfig.showTabRanking);
            SHOW_TAB_ARTIFACTS.set(GuiConfig.showTabArtifacts);

            SPEC.save();
        } catch (Exception ignored) {}
    }
}
