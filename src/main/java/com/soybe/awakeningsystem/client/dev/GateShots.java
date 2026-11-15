package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.world.RuinGateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.gateShots: flies to the fixed Ruin Gate nearest to the
 * player, waits for its ground to be generated and the gate raised, and photographs it from close by and from afar
 * (screenshots/gs_*.png), so the gate's ruin and its floating name and rank can be checked.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class GateShots {
    private GateShots() {}

    private static final boolean ON = System.getProperty("awakening.gateShots") != null;
    private static int t = -1;
    private static int[] spot;
    private static int ground;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) {
            mc.options.hideGui = true;
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp == null) return;
                for (String c : new String[]{"gamemode spectator " + sp.getGameProfile().getName(), "time set noon", "gamerule doDaylightCycle false", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c);
                ServerLevel world = server.overworld();
                spot = RuinGateManager.nearestFixedGate(world, sp.blockPosition().offset(Integer.getInteger("gateOff", 900), 0, Integer.getInteger("gateOffZ", 900)));
                AwakeningSystemMod.LOGGER.info("[GateShots] gate spot {}", spot == null ? "none" : spot[0] + " " + spot[1]);
                if (spot != null) sp.teleportTo(world, spot[0] + 0.5, 160, spot[1] + 0.5, -45, 60);
            });
        }
        if (spot == null) {
            if (t > 60) mc.stop();
            return;
        }
        if (t == 260) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            ServerLevel world = server.overworld();
            ground = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spot[0], spot[1]);
            AwakeningSystemMod.LOGGER.info("[GateShots] ground {} block {}", ground, world.getBlockState(new BlockPos(spot[0], ground - 1, spot[1])));
            if (sp != null) sp.teleportTo(world, spot[0] - 13.5, ground + 5, spot[1] - 13.5, -45, 12);
        });
        if (t == 380) Screenshot.grab(mc.gameDirectory, "gs_0.png", mc.getMainRenderTarget(), msg -> {});
        if (t == 390) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) sp.teleportTo(server.overworld(), spot[0] - 45.5, ground + 16, spot[1] - 45.5, -45, 12);
        });
        if (t == 480) Screenshot.grab(mc.gameDirectory, "gs_1.png", mc.getMainRenderTarget(), msg -> {});
        if (t == 490) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) sp.teleportTo(server.overworld(), spot[0] + 8.5, ground + 4, spot[1] + 0.5, 90, 15);
        });
        if (t == 570) Screenshot.grab(mc.gameDirectory, "gs_2.png", mc.getMainRenderTarget(), msg -> {});
        if (t == 590) mc.stop();
    }
}
