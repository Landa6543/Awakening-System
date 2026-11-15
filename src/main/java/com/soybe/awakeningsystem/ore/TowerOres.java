package com.soybe.awakeningsystem.ore;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModCreativeTabs;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.valueproviders.UniformInt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Great Tower's ores: each one found only on a band of floors and given a rank, from Ignite (D, floors 10-20)
 * to Lumenite (S, floors 90-100). A metal has its ore, a raw lump, an ingot and a storage block; a crystal has its
 * ore, the gem and a block. Two alloys (Fungarite, Spectrite) are only made, and Ignite ore sometimes gives up
 * Molten Scrap. Textures are the mod's own, coloured after the user's reference pictures.
 * <p>
 * The veins are written into a tower floor's ground while the floor generates ({@link #placeVeins}).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class TowerOres {
    private TowerOres() {}

    /** One ore: its id, whether it is a crystal, its rank (0 = E .. 5 = S), the floors it is found on, its light. */
    public record Kind(String id, boolean gem, int rank, int minFloor, int maxFloor, int light) {}

    public static final List<Kind> KINDS = List.of(
            new Kind("ignite", false, 1, 10, 20, 6),
            new Kind("grave_steel", false, 1, 15, 25, 0),
            new Kind("orichalcum", false, 2, 20, 30, 0),
            new Kind("gelidite", false, 2, 25, 35, 0),
            new Kind("frost_crystal", true, 2, 30, 40, 3),
            new Kind("verdanite", false, 3, 35, 50, 0),
            new Kind("perennite", false, 3, 40, 55, 0),
            new Kind("dracolead", false, 4, 50, 65, 0),
            new Kind("xenium", false, 4, 60, 75, 0),
            new Kind("eridanium", true, 4, 70, 85, 5),
            new Kind("exodite", false, 5, 80, 95, 0),
            new Kind("lumenite", false, 5, 90, 100, 8));

    public static final Map<String, DeferredHolder<Block, Block>> ORE_BLOCKS = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<Block, Block>> STORAGE_BLOCKS = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<Item, Item>> ITEMS = new LinkedHashMap<>();
    private static final List<DeferredHolder<Item, ? extends Item>> TAB = new ArrayList<>();

    private static Rarity rarity(int rank) {
        return rank >= 5 ? Rarity.EPIC : rank >= 3 ? Rarity.RARE : rank >= 2 ? Rarity.UNCOMMON : Rarity.COMMON;
    }

    private static DeferredHolder<Item, Item> item(String id, int rank) {
        DeferredHolder<Item, Item> h = AwakeningSystemMod.ITEMS.register(id, () -> new Item(new Item.Properties().rarity(rarity(rank))));
        ITEMS.put(id, h);
        TAB.add(h);
        return h;
    }

    private static DeferredHolder<Block, Block> block(String id, java.util.function.Supplier<Block> make, int rank) {
        DeferredHolder<Block, Block> b = SystemBlocks.BLOCKS.register(id, make);
        TAB.add(AwakeningSystemMod.ITEMS.register(id, () -> new BlockItem(b.get(), new Item.Properties().rarity(rarity(rank)))));
        return b;
    }

    static {
        for (Kind k : KINDS) {
            float hard = 3.0F + k.rank * 1.2F;
            ORE_BLOCKS.put(k.id, block(k.id + "_ore", () -> new DropExperienceBlock(UniformInt.of(2 + k.rank, 4 + k.rank * 2),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).strength(hard, hard + 3.0F).lightLevel(s -> k.light)), k.rank));
            if (!k.gem) item("raw_" + k.id, k.rank);
            item(k.gem ? k.id : k.id + "_ingot", k.rank);
            STORAGE_BLOCKS.put(k.id, block(k.id + "_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .strength(hard + 2.0F, 8.0F).lightLevel(s -> k.light).mapColor(MapColor.METAL)
                    .sound(k.gem ? SoundType.AMETHYST : SoundType.METAL)), k.rank));
        }
        for (String alloy : new String[]{"fungarite", "spectrite"}) {
            item(alloy + "_ingot", 3);
            STORAGE_BLOCKS.put(alloy, block(alloy + "_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .strength(7.0F, 8.0F).lightLevel(s -> alloy.equals("fungarite") ? 6 : 4)), 3));
        }
        item("molten_scrap", 1);
    }

    /** Loads this class so its blocks and items join the registers before they fire (called from the mod's start). */
    public static void init() {}

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != ModCreativeTabs.MATERIALS_TAB.getKey()) return;
        for (DeferredHolder<Item, ? extends Item> h : TAB) event.accept(h.get());
    }

    // ── veins in the tower's floors ─────────────────────────────────────────

    /** Rock an ore vein may replace: any of the stones the tower's floors are made of. */
    private static boolean host(BlockState s) {
        return s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.BASE_STONE_NETHER) || s.is(BlockTags.TERRACOTTA)
                || s.is(Blocks.END_STONE) || s.is(Blocks.COBBLED_DEEPSLATE) || s.is(Blocks.CALCITE) || s.is(Blocks.SANDSTONE)
                || s.is(Blocks.RED_SANDSTONE) || s.is(Blocks.SMOOTH_RED_SANDSTONE) || s.is(Blocks.MOSSY_COBBLESTONE)
                || s.is(Blocks.PACKED_ICE) || s.is(Blocks.OBSIDIAN);
    }

    private static double hash(long a, long b, long c) {
        long h = a * 341873128712L ^ b * 132897987541L ^ c * 2654435761L;
        h ^= h >>> 33; h *= 0xff51afd7ed558ccdL; h ^= h >>> 33;
        return (h & 0xFFFFFFL) / (double) 0xFFFFFF;
    }

    /**
     * Writes the ore veins of every ore whose floors include this one into a newly generated chunk: a few small
     * blobs per chunk, anywhere in the rock between the bottom and a little under the surface (so the cave floors
     * show them in their walls and roofs).
     */
    public static void placeVeins(int floor, ChunkAccess chunk, int bottom, int top) {
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (Kind k : KINDS) {
            if (floor < k.minFloor || floor > k.maxFloor) continue;
            BlockState ore = ORE_BLOCKS.get(k.id).get().defaultBlockState();
            int veins = 5 - Math.min(3, k.rank / 2);
            for (int v = 0; v < veins; v++) {
                long seed = (long) floor * 31 + k.id.hashCode() * 7L + v;
                int cx = x0 + 2 + (int) (hash(chunk.getPos().x, chunk.getPos().z, seed) * 12);
                int cz = z0 + 2 + (int) (hash(chunk.getPos().z, chunk.getPos().x, seed + 99) * 12);
                int cy = bottom + (int) (hash(chunk.getPos().x + seed, chunk.getPos().z, 7) * Math.max(1, top - bottom));
                int size = 2 + (int) (hash(cx, cz, cy) * 5);
                for (int i = 0; i < size; i++) {
                    int x = cx + (int) ((hash(cx, i, 1) - 0.5) * 3), y = cy + (int) ((hash(cy, i, 2) - 0.5) * 3), z = cz + (int) ((hash(cz, i, 3) - 0.5) * 3);
                    if (x < x0 || x > x0 + 15 || z < z0 || z > z0 + 15 || y <= chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) continue;
                    if (host(chunk.getBlockState(p.set(x, y, z)))) chunk.setBlockState(p, ore, false);
                }
            }
        }
    }
}
