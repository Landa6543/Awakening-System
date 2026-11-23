package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * The attack damage of the mod's special weapons, set in one place so it follows the worlds' order
 * (Overworld → Ranker Tower → Isekai → Murim → Teyvat), like the 2000-level progression. The few unique
 * weapons sit above Teyvat, and the Creator Claymore stays the strongest weapon of the game.
 *
 * <p>The numbers are the damage shown on the tooltip (the player's own 1 point included).
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class WeaponDamageScale {
    private WeaponDamageScale() {}

    private static final Map<String, Integer> DAMAGE = new HashMap<>();

    private static void set(int damage, String... ids) {
        for (String id : ids) DAMAGE.put(id, damage);
    }

    static {
        // Overworld: the Solo Leveling gates and the everyday weapons (10-18)
        set(10, "xmas_tree_sword", "knight_killer");
        set(11, "kasakas_venom_fang");
        set(12, "barukas_dagger");
        set(13, "soul_solid");
        set(14, "truth_kasakas_venom_fang", "phantomblade", "radiant_light_sword", "unbreakable_faith");
        set(15, "blue_monsoon", "crimson_shadow", "dawns_ballad", "moonshadow");
        set(16, "demon_kings_daggers", "shadow_daggers");
        set(18, "ennios_roar");
        // Ranker Tower: the tower's legendary blades (21-26)
        set(21, "lambent_light");
        set(23, "dark_repulser");
        set(24, "elucidator");
        set(26, "transcendent_thorn");
        // Isekai (30-38)
        set(30, "adamantite_war_hammer");
        set(33, "gigantic_greatsword");
        set(35, "asrahan_holy_sword");
        set(38, "black_dragon_scythe");
        // Murim (42-52)
        set(42, "jade_feijian");
        set(44, "flying_sword");
        set(46, "astral_guandao");
        set(48, "sky_demon_sword");
        set(50, "heaven_shaking_sword");
        set(52, "plum_blossom_sword");
        // Teyvat: the Genshin (and gacha) weapons go by their stars, see teyvatDamage (55-68)
        set(66, "primogem_sword");
        set(68, "staff_of_homa");
        // the unique weapons, above every world (72-80)
        set(72, "dread_scythe");
        set(75, "void_claymore");
        set(80, "la_doncella");
        // and the Creator's, always the strongest
        set(100, "creator_claymore");
    }

    /** Teyvat's weapons by their stars, the stronger ones of each rarity a little higher. */
    private static int teyvatDamage(GenshinWeaponItem.WeaponType type) {
        if (type.stars >= 5) return 64 + Math.max(0, Math.min(4, type.attackDamage - 12));
        if (type.stars == 4) return 58 + Math.max(0, Math.min(3, type.attackDamage - 9));
        return 55;
    }

    /** The damage the scale gives this item, or -1 when the item is not one of the scaled weapons. */
    public static int damageFor(Item item) {
        if (item instanceof GenshinWeaponItem genshin) return teyvatDamage(genshin.getWeaponType());
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (!AwakeningSystemMod.MOD_ID.equals(id.getNamespace())) return -1;
        return DAMAGE.getOrDefault(id.getPath(), -1);
    }

    @SubscribeEvent
    public static void onModifiers(ItemAttributeModifierEvent event) {
        int damage = damageFor(event.getItemStack().getItem());
        if (damage <= 0) return;
        event.replaceModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage - 1.0D, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
}
