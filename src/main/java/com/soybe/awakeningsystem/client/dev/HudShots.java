package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.hudShots: photographs the HUD (minimap included) at a few
 * window sizes, then the HUD tab of the interface settings (screenshots/hs_*.png).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class HudShots {
    private HudShots() {}

    private static final boolean ON = System.getProperty("awakening.hudShots") != null;
    private static final int[][] SIZES = {{854, 480}, {1280, 720}, {1920, 1080}};
    private static int t = -1;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        mc.options.pauseOnLostFocus = false;
        t++;
        if (t < 120 && mc.screen != null) mc.setScreen(null);
        if (System.getProperty("awakening.hudShots").equals("click") && t < 320 && t > 5) t = 319;   // straight to the settings
        for (int i = 0; i < SIZES.length; i++) {
            int at = 120 + i * 80;
            if (t == at) mc.getWindow().setWindowed(SIZES[i][0], SIZES[i][1]);
            if (t == at + 60) Screenshot.grab(mc.gameDirectory, "hs_" + SIZES[i][0] + ".png", mc.getMainRenderTarget(), msg -> {});
        }
        int cfg = 120 + SIZES.length * 80;
        if (t == cfg) {
            mc.getWindow().setWindowed(1280, 720);
            var screen = new com.soybe.awakeningsystem.client.gui.GuiConfigScreen(null);
            mc.setScreen(screen);
            try {   // straight to the HUD tab
                var f = screen.getClass().getDeclaredField("currentTab");
                f.setAccessible(true);
                f.setInt(screen, 2);
            } catch (ReflectiveOperationException ignored) {}
        }
        if (t == cfg + 40) Screenshot.grab(mc.gameDirectory, "hs_config.png", mc.getMainRenderTarget(), msg -> {});
        if (t == cfg + 50 && mc.screen != null) {
            // press the Minimap switch like a player would: down its column until the minimap turns off
            var s = mc.screen;
            boolean before = dev.diorama.Settings.get().minimap;
            int x = s.width / 2 + 60;
            for (int y = s.height / 2; y < s.height; y++) {
                s.mouseClicked(x, y, 0);
                if (dev.diorama.Settings.get().minimap != before) {
                    AwakeningSystemMod.LOGGER.info("[HudShots] minimap switch at y {} turned it {}", y, dev.diorama.Settings.get().minimap);
                    break;
                }
                if (mc.screen != s) { mc.setScreen(s); }
                s.mouseClicked(x, y, 0);   // undo whatever else that row was
            }
            mc.setScreen(null);
        }
        if (t == cfg + 90) Screenshot.grab(mc.gameDirectory, "hs_off.png", mc.getMainRenderTarget(), msg -> {});
        if (t == cfg + 95) { var ds = dev.diorama.Settings.get(); ds.minimap = true; ds.save(); }
        if (t == cfg + 100) mc.stop();
    }
}
