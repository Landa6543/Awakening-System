package com.soybe.awakeningsystem.world.village;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active with -Dawakening.townShots: builds the town on floor 11 of the tower
 * in a singleplayer world and takes pictures of it from a few places (screenshots/ts_*.png), then closes the game.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class TownShots {
    private TownShots() {}

    private static final boolean ON = System.getProperty("awakening.townShots") != null;
    private static int t = -1;
    private static final java.util.List<TownNpcEntity> LINEUP = new java.util.ArrayList<>();
    private static boolean wasSao;

    /** name, x, z, absolute y (or -1 for "on the ground" plus the offset), y offset, yaw, pitch */
    private record View(String name, int x, int z, int y, int up, float yaw, float pitch) {}

    private static final View[] VIEWS = {
            new View("aerial_se", 75, 75, 135, 0, 135, 30),
            new View("aerial_nw", -80, -70, 135, 0, -45, 30),
            new View("high_street", 0, 45, -1, 2, 180, 3),
            new View("chapel", 0, -86, -1, 2, 180, -12),
            new View("cross_west", -32, 0, -1, 2, 90, 3),
            new View("cross_east", 32, 0, -1, 2, -90, 3),
            new View("ring_north", -20, -45, -1, 2, -90, 3),
            new View("fields", 50, 48, -1, 9, -45, 30),
            new View("square", -12, 12, -1, 4, -135, 15),
            new View("tavern_inside", 0, 0, 0, 0, 90, 5),
            new View("people", 6, 16, -1, 2, 160, 8),
            new View("lineup", 0, 117, -1, 1, 0, 4),
            new View("talk_rpg", 0, 127, -1, 1, 0, 8),
            new View("talk_sao", 0, 127, -1, 1, 0, 8)};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        t++;
        var server = mc.getSingleplayerServer();
        ServerLevel floor = server.getLevel(com.soybe.awakeningsystem.tower.GreatTowerWorldManager.getDimensionForFloor(11));
        if (floor == null) return;
        if (t == 40) {
            server.execute(() -> {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamerule doDaylightCycle false");
                floor.setDayTime(6000);
                // the test world keeps the people of earlier runs: clear them first
                for (var old : floor.getEntities(TownNpcContent.TOWN_NPC, e -> true)) old.discard();
                MedievalVillage.build(floor, 0, 0);
                // every skin in a row on a clear, flat lawn, standing still and facing the camera
                int gy = floor.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 130);
                for (int px = -18; px <= 18; px++)
                    for (int pz = 112; pz <= 136; pz++)
                        for (int py = gy - 3; py <= gy + 14; py++)
                            floor.setBlock(new BlockPos(px, py, pz), py < gy - 1 ? net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()
                                    : py == gy - 1 ? net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState()
                                    : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                for (int i = 0; i < TownNpcEntity.SKINS.length; i++) {
                    int x = -TownNpcEntity.SKINS.length + 1 + i * 2, z = 130, y = gy;
                    TownNpcEntity npc = TownNpcContent.TOWN_NPC.create(floor);
                    if (npc == null) continue;
                    npc.moveTo(x + 0.5, y, z + 0.5, 180, 0);
                    npc.settle(new BlockPos(x, y, z), "", i);
                    npc.setNoAi(true);
                    npc.setYHeadRot(180);
                    npc.setYBodyRot(180);
                    floor.addFreshEntity(npc);
                    LINEUP.add(npc);
                }
            });
            mc.options.hideGui = true;
        }
        if (t < 200) return;
        int i = (t - 200) / 60, k = (t - 200) % 60;
        if (i >= VIEWS.length) {
            if (k == 5) mc.stop();
            return;
        }
        View v = VIEWS[i];
        String only = System.getProperty("awakening.townShotsOnly", "");
        if (!only.isEmpty() && !only.contains(v.name())) { if (k == 0) t += 59; return; }
        if (k == 0) server.execute(() -> {
            var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) return;
            double x = v.x() + 0.5, z = v.z() + 0.5, y;
            if (v.name().equals("tavern_inside")) {
                // the tavern's door faces the high street at (4, ?, -20): stand inside, looking across the room
                int dy = 60;
                while (dy < 200 && !(floor.getBlockState(new BlockPos(4, dy, -20)).getBlock() instanceof DoorBlock)) dy++;
                x = 12.5; y = dy; z = -19.5;
            } else if (v.y() >= 0) y = v.y();
            else y = floor.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, v.x(), v.z()) + v.up();
            sp.teleportTo(floor, x, y, z, v.yaw(), v.pitch());
            sp.getAbilities().flying = true;
            sp.getAbilities().mayfly = true;
            sp.onUpdateAbilities();
        });
        if (k == 10 && v.name().startsWith("talk")) {
            if (v.name().equals("talk_sao")) { wasSao = com.soybe.awakeningsystem.client.sao.UiStyle.fullDive(); com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(true); }
            mc.options.hideGui = false;
            server.execute(() -> {
                var sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (sp != null && LINEUP.size() > 4) com.soybe.awakeningsystem.dialogue.NpcDialogueManager.startDialogue(sp, LINEUP.get(4), "town_npc");
            });
        }
        if (k == 58 && v.name().startsWith("talk")) {
            if (mc.screen != null) mc.screen.onClose();
            mc.options.hideGui = true;
            if (v.name().equals("talk_sao")) com.soybe.awakeningsystem.client.sao.UiStyle.setFullDive(wasSao);
        }
        if (k == 54) System.out.println("[TownShots] " + v.name() + " cam=" + mc.player.position() + " yaw=" + mc.player.getYRot()
                + " npc0=" + (LINEUP.isEmpty() ? "-" : LINEUP.get(0).position()));
        if (k == 55) Screenshot.grab(mc.gameDirectory, "ts_" + (i < 10 ? "0" : "") + i + "_" + v.name() + ".png", mc.getMainRenderTarget(), msg -> {});
    }
}
