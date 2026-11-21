package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.tower.GreatTowerWorldManager;
import com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * A development check, only active with -Dawakening.towerMobs=1,2,3,...: stands a creative player on each Great Tower
 * floor, asks for the floor's monsters and logs every monster that dies or vanishes within its first 30 seconds, with
 * the cause, so "they appear and die at once" can be traced.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class TowerMobCheck {
    private TowerMobCheck() {}

    private static final String FLOORS = System.getProperty("awakening.towerMobs");
    private static final int WAIT = 400;
    private static int t = -1;

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (FLOORS == null || e.getEntity().level().isClientSide() || !(e.getEntity() instanceof Mob m)) return;
        if (!GreatTowerWorldManager.isGreatTowerDimension(m.level()) || m.tickCount > 600) return;
        BlockPos p = m.blockPosition();
        AwakeningSystemMod.LOGGER.info("[TowerMobs] DIED {} age {} at {} by {} in {} feet {} hp {}", m.getType().getDescriptionId(), m.tickCount, p,
                e.getSource().getMsgId(), m.level().getBlockState(p.above()).getBlock(), m.level().getBlockState(p).getBlock(), m.getMaxHealth());
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent e) {
        if (FLOORS == null || e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob m)) return;
        if (!GreatTowerWorldManager.isGreatTowerDimension(e.getLevel()) || m.tickCount > 600 || m.getRemovalReason() == null) return;
        if (m.getRemovalReason() == net.minecraft.world.entity.Entity.RemovalReason.KILLED) return;
        AwakeningSystemMod.LOGGER.info("[TowerMobs] GONE {} age {} at {} reason {}", m.getType().getDescriptionId(), m.tickCount, m.blockPosition(), m.getRemovalReason());
    }

    private static final boolean PORTALS = System.getProperty("awakening.portalCheck") != null;

    /** With -Dawakening.portalCheck: enters floors 1-24 one by one and logs where the way up stands and how it sits. */
    private static void portalCheck(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        if (t < 60 || (t - 60) % 50 != 0) return;
        int f = 1 + (t - 60) / 50;
        if (f > 24) { mc.stop(); return; }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            GreatTowerWorldManager.enterFloor(sp, f);
            ServerLevel lvl = server.getLevel(GreatTowerWorldManager.getDimensionForFloor(f));
            var lay = GreatTowerWorldManager.getLayout(f);
            BlockPos found = null;
            var mp = new BlockPos.MutableBlockPos();
            for (int dx = -260; dx <= 260 && found == null; dx++) for (int dz = -260; dz <= 260 && found == null; dz++) {
                int x = lay.advanceX() + dx, z = lay.advanceZ() + dz;
                if (Math.abs(dx) > 40 && Math.abs(dz) > 40) continue;   // near the layout's spot only
                if (!lvl.hasChunk(x >> 4, z >> 4)) continue;
                for (int y = lvl.getMinBuildHeight(); y < 200; y++)
                    if (lvl.getBlockState(mp.set(x, y, z)).getBlock() instanceof com.soybe.awakeningsystem.block.AscensionPortalBlock b
                            && lvl.getBlockState(mp).toString().contains("ascend")) { found = mp.immutable(); break; }
            }
            int ground = TowerFloorTerrain.height(f, lay.advanceX(), lay.advanceZ());
            AwakeningSystemMod.LOGGER.info("[PortalCheck] floor {} layout adv {},{} terrain {} -> portal {} sky {}", f, lay.advanceX(), lay.advanceZ(), ground, found,
                    found == null ? "-" : lvl.canSeeSky(found.above(4)));
        });
    }

    private static final String BEACON = System.getProperty("awakening.beaconShot");

    /** With -Dawakening.beaconShot=FLOOR: arrives on the floor and photographs the way up's light from the arrival. */
    private static void beaconShot(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        int f = Integer.parseInt(BEACON);
        if (t == 60) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set day");
            GreatTowerWorldManager.enterFloor(sp, f);
        });
        if (t == 140) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            var lay = GreatTowerWorldManager.getLayout(f);
            double dx = lay.advanceX() - sp.getX(), dz = lay.advanceZ() - sp.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90F;
            sp.teleportTo(sp.serverLevel(), sp.getX(), sp.getY() + 12, sp.getZ(), yaw, 8);
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode spectator " + sp.getGameProfile().getName());
        });
        if (t == 260) { mc.options.hideGui = true; }
        if (t == 270) net.minecraft.client.Screenshot.grab(mc.gameDirectory, "beacon_" + f + ".png", mc.getMainRenderTarget(), msg -> {});
        if (t == 290) mc.stop();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (BEACON != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
            mc.options.pauseOnLostFocus = false;
            if (mc.screen != null) mc.setScreen(null);
            t++;
            beaconShot(mc);
            return;
        }
        if (PORTALS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
            mc.options.pauseOnLostFocus = false;
            if (mc.screen != null) mc.setScreen(null);
            t++;
            portalCheck(mc);
            return;
        }
        if (FLOORS == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) mc.setScreen(null);
        t++;
        var server = mc.getSingleplayerServer();
        String[] floors = FLOORS.split(",");
        if (t == 40) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode creative " + sp.getGameProfile().getName());
        });
        if (t < 60) return;
        int k = (t - 60) / WAIT, phase = (t - 60) % WAIT;
        if (k >= floors.length) { if (phase == 20) mc.stop(); return; }
        int f = Integer.parseInt(floors[k].trim());
        if (phase == 0) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            ServerLevel lvl = server.getLevel(GreatTowerWorldManager.getDimensionForFloor(f));
            if (sp == null || lvl == null) return;
            int x = -100, z = -100, g = TowerFloorTerrain.height(f, x, z);
            sp.teleportTo(lvl, x + 0.5, g + 2, z + 0.5, 0, 0);
        });
        if (phase == 80) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            ServerLevel lvl = sp.serverLevel();
            for (int i = 0; i < 3; i++) GreatTowerWorldManager.ensureFloorSpawns(lvl, sp.blockPosition(), f);
            long n = lvl.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(128)).size();
            AwakeningSystemMod.LOGGER.info("[TowerMobs] floor {} at {} mobs near {}", f, sp.blockPosition(), n);
            // ores in the 5x5 chunks around: what a player would find digging here
            java.util.Map<String, Integer> ores = new java.util.TreeMap<>();
            var c = sp.chunkPosition();
            for (int cx = c.x - 2; cx <= c.x + 2; cx++) for (int cz = c.z - 2; cz <= c.z + 2; cz++) {
                var ch = lvl.getChunk(cx, cz);
                var mp = new BlockPos.MutableBlockPos();
                for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = lvl.getMinBuildHeight(); y < lvl.getMaxBuildHeight(); y++) {
                    var st = ch.getBlockState(mp.set(c.getMinBlockX() + (cx - c.x) * 16 + x, y, c.getMinBlockZ() + (cz - c.z) * 16 + z));
                    String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();
                    if (id.endsWith("_ore")) ores.merge(id, 1, Integer::sum);
                }
            }
            AwakeningSystemMod.LOGGER.info("[TowerMobs] floor {} ores in 80x80 {}", f, ores);
        });
        if (phase == WAIT - 5) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            var mobs = sp.serverLevel().getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(128));
            java.util.Map<String, Integer> kinds = new java.util.TreeMap<>();
            for (Mob m : mobs) kinds.merge(m.getType().getDescriptionId().replace("entity.awakening_system.", "") + "@" + m.getPersistentData().getInt("rpg_mob_level"), 1, Integer::sum);
            AwakeningSystemMod.LOGGER.info("[TowerMobs] floor {} after 16s alive {}", f, kinds);
        });
    }
}
