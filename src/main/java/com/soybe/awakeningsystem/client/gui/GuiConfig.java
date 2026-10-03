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
        CARD("Tarjeta Holográfica"),
        VANILLA("Corazones Vanilla (+MP)"),
        COMPACT_BAR("Barras Compactas Hotbar");

        private final String label;

        HealthDisplayMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // --- Sistema Principal (SystemScreen) ---
    public static float systemGuiScale = 1.0F; // 0.5x - 1.8x
    public static int systemGuiOffsetX = 0;
    public static int systemGuiOffsetY = 0;
    public static boolean showPlayer3DModel = true;
    public static boolean showDimensionSideTabs = true;
    public static boolean uiSoundsEnabled = true;
    public static boolean freeMoveEnabled = true;
    public static boolean enableBackgroundBlur = false; // Sin desenfoque por defecto (estilo AR nítido)

    // --- HUD en Juego (SystemHudOverlay) ---
    public static boolean showHudStatusCard = true;
    public static boolean showHudSkillBar = true;
    public static boolean showHudAppraisal = true;
    public static boolean showHudCombo = true;
    public static boolean showMinimap = false;
    public static float hudScale = 1.0F; // 0.5x - 2.0x
    public static int hudOffsetX = 0;
    public static int hudOffsetY = 0;

    // Modo de visualización de salud y recursos
    public static HealthDisplayMode healthDisplayMode = HealthDisplayMode.CARD;

    // --- Notificaciones de Constelaciones y Sistema ---
    public static float constellationNotifScale = 0.85F; // 0.4x - 1.5x (por defecto más compacta)
    public static boolean compactConstellationPopups = false;
    public static float systemNotificationScale = 1.0F; // 0.5x - 1.5x
    public static int constellationNotifX = -1;
    public static int constellationNotifY = -1;

    // Posiciones libres personalizadas arrastrables con el ratón (-1 = automático por defecto)
    public static int statusCardX = -1;
    public static int statusCardY = -1;
    public static int skillBarX = -1;
    public static int skillBarY = -1;
    public static int appraisalX = -1;
    public static int appraisalY = -1;
    public static int comboX = -1;
    public static int comboY = -1;

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
        public boolean enableBackgroundBlur = false;

        public boolean showHudStatusCard = true;
        public boolean showHudSkillBar = true;
        public boolean showHudAppraisal = true;
        public boolean showHudCombo = true;
        public boolean showMinimap = false;
        public float hudScale = 1.0F;
        public int hudOffsetX = 0;
        public int hudOffsetY = 0;

        public String healthDisplayMode = HealthDisplayMode.CARD.name();

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
                        enableBackgroundBlur = data.enableBackgroundBlur;

                        showHudStatusCard = data.showHudStatusCard;
                        showHudSkillBar = data.showHudSkillBar;
                        showHudAppraisal = data.showHudAppraisal;
                        showHudCombo = data.showHudCombo;
                        showMinimap = data.showMinimap;
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
            data.enableBackgroundBlur = enableBackgroundBlur;

            data.showHudStatusCard = showHudStatusCard;
            data.showHudSkillBar = showHudSkillBar;
            data.showHudAppraisal = showHudAppraisal;
            data.showHudCombo = showHudCombo;
            data.showMinimap = showMinimap;
            data.hudScale = hudScale;
            data.hudOffsetX = hudOffsetX;
            data.hudOffsetY = hudOffsetY;

            data.constellationNotifScale = constellationNotifScale;
            data.compactConstellationPopups = compactConstellationPopups;
            data.systemNotificationScale = systemNotificationScale;
            data.constellationNotifX = constellationNotifX;
            data.constellationNotifY = constellationNotifY;

            data.healthDisplayMode = healthDisplayMode != null ? healthDisplayMode.name() : HealthDisplayMode.CARD.name();

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
        enableBackgroundBlur = false;

        showHudStatusCard = true;
        showHudSkillBar = true;
        showHudAppraisal = true;
        showHudCombo = true;
        hudScale = 1.0F;
        hudOffsetX = 0;
        hudOffsetY = 0;

        constellationNotifScale = 0.85F;
        compactConstellationPopups = false;
        systemNotificationScale = 1.0F;
        constellationNotifX = -1;
        constellationNotifY = -1;

        healthDisplayMode = HealthDisplayMode.CARD;

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
