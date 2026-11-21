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

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
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
