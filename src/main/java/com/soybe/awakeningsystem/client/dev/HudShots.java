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
        if (t < 120 && t < 60 && mc.screen != null) mc.setScreen(null);
        if (System.getProperty("awakening.hudShots").equals("click") && t < 320 && t > 5) t = 319;   // straight to the settings
        if (System.getProperty("awakening.hudShots").startsWith("sao_")) {
            // a full-dive window, with a few test shadows in the army for the shadow command
            String which = System.getProperty("awakening.hudShots").substring(4);
            if (t == 60) {
                com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true);
                var d = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                try {
                    var army = d.getStoredShadowArmy();
                    String[] names = {"Igris", "Iron", "Tank", "Beru", "Kaisel", "Greed"};
                    for (int i = 0; i < names.length; i++) {
                        var tag = new net.minecraft.nbt.CompoundTag();
                        tag.putString("ShadowBaseName", names[i]);
                        tag.putInt("RpgMobLevel", 20 + i * 15);
                        tag.putInt("ShadowRank", i % 4);
                        tag.putInt("SquadId", i % 3);
                        tag.putInt("ShadowKills", i * 7);
                        tag.putFloat("Health", 30 + i * 10);
                        tag.putFloat("MaxHealth", 90);
                        army.add(tag);
                    }
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.info("[HudShots] army is read-only: {}", e.toString());
                }
                mc.setScreen(switch (which) {
                    case "shadow" -> new com.soybe.awakeningsystem.client.gui.ShadowCommandScreen();
                    case "fables" -> new com.soybe.awakeningsystem.fable.FablesScreen(null);
                    case "config" -> new com.soybe.awakeningsystem.client.gui.GuiConfigScreen(null);
                    case "classpick" -> new com.soybe.awakeningsystem.ClassSelectionScreen(null);
                    case "classtree" -> {
                        // the class set on the server and synced, as the tree asks the server for its data on opening
                        var server = mc.getSingleplayerServer();
                        server.execute(() -> {
                            var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                            com.soybe.awakeningsystem.SystemEvents.data(sp).setHunterClass("sword_saint");
                            AwakeningSystemMod.sync(sp);
                        });
                        yield null;
                    }
                    default -> null;
                });
            }
            if (t == 70 && which.equals("classtree")) mc.setScreen(new com.soybe.awakeningsystem.ClassSkillTreeScreen(null));
            if (t == 78 && which.equals("classtree")) mc.getWindow().setWindowed(1280, 720);
            if (t == 90) Screenshot.grab(mc.gameDirectory, "hs_sao_" + which + ".png", mc.getMainRenderTarget(), msg -> {});
            if (t == 95) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            if (t == 100) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("ring")) {
            // the Link Ring in hand and in the hotbar
            var server = mc.getSingleplayerServer();
            if (t == 60 && server != null) server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                sp.getInventory().selected = 0;
                sp.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "link_ring"))));
                sp.setXRot(10);
            });
            if (t == 90) mc.player.getInventory().selected = 0;
            if (t == 110) Screenshot.grab(mc.gameDirectory, "hs_ring.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 115) mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
            if (t == 150) Screenshot.grab(mc.gameDirectory, "hs_ring3.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 155) mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            if (t == 170) mc.stop();
            return;
        }
        if (System.getProperty("awakening.hudShots").equals("notif")) {
            // a boss bar and the habit notice at once: the notice must sit under the bar
            var server = mc.getSingleplayerServer();
            if (t == 60 && server != null) server.execute(() -> {
                var src = server.createCommandSourceStack();
                server.getCommands().performPrefixedCommand(src, "bossbar add awakening_system:test \"Igris, Blood-Red Commander\"");
                server.getCommands().performPrefixedCommand(src, "bossbar set awakening_system:test players @a");
            });
            if (t == 90) com.soybe.awakeningsystem.AwakeningSystemClient.showNotification("awakening_system.notification.habit_upgrade.title",
                    "awakening_system.notification.habit_upgrade.message", "Ojo de Halcón:::3", 0xFF50C878);
            if (t == 110) Screenshot.grab(mc.gameDirectory, "hs_notif.png", mc.getMainRenderTarget(), msg -> {});
            if (t == 130) mc.stop();
            return;
        }
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
