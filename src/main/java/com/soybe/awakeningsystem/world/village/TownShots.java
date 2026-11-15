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
            new View("people", 6, 16, -1, 2, 160, 8)};

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
                MedievalVillage.build(floor, 0, 0);
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
        if (k == 55) Screenshot.grab(mc.gameDirectory, "ts_" + (i < 10 ? "0" : "") + i + "_" + v.name() + ".png", mc.getMainRenderTarget(), msg -> {});
    }
}
