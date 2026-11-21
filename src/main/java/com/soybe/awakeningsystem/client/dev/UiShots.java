package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.uiShots: the interface settings, a death with ghosts
 * turned off, the classic status and shop screens and the full-dive card (screenshots/ui_*.png).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class UiShots {
    private UiShots() {}

    private static final boolean ON = System.getProperty("awakening.uiShots") != null;
    private static int t = -1;
    private static boolean wasGhost = true;

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "ui_" + name + ".png", mc.getMainRenderTarget(), msg -> {});
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) { mc.getWindow().setWindowed(1280, 720); com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false); }
        if (t == 60) mc.setScreen(new com.soybe.awakeningsystem.client.gui.GuiConfigScreen(null));
        if (t == 80) shot(mc, "config");
        if (t == 85) {
            mc.setScreen(null);
            wasGhost = com.soybe.awakeningsystem.client.GhostPreferenceClient.ghostOnDeath();
            if (wasGhost) com.soybe.awakeningsystem.client.GhostPreferenceClient.toggle();   // ghosts off
        }
        if (t == 100) server.execute(() -> {
            var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode survival " + sp.getGameProfile().getName());
            sp.hurt(sp.damageSources().genericKill(), Float.MAX_VALUE);
        });
        if (t == 140 && mc.player.isDeadOrDying()) mc.player.respawn();
        if (t == 180) server.execute(() -> {
            var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            AwakeningSystemMod.LOGGER.info("[UiShots] after death with ghosts off: ghost {}", com.soybe.awakeningsystem.tombstone.TombstoneManager.isGhostMode(sp));
        });
        if (t == 185 && wasGhost) com.soybe.awakeningsystem.client.GhostPreferenceClient.toggle();   // back as it was
        if (t == 200) mc.setScreen(new com.soybe.awakeningsystem.SystemScreen(0));
        if (t == 225) shot(mc, "status");
        if (t == 226) server.execute(() -> {
            var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            var d = com.soybe.awakeningsystem.SystemEvents.data(sp);
            if (!d.isAwakened()) d.awakenWithPotential("C");   // a dormant System only shows status and missions
            AwakeningSystemMod.sync(sp);
        });
        if (t == 230) mc.setScreen(new com.soybe.awakeningsystem.SystemScreen(4));
        if (t == 245 && mc.screen != null) {
            for (var c : mc.screen.children())
                if (c instanceof net.minecraft.client.gui.components.EditBox box && box.isVisible()) { box.setFocused(true); box.setValue("cris"); break; }
        }
        if (t == 260) shot(mc, "shop");
        if (t == 265) { mc.setScreen(null); com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true); }
        if (t == 270) mc.setScreen(new com.soybe.awakeningsystem.client.sao.SaoMenuScreen());
        if (t == 300) shot(mc, "sao_card");
        if (t == 305) { mc.setScreen(null); com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false); }
        if (t == 320) mc.stop();
    }
}
