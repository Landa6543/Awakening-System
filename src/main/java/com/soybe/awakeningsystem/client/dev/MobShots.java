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
    private static final float[][] VIEWS = {{180, 1.4F, 3.6F, 8}, {90, 1.4F, 3.6F, 8}, {0, 1.4F, 3.6F, 8}, {135, 2.2F, 3.4F, 22}, {200, 4.2F, 2.4F, 60},
            {0, 1.4F, 3.6F, 8}, {40, 2.2F, 3.4F, 18}, {60, 1.6F, 4.2F, 8}};   // the last one mid-swing   // the last two, from the front, after the creature is roused

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
                else if (System.getProperty("awakening.mobFight") == null) m.getNavigation().moveTo(20, 201, 0, 0.8);
            }
            sl.addFreshEntity(mob);
        });
        if (t == 40) {
            mc.options.hideGui = true;
            // the SAO target marker outlines whatever the camera points at in red: off for clean pictures
            com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
        }
        if (t == 70 && mob != null) AwakeningSystemMod.LOGGER.info("[MobShots] {} removed={} reason={} at {}", mob.getType(), mob.isRemoved(), mob.getRemovalReason(), mob.position());
        if (System.getProperty("awakening.mobFight") != null) {
            // a real fight: the player in survival (unhurtable) a few blocks away, the creature with its AI; every
            // second the server's and the client's view of it are logged and a picture taken
            if (t == 60) server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp == null) return;
                sp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 2000, 9, false, false));
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 2000, 9, false, false));
                sp.teleportTo(sp.serverLevel(), -3.5, 200, -12.5, -20, 10);
            });
            if (t > 60 && t % 20 == 0 && mob != null) {
                var cm = mc.level.getEntity(mob.getId());
                String sh = mob instanceof com.soybe.awakeningsystem.entity.bestiary.PatchworkBearEntity b ? "server hostile=" + b.isHostile() : "";
                String ch = cm instanceof com.soybe.awakeningsystem.entity.bestiary.PatchworkBearEntity b ? " client hostile=" + b.isHostile() : "";
                AwakeningSystemMod.LOGGER.info("[MobFight] t={} {}{} dist={} target={} player hp={} goals={}", t, sh, ch, String.format("%.1f", mob.distanceTo(mc.player)),
                        mob instanceof Mob m && m.getTarget() != null ? m.getTarget().getName().getString() : "none", mc.player.getHealth(),
                        mob instanceof Mob m2 ? m2.goalSelector.getAvailableGoals().stream().filter(g -> g.isRunning()).map(g -> g.getGoal().getClass().getSimpleName()).toList() : "");
            }
            if (t == 200) server.execute(() -> { ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID()); if (sp != null) sp.teleportTo(sp.serverLevel(), -4.5, 200, -5.0, -30, 15); });
            if (t >= 100 && t % 40 == 0 && t <= 420)
                Screenshot.grab(mc.gameDirectory, "mf_" + ((t - 100) / 40) + ".png", mc.getMainRenderTarget(), msg -> {});
            if (t == 440) mc.stop();
            return;
        }
        if (CLOSE) {
            int k = (t - 80) / 40, phase = (t - 80) % 40;
            if (t < 80) return;
            if (k >= VIEWS.length) {
                if (phase == 5) mc.stop();
                return;
            }
            float[] v = VIEWS[k];
            if (k == 5 && phase == 0) server.execute(() -> {
                if (mob instanceof com.soybe.awakeningsystem.entity.bestiary.PatchworkBearEntity b) b.burst();
                if (mob instanceof com.soybe.awakeningsystem.entity.bestiary.SonrisaEntity s) s.wake();
            });
            if (phase == 0) server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp == null) return;
                double a = Math.toRadians(v[0]);
                // the creature faces south (+z); a view's yaw is where the camera stands round it
                double x = 0.5 - Math.sin(a) * v[2], z = 0.5 + Math.cos(a) * v[2];
                float look = (float) Math.toDegrees(Math.atan2(-(0.5 - x), 0.5 - z)) + 16.0F;   // the crosshair just off the creature: a targeted creature is outlined in red
                sp.teleportTo(sp.serverLevel(), x, 200 + v[1], z, look, v[3]);
            });
            if (k == 6 && phase == 34 && mob != null) {   // after the pictures of the roused creature
                // can a player hit it now? the client's box, the server's box, and a ray from the eye to its middle
                var cm = mc.level.getEntity(mob.getId());
                var eye = mc.player.getEyePosition();
                boolean hit = cm != null && cm.getBoundingBox().clip(eye, cm.getBoundingBox().getCenter()).isPresent()
                        || cm != null && cm.getBoundingBox().contains(eye);
                AwakeningSystemMod.LOGGER.info("[MobShots] roused: client box {} server box {} ray hits {}", cm == null ? "-" : cm.getBoundingBox(), mob.getBoundingBox(), hit);
                server.execute(() -> {
                    var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                    if (sp == null || !(mob instanceof net.minecraft.world.entity.LivingEntity le)) return;
                    float before = le.getHealth();
                    sp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                    sp.attack(le);
                    AwakeningSystemMod.LOGGER.info("[MobShots] survival hit: health {} -> {}", before, le.getHealth());
                    sp.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                });
            }
            if (k == 7 && phase == 20) server.execute(() -> { if (mob instanceof Mob mm) mm.swing(net.minecraft.world.InteractionHand.MAIN_HAND); });
            if (phase == 30) Screenshot.grab(mc.gameDirectory, "ms_" + k + ".png", mc.getMainRenderTarget(), msg -> {});
            return;
        }
        if (t > 60 && System.getProperty("awakening.mobFight") == null && mob instanceof Mob m && !m.level().isClientSide && t % 20 == 0) server.execute(() -> m.getNavigation().moveTo(20, 201, 0, 0.8));
        if (t > 60 && mob != null && t % 5 == 0) AwakeningSystemMod.LOGGER.info("[MobShots] t={} x={} y={}", t, String.format("%.2f", mob.getX()), String.format("%.2f", mob.getY()));
        if (t >= 100 && t <= 280 && t % 30 == 10)
            Screenshot.grab(mc.gameDirectory, "ms_" + ((t - 100) / 30) + ".png", mc.getMainRenderTarget(), msg -> {});
        if (t == 300) mc.stop();
    }
}
