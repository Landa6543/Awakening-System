package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tells each player on a tower floor where that floor's way up stands, so their game draws a column of light over
 * it (TowerWayUpBeam) seen from anywhere on the floor. The spot is remembered when the floor is entered (where its
 * shrine was built); after a restart it is found again by looking for the portal block around the floor's planned spot.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TowerPortalBeacon {
    private TowerPortalBeacon() {}

    private static final Map<Integer, BlockPos> SPOTS = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> LOOKED = new ConcurrentHashMap<>();
    /** What each player was last told: the spot, or null for nothing. */
    private static final Map<UUID, BlockPos> TOLD = new ConcurrentHashMap<>();
    private static final BlockPos NONE = new BlockPos(0, Integer.MIN_VALUE, 0);

    public static void remember(int floor, BlockPos ground) {
        SPOTS.put(floor, ground);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        SPOTS.clear();   // the next world opened has floors of its own
        LOOKED.clear();
        TOLD.clear();
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        // Desactivado: el haz tipo faro fue eliminado por peticion del usuario
        if (true) return;
        var server = event.getServer();
        if (server.getTickCount() % 10 != 0) return;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            ServerLevel level = sp.serverLevel();
            BlockPos at = NONE;
            if (GreatTowerWorldManager.isGreatTowerDimension(level)) {
                int floor = GreatTowerWorldManager.getFloorForPos(level, sp.blockPosition());
                if (floor < GreatTowerWorldManager.TOWER_FLOORS.length) {   // the top floor has no way up
                    BlockPos spot = SPOTS.get(floor);
                    if (spot == null) spot = find(level, floor, server.getTickCount());
                    if (spot != null) at = spot;
                }
            }
            if (!at.equals(TOLD.get(sp.getUUID()))) {
                TOLD.put(sp.getUUID(), at);
                PacketDistributor.sendToPlayer(sp, at == NONE ? new TowerWayUpPayload(false, 0, 0, 0) : new TowerWayUpPayload(true, at.getX(), at.getY(), at.getZ()));
            }
        }
    }

    /** After a restart: the portal block near the floor's planned spot, looked for at most once a minute. */
    private static BlockPos find(ServerLevel level, int floor, long now) {
        Long last = LOOKED.get(floor);
        if (last != null && now - last < 1200) return null;
        LOOKED.put(floor, now);
        var lay = GreatTowerWorldManager.getLayout(floor);
        var m = new BlockPos.MutableBlockPos();
        for (int dx = -24; dx <= 24; dx++) for (int dz = -24; dz <= 24; dz++) {
            int x = lay.advanceX() + dx, z = lay.advanceZ() + dz;
            if (!level.hasChunk(x >> 4, z >> 4)) continue;
            for (int y = level.getMinBuildHeight() + 1; y < 160; y++) {
                var st = level.getBlockState(m.set(x, y, z));
                if (st.getBlock() instanceof AscensionPortalBlock && st.getValue(AscensionPortalBlock.PORTAL_TYPE) == AscensionPortalBlock.PortalType.ASCEND) {
                    BlockPos found = new BlockPos(x, y - 1, z);
                    SPOTS.put(floor, found);
                    return found;
                }
            }
        }
        return null;
    }
}
