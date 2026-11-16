package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.blockShots=id1,id2,...: sets the given blocks in a row
 * on a stone wall (two rows: the first half of the list, then the rest) and photographs them from the front in
 * daylight (screenshots/bs.png).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class BlockShots {
    private BlockShots() {}

    private static final String IDS = System.getProperty("awakening.blockShots");
    private static int t = -1;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (IDS == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            String n = sp.getGameProfile().getName();
            String[] ids = IDS.split(",");
            int half = (ids.length + 1) / 2;
            java.util.List<String> cmds = new java.util.ArrayList<>(java.util.List.of("time set noon", "weather clear", "gamerule doDaylightCycle false",
                    "fill -2 199 -2 " + (half * 2 + 2) + " 206 -2 minecraft:stone", "fill -2 199 -1 " + (half * 2 + 2) + " 206 6 minecraft:air",
                    "fill -2 198 -1 " + (half * 2 + 2) + " 198 6 minecraft:grass_block", "gamemode spectator " + n));
            for (int i = 0; i < ids.length; i++) {
                int x = (i % half) * 2, y = i < half ? 203 : 200;
                cmds.add("setblock " + x + " " + y + " -2 " + ids[i].trim());
            }
            cmds.add("tp " + n + " " + (half - 0.5) + " 202 " + (half * 1.15 + 1.5) + " 180 0");
            for (String c : cmds) server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c);
        });
        if (t == 40) mc.options.hideGui = true;
        if (t == 140) Screenshot.grab(mc.gameDirectory, "bs.png", mc.getMainRenderTarget(), msg -> {});
        if (t == 160) mc.stop();
    }
}
