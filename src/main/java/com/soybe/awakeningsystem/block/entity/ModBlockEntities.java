package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AwakeningSystemMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RouletteAltarBlockEntity>> ROULETTE_ALTAR =
            BLOCK_ENTITY_TYPES.register("roulette_altar", () ->
                    BlockEntityType.Builder.of(RouletteAltarBlockEntity::new, SystemBlocks.ROULETTE_ALTAR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishingTrapBlockEntity>> FISHING_BLOCK =
            BLOCK_ENTITY_TYPES.register("fishing_block", () ->
                    BlockEntityType.Builder.of(FishingTrapBlockEntity::new, SystemBlocks.FISHING_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LockedTreasureChestBlockEntity>> LOCKED_TREASURE_CHEST =
            BLOCK_ENTITY_TYPES.register("locked_treasure_chest", () ->
                    BlockEntityType.Builder.of(LockedTreasureChestBlockEntity::new,
                            SystemBlocks.LOCKED_TREASURE_CHEST.get(),
                            SystemBlocks.LOCKED_CHEST_OVERWORLD.get(),
                            SystemBlocks.LOCKED_CHEST_NETHER.get(),
                            SystemBlocks.LOCKED_CHEST_END.get(),
                            SystemBlocks.LOCKED_CHEST_TEYVAT.get(),
                            SystemBlocks.LOCKED_CHEST_ISEKAI.get(),
                            SystemBlocks.LOCKED_CHEST_MURIM.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity>> ABYSSAL_FURNACE =
            BLOCK_ENTITY_TYPES.register("abyssal_furnace", () ->
                    BlockEntityType.Builder.of((pos, state) -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity(pos, state, com.soybe.awakeningsystem.furnace.FurnaceTier.ABYSSAL),
                            SystemBlocks.ABYSSAL_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity>> MITHRIL_FURNACE =
            BLOCK_ENTITY_TYPES.register("mithril_furnace", () ->
                    BlockEntityType.Builder.of((pos, state) -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity(pos, state, com.soybe.awakeningsystem.furnace.FurnaceTier.MITHRIL),
                            SystemBlocks.MITHRIL_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity>> ADAMANTITE_FURNACE =
            BLOCK_ENTITY_TYPES.register("adamantite_furnace", () ->
                    BlockEntityType.Builder.of((pos, state) -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity(pos, state, com.soybe.awakeningsystem.furnace.FurnaceTier.ADAMANTITE),
                            SystemBlocks.ADAMANTITE_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity>> SPIRIT_JADE_FURNACE =
            BLOCK_ENTITY_TYPES.register("spirit_jade_furnace", () ->
                    BlockEntityType.Builder.of((pos, state) -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity(pos, state, com.soybe.awakeningsystem.furnace.FurnaceTier.SPIRIT_JADE),
                            SystemBlocks.SPIRIT_JADE_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity>> ASTRAL_FURNACE =
            BLOCK_ENTITY_TYPES.register("astral_furnace", () ->
                    BlockEntityType.Builder.of((pos, state) -> new com.soybe.awakeningsystem.furnace.CustomTierFurnaceBlockEntity(pos, state, com.soybe.awakeningsystem.furnace.FurnaceTier.ASTRAL),
                            SystemBlocks.ASTRAL_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DragonNestBlockEntity>> DRAGON_NEST =
            BLOCK_ENTITY_TYPES.register("dragon_nest", () ->
                    BlockEntityType.Builder.of(DragonNestBlockEntity::new, SystemBlocks.DRAGON_NEST.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AncientDragonEggBlockEntity>> ANCIENT_DRAGON_EGG =
            BLOCK_ENTITY_TYPES.register("ancient_dragon_egg", () ->
                    BlockEntityType.Builder.of(AncientDragonEggBlockEntity::new,
                            SystemBlocks.ANCIENT_DRAGON_EGG_FIRE.get(),
                            SystemBlocks.ANCIENT_DRAGON_EGG_ICE.get(),
                            SystemBlocks.ANCIENT_DRAGON_EGG_FOREST.get(),
                            SystemBlocks.ANCIENT_DRAGON_EGG_SHADOW.get(),
                            SystemBlocks.ANCIENT_DRAGON_EGG_LIGHTNING.get(),
                            SystemBlocks.ANCIENT_DRAGON_EGG_CELESTIAL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SlotMachineBlockEntity>> SLOT_MACHINE =
            BLOCK_ENTITY_TYPES.register("slot_machine", () ->
                    BlockEntityType.Builder.of(SlotMachineBlockEntity::new, SystemBlocks.SLOT_MACHINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlushieBlockEntity>> PLUSHIE =
            BLOCK_ENTITY_TYPES.register("plushie", () ->
                    BlockEntityType.Builder.of(PlushieBlockEntity::new, SystemBlocks.getPlushieBlocksArray()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SubwayDoorBlockEntity>> SUBWAY_DOOR =
            BLOCK_ENTITY_TYPES.register("subway_door", () ->
                    BlockEntityType.Builder.of(SubwayDoorBlockEntity::new, SystemBlocks.SUBWAY_DOOR.get()).build(null));
}
