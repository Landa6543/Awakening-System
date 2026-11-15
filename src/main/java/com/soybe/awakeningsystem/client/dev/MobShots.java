package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.mobShots=<entity id>: builds a flat stone floor with a
 * low step, puts the creature on it and lets it walk toward the step while the camera, at ground level beside it,
 * takes a picture every second (screenshots/ms_*.png). Shows feet sinking into the floor or floating above it.
 * With -Dawakening.mobClose as well, the creature stands still instead and the camera circles it close up
 * (front, side, back, three-quarter and from above), to look at its model and texture.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class MobShots {
    private MobShots() {}

    private static final String ID = System.getProperty("awakening.mobShots");
    private static final boolean CLOSE = System.getProperty("awakening.mobClose") != null;
    private static int t = -1;
    private static Entity mob;
    /** Close-up views: yaw of the camera round the creature, height, distance, pitch. */
    private static final float[][] VIEWS = {{180, 1.4F, 3.6F, 8}, {90, 1.4F, 3.6F, 8}, {0, 1.4F, 3.6F, 8}, {135, 2.2F, 3.4F, 22}, {200, 4.2F, 2.4F, 60}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (ID == null) return;
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
            String n = sp.getGameProfile().getName();
            for (String c : new String[]{"time set noon", "weather clear", "gamerule doDaylightCycle false", "gamerule doMobSpawning false",
                    "fill -30 199 -30 30 199 30 minecraft:smooth_stone", "fill -30 200 -30 30 215 30 minecraft:air",
                    CLOSE ? "say close" : "fill 6 200 -30 30 200 30 minecraft:stone_bricks", "gamemode spectator " + n,
                    "tp " + n + " -2 200.6 -14 0 0"})
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c);
            var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(ID));
            mob = type.create(sl);
            if (mob == null) return;
            mob.moveTo(CLOSE ? 0.5 : -8, 200, CLOSE ? 0.5 : 0, CLOSE ? 0 : -90, 0);
            if (mob instanceof Mob m) {
                m.setPersistenceRequired();
                if (CLOSE) m.setNoAi(true);
                else m.getNavigation().moveTo(20, 201, 0, 0.8);
            }
            sl.addFreshEntity(mob);
        });
        if (t == 40) mc.options.hideGui = true;
        if (CLOSE) {
            int k = (t - 80) / 40, phase = (t - 80) % 40;
            if (t < 80) return;
            if (k >= VIEWS.length) {
                if (phase == 5) mc.stop();
                return;
            }
            float[] v = VIEWS[k];
            if (phase == 0) server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp == null) return;
                double a = Math.toRadians(v[0]);
                // the creature faces south (+z); a view's yaw is where the camera stands round it
                double x = 0.5 - Math.sin(a) * v[2], z = 0.5 + Math.cos(a) * v[2];
                float look = (float) Math.toDegrees(Math.atan2(-(0.5 - x), 0.5 - z));
                sp.teleportTo(sp.serverLevel(), x, 200 + v[1], z, look, v[3]);
            });
            if (phase == 30) Screenshot.grab(mc.gameDirectory, "ms_" + k + ".png", mc.getMainRenderTarget(), msg -> {});
            return;
        }
        if (t > 60 && mob instanceof Mob m && !m.level().isClientSide && t % 20 == 0) server.execute(() -> m.getNavigation().moveTo(20, 201, 0, 0.8));
        if (t > 60 && mob != null && t % 5 == 0) AwakeningSystemMod.LOGGER.info("[MobShots] t={} x={} y={}", t, String.format("%.2f", mob.getX()), String.format("%.2f", mob.getY()));
        if (t >= 100 && t <= 280 && t % 30 == 10)
            Screenshot.grab(mc.gameDirectory, "ms_" + ((t - 100) / 30) + ".png", mc.getMainRenderTarget(), msg -> {});
        if (t == 300) mc.stop();
    }
}
