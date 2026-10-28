package com.soybe.awakeningsystem.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Configuración persistente del cliente para la GUI y el HUD.
 * Permite redimensionar, mover con el ratón y activar/desactivar componentes de la interfaz.
 */
public final class GuiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public enum HealthDisplayMode {
        CARD("Holographic Card"),
        VANILLA("Vanilla Hearts (+MP)"),
        COMPACT_BAR("Compact Hotbar Bars");

        private final String label;

        HealthDisplayMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public net.minecraft.network.chat.Component getComponent() {
            return net.minecraft.network.chat.Component.translatable("awakening_system.gui_config.health_mode." + name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    public enum FoodDisplayMode {
        BAR("System Bar"),
        VANILLA("Vanilla Drumsticks"),
        OFF("Hidden");

        private final String label;

        FoodDisplayMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public net.minecraft.network.chat.Component getComponent() {
            return net.minecraft.network.chat.Component.translatable("awakening_system.gui_config.food_mode." + name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    public static float systemGuiScale = 1.0F;
    public static int systemGuiOffsetX = 0;
    public static int systemGuiOffsetY = 0;
    public static boolean showPlayer3DModel = true;
    public static boolean showDimensionSideTabs = true;
    public static boolean uiSoundsEnabled = true;
    public static boolean freeMoveEnabled = true;
    public static final boolean enableBackgroundBlur = false;

    // Tabs of System Window (/system)
    public static boolean showTabStatus = true;
    public static boolean showTabSkills = true;
    public static boolean showTabMissions = true;
    public static boolean showTabTitles = true;
    public static boolean showTabShop = true;
    public static boolean showTabProgress = true;
    public static boolean showTabRanking = true;
    public static boolean showTabArtifacts = true;

    public static int countActiveTabs() {
        int count = 0;
        if (showTabStatus) count++;
        if (showTabSkills) count++;
        if (showTabMissions) count++;
        if (showTabTitles) count++;
        if (showTabShop) count++;
        if (showTabProgress) count++;
        if (showTabRanking) count++;
        if (showTabArtifacts) count++;
        return count;
    }

    public static boolean showHudStatusCard = true;
    public static boolean showHudFood = false;
    public static boolean hideVanillaFood = false;
    public static boolean showHudSkillBar = true;
    public static boolean showHudAppraisal = true;
    public static boolean showHudCombo = true;
    public static boolean showHudDokkaebiScenario = true;
    public static boolean showConstellationPopups = true;
    public static boolean showBossAlert = true;
    public static int bossAlertX = -1;
    public static int bossAlertY = -1;
    public static boolean showMinimap = false;
    public static int minimapX = -1;
    public static int minimapY = -1;
    public static float minimapScale = 1.0F;
    public static float minimapZoom = 1.0F;
    public static String minimapShape = "CIRCULAR"; // "CIRCULAR" or "SQUARE"
    public static String minimapCaveMode = "AUTO"; // "AUTO", "ALWAYS", "NEVER"
    public static boolean minimapShowEntities = true;
    public static boolean minimapShowWaypoints = false;
    public static boolean minimapRotateWithPlayer = false;
    public static String minimapLightingMode = "DAY"; // "DAY", "AUTO", "NIGHT"
    public static boolean minimapShowHostiles = true;
    public static boolean minimapShowPassives = true;
    public static boolean forceEnableMinimapWithExternalMods = false;
    public static boolean autoDeathWaypoints = false;
    public static boolean minimapPerceptionRadar = true;
    public static boolean minimapDangerAlert = true;
    public static boolean minimapShowItems = true;
    public static boolean minimapShowGates = true;

    public static float hudScale = 1.0F;
    public static int hudOffsetX = 0;
    public static int hudOffsetY = 0;

    public static HealthDisplayMode healthDisplayMode = HealthDisplayMode.CARD;
    public static FoodDisplayMode foodDisplayMode = FoodDisplayMode.VANILLA;

    public static float constellationNotifScale = 0.85F;
    public static boolean compactConstellationPopups = false;
    public static float systemNotificationScale = 1.0F;
    public static int constellationNotifX = -1;
    public static int constellationNotifY = -1;

    public static int statusCardX = -1;
    public static int statusCardY = -1;
    public static int skillBarX = -1;
    public static int skillBarY = -1;
    public static int appraisalX = -1;
    public static int appraisalY = -1;
    public static int comboX = -1;
    public static int comboY = -1;

    public static boolean isHudFoodBarVisible() {
        return foodDisplayMode == FoodDisplayMode.BAR;
    }

    public static boolean shouldHideVanillaFood() {
        return foodDisplayMode == FoodDisplayMode.BAR || foodDisplayMode == FoodDisplayMode.OFF;
    }

    public static boolean shouldHideVanillaHearts() {
        if (!showHudStatusCard) return false;
        return healthDisplayMode == HealthDisplayMode.CARD || healthDisplayMode == HealthDisplayMode.COMPACT_BAR;
    }

    public static void resetHudPositions() {
        statusCardX = -1;
        statusCardY = -1;
        skillBarX = -1;
        skillBarY = -1;
        appraisalX = -1;
        appraisalY = -1;
        comboX = -1;
        comboY = -1;
        constellationNotifX = -1;
        constellationNotifY = -1;
        minimapX = -1;
        minimapY = -1;
        bossAlertX = -1;
        bossAlertY = -1;
        save();
    }

    public static class ConfigData {
        public float systemGuiScale = 1.0F;
        public int systemGuiOffsetX = 0;
        public int systemGuiOffsetY = 0;
        public boolean showPlayer3DModel = true;
        public boolean showDimensionSideTabs = true;
        public boolean uiSoundsEnabled = true;
        public boolean freeMoveEnabled = true;

        public boolean showTabStatus = true;
        public boolean showTabSkills = true;
        public boolean showTabMissions = true;
        public boolean showTabTitles = true;
        public boolean showTabShop = true;
        public boolean showTabProgress = true;
        public boolean showTabRanking = true;
        public boolean showTabArtifacts = true;

        public boolean showHudStatusCard = true;
        public boolean showHudFood = false;
        public boolean hideVanillaFood = false;
        public boolean showHudSkillBar = true;
        public boolean showHudAppraisal = true;
        public boolean showHudCombo = true;
        public boolean showHudDokkaebiScenario = true;
        public boolean showConstellationPopups = true;
        public boolean showBossAlert = true;
        public int bossAlertX = -1;
        public int bossAlertY = -1;
        public Boolean showMinimap = false;
        public int minimapX = -1;
        public int minimapY = -1;
        public float minimapScale = 1.0F;
        public float minimapZoom = 1.0F;
        public String minimapShape = "CIRCULAR";
        public String minimapCaveMode = "AUTO";
        public boolean minimapShowEntities = true;
        public boolean minimapShowWaypoints = false;
        public boolean minimapRotateWithPlayer = false;
        public String minimapLightingMode = "DAY";
        public boolean minimapShowHostiles = true;
        public boolean minimapShowPassives = true;
        public boolean forceEnableMinimapWithExternalMods = false;
        public boolean autoDeathWaypoints = false;
        public boolean minimapPerceptionRadar = true;
        public boolean minimapDangerAlert = true;
        public boolean minimapShowItems = true;
        public boolean minimapShowGates = true;
        public float hudScale = 1.0F;
        public int hudOffsetX = 0;
        public int hudOffsetY = 0;

        public String healthDisplayMode = HealthDisplayMode.CARD.name();
        public String foodDisplayMode = FoodDisplayMode.VANILLA.name();

        public float constellationNotifScale = 0.85F;
        public boolean compactConstellationPopups = false;
        public float systemNotificationScale = 1.0F;
        public int constellationNotifX = -1;
        public int constellationNotifY = -1;

        public int statusCardX = -1;
        public int statusCardY = -1;
        public int skillBarX = -1;
        public int skillBarY = -1;
        public int appraisalX = -1;
        public int appraisalY = -1;
        public int comboX = -1;
        public int comboY = -1;
    }

    private GuiConfig() {}

    public static void load() {
        try {
            File dir = new File(Minecraft.getInstance().gameDirectory, "config");
            if (!dir.exists()) dir.mkdirs();
            configFile = new File(dir, "awakening_system_gui.json");
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    ConfigData data = GSON.fromJson(reader, ConfigData.class);
                    if (data != null) {
                        systemGuiScale = Math.max(0.5F, Math.min(1.8F, data.systemGuiScale));
                        systemGuiOffsetX = data.systemGuiOffsetX;
                        systemGuiOffsetY = data.systemGuiOffsetY;
                        showPlayer3DModel = data.showPlayer3DModel;
                        showDimensionSideTabs = data.showDimensionSideTabs;
                        uiSoundsEnabled = data.uiSoundsEnabled;
                        freeMoveEnabled = data.freeMoveEnabled;

                        showTabStatus = data.showTabStatus;
                        showTabSkills = data.showTabSkills;
                        showTabMissions = data.showTabMissions;
                        showTabTitles = data.showTabTitles;
                        showTabShop = data.showTabShop;
                        showTabProgress = data.showTabProgress;
                        showTabRanking = data.showTabRanking;
                        showTabArtifacts = data.showTabArtifacts;

                        showHudStatusCard = data.showHudStatusCard;
                        showHudFood = data.showHudFood;
                        hideVanillaFood = data.hideVanillaFood;
                        showHudSkillBar = data.showHudSkillBar;
                        showHudAppraisal = data.showHudAppraisal;
                        showHudCombo = data.showHudCombo;
                        showHudDokkaebiScenario = data.showHudDokkaebiScenario;
                        showConstellationPopups = data.showConstellationPopups;
                        showBossAlert = data.showBossAlert;
                        bossAlertX = data.bossAlertX;
                        bossAlertY = data.bossAlertY;
                        showMinimap = data.showMinimap != null ? data.showMinimap : false;
                        minimapX = data.minimapX;
                        minimapY = data.minimapY;
                        minimapScale = Math.max(0.5F, Math.min(2.0F, data.minimapScale <= 0 ? 1.0F : data.minimapScale));
                        minimapZoom = Math.max(0.5F, Math.min(3.0F, data.minimapZoom <= 0 ? 1.0F : data.minimapZoom));
                        minimapShape = data.minimapShape != null ? data.minimapShape : "CIRCULAR";
                        minimapCaveMode = data.minimapCaveMode != null ? data.minimapCaveMode : "AUTO";
                        minimapShowEntities = data.minimapShowEntities;
                        minimapShowWaypoints = data.minimapShowWaypoints;
                        minimapRotateWithPlayer = data.minimapRotateWithPlayer;
                        minimapLightingMode = data.minimapLightingMode != null ? data.minimapLightingMode : "DAY";
                        minimapShowHostiles = data.minimapShowHostiles;
                        minimapShowPassives = data.minimapShowPassives;
                        forceEnableMinimapWithExternalMods = data.forceEnableMinimapWithExternalMods;
                        autoDeathWaypoints = data.autoDeathWaypoints;
                        minimapPerceptionRadar = data.minimapPerceptionRadar;
                        minimapDangerAlert = data.minimapDangerAlert;
                        minimapShowItems = data.minimapShowItems;
                        minimapShowGates = data.minimapShowGates;

                        hudScale = Math.max(0.5F, Math.min(2.0F, data.hudScale));
                        hudOffsetX = data.hudOffsetX;
                        hudOffsetY = data.hudOffsetY;

                        constellationNotifScale = Math.max(0.4F, Math.min(1.5F, data.constellationNotifScale));
                        compactConstellationPopups = data.compactConstellationPopups;
                        systemNotificationScale = Math.max(0.4F, Math.min(1.5F, data.systemNotificationScale));
                        constellationNotifX = data.constellationNotifX;
                        constellationNotifY = data.constellationNotifY;

                        if (data.healthDisplayMode != null) {
                            try {
                                healthDisplayMode = HealthDisplayMode.valueOf(data.healthDisplayMode);
                            } catch (Exception ignored) {
                                healthDisplayMode = HealthDisplayMode.CARD;
                            }
                        }

                        if (data.foodDisplayMode != null) {
                            try {
                                if ("BOTH".equals(data.foodDisplayMode)) { foodDisplayMode = FoodDisplayMode.BAR; } else { foodDisplayMode = FoodDisplayMode.valueOf(data.foodDisplayMode); }
                            } catch (Exception ignored) {
                                foodDisplayMode = FoodDisplayMode.BAR;
                            }
                        } else {
                            if (!data.hideVanillaFood && !data.showHudFood) {
                                foodDisplayMode = FoodDisplayMode.VANILLA;
                            } else if (!data.hideVanillaFood && data.showHudFood) {
                                foodDisplayMode = FoodDisplayMode.BAR;
                            } else if (!data.showHudFood) {
                                foodDisplayMode = FoodDisplayMode.OFF;
                            } else {
                                foodDisplayMode = FoodDisplayMode.BAR;
                            }
                        }

                        statusCardX = data.statusCardX;
                        statusCardY = data.statusCardY;
                        skillBarX = data.skillBarX;
                        skillBarY = data.skillBarY;
                        appraisalX = data.appraisalX;
                        appraisalY = data.appraisalY;
                        comboX = data.comboX;
                        comboY = data.comboY;
                    }
                }
            } else {
                save();
            }
            com.soybe.awakeningsystem.SystemClientConfig.syncToGuiConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            if (configFile == null) {
                File dir = new File(Minecraft.getInstance().gameDirectory, "config");
                if (!dir.exists()) dir.mkdirs();
                configFile = new File(dir, "awakening_system_gui.json");
            }
            ConfigData data = new ConfigData();
            data.systemGuiScale = systemGuiScale;
            data.systemGuiOffsetX = systemGuiOffsetX;
            data.systemGuiOffsetY = systemGuiOffsetY;
            data.showPlayer3DModel = showPlayer3DModel;
            data.showDimensionSideTabs = showDimensionSideTabs;
            data.uiSoundsEnabled = uiSoundsEnabled;
            data.freeMoveEnabled = freeMoveEnabled;

            data.showTabStatus = showTabStatus;
            data.showTabSkills = showTabSkills;
            data.showTabMissions = showTabMissions;
            data.showTabTitles = showTabTitles;
            data.showTabShop = showTabShop;
            data.showTabProgress = showTabProgress;
            data.showTabRanking = showTabRanking;
            data.showTabArtifacts = showTabArtifacts;

            data.showHudStatusCard = showHudStatusCard;
            data.showHudFood = showHudFood;
            data.hideVanillaFood = hideVanillaFood;
            data.showHudSkillBar = showHudSkillBar;
            data.showHudAppraisal = showHudAppraisal;
            data.showHudCombo = showHudCombo;
            data.showHudDokkaebiScenario = showHudDokkaebiScenario;
            data.showConstellationPopups = showConstellationPopups;
            data.showBossAlert = showBossAlert;
            data.bossAlertX = bossAlertX;
            data.bossAlertY = bossAlertY;
            data.showMinimap = showMinimap;
            data.minimapX = minimapX;
            data.minimapY = minimapY;
            data.minimapScale = minimapScale;
            data.minimapZoom = minimapZoom;
            data.minimapShape = minimapShape;
            data.minimapCaveMode = minimapCaveMode;
            data.minimapShowEntities = minimapShowEntities;
            data.minimapShowWaypoints = minimapShowWaypoints;
            data.minimapRotateWithPlayer = minimapRotateWithPlayer;
            data.minimapLightingMode = minimapLightingMode;
            data.minimapShowHostiles = minimapShowHostiles;
            data.minimapShowPassives = minimapShowPassives;
            data.forceEnableMinimapWithExternalMods = forceEnableMinimapWithExternalMods;
            data.autoDeathWaypoints = autoDeathWaypoints;
            data.minimapPerceptionRadar = minimapPerceptionRadar;
            data.minimapDangerAlert = minimapDangerAlert;
            data.minimapShowItems = minimapShowItems;
            data.minimapShowGates = minimapShowGates;

            data.hudScale = hudScale;
            data.hudOffsetX = hudOffsetX;
            data.hudOffsetY = hudOffsetY;

            data.constellationNotifScale = constellationNotifScale;
            data.compactConstellationPopups = compactConstellationPopups;
            data.systemNotificationScale = systemNotificationScale;
            data.constellationNotifX = constellationNotifX;
            data.constellationNotifY = constellationNotifY;

            data.healthDisplayMode = healthDisplayMode != null ? healthDisplayMode.name() : HealthDisplayMode.CARD.name();
            data.foodDisplayMode = foodDisplayMode != null ? foodDisplayMode.name() : FoodDisplayMode.BAR.name();

            data.statusCardX = statusCardX;
            data.statusCardY = statusCardY;
            data.skillBarX = skillBarX;
            data.skillBarY = skillBarY;
            data.appraisalX = appraisalX;
            data.appraisalY = appraisalY;
            data.comboX = comboX;
            data.comboY = comboY;

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(data, writer);
            }
            com.soybe.awakeningsystem.SystemClientConfig.syncFromGuiConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetDefaults() {
        systemGuiScale = 1.0F;
        systemGuiOffsetX = 0;
        systemGuiOffsetY = 0;
        showPlayer3DModel = true;
        showDimensionSideTabs = true;
        uiSoundsEnabled = true;
        freeMoveEnabled = true;

        showTabStatus = true;
        showTabSkills = true;
        showTabMissions = true;
        showTabTitles = true;
        showTabShop = true;
        showTabProgress = true;
        showTabRanking = true;
        showTabArtifacts = true;

        showHudStatusCard = true;
        showHudSkillBar = true;
        showHudAppraisal = true;
        showHudCombo = true;
        showHudDokkaebiScenario = true;
        showConstellationPopups = true;
        showMinimap = true;
        minimapX = -1;
        minimapY = -1;
        minimapScale = 1.0F;
        minimapZoom = 1.0F;
        minimapShape = "CIRCULAR";
        minimapCaveMode = "AUTO";
        minimapShowEntities = true;
        minimapShowWaypoints = false;
        minimapRotateWithPlayer = false;
        minimapLightingMode = "DAY";
        minimapShowHostiles = true;
        minimapShowPassives = true;
        minimapShowItems = true;
        minimapShowGates = true;

        hudScale = 1.0F;
        hudOffsetX = 0;
        hudOffsetY = 0;

        constellationNotifScale = 0.85F;
        compactConstellationPopups = false;
        systemNotificationScale = 1.0F;
        constellationNotifX = -1;
        constellationNotifY = -1;

        healthDisplayMode = HealthDisplayMode.CARD;
        foodDisplayMode = FoodDisplayMode.VANILLA;
        showHudFood = false;
        hideVanillaFood = false;

        statusCardX = -1;
        statusCardY = -1;
        skillBarX = -1;
        skillBarY = -1;
        appraisalX = -1;
        appraisalY = -1;
        comboX = -1;
        comboY = -1;

        save();
    }
}
