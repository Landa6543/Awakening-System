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

    // --- MATERIALES DE ARMADURA POST-NETHERITE ---
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ABYSSAL = ARMOR_MATERIALS.register(
            "abyssal",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 4);
                        map.put(ArmorItem.Type.LEGGINGS, 7);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.BODY, 8);
                    }),
                    19,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.ABYSSAL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal"))),
                    3.2F,
                    0.12F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MITHRIL = ARMOR_MATERIALS.register(
            "mithril",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 4);
                        map.put(ArmorItem.Type.LEGGINGS, 7);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.BODY, 9);
                    }),
                    22,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.MITHRIL_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mithril"))),
                    3.5F,
                    0.15F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ADAMANTITE = ARMOR_MATERIALS.register(
            "adamantite",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 5);
                        map.put(ArmorItem.Type.LEGGINGS, 8);
                        map.put(ArmorItem.Type.CHESTPLATE, 10);
                        map.put(ArmorItem.Type.HELMET, 5);
                        map.put(ArmorItem.Type.BODY, 10);
                    }),
                    18,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(com.soybe.awakeningsystem.SystemItems.ADAMANTITE_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "adamantite"))),
                    4.0F,
                    0.25F
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
                    5.0F,
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

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MORA = ARMOR_MATERIALS.register(
            "mora",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 4);
                        map.put(ArmorItem.Type.LEGGINGS, 7);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.HELMET, 4);
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

    private ModArmorMaterials() {}
}
