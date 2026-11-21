package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Which look the System's menus use: the mod's normal windows (default) or the floating "full dive" menu
 * (round buttons, white and orange panels). The choice is the player's own and is kept in a small file in
 * the config folder. With the full-dive look on, the System key opens {@link SaoMenuScreen} and Escape
 * opens {@link SaoPauseScreen}; the normal windows are still what every entry leads to.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class UiStyle {
    private UiStyle() {}

    private static Boolean fullDive;
    /** Set just before one of these menus opens a normal window on purpose, so it is not swapped back. */
    static boolean passThrough;

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("awakening_system_ui_style.txt");
    }

    public static boolean fullDive() {
        if (fullDive == null) {
            try {
                fullDive = Files.exists(file()) && new String(Files.readAllBytes(file()), StandardCharsets.UTF_8).trim().equalsIgnoreCase("full_dive");
            } catch (Exception e) {
                fullDive = false;
            }
        }
        return fullDive;
    }

    public static void setFullDive(boolean on) {
        fullDive = on;
        try {
            Files.write(file(), (on ? "full_dive" : "normal").getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (passThrough) {
            passThrough = false;
            return;
        }
        if (!fullDive() || Minecraft.getInstance().level == null) return;
        if (event.getNewScreen() instanceof SystemScreen && !(event.getCurrentScreen() instanceof SystemScreen)) {
            event.setNewScreen(new SaoMenuScreen());
        } else if (event.getNewScreen() instanceof PauseScreen) {
            event.setNewScreen(new SaoPauseScreen());
        } else if (event.getNewScreen() instanceof com.soybe.awakeningsystem.client.gui.ShadowCommandScreen) {
            event.setNewScreen(new SaoShadowScreen(event.getCurrentScreen()));
        } else if (event.getNewScreen() instanceof com.soybe.awakeningsystem.fable.FablesScreen) {
            event.setNewScreen(new SaoFablesScreen(event.getCurrentScreen()));
        } else if (event.getNewScreen() instanceof com.soybe.awakeningsystem.client.gui.GuiConfigScreen) {
            event.setNewScreen(new SaoSettingsScreen(event.getCurrentScreen()));
        } else if (event.getNewScreen() instanceof com.soybe.awakeningsystem.ClassSkillTreeScreen) {
            event.setNewScreen(new SaoClassTreeScreen(event.getCurrentScreen()));
        } else if (event.getNewScreen() instanceof com.soybe.awakeningsystem.ClassSelectionScreen) {
            event.setNewScreen(new SaoClassChoiceScreen(event.getCurrentScreen()));
        }
    }
}
