package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Every creature of the mod leaves something: none of them had a loot table, so many (the tiger, the wolves,
 * the orcs, the beholder…) often dropped nothing at all. When one killed by a player leaves no drops, it leaves
 * what fits it: hide and meat from beasts, nuggets from orcs and goblins, eyes and pearls from the eyes, iron and
 * redstone from the ruin machines, and so on.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class BasicCreatureDrops {
    private BasicCreatureDrops() {}

    /** One possible drop: an item, how many (min to max), and how likely (0 to 1). */
    private record Drop(Item item, int min, int max, float chance) {}

    private static final Map<String, Drop[]> DROPS = new HashMap<>();

    private static void put(String id, Drop... drops) {
        DROPS.put(id, drops);
    }

    private static Drop d(Item item, int min, int max) {
        return new Drop(item, min, max, 1F);
    }

    private static Drop d(Item item, int min, int max, float chance) {
        return new Drop(item, min, max, chance);
    }

    static {
        // beasts and animals
        put("white_thunder_tiger", d(Items.LEATHER, 1, 3), d(Items.BONE, 1, 2), d(Items.BEEF, 1, 2, 0.6F));
        put("shadow_direwolf", d(Items.BONE, 1, 3), d(Items.LEATHER, 0, 2));
        put("spirit_fox", d(Items.RABBIT_HIDE, 1, 2), d(Items.SWEET_BERRIES, 1, 3, 0.5F));
        put("great_snowboar_king", d(Items.PORKCHOP, 3, 6), d(Items.LEATHER, 2, 4));
        put("cloud_crane", d(Items.FEATHER, 2, 4), d(Items.CHICKEN, 1, 2));
        put("mana_cow", d(Items.BEEF, 1, 3), d(Items.LEATHER, 0, 2));
        put("mana_pig", d(Items.PORKCHOP, 1, 3));
        put("mana_sheep", d(Items.MUTTON, 1, 2), d(Items.WHITE_WOOL, 1, 1));
        put("mana_chicken", d(Items.CHICKEN, 1, 1), d(Items.FEATHER, 0, 2));
        put("jade_armored_beast", d(Items.LEATHER, 2, 4), d(Items.EMERALD, 1, 2, 0.5F));
        put("ant_soldier", d(Items.STRING, 1, 2), d(Items.SPIDER_EYE, 0, 1));
        put("mother_spider", d(Items.STRING, 2, 5), d(Items.SPIDER_EYE, 1, 2));
        put("mother_spider_boss", d(Items.STRING, 6, 12), d(Items.SPIDER_EYE, 2, 4), d(Items.FERMENTED_SPIDER_EYE, 1, 2));
        put("spider_head", d(Items.STRING, 1, 3), d(Items.SPIDER_EYE, 1, 1));
        put("isekai_slime", d(Items.SLIME_BALL, 1, 3));
        put("genshin_slime", d(Items.SLIME_BALL, 1, 2));
        // humanoid brutes
        put("abyssal_orc", d(Items.LEATHER, 1, 2), d(Items.IRON_NUGGET, 1, 4), d(Items.BONE, 0, 1));
        put("goblin_scout", d(Items.GOLD_NUGGET, 1, 3), d(Items.STRING, 0, 1));
        put("goblin_warrior", d(Items.GOLD_NUGGET, 1, 4), d(Items.IRON_NUGGET, 0, 2));
        put("goblin_shaman", d(Items.GOLD_NUGGET, 1, 3), d(Items.GLOWSTONE_DUST, 1, 2));
        put("beastman_warrior", d(Items.LEATHER, 1, 3), d(Items.BONE, 1, 2));
        put("hilichurl", d(Items.BONE, 1, 1), d(Items.STICK, 1, 2));
        put("hilichurl_berserker", d(Items.BONE, 1, 2), d(Items.STICK, 1, 2));
        put("hilichurl_shooter", d(Items.BONE, 1, 1), d(Items.ARROW, 1, 3));
        put("mitachurl", d(Items.BONE, 2, 3), d(Items.LEATHER, 1, 2), d(Items.OAK_PLANKS, 1, 3, 0.5F));
        put("frostarm_lawachurl", d(Items.BONE, 2, 4), d(Items.PACKED_ICE, 1, 2));
        put("stonehide_lawachurl", d(Items.BONE, 2, 4), d(Items.COBBLESTONE, 2, 4));
        put("thunderhelm_lawachurl", d(Items.BONE, 2, 4), d(Items.COPPER_INGOT, 1, 3));
        put("jiangshi", d(Items.ROTTEN_FLESH, 1, 3), d(Items.PAPER, 1, 1));
        put("demonic_cultivator", d(Items.PAPER, 1, 3), d(Items.REDSTONE, 1, 2));
        put("blood_sect_disciple", d(Items.PAPER, 1, 2), d(Items.IRON_NUGGET, 1, 3));
        put("mount_hua_disciple", d(Items.PAPER, 1, 2), d(Items.IRON_NUGGET, 1, 3));
        put("frost_elf_assassin", d(Items.ARROW, 2, 4), d(Items.PACKED_ICE, 0, 1));
        put("shadow_infantry", d(Items.BONE, 1, 2), d(Items.IRON_NUGGET, 1, 3));
        // magic and spirits
        put("abyss_mage_cryo", d(Items.GLOWSTONE_DUST, 1, 3), d(Items.SNOWBALL, 1, 3));
        put("abyss_mage_electro", d(Items.GLOWSTONE_DUST, 1, 3), d(Items.AMETHYST_SHARD, 1, 2));
        put("abyss_mage_hydro", d(Items.GLOWSTONE_DUST, 1, 3), d(Items.PRISMARINE_CRYSTALS, 1, 2));
        put("abyss_mage_pyro", d(Items.GLOWSTONE_DUST, 1, 3), d(Items.BLAZE_POWDER, 1, 2));
        put("abyssal_beholder", d(Items.SPIDER_EYE, 1, 3), d(Items.ENDER_PEARL, 0, 1), d(Items.GHAST_TEAR, 1, 1, 0.25F));
        put("glare", d(Items.GLOW_INK_SAC, 1, 2));
        put("abyssal_necromancer", d(Items.BONE, 2, 4), d(Items.PHANTOM_MEMBRANE, 0, 1));
        put("specter", d(Items.PHANTOM_MEMBRANE, 1, 2));
        put("tall_stalker", d(Items.ENDER_PEARL, 0, 2), d(Items.BONE, 1, 1));
        put("stilt_walker", d(Items.STICK, 2, 4), d(Items.STRING, 1, 2));
        put("water_mimic", d(Items.PRISMARINE_SHARD, 1, 3));
        put("wildfire", d(Items.BLAZE_POWDER, 1, 3), d(Items.BLAZE_ROD, 0, 1));
        put("mana_pixie", d(Items.GLOWSTONE_DUST, 1, 2));
        put("screaming_mandrake", d(Items.POISONOUS_POTATO, 1, 2), d(Items.WHEAT_SEEDS, 1, 3));
        put("whopperflower", d(Items.BONE_MEAL, 1, 3), d(Items.RED_TULIP, 0, 1));
        put("elemental_lord", d(Items.AMETHYST_SHARD, 2, 4), d(Items.GLOWSTONE_DUST, 2, 4));
        // ruin machines and stone creatures
        put("ruin_guard", d(Items.IRON_INGOT, 1, 3), d(Items.REDSTONE, 2, 4));
        put("ruin_hunter", d(Items.IRON_INGOT, 1, 2), d(Items.REDSTONE, 1, 3));
        put("ruin_grader", d(Items.IRON_INGOT, 2, 4), d(Items.REDSTONE, 2, 5));
        put("ruin_sentinel", d(Items.IRON_INGOT, 1, 3), d(Items.REDSTONE, 2, 4));
        put("ruin_drake", d(Items.IRON_INGOT, 2, 4), d(Items.REDSTONE, 2, 4));
        put("ruin_serpent", d(Items.IRON_INGOT, 3, 6), d(Items.REDSTONE, 3, 6));
        put("sculk_golem", d(Items.SCULK, 2, 4), d(Items.ECHO_SHARD, 1, 1, 0.2F));
        put("colossal_sculk_warden", d(Items.SCULK, 6, 12), d(Items.ECHO_SHARD, 1, 3));
        put("geovishap", d(Items.AMETHYST_SHARD, 1, 3), d(Items.COBBLESTONE, 1, 3));
        put("bathysmal_vishap", d(Items.PRISMARINE_SHARD, 1, 3), d(Items.PRISMARINE_CRYSTALS, 1, 2));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide || !event.isRecentlyHit() || !event.getDrops().isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (!AwakeningSystemMod.MOD_ID.equals(id.getNamespace())) return;
        // shadows of the player and tamed or owned creatures leave nothing
        if (entity.getTags().contains("shadow_soldier")) return;
        if (entity instanceof net.minecraft.world.entity.OwnableEntity own && own.getOwnerUUID() != null) return;
        Drop[] drops = DROPS.get(id.getPath());
        if (drops == null) return;
        var rnd = entity.getRandom();
        for (Drop drop : drops) {
            if (rnd.nextFloat() >= drop.chance()) continue;
            int n = drop.min() + (drop.max() > drop.min() ? rnd.nextInt(drop.max() - drop.min() + 1) : 0);
            if (n <= 0) continue;
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.3, entity.getZ(), new ItemStack(drop.item(), n)));
        }
        // at least one thing, always
        if (event.getDrops().isEmpty()) {
            Drop first = drops[0];
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.3, entity.getZ(), new ItemStack(first.item(), Math.max(1, first.min()))));
        }
    }
}
