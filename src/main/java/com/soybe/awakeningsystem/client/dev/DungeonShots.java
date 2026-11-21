package com.soybe.awakeningsystem.client.dev;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.DungeonManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.dungeonShots: opens a gate dungeon for the player, logs its
 * rooms and how flat their floors are, then photographs a few rooms from inside (screenshots/ds_*.png).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class DungeonShots {
    private DungeonShots() {}

    private static final boolean ON = System.getProperty("awakening.dungeonShots") != null;
    private static int t = -1;
    private static java.util.List<BlockPos> rooms = new java.util.ArrayList<>();

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null && !(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen)) mc.setScreen(null);
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) {
            mc.options.hideGui = true;
            com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(false);
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp == null) return;
                server.getCommands().performPrefixedCommand(sp.createCommandSourceStack().withPermission(4), "system dungeon E crypt");
            });
        }
        if (t == 260) server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            DungeonManager.DungeonSession s = DungeonManager.getSession(sp.getUUID());
            if (s == null) { AwakeningSystemMod.LOGGER.info("[DungeonShots] no session at {}", sp.blockPosition()); return; }
            ServerLevel lvl = server.getLevel(s.dungeonDimension);
            int holes = 0, checked = 0;
            for (var r : s.activeRooms) {
                rooms.add(r.pos);
                for (int dx = -6; dx <= 6; dx += 3) for (int dz = -6; dz <= 6; dz += 3) {
                    BlockPos fl = new BlockPos(r.pos.getX() + dx, r.pos.getY() - 1, r.pos.getZ() + dz);
                    checked++;
                    if (lvl.getBlockState(fl).isAir() && lvl.getBlockState(fl.below()).isAir()) holes++;
                }
            }
            AwakeningSystemMod.LOGGER.info("[DungeonShots] layout {} rooms {} corridors {} floor holes {}/{} throne {}", s.layoutType, s.activeRooms.size(),
                    s.activeCorridors.size(), holes, checked, s.thronePos);
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode spectator " + sp.getGameProfile().getName());
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "effect give " + sp.getGameProfile().getName() + " minecraft:night_vision 600 0 true");
        });
        if (t == 250) server.execute(() -> {
            // loot check: how many gate monsters are Limbo stalkers, and what the first dozen drop when a player kills them
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            DungeonManager.DungeonSession s = sp == null ? null : DungeonManager.getSession(sp.getUUID());
            if (s == null) return;
            ServerLevel lvl = server.getLevel(s.dungeonDimension);
            var mobs = new java.util.ArrayList<net.minecraft.world.entity.Mob>();
            for (var e : lvl.getAllEntities()) if (e instanceof net.minecraft.world.entity.Mob m && m.getTags().contains("dungeon_mob")) mobs.add(m);
            long limbo = mobs.stream().filter(m -> m instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature).count();
            java.util.Map<String, Integer> drops = new java.util.TreeMap<>();
            int killed = 0;
            for (var e : lvl.getAllEntities()) if (e instanceof net.minecraft.world.entity.item.ItemEntity) e.addTag("ds_counted");
            for (var m : mobs) {
                if (killed >= 12) break;
                var at = m.blockPosition();
                m.hurt(sp.damageSources().playerAttack(sp), Float.MAX_VALUE);
                if (!m.isDeadOrDying()) continue;
                killed++;
                AwakeningSystemMod.LOGGER.info("[DungeonShots] killed {} lvl {} hp {} tags {}", m.getType().getDescriptionId(), m.getPersistentData().getInt("rpg_mob_level"), m.getMaxHealth(), m.getTags());
                for (var it : lvl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(at).inflate(3))) {
                    if (it.getTags().contains("ds_counted")) continue;
                    it.addTag("ds_counted");
                    drops.merge(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(it.getItem().getItem()).getPath(), it.getItem().getCount(), Integer::sum);
                }
            }
            AwakeningSystemMod.LOGGER.info("[DungeonShots] dungeon mobs {} limbo {} killed {} drops {}", mobs.size(), limbo, killed, drops);
        });
        int[] at = {300, 360, 420, 480};
        for (int i = 0; i < at.length; i++) {
            final int k = i;
            if (t == at[i] && rooms.size() > 0) server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                BlockPos r = rooms.get(Math.min(rooms.size() - 1, new int[]{0, 1, 4, 8}[k]));
                DungeonManager.DungeonSession ss = DungeonManager.getSession(sp.getUUID()); if (sp != null && ss != null) sp.teleportTo(server.getLevel(ss.dungeonDimension), r.getX() + 0.5, r.getY() + 3, r.getZ() + 8.5, 180, 15);
            });
            if (t == at[i] + 50) Screenshot.grab(mc.gameDirectory, "ds_" + i + ".png", mc.getMainRenderTarget(), msg -> {});
        }
        if (t == 560) mc.stop();
    }
}
