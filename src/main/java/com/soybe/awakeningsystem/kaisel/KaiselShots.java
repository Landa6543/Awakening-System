package com.soybe.awakeningsystem.kaisel;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.kaiselShots: puts a living Kaisel and a shadow Kaisel
 * (made the way "Arise" makes shadows) in front of the player, photographs them, then rides the shadow up into
 * the air and photographs it flying, then closes the game. Pictures go to screenshots/ks_*.png.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class KaiselShots {
    private KaiselShots() {}

    private static final boolean ON = System.getProperty("awakening.kaiselShots") != null;
    private static int t = -1;
    private static KaiselEntity living, shadow;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            ServerLevel sl = sp.serverLevel();
            for (String c : new String[]{"time set noon", "weather clear", "gamerule doDaylightCycle false", "gamerule doMobSpawning false",
                    "kill @e[type=awakening_system:kaisel]", "fill -20 199 -20 20 199 20 minecraft:smooth_stone", "fill -20 200 -20 20 230 20 minecraft:air",
                    "gamemode creative " + sp.getGameProfile().getName(), "tp " + sp.getGameProfile().getName() + " 3.5 201 13.5 160 8"})
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c);
            living = KaiselContent.KAISEL.create(sl);
            living.moveTo(0.5, 200, 0, 20, 0);
            living.setNoAi(true);
            sl.addFreshEntity(living);
        });
        if (t == 40) mc.options.hideGui = true;
        if (t == 110) shot(mc, "ks_0_living");
        // the shadow appears once the living one is gone (shadows attack anything hostile)
        if (t == 115) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            ServerLevel sl = sp.serverLevel();
            living.discard();
            // exactly what a player types: /kaisel sombra
            server.getCommands().performPrefixedCommand(sp.createCommandSourceStack(), "kaisel sombra");
            shadow = sl.getEntities(KaiselContent.KAISEL, KaiselEntity::isShadow).stream().findFirst().orElse(null);
            System.out.println("[KaiselShots] /kaisel sombra -> " + (shadow == null ? "nothing" : shadow.getName().getString() + " master=" + shadow.masterId()));
        });
        if (t == 145) shot(mc, "ks_1_shadow");
        // ride the shadow and lift it into the air, flying
        if (t == 150) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null || shadow == null) return;
            shadow.setNoAi(true);
            shadow.teleportTo(0.5, 212, 0.5);
            shadow.setYRot(180);
            shadow.setFlying(true);
            sp.startRiding(shadow, true);
        });
        if (t == 175) mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (t == 200) shot(mc, "ks_2_ride_back");
        if (t == 205) mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        if (t == 230) shot(mc, "ks_3_ride_front");
        if (t == 235) mc.options.setCameraType(CameraType.FIRST_PERSON);
        if (t == 255) shot(mc, "ks_4_ride_first");
        if (t == 270) mc.stop();
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), msg -> {});
    }
}
