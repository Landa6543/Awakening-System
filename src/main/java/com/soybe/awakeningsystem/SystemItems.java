package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.item.AsrahanHolySwordItem;
import com.soybe.awakeningsystem.item.BlackDragonScytheItem;
import com.soybe.awakeningsystem.item.DawnsBalladItem;
import com.soybe.awakeningsystem.item.DeathKingClockItem;
import com.soybe.awakeningsystem.item.DreadScytheItem;
import com.soybe.awakeningsystem.item.ElucidatorItem;
import com.soybe.awakeningsystem.item.DarkRepulserItem;
import com.soybe.awakeningsystem.item.LambentLightItem;
import com.soybe.awakeningsystem.item.FlyingSwordItem;
import com.soybe.awakeningsystem.item.GiganticGreatswordItem;

import com.soybe.awakeningsystem.item.HeavenShakingSwordItem;
import com.soybe.awakeningsystem.item.MarrowCleansingPillItem;
import com.soybe.awakeningsystem.item.RisingDragonPillItem;
import com.soybe.awakeningsystem.item.RealmBreakthroughPillItem;
import com.soybe.awakeningsystem.item.BlackTortoisePillItem;
import com.soybe.awakeningsystem.item.ManhwaArmorItem;
import com.soybe.awakeningsystem.item.ManhwaArtifactItem;
import com.soybe.awakeningsystem.item.PlumBlossomSwordItem;
import com.soybe.awakeningsystem.item.RadiantLightSwordItem;
import com.soybe.awakeningsystem.item.RankerReturnScrollItem;
import com.soybe.awakeningsystem.item.CreatorClaymoreItem;
import com.soybe.awakeningsystem.item.LaDoncellaSwordItem;
import com.soybe.awakeningsystem.item.ManaFoodItem;
import com.soybe.awakeningsystem.item.ManaMilkBucketItem;
import com.soybe.awakeningsystem.item.RuneStoneItem;
import com.soybe.awakeningsystem.item.ShadowDaggersItem;
import com.soybe.awakeningsystem.item.SkyDemonSwordItem;
import com.soybe.awakeningsystem.item.TranscendentThornItem;
import com.soybe.awakeningsystem.item.UnbreakableFaithItem;
import com.soybe.awakeningsystem.item.SoulSolidSwordItem;
import com.soybe.awakeningsystem.item.NineRevolutionsPillItem;
import com.soybe.awakeningsystem.item.CrimsonPhoenixPillItem;
import com.soybe.awakeningsystem.item.WhiteTigerPillItem;
import com.soybe.awakeningsystem.item.HeavenlyEnlightenmentPillItem;
import com.soybe.awakeningsystem.item.FoundationBuildingPillItem;
import com.soybe.awakeningsystem.item.PillOfPrimordialRebirthItem;
import com.soybe.awakeningsystem.item.WildfireCrownItem;
import com.soybe.awakeningsystem.item.WornLoreBookItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.soybe.awakeningsystem.entity.WyvernEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import com.soybe.awakeningsystem.item.CustomToolTiers;
import com.soybe.awakeningsystem.item.ModArmorMaterials;
import com.soybe.awakeningsystem.furnace.FurnaceUpgradeItem;
import com.soybe.awakeningsystem.furnace.FurnaceTier;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public final class SystemItems {
    public static final DeferredHolder<Item, Item> AWAKENING_COIN = AwakeningSystemMod.ITEMS.register(
            "awakening_coin", () -> new com.soybe.awakeningsystem.item.AwakeningCoinItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DUNGEON_KEY = AwakeningSystemMod.ITEMS.register(
            "dungeon_key", () -> new Item(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> HUNTER_GUIDE = AwakeningSystemMod.ITEMS.register(
            "hunter_guide", () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> HUNTER_BESTIARY = AwakeningSystemMod.ITEMS.register(
            "hunter_bestiary", () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> WORN_LORE_BOOK = AwakeningSystemMod.ITEMS.register(
            "worn_lore_book", () -> new WornLoreBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> RANKER_TOWER_TICKET = AwakeningSystemMod.ITEMS.register(
            "ranker_tower_ticket", () -> new Item(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> RANKER_RETURN_SCROLL = AwakeningSystemMod.ITEMS.register(
            "ranker_return_scroll", () -> new RankerReturnScrollItem(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> ROULETTE_TOKEN = AwakeningSystemMod.ITEMS.register(
            "roulette_token", () -> new com.soybe.awakeningsystem.item.RouletteTokenItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ROULETTE_ALTAR = AwakeningSystemMod.ITEMS.register(
            "roulette_altar", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.ROULETTE_ALTAR.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ANT_SOLDIER_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ant_soldier_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANT_SOLDIER, 0x141020, 0x9040C0, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSSAL_BEHOLDER_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyssal_beholder_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_BEHOLDER, 0x120A1E, 0xE02875, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MANA_PIXIE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mana_pixie_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MANA_PIXIE, 0x50E0D0, 0xF080FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> SCREAMING_MANDRAKE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "screaming_mandrake_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.SCREAMING_MANDRAKE, 0x8B5A2B, 0x40A030, new Item.Properties()));

    public static final DeferredHolder<Item, Item> CELESTIAL_DRAGON_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "celestial_dragon_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.CELESTIAL_DRAGON, 0x1A5E4F, 0xD4AF37, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_CELESTIAL_DRAGON_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_celestial_dragon_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_CELESTIAL_DRAGON, 0x0A1F1B, 0xFFD700, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_spawn_egg", () -> new com.soybe.awakeningsystem.item.AncientDragonSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON, 0x8B0000, 0xFFD700,
                    com.soybe.awakeningsystem.entity.AncientDragonEntity.VARIANT_FIRE, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_ICE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_ice_spawn_egg", () -> new com.soybe.awakeningsystem.item.AncientDragonSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON, 0x143156, 0x00E5FF,
                    com.soybe.awakeningsystem.entity.AncientDragonEntity.VARIANT_ICE, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_FOREST_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_forest_spawn_egg", () -> new com.soybe.awakeningsystem.item.AncientDragonSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON, 0x185226, 0xD4AF37,
                    com.soybe.awakeningsystem.entity.AncientDragonEntity.VARIANT_FOREST, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_SHADOW_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_shadow_spawn_egg", () -> new com.soybe.awakeningsystem.item.AncientDragonSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON, 0x1A1226, 0xB833FF,
                    com.soybe.awakeningsystem.entity.AncientDragonEntity.VARIANT_SHADOW, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_LIGHTNING_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_lightning_spawn_egg", () -> new com.soybe.awakeningsystem.item.AncientDragonSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON, 0x1A2B4C, 0xFFE042,
                    com.soybe.awakeningsystem.entity.AncientDragonEntity.VARIANT_LIGHTNING, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_CELESTIAL_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_celestial_spawn_egg", () -> new com.soybe.awakeningsystem.item.AncientDragonSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANCIENT_DRAGON, 0xF5F2EB, 0xE5B834,
                    com.soybe.awakeningsystem.entity.AncientDragonEntity.VARIANT_CELESTIAL, new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGON_NEST = AwakeningSystemMod.ITEMS.register(
            "dragon_nest", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.DRAGON_NEST.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_EGG_FIRE = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_egg_fire", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ANCIENT_DRAGON_EGG_FIRE.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).fireResistant()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_EGG_ICE = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_egg_ice", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ANCIENT_DRAGON_EGG_ICE.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_EGG_FOREST = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_egg_forest", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ANCIENT_DRAGON_EGG_FOREST.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_EGG_SHADOW = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_egg_shadow", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ANCIENT_DRAGON_EGG_SHADOW.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_EGG_LIGHTNING = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_egg_lightning", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ANCIENT_DRAGON_EGG_LIGHTNING.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ANCIENT_DRAGON_EGG_CELESTIAL = AwakeningSystemMod.ITEMS.register(
            "ancient_dragon_egg_celestial", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ANCIENT_DRAGON_EGG_CELESTIAL.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()));

    public static final DeferredHolder<Item, Item> CELESTIAL_DRAGON_PEARL = AwakeningSystemMod.ITEMS.register(
            "celestial_dragon_pearl", () -> new com.soybe.awakeningsystem.item.CelestialDragonPearlItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IMMORTAL_DRAGON_SCALE = AwakeningSystemMod.ITEMS.register(
            "immortal_dragon_scale", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.immortal_dragon_scale.desc"));
                    tooltip.add(Component.translatable("item.awakening_system.immortal_dragon_scale.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> CELESTIAL_DRAGON_TRIBUTE = AwakeningSystemMod.ITEMS.register(
            "celestial_dragon_tribute", () -> new com.soybe.awakeningsystem.item.CelestialDragonTributeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> CELESTIAL_DRAGON_ALTAR = AwakeningSystemMod.ITEMS.register(
            "celestial_dragon_altar", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.CELESTIAL_DRAGON_ALTAR.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.celestial_dragon_altar.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.celestial_dragon_altar.use"));
                }
            });

    public static final DeferredHolder<Item, Item> GODDESS_ALTAR = AwakeningSystemMod.ITEMS.register(
            "goddess_altar", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.goddess_altar.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.goddess_altar.use"));
                }
            });

    public static final DeferredHolder<Item, Item> BOUNTY_BOARD = AwakeningSystemMod.ITEMS.register(
            "bounty_board", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.BOUNTY_BOARD.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.bounty_board.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.bounty_board.use"));
                }
            });

    public static final DeferredHolder<Item, Item> ADVENTURER_MEDAL = AwakeningSystemMod.ITEMS.register(
            "adventurer_medal", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.adventurer_medal.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> TAVERN_CHAIR = AwakeningSystemMod.ITEMS.register(
            "tavern_chair", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_CHAIR.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.tavern_chair.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> CAMPFIRE_LOG_SEAT = AwakeningSystemMod.ITEMS.register(
            "campfire_log_seat", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.CAMPFIRE_LOG_SEAT.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.campfire_log_seat.desc"));
                }
            });


    public static final DeferredHolder<Item, Item> TAVERN_TABLE = AwakeningSystemMod.ITEMS.register(
            "tavern_table", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_TABLE.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.tavern_table.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_RAIL_TRACK = AwakeningSystemMod.ITEMS.register(
            "subway_rail_track", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_RAIL_TRACK.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.subway_rail_track.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_WIDE_RAIL_TRACK = AwakeningSystemMod.ITEMS.register(
            "subway_wide_rail_track", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.subway_wide_rail_track.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_DIGITAL_SCREEN = AwakeningSystemMod.ITEMS.register(
            "subway_digital_screen", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_DIGITAL_SCREEN.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.subway_digital_screen.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_GRAB_HANDLE = AwakeningSystemMod.ITEMS.register(
            "subway_grab_handle", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_GRAB_HANDLE.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.subway_grab_handle.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_TRAIN_WHEEL = AwakeningSystemMod.ITEMS.register(
            "subway_train_wheel", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_TRAIN_WHEEL.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.subway_train_wheel.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_PASSENGER_SEAT = AwakeningSystemMod.ITEMS.register(
            "subway_passenger_seat", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_PASSENGER_SEAT.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§7Asiento ergonómico de acero inoxidable de los vagones del Metro de Seúl."));
                    tooltip.add(Component.literal("§e✦ Clic derecho para sentarse y cultivar Qi (+4/s)."));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_TRAIN_CONTROLLER = AwakeningSystemMod.ITEMS.register(
            "subway_train_controller", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_TRAIN_CONTROLLER.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§7Consola de mando y conducción del tren del Metro de Seúl."));
                    tooltip.add(Component.literal("§6✦ Clic derecho sobre vías para ensamblar y conducir el vagón (W / S)."));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_STAINLESS_STEEL = AwakeningSystemMod.ITEMS.register(
            "subway_stainless_steel", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_STAINLESS_STEEL.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§7Placas de acero inoxidable de grado ferroviario."));
                    tooltip.add(Component.literal("§e✦ Bloque de construcción para chasis, piso y carrocería de vagones de metro."));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_DOOR = AwakeningSystemMod.ITEMS.register(
            "subway_door", () -> new net.minecraft.world.item.DoubleHighBlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_DOOR.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§7Puerta corrediza doble de metro con ventanal reforzado."));
                    tooltip.add(Component.literal("§a✦ Apertura doble sincronizada para abordaje desde andenes y estaciones."));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_PLATFORM = AwakeningSystemMod.ITEMS.register(
            "subway_platform", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_PLATFORM.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§7Baldosas de terrazo pulido para andenes de estaciones de metro (ORV)."));
                    tooltip.add(Component.literal("§e✦ Permite construir andenes de abordaje a nivel del vagón."));
                }
            });

    public static final DeferredHolder<Item, Item> SUBWAY_PLATFORM_EDGE = AwakeningSystemMod.ITEMS.register(
            "subway_platform_edge", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.SUBWAY_PLATFORM_EDGE.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§7Borde de andén con franja podotáctil y línea amarilla de seguridad."));
                    tooltip.add(Component.literal("§6✦ Orienta mirando hacia las vías férreas para delimitar el andén de espera."));
                }
            });

    public static final DeferredHolder<Item, Item> ARCHITECT_DRAFTING_TABLE = AwakeningSystemMod.ITEMS.register(
            "architect_drafting_table", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ARCHITECT_DRAFTING_TABLE.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.architect_drafting_table.desc"));
                }
            });

    public static final DeferredHolder<Item, Item> DIMENSIONAL_RIFT_STAND = AwakeningSystemMod.ITEMS.register(
            "dimensional_rift_stand", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.DIMENSIONAL_RIFT_STAND.get(),
                    new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.dimensional_rift_stand.desc"));
                }
            });


    public static final DeferredHolder<Item, Item> BOTTLED_MANA_PIXIE = AwakeningSystemMod.ITEMS.register(
            "bottled_mana_pixie", () -> new com.soybe.awakeningsystem.item.BottledManaPixieItem(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> MANDRAKE_ROOT = AwakeningSystemMod.ITEMS.register(
            "mandrake_root", () -> new com.soybe.awakeningsystem.item.MandrakeRootItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> YGGDRASIL_HEART_SEED = AwakeningSystemMod.ITEMS.register(
            "yggdrasil_heart_seed", () -> new com.soybe.awakeningsystem.item.YggdrasilHeartSeedItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> YGGDRASIL_TREANT_KEY = AwakeningSystemMod.ITEMS.register(
            "yggdrasil_treant_key", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.yggdrasil_treant_key.desc"));
                    tooltip.add(Component.translatable("item.awakening_system.yggdrasil_treant_key.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_TREASURE_CHEST = AwakeningSystemMod.ITEMS.register(
            "locked_treasure_chest", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_treasure_chest.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_treasure_chest.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_CHEST_OVERWORLD = AwakeningSystemMod.ITEMS.register(
            "locked_chest_overworld", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_CHEST_OVERWORLD.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_overworld.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_overworld.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_CHEST_NETHER = AwakeningSystemMod.ITEMS.register(
            "locked_chest_nether", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_CHEST_NETHER.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_nether.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_nether.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_CHEST_END = AwakeningSystemMod.ITEMS.register(
            "locked_chest_end", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_CHEST_END.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_end.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_end.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_CHEST_TEYVAT = AwakeningSystemMod.ITEMS.register(
            "locked_chest_teyvat", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_CHEST_TEYVAT.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_teyvat.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_teyvat.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_CHEST_ISEKAI = AwakeningSystemMod.ITEMS.register(
            "locked_chest_isekai", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_CHEST_ISEKAI.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_isekai.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_isekai.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> LOCKED_CHEST_MURIM = AwakeningSystemMod.ITEMS.register(
            "locked_chest_murim", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_CHEST_MURIM.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_murim.desc"));
                    tooltip.add(Component.translatable("block.awakening_system.locked_chest_murim.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> JADE_ANCESTRAL_KEY = AwakeningSystemMod.ITEMS.register(
            "jade_ancestral_key", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.jade_ancestral_key.desc"));
                    tooltip.add(Component.translatable("item.awakening_system.jade_ancestral_key.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> TEYVAT_SHRINE_KEY = AwakeningSystemMod.ITEMS.register(
            "teyvat_shrine_key", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.teyvat_shrine_key.desc"));
                    tooltip.add(Component.translatable("item.awakening_system.teyvat_shrine_key.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> WU_XING_MANUAL = AwakeningSystemMod.ITEMS.register(
            "wu_xing_manual", () -> new com.soybe.awakeningsystem.item.WuXingManualItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> SOUL_TRANSFORMATION_PILL = AwakeningSystemMod.ITEMS.register(
            "soul_transformation_pill", () -> new com.soybe.awakeningsystem.item.SoulTransformationPillItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MARROW_CLEANSING_PILL = AwakeningSystemMod.ITEMS.register(
            "marrow_cleansing_pill", () -> new MarrowCleansingPillItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> RISING_DRAGON_PILL = AwakeningSystemMod.ITEMS.register(
            "rising_dragon_pill", () -> new RisingDragonPillItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> REALM_BREAKTHROUGH_PILL = AwakeningSystemMod.ITEMS.register(
            "realm_breakthrough_pill", () -> new RealmBreakthroughPillItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> TRIBULATION_SHIELD_PILL = AwakeningSystemMod.ITEMS.register(
            "tribulation_shield_pill", () -> new com.soybe.awakeningsystem.item.TribulationShieldPillItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> BLACK_TORTOISE_PILL = AwakeningSystemMod.ITEMS.register(
            "black_tortoise_pill", () -> new BlackTortoisePillItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MURIM_ALCHEMY_CAULDRON = AwakeningSystemMod.ITEMS.register(
            "murim_alchemy_cauldron", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.MURIM_ALCHEMY_CAULDRON.get(),
                    new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> FIVE_ELEMENTS_PEDESTAL = AwakeningSystemMod.ITEMS.register(
            "five_elements_pedestal", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> JADE_GUARDIAN_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "jade_guardian_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.JADE_GUARDIAN_BOSS, 0x2E8B57, 0xFFD700, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELEMENTAL_DEMON_CROWN = AwakeningSystemMod.ITEMS.register(
            "elemental_demon_crown", () -> new com.soybe.awakeningsystem.item.ElementalDemonCrownItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> CHAOS_MANA_CORE = AwakeningSystemMod.ITEMS.register(
            "chaos_mana_core", () -> new com.soybe.awakeningsystem.item.ChaosManaCoreItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELEMENTAL_CASTLE_KEY = AwakeningSystemMod.ITEMS.register(
            "elemental_castle_key", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.elemental_castle_key.desc"));
                    tooltip.add(Component.translatable("item.awakening_system.elemental_castle_key.lore"));
                }
            });

    public static final DeferredHolder<Item, Item> ELEMENTAL_BARRIER = AwakeningSystemMod.ITEMS.register(
            "elemental_barrier", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ELEMENTAL_BARRIER.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ELEMENTAL_ALTAR = AwakeningSystemMod.ITEMS.register(
            "elemental_altar", () -> new net.minecraft.world.item.BlockItem(
                    com.soybe.awakeningsystem.block.SystemBlocks.ELEMENTAL_ALTAR.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ELEMENTAL_DEMON_KING_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "elemental_demon_king_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_DEMON_KING, 0x4B0082, 0x00FFFF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELEMENTAL_LORD_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "elemental_lord_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_LORD, 0x00FFFF, 0xFFA500, new Item.Properties()));

    // Huevos de Spawn de Nuevos Mobs
    public static final DeferredHolder<Item, Item> GOBLIN_SCOUT_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "goblin_scout_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_SCOUT, 0x3C6B2F, 0x8A6239, new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOBLIN_WARRIOR_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "goblin_warrior_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_WARRIOR, 0x2E5324, 0x71797E, new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOBLIN_SHAMAN_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "goblin_shaman_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GOBLIN_SHAMAN, 0x4A6B35, 0x9932CC, new Item.Properties()));

    public static final DeferredHolder<Item, Item> BEASTMAN_WARRIOR_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "beastman_warrior_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.BEASTMAN_WARRIOR, 0x54493F, 0xB22222, new Item.Properties()));

    public static final DeferredHolder<Item, Item> JIANGSHI_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "jiangshi_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.JIANGSHI, 0x1B263B, 0xFFD700, new Item.Properties()));

    public static final DeferredHolder<Item, Item> DEMONIC_CULTIVATOR_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "demonic_cultivator_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.DEMONIC_CULTIVATOR, 0x111111, 0x8B0000, new Item.Properties()));

    public static final DeferredHolder<Item, Item> JADE_ARMORED_BEAST_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "jade_armored_beast_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.JADE_ARMORED_BEAST, 0x00A86B, 0x2E8B57, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MOUNT_HUA_DISCIPLE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mount_hua_disciple_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MOUNT_HUA_DISCIPLE, 0xEDE8F5, 0xFF69B4, new Item.Properties()));

    // Materiales de Botín de Nuevos Mobs
    public static final DeferredHolder<Item, Item> GOBLIN_EAR = AwakeningSystemMod.ITEMS.register(
            "goblin_ear", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> BEASTMAN_FUR = AwakeningSystemMod.ITEMS.register(
            "beastman_fur", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> BEASTMAN_FANG = AwakeningSystemMod.ITEMS.register(
            "beastman_fang", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> TAOIST_TALISMAN = AwakeningSystemMod.ITEMS.register(
            "taoist_talisman", () -> new com.soybe.awakeningsystem.item.TaoistTalismanItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> JADE_CARAPACE_SCALE = AwakeningSystemMod.ITEMS.register(
            "jade_carapace_scale", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> THOUSAND_YEAR_GINSENG = AwakeningSystemMod.ITEMS.register(
            "thousand_year_ginseng", () -> new com.soybe.awakeningsystem.item.ThousandYearGinsengItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> WILD_GINSENG = AwakeningSystemMod.ITEMS.register(
            "wild_ginseng", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.WILD_GINSENG.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> SNOW_LOTUS = AwakeningSystemMod.ITEMS.register(
            "snow_lotus", () -> new com.soybe.awakeningsystem.item.SnowLotusItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MURIM_ASCENSION_TALISMAN = AwakeningSystemMod.ITEMS.register(
            "murim_talisman", () -> new com.soybe.awakeningsystem.item.MurimAscensionTalismanItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> FLYING_SWORD = AwakeningSystemMod.ITEMS.register(
            "flying_sword", () -> new FlyingSwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ASTRAL_CRYSTAL_SCALE = AwakeningSystemMod.ITEMS.register(
            "astral_crystal_scale", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredHolder<Item, Item> DEATH_KING_CLOCK = AwakeningSystemMod.ITEMS.register(
            "death_king_clock", () -> new DeathKingClockItem(
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> PLUM_BLOSSOM_SWORD = AwakeningSystemMod.ITEMS.register(
            "plum_blossom_sword", () -> new PlumBlossomSwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> GIGANTIC_GREATSWORD = AwakeningSystemMod.ITEMS.register(
            "gigantic_greatsword", () -> new GiganticGreatswordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LA_DONCELLA = AwakeningSystemMod.ITEMS.register(
            "la_doncella", () -> new LaDoncellaSwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> CREATOR_CLAYMORE = AwakeningSystemMod.ITEMS.register(
            "creator_claymore", () -> new CreatorClaymoreItem(new Item.Properties().stacksTo(1)));

    // --- ARMAS TRIBUTO DE SWORD ART ONLINE (SAO) ---
    public static final DeferredHolder<Item, Item> ELUCIDATOR = AwakeningSystemMod.ITEMS.register(
            "elucidator", () -> new ElucidatorItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> DARK_REPULSER = AwakeningSystemMod.ITEMS.register(
            "dark_repulser", () -> new DarkRepulserItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LAMBENT_LIGHT = AwakeningSystemMod.ITEMS.register(
            "lambent_light", () -> new LambentLightItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> TRANSCENDENT_THORN = AwakeningSystemMod.ITEMS.register(
            "transcendent_thorn", () -> new TranscendentThornItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> DAWNS_BALLAD = AwakeningSystemMod.ITEMS.register(
            "dawns_ballad", () -> new DawnsBalladItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SHADOW_DAGGERS = AwakeningSystemMod.ITEMS.register(
            "shadow_daggers", () -> new ShadowDaggersItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> RADIANT_LIGHT_SWORD = AwakeningSystemMod.ITEMS.register(
            "radiant_light_sword", () -> new RadiantLightSwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SKY_DEMON_SWORD = AwakeningSystemMod.ITEMS.register(
            "sky_demon_sword", () -> new SkyDemonSwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> HEAVEN_SHAKING_SWORD = AwakeningSystemMod.ITEMS.register(
            "heaven_shaking_sword", () -> new HeavenShakingSwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ASRAHAN_HOLY_SWORD = AwakeningSystemMod.ITEMS.register(
            "asrahan_holy_sword", () -> new AsrahanHolySwordItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> UNBREAKABLE_FAITH = AwakeningSystemMod.ITEMS.register(
            "unbreakable_faith", () -> new UnbreakableFaithItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> BLACK_DRAGON_SCYTHE = AwakeningSystemMod.ITEMS.register(
            "black_dragon_scythe", () -> new BlackDragonScytheItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> DREAD_SCYTHE = AwakeningSystemMod.ITEMS.register(
            "dread_scythe", () -> new DreadScytheItem(new Item.Properties().stacksTo(1)));

    // === SISTEMA GACHA AUTÉNTICO & TEYVAT ===
    public static final DeferredHolder<Item, Item> PRIMOGEM = AwakeningSystemMod.ITEMS.register(
            "primogem", () -> new com.soybe.awakeningsystem.item.PrimogemItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> INTERTWINED_FATE = AwakeningSystemMod.ITEMS.register(
            "intertwined_fate", () -> new com.soybe.awakeningsystem.item.IntertwinedFateItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ACQUAINT_FATE = AwakeningSystemMod.ITEMS.register(
            "acquaint_fate", () -> new com.soybe.awakeningsystem.item.AcquaintFateItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> COMPANION_CONTRACT = AwakeningSystemMod.ITEMS.register(
            "companion_contract", () -> new com.soybe.awakeningsystem.item.CompanionContractItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MASTERLESS_STARDUST = AwakeningSystemMod.ITEMS.register(
            "masterless_stardust", () -> new com.soybe.awakeningsystem.item.MasterlessStardustItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MASTERLESS_STARGLITTER = AwakeningSystemMod.ITEMS.register(
            "masterless_starglitter", () -> new com.soybe.awakeningsystem.item.MasterlessStarglitterItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GIFT_CARD_5 = AwakeningSystemMod.ITEMS.register(
            "gift_card_5", () -> new com.soybe.awakeningsystem.item.GiftCardItem(5, Rarity.COMMON));

    public static final DeferredHolder<Item, Item> GIFT_CARD_15 = AwakeningSystemMod.ITEMS.register(
            "gift_card_15", () -> new com.soybe.awakeningsystem.item.GiftCardItem(15, Rarity.UNCOMMON));

    public static final DeferredHolder<Item, Item> GIFT_CARD_30 = AwakeningSystemMod.ITEMS.register(
            "gift_card_30", () -> new com.soybe.awakeningsystem.item.GiftCardItem(30, Rarity.RARE));

    public static final DeferredHolder<Item, Item> GIFT_CARD_50 = AwakeningSystemMod.ITEMS.register(
            "gift_card_50", () -> new com.soybe.awakeningsystem.item.GiftCardItem(50, Rarity.EPIC));

    public static final DeferredHolder<Item, Item> GIFT_CARD_100 = AwakeningSystemMod.ITEMS.register(
            "gift_card_100", () -> new com.soybe.awakeningsystem.item.GiftCardItem(100, Rarity.EPIC));


    public static final DeferredHolder<Item, Item> STAFF_OF_HOMA = AwakeningSystemMod.ITEMS.register(
            "staff_of_homa", () -> new com.soybe.awakeningsystem.item.StaffOfHomaItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> AMOS_BOW = AwakeningSystemMod.ITEMS.register(
            "amos_bow", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.AMOS_BOW, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> AQUA_SIMULACRA = AwakeningSystemMod.ITEMS.register(
            "aqua_simulacra", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.AQUA_SIMULACRA, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> THUNDERING_PULSE = AwakeningSystemMod.ITEMS.register(
            "thundering_pulse", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.THUNDERING_PULSE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ASTRAL_ARCHERS_BOW = AwakeningSystemMod.ITEMS.register(
            "astral_archers_bow", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.ASTRAL_ARCHERS_BOW, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SKYWARD_HARP = AwakeningSystemMod.ITEMS.register(
            "skyward_harp", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.SKYWARD_HARP, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> POLAR_STAR = AwakeningSystemMod.ITEMS.register(
            "polar_star", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.POLAR_STAR, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> HUNTER_PATH = AwakeningSystemMod.ITEMS.register(
            "hunter_path", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.HUNTER_PATH, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> HAMAYUMI = AwakeningSystemMod.ITEMS.register(
            "hamayumi", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.HAMAYUMI, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> END_OF_THE_LINE = AwakeningSystemMod.ITEMS.register(
            "end_of_the_line", () -> new com.soybe.awakeningsystem.item.GenshinBowItem(com.soybe.awakeningsystem.item.GenshinBowItem.BowType.END_OF_THE_LINE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ENGULFING_LIGHTNING = AwakeningSystemMod.ITEMS.register(
            "engulfing_lightning", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.ENGULFING_LIGHTNING, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> PRIMORDIAL_JADE_WINGED_SPEAR = AwakeningSystemMod.ITEMS.register(
            "primordial_jade_winged_spear", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.PRIMORDIAL_JADE_WINGED_SPEAR, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> CRIMSON_MOONS_SEMBLANCE = AwakeningSystemMod.ITEMS.register(
            "crimson_moons_semblance", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.CRIMSON_MOONS_SEMBLANCE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VORTEX_VANQUISHER = AwakeningSystemMod.ITEMS.register(
            "vortex_vanquisher", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.VORTEX_VANQUISHER, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> THE_CATCH = AwakeningSystemMod.ITEMS.register(
            "the_catch", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.THE_CATCH, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> WOLFS_GRAVESTONE = AwakeningSystemMod.ITEMS.register(
            "wolfs_gravestone", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.WOLFS_GRAVESTONE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> REDHORN_STONETHRESHER = AwakeningSystemMod.ITEMS.register(
            "redhorn_stonethresher", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.REDHORN_STONETHRESHER, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VERDICT = AwakeningSystemMod.ITEMS.register(
            "verdict", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.VERDICT, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> MISTSPLITTER_REFORGED = AwakeningSystemMod.ITEMS.register(
            "mistsplitter_reforged", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.MISTSPLITTER_REFORGED, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> PRIMORDIAL_JADE_CUTTER = AwakeningSystemMod.ITEMS.register(
            "primordial_jade_cutter", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.PRIMORDIAL_JADE_CUTTER, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> FREEDOM_SWORN = AwakeningSystemMod.ITEMS.register(
            "freedom_sworn", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.FREEDOM_SWORN, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> KEY_OF_KHAJ_NISUT = AwakeningSystemMod.ITEMS.register(
            "key_of_khaj_nisut", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.KEY_OF_KHAJ_NISUT, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ABSOLUTION = AwakeningSystemMod.ITEMS.register(
            "absolution", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.ABSOLUTION, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SKYWARD_BLADE = AwakeningSystemMod.ITEMS.register(
            "skyward_blade", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.SKYWARD_BLADE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> THE_FLUTE = AwakeningSystemMod.ITEMS.register(
            "the_flute", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.THE_FLUTE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> THE_BLACK_SWORD = AwakeningSystemMod.ITEMS.register(
            "the_black_sword", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.THE_BLACK_SWORD, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> BLACK_TASSEL = AwakeningSystemMod.ITEMS.register(
            "black_tassel", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.BLACK_TASSEL, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> FLEUVE_CENDRE_FERRYMAN = AwakeningSystemMod.ITEMS.register(
            "fleuve_cendre_ferryman", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.FLEUVE_CENDRE_FERRYMAN, new Item.Properties().stacksTo(1)));

    // === CURIOSIDADES Y DIVISAS DE HOYOVERSE (GENSHIN & STAR RAIL) ===
    public static final DeferredHolder<Item, Item> GALACTIC_BIG_LOTTO = AwakeningSystemMod.ITEMS.register(
            "galactic_big_lotto", () -> new com.soybe.awakeningsystem.item.GalacticBigLottoItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> COSMIC_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "cosmic_fragment", () -> new com.soybe.awakeningsystem.item.CosmicFragmentItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ROBE_OF_THE_BEAUTY = AwakeningSystemMod.ITEMS.register(
            "robe_of_the_beauty", () -> new com.soybe.awakeningsystem.item.RobeOfTheBeautyItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> PRAISE_OF_LOFTY_MORALITY = AwakeningSystemMod.ITEMS.register(
            "praise_of_lofty_morality", () -> new com.soybe.awakeningsystem.item.PraiseOfLoftyMoralityItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GENESIS_CRYSTAL = AwakeningSystemMod.ITEMS.register(
            "genesis_crystal", () -> new com.soybe.awakeningsystem.item.GenesisCrystalItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> STELLAR_JADE = AwakeningSystemMod.ITEMS.register(
            "stellar_jade", () -> new com.soybe.awakeningsystem.item.StellarJadeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> STAR_RAIL_SPECIAL_PASS = AwakeningSystemMod.ITEMS.register(
            "star_rail_special_pass", () -> new com.soybe.awakeningsystem.item.StarRailSpecialPassItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> HEROS_WIT = AwakeningSystemMod.ITEMS.register(
            "heros_wit", () -> new com.soybe.awakeningsystem.item.HerosWitItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MORA = AwakeningSystemMod.ITEMS.register(
            "mora", () -> new com.soybe.awakeningsystem.item.MoraItem(new Item.Properties()));

    // === DISCOS DE MÚSICA OFICIALES HOYOVERSE ===
    public static final DeferredHolder<Item, Item> MUSIC_DISC_TAKE_THE_JOURNEY = AwakeningSystemMod.ITEMS.register(
            "music_disc_take_the_journey", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "take_the_journey")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_SPACE_WALK = AwakeningSystemMod.ITEMS.register(
            "music_disc_space_walk", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "space_walk")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_QINGCE_NIGHT = AwakeningSystemMod.ITEMS.register(
            "music_disc_qingce_night", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "qingce_night")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_WINDRISE_NIGHT = AwakeningSystemMod.ITEMS.register(
            "music_disc_windrise_night", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "windrise_night")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_DIVINE_FOX = AwakeningSystemMod.ITEMS.register(
            "music_disc_divine_fox", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "divine_fox")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_WATERS_POEM = AwakeningSystemMod.ITEMS.register(
            "music_disc_waters_poem", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "waters_poem")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_DAY_OF_HOPE = AwakeningSystemMod.ITEMS.register(
            "music_disc_day_of_hope", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "day_of_hope")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_CHASING_STARS = AwakeningSystemMod.ITEMS.register(
            "music_disc_chasing_stars", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chasing_stars")))));

    public static final DeferredHolder<Item, Item> MUSIC_DISC_SALTED_MOON = AwakeningSystemMod.ITEMS.register(
            "music_disc_salted_moon", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "salted_moon")))));

    // === HONKAI: STAR RAIL & ZENLESS ZONE ZERO ===
    public static final DeferredHolder<Item, Item> HERTA_HAMMER = AwakeningSystemMod.ITEMS.register(
            "herta_hammer", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.HERTA_HAMMER, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> QINGMING_SWORD = AwakeningSystemMod.ITEMS.register(
            "qingming_sword", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.QINGMING_SWORD, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> TAILLESS = AwakeningSystemMod.ITEMS.register(
            "tailless", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.TAILLESS, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SHUNGUANG_SWORD = AwakeningSystemMod.ITEMS.register(
            "shunguang_sword", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.SHUNGUANG_SWORD, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SHIYUAN_SWORD = AwakeningSystemMod.ITEMS.register(
            "shiyuan_sword", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.SHIYUAN_SWORD, new Item.Properties().stacksTo(1)));

    // === HONKAI IMPACT 3RD ===
    public static final DeferredHolder<Item, Item> DOMAIN_OF_SANCTION = AwakeningSystemMod.ITEMS.register(
            "domain_of_sanction", () -> new com.soybe.awakeningsystem.item.GenshinWeaponItem(com.soybe.awakeningsystem.item.GenshinWeaponItem.WeaponType.DOMAIN_OF_SANCTION, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LIGHT_CONE_MILKY_WAY = AwakeningSystemMod.ITEMS.register(
            "light_cone_milky_way", () -> new com.soybe.awakeningsystem.item.LightConeNightOnTheMilkyWayItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LIGHT_CONE_PASSING_SHORE = AwakeningSystemMod.ITEMS.register(
            "light_cone_passing_shore", () -> new com.soybe.awakeningsystem.item.LightConeAlongThePassingShoreItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LIGHT_CONE_PATIENCE = AwakeningSystemMod.ITEMS.register(
            "light_cone_patience", () -> new com.soybe.awakeningsystem.item.LightConePatienceIsAllYouNeedItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LIGHT_CONE_DREAMS_REST = AwakeningSystemMod.ITEMS.register(
            "light_cone_dreams_rest", () -> new com.soybe.awakeningsystem.item.LightConeWhereDreamsShouldRestItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LIGHT_CONE_BEFORE_DAWN = AwakeningSystemMod.ITEMS.register(
            "light_cone_before_dawn", () -> new com.soybe.awakeningsystem.item.LightConeBeforeDawnItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> GACHA_CAPSULE = AwakeningSystemMod.ITEMS.register(
            "gacha_capsule", () -> new com.soybe.awakeningsystem.item.GachaCapsuleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> SLOT_MACHINE = AwakeningSystemMod.ITEMS.register(
            "slot_machine", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SLOT_MACHINE.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> GACHA_MACHINE = AwakeningSystemMod.ITEMS.register(
            "gacha_machine", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.GACHA_MACHINE.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ABYSSAL_VOID_STONE = AwakeningSystemMod.ITEMS.register(
            "abyssal_void_stone", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ABYSSAL_VOID_STONE.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> CELESTIAL_WAYPOINT = AwakeningSystemMod.ITEMS.register(
            "celestial_waypoint", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_WAYPOINT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> TRILUNAR_ALTAR = AwakeningSystemMod.ITEMS.register(
            "trilunar_altar", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.TRILUNAR_ALTAR.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> LUNAR_AUTHORITY_ARIA = AwakeningSystemMod.ITEMS.register(
            "lunar_authority_aria", () -> new com.soybe.awakeningsystem.item.LunarAuthorityItem(com.soybe.awakeningsystem.item.LunarAuthorityItem.MoonGoddess.ARIA, new Item.Properties()));

    public static final DeferredHolder<Item, Item> LUNAR_AUTHORITY_SONNET = AwakeningSystemMod.ITEMS.register(
            "lunar_authority_sonnet", () -> new com.soybe.awakeningsystem.item.LunarAuthorityItem(com.soybe.awakeningsystem.item.LunarAuthorityItem.MoonGoddess.SONNET, new Item.Properties()));

    public static final DeferredHolder<Item, Item> LUNAR_AUTHORITY_CANON = AwakeningSystemMod.ITEMS.register(
            "lunar_authority_canon", () -> new com.soybe.awakeningsystem.item.LunarAuthorityItem(com.soybe.awakeningsystem.item.LunarAuthorityItem.MoonGoddess.CANON, new Item.Properties()));

    public static final DeferredHolder<Item, Item> TRILUNAR_GUIDE = AwakeningSystemMod.ITEMS.register(
            "trilunar_guide", () -> new com.soybe.awakeningsystem.item.TrilunarGuideItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> COMMAND_SPELL = AwakeningSystemMod.ITEMS.register(
            "command_spell", () -> new com.soybe.awakeningsystem.item.CommandSpellItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_PYRO = AwakeningSystemMod.ITEMS.register(
            "vision_pyro", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.PYRO, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_ELECTRO = AwakeningSystemMod.ITEMS.register(
            "vision_electro", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.ELECTRO, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_CRYO = AwakeningSystemMod.ITEMS.register(
            "vision_cryo", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.CRYO, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_ANEMO = AwakeningSystemMod.ITEMS.register(
            "vision_anemo", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.ANEMO, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_HYDRO = AwakeningSystemMod.ITEMS.register(
            "vision_hydro", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.HYDRO, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_GEO = AwakeningSystemMod.ITEMS.register(
            "vision_geo", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.GEO, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VISION_DENDRO = AwakeningSystemMod.ITEMS.register(
            "vision_dendro", () -> new com.soybe.awakeningsystem.item.TeyvatVisionItem(com.soybe.awakeningsystem.item.TeyvatVisionItem.Element.DENDRO, new Item.Properties().stacksTo(1)));

    // === ARMAS EXÓTICAS MÍSTICAS ===
    public static final DeferredHolder<Item, Item> JADE_FEIJIAN = AwakeningSystemMod.ITEMS.register(
            "jade_feijian", () -> new com.soybe.awakeningsystem.item.JadeFeijianItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ASTRAL_GUANDAO = AwakeningSystemMod.ITEMS.register(
            "astral_guandao", () -> new com.soybe.awakeningsystem.item.AstralGuandaoItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ADAMANTITE_WAR_HAMMER = AwakeningSystemMod.ITEMS.register(
            "adamantite_war_hammer", () -> new com.soybe.awakeningsystem.item.AdamantiteWarHammerItem(new Item.Properties().stacksTo(1)));



    // === PIEDRAS DE RUNA FÍSICAS ===
    public static final DeferredHolder<Item, Item> RUNE_STONE_RULERS_AUTHORITY = AwakeningSystemMod.ITEMS.register(
            "rune_stone_rulers_authority", () -> new RuneStoneItem(new Item.Properties(), RuneStoneItem.RuneType.RULERS_AUTHORITY));

    public static final DeferredHolder<Item, Item> RUNE_STONE_DRAGON_EYES = AwakeningSystemMod.ITEMS.register(
            "rune_stone_dragon_eyes", () -> new RuneStoneItem(new Item.Properties(), RuneStoneItem.RuneType.DRAGON_EYES));

    public static final DeferredHolder<Item, Item> RUNE_STONE_BLOODTHIRST = AwakeningSystemMod.ITEMS.register(
            "rune_stone_bloodthirst", () -> new RuneStoneItem(new Item.Properties(), RuneStoneItem.RuneType.BLOODTHIRST));

    public static final DeferredHolder<Item, Item> RUNE_STONE_SHADOW_STEP = AwakeningSystemMod.ITEMS.register(
            "rune_stone_shadow_step", () -> new RuneStoneItem(new Item.Properties(), RuneStoneItem.RuneType.SHADOW_STEP));

    public static final DeferredHolder<Item, Item> RUNE_STONE_FOURTH_WALL = AwakeningSystemMod.ITEMS.register(
            "rune_stone_fourth_wall", () -> new RuneStoneItem(new Item.Properties(), RuneStoneItem.RuneType.FOURTH_WALL));

    public static final DeferredHolder<Item, Item> RUNE_STONE_IRON_BODY = AwakeningSystemMod.ITEMS.register(
            "rune_stone_iron_body", () -> new RuneStoneItem(new Item.Properties(), RuneStoneItem.RuneType.IRON_BODY));

    // === RELIQUIAS Y ACCESORIOS PASIVOS ===
    public static final DeferredHolder<Item, Item> DEMON_MONARCH_RING = AwakeningSystemMod.ITEMS.register(
            "demon_monarch_ring", () -> new ManhwaArtifactItem(new Item.Properties(), ManhwaArtifactItem.ArtifactType.DEMON_MONARCH_RING));

    public static final DeferredHolder<Item, Item> ASRAHAN_MANA_CHALICE = AwakeningSystemMod.ITEMS.register(
            "asrahan_mana_chalice", () -> new ManhwaArtifactItem(new Item.Properties(), ManhwaArtifactItem.ArtifactType.ASRAHAN_MANA_CHALICE));

    public static final DeferredHolder<Item, Item> KING_SLAYER_NECKLACE = AwakeningSystemMod.ITEMS.register(
            "king_slayer_necklace", () -> new ManhwaArtifactItem(new Item.Properties(), ManhwaArtifactItem.ArtifactType.KING_SLAYER_NECKLACE));

    public static final DeferredHolder<Item, Item> PHOENIX_FEATHER = AwakeningSystemMod.ITEMS.register(
            "phoenix_feather", () -> new ManhwaArtifactItem(new Item.Properties(), ManhwaArtifactItem.ArtifactType.PHOENIX_FEATHER));

    // ==========================================
    // === MINERALES Y MATERIALES POST-NETHERITE ===
    // ==========================================
    // 0. Mineral Abisal
    public static final DeferredHolder<Item, Item> RAW_ABYSSAL_ORE = AwakeningSystemMod.ITEMS.register(
            "raw_abyssal_ore", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ABYSSAL_INGOT = AwakeningSystemMod.ITEMS.register(
            "abyssal_ingot", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ABYSSAL_NUGGET = AwakeningSystemMod.ITEMS.register(
            "abyssal_nugget", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    // 1. Mithril
    public static final DeferredHolder<Item, Item> RAW_MITHRIL = AwakeningSystemMod.ITEMS.register(
            "raw_mithril", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> MITHRIL_INGOT = AwakeningSystemMod.ITEMS.register(
            "mithril_ingot", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> MITHRIL_NUGGET = AwakeningSystemMod.ITEMS.register(
            "mithril_nugget", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    // 2. Adamantita
    public static final DeferredHolder<Item, Item> RAW_ADAMANTITE = AwakeningSystemMod.ITEMS.register(
            "raw_adamantite", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ADAMANTITE_INGOT = AwakeningSystemMod.ITEMS.register(
            "adamantite_ingot", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ADAMANTITE_NUGGET = AwakeningSystemMod.ITEMS.register(
            "adamantite_nugget", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));

    // 3. Jade Espiritual
    public static final DeferredHolder<Item, Item> RAW_SPIRIT_JADE = AwakeningSystemMod.ITEMS.register(
            "raw_spirit_jade", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_SHARD = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_shard", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    // 4. Metal Astral
    public static final DeferredHolder<Item, Item> RAW_ASTRAL = AwakeningSystemMod.ITEMS.register(
            "raw_astral", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ASTRAL_INGOT = AwakeningSystemMod.ITEMS.register(
            "astral_ingot", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ASTRAL_NUGGET = AwakeningSystemMod.ITEMS.register(
            "astral_nugget", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    // ==========================================
    // === BLOQUES Y MENAS POST-NETHERITE ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ABYSSAL_STONE = AwakeningSystemMod.ITEMS.register(
            "abyssal_stone", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ABYSSAL_STONE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ABYSSAL_RESONANT_CRYSTAL = AwakeningSystemMod.ITEMS.register(
            "abyssal_resonant_crystal", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ABYSSAL_RESONANT_CRYSTAL.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ABYSSAL_ORE = AwakeningSystemMod.ITEMS.register(
            "abyssal_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ABYSSAL_ORE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> DEEPSLATE_ABYSSAL_ORE = AwakeningSystemMod.ITEMS.register(
            "deepslate_abyssal_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.DEEPSLATE_ABYSSAL_ORE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ABYSSAL_BLOCK = AwakeningSystemMod.ITEMS.register(
            "abyssal_block", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ABYSSAL_BLOCK.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> MITHRIL_ORE = AwakeningSystemMod.ITEMS.register(
            "mithril_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MITHRIL_ORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> DEEPSLATE_MITHRIL_ORE = AwakeningSystemMod.ITEMS.register(
            "deepslate_mithril_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.DEEPSLATE_MITHRIL_ORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> MITHRIL_BLOCK = AwakeningSystemMod.ITEMS.register(
            "mithril_block", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MITHRIL_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> ADAMANTITE_ORE = AwakeningSystemMod.ITEMS.register(
            "adamantite_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADAMANTITE_ORE.get(), new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ADAMANTITE_BLOCK = AwakeningSystemMod.ITEMS.register(
            "adamantite_block", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADAMANTITE_BLOCK.get(), new Item.Properties().fireResistant().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> SPIRIT_JADE_ORE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_JADE_ORE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_BLOCK = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_block", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_JADE_BLOCK.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ASTRAL_ORE = AwakeningSystemMod.ITEMS.register(
            "astral_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ASTRAL_ORE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ASTRAL_BLOCK = AwakeningSystemMod.ITEMS.register(
            "astral_block", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ASTRAL_BLOCK.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    // ==========================================
    // === MINERALES DE TEYVAT (BLOCK ITEMS) ===
    // ==========================================
    public static final DeferredHolder<Item, Item> PRIMOGEM_ORE = AwakeningSystemMod.ITEMS.register(
            "primogem_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.PRIMOGEM_ORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> DEEPSLATE_PRIMOGEM_ORE = AwakeningSystemMod.ITEMS.register(
            "deepslate_primogem_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.DEEPSLATE_PRIMOGEM_ORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> WHITE_IRON_ORE = AwakeningSystemMod.ITEMS.register(
            "white_iron_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.WHITE_IRON_ORE.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> AGNIDUS_AGATE_ORE = AwakeningSystemMod.ITEMS.register(
            "agnidus_agate_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.AGNIDUS_AGATE_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> SHIVADA_JADE_ORE = AwakeningSystemMod.ITEMS.register(
            "shivada_jade_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SHIVADA_JADE_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> VARUNADA_LAZURITE_ORE = AwakeningSystemMod.ITEMS.register(
            "varunada_lazurite_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.VARUNADA_LAZURITE_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> VAJRADA_AMETHYST_ORE = AwakeningSystemMod.ITEMS.register(
            "vajrada_amethyst_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.VAJRADA_AMETHYST_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> VAYUDA_TURQUOISE_ORE = AwakeningSystemMod.ITEMS.register(
            "vayuda_turquoise_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.VAYUDA_TURQUOISE_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> PRITHIVA_TOPAZ_ORE = AwakeningSystemMod.ITEMS.register(
            "prithiva_topaz_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.PRITHIVA_TOPAZ_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> NAGADUS_EMERALD_ORE = AwakeningSystemMod.ITEMS.register(
            "nagadus_emerald_ore", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.NAGADUS_EMERALD_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ==========================================
    // === FRAGMENTOS Y CHUNKS DE TEYVAT ===
    // ==========================================
    public static final DeferredHolder<Item, Item> WHITE_IRON_CHUNK = AwakeningSystemMod.ITEMS.register(
            "white_iron_chunk", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));

    // ==========================================
    // === HORNOS DE ALTA VELOCIDAD (ADDON IRON FURNACES) ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ABYSSAL_FURNACE = AwakeningSystemMod.ITEMS.register(
            "abyssal_furnace", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ABYSSAL_FURNACE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> MITHRIL_FURNACE = AwakeningSystemMod.ITEMS.register(
            "mithril_furnace", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MITHRIL_FURNACE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ADAMANTITE_FURNACE = AwakeningSystemMod.ITEMS.register(
            "adamantite_furnace", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADAMANTITE_FURNACE.get(), new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_FURNACE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_furnace", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_JADE_FURNACE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ASTRAL_FURNACE = AwakeningSystemMod.ITEMS.register(
            "astral_furnace", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ASTRAL_FURNACE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    // ==========================================
    // === ÍTEMS DE MEJORA DE HORNOS ===
    // ==========================================
    public static final DeferredHolder<Item, Item> UPGRADE_NETHERITE_TO_ABYSSAL = AwakeningSystemMod.ITEMS.register(
            "upgrade_netherite_to_abyssal", () -> new FurnaceUpgradeItem(FurnaceTier.ABYSSAL, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> UPGRADE_NETHERITE_TO_MITHRIL = AwakeningSystemMod.ITEMS.register(
            "upgrade_netherite_to_mithril", () -> new FurnaceUpgradeItem(FurnaceTier.MITHRIL, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> UPGRADE_MITHRIL_TO_ADAMANTITE = AwakeningSystemMod.ITEMS.register(
            "upgrade_mithril_to_adamantite", () -> new FurnaceUpgradeItem(FurnaceTier.ADAMANTITE, new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> UPGRADE_ADAMANTITE_TO_SPIRIT_JADE = AwakeningSystemMod.ITEMS.register(
            "upgrade_adamantite_to_spirit_jade", () -> new FurnaceUpgradeItem(FurnaceTier.SPIRIT_JADE, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> UPGRADE_SPIRIT_JADE_TO_ASTRAL = AwakeningSystemMod.ITEMS.register(
            "upgrade_spirit_jade_to_astral", () -> new FurnaceUpgradeItem(FurnaceTier.ASTRAL, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));


    // ==========================================
    // === LLAVES DIMENSIONALES ===
    // ==========================================
    public static final DeferredHolder<Item, Item> OVERWORLD_DUNGEON_KEY = AwakeningSystemMod.ITEMS.register(
            "overworld_dungeon_key", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> NETHER_FLAME_KEY = AwakeningSystemMod.ITEMS.register(
            "nether_flame_key", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> END_VOID_KEY = AwakeningSystemMod.ITEMS.register(
            "end_void_key", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ISEKAI_MANA_KEY = AwakeningSystemMod.ITEMS.register(
            "isekai_mana_key", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> MURIM_DAO_KEY = AwakeningSystemMod.ITEMS.register(
            "murim_dao_key", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // ==========================================
    // === PLANTILLAS DE MEJORA DE HERRERÍA (SMITHING TEMPLATES) ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ABYSSAL_UPGRADE_SMITHING_TEMPLATE = AwakeningSystemMod.ITEMS.register(
            "abyssal_upgrade_smithing_template", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> MITHRIL_UPGRADE_SMITHING_TEMPLATE = AwakeningSystemMod.ITEMS.register(
            "mithril_upgrade_smithing_template", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ADAMANTITE_UPGRADE_SMITHING_TEMPLATE = AwakeningSystemMod.ITEMS.register(
            "adamantite_upgrade_smithing_template", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_UPGRADE_SMITHING_TEMPLATE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_upgrade_smithing_template", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ASTRAL_UPGRADE_SMITHING_TEMPLATE = AwakeningSystemMod.ITEMS.register(
            "astral_upgrade_smithing_template", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    // ==========================================
    // === HERRAMIENTAS ABISAL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ABYSSAL_SWORD = AwakeningSystemMod.ITEMS.register(
            "abyssal_sword", () -> new SwordItem(CustomToolTiers.ABYSSAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(CustomToolTiers.ABYSSAL, 3, -2.4F))));
    public static final DeferredHolder<Item, Item> ABYSSAL_PICKAXE = AwakeningSystemMod.ITEMS.register(
            "abyssal_pickaxe", () -> new PickaxeItem(CustomToolTiers.ABYSSAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ABYSSAL, 1.0F, -2.8F))));
    public static final DeferredHolder<Item, Item> ABYSSAL_AXE = AwakeningSystemMod.ITEMS.register(
            "abyssal_axe", () -> new AxeItem(CustomToolTiers.ABYSSAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ABYSSAL, 5.0F, -3.0F))));
    public static final DeferredHolder<Item, Item> ABYSSAL_SHOVEL = AwakeningSystemMod.ITEMS.register(
            "abyssal_shovel", () -> new ShovelItem(CustomToolTiers.ABYSSAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ABYSSAL, 1.5F, -3.0F))));
    public static final DeferredHolder<Item, Item> ABYSSAL_HOE = AwakeningSystemMod.ITEMS.register(
            "abyssal_hoe", () -> new HoeItem(CustomToolTiers.ABYSSAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ABYSSAL, 0.0F, -3.0F))));

    // ==========================================
    // === HERRAMIENTAS MITHRIL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> MITHRIL_SWORD = AwakeningSystemMod.ITEMS.register(
            "mithril_sword", () -> new SwordItem(CustomToolTiers.MITHRIL,
                    new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(CustomToolTiers.MITHRIL, 3, -2.4F))));
    public static final DeferredHolder<Item, Item> MITHRIL_PICKAXE = AwakeningSystemMod.ITEMS.register(
            "mithril_pickaxe", () -> new PickaxeItem(CustomToolTiers.MITHRIL,
                    new Item.Properties().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.MITHRIL, 1.0F, -2.8F))));
    public static final DeferredHolder<Item, Item> MITHRIL_AXE = AwakeningSystemMod.ITEMS.register(
            "mithril_axe", () -> new AxeItem(CustomToolTiers.MITHRIL,
                    new Item.Properties().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.MITHRIL, 5.0F, -3.0F))));
    public static final DeferredHolder<Item, Item> MITHRIL_SHOVEL = AwakeningSystemMod.ITEMS.register(
            "mithril_shovel", () -> new ShovelItem(CustomToolTiers.MITHRIL,
                    new Item.Properties().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.MITHRIL, 1.5F, -3.0F))));
    public static final DeferredHolder<Item, Item> MITHRIL_HOE = AwakeningSystemMod.ITEMS.register(
            "mithril_hoe", () -> new HoeItem(CustomToolTiers.MITHRIL,
                    new Item.Properties().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.MITHRIL, 0.0F, -3.0F))));

    // ==========================================
    // === HERRAMIENTAS ADAMANTITA ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ADAMANTITE_SWORD = AwakeningSystemMod.ITEMS.register(
            "adamantite_sword", () -> new SwordItem(CustomToolTiers.ADAMANTITE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(CustomToolTiers.ADAMANTITE, 3, -2.4F))));
    public static final DeferredHolder<Item, Item> ADAMANTITE_PICKAXE = AwakeningSystemMod.ITEMS.register(
            "adamantite_pickaxe", () -> new PickaxeItem(CustomToolTiers.ADAMANTITE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.ADAMANTITE, 1.0F, -2.8F))));
    public static final DeferredHolder<Item, Item> ADAMANTITE_AXE = AwakeningSystemMod.ITEMS.register(
            "adamantite_axe", () -> new AxeItem(CustomToolTiers.ADAMANTITE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.ADAMANTITE, 5.0F, -3.0F))));
    public static final DeferredHolder<Item, Item> ADAMANTITE_SHOVEL = AwakeningSystemMod.ITEMS.register(
            "adamantite_shovel", () -> new ShovelItem(CustomToolTiers.ADAMANTITE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.ADAMANTITE, 1.5F, -3.0F))));
    public static final DeferredHolder<Item, Item> ADAMANTITE_HOE = AwakeningSystemMod.ITEMS.register(
            "adamantite_hoe", () -> new HoeItem(CustomToolTiers.ADAMANTITE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(CustomToolTiers.ADAMANTITE, 0.0F, -3.0F))));

    // ==========================================
    // === HERRAMIENTAS JADE ESPIRITUAL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_SWORD = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_sword", () -> new SwordItem(CustomToolTiers.SPIRIT_JADE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(CustomToolTiers.SPIRIT_JADE, 3, -2.4F))));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_PICKAXE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_pickaxe", () -> new PickaxeItem(CustomToolTiers.SPIRIT_JADE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.SPIRIT_JADE, 1.0F, -2.8F))));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_AXE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_axe", () -> new AxeItem(CustomToolTiers.SPIRIT_JADE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.SPIRIT_JADE, 5.0F, -3.0F))));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_SHOVEL = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_shovel", () -> new ShovelItem(CustomToolTiers.SPIRIT_JADE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.SPIRIT_JADE, 1.5F, -3.0F))));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_HOE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_hoe", () -> new HoeItem(CustomToolTiers.SPIRIT_JADE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.SPIRIT_JADE, 0.0F, -3.0F))));

    // ==========================================
    // === HERRAMIENTAS METAL ASTRAL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ASTRAL_SWORD = AwakeningSystemMod.ITEMS.register(
            "astral_sword", () -> new SwordItem(CustomToolTiers.ASTRAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(CustomToolTiers.ASTRAL, 3, -2.4F))));
    public static final DeferredHolder<Item, Item> ASTRAL_PICKAXE = AwakeningSystemMod.ITEMS.register(
            "astral_pickaxe", () -> new PickaxeItem(CustomToolTiers.ASTRAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ASTRAL, 1.0F, -2.8F))));
    public static final DeferredHolder<Item, Item> ASTRAL_AXE = AwakeningSystemMod.ITEMS.register(
            "astral_axe", () -> new AxeItem(CustomToolTiers.ASTRAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ASTRAL, 5.0F, -3.0F))));
    public static final DeferredHolder<Item, Item> ASTRAL_SHOVEL = AwakeningSystemMod.ITEMS.register(
            "astral_shovel", () -> new ShovelItem(CustomToolTiers.ASTRAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ASTRAL, 1.5F, -3.0F))));
    public static final DeferredHolder<Item, Item> ASTRAL_HOE = AwakeningSystemMod.ITEMS.register(
            "astral_hoe", () -> new HoeItem(CustomToolTiers.ASTRAL,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(CustomToolTiers.ASTRAL, 0.0F, -3.0F))));

    // ==========================================
    // === ARMADURA ABISAL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ABYSSAL_HELMET = AwakeningSystemMod.ITEMS.register(
            "abyssal_helmet", () -> new ManhwaArmorItem(ModArmorMaterials.ABYSSAL, ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.HELMET.getDurability(40)),
                    ManhwaArmorItem.SetType.ABYSSAL));
    public static final DeferredHolder<Item, Item> ABYSSAL_CHESTPLATE = AwakeningSystemMod.ITEMS.register(
            "abyssal_chestplate", () -> new ManhwaArmorItem(ModArmorMaterials.ABYSSAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.CHESTPLATE.getDurability(40)),
                    ManhwaArmorItem.SetType.ABYSSAL));
    public static final DeferredHolder<Item, Item> ABYSSAL_LEGGINGS = AwakeningSystemMod.ITEMS.register(
            "abyssal_leggings", () -> new ManhwaArmorItem(ModArmorMaterials.ABYSSAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.LEGGINGS.getDurability(40)),
                    ManhwaArmorItem.SetType.ABYSSAL));
    public static final DeferredHolder<Item, Item> ABYSSAL_BOOTS = AwakeningSystemMod.ITEMS.register(
            "abyssal_boots", () -> new ManhwaArmorItem(ModArmorMaterials.ABYSSAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.BOOTS.getDurability(40)),
                    ManhwaArmorItem.SetType.ABYSSAL));

    // ==========================================
    // === ARMADURA MITHRIL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> MITHRIL_HELMET = AwakeningSystemMod.ITEMS.register(
            "mithril_helmet", () -> new ManhwaArmorItem(ModArmorMaterials.MITHRIL, ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.RARE).durability(ArmorItem.Type.HELMET.getDurability(45)),
                    ManhwaArmorItem.SetType.MITHRIL));
    public static final DeferredHolder<Item, Item> MITHRIL_CHESTPLATE = AwakeningSystemMod.ITEMS.register(
            "mithril_chestplate", () -> new ManhwaArmorItem(ModArmorMaterials.MITHRIL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.RARE).durability(ArmorItem.Type.CHESTPLATE.getDurability(45)),
                    ManhwaArmorItem.SetType.MITHRIL));
    public static final DeferredHolder<Item, Item> MITHRIL_LEGGINGS = AwakeningSystemMod.ITEMS.register(
            "mithril_leggings", () -> new ManhwaArmorItem(ModArmorMaterials.MITHRIL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.RARE).durability(ArmorItem.Type.LEGGINGS.getDurability(45)),
                    ManhwaArmorItem.SetType.MITHRIL));
    public static final DeferredHolder<Item, Item> MITHRIL_BOOTS = AwakeningSystemMod.ITEMS.register(
            "mithril_boots", () -> new ManhwaArmorItem(ModArmorMaterials.MITHRIL, ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.RARE).durability(ArmorItem.Type.BOOTS.getDurability(45)),
                    ManhwaArmorItem.SetType.MITHRIL));

    // ==========================================
    // === ARMADURA ADAMANTITA ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ADAMANTITE_HELMET = AwakeningSystemMod.ITEMS.register(
            "adamantite_helmet", () -> new ManhwaArmorItem(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).durability(ArmorItem.Type.HELMET.getDurability(60)),
                    ManhwaArmorItem.SetType.ADAMANTITE));
    public static final DeferredHolder<Item, Item> ADAMANTITE_CHESTPLATE = AwakeningSystemMod.ITEMS.register(
            "adamantite_chestplate", () -> new ManhwaArmorItem(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).durability(ArmorItem.Type.CHESTPLATE.getDurability(60)),
                    ManhwaArmorItem.SetType.ADAMANTITE));
    public static final DeferredHolder<Item, Item> ADAMANTITE_LEGGINGS = AwakeningSystemMod.ITEMS.register(
            "adamantite_leggings", () -> new ManhwaArmorItem(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).durability(ArmorItem.Type.LEGGINGS.getDurability(60)),
                    ManhwaArmorItem.SetType.ADAMANTITE));
    public static final DeferredHolder<Item, Item> ADAMANTITE_BOOTS = AwakeningSystemMod.ITEMS.register(
            "adamantite_boots", () -> new ManhwaArmorItem(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE).durability(ArmorItem.Type.BOOTS.getDurability(60)),
                    ManhwaArmorItem.SetType.ADAMANTITE));

    // ==========================================
    // === ARMADURA JADE ESPIRITUAL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_HELMET = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_helmet", () -> new ManhwaArmorItem(ModArmorMaterials.SPIRIT_JADE, ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.HELMET.getDurability(80)),
                    ManhwaArmorItem.SetType.SPIRIT_JADE));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_CHESTPLATE = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_chestplate", () -> new ManhwaArmorItem(ModArmorMaterials.SPIRIT_JADE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.CHESTPLATE.getDurability(80)),
                    ManhwaArmorItem.SetType.SPIRIT_JADE));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_LEGGINGS = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_leggings", () -> new ManhwaArmorItem(ModArmorMaterials.SPIRIT_JADE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.LEGGINGS.getDurability(80)),
                    ManhwaArmorItem.SetType.SPIRIT_JADE));
    public static final DeferredHolder<Item, Item> SPIRIT_JADE_BOOTS = AwakeningSystemMod.ITEMS.register(
            "spirit_jade_boots", () -> new ManhwaArmorItem(ModArmorMaterials.SPIRIT_JADE, ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.BOOTS.getDurability(80)),
                    ManhwaArmorItem.SetType.SPIRIT_JADE));

    // ==========================================
    // === ARMADURA METAL ASTRAL ===
    // ==========================================
    public static final DeferredHolder<Item, Item> ASTRAL_HELMET = AwakeningSystemMod.ITEMS.register(
            "astral_helmet", () -> new ManhwaArmorItem(ModArmorMaterials.ASTRAL, ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.HELMET.getDurability(120)),
                    ManhwaArmorItem.SetType.ASTRAL));
    public static final DeferredHolder<Item, Item> ASTRAL_CHESTPLATE = AwakeningSystemMod.ITEMS.register(
            "astral_chestplate", () -> new ManhwaArmorItem(ModArmorMaterials.ASTRAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.CHESTPLATE.getDurability(120)),
                    ManhwaArmorItem.SetType.ASTRAL));
    public static final DeferredHolder<Item, Item> ASTRAL_LEGGINGS = AwakeningSystemMod.ITEMS.register(
            "astral_leggings", () -> new ManhwaArmorItem(ModArmorMaterials.ASTRAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.LEGGINGS.getDurability(120)),
                    ManhwaArmorItem.SetType.ASTRAL));
    public static final DeferredHolder<Item, Item> ASTRAL_BOOTS = AwakeningSystemMod.ITEMS.register(
            "astral_boots", () -> new ManhwaArmorItem(ModArmorMaterials.ASTRAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC).durability(ArmorItem.Type.BOOTS.getDurability(120)),
                    ManhwaArmorItem.SetType.ASTRAL));

    // ==========================================
    // === ALIMENTOS Y DROPS DE ANIMALES DE MANÁ ===
    // ==========================================
    public static final DeferredHolder<Item, Item> MANA_BEEF = AwakeningSystemMod.ITEMS.register(
            "mana_beef", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build()),
                    15, null));

    public static final DeferredHolder<Item, Item> COOKED_MANA_BEEF = AwakeningSystemMod.ITEMS.register(
            "cooked_mana_beef", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(9).saturationModifier(0.9F).build()),
                    35, new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0)));

    public static final DeferredHolder<Item, Item> MANA_PORKCHOP = AwakeningSystemMod.ITEMS.register(
            "mana_porkchop", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build()),
                    15, null));

    public static final DeferredHolder<Item, Item> COOKED_MANA_PORKCHOP = AwakeningSystemMod.ITEMS.register(
            "cooked_mana_porkchop", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(9).saturationModifier(0.9F).build()),
                    35, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0)));

    public static final DeferredHolder<Item, Item> MANA_MUTTON = AwakeningSystemMod.ITEMS.register(
            "mana_mutton", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build()),
                    12, null));

    public static final DeferredHolder<Item, Item> COOKED_MANA_MUTTON = AwakeningSystemMod.ITEMS.register(
            "cooked_mana_mutton", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8F).build()),
                    30, new MobEffectInstance(MobEffects.REGENERATION, 200, 0)));

    public static final DeferredHolder<Item, Item> MANA_CHICKEN_MEAT = AwakeningSystemMod.ITEMS.register(
            "mana_chicken_meat", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build()),
                    10, null));

    public static final DeferredHolder<Item, Item> COOKED_MANA_CHICKEN_MEAT = AwakeningSystemMod.ITEMS.register(
            "cooked_mana_chicken_meat", () -> new ManaFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.7F).build()),
                    25, new MobEffectInstance(MobEffects.JUMP, 400, 0)));

    public static final DeferredHolder<Item, Item> MANA_MILK_BUCKET = AwakeningSystemMod.ITEMS.register(
            "mana_milk_bucket", () -> new ManaMilkBucketItem(new Item.Properties()));

    // Huevos de Generación de Animales de Maná
    public static final DeferredHolder<Item, Item> MANA_COW_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mana_cow_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MANA_COW, 0x1E2B4C, 0x00E5FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MANA_PIG_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mana_pig_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MANA_PIG, 0x3B1A40, 0xAA00FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MANA_SHEEP_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mana_sheep_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MANA_SHEEP, 0x2A355A, 0x64B5F6, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MANA_CHICKEN_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mana_chicken_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MANA_CHICKEN, 0x8B2500, 0xFFD700, new Item.Properties()));

    // Huevos de Generación de Criaturas y Jefes Faltantes
    public static final DeferredHolder<Item, Item> SHADOW_INFANTRY_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "shadow_infantry_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.SHADOW_INFANTRY, 0x12131A, 0x00E5FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSSAL_ORC_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyssal_orc_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_ORC, 0x223024, 0x66BB6A, new Item.Properties()));

    public static final DeferredHolder<Item, Item> SHADOW_MONARCH_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "shadow_monarch_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.SHADOW_MONARCH_BOSS, 0x0F0918, 0x9C27B0, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSSAL_NECROMANCER_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyssal_necromancer_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_NECROMANCER, 0x1A1423, 0x00E676, new Item.Properties()));

    public static final DeferredHolder<Item, Item> FROST_ELF_ASSASSIN_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "frost_elf_assassin_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.FROST_ELF_ASSASSIN, 0x263238, 0x80D8FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ISEKAI_SLIME_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "isekai_slime_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ISEKAI_SLIME, 0x00B0FF, 0xE0F7FA, new Item.Properties()));

    public static final DeferredHolder<Item, Item> GENSHIN_SLIME_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "genshin_slime_spawn_egg", () -> new com.soybe.awakeningsystem.item.GenshinSlimeSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GENSHIN_SLIME, 0x0288D1, 0xFFEB3B, new Item.Properties()));

    public static final DeferredHolder<Item, Item> HILICHURL_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "hilichurl_spawn_egg", () -> new com.soybe.awakeningsystem.item.HilichurlSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.HILICHURL, 0x4A2E18, 0xEAE6DF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MITACHURL_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "mitachurl_spawn_egg", () -> new com.soybe.awakeningsystem.item.MitachurlSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.MITACHURL, 0x2C1A14, 0xB71C1C, new Item.Properties()));

    public static final DeferredHolder<Item, Item> SPIRIT_FOX_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "spirit_fox_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX, 0xE65100, 0xFFF9C4, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ASTRAL_DRAGON_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "astral_dragon_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ASTRAL_DRAGON, 0x303F9F, 0x80D8FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> SHADOW_DIREWOLF_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "shadow_direwolf_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.SHADOW_DIREWOLF, 0x1A1A24, 0x7986CB, new Item.Properties()));

    public static final DeferredHolder<Item, Item> WHITE_THUNDER_TIGER_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "white_thunder_tiger_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.WHITE_THUNDER_TIGER, 0xFAFAFA, 0x00E5FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> CLOUD_CRANE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "cloud_crane_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE, 0xFFFFFF, 0xD32F2F, new Item.Properties()));

    public static final DeferredHolder<Item, Item> DOKKAEBI_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "dokkaebi_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.DOKKAEBI, 0x311B92, 0xFFD700, new Item.Properties()));

    public static final DeferredHolder<Item, Item> PAIMON_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "paimon_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.PAIMON, 0xFFFFFF, 0xFFD700, new Item.Properties()));

    public static final DeferredHolder<Item, Item> WYVERN_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "wyvern_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.WYVERN, 0x2E7D32, 0x8E24AA, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANEMO_HYPOSTASIS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "anemo_hypostasis_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ANEMO_HYPOSTASIS, 0x3DE3B8, 0x1A4742, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELECTRO_HYPOSTASIS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "electro_hypostasis_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_HYPOSTASIS, 0x935FE6, 0x311B4D, new Item.Properties()));

    public static final DeferredHolder<Item, Item> GEO_HYPOSTASIS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "geo_hypostasis_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GEO_HYPOSTASIS, 0xD4AF37, 0x5D4037, new Item.Properties()));

    public static final DeferredHolder<Item, Item> CRYO_HYPOSTASIS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "cryo_hypostasis_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.CRYO_HYPOSTASIS, 0x80DEEA, 0x006064, new Item.Properties()));

    public static final DeferredHolder<Item, Item> PYRO_HYPOSTASIS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "pyro_hypostasis_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.PYRO_HYPOSTASIS, 0xFF5722, 0xBF360C, new Item.Properties()));

    public static final DeferredHolder<Item, Item> HYDRO_HYPOSTASIS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "hydro_hypostasis_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.HYDRO_HYPOSTASIS, 0x29B6F6, 0x01579B, new Item.Properties()));

    public static final DeferredHolder<Item, Item> RUIN_GUARD_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ruin_guard_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.RUIN_GUARD, 0x6E5B4B, 0xFF9800, new Item.Properties()));

    public static final DeferredHolder<Item, Item> RUIN_HUNTER_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "ruin_hunter_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.RUIN_HUNTER, 0x4E3B31, 0xFFA726, new Item.Properties()));

    public static final DeferredHolder<Item, Item> STONEHIDE_LAWACHURL_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "stonehide_lawachurl_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.STONEHIDE_LAWACHURL, 0x4A3728, 0xD4AF37, new Item.Properties()));

    public static final DeferredHolder<Item, Item> CRYO_REGISVINE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "cryo_regisvine_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.CRYO_REGISVINE, 0x1A237E, 0x80DEEA, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSS_MAGE_PYRO_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyss_mage_pyro_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_PYRO, 0xB71C1C, 0xFF5722, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSS_MAGE_HYDRO_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyss_mage_hydro_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_HYDRO, 0x0D47A1, 0x29B6F6, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSS_MAGE_CRYO_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyss_mage_cryo_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_CRYO, 0x006064, 0x80DEEA, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ABYSS_MAGE_ELECTRO_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "abyss_mage_electro_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.ABYSS_MAGE_ELECTRO, 0x4A148C, 0xAB47BC, new Item.Properties()));

    // --- MATERIALES DE AUTÓMATAS DE RUINAS (KHAENRI'AH / GENSHIN IMPACT) ---
    public static final DeferredHolder<Item, Item> CHAOS_DEVICE = AwakeningSystemMod.ITEMS.register(
            "chaos_device", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§6§l[Material de Autómata ✦✦]"));
                    tooltip.add(Component.literal("§7Dispositivo del Caos obtenido de un Guardián de las Ruinas."));
                    tooltip.add(Component.literal("§8Estructura de engranajes arcanos proveniente de la antigua Khaenri'ah."));
                }
            });

    public static final DeferredHolder<Item, Item> CHAOS_CIRCUIT = AwakeningSystemMod.ITEMS.register(
            "chaos_circuit", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§5§l[Material de Autómata ✦✦✦]"));
                    tooltip.add(Component.literal("§7Circuito del Caos con energía biomecánica residual."));
                    tooltip.add(Component.literal("§8Canaliza impulsos motrices de los autómatas destructores."));
                }
            });

    public static final DeferredHolder<Item, Item> CHAOS_CORE = AwakeningSystemMod.ITEMS.register(
            "chaos_core", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§d§l[Material de Autómata ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Núcleo del Caos perfecto y palpitante de energía infinita."));
                    tooltip.add(Component.literal("§8El corazón motriz supremo de la maquinaria de guerra de Khaenri'ah."));
                }
            });

    // --- MATERIALES DE LÍNEAS LEY / MAGOS DEL ABISMO (GENSHIN IMPACT) ---
    public static final DeferredHolder<Item, Item> DEAD_LEY_LINE_BRANCH = AwakeningSystemMod.ITEMS.register(
            "dead_ley_line_branch", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§a§l[Material del Abismo ✦✦]"));
                    tooltip.add(Component.literal("§7Ramita seca obtenida de un Mago del Abismo."));
                    tooltip.add(Component.literal("§8Contiene vestigios marchitos de energía telúrica subterránea."));
                }
            });

    public static final DeferredHolder<Item, Item> DEAD_LEY_LINE_LEAVES = AwakeningSystemMod.ITEMS.register(
            "dead_ley_line_leaves", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§9§l[Material del Abismo ✦✦✦]"));
                    tooltip.add(Component.literal("§7Hojas secas de las ramas profundas de las líneas ley."));
                    tooltip.add(Component.literal("§8Canalizan los misterios arcanos de la sabiduría del mundo."));
                }
            });

    public static final DeferredHolder<Item, Item> LEY_LINE_SPROUT = AwakeningSystemMod.ITEMS.register(
            "ley_line_sprout", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§5§l[Material del Abismo ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Brote tierno y resplandeciente de las líneas ley."));
                    tooltip.add(Component.literal("§8Fluye con la corriente vital más pura del árbol que sostiene la creación."));
                }
            });

    // --- MATERIALES DE ASCENSIÓN DE HIPÓSTASIS (GENSHIN IMPACT) ---
    public static final DeferredHolder<Item, Item> HURRICANE_SEED = AwakeningSystemMod.ITEMS.register(
            "hurricane_seed", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§b§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Semilla cilíndrica dejada por el Hipóstasis Anemo."));
                    tooltip.add(Component.literal("§8Contiene el poder de la tempestad primordial que surca los cielos."));
                }
            });

    public static final DeferredHolder<Item, Item> LIGHTNING_PRISM = AwakeningSystemMod.ITEMS.register(
            "lightning_prism", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§d§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Prisma brillante abandonado por el Hipóstasis Electro."));
                    tooltip.add(Component.literal("§8Refracta la energía de relámpago pura y de alta concentración."));
                }
            });

    public static final DeferredHolder<Item, Item> BASALT_PILLAR = AwakeningSystemMod.ITEMS.register(
            "basalt_pillar", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§6§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Pilar de piedra densa caído del Hipóstasis Geo."));
                    tooltip.add(Component.literal("§8El núcleo terrenal imbuido con la gravedad inquebrantable de la tierra."));
                }
            });

    public static final DeferredHolder<Item, Item> CRYSTALLINE_BLOOM = AwakeningSystemMod.ITEMS.register(
            "crystalline_bloom", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§3§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Flor cristalina gélida desprendida del Hipóstasis Cryo."));
                    tooltip.add(Component.literal("§8Emana una escarcha perpetua capaz de congelar el flujo elemental."));
                }
            });

    public static final DeferredHolder<Item, Item> SMOLDERING_PEARL = AwakeningSystemMod.ITEMS.register(
            "smoldering_pearl", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§c§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Perla ardiente obtenida del núcleo del Hipóstasis Pyro."));
                    tooltip.add(Component.literal("§8Arde con una llama inextinguible nacida de la combustión más pura."));
                }
            });

    public static final DeferredHolder<Item, Item> DEW_OF_REPUDIATION = AwakeningSystemMod.ITEMS.register(
            "dew_of_repudiation", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§9§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Gota de agua pristina condensada del Hipóstasis Hydro."));
                    tooltip.add(Component.literal("§8Rechaza cualquier impureza en su corriente y fluye eternamente viva."));
                }
            });

    // --- MATERIALES DE ASCENSIÓN DE REGISVID (GENSHIN IMPACT) ---
    public static final DeferredHolder<Item, Item> HOARFROST_CORE = AwakeningSystemMod.ITEMS.register(
            "hoarfrost_core", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§b§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Núcleo de Escarcha puro desprendido de la Regisvid Criogénica."));
                    tooltip.add(Component.literal("§8Contiene la esencia vegetal sobreenfriada nacida de las líneas ley."));
                }
            });

    public static final DeferredHolder<Item, Item> EVERFLAME_SEED = AwakeningSystemMod.ITEMS.register(
            "everflame_seed", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§c§l[Material de Ascensión ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Semilla de llama inextinguible obtenida de la Regisvid Pyro."));
                    tooltip.add(Component.literal("§8Nutrida por el fuego elemental supremo que arde en la tierra."));
                }
            });

    // --- BLOQUES DE HIPÓSTASIS ELEMENTAL (ITEMS) ---
    public static final DeferredHolder<Item, Item> ANEMO_HYPOSTASIS_BLOCK = AwakeningSystemMod.ITEMS.register(
            "anemo_hypostasis_block", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.ANEMO_HYPOSTASIS_BLOCK.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ELECTRO_HYPOSTASIS_BLOCK = AwakeningSystemMod.ITEMS.register(
            "electro_hypostasis_block", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.ELECTRO_HYPOSTASIS_BLOCK.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> GEO_HYPOSTASIS_BLOCK = AwakeningSystemMod.ITEMS.register(
            "geo_hypostasis_block", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.GEO_HYPOSTASIS_BLOCK.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> CRYO_HYPOSTASIS_BLOCK = AwakeningSystemMod.ITEMS.register(
            "cryo_hypostasis_block", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.CRYO_HYPOSTASIS_BLOCK.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> PYRO_HYPOSTASIS_BLOCK = AwakeningSystemMod.ITEMS.register(
            "pyro_hypostasis_block", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.PYRO_HYPOSTASIS_BLOCK.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> HYDRO_HYPOSTASIS_BLOCK = AwakeningSystemMod.ITEMS.register(
            "hydro_hypostasis_block", () -> new net.minecraft.world.item.BlockItem(com.soybe.awakeningsystem.block.SystemBlocks.HYDRO_HYPOSTASIS_BLOCK.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> WYVERN_SCALE = AwakeningSystemMod.ITEMS.register(
            "wyvern_scale", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§2§l[Material RPG Rango S] §7Escama ultraligera y resistente de depredador aéreo."));
                    tooltip.add(Component.literal("§8Inmune a la corrosión de ácidos y quemaduras."));
                }
            });

    public static final DeferredHolder<Item, Item> WYVERN_FANG = AwakeningSystemMod.ITEMS.register(
            "wyvern_fang", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§a§l[Material RPG] §7Colmillo afilado empapado en ponzoña de wyvern."));
                    tooltip.add(Component.literal("§8Utilizado en la forja de armas con sangrado y veneno."));
                }
            });

    public static final DeferredHolder<Item, Item> WYVERN_EGG = AwakeningSystemMod.ITEMS.register(
            "wyvern_egg", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§6§l[Huevo Mítico de Wyvern] §fRobado de los riscos y nidos más altos."));
                    tooltip.add(Component.literal("§e¡Incúbalo cerca de fogatas o lava para obtener una cría domesticable!"));
                }

                @Override
                public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx) {
                    Level level = ctx.getLevel();
                    if (!level.isClientSide() && ctx.getPlayer() != null) {
                        BlockPos spawnPos = ctx.getClickedPos().relative(ctx.getClickedFace());
                        WyvernEntity wyvern = new WyvernEntity(com.soybe.awakeningsystem.entity.ModEntities.WYVERN.get(), level);
                        wyvern.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                        wyvern.setWyvernAge(0); // Nace como cría bebé
                        wyvern.setVariant(level.random.nextInt(3));
                        level.addFreshEntity(wyvern);
                        if (!ctx.getPlayer().getAbilities().instabuild) {
                            ctx.getItemInHand().shrink(1);
                        }
                        level.playSound(null, spawnPos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
                        ctx.getPlayer().displayClientMessage(Component.literal("§6¡Ha nacido una cría de Wyvern! ¡Aliméntala con Carne de Maná para domesticarla!"), false);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            });

    public static final DeferredHolder<Item, Item> WYVERN_SADDLE = AwakeningSystemMod.ITEMS.register(
            "wyvern_saddle", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§d§l[Arnés de Jinete Aéreo] §fMontura artesanal reforzada con correas de cuero draconiano."));
                    tooltip.add(Component.literal("§bEquípala en un Wyvern domesticado para surcar los cielos a toda velocidad."));
                }
            });

    public static final DeferredHolder<Item, Item> IGRIS_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "igris_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS, 0x8B0000, 0x212121, new Item.Properties()));

    public static final DeferredHolder<Item, Item> BARAN_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "baran_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.BARAN_BOSS, 0xF5F5F5, 0x00E5FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> BLOOD_SECT_DISCIPLE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "blood_sect_disciple_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.BLOOD_SECT_DISCIPLE, 0x3E0A0A, 0xC62828, new Item.Properties()));

    public static final DeferredHolder<Item, Item> BLOOD_PATRIARCH_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "blood_patriarch_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS, 0x1F0000, 0xFF5252, new Item.Properties()));

    public static final DeferredHolder<Item, Item> YGGDRASIL_TREANT_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "yggdrasil_treant_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS, 0x2E1F0F, 0x00E676, new Item.Properties()));

    // === 40 ARTEFACTOS DE TEYVAT (GENSHIN IMPACT) ===
    public static final DeferredHolder<Item, Item> ARTIFACT_CRIMSON_WITCH_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_crimson_witch_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_CRIMSON_WITCH_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_crimson_witch_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_CRIMSON_WITCH_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_crimson_witch_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_CRIMSON_WITCH_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_crimson_witch_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_CRIMSON_WITCH_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_crimson_witch_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.CRIMSON_WITCH,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_GLADIATORS_FINALE_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_gladiator_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_GLADIATORS_FINALE_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_gladiator_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_GLADIATORS_FINALE_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_gladiator_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_GLADIATORS_FINALE_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_gladiator_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_GLADIATORS_FINALE_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_gladiator_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.GLADIATORS_FINALE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_VIRIDESCENT_VENERER_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_viridescent_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_VIRIDESCENT_VENERER_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_viridescent_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_VIRIDESCENT_VENERER_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_viridescent_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_VIRIDESCENT_VENERER_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_viridescent_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_VIRIDESCENT_VENERER_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_viridescent_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.VIRIDESCENT_VENERER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_MAIDEN_BELOVED_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_maiden_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_MAIDEN_BELOVED_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_maiden_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_MAIDEN_BELOVED_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_maiden_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_MAIDEN_BELOVED_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_maiden_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_MAIDEN_BELOVED_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_maiden_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.MAIDEN_BELOVED,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BLOODSTAINED_CHIVALRY_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_bloodstained_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BLOODSTAINED_CHIVALRY,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BLOODSTAINED_CHIVALRY_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_bloodstained_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BLOODSTAINED_CHIVALRY,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BLOODSTAINED_CHIVALRY_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_bloodstained_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BLOODSTAINED_CHIVALRY,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BLOODSTAINED_CHIVALRY_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_bloodstained_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BLOODSTAINED_CHIVALRY,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BLOODSTAINED_CHIVALRY_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_bloodstained_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BLOODSTAINED_CHIVALRY,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BERSERKER_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_berserker_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BERSERKER_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_berserker_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BERSERKER_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_berserker_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BERSERKER_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_berserker_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BERSERKER_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_berserker_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BERSERKER,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_THE_EXILE_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_exile_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.THE_EXILE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_THE_EXILE_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_exile_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.THE_EXILE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_THE_EXILE_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_exile_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.THE_EXILE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_THE_EXILE_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_exile_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.THE_EXILE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_THE_EXILE_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_exile_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.THE_EXILE,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BRAVE_HEART_FLOWER = AwakeningSystemMod.ITEMS.register(
            "artifact_brave_heart_flower", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART,
                    com.soybe.awakeningsystem.artifact.ArtifactType.FLOWER,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BRAVE_HEART_PLUME = AwakeningSystemMod.ITEMS.register(
            "artifact_brave_heart_plume", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART,
                    com.soybe.awakeningsystem.artifact.ArtifactType.PLUME,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BRAVE_HEART_SANDS = AwakeningSystemMod.ITEMS.register(
            "artifact_brave_heart_sands", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART,
                    com.soybe.awakeningsystem.artifact.ArtifactType.SANDS,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BRAVE_HEART_GOBLET = AwakeningSystemMod.ITEMS.register(
            "artifact_brave_heart_goblet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART,
                    com.soybe.awakeningsystem.artifact.ArtifactType.GOBLET,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARTIFACT_BRAVE_HEART_CIRCLET = AwakeningSystemMod.ITEMS.register(
            "artifact_brave_heart_circlet", () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(
                    com.soybe.awakeningsystem.artifact.ArtifactSet.BRAVE_HEART,
                    com.soybe.awakeningsystem.artifact.ArtifactType.CIRCLET,
                    new Item.Properties()));

    // === 28 GEMAS ELEMENTALES DE ASCENSIÓN ===
    public static final DeferredHolder<Item, Item> AGNIDUS_AGATE_SLIVER = AwakeningSystemMod.ITEMS.register(
            "agnidus_agate_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Pyro", "Pedacito", "§c", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> AGNIDUS_AGATE_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "agnidus_agate_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Pyro", "Fragmento", "§c", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> AGNIDUS_AGATE_CHUNK = AwakeningSystemMod.ITEMS.register(
            "agnidus_agate_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Pyro", "Trozo", "§c", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> AGNIDUS_AGATE_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "agnidus_agate_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Pyro", "Gema", "§c", 5, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SHIVADA_JADE_SLIVER = AwakeningSystemMod.ITEMS.register(
            "shivada_jade_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Cryo", "Pedacito", "§b", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SHIVADA_JADE_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "shivada_jade_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Cryo", "Fragmento", "§b", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SHIVADA_JADE_CHUNK = AwakeningSystemMod.ITEMS.register(
            "shivada_jade_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Cryo", "Trozo", "§b", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SHIVADA_JADE_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "shivada_jade_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Cryo", "Gema", "§b", 5, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAYUDA_TURQUOISE_SLIVER = AwakeningSystemMod.ITEMS.register(
            "vayuda_turquoise_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Anemo", "Pedacito", "§a", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAYUDA_TURQUOISE_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "vayuda_turquoise_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Anemo", "Fragmento", "§a", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAYUDA_TURQUOISE_CHUNK = AwakeningSystemMod.ITEMS.register(
            "vayuda_turquoise_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Anemo", "Trozo", "§a", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAYUDA_TURQUOISE_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "vayuda_turquoise_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Anemo", "Gema", "§a", 5, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VARUNADA_LAZURITE_SLIVER = AwakeningSystemMod.ITEMS.register(
            "varunada_lazurite_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Hydro", "Pedacito", "§9", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VARUNADA_LAZURITE_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "varunada_lazurite_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Hydro", "Fragmento", "§9", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VARUNADA_LAZURITE_CHUNK = AwakeningSystemMod.ITEMS.register(
            "varunada_lazurite_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Hydro", "Trozo", "§9", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VARUNADA_LAZURITE_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "varunada_lazurite_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Hydro", "Gema", "§9", 5, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PRITHIVA_TOPAZ_SLIVER = AwakeningSystemMod.ITEMS.register(
            "prithiva_topaz_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Geo", "Pedacito", "§6", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PRITHIVA_TOPAZ_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "prithiva_topaz_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Geo", "Fragmento", "§6", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PRITHIVA_TOPAZ_CHUNK = AwakeningSystemMod.ITEMS.register(
            "prithiva_topaz_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Geo", "Trozo", "§6", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PRITHIVA_TOPAZ_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "prithiva_topaz_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Geo", "Gema", "§6", 5, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NAGADUS_EMERALD_SLIVER = AwakeningSystemMod.ITEMS.register(
            "nagadus_emerald_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Dendro", "Pedacito", "§2", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NAGADUS_EMERALD_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "nagadus_emerald_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Dendro", "Fragmento", "§2", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NAGADUS_EMERALD_CHUNK = AwakeningSystemMod.ITEMS.register(
            "nagadus_emerald_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Dendro", "Trozo", "§2", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NAGADUS_EMERALD_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "nagadus_emerald_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Dendro", "Gema", "§2", 5, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAJRADA_AMETHYST_SLIVER = AwakeningSystemMod.ITEMS.register(
            "vajrada_amethyst_sliver", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Electro", "Pedacito", "§d", 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAJRADA_AMETHYST_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "vajrada_amethyst_fragment", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Electro", "Fragmento", "§d", 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAJRADA_AMETHYST_CHUNK = AwakeningSystemMod.ITEMS.register(
            "vajrada_amethyst_chunk", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Electro", "Trozo", "§d", 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VAJRADA_AMETHYST_GEMSTONE = AwakeningSystemMod.ITEMS.register(
            "vajrada_amethyst_gemstone", () -> new com.soybe.awakeningsystem.item.ElementalGemItem(
                    "Electro", "Gema", "§d", 5, new Item.Properties()));

    // === MATERIALES DE MEJORA, ECONOMÍA Y COMIDA DE TEYVAT ===
    public static final DeferredHolder<Item, Item> MYSTIC_ENHANCEMENT_ORE = AwakeningSystemMod.ITEMS.register(
            "mystic_enhancement_ore", () -> new com.soybe.awakeningsystem.item.EnhancementOreItem(10000, 4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> FINE_ENHANCEMENT_ORE = AwakeningSystemMod.ITEMS.register(
            "fine_enhancement_ore", () -> new com.soybe.awakeningsystem.item.EnhancementOreItem(2000, 3, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ENHANCEMENT_ORE = AwakeningSystemMod.ITEMS.register(
            "enhancement_ore", () -> new com.soybe.awakeningsystem.item.EnhancementOreItem(400, 2, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CROWN_OF_INSIGHT = AwakeningSystemMod.ITEMS.register(
            "crown_of_insight", () -> new com.soybe.awakeningsystem.item.CrownOfInsightItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MORA_POUCH = AwakeningSystemMod.ITEMS.register(
            "mora_pouch", () -> new com.soybe.awakeningsystem.item.MoraPouchItem(10000, "Bolsa de Mora del Aventurero", new Item.Properties()));
    public static final DeferredHolder<Item, Item> MORA_BUNDLE = AwakeningSystemMod.ITEMS.register(
            "mora_bundle", () -> new com.soybe.awakeningsystem.item.MoraPouchItem(50000, "Fardo Dorado de Mora", new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADVENTURERS_EXPERIENCE = AwakeningSystemMod.ITEMS.register(
            "adventurers_experience", () -> new com.soybe.awakeningsystem.item.HerosWitItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> CRYSTAL_CORE = AwakeningSystemMod.ITEMS.register(
            "crystal_core", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ADEPTUS_TEMPTATION = AwakeningSystemMod.ITEMS.register(
            "adeptus_temptation", () -> new com.soybe.awakeningsystem.item.TeyvatFoodItem(com.soybe.awakeningsystem.item.TeyvatFoodItem.FoodType.ADEPTUS_TEMPTATION, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SWEET_MADAME = AwakeningSystemMod.ITEMS.register(
            "sweet_madame", () -> new com.soybe.awakeningsystem.item.TeyvatFoodItem(com.soybe.awakeningsystem.item.TeyvatFoodItem.FoodType.SWEET_MADAME, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALMOND_TOFU = AwakeningSystemMod.ITEMS.register(
            "almond_tofu", () -> new com.soybe.awakeningsystem.item.TeyvatFoodItem(com.soybe.awakeningsystem.item.TeyvatFoodItem.FoodType.ALMOND_TOFU, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SUNSETTIA = AwakeningSystemMod.ITEMS.register(
            "sunsettia", () -> new com.soybe.awakeningsystem.item.TeyvatFoodItem(com.soybe.awakeningsystem.item.TeyvatFoodItem.FoodType.SUNSETTIA, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANA_FRUIT = AwakeningSystemMod.ITEMS.register(
            "mana_fruit", () -> new ManaFoodItem(
                    new Item.Properties().rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build()),
                    25, new MobEffectInstance(MobEffects.REGENERATION, 300, 1)));

    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_FRUIT = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_fruit", () -> new com.soybe.awakeningsystem.item.SpiritPlumFruitItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_NUT = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_nut", () -> new com.soybe.awakeningsystem.item.CelestialPineNutItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IMMORTALITY_PEACH = AwakeningSystemMod.ITEMS.register(
            "immortality_peach", () -> new com.soybe.awakeningsystem.item.HeavenlyLongevityPeachItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELDORIA_FRUIT = AwakeningSystemMod.ITEMS.register(
            "eldoria_fruit", () -> new com.soybe.awakeningsystem.item.EldoriaFruitItem(new Item.Properties()));

    // Materiales auténticos de Teyvat (Resinas, Slimes y Hilichurls)
    public static final DeferredHolder<Item, Item> ORIGINAL_RESIN = AwakeningSystemMod.ITEMS.register(
            "original_resin", () -> new com.soybe.awakeningsystem.item.OriginalResinItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> TRANSIENT_RESIN = AwakeningSystemMod.ITEMS.register(
            "transient_resin", () -> new com.soybe.awakeningsystem.item.TransientResinItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> VISIONS_MENU = AwakeningSystemMod.ITEMS.register(
            "visions_menu", () -> new com.soybe.awakeningsystem.item.VisionsMenuItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> CONDENSED_RESIN = AwakeningSystemMod.ITEMS.register(
            "condensed_resin", () -> new com.soybe.awakeningsystem.item.CondensedResinItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> SLIME_CONDENSATE = AwakeningSystemMod.ITEMS.register(
            "slime_condensate", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));

    public static final DeferredHolder<Item, Item> SLIME_SECRETIONS = AwakeningSystemMod.ITEMS.register(
            "slime_secretions", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> SLIME_CONCENTRATE = AwakeningSystemMod.ITEMS.register(
            "slime_concentrate", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> DAMAGED_MASK = AwakeningSystemMod.ITEMS.register(
            "damaged_mask", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));

    public static final DeferredHolder<Item, Item> STAINED_MASK = AwakeningSystemMod.ITEMS.register(
            "stained_mask", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> OMINOUS_MASK = AwakeningSystemMod.ITEMS.register(
            "ominous_mask", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> HEAVY_HORN = AwakeningSystemMod.ITEMS.register(
            "heavy_horn", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§a§l[Material de Mitachurl ✦✦]"));
                    tooltip.add(Component.literal("§7Un cuerno tosco y curvado arrancado de un Mitachurl."));
                    tooltip.add(Component.literal("§8Se suele emplear como bocina de combate en los campamentos Hilichurl."));
                }
            });

    public static final DeferredHolder<Item, Item> BLACK_BRONZE_HORN = AwakeningSystemMod.ITEMS.register(
            "black_bronze_horn", () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§9§l[Material de Mitachurl ✦✦✦]"));
                    tooltip.add(Component.literal("§7Un cuerno metálico teñido de un bronce oscuro y amenazante."));
                    tooltip.add(Component.literal("§8Simboliza el estatus de capitán y combatiente veterano entre los suyos."));
                }
            });

    public static final DeferredHolder<Item, Item> BLACK_CRYSTAL_HORN = AwakeningSystemMod.ITEMS.register(
            "black_crystal_horn", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("§d§l[Material de Lawachurl ✦✦✦✦]"));
                    tooltip.add(Component.literal("§7Cuerno colosal cristalizado con vetas de energía elemental pura."));
                    tooltip.add(Component.literal("§8Perteneciente al cacique supremo Lawachurl Acorazado."));
                }
            });

    // Bloques de PrimogemCraft (Mora maciza y Vasija de Tesoro)
    public static final DeferredHolder<Item, Item> MORA_BLOCK = AwakeningSystemMod.ITEMS.register(
            "mora_block", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MORA_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> TEYVAT_JAR = AwakeningSystemMod.ITEMS.register(
            "teyvat_jar", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.TEYVAT_JAR.get(), new Item.Properties().rarity(Rarity.COMMON)));

    // Bolsas de Almacenamiento Gigante de PrimogemCraft
    public static final DeferredHolder<Item, Item> WANDERER_POUCH = AwakeningSystemMod.ITEMS.register(
            "wanderer_pouch", () -> new com.soybe.awakeningsystem.item.pouch.PouchItem(com.soybe.awakeningsystem.item.pouch.PouchTier.WANDERER));

    public static final DeferredHolder<Item, Item> ADVENTURER_POUCH = AwakeningSystemMod.ITEMS.register(
            "adventurer_pouch", () -> new com.soybe.awakeningsystem.item.pouch.PouchItem(com.soybe.awakeningsystem.item.pouch.PouchTier.ADVENTURER));

    public static final DeferredHolder<Item, Item> OUTLANDER_POUCH = AwakeningSystemMod.ITEMS.register(
            "outlander_pouch", () -> new com.soybe.awakeningsystem.item.pouch.PouchItem(com.soybe.awakeningsystem.item.pouch.PouchTier.OUTLANDER));

    public static final DeferredHolder<Item, Item> EMERGENCY_POUCH = AwakeningSystemMod.ITEMS.register(
            "emergency_pouch", () -> new com.soybe.awakeningsystem.item.pouch.PouchItem(com.soybe.awakeningsystem.item.pouch.PouchTier.EMERGENCY));

    // Herramientas y Armas Primordiales de Teyvat (PrimogemCraft)
    public static final DeferredHolder<Item, Item> PRIMOGEM_PICKAXE = AwakeningSystemMod.ITEMS.register(
            "primogem_pickaxe", () -> new com.soybe.awakeningsystem.item.PrimogemPickaxeItem());

    public static final DeferredHolder<Item, Item> PRIMOGEM_SWORD = AwakeningSystemMod.ITEMS.register(
            "primogem_sword", () -> new com.soybe.awakeningsystem.item.PrimogemSwordItem());

    // Armadura de Mora Áurea
    public static final DeferredHolder<Item, Item> MORA_HELMET = AwakeningSystemMod.ITEMS.register(
            "mora_helmet", () -> new com.soybe.awakeningsystem.item.ManhwaArmorItem(
                    com.soybe.awakeningsystem.item.ModArmorMaterials.MORA, net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(40)),
                    com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MORA));

    public static final DeferredHolder<Item, Item> MORA_CHESTPLATE = AwakeningSystemMod.ITEMS.register(
            "mora_chestplate", () -> new com.soybe.awakeningsystem.item.ManhwaArmorItem(
                    com.soybe.awakeningsystem.item.ModArmorMaterials.MORA, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(40)),
                    com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MORA));

    public static final DeferredHolder<Item, Item> MORA_LEGGINGS = AwakeningSystemMod.ITEMS.register(
            "mora_leggings", () -> new com.soybe.awakeningsystem.item.ManhwaArmorItem(
                    com.soybe.awakeningsystem.item.ModArmorMaterials.MORA, net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(40)),
                    com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MORA));

    public static final DeferredHolder<Item, Item> MORA_BOOTS = AwakeningSystemMod.ITEMS.register(
            "mora_boots", () -> new com.soybe.awakeningsystem.item.ManhwaArmorItem(
                    com.soybe.awakeningsystem.item.ModArmorMaterials.MORA, net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(40)),
                    com.soybe.awakeningsystem.item.ManhwaArmorItem.SetType.MORA));

    // Curios Legendarios del Universo Simulado (PrimogemCraft)
    public static final DeferredHolder<Item, Item> OMNISCIENCE_POUCH = AwakeningSystemMod.ITEMS.register(
            "omniscience_pouch", () -> new com.soybe.awakeningsystem.item.OmnisciencePouchItem());

    public static final DeferredHolder<Item, Item> DOCTORS_ROBE = AwakeningSystemMod.ITEMS.register(
            "doctors_robe", () -> new com.soybe.awakeningsystem.item.DoctorsRobeItem());

    // ========================================================
    // MADERAS Y ÁRBOLES CANÓNICOS DE TEYVAT (GENSHIN IMPACT)
    // ========================================================
    public static final DeferredHolder<Item, Item> CUIHUA_LOG = AwakeningSystemMod.ITEMS.register(
            "cuihua_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_LEAVES = AwakeningSystemMod.ITEMS.register(
            "cuihua_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_SAPLING = AwakeningSystemMod.ITEMS.register(
            "cuihua_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_PLANKS = AwakeningSystemMod.ITEMS.register(
            "cuihua_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_PLANKS.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> ADHIGAMA_LOG = AwakeningSystemMod.ITEMS.register(
            "adhigama_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_LEAVES = AwakeningSystemMod.ITEMS.register(
            "adhigama_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_SAPLING = AwakeningSystemMod.ITEMS.register(
            "adhigama_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_PLANKS = AwakeningSystemMod.ITEMS.register(
            "adhigama_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_PLANKS.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> SANDBEARER_LOG = AwakeningSystemMod.ITEMS.register(
            "sandbearer_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_LEAVES = AwakeningSystemMod.ITEMS.register(
            "sandbearer_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_SAPLING = AwakeningSystemMod.ITEMS.register(
            "sandbearer_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_PLANKS = AwakeningSystemMod.ITEMS.register(
            "sandbearer_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_PLANKS.get(), new Item.Properties()));

    // --- MADERAS Y ÁRBOLES CANÓNICOS DE ISEKAI ---
    public static final DeferredHolder<Item, Item> MANA_WOOD_LOG = AwakeningSystemMod.ITEMS.register(
            "mana_wood_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MANA_WOOD_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANA_WOOD_LEAVES = AwakeningSystemMod.ITEMS.register(
            "mana_wood_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MANA_WOOD_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANA_WOOD_SAPLING = AwakeningSystemMod.ITEMS.register(
            "mana_wood_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MANA_WOOD_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANA_WOOD_PLANKS = AwakeningSystemMod.ITEMS.register(
            "mana_wood_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MANA_WOOD_PLANKS.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELDORIA_LOG = AwakeningSystemMod.ITEMS.register(
            "eldoria_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELDORIA_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELDORIA_LEAVES = AwakeningSystemMod.ITEMS.register(
            "eldoria_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELDORIA_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELDORIA_SAPLING = AwakeningSystemMod.ITEMS.register(
            "eldoria_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELDORIA_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELDORIA_PLANKS = AwakeningSystemMod.ITEMS.register(
            "eldoria_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELDORIA_PLANKS.get(), new Item.Properties()));

    // --- MADERAS Y ÁRBOLES CANÓNICOS DE MURIM (CULTIVO) ---
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_LOG = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_LEAVES = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_SAPLING = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_PLANKS = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_STAIRS = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_stairs", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_SLAB = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_slab", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_FENCE = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_fence", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_FENCE_GATE = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_fence_gate", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_DOOR = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_door", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPIRIT_PLUM_TRAPDOOR = AwakeningSystemMod.ITEMS.register(
            "spirit_plum_trapdoor", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SPIRIT_PLUM_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_LOG = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_log", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_LEAVES = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_SAPLING = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_sapling", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_PLANKS = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_planks", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_STAIRS = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_stairs", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_SLAB = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_slab", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_FENCE = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_fence", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_FENCE_GATE = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_fence_gate", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_DOOR = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_door", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CELESTIAL_PINE_TRAPDOOR = AwakeningSystemMod.ITEMS.register(
            "celestial_pine_trapdoor", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CELESTIAL_PINE_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> IMMORTALITY_PEACH_LEAVES = AwakeningSystemMod.ITEMS.register(
            "immortality_peach_leaves", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.IMMORTALITY_PEACH_LEAVES.get(), new Item.Properties()));

    // --- ESPADA SOUL SOLID (BROOK) ---
    public static final DeferredHolder<Item, Item> SOUL_SOLID = AwakeningSystemMod.ITEMS.register(
            "soul_solid", () -> new SoulSolidSwordItem(new Item.Properties().stacksTo(1)));

    // --- NUEVAS PÍLDORAS DE CULTIVO Y CAMBIO DE CLASE ---
    public static final DeferredHolder<Item, Item> NINE_REVOLUTIONS_PILL = AwakeningSystemMod.ITEMS.register(
            "nine_revolutions_pill", () -> new NineRevolutionsPillItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> CRIMSON_PHOENIX_PILL = AwakeningSystemMod.ITEMS.register(
            "crimson_phoenix_pill", () -> new CrimsonPhoenixPillItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> WHITE_TIGER_PILL = AwakeningSystemMod.ITEMS.register(
            "white_tiger_pill", () -> new WhiteTigerPillItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> HEAVENLY_ENLIGHTENMENT_PILL = AwakeningSystemMod.ITEMS.register(
            "heavenly_enlightenment_pill", () -> new HeavenlyEnlightenmentPillItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> FOUNDATION_BUILDING_PILL = AwakeningSystemMod.ITEMS.register(
            "foundation_building_pill", () -> new FoundationBuildingPillItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> PRIMORDIAL_REBIRTH_PILL = AwakeningSystemMod.ITEMS.register(
            "primordial_rebirth_pill", () -> new PillOfPrimordialRebirthItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // --- OBJETOS DE JEFE WILDFIRE ---
    public static final DeferredHolder<Item, Item> WILDFIRE_CROWN = AwakeningSystemMod.ITEMS.register(
            "wildfire_crown", () -> new WildfireCrownItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> WILDFIRE_CROWN_FRAGMENT = AwakeningSystemMod.ITEMS.register(
            "wildfire_crown_fragment", () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));

    // --- HUEVOS DE GENERACIÓN ADICIONALES ---
    public static final DeferredHolder<Item, Item> WILDFIRE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "wildfire_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.WILDFIRE, 0xE65100, 0xFFEB3B, new Item.Properties()));
    public static final DeferredHolder<Item, Item> LUNAR_SHADOW_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "lunar_shadow_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.LUNAR_SHADOW, 0x1A237E, 0x80D8FF, new Item.Properties()));
    public static final DeferredHolder<Item, Item> LUNAR_GUARDIAN_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "lunar_guardian_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.LUNAR_GUARDIAN, 0x311B92, 0xFFD700, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GLARE_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "glare_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GLARE, 0x43A047, 0xFDD835, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CHILDE_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "childe_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.CHILDE_BOSS, 0x1E88E5, 0xD32F2F, new Item.Properties()));
    public static final DeferredHolder<Item, Item> AZHDAHA_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "azhdaha_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.AZHDAHA_BOSS, 0x5D4037, 0xFFB300, new Item.Properties()));
    public static final DeferredHolder<Item, Item> RAIDEN_SHOGUN_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "raiden_shogun_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.RAIDEN_SHOGUN_BOSS, 0x6A1B9A, 0xBA68C8, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GOLDEN_WOLFLORD_BOSS_SPAWN_EGG = AwakeningSystemMod.ITEMS.register(
            "golden_wolflord_boss_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
                    com.soybe.awakeningsystem.entity.ModEntities.GOLDEN_WOLFLORD_BOSS, 0xF57F17, 0xFFD54F, new Item.Properties()));

    // Materiales de Jefes Semanales y Mundiales
    public static final DeferredHolder<Item, Item> MUDRA_OF_THE_MALEFIC_GENERAL = AwakeningSystemMod.ITEMS.register(
            "mudra_of_the_malefic_general", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> RIFTBORN_REGALIA = AwakeningSystemMod.ITEMS.register(
            "riftborn_regalia", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> GOLDEN_WOLFLORD_CORE = AwakeningSystemMod.ITEMS.register(
            "golden_wolflord_core", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    // --- MESA DE ASCENSIÓN CELESTIAL DE COMPAÑEROS ---
    public static final DeferredHolder<Item, Item> COMPANION_ASCENSION_TABLE = AwakeningSystemMod.ITEMS.register(
            "companion_ascension_table", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.COMPANION_ASCENSION_TABLE.get(), new Item.Properties()));

    // ==========================================
    // ✦ ENGAÑOS DE LOS FATUI (DELUSIONS) ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> PYRO_DELUSION = AwakeningSystemMod.ITEMS.register("pyrodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.PYRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> HYDRO_DELUSION = AwakeningSystemMod.ITEMS.register("hydrodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.HYDRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYO_DELUSION = AwakeningSystemMod.ITEMS.register("cryodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.CRYO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELECTRO_DELUSION = AwakeningSystemMod.ITEMS.register("electrodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.ELECTRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ANEMO_DELUSION = AwakeningSystemMod.ITEMS.register("anemodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.ANEMO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GEO_DELUSION = AwakeningSystemMod.ITEMS.register("geodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.GEO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> DENDRO_DELUSION = AwakeningSystemMod.ITEMS.register("dendrodelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.DENDRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> BLANK_DELUSION = AwakeningSystemMod.ITEMS.register("blankdelusion",
            () -> new com.soybe.awakeningsystem.item.TeyvatDelusionItem(com.soybe.awakeningsystem.item.TeyvatDelusionItem.DelusionElement.BLANK, new Item.Properties()));
    public static final DeferredHolder<Item, Item> DELUSION_FRAGMENT = AwakeningSystemMod.ITEMS.register("delusion_fragment",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    // ==========================================
    // ✦ SIGILOS ELEMENTALES DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> ANEMO_SIGIL = AwakeningSystemMod.ITEMS.register("anemosigil",
            () -> new com.soybe.awakeningsystem.item.TeyvatSigilItem(com.soybe.awakeningsystem.item.TeyvatSigilItem.SigilType.ANEMO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GEO_SIGIL = AwakeningSystemMod.ITEMS.register("geosigil",
            () -> new com.soybe.awakeningsystem.item.TeyvatSigilItem(com.soybe.awakeningsystem.item.TeyvatSigilItem.SigilType.GEO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELECTRO_SIGIL = AwakeningSystemMod.ITEMS.register("electrosigil",
            () -> new com.soybe.awakeningsystem.item.TeyvatSigilItem(com.soybe.awakeningsystem.item.TeyvatSigilItem.SigilType.ELECTRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> DENDRO_SIGIL = AwakeningSystemMod.ITEMS.register("dendrosigil",
            () -> new com.soybe.awakeningsystem.item.TeyvatSigilItem(com.soybe.awakeningsystem.item.TeyvatSigilItem.SigilType.DENDRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GUARDIANS_SIGIL = AwakeningSystemMod.ITEMS.register("guardianssigil",
            () -> new com.soybe.awakeningsystem.item.TeyvatSigilItem(com.soybe.awakeningsystem.item.TeyvatSigilItem.SigilType.GUARDIAN, new Item.Properties()));
    public static final DeferredHolder<Item, Item> LIGHT_REALM_SIGIL = AwakeningSystemMod.ITEMS.register("lightrealmsigil",
            () -> new com.soybe.awakeningsystem.item.TeyvatSigilItem(com.soybe.awakeningsystem.item.TeyvatSigilItem.SigilType.LIGHT_REALM, new Item.Properties()));

    // ==========================================
    // ✦ LLAVES DE SANTUARIOS DE LAS PROFUNDIDADES ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> ANEMO_SHRINE_KEY = AwakeningSystemMod.ITEMS.register("anemo_shrine_of_depths_key",
            () -> new com.soybe.awakeningsystem.item.TeyvatShrineKeyItem(com.soybe.awakeningsystem.item.TeyvatShrineKeyItem.ShrineRegion.ANEMO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GEO_SHRINE_KEY = AwakeningSystemMod.ITEMS.register("geo_shrine_of_depths_key",
            () -> new com.soybe.awakeningsystem.item.TeyvatShrineKeyItem(com.soybe.awakeningsystem.item.TeyvatShrineKeyItem.ShrineRegion.GEO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELECTRO_SHRINE_KEY = AwakeningSystemMod.ITEMS.register("electro_shrine_of_depths_key",
            () -> new com.soybe.awakeningsystem.item.TeyvatShrineKeyItem(com.soybe.awakeningsystem.item.TeyvatShrineKeyItem.ShrineRegion.ELECTRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> DENDRO_SHRINE_KEY = AwakeningSystemMod.ITEMS.register("dendro_shrine_of_depths_key",
            () -> new com.soybe.awakeningsystem.item.TeyvatShrineKeyItem(com.soybe.awakeningsystem.item.TeyvatShrineKeyItem.ShrineRegion.DENDRO, new Item.Properties()));

    // ==========================================
    // ✦ ÓCULOS Y PIEDRAS DE RESONANCIA ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> ANEMOCULUS = AwakeningSystemMod.ITEMS.register("anemoculus",
            () -> new com.soybe.awakeningsystem.item.OculusItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.ANEMO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GEOCULUS = AwakeningSystemMod.ITEMS.register("geoculus",
            () -> new com.soybe.awakeningsystem.item.OculusItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.GEO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELECTROCULUS = AwakeningSystemMod.ITEMS.register("electroculus",
            () -> new com.soybe.awakeningsystem.item.OculusItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.ELECTRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> DENDROCULUS = AwakeningSystemMod.ITEMS.register("dendroculus",
            () -> new com.soybe.awakeningsystem.item.OculusItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.DENDRO, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANEMOCULUS_RESONANCE_STONE = AwakeningSystemMod.ITEMS.register("anemoculus_resonance_stone",
            () -> new com.soybe.awakeningsystem.item.OculusResonanceStoneItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.ANEMO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GEOCULUS_RESONANCE_STONE = AwakeningSystemMod.ITEMS.register("geoculus_resonance_stone",
            () -> new com.soybe.awakeningsystem.item.OculusResonanceStoneItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.GEO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ELECTROCULUS_RESONANCE_STONE = AwakeningSystemMod.ITEMS.register("electroculus_resonance_stone",
            () -> new com.soybe.awakeningsystem.item.OculusResonanceStoneItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.ELECTRO, new Item.Properties()));
    public static final DeferredHolder<Item, Item> DENDROCULUS_RESONANCE_STONE = AwakeningSystemMod.ITEMS.register("dendroculus_resonance_stone",
            () -> new com.soybe.awakeningsystem.item.OculusResonanceStoneItem(com.soybe.awakeningsystem.item.OculusItem.OculusElement.DENDRO, new Item.Properties()));

    // ==========================================
    // ✦ PROTOTIPOS DE FORJA (BILLETS) ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> NORTHLANDER_SWORD_BILLET = AwakeningSystemMod.ITEMS.register("northlanderswordbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.NORTHLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.SWORD, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NORTHLANDER_BOW_BILLET = AwakeningSystemMod.ITEMS.register("northlanderbowbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.NORTHLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.BOW, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NORTHLANDER_CLAYMORE_BILLET = AwakeningSystemMod.ITEMS.register("northlanderclaymorebillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.NORTHLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.CLAYMORE, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NORTHLANDER_CATALYST_BILLET = AwakeningSystemMod.ITEMS.register("northlandercatalystbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.NORTHLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.CATALYST, new Item.Properties()));
    public static final DeferredHolder<Item, Item> NORTHLANDER_POLEARM_BILLET = AwakeningSystemMod.ITEMS.register("northlanderpolearmbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.NORTHLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.POLEARM, new Item.Properties()));

    public static final DeferredHolder<Item, Item> MIDLANDER_SWORD_BILLET = AwakeningSystemMod.ITEMS.register("midlanderswordbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.MIDLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.SWORD, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MIDLANDER_BOW_BILLET = AwakeningSystemMod.ITEMS.register("midlanderbowbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.MIDLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.BOW, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MIDLANDER_CLAYMORE_BILLET = AwakeningSystemMod.ITEMS.register("midlanderclaymorebillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.MIDLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.CLAYMORE, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MIDLANDER_CATALYST_BILLET = AwakeningSystemMod.ITEMS.register("midlandercatalystbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.MIDLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.CATALYST, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MIDLANDER_POLEARM_BILLET = AwakeningSystemMod.ITEMS.register("midlanderpolearmbillet",
            () -> new com.soybe.awakeningsystem.item.ForgeBilletItem(com.soybe.awakeningsystem.item.ForgeBilletItem.BilletSeries.MIDLANDER, com.soybe.awakeningsystem.item.ForgeBilletItem.WeaponCategory.POLEARM, new Item.Properties()));

    // ==========================================
    // ✦ PECES Y PESCA DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> MEDAKA = AwakeningSystemMod.ITEMS.register("medaka_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.MEDAKA, new Item.Properties()));
    public static final DeferredHolder<Item, Item> AIZEN_MEDAKA = AwakeningSystemMod.ITEMS.register("aizen_medaka_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.AIZEN_MEDAKA, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SWEET_FLOWER_MEDAKA = AwakeningSystemMod.ITEMS.register("sweet_flower_medaka_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.SWEET_FLOWER_MEDAKA, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GLAZE_MEDAKA = AwakeningSystemMod.ITEMS.register("glaze_medaka_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.GLAZE_MEDAKA, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYSTALFISH = AwakeningSystemMod.ITEMS.register("crystalfish_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.CRYSTALFISH, new Item.Properties()));
    public static final DeferredHolder<Item, Item> VENOMSPINE_FISH = AwakeningSystemMod.ITEMS.register("venomspine_fish_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.VENOMSPINE_FISH, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ABIDING_ANGELFISH = AwakeningSystemMod.ITEMS.register("abiding_angelfish_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.ABIDING_ANGELFISH, new Item.Properties()));
    public static final DeferredHolder<Item, Item> RAIMEI_ANGELFISH = AwakeningSystemMod.ITEMS.register("raimei_angelfish_item",
            () -> new com.soybe.awakeningsystem.item.TeyvatFishItem(com.soybe.awakeningsystem.item.TeyvatFishItem.FishVariety.RAIMEI_ANGELFISH, new Item.Properties()));
    public static final DeferredHolder<Item, Item> FISHING_LINE_STABILIZER = AwakeningSystemMod.ITEMS.register("fishing_line_stabilizer",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    // Cebos oficiales de Teyvat (Genshin Nature)
    public static final DeferredHolder<Item, Item> FRUIT_PASTE_BAIT = AwakeningSystemMod.ITEMS.register("fruit_paste_bait",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> REDROT_BAIT = AwakeningSystemMod.ITEMS.register("redrot_bait",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> FALSE_WORM_BAIT = AwakeningSystemMod.ITEMS.register("false_worm_bait",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> FAKE_FLY_BAIT = AwakeningSystemMod.ITEMS.register("fake_fly_bait",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> GLOWGRASS_BAIT = AwakeningSystemMod.ITEMS.register("glowgrass_bait",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SUGARDEW_BAIT = AwakeningSystemMod.ITEMS.register("sugardew_bait",
            () -> new Item(new Item.Properties()));

    // ==========================================
    // ✦ GASTRONOMÍA ADICIONAL DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> GRILLED_TIGER_FISH = AwakeningSystemMod.ITEMS.register("grilled_tiger_fish",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
    public static final DeferredHolder<Item, Item> CALLA_LILY_SEAFOOD_SOUP = AwakeningSystemMod.ITEMS.register("calla_lily_seafood_soup",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(7).saturationModifier(0.8F).build())));
    public static final DeferredHolder<Item, Item> FRIED_RADISH_BALLS = AwakeningSystemMod.ITEMS.register("fried_radish_balls",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).build())));
    public static final DeferredHolder<Item, Item> OMINOUS_DISH = AwakeningSystemMod.ITEMS.register("ominous_dish",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).build())));
    public static final DeferredHolder<Item, Item> RADISH = AwakeningSystemMod.ITEMS.register("radish",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())));

    // ==========================================
    // ✦ MINERALES DE TEYVAT (ITEMS Y BLOQUES) ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> COR_LAPIS_ITEM = AwakeningSystemMod.ITEMS.register("corlapis_item",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> NOCTILUCOUS_JADE_ITEM = AwakeningSystemMod.ITEMS.register("noctilucousjade_item",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> STARSILVER_CHUNK = AwakeningSystemMod.ITEMS.register("starsilver_chunk_item",
            () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> CRYSTAL_CHUNK = AwakeningSystemMod.ITEMS.register("crystal_chunk_item",
            () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> MAGICAL_CRYSTAL_CHUNK = AwakeningSystemMod.ITEMS.register("magical_crystal_chunk_item",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ELECTRO_CRYSTAL = AwakeningSystemMod.ITEMS.register("electrocrystal_item",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // BlockItems de Minerales
    public static final DeferredHolder<Item, Item> COR_LAPIS_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("corlapis",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.COR_LAPIS.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> NOCTILUCOUS_JADE_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("noctilucousjade",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.NOCTILUCOUS_JADE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> STARSILVER_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("starsilver",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.STARSILVER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRYSTAL_CHUNK_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("crystal_chunk_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CRYSTAL_CHUNK_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAGICAL_CRYSTAL_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("magical_crystal_chunk_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAGICAL_CRYSTAL_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ELECTRO_CRYSTAL_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("electrocrystal",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELECTRO_CRYSTAL_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    // BlockItems de Santuarios, Óculos y Muebles
    public static final DeferredHolder<Item, Item> ANEMO_SHRINE_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("anemo_shrine_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ANEMO_SHRINE_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> GEO_SHRINE_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("geo_shrine_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.GEO_SHRINE_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> ELECTRO_SHRINE_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("electro_shrine_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELECTRO_SHRINE_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> DENDRO_SHRINE_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("dendro_shrine_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.DENDRO_SHRINE_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> ANEMOCULUS_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("anemoculus_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ANEMOCULUS_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> GEOCULUS_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("geoculus_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.GEOCULUS_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ELECTROCULUS_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("electroculus_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELECTROCULUS_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> DENDROCULUS_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("dendroculus_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.DENDROCULUS_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> FISHING_BLOCK_ITEM = AwakeningSystemMod.ITEMS.register("fishing_block",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.FISHING_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> FISHING_GUILD_STATION_ITEM = AwakeningSystemMod.ITEMS.register("fishing_guild_station",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.FISHING_GUILD_STATION.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> BAMBOO_TEA_TABLE_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_outdoor_tea_table",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_TEA_TABLE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_TABLE_ITEM = AwakeningSystemMod.ITEMS.register("otogi_rakushi_square_table",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_TABLE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_STOOL_ITEM = AwakeningSystemMod.ITEMS.register("otogi_rakushi_square_stool",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_STOOL.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_STOOL_ITEM = AwakeningSystemMod.ITEMS.register("square_yumemiru_stool",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_STOOL.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_FLOOR_LAMP_ITEM = AwakeningSystemMod.ITEMS.register("liuli_pavilion_sandbearer_floor_lamp",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_FLOOR_LAMP.get(), new Item.Properties()));

    // BlockItems de Maderas de Teyvat (Stairs, Slabs, Fences, Doors, Trapdoors)
    public static final DeferredHolder<Item, Item> CUIHUA_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("cuihua_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("cuihua_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("cuihua_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("cuihua_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("cuihua_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.CUIHUA_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUIHUA_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("cuihua_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CUIHUA_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> SANDBEARER_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("sandbearer_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("sandbearer_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("sandbearer_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("sandbearer_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("sandbearer_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.SANDBEARER_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANDBEARER_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("sandbearer_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.SANDBEARER_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> ADHIGAMA_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("adhigama_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("adhigama_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("adhigama_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("adhigama_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("adhigama_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.ADHIGAMA_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ADHIGAMA_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("adhigama_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ADHIGAMA_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> ARALIA_WOOD_ITEM = AwakeningSystemMod.ITEMS.register("aralia_wood",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_WOOD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_LEAVES_ITEM = AwakeningSystemMod.ITEMS.register("aralia_leaves",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_PLANKS_ITEM = AwakeningSystemMod.ITEMS.register("aralia_planks",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("aralia_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("aralia_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("aralia_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("aralia_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("aralia_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.ARALIA_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_SAPLING_ITEM = AwakeningSystemMod.ITEMS.register("aralia_sapling",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARALIA_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("aralia_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ARALIA_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> YUMEMIRU_WOOD_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_wood",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_WOOD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_LEAVES_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_leaves",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_PLANKS_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_planks",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.YUMEMIRU_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_SAPLING_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_sapling",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> YUMEMIRU_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("yumemiru_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.YUMEMIRU_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> OTOGI_WOOD_ITEM = AwakeningSystemMod.ITEMS.register("otogi_wood",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_WOOD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_LEAVES_ITEM = AwakeningSystemMod.ITEMS.register("otogi_leaves",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_PLANKS_ITEM = AwakeningSystemMod.ITEMS.register("otogi_planks",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("otogi_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("otogi_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("otogi_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("otogi_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("otogi_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.OTOGI_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_SAPLING_ITEM = AwakeningSystemMod.ITEMS.register("otogi_sapling",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OTOGI_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("otogi_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.OTOGI_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> BAMBOO_SEGMENT_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_segment",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_SEGMENT.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_PLANKS_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_planks",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.BAMBOO_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAMBOO_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("bamboo_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BAMBOO_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> MAPLE_WOOD_ITEM = AwakeningSystemMod.ITEMS.register("maple_wood",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_WOOD.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_LEAVES_ITEM = AwakeningSystemMod.ITEMS.register("maple_leaves",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_PLANKS_ITEM = AwakeningSystemMod.ITEMS.register("maple_planks",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_PLANKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_STAIRS_ITEM = AwakeningSystemMod.ITEMS.register("maple_stairs",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_STAIRS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_SLAB_ITEM = AwakeningSystemMod.ITEMS.register("maple_slab",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_SLAB.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_FENCE_ITEM = AwakeningSystemMod.ITEMS.register("maple_fence",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_FENCE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_FENCE_GATE_ITEM = AwakeningSystemMod.ITEMS.register("maple_fence_gate",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_FENCE_GATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_SAPLING_ITEM = AwakeningSystemMod.ITEMS.register("maple_sapling",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_DOOR_ITEM = AwakeningSystemMod.ITEMS.register("maple_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(SystemBlocks.MAPLE_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MAPLE_TRAPDOOR_ITEM = AwakeningSystemMod.ITEMS.register("maple_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(SystemBlocks.MAPLE_TRAPDOOR.get(), new Item.Properties()));

    // ==========================================
    // ✦ 60 NUEVOS ARTEFACTOS CANÓNICOS DE TEYVAT ✦
    // ==========================================
    public static final java.util.List<DeferredHolder<Item, Item>> EXPANDED_ARTIFACTS = registerExpandedArtifacts();

    private static java.util.List<DeferredHolder<Item, Item>> registerExpandedArtifacts() {
        java.util.List<DeferredHolder<Item, Item>> list = new java.util.ArrayList<>();
        com.soybe.awakeningsystem.artifact.ArtifactSet[] sets = {
                com.soybe.awakeningsystem.artifact.ArtifactSet.NOBLESSE_OBLIGE,
                com.soybe.awakeningsystem.artifact.ArtifactSet.EMBLEM_OF_SEVERED_FATE,
                com.soybe.awakeningsystem.artifact.ArtifactSet.THUNDERING_FURY,
                com.soybe.awakeningsystem.artifact.ArtifactSet.BLIZZARD_STRAYER,
                com.soybe.awakeningsystem.artifact.ArtifactSet.HEART_OF_DEPTH,
                com.soybe.awakeningsystem.artifact.ArtifactSet.ARCHAIC_PETRA,
                com.soybe.awakeningsystem.artifact.ArtifactSet.DEEPWOOD_MEMORIES,
                com.soybe.awakeningsystem.artifact.ArtifactSet.SHIMENAWA_REMINISCENCE,
                com.soybe.awakeningsystem.artifact.ArtifactSet.DESERT_PAVILION,
                com.soybe.awakeningsystem.artifact.ArtifactSet.OCEAN_HUED_CLAM,
                com.soybe.awakeningsystem.artifact.ArtifactSet.TENACITY_OF_THE_MILLELITH,
                com.soybe.awakeningsystem.artifact.ArtifactSet.GOLDEN_TROUPE
        };
        com.soybe.awakeningsystem.artifact.ArtifactType[] types = com.soybe.awakeningsystem.artifact.ArtifactType.values();
        for (com.soybe.awakeningsystem.artifact.ArtifactSet s : sets) {
            for (com.soybe.awakeningsystem.artifact.ArtifactType t : types) {
                String id = "artifact_" + s.getIdName() + "_" + t.getIdName();
                DeferredHolder<Item, Item> holder = AwakeningSystemMod.ITEMS.register(id,
                        () -> new com.soybe.awakeningsystem.artifact.GenshinArtifactItem(s, t, new Item.Properties()));
                list.add(holder);
            }
        }
        return list;
    }

    private SystemItems() {}


    // ========================================================
    // PELUCHES (PLUSHIES) DE GENSHIN IMPACT CON ANIMACIÓN BOUNCE
    // ========================================================
    public static final DeferredHolder<Item, Item> PLUSHIES_LOOTBOX = AwakeningSystemMod.ITEMS.register(
            "plushieslootbox", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.PLUSHIES_LOOTBOX.get(), new Item.Properties().rarity(Rarity.RARE)));

    // ==========================================
    // ✦ MONUMENTOS ELEMENTALES DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Item, Item> ANEMO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "anemo_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ANEMO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> GEO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "geo_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.GEO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> ELECTRO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "electro_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.ELECTRO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> DENDRO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "dendro_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.DENDRO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> HYDRO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "hydro_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.HYDRO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> PYRO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "pyro_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.PYRO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> CRYO_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "cryo_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.CRYO_MONUMENT.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> BLANK_MONUMENT = AwakeningSystemMod.ITEMS.register(
            "blank_monument", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.BLANK_MONUMENT.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> GUILD_HUNTING_BOARD_ITEM = AwakeningSystemMod.ITEMS.register(
            "guild_hunting_board", () -> new net.minecraft.world.item.BlockItem(SystemBlocks.GUILD_HUNTING_BOARD.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final java.util.Map<String, DeferredHolder<Item, Item>> PLUSHIE_ITEMS = registerAllPlushieItems();

    private static java.util.Map<String, DeferredHolder<Item, Item>> registerAllPlushieItems() {
        java.util.Map<String, DeferredHolder<Item, Item>> map = new java.util.LinkedHashMap<>();
        for (String id : SystemBlocks.PLUSHIE_IDS) {
            DeferredHolder<Item, Item> holder = AwakeningSystemMod.ITEMS.register(id, () ->
                    new net.minecraft.world.item.BlockItem(SystemBlocks.PLUSHIE_BLOCKS.get(id).get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
            map.put(id, holder);
        }
        return map;
    }

    public static ItemStack getRandomPlushieItem(net.minecraft.util.RandomSource random) {
        List<DeferredHolder<Item, Item>> items = new java.util.ArrayList<>(PLUSHIE_ITEMS.values());
        if (items.isEmpty()) return ItemStack.EMPTY;
        return new ItemStack(items.get(random.nextInt(items.size())).get());
    }

    public static void register() {
        // Trigger static initialization to register items before DeferredRegister is registered to modBus
    }

    public static void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Exclusivo en las pestañas creativas propias del mod (ModCreativeTabs). Las pestañas vanilla quedan 100% limpias.
        // Ocultar cualquier objeto relacionado al Wither (wither_spawn_egg, etc.) de las pestañas creativas y de búsqueda
        try {
            java.util.List<ItemStack> parentToRemove = new java.util.ArrayList<>();
            for (ItemStack stack : event.getParentEntries()) {
                if (!stack.isEmpty()) {
                    String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase();
                    if (path.contains("wither")) {
                        parentToRemove.add(stack);
                    }
                }
            }
            for (ItemStack s : parentToRemove) {
                event.remove(s, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }

            java.util.List<ItemStack> searchToRemove = new java.util.ArrayList<>();
            for (ItemStack stack : event.getSearchEntries()) {
                if (!stack.isEmpty()) {
                    String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase();
                    if (path.contains("wither")) {
                        searchToRemove.add(stack);
                    }
                }
            }
            for (ItemStack s : searchToRemove) {
                event.remove(s, net.minecraft.world.item.CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        } catch (Exception ignored) {}
    }

    public static ItemStack coin(int amount) {
        int qty = Math.min(64, Math.max(1, amount));
        if (AWAKENING_COIN != null && AWAKENING_COIN.isBound()) {
            return new ItemStack(AWAKENING_COIN.value(), qty);
        }
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "awakening_coin"));
        if (item != null && item != Items.AIR) {
            return new ItemStack(item, qty);
        }
        return new ItemStack(Items.GOLD_NUGGET, qty);
    }
}
