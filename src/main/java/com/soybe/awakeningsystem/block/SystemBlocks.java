package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SystemBlocks {
    private SystemBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, AwakeningSystemMod.MOD_ID);

    public static final DeferredHolder<Block, WildGinsengBlock> WILD_GINSENG = BLOCKS.register(
            "wild_ginseng", () -> new WildGinsengBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .lightLevel(state -> 6)
            )
    );

    public static final DeferredHolder<Block, RouletteAltarBlock> ROULETTE_ALTAR = BLOCKS.register(
            "roulette_altar", () -> new RouletteAltarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(2.5F, 6.0F)
                            .sound(SoundType.DEEPSLATE)
                            .lightLevel(state -> 9)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, CelestialDragonAltarBlock> CELESTIAL_DRAGON_ALTAR = BLOCKS.register(
            "celestial_dragon_altar", () -> new CelestialDragonAltarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(50.0F, 3600000.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> 12)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, GoddessAltarBlock> GODDESS_ALTAR = BLOCKS.register(
            "goddess_altar", () -> new GoddessAltarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIAMOND)
                            .strength(3.5F, 1200.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 12)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, BountyBoardBlock> BOUNTY_BOARD = BLOCKS.register(
            "bounty_board", () -> new BountyBoardBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, TavernChairBlock> TAVERN_CHAIR = BLOCKS.register(
            "tavern_chair", () -> new TavernChairBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(1.5F, 2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, CampfireLogSeatBlock> CAMPFIRE_LOG_SEAT = BLOCKS.register(
            "campfire_log_seat", () -> new CampfireLogSeatBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(1.5F, 2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );


    public static final DeferredHolder<Block, MurimAlchemyCauldronBlock> MURIM_ALCHEMY_CAULDRON = BLOCKS.register(
            "murim_alchemy_cauldron", () -> new MurimAlchemyCauldronBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(3.5F, 12.0F)
                            .sound(SoundType.ANVIL)
                            .lightLevel(state -> state.getValue(MurimAlchemyCauldronBlock.LIT) ? 12 : 2)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, TavernTableBlock> TAVERN_TABLE = BLOCKS.register(
            "tavern_table", () -> new TavernTableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0F, 2.5F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayRailTrackBlock> SUBWAY_RAIL_TRACK = BLOCKS.register(
            "subway_rail_track", () -> new SubwayRailTrackBlock(
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayWideRailTrackBlock> SUBWAY_WIDE_RAIL_TRACK = BLOCKS.register(
            "subway_wide_rail_track", () -> new SubwayWideRailTrackBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(3.5F, 6.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayDigitalScreenBlock> SUBWAY_DIGITAL_SCREEN = BLOCKS.register(
            "subway_digital_screen", () -> new SubwayDigitalScreenBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.LANTERN)
                            .lightLevel(state -> 12)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayGrabHandleBlock> SUBWAY_GRAB_HANDLE = BLOCKS.register(
            "subway_grab_handle", () -> new SubwayGrabHandleBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.5F, 2.0F)
                            .sound(SoundType.CHAIN)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayTrainWheelBlock> SUBWAY_TRAIN_WHEEL = BLOCKS.register(
            "subway_train_wheel", () -> new SubwayTrainWheelBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(4.0F, 8.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayPassengerSeatBlock> SUBWAY_PASSENGER_SEAT = BLOCKS.register(
            "subway_passenger_seat", () -> new SubwayPassengerSeatBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayTrainControllerBlock> SUBWAY_TRAIN_CONTROLLER = BLOCKS.register(
            "subway_train_controller", () -> new SubwayTrainControllerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(4.0F, 8.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, Block> SUBWAY_STAINLESS_STEEL = BLOCKS.register(
            "subway_stainless_steel", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(4.5F, 9.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, SubwayDoorBlock> SUBWAY_DOOR = BLOCKS.register(
            "subway_door", () -> new SubwayDoorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(4.0F, 8.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, SubwayPlatformBlock> SUBWAY_PLATFORM = BLOCKS.register(
            "subway_platform", () -> new SubwayPlatformBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.5F, 6.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, SubwayPlatformEdgeBlock> SUBWAY_PLATFORM_EDGE = BLOCKS.register(
            "subway_platform_edge", () -> new SubwayPlatformEdgeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.5F, 6.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, ArchitectDraftingTableBlock> ARCHITECT_DRAFTING_TABLE = BLOCKS.register(
            "architect_drafting_table", () -> new ArchitectDraftingTableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5F, 3.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, DimensionalRiftStandBlock> DIMENSIONAL_RIFT_STAND = BLOCKS.register(
            "dimensional_rift_stand", () -> new DimensionalRiftStandBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_TREASURE_CHEST = BLOCKS.register(
            "locked_treasure_chest", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(3.0F, 1200.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.AUTO
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_CHEST_OVERWORLD = BLOCKS.register(
            "locked_chest_overworld", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0F, 1200.0F).sound(SoundType.WOOD).noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.OVERWORLD
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_CHEST_NETHER = BLOCKS.register(
            "locked_chest_nether", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(3.0F, 1200.0F).sound(SoundType.NETHER_WOOD).noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.NETHER
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_CHEST_END = BLOCKS.register(
            "locked_chest_end", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(3.0F, 1200.0F).sound(SoundType.STONE).noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.END
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_CHEST_TEYVAT = BLOCKS.register(
            "locked_chest_teyvat", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.TEYVAT
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_CHEST_ISEKAI = BLOCKS.register(
            "locked_chest_isekai", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(3.0F, 1200.0F).sound(SoundType.AMETHYST).noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.ISEKAI
            )
    );

    public static final DeferredHolder<Block, LockedTreasureChestBlock> LOCKED_CHEST_MURIM = BLOCKS.register(
            "locked_chest_murim", () -> new LockedTreasureChestBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.EMERALD).strength(3.0F, 1200.0F).sound(SoundType.BAMBOO_WOOD).noOcclusion(),
                    LockedTreasureChestBlock.ChestTheme.MURIM
            )
    );

    public static final DeferredHolder<Block, AscensionPortalBlock> ASCENSION_PORTAL = BLOCKS.register(
            "tower_portal", () -> new AscensionPortalBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .noCollission()
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 15)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, FiveElementsPedestalBlock> FIVE_ELEMENTS_PEDESTAL = BLOCKS.register(
            "five_elements_pedestal", () -> new FiveElementsPedestalBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DEEPSLATE)
                            .strength(4.0F, 1200.0F)
                            .sound(SoundType.DEEPSLATE_TILES)
                            .lightLevel(state -> state.getValue(FiveElementsPedestalBlock.ACTIVE) ? 12 : 6)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalBarrierBlock> ELEMENTAL_BARRIER = BLOCKS.register(
            "elemental_barrier", () -> new ElementalBarrierBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalAltarBlock> ELEMENTAL_ALTAR = BLOCKS.register(
            "elemental_altar", () -> new ElementalAltarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.QUARTZ)
                            .strength(4.5F, 1200.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(state -> state.getValue(ElementalAltarBlock.ACTIVE) ? 14 : 7)
                            .noOcclusion()
            )
    );

    // --- MINERALES Y BLOQUES METÁLICOS POST-NETHERITE ---
    public static final DeferredHolder<Block, Block> ABYSSAL_STONE = BLOCKS.register(
            "abyssal_stone", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(4.5F, 10.0F)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ABYSSAL_RESONANT_CRYSTAL = BLOCKS.register(
            "abyssal_resonant_crystal", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(2.5F, 5.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(s -> 12)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ABYSSAL_ORE = BLOCKS.register(
            "abyssal_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(30.0F, 1200.0F)
                            .sound(SoundType.ANCIENT_DEBRIS)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> DEEPSLATE_ABYSSAL_ORE = BLOCKS.register(
            "deepslate_abyssal_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DEEPSLATE)
                            .strength(32.0F, 1200.0F)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ABYSSAL_BLOCK = BLOCKS.register(
            "abyssal_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(50.0F, 1200.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> MITHRIL_ORE = BLOCKS.register(
            "mithril_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(4.5F, 9.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> DEEPSLATE_MITHRIL_ORE = BLOCKS.register(
            "deepslate_mithril_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DEEPSLATE)
                            .strength(6.0F, 12.0F)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> MITHRIL_BLOCK = BLOCKS.register(
            "mithril_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIAMOND)
                            .strength(6.5F, 14.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ADAMANTITE_ORE = BLOCKS.register(
            "adamantite_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NETHER)
                            .strength(7.5F, 16.0F)
                            .sound(SoundType.NETHER_ORE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ADAMANTITE_BLOCK = BLOCKS.register(
            "adamantite_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(8.5F, 20.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> SPIRIT_JADE_ORE = BLOCKS.register(
            "spirit_jade_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.EMERALD)
                            .strength(8.0F, 18.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 5)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> SPIRIT_JADE_BLOCK = BLOCKS.register(
            "spirit_jade_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.EMERALD)
                            .strength(9.0F, 22.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(s -> 8)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ASTRAL_ORE = BLOCKS.register(
            "astral_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(10.0F, 30.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(s -> 9)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> ASTRAL_BLOCK = BLOCKS.register(
            "astral_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(12.0F, 40.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(s -> 12)
                            .requiresCorrectToolForDrops()
            )
    );

    // ==========================================
    // === MINERALES DE TEYVAT (PRIMOGEM & ARCHON ORES) ===
    // ==========================================
    public static final DeferredHolder<Block, Block> PRIMOGEM_ORE = BLOCKS.register(
            "primogem_ore", () -> new DropExperienceBlock(
                    UniformInt.of(3, 7),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(4.5F, 6.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 6)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> DEEPSLATE_PRIMOGEM_ORE = BLOCKS.register(
            "deepslate_primogem_ore", () -> new DropExperienceBlock(
                    UniformInt.of(4, 8),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DEEPSLATE)
                            .strength(6.0F, 7.0F)
                            .sound(SoundType.DEEPSLATE)
                            .lightLevel(s -> 6)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> WHITE_IRON_ORE = BLOCKS.register(
            "white_iron_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0F, 3.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> AGNIDUS_AGATE_ORE = BLOCKS.register(
            "agnidus_agate_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> SHIVADA_JADE_ORE = BLOCKS.register(
            "shivada_jade_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> VARUNADA_LAZURITE_ORE = BLOCKS.register(
            "varunada_lazurite_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> VAJRADA_AMETHYST_ORE = BLOCKS.register(
            "vajrada_amethyst_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> VAYUDA_TURQUOISE_ORE = BLOCKS.register(
            "vayuda_turquoise_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> PRITHIVA_TOPAZ_ORE = BLOCKS.register(
            "prithiva_topaz_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, Block> NAGADUS_EMERALD_ORE = BLOCKS.register(
            "nagadus_emerald_ore", () -> new DropExperienceBlock(
                    UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(3.5F, 4.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(s -> 4)
                            .requiresCorrectToolForDrops()
            )
    );

    // --- HORNOS AVANZADOS (ADDON IRON FURNACES / STANDALONE) ---
    public static final DeferredHolder<Block, com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock> ABYSSAL_FURNACE = BLOCKS.register(
            "abyssal_furnace", () -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(5.5F, 10.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> state.getValue(com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock.LIT) ? 13 : 0)
                            .requiresCorrectToolForDrops(),
                    com.soybe.awakeningsystem.furnace.FurnaceTier.ABYSSAL
            )
    );

    public static final DeferredHolder<Block, com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock> MITHRIL_FURNACE = BLOCKS.register(
            "mithril_furnace", () -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIAMOND)
                            .strength(6.0F, 12.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> state.getValue(com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock.LIT) ? 13 : 0)
                            .requiresCorrectToolForDrops(),
                    com.soybe.awakeningsystem.furnace.FurnaceTier.MITHRIL
            )
    );

    public static final DeferredHolder<Block, com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock> ADAMANTITE_FURNACE = BLOCKS.register(
            "adamantite_furnace", () -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(8.0F, 18.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> state.getValue(com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock.LIT) ? 14 : 0)
                            .requiresCorrectToolForDrops(),
                    com.soybe.awakeningsystem.furnace.FurnaceTier.ADAMANTITE
            )
    );

    public static final DeferredHolder<Block, com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock> SPIRIT_JADE_FURNACE = BLOCKS.register(
            "spirit_jade_furnace", () -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.EMERALD)
                            .strength(9.0F, 22.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> state.getValue(com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock.LIT) ? 14 : 0)
                            .requiresCorrectToolForDrops(),
                    com.soybe.awakeningsystem.furnace.FurnaceTier.SPIRIT_JADE
            )
    );

    public static final DeferredHolder<Block, com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock> ASTRAL_FURNACE = BLOCKS.register(
            "astral_furnace", () -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(12.0F, 36.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> state.getValue(com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlock.LIT) ? 15 : 0)
                            .requiresCorrectToolForDrops(),
                    com.soybe.awakeningsystem.furnace.FurnaceTier.ASTRAL
            )
    );

    public static final DeferredHolder<Block, DragonNestBlock> DRAGON_NEST = BLOCKS.register(
            "dragon_nest", () -> new DragonNestBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5F, 12.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, AncientDragonEggBlock> ANCIENT_DRAGON_EGG_FIRE = BLOCKS.register(
            "ancient_dragon_egg_fire", () -> {
                AncientDragonEggBlock block = new AncientDragonEggBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_RED)
                                .strength(3.0F, 15.0F)
                                .sound(SoundType.STONE)
                                .lightLevel(state -> 6)
                                .noOcclusion()
                );
                return block;
            }
    );

    public static final DeferredHolder<Block, AncientDragonEggBlock> ANCIENT_DRAGON_EGG_ICE = BLOCKS.register(
            "ancient_dragon_egg_ice", () -> {
                AncientDragonEggBlock block = new AncientDragonEggBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.ICE)
                                .strength(3.0F, 15.0F)
                                .sound(SoundType.GLASS)
                                .lightLevel(state -> 6)
                                .noOcclusion()
                );
                return block;
            }
    );

    public static final DeferredHolder<Block, AncientDragonEggBlock> ANCIENT_DRAGON_EGG_FOREST = BLOCKS.register(
            "ancient_dragon_egg_forest", () -> {
                AncientDragonEggBlock block = new AncientDragonEggBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.EMERALD)
                                .strength(3.0F, 15.0F)
                                .sound(SoundType.STONE)
                                .lightLevel(state -> 6)
                                .noOcclusion()
                );
                return block;
            }
    );

    public static final DeferredHolder<Block, AncientDragonEggBlock> ANCIENT_DRAGON_EGG_SHADOW = BLOCKS.register(
            "ancient_dragon_egg_shadow", () -> {
                AncientDragonEggBlock block = new AncientDragonEggBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_PURPLE)
                                .strength(3.0F, 15.0F)
                                .sound(SoundType.STONE)
                                .lightLevel(state -> 6)
                                .noOcclusion()
                );
                return block;
            }
    );

    public static final DeferredHolder<Block, AncientDragonEggBlock> ANCIENT_DRAGON_EGG_LIGHTNING = BLOCKS.register(
            "ancient_dragon_egg_lightning", () -> {
                AncientDragonEggBlock block = new AncientDragonEggBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                                .strength(3.0F, 15.0F)
                                .sound(SoundType.COPPER)
                                .lightLevel(state -> 8)
                                .noOcclusion()
                );
                return block;
            }
    );

    public static final DeferredHolder<Block, AncientDragonEggBlock> ANCIENT_DRAGON_EGG_CELESTIAL = BLOCKS.register(
            "ancient_dragon_egg_celestial", () -> {
                AncientDragonEggBlock block = new AncientDragonEggBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.GOLD)
                                .strength(3.0F, 15.0F)
                                .sound(SoundType.AMETHYST)
                                .lightLevel(state -> 10)
                                .noOcclusion()
                );
                return block;
            }
    );

    public static final DeferredHolder<Block, SlotMachineBlock> SLOT_MACHINE = BLOCKS.register(
            "slot_machine", () -> new SlotMachineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(3.0F, 1200.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 14)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, GachaMachineBlock> GACHA_MACHINE = BLOCKS.register(
            "gacha_machine", () -> new GachaMachineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(3.0F, 1200.0F)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );

    // Bloque Cósmico de las Profundidades del Abismo (Genshin Spiral Abyss)
    public static final DeferredHolder<Block, Block> ABYSSAL_VOID_STONE = BLOCKS.register(
            "abyssal_void_stone", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
            )
    );

    // Punto de Teletransporte Celestial a Teyvat (Genshin Waypoint)
    public static final DeferredHolder<Block, CelestialWaypointBlock> CELESTIAL_WAYPOINT = BLOCKS.register(
            "celestial_waypoint", () -> new CelestialWaypointBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(3.5F, 1200.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 12)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            )
    );

    // Altar Sagrado de la Diosa Trilunar (Columbina y las Tres Diosas de la Luna)
    public static final DeferredHolder<Block, TrilunarAltarBlock> TRILUNAR_ALTAR = BLOCKS.register(
            "trilunar_altar", () -> new TrilunarAltarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(4.0F, 1200.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 14)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            )
    );

    // Bloque de Mora maciza de PrimogemCraft
    public static final DeferredHolder<Block, MoraBlock> MORA_BLOCK = BLOCKS.register(
            "mora_block", () -> new MoraBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            )
    );

    // Vasija de Tesoro de Teyvat de PrimogemCraft
    public static final DeferredHolder<Block, TeyvatJarBlock> TEYVAT_JAR = BLOCKS.register(
            "teyvat_jar", () -> new TeyvatJarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                            .strength(0.4F, 1.0F)
                            .sound(SoundType.DECORATED_POT)
                            .noOcclusion()
            )
    );

    // ========================================================
    // MADERAS Y ÁRBOLES CANÓNICOS DE TEYVAT (GENSHIN IMPACT)
    // ========================================================

    // 1. Árbol de Cuihua (Mondstadt & Bishui Plains)
    public static class TeyvatSaplingBlock extends BushBlock implements BonemealableBlock {
        public static final com.mojang.serialization.MapCodec<TeyvatSaplingBlock> CODEC = simpleCodec(TeyvatSaplingBlock::new);

        public TeyvatSaplingBlock(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        @Override
        protected boolean mayPlaceOn(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
            return state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(Blocks.FARMLAND) || state.is(Blocks.MOSS_BLOCK)
                    || state.is(Blocks.NETHERRACK) || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM)
                    || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE);
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
            return true;
        }

        @Override
        public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
            return (double) random.nextFloat() < 0.45D;
        }

        @Override
        public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
            advanceTree(level, pos, state, random);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
                advanceTree(level, pos, state, random);
            }
        }

        public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
            BlockPos corner = find2x2Corner(level, pos, state.getBlock());
            boolean is2x2 = corner != null;
            if (is2x2) {
                level.removeBlock(corner, false);
                level.removeBlock(corner.east(), false);
                level.removeBlock(corner.south(), false);
                level.removeBlock(corner.south().east(), false);
            } else {
                level.removeBlock(pos, false);
            }
            BlockPos spawnPos = is2x2 ? corner : pos;
            boolean success = false;
            if (state.is(CUIHUA_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateLargeCuihuaTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateCuihuaTree(level, spawnPos, random, true);
            } else if (state.is(ADHIGAMA_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateColossalAdhigamaTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateAdhigamaTree(level, spawnPos, random, true);
            } else if (state.is(SANDBEARER_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateLargeSandbearerTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateSandbearerTree(level, spawnPos, random, true);
            } else if (state.is(MANA_WOOD_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateLargeManaTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateManaTree(level, spawnPos, random, true);
            } else if (state.is(ELDORIA_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateLargeEldoriaTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateEldoriaTree(level, spawnPos, random, true);
            } else if (state.is(SPIRIT_PLUM_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.MurimTreeBuilder.generateLargeSpiritPlumTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.MurimTreeBuilder.generateSpiritPlumTree(level, spawnPos, random, true);
            } else if (state.is(CELESTIAL_PINE_SAPLING.get())) {
                success = is2x2
                        ? com.soybe.awakeningsystem.world.MurimTreeBuilder.generateLargeCelestialPineTree(level, spawnPos, random, true)
                        : com.soybe.awakeningsystem.world.MurimTreeBuilder.generateCelestialPineTree(level, spawnPos, random, true);
            } else if (state.is(ARALIA_SAPLING.get())) {
                success = com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateAraliaTree(level, spawnPos, random, true);
            } else if (state.is(OTOGI_SAPLING.get())) {
                success = com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateOtogiTree(level, spawnPos, random, true);
            } else if (state.is(YUMEMIRU_SAPLING.get())) {
                success = com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateYumemiruTree(level, spawnPos, random, true);
            } else if (state.is(MAPLE_SAPLING.get())) {
                success = com.soybe.awakeningsystem.world.TeyvatRegionalBuilder.generateMapleTree(level, spawnPos, random, true);
            }
            if (!success) {
                if (is2x2) {
                    level.setBlock(corner, state, 3);
                    level.setBlock(corner.east(), state, 3);
                    level.setBlock(corner.south(), state, 3);
                    level.setBlock(corner.south().east(), state, 3);
                } else {
                    level.setBlock(pos, state, 3);
                }
            }
        }

        private BlockPos find2x2Corner(ServerLevel level, BlockPos pos, Block block) {
            for (int dx = -1; dx <= 0; dx++) {
                for (int dz = -1; dz <= 0; dz++) {
                    BlockPos p = pos.offset(dx, 0, dz);
                    if (level.getBlockState(p).is(block) &&
                        level.getBlockState(p.east()).is(block) &&
                        level.getBlockState(p.south()).is(block) &&
                        level.getBlockState(p.south().east()).is(block)) {
                        return p;
                    }
                }
            }
            return null;
        }
    }

    public static final DeferredHolder<Block, RotatedPillarBlock> CUIHUA_LOG = BLOCKS.register(
            "cuihua_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG))
    );
    public static final DeferredHolder<Block, FruitLeavesBlock> CUIHUA_LEAVES = BLOCKS.register(
            "cuihua_leaves", () -> new FruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).randomTicks().noOcclusion(),
                    () -> SystemItems.SUNSETTIA.get())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> CUIHUA_SAPLING = BLOCKS.register(
            "cuihua_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> CUIHUA_PLANKS = BLOCKS.register(
            "cuihua_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS))
    );

    // 2. Árbol de Adhigama (Selva Tropical de Sumeru / Dharma Forest)
    public static final DeferredHolder<Block, RotatedPillarBlock> ADHIGAMA_LOG = BLOCKS.register(
            "adhigama_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LOG))
    );
    public static final DeferredHolder<Block, LeavesBlock> ADHIGAMA_LEAVES = BLOCKS.register(
            "adhigama_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES).noOcclusion())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> ADHIGAMA_SAPLING = BLOCKS.register(
            "adhigama_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SAPLING).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> ADHIGAMA_PLANKS = BLOCKS.register(
            "adhigama_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PLANKS))
    );

    // 3. Árbol de Sandbearer / Madera Arenosa (Picos Kársticos de Liyue)
    public static final DeferredHolder<Block, RotatedPillarBlock> SANDBEARER_LOG = BLOCKS.register(
            "sandbearer_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LOG))
    );
    public static final DeferredHolder<Block, LeavesBlock> SANDBEARER_LEAVES = BLOCKS.register(
            "sandbearer_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).noOcclusion())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> SANDBEARER_SAPLING = BLOCKS.register(
            "sandbearer_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_SAPLING).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> SANDBEARER_PLANKS = BLOCKS.register(
            "sandbearer_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS))
    );

    // ========================================================
    // MADERAS Y ÁRBOLES CANÓNICOS DEL MUNDO ISEKAI
    // ========================================================

    // 4. Árbol de Madera de Maná (Bosque Místico de Isekai)
    public static final DeferredHolder<Block, RotatedPillarBlock> MANA_WOOD_LOG = BLOCKS.register(
            "mana_wood_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_STEM).sound(SoundType.WOOD))
    );
    public static final DeferredHolder<Block, FruitLeavesBlock> MANA_WOOD_LEAVES = BLOCKS.register(
            "mana_wood_leaves", () -> new FruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).lightLevel(s -> 5).randomTicks().noOcclusion(),
                    () -> SystemItems.MANA_FRUIT.get())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> MANA_WOOD_SAPLING = BLOCKS.register(
            "mana_wood_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).lightLevel(s -> 3).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> MANA_WOOD_PLANKS = BLOCKS.register(
            "mana_wood_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).sound(SoundType.WOOD))
    );

    // 5. Árbol Sagrado de Eldoria (Llanuras Doradas / Arboleda Sagrada de Isekai)
    public static final DeferredHolder<Block, RotatedPillarBlock> ELDORIA_LOG = BLOCKS.register(
            "eldoria_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LOG).sound(SoundType.WOOD))
    );
    public static final DeferredHolder<Block, LeavesBlock> ELDORIA_LEAVES = BLOCKS.register(
            "eldoria_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LEAVES).lightLevel(s -> 4).noOcclusion())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> ELDORIA_SAPLING = BLOCKS.register(
            "eldoria_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING).lightLevel(s -> 2).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> ELDORIA_PLANKS = BLOCKS.register(
            "eldoria_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_PLANKS).sound(SoundType.WOOD))
    );

    // ========================================================
    // MADERAS Y ÁRBOLES CANÓNICOS DEL MUNDO MURIM (CULTIVO)
    // ========================================================

    // 6. Árbol de Ciruelo Espiritual del Monte Hua (Murim)
    public static final DeferredHolder<Block, RotatedPillarBlock> SPIRIT_PLUM_LOG = BLOCKS.register(
            "spirit_plum_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LOG).sound(SoundType.WOOD))
    );
    public static final DeferredHolder<Block, FruitLeavesBlock> SPIRIT_PLUM_LEAVES = BLOCKS.register(
            "spirit_plum_leaves", () -> new FruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LEAVES).lightLevel(s -> 4).randomTicks().noOcclusion(),
                    () -> SystemItems.SPIRIT_PLUM_FRUIT.get())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> SPIRIT_PLUM_SAPLING = BLOCKS.register(
            "spirit_plum_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SAPLING).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> SPIRIT_PLUM_PLANKS = BLOCKS.register(
            "spirit_plum_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).sound(SoundType.WOOD))
    );
    public static final DeferredHolder<Block, StairBlock> SPIRIT_PLUM_STAIRS = BLOCKS.register(
            "spirit_plum_stairs", () -> new StairBlock(SPIRIT_PLUM_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SPIRIT_PLUM_PLANKS.get()))
    );
    public static final DeferredHolder<Block, SlabBlock> SPIRIT_PLUM_SLAB = BLOCKS.register(
            "spirit_plum_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SPIRIT_PLUM_PLANKS.get()))
    );
    public static final DeferredHolder<Block, FenceBlock> SPIRIT_PLUM_FENCE = BLOCKS.register(
            "spirit_plum_fence", () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(SPIRIT_PLUM_PLANKS.get()))
    );
    public static final DeferredHolder<Block, FenceGateBlock> SPIRIT_PLUM_FENCE_GATE = BLOCKS.register(
            "spirit_plum_fence_gate", () -> new FenceGateBlock(WoodType.CHERRY, BlockBehaviour.Properties.ofFullCopy(SPIRIT_PLUM_PLANKS.get()))
    );
    public static final DeferredHolder<Block, DoorBlock> SPIRIT_PLUM_DOOR = BLOCKS.register(
            "spirit_plum_door", () -> new DoorBlock(BlockSetType.CHERRY, BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_DOOR))
    );
    public static final DeferredHolder<Block, TrapDoorBlock> SPIRIT_PLUM_TRAPDOOR = BLOCKS.register(
            "spirit_plum_trapdoor", () -> new TrapDoorBlock(BlockSetType.CHERRY, BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_TRAPDOOR))
    );

    // 7. Árbol de Pino Celestial Milenario (Murim)
    public static final DeferredHolder<Block, RotatedPillarBlock> CELESTIAL_PINE_LOG = BLOCKS.register(
            "celestial_pine_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LOG).sound(SoundType.WOOD))
    );
    public static final DeferredHolder<Block, LeavesBlock> CELESTIAL_PINE_LEAVES = BLOCKS.register(
            "celestial_pine_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES).lightLevel(s -> 3).noOcclusion())
    );
    public static final DeferredHolder<Block, TeyvatSaplingBlock> CELESTIAL_PINE_SAPLING = BLOCKS.register(
            "celestial_pine_sapling", () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING).randomTicks().noOcclusion())
    );
    public static final DeferredHolder<Block, Block> CELESTIAL_PINE_PLANKS = BLOCKS.register(
            "celestial_pine_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).sound(SoundType.WOOD))
    );
    public static final DeferredHolder<Block, StairBlock> CELESTIAL_PINE_STAIRS = BLOCKS.register(
            "celestial_pine_stairs", () -> new StairBlock(CELESTIAL_PINE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(CELESTIAL_PINE_PLANKS.get()))
    );
    public static final DeferredHolder<Block, SlabBlock> CELESTIAL_PINE_SLAB = BLOCKS.register(
            "celestial_pine_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CELESTIAL_PINE_PLANKS.get()))
    );
    public static final DeferredHolder<Block, FenceBlock> CELESTIAL_PINE_FENCE = BLOCKS.register(
            "celestial_pine_fence", () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(CELESTIAL_PINE_PLANKS.get()))
    );
    public static final DeferredHolder<Block, FenceGateBlock> CELESTIAL_PINE_FENCE_GATE = BLOCKS.register(
            "celestial_pine_fence_gate", () -> new FenceGateBlock(WoodType.SPRUCE, BlockBehaviour.Properties.ofFullCopy(CELESTIAL_PINE_PLANKS.get()))
    );
    public static final DeferredHolder<Block, DoorBlock> CELESTIAL_PINE_DOOR = BLOCKS.register(
            "celestial_pine_door", () -> new DoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_DOOR))
    );
    public static final DeferredHolder<Block, TrapDoorBlock> CELESTIAL_PINE_TRAPDOOR = BLOCKS.register(
            "celestial_pine_trapdoor", () -> new TrapDoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_TRAPDOOR))
    );

    // 8. Melocotonero Celestial de la Inmortalidad (Murim)
    public static final DeferredHolder<Block, FruitLeavesBlock> IMMORTALITY_PEACH_LEAVES = BLOCKS.register(
            "immortality_peach_leaves", () -> new FruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).lightLevel(s -> 6).randomTicks().noOcclusion(),
                    () -> SystemItems.IMMORTALITY_PEACH.get())
    );

    // ========================================================
    // MESA DE ASCENSIÓN CELESTIAL DE COMPAÑEROS
    // ========================================================
    public static final DeferredHolder<Block, CompanionAscensionTableBlock> COMPANION_ASCENSION_TABLE = BLOCKS.register(
            "companion_ascension_table", () -> new CompanionAscensionTableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.5F, 12.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );
    // ========================================================
    // BLOQUES DE HIPÓSTASIS ELEMENTAL (TROFEOS Y PODER ARCANO)
    // ========================================================
    public static final DeferredHolder<Block, Block> ANEMO_HYPOSTASIS_BLOCK = BLOCKS.register(
            "anemo_hypostasis_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GREEN)
                            .strength(4.0F, 12.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> ELECTRO_HYPOSTASIS_BLOCK = BLOCKS.register(
            "electro_hypostasis_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(4.0F, 12.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> GEO_HYPOSTASIS_BLOCK = BLOCKS.register(
            "geo_hypostasis_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BROWN)
                            .strength(5.0F, 15.0F)
                            .sound(SoundType.DEEPSLATE)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> CRYO_HYPOSTASIS_BLOCK = BLOCKS.register(
            "cryo_hypostasis_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(4.0F, 12.0F)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> PYRO_HYPOSTASIS_BLOCK = BLOCKS.register(
            "pyro_hypostasis_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(4.0F, 12.0F)
                            .sound(SoundType.NETHERRACK)
                            .lightLevel(state -> 12)
                            .noOcclusion()
            )
    );
    public static final DeferredHolder<Block, Block> HYDRO_HYPOSTASIS_BLOCK = BLOCKS.register(
            "hydro_hypostasis_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(4.0F, 12.0F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );

    // ========================================================
    // PELUCHES (PLUSHIES) DE GENSHIN IMPACT CON ANIMACIÓN BOUNCE
    // ========================================================
    public static final String[] PLUSHIE_IDS = {
            "aetherplushie", "albedoplushie", "aloyplushie", "amberplushie", "ayakaplushie", "ayatoplushie",
            "barbaraplushie", "beidouplushie", "bennettplushie", "candaceplushie", "chongyunplushie", "colleiplushie",
            "cynoplushie", "dainsleifplushie", "dilucplushie", "dionaplushie", "doriplushie", "eulaplushie",
            "fischlplushie", "ganyuplushie", "gorouplushie", "greencatplushie", "guobaplushie", "heizouplushie",
            "hellcatplushie", "hutaoplushie", "ittoplushie", "jeanplushie", "kaeyaplushie", "kazuhaplushie",
            "keqingplushie", "kleeplushie", "kokomiplushie", "kukiplushie", "laylaplushie", "lisaplushie",
            "lumineplushie", "monaplushie", "nahidaplushie", "nilouplushie", "ningguangplushie", "noelleplushie",
            "paimonplushie", "qiqiplushie", "raidenplushie", "razorplushie", "rosariaplushie", "saraplushie",
            "sayuplushie", "shenheplushie", "sucroseplushie", "tartagliaplushie", "thomaplushie", "tighnariplushie",
            "ventiplushie", "xianglingplushie", "xiaoplushie", "xingqiuplushie", "xinyanplushie", "yaemikoplushie",
            "yanfeiplushie", "yelanplushie", "yoimiyaplushie", "yunjinplushie", "zhongliplushie"
    };

    public static final DeferredHolder<Block, PlushiesLootboxBlock> PLUSHIES_LOOTBOX = BLOCKS.register(
            "plushieslootbox", () -> new PlushiesLootboxBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(1.0F, 3.0F)
                            .sound(SoundType.WOOD)
            )
    );

    // ==========================================
    // ✦ MONUMENTOS ELEMENTALES DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Block, ElementalMonumentBlock> ANEMO_MONUMENT = BLOCKS.register(
            "anemo_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.ANEMO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> GEO_MONUMENT = BLOCKS.register(
            "geo_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.GEO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> ELECTRO_MONUMENT = BLOCKS.register(
            "electro_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.ELECTRO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> DENDRO_MONUMENT = BLOCKS.register(
            "dendro_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.DENDRO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> HYDRO_MONUMENT = BLOCKS.register(
            "hydro_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.HYDRO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> PYRO_MONUMENT = BLOCKS.register(
            "pyro_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.PYRO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> CRYO_MONUMENT = BLOCKS.register(
            "cryo_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.CRYO,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElementalMonumentBlock> BLANK_MONUMENT = BLOCKS.register(
            "blank_monument", () -> new ElementalMonumentBlock(
                    ElementalMonumentBlock.MonumentElement.BLANK,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    // ==========================================
    // ✦ BLOQUES DE CONSTRUCCIÓN DE TEYVAT ✦
    // ==========================================
    // Cuihua derivados
    public static final DeferredHolder<Block, StairBlock> CUIHUA_STAIRS = BLOCKS.register("cuihua_stairs",
            () -> new StairBlock(CUIHUA_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(CUIHUA_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> CUIHUA_SLAB = BLOCKS.register("cuihua_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CUIHUA_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> CUIHUA_FENCE = BLOCKS.register("cuihua_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(CUIHUA_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> CUIHUA_FENCE_GATE = BLOCKS.register("cuihua_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(CUIHUA_PLANKS.get())));
    public static final DeferredHolder<Block, DoorBlock> CUIHUA_DOOR = BLOCKS.register("cuihua_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> CUIHUA_TRAPDOOR = BLOCKS.register("cuihua_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));

    // Sandbearer derivados
    public static final DeferredHolder<Block, StairBlock> SANDBEARER_STAIRS = BLOCKS.register("sandbearer_stairs",
            () -> new StairBlock(SANDBEARER_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SANDBEARER_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> SANDBEARER_SLAB = BLOCKS.register("sandbearer_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SANDBEARER_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> SANDBEARER_FENCE = BLOCKS.register("sandbearer_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(SANDBEARER_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> SANDBEARER_FENCE_GATE = BLOCKS.register("sandbearer_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(SANDBEARER_PLANKS.get())));
    public static final DeferredHolder<Block, DoorBlock> SANDBEARER_DOOR = BLOCKS.register("sandbearer_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> SANDBEARER_TRAPDOOR = BLOCKS.register("sandbearer_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));

    // Adhigama derivados
    public static final DeferredHolder<Block, StairBlock> ADHIGAMA_STAIRS = BLOCKS.register("adhigama_stairs",
            () -> new StairBlock(ADHIGAMA_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(ADHIGAMA_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> ADHIGAMA_SLAB = BLOCKS.register("adhigama_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(ADHIGAMA_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> ADHIGAMA_FENCE = BLOCKS.register("adhigama_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(ADHIGAMA_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> ADHIGAMA_FENCE_GATE = BLOCKS.register("adhigama_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(ADHIGAMA_PLANKS.get())));
    public static final DeferredHolder<Block, DoorBlock> ADHIGAMA_DOOR = BLOCKS.register("adhigama_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> ADHIGAMA_TRAPDOOR = BLOCKS.register("adhigama_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));

    // Aralia (Inazuma)
    public static final DeferredHolder<Block, RotatedPillarBlock> ARALIA_WOOD = BLOCKS.register("aralia_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LOG)));
    public static final DeferredHolder<Block, LeavesBlock> ARALIA_LEAVES = BLOCKS.register("aralia_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LEAVES).noOcclusion()));
    public static final DeferredHolder<Block, Block> ARALIA_PLANKS = BLOCKS.register("aralia_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS)));
    public static final DeferredHolder<Block, StairBlock> ARALIA_STAIRS = BLOCKS.register("aralia_stairs",
            () -> new StairBlock(ARALIA_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(ARALIA_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> ARALIA_SLAB = BLOCKS.register("aralia_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(ARALIA_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> ARALIA_FENCE = BLOCKS.register("aralia_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(ARALIA_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> ARALIA_FENCE_GATE = BLOCKS.register("aralia_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(ARALIA_PLANKS.get())));
    public static final DeferredHolder<Block, TeyvatSaplingBlock> ARALIA_SAPLING = BLOCKS.register("aralia_sapling",
            () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_SAPLING).randomTicks().noOcclusion()));
    public static final DeferredHolder<Block, DoorBlock> ARALIA_DOOR = BLOCKS.register("aralia_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> ARALIA_TRAPDOOR = BLOCKS.register("aralia_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_TRAPDOOR)));

    // Yumemiru (Sakura Inazuma)
    public static final DeferredHolder<Block, RotatedPillarBlock> YUMEMIRU_WOOD = BLOCKS.register("yumemiru_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LOG)));
    public static final DeferredHolder<Block, LeavesBlock> YUMEMIRU_LEAVES = BLOCKS.register("yumemiru_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LEAVES).noOcclusion()));
    public static final DeferredHolder<Block, Block> YUMEMIRU_PLANKS = BLOCKS.register("yumemiru_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS)));
    public static final DeferredHolder<Block, StairBlock> YUMEMIRU_STAIRS = BLOCKS.register("yumemiru_stairs",
            () -> new StairBlock(YUMEMIRU_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(YUMEMIRU_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> YUMEMIRU_SLAB = BLOCKS.register("yumemiru_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(YUMEMIRU_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> YUMEMIRU_FENCE = BLOCKS.register("yumemiru_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(YUMEMIRU_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> YUMEMIRU_FENCE_GATE = BLOCKS.register("yumemiru_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(YUMEMIRU_PLANKS.get())));
    public static final DeferredHolder<Block, TeyvatSaplingBlock> YUMEMIRU_SAPLING = BLOCKS.register("yumemiru_sapling",
            () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SAPLING).randomTicks().noOcclusion()));
    public static final DeferredHolder<Block, DoorBlock> YUMEMIRU_DOOR = BLOCKS.register("yumemiru_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> YUMEMIRU_TRAPDOOR = BLOCKS.register("yumemiru_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_TRAPDOOR)));

    // Otogi (Inazuma)
    public static final DeferredHolder<Block, RotatedPillarBlock> OTOGI_WOOD = BLOCKS.register("otogi_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredHolder<Block, LeavesBlock> OTOGI_LEAVES = BLOCKS.register("otogi_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).noOcclusion()));
    public static final DeferredHolder<Block, Block> OTOGI_PLANKS = BLOCKS.register("otogi_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
    public static final DeferredHolder<Block, StairBlock> OTOGI_STAIRS = BLOCKS.register("otogi_stairs",
            () -> new StairBlock(OTOGI_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(OTOGI_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> OTOGI_SLAB = BLOCKS.register("otogi_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(OTOGI_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> OTOGI_FENCE = BLOCKS.register("otogi_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(OTOGI_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> OTOGI_FENCE_GATE = BLOCKS.register("otogi_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(OTOGI_PLANKS.get())));
    public static final DeferredHolder<Block, TeyvatSaplingBlock> OTOGI_SAPLING = BLOCKS.register("otogi_sapling",
            () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks().noOcclusion()));
    public static final DeferredHolder<Block, DoorBlock> OTOGI_DOOR = BLOCKS.register("otogi_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> OTOGI_TRAPDOOR = BLOCKS.register("otogi_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));

    // Bamboo (Bambú de Liyue)
    public static final DeferredHolder<Block, RotatedPillarBlock> BAMBOO_SEGMENT = BLOCKS.register("bamboo_segment",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_BLOCK)));
    public static final DeferredHolder<Block, Block> BAMBOO_PLANKS = BLOCKS.register("bamboo_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS)));
    public static final DeferredHolder<Block, StairBlock> BAMBOO_STAIRS = BLOCKS.register("bamboo_stairs",
            () -> new StairBlock(BAMBOO_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BAMBOO_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> BAMBOO_SLAB = BLOCKS.register("bamboo_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BAMBOO_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> BAMBOO_FENCE = BLOCKS.register("bamboo_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(BAMBOO_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> BAMBOO_FENCE_GATE = BLOCKS.register("bamboo_fence_gate",
            () -> new FenceGateBlock(WoodType.BAMBOO, BlockBehaviour.Properties.ofFullCopy(BAMBOO_PLANKS.get())));
    public static final DeferredHolder<Block, DoorBlock> BAMBOO_DOOR = BLOCKS.register("bamboo_door",
            () -> new DoorBlock(BlockSetType.BAMBOO, BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> BAMBOO_TRAPDOOR = BLOCKS.register("bamboo_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.BAMBOO, BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_TRAPDOOR)));

    // Maple (Arce Rojo / Momiji)
    public static final DeferredHolder<Block, RotatedPillarBlock> MAPLE_WOOD = BLOCKS.register("maple_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_LOG)));
    public static final DeferredHolder<Block, LeavesBlock> MAPLE_LEAVES = BLOCKS.register("maple_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_LEAVES).noOcclusion()));
    public static final DeferredHolder<Block, Block> MAPLE_PLANKS = BLOCKS.register("maple_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_PLANKS)));
    public static final DeferredHolder<Block, StairBlock> MAPLE_STAIRS = BLOCKS.register("maple_stairs",
            () -> new StairBlock(MAPLE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(MAPLE_PLANKS.get())));
    public static final DeferredHolder<Block, SlabBlock> MAPLE_SLAB = BLOCKS.register("maple_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(MAPLE_PLANKS.get())));
    public static final DeferredHolder<Block, FenceBlock> MAPLE_FENCE = BLOCKS.register("maple_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(MAPLE_PLANKS.get())));
    public static final DeferredHolder<Block, FenceGateBlock> MAPLE_FENCE_GATE = BLOCKS.register("maple_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(MAPLE_PLANKS.get())));
    public static final DeferredHolder<Block, TeyvatSaplingBlock> MAPLE_SAPLING = BLOCKS.register("maple_sapling",
            () -> new TeyvatSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_PROPAGULE).randomTicks().noOcclusion()));
    public static final DeferredHolder<Block, DoorBlock> MAPLE_DOOR = BLOCKS.register("maple_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_DOOR)));
    public static final DeferredHolder<Block, TrapDoorBlock> MAPLE_TRAPDOOR = BLOCKS.register("maple_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_TRAPDOOR)));

    // ==========================================
    // ✦ MINERALES Y ROCAS DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Block, Block> COR_LAPIS = BLOCKS.register("corlapis",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 4)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> NOCTILUCOUS_JADE = BLOCKS.register("noctilucousjade",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 7)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> STARSILVER = BLOCKS.register("starsilver",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.5F, 8.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> CRYSTAL_CHUNK_BLOCK = BLOCKS.register("crystal_chunk_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 5)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> MAGICAL_CRYSTAL_BLOCK = BLOCKS.register("magical_crystal_chunk_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 8)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> ELECTRO_CRYSTAL_BLOCK = BLOCKS.register("electrocrystal",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.5F, 5.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 9)
                    .requiresCorrectToolForDrops()));

    // ==========================================
    // ✦ SANTUARIOS DE LAS PROFUNDIDADES & ÓCULOS ✦
    // ==========================================
    public static final DeferredHolder<Block, ShrineOfDepthsBlock> ANEMO_SHRINE_BLOCK = BLOCKS.register("anemo_shrine_block",
            () -> new ShrineOfDepthsBlock(ShrineOfDepthsBlock.ShrineElement.ANEMO, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(10.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(s -> 6)));

    public static final DeferredHolder<Block, ShrineOfDepthsBlock> GEO_SHRINE_BLOCK = BLOCKS.register("geo_shrine_block",
            () -> new ShrineOfDepthsBlock(ShrineOfDepthsBlock.ShrineElement.GEO, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(10.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(s -> 6)));

    public static final DeferredHolder<Block, ShrineOfDepthsBlock> ELECTRO_SHRINE_BLOCK = BLOCKS.register("electro_shrine_block",
            () -> new ShrineOfDepthsBlock(ShrineOfDepthsBlock.ShrineElement.ELECTRO, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(10.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(s -> 6)));

    public static final DeferredHolder<Block, ShrineOfDepthsBlock> DENDRO_SHRINE_BLOCK = BLOCKS.register("dendro_shrine_block",
            () -> new ShrineOfDepthsBlock(ShrineOfDepthsBlock.ShrineElement.DENDRO, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(10.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(s -> 6)));

    public static final DeferredHolder<Block, OculusBlock> ANEMOCULUS_BLOCK = BLOCKS.register("anemoculus_block",
            () -> new OculusBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.5F, 1.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 14)
                    .noOcclusion()));

    public static final DeferredHolder<Block, OculusBlock> GEOCULUS_BLOCK = BLOCKS.register("geoculus_block",
            () -> new OculusBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.5F, 1.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 14)
                    .noOcclusion()));

    public static final DeferredHolder<Block, OculusBlock> ELECTROCULUS_BLOCK = BLOCKS.register("electroculus_block",
            () -> new OculusBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.5F, 1.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 14)
                    .noOcclusion()));

    public static final DeferredHolder<Block, OculusBlock> DENDROCULUS_BLOCK = BLOCKS.register("dendroculus_block",
            () -> new OculusBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.5F, 1.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 14)
                    .noOcclusion()));

    public static final DeferredHolder<Block, FishingTrapBlock> FISHING_BLOCK = BLOCKS.register("fishing_block",
            () -> new FishingTrapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, FishingStationBlock> FISHING_GUILD_STATION = BLOCKS.register("fishing_guild_station",
            () -> new FishingStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    // ==========================================
    // ✦ MUEBLES Y DECORACIÓN DE TEYVAT ✦
    // ==========================================
    public static final DeferredHolder<Block, TeyvatTableBlock> BAMBOO_TEA_TABLE = BLOCKS.register("bamboo_outdoor_tea_table",
            () -> new TeyvatTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, TeyvatTableBlock> OTOGI_TABLE = BLOCKS.register("otogi_rakushi_square_table",
            () -> new TeyvatTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, TeyvatStoolBlock> OTOGI_STOOL = BLOCKS.register("otogi_rakushi_square_stool",
            () -> new TeyvatStoolBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, TeyvatStoolBlock> YUMEMIRU_STOOL = BLOCKS.register("square_yumemiru_stool",
            () -> new TeyvatStoolBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, TeyvatFloorLampBlock> SANDBEARER_FLOOR_LAMP = BLOCKS.register("liuli_pavilion_sandbearer_floor_lamp",
            () -> new TeyvatFloorLampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, com.soybe.awakeningsystem.guild.GuildHuntingBoardBlock> GUILD_HUNTING_BOARD = BLOCKS.register("guild_hunting_board",
            () -> new com.soybe.awakeningsystem.guild.GuildHuntingBoardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final java.util.Map<String, DeferredHolder<Block, PlushieBlock>> PLUSHIE_BLOCKS = registerAllPlushieBlocks();

    private static java.util.Map<String, DeferredHolder<Block, PlushieBlock>> registerAllPlushieBlocks() {
        java.util.Map<String, DeferredHolder<Block, PlushieBlock>> map = new java.util.LinkedHashMap<>();
        for (String id : PLUSHIE_IDS) {
            DeferredHolder<Block, PlushieBlock> holder = BLOCKS.register(id, () -> new PlushieBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOL)
                            .strength(0.5F, 1.0F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            ));
            map.put(id, holder);
        }
        return map;
    }

    public static Block[] getPlushieBlocksArray() {
        return PLUSHIE_BLOCKS.values().stream().map(DeferredHolder::get).toArray(Block[]::new);
    }
}

