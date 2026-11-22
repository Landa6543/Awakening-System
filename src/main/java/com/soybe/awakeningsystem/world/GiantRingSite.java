package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The one giant ring of Teyvat. Its site is worked out from the world's seed, some 700 blocks from Teyvat's
 * spawn; the ruin is raised there the first time a player comes within sight of it. In front of it stands a
 * small altar (a lodestone): once it has been given everything in {@link #COST}, over any number of visits,
 * the ring is repaired. The repaired ring is the gate to
 * Aethergard, which will open once that dimension exists.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class GiantRingSite {
    private GiantRingSite() {}

    /**
     * What the altar asks for to rebuild the ring, given over as many visits as it takes (by anyone): stone to
     * rebuild it, netherite to bind it and primogems (200 wishes' worth) and treasures of every world to wake its runes.
     */
    public static final String[][] COST = {{"awakening_system:primogem", "32000"}, {"minecraft:netherite_ingot", "64"},
            {"minecraft:calcite", "4096"}, {"minecraft:nether_star", "8"}, {"minecraft:echo_shard", "128"},
            {"minecraft:heart_of_the_sea", "8"}, {"minecraft:dragon_breath", "64"}, {"minecraft:totem_of_undying", "16"}};
    private static final int DISTANCE = 700, SIGHT = 220;

    /** Where the ring is and in what state, kept with Teyvat's data. */
    public static final class Data extends SavedData {
        BlockPos base;
        Direction facing = Direction.NORTH;
        boolean built, repaired;
        /** False for rings raised before the ground was read right (they may stand buried in a cave). */
        boolean onSurface;
        /** What has been given to the altar so far, by item id. */
        final java.util.Map<String, Integer> given = new java.util.HashMap<>();

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data d = new Data();
            d.base = NbtUtils.readBlockPos(tag, "Base").orElse(null);
            d.facing = Direction.from2DDataValue(tag.getInt("Facing"));
            d.built = tag.getBoolean("Built");
            d.repaired = tag.getBoolean("Repaired");
            d.onSurface = tag.getBoolean("OnSurface");
            CompoundTag g = tag.getCompound("Given");
            for (String k : g.getAllKeys()) d.given.put(k, g.getInt(k));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            if (base != null) tag.put("Base", NbtUtils.writeBlockPos(base));
            tag.putInt("Facing", facing.get2DDataValue());
            tag.putBoolean("Built", built);
            tag.putBoolean("Repaired", repaired);
            tag.putBoolean("OnSurface", onSurface);
            CompoundTag g = new CompoundTag();
            given.forEach(g::putInt);
            tag.put("Given", g);
            return tag;
        }
    }

    /** The ring's record in Teyvat (for the way to Aethergard). */
    static Data dataOf(ServerLevel teyvat) {
        return data(teyvat);
    }

    private static Data data(ServerLevel teyvat) {
        return teyvat.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), "awakening_giant_ring");
    }

    /** The ring's site in Teyvat (its foot, the ground not yet looked at), chosen once from the seed. */
    private static Data site(ServerLevel teyvat) {
        Data d = data(teyvat);
        if (d.base == null) {
            java.util.Random r = new java.util.Random(teyvat.getSeed() ^ 0x5A17E5L);
            double a = r.nextDouble() * Math.PI * 2;
            BlockPos spawn = teyvat.getSharedSpawnPos();
            d.base = new BlockPos(spawn.getX() + (int) (Math.cos(a) * DISTANCE), 0, spawn.getZ() + (int) (Math.sin(a) * DISTANCE));
            // the opening faces back towards the spawn
            d.facing = Direction.getNearest(-Math.cos(a), 0, -Math.sin(a));
            d.setDirty();
        }
        return d;
    }

    /** The altar in front of the ring, on the spawn's side. */
    public static BlockPos altar(BlockPos base, Direction facing) {
        return base.relative(facing, GiantPortalRing.DEPTH / 2 + 7);
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 100 != 0) return;
        ServerLevel teyvat = event.getServer().getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null || teyvat.players().isEmpty()) return;
        Data d = site(teyvat);
        if (d.built && d.onSurface) return;
        for (ServerPlayer p : teyvat.players()) {
            double dx = p.getX() - d.base.getX(), dz = p.getZ() - d.base.getZ();
            if (dx * dx + dz * dz > (double) SIGHT * SIGHT) continue;
            if (d.built) {
                // raised before the ground was read right: if it stands buried, it rises again on the surface
                BlockPos ground = GiantPortalRing.siteGround(teyvat, d.base, d.facing);
                if (ground.getY() > d.base.getY() + 6) raise(teyvat, d);
                else {
                    d.onSurface = true;
                    d.setDirty();
                }
            } else raise(teyvat, d);
            return;
        }
    }

    /** Raises the ruin on the ground at its site, with its altar. */
    private static void raise(ServerLevel teyvat, Data d) {
        BlockPos ground = GiantPortalRing.siteGround(teyvat, d.base, d.facing);
        d.base = ground;
        d.onSurface = true;
        GiantPortalRing.clearSite(teyvat, ground, d.facing);
        GiantPortalRing.build(teyvat, ground, d.facing, d.repaired);
        placeAltar(teyvat, ground, d.facing);
        d.built = true;
        d.setDirty();
        AwakeningSystemMod.LOGGER.info("Giant ring raised in Teyvat at {}", ground);
    }

    private static void placeAltar(ServerLevel level, BlockPos base, Direction facing) {
        BlockPos a = GiantPortalRing.ground(level, altar(base, facing));
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) level.setBlock(a.offset(x, -1, z), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(a, Blocks.LODESTONE.defaultBlockState(), 3);
        level.setBlock(a.above(), Blocks.AIR.defaultBlockState(), 3);
    }

    /** The altar: once it has been given everything it asks for, the ring is made whole again. */
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND || !level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) return;
        if (!level.getBlockState(event.getPos()).is(Blocks.LODESTONE)) return;
        Data d = data(level);
        if (!d.built || d.base == null) return;
        BlockPos altar = event.getPos();
        BlockPos expected = altar(d.base, d.facing);
        if (Math.abs(altar.getX() - expected.getX()) > 1 || Math.abs(altar.getZ() - expected.getZ()) > 1) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (d.repaired) {
            player.displayClientMessage(Component.translatable("awakening_system.giant_portal.already_repaired"), true);
            return;
        }
        // the player gives what they carry of each thing still missing (creative players give it all)
        boolean gaveSome = false, done = true;
        StringBuilder status = new StringBuilder();
        var inv = player.getInventory();
        for (String[] need : COST) {
            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(need[0]));
            int wanted = Integer.parseInt(need[1]);
            int have = d.given.getOrDefault(need[0], 0);
            if (have < wanted) {
                if (player.isCreative()) {
                    have = wanted;
                    gaveSome = true;
                } else {
                    for (int i = 0; i < inv.getContainerSize() && have < wanted; i++) {
                        ItemStack s = inv.getItem(i);
                        if (!s.is(item)) continue;
                        int take = Math.min(wanted - have, s.getCount());
                        s.shrink(take);
                        have += take;
                        gaveSome = true;
                    }
                }
                d.given.put(need[0], have);
            }
            if (have < wanted) done = false;
            if (status.length() > 0) status.append("  ·  ");
            status.append(item.getDescription().getString()).append(' ').append(have).append('/').append(wanted);
        }
        d.setDirty();
        if (!done) {
            player.displayClientMessage(Component.translatable(gaveSome ? "awakening_system.giant_portal.given" : "awakening_system.giant_portal.need",
                    status.toString()), false);
            level.playSound(null, altar, gaveSome ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, gaveSome ? 1.0F : 0.6F);
            if (gaveSome) level.sendParticles(ParticleTypes.ENCHANT, altar.getX() + 0.5, altar.getY() + 1.2, altar.getZ() + 0.5, 40, 0.4, 0.4, 0.4, 0.5);
            return;
        }        GiantPortalRing.build(level, d.base, d.facing, true);
        d.repaired = true;
        d.setDirty();
        var c = GiantPortalRing.centre(d.base);
        level.sendParticles(ParticleTypes.END_ROD, c.getX() + 0.5, c.getY(), c.getZ() + 0.5, 300, 14, 14, 14, 0.05);
        level.playSound(null, altar, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 0.7F);
        level.playSound(null, altar, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("awakening_system.giant_portal.repaired"), false);
    }

    /** For /giantportal fix (testing): raises the ring if needed and repairs it, without the offerings. */
    public static Component forceRepair(net.minecraft.server.MinecraftServer server) {
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) return Component.translatable("awakening_system.giant_portal.no_teyvat");
        Data d = site(teyvat);
        if (!d.built || !d.onSurface) raise(teyvat, d);
        GiantPortalRing.build(teyvat, d.base, d.facing, true);
        d.repaired = true;
        d.setDirty();
        return Component.translatable("awakening_system.giant_portal.locate_repaired", d.base.getX(), d.base.getZ());
    }

    /** For /giantportal locate: the site in Teyvat. */
    public static Component describe(net.minecraft.server.MinecraftServer server) {
        ServerLevel teyvat = server.getLevel(TeyvatWorldManager.TEYVAT_WORLD);
        if (teyvat == null) return Component.translatable("awakening_system.giant_portal.no_teyvat");
        Data d = site(teyvat);
        return Component.translatable(d.repaired ? "awakening_system.giant_portal.locate_repaired" : d.built
                ? "awakening_system.giant_portal.locate_ruined" : "awakening_system.giant_portal.locate_unbuilt", d.base.getX(), d.base.getZ());
    }
}
