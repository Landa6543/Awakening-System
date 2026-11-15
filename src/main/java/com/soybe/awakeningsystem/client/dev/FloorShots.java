package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.tower.GreatTowerWorldManager;
import com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A development photographer, only active with -Dawakening.floorShots=16,17,...: visits each Great Tower floor in
 * spectator mode and takes a few pictures of it (screenshots/fs_FLOOR_N.png), from its entry portal, from above and
 * from its centre. Lets the land of a floor be checked without walking it.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class FloorShots {
    private FloorShots() {}

    private static final String FLOORS = System.getProperty("awakening.floorShots");
    private static final int WAIT = 170;
    private static int t = -1;
    private static List<double[]> views;     // floor, x, y, z, yaw, pitch

    private static void plan() {
        views = new ArrayList<>();
        for (String s : FLOORS.split(",")) {
            int f = Integer.parseInt(s.trim());
            int[][] spots = switch (f) {
                case 16 -> new int[][]{{-230, -50, -90}, {0, 0, -60}};
                case 17 -> new int[][]{{50, -240, 0}, {0, 0, 150}};
                case 18 -> new int[][]{{-210, -210, -45}, {0, 0, 120}};
                case 19 -> new int[][]{{-190, 190, -135}, {0, 0, 45}};
                default -> new int[][]{{-140, 0, -90}, {-50, 0, -90}};
            };
            boolean cave = TowerFloorTerrain.isCave(f);
            for (int[] sp : spots) {
                int g = TowerFloorTerrain.height(f, sp[0], sp[1]);
                views.add(new double[]{f, sp[0] + 0.5, g + 2.6, sp[1] + 0.5, sp[2], 8});
                if (!cave) views.add(new double[]{f, sp[0] + 0.5, g + 45, sp[1] + 0.5, sp[2], 28});
            }
            if (cave) views.add(new double[]{f, -30.5, 60, -30.5, -45, 35});
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (FLOORS == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        if (views == null) plan();
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) {
            mc.options.hideGui = true;
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp == null) return;
                for (String c : new String[]{"gamemode spectator " + sp.getGameProfile().getName(), "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c);
            });
        }
        int k = (t - 60) / WAIT, phase = (t - 60) % WAIT;
        if (t < 60) return;
        if (k >= views.size()) {
            if (phase == 20) mc.stop();
            return;
        }
        double[] v = views.get(k);
        if (phase == 0 || phase == 60) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            ServerLevel lvl = server.getLevel(GreatTowerWorldManager.getDimensionForFloor((int) v[0]));
            if (sp != null && lvl != null) sp.teleportTo(lvl, v[1], v[2], v[3], (float) v[4], (float) v[5]);
        });
        if (phase == WAIT - 10) {
            int n = 0;
            for (int i = 0; i < k; i++) if (views.get(i)[0] == v[0]) n++;
            Screenshot.grab(mc.gameDirectory, "fs_" + (int) v[0] + "_" + n + ".png", mc.getMainRenderTarget(), msg -> {});
        }
    }
}
