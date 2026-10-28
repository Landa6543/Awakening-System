package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, AwakeningSystemMod.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ABYSSAL = ARMOR_MATERIALS.register(
            "abyssal",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 9);
                    }),
                    14,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.ABYSSAL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal"))),
                    2.5F,
                    0.05F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SOUL_ALLOY = ARMOR_MATERIALS.register(
            "soul_alloy",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 4);
                        map.put(ArmorItem.Type.LEGGINGS, 7);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.BODY, 9);
                    }),
                    20,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.SOUL_ALLOY_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "soul_alloy"))),
                    3.5F,
                    0.15F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MITHRIL = ARMOR_MATERIALS.register(
            "mithril",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 5);
                        map.put(ArmorItem.Type.LEGGINGS, 8);
                        map.put(ArmorItem.Type.CHESTPLATE, 11);
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.BODY, 11);
                    }),
                    22,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.MITHRIL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mithril"))),
                    4.5F,
                    0.25F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ADAMANTITE = ARMOR_MATERIALS.register(
            "adamantite",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 5);
                        map.put(ArmorItem.Type.LEGGINGS, 9);
                        map.put(ArmorItem.Type.CHESTPLATE, 12);
                        map.put(ArmorItem.Type.HELMET, 5);
                        map.put(ArmorItem.Type.BODY, 12);
                    }),
                    18,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.ADAMANTITE_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "adamantite"))),
                    5.0F,
                    0.30F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SPIRIT_JADE = ARMOR_MATERIALS.register(
            "spirit_jade",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 6);
                        map.put(ArmorItem.Type.LEGGINGS, 9);
                        map.put(ArmorItem.Type.CHESTPLATE, 12);
                        map.put(ArmorItem.Type.HELMET, 6);
                        map.put(ArmorItem.Type.BODY, 12);
                    }),
                    25,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.SPIRIT_JADE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spirit_jade"))),
                    5.5F,
                    0.35F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ASTRAL = ARMOR_MATERIALS.register(
            "astral",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 7);
                        map.put(ArmorItem.Type.LEGGINGS, 10);
                        map.put(ArmorItem.Type.CHESTPLATE, 14);
                        map.put(ArmorItem.Type.HELMET, 7);
                        map.put(ArmorItem.Type.BODY, 14);
                    }),
                    30,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.ASTRAL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "astral"))),
                    6.0F,
                    0.50F
            )
    );

        public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ASTRALITE = ARMOR_MATERIALS.register(
            "astralite",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 4);
                        map.put(ArmorItem.Type.LEGGINGS, 8);
                        map.put(ArmorItem.Type.CHESTPLATE, 10);
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.BODY, 10);
                    }),
                    26,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.ASTRALITE_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "astralite"))),
                    4.0F,
                    0.20F
            )
    );

public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MORA = ARMOR_MATERIALS.register(
            "mora",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 9);
                    }),
                    25,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.MORA_POUCH.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mora"))),
                    3.5F,
                    0.20F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CELESTIAL = ARMOR_MATERIALS.register(
            "celestial",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 8);
                        map.put(ArmorItem.Type.LEGGINGS, 11);
                        map.put(ArmorItem.Type.CHESTPLATE, 16);
                        map.put(ArmorItem.Type.HELMET, 8);
                        map.put(ArmorItem.Type.BODY, 16);
                    }),
                    32,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.CELESTIAL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial"))),
                    7.0F,
                    0.55F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRIMORDIAL = ARMOR_MATERIALS.register(
            "primordial",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 9);
                        map.put(ArmorItem.Type.LEGGINGS, 13);
                        map.put(ArmorItem.Type.CHESTPLATE, 18);
                        map.put(ArmorItem.Type.HELMET, 9);
                        map.put(ArmorItem.Type.BODY, 18);
                    }),
                    35,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.PRIMORDIAL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "primordial"))),
                    8.5F,
                    0.70F
            )
    );

    private ModArmorMaterials() {}
}
