package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Meteor craters heal: some days after the impact the ground goes back to what it was before.
 * <ul>
 *   <li>At impact the box around the crater is recorded twice: the ground before, and the crater just made.</li>
 *   <li>After {@link #HEAL_AFTER_TICKS} (5 Minecraft days), with no player close by, every block that is still
 *       the crater's (or became empty: mined ore, burnt-out fire) goes back to the original ground.</li>
 *   <li>If someone built there (any solid block that was not part of the crater), the crater is left as it is,
 *       for good. Snow, grass, plants, water and fire do not count as building.</li>
 * </ul>
 */
public final class MeteorCraterHealer extends SavedData {
    private static final String DATA_NAME = "awakening_meteor_craters";
    public static final long HEAL_AFTER_TICKS = 5 * 24000L;
    private static final int RADIUS = 9, BELOW = 6, ABOVE = 9;
    private static final double NO_PLAYER_WITHIN = 48.0D;

    private static final class Crater {
        ResourceKey<Level> dimension;
        BlockPos centre;
        long time;
        final List<BlockState> palette = new ArrayList<>();
        int[] before, after;
    }

    private final List<Crater> craters = new ArrayList<>();

    /** The ground before the impact, taken just before the crater is dug. */
    public record Snapshot(BlockState[] states) {}

    public static Snapshot before(ServerLevel level, BlockPos centre) {
        List<BlockState> out = new ArrayList<>();
        for (BlockPos p : box(centre)) out.add(level.getBlockState(p));
        return new Snapshot(out.toArray(new BlockState[0]));
    }

    /** Called right after the crater is dug: records it next to the ground it replaced. */
    public static void record(ServerLevel level, BlockPos centre, Snapshot before) {
        Crater c = new Crater();
        c.dimension = level.dimension();
        c.centre = centre.immutable();
        c.time = level.getGameTime();
        Map<BlockState, Integer> index = new HashMap<>();
        int n = before.states().length;
        c.before = new int[n];
        c.after = new int[n];
        int i = 0;
        for (BlockPos p : box(centre)) {
            c.before[i] = indexOf(c, index, before.states()[i]);
            c.after[i] = indexOf(c, index, level.getBlockState(p));
            i++;
        }
        MeteorCraterHealer data = get(level.getServer());
        data.craters.add(c);
        data.setDirty();
    }

    private static int indexOf(Crater c, Map<BlockState, Integer> index, BlockState state) {
        return index.computeIfAbsent(state, s -> {
            c.palette.add(s);
            return c.palette.size() - 1;
        });
    }

    private static Iterable<BlockPos> box(BlockPos centre) {
        return BlockPos.betweenClosed(centre.offset(-RADIUS, -BELOW, -RADIUS), centre.offset(RADIUS, ABOVE, RADIUS));
    }

    /** Checked every 10 seconds from the meteor manager. */
    public static void tick(MinecraftServer server) {
        MeteorCraterHealer data = get(server);
        if (data.craters.isEmpty()) return;
        long now = server.overworld().getGameTime();
        Iterator<Crater> it = data.craters.iterator();
        while (it.hasNext()) {
            Crater c = it.next();
            ServerLevel level = server.getLevel(c.dimension);
            if (level == null || now < c.time) {   // a world whose clock went back: forget it
                it.remove();
                data.setDirty();
                continue;
            }
            if (now - c.time < HEAL_AFTER_TICKS) continue;
            Vec3Holder centre = new Vec3Holder(c.centre);
            if (level.getNearestPlayer(centre.x, centre.y, centre.z, NO_PLAYER_WITHIN, false) != null) continue;
            heal(level, c);
            it.remove();
            data.setDirty();
        }
    }

    /**
     * Blocks the world changes by itself (grass spreading onto bared dirt, leaves, sand and gravel sliding in),
     * which must not count as someone building. Block entities (chests, furnaces...) always count.
     */
    private static boolean changesByItself(BlockState state) {
        if (state.hasBlockEntity()) return false;
        return state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.LEAVES)
                || state.is(net.minecraft.tags.BlockTags.SAND) || state.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                || state.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK) || state.is(net.minecraft.world.level.block.Blocks.ICE);
    }

    private record Vec3Holder(double x, double y, double z) {
        Vec3Holder(BlockPos p) {
            this(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D);
        }
    }

    private static void heal(ServerLevel level, Crater c) {
        // first look for building: a solid block that is neither the crater's nor the ground's
        int i = 0;
        for (BlockPos p : box(c.centre)) {
            BlockState now = level.getBlockState(p);
            BlockState crater = c.palette.get(c.after[i]), ground = c.palette.get(c.before[i]);
            i++;
            if (now.equals(crater) || now.equals(ground) || now.isAir() || now.canBeReplaced() || changesByItself(now)) continue;
            AwakeningSystemMod.LOGGER.info("[Meteor] crater at {} kept: someone built there ({} at {})", c.centre.toShortString(), now, p.toShortString());
            return;
        }
        int restored = 0;
        i = 0;
        for (BlockPos p : box(c.centre)) {
            BlockState now = level.getBlockState(p);
            BlockState crater = c.palette.get(c.after[i]), ground = c.palette.get(c.before[i]);
            i++;
            if (now.equals(ground)) continue;
            if (now.equals(crater) || now.isAir() || now.canBeReplaced() || changesByItself(now)) {
                level.setBlock(p.immutable(), ground, Block.UPDATE_CLIENTS);
                restored++;
            }
        }
        AwakeningSystemMod.LOGGER.info("[Meteor] crater at {} healed: {} blocks back to the original ground", c.centre.toShortString(), restored);
    }

    // ---------------------------------------------------------------- saving
    public static MeteorCraterHealer get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(MeteorCraterHealer::new, MeteorCraterHealer::load, null), DATA_NAME);
    }

    private static MeteorCraterHealer load(CompoundTag tag, HolderLookup.Provider registries) {
        MeteorCraterHealer data = new MeteorCraterHealer();
        var blocks = registries.lookupOrThrow(Registries.BLOCK);
        ListTag list = tag.getList("Craters", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Crater c = new Crater();
            c.dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(t.getString("Dimension")));
            c.centre = BlockPos.of(t.getLong("Centre"));
            c.time = t.getLong("Time");
            ListTag pal = t.getList("Palette", Tag.TAG_COMPOUND);
            for (int k = 0; k < pal.size(); k++) c.palette.add(NbtUtils.readBlockState(blocks, pal.getCompound(k)));
            c.before = t.getIntArray("Before");
            c.after = t.getIntArray("After");
            if (c.before.length == c.after.length && c.before.length > 0) data.craters.add(c);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Crater c : craters) {
            CompoundTag t = new CompoundTag();
            t.putString("Dimension", c.dimension.location().toString());
            t.putLong("Centre", c.centre.asLong());
            t.putLong("Time", c.time);
            ListTag pal = new ListTag();
            for (BlockState s : c.palette) pal.add(NbtUtils.writeBlockState(s));
            t.put("Palette", pal);
            t.putIntArray("Before", c.before);
            t.putIntArray("After", c.after);
            list.add(t);
        }
        tag.put("Craters", list);
        return tag;
    }

    /** For testing: makes every recorded crater old enough to heal. */
    public static int ageAll(MinecraftServer server) {
        MeteorCraterHealer data = get(server);
        for (Crater c : data.craters) c.time -= HEAL_AFTER_TICKS;
        data.setDirty();
        return data.craters.size();
    }
}
