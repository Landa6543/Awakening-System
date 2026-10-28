package com.soybe.awakeningsystem.furnace;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AwakeningSystemMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> ABYSSAL_FURNACE_MENU =
            MENUS.register("abyssal_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.ABYSSAL), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> SOUL_ALLOY_FURNACE_MENU =
            MENUS.register("soul_alloy_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.SOUL_ALLOY), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> ASTRALITE_FURNACE_MENU =
            MENUS.register("astralite_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.ASTRALITE), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> MITHRIL_FURNACE_MENU =
            MENUS.register("mithril_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.MITHRIL), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> ADAMANTITE_FURNACE_MENU =
            MENUS.register("adamantite_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.ADAMANTITE), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> SPIRIT_JADE_FURNACE_MENU =
            MENUS.register("spirit_jade_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.SPIRIT_JADE), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> ASTRAL_FURNACE_MENU =
            MENUS.register("astral_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.ASTRAL), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> CELESTIAL_FURNACE_MENU =
            MENUS.register("celestial_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.CELESTIAL), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CustomTierFurnaceMenu>> PRIMORDIAL_FURNACE_MENU =
            MENUS.register("primordial_furnace_menu", () -> new MenuType<>((id, inv) ->
                    new CustomTierFurnaceMenu(id, inv, new net.minecraft.world.SimpleContainer(6), new net.minecraft.world.inventory.SimpleContainerData(6), FurnaceTier.PRIMORDIAL), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<com.soybe.awakeningsystem.subspace.SubspaceMenu>> SUBSPACE_MENU =
            MENUS.register("subspace_menu", () -> new MenuType<>(com.soybe.awakeningsystem.subspace.SubspaceMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<com.soybe.awakeningsystem.item.pouch.PouchMenu>> POUCH_MENU =
            MENUS.register("pouch_menu", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.soybe.awakeningsystem.item.pouch.PouchMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<com.soybe.awakeningsystem.block.entity.FishingTrapMenu>> FISHING_TRAP_MENU =
            MENUS.register("fishing_trap_menu", () -> new MenuType<>(com.soybe.awakeningsystem.block.entity.FishingTrapMenu::new, FeatureFlags.VANILLA_SET));
}

