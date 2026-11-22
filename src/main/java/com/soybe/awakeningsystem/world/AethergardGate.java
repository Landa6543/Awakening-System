package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The way between Teyvat's giant ring and Aethelgard, the RPG realm. Once the ring is
 * repaired, it works both ways:
 * <ul>
 * <li>a twin ring stands on dry land in Aethelgard, facing the same way, so the two openings line up;</li>
 * <li>a player near either ring sees the other world through it, live: its blocks as they change, its light,
 * its creatures and players moving (see {@link RingLiveView} and {@code client.AethergardWindow});</li>
 * <li>walking through the opening takes the player to the other ring, at the same place in it, with no
 * loading screen in between.</li>
 * </ul>
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class AethergardGate {
    private AethergardGate() {}

    /** Aethelgard, the RPG realm of the roadmap (capital Oakhaven, the Adventurers' Guild, Niflheim in the cold north). */
    public static final ResourceKey<Level> AETHERGARD = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "aethelgard_realm"));

    // ---- the twin ring in Aethelgard

    public static final class Data extends SavedData {
        boolean landingBuilt, onSurface;
        int style;
        Direction facing = Direction.NORTH;
        BlockPos base = BlockPos.ZERO;

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            d.landingBuilt = tag.getBoolean("Built");
            d.onSurface = tag.getBoolean("OnSurface");
            d.style = tag.getInt("Style");
            d.base = net.minecraft.nbt.NbtUtils.readBlockPos(tag, "Base").orElse(BlockPos.ZERO);
            d.facing = Direction.from2DDataValue(tag.getInt("Facing"));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("Built", landingBuilt);
            tag.putBoolean("OnSurface", onSurface);
            tag.putInt("Style", style);
            tag.put("Base", net.minecraft.nbt.NbtUtils.writeBlockPos(base));
            tag.putInt("Facing", facing.get2DDataValue());
            return tag;
        }
    }

    private static Data data(ServerLevel aether) {
        return aether.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), "awakening_aethergard_gate");
    }

    /** Builds the twin ring on dry land near the realm's centre, facing like Teyvat's, the first time it is needed. */
    private static void ensureLanding(ServerLevel aether, Direction facing) {
        Data d = data(aether);
        if (d.landingBuilt && d.onSurface && d.style >= GiantRingSite.STYLE) return;
        BlockPos ground = null;
        if (d.landingBuilt) {
            // built before the ground was read right: it stays unless it stands buried
            BlockPos here = GiantPortalRing.siteGround(aether, d.base, d.facing);
            GiantPortalRing.removeOld(aether, d.base, d.facing);
            if (here.getY() <= d.base.getY() + 6) {
                // standing right, but built in an older look: rebuilt where it is
                GiantPortalRing.build(aether, d.base, d.facing, true);
                d.onSurface = true;
                d.style = GiantRingSite.STYLE;
                d.setDirty();
                return;
            }
            ground = here;
        }
        // the first spot along the east road from 0,0 that is land, not sea
        for (int step = 0; step < 40 && ground == null; step++) {
            BlockPos probe = GiantPortalRing.ground(aether, new BlockPos(step * 64, 0, 0));
            if (aether.getBlockState(probe.below()).getFluidState().isEmpty() && probe.getY() > aether.getSeaLevel()) ground = probe;
        }
        if (ground == null) ground = GiantPortalRing.ground(aether, BlockPos.ZERO);
        ground = GiantPortalRing.siteGround(aether, ground, facing);
        // the opening and the ground round the ring's foot cleared of trees and hills
        GiantPortalRing.clearSite(aether, ground, facing);
        GiantPortalRing.build(aether, ground, facing, true);
        d.landingBuilt = true;
        d.onSurface = true;
        d.style = GiantRingSite.STYLE;
        d.facing = facing;
        d.base = ground;
        d.setDirty();
        AwakeningSystemMod.LOGGER.info("Aethelgard: the twin ring stands at {}", ground);
    }

    // ---- the two ends

    /** One end of the way: the level, the centre of its ring's opening and the way the ring faces. */
    record End(ServerLevel level, BlockPos centre, Direction facing) {}

    /** Both ends, if the Teyvat ring is repaired: [Teyvat, Aethelgard]. */
    private static End[] ends(MinecraftServer server) {
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        ServerLevel aether = server.getLevel(AETHERGARD);
        if (teyvat == null || aether == null) return null;
        GiantRingSite.Data site = GiantRingSite.dataOf(teyvat);
        if (site == null || !site.repaired || site.base == null) return null;
        ensureLanding(aether, site.facing);
        return new End[]{new End(teyvat, GiantPortalRing.centre(site.base), site.facing),
                new End(aether, GiantPortalRing.centre(data(aether).base), data(aether).facing)};
    }

    // ---- crossing and the view

    private static final Map<UUID, Double> LAST_SIDE = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int t = server.getTickCount();
        RingLiveView.tick(server);
        End[] ends = null;
        boolean looked = false;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            boolean inTeyvat = p.level().dimension().equals(TeyvatWorldManager.TEYVAT_WORLD);
            boolean inAether = p.level().dimension().equals(AETHERGARD);
            if (!inTeyvat && !inAether) {
                if (t % 20 == 0) RingLiveView.stop(p);
                continue;
            }
            if (!looked) {
                ends = ends(server);
                looked = true;
            }
            if (ends == null) {
                if (t % 20 == 0) RingLiveView.stop(p);
                continue;
            }
            End here = inTeyvat ? ends[0] : ends[1], there = inTeyvat ? ends[1] : ends[0];
            crossing(p, here, there, t);
            if (t % 10 == 0) RingLiveView.update(p, here, there);
        }
    }

    /** Through the opening: from one side of the ring to the other, inside its hole, takes you across. */
    private static void crossing(ServerPlayer p, End here, End there, long tick) {
        Direction f = here.facing;
        Vec3 c = Vec3.atCenterOf(here.centre);
        Vec3 rel = p.position().add(0, 0.9, 0).subtract(c);
        Direction right = f.getClockWise();
        double u = rel.x * right.getStepX() + rel.z * right.getStepZ();
        double v = rel.y;
        double w = rel.x * f.getStepX() + rel.z * f.getStepZ();
        Double last = LAST_SIDE.put(p.getUUID(), w);
        double hole = GiantPortalRing.R - GiantPortalRing.T - 1;
        if (last == null || u * u + v * v > hole * hole || Math.abs(w) > 2) return;
        if (Math.signum(last) == Math.signum(w) || last == 0) return;
        if (COOLDOWN.getOrDefault(p.getUUID(), 0L) > tick) return;
        // arrive at the same place in the other ring, a step past its opening, looking the same way and still moving
        Vec3 out = Vec3.atCenterOf(there.centre).add(right.getStepX() * u, v - 0.9, right.getStepZ() * u)
                .add(f.getStepX() * Math.signum(w) * 2.0, 0, f.getStepZ() * Math.signum(w) * 2.0);
        Vec3 motion = p.getDeltaMovement();
        COOLDOWN.put(p.getUUID(), tick + 60);
        LAST_SIDE.remove(p.getUUID());
        // the client is told first, so it skips the loading screen
        PacketDistributor.sendToPlayer(p, new RingLiveView.CrossPayload());
        RingLiveView.stop(p);
        p.teleportTo(there.level, out.x, out.y, out.z, p.getYRot(), p.getXRot());
        p.setDeltaMovement(motion);
        p.hurtMarked = true;
        there.level.playSound(null, out.x, out.y, out.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.6F);
    }
}
