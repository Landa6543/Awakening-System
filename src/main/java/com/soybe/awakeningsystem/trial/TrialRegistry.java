package com.soybe.awakeningsystem.trial;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Bloques, objetos y block entities de las Cámaras de Desafío, en sus propios registros. */
public final class TrialRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AwakeningSystemMod.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, AwakeningSystemMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AwakeningSystemMod.MOD_ID);

    // Irrompibles en supervivencia, como los generadores de prueba de Minecraft
    public static final DeferredHolder<Block, TrialSpawnerBlock> SHADOW_TRIAL_SPAWNER = BLOCKS.register("shadow_trial_spawner",
            () -> new TrialSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F).sound(SoundType.TRIAL_SPAWNER).noOcclusion()
                    .lightLevel(s -> s.getValue(TrialSpawnerBlock.ACTIVE) ? 12 : 5)));
    public static final DeferredHolder<Block, TrialVaultBlock> AWAKENING_VAULT = BLOCKS.register("awakening_vault",
            () -> new TrialVaultBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(-1.0F, 3600000.0F).sound(SoundType.VAULT).lightLevel(s -> 6)));

    // Barrera de fuerza: encierra a los cazadores en la sala mientras dura la prueba
    public static final DeferredHolder<Block, net.minecraft.world.level.block.TransparentBlock> FORCE_BARRIER = BLOCKS.register("trial_force_barrier",
            () -> new net.minecraft.world.level.block.TransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).noOcclusion().noLootTable()
                    .lightLevel(s -> 10).isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isRedstoneConductor((s, l, p) -> false)));
    public static final DeferredHolder<Block, RuneArrowTrapBlock> RUNE_ARROW_TRAP = BLOCKS.register("rune_arrow_trap",
            () -> new RuneArrowTrapBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                    .strength(30.0F, 1200.0F).sound(SoundType.DEEPSLATE_BRICKS).requiresCorrectToolForDrops().lightLevel(s -> 3)));
    public static final DeferredHolder<Block, AnemoVentBlock> ANEMO_VENT = BLOCKS.register("anemo_vent",
            () -> new AnemoVentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(-1.0F, 3600000.0F).sound(SoundType.COPPER_GRATE).lightLevel(s -> 8)));
    public static final DeferredHolder<Block, Block> RUNIC_ABYSSAL_STONE = BLOCKS.register("runic_abyssal_stone",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                    .strength(3.0F, 9.0F).sound(SoundType.DEEPSLATE_BRICKS).requiresCorrectToolForDrops().lightLevel(s -> 5)));

    public static final DeferredHolder<Item, BlockItem> FORCE_BARRIER_ITEM = ITEMS.register("trial_force_barrier",
            () -> new BlockItem(FORCE_BARRIER.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, BlockItem> RUNE_ARROW_TRAP_ITEM = ITEMS.register("rune_arrow_trap",
            () -> new BlockItem(RUNE_ARROW_TRAP.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, BlockItem> ANEMO_VENT_ITEM = ITEMS.register("anemo_vent",
            () -> new BlockItem(ANEMO_VENT.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, BlockItem> RUNIC_ABYSSAL_STONE_ITEM = ITEMS.register("runic_abyssal_stone",
            () -> new BlockItem(RUNIC_ABYSSAL_STONE.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> SHADOW_TRIAL_SPAWNER_ITEM = ITEMS.register("shadow_trial_spawner",
            () -> new BlockItem(SHADOW_TRIAL_SPAWNER.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, BlockItem> AWAKENING_VAULT_ITEM = ITEMS.register("awakening_vault",
            () -> new BlockItem(AWAKENING_VAULT.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> TRIAL_KEY = ITEMS.register("awakening_trial_key",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.awakening_trial_key.desc"));
                }
            });

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrialSpawnerBlockEntity>> SPAWNER_ENTITY =
            BLOCK_ENTITIES.register("shadow_trial_spawner",
                    () -> BlockEntityType.Builder.of(TrialSpawnerBlockEntity::new, SHADOW_TRIAL_SPAWNER.get()).build(null));
    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrialVaultBlockEntity>> VAULT_ENTITY =
            BLOCK_ENTITIES.register("awakening_vault",
                    () -> BlockEntityType.Builder.of(TrialVaultBlockEntity::new, AWAKENING_VAULT.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RuneArrowTrapBlock.Entity>> ARROW_TRAP_ENTITY =
            BLOCK_ENTITIES.register("rune_arrow_trap",
                    () -> BlockEntityType.Builder.of(RuneArrowTrapBlock.Entity::new, RUNE_ARROW_TRAP.get()).build(null));
    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnemoVentBlock.VentEntity>> ANEMO_VENT_ENTITY =
            BLOCK_ENTITIES.register("anemo_vent",
                    () -> BlockEntityType.Builder.of(AnemoVentBlock.VentEntity::new, ANEMO_VENT.get()).build(null));

    private TrialRegistry() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
